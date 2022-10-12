package com.tools.hamzabm.dataexp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(0xffBC204C),
    primaryVariant = Color.White,
    secondary =  Color.White,

)

private val LightColorPalette = lightColors(
    primary = Color(0xffBC204C),
    primaryVariant = Color(0xffe12952),
    secondary =  Color(0xff20bc90),
    onPrimary = Color.White,
    surface = Color.White,
    background = Color.White,
    onSecondary = Color.Black
)

@Composable
fun InternalDataExplorerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}