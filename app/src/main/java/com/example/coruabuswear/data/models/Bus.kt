package com.example.coruabuswear.data.models

data class Bus(val id: Int, val line: BusLine) {
    /*
        * -3: Not initialized
        * -2: 0 min
        * -1: <1 min
        * 0 = -2 (for sorting purposes)
     */
    var remainingTime: Int
    fun updateRemainingTime(remainingTime: Int) {
        if (remainingTime == 0) {
            this.remainingTime = -2
        } else {
            this.remainingTime = remainingTime
        }
    }
    constructor(id: Int, line: BusLine, remainingTime: Int) : this(id, line) {
        if (remainingTime == 0) {
            this.remainingTime = -2
        } else {
            this.remainingTime = remainingTime
        }
    }
    init {
        this.remainingTime = -3
    }

    @Override
    override fun toString(): String {
        return "Bus(id=$id, line=$line, remainingTime=$remainingTime)"
    }

    fun getRemainingTime(): String {
        return if (remainingTime == -2) {
            "N/A"
        } else if (remainingTime == -1) {
            "<1 min"
        } else if (remainingTime == 0) {
            // TODO I18N
            "En parada"
        } else {
            "$remainingTime min"
        }
    }
}
