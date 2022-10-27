package com.clifertam.watertracker.repository

import com.clifertam.watertracker.db.WaterDao
import com.clifertam.watertracker.db.repository.WaterRepository
import com.clifertam.watertracker.model.DrunkWater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek
import javax.inject.Inject

class WaterRepositoryImpl @Inject constructor(
    private val waterDao: WaterDao
) : WaterRepository {
    override suspend fun insertWater(water: DrunkWater) {
        waterDao.insertWater(water)
    }

    override suspend fun getWaterDaily(year: Int, month: Int, day: Int): List<DrunkWater>  {
        return waterDao.getTodayWater(year, month, day)
    }

    override suspend fun getWaterDailySum(year: Int, month: Int, day: Int): Float? {
        return waterDao.getTodayWaterSum(year, month, day)
    }

    override suspend fun getWaterWeek(
        year: Int,
        week: Int
    ): List<DrunkWater> {
        return waterDao.getWeekWater(year, week)
    }

    override suspend fun getWaterMonth(month: Int, year: Int): List<DrunkWater> {
        return waterDao.getMonthWater(month, year)
    }
}