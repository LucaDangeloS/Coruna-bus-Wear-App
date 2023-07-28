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

    @Override
    override fun toString(): String {
        return "Bus(id=$id, line=$line, remainingTime=$remainingTime)"
    }
}
