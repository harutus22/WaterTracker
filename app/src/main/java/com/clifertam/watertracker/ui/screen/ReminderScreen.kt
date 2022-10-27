package com.clifertam.watertracker.ui.screen

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.work.*
import com.chargemap.compose.numberpicker.FullHours
import com.chargemap.compose.numberpicker.Hours
import com.chargemap.compose.numberpicker.HoursNumberPicker
import com.clifertam.watertracker.R
import com.clifertam.watertracker.model.TimerData
import com.clifertam.watertracker.ui.theme.*
import com.clifertam.watertracker.utils.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

const val CHANNEL_ID = "water_reminder"


@Composable
fun ReminderScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    purchaseClick: () -> Unit,
    isBought: Boolean,
) {
    if (!isBought) {
        RegReminder(modifier = modifier)
    } else {
        NotRegReminder(modifier = modifier, purchaseClick)
    }
}

@Composable
fun RegReminder(modifier: Modifier) {
    createNotificationChannel(LocalContext.current)
    val context = LocalContext.current
    val alarmMgr: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
        val canIt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager!!.canScheduleExactAlarms()
        } else {
            true
        }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!canIt) {
            Intent().also { intent ->
                intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                context.startActivity(intent)
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))

    val scope = rememberCoroutineScope()
    val dataStore = WaterDataStore(context)
    val wake = remember {
        mutableStateOf("6:00")
    }
    val sleep = remember {
        mutableStateOf("23:00")
    }
    LaunchedEffect(key1 = true, block = {
        dataStore.getWakeUpTime.collectLatest {
            wake.value = it
        }
    })
    LaunchedEffect(key1 = true, block = {
        dataStore.getSleepTime.collectLatest {
            sleep.value = it
        }
    })
    val intakeReminder = remember {
        mutableStateOf(0)
    }
    LaunchedEffect(key1 = true, block = {
        dataStore.intakeReminder.collectLatest {
            intakeReminder.value = it!!.toInt()
        }
    })
    val whichTime = remember {
        mutableStateOf(false)
    }
    val isOpenPop = remember {
        mutableStateOf(false)
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(modifier = modifier) {

            TimeItem(text = stringResource(R.string.wake_time_title), time = wake.value, isClickable = !isOpenPop.value) {
                whichTime.value = false
                isOpenPop.value = true
            }
            TimeItem(
                text = stringResource(R.string.time_to_sleep_title),
                time = sleep.value,
                isClickable = !isOpenPop.value
            ) {
                whichTime.value = true
                isOpenPop.value = true
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Set water intake reminder",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 30.dp)
            )
            DropDownReminder(count = intakeReminder.value,
                onIntakeReminderClick = { value, index ->
                    intakeReminder.value = index
                    val (hours, min) = wake.value.split(":").map { it -> it.toInt() }
                    steIntakeReminder(context,
                        value.replace(Regex("[^0-9]"), "").toLong(),
                        index == 0, hours, min)
                    scope.launch {
                        dataStore.savePrefs(WaterDataStore.USER_INTAKE_REMINDER, index.toString())
                    }
                })
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.BottomStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.waves),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        if (isOpenPop.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val (hours, min)  = if (!whichTime.value) wake.value.split(":").map { it.toInt() } else sleep.value.split(":").map { it.toInt() }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.9f)
                        .background(ReminderPopBack)
                )

                PopUpTimer(
                    modifier = Modifier.align(Alignment.Center),
                    whichTime.value,
                    onCancel = {
                        isOpenPop.value = false
                    },
                    onTimeChosen = {
                        val gson = Gson()
                        val zeroHour = if (it.hours < 10) "0" else ""
                        val zeroMinute = if (it.minutes < 10) "0" else ""
                        if (!whichTime.value) {
                            wake.value = "$zeroHour${it.hours}:$zeroMinute${it.minutes}"
                            val (hours, min) = wake.value.split(":").map { it -> it.toInt() }
                            setWakeSleepReminder(hours, min, !whichTime.value, context, alarmMgr)
                            scope.launch {
                                val data = gson.toJson(TimerData(it.hours, it.minutes))
                                dataStore.savePrefs(WaterDataStore.USER_WAKE_UP_TIME, data)
                            }
                        } else {
                            sleep.value = "$zeroHour${it.hours}:$zeroMinute${it.minutes}"
                            val (hours, min) = sleep.value.split(":").map { it -> it.toInt() }
                            setWakeSleepReminder(hours, min, whichTime.value, context, alarmMgr)
                            scope.launch {
                                val data = gson.toJson(TimerData(it.hours, it.minutes))
                                dataStore.savePrefs(WaterDataStore.USER_SLEEP_TIME, data)
                            }
                        }
                        isOpenPop.value = false
                    }, hour = hours, minute = min)
            }
        }
    }
}

private fun steIntakeReminder(context: Context, intervalTime: Long, isMinutes: Boolean, hour: Int, minute: Int) {
    val currentDate = Calendar.getInstance()
    val dueDate = Calendar.getInstance()
    dueDate.set(Calendar.HOUR_OF_DAY, hour)
    dueDate.set(Calendar.MINUTE, minute)
    dueDate.set(Calendar.SECOND, 0)
    val timeType =  if(isMinutes) TimeUnit.MINUTES else TimeUnit.HOURS
    val constraints = Constraints.Builder()
        .build()
    val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
    val work = PeriodicWorkRequestBuilder<IntakeWorker>(intervalTime, timeType)
        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
        .setConstraints(constraints)
        .build()
    val workManager = WorkManager.getInstance(context)
    workManager.enqueueUniquePeriodicWork("intake", ExistingPeriodicWorkPolicy.REPLACE, work)
}

private fun setWakeSleepReminder(hour: Int, minute: Int, isNight: Boolean, context: Context, alarmManager: AlarmManager) {
    val currentDate = Calendar.getInstance()
    val dueDate = Calendar.getInstance()
    dueDate.set(Calendar.HOUR_OF_DAY, hour)
    dueDate.set(Calendar.MINUTE, minute)
    dueDate.set(Calendar.SECOND, 0)
    if (dueDate.before(currentDate)) {
        dueDate.add(Calendar.HOUR_OF_DAY, 24)
    }
    val constraints = Constraints.Builder()
        .build()
    val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
    val myData: Data = workDataOf(
        REMINDER_DATA_HOUR to hour,
        REMINDER_DATA_MINUTE to minute,
        REMINDER_DATA_TYPE to isNight)
    val dailyWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setConstraints(constraints).setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
        .setInputData(myData)
        .build()
    WorkManager.getInstance(context).enqueue(dailyWorkRequest)
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "water_notification"
        val descriptionText = "water_notification"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}

@Composable
fun PopUpTimer(
    modifier: Modifier,
    isWakeUp: Boolean,
    onTimeChosen: (hours: Hours) -> Unit,
    onCancel: () -> Unit,
    hour: Int = 0,
    minute: Int = 0
) {
    var pickerValue by remember { mutableStateOf<Hours>(FullHours(hour, minute)) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xffFEFEFE)),

        ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (!isWakeUp) "Wake Time" else "Time falling sleep",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                Image(
                    modifier = Modifier.weight(1f),
                    painter = painterResource(id = R.drawable.clock),
                    contentDescription = ""
                )
                HoursNumberPicker(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 20.dp),
                    dividersColor = RegBtnFst,
                    leadingZero = true,
                    value = pickerValue,
                    onValueChange = {
                        pickerValue = it

                    },
                    hoursDivider = {
                        Text(
                            modifier = Modifier.size(24.dp),
                            textAlign = TextAlign.Center,
                            text = ":"
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.End,

                ) {
                Text(text = "CANCEL", modifier = modifier.clickable {
                    onCancel()
                })
                Spacer(modifier = Modifier.width(80.dp))
                Text(text = "OK", color = RegBtnFst, modifier = modifier.clickable {
                    onTimeChosen(pickerValue)
                })
            }
        }

    }

}

@Composable
fun DropDownReminder(count: Int = 0,  onIntakeReminderClick: (value: String, index: Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }
    val mCities = listOf(
//        "2 minutes",
        "30 minutes",
        "1 hour",
        "2 hours",
        "3 hours",
        "4 hours",
        "5 hours",
        "6 hours",
        "7 hours"
    )
    var mSelectedText = mCities[count]
    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ReminderBtnBack)
            .onGloballyPositioned {
                mTextFieldSize = it.size.toSize()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                        )
                    ) {
                        append("Every ")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MainColor,
                        )
                    ) {
                        append(mSelectedText)
                    }
                }
            )
            Icon(icon, "",
                Modifier.clickable { expanded = !expanded })
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
                .background(ReminderBtnBack)
        ) {
            mCities.forEachIndexed() { index, label ->
                DropdownMenuItem(onClick = {
                    mSelectedText = label
                    expanded = false
                    onIntakeReminderClick(label, index)
                }) {
                    Text(text = label, color = MainColor)
                }
            }
        }
    }
}

@Composable
fun TimeItem(text: String, time: String, isClickable: Boolean, onClockClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ReminderBtnBack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = time,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MainColor,
                modifier = Modifier.clickable(enabled = isClickable) {
                    onClockClick()
                }
            )
        }
    }
}

@Composable
fun NotRegReminder(modifier: Modifier, purchaseClick: () -> Unit) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    Column(
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.not_no_reg
                ),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
            Column {
                Image(
                    painter = painterResource(
                        id = R.drawable.reminder_no_reg_img
                    ),
                    contentDescription = "",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        tint = Color.White,
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.add_reminder),
                        style = TextStyle(
                            color = Color.White,
                        ),
                        fontSize = 20.sp
                    )
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bubbles),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .padding(40.dp)
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            brush = RegButtonGradient,
                            shape = RoundedCornerShape(percent = 20)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null // To disable ripple
                        ) {

                        },
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            purchaseClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(8.dp),
                        shape = RoundedCornerShape(percent = 20),
                        colors = ButtonDefaults.buttonColors(RegBtnFst)
                    ) {
                        Text(
                            text = "$ 3/ Year",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.waves),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }

            }
        }
    }
}