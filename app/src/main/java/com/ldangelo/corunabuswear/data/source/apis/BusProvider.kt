package com.ldangelo.corunabuswear.data.source.apis

import android.util.Log
import androidx.compose.ui.graphics.Color as Colorx
import com.ldangelo.corunabuswear.data.ApiConstants.BUS_API_ROOT
import com.ldangelo.corunabuswear.data.model.BusLine
import com.ldangelo.corunabuswear.data.model.BusStop
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val client = OkHttpClient()

interface BusApi {
    fun fetchStopsLinesData(): Triple<List<BusStop>, List<BusLine>, JSONArray>
    fun fetchStops(latitude: Double, longitude: Double, radius: Int, limit: Int): List<JSONObject>
    fun fetchBuses(stopId: Int): List<JSONObject>
    fun mockStops(): List<JSONObject>
    fun mockBuses(): List<JSONObject>
}

object BusProvider : BusApi {
    override fun fetchStopsLinesData(): Triple<List<BusStop>, List<BusLine>, JSONArray> {
        // For some reason the API is called with the 2016 year date, probably because it was made that year
        val fetchDate = LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0)
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
        val uri = BUS_API_ROOT +
                "&dato=${fetchDate.format(formatter)}_gl_3_${fetchDate.format(formatter)}" +
                "&func=7"

        val request = Request.Builder()
            .url(uri)
            .build()

        var busStops: List<BusStop>
        var busLines: List<BusLine>

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val json = JSONObject(response.body!!.string()).getJSONObject("iTranvias").getJSONObject("actualizacion")
            busLines = parseBusLinesFromJsonArray(json.getJSONArray("lineas"))
            busStops = parseBusStopsFromJsonArray(json.getJSONArray("paradas"))
            // bus connections will be added to an exception list
            return Triple(busStops, busLines, json.getJSONObject("enlaces").getJSONArray("origen"))
        }
    }

    override fun fetchStops(latitude: Double, longitude: Double, radius: Int, limit: Int): List<JSONObject> {
        val uri = BUS_API_ROOT +
                "&dato=${latitude}_" +
                "${longitude}_" +
                "${radius}_" +
                "$limit&func=3"

        val request = Request.Builder()
            .url(uri)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 429) {
                    throw TooManyRequestsException("Unexpected code $response")
                } else {
                    throw IOException("Unexpected code $response")
                }
            }
            val json = JSONObject(response.body!!.string())
            val stops = json.getJSONArray("posgps")
            val jsonStops: MutableList<JSONObject> = mutableListOf()
            for (i in 0 until stops.length()) {
                jsonStops.add(stops.getJSONObject(i))
            }
        return jsonStops
        }
    }

    override fun fetchBuses(stopId: Int): List<JSONObject> {
        // https://developer.android.com/topic/libraries/architecture/workmanager
        val uri = BUS_API_ROOT +
            "&dato=${stopId}"+
            "&func=0"

        val request = Request.Builder()
            .url(uri)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 429) {
                    throw TooManyRequestsException("Unexpected code $response")
                }
                throw IOException("Unexpected code $response")
            }
            val json = JSONObject(response.body!!.string())
            val lineas: JSONArray
            try {
                lineas = json.getJSONObject("buses").getJSONArray("lineas")
            } catch (e: Exception) {
                return mutableListOf()
            }
            val jsonBuses: MutableList<JSONObject> = mutableListOf()

            for (i in 0 until lineas.length()) {
                val linea = lineas.getJSONObject(i)
                val buses = linea.getJSONArray("buses")

                for (j in 0 until buses.length()) {
                    try {
                        jsonBuses.add(buses.getJSONObject(j).accumulate("linea", linea.getInt("linea")))
                    } catch (e: BusLineIsConnection) {
                        // do nothing
                        Log.d("DEBUG_TAG", "Bus line is a connection")
                    }
                }
            }
            return jsonBuses
        }
    }

    override fun mockStops(): List<JSONObject> {
        val jsonStops: MutableList<JSONObject> = mutableListOf()
        val randomIds = (1..10).shuffled().take(5)
        for (i in randomIds) {
            jsonStops.add(
                JSONObject().accumulate("parada", i).accumulate("distancia", (1..200).random())
            )
        }
        return jsonStops
    }

    override fun mockBuses(): List<JSONObject> {
        val jsonBuses: MutableList<JSONObject> = mutableListOf()
        val linesPool: List<Int> = listOf(2451, 2400, 1700, 200, 300, 1500, 301, 600, 601, 700, 800, 1900, 1100, 1200, 1400, 1800, 2400, 1800, 1801)
        val timesPool: List<Int> = listOf(1, 2, 4, 5, 7, 8, 10, 14, 18, 23, 30, 32, 35, 40, 45, 50, 60)
        val busesAmount = 13
        val randomLines = linesPool.shuffled().take(busesAmount)
        val randomTimes = timesPool.shuffled().take(busesAmount)
        for (i in 0 until busesAmount) {
            jsonBuses.add(
                JSONObject().accumulate("bus", randomLines[i])
                    .accumulate("linea", randomLines[i])
                    .accumulate("tiempo", randomTimes[i])
            )
        }
        return jsonBuses
    }




    // BusLine
    private fun parseBusLineFromJson(json: JSONObject): BusLine {
        val id = json.getInt("id")
        val name = json.getString("lin_comer")
        val color = Colorx(android.graphics.Color.parseColor("#${json.getString("color")}"))
        return BusLine(id, name, color)
    }

    private fun parseBusLinesFromJsonArray(jsonArray: JSONArray): List<BusLine> {
        val busLines = mutableListOf<BusLine>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            val busLine = parseBusLineFromJson(json)
            busLines.add(busLine)
        }
        return busLines
    }

    // BusStop
    private fun parseBusStopFromJson(json: JSONObject): BusStop {
        val id = json.getInt("id")
        val name = json.getString("nombre")
        return BusStop(id, name)
    }

    private fun parseBusStopsFromJsonArray(jsonArray: JSONArray): List<BusStop> {
        val busStops = mutableListOf<BusStop>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            val busStop = parseBusStopFromJson(json)
            busStops.add(busStop)
        }
        return busStops
    }
    // Custom exceptions
    class TooManyRequestsException(message: String) : Exception(message)
    class BusLineIsConnection(message: String) : Exception(message)
}
