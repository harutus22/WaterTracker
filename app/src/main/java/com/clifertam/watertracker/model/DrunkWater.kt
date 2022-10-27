package com.clifertam.watertracker.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_drunk")
data class DrunkWater(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "water_drink_time")
    val time: String,
    @ColumnInfo(name = "water_drink_amount")
    val amount: Int,
    @ColumnInfo(name = "week_day_name")
    val weekDay: String,
    val day: Int,
    val week: Int,
    val month: Int,
    val year: Int
)
