package com.ldangelo.corunabuswear.data.repository

import android.app.Activity
import android.location.Location
import android.util.Log
import com.ldangelo.corunabuswear.data.model.Bus
import com.ldangelo.corunabuswear.data.model.BusLine
import com.ldangelo.corunabuswear.data.model.BusStop
import com.ldangelo.corunabuswear.data.source.apis.BusApi
import com.ldangelo.corunabuswear.data.source.apis.BusProvider
import com.ldangelo.corunabuswear.data.source.apis.BusProvider.fetchBuses
import com.ldangelo.corunabuswear.data.source.local.getBusLine
import com.ldangelo.corunabuswear.data.source.local.getBusStop
import com.ldangelo.corunabuswear.data.source.parseConnectionsFromJsonArray
import org.json.JSONObject
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

interface IBusesRepository {
    fun updateSingleStop(stop: BusStop): BusStop
    fun updateAllStops(busStops: List<BusStop>): List<BusStop>
    fun getNearbyStops(loc: Location, prevStops: List<BusStop>): List<BusStop>
    fun updateDefinitions(): Triple<List<BusStop>, List<BusLine>, List<BusLine>>
    class UnknownDataException(message: String) : Exception(message)
}

@Singleton
class BusesRepository @Inject constructor(
    activity: Activity,
    private val busProvider: BusApi = BusProvider,
): IBusesRepository {
    private val context = activity.applicationContext
    private val mocking = false
    private val busLineCache = mutableMapOf<Int, BusLine>()

    override fun getNearbyStops(loc: Location, prevStops: List<BusStop>) : List<BusStop> {
        try {
            val radius = 1000
            val limit = 5
            val jsonStops: List<JSONObject> = if (mocking) {
                busProvider.mockStops()
            } else {
                busProvider.fetchStops(loc.latitude, loc.longitude, radius, limit)
            }
            
            val newStops = jsonStops.mapNotNull { stop ->
                val id = stop.getInt("parada")
                val storedStop = getBusStop<BusStop>(context, id.toString())
                storedStop?.apply { updateDistance(stop.getInt("distancia")) }
            }

            val stops = mutableListOf<BusStop>()
            newStops.forEach { newStop ->
                val existingStop = prevStops.find { it.id == newStop.id }
                if (existingStop != null) {
                    existingStop.updateDistance(newStop.distance)
                    stops.add(existingStop)
                } else {
                    stops.add(newStop)
                }
            }
            return stops

        } catch (e: BusProvider.TooManyRequestsException) {
            Log.d(TAG, "Too many requests: $e")
        } catch (e: SocketTimeoutException) {
            Log.d(TAG, "Connection error: $e")
        }
        return emptyList()
    }

    private fun getBusLineWithCache(lineaId: Int): BusLine? {
        return busLineCache.getOrPut(lineaId) {
            getBusLine<BusLine>(context, lineaId.toString()) ?: return null
        }
    }

    override fun updateSingleStop(stop: BusStop): BusStop {
        try {
            val busesJson = if (mocking) {
                busProvider.mockBuses()
            } else {
                fetchBuses(stop.id)
            }
            
            val buses = busesJson.mapNotNull { busObj ->
                try {
                    val lineaId = busObj.getInt("linea")
                    val busLine = getBusLineWithCache(lineaId) ?: return@mapNotNull null
                    val remainingTime = try { busObj.getInt("tiempo") } catch (e: Exception) { -1 }
                    Bus(busObj.getInt("bus"), busLine, remainingTime)
                } catch (e: Exception) {
                    null
                }
            }
            
            stop.updateBuses(buses)
            Log.d(TAG, "Updated stop $stop")
        } catch (e: BusProvider.TooManyRequestsException) {
            Log.d(TAG, "Too many requests: $e")
        } catch (e: SocketTimeoutException) {
            Log.d(TAG, "Connection error: $e")
        }
        return stop
    }

    override fun updateAllStops(busStops: List<BusStop>): List<BusStop> {
        return busStops.map { updateSingleStop(it) }
    }

    override fun updateDefinitions(): Triple<List<BusStop>, List<BusLine>, List<BusLine>> {
        try {
            val (stops, lines, jsonConnections) = busProvider.fetchStopsLinesData()
            var connections = parseConnectionsFromJsonArray(jsonConnections)
            connections = connections.filter { busLine -> !lines.any { it.id == busLine.id } }
            
            // Update cache when definitions are updated
            busLineCache.clear()
            lines.forEach { busLineCache[it.id] = it }
            connections.forEach { busLineCache[it.id] = it }

            return Triple(stops, lines, connections)
        } catch (e: BusProvider.TooManyRequestsException) {
            Log.d(TAG, "Too many requests: $e")
            throw e
        } catch (e: SocketTimeoutException) {
            Log.d(TAG, "Connection error: $e")
        }
        return Triple(emptyList(), emptyList(), emptyList())
    }

    companion object {
        const val TAG = "BusRepository"
    }
}