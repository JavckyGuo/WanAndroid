package com.jacky.wanandroid.data.remote

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 持久化 WanAndroid 登录 Cookie 的 OkHttp CookieJar。
 *
 * Cookie 文本使用 Android Keystore 管理的 AES-GCM 密钥加密后才写入
 * SharedPreferences；应用不会保存用户的明文密码。
 */
@Singleton
class EncryptedCookieJar @Inject constructor(
    @ApplicationContext context: Context
) : CookieJar {
    private val prefs = context.getSharedPreferences("secure_session", Context.MODE_PRIVATE)
    private val baseUrl = "https://wanandroid.com/".toHttpUrl()
    private var cookies = readCookies().toMutableList()

    /**
     * 合并响应 Cookie，并按 name/domain/path 替换旧值。
     *
     * 所有读写均同步，避免并发网络请求互相覆盖内存与磁盘状态。
     */
    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = System.currentTimeMillis()
        this.cookies.removeAll { old ->
            cookies.any {
                it.name == old.name && it.domain == old.domain && it.path == old.path
            }
        }
        this.cookies.addAll(cookies.filter { it.expiresAt > now })
        this.cookies.removeAll { it.expiresAt <= now }
        persist()
    }

    /** 仅向匹配目标 URL 的请求附加未过期 Cookie。 */
    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        cookies.removeAll { it.expiresAt <= System.currentTimeMillis() }
        return cookies.filter { it.matches(url) }
    }

    /** 退出登录时同时清理内存 Cookie 与加密持久化数据。 */
    @Synchronized
    fun clear() {
        cookies.clear()
        prefs.edit().remove(DATA_KEY).apply()
    }

    private fun persist() {
        val serialized = cookies.joinToString("\n") { it.toString() }
        prefs.edit().putString(DATA_KEY, encrypt(serialized)).apply()
    }

    /**
     * 密钥失效、数据损坏或旧版本格式不兼容时返回空会话，
     * 不让启动阶段因解密异常崩溃。
     */
    private fun readCookies(): List<Cookie> = runCatching {
        prefs.getString(DATA_KEY, null)
            ?.let(::decrypt)
            ?.lineSequence()
            ?.mapNotNull { Cookie.parse(baseUrl, it) }
            ?.toList()
            .orEmpty()
    }.getOrDefault(emptyList())

    /** 获取已有 Keystore 密钥，首次运行时创建不可导出的 AES 密钥。 */
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        ).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generateKey()
        }
    }

    /** 将随机 IV 与密文拼接后编码为可持久化的 Base64 字符串。 */
    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, key())
        }
        return Base64.encodeToString(
            cipher.iv + cipher.doFinal(value.toByteArray()),
            Base64.NO_WRAP
        )
    }

    /** 从前 12 字节恢复 GCM IV，再校验并解密剩余数据。 */
    private fun decrypt(value: String): String {
        val bytes = Base64.decode(value, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, 12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(
                Cipher.DECRYPT_MODE,
                key(),
                GCMParameterSpec(128, iv)
            )
        }
        return String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)))
    }

    private companion object {
        const val KEY_ALIAS = "wanandroid_cookie_key"
        const val DATA_KEY = "cookies"
    }
}
