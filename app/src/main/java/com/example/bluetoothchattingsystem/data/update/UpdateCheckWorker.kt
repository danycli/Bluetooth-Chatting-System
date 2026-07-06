package com.example.bluetoothchattingsystem.data.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class UpdateCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "bchat_updates_channel"
        private const val NOTIFICATION_ID = 1002
    }

    override suspend fun doWork(): Result {
        Log.d("UpdateCheckWorker", "Background periodic update check running...")
        val updateManager = AppUpdateManager(applicationContext)

        try {
            val updateInfo = updateManager.checkForUpdates(force = false)
            if (updateInfo != null) {
                Log.i("UpdateCheckWorker", "New update available: ${updateInfo.versionName}. Sending notification.")
                sendUpdateNotification(updateInfo)
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("UpdateCheckWorker", "Error checking for updates in background", e)
            return Result.failure()
        }
    }

    private fun sendUpdateNotification(updateInfo: AppUpdateInfo) {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when a new version of B-Chat is available"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val iconResId = context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
        val defaultIcon = if (iconResId != 0) iconResId else android.R.drawable.stat_sys_download_done

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(defaultIcon)
            .setContentTitle("B-Chat Update Available")
            .setContentText("Version ${updateInfo.versionName} is now available to download.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Version ${updateInfo.versionName} is now available!\n\nRelease Notes:\n${updateInfo.releaseNotes}"
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
