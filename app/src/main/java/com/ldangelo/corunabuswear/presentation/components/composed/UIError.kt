package com.ldangelo.corunabuswear.presentation.components.composed

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Text
import com.ldangelo.corunabuswear.presentation.theme.wearColorPalette
import org.koin.android.BuildConfig

@Composable
private fun ShowText(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = wearColorPalette.primary,
        text = text
    )
}

@Composable
fun UpdateUIError(text: String, realError: String = "") {
    if (BuildConfig.DEBUG) {
        ShowText(text + "\n" + realError)
    } else {
        ShowText(text)
    }

}