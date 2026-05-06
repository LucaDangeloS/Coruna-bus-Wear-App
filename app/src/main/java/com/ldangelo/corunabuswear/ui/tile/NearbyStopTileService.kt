package com.ldangelo.corunabuswear.ui.tile

import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.ldangelo.corunabuswear.R
import com.ldangelo.corunabuswear.data.model.Bus
import com.ldangelo.corunabuswear.data.model.BusLine
import com.ldangelo.corunabuswear.data.model.BusStop
import com.ldangelo.corunabuswear.data.repository.BusesRepository
import com.ldangelo.corunabuswear.data.repository.IBusesRepository
import com.ldangelo.corunabuswear.data.source.apis.BusProvider
import com.ldangelo.corunabuswear.data.source.apis.LocationProvider
import com.ldangelo.corunabuswear.data.source.local.clearAllStoredBusData
import com.ldangelo.corunabuswear.data.source.local.getBusLine
import com.ldangelo.corunabuswear.data.source.local.saveBusConnection
import com.ldangelo.corunabuswear.data.source.local.saveBusLine
import com.ldangelo.corunabuswear.data.source.local.saveBusStop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val RESOURCES_VERSION = "404"
private const val TAG = "NearbyStopTile"
private const val RED_TRANVIAS = 0xfff34639.toInt()
private const val DARK_CARD_BG = 0xff222222.toInt()
private const val LIGHT_ASH = 0xffafafaf.toInt()

fun String.toTitleCase(): String {
    return try {
        this.lowercase().split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    } catch (e: Exception) {
        this
    }
}

@OptIn(ExperimentalHorologistApi::class)
class NearbyStopTileService : SuspendingTileService() {

    private val busesRepository by lazy { BusesRepository(this) }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping("refresh", ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                    .setResourceId(R.drawable.ic_refresh)
                    .build())
                .build())
            .build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        Log.d(TAG, "tileRequest - START")
        val (data, lastUpdated) = try {
            fetchData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchData", e)
            emptyList<Pair<BusStop, List<Bus>>>() to LocalTime.now()
        }

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(layout(data, lastUpdated, requestParams.deviceConfiguration))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun updateAndStoreDefinitions() {
        Log.d(TAG, "Updating Bus definitions in tile service")
        val (stops, lines, connections) = busesRepository.updateDefinitions()
        clearAllStoredBusData(this)
        stops.forEach { busStop -> saveBusStop(this, busStop.id.toString(), busStop) }
        lines.forEach { busLine -> saveBusLine(this, busLine.id.toString(), busLine) }
        connections.forEach { busLine -> saveBusConnection(this, busLine.id.toString(), busLine) }
    }

    private suspend fun fetchData(): Pair<List<Pair<BusStop, List<Bus>>>, LocalTime> = withContext(Dispatchers.IO) {
        LocationProvider.init(this@NearbyStopTileService)
        var loc = LocationProvider.fetchLocation()
        
        if (loc == null) {
            loc = android.location.Location("default").apply {
                latitude = 43.373212
                longitude = -8.432828
            }
        }

        var stops: List<BusStop> = emptyList()
        try {
            stops = busesRepository.getNearbyStops(loc, emptyList(), 3)
        } catch (e: IBusesRepository.UnknownDataException) {
            updateAndStoreDefinitions()
            stops = try {
                busesRepository.getNearbyStops(loc, emptyList(), 3)
            } catch (e: Exception) {
                emptyList<BusStop>()
            }
        } catch (e: Exception) {
            Log.e(TAG, "General error fetching stops", e)
        }

        val results = stops.map { stop ->
            val busesJson = try {
                BusProvider.fetchBuses(stop.id)
            } catch (e: Exception) {
                emptyList()
            }

            val buses = busesJson.mapNotNull { busObj ->
                try {
                    val lineaId = busObj.getInt("linea")
                    val busLine = getBusLine<BusLine>(this@NearbyStopTileService, lineaId.toString())
                        ?: BusLine(lineaId, lineaId.toString(), androidx.compose.ui.graphics.Color.Gray)

                    val remainingTime = try { busObj.getInt("tiempo") } catch (e: Exception) { -1 }
                    Bus(busObj.getInt("bus"), busLine, remainingTime)
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { if (it.remainingTime < 0) Int.MAX_VALUE else it.remainingTime }
            
            stop to buses.take(3)
        }
        results to LocalTime.now()
    }

    private fun layout(
        data: List<Pair<BusStop, List<Bus>>>,
        lastUpdated: LocalTime,
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): LayoutElementBuilders.LayoutElement {
        val refreshAction = ActionBuilders.LoadAction.Builder().build()
        val refreshClickable = ModifiersBuilders.Clickable.Builder()
            .setOnClick(refreshAction)
            .setId("refresh_btn")
            .build()

        if (data.isEmpty()) {
            return PrimaryLayout.Builder(deviceParameters)
                .setResponsiveContentInsetEnabled(true)
                .setContent(
                    LayoutElementBuilders.Column.Builder()
                        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                        .addContent(
                            Text.Builder(this, "Sin paradas cercanas")
                                .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                                .setColor(ColorBuilders.argb(LIGHT_ASH))
                                .build()
                        )
                        .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(8f)).build())
                        .addContent(
                            Button.Builder(this, refreshClickable)
                                .setIconContent("refresh")
                                .setButtonColors(ButtonColors(
                                    ColorBuilders.argb(RED_TRANVIAS),
                                    ColorBuilders.argb(0xFFFFFFFF.toInt())
                                ))
                                .setSize(DimensionBuilders.dp(32f))
                                .build()
                        )
                        .build()
                )
                .build()
        }

        val rootColumn = LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)

        // Title
        rootColumn.addContent(
            Text.Builder(this, "TRANVÍAS CORUÑA")
                .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                .setColor(ColorBuilders.argb(RED_TRANVIAS))
                .build()
        )
        rootColumn.addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(4f)).build())

        data.take(3).forEach { (stop, buses) ->
            rootColumn.addContent(stopCard(stop, buses))
            rootColumn.addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(2f)).build())
        }

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        
        // Footer
        val footerRow = LayoutElementBuilders.Row.Builder()
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .addContent(
                Text.Builder(this, lastUpdated.format(timeFormatter))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setColor(ColorBuilders.argb(0xff777777.toInt()))
                    .build()
            )
            .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(DimensionBuilders.dp(8f)).build())
            .addContent(
                Button.Builder(this, refreshClickable)
                    .setIconContent("refresh")
                    .setButtonColors(ButtonColors(
                        ColorBuilders.argb(RED_TRANVIAS),
                        ColorBuilders.argb(0xFFFFFFFF.toInt())
                    ))
                    .setSize(DimensionBuilders.dp(24f))
                    .build()
            )

        rootColumn.addContent(LayoutElementBuilders.Spacer.Builder().setHeight(DimensionBuilders.dp(2f)).build())
        rootColumn.addContent(footerRow.build())

        return LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .setModifiers(ModifiersBuilders.Modifiers.Builder()
                .setPadding(ModifiersBuilders.Padding.Builder()
                    .setTop(DimensionBuilders.dp(2f))
                    .setBottom(DimensionBuilders.dp(2f))
                    .setStart(DimensionBuilders.dp(16f))
                    .setEnd(DimensionBuilders.dp(16f))
                    .build())
                .build())
            .addContent(rootColumn.build())
            .build()
    }

    private fun stopCard(stop: BusStop, buses: List<Bus>): LayoutElementBuilders.LayoutElement {
        val stopColumn = LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_START)
            .addContent(
                Text.Builder(this, stop.name.toTitleCase())
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                    .build()
            )

        val busRow = LayoutElementBuilders.Row.Builder()
            .setWidth(DimensionBuilders.expand())
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)

        if (buses.isEmpty()) {
            busRow.addContent(
                Text.Builder(this, "Sin buses próximos")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setColor(ColorBuilders.argb(0xff777777.toInt()))
                    .build()
            )
        } else {
            buses.forEachIndexed { index, bus ->
                busRow.addContent(busChip(bus))
                if (index < buses.size - 1) {
                    busRow.addContent(LayoutElementBuilders.Spacer.Builder().setWidth(DimensionBuilders.dp(2f)).build())
                }
            }
        }
        stopColumn.addContent(busRow.build())

        return LayoutElementBuilders.Box.Builder()
            .setWidth(DimensionBuilders.expand())
            .setModifiers(ModifiersBuilders.Modifiers.Builder()
                .setBackground(ModifiersBuilders.Background.Builder()
                    .setColor(ColorBuilders.argb(DARK_CARD_BG))
                    .setCorner(ModifiersBuilders.Corner.Builder().setRadius(DimensionBuilders.dp(8f)).build())
                    .build())
                .setPadding(ModifiersBuilders.Padding.Builder()
                    .setAll(DimensionBuilders.dp(4f))
                    .build())
                .build())
            .addContent(stopColumn.build())
            .build()
    }

    private fun busChip(bus: Bus): LayoutElementBuilders.LayoutElement {
        val lineColor = bus.line.color.toArgb()
        
        return LayoutElementBuilders.Box.Builder()
            .setModifiers(ModifiersBuilders.Modifiers.Builder()
                .setBackground(ModifiersBuilders.Background.Builder()
                    .setColor(ColorBuilders.argb(lineColor))
                    .setCorner(ModifiersBuilders.Corner.Builder().setRadius(DimensionBuilders.dp(4f)).build())
                    .build())
                .setPadding(ModifiersBuilders.Padding.Builder()
                    .setStart(DimensionBuilders.dp(4f))
                    .setEnd(DimensionBuilders.dp(4f))
                    .build())
                .build())
            .addContent(
                LayoutElementBuilders.Row.Builder()
                    .addContent(
                        Text.Builder(this, bus.line.name)
                            .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                            .setColor(ColorBuilders.argb(0xffffffff.toInt()))
                            .build()
                    )
                    .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(DimensionBuilders.dp(4f)).build())
                    .addContent(
                        Text.Builder(this, bus.getRemainingTime().replace("En parada", "0m").replace(" min", "m"))
                            .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                            .setColor(ColorBuilders.argb(0xffdfdfdf.toInt()))
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
