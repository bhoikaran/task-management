package com.example.taskmanagement.businesslogic.interactors

import android.view.View


fun interface GeneralItemListener {
    fun onItemClick(view: View?, position: Int, item: Any?)
}