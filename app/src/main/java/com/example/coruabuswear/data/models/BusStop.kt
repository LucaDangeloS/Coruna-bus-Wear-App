package com.example.coruabuswear.data.models

class BusStop (
    val name: String?,
    val code: Int,
    val distance: Int,
    val buses: List<Bus>,
) {
//    fun parseFromJson(json: String): BusStop {
//        return BusStop("", 0, 0, listOf(Bus(0, 0, BusLine("", Color(0)))))
//    }
}