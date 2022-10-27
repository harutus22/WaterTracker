package com.clifertam.watertracker.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.clifertam.watertracker.ui.theme.MainColor
import com.clifertam.watertracker.utils.*

@Composable
fun AppBarCustom(
    onNavigationIconClick: () -> Unit,
    navController: NavController
) {
    val name = remember {
        mutableStateOf("")
    }
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            name.value = getTitle(backStackEntry.destination.route!!)
        }
    }
    TopAppBar(
        backgroundColor = Color.White,
        title = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.value,
                    style = TextStyle(color = MainColor, fontSize = 18.sp)
                )
            }

        },
        navigationIcon = {
            IconButton(onClick = {
                onNavigationIconClick()
            }) {
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = null,
                    tint = MainColor
                )
            }
        },
        actions = {
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = "",
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    )
}

private fun getTitle(id: String): String {
    return when (id) {
        Screen.MainScreen.route -> ""
        Screen.HistoryScreen.route -> "History"
        Screen.PersonalInformationScreen.route -> "Personal information"
        Screen.ReminderScreen.route -> "Reminder"
        else -> ""
    }
}