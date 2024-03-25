package com.ldangelo.corunabuswear

import android.content.res.Resources
import androidx.compose.ui.unit.Dp

fun Int.toDp(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Dp.toPx(): Int = (this.value * Resources.getSystem().displayMetrics.density).toInt()