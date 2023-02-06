package com.dhwani.todo

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhwani.todo.MyApplication.Companion.allData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    lateinit var btnAdd: FloatingActionButton
    lateinit var rvTodo: RecyclerView
    lateinit var fireStore: FirebaseFirestore
    var isInitialLoadingComplete = false
    var TO_DO_LIST = "TODOLIST"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fireStore = FirebaseFirestore.getInstance()

        findViews()
        clickListeners()
        loadFireStoreData()
    }

    private fun findViews() {
        btnAdd = findViewById(R.id.btnAdd)
        rvTodo = findViewById(R.id.rvToDo)
    }

    private fun clickListeners() {
        btnAdd.setOnClickListener {
            createToDo()
        }
    }

    private fun createTodoListRv(list: ArrayList<SingleTask>) {
        val adp = TaskListAdapter(list)
        rvTodo.adapter = adp
        rvTodo.layoutManager = LinearLayoutManager(this)
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
            addListToFirebase(etToDo.text.toString(), taskName)
        }

        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun editTaskName(taskName: String) {
        createToDo(taskName)
    }

    fun deleteTask(toDoName: String) {
        deleteDataFromFirebase(toDoName)
    }

    @SuppressLint("HardwareIds")
    fun getAndroidId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            ""
        }
    }

    var prgDialog: Dialog? = null
    fun showProgressDialog() {
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
    * Firebase call
    *
    * */

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
                            arl.add(TaskDetails(n, c))
                        }
                        val single = SingleTask(taskName, arl)
                        allData.taskList.add(single)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                createTodoListRv(allData.taskList)
                dismissProgressDialog()
            }.addOnFailureListener() {
                dismissProgressDialog()
                Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addListToFirebase(toDoName: String, oldName: String = "") {
        showProgressDialog()
        if (oldName.isEmpty()) {
            val arl = ArrayList<TaskDetails>()
            arl.add(TaskDetails("'abc'", false))
            val task = SingleTask(taskName = toDoName, arl)
            allData.taskList.add(task)
        } else {
            allData.taskList.find {
                it.taskName == oldName
            }?.taskName = toDoName
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

    private fun deleteDataFromFirebase(toDoName: String) {
        showProgressDialog()
        allData.taskList.remove(allData.taskList.find { it.taskName == toDoName })

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
}