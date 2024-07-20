package com.ldangelo.corunabuswear.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.Wearable
import com.ldangelo.corunabuswear.data.MessagePaths.OUT.DEPLOY
import com.ldangelo.corunabuswear.ui.theme.CoruñaBusWearTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.ldangelo.corunabuswear.R
import com.ldangelo.corunabuswear.data.MessagePaths.GET_SETTINGS
import com.ldangelo.corunabuswear.data.MessagePaths.OUT.SET_SETTINGS
import com.ldangelo.corunabuswear.data.WearableSettingsConstants.PREFS_NAMES
import com.ldangelo.corunabuswear.data.WearableSettingsConstants.PREFS_OPTIONS
import com.ldangelo.corunabuswear.data.WearableSettingsConstants.PREFS_UNITS
import com.ldangelo.corunabuswear.data.WearableSettingsConstants.PREF_TYPES
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import org.json.JSONObject
import java.util.concurrent.CompletableFuture

class SettingsActivity : ComponentActivity() {
//    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
//    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Composable
        fun toastMessage(message: String, isError: Boolean = false) {

        }

        @Composable
        fun DropdownMenuPicker(
            header: String,
            items: List<String>,
            currentValue: String,
            units: String = "",
            onItemSelected: suspend (String) -> Unit
        ) {
            var expanded by remember { mutableStateOf(false) }
            var selectedItem by remember { mutableStateOf(currentValue) }
            var prevSelectedItem by remember { mutableStateOf(currentValue) }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = header)

                Button(onClick = { expanded = true }) {
                    Text("$selectedItem $units")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text("$item $units") },
                            onClick = {
                                selectedItem = item
                                expanded = false
                                lifecycleScope.launch {
                                    try {
                                        onItemSelected(item)
                                        prevSelectedItem = item
                                        // TODO: Add toast messages
                                    } catch (e : InterruptedException) {
                                        selectedItem = prevSelectedItem
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        @Composable
        fun SwitchMenuItem(
            header: String,
            currentValue: Boolean,
            onSwitched: suspend (Boolean) -> Unit
        ) {
            var checked by remember { mutableStateOf(currentValue) }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = header)

                Switch(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        lifecycleScope.launch {
                            try {
                                onSwitched(it)
                            } catch (e : InterruptedException) {
                                checked = !it
                            }
                        }
                    }
                )
            }
        }

        setContent {
            var isLoading by remember { mutableStateOf(true) }
            var prefsData by remember { mutableStateOf<MessageEvent?>(null) }
            var jsonData by remember { mutableStateOf<JSONObject?>(null) }

            LaunchedEffect(Unit) {
                prefsData = syncWearablePrefs()
                val data = prefsData?.data?.decodeToString() ?: ""
                jsonData = JSONObject(data)
                Log.d(TAG, "Ended syncWearablePrefs")
                isLoading = false
            }

            CoruñaBusWearTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    ) {
                        // Settings
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 0.dp, 0.dp, 0.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator()
                                } else if (prefsData == null) {
                                    // TODO: Error message on connection failure
                                    Text(text = "Error")
                                }
                            }
                        }

                        jsonData?.let {
                            for (i in PREFS_NAMES.iterator()) {
                                try {
                                    if (!it.has(i.key)) {
                                        Log.d(TAG, "Key not found: ${i.key}")
                                        continue
                                    }
                                    when (PREF_TYPES[i.key]) {
                                        Number::class -> item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(0.dp, 8.dp),
                                            ) {
                                                DropdownMenuPicker(
                                                    header = stringResource(i.value),
                                                    items = PREFS_OPTIONS[i.key]!!.toList(),
                                                    units = PREFS_UNITS[i.key]!!,
                                                    currentValue = it.getString(i.key),
                                                    onItemSelected = { item ->
                                                        onSettingChanged(
                                                            item.toInt(),
                                                            i.key
                                                        )
                                                    }
                                                )
                                            }
                                        }

                                        Boolean::class -> item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(0.dp, 8.dp),
                                            ) {
                                                SwitchMenuItem(
                                                    header = stringResource(i.value),
                                                    currentValue = it.getBoolean(i.key),
                                                    onSwitched = { isChecked ->
                                                        onSettingChanged(
                                                            isChecked,
                                                            i.key
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } catch (exception: Exception) {
                                    Log.d(TAG, "Error: $exception")
                                    continue
                                }
                            }
                        }

                        // "Launch app" button
                        item {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = {
                                        startWearableActivity()
                                    },
                                    enabled = true,
                                    modifier = Modifier.padding(0.dp, 16.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.launch_wearable_app),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun retrieveNodes(): MutableList<Node> {
        return Wearable.getNodeClient(this@SettingsActivity).connectedNodes.await()
    }

    private suspend fun syncWearablePrefs() : MessageEvent? {
        var messageEvent : MessageEvent? = null
        try {
            val nodes = retrieveNodes()

            if (nodes.isEmpty()) {
                // TODO: toast message
                return null
            }

            val future = CompletableFuture<MessageEvent>()
            val listener = fun (messageEvent: MessageEvent) {
                if (messageEvent.path == GET_SETTINGS) {
                    future.complete(messageEvent)
                }
            }

            messageClient.addListener { msg ->
                listener(msg)
            }

            // Send request message
            nodes.map { node ->
                messageClient.sendMessage(node.id, GET_SETTINGS, byteArrayOf())
                    .await()
            }

            Log.d(TAG, "Syncing wearable preferences requests sent successfully")

            // Wait for the response
            messageEvent =  future.await()

            messageClient.removeListener { msg ->
                listener(msg)
            }

        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Syncing wearable preferences failed: $exception")
        }
        return messageEvent
    }

    private suspend fun onSettingChanged(newValue: Any, settingPath: String) {
        try {
            val nodes = retrieveNodes()
            var wasSuccessful = false
            nodes.map { node ->
                messageClient.sendMessage(node.id, "$SET_SETTINGS/$settingPath", newValue.toString().toByteArray())
                    .await().apply {
                        wasSuccessful = true
                    }
            }
            if (!wasSuccessful) {
                throw InterruptedException("Failed to change setting")
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        }
        catch (exception: Exception) {
            throw exception
        }
    }

    private fun startWearableActivity() {
        lifecycleScope.launch {
            try {
                val nodes = retrieveNodes()

                if (nodes.isEmpty()) {
                    // toast message
                    return@launch
                }

                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, DEPLOY, byteArrayOf())
                            .await()
                    }
                }.awaitAll()

                Log.d(TAG, "Starting activity requests sent successfully")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            }
            catch (exception: Exception) {
                Log.d(TAG, "Starting activity failed: $exception")
            }
        }
    }

    companion object {
        private const val TAG = "DataLayerService"
    }

//    override fun onMessageReceived(messageEvent: MessageEvent) {
//        when (messageEvent.path) {
//            GET_SETTINGS -> {
//                future.complete(messageEvent)
//            }
//        }
//    }
}
