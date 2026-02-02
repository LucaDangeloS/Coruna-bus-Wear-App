package com.ldangelo.corunabuswear.ui.fragment.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.wear.compose.material.Text

@Composable
fun AutoResizingText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    targetTextSize: TextUnit,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight
) {
    // Remember the size for this specific text to avoid loops during scrolling
    var textSize by remember(text) {
        mutableStateOf(targetTextSize)
    }

    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontSize = textSize,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        fontWeight = fontWeight,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                // Only decrease if it actually overflows, and keep it stable
                val nextSize = textSize * 0.9f
                if (nextSize.value > 2f) { // Minimum readable size
                    textSize = nextSize
                }
            }
        }
    )
}