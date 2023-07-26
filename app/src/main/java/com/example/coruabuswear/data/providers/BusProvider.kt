package com.example.coruabuswear.data.providers

import android.graphics.Color
import android.util.Log
import androidx.compose.ui.graphics.Color as Colorx
import com.example.coruabuswear.data.ApiConstants.BUS_API_ROOT
import com.example.coruabuswear.data.ContextHolder.getApplicationContext
import com.example.coruabuswear.data.local.getBusStop
import com.example.coruabuswear.data.models.Bus
import com.example.coruabuswear.data.models.BusLine
import com.example.coruabuswear.data.models.BusStop
import com.example.coruabuswear.presentation.MainActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val client = OkHttpClient()

object BusProvider {
    fun fetchStopsLinesData(): Pair<List<BusStop>, List<BusLine>> {
        // For some reason the API is called with the 2016 year date, probably because it was made that year
        val currDate = LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0)
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
        val uri = BUS_API_ROOT +
                "&dato=${currDate.format(formatter)}_gl_3_${currDate.format(formatter)}" +
                "&func=7"

        val request = Request.Builder()
            .url(uri)
            .build()

        var busStops = emptyList<BusStop>()
        var busLines = emptyList<BusLine>()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val json = JSONObject(response.body!!.string()).getJSONObject("iTranvias").getJSONObject("actualizacion")
            busStops = parseBusStopsFromJsonArray(json.getJSONArray("paradas"))
            busLines = parseBusLinesFromJsonArray(json.getJSONArray("lineas"))
        }

        return Pair(busStops, busLines)
    }

    fun fetchStops(latitude: Double, longitude: Double, radius: Int, limit: Int): List<BusStop> {
        val uri = BUS_API_ROOT +
                "&dato=${latitude}_" +
                "${longitude}_" +
                "${radius}_" +
                "$limit&func=3"

        val request = Request.Builder()
            .url(uri)
            .build()

        val busStops = mutableListOf<BusStop>()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val json = JSONObject(response.body!!.string())
            val stops = json.getJSONArray("posgps")
            for (i in 0 until stops.length()) {
                val stop = stops.getJSONObject(i)
                val id = stop.getInt("parada")

                val storedStop = getBusStop<BusStop>(getApplicationContext(), id.toString())
                ?: throw Exception("Bus stop $id does not exist in local storage")

                val distance = stop.getInt("distancia")
                storedStop.updateDistance(distance)
                busStops += storedStop
            }
        }
        return busStops
    }
}

fun parseBusLineFromJson(jsonStr: String): BusLine {
    val json = JSONObject(jsonStr)
    val id = json.getInt("id")
    val name = json.getString("lin_comer")
    val color = Colorx(android.graphics.Color.parseColor("#${json.getString("color")}"))
    return BusLine(id, name, color)
}

fun parseBusLinesFromJsonArray(jsonArray: JSONArray): List<BusLine> {
    val busLines = mutableListOf<BusLine>()
    for (i in 0 until jsonArray.length()) {
        val json = jsonArray.getJSONObject(i)
        val busLine = parseBusLineFromJson(json.toString())
        busLines.add(busLine)
    }
    return busLines
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