package com.dhwani.todo

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.dhwani.todo.MyApplication.Companion.CHANNEL_ID
import com.dhwani.todo.TaskListActivity.Companion.name

class AlarmService : Service() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
        val task = intent.getStringExtra(name)
        val alarmTitle = "your task ${task} time is going to end"

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(alarmTitle)
            .setContentText("Ring Ring .. Ring Ring")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun channelComment(channelId: String, context: Context, builde: NotificationCompat.Builder) {
        builde.setChannelId(channelId)
        val description = context.getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_HIGH

        val mChannel = NotificationChannel(channelId, "Comment Notifications", importance)
        mChannel.description = description
        mChannel.enableLights(true)
        mChannel.lightColor = Color.parseColor("#3ECAFC")
        mChannel.enableVibration(true)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}