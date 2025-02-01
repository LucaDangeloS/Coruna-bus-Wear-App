package com.ldangelo.corunabuswear.data.source.apis

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks.await
import com.ldangelo.corunabuswear.data.AppConstants.DEFAULT_LOC_DISTANCE
import com.ldangelo.corunabuswear.data.AppConstants.DEFAULT_LOC_INTERVAL
import com.ldangelo.corunabuswear.data.AppConstants.LOC_DISTANCE_KEY
import com.ldangelo.corunabuswear.data.AppConstants.LOC_INTERVAL_KEY
import com.ldangelo.corunabuswear.data.AppConstants.SETTINGS_PREF
import com.ldangelo.corunabuswear.data.source.local.getStringOrDefaultPreference

object LocationProvider {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun init(context: Context) {
        fusedLocationClient = getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation(): Location? {
        var location: Location? = null
        // TODO: Add timeout
        location = await(
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            )
        )

        return location
    }

    fun permissionCheck(context: Context, activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("DEBUG_TAG", "No permission")
            // request both coarse and fine location permissions
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                10
            )
        } else {
            // Check if location is enabled
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("DEBUG_TAG", "Location not enabled")
                return false
            }
        }
        return true
    }

    fun requestLocationUpdates(
        locationRequest: LocationRequest,
        locationListener: LocationCallback
    ) {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationListener,
                null
            )
        } catch (e: SecurityException) {
            Log.d("DEBUG_TAG", "Security exception")
            throw PermissionNotGrantedException("Permission not granted")
        }
    }

    fun removeLocationUpdates(locationListener: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationListener)
    }
}

// Custom exception for permission
class PermissionNotGrantedException(message: String) : Exception(message)

