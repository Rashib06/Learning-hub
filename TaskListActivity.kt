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
        setContentView(R.layout.activity_task_list)

        taskName = intent.getStringExtra("TASKNAME")

        fireStore = FirebaseFirestore.getInstance()

        rvTaskList = findViewById(R.id.rvTaskList)
        btnAdd = findViewById(R.id.btnAdd)
        etAddTask = findViewById(R.id.etAddTask)
        ivBack = findViewById(R.id.ivBack)

        Listeners()
        createTaskListRecyclerView()
    }

    private fun Listeners() {
        ivBack.setOnClickListener {
            onBackPressed()
        }

        btnAdd.setOnClickListener {
            if (etAddTask.text.toString().isNotEmpty()) {
                addListToFirebase(etAddTask.text.toString(), Calendar.getInstance().timeInMillis)
            } else {
                Toast.makeText(this, "Please add task name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createTaskListRecyclerView() {
        val arl = allData.taskList.find {
            it.taskName == taskName
        }?.arlTaskDetails
        Log.e("TAG", "createTaskListRecyclerView: ${arl?.size}")
        rvTaskList.adapter = TaskListAdapter(arl ?: ArrayList())
        rvTaskList.layoutManager = LinearLayoutManager(this)
    }

    fun pickDateAndTime(task: TaskDetails) {
        val c = Calendar.getInstance()
        c.timeInMillis = task.dueDateTime
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this, { view, year, month, day ->
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, month)
            c.set(Calendar.DAY_OF_MONTH, day)
            addListToFirebase(task.name, c.timeInMillis, true)
        }, year, month, day).show()
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

    /*
    *
    * Firebase calling
    * */
    private fun addListToFirebase(toDoName: String, time: Long, isEdit: Boolean = false) {
        if (allData.taskList.find { it.taskName == taskName }?.arlTaskDetails?.any { it.name.trim() == toDoName.trim() } == true && !isEdit) {
            Toast.makeText(this, "This task is already exists.", Toast.LENGTH_SHORT).show()
            return
        }
        showProgressDialog()
        if (isEdit) {
            allData.taskList.find {
                it.taskName == taskName
            }?.arlTaskDetails?.find {
                it.name == toDoName
            }?.dueDateTime = time
        } else {
            val task = TaskDetails(toDoName, false, time)
            allData.taskList.find {
                it.taskName == taskName
            }?.arlTaskDetails?.add(task)
        }
        fireStore.collection(TO_DO_LIST).document(getAndroidId(this)).set(allData).addOnCompleteListener {
            loadFireStoreData()
            dismissProgressDialog()
        }.addOnFailureListener {
            dismissProgressDialog()
            Log.e("TAG", "addListToFirebase: failed: ")
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFireStoreData() {
        showProgressDialog()
        fireStore.collection(TO_DO_LIST).document(getAndroidId(this))
            .get().addOnCompleteListener {
                MyApplication.allData.taskList.clear()
                try {
                    for (t in (it.result.get("taskList") as ArrayList<*>)) {
                        val taskName = (t as HashMap<*, *>).get("taskName").toString()
                        val arl = ArrayList<TaskDetails>()
                        for (tl in t.get("arlTaskDetails") as ArrayList<*>) {
                            val n: String = (tl as HashMap<*, *>).get("name").toString()
                            val c = (tl).get("completed").toString().toBoolean()
                            val d = tl.get("dueDateTime").toString().toLong()
                            arl.add(TaskDetails(n, c, d))
                        }
                        val single = SingleTask(taskName, arl)
                        MyApplication.allData.taskList.add(single)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                createTaskListRecyclerView()
                dismissProgressDialog()
            }.addOnFailureListener() {
                dismissProgressDialog()
                Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
            }
    }

    fun getTimeString(): String {
        val date = Date(System.currentTimeMillis())
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
        return simpleDateFormat.format(date)
    }
}