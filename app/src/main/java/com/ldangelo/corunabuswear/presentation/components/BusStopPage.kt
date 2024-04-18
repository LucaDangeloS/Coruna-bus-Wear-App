package com.ldangelo.corunabuswear.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.ldangelo.corunabuswear.data.models.Bus
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import com.ldangelo.corunabuswear.data.viewmodels.BusStopViewModel
import kotlin.math.max

@Composable
fun BusStopPage(stop: BusStopViewModel) {
    val columnPadding = PaddingValues(
        top = 6.dp,
        bottom = 0.dp,
        start = 10.dp,
        end = 10.dp
    )
    val scrollState = ScalingLazyListState()
    val buses: List<Bus> by stop.buses.buses.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.
                fillMaxWidth()
                .align(Alignment.CenterHorizontally)
            ,
            contentAlignment = Alignment.TopCenter
        ) {
            BusListHeader(stop, scrollState)
            //Last updated Text (some timer from when this function is called?) (Add in the pull refresh)
            //https://developer.android.com/reference/kotlin/androidx/compose/material/pullrefresh/package-summary
            // Add alternative implementation for square watches?
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .absoluteOffset(y = 10.dp),
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(3.dp),
                anchorType = ScalingLazyListAnchorType.ItemStart,
                contentPadding = columnPadding,
            ) {
                if (buses.isEmpty()) {
                    item {
                        Text(
                            text = "No hay buses :(",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 14.dp),
                            color = MaterialTheme.colors.onSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    for (bus in buses) {
                        item {
                            BusListElement(bus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusListHeader(stop: BusStopViewModel, scrollState: ScalingLazyListState) {
    val redRectHeight = 62.dp
    val stopNameRectHeight = 38.dp
    val stopNameRectWidth = 0.58f
    val scrollItemSize = 32f
    val scrollFractionIndex = ((max(scrollState.centerItemIndex, 0)) +
            ((scrollState.centerItemScrollOffset + scrollItemSize) / (scrollItemSize * 2f)) - 0.5f)

    val scrollFadingIndex = 2f
//    println("${scrollState.centerItemIndex} ${scrollState.centerItemScrollOffset}")
    val headerAlpha by animateFloatAsState(
        targetValue = (scrollFadingIndex - (scrollFractionIndex)).coerceIn(0f, 0.999f), label = "headerAlpha",
//            1 - (pagerState.currentPageOffsetFraction * 2).absoluteValue)
    )
    val headerOffset by animateDpAsState(
        targetValue = (max(((scrollFractionIndex - 1) * scrollItemSize), 0f)).dp, label = "HeaderOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(redRectHeight)
            .offset(y = -headerOffset)
            // Draw shadow only in the bottom and no shadow in the sides nor top
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
                    text = "${stop.distance.value} m",
                    modifier = Modifier
                        .align(Alignment.BottomEnd),
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
                AutoResizingText(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomCenter),
                    text = stop.name,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onPrimary,
                    targetTextSize = 15.sp,
                    maxLines = 2,
                    fontWeight = FontWeight.W500,
                )
            }
            Box(
                modifier = Modifier
                    .weight((1 - stopNameRectWidth) / 2)
            ) {}
        }
    }
}

@Composable
fun BusListElement(bus: Bus) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(bus.line.color, MaterialTheme.colors.background),
                    startX = 160f,
                    endX = 450f,
                    tileMode = androidx.compose.ui.graphics.TileMode.Clamp
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 14.dp)
                .clip(RoundedCornerShape(6.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = bus.line.name,
                modifier = Modifier,
                color = MaterialTheme.colors.onPrimary,
                textAlign = TextAlign.Left,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = bus.getRemainingTime(),
                modifier = Modifier,
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.Right,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}