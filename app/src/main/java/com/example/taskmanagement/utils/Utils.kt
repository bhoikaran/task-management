package com.example.taskmanagement.utils

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.taskmanagement.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Utils {


    fun showMessageYesNo(
        context: Context,
        message: String?,
        okListener: DialogInterface.OnClickListener?
    ) {
        AlertDialog.Builder(context, R.style.DialogTheme)
            .setMessage(message)
            .setPositiveButton(
                context.resources.getString(R.string.text_yes),
                okListener
            )
            .setNegativeButton(context.resources.getString(R.string.text_no), null)
            .create()
            .show()
    }
    /*  fun showMessageYesNo(context: Context, message: String, listener: DialogInterface.OnClickListener) {
          AlertDialog.Builder(context)
              .setMessage(message)
              .setPositiveButton("Yes", listener)
              .setNegativeButton("No", null)
              .show()
      }*/


    fun openDatePicker(
        context: Context,
        currentDate: Long = System.currentTimeMillis(),
        minDate: Long? = null,
        onDateSelected: (Long, String) -> Unit
    ) {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }

        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedMillis = calendar.timeInMillis
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(selectedMillis))
                onDateSelected(selectedMillis, formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        minDate?.let { datePicker.datePicker.minDate = it }

        datePicker.show()
    }

    fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
    }



}