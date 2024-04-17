package com.ldangelo.corunabuswear.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ldangelo.corunabuswear.data.models.Bus

class BusStopViewModel (val id: Int, val name: String) : ViewModel() {
    private val _distance: MutableLiveData<Int> = MutableLiveData(9999)
    private val _buses: BusesViewModel = BusesViewModel()

    val distance: LiveData<Int> = _distance
    val buses: BusesViewModel = _buses

    fun updateBuses(buses: List<Bus>) {
        this._buses.updateBuses(buses)
    }

    fun updateDistance(distance: Int) {
        this._distance.value = distance
    }

    @Override
    override fun toString(): String {
        // print sublist og buses, the list can be empty
        val busesLocal = buses.buses.value ?: emptyList()
        return "BusStop(id=$id, name=$name, distance=$distance, buses=${busesLocal.subList(0,
            Integer.min(3, busesLocal.size)
        )})"
    }
}