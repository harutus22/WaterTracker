package com.clifertam.watertracker.utils

import android.content.Context

fun getNameOfWeek(day: Int): String =
     when(day) {
        2 -> MON
        3 -> TUE
        4 -> WEN
        5 -> THR
        6 -> FRI
        7 -> SAT
        else -> SUN
}

fun getMonth(value: Int): String {
   var month = value
   if (value > 11)
      month = 0
   return MONTH_LIST[month]
}

fun convertPixelsToDp(context: Context, pixels: Float): Float {
   val screenPixelDensity = context.resources.displayMetrics.density
   return pixels * screenPixelDensity
}