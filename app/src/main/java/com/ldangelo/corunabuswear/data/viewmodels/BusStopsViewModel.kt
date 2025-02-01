package com.ldangelo.corunabuswear.data.viewmodels

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.ldangelo.corunabuswear.data.ApiConstants.BUS_API_FETCH_TIME
import com.ldangelo.corunabuswear.data.ApiConstants.MINUTE_API_LIMIT
import com.ldangelo.corunabuswear.data.AppConstants.DEFAULT_FETCH_ALL_BUSES_ON_LOCATION_UPDATE
import com.ldangelo.corunabuswear.data.AppConstants.DEFAULT_LOC_INTERVAL
import com.ldangelo.corunabuswear.data.AppConstants.DEFAULT_STOPS_FETCH
import com.ldangelo.corunabuswear.data.AppConstants.FETCH_ALL_BUSES_ON_LOCATION_UPDATE_KEY
import com.ldangelo.corunabuswear.data.AppConstants.LOC_INTERVAL_KEY
import com.ldangelo.corunabuswear.data.AppConstants.SETTINGS_PREF
import com.ldangelo.corunabuswear.data.AppConstants.STOPS_FETCH_KEY
import com.ldangelo.corunabuswear.data.repository.BusesRepository
import com.ldangelo.corunabuswear.data.repository.IBusesRepository
import com.ldangelo.corunabuswear.data.repository.LocationRepository
import com.ldangelo.corunabuswear.data.source.local.clearAllStoredBusData
import com.ldangelo.corunabuswear.data.source.local.getStringOrDefaultPreference
import com.ldangelo.corunabuswear.data.source.local.saveBusConnection
import com.ldangelo.corunabuswear.data.source.local.saveBusLine
import com.ldangelo.corunabuswear.data.source.local.saveBusStop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class BusStopsListViewModel @Inject constructor(
    context: Context,
    private val busesRepository: BusesRepository,
    locationRepository: LocationRepository
): ViewModel() {
    // bus stops
    private val _busStops = MutableStateFlow<List<StopViewModel>>(
        value = emptyList()
    )
    var busStops: StateFlow<List<StopViewModel>> = _busStops

    // location
    private var _location = MutableStateFlow<Location?>(null)
    var location: StateFlow<Location?> = _location
    private val _locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            _location.value = p0.lastLocation
        }
    }
    private val _loadedLocation = MutableStateFlow(false)
    var loadedLocation: StateFlow<Boolean> = _loadedLocation
    private val _fetchedStops = MutableStateFlow(false)
    var fetchedStops: StateFlow<Boolean> = _fetchedStops

    private var updatedDefinitions = false

    private var lastAPICallTimestamp = 0L
    private var lastAPICallDelay = 0L
    private var prevPageIndex = 0
    private var currentPageIndex: Int = 0
    private var fetchTimer: Timer? = null
    private var numberOfStopsPref: String = getStringOrDefaultPreference(
        SETTINGS_PREF,
        context,
        STOPS_FETCH_KEY,
        DEFAULT_STOPS_FETCH.toString(),
    )
    private val loadAllBusesOnLocationFetch: Boolean = getStringOrDefaultPreference(
        SETTINGS_PREF,
        context,
        FETCH_ALL_BUSES_ON_LOCATION_UPDATE_KEY,
        DEFAULT_FETCH_ALL_BUSES_ON_LOCATION_UPDATE.toString(),
    ).toBoolean()
    private val locationInterval = getStringOrDefaultPreference(
        SETTINGS_PREF,
        context,
        LOC_INTERVAL_KEY,
        DEFAULT_LOC_INTERVAL.toString(),
    ).toLong()
    private val locationDistance = getStringOrDefaultPreference(
        SETTINGS_PREF,
        context,
        LOC_INTERVAL_KEY,
        DEFAULT_LOC_INTERVAL.toString(),
    ).toFloat()

    init {
        locationRepository.requestLocationUpdates(
            priority = Priority.PRIORITY_HIGH_ACCURACY,
            interval = locationInterval,
            distance = locationDistance,
            locationCallback = _locationCallback
        )
        // when there's a new location, update the nearby stops
        viewModelScope.launch(Dispatchers.IO) {
            try {
                location.collect { loc ->
                    if (loc != null) {
                        Log.d(TAG, "Location: $loc")
                        _loadedLocation.emit(true)
                        var stops: List<StopViewModel>
                        try {
                            stops = busesRepository.getNearbyStops(loc, busStops.value
                                .map { it.toBusStop() })
                                .map { StopViewModel.fromBusStop(it) }
                        } catch (e: IBusesRepository.UnknownDataException) {
                            if (!updatedDefinitions) {
                                updateAndStoreDefinitions(context)
                                stops = busesRepository.getNearbyStops(loc, busStops.value
                                    .map { it.toBusStop() })
                                    .map { StopViewModel.fromBusStop(it) }
                            } else {
                                Log.e(TAG, "Error fetching nearby stops", e)
                                throw e
                            }
                        }
                        _busStops.emit(stops)
                        if (stops.isNotEmpty()) {
                            beginPeriodicBusFetch(0)
                        }
                        _fetchedStops.emit(true)
                    } else {
                        Log.d(TAG, "Location is null")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting location updates", e)
            }
        }
    }

    private fun updateAndStoreDefinitions(context: Context) {
        Log.d(TAG, "Updating Bus definitions")
        val (stops, lines, connections) = busesRepository.updateDefinitions()
        clearAllStoredBusData(context)
        stops.forEach { busStop ->
            saveBusStop(context, busStop.id.toString(), busStop)
        }
        lines.forEach { busLine ->
            saveBusLine(context, busLine.id.toString(), busLine)
        }
        connections.forEach { busLine ->
            saveBusConnection(context, busLine.id.toString(), busLine)
        }
        Log.d(TAG, "Updated!")
        updatedDefinitions = true
    }

    private fun singleBusFetch(delay: Long = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (delay > 0) {
                    Thread.sleep(delay)
                }
                val stop = busStops.value[currentPageIndex - 1]
                val updatedStop = busesRepository.updateSingleStop(stop.toBusStop())
                stop.updateBuses(updatedStop.buses)
                stop.updateApiWasCalled(true)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun allBusesFetch(delay: Long = 300) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedStops = busesRepository.updateAllStops(busStops.value.map { it.toBusStop() })
            busStops.value.forEachIndexed { index, stop ->
                Thread.sleep(delay)
                stop.updateBuses(updatedStops[index].buses)
                stop.updateApiWasCalled(true)
            }
        }
    }

    private fun beginPeriodicBusFetch(initialDelay: Long) {
        Log.d(TAG, "Starting regular bus updates")
        fetchTimer?.cancel()

        val period: Long = if (currentPageIndex != 0) {
            BUS_API_FETCH_TIME
        } else {
            (BUS_API_FETCH_TIME * max((numberOfStopsPref.toFloat() / MINUTE_API_LIMIT.toFloat()), 1F)).toLong()
        }
        fetchTimer = Timer()
        fetchTimer!!.schedule(object : TimerTask() {
            override fun run() {
                if (initialDelay > 0) {
                    Thread.sleep(initialDelay)
                }

                // SINGLE STOP
                if (currentPageIndex > 0) {
//                    Log.d(TAG, "Updating single : ${currentPageIndex - 1}")
                    singleBusFetch()
                    lastAPICallDelay = period
                    lastAPICallTimestamp = System.currentTimeMillis()
                } else {
                    // ALL STOPS
                    if (loadAllBusesOnLocationFetch) {
//                        Log.d(TAG, "Updating all stops : ${currentPageIndex - 1}")
                        allBusesFetch()
                        lastAPICallDelay = period
                        lastAPICallTimestamp = System.currentTimeMillis()
                    }
                }
            }
        }, initialDelay, period)
    }

    fun updatePageIndex(pageIndex: Int) {
        currentPageIndex = pageIndex
        var remainingTime: Long? = null
        if (prevPageIndex > 0 && pageIndex == 0) {
//            Log.d(TAG, "Page index changed to $pageIndex")
            remainingTime = lastAPICallDelay - (System.currentTimeMillis() - lastAPICallTimestamp)
//            Log.d("DEBUG_TAG", "Page changed to 0 waiting $remainingTime ms")
        } else if (prevPageIndex == 0 && pageIndex > 0) {
//            Log.d(TAG, "Page index changed to $pageIndex")
            remainingTime = BUS_API_FETCH_TIME - (System.currentTimeMillis() - lastAPICallTimestamp)
//            Log.d("DEBUG_TAG", "Page changed from 0, waiting $remainingTime ms")
        }
//        Log.d("DEBUG_TAG", "Remaining time: $remainingTime")
        if (remainingTime != null && fetchTimer != null) {
            beginPeriodicBusFetch(max(remainingTime, 0))
        }
        prevPageIndex = pageIndex
        return
    }

    companion object {
        private const val TAG = "BusStopsListViewModel"
    }
}