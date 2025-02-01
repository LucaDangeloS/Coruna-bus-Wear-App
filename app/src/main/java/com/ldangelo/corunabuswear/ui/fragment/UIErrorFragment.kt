package com.ldangelo.corunabuswear.ui.fragment

import androidx.compose.runtime.Composable
import com.ldangelo.corunabuswear.data.AppConstants.DEBUG
import com.ldangelo.corunabuswear.ui.fragment.components.CenteredText

@Composable
fun UIErrorFragment(text: String, realError: String = "") {
    if (DEBUG) {
        CenteredText(text + "\n" + realError)
    } else {
        CenteredText(text)
    }

}