package com.example.taskmanagement.view.dialogsearchselect

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.businesslogic.model.PojoDialogSearch


class ViewModelDialogSearch {
    var observerProgressBar: ObservableBoolean = ObservableBoolean(false)
    var observerIsNoRecords: ObservableBoolean = ObservableBoolean(false)
    var observableTitle: ObservableInt = ObservableInt()

    var observableSearch: ObservableString = ObservableString()
    var observableSearchList: ObservableArrayList<PojoDialogSearch?> =
        ObservableArrayList<PojoDialogSearch?>()
}