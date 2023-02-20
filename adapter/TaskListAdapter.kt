package com.dhwani.todo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dhwani.todo.R
import com.dhwani.todo.TaskDetails
import com.dhwani.todo.TaskListActivity
import com.github.thunder413.datetimeutils.DateTimeUnits
import com.github.thunder413.datetimeutils.DateTimeUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TaskListAdapter(private val list: ArrayList<TaskDetails>)
