/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.ldangelo.corunabuswear.presentation

import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ldangelo.corunabuswear.presentation.theme.CoruñaBusWearTheme
import com.ldangelo.corunabuswear.data.ApiConstants.BUS_API_FETCH_TIME
import com.ldangelo.corunabuswear.data.ContextHolder.setApplicationContext
import com.ldangelo.corunabuswear.data.providers.BusProvider
import com.ldangelo.corunabuswear.data.providers.BusProvider.fetchStops
import com.ldangelo.corunabuswear.data.providers.LocationProvider.startRegularLocationUpdates
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.ldangelo.corunabuswear.data.ApiConstants.MINUTE_API_LIMIT
import com.ldangelo.corunabuswear.data.AppConstants
import com.ldangelo.corunabuswear.data.ContextHolder.setLifecycleScope
import com.ldangelo.corunabuswear.data.local.saveLog
import com.ldangelo.corunabuswear.data.providers.BusProvider.fetchBuses
import com.ldangelo.corunabuswear.data.providers.BusProvider.mockBusApi
import com.ldangelo.corunabuswear.data.providers.retryUpdateDefinitions
import com.ldangelo.corunabuswear.data.viewmodel.BusStopViewModel
import com.ldangelo.corunabuswear.data.viewmodel.BusStopsListViewModel
import com.ldangelo.corunabuswear.presentation.components.composed.UpdateUILoading
import com.ldangelo.corunabuswear.presentation.components.composed.UpdateUIError
import com.ldangelo.corunabuswear.presentation.components.composed.UpdateUIWithBuses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.max


class MainActivity : FragmentActivity() {
    var definitionsUpdated = false
    private lateinit var locationListener: LocationCallback
    private var executor = Executors.newSingleThreadScheduledExecutor()
    private var busTaskScheduler: ScheduledFuture<*>? = null
    private var busStops: BusStopsListViewModel = BusStopsListViewModel()
    private var vibrator: Vibrator? = null
    // // // // // Mocking // // // // //
    private var mockLocation = false
    private var mockLocationCoordinates = Pair(43.3470, -8.4004)
    private var mockLocationExecutor = Executors.newSingleThreadScheduledExecutor()
    private var mockTaskScheduler: ScheduledFuture<*>? = null
    private var mockApi = false
    // // // // // // // // // // // // //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApplicationContext(this)
        setLifecycleScope(lifecycleScope)

        // Set vibrator service
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        // Update UI to loading state
        displayContent { UpdateUILoading("Obteniendo localización...") }

        // create location listener callback
        locationListener = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Create double vibration effect
                val vibrationEffect = android.os.VibrationEffect.createWaveform(
                    longArrayOf(20, 60, 20),
                    intArrayOf(30, 0, 30),
                    -1
                )
                if (busStops.busStops.value.isNullOrEmpty()) {
                    displayContent { UpdateUILoading("Obteniendo paradas...") }
                }
                // Handle location updates here
                val location = locationResult.lastLocation
                if (location != null) {
                    vibrator?.vibrate(vibrationEffect)
                    updateLocation(location)
                } else {
                    Log.d("DEBUG_TAG", "Location is null")
                }
            }
        }
        // // // // // Mocking // // // // //
        if (mockLocation) {
            mockTaskScheduler = mockLocationExecutor.scheduleAtFixedRate({
                val loc = Location("mock")
                loc.latitude = mockLocationCoordinates.first
                loc.longitude = mockLocationCoordinates.second
                // vary it by 0.005% of the value
                loc.latitude += (Math.random() - 0.5) * 0.005
                loc.longitude += (Math.random() - 0.5) * 0.005
                    updateLocation(loc)
                Log.d("DEBUG_TAG", "Mock location updated to ${loc.latitude}, ${loc.longitude}")
            }, 0, 15000L, TimeUnit.MILLISECONDS)
            return
        }
        // // // // // // // // // // // // //
        // Start location updates and attach listener
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
            displayContent { UpdateUIError("Permiso de localización no concedido") }
            return
        } else {
            for (result in grantResults) {
                if (result != PERMISSION_GRANTED) {
                    Log.d("DEBUG_TAG", "Permission not granted")
                    displayContent { UpdateUIError("Permiso de localización no concedido") }
                    return
                }
            }
        }
    }

    private fun startRegularBusUpdates(busStopsListViewModel: BusStopsListViewModel, initialDelay: Long = 0) {
        Log.d("DEBUG_TAG", "Starting regular bus updates")
        busTaskScheduler?.cancel(true)
        val busStops: List<BusStopViewModel> = busStopsListViewModel.busStops.value ?: emptyList()
        val delay = (BUS_API_FETCH_TIME * max((busStops.size.toFloat() / MINUTE_API_LIMIT.toFloat()), 1F)).toLong()

        busTaskScheduler = executor.scheduleAtFixedRate({
            lifecycleScope.launch(Dispatchers.IO) {

                Log.d("DEBUG_TAG", "Updating buses")
                for (stop in busStops) {
                    // // // // // Mocking // // // // //
                    if (mockApi) {
                        val buses = mockBusApi(this@MainActivity)
                        withContext(Dispatchers.Main) {
                            stop.updateBuses(buses)
                        }
                        continue
                    }
                    // // // // // // // // // // // // //
                    try {
                        val buses = retryUpdateDefinitions ({
                            Thread.sleep(500)
                            fetchBuses(stop.id)
                        }, this@MainActivity,
                        {
                            displayContent {
                                UpdateUILoading("Actualizando índice...")
                            }
                        })
                        withContext(Dispatchers.Main) {
                            stop.updateBuses(buses)
                        }
                    } catch (e: BusProvider.TooManyRequestsException) {
                        continue
                    }
                }
            }
        }, initialDelay, delay, TimeUnit.MILLISECONDS)
    }

    private fun updateLocation(location: Location) {
        busTaskScheduler?.cancel(true)
        val radius = applicationContext.getSharedPreferences(AppConstants.SETTINGS_PREF, Context.MODE_PRIVATE).getInt(
            AppConstants.STOPS_RADIUS_KEY,
            AppConstants.DEFAULT_STOPS_RADIUS
        )
        val limit = applicationContext.getSharedPreferences(AppConstants.SETTINGS_PREF, Context.MODE_PRIVATE).getInt(
            AppConstants.STOPS_FETCH_KEY,
            AppConstants.DEFAULT_STOPS_FETCH
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val tmpStops = retryUpdateDefinitions ({
                    fetchStops(location.latitude, location.longitude, radius, limit)
                }, this@MainActivity,
                {
                    displayContent {
                        UpdateUILoading("Actualizando índice...")
                    }
                })
                // assign to viewmodel in Main thread
                withContext(Dispatchers.Main) {
                    busStops.updateBusStops(tmpStops)
                }
                displayContent { UpdateUIWithBuses(busStops, vibrator, onBackPressedDispatcher) }
                startRegularBusUpdates(busStops)
            } catch (e: BusProvider.TooManyRequestsException) {
                Log.d("ERROR_TAG", "Too many requests: $e")
                if (busTaskScheduler?.isCancelled == true) {
                    withContext(Dispatchers.Main) {
                        displayContent { UpdateUIError("Demasiadas peticiones, espera un poco") }
                    }
                }
            } catch (e: IOException) {
                Log.d("ERROR_TAG", "Error fetching stops: $e")
                saveLog(this@MainActivity, "Error fetching stops: $e")
                withContext(Dispatchers.Main) {
                    displayContent { UpdateUIError("Error al obtener paradas", e.toString()) }
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

