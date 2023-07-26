package com.example.coruabuswear.data.local

import android.content.Context
import android.graphics.Color
import androidx.compose.ui.graphics.Color as Colorx
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coruabuswear.data.models.BusLine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

data class BusLineStorage(val id: Int, val value: BusLine)

class DataSource {

    private val _configs = listOf(
        BusLineStorage(1, BusLine(1, "1", Colorx(Color.RED))),
        BusLineStorage(2, BusLine(2, "2A", Colorx(Color.BLUE))),
    )

    fun getConfigs(): Flow<List<BusLineStorage>> {
        return flow {
            delay(500) // mock network delay
            emit(_configs)
        }
    }
}

class MainViewModel(
    private val dataSource: DataSource,
    private val configStorage: Storage<BusLineStorage>
) : ViewModel() {

    init {
        loadConfigs()
    }

    private fun loadConfigs() = viewModelScope.launch {
        dataSource
            .getConfigs()
            .flatMapConcat(configStorage::insert)
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "com.enyason.androiddatastoreexample.shared.preferences")

val dataAccessModule = module {

    single<Storage<BusLineStorage>> {
        PersistentData(
            gson = get(),
            type = object : TypeToken<List<BusLineStorage>>() {}.type,
            preferenceKey = stringPreferencesKey("config"),
            dataStore = androidContext().dataStore
        )
    }

    single { Gson() }

    viewModel {
        MainViewModel(
            dataSource = DataSource(),
            configStorage = get()
        )
    }

}