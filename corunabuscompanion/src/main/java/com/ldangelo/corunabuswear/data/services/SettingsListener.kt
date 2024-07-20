package com.ldangelo.corunabuswear.data.services

import android.content.Intent
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.ldangelo.corunabuswear.activities.SettingsActivity
import com.ldangelo.corunabuswear.data.MessagePaths.IN.DEPLOY_SETTINGS

class SettingsListener: WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            DEPLOY_SETTINGS -> {
                startActivity(Intent(this, SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }
}
