package com.clifertam.watertracker.model

import androidx.compose.ui.geometry.Offset

data class MenuDotItem(
    val position: Offset,
    val radius: Float,
    var isChecked: Boolean = false
)
