package com.example.coruabuswear.data.local
import android.content.Context
import com.example.coruabuswear.data.providers.BusProvider
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers

fun <T> saveBusStop(context: Context, key: String, obj: T) {
    return saveObject("BusStop", context, key, obj)
}

inline fun <reified T> getBusStop(context: Context, key: String): T? {
    return getObject("BusStop", context, key)
}

fun <T> saveBusLine(context: Context, key: String, obj: T) {
    return saveObject("BusLine", context, key, obj)
}

inline fun <reified T> getBusLine(context: Context, key: String): T? {
    return getObject("BusLine", context, key)
}

fun <T> saveObject(prefName: String, context: Context, key: String, obj: T) {
    val gson = Gson()
    val json = gson.toJson(obj)
    val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(key, json).apply()
}

// Function to retrieve a custom object from SharedPreferences
inline fun <reified T> getObject(prefName: String, context: Context, key: String): T? {
    val gson = Gson()
    val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    val json = sharedPreferences.getString(key, null)
    return gson.fromJson(json, T::class.java)
}

fun clearAllSharedPreferences(context: Context) {
    var sharedPreferences = context.getSharedPreferences("BusLine", Context.MODE_PRIVATE)
    var editor = sharedPreferences.edit()
    editor.clear()
    editor.apply()
    sharedPreferences = context.getSharedPreferences("BusStop", Context.MODE_PRIVATE)
    editor = sharedPreferences.edit()
    editor.clear()
    editor.apply()
}

