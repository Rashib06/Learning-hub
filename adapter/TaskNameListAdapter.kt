package com.dhwani.todo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.dhwani.todo.MainActivity
import com.dhwani.todo.R
import com.dhwani.todo.SingleTask

class TaskNameListAdapter(private val list: ArrayList<SingleTask>) : RecyclerView.Adapter<TaskNameListAdapter.VHolder>() {

    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHolder {
        context = parent.context
        return VHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_home_screen_task, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VHolder, position: Int) {
        holder.setData(position)
    }

    inner class VHolder(itemView: View) : ViewHolder(itemView) {
        fun setData(position: Int) {
            itemView.findViewById<TextView>(R.id.tvName).text = list[position].taskName

            itemView.setOnClickListener {
                (context as MainActivity).gotoTaskDetails(list[position].taskName)
            }

            itemView.findViewById<ImageView>(R.id.ivEdit).setOnClickListener {
                (context as MainActivity).editTaskName(list[position].taskName)
            }

            itemView.findViewById<ImageView>(R.id.ivDelete).setOnClickListener {
                (context as MainActivity).deleteTask(list[position].taskName)
            }
        }
    }
}