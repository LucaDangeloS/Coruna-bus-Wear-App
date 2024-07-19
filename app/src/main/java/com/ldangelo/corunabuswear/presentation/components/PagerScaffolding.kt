package com.ldangelo.corunabuswear.presentation.components

import android.os.Vibrator
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerScaffolding(pagerState: PagerState,
                     maxPages: () -> Int,
                     animationScope: CoroutineScope?,
                     vibrator: Vibrator?,
                     onBackPressedDispatcher: OnBackPressedDispatcher,
                     content: @Composable (Int) -> Unit) {

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = 0f
            override val selectedPage: Int
                get() = pagerState.currentPage
            override val pageCount: Int
                get() = maxPages()
        }
    }
    val vibrationEffect = android.os.VibrationEffect.createOneShot(50, 20)
    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // if page is not 0, go to page 0
                if (pagerState.currentPage != 0) {
                    animationScope?.launch {
                        pagerState.animateScrollToPage(0)
                    }
                    vibrator?.vibrate(vibrationEffect)
                } else {
                    // else, exit app

                }
            }
        }
    }
    onBackPressedDispatcher.addCallback(backCallback)

    fun onRotaryScroll(pixels: Float): Boolean {
        val currentPage = pagerState.currentPage
        val nextPage = pixels / 20
        val truncatedNextPage = if (nextPage > 1) 1 else if (nextPage < -1) -1 else 0
        if (truncatedNextPage == 0) {
            return false
        }
        if (currentPage + truncatedNextPage < 0 || currentPage + truncatedNextPage >= maxPages()) {
            return false
        }
        animationScope?.launch {
            pagerState.animateScrollToPage(currentPage + truncatedNextPage)
        }
        vibrator?.vibrate(vibrationEffect)
        return true
    }

//    // Define a state for holding the current time as a string
//    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
//
//    // Use LaunchedEffect to periodically update the time
//    LaunchedEffect(Unit) {
//        while (isActive) {
//            currentTime = System.currentTimeMillis()
//            delay(1000) // Update the time every second
//        }
//    }

    Scaffold (
        pageIndicator = {
            HorizontalPageIndicator(
                pageIndicatorState = pageIndicatorState,
                selectedColor = MaterialTheme.colors.primary,
                unselectedColor = MaterialTheme.colors.onSecondary.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(vertical = 6.dp),
            )
        },
        timeText = {
            TimeText(
                timeTextStyle = TextStyle(
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onSecondary,
                ),
//                endCurvedContent = {
//                    basicCurvedText(
//                        Instant.ofEpochMilli(currentTime).atZone(ZoneId.systemDefault()).toLocalTime().toString(),
//                        style = CurvedTextStyle(
//                            fontSize = 12.sp,
////                            color = MaterialTheme.colors.onSecondary,
//                        ),
//                    )
//                }
            )
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(bottom = 3.dp)
                .onRotaryScrollEvent {
                    onRotaryScroll(it.horizontalScrollPixels)
                }
                .focusRequester(focusRequester)
                .focusable(),
            pageSpacing = 0.dp,
            userScrollEnabled = true,
            reverseLayout = false,
            pageSize = PageSize.Fill,
            flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
            key = { it },
            pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
                pagerState,
                Orientation.Horizontal
            ),
            pageContent = {
                content(it)
            },
        )
    }
}