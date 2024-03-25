package com.ldangelo.corunabuswear.data.providers

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
import com.ldangelo.corunabuswear.data.ContextHolder.getApplicationContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks.await

object LocationProvider {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    fun fetchLocation(activity: Activity): Location? {
        val context = getApplicationContext()
        var location: Location? = null
        fusedLocationClient = getFusedLocationProviderClient(context)

        if (!permissionCheck(context, activity)) {
            Log.d("DEBUG_TAG", "Permission not granted")
//            throw PermissionNotGrantedException("Permission not granted")
        }

        // TODO: Add timeout
        location = await(
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            )
        )

        return location
    }

    @SuppressLint("MissingPermission")
    fun startRegularLocationUpdates(locationListener: LocationCallback, activity: Activity) {
        val context = getApplicationContext()
        fusedLocationClient = getFusedLocationProviderClient(context)

        if (!permissionCheck(context, activity)) {
            // Raise permission exception
            Log.d("DEBUG_TAG", "Permission not granted")
//            throw PermissionNotGrantedException("Permission not granted")
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 30000)
            .setMinUpdateDistanceMeters(30f)

        fusedLocationClient.requestLocationUpdates(
            locationRequest.build(),
            locationListener,
            null
        )
    }

    fun stopFetchingLocation(locationListener: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationListener)
    }


    private fun permissionCheck(context: Context, activity: Activity): Boolean {
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
}

// Custom exception for permission
class PermissionNotGrantedException(message: String) : Exception(message)

