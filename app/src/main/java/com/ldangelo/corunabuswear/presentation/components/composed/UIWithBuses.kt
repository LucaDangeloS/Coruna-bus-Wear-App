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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.ldangelo.corunabuswear.data.viewmodels.BusStopViewModel
import com.ldangelo.corunabuswear.data.viewmodels.BusStopsListViewModel
import com.ldangelo.corunabuswear.presentation.components.BusStopPage
import com.ldangelo.corunabuswear.presentation.components.PagerScaffolding
import com.ldangelo.corunabuswear.presentation.components.StopsPage
import kotlinx.coroutines.launch


@Composable
fun UpdateUIWithBuses(
    busStopViewModel: BusStopsListViewModel,
    currentPageIndex: MutableState<Int>,
    vibrator: Vibrator?,
    onBackPressedDispatcher: OnBackPressedDispatcher?,
    newPageIndex: Int = -1,
    shouldScroll: MutableState<Boolean>
) {
    val busStops: List<BusStopViewModel> by busStopViewModel.busStops.observeAsState(emptyList())

    Log.d("DEBUG_TAG", "Update UI with buses method called from ${currentPageIndex.value} to $newPageIndex")
    if (busStops.isEmpty()) {
        UpdateUINoStops()
        return
    }

    UIWithBuses(busStopViewModel, currentPageIndex, vibrator, onBackPressedDispatcher, newPageIndex, shouldScroll)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UIWithBuses(
    busStopViewModel: BusStopsListViewModel,
    currentPageIndexState: MutableState<Int>,
    vibrator: Vibrator?,
    onBackPressedDispatcher: OnBackPressedDispatcher?,
    newPageIndex: Int = -1,
    shouldScroll: MutableState<Boolean>
) {
    val busStops: List<BusStopViewModel> by busStopViewModel.busStops.observeAsState(emptyList())
    val currentPageIndex by currentPageIndexState
    val pagerState = rememberPagerState(
        initialPage = currentPageIndex,
        pageCount = { busStops.size + 1 },
    )
    val animationScope = rememberCoroutineScope()

    // goto to the new page where the user was
    if (newPageIndex != -1 && shouldScroll.value) {
        with(animationScope) {
            launch {
                pagerState.animateScrollToPage(newPageIndex)
                shouldScroll.value = false
            }
        }
    }

    PagerScaffolding(
        pagerState,
        currentPageIndexState,
        { busStops.size + 1 },
        animationScope,
        vibrator,
        onBackPressedDispatcher
    ) {
        if (it == 0) {
            Column {
                StopsPage(busStops, pagerState, animationScope)
            }
        } else {
            BusStopPage(busStops[it - 1])
        }
    }
}