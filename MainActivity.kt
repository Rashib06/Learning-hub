package com.dhwani.todo

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhwani.todo.MyApplication.Companion.TO_DO_LIST
import com.dhwani.todo.MyApplication.Companion.allData
import com.dhwani.todo.MyApplication.Companion.getAndroidId
import com.dhwani.todo.adapter.TaskNameListAdapter
import com.github.thunder413.datetimeutils.DateTimeUnits
import com.github.thunder413.datetimeutils.DateTimeUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    lateinit var btnAdd: FloatingActionButton
    lateinit var rvTodo: RecyclerView
    lateinit var fireStore: FirebaseFirestore
    lateinit var drawerLayout: DrawerLayout
    lateinit var ivMenu: ImageView
    lateinit var navMenu: NavigationView
    var isInitialLoadingComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // get start of this week in milliseconds
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        Log.e("TAG", "Start of this week:       " + cal.getTime())
        Log.e("TAG", "... in milliseconds:      " + DateTimeUtils.isToday(DateTimeUtils.formatDate(1676744375663, DateTimeUnits.MILLISECONDS)));

        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        Log.e("TAG", "Start of this week:       " + cal.getTime())
        Log.e("TAG", "... in milliseconds:      " + cal.getTimeInMillis());

        fireStore = FirebaseFirestore.getInstance()

        findViews()
        clickListeners()
        loadFireStoreData()
    }

    private fun findViews() {
        btnAdd = findViewById(R.id.btnAdd)
        rvTodo = findViewById(R.id.rvToDo)
        drawerLayout = findViewById(R.id.dl)
        ivMenu = findViewById(R.id.ivMenu)
        navMenu = findViewById(R.id.nv)
        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerToggle.syncState()
    }

    private fun clickListeners() {
        btnAdd.setOnClickListener {
            createToDo()
        }
        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navMenu.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_my_weekly_task -> {
                    val i = Intent(this@MainActivity, FilteredTaskListActivity::class.java)
                    i.putExtra("MYWEEKLYTASK", true)
                    startActivity(i)
                }
                R.id.menu_today -> {
                    val i = Intent(this@MainActivity, FilteredTaskListActivity::class.java)
                    startActivity(i)
                }
            }
            true
        }

        val switch = (navMenu.menu.findItem(R.id.menu_dark).actionView as SwitchCompat)
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                val pref = getSharedPreferences(packageName, MODE_PRIVATE)
                pref.edit().putBoolean("DARKMODE", isChecked).commit()
                setMode()
            }
        }
        switch.isChecked = getSharedPreferences(packageName, MODE_PRIVATE).getBoolean("DARKMODE", false)
        setMode()
    }

    fun setMode() {
        val darkMode = getSharedPreferences(packageName, MODE_PRIVATE).getBoolean("DARKMODE", false)
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun createTodoListRv(list: ArrayList<SingleTask>) {
        val adp = TaskNameListAdapter(list)
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

    fun gotoTaskDetails(name: String) {
        val i = Intent(this, TaskListActivity::class.java)
        i.putExtra("TASKNAME", name)
        startActivity(i)
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
        try {
            if (prgDialog != null && prgDialog?.isShowing == true)
                prgDialog?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                        try {
                            for (tl in t.get("arlTaskDetails") as ArrayList<*>) {
                                val n: String = (tl as HashMap<*, *>).get("name").toString()
                                val c = (tl).get("completed").toString().toBoolean()
                                val d = tl.get("dueDateTime").toString().toLong()
                                Log.e("TAG", "loadFireStoreData: ${arl.size} :: ${n} :: ${c}")
                                arl.add(TaskDetails(n, c, d))
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
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