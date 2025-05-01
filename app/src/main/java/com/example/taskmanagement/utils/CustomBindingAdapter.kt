package com.example.taskmanagement.utils

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.taskmanagement.R
import com.example.taskmanagement.model.Status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CustomBindingAdapter {


    @JvmStatic
    @BindingAdapter("formattedDate")
    fun setFormattedDate(textView: TextView, timestamp: Long?) {
        timestamp?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = sdf.format(Date(it))
            textView.text = formattedDate
        } ?: run {
            textView.text = "-"
        }
    }

    @JvmStatic
    @BindingAdapter("statusColor")
    fun setStatusColor(view: TextView, status: Status) {
        val colorRes = when (status) {

            Status.IN_PROGRESS -> R.color.status_in_progress
            Status.COMPLETED -> R.color.status_completed
            else -> R.color.status_unknown
        }
        view.setTextColor(ContextCompat.getColor(view.context, colorRes))
    }

}