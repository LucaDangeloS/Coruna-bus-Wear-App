package com.ldangelo.corunabuswear.data.wearDatalayer.service

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.ldangelo.corunabuswear.data.AppConstants.DEFAULT_FETCH_ALL_BUSES_ON_LOCATION_UPDATE
import com.ldangelo.corunabuswear.data.AppConstants.DEFAULT_LOC_DISTANCE
import com.ldangelo.corunabuswear.data.AppConstants.DEFAULT_LOC_INTERVAL
import com.ldangelo.corunabuswear.data.AppConstants.DEFAULT_STOPS_FETCH
import com.ldangelo.corunabuswear.data.AppConstants.DEFAULT_STOPS_RADIUS
import com.ldangelo.corunabuswear.data.AppConstants.FETCH_ALL_BUSES_ON_LOCATION_UPDATE_KEY
import com.ldangelo.corunabuswear.data.AppConstants.LOC_DISTANCE_KEY
import com.ldangelo.corunabuswear.data.AppConstants.LOC_INTERVAL_KEY
import com.ldangelo.corunabuswear.data.AppConstants.SETTINGS_PREF
import com.ldangelo.corunabuswear.data.AppConstants.STOPS_FETCH_KEY
import com.ldangelo.corunabuswear.data.AppConstants.STOPS_RADIUS_KEY
import com.ldangelo.corunabuswear.data.wearDatalayer.MessagePaths.IN.DEPLOY
import com.ldangelo.corunabuswear.data.wearDatalayer.MessagePaths.GET_SETTINGS
import com.ldangelo.corunabuswear.data.wearDatalayer.MessagePaths.IN.SET_SETTINGS
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import com.ldangelo.corunabuswear.data.wearDatalayer.MessagePaths.SETTINGS
import com.ldangelo.corunabuswear.data.wearDatalayer.sendCurrentSettings
import com.ldangelo.corunabuswear.data.source.local.getStringOrDefaultPreference
import com.ldangelo.corunabuswear.data.source.local.saveLog
import com.ldangelo.corunabuswear.data.source.local.saveStringPreference
import com.ldangelo.corunabuswear.activity.MainActivity
import org.json.JSONObject


class DataLayerListenerService : WearableListenerService() {
    private val messageClient by lazy { Wearable.getMessageClient(this) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { dataEvent ->
            val uri = dataEvent.dataItem.uri
            when (uri.path) {
                SETTINGS -> {
                    try {
                        val nodeId = uri.host!!
                        val payload = uri.toString().toByteArray()

                    } catch (cancellationException: CancellationException) {
                        throw cancellationException
                    } catch (exception: Exception) {
                        Log.d(TAG, "Message failed")
                    }

                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Received message: ${messageEvent.path}")

        when (messageEvent.path) {
            DEPLOY -> {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                return
            }

            GET_SETTINGS -> {
                val settingsJson = JSONObject()

                settingsJson.put(STOPS_FETCH_KEY, getStringOrDefaultPreference(
                    SETTINGS_PREF,
                    this,
                    STOPS_FETCH_KEY,
                    DEFAULT_STOPS_FETCH.toString()
                )
                )
                settingsJson.put(STOPS_RADIUS_KEY, getStringOrDefaultPreference(
                    SETTINGS_PREF,
                    this,
                    STOPS_RADIUS_KEY,
                    DEFAULT_STOPS_RADIUS.toString()
                )
                )
                settingsJson.put(LOC_DISTANCE_KEY, getStringOrDefaultPreference(
                    SETTINGS_PREF,
                    this,
                    LOC_DISTANCE_KEY,
                    DEFAULT_LOC_DISTANCE.toInt().toString()
                )
                )
                settingsJson.put(LOC_INTERVAL_KEY, getStringOrDefaultPreference(
                    SETTINGS_PREF,
                    this,
                    LOC_INTERVAL_KEY,
                    DEFAULT_LOC_INTERVAL.toString()
                )
                )
                settingsJson.put(FETCH_ALL_BUSES_ON_LOCATION_UPDATE_KEY, getStringOrDefaultPreference(
                    SETTINGS_PREF,
                    this,
                    FETCH_ALL_BUSES_ON_LOCATION_UPDATE_KEY,
                    DEFAULT_FETCH_ALL_BUSES_ON_LOCATION_UPDATE.toString()
                )
                )

                Log.d(TAG, "Sending settings: $settingsJson")
                sendCurrentSettings(this, settingsJson.toString())
                return
            }
        }

        if (messageEvent.path.startsWith(SET_SETTINGS)) {
            val stringEndpoint = messageEvent.path.split("/").last { it.isNotEmpty() }
            val value = messageEvent.data.decodeToString()
            try {
                saveStringPreference(SETTINGS_PREF, this, stringEndpoint, value)
            } catch (e: Exception) {
                Log.d(TAG, "Failed to save setting $stringEndpoint : $value")
                saveLog(this, "Failed to save setting $stringEndpoint : $value")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        private const val TAG = "DataLayerService"
    }
}