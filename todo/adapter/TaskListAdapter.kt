package com.dhwani.todo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dhwani.todo.R
import com.dhwani.todo.TaskDetails
import com.dhwani.todo.TaskListActivity
import com.github.thunder413.datetimeutils.DateTimeUnits
import com.github.thunder413.datetimeutils.DateTimeUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TaskListAdapter(private val list: ArrayList<TaskDetails>) : RecyclerView.Adapter<TaskListAdapter.VHolder>() {

    lateinit var context: Context

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
            itemView.findViewById<TextView>(R.id.tvDueDate).text = getCurrentTime(list[position].dueDateTime)

            itemView.findViewById<TextView>(R.id.tvDueDate).setOnClickListener {
                (context as TaskListActivity).pickDateAndTime(list[position])
            }

//            itemView.findViewById<ImageView>(R.id.ivEdit).setOnClickListener {
//
//            }
//
//            itemView.findViewById<ImageView>(R.id.ivDelete).setOnClickListener {
//
//            }
        }
    }

    fun getCurrentTime(time: String): String {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val sdfOutPutToSend = SimpleDateFormat("dd/MM/yyyy")
            sdfOutPutToSend.timeZone = TimeZone.getDefault()
            val gmt = sdf.parse(time)
            return sdfOutPutToSend.format(gmt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return time
    }

    fun getCurrentTime(time: Long): String {
        val c = Calendar.getInstance()
        c.timeInMillis = time
        return try {
            "${c.get(Calendar.DAY_OF_MONTH)}/${c.get(Calendar.MONTH) + 1}/${c.get(Calendar.YEAR)}"
        } catch (e: Exception) {
            DateTimeUtils.formatWithPattern(DateTimeUtils.formatDate(time, DateTimeUnits.MILLISECONDS), "dd/MM/yyyy")
        }
    }
}