/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.keepalive

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.notification.NOTIFICATION_ID_TASK_SERVICE
import com.movtery.zalithlauncher.notification.NotificationChannelData
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlin.system.exitProcess

private const val TAG = "TaskKeepAliveService"

/**
 * ?????? (Task Keep Alive Foreground Service)
 */
class TaskKeepAliveService : Service() {
    companion object {
        const val ACTION_TERMINATE = "com.cubiq.app.action.TERMINATE_TASKS"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationManager.createNotificationChannel(this, NotificationChannelData.TASK_SERVICE_CHANNEL)
        startForegroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_TERMINATE) {
            Logger.info(TAG, "Terminate action received from notification. Stopping all tasks and exiting...")
            TaskKeepAlive.reset()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        }
        startForegroundNotification()
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // User swiped launcher away from recents: cancel active tasks cleanly
        Logger.info(TAG, "Launcher task removed from recents by user. Stopping background tasks.")
        TaskKeepAlive.reset()
        stopSelf()
    }

    private fun startForegroundNotification() {
        val data = NotificationChannelData.TASK_SERVICE_CHANNEL

        val terminateIntent = Intent(this, TaskKeepAliveService::class.java).apply {
            action = ACTION_TERMINATE
        }
        val terminatePendingIntent = PendingIntent.getService(
            this,
            1001,
            terminateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, data.channelId)
            .setContentTitle("Cubiq Download Service")
            .setContentText("Game Files Are Being Downloaded")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(R.drawable.ic_close, "Terminate", terminatePendingIntent)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID_TASK_SERVICE,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID_TASK_SERVICE, notification)
            }
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to start foreground notification", e)
            stopSelf()
        }
    }
}
