/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.coruabuswear.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.coruabuswear.R
import com.example.coruabuswear.data.ContextHolder.setApplicationContext
import com.example.coruabuswear.data.local.clearAllSharedPreferences
import com.example.coruabuswear.data.local.saveBusStop
import com.example.coruabuswear.data.models.BusStop
import com.example.coruabuswear.data.providers.BusProvider
import com.example.coruabuswear.data.providers.BusProvider.fetchStops
import com.example.coruabuswear.data.providers.LocationProvider.fetchLocation
import com.example.coruabuswear.data.providers.LocationProvider.fetchLocationContinuously
import com.example.coruabuswear.data.providers.LocationProvider.stopFetchingLocation
import com.example.coruabuswear.presentation.theme.CoruñaBusWearTheme
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var definitionsUpdated = false
    private var locationUpdated = false
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApplicationContext(this)
//        clearAllSharedPreferences(this@MainActivity)
        updateUILoadingLocation()
        locationListener = LocationListener { _location ->
            println("!!! Location changed: $_location")
            if (!locationUpdated) {
                locationUpdated = true
            }
            updateUIWithLocation(_location)
        }
        lifecycleScope.launch(Dispatchers.IO) {
            fetchLocationContinuously(this@MainActivity, locationListener)
        }

//        val location = Flowable.fromCallable {
//            fetchLocation(this@MainActivity)
//        }
//        val locationBackground = location.subscribeOn(Schedulers.io())
//        val locationObserver = locationBackground.observeOn(Schedulers.single())
//
//        val locationCancellation = locationObserver.subscribe(
//            { x: Location ->
//                updateUIWithLocation(x)
//            }
//        ) { obj: Throwable -> obj.printStackTrace(); updateUINoLocation() }

        // Timeout for location, if it takes too long, show error
//        lifecycleScope.launch(Dispatchers.IO) {
//            kotlinx.coroutines.delay(20000)
//            withContext(Dispatchers.Main) {
//                if (!definitionsUpdated && !locationUpdated) {
//                    updateUINoLocation()
//                    // Stop listening for location updates
//                    Log.d("DEBUG_TAG", "Stopping location updates")
//                    stopFetchingLocation(this@MainActivity, locationListener)
//                } else if (definitionsUpdated) {
//                    updateUIUpdatingDefinitions()
//                }
//            }
//        }
    }

    private fun updateUIUpdatingDefinitions() {
        setContent {
            WearApp("Updating definitions!")
        }
    }

    private fun updateUIError() {
        setContent {
            WearApp("ERROR!")
        }
    }

    private fun updateUINoLocation() {
        setContent {
            WearApp("No location")
        }
    }

    @SuppressLint("CheckResult")
    private fun updateBusDefinitions(context: Context) {
        Log.d("DEBUG_TAG", "Updating Bus definitions")
        val (stops, lines) = BusProvider.fetchStopsLinesData()
        clearAllSharedPreferences(context)
        Log.d("DEBUG_TAG", "$stops \n $lines")
        stops.forEach { busStop ->
            saveBusStop(context, busStop.id.toString(), busStop)
        }
        lines.forEach { busLine ->
            saveBusStop(context, busLine.id.toString(), busLine)
        }
        Log.d("DEBUG_TAG", "Updated!")
    }

    private fun updateUIWithLocation(location: Location) {
        Log.d("DEBUG_TAG", "Update UI method called")
        var stops: List<BusStop> = emptyList()
        lifecycleScope.launch(Dispatchers.IO) {
            stops = try {
                fetchStops(location.latitude, location.longitude, 300, 3)
            } catch (e: Exception) {
                if (!definitionsUpdated) {
                    definitionsUpdated = true
                    updateBusDefinitions(this@MainActivity)
                } else {
                    withContext(Dispatchers.Main) {
                        updateUIError()
                    }
                }
                fetchStops(location.latitude, location.longitude, 300, 3)
            }
            withContext(Dispatchers.Main) {
                updateUIWithStops(stops)
            }
        }
    }

    private fun updateUIWithStops(stops: List<BusStop>) {
        setContent {
            WearApp("$stops")
        }
    }

    private fun updateUILoadingLocation() {
        setContent {
            CoruñaBusWearTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally

                ) {
                    CircularProgressIndicator(
                        indicatorColor = MaterialTheme.colors.secondary,
                        trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
                        strokeWidth = 4.dp
                    )
                }
            }
        }
    }
}

@Composable
fun WearApp(greetingName: String) {
    CoruñaBusWearTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center
        ) {
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}