package com.example.taskmanagement.businesslogic.viewmodel

import android.R
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.taskmanagement.MyApplication


class ViewModelMain(mApplication: MyApplication) : ViewModelBase(mApplication) {


    private val selectedItemId = MutableLiveData<Int>();

    fun getSelectedItemId(): LiveData<Int> {
        return selectedItemId
    }

    fun setSelectedItemId(itemId: Int) {
        selectedItemId.value = itemId
    }
}