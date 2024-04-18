package com.ldangelo.corunabuswear.presentation.components.composed

import android.os.Vibrator
import android.util.Log
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.ldangelo.corunabuswear.data.viewmodels.BusStopViewModel
import com.ldangelo.corunabuswear.data.viewmodels.BusStopsListViewModel
import com.ldangelo.corunabuswear.presentation.components.BusStopPage
import com.ldangelo.corunabuswear.presentation.components.PagerScaffolding
import com.ldangelo.corunabuswear.presentation.components.StopsPage


@Composable
fun UpdateUIWithBuses(busStopViewModel: BusStopsListViewModel, currentPageIndex: MutableState<Int>, vibrator: Vibrator?, onBackPressedDispatcher: OnBackPressedDispatcher?) {
    val busStops : List<BusStopViewModel> by busStopViewModel.busStops.observeAsState(emptyList())

    Log.d("DEBUG_TAG", "Update UI with buses method called")
    if (busStops.isEmpty()) {
        UpdateUINoStops()
        return
    }
    UIWithBuses(busStopViewModel, currentPageIndex, vibrator, onBackPressedDispatcher)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UIWithBuses(busStopViewModel: BusStopsListViewModel, currentPageIndexState: MutableState<Int>, vibrator: Vibrator?, onBackPressedDispatcher: OnBackPressedDispatcher?) {
    val busStops : List<BusStopViewModel> by busStopViewModel.busStops.observeAsState(emptyList())
    val currentPageIndex by currentPageIndexState
    val pagerState = rememberPagerState(
        initialPage = currentPageIndex,
        pageCount = { busStops.size + 1 },
    )
    val animationScope = rememberCoroutineScope()

    PagerScaffolding(pagerState, currentPageIndexState, { busStops.size + 1 }, animationScope, vibrator, onBackPressedDispatcher) {
        // TODO: Implement page specific periodic fetching
        // timeout like 10 seconds change
        if (it == 0) {
            Column {
                StopsPage(busStops, pagerState, animationScope)
            }
        } else {
            BusStopPage(busStops[it - 1])
        }
    }
}