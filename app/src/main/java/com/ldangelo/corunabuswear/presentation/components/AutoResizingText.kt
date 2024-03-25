package com.ldangelo.corunabuswear.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val textSize = remember {
        mutableStateOf(targetTextSize)
    }

    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontSize = textSize.value,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        fontWeight = fontWeight,
        onTextLayout = { textLayoutResult ->
            val lineIndex = textLayoutResult.lineCount - 1

            if (textLayoutResult.isLineEllipsized(lineIndex)) {
                textSize.value = textSize.value * 0.95f
            }
        }
    )
}