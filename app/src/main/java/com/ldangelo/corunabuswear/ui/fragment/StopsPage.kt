package com.ldangelo.corunabuswear.ui.fragment

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.wear.compose.material.Text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import com.ldangelo.corunabuswear.data.ContextHolder
import com.ldangelo.corunabuswear.data.wearDatalayer.openSettings
import com.ldangelo.corunabuswear.data.models.Bus
import com.ldangelo.corunabuswear.data.viewmodels.BusStopViewModel
import com.ldangelo.corunabuswear.data.viewmodels.BusesViewModel
import kotlinx.coroutines.Dispatchers

// receives a list of stops
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StopsPage(stops: List<BusStopViewModel>, pagerState: PagerState, animationScope: CoroutineScope) {
    val columnPadding = PaddingValues(
        top = 6.dp,
        bottom = 0.dp,
        start = 10.dp,
        end = 10.dp
    )
    val scrollState = ScalingLazyListState()

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
                rotaryScrollableBehavior = null,
            ) {
                for ((index, stop) in stops.withIndex()) {
                    item {
                        StopListElement(stop, index + 1, pagerState, animationScope)
                    }
                }
                // some margin at the end
                item {
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxWidth()
                    )
                }
                item {
                    GearButton(onClick = {
                        val context = ContextHolder.getApplicationContext()
                        ContextHolder.getLifecycleScope().launch(Dispatchers.IO) {
                            openSettings(context)
                        }
                    })
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StopListElement(stop: BusStopViewModel, index: Int, pagerState: PagerState, animationScope: CoroutineScope) {

    Card (
        onClick =  {
            animationScope.launch {
                pagerState.scrollToPage(index)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp, horizontal = 0.dp),
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
                        repeatDelayMillis = 4000 + index * 1500,
                        initialDelayMillis = 1000 + index * 1500,
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
            BusStopsIconRow(stop, stop.buses)
        }
    }
}

@Composable
fun BusStopsIconRow(busStopViewModel: BusStopViewModel, buses: BusesViewModel) {
    val obsBuses : List<Bus> by buses.buses.observeAsState(emptyList())
    val distance : Int by busStopViewModel.distance.observeAsState(9999)
    val lines = obsBuses.map { it.line }.distinct()

    // For each line that stops at this stop, add a little icon that is a rectangle with the color of the line, with the line number inside in white
    // Stack them horizontally

    val diameter = 12.dp

    // Stack icons horizontally
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp),
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
                        .clip(RoundedCornerShape(diameter / 2))
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
            text = "$distance m",
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