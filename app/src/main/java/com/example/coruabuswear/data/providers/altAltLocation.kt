package com.example.coruabuswear.data.providers

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.coruabuswear.presentation.WearApp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.Tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class altAltLocation : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
         var location: Location? = null
        // fetch location in a non-blocking coroutine
        try {
            lifecycleScope.launch { location = fetchLoc() }
        } catch (e: Exception) {
            Log.d("MainTag", "XXX Exception: $e")
        }
        if (location != null) {
            Log.d("MainTag", "Location: ${location?.latitude} ${location?.longitude}")
        }
        setContent {
            WearApp("${location?.latitude} ${location?.longitude}")
        }
    }


    suspend fun fetchLoc(): Location? = withContext(Dispatchers.Default) {
        var location: Location? = null
        try {
            location = await(fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null))
        } catch (e: Exception) {
            Log.d("MainTag", "Exception: $e")
        }

        return@withContext location
    }
}