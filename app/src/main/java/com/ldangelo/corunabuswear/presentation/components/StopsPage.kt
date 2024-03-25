package com.ldangelo.corunabuswear.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListAnchorType
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import com.ldangelo.corunabuswear.data.models.BusLine
import com.ldangelo.corunabuswear.data.models.BusStop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// receives a list of stops
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StopsPage(stops: List<BusStop>, pagerState: PagerState, animationScope: CoroutineScope) {
    val columnPadding = PaddingValues(
        top = 6.dp,
        bottom = 0.dp,
        start = 10.dp,
        end = 10.dp
    )
    val scrollState: ScalingLazyListState = rememberScalingLazyListState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
            ,
            contentAlignment = Alignment.TopCenter
        ) {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(3.dp),
                anchorType = ScalingLazyListAnchorType.ItemStart,
                contentPadding = columnPadding,
            ) {
                for ((index, stop) in stops.withIndex()) {
                    item {
                        StopListElement(stop, index + 1, pagerState, animationScope)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StopListElement(stop: BusStop, index: Int, pagerState: PagerState, animationScope: CoroutineScope) {
    val lines = stop.buses.map { it.line }.distinct()

    Card (
        onClick =  {
            animationScope.launch {
                // TODO: Make animation work?
                pagerState.scrollToPage(index)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp, horizontal = 0.dp),
        // TODO: change background to better gradient
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = MaterialTheme.colors.primary,
            endBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.6f),
            gradientDirection = LayoutDirection.Ltr,
        ),
    ) {
        Column {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .basicMarquee(
                        delayMillis = 1000,
                        initialDelayMillis = 1500,
                    ),
                text = stop.name,
                color = MaterialTheme.colors.onSecondary,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W500,
            )
            // add little icons
            BusStopsIconRow(stop, lines)
        }
    }
}

@Composable
fun BusStopsIconRow(busStop: BusStop, lines: List<BusLine>) {
    // For each line that stops at this stop, add a little icon that is a rectangle with the color of the line, with the line number inside in white
    // Stack them horizontally

    val diameter = 12.dp

    // Stack icons horizontally
    Row (
        modifier = Modifier.fillMaxWidth().padding(start = 5.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        ) {
            for (line in lines) {
                Box(
                    modifier = Modifier
                        .height(diameter)
                        .width(diameter)
                        .clip(RoundedCornerShape(diameter/2))
                        .background(
                            color = line.color,
                        )
                )
                {
                    AutoResizingText(
                        text = line.name,
                        modifier = Modifier
                            .align(Alignment.Center),
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        targetTextSize = 5.sp,
                        fontWeight = FontWeight.W500,
                    )
                }
            }
        }
        Text(
            text = busStop.distance.toString() + " m",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Bottom),
            color = MaterialTheme.colors.onSecondary,
            textAlign = TextAlign.Left,
            fontSize = 7.sp,
            letterSpacing = 0.1.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}