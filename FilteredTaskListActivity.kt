package com.dhwani.todo

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
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
    lateinit var ivSearch: ImageView
    lateinit var tvTitle: TextView
    lateinit var etSearch: EditText
    lateinit var fireStore: FirebaseFirestore
    private var myWeeklyTask: Int = 0

    override fun onBackPressed() {
        if (etSearch.visibility == View.VISIBLE) {
            manageSearchVisibility(false)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filtered_task_list)

        fireStore = FirebaseFirestore.getInstance()

        rvTaskList = findViewById(R.id.rvTaskList)
        ivBack = findViewById(R.id.ivBack)
        ivSearch = findViewById(R.id.ivSearch)
        tvTitle = findViewById(R.id.tvTitle)
        etSearch = findViewById(R.id.etSearch)

        myWeeklyTask = intent.getIntExtra("MYWEEKLYTASK", 1)

        Listeners()
        createTaskListRecyclerView(getTaskList(""))

    }

    private fun Listeners() {
        ivBack.setOnClickListener {
            onBackPressed()
        }

        ivSearch.setOnClickListener {
            if (etSearch.visibility == View.VISIBLE) {
                manageSearchVisibility(false)
            } else {
                manageSearchVisibility(true)
            }
        }

        etSearch.doOnTextChanged { text, start, before, count ->
            createTaskListRecyclerView(getTaskList(text.toString()) ?: ArrayList())
        }
    }

    fun manageSearchVisibility(show: Boolean) {
        if (show) {
            etSearch.visibility = View.VISIBLE
            tvTitle.visibility = View.GONE
            ivSearch.visibility = View.GONE
        } else {
            etSearch.visibility = View.GONE
            tvTitle.visibility = View.VISIBLE
            ivSearch.visibility = View.VISIBLE
        }
    }

    private fun createTaskListRecyclerView(arl: ArrayList<FilteredTask>) {
        rvTaskList.adapter = FilteredTaskListAdapter(arl)
        rvTaskList.layoutManager = LinearLayoutManager(this)
    }

    fun getTaskList(search: String): ArrayList<FilteredTask> {
        if (myWeeklyTask == 0) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            val start = cal.getTimeInMillis()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            val end = cal.getTimeInMillis()
            val arl = ArrayList<FilteredTask>()
            MyApplication.allData.taskList.forEach { it ->
                it.arlTaskDetails.forEach { singleTask ->
                    if (singleTask.dueDateTime in start..end) {
                        if (search.isEmpty()) {
                            arl.add(FilteredTask(it.taskName, singleTask.name, singleTask.isCompleted, singleTask.dueDateTime))
                        } else {
                            if (singleTask.name.lowercase().contains(search)) {
                                arl.add(FilteredTask(it.taskName, singleTask.name, singleTask.isCompleted, singleTask.dueDateTime))
                            }
                        }
                    }
                }
            }
            return arl
        } else if (myWeeklyTask == 1) {
            val arl = ArrayList<FilteredTask>()
            MyApplication.allData.taskList.forEach { it ->
                it.arlTaskDetails.forEach { singleTask ->
                    if (DateTimeUtils.isToday(DateTimeUtils.formatDate(singleTask.dueDateTime, DateTimeUnits.MILLISECONDS))) {
                        if (search.isEmpty()) {
                            arl.add(FilteredTask(it.taskName, singleTask.name, singleTask.isCompleted, singleTask.dueDateTime))
                        } else {
                            if (singleTask.name.lowercase().contains(search)) {
                                arl.add(FilteredTask(it.taskName, singleTask.name, singleTask.isCompleted, singleTask.dueDateTime))
                            }
                        }
                    }
                }
            }
            return arl
        } else {
            val c = Calendar.getInstance()
            c[Calendar.DAY_OF_MONTH] = 1
            val firstTimeStamp = c.timeInMillis

//            val givenDate: LocalDate = LocalDate.parse(newDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
//            val lastDayOfMonthDateGivenDate: LocalDate = givenDate.withDayOfMonth(givenDate.getMonth().length(givenDate.isLeapYear()))
//            val lastCal = dates.parse(lastDayOfMonthDateGivenDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))


        }

        return ArrayList()
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