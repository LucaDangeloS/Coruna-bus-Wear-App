package com.example.coruabuswear.data.models

import org.json.JSONArray
import org.json.JSONObject
import android.graphics.Color
import androidx.compose.ui.graphics.Color as Colorx

data class BusLine (
    val id : Int,
    val name: String,
    val color: Colorx,
) {

}
fun parseBusLineFromJson(jsonStr: String): BusLine {
    val json = JSONObject(jsonStr)
    val id = json.getInt("id")
    val name = json.getString("lin_comer")
    val color = Colorx(Color.parseColor("#${json.getString("color")}"))
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