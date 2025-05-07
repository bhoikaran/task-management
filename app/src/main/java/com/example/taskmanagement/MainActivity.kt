package com.example.taskmanagement

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanagement.adapter.TaskAdapter
import com.example.taskmanagement.databinding.ActivityMainBinding
import com.example.taskmanagement.interactors.GeneralItemListener
import com.example.taskmanagement.interactors.GeneralListener
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.utils.ExportToExcel
import com.example.taskmanagement.utils.Utils
import com.example.taskmanagement.viewmodel.TaskViewModel
import org.apache.poi.xssf.usermodel.XSSFWorkbook


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>
    private var workbookToExport: XSSFWorkbook? = null

    private lateinit var intent: Intent
    private var userIdList: List<Int?> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        exportLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        workbookToExport?.let {
                            ExportToExcel().saveWorkbookToUri(this, uri, it)
                        }
                    }
                }
            }


        // Initialize ViewModel and RecyclerView
        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.viewModel = viewModel
        mBinding.generalListener = generalListener
        setupRecyclerView()
        setupFilters()

    }


    override fun onResume() {
        super.onResume()
        viewModel.loadAllTasks()

        observeData()
    }


    private fun setupRecyclerView() {
        adapter = TaskAdapter(emptyList(), generalItemListener)
        mBinding.recyclerView.adapter = adapter
        mBinding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupFilters() {
        // Observe users and update Spinner
        viewModel.allUsers.observe(this) { users ->
            val userNames = listOf("All") + users.map { it.name }
            userIdList = listOf(null) + users.map { it.id }

            val userAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                userNames
            )
            userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            mBinding.spinnerUser.adapter = userAdapter
        }

        // Status Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.status_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            mBinding.spinnerStatus.adapter = adapter
        }


    }


    private fun observeData() {
        viewModel.filteredTasks.observe(this) { tasks ->
//            adapter.tasks = tasks
        }
        viewModel.allUsers.observe(this) { users ->
//            adapter.users = users
        }
    }


    private val generalListener: GeneralListener = GeneralListener { view ->
        when (view?.id) {
            R.id.fabAddTask -> {
//                mActivity?.navigateToScheduleSession()
                startActivity(Intent(this, NewTaskActivity::class.java))
            }

            R.id.btnApplyFilters -> {
//                mActivity?.navigateToScheduleSession()
                val selectedUserId = userIdList.getOrNull(mBinding.spinnerUser.selectedItemPosition)
                val selectedStatus = when (mBinding.spinnerStatus.selectedItemPosition) {

                    1 -> Status.IN_PROGRESS.toString()
                    2 -> Status.VERIFIED_DONE.toString()
                    else -> null // "All"
                }

                viewModel.applyFilters(selectedUserId, selectedStatus)
            }

            R.id.exportToExcel -> {
//                ExportToExcel().exportTasksToExcel(this,viewModel.filteredTasks.value!!,viewModel.allUsers.value!!)

                val tasks = viewModel.filteredTasks.value
                val users = viewModel.allUsers.value

                if (!tasks.isNullOrEmpty() && !users.isNullOrEmpty()) {
                    workbookToExport = ExportToExcel().createExcelWorkbook(
                        viewModel.filteredTasks.value!!,
                        viewModel.allUsers.value!!
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
                            REQUEST_CODE_STORAGE_PERMISSION
                        )
                    }

                } else {

                    Toast.makeText(this, "No data to export", Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    private val generalItemListener: GeneralItemListener =
        GeneralItemListener { view, position, item ->
            if (item is Task) {

                if (view.id == R.id.imageview_delete) {
                    Utils().showMessageYesNo(
                        this, getString(R.string.text_delete_item)
                    ) { dialogInterface, i ->
                        viewModel.deleteTask(item)
                    }
                } else if (view.id == R.id.imageview_edit) {
                    val intent = Intent(this@MainActivity, NewTaskActivity::class.java)
                    intent.putExtra("task_id", item.id)
                    this@MainActivity.startActivity(intent)
                }
            }

        }


    override fun onRequestPermissionsResult(
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
    }


    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION = 200

    }
}