package com.ldangelo.corunabuswear.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.ldangelo.corunabuswear.R
import com.ldangelo.corunabuswear.data.viewmodels.StopViewModel
import com.ldangelo.corunabuswear.ui.fragment.components.BusEntry
import com.ldangelo.corunabuswear.ui.fragment.components.BusStopHeader

@Composable
fun BusStopFragment(stop: StopViewModel) {
    val columnPadding = PaddingValues(
        top = 6.dp,
        bottom = 0.dp,
        start = 10.dp,
        end = 10.dp
    )
    val scrollState = ScalingLazyListState()
    val buses by stop.buses.collectAsState()
    val apiWasCalled by stop.apiWasCalled.collectAsState()
    
    // Remember the bus stop data to avoid recomposing the header unnecessarily
    val busStopData = remember(stop.id, stop.name) { stop.toBusStop() }

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
            BusStopHeader(busStopData, scrollState)
            
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .absoluteOffset(y = 10.dp),
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(3.dp),
                anchorType = ScalingLazyListAnchorType.ItemStart,
                contentPadding = columnPadding,
                rotaryScrollableBehavior = null,
            ) {
                if (buses.isNotEmpty()) {
                    items(buses) { bus ->
                        BusEntry(bus)
                    }
                }
                else if (apiWasCalled) {
                    item {
                        Text(
                            text = stringResource(R.string.no_buses_found),
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
                    item {
                        Text(
                            text = stringResource(R.string.loading_buses),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 14.dp),
                            color = MaterialTheme.colors.onSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
