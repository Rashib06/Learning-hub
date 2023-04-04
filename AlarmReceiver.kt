package com.dhwani.todo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.dhwani.todo.TaskListActivity.Companion.ALARMS
import com.dhwani.todo.TaskListActivity.Companion.TASK
import com.dhwani.todo.TaskListActivity.Companion.ids
import com.dhwani.todo.TaskListActivity.Companion.name
import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("TAG", "onReceive: ${intent?.getIntExtra(ids, 0)} ::: name : ${intent?.getStringExtra(name)}")
        val id = intent?.getIntExtra(ids, 0)
        val removal = Gson().fromJson(Prefs.getString(ALARMS, Gson().toJson(RemovedAlarms())), RemovedAlarms::class.java)
        Log.e("TAG", "onReceive: ${removal}")
        if (!removal.alarms.contains(id)) {
            startAlarmService(context!!, intent)
        }
    }

    private fun startAlarmService(context: Context, intent: Intent?) {
        val intentService = Intent(context, AlarmService::class.java)
        intentService.putExtra(ids, intent?.getIntExtra(ids, 0))
        intentService.putExtra(name, intent?.getStringExtra(name))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService)
        } else {
            context.startService(intentService)
        }
    }
}