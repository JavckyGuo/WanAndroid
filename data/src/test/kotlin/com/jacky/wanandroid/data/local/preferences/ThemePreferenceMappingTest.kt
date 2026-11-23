package com.jacky.wanandroid.data.local.preferences

import com.jacky.wanandroid.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

/** 验证主题枚举与 DataStore 稳定字符串之间的双向映射和容错回退。 */
class ThemePreferenceMappingTest {
    @Test
    fun `missing and unknown values fall back to system`() {
        assertEquals(ThemeMode.SYSTEM, themeModeFromStorage(null))
        assertEquals(ThemeMode.SYSTEM, themeModeFromStorage("unknown"))
        assertEquals(ThemeMode.SYSTEM, themeModeFromStorage(""))
    }

    @Test
    fun `stored values map to all theme modes`() {
        assertEquals(ThemeMode.SYSTEM, themeModeFromStorage("system"))
        assertEquals(ThemeMode.LIGHT, themeModeFromStorage("light"))
        assertEquals(ThemeMode.DARK, themeModeFromStorage("dark"))
        assertEquals(ThemeMode.DARK, themeModeFromStorage("DARK"))
    }

    @Test
    fun `all theme modes have stable storage values`() {
        assertEquals("system", ThemeMode.SYSTEM.toStorageValue())
        assertEquals("light", ThemeMode.LIGHT.toStorageValue())
        assertEquals("dark", ThemeMode.DARK.toStorageValue())
    }
}
