package com.dhwani.todo

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhwani.todo.MyApplication.Companion.TO_DO_LIST
import com.dhwani.todo.MyApplication.Companion.allData
import com.dhwani.todo.MyApplication.Companion.getAndroidId
import com.dhwani.todo.adapter.TaskListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment.OnButtonClickListener
import com.pixplicity.easyprefs.library.Prefs
import java.util.*
import kotlin.collections.ArrayList


class TaskListActivity : AppCompatActivity() {

    companion object {
        val ALARMS = "canceled_alarm"
        val TASK = "TASK"
        val ids = "ids"
        val name = "name"
        val split = "|||||"
    }

    lateinit var rvTaskList: RecyclerView
    lateinit var btnAdd: FloatingActionButton
    lateinit var ivBack: ImageView
    lateinit var ivSearch: ImageView
    lateinit var ivDeleteAll: ImageView
    lateinit var tvTitle: TextView
    lateinit var etSearch: EditText
    var taskName: String? = null
    lateinit var fireStore: FirebaseFirestore
    var pendingIntent: PendingIntent? = null

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
        createTaskListRecyclerView(getTaskList("") ?: emptyList())
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

    fun getTaskList(search: String): List<TaskDetails>? {
        return if (search.isEmpty()) {
            allData.taskList.find {
                it.taskName == taskName
            }?.arlTaskDetails?.sortedBy {
                it.priority
            }?.toList()
        } else {
            allData.taskList.find {
                it.taskName == taskName
            }?.arlTaskDetails?.filter {
                it.name.lowercase().contains(search.lowercase())
            }?.sortedBy {
                it.priority
            }?.toList()
        }
    }

    var adp: TaskListAdapter? = null
    private fun createTaskListRecyclerView(arl: List<TaskDetails>) {
        if (adp == null) {
            adp = TaskListAdapter() {
                Log.e("TAG", "createTaskListRecyclerView: adp: ${it.joinToString()}")
                priorityChanged(it)
            }
            rvTaskList.adapter = adp
            rvTaskList.layoutManager = LinearLayoutManager(this)

            /*val helper = RecyclerHelper<TaskDetails>(arl, adp as RecyclerView.Adapter<RecyclerView.ViewHolder>)
            helper
                .setRecyclerItemDragEnabled(true)
                .setRecyclerItemSwipeEnabled(false)
                .setOnDragItemListener(object : OnDragListener {
                    override fun onDragItemListener(fromPosition: Int, toPosition: Int) {
                        Collections.swap(arl, fromPosition, toPosition)
                        arl.forEachIndexed { index, taskDetails ->
                            taskDetails.priority = index + 1
                        }
                        priorityChanged(arl)
                    }
                })
            val itemTouchHelper = ItemTouchHelper(helper)
            itemTouchHelper.attachToRecyclerView(rvTaskList)*/

            val swipeAndDragHelper = SwipeAndDragHelper(adp!!)
            val touchHelper = ItemTouchHelper(swipeAndDragHelper)
            touchHelper.attachToRecyclerView(rvTaskList)
        }
        adp?.setData(arl)
    }

    fun pickDateAndTime(task: TaskDetails) {
        val c = Calendar.getInstance()
        c.timeInMillis = task.dueDateTime
        c[Calendar.SECOND] = 0

        val dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
            "Pick date and time",
            "OK",
            "Cancel"
        )
        dateTimeDialogFragment.startAtCalendarView()
        dateTimeDialogFragment.set24HoursMode(true)
        dateTimeDialogFragment.setDefaultDateTime(c.time)

        dateTimeDialogFragment.setOnButtonClickListener(object : OnButtonClickListener {
            override fun onPositiveButtonClick(date: Date?) {
                Log.e("TAG", "onPositiveButtonClick: ${date}")
                if (task.notifiable) {
                    setOrRemoveAlarm(task)
                }
                addListToFirebase(task.name, date?.time ?: c.timeInMillis, true, task.id)
            }

            override fun onNegativeButtonClick(date: Date?) {
            }
        })
        dateTimeDialogFragment.show(supportFragmentManager, "dialog_time")
    }

    fun priorityChanged(dataSet: ArrayList<TaskDetails>) {
        showProgressDialog()

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

    fun deleteTask(task: TaskDetails) {
        showProgressDialog()
        allData.taskList.find {
            it.taskName == taskName
        }?.arlTaskDetails?.removeIf {
            it.id == task.id
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

    fun enableDisableAlarm(task: TaskDetails) {
        showProgressDialog()
        allData.taskList.find {
            it.taskName == taskName
        }?.arlTaskDetails?.find {
            if (it.name == task.name && it.dueDateTime == task.dueDateTime) {
                it.notifiable = !it.notifiable
                setOrRemoveAlarm(it)
            }
            it.name == task.name
        }
        fireStore.collection(TO_DO_LIST).document(getAndroidId(this)).set(allData).addOnCompleteListener {
            loadFireStoreData()
            dismissProgressDialog()
        }.addOnFailureListener {
            dismissProgressDialog()
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setOrRemoveAlarm(task: TaskDetails) {
        runOnUiThread {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (task.notifiable) {
                val intent = Intent(this, AlarmReceiver::class.java)
                intent.putExtra(ids, task.id)
                intent.putExtra(name, task.name)
                pendingIntent = PendingIntent.getBroadcast(this, Random().nextInt(Int.MAX_VALUE), intent, FLAG_IMMUTABLE)
                val c = Calendar.getInstance()
                c.timeInMillis = task.dueDateTime
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
                removeFromRemovalList(task)
            } else {
                addToRemovelList(task)
            }
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
            it.id == task.id
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
    private fun addListToFirebase(toDoName: String, time: Long, isEdit: Boolean = false, id: Int = 0) {
        showProgressDialog()
        if (isEdit) {
            allData.taskList.find {
                it.taskName == taskName
            }?.arlTaskDetails?.find {
                it.id == id
            }?.dueDateTime = time
        } else {
            val list = allData.taskList.find {
                it.taskName == taskName
            }?.arlTaskDetails
            val task = TaskDetails(getPriority(list ?: ArrayList()), toDoName, false, time, false)
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
                allData.taskList.clear()
                try {
                    for (t in (it.result.get("taskList") as ArrayList<*>)) {
                        val taskName = (t as HashMap<*, *>).get("taskName").toString()
                        val arl = ArrayList<TaskDetails>()
                        for (tl in t.get("arlTaskDetails") as ArrayList<*>) {
                            val p: Int = (tl as HashMap<*, *>).get("priority").toString().toInt()
                            val n: String = (tl as HashMap<*, *>).get("name").toString()
                            val c = (tl).get("completed").toString().toBoolean()
                            val d = tl.get("dueDateTime").toString().toLong()
                            val noti = tl.get("notifiable").toString().toBoolean()
                            val id = tl.get("id").toString().toInt()
                            arl.add(TaskDetails(p, n, c, d, noti, id))
                        }
                        val single = SingleTask(taskName, arl)
                        allData.taskList.add(single)
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

    fun getPriority(data: ArrayList<TaskDetails>): Int {
        return if (data.isNullOrEmpty()) {
            1
        } else {
            data.size + 1
        }
    }

    private fun addToRemovelList(task: TaskDetails) {
        if (Prefs.contains(ALARMS)) {
            val removelID = Gson().fromJson(Prefs.getString(ALARMS, ""), RemovedAlarms::class.java)
            if (!removelID.alarms.any { it == task.id }) {
                removelID.alarms.add(task.id)
            }
            Log.e("TAG", "addToRemovelList: ${removelID}")
            Prefs.putString(ALARMS, Gson().toJson(removelID))
            val r = Gson().fromJson(Prefs.getString(ALARMS, ""), RemovedAlarms::class.java)
            Log.e("TAG", "addToRemovelList: ${r}")
        } else {
            val remove = RemovedAlarms()
            remove.alarms.add(task.id)
            Prefs.putString(ALARMS, Gson().toJson(remove))
        }
    }

    private fun removeFromRemovalList(task: TaskDetails) {
        if (Prefs.contains(ALARMS)) {
            val removedAlarms = Gson().fromJson(Prefs.getString(ALARMS, ""), RemovedAlarms::class.java)
            removedAlarms.alarms.removeIf {
                it == task.id
            }
            Log.e("TAG", "addToRemovelList: ${removedAlarms}")
            Prefs.putString(ALARMS, Gson().toJson(removedAlarms))
        }
    }
}