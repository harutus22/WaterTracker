package com.clifertam.watertracker.model

import androidx.compose.ui.geometry.Offset

data class BarPositionItem(
    val startPosition: Offset,
    val endPosition: Offset,
    var isClicked: Boolean,
    val value: Float,
    val day: Int,
    val month: Int
)