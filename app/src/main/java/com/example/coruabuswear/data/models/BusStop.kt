package com.example.coruabuswear.data.models
import org.json.JSONArray
import org.json.JSONObject

data class BusStop (val id: Int, val name: String?) {
    var distance: Int = 9999
    lateinit var buses: List<Bus>

    fun updateBuses(buses: List<Bus>) {
        this.buses = buses
    }
}

fun parseBusStopFromJson(jsonStr: String): BusStop {
    val json = JSONObject(jsonStr)
    val id = json.getInt("id")
    val name = json.getString("nombre")
    return BusStop(id, name)
}

fun parseBusStopsFromJsonArray(jsonArray: JSONArray): List<BusStop> {
    val busStops = mutableListOf<BusStop>()
    for (i in 0 until jsonArray.length()) {
        val json = jsonArray.getJSONObject(i)
        val busStop = parseBusStopFromJson(json.toString())
        busStops.add(busStop)
    }
    return busStops
}