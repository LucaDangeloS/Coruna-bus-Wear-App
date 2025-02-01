package com.ldangelo.corunabuswear.ui.fragment.components

import android.os.Vibrator
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.ldangelo.corunabuswear.data.viewmodels.BusStopsListViewModel
import com.ldangelo.corunabuswear.ui.fragment.BusStopFragment
import com.ldangelo.corunabuswear.ui.fragment.StopsPageFragment
import com.ldangelo.corunabuswear.ui.theme.wearColorPalette
import kotlinx.coroutines.launch

@Composable
fun UIWithBuses(
    busStopViewModel: BusStopsListViewModel,
    currentPageIndexState: MutableState<Int>,
    vibrator: Vibrator?,
    onBackPressedDispatcher: OnBackPressedDispatcher,
    newPageIndex: Int = -1,
    shouldScroll: MutableState<Boolean>,
    onStopPageScrollCallback: (Int) -> Unit = {},
) {
    if (busStopViewModel.busStops.collectAsStateWithLifecycle().value.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = wearColorPalette.primary,
                    text = "No hay paradas cercanas"
                )
            }
        }
        return
    }

    val busStops by busStopViewModel.busStops.collectAsStateWithLifecycle()
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
            }
        }
        shouldScroll.value = false
    }

    // update currentPageIndexState when pagerState.currentPage changes
    currentPageIndexState.value = pagerState.currentPage
    onStopPageScrollCallback(pagerState.currentPage)

    PagerScaffolding(
        pagerState,
        { busStops.size + 1 },
        animationScope,
        vibrator,
        onBackPressedDispatcher
    ) {
        if (it == 0) {
            Column {
                StopsPageFragment(busStops, pagerState, animationScope)
            }
        } else {
            BusStopFragment(busStops[it - 1])
        }
    }
}