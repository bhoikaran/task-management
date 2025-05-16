package com.example.taskmanagement.businesslogic.interactors

interface ViewPageListener {
    fun onItemMove(position: Int)
    fun onItemNavigateTo(position: Int)

    val pageCount: Int
}