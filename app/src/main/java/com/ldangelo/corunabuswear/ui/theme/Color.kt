package com.ldangelo.corunabuswear.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val RedTranvias = Color(0xfff34639)
val RedTranvias2 = Color(0xffc0372d)
val RedTranviasSecondary = Color(0xffe30c18)
val RedTranviasSecondary2 = Color(0xffb00912)
val Ash = Color(0xff4c4c4c)
val DarkerAsh = Color(0xff2f2f2f)

internal val wearColorPalette: Colors = Colors(
        primary = RedTranvias,
        primaryVariant = RedTranvias2,
        secondary = Color.White,
        secondaryVariant = Color.White,
        error = RedTranviasSecondary,
        onPrimary = Color.White,
        onSecondary = Ash,
        onError = RedTranviasSecondary2,
        background = Color.White,
        onBackground = DarkerAsh,
)