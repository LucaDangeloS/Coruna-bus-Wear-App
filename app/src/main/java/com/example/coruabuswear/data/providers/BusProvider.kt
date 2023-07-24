package com.example.coruabuswear.data.providers

import android.util.Log
import com.example.coruabuswear.data.ApiConstants.BUS_API_ROOT
import com.example.coruabuswear.data.models.Bus
import com.example.coruabuswear.data.models.BusLine
import com.example.coruabuswear.data.models.BusStop
import com.example.coruabuswear.data.models.parseBusLinesFromJsonArray
import com.example.coruabuswear.data.models.parseBusStopsFromJsonArray
import okhttp3.OkHttpClient
import okhttp3.Request
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

        var busStops = emptyList<BusStop>()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            for ((name, value) in response.headers) {
                println("$name: $value")
            }
            // "posgps":[{"parada":437,"distancia":71},{"parada":185,"distancia":88},
            println(response.body!!.string())
        }

//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                e.printStackTrace()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//
//                    for ((name, value) in response.headers) {
//                        println("$name: $value")
//                    }
//
//                    println(response.body!!.string())
////                    _response = response.body!!.string()
//                }
//            }
//        })

        return busStops
    }
}