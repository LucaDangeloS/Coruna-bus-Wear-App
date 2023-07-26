package com.example.coruabuswear.data.models
import org.json.JSONArray
import org.json.JSONObject

data class BusStop (val id: Int, val name: String?) {
    var distance: Int = 9999
    lateinit var buses: List<Bus>

    fun updateBuses(buses: List<Bus>) {
        this.buses = buses
    }

    fun updateDistance(distance: Int) {
        this.distance = distance
    }

    @Override
    override fun toString(): String {
        return "BusStop(id=$id, name=$name, distance=$distance)"
    }
}

