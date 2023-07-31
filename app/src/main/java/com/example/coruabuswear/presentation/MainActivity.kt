/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.coruabuswear.presentation

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PageIndicatorState
import com.example.coruabuswear.presentation.theme.CoruñaBusWearTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.example.coruabuswear.data.ApiConstants.BUS_API_FETCH_TIME
import com.example.coruabuswear.data.ContextHolder.setApplicationContext
import com.example.coruabuswear.data.local.clearAllSharedPreferences
import com.example.coruabuswear.data.local.saveBusLine
import com.example.coruabuswear.data.local.saveBusStop
import com.example.coruabuswear.data.models.BusStop
import com.example.coruabuswear.data.providers.BusProvider
import com.example.coruabuswear.data.providers.BusProvider.fetchBuses
import com.example.coruabuswear.data.providers.BusProvider.fetchStops
import com.example.coruabuswear.data.providers.LocationProvider.startRegularLocationUpdates
import com.example.coruabuswear.presentation.components.BusStopPage
import com.example.coruabuswear.presentation.theme.wearColorPalette
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private var definitionsUpdated = false
    private lateinit var locationListener: LocationCallback
    private var executor = Executors.newSingleThreadScheduledExecutor()
    private var busTaskScheduler: ScheduledFuture<*>? = null
    var busStops: List<BusStop> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApplicationContext(this)
        updateUILoading("Obteniendo localización...")

        locationListener = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                updateUILoading("Obteniendo paradas...")
                // Handle location updates here
                val _location = locationResult.lastLocation
                if (_location != null) {
                    updateUIWithStops(_location)
                } else {
                    Log.d("DEBUG_TAG", "Location is null")
                }
            }
        }
        startRegularLocationUpdates(this@MainActivity, locationListener)
    }

    private fun updateUIError() {
        displayContent {
            WearApp("ERROR!")
        }
    }

    private fun updateUINoLocation() {
        displayContent {
            WearApp("No location")
        }
    }

    private suspend fun <T> retryUpdateDefinitions(function: suspend () -> T, context: Context): T {
        try {
            return function()
        } catch (e: Exception) {
            if (definitionsUpdated) {
                throw e
            }
            definitionsUpdated = true
            updateUILoading("Actualizando índice...")
            Log.d("DEBUG_TAG", "Updating Bus definitions")
            val (stops, lines) = BusProvider.fetchStopsLinesData()
            clearAllSharedPreferences(context)
            Log.d("DEBUG_TAG", "$stops \n $lines")
            stops.forEach { busStop ->
                saveBusStop(context, busStop.id.toString(), busStop)
            }
            lines.forEach { busLine ->
                saveBusLine(context, busLine.id.toString(), busLine)
            }
            Log.d("DEBUG_TAG", "Updated!")
        }
        return function()
    }

    private fun startRegularBusUpdates() {
        Log.d("DEBUG_TAG", "Starting regular bus updates")
        busTaskScheduler = executor.scheduleAtFixedRate({
            updateUIWithBuses()
        }, 0, BUS_API_FETCH_TIME, TimeUnit.MILLISECONDS)
    }

    private fun updateUIWithStops(location: Location) {
        Log.d("DEBUG_TAG", "Update UI method called")
        // https://developer.android.com/topic/libraries/architecture/workmanager
        busTaskScheduler?.cancel(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
            busStops = retryUpdateDefinitions ({
                fetchStops(location.latitude, location.longitude, 300, 3)
            }, this@MainActivity)
//                withContext(Dispatchers.Main) {
                    startRegularBusUpdates()
//                }
            } catch (e: Exception) {
                Log.d("ERROR_TAG", "Error fetching stops: $e")
                withContext(Dispatchers.Main) {
                    updateUIError()
                }
            }
        }
    }

    private fun updateUIWithBuses() {
        Log.d("DEBUG_TAG", "Update UI with buses method called")
        // call th API to get the buses in the stops
        lifecycleScope.launch(Dispatchers.IO) {
            for (stop in busStops) {
                retryUpdateDefinitions ({
                    stop.updateBuses(fetchBuses(stop.id))
                }, this@MainActivity)
//                    stop.updateBuses(mockBusApi(this@MainActivity))
            }
            withContext(Dispatchers.Main) {
                displayContent {
                    WearApp(busStops)
                }
            }
        }
    }

    private fun updateUILoading(loadingText: String? = null) {
        displayContent {
            val endText = ""
            Scaffold (
                timeText = {
                    TimeText(
                        modifier = Modifier.padding(2.dp),
                        // reduce font
                        timeTextStyle =
                            TextStyle(
                                fontSize = 12.sp,
                                color = MaterialTheme.colors.onSecondary,
                            )
                        ,
//                        endLinearContent = {
//                            Text(
//                                text = endText,y
//                                color = MaterialTheme.colors.onBackground
//                            )y
//                        },
//                        endCurvedContent = {
//                            basicCurvedText(
//                                endText,
//                                style = {
//                                    CurvedTextStyle(
//                                        fontSize = 12.sp,
//                                        color = MaterialTheme.colors.onSecondary,
//                                    )
//                                },
//                            )
//                        }
                    )
                },
                vignette = {
                    Vignette(vignettePosition = VignettePosition.TopAndBottom)
                }
            ) {
            Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Box(modifier = Modifier.fillMaxSize().align(Alignment.CenterHorizontally)) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                            indicatorColor = wearColorPalette.primary,
                            trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.3f),
                            strokeWidth = 6.dp
                        )
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = loadingText ?: "",
                            color = MaterialTheme.colors.onBackground,
                        )
                    }
                }
            }
        }
    }

    private fun displayContent(composable: @Composable () -> Unit) {
        setContent {
            CoruñaBusWearTheme {
                composable()
            }
        }
    }
}

@Composable
fun WearApp(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShowText(text)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WearApp(busStops: List<BusStop>) {
    val currentPageIndex by remember { mutableStateOf(0) }
    val maxPages = busStops.size
    val pagerState = rememberPagerState(initialPage = currentPageIndex)
    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = 0f
            override val selectedPage: Int
                get() = pagerState.currentPage
            override val pageCount: Int
                get() = maxPages
        }
    }

    Scaffold (
        pageIndicator = {
            HorizontalPageIndicator(
                pageIndicatorState = pageIndicatorState,
                selectedColor = MaterialTheme.colors.primary,
                unselectedColor = MaterialTheme.colors.onSecondary.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            )
        },
        timeText = {
            TimeText(
                timeTextStyle = TextStyle(
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onSecondary,
                ),

            )
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        }
            ) {
        HorizontalPager(
            maxPages,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(bottom = 3.dp)
        ) { page ->
            BusStopPage(busStops[page], pagerState)
        }
    }
}

@Composable
fun ShowText(text: String) {
    Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = wearColorPalette.primary,
            text = text
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}