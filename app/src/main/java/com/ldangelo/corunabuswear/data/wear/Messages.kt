package com.ldangelo.corunabuswear.data.wear

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import com.ldangelo.corunabuswear.R
import com.ldangelo.corunabuswear.data.wear.MessagePaths.GET_SETTINGS
import com.ldangelo.corunabuswear.data.wear.MessagePaths.OUT.DEPLOY_SETTINGS

fun getNodes(context: Context): Collection<String> {
    return Tasks.await(Wearable.getNodeClient(context).connectedNodes).map { it.id }
}

fun emitMessage(context: Context, path: String, message: String): Task<Int> {
    val nodes = getNodes(context)
    val firstNode = nodes.firstOrNull() ?: throw IllegalStateException("No nodes found")
    firstNode.also {
        Log.d("DataLayerService", "$message sent to as ${message.toByteArray()}")
        return Wearable.getMessageClient(context).sendMessage(
            it,
            path,
            message.toByteArray()
        )
    }
}

fun broadcastMessage(context: Context, path: String, message: String): Task<List<Int>> {
    val nodes = getNodes(context)
    Log.d("DataLayerService", nodes.toString());
    val tasks = nodes.map {
        Wearable.getMessageClient(context).sendMessage(
            it,
            path,
            message.toByteArray()
        )
    }
    return Tasks.whenAllSuccess<Int>(tasks)
}

fun openSettings(context: Context) {
    val sendTask: Task<*>;
    try {
//        sendTask = emitMessage(context, DEPLOY_SETTINGS, "")
        sendTask = broadcastMessage(context, DEPLOY_SETTINGS, "")
    } catch (e: IllegalStateException) {
        Toast.makeText(context, R.string.no_device_found, Toast.LENGTH_SHORT).show()
        return
    }
    sendTask.apply {
        addOnSuccessListener {
            Toast.makeText(context, R.string.settings_open, Toast.LENGTH_SHORT).show()
        }
        addOnFailureListener {
            Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show()
        }
    }
}

fun sendCurrentSettings(context: Context, settings: String) {
    val sendTask: Task<*>;
    try {
        sendTask = emitMessage(context, GET_SETTINGS, settings)
    } catch (e: IllegalStateException) {
        return
    }

    sendTask.apply {
        addOnSuccessListener {

        }
        addOnFailureListener {

        }
    }
}