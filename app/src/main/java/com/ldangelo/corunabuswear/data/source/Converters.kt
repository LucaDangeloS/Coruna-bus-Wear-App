package com.ldangelo.corunabuswear.data.source

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.ldangelo.corunabuswear.data.model.Bus
import com.ldangelo.corunabuswear.data.model.BusLine
import com.ldangelo.corunabuswear.data.repository.IBusesRepository
import com.ldangelo.corunabuswear.data.source.apis.BusProvider.BusLineIsConnection
import com.ldangelo.corunabuswear.data.source.local.getBusConnection
import com.ldangelo.corunabuswear.data.source.local.getBusLine
import org.json.JSONArray
import org.json.JSONObject

// JSON parsing
fun parseBusFromJson(busObj: JSONObject, context: Context): Bus {
    val lineaId: Int = busObj.getInt("linea")
    val busLine = getBusLine<BusLine>(context, lineaId.toString())
    if (busLine == null) {
        if (getBusConnection<BusLine>(context, lineaId.toString()) != null) {
            throw BusLineIsConnection("Bus line $lineaId is a connection")
        }
        throw IBusesRepository.UnknownDataException("Bus line $lineaId does not exist in local storage")
    }
    val remainingTime = try {busObj.getInt("tiempo")} catch (e: Exception) {-1}
    return Bus(busObj.getInt("bus"), busLine, remainingTime)
}

// Connections
fun parseConnectionFromJson(json: JSONObject): List<BusLine> {
    val placeholderColor = Color(android.graphics.Color.parseColor("#000000"))
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

fun parseConnectionsFromJsonArray(jsonArray: JSONArray): List<BusLine> {
    val busLines = mutableListOf<BusLine>()
    for (i in 0 until jsonArray.length()) {
        val json = jsonArray.getJSONObject(i)
        val busLine = parseConnectionFromJson(json)
        busLines.addAll(busLine)
    }

    // Remove duplicates
    return busLines.distinctBy { it.id }
}