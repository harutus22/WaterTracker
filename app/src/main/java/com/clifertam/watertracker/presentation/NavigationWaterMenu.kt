package com.clifertam.watertracker.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clifertam.watertracker.R
import com.clifertam.watertracker.model.MenuItem
import com.clifertam.watertracker.ui.theme.MainColor
import com.clifertam.watertracker.utils.LOG_OUT_ID


@Composable
fun NavigationHeader(onCloseClick: () -> Unit) {
    Column(modifier = Modifier
        .padding(vertical = 40.dp),) {
        Icon(
            Icons.Filled.Close,
            contentDescription = null,
            tint = MainColor,
            modifier = Modifier.align(alignment = Alignment.End)
                .padding(end = 16.dp).clickable {
                    onCloseClick()
                }
        )
        Row(

        ) {
            Image(
                painter = painterResource(id = R.drawable.drawer_image),
                contentDescription = "WATER TRACKER",
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "WATER TRACKER",
                color = MainColor,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(3f)
                    .align(alignment = Alignment.CenterVertically)
            )
        }
    }

}

@Composable
fun NavigationBody(
    items: List<MenuItem>,
    modifier: Modifier = Modifier,
    itemTextStyle: TextStyle = TextStyle(fontSize = 18.sp),
    onItemClick: (MenuItem) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onItemClick(item)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = item.icon),
                    contentDescription = item.contentDescription,
                    tint = MainColor,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.title,
                    style = itemTextStyle,
                    modifier = Modifier.weight(1f)
                )
            }
            if (item.id != LOG_OUT_ID) {
                Divider(
                    color = MainColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}