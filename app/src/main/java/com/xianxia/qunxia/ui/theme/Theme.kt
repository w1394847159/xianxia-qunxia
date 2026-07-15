package com.xianxia.qunxia.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 修仙主题色系
val JiangHuBlack = Color(0xFF1A1A2E)
val JiangHuDarkBlue = Color(0xFF16213E)
val JiangHuNavy = Color(0xFF0F3460)
val JiangHuGold = Color(0xFFD4A853)
val JiangHuRed = Color(0xFFE94560)
val JiangHuJade = Color(0xFF4CAF7A)
val JiangHuWhite = Color(0xFFEEEEEE)
val JiangHuGray = Color(0xFF888888)
val JiangHuPaper = Color(0xFFF5F0E8)

private val DarkColorScheme = darkColorScheme(
    primary = JiangHuGold,
    secondary = JiangHuJade,
    tertiary = JiangHuRed,
    background = JiangHuBlack,
    surface = JiangHuDarkBlue,
    onPrimary = JiangHuBlack,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = JiangHuWhite,
    onSurface = JiangHuWhite,
    outline = JiangHuNavy
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8B6F3A),
    secondary = JiangHuJade,
    tertiary = JiangHuRed,
    background = JiangHuPaper,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF2C2C2C),
    onSurface = Color(0xFF2C2C2C),
    outline = Color(0xFFD0D0D0)
)

@Composable
fun XianXiaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
