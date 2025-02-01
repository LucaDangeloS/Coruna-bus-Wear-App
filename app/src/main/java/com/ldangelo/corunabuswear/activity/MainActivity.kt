package com.ldangelo.corunabuswear.activity

import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.ldangelo.corunabuswear.R
import com.ldangelo.corunabuswear.data.ContextHolder.setApplicationContext
import com.ldangelo.corunabuswear.data.ContextHolder.setLifecycleScope
import com.ldangelo.corunabuswear.data.repository.BusesRepository
import com.ldangelo.corunabuswear.data.repository.LocationRepository
import com.ldangelo.corunabuswear.data.viewmodels.BusStopsListViewModel
import com.ldangelo.corunabuswear.ui.fragment.BusStopFragment
import com.ldangelo.corunabuswear.ui.fragment.StopsPageFragment
import com.ldangelo.corunabuswear.ui.fragment.UILoadingFragment
import com.ldangelo.corunabuswear.ui.fragment.components.CenteredText
import com.ldangelo.corunabuswear.ui.fragment.components.PagerScaffolding
import com.ldangelo.corunabuswear.ui.theme.CoruñaBusWearTheme


class MainActivity : FragmentActivity() {
    private lateinit var busViewModel: BusStopsListViewModel
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApplicationContext(this)
        setLifecycleScope(lifecycleScope)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        busViewModel = BusStopsListViewModel(
            this,
            busesRepository = BusesRepository(
                activity = this
            ),
            locationRepository = LocationRepository(
                activity = this,
            )
        )

        // Set vibrator service
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        // Set loading ui
        setContent {
            CoruñaBusWearTheme {
                UILoadingFragment(getString(R.string.getting_location))
            }
        }


        setContent {
            // Data
            val busStops by busViewModel.busStops.collectAsState()
            val loaded by busViewModel.loadedLocation.collectAsState()
            val fetchedStops by busViewModel.fetchedStops.collectAsState()

            // UI
//            val scrollState = rememberScrollState()
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { busStops.size + 1 },
            )
            busViewModel.updatePageIndex(pagerState.currentPage)
            val animationScope = rememberCoroutineScope()

            CoruñaBusWearTheme {
                if (busStops.isNotEmpty()) {
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
                } else if (!loaded) {
                    UILoadingFragment(getString(R.string.getting_location))
                } else if (!fetchedStops) {
                    UILoadingFragment(getString(R.string.getting_stops))
                } else {
                    CenteredText(text = getString(R.string.no_stops_nearby))
                }
            }

        }
    }
}
