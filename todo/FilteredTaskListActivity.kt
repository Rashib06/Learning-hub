package com.dhwani.todo

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhwani.todo.adapter.FilteredTaskListAdapter
import com.github.thunder413.datetimeutils.DateTimeUnits
import com.github.thunder413.datetimeutils.DateTimeUtils
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

class FilteredTaskListActivity : AppCompatActivity() {

    lateinit var rvTaskList: RecyclerView
    lateinit var ivBack: ImageView
    lateinit var fireStore: FirebaseFirestore
    var myWeeklyTask = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filtered_task_list)

        fireStore = FirebaseFirestore.getInstance()

        rvTaskList = findViewById(R.id.rvTaskList)
        ivBack = findViewById(R.id.ivBack)

        myWeeklyTask = intent.getBooleanExtra("MYWEEKLYTASK", false)

        Listeners()
        createTaskListRecyclerView()

    }

    private fun Listeners() {
        ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun createTaskListRecyclerView() {
        val arl = getTaskList()
        rvTaskList.adapter = FilteredTaskListAdapter(arl)
        rvTaskList.layoutManager = LinearLayoutManager(this)
    }

    fun getTaskList(): ArrayList<FilteredTask> {
        if (myWeeklyTask) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            val start = cal.getTimeInMillis()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            val end = cal.getTimeInMillis()
            val arl = ArrayList<FilteredTask>()
            MyApplication.allData.taskList.forEach { it ->
                it.arlTaskDetails.forEach { singleTask ->
                    if (singleTask.dueDateTime in start..end) {
                        arl.add(FilteredTask(it.taskName, singleTask.name, singleTask.isCompleted, singleTask.dueDateTime))
                    }
                }
            }
            return arl
        } else {
            val arl = ArrayList<FilteredTask>()
            MyApplication.allData.taskList.forEach { it ->
                it.arlTaskDetails.forEach { singleTask ->
                    if (DateTimeUtils.isToday(DateTimeUtils.formatDate(singleTask.dueDateTime, DateTimeUnits.MILLISECONDS))) {
                        arl.add(FilteredTask(it.taskName, singleTask.name, singleTask.isCompleted, singleTask.dueDateTime))
                    }
                }
            }
            return arl
        }
    }

    var prgDialog: Dialog? = null
    private fun showProgressDialog() {
        if (prgDialog == null) {
            prgDialog = Dialog(this)
            prgDialog?.setContentView(R.layout.loader)
            prgDialog?.setCancelable(false)
            prgDialog?.setCanceledOnTouchOutside(false)
            prgDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            prgDialog?.window?.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        prgDialog?.show()
    }

    fun dismissProgressDialog() {
        if (prgDialog != null && prgDialog?.isShowing == true)
            prgDialog?.dismiss()
    }
}