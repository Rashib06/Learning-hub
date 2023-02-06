package com.example.task_box

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MyApplication : AppCompatActivity() {
    companion object {
        var allData = TasksBean()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_application)
    }
}