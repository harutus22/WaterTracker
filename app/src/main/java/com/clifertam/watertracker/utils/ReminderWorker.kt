package com.clifertam.watertracker.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.clifertam.watertracker.MainActivity
import com.clifertam.watertracker.R
import com.clifertam.watertracker.ui.screen.CHANNEL_ID
import java.util.*
import java.util.concurrent.TimeUnit

const val REMINDER_DATA_HOUR = "reminder_data_hour"
const val REMINDER_DATA_MINUTE = "reminder_data_minute"
const val REMINDER_DATA_TYPE = "reminder_data_type"
const val WAKE_NOTIFICATION_REQUEST_CODE = 123
const val SLEEP_NOTIFICATION_REQUEST_CODE = 321

class ReminderWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, inputData.getInt(REMINDER_DATA_HOUR, 0))
        dueDate.set(Calendar.MINUTE, inputData.getInt(REMINDER_DATA_MINUTE, 0))
        dueDate.set(Calendar.SECOND, 0)
        val isNight = inputData.getBoolean(REMINDER_DATA_TYPE, true)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val constraints = Constraints.Builder()
            .build()
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val dailyWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setConstraints(constraints).setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(dailyWorkRequest)

        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            if (isNight)   SLEEP_NOTIFICATION_REQUEST_CODE else WAKE_NOTIFICATION_REQUEST_CODE,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = if (isNight) applicationContext.getString(R.string.time_to_sleep_title)
            else applicationContext.getString(R.string.wake_time_title)
        val content = if (isNight) applicationContext.getString(R.string.time_to_sleep_content)
            else applicationContext.getString(R.string.wake_time_content)
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setStyle(
//                NotificationCompat.BigTextStyle()
//                    .bigText("descs")
//            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        return Result.success()
    }
}