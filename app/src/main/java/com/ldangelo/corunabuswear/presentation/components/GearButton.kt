package com.ldangelo.corunabuswear.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ldangelo.coruabuswear.R

// Create a gear floating circular button
@Composable
fun GearButton(onClick: () -> Unit) {
    FloatingActionButton(onClick = {
        onClick()
    },
        shape = MaterialTheme.shapes.small,
        containerColor = MaterialTheme.colors.background,
        elevation = FloatingActionButtonDefaults.elevation(),
        modifier = Modifier.size(24.dp)
    ) {
        // Gear icon
        Icon(
            Icons.Rounded.Settings,
            contentDescription = stringResource(id = R.string.settings_complication_id),
            tint = MaterialTheme.colors.onBackground,
            modifier = Modifier.size(18.dp)
        )
    }
}