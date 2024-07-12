package com.ldangelo.corunabuswear.data.companion

import android.content.Context
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Wearable
import com.ldangelo.corunabuswear.R
import com.ldangelo.corunabuswear.data.companion.MessagePaths.DEPLOY_SETTINGS
import com.ldangelo.corunabuswear.presentation.components.getNodes

fun emitMessage(context: Context, path: String, message: String): Task<Int> {
    // TODO: Fix when there are no nodes
    getNodes(context).first().also {
        return Wearable.getMessageClient(context).sendMessage(
            it,
            path,
            message.toByteArray()
        )
    }
}

fun openSettings(context: Context) {
    val sendTask: Task<*> = emitMessage(context, DEPLOY_SETTINGS, "")
    sendTask.apply {
        addOnSuccessListener {
            Toast.makeText(context, R.string.settings_open, Toast.LENGTH_SHORT).show()
        }
        addOnFailureListener {
            Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show()
        }
    }
}