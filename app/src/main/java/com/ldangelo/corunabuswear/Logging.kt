package com.ldangelo.corunabuswear

import android.content.Context
import android.util.Log

object Logger {
    private const val TAG = "CoruñaBusWear" // Your app's tag
    const val LOG_FILE = "log_"

    fun v(message: String) {
        Log.v(TAG, message)
    }

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }

    fun e(message: String) {
        Log.e(TAG, message)
    }

    fun e(message: String, throwable: Throwable) {
        Log.e(TAG, message, throwable)
    }

    fun saveLog(context: Context, log: String) {
        context.openFileOutput(LOG_FILE + java.time.LocalDateTime.now().toString() + ".log", Context.MODE_PRIVATE).use {
            it.write(log.toByteArray())
        }
    }
}