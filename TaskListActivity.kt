package com.dhwani.todo

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhwani.todo.MyApplication.Companion.TO_DO_LIST
import com.dhwani.todo.MyApplication.Companion.allData
import com.dhwani.todo.MyApplication.Companion.getAndroidId
import com.dhwani.todo.adapter.TaskListAdapter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class TaskListActivity : AppCompatActivity() {

    lateinit var rvTaskList: RecyclerView
    lateinit var btnAdd: Button
    lateinit var etAddTask: EditText
    lateinit var ivBack: ImageView
    var taskName: String? = null
    lateinit var fireStore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}