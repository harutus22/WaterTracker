package com.clifertam.watertracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.clifertam.watertracker.model.DrunkWater
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.Month

@Dao
interface WaterDao {
    @Insert
    suspend fun insertWater(drunkWater: DrunkWater)

    @Query("SELECT * FROM water_drunk WHERE year = :year AND month = :month AND day = :day")
    suspend fun getTodayWater(year: Int, month: Int, day: Int): List<DrunkWater>

    @Query("SELECT * FROM water_drunk WHERE year = :year AND week = :week")
    suspend fun getWeekWater(year: Int, week: Int): List<DrunkWater>

    @Query("SELECT * FROM water_drunk WHERE month = :month AND year = :year ORDER BY day ASC")
    suspend fun getMonthWater(month: Int, year: Int): List<DrunkWater>

    @Query("SELECT SUM(water_drink_amount) FROM water_drunk WHERE year = :year AND month = :month AND day = :day")
    suspend fun getTodayWaterSum(year: Int, month: Int, day: Int): Float?
}