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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.ldangelo.corunabuswear.R
import com.ldangelo.corunabuswear.data.viewmodels.StopViewModel
import com.ldangelo.corunabuswear.ui.fragment.components.BusEntry
import com.ldangelo.corunabuswear.ui.fragment.components.BusStopHeader
import com.ldangelo.corunabuswear.ui.fragment.components.CenteredText

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

//    if (!apiWasCalled) {
//        Column(
//            modifier = Modifier.fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(0.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Box(
//                modifier = Modifier.
//                fillMaxWidth()
//                    .align(Alignment.CenterHorizontally)
//                ,
//                contentAlignment = Alignment.TopCenter
//            ) {
//                BusStopHeader(stop.toBusStop(), scrollState)
//                CenteredText(
//                    text = stringResource(R.string.loading_buses),
//                    color = MaterialTheme.colors.onSecondary
//                )
//            }
//            }
//    }

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
            BusStopHeader(stop.toBusStop(), scrollState)
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
                rotaryScrollableBehavior = null,
            ) {
                if (buses.isNotEmpty()) {
                    for (bus in buses) {
                        item {
                            BusEntry(bus)
                        }
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



