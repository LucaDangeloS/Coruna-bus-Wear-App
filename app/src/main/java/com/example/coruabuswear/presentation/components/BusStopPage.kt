package com.example.coruabuswear.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.example.coruabuswear.data.models.Bus
import com.example.coruabuswear.data.models.BusStop
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun BusStopPage(stop: BusStop) {
    val lazyListState = rememberLazyListState()
    val paddingValues = PaddingValues(
        top = 5.dp,
        bottom = 0.dp,
        start = 0.dp,
        end = 0.dp
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(0.55f),
        ) {
            AutoResizingText(
                modifier = Modifier
                    .fillMaxSize(),
                text = stop.name,
                color = Color.White,
                targetTextSize = 15.sp,
                maxLines = 2
            )
        }
            Text(
                text = "${stop.distance} m",
                modifier = Modifier,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
            )

        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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