package com.clifertam.watertracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.clifertam.watertracker.model.DrunkWater

@Database(entities = [DrunkWater::class], version = 1)
abstract class WaterDatabase: RoomDatabase() {
    abstract fun waterDao(): WaterDao
}