package com.clifertam.watertracker.ui.screen

import android.graphics.Typeface
import android.util.Log
import android.widget.NumberPicker
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.clifertam.watertracker.R
import com.clifertam.watertracker.model.DrunkWater
import com.clifertam.watertracker.model.MenuDotItem
import com.clifertam.watertracker.repository.WaterViewModel
import com.clifertam.watertracker.ui.theme.GoalReachedBlur
import com.clifertam.watertracker.ui.theme.MainColor
import com.clifertam.watertracker.ui.theme.MenuHorizontalGradient
import com.clifertam.watertracker.utils.WaterDataStore
import com.clifertam.watertracker.utils.getNameOfWeek
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.clifertam.watertracker.utils.convertPixelsToDp

private var menuDotsPosition = ArrayList<MenuDotItem>()

@Composable
fun MainScreen(
    navController: NavController,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val context = LocalContext.current
    val dataStore = WaterDataStore(context)
    var jarSize by remember {
        mutableStateOf(2000)
    }

    var waterRemainingJar by remember {
        mutableStateOf(0f)
    }
    var waterDrunk by remember {
        mutableStateOf(0f)
    }

    val isMenuOpen = remember {
        mutableStateOf(false)
    }
    var goalReached by remember {
        mutableStateOf(false)
    }
    val viewModel: WaterViewModel = hiltViewModel()
    LaunchedEffect(key1 = true) {
        dataStore.getGoal.collectLatest {
            jarSize = if (it != "0") (it!!.toFloat() * 1000).toInt() else 2000
            viewModel.getDailyWaterSum()
        }
    }

    val jarCount = jarSize / 250
    val jarLimit = if (jarCount == 0 || jarCount == 1) 1 else jarCount * 2

    val imageBitmap: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.jar)
    val imageWidth = imageBitmap.width
    val imageHeight = imageBitmap.height
    val lineShort = imageWidth / 30
    val lineLong = lineShort * 2
    val x = (imageWidth - imageWidth / 16).toFloat() - 32f
    var y = imageHeight.toFloat() - 130f
    val distance = ((imageHeight - imageHeight / 5) / jarLimit).toFloat()
    val yWater = imageHeight.toFloat()
    val topLimit = 120f

    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val pxValue = with(LocalDensity.current) { screenHeight.toPx() }
    val pyValue = with(LocalDensity.current) { screenWidth.toPx() }

    LaunchedEffect(key1 = true) {
        viewModel.waterSum.collectLatest() { water ->
            if (water != null) {
                val percentageY = (imageHeight.toFloat() - imageHeight.toFloat() / 5 + 130f) / 100f
                val numberPercentage = jarSize.toFloat() / 100
                val addWater = (water.toFloat() / numberPercentage)
                waterRemainingJar += percentageY * addWater
                waterDrunk = water.toFloat()
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column {
            Spacer(modifier = Modifier.height(10.dp))
            RemainingToGoal(remainingWater = waterDrunk.toInt(), jarSize) {
                goalReached = it
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {

                val customPainter =
                    object : Painter() {

                        override val intrinsicSize: Size
                            get() = Size(
                                imageWidth.toFloat(),
                                (imageHeight - imageHeight / 16).toFloat()
                            )

                        override fun DrawScope.onDraw() {
                            var count = 0

                            for (i in 0..jarLimit) {
                                val line = if (i % 2 == 0)
                                    lineLong
                                else
                                    lineShort
                                val distanceTextLine = if (line == lineShort) {
                                    lineLong.toFloat()
                                } else {
                                    lineLong * convertPixelsToDp(context, 0.8f)
                                }
                                if (i != 0)
                                drawLine(
                                    color = Color(0xff5396C2),
                                    start = Offset(x, y),
                                    end = Offset(
                                        x - line,
                                        y
                                    ),
                                    strokeWidth = 2f,
                                )
                                if (i % 2 == 0 || i == 0) { //to show 0 change || and i == 0
                                    drawIntoCanvas {
                                        val textPaint = Paint().asFrameworkPaint().apply {
                                            isAntiAlias = true
                                            textSize = 16.sp.toPx()
                                            color = Color(0xff5396C2).toArgb()
                                            typeface =
                                                Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                                        }

                                        val number =
                                            getValidatedNumber(((250 * count).toFloat() / 1000).toString())
                                        if (number != "0.0")
                                        it.nativeCanvas.drawText(
                                            number,
                                            x - line - distanceTextLine,
                                            y + 8.sp.toPx(),
                                            textPaint
                                        )
                                        count++
                                    }
                                }
                                y -= distance
                            }
                            y = imageHeight.toFloat() - 130f
                        }
                    }
                var animationPlayed by remember() { // to play animation only once
                    mutableStateOf(false)
                }
                val leastAddingWater =
                    if (yWater - waterRemainingJar <= topLimit) topLimit else yWater - waterRemainingJar
                val waterAdd by animateFloatAsState( // animate from 0 to 360 degree for 1000ms
                    targetValue = if (animationPlayed) leastAddingWater else 0f,
                    animationSpec = tween(durationMillis = 1000))
                LaunchedEffect(key1 = true) { // fired on view creation, state change triggers the animation
                    animationPlayed = true
                }
                val waterPainter = remember {

                        object : Painter() {
                        val left = 28f
                        val right = 28f
                        override val intrinsicSize: Size
                            get() = Size(
                                imageWidth.toFloat(),
                                (imageHeight).toFloat()
                            )

                        override fun DrawScope.onDraw() {

                            val pathWater1 = Path().apply {

                                moveTo(imageWidth - right, yWater)

                                lineTo(
                                    left,
                                    yWater
                                )
                                lineTo(
                                    left,
                                    waterAdd
                                )
                                cubicTo(
                                    imageWidth - intrinsicSize.width / 1.8f,
                                    waterAdd - imageHeight / 22  + imageHeight / 16,
                                    imageWidth - intrinsicSize.width / 3f,
                                    waterAdd + imageHeight / 16,
                                    imageWidth - right,
                                    waterAdd
                                )
                                lineTo(
                                    imageWidth - right,
                                    waterAdd
                                )
                                close()
                            }
                            val pathWater2 = Path().apply {
                                moveTo(imageWidth - right, yWater)
                                lineTo(left, yWater)
                                lineTo(
                                    left,
                                    waterAdd
                                )
                                cubicTo(
                                    left,
                                    waterAdd + imageHeight / 16,
                                    imageWidth - intrinsicSize.width / 1.2f,
                                    waterAdd ,
                                    imageWidth - right,
                                    waterAdd + imageHeight / 16
                                )
                                lineTo(imageWidth - right, waterAdd)
                                close()
                            }
                            val pathWater3 = Path().apply {
                                moveTo(imageWidth - right, yWater)
                                lineTo(left, yWater)
                                lineTo(
                                    left,
                                    waterAdd + imageHeight / 32
                                )
                                cubicTo(
                                    imageWidth - intrinsicSize.width / 1.0f,
                                    waterAdd + imageHeight / 24,
                                    imageWidth - intrinsicSize.width / 2f,
                                    waterAdd ,
                                    imageWidth - intrinsicSize.width / 4f,
                                    waterAdd + imageHeight / 32
                                )
                                cubicTo(
                                    imageWidth - intrinsicSize.width / 5f,
                                    waterAdd + imageHeight / 28,
                                    imageWidth - intrinsicSize.width / 7f,
                                    waterAdd,
                                    imageWidth - right,
                                    waterAdd + imageHeight / 28
                                )
                                lineTo(imageWidth - right, waterAdd)
                                close()
                            }
                            if (waterDrunk != 0f) {
                                drawPath(path = pathWater1, Color(0xa627E0F9))
                                drawPath(path = pathWater2, Color(0xB347B4FD))
                                drawPath(path = pathWater3, Color(0xa623B0FF))
                            }
                        }
                    }
                }
                Image(
                    painter = waterPainter,
                    contentDescription = "",
                    modifier = Modifier.clip(shape = RectangleShape)
                )
                Image(
                    painter = painterResource(id = R.drawable.jar),
                    contentDescription = "",
                    modifier = Modifier.fillMaxHeight()
                )
                Image(
                    painter = customPainter,
                    contentDescription = "",
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = {

                            isMenuOpen.value = true

                        }, contentColor = MainColor,
                        backgroundColor = Color.White
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(content = {
                        items(getListWater(jarSize, waterDrunk)) { item ->
                            Image(
                                painter = painterResource(id = when (item) {
                                    0 -> R.drawable.empty_glass
                                    1 -> R.drawable.half_glass
                                    else -> R.drawable.full_glass
                                }
                                ),
                                contentDescription = ""
                            )
                        }
                    })
                }

            }

        }

        if (isMenuOpen.value)
            WaterMenu(isMenuOpen = { it, time ->
                isMenuOpen.value = false
                if (it != -1) {
                    val percentageY = (imageHeight.toFloat() - imageHeight.toFloat() / 5 + 130f) / 100f
                    val numberPercentage = jarSize.toFloat() / 100
                    val addWater = (it.toFloat() / numberPercentage)
                    waterRemainingJar += percentageY * addWater
                    waterDrunk += it
                    val rightNow = LocalDateTime.now()
                    val calendar = Calendar.getInstance()
                    viewModel.addWater(DrunkWater(0, time, it,
                        getNameOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
                        rightNow.dayOfMonth,
                        calendar.get(Calendar.WEEK_OF_YEAR),
                        rightNow.monthValue,
                        rightNow.year
                    ))
                }
            })
        if (goalReached)
            GoalReachedPopUp(waterDrunk.toString()) {
                goalReached = false
            }

    }
}

private fun getListWater(
    jarSize: Int,
    waterDrunk: Float
) : List<Int> {
    val array = ArrayList<Int>()
    val drunkPercent = waterDrunk.toFloat()
    val itemCount = jarSize.toFloat() / 8
    for (i in 1 .. 8) {
        if (itemCount * i <= drunkPercent) {
            array.add(2)
        } else if (itemCount * i > drunkPercent && itemCount * (i - 1) < drunkPercent){
            array.add(1)
        } else {
            array.add(0)
        }
    }
    return array.toList()
}

@Composable
fun WaterMenu(
    modifier: Modifier = Modifier.fillMaxSize(),
    isMenuOpen: (Int, String) -> (Unit)
) {
    var pos by remember {
        mutableStateOf(0)
    }
    val waterChosen = 25
    var prevDot by remember {
        mutableStateOf(-1)
    }
    var minutes by remember {
        mutableStateOf("00")
    }
    var hours by remember {
        mutableStateOf("00")
    }
    val cornerRadius = CornerRadius(
        x = 40f,
        y = 40f,
    )
    Box(
        modifier = modifier
            .drawBehind {
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                offset = Offset(0f, size.height - size.height / 5 * 2),
                                size = Size(size.width, size.height / 5 * 2),
                            ),
                            topLeft = cornerRadius,
                            topRight = cornerRadius
                        )
                    )
                }
                menuDotsPosition = ArrayList<MenuDotItem>()
                drawPath(path = path, brush = MenuHorizontalGradient)
                drawOval(
                    color = MainColor,
                    topLeft = Offset(-size.width / 2, size.height - size.height * 2 / 6),
                    size = Size(size.width * 2, size.height)
                )

                val offsetAngleDegree = 25f
                val radius = size.width / 6
                val numberOfDots = 8
                val lineDegree = (180f - offsetAngleDegree * 2) / numberOfDots
                for (dotNumber in 0..numberOfDots) {
                    val angleInDegrees = lineDegree * dotNumber - 180f + offsetAngleDegree
                    val angleRad = Math
                        .toRadians(angleInDegrees.toDouble())
                        .toFloat()

                    val dotDistanceFromMainCircle = radius * 1.8f

                    val dotRadius = radius * .08f
                    var isChecked = false
                    drawCircle(
                        center = Offset(
                            x = (radius + dotDistanceFromMainCircle) * cos(angleRad) + size.center.x,
                            y = (radius + dotDistanceFromMainCircle) * sin(angleRad) + size.height,
                        ),
                        color = Color.White,
                        radius = dotRadius
                    )

                    if (dotNumber == pos) {
                        drawCircle(
                            center = Offset(
                                x = (radius + dotDistanceFromMainCircle) * cos(angleRad) + size.center.x,
                                y = (radius + dotDistanceFromMainCircle) * sin(angleRad) + size.height,
                            ),
                            color = Color.White,
                            radius = dotRadius * 3,
                            style = Stroke(4F)
                        )
                        isChecked = true
                    }
                    menuDotsPosition.add(
                        MenuDotItem(
                            Offset(
                                x = (radius + dotDistanceFromMainCircle) * cos(angleRad) + size.center.x,
                                y = (radius + dotDistanceFromMainCircle) * sin(angleRad) + size.height,
                            ),
                            radius = dotRadius,
                            isChecked = isChecked
                        )
                    )
                }
            }
            .tapOrPress(
                onCompleted = { offset ->
                    val tapDistance = 20
                    menuDotsPosition.forEachIndexed { index, dot ->
                        if (prevDot == index) {

                        } else if (
                            offset.x < dot.position.x + tapDistance
                            && offset.x > dot.position.x - dot.radius - tapDistance &&
                            offset.y < dot.position.y + tapDistance
                            && offset.y > dot.position.y - dot.radius - tapDistance
                        ) {
                            dot.isChecked = true
                            prevDot = index
                            pos = index
                        } else {
                            dot.isChecked = false
                        }
                    }


                }
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val configuration = LocalConfiguration.current

        val screenHeight = configuration.screenHeightDp.dp
        val distanceHeight = 30.dp
        val distanceWidth = 20.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight / 5 * 2 - distanceHeight)
                .padding(end = distanceWidth, start = distanceWidth),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(text = stringResource(R.string.cancel), color = Color(0xff012B51), modifier = Modifier.clickable {
                isMenuOpen(-1, "")
            })
            Text(text = stringResource(R.string.add), color = Color(0xff012B51), modifier = Modifier.clickable {
                isMenuOpen(50 + waterChosen * pos, "$hours:$minutes")
            })
        }
        Text(
            text = "${50 + waterChosen * pos} мл", modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight / 4 + distanceHeight),
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp, start = 10.dp, bottom = 30.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "50 мл", color = Color.White)
            TimeWheelPicker(hours = {
                hours = String.format("%02d", it);
            },
                minutes = {
                    minutes = String.format("%02d", it);
                })
            Text(text = "250 мл", color = Color.White)
        }

    }
}


@Composable
fun RemainingToGoal(
    remainingWater: Int,
    jarSize: Int,
    goalReached: (Boolean) -> Unit
) {

    val percentage = if (remainingWater == 0) 100f else 100 * remainingWater.toFloat() / jarSize
    if (jarSize <= remainingWater)
        goalReached(true)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${String.format("%.2f",percentage)}%",
            color = MainColor,
            fontSize = 24.sp
        )
        Text(text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = MainColor
                )
            ) {
                append("${if (jarSize - remainingWater < 0) 0 else jarSize - remainingWater} ml")
            }
            withStyle(SpanStyle()) {
                append(" to the goal")
            }
        })
    }
}

@Composable
fun Modifier.tapOrPress(
    onCompleted: (offset: Offset) -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.pointerInput(interactionSource) {
        forEachGesture {
            coroutineScope {
                awaitPointerEventScope {
                    val tap = awaitFirstDown().also { it.consumeDownChange() }
                    val up = waitForUpOrCancellation()
                    if (up == null) {
                    } else {
                        up.consumeDownChange()
                        onCompleted(tap.position)
                    }
                }
            }
        }
    }
}

private fun getValidatedNumber(text: String): String {
    // Start by filtering out unwanted characters like commas and multiple decimals
    val filteredChars = text.filterIndexed { index, c ->
        c in "0123456789" ||                      // Take all digits
                (c == '.' && text.indexOf('.') == index)  // Take only the first decimal
    }
    return if (filteredChars.contains('.')) {
        val beforeDecimal = filteredChars.substringBefore('.')
        val afterDecimal = filteredChars.substringAfter('.')
        beforeDecimal.take(3) + "." + afterDecimal.take(2)    // If decimal is present, take first 3 digits before decimal and first 2 digits after decimal
    } else {
        filteredChars.take(3)                     // If there is no decimal, just take the first 3 digits
    }
}

@Composable
fun TimeWheelPicker(
    hours: (Int) -> Unit,
    minutes: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .border(
                width = 2.dp,
                color = Color.White,
                shape = RoundedCornerShape(10.dp)
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AndroidView(
                factory = { context ->
                    NumberPicker(context).apply {
                        setOnValueChangedListener { numberPicker, position, value ->
                            Log.d("PICKER", "i-$position i2-$value")
                            hours(value)
                        }
                        minValue = 0
                        maxValue = 23
                        textColor = context.getColor(R.color.white)
                        selectionDividerHeight = 0
                        setFormatter {
                            String.format("%02d", it);
                        }

                    }
                }
            )
            Text(text = ":", color = Color.White)
            AndroidView(
                factory = { context ->
                    NumberPicker(context).apply {
                        setOnValueChangedListener { numberPicker, position, value ->
                            minutes(value)
                        }
                        minValue = 0
                        maxValue = 59
                        textColor = context.getColor(R.color.white)
                        selectionDividerHeight = 0
                        setFormatter {
                            String.format("%02d", it);
                        }
                    }
                }
            )
        }

    }
}

@Composable
fun GoalReachedPopUp(
    amount: String,
    onCloseClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = GoalReachedBlur), contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .clip(shape = RoundedCornerShape(20.dp))
                .background(Color.White)
            ,
        ) {
            IconButton(onClick = {
                onCloseClick()
            }, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Filled.Close, contentDescription = "", tint = Color.LightGray)
            }
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp), horizontalAlignment = Alignment.CenterHorizontally)
            {
                Image(painter = painterResource(id = R.drawable.drop), contentDescription = "Goal Reached")
                Spacer(modifier = Modifier.height(10.dp))
                Text(lineHeight = 22.sp, text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = MainColor,
                            fontSize = 18.sp,

                        )
                    ) {
                        append("Поздравляем!\n")
                    }
                    withStyle(SpanStyle()) {
                        append("Цель достигнута!\n")
                    }
                    withStyle(SpanStyle()) {
                        append("Вы выпили ")
                    }
                    withStyle(
                        SpanStyle(
                            color = MainColor
                        )
                    ) {
                        append("$amount литра ")
                    }
                    withStyle(SpanStyle()) {
                        append("воды")
                    }
                }, textAlign = TextAlign.Center)
            }
        }
    }
}