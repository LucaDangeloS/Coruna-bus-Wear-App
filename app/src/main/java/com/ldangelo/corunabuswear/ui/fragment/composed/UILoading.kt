package com.ldangelo.corunabuswear.ui.fragment.composed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.ldangelo.corunabuswear.data.ContextHolder
import com.ldangelo.corunabuswear.data.wearDatalayer.openSettings
import com.ldangelo.corunabuswear.ui.fragment.GearButton
import com.ldangelo.corunabuswear.ui.theme.wearColorPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UpdateUILoading(loadingText: String? = null) {
    val endText = ""
    Scaffold (
        timeText = {
            TimeText(
                modifier = Modifier.padding(2.dp),
                // reduce font
                timeTextStyle =
                TextStyle(
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onSecondary,
                ),
//                        endCurvedContent = {
//                            basicCurvedText(
//                                endText,
//                                style = {
//                                    CurvedTextStyle(
//                                        fontSize = 12.sp,
//                                        color = MaterialTheme.colors.onSecondary,
//                                    )
//                                },
//                            )
//                        }
            )
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .displayCutoutPadding(),
                    indicatorColor = wearColorPalette.primary,
                    trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.3f),
                    strokeWidth = 6.dp
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(6.dp),
                    text = loadingText ?: "",
                    color = MaterialTheme.colors.onBackground,
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp, 0.dp, 0.dp, 14.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    GearButton(onClick = {
                        val context = ContextHolder.getApplicationContext()
                        ContextHolder.getLifecycleScope().launch(Dispatchers.IO) {
                            openSettings(context)
                        }
                    })
                }
            }
        }
    }
}