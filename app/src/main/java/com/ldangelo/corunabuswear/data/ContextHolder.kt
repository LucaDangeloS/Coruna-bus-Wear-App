package com.ldangelo.corunabuswear.data

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope

object ContextHolder {
    private lateinit var appContext: Context
    private lateinit var lifecycleScope: LifecycleCoroutineScope
    fun setApplicationContext(context: Context) {
        appContext = context.applicationContext
    }

    fun setLifecycleScope(scope: LifecycleCoroutineScope) {
        lifecycleScope = scope
    }

    fun getApplicationContext(): Context {
        return appContext
    }

    fun getLifecycleScope(): LifecycleCoroutineScope {
        return lifecycleScope
    }
}