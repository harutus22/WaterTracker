package com.clifertam.watertracker.db.repository

import com.clifertam.watertracker.model.DrunkWater
import kotlinx.coroutines.flow.Flow

interface WaterRepository {

    suspend fun insertWater(water: DrunkWater)

    suspend fun getWaterDaily(year: Int, month: Int, day: Int): List<DrunkWater>

    suspend fun getWaterDailySum(year: Int, month: Int, day: Int): Float?

    suspend fun getWaterWeek(year: Int, week: Int): List<DrunkWater>

    suspend fun getWaterMonth(month: Int, year: Int): List<DrunkWater>


}