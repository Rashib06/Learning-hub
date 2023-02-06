package com.example.task_box


import com.google.gson.annotations.SerializedName

data class TasksBean(
    @SerializedName("taskList")
    val taskList: ArrayList<SingleTask> = ArrayList()
)

data class SingleTask(
    @SerializedName("taskName")
    var taskName: String = "",
    @SerializedName("arlTaskDetails")
    val arlTaskDetails: ArrayList<TaskDetails> = ArrayList()
)

data class TaskDetails(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false
)