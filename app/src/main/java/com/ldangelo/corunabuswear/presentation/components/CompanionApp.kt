package com.ldangelo.corunabuswear.presentation.components

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable

fun getNodes(context: Context): Collection<String> {
    return Tasks.await(Wearable.getNodeClient(context).connectedNodes).map { it.id }
}