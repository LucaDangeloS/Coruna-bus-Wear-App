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
import androidx.wear.ambient.AmbientLifecycleObserver
import com.ldangelo.corunabuswear.presentation.theme.CoruñaBusWearTheme
import com.ldangelo.corunabuswear.data.ApiConstants.BUS_API_FETCH_TIME
import com.ldangelo.corunabuswear.data.ContextHolder.setApplicationContext
import com.ldangelo.corunabuswear.data.models.BusStop
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


class MainActivity : FragmentActivity() {
    var definitionsUpdated = false
    private lateinit var locationListener: LocationCallback
    private var executor = Executors.newSingleThreadScheduledExecutor()
    private var busTaskScheduler: ScheduledFuture<*>? = null
    private var busStops: List<BusStop> = mutableListOf()
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
        lifecycle.addObserver(ambientObserver)

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
                if (busStops.isEmpty()) {
                    displayContent { UpdateUILoading("Obteniendo paradas...") }
                }
                // Handle location updates here
                val location = locationResult.lastLocation
                if (location != null) {
                    vibrator?.vibrate(vibrationEffect)
                    updateUIWithLocation(location)
                } else {
                    Log.d("DEBUG_TAG", "Location is null")
                }
            }
        }
        // // // // // Mocking // // // // //
        if (mockLocation) {
            val location = Location("mock")
            location.latitude = mockLocationCoordinates.first
            location.longitude = mockLocationCoordinates.second
            updateLocation(location)

            mockTaskScheduler = mockLocationExecutor.scheduleAtFixedRate({
                val loc = Location("mock")
                loc.latitude = mockLocationCoordinates.first
                loc.longitude = mockLocationCoordinates.second
                // vary it by 0.005% of the value
                loc.latitude += (Math.random() - 0.5) * 0.005
                loc.longitude += (Math.random() - 0.5) * 0.005
                    updateLocation(loc)
                Log.d("DEBUG_TAG", "Mock location updated to ${loc.latitude}, ${loc.longitude}")
            }, 10000L, 15000L, TimeUnit.MILLISECONDS)
            return
        }
        // // // // // // // // // // // // //
        // Start location updates and attach listener
        if (mockLocation) {
            val location = Location("mock")
            location.latitude = mockLocationCoordinates.first
            location.longitude = mockLocationCoordinates.second
            updateUIWithLocation(location)
            return
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

    private fun startRegularBusUpdates(busStops: List<BusStop>, initialDelay: Long = 0) {
        Log.d("DEBUG_TAG", "Starting regular bus updates")
        busTaskScheduler = executor.scheduleAtFixedRate({
            lifecycleScope.launch(Dispatchers.IO) {
                for (stop in busStops) {
                    if (mockApi) {
                        stop.updateBuses(mockBusApi(this@MainActivity))
                        continue
                    }
                    try {
                        retryUpdateDefinitions ({
                            stop.updateBuses(fetchBuses(stop.id))
                            Thread.sleep(500)
                        }, this@MainActivity)
                    } catch (e: BusProvider.TooManyRequestsException) {
                        continue
                    }
                }
            }
        }, initialDelay, BUS_API_FETCH_TIME * (busStops.size / MINUTE_API_LIMIT), TimeUnit.MILLISECONDS)
    }

    private fun updateUIWithLocation(location: Location) {
        Log.d("DEBUG_TAG", "Update UI method called")
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
                busStops = retryUpdateDefinitions ({
                    fetchStops(location.latitude, location.longitude, radius, limit)
                }, this@MainActivity)
                displayContent { UpdateUIWithBuses(busStops, this@MainActivity, vibrator, onBackPressedDispatcher) }
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

