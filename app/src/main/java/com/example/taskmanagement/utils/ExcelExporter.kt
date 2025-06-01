package com.example.taskmanagement.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ExcelExporter(private val context: Context) {

    /**
     * Creates an Excel workbook with a single "Tasks" sheet, writes it to a temporary file,
     * and then copies the content of the temporary file to the specified URI.
     * This operation is performed on a background thread using Kotlin Coroutines.
     *
     * @param uri The URI where the Excel file should be saved (obtained from ACTION_CREATE_DOCUMENT).
     * @param taskList The list of TaskModel objects to export.
     * @param users The list of UserModel objects to look up assign person names.
     */
    fun exportTasksToExcel(uri: Uri, taskList: List<TaskModel>, users: List<UserModel>) {
        val activity = context as? androidx.appcompat.app.AppCompatActivity
        if (activity == null) {
            Toast.makeText(context, "Context must be an AppCompatActivity to export Excel.", Toast.LENGTH_LONG).show()
            return
        }

        activity.lifecycleScope.launch(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                val workbook = XSSFWorkbook() // Create a new Excel workbook (.xlsx format)

                // Create and populate the single "Tasks" sheet
                createTaskSheet(workbook, taskList, users)

                // 1. Write to a temporary file first
                tempFile = File(context.cacheDir, "temp_excel_export_${Utils().formatDate(System.currentTimeMillis())}.xlsx")
                FileOutputStream(tempFile).use { fos ->
                    workbook.write(fos)
                    fos.flush() // Ensure all data is written to the temp file
                }
                workbook.close() // Close the workbook after writing to temp file

                // 2. Open output stream for the user-selected URI
                val outputStream = context.contentResolver.openOutputStream(uri)

                if (outputStream != null) {
                    // 3. Copy content from temporary file to the user-selected URI
                    FileInputStream(tempFile).use { fis ->
                        outputStream.use { os ->
                            fis.copyTo(os) // Efficiently copy bytes
                            os.flush() // Ensure data is flushed to the final destination
                        }
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Excel exported successfully!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to open output stream for selected location. File might not be created.", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error exporting Excel: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                // 4. Delete the temporary file
                tempFile?.delete()
            }
        }
    }

    /**
     * Creates and populates the "Tasks" sheet in the given workbook.
     *
     * @param workbook The Excel workbook.
     * @param taskList The list of TaskModel objects.
     * @param users The list of UserModel objects for looking up assign person names.
     */
    private fun createTaskSheet(workbook: XSSFWorkbook, taskList: List<TaskModel>, users: List<UserModel>) {
        val sheet: Sheet = workbook.createSheet("Tasks")

        // Define header row for tasks as per user's request
        val taskHeaders = listOf(
            "Index",
            "Title",
            "Description",
            "Assign Person",
            "Assign Date",
            "Completion Date",
            "Status",
            "Remark",
            "User Remark" // Corrected index for User Remark
        )

        // Create the header row
        val headerRow: Row = sheet.createRow(0)
        taskHeaders.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            // Apply bold style to header cells (optional)
            val headerStyle = workbook.createCellStyle()
            val font = workbook.createFont()
            font.bold = true
            headerStyle.setFont(font)
            cell.cellStyle = headerStyle
        }

        // Populate data rows for tasks
        taskList.forEachIndexed { rowIndex, task ->
            val row = sheet.createRow(rowIndex + 1) // +1 because row 0 is for headers

            // Find the assigned person's name
            val assignPersonName = users.find { it.uid == task.assignPersonId }?.name ?: "Unknown"

            // Populate cells based on the new header structure
            row.createCell(0).setCellValue((rowIndex + 1).toDouble()) // Index (starting from 1)
            row.createCell(1).setCellValue(task.title)
            row.createCell(2).setCellValue(task.taskDetail) // "Description" maps to taskDetail
            row.createCell(3).setCellValue(assignPersonName)
            row.createCell(4).setCellValue(Utils().formatDate(task.assignDate))
            row.createCell(5).setCellValue(task.completionDate?.let { Utils().formatDate(it) } ?: "")
            row.createCell(6).setCellValue(task.status.name)
            row.createCell(7).setCellValue(task.remark ?: "")
            row.createCell(8).setCellValue(task.userRemark ?: "") // Corrected index for User Remark
        }

        // Removed autoSizeColumn as per user request to prevent crashes
    }

    /**
     * Formats a timestamp (Long) into a human-readable date and time string.
     * @param timestamp The timestamp in milliseconds.
     * @return Formatted date string, or empty string if an error occurs.
     */
   
}








/*

class ExcelExporter(private val context: Context) {

    open fun exportExcel(taskList: List<TaskModel>, users: List<UserModel>) {
        if (!isExternalStorageWritable()) {
            Toast.makeText(context, "External storage not writable.", Toast.LENGTH_LONG).show()
            return
        }

        val workbook = XSSFWorkbook() // For .xlsx format

        // Create Task Sheet
        createTaskSheet(workbook, taskList)

        // Create User Sheet
        createUserSheet(workbook, users)

        // Save the workbook
        val fileName = "ExportedData_${getCurrentDateTime()}.xlsx"
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "YourAppExports")
        if (!directory.exists()) {
            directory.mkdirs() // Create directory if it doesn't exist
        }
        val file = File(directory, fileName)

        try {
            val fileOut = FileOutputStream(file)
            workbook.write(fileOut)
            fileOut.close()
            workbook.close()
            Toast.makeText(context, "Excel exported successfully to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createTaskSheet(workbook: XSSFWorkbook, taskList: List<TaskModel>) {
        val sheet: Sheet = workbook.createSheet("Tasks")

        // Create header row
        val headerRow: Row = sheet.createRow(0)
        val taskHeaders = listOf(
            "Title", "Task Detail", "Assign Person ID", "Assign Date",
            "Completion Date", "Remark", "User Remark", "Status", "Created By"
        )
        taskHeaders.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        // Populate data rows
        taskList.forEachIndexed { rowIndex, task ->
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0).setCellValue(task.title)
            row.createCell(1).setCellValue(task.taskDetail)
            row.createCell(2).setCellValue(task.assignPersonId)
            row.createCell(3).setCellValue(Utils().Utils().Utils().Utils().Utils().formatDate(task.assignDate))
            row.createCell(4).setCellValue(task.completionDate?.let { Utils().Utils().Utils().Utils().Utils().formatDate(it) } ?: "")
            row.createCell(5).setCellValue(task.remark ?: "")
            row.createCell(6).setCellValue(task.userRemark ?: "")
            row.createCell(7).setCellValue(task.status.name)
            row.createCell(8).setCellValue(task.createdBy)
        }

        // Auto-size columns for better readability
//        taskHeaders.forEachIndexed { index, _ ->
//            sheet.autoSizeColumn(index)
//        }
    }

    private fun createUserSheet(workbook: XSSFWorkbook, users: List<UserModel>) {
        val sheet: Sheet = workbook.createSheet("Users")

        // Create header row
        val headerRow: Row = sheet.createRow(0)
        val userHeaders = listOf("UID", "Name", "Role", "Email")
        userHeaders.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        // Populate data rows
        users.forEachIndexed { rowIndex, user ->
            val row = sheet.createRow(rowIndex + 1)
            row.createCell(0).setCellValue(user.uid)
            row.createCell(1).setCellValue(user.name)
            row.createCell(2).setCellValue(user.role)
            row.createCell(3).setCellValue(user.email)
        }

        // Auto-size columns
//        userHeaders.forEachIndexed { index, _ ->
//            sheet.autoSizeColumn(index)
//        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }



    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }
}*/
