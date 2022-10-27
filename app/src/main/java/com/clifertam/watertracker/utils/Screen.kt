package com.clifertam.watertracker.utils

sealed class Screen(val route: String) {
    object HistoryScreen: Screen("history_screen")
    object MainScreen: Screen("main_screen")
    object PersonalInformationScreen: Screen("personal_information_screen")
    object ReminderScreen: Screen("reminder_screen")
}
