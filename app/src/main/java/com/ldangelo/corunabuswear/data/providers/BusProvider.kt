package com.ldangelo.corunabuswear.data.providers

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color as Colorx
import com.ldangelo.corunabuswear.data.ApiConstants.BUS_API_ROOT
import com.ldangelo.corunabuswear.data.ContextHolder.getApplicationContext
import com.ldangelo.corunabuswear.data.local.clearAllStoredBusData
import com.ldangelo.corunabuswear.data.local.getBusConnection
import com.ldangelo.corunabuswear.data.local.getBusLine
import com.ldangelo.corunabuswear.data.local.getBusStop
import com.ldangelo.corunabuswear.data.local.saveBusConnection
import com.ldangelo.corunabuswear.data.local.saveBusLine
import com.ldangelo.corunabuswear.data.local.saveBusStop
import com.ldangelo.corunabuswear.data.local.saveLog
import com.ldangelo.corunabuswear.data.models.Bus
import com.ldangelo.corunabuswear.data.models.BusLine
import com.ldangelo.corunabuswear.data.models.BusStop
import com.ldangelo.corunabuswear.presentation.MainActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val client = OkHttpClient()

object BusProvider {
    fun fetchStopsLinesData(): Triple<List<BusStop>, List<BusLine>, List<BusLine>> {
        // For some reason the API is called with the 2016 year date, probably because it was made that year
        val fetchDate = LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0)
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
        val uri = BUS_API_ROOT +
                "&dato=${fetchDate.format(formatter)}_gl_3_${fetchDate.format(formatter)}" +
                "&func=7"

        val request = Request.Builder()
            .url(uri)
            .build()

        var busStops = emptyList<BusStop>()
        var busLines = emptyList<BusLine>()
        var busConnections = emptyList<BusLine>()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val json = JSONObject(response.body!!.string()).getJSONObject("iTranvias").getJSONObject("actualizacion")
            busLines = parseBusLinesFromJsonArray(json.getJSONArray("lineas"))
            busStops = parseBusStopsFromJsonArray(json.getJSONArray("paradas"))
            // bus connections will be added to an exception list
            busConnections = parseConnectionsFromJsonArray(json.getJSONObject("enlaces").getJSONArray("origen"))
            // Remove from busConnections entries that are in busLines
            busConnections = busConnections.filter { busLine -> !busLines.any { it.id == busLine.id } }
        }

        return Triple(busStops, busLines, busConnections)
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
            if (!response.isSuccessful) {
                if (response.code == 429) {
                    throw TooManyRequestsException("Unexpected code $response")
                } else {
                    throw IOException("Unexpected code $response")
                }
            }
            val json = JSONObject(response.body!!.string())
            val stops = json.getJSONArray("posgps")
            for (i in 0 until stops.length()) {
                val stop = stops.getJSONObject(i)
                val id = stop.getInt("parada")

                val storedStop = getBusStop<BusStop>(getApplicationContext(), id.toString())
                ?: throw UnknownDataException("Bus stop $id does not exist in local storage")

                val distance = stop.getInt("distancia")
                storedStop.updateDistance(distance)
                busStops += storedStop
            }
        }
        return busStops
    }

    fun fetchBuses(stopId: Int): List<Bus> {
        // https://developer.android.com/topic/libraries/architecture/workmanager
        val uri = BUS_API_ROOT +
            "&dato=${stopId}"+
            "&func=0"

        val request = Request.Builder()
            .url(uri)
            .build()

        val busList = mutableListOf<Bus>()

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
                return emptyList()
            }
            for (i in 0 until lineas.length()) {
                val linea = lineas.getJSONObject(i)

                val buses = linea.getJSONArray("buses")
                for (j in 0 until buses.length()) {
                    try {
                        busList += parseBusFromJson(
                            buses.getJSONObject(j).accumulate("linea", linea.getInt("linea"))
                        )
                    } catch (e: BusLineIsConnection) {
                        // do nothing
                        Log.d("DEBUG_TAG", "Bus line is a connection")
                    }
                }
            }
        }
        // sort busList by time
        busList.sortBy { it.remainingTime }
        return busList
    }

    fun mockBusApi(context: Context): List<Bus> {
        val initialTime = LocalDateTime.now()
        println("Mocking bus api")
        val busList = mutableListOf<Bus>()
        val busLineBUH = getBusLine<BusLine>(context, "1800")
        val busLine1A = getBusLine<BusLine>(context, "1900")
        val busLine11 = getBusLine<BusLine>(context, "1100")
        val busLine3A = getBusLine<BusLine>(context, "301")
        val busLine3 = getBusLine<BusLine>(context, "300")
        val endTime = LocalDateTime.now()
        busList.add(Bus(5, busLineBUH!!, 15))
        busList.add(Bus(1, busLine1A!!, 5))
        busList.add(Bus(2, busLine11!!, 2))
        busList.add(Bus(3, busLine3A!!, 10))
        busList.add(Bus(3, busLine3A, 30))
        busList.add(Bus(4, busLine3!!, -1))
        busList.add(Bus(4, busLine3, 4))
        print("Mocking bus api took ${endTime.second - initialTime.second} seconds")
        return busList
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

    // Bus
    private fun parseBusFromJson(busObj: JSONObject): Bus {
        val lineaId: Int = busObj.getInt("linea")
        val busLine = getBusLine<BusLine>(getApplicationContext(), lineaId.toString())
        if (busLine == null) {
            if (getBusConnection<BusLine>(getApplicationContext(), lineaId.toString()) != null) {
                throw BusLineIsConnection("Bus line $lineaId is a connection")
            }
            throw UnknownDataException("Bus line $lineaId does not exist in local storage")
        }
        val remainingTime = try {busObj.getInt("tiempo")} catch (e: Exception) {-1}
        return Bus(busObj.getInt("bus"), busLine, remainingTime)
    }

    // Connections
    private fun parseConnectionFromJson(json: JSONObject): List<BusLine> {
        val placeholderColor = Colorx(android.graphics.Color.parseColor("#000000"))
        val busLines = mutableListOf<BusLine>()
        val sentidos = json.getJSONArray("sentidos")

        for (i in 0 until sentidos.length()) {
            val sentido = sentidos.getJSONObject(i)
            val destinos = sentido.getJSONArray("destinos")

            for (j in 0 until destinos.length()) {
                val lineaObj = destinos.getJSONObject(j)
                val linea = lineaObj.getInt("linea")

                busLines.add(BusLine(linea, linea.toString(), placeholderColor))
            }
        }
        return busLines
    }
    private fun parseConnectionsFromJsonArray(jsonArray: JSONArray): List<BusLine> {
        val busLines = mutableListOf<BusLine>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            val busLine = parseConnectionFromJson(json)
            busLines.addAll(busLine)
        }

        // Remove duplicates
        return busLines.distinctBy { it.id }
    }

    // Custom exceptions
    class UnknownDataException(message: String) : Exception(message)
    class TooManyRequestsException(message: String) : Exception(message)
    class BusLineIsConnection(message: String) : Exception(message)
}

suspend fun <T> retryUpdateDefinitions(function: suspend () -> T, context: MainActivity): T {
    try {
        return function()
    } catch (e: BusProvider.UnknownDataException) {
        if (context.definitionsUpdated) {
            // Store in a log file
            saveLog(context, e.toString())
            Log.d("ERROR_TAG", e.toString())
            throw e
        }
        context.definitionsUpdated = true
//        displayContent{
//            UpdateUILoading("Actualizando índice...")
//        }
        Log.d("DEBUG_TAG", "Updating Bus definitions")
        val (stops, lines, connections) = BusProvider.fetchStopsLinesData()
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
        Log.d("DEBUG_TAG", "Updated!")
    } catch (e: BusProvider.TooManyRequestsException) {
        Log.d("ERROR_TAG", "Too many requests: $e")
        throw e
    }
    return function()
}
