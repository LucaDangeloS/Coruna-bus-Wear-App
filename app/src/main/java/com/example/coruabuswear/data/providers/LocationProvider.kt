package com.example.coruabuswear.data.providers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.coruabuswear.presentation.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks.await

object LocationProvider {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    fun fetchLocation(context: Context): Location {
        var location: Location? = null
        fusedLocationClient = getFusedLocationProviderClient(context)
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // TODO: WHAT TO DO WITH THIS LISTENER?
        locationListener = LocationListener { _location -> println("Location changed: $_location") }

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
        } else {
//            locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER,
//                10000,
//                0f,
//                locationListener
//            )
            if (!locationManager.isLocationEnabled) {
                Log.d("DEBUG_TAG", "Location not enabled")
                // Request to enable it
            }
        }
        try {
            // TODO: Add timeout
            location = await(
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, // TODO: Change accuracy according to background/foreground usage
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
}