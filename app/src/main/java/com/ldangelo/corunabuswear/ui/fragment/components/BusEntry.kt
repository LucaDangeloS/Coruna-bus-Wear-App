package com.ldangelo.corunabuswear.ui.fragment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.ldangelo.corunabuswear.data.model.Bus

@Composable
fun BusEntry(bus: Bus) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(bus.line.color, MaterialTheme.colors.background),
                    startX = 160f,
                    endX = 450f,
                    tileMode = androidx.compose.ui.graphics.TileMode.Clamp
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 14.dp)
                .clip(RoundedCornerShape(6.dp)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = bus.line.name,
                modifier = Modifier,
                color = MaterialTheme.colors.onPrimary,
                textAlign = TextAlign.Left,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = bus.getRemainingTime(),
                modifier = Modifier,
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.Right,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}