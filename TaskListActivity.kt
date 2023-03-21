package com.dhwani.todo

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhwani.todo.MyApplication.Companion.TO_DO_LIST
import com.dhwani.todo.MyApplication.Companion.allData
import com.dhwani.todo.MyApplication.Companion.getAndroidId
import com.dhwani.todo.adapter.TaskListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class TaskListActivity : AppCompatActivity() {

    lateinit var rvTaskList: RecyclerView
    lateinit var btnAdd: FloatingActionButton
    lateinit var ivBack: ImageView
    lateinit var ivSearch: ImageView
    lateinit var ivDeleteAll: ImageView
    lateinit var tvTitle: TextView
    lateinit var etSearch: EditText
    var taskName: String? = null
    lateinit var fireStore: FirebaseFirestore

    override fun onBackPressed() {
        if (etSearch.visibility == View.VISIBLE) {
            manageSearchVisibility(false)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        taskName = intent.getStringExtra("TASKNAME")

        fireStore = FirebaseFirestore.getInstance()

        rvTaskList = findViewById(R.id.rvTaskList)
        btnAdd = findViewById(R.id.btnAdd)
        ivBack = findViewById(R.id.ivBack)
        ivSearch = findViewById(R.id.ivSearch)
        tvTitle = findViewById(R.id.tvTitle)
        etSearch = findViewById(R.id.etSearch)
        ivDeleteAll = findViewById(R.id.ivDelete)

        Listeners()
        createTaskListRecyclerView(getTaskList("") ?: ArrayList())
    }

    private fun Listeners() {
        ivBack.setOnClickListener {
            onBackPressed()
        }

        findViewById<FloatingActionButton>(R.id.btnAdd).setOnClickListener {
            createToDo()
        }

        ivSearch.setOnClickListener {
            if (etSearch.visibility == View.VISIBLE) {
                manageSearchVisibility(false)
            } else {
                manageSearchVisibility(true)
            }
        }

        ivDeleteAll.setOnClickListener {
            deleteAllTask()
        }

        etSearch.doOnTextChanged { text, start, before, count ->
            createTaskListRecyclerView(getTaskList(text.toString()) ?: ArrayList())
        }
    }

    fun manageSearchVisibility(show: Boolean) {
        if (show) {
            etSearch.visibility = View.VISIBLE
            tvTitle.visibility = View.GONE
            ivDeleteAll.visibility = View.GONE
            ivSearch.visibility = View.GONE
        } else {
            etSearch.visibility = View.GONE
            ivDeleteAll.visibility = View.VISIBLE
            tvTitle.visibility = View.VISIBLE
            ivSearch.visibility = View.VISIBLE
        }
    }

    private fun createToDo(taskName: String = "") {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_create_to_do)
        dialog.show()

        val etToDo = dialog.findViewById<EditText>(R.id.etName)
        etToDo.setText(taskName)

        dialog.findViewById<Button>(R.id.btnCreate).setOnClickListener {
            if (etToDo.text.toString().trim().isEmpty()) {
                return@setOnClickListener
            }
            dialog.dismiss()
            if (etToDo.text.toString().isNotEmpty()) {
                addListToFirebase(etToDo.text.toString(), Calendar.getInstance().timeInMillis)
            } else {
                Toast.makeText(this, "Please add task name", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun getTaskList(search: String): ArrayList<TaskDetails>? {
        return if (search.isEmpty()) {
            allData.taskList.find {
                it.taskName == taskName
            }?.arlTaskDetails
        } else {
            allData.taskList.find {
                it.taskName == taskName
            }?.arlTaskDetails?.filter {
                it.name.lowercase().contains(search.lowercase())
            } as ArrayList<TaskDetails>
        }
    }

    private fun createTaskListRecyclerView(arl: ArrayList<TaskDetails>) {
        rvTaskList.adapter = TaskListAdapter(arl)
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

    fun deleteTask(task: TaskDetails) {
        showProgressDialog()
        allData.taskList.find {
            it.taskName == taskName
        }?.arlTaskDetails?.removeIf {
            it.name == task.name
        }
        fireStore.collection(TO_DO_LIST).document(getAndroidId(this)).set(allData).addOnCompleteListener {
            Log.e("TAG", "addListToFirebase: complete")
            loadFireStoreData()
            dismissProgressDialog()
        }.addOnFailureListener {
            dismissProgressDialog()
            Log.e("TAG", "addListToFirebase: failed: ")
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteAllTask() {
        showProgressDialog()
        allData.taskList.find {
            it.taskName == taskName
        }?.arlTaskDetails?.clear()
        fireStore.collection(TO_DO_LIST).document(getAndroidId(this)).set(allData).addOnCompleteListener {
            Log.e("TAG", "addListToFirebase: complete")
            loadFireStoreData()
            dismissProgressDialog()
        }.addOnFailureListener {
            dismissProgressDialog()
            Log.e("TAG", "addListToFirebase: failed: ")
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkUnCheckTask(task: TaskDetails, isCompleted: Boolean) {
        showProgressDialog()
        allData.taskList.find {
            it.taskName == taskName
        }?.arlTaskDetails?.find {
            it.name == task.name
        }?.isCompleted = isCompleted

        fireStore.collection(TO_DO_LIST).document(getAndroidId(this)).set(allData).addOnCompleteListener {
            Log.e("TAG", "addListToFirebase: complete")
            loadFireStoreData()
            dismissProgressDialog()
        }.addOnFailureListener {
            dismissProgressDialog()
            Log.e("TAG", "addListToFirebase: failed: ")
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
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

    /*
    *
    * Firebase calling
    * */
    private fun addListToFirebase(toDoName: String, time: Long, isEdit: Boolean = false) {
//        if (allData.taskList.find { it.taskName == taskName }?.arlTaskDetails?.any { it.name.trim() == toDoName.trim() } == true && !isEdit) {
//            Toast.makeText(this, "This task is already exists.", Toast.LENGTH_SHORT).show()
//            return
//        }
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
//            etAddTask.setText("")
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
                allData.taskList.clear()
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
                createTaskListRecyclerView(getTaskList("") ?: ArrayList())
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