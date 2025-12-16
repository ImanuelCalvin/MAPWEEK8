package com.example.mapweek8

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class SecondNotificationService : Service() {

    private lateinit var builder: NotificationCompat.Builder
    private lateinit var handler: Handler

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        val channelId = CHANNEL_ID
        createNotificationChannel(channelId)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )

        builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Third worker process is done")
            .setContentText("Second notification running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        // Cara AMAN untuk target SDK tinggi
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            builder.build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
        )

        val thread = HandlerThread("SecondNotificationThread")
        thread.start()
        handler = Handler(thread.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        handler.post {
            countdown()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Second Worker Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun countdown() {
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        for (i in 5 downTo 0) {
            Thread.sleep(1000L)
            builder.setContentText("$i seconds remaining")
            manager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    companion object {
        private const val CHANNEL_ID = "002"
        private const val NOTIFICATION_ID = 2002
    }
}
