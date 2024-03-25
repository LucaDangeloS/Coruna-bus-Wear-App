package com.ldangelo.corunabuswear.data

import android.content.Context

object ContextHolder {
    private lateinit var appContext: Context

    fun setApplicationContext(context: Context) {
        appContext = context.applicationContext
    }

    fun getApplicationContext(): Context {
        return appContext
    }
}