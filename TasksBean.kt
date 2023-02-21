package com.dhwani.todo

import com.github.thunder413.datetimeutils.DateTimeUtils
import com.google.gson.annotations.SerializedName

data class TasksBean(
    @SerializedName("taskList")
    var taskList: ArrayList<SingleTask> = ArrayList()
)

data class SingleTask(
    @SerializedName("taskName")
    var taskName: String = "",
    @SerializedName("arlTaskDetails")
    var arlTaskDetails: ArrayList<TaskDetails> = ArrayList()
)

data class TaskDetails(
    @SerializedName("name")
    var name: String = "",
    @SerializedName("isCompleted")
    var isCompleted: Boolean = false,
    @SerializedName("dueDate")
    var dueDateTime: Long = 0
) {
//    fun getDueDate(): String {
//        return DateTimeUtils.formatWithPattern(DateTimeUtils.formatDate(dueDateTime), "dd/MM/yyyy HH:mm")
//    }
}