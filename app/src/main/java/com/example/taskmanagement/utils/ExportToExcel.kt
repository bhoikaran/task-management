package com.example.taskmanagement.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.User
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ExportToExcel {

    fun createExcelWorkbook(taskList: List<Task>, users: List<User>): XSSFWorkbook {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Tasks")

        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Index")
        header.createCell(1).setCellValue("Title")
        header.createCell(2).setCellValue("Description")
        header.createCell(3).setCellValue("Assign Person")
        header.createCell(4).setCellValue("Assign Date")
        header.createCell(5).setCellValue("Completion Date")
        header.createCell(6).setCellValue("Status")
        header.createCell(7).setCellValue("Remark")

        for ((i, task) in taskList.withIndex()) {
            val row = sheet.createRow(i + 1)
            val assignPersonName = users.find { it.id == task.assignPersonId }?.name ?: "Unknown"
            row.createCell(0).setCellValue((i + 1).toDouble())
            row.createCell(1).setCellValue(task.title)
            row.createCell(2).setCellValue(task.taskDetail)
            row.createCell(3).setCellValue(assignPersonName)
            row.createCell(4).setCellValue(Utils().formatDate(task.assignDate))
            row.createCell(5).setCellValue(task.completionDate?.let { Utils().formatDate(it) })
            row.createCell(6).setCellValue(task.status.name)
            row.createCell(7).setCellValue(task.remark ?: "")
        }

        return workbook
    }

    fun saveWorkbookToUri(context: Context, uri: Uri, workbook: XSSFWorkbook) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                workbook.write(outputStream)
                Toast.makeText(context, "Excel saved to: ${uri.path}", Toast.LENGTH_LONG).show()
            }
            workbook.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
