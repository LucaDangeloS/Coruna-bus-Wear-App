package com.ldangelo.corunabuswear.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ldangelo.corunabuswear.data.models.Bus

class BusesViewModel : ViewModel() {
    private val _buses = MutableLiveData<List<Bus>>(emptyList())
    val buses: LiveData<List<Bus>> = _buses
    fun updateBuses(buses: List<Bus>) {
        _buses.value = buses
    }
}