package com.jacky.wanandroid.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Signal Desk 视觉体系使用的共享品牌色。 */
val SignalCyan = Color(0xFF00A39A)
val CompileAmber = Color(0xFFF2B134)
val Ink = Color(0xFF101828)
val Paper = Color(0xFFF7F8FA)
val Slate = Color(0xFF667085)
val Night = Color(0xFF0B1220)
val NightSurface = Color(0xFF131D2E)

private val LightColors = lightColorScheme(
    primary = SignalCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2F5F1),
    onPrimaryContainer = Color(0xFF003734),
    secondary = Ink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE6F2),
    onSecondaryContainer = Ink,
    tertiary = CompileAmber,
    onTertiary = Color(0xFF3E2B00),
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE9EDF2),
    onSurfaceVariant = Slate,
    outline = Color(0xFFCBD2DC),
    outlineVariant = Color(0xFFE1E6ED)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF50D8CF),
    onPrimary = Color(0xFF003734),
    primaryContainer = Color(0xFF00504B),
    onPrimaryContainer = Color(0xFF9CF2EA),
    secondary = Color(0xFFD7E2F4),
    onSecondary = Night,
    secondaryContainer = Color(0xFF253248),
    onSecondaryContainer = Color(0xFFE1E9F6),
    tertiary = Color(0xFFFFCE72),
    onTertiary = Color(0xFF432F00),
    background = Night,
    onBackground = Color(0xFFE6EBF2),
    surface = NightSurface,
    onSurface = Color(0xFFE6EBF2),
    surfaceVariant = Color(0xFF202B3D),
    onSurfaceVariant = Color(0xFFB5BFCE),
    outline = Color(0xFF566174),
    outlineVariant = Color(0xFF2C384B)
)

private val WanTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.8).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 35.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 23.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.4.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.6.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.8.sp)
)

/** 向全应用提供固定的浅色/深色色板与排版，不启用动态色。 */
@Composable
fun WanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WanTypography,
        content = content,
    )
}
