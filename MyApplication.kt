package com.dhwani.todo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import com.google.firebase.FirebaseApp

class MyApplication : Application() {

    companion object {
        var TO_DO_LIST = "TODOLIST"
        var allData = TasksBean()

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
    }
}