package com.dhwani.todo

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.provider.Settings
import com.google.firebase.FirebaseApp
import com.pixplicity.easyprefs.library.Prefs

class MyApplication : Application() {

    companion object {
        var TO_DO_LIST = "TODOLIST"
        var allData = TasksBean()
        val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"

        @SuppressLint("HardwareIds")
        fun getAndroidId(context: Context): String {
            return try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
                ""
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(baseContext)

        //Shared Prefs
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()

        createNotificationChannnel()
    }

    private fun createNotificationChannnel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}