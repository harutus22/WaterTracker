package com.clifertam.watertracker.ui.screen

import android.graphics.Typeface
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.clifertam.watertracker.R
import com.clifertam.watertracker.model.BarPositionItem
import com.clifertam.watertracker.model.DrunkWater
import com.clifertam.watertracker.model.MonthSum
import com.clifertam.watertracker.model.WeekDaySum
import com.clifertam.watertracker.repository.WaterViewModel
import com.clifertam.watertracker.ui.theme.*
import com.clifertam.watertracker.utils.*
import kotlinx.coroutines.flow.collectLatest
import java.util.*
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var goals by remember {
        mutableStateOf("")
    }
    var menuClicked by remember {
        mutableStateOf(0)
    }

    val context = LocalContext.current
    val dataStore = WaterDataStore(context)
    LaunchedEffect(key1 = true) {
        dataStore.getGoal.collectLatest {
            goals = if (it != "0") (it!!.toFloat() * 1000).toInt().toString() else "2000"
        }
    }
    Column(modifier = modifier.padding(top = 10.dp)) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DAILY GOALS: $goals ML",
                color = HistoryTextColor
            )
            val inline = mapOf(
                Pair(stringResource(R.string.id), InlineTextContent(
                    Placeholder(
                        width = 24.sp,
                        height = 24.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.next),
                        "",
                        tint = HistoryTextColor
                    )
                })
            )
            Text(text = buildAnnotatedString {
                append(stringResource(R.string.edit_goals))
                appendInlineContent(id = stringResource(R.string.id))
            }, color = HistoryTextColor, inlineContent = inline,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.PersonalInformationScreen.route)
                })
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(HistoryMenuColor),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TextChosen(
                title = stringResource(R.string.today), number = 0, onItemClicked = {
                    menuClicked = it
                }, modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(), numberChosen = menuClicked
            )

            TextChosen(
                title = stringResource(R.string.week), number = 1, onItemClicked = {
                    menuClicked = it
                }, modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(), numberChosen = menuClicked
            )
            TextChosen(
                title = stringResource(R.string.month), number = 2, onItemClicked = {
                    menuClicked = it
                }, modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(), numberChosen = menuClicked
            )

        }

        val viewModel: WaterViewModel = hiltViewModel()


        when (menuClicked) {
            0 -> TodayGraphic(viewModel)
            1 -> WeekGraphic(viewModel, goals.toInt())
            else -> MonthGraphic(viewModel, goals.toInt())
        }

    }
}

private val weekDays = arrayOf(MON, TUE, WEN, THR, FRI, SAT, SUN)

@Composable
fun TextChosen(
    title: String,
    number: Int,
    numberChosen: Int,
    onItemClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isClicked = number == numberChosen
    Box(modifier = modifier
        .clip(if (isClicked) RoundedCornerShape(40.dp) else RoundedCornerShape(0.dp))
        .background(if (isClicked) HistoryMenuChosen else Color.Transparent)
        .clickable {
            onItemClicked(number)
        }) {
        Text(
            text = title, textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = if (isClicked) Color.White else HistoryMenuChosen,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
        )
    }
}

@Composable
fun TodayGraphic(
    viewModel: WaterViewModel
) {
    val waterList = remember {
        mutableStateListOf<DrunkWater>()
    }
    LaunchedEffect(key1 = true) {
        viewModel.getDailyWaterDrunk()
        viewModel.waterFlow.collectLatest() { water ->
            waterList.clear()
            waterList.addAll(water)
        }
        viewModel.getDailyWaterSum()
    }
    val calender: Calendar = Calendar.getInstance()
    Log.d("Week", "Current Week:" + calender.get(Calendar.WEEK_OF_YEAR))
    LazyColumn(
        content = {
            items(waterList) { water ->
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(10.dp)
                        )
                        .background(HistoryMenuItemBack)
                        .padding(vertical = 15.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = water.time, color = HistoryTextColor)
                        Text(text = "+ ${water.amount} ml", color = HistoryTextColor)
                    }
                }
            }
        }, modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp)
    )
}

@Composable
fun WeekGraphic(
    viewModel: WaterViewModel,
    goal: Int
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val calendar by remember {
        mutableStateOf(Calendar.getInstance())
    }
    Log.d("Week", "Current Week:" + calendar.get(Calendar.YEAR))
    val weekList = remember {
        mutableStateListOf<WeekDaySum>()
    }

    var weekDay by remember {
        mutableStateOf(calendar.get(Calendar.WEEK_OF_YEAR))
    }

    var year by remember {
        mutableStateOf(calendar.get(Calendar.YEAR))
    }

    val maxWeeks = getTotalWeeksInYear(calendar.get(Calendar.YEAR))

    LaunchedEffect(key1 = true) {
        viewModel.getWeekWaterDrunk(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.WEEK_OF_YEAR)
        )
        viewModel.weekFlow.collectLatest {
            weekList.clear()
            weekList.addAll(it!!.toList())
        }
    }
    var averageIntake = 0f
    var minimalIntake = if (weekList.isNotEmpty()) weekList[0].sum.toFloat() else 0f
    var maximalIntake = 0f
    var count = 0
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 10.dp)
            .height(screenHeight / 5 * 2)
            .clip(RoundedCornerShape(20.dp))
            .background(HistoryMenuItemBack)

    ) {

        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(id = R.drawable.previous),
                    contentDescription = stringResource(R.string.previous_week),
                    modifier = Modifier.clickable {
                        var weekOfTheYear = calendar.get(Calendar.WEEK_OF_YEAR) - 1
                        if (weekOfTheYear < 1) {
                            year--
                            calendar.set(Calendar.YEAR, year)
                            weekOfTheYear = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)
                        }
                        calendar.set(Calendar.WEEK_OF_YEAR, weekOfTheYear)
                        viewModel.getWeekWaterDrunk(
                            year,
                            weekOfTheYear
                        )
//                        weekDay--
//                        if (weekDay < 0) {
//                            year--
//                            weekDay = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)
//                        }
//                        viewModel.getWeekWaterDrunk(year, weekDay)
                    }
                )
                calendar.firstDayOfWeek = Calendar.MONDAY
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val dalo = calendar.get(Calendar.DAY_OF_MONTH)
                val alo = calendar.get(Calendar.MONTH)
                val aasd = calendar.get(Calendar.YEAR)
                val maxDays =
                    calendar.get(Calendar.DAY_OF_MONTH) + 6 - calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                Text(
                    text = "${MONTH_LIST[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.DAY_OF_MONTH)} - " +
                            if (maxDays > 0) {
                                "${getMonth(calendar.get(Calendar.MONTH) + 1)} $maxDays"
                            } else
                                "${calendar.get(Calendar.DAY_OF_MONTH) + 6}"
                                        + "",
                    color = HistoryTextColor, fontSize = 20.sp
                )
                Image(painter = painterResource(id = R.drawable.next),
                    contentDescription = stringResource(R.string.next_week),
                    modifier = Modifier.clickable {
                        Log.d("WEEK_DAY", calendar.get(Calendar.WEEK_OF_YEAR).toString())
                        var weekOfTheYear = calendar.get(Calendar.WEEK_OF_YEAR) + 1
                        if (weekOfTheYear > maxWeeks) {
                            year++
                            calendar.set(Calendar.YEAR, year)
                            weekOfTheYear = 1
                        }
                        calendar.set(Calendar.WEEK_OF_YEAR, weekOfTheYear)
                        viewModel.getWeekWaterDrunk(
                            year,
                            weekOfTheYear
                        )
//                        weekDay++
//                        if (weekDay > maxWeeks) {
//                            year++
//                            weekDay = 0
//                        }
//                        viewModel.getWeekWaterDrunk(year, weekDay)
                    }
                )
            }

            weekList.forEach {
                if (it.sum != 0)
                    count++
                averageIntake += it.sum
                if (minimalIntake > it.sum)
                    minimalIntake = it.sum.toFloat()
                else if (maximalIntake < it.sum)
                    maximalIntake = it.sum.toFloat()
            }
            averageIntake = if (averageIntake == 0f) 0f else averageIntake / count
//            var animationPlayed by remember() { // to play animation only once
//                mutableStateOf(false)
//            }
//            val leastAddingWater =
//                if (yWater - waterRemainingJar <= topLimit) topLimit else yWater - waterRemainingJar
//            val waterAdd by animateFloatAsState( // animate from 0 to 360 degree for 1000ms
//                targetValue = if (animationPlayed) leastAddingWater else 0f,
//                animationSpec = tween(durationMillis = 1000)
//            )
//            LaunchedEffect(key1 = true) { // fired on view creation, state change triggers the animation
//                animationPlayed = true
//            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 40.dp, vertical = 50.dp)
            ) {
                val itemWidth = size.width / weekDays.size
                val radius = itemWidth / 10
                val itemHeight = size.height
                weekDays.forEachIndexed { index, weekday ->

                    var addedWater = 0f
                    var sum = 0f
                    weekList.forEach {
                        if (weekday == it.day) {
                            sum = it.sum.toFloat()
                            val item = itemHeight / 100
                            addedWater = item * (it.sum.toFloat() / goal * 100)
                        }
                    }
                    val textPaint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        textSize = 16.sp.toPx()
                        color = Color(0xff5396C2).toArgb()
                        typeface =
                            Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    }

                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            weekday,
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f,
                            0f,
                            textPaint
                        )
                    }
                    drawRoundRect(
                        color = Color(0xA6F0F0F0),
                        topLeft = Offset(
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f,
                            30f
                        ),
                        cornerRadius = CornerRadius(10f, 10f),
                        size = Size(itemWidth / 1.5f, itemHeight),
                        style = Fill
                    )
                    drawRoundRect(
                        color = Color(0xffAFB3B4),
                        topLeft = Offset(
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f,
                            30f
                        ),
                        cornerRadius = CornerRadius(10f, 10f),
                        size = Size(itemWidth / 1.5f, itemHeight),
                        style = Stroke(width = 2f)
                    )


                    val path = Path().apply {
                        moveTo(
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f + 1 + itemWidth / 1.5f - 1,
                            itemHeight + 30f - addedWater
                        )
                        lineTo(
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f,
                            itemHeight + 30f - addedWater
                        )
                        cubicTo(
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f,
                            itemHeight + 50f - addedWater,
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f + 1 + itemWidth / 3f - 1,
                            itemHeight + 50f - addedWater,
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f + 1 + itemWidth / 2.5f - 1,
                            itemHeight + 10f - addedWater
                        )
                        cubicTo(
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f + 1 + itemWidth / 7f - 1,
                            itemHeight + 50f - addedWater,
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f + 1,
                            itemHeight + 50f - addedWater,
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f + 1 + itemWidth / 1.7f - 1,
                            itemHeight + 20f - addedWater
                        )
                        close()
                    }
                    if (sum < goal)
                        clipPath(path, clipOp = ClipOp.Difference) {
                            drawRoundRect(
                                color = BarColorFill,
                                topLeft = Offset(
                                    0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f + 1,
                                    itemHeight + 30f
                                ),
                                cornerRadius = CornerRadius(10f, 10f),
                                size = Size(itemWidth / 1.5f - 1, -addedWater),
                                style = Fill
                            )
                        }
                    else
                        drawRoundRect(
                            color = BarColorFill,
                            topLeft = Offset(
                                0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f + 1,
                                30f
                            ),
                            cornerRadius = CornerRadius(10f, 10f),
                            size = Size(itemWidth / 1.5f - 1, itemHeight),
                            style = Fill
                        )
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            "${String.format("%.1f", sum / 1000)}l",
                            0f + itemWidth * index + (itemWidth - textPaint.textSize) / 2f,
                            itemHeight + itemHeight / 6f,
                            textPaint
                        )
                    }

                    drawCircle(
                        color = when (sum) {
                            0f -> Color.Transparent
                            averageIntake -> AverageWeekIntakeColor
                            minimalIntake -> MinimalWeekIntakeColor
                            maximalIntake -> MaximalWeekIntakeColor
                            else -> AverageWeekIntakeColor
                        },
                        center = Offset(
                            0f + itemWidth * index + (itemWidth - textPaint.textSize),
                            itemHeight + itemHeight / 4f
                        ),
                        radius = radius
                    )
                }
            }

        }

    }
    Column(
        modifier = Modifier
            .background(ReminderPopBack)
            .padding(horizontal = 10.dp)
            .fillMaxSize()

    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.average_intake),
                modifier = Modifier.height(screenHeight / 20),
                contentDescription = stringResource(R.string.average_intake)
            )
            Text(
                text = stringResource(R.string.average_intake),
                color = Color(0xFF012B51),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = "${String.format("%.2f", averageIntake / 1000)} l",
                color = Color(0xFF6EC0F0)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.average_intake),
                modifier = Modifier.height(screenHeight / 20),
                contentDescription = stringResource(R.string.minimal_intake)
            )
            Text(
                text = stringResource(R.string.minimal_intake),
                color = Color(0xFF012B51),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = "${String.format("%.2f", minimalIntake / 1000)} l",
                color = Color(0xFF6EC0F0)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.average_intake),
                modifier = Modifier.height(screenHeight / 20),
                contentDescription = stringResource(R.string.maximal_intake)
            )
            Text(
                text = stringResource(R.string.maximal_intake),
                color = Color(0xFF012B51),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = "${String.format("%.2f", maximalIntake / 1000)} l",
                color = Color(0xFF6EC0F0)
            )
        }
    }
}

@Composable
fun MonthGraphic(
    viewModel: WaterViewModel,
    goal: Int
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val barPosition = remember {
        mutableStateListOf<BarPositionItem>()
    }
    var waterInfoWindow by remember {
        mutableStateOf<BarPositionItem?>(null)
    }
    val calendar by remember {
        mutableStateOf(Calendar.getInstance())
    }
    val monthDaysCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    Log.d("Week", "Current Week:" + calendar.get(Calendar.YEAR))
    val monthList = remember {
        mutableStateListOf<MonthSum>()
    }

    LaunchedEffect(key1 = true) {
        viewModel.getMonthWaterDrunk(
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )
        viewModel.monthFlow.collectLatest {
            monthList.clear()
            monthList.addAll(it!!.toList())
        }
    }
    var averageIntake = 0f
    var minimalIntake = if (monthList.isNotEmpty()) monthList[0].sum.toFloat() else 0f
    var maximalIntake = 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 10.dp)
            .height(screenHeight / 5 * 2)
            .clip(RoundedCornerShape(20.dp))
            .background(HistoryMenuItemBack)

    ) {

        Column(modifier = Modifier.padding(vertical = 20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(id = R.drawable.previous),
                    contentDescription = stringResource(R.string.previous_month),
                    modifier = Modifier.clickable {
                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)

                        viewModel.getMonthWaterDrunk(
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.YEAR)
                        )
                    }
                )
                Text(
                    text = "${MONTH_LIST[calendar.get(Calendar.MONTH)]} 1 - ${
                        calendar.getActualMaximum(
                            Calendar.DAY_OF_MONTH
                        )
                    }",
                    color = HistoryTextColor, fontSize = 20.sp
                )
                Image(painter = painterResource(id = R.drawable.next),
                    contentDescription = stringResource(R.string.next_month),
                    modifier = Modifier.clickable {
                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1)
                        viewModel.getMonthWaterDrunk(
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.YEAR)
                        )
                    }
                )
            }

            monthList.forEach {
                averageIntake += it.sum
                if (minimalIntake > it.sum)
                    minimalIntake = it.sum.toFloat()
                else if (maximalIntake < it.sum)
                    maximalIntake = it.sum.toFloat()
            }
            averageIntake /= monthDaysCount

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 40.dp, vertical = 40.dp)
                    .tapOrPress { offset ->
                        barPosition.forEachIndexed { _, barPositionItem ->

                            if (offset.x >= barPositionItem.startPosition.x && offset.x <= barPositionItem.endPosition.x
                                && offset.y <= barPositionItem.startPosition.y && offset.y >= barPositionItem.endPosition.y
                            ) {
                                Log.d("OFFSET", "aasd")
                                waterInfoWindow = barPositionItem
                                return@tapOrPress
                            } else {
                                waterInfoWindow = null
                            }
                        }
                    }
            ) {
                val itemWidth = size.width / monthDaysCount
                val windowWidth = size.width / 5
                val itemHeight = size.height
                val maxWaterLimit = if (monthList.isNotEmpty()) monthList.maxOf {
                    if (it.sum > goal)
                        (it.sum.toDouble() / 1000).roundToInt() * 1000
                    else
                        (goal.toDouble() / 1000).roundToInt() * 1000
                } else
                    (goal.toDouble() / 1000).roundToInt() * 1000
                val item = itemHeight / 100
                val count = maxWaterLimit / 1000
                val textPaint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    textSize = 16.sp.toPx()
                    color = Color(0xff5396C2).toArgb()
                    typeface =
                        Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                val winTextPaintOne = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    textSize = (windowWidth/4).toSp().toPx()
                    color = Color.Black.toArgb()
                    typeface =
                        Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                val winTextPaintTwo = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    textSize = (windowWidth/5).toSp().toPx()
                    color = Color(0xff5396C2).toArgb()
                    typeface =
                        Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                for (i in 1..count) {

                    drawIntoCanvas {
                        val lineHeight = itemHeight - item * i * (1000f / maxWaterLimit * 100) + 30f
                        drawLine(
                            Color(0xFFAFB3B4),
                            Offset(0f, lineHeight),
                            Offset(
                                size.width,
                                lineHeight
                            ),
                            strokeWidth = 2f
                        )
                        it.nativeCanvas.drawText(
                            "${i}l",
                            0f - textPaint.textSize * 5 / 4,
                            lineHeight + textPaint.textSize / 3,
                            textPaint
                        )
                    }
                }
                if (waterInfoWindow != null) {
                    val size = (waterInfoWindow!!.endPosition.x - waterInfoWindow!!.startPosition.x) / 2
                    val windowStartPos = waterInfoWindow!!.startPosition.x + size - windowWidth / 2
                    drawRoundRect(
                        Color.White,
                        topLeft = Offset(windowStartPos, 0f),
                        size = Size(windowWidth, -itemHeight/4),
                        cornerRadius = CornerRadius(
                            10f, 10f
                        ),
                        style = Fill
                    )
                    drawRoundRect(
                        Color(0xFFAFB3B4),
                        topLeft = Offset(windowStartPos, 0f),
                        size = Size(windowWidth, -itemHeight/4),
                        cornerRadius = CornerRadius(
                            10f, 10f
                        ),
                        style = Stroke(2f)
                    )
                    drawLine(
                        Color(0xFFAFB3B4),
                        Offset(
                            waterInfoWindow!!.startPosition.x + size,
                            0f
                        ),
                        Offset(
                            waterInfoWindow!!.startPosition.x + size,
                            waterInfoWindow!!.endPosition.y
                        ),
                        strokeWidth = 4f,
                    )
                    //TODO speed of window open
                    drawIntoCanvas {
                        val textOne = "${waterInfoWindow!!.value}l"
                        val textTwo = "${waterInfoWindow!!.day} ${getMonth(waterInfoWindow!!.month).substring(0, 3)}"
                        val textPos = windowStartPos + windowWidth / 2
                        val textOneSize = winTextPaintOne.measureText(textOne)
                        val textTwoSize = winTextPaintTwo.measureText(textTwo)
                        it.nativeCanvas.drawText(
                            textOne,
                            textPos - textOneSize / 2,
                            (0f - itemHeight/4) / 2,
                            winTextPaintOne
                        )
                        it.nativeCanvas.drawText(
                            textTwo,
                            textPos - textTwoSize / 2,
                            (0f - itemHeight/4) / 6,
                            winTextPaintTwo
                        )
                    }
                }

                for (i in 0 until monthDaysCount) {

                    var addedWater = 0f
                    var sum = 0f
                    sum =
                        if (monthList.isEmpty() || monthList.size <= i || monthList[i].sum == 0) 10f else monthList[i].sum.toFloat()

                    addedWater = item * (sum / maxWaterLimit * 100)

                    val topLeft = 0f + itemWidth * i + (itemWidth - textPaint.textSize) / 2f
                    drawRoundRect(
                        color = BarColorFill,
                        topLeft = Offset(
                            topLeft,
                            itemHeight + 30f
                        ),
                        size = Size(itemWidth / 1.5f - 1, -addedWater),
                        style = Fill
                    )


                    if (barPosition.size == monthDaysCount) {
                        barPosition[i] =
                            BarPositionItem(
                                Offset(
                                    topLeft,
                                    itemHeight + 30f
                                ),
                                Offset(
                                    topLeft + itemWidth / 1.5f - 1,
                                    itemHeight + 30f - addedWater
                                ),
                                false,
                                sum / 1000,
                                i + 1,
                                if (monthList.isNotEmpty()) monthList[i].month else 0
                            )
                    } else
                        barPosition.add(
                            BarPositionItem(
                                Offset(
                                    topLeft,
                                    itemHeight + 30f
                                ),
                                Offset(
                                    topLeft + itemWidth / 1.5f - 1,
                                    itemHeight + 30f - addedWater
                                ),
                                false,
                                0f,
                                i + 1,
                                0
                            )
                        )
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .background(ReminderPopBack)
            .padding(horizontal = 10.dp)
            .fillMaxSize()

    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.average_intake),
                modifier = Modifier.height(screenHeight / 20),
                contentDescription = stringResource(R.string.average_intake)
            )
            Text(
                text = stringResource(R.string.average_intake),
                color = Color(0xFF012B51),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = "${String.format("%.2f", averageIntake / 1000)} l",
                color = Color(0xFF6EC0F0)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.average_intake),
                modifier = Modifier.height(screenHeight / 20),
                contentDescription = stringResource(R.string.minimal_intake)
            )
            Text(
                text = stringResource(R.string.minimal_intake),
                color = Color(0xFF012B51),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = "${String.format("%.2f", minimalIntake / 1000)} l",
                color = Color(0xFF6EC0F0)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.average_intake),
                modifier = Modifier.height(screenHeight / 20),
                contentDescription = stringResource(R.string.maximal_intake)
            )
            Text(
                text = stringResource(R.string.maximal_intake),
                color = Color(0xFF012B51),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = "${String.format("%.2f", maximalIntake / 1000)} l",
                color = Color(0xFF6EC0F0)
            )
        }
    }
}

private fun getTotalWeeksInYear(year: Int): Int {
    val mCalendar: Calendar = GregorianCalendar(TimeZone.getDefault())
    mCalendar.firstDayOfWeek = Calendar.MONDAY
    // Workaround
    mCalendar[year, Calendar.DECEMBER] = 31
    val totalDaysInYear = mCalendar[Calendar.DAY_OF_YEAR]
    println(totalDaysInYear)
    return totalDaysInYear / 7
}