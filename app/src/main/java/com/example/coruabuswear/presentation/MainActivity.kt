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
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.wear.ambient.AmbientModeSupport
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
import com.example.coruabuswear.R
import com.example.coruabuswear.data.ApiConstants.BUS_API_FETCH_TIME
import com.example.coruabuswear.data.ContextHolder.setApplicationContext
import com.example.coruabuswear.data.local.clearAllSharedPreferences
import com.example.coruabuswear.data.local.saveBusConnection
import com.example.coruabuswear.data.local.saveBusLine
import com.example.coruabuswear.data.local.saveBusStop
import com.example.coruabuswear.data.local.saveLog
import com.example.coruabuswear.data.models.BusStop
import com.example.coruabuswear.data.providers.BusProvider
import com.example.coruabuswear.data.providers.BusProvider.fetchBuses
import com.example.coruabuswear.data.providers.BusProvider.fetchStops
import com.example.coruabuswear.data.providers.BusProvider.mockBusApi
import com.example.coruabuswear.data.providers.LocationProvider.startRegularLocationUpdates
import com.example.coruabuswear.data.providers.PermissionNotGrantedException
import com.example.coruabuswear.presentation.components.BusStopPage
import com.example.coruabuswear.presentation.components.StopsPage
import com.example.coruabuswear.presentation.theme.wearColorPalette
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {
    private var definitionsUpdated = false
    private lateinit var locationListener: LocationCallback
    private var executor = Executors.newSingleThreadScheduledExecutor()
    private var busTaskScheduler: ScheduledFuture<*>? = null
    private var busStops: List<BusStop> = mutableListOf()
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    // Ambient functionality, that for some reason... doesn't work in my watch
//    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback {
//        val ambientCallback = object : AmbientModeSupport.AmbientCallback() {
//            override fun onEnterAmbient(ambientDetails: Bundle?) {
//                println("ENTER AMBIENT")
//            }
//
//            override fun onExitAmbient() {
//                println("EXIT AMBIENT")
//            }
//        }
//        return ambientCallback
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApplicationContext(this)
        ambientController = AmbientModeSupport.attach(this)
        ambientController.setAutoResumeEnabled(true)

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
        startRegularLocationUpdates(locationListener, this@MainActivity)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) {
            Log.d("DEBUG_TAG", "Request cancelled")
            updateUIErrorAPI("Permiso de localización no concedido")
            return
        } else {
            for (result in grantResults) {
                if (result != PERMISSION_GRANTED) {
                    Log.d("DEBUG_TAG", "Permission not granted")
                    updateUIErrorAPI("Permiso de localización no concedido")
                    return
                }
            }
        }
    }

    private suspend fun <T> retryUpdateDefinitions(function: suspend () -> T, context: Context): T {
        try {
            return function()
        } catch (e: BusProvider.UnknownDataException) {
            if (definitionsUpdated) {
                // Store in a log file
                saveLog(context, e.toString())
                Log.d("ERROR_TAG", e.toString())
                throw e
            }
            definitionsUpdated = true
            updateUILoading("Actualizando índice...")
            Log.d("DEBUG_TAG", "Updating Bus definitions")
            val (stops, lines, connections) = BusProvider.fetchStopsLinesData()
            clearAllSharedPreferences(context)
            stops.forEach { busStop ->
                saveBusStop(context, busStop.id.toString(), busStop)
            }
            lines.forEach { busLine ->
                saveBusLine(context, busLine.id.toString(), busLine)
            }
            connections.forEach { busLine ->
                saveBusConnection(context, busLine.id.toString(), busLine)
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

    private fun updateUIErrorAPI(text: String) {
        displayContent {
            WearApp(text)
        }
    }

    // Later switch to changing to first tab
    private fun updateUINoStops() {
        displayContent {
            WearApp("No hay paradas cercanas")
        }
    }

    private fun updateUIWithStops(location: Location) {
        Log.d("DEBUG_TAG", "Update UI method called")
        busTaskScheduler?.cancel(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                busStops = retryUpdateDefinitions ({
                    fetchStops(location.latitude, location.longitude, 300, 3)
                }, this@MainActivity)
                startRegularBusUpdates()
            } catch (e: BusProvider.TooManyRequestsException) {
                Log.d("ERROR_TAG", "Too many requests: $e")
                withContext(Dispatchers.Main) {
                    updateUIErrorAPI("Demasiadas peticiones, espera un poco")
                }
            } catch (e: IOException) {
                Log.d("ERROR_TAG", "Error fetching stops: $e")
                withContext(Dispatchers.Main) {
                    updateUIErrorAPI("Error al obtener paradas")
                }
            }
        }
    }

    private fun updateUIWithBuses() {
        Log.d("DEBUG_TAG", "Update UI with buses method called")
        if (busStops.isEmpty()) {
            updateUINoStops()
            return
        }
        try {
            // call the API to get the buses in the stops
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
        } catch (e: IOException) {
            Log.d("ERROR_TAG", "Error fetching buses: $e")
            updateUIErrorAPI("Error al obtener buses")
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.CenterHorizontally)
                        ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize()
                                .displayCutoutPadding(),
                            indicatorColor = wearColorPalette.primary,
                            trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.3f),
                            strokeWidth = 6.dp
                        )
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(6.dp),
                            text = loadingText ?: "",
                            color = MaterialTheme.colors.onBackground,
                            textAlign = TextAlign.Center,
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
                    .verticalScroll(scrollState)
                    .padding(8.dp),
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
        val maxPages = busStops.size + 1
        //    https://www.youtube.com/watch?v=2CzWz5Ad4iM <- Rotary input TODO
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
                    .padding(bottom = 3.dp),
                state = pagerState,
                pageSpacing = 0.dp,
                userScrollEnabled = true,
                reverseLayout = false,
                beyondBoundsPageCount = 0,
                pageSize = PageSize.Fill,
                flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
                key = null,
                pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
                    Orientation.Horizontal
                ),
                pageContent = {
                    if (it == 0) {
                        StopsPage(busStops, pagerState)
                    } else {
                        BusStopPage(busStops[it - 1], pagerState)
                    }
                }
            )
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
    fun SplashScreen() {
        CoruñaBusWearTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(
                            // Grab the logo from the drawable folder
                            id = R.drawable.ic_launcher
                        ),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}

