package com.ldangelo.corunabuswear.presentation.components.composed

import android.content.Context
import android.os.Vibrator
import android.util.Log
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.ldangelo.corunabuswear.data.models.BusStop
import com.ldangelo.corunabuswear.presentation.components.BusStopPage
import com.ldangelo.corunabuswear.presentation.components.PagerScaffolding
import com.ldangelo.corunabuswear.presentation.components.StopsPage


@Composable
fun UpdateUIWithBuses(busStops: List<BusStop>, context: Context, vibrator: Vibrator?, onBackPressedDispatcher: OnBackPressedDispatcher?) {
    Log.d("DEBUG_TAG", "Update UI with buses method called")
    if (busStops.isEmpty()) {
        UpdateUINoStops()
        return
    }
    UIWithBuses(busStops, context, vibrator, onBackPressedDispatcher)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UIWithBuses(busStops: List<BusStop>, context: Context, vibrator: Vibrator?, onBackPressedDispatcher: OnBackPressedDispatcher?) {
    val currentPageIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = currentPageIndex,
        pageCount = { busStops.size + 1 },
    )
    val animationScope = rememberCoroutineScope()

    PagerScaffolding(pagerState, { busStops.size + 1 }, animationScope, vibrator, onBackPressedDispatcher) { it ->
        if (it == 0) {
            Column {
                StopsPage(busStops, pagerState, animationScope)
            }
        } else {
            BusStopPage(busStops[it - 1])
        }
    }
}