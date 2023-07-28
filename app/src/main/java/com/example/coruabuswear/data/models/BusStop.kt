package com.example.coruabuswear.data.models
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Integer.min

data class BusStop (val id: Int, val name: String?) {
    var distance: Int = 9999
    var buses: List<Bus> = emptyList()

    fun updateBuses(buses: List<Bus>) {
        this.buses = buses
    }

    fun updateDistance(distance: Int) {
        this.distance = distance
    }

    @Override
    override fun toString(): String {
        // print sublist og buses, the list can be empty
        return "BusStop(id=$id, name=$name, distance=$distance, buses=${buses?.subList(0, min(3, buses.size))})"
    }
}

