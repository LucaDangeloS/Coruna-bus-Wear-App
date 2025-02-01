package com.ldangelo.corunabuswear.data.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldangelo.corunabuswear.data.model.Bus
import com.ldangelo.corunabuswear.data.model.BusStop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StopViewModel (val id: Int, val name: String) : ViewModel() {
    private val _distance: MutableStateFlow<Int> = MutableStateFlow(9999)
    private val _apiWasCalled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _buses = MutableStateFlow<List<Bus>>(emptyList())

    val distance: StateFlow<Int> = _distance.asStateFlow()
    val apiWasCalled: StateFlow<Boolean> = _apiWasCalled.asStateFlow()
    val buses: StateFlow<List<Bus>> = _buses

    fun updateBuses(buses: List<Bus>) {
//        this._buses.updateBuses(emptyList())
        _buses.tryEmit(buses)
    }

    fun updateDistance(newDistance: Int) {
        viewModelScope.launch {
            _distance.emit(newDistance)
        }
    }

    fun updateApiWasCalled(apiWasCalled: Boolean) {
        viewModelScope.launch { _apiWasCalled.emit(apiWasCalled) }
    }

    fun toBusStop(): BusStop {
        return BusStop(id, name, _distance.value, buses.value)
    }

    companion object {
        fun fromBusStop(busStop: BusStop): StopViewModel {
            val stopViewModel = StopViewModel(busStop.id, busStop.name)
            stopViewModel.updateDistance(busStop.distance)
            stopViewModel.updateBuses(busStop.buses)
            return stopViewModel
        }
    }

    @Override
    override fun toString(): String {
        // print sublist og buses, the list can be empty
        val busesLocal = buses.value
        return "BusStop(id=$id, name=$name, distance=${_distance.value}, buses=${
            busesLocal.subList(
                0,
                Integer.min(3, busesLocal.size)
            )
        })"
    }
}