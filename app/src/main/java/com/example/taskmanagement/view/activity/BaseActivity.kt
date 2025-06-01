package com.example.taskmanagement.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskmanagement.view.activity.LoginActivity
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.UserModel
import com.example.taskmanagement.utils.ExcelExporter
import com.example.taskmanagement.utils.ExportToExcel
import com.example.taskmanagement.utils.Utils
import com.example.taskmanagement.view.fragments.AdminHomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apache.poi.xssf.usermodel.XSSFWorkbook

open class BaseActivity : AppCompatActivity() {


    internal lateinit var auth: FirebaseAuth
    internal lateinit var firestore: FirebaseFirestore
    private var workbook: XSSFWorkbook? = null
    private lateinit var pendingIntent: Intent
   lateinit var taskList: List<TaskModel>
   lateinit var  users: List<UserModel>
    private var workbookToExport: XSSFWorkbook? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_base)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }


    private val exportLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    workbook?.let { ExportToExcel().saveWorkbookToUri(this, uri, it) }
                }
            }
        }
    /*  open fun exportExcel(taskList: List<TaskModel>, users: List<UserModel>) {


          if (taskList.isNotEmpty() && users.isNotEmpty()) {
              workbookToExport = ExportToExcel().createExcelWorkbook(
                  taskList,
                  users
              )
              val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                  addCategory(Intent.CATEGORY_OPENABLE)
                  type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                  putExtra(
                      Intent.EXTRA_TITLE,
                      "Tasks_${Utils().formatDate(System.currentTimeMillis())}.xlsx"
                  )
              }

              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                  exportLauncher.launch(intent)
              } else {
                  this.intent = intent
                  requestPermissions(
                      arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                      AdminHomeFragment.Companion.REQUEST_CODE_STORAGE_PERMISSION
                  )
              }

          } else {

              Toast.makeText(this, "No data to export", Toast.LENGTH_LONG).show()
          }





      }
  */

    /*open fun exportExcel(taskList: List<TaskModel>, users: List<UserModel>) {
        if (taskList.isNotEmpty() && users.isNotEmpty()) {
            try {
                workbookToExport = ExportToExcel().createExcelWorkbook(taskList, users)
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(
                        Intent.EXTRA_TITLE,
                        "Tasks_${Utils().formatDate(System.currentTimeMillis())}.xlsx"
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    (exportLauncher as androidx.activity.result.ActivityResultLauncher<Intent>).launch(intent)
                } else {
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE_STORAGE_PERMISSION
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error creating Excel: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("ExcelExport", "Error creating workbook", e)
            }
        } else {
            Toast.makeText(this, "No data to export", Toast.LENGTH_LONG).show()
        }
    }*/


    /* open fun exportExcel(taskList: List<TaskModel>, users: List<UserModel>) {
         if (taskList.isNotEmpty() && users.isNotEmpty()) {
             try {
 //                workbookToExport = ExportToExcel().createExcelWorkbook(taskList, users)



                 workbookToExport = ExportToExcel().createTestWorkbook()


                 Log.d("Tag","workbookToExport : $workbookToExport")
                 val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                     addCategory(Intent.CATEGORY_OPENABLE)
                     type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                     putExtra(
                         Intent.EXTRA_TITLE,
                         "Tasks_${Utils().formatDate(System.currentTimeMillis())}.xlsx"
                     )
                 }

                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                     (exportLauncher as androidx.activity.result.ActivityResultLauncher<Intent>).launch(
                         intent
                     )
                 } else {
                     requestPermissions(
                         arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                         REQUEST_CODE_STORAGE_PERMISSION
                     )
                 }
             } catch (e: Exception) {
                 Toast.makeText(
                     this,
                     "Error creating Excel: ${e.message}",
                     Toast.LENGTH_LONG
                 ).show()
                 Log.e("ExcelExport", "Error creating workbook", e)
             }
         } else {
             Toast.makeText(this, "No data to export", Toast.LENGTH_LONG).show()
         }
     }
 */


    open fun initiateExcelExport(taskList: List<TaskModel>, users: List<UserModel>) {

        if (checkStoragePermission()) {
//            ExcelExporter(this).exportExcel(taskList, users)
            if (checkStoragePermission()) {
                // Launch the system file picker to let the user choose where to save
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // MIME type for .xlsx
                    putExtra(Intent.EXTRA_TITLE, "Tasks_Export_${Utils().formatDate(System.currentTimeMillis())}.xlsx") // Suggested file name
                }
                createDocumentLauncher.launch(intent)
            }
        }
    }
    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // User selected a URI, now export the Excel file to it
               ExcelExporter(this).exportTasksToExcel(uri, taskList, users)
            } ?: run {
                Toast.makeText(this, "File selection cancelled or failed.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "File selection cancelled.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkStoragePermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (exportLauncher as androidx.activity.result.ActivityResultLauncher<Intent>).launch(
                intent
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
                return false
            } else {
                return true
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now call your export function
                // exportExcel(taskList, userList) // Call your function here
            } else {
                // Permission denied, inform the user
                Toast.makeText(
                    this,
                    "Storage permission denied. Cannot export Excel.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    /* override fun onRequestPermissionsResult(
         requestCode: Int,
         permissions: Array<out String>,
         grantResults: IntArray
     ) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
             exportLauncher.launch(intent)
         } else {
             Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show()
         }
     }*/


    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION = 200

    }


}