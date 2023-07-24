package com.example.coruabuswear.data.models

data class Bus(val id: Int, val line: BusLine) {
    var remainingTime: Int

    fun updateRemainingTime(remainingTime: Int) {
        this.remainingTime = remainingTime
    }
    constructor(id: Int, line: BusLine, remainingTime: Int) : this(id, line) {
        this.remainingTime = remainingTime
    }
    init {
        this.remainingTime = -1
    }

//    fun parseFromJson(json: String): Bus {
//        return Bus(0, 0, BusLine("", Color(0)))
//    }
}
