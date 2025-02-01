package com.ldangelo.corunabuswear.data

object ApiConstants {
    const val BUS_API_ROOT = "https://itranvias.com/queryitr_v3.php?"
    const val BUS_API_FETCH_TIME = 20000L
    const val MINUTE_API_LIMIT = 4
}

object AppConstants {
    const val DEBUG = true
    const val SETTINGS_PREF = "settings"

    const val STOPS_FETCH_KEY = "stops_fetch"
    const val DEFAULT_STOPS_FETCH = 5

    const val STOPS_RADIUS_KEY = "stops_radius"
    const val DEFAULT_STOPS_RADIUS = 1000

    const val LOC_DISTANCE_KEY = "location_distance"
    const val DEFAULT_LOC_DISTANCE = 30f

    const val LOC_INTERVAL_KEY = "location_interval"
    const val DEFAULT_LOC_INTERVAL = 30000L

    const val FETCH_ALL_BUSES_ON_LOCATION_UPDATE_KEY = "fetch_all_buses_on_location_update"
    const val DEFAULT_FETCH_ALL_BUSES_ON_LOCATION_UPDATE = true
}