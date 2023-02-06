package com.dhwani.todo

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {

    companion object {
        var allData = TasksBean()
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(baseContext)
    }
}