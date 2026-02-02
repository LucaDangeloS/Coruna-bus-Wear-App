package com.ldangelo.corunabuswear.ui.fragment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeSpacing
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.ldangelo.corunabuswear.data.ContextHolder
import com.ldangelo.corunabuswear.data.model.Bus
import com.ldangelo.corunabuswear.data.model.BusLine
import com.ldangelo.corunabuswear.data.viewmodels.StopViewModel
import com.ldangelo.corunabuswear.data.wear.openSettings
import com.ldangelo.corunabuswear.ui.fragment.components.GearButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StopsPageFragment(stops: List<StopViewModel>, pagerState: PagerState, animationScope: CoroutineScope) {
    val columnPadding = PaddingValues(
        top = 6.dp,
        bottom = 0.dp,
        start = 10.dp,
        end = 10.dp
    )
    val scrollState = ScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        anchorType = ScalingLazyListAnchorType.ItemStart,
        contentPadding = columnPadding,
        rotaryScrollableBehavior = null,
    ) {
        itemsIndexed(stops) { index, stop ->
            StopListElement(stop, index + 1, pagerState, animationScope)
        }
        
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StopListElement(stop: StopViewModel, index: Int, pagerState: PagerState, animationScope: CoroutineScope) {
    val buses by stop.buses.collectAsState()
    val distance by stop.distance.collectAsState()
    
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
            BusStopsIconRow(distance, buses, index)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BusStopsIconRow(distance: Int, buses: List<Bus>, index: Int) {
    val lines = remember(buses) { buses.map { it.line }.distinct() }
    val diameter = 12.dp

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .basicMarquee(
                    spacing = MarqueeSpacing(20.dp),
                    repeatDelayMillis = 3000 + index * 1000,
                    initialDelayMillis = 2000 + index * 1000,
                ),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (line in lines) {
                BusLineIcon(line, diameter)
            }
        }
        Text(
            text = "$distance m",
            modifier = Modifier.padding(start = 4.dp),
            color = MaterialTheme.colors.onSecondary,
            textAlign = TextAlign.Left,
            fontSize = 7.sp,
            letterSpacing = 0.1.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun BusLineIcon(line: BusLine, diameter: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .height(diameter)
            .width(diameter)
            .clip(RoundedCornerShape(diameter / 2))
            .background(color = line.color)
    ) {
        Text(
            text = line.name,
            modifier = Modifier.align(Alignment.Center),
            fontSize = 5.sp,
            color = MaterialTheme.colors.onPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontWeight = FontWeight.W500,
        )
    }
}