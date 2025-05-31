package com.example.taskmanagement.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.UserModel
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook



class ExportToExcel {

    fun createExcelWorkbook(taskList: List<TaskModel>, users: List<UserModel>): XSSFWorkbook {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Tasks")

        // Create header
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("Index")
        header.createCell(1).setCellValue("Title")
        header.createCell(2).setCellValue("Description")
        header.createCell(3).setCellValue("Assign Person")
        header.createCell(4).setCellValue("Assign Date")
        header.createCell(5).setCellValue("Completion Date")
        header.createCell(6).setCellValue("Status")
        header.createCell(7).setCellValue("Remark")

        // Create data rows
        for ((i, task) in taskList.withIndex()) {
            val row = sheet.createRow(i + 1)
            val assignPersonName = users.find { it.uid == task.assignPersonId }?.name ?: "Unknown"

            row.createCell(0).setCellValue((i + 1).toDouble())
            row.createCell(1).setCellValue(task.title)
            row.createCell(2).setCellValue(task.taskDetail)
            row.createCell(3).setCellValue(assignPersonName)
            row.createCell(4).setCellValue(Utils().formatDate(task.assignDate))

            // Safe handling for nullables
            row.createCell(5).setCellValue(task.completionDate?.let { Utils().formatDate(it) } ?: "")
            row.createCell(6).setCellValue(task.status.name)
            row.createCell(7).setCellValue(task.remark ?: "")
        }

        // Auto-size columns
        for (i in 0..7) {
            sheet.autoSizeColumn(i)
        }

        return workbook
    }

    fun saveWorkbookToUri(context: Context, uri: Uri, workbook: XSSFWorkbook) {
        var outputStream = context.contentResolver.openOutputStream(uri)
        try {
            outputStream?.use { stream ->
                workbook.write(stream)
                Toast.makeText(
                    context,
                    "Excel exported successfully",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Export failed: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            Log.e("ExcelExport", "Error saving file", e)
        } finally {
            try {
                workbook.close()
                outputStream?.close()
            } catch (e: Exception) {
                Log.e("ExcelExport", "Error closing resources", e)
            }
        }
    }
}

/*
class ExportToExcel {

    fun createExcelWorkbook(taskList: List<TaskModel>, users: List<UserModel>): XSSFWorkbook {
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
            val assignPersonName = users.find { it.uid == task.assignPersonId }?.name ?: "Unknown"
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
*/
