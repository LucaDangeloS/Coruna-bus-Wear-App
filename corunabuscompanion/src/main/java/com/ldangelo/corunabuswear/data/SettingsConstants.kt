package com.ldangelo.corunabuswear.data

import com.ldangelo.corunabuswear.R

object WearableSettingsConstants {
    const val STOPS_FETCH_KEY = "stops_fetch"
    const val STOPS_RADIUS_KEY = "stops_radius"
    const val LOC_DISTANCE_KEY = "location_distance"
    const val LOC_INTERVAL_KEY = "location_interval"
    const val FETCH_ALL_BUSES_ON_LOCATION_UPDATE_KEY = "fetch_all_buses_on_location_update"

    val PREFS_NAMES : Map<String, Int> = mapOf(
        STOPS_FETCH_KEY to R.string.stops_fetch_setting,
        STOPS_RADIUS_KEY to R.string.stops_radius_setting,
        LOC_DISTANCE_KEY to R.string.loc_distance_setting,
        LOC_INTERVAL_KEY to R.string.loc_interval_setting,
        FETCH_ALL_BUSES_ON_LOCATION_UPDATE_KEY to R.string.fetch_all_buses_on_location_update_setting,
    )

    val PREF_TYPES : Map<String, Any> = mapOf(
        STOPS_FETCH_KEY to Number::class,
        STOPS_RADIUS_KEY to Number::class,
        LOC_DISTANCE_KEY to Number::class,
        LOC_INTERVAL_KEY to Number::class,
        FETCH_ALL_BUSES_ON_LOCATION_UPDATE_KEY to Boolean::class,
    )

    val PREFS_OPTIONS : Map<String, Array<String>> = mapOf(
        STOPS_FETCH_KEY to arrayOf("1", "2", "3", "4", "5", "6"),
        STOPS_RADIUS_KEY to arrayOf("500", "1000", "1500", "2000", "2500"),
        LOC_DISTANCE_KEY to arrayOf("20", "30", "40", "50"),
        LOC_INTERVAL_KEY to arrayOf("20000", "30000", "40000", "50000"),
    )

    val PREFS_UNITS : Map<String, String> = mapOf(
        STOPS_FETCH_KEY to "",
        STOPS_RADIUS_KEY to "m",
        LOC_DISTANCE_KEY to "m",
        LOC_INTERVAL_KEY to "ms"
    )
}