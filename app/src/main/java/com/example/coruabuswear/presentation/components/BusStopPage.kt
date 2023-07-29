package com.example.coruabuswear.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.example.coruabuswear.data.models.Bus
import com.example.coruabuswear.data.models.BusStop
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme

@Composable
fun BusStopPage(stop: BusStop) {
    val columnPadding = PaddingValues(
        top = 2.dp,
        bottom = 12.dp,
        start = 0.dp,
        end = 0.dp
    )
    val redRectHeight = 60.dp
    val stopNameRectHeight = 38.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(redRectHeight)
                .background(MaterialTheme.colors.primary)

        ) {
            Box(
                modifier = Modifier
                    .height(stopNameRectHeight)
                    .fillMaxWidth(0.55f)
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 1.dp),

            ) {
                AutoResizingText(
                    modifier = Modifier
                        .fillMaxSize(),
                    text = stop.name,
                    color = MaterialTheme.colors.onPrimary,
                    targetTextSize = 15.sp,
                    maxLines = 2,
                    fontWeight = FontWeight.W500,
                )
            }
        }
            Text(
                text = "${stop.distance} m",
                modifier = Modifier,
                color = MaterialTheme.colors.onSecondary,
                textAlign = TextAlign.Center,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
            )

        //Last updated Text (some timer from when this function is called?)

        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(columnPadding),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            for (bus in stop.buses) {
                item {
                    BusListElement(bus)
                }
            }
        }
    }
}

@Composable
fun BusListElement(bus: Bus) {
    Text(
        text = bus.line.name,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center
    )
}