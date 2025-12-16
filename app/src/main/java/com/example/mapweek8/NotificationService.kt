package com.example.mapweek8

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        notificationBuilder = startForegroundNotification()

        val handlerThread = HandlerThread("NotificationThread")
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")

        serviceHandler.post {
            countDown(notificationBuilder)
            notifyCompletion(id)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startForegroundNotification(): NotificationCompat.Builder {

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_IMMUTABLE else 0
        )

        val channelId = createNotificationChannel()

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Second worker process is done")
            .setContentText("Notification service running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, builder.build())
        return builder
    }

    private fun createNotificationChannel(): String {
        val channelId = "001"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Worker Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return channelId
    }

    private fun countDown(builder: NotificationCompat.Builder) {
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        for (i in 10 downTo 0) {
            Thread.sleep(1000L)
            builder.setContentText("$i seconds remaining")
            manager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun notifyCompletion(id: String) {
        Handler(Looper.getMainLooper()).post {
            mutableId.value = id
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val EXTRA_ID = "Id"

        private val mutableId = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableId
    }
}
