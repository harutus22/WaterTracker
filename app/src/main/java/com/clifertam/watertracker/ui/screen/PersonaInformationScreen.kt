package com.clifertam.watertracker.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.clifertam.watertracker.R
import com.clifertam.watertracker.ui.theme.*
import com.clifertam.watertracker.utils.WaterDataStore
import com.github.krottv.compose.sliders.SliderValueHorizontal
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

val list = listOf("4,l", "3,l", "2,l", "1,l", "0,l")
val listSize = listOf("", "", "", "", "", "", "", "")

@Composable
fun PersonaInformationScreen(
    navController: NavController,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = WaterDataStore(context)
//TODO dataSore only first is working, weight not working

    var text by remember { mutableStateOf("") }
    var weightPerson by remember { mutableStateOf("") }
    var recommendedWater by remember { mutableStateOf(0) }
    val waterValue = remember { mutableStateOf(0f) }
    LaunchedEffect(key1 = true) {
        dataStore.getGoal.collectLatest {
            waterValue.value = it!!.toFloat()
        }
    }
    LaunchedEffect(key1 = true) {
        dataStore.getWeight.collectLatest {
            val nullable = it == null || it == ""
            weightPerson = if (nullable) "" else it!!
            recommendedWater = getRecommendedWater(if (nullable) 0 else it!!.toInt())
        }
    }
    LaunchedEffect(key1 = true) {
        dataStore.getName.collectLatest {
            text = it!!
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            onValueChange = { newText ->
                text = newText
            },
            label = { Text(text = "Your name") },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                cursorColor = RegBtnFst,
                focusedLabelColor = Color.Black,
                focusedIndicatorColor = RegBtnFst,
                unfocusedIndicatorColor = RegBtnFst
            ),
            keyboardOptions =
            KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = weightPerson,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            onValueChange = { newText ->
                if (newText.length < 4) {
                    weightPerson = newText
                    recommendedWater =
                        getRecommendedWater(if (newText == "") 0 else newText.toInt())
                }
            },
            label = { Text(text = "Weight") },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                cursorColor = RegBtnFst,
                focusedLabelColor = Color.Black,
                focusedIndicatorColor = RegBtnFst,
                unfocusedIndicatorColor = RegBtnFst
            ),
            keyboardOptions =
            KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            trailingIcon = {
                Icon(
                    Icons.Filled.Refresh, "",
                    tint = RegBtnFst,
                    modifier = Modifier.clickable {
                        weightPerson = ""
                        recommendedWater = 0
//                        waterValue.value = 0f
                    })
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontSize = 16.sp,
                    )
                ) {
                    append("Your recommended daily goal ")
                }
                withStyle(
                    style = SpanStyle(
                        fontSize = 16.sp,
                        color = RegBtnFst
                    )
                ) {
                    append("${if (recommendedWater == 0) "0" else recommendedWater} ml")
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.much_per_day),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MainColor),
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.weight(3f),
                ) {
                    Text(
                        text = stringResource(R.string.recommendation),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.daily_rate_consumption),
                        color = RecommendText
                    )
                }
                Image(
                    modifier = Modifier.weight(1f),
                    painter = painterResource(id = R.drawable.recomend_girl),
                    contentDescription = ""
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ReminderPopBack),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(250.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding = PaddingValues(vertical = 16.dp)
                                ) {
                                    items(list) {
                                        Text(text = it, color = Color(0xff012B51))
                                    }
                                }
                            }
                            Slider(waterValue) {
                                waterValue.value = getValidatedNumber(it.toString()).toFloat()
                            }
                        }
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bottle),
                            contentDescription = "",
                        )
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = "${waterValue.value} l",
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        scope.launch {
                            dataStore.savePrefs(WaterDataStore.USER_NAME_KEY, text)
                            dataStore.savePrefs(WaterDataStore.USER_WEIGHT_KEY, weightPerson)
                            dataStore.savePrefs(
                                WaterDataStore.USER_GOAL_KEY,
                                waterValue.value.toString()
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(percent = 20),
                    colors = ButtonDefaults.buttonColors(RegBtnFst)
                ) {
                    Text(
                        text = "Save",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun Slider(waterValue: MutableState<Float>, onSlided: (Float) -> Unit) {
    SliderValueHorizontal(
        waterValue.value,
        {
            waterValue.value = it
            onSlided(it)
        },
        modifier = Modifier
            .graphicsLayer {
                rotationZ = 270f
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth
                    )
                )
                layout(placeable.height, placeable.width) {
                    placeable.place(-placeable.width, 0)
                }
            },
        thumbHeightMax = false,
        track = { modifier: Modifier,
                  progress: Float,
                  _, _, _ ->

            Box(
                Modifier
                    .padding(vertical = 30.dp)
                    .height(20.dp)
                    .then(modifier)
            ) {
                val bgTrack = Modifier
                    .border(
                        border = BorderStroke(2.dp, color = Color(0xffC4D0DD)),
                        RoundedCornerShape(100)
                    )
                    .background(
                        Color.White,
                        RoundedCornerShape(100)
                    )

                Spacer(
                    bgTrack
                        .fillMaxHeight()
                        .then(modifier)
                )

                val bgProgress = Modifier.background(
                    SliderGradient, RoundedCornerShape(100)
                )

                Spacer(
                    bgProgress
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress)
                        .then(modifier)
                )
                val drawPadding: Float = with(LocalDensity.current) { 20.dp.toPx() }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val yStart = 0f
                    val yEnd = size.height
                    val distance: Float =
                        (size.width.minus(2 * drawPadding)).div(listSize.size.minus(1))
                    listSize.forEachIndexed { index, step ->
                        drawLine(
                            color = Color(0xffC4D0DD),
                            start = Offset(x = drawPadding + index.times(distance), y = yStart),
                            end = Offset(x = drawPadding + index.times(distance), y = yEnd)
                        )
                    }
                }
            }
        },
        thumb = { modifier, _: Dp,
                  mutableSource,
                  _, _ ->

            val color by animateColorAsState(
                Color.White,
                TweenSpec(durationMillis = 200)
            )

            Box(
                modifier
                    .clip(CircleShape)
                    .background(color)
            )

        },
        thumbSizeInDp = DpSize(40.dp, 40.dp),
        valueRange = 0f..4f,
    )

}

private fun getValidatedNumber(text: String): String {
    // Start by filtering out unwanted characters like commas and multiple decimals
    val filteredChars = text.filterIndexed { index, c ->
        c in "0123456789" ||                      // Take all digits
                (c == '.' && text.indexOf('.') == index)  // Take only the first decimal
    }
    // Now we need to remove extra digits from the input
    return if (filteredChars.contains('.')) {
        val beforeDecimal = filteredChars.substringBefore('.')
        val afterDecimal = filteredChars.substringAfter('.')
        beforeDecimal.take(3) + "." + afterDecimal.take(2)    // If decimal is present, take first 3 digits before decimal and first 2 digits after decimal
    } else {
        filteredChars.take(3)                     // If there is no decimal, just take the first 3 digits
    }
}

private fun getRecommendedWater(weight: Int): Int {
    return 30 * weight
}