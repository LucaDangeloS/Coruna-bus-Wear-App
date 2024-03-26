package com.ldangelo.corunabuswear.data.services

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.ldangelo.corunabuswear.SyncActivity

class SettingsListener: WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
//        Log.d(TAG, String(messageEvent.data))
        if (messageEvent.path == MESSAGE_PATH) {
            val startIntent = Intent(this, SyncActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(startIntent)
        }
    }

    companion object{
        private const val MESSAGE_PATH = "/deploy/settings"
    }
}
