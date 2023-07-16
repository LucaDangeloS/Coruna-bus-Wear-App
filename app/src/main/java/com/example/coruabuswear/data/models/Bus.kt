package com.example.coruabuswear.data.models

data class Bus(
    val id: Int,
    var remainingTime: Int,
    val line: BusLine,
) {
    fun updateRemainingTime(remainingTime: Int) {
        this.remainingTime = remainingTime
    }

//    fun parseFromJson(json: String): Bus {
//        return Bus(0, 0, BusLine("", Color(0)))
//    }
}
