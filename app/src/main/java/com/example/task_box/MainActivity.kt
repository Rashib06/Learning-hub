package com.example.task_box

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
        // rvTodo.adapter = adp
        rvTodo.layoutManager = LinearLayoutManager(this)
    }

    private fun createToDo(taskName: String = "") {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_create_to_do)
        dialog.show()

        val etToDo = dialog.findViewById<EditText>(R.id.etName)
        etToDo.setText(taskName)

        dialog.findViewById<Button>(R.id.btnCreate).setOnClickListener {

        }

        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun editTaskName(taskName: String) {
        createToDo(taskName)
    }


    @SuppressLint("HardwareIds")
    fun getAndroidId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            ""
        }
    }




    /*
    * Firebase call
    *
    * */

    private fun loadFireStoreData() {

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


}
}