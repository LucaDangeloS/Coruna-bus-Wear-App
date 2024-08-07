package com.ldangelo.corunabuswear.data.local
import android.content.Context
import com.ldangelo.corunabuswear.data.AppConstants.LOG_FILE
import com.google.gson.Gson

fun <BusStop> saveBusStop(context: Context, key: String, obj: BusStop) {
    return saveObject("BusStop", context, key, obj)
}

inline fun <reified BusStop> getBusStop(context: Context, key: String): BusStop? {
    return getObject<BusStop>("BusStop", context, key)
}

fun <BusLine> saveBusLine(context: Context, key: String, obj: BusLine) {
    return saveObject("BusLine", context, key, obj)
}

inline fun <reified BusLine> getBusLine(context: Context, key: String): BusLine? {
    return getObject<BusLine>("BusLine", context, key)
}

fun <BusLine> saveBusConnection(context: Context, key: String, obj: BusLine) {
    return saveObject("BusConnection", context, key, obj)
}

inline fun <reified BusLine> getBusConnection(context: Context, key: String): BusLine? {
    return getObject<BusLine>("BusConnection", context, key)
}

fun getStringOrDefaultPreference(prefName: String, context: Context, key: String, default: String): String {
    val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    return sharedPreferences.getString(key, default) ?: default
}

fun saveStringPreference(prefName: String, context: Context, key: String, value: String) {
    val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(key, value).apply()
}

fun clearAllStoredBusData(context: Context) {
    var sharedPreferences = context.getSharedPreferences("BusLine", Context.MODE_PRIVATE)
    var editor = sharedPreferences.edit()
    editor.clear()
    editor.apply()
    sharedPreferences = context.getSharedPreferences("BusStop", Context.MODE_PRIVATE)
    editor = sharedPreferences.edit()
    editor.clear()
    editor.apply()
}

// Functions to store logs, appending to the previous log with date and time
fun saveLog(context: Context, log: String) {
//    val sharedPreferences = context.getSharedPreferences("Logs", Context.MODE_PRIVATE)
//    val editor = sharedPreferences.edit()
//    // put date and time in the log
//    editor.putString(java.time.LocalDateTime.now().toString(), "$log")
//    editor.apply()
    context.openFileOutput(LOG_FILE + java.time.LocalDateTime.now().toString() + ".log", Context.MODE_PRIVATE).use {
        it.write(log.toByteArray())
    }
}

// Functions to save and retrieve a custom objects from SharedPreferences
fun <T> saveObject(prefName: String, context: Context, key: String, obj: T) {
    val gson = Gson()
    val json = gson.toJson(obj)
    val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(key, json).apply()
}

inline fun <reified T> getObject(prefName: String, context: Context, key: String): T? {
    val gson = Gson()
    val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    val json = sharedPreferences.getString(key, null)
    return gson.fromJson(json, T::class.java)
}
