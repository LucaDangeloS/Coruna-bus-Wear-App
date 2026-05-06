package com.ldangelo.corunabuswear.data.repository

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.ldangelo.corunabuswear.data.source.apis.LocationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface ILocationRepository {
    fun fetchLocation(): Location
    fun mockLocation(): Location
}

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationProvider: LocationProvider = LocationProvider
): ILocationRepository {
    private var mocking = false

    init {
        locationProvider.init(context)
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

        try {
            locationProvider.requestLocationUpdates(
                locationRequest.build(),
                locationCallback,
            )
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error requesting location updates", e)
        }
    }
}
