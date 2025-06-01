package com.example.taskmanagement.utils

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.model.TaskModel
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

    // Shares as plain text in message body
    fun shareTaskDetails(context: Context, task: TaskModel, assigneeName: String) {
        val shareText = """
        📝 Task Details
        
        🏷️ Title: ${task.title}
        📄 Description: ${task.taskDetail}
        👤 Assigned To: $assigneeName
        📅 Assigned Date: ${Utils().formatDate(task.assignDate)}
        ✅ Completion Date: ${task.completionDate?.let { Utils().formatDate(it) } ?: "Not completed"}
        🚦 Status: ${task.status.name.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }}
        💬 Remarks: ${task.remark ?: "None"}
        💬 User Remarks: ${task.userRemark ?: "None"}
    """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Task: ${task.title}")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Task Details"))
    }

}