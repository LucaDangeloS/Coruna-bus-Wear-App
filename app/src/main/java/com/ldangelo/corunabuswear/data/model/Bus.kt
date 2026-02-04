package com.ldangelo.corunabuswear.data.model

data class Bus(
    val id: Int,
    val line: BusLine,
    val remainingTime: Int = -3
) {
    /*
        * -3: Not initialized
        * -2: 0 min
        * -1: <1 min
        * 0 = -2 (for sorting purposes)
     */
    fun getRemainingTime(): String {
        return if (remainingTime == -2 || remainingTime == 0) {
            "En parada"
        } else if (remainingTime == -1) {
            "<1 min"
        } else if (remainingTime == -3) {
            "N/A"
        } else {
            "$remainingTime min"
        }
    }

    @Override
    override fun toString(): String {
        return "Bus(id=$id, line=$line, remainingTime=$remainingTime)"
    }
}
