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

@Singleton
class EncryptedCookieJar @Inject constructor(@ApplicationContext context: Context) : CookieJar {
    private val prefs = context.getSharedPreferences("secure_session", Context.MODE_PRIVATE)
    private val baseUrl = "https://wanandroid.com/".toHttpUrl()
    private var cookies = readCookies().toMutableList()

    @Synchronized override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = System.currentTimeMillis()
        this.cookies.removeAll { old -> cookies.any { it.name == old.name && it.domain == old.domain && it.path == old.path } }
        this.cookies.addAll(cookies.filter { it.expiresAt > now })
        this.cookies.removeAll { it.expiresAt <= now }
        persist()
    }

    @Synchronized override fun loadForRequest(url: HttpUrl): List<Cookie> {
        cookies.removeAll { it.expiresAt <= System.currentTimeMillis() }
        return cookies.filter { it.matches(url) }
    }

    @Synchronized fun clear() {
        cookies.clear()
        prefs.edit().remove(DATA_KEY).apply()
    }

    private fun persist() = prefs.edit().putString(DATA_KEY, encrypt(cookies.joinToString("\n") { it.toString() })).apply()
    private fun readCookies(): List<Cookie> = runCatching {
        prefs.getString(DATA_KEY, null)?.let(::decrypt)?.lineSequence()
            ?.mapNotNull { Cookie.parse(baseUrl, it) }?.toList().orEmpty()
    }.getOrDefault(emptyList())

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
            generateKey()
        }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        return Base64.encodeToString(cipher.iv + cipher.doFinal(value.toByteArray()), Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val bytes = Base64.decode(value, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, 12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv)) }
        return String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)))
    }

    private companion object { const val KEY_ALIAS = "wanandroid_cookie_key"; const val DATA_KEY = "cookies" }
}
