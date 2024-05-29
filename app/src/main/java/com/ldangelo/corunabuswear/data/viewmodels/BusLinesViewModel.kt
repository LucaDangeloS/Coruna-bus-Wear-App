package com.ldangelo.corunabuswear.data.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ldangelo.corunabuswear.data.models.BusStop

class BusStopsListViewModel : ViewModel() {
    private val _busStops = MutableLiveData<List<BusStopViewModel>>(emptyList())
    val busStops: LiveData<List<BusStopViewModel>> = _busStops

    private fun updateBusStopsFromModelView(busStops: List<BusStopViewModel>) {
        _busStops.value = busStops
    }

    fun updateBusStops(busStops: List<BusStop>) {
        // Find coincidences between the new bus stops and the current ones
        // And update the buses and distance
        val busStopViewModels = busStops.map { busStop ->
            val tmp = BusStopViewModel(
                id = busStop.id,
                name = busStop.name,
            )
            val currentBusStop = _busStops.value?.find { it.id == busStop.id }
            if (currentBusStop != null) {
                currentBusStop.buses.buses.value?.let { tmp.updateBuses(it) }
            } else {
                tmp.updateBuses(busStop.buses)
            }
            tmp.updateDistance(busStop.distance)
            return@map tmp
        }
        updateBusStopsFromModelView(busStopViewModels)
    }
}