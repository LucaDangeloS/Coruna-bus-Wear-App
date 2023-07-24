/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.coruabuswear.presentation

import android.location.Location
import android.os.Bundle
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
import com.example.coruabuswear.data.models.BusStop
import com.example.coruabuswear.data.providers.BusProvider.fetchStops
import com.example.coruabuswear.data.providers.LocationProvider.fetchLocation
import com.example.coruabuswear.presentation.theme.CoruñaBusWearTheme
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.coruabuswear.data.providers.BusProvider.fetchStopsLinesData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateUILoadingLocation()
        val location = Flowable.fromCallable {
            fetchLocation(this@MainActivity)
        }
        val composed = Flowable.fromCallable {
            fetchStopsLinesData()
        }
        val locationBackground = location.subscribeOn(Schedulers.io())
        val composedBackground = composed.subscribeOn(Schedulers.io())
        val locationObserver = locationBackground.observeOn(Schedulers.single())
        val composedObserver = composedBackground.observeOn(Schedulers.single())

        val locationCancellation = locationObserver.subscribe(
            { x: Location ->
                updateUI(x)
            }
        ) { obj: Throwable -> obj.printStackTrace(); updateUINoLocation() }

        val composedCancellation = composedObserver.subscribe(
            {
                println(it.first)
                println(it.second)
            }
        ) { obj: Throwable -> obj.printStackTrace() }

//        lifecycleScope.launch(Dispatchers.IO) {
//            location = fetchLocation(this@MainActivity)
//            launch(Dispatchers.Main) {
//                if (location != null) {
//                    println("Location not null!!!!!!!!!!!!!")
//                    updateUI(location!!)
//                } else {
//                    println("Location null?????????????????")
//                    updateUINoLocation()
//                }
//            }
//        }
    }

    private fun updateUINoLocation() {
        setContent {
            WearApp("No location")
        }
    }

    private fun updateUI(location: Location) {
        setContent {
            WearApp("${location.latitude} ${location.longitude}")
        }
        // fetch stops in worker thread
        var stops: List<BusStop>? = null

//        stops = fetchStops(location.latitude, location.longitude, 200, 5)
        lifecycleScope.launch(Dispatchers.IO) {
            stops = fetchStops(location.latitude, location.longitude, 200, 5)
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