package com.ldangelo.corunabuswear.ui.fragment.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.ldangelo.corunabuswear.data.model.BusStop
import java.util.Locale
import kotlin.math.max

private fun String.toTitleCase(): String {
    return try {
        this.lowercase().split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    } catch (e: Exception) {
        this
    }
}

@Composable
fun BusStopHeader(stop: BusStop, scrollState: ScalingLazyListState) {
    val redRectHeight = 62.dp
    val stopNameRectHeight = 38.dp
    val stopNameRectWidth = 0.58f
    val scrollItemSize = 32f
    val scrollFractionIndex = ((max(scrollState.centerItemIndex, 0)) +
            ((scrollState.centerItemScrollOffset + scrollItemSize) / (scrollItemSize * 2f)) - 0.5f)

    val scrollFadingIndex = 2f
    val headerAlpha by animateFloatAsState(
        targetValue = (scrollFadingIndex - (scrollFractionIndex)).coerceIn(0f, 0.999f), label = "headerAlpha",
    )
    val headerOffset by animateDpAsState(
        targetValue = (max(((scrollFractionIndex - 1) * scrollItemSize), 0f)).dp, label = "HeaderOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(redRectHeight)
            .offset(y = -headerOffset)
            .background(MaterialTheme.colors.primary.copy(alpha = headerAlpha))
            .clip(GenericShape { size, _ ->
                lineTo(size.width, 0f)
                lineTo(size.width, Float.MAX_VALUE)
                lineTo(0f, Float.MAX_VALUE)
            })
            .shadow(
                (8 * headerAlpha).dp,
                spotColor = MaterialTheme.colors.primary.copy(alpha = headerAlpha),
                ambientColor = MaterialTheme.colors.primary.copy(alpha = headerAlpha),
            )
            .alpha(headerAlpha),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Bottom)
                    .weight((1 - stopNameRectWidth) / 2)
                    .padding(end = 3.dp, bottom = 2.dp)
            ) {
                Text(
                    text = "${stop.distance} m",
                    modifier = Modifier.align(Alignment.BottomEnd),
                    color = MaterialTheme.colors.onSecondary,
                    textAlign = TextAlign.Right,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier
                    .height(stopNameRectHeight)
                    .weight(stopNameRectWidth)
                    .align(Alignment.Bottom)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomCenter),
                    text = stop.name.toTitleCase(),
                    color = MaterialTheme.colors.onPrimary,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W500,
                )
            }
            Box(
                modifier = Modifier.weight((1 - stopNameRectWidth) / 2)
            ) {}
        }
    }
}
