package com.clifertam.watertracker.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.clifertam.watertracker.MainActivity
import com.clifertam.watertracker.R
import com.clifertam.watertracker.ui.screen.CHANNEL_ID

class IntakeWorker(ctx: Context, params: WorkerParameters): Worker(ctx, params) {
    override fun doWork(): Result {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            555,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Water Intake")
            .setContentText("It is time to drink daily water.")
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