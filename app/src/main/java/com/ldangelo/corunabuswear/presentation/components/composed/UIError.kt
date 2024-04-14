package com.ldangelo.corunabuswear.presentation.components.composed

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Text
import com.ldangelo.corunabuswear.data.AppConstants.DEBUG
import com.ldangelo.corunabuswear.presentation.theme.wearColorPalette

@Composable
private fun ShowText(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = wearColorPalette.primary,
            text = text
        )
    }
}

@Composable
fun UpdateUIError(text: String, realError: String = "") {
    if (DEBUG) {
        ShowText(text + "\n" + realError)
    } else {
        ShowText(text)
    }

}