package com.dhwani.todo

data class FilteredTask(
    val taskHeading: String = "",
    val taskName: String = "",
    val isCompleted: Boolean = false,
    val dueDate: Long = 0
)