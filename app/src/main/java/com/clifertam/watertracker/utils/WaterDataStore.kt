package com.clifertam.watertracker.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.clifertam.watertracker.model.TimerData
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WaterDataStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(WATER_USER)
        val USER_NAME_KEY = stringPreferencesKey(USER_NAME)
        val USER_WEIGHT_KEY = stringPreferencesKey(USER_WEIGHT)
        val USER_GOAL_KEY = stringPreferencesKey(USER_GOAL)
        val USER_WAKE_UP_TIME = stringPreferencesKey(WAKE_UP_TIME)
        val USER_SLEEP_TIME = stringPreferencesKey(SLEEP_TIME)
        val USER_INTAKE_REMINDER = stringPreferencesKey(INTAKE_REMINDER)
    }

    val getName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME_KEY] ?: ""
        }

    val getGoal: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_GOAL_KEY] ?: "0"
        }

    val getWeight: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_WEIGHT_KEY] ?: ""
        }

    val getWakeUpTime: Flow<String> = context.dataStore.data
        .map { preferences ->
            val gson = Gson()
            val json = preferences[USER_WAKE_UP_TIME] ?: "{\"hour\":\"0\", \"minutes\":\"0\"}"
            val timer = gson.fromJson(json, TimerData::class.java)
            val zeroHour = if (timer.hour < 10) "0" else ""
            val zeroMinute = if (timer.minutes < 10) "0" else ""
            "$zeroHour${timer.hour}:$zeroMinute${timer.minutes}"
        }

    val getSleepTime: Flow<String> = context.dataStore.data
        .map { preferences ->
            val gson = Gson()
            val json = preferences[USER_SLEEP_TIME] ?: "{\"hour\":\"0\", \"minutes\":\"0\"}"
            val timer = gson.fromJson(json, TimerData::class.java)
            val zeroHour = if (timer.hour < 10) "0" else ""
            val zeroMinute = if (timer.minutes < 10) "0" else ""
            "$zeroHour${timer.hour}:$zeroMinute${timer.minutes}"
        }

    val intakeReminder: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_INTAKE_REMINDER] ?: "0"
        }

    suspend fun savePrefs(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { preference ->
            preference[key] = value
        }
    }
}