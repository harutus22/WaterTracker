package com.clifertam.watertracker.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clifertam.watertracker.db.repository.WaterRepository
import com.clifertam.watertracker.model.DrunkWater
import com.clifertam.watertracker.model.MonthSum
import com.clifertam.watertracker.model.WeekDaySum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class WaterViewModel @Inject constructor(
    private val waterRepository: WaterRepository,
): ViewModel() {
    private val _waterFlow = MutableSharedFlow<List<DrunkWater>>()
    val waterFlow = _waterFlow.asSharedFlow()

    private val _waterSum = MutableSharedFlow<Float?>()
    val waterSum = _waterSum.asSharedFlow()

    private val _weekFlow = MutableSharedFlow<List<WeekDaySum>?>()
    val weekFlow = _weekFlow.asSharedFlow()

    private val _monthFlow = MutableSharedFlow<List<MonthSum>?>()
    val monthFlow = _monthFlow.asSharedFlow()

    init {
        getDailyWaterDrunk()
    }

    fun getDailyWaterDrunk() {
        viewModelScope.launch {
            val today = LocalDateTime.now()
            _waterFlow.emit(waterRepository.getWaterDaily(today.year, today.monthValue, today.dayOfMonth))
        }
    }

    fun getDailyWaterSum() {
        val today = LocalDateTime.now()
        viewModelScope.launch {
            _waterSum.emit(waterRepository.getWaterDailySum(today.year, today.monthValue, today.dayOfMonth))
        }
    }

    fun getMonthWaterDrunk(month: Int, year: Int){
        viewModelScope.launch {
            val monthList = waterRepository.getWaterMonth(month, year)
                .groupBy {
                    it.day
                }.map {
                    MonthSum(it.value.component1().day, it.value.sumOf { drunk ->
                        drunk.amount
                    }, month)
                }
            val finalList = List(31) { MonthSum(0, 0, 0)}.toMutableList()
            monthList.forEachIndexed { _, monthSum ->
                finalList[monthSum.day - 1] = monthSum
            }
            _monthFlow.emit(finalList)
        }
    }

    fun getWeekWaterDrunk(year: Int, week: Int) {
        viewModelScope.launch {

            val weekList  = waterRepository.getWaterWeek(year, week)
                .groupBy {
                    it.day
                }.map { it ->
                    WeekDaySum(it.value.component1().weekDay, it.value.sumOf {
                        it.amount
                    })
                }
            _weekFlow.emit(weekList)
        }
    }

    fun addWater(drunkWater: DrunkWater){
        viewModelScope.launch {
            waterRepository.insertWater(drunkWater)
        }
    }
}