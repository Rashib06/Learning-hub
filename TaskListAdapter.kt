package com.dhwani.todo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dhwani.todo.R
import com.dhwani.todo.SwipeAndDragHelper
import com.dhwani.todo.TaskDetails
import com.dhwani.todo.TaskListActivity
import com.github.thunder413.datetimeutils.DateTimeUnits
import com.github.thunder413.datetimeutils.DateTimeUtils
import java.util.*
import kotlin.collections.ArrayList


class TaskListAdapter(val onItemMoved: (ArrayList<TaskDetails>) -> Unit) : RecyclerView.Adapter<TaskListAdapter.VHolder>(), SwipeAndDragHelper.ItemTouchHelperAdapter {

    lateinit var context: Context
    private val list: ArrayList<TaskDetails> = ArrayList()

    fun setData(lst: List<TaskDetails>) {
        list.clear()
        list.addAll(lst)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListAdapter.VHolder {
        context = parent.context
        return VHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_single_task, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VHolder, position: Int) {
        holder.setData(position)
    }

    inner class VHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData(position: Int) {
            itemView.findViewById<TextView>(R.id.tvTask).text = list[position].name
            itemView.findViewById<TextView>(R.id.tvDueDate).text = getCurrentDate(list[position].dueDateTime)
            itemView.findViewById<TextView>(R.id.tvDueTime).text = getCurrentTime(list[position].dueDateTime)
            itemView.findViewById<ImageView>(R.id.ivAlarm).setColorFilter(
                if (list[position].notifiable) {
                    ContextCompat.getColor(context, R.color.green)
                } else {
                    ContextCompat.getColor(context, R.color.black)
                }
            )
            val cbCompleted = itemView.findViewById<CheckBox>(R.id.cbCompleted)
            cbCompleted.isChecked = list[position].isCompleted

            itemView.findViewById<ConstraintLayout>(R.id.cl).setOnClickListener {
                (context as TaskListActivity).pickDateAndTime(list[position])
            }

            itemView.findViewById<ImageView>(R.id.ivDelete).setOnClickListener {
                (context as TaskListActivity).deleteTask(list[position])
            }

            itemView.findViewById<ImageView>(R.id.ivAlarm).setOnClickListener {
                (context as TaskListActivity).enableDisableAlarm(list[position])
            }

            cbCompleted.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed)
                    (context as TaskListActivity).checkUnCheckTask(list[position], isChecked)
            }
        }
    }


    fun getCurrentDate(time: Long): String {
        val c = Calendar.getInstance()
        c.timeInMillis = time
        return try {
            "${c.get(Calendar.DAY_OF_MONTH)}/${c.get(Calendar.MONTH) + 1}/${c.get(Calendar.YEAR)}"
        } catch (e: Exception) {
            DateTimeUtils.formatWithPattern(DateTimeUtils.formatDate(time, DateTimeUnits.MILLISECONDS), "dd/MM/yyyy")
        }
    }

    fun getCurrentTime(time: Long): String {
        val c = Calendar.getInstance()
        c.timeInMillis = time
        return try {
            DateTimeUtils.setTimeZone(TimeZone.getDefault().id)
            DateTimeUtils.formatWithPattern(DateTimeUtils.formatDate(time, DateTimeUnits.MILLISECONDS), "HH:mm")
        } catch (e: Exception) {
            "${c.get(Calendar.HOUR_OF_DAY)}/${c.get(Calendar.MINUTE)}"
//            DateTimeUtils.formatWithPattern(DateTimeUtils.formatDate(time, DateTimeUnits.MILLISECONDS), "HH:mm")
        }
    }

    override fun onViewMoved(oldPosition: Int, newPosition: Int) {
        Collections.swap(list, oldPosition, newPosition)
        notifyItemMoved(oldPosition, newPosition)
        Log.e("TAG", "onViewMoved: adp: ${list.joinToString()}")
        list.forEachIndexed { index, taskDetails ->
            taskDetails.priority = index + 1
        }
    }

    override fun onMoveCompleted() {
        onItemMoved(list)
    }
}