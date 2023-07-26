package com.example.coruabuswear.data.providers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.coruabuswear.presentation.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LocationProvider {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager

    @SuppressLint("MissingPermission")
    fun fetchLocation(context: Context): Location {
        var location: Location? = null
        fusedLocationClient = getFusedLocationProviderClient(context)
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // TODO: WHAT TO DO WITH THIS LISTENER?

        if (!permissionCheck(context)) {
            throw Exception("Permission not granted")
        }

        try {
            // TODO: Add timeout
            location = await(
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null // TODO: Add cancellation token
                )
            )
            if (location == null) {
                throw Exception("Location is null")
            }
        } catch (e: Exception) {
            Log.d("EXCEPTION_TAG", "Exception: $e")
            throw e
        }

        return location
    }

    @SuppressLint("MissingPermission")
    fun fetchLocationContinuously(context: Context, locationListener: LocationListener) {
        fusedLocationClient = getFusedLocationProviderClient(context)
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!permissionCheck(context)) {
            throw Exception("Permission not granted")
        }

        CoroutineScope(Dispatchers.Main).launch {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationManager.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,
                20000,
                40f,
                locationListener,
                Looper.getMainLooper()
            )
//            } else {
//                locationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER,
//                    20000,
//                    40f,
//                    locationListener,
//                    Looper.getMainLooper()
//                )
//            }
        }
    }

    fun stopFetchingLocation(context: Context, locationListener: LocationListener) {
        locationManager.removeUpdates(locationListener)
    }

    private fun permissionCheck(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MainTag", "No permission")
            // request both coarse and fine location permissions
            ActivityCompat.requestPermissions(
                context as MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                10
            )
            permissionCheck(context)
        } else {
            if (!locationManager.isLocationEnabled) {
                Log.d("DEBUG_TAG", "Location not enabled")
                // Request to enable it ?
                return false
            }
        }
        return true
    }
}

