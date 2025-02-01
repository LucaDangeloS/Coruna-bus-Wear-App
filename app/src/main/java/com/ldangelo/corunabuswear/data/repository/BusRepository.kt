package com.ldangelo.corunabuswear.data.repository

import android.app.Activity
import android.location.Location
import android.util.Log
import com.ldangelo.corunabuswear.data.model.BusLine
import com.ldangelo.corunabuswear.data.model.BusStop
import com.ldangelo.corunabuswear.data.source.apis.BusApi
import com.ldangelo.corunabuswear.data.source.apis.BusProvider
import com.ldangelo.corunabuswear.data.source.apis.BusProvider.fetchBuses
import com.ldangelo.corunabuswear.data.source.local.getBusStop
import com.ldangelo.corunabuswear.data.source.parseBusFromJson
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


    override fun getNearbyStops(loc: Location, prevStops: List<BusStop>) : List<BusStop> {
        try {
            val radius = 1000
            val limit = 5
            // TODO: Remove
            val jsonStops: List<JSONObject>
            jsonStops = if (mocking) {
                busProvider.mockStops()
            } else {
                busProvider.fetchStops(loc.latitude, loc.longitude, radius, limit)
            }
            val newStops = mutableListOf<BusStop>()
            for (stop in jsonStops) {
                val id = stop.getInt("parada")
                val storedStop = getBusStop<BusStop>(context, id.toString())
                    ?: throw IBusesRepository.UnknownDataException("Bus stop $id does not exist in local storage")
                val distance = stop.getInt("distancia")
                storedStop.updateDistance(distance)
                newStops.add(storedStop)
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

    override fun updateSingleStop(stop: BusStop): BusStop {
        try {
            // TODO: Remove
            val buses = if (mocking) {
                busProvider.mockBuses().map { parseBusFromJson(it, context) }
            } else {
                fetchBuses(stop.id).map { parseBusFromJson(it, context) }
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
        val updatedStops = busStops.toMutableList().map { updateSingleStop(it) }
        return updatedStops
    }

    override fun updateDefinitions(): Triple<List<BusStop>, List<BusLine>, List<BusLine>> {
        try {
            val (stops, lines, jsonConnections) = busProvider.fetchStopsLinesData()
            var connections = parseConnectionsFromJsonArray(jsonConnections)
            connections = connections.filter { busLine -> !lines.any { it.id == busLine.id } }
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