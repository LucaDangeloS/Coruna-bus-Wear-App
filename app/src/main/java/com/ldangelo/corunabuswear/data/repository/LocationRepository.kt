package com.ldangelo.corunabuswear.data.repository

import android.app.Activity
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.ldangelo.corunabuswear.data.source.apis.LocationProvider
import com.ldangelo.corunabuswear.data.source.apis.PermissionNotGrantedException
import javax.inject.Inject
import javax.inject.Singleton

interface ILocationRepository {
    fun fetchLocation(): Location
    fun mockLocation(): Location
}

@Singleton
class LocationRepository @Inject constructor(
    activity: Activity,
    private val locationProvider: LocationProvider = LocationProvider
): ILocationRepository {
    private val context: Context = activity.applicationContext
    private var mocking = false

    init {
        locationProvider.init(context)
        if (!locationProvider.permissionCheck(context, activity)) {
            // Raise permission exception
            Log.d("DEBUG_TAG", "Permission not granted")
            throw PermissionNotGrantedException("Permission not granted")
        }
    }

    override fun fetchLocation(): Location {
        return locationProvider.fetchLocation() ?: Location("none").apply {
            latitude = 0.0
            longitude = 0.0
        }
    }

    override fun mockLocation(): Location {
        mocking = true
        fun rand(): Double = (Math.random() - 0.5) / 50

        val location = Location("mock").apply {
            latitude = 43.3470 + rand()
            longitude = -8.4004 + rand()
            Log.i(
                "location",
                "Lat: $latitude, Long:$longitude"
            )
        }
        return location
    }

    fun requestLocationUpdates(priority: Int, interval: Long, distance: Float, locationCallback: LocationCallback) {
        val locationRequest =
            LocationRequest.Builder(priority, interval)
                .setMinUpdateDistanceMeters(distance)

        locationProvider.requestLocationUpdates(
            locationRequest.build(),
            locationCallback,
        )
    }

//    override fun startRegularLocationUpdates(activity: Activity, context: Context): Location {
//        if (mock) {
//
//        }
//        if (!LocationProvider.permissionCheck(context, activity)) {
//            // Raise permission exception
//            Log.d("DEBUG_TAG", "Permission not granted")
//            throw PermissionNotGrantedException("Permission not granted")
//        }
//        val interval = getStringOrDefaultPreference(
//            SETTINGS_PREF,
//            context,
//            LOC_INTERVAL_KEY,
//            DEFAULT_LOC_INTERVAL.toString()
//        ).toLong()
//        val distanceUpdate = getStringOrDefaultPreference(
//            SETTINGS_PREF,
//            context,
//            LOC_DISTANCE_KEY,
//            DEFAULT_LOC_DISTANCE.toString()
//        ).toFloat()
//        val locationRequest =
//            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
//                .setMinUpdateDistanceMeters(distanceUpdate)
//
//        locationProvider.requestLocationUpdates(
//            locationRequest.build(),
//            locationCallback,
//        )
//        return locationProvider.fetchLocation(context)!!
//    }

//    override fun stopLocationUpdates() {
//        locationProvider.removeLocationUpdates(locationCallback)
//    }
}
