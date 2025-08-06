package com.example.realsensecapture.ui

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SettingsRepository(private val context: Context) {
    private val resolutionKey = stringPreferencesKey("resolution")
    private val fpsKey = intPreferencesKey("fps")
    private val thresholdKey = longPreferencesKey("threshold")

    val resolutionFlow: Flow<String> = context.dataStore.data.map { it[resolutionKey] ?: "640x480" }
    val fpsFlow: Flow<Int> = context.dataStore.data.map { it[fpsKey] ?: 30 }
    val thresholdFlow: Flow<Long> = context.dataStore.data.map { it[thresholdKey] ?: 100L * 1024 * 1024 }

    suspend fun setResolution(value: String) {
        context.dataStore.edit { it[resolutionKey] = value }
    }

    suspend fun setFps(value: Int) {
        context.dataStore.edit { it[fpsKey] = value }
    }

    suspend fun setThreshold(value: Long) {
        context.dataStore.edit { it[thresholdKey] = value }
    }
}

