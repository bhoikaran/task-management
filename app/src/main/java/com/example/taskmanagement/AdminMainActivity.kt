package com.example.taskmanagement


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanagement.adapter.TaskAdapter
import com.example.taskmanagement.databinding.ActivityMainBinding
import com.example.taskmanagement.interactors.GeneralItemListener
import com.example.taskmanagement.interactors.GeneralListener
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.TaskModel
import com.example.taskmanagement.model.UserModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import com.example.taskmanagement.utils.ExportToExcel
import com.example.taskmanagement.utils.Utils
import com.example.taskmanagement.viewmodel.AdminViewModel
import com.example.taskmanagement.viewmodel.TaskViewModel
import com.example.taskmanagement.viewmodel.TaskViewModelFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class AdminMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: AdminViewModel
    private lateinit var adapter: TaskAdapter

    private var userIds: List<String?> = listOf()
    private var workbook: XSSFWorkbook? = null
    private lateinit var pendingIntent: Intent

    private val exportLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    workbook?.let { ExportToExcel().saveWorkbookToUri(this, uri, it) }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val repo = FirestoreTaskRepository()
        viewModel = ViewModelProvider(this, TaskViewModelFactory(repo))
            .get(AdminViewModel::class.java)

        binding.viewModel = viewModel
        binding.generalListener = generalListener

        setupRecycler()
        setupFilters()
        observeData()
    }

    private fun setupRecycler() {
        adapter = TaskAdapter(emptyList(), itemListener)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFilters() {
        viewModel.allUsers.observe(this) { users ->
            val names = listOf("All") + users.map { it.name }
            userIds = listOf(null) + users.map { it.uid }

            binding.spinnerUser.adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, names
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        val statusNames = listOf("All") + Status.values().map { it.name }
        binding.spinnerStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, statusNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun observeData() {
        viewModel.filteredTasks.observe(this) { tasks ->
            adapter.submitList(tasks)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.applyFilters(null, null)
    }

    private val generalListener = GeneralListener { view ->
        when (view?.id) {
            R.id.fabAddTask -> startActivity(
                Intent(this, NewTaskActivity::class.java)
            )
            R.id.btnApplyFilters -> {
                val uPos = binding.spinnerUser.selectedItemPosition
                val userId = userIds.getOrNull(uPos)
                val sPos = binding.spinnerStatus.selectedItemPosition
                val status = if (sPos == 0) null else Status.values()[sPos - 1].name
                viewModel.applyFilters(userId, status)
            }
//            R.id.exportToExcel -> exportTasks()
        }
    }

   /* private fun exportTasks() {
        val tasks = viewModel.filteredTasks.value ?: emptyList<TaskModel>()
        val users = viewModel.allUsers.value ?: emptyList<UserModel>()
        if (tasks.isEmpty() || users.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_LONG).show()
            return
        }
        workbook = ExportToExcel().createExcelWorkbook(tasks, users)
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type =
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(
                Intent.EXTRA_TITLE,
                "Tasks_${Utils().formatDate(System.currentTimeMillis())}.xlsx"
            )
        }
        pendingIntent = intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            exportLauncher.launch(intent)
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE
            )
        }
    }
*/
    private val itemListener = GeneralItemListener { view, _, item ->
        if (item is TaskModel) {
            when (view.id) {
                R.id.imageview_delete -> viewModel.deleteTask(item)
                R.id.imageview_edit -> Intent(this, NewTaskActivity::class.java).also {
                    it.putExtra("task_id", item.id)
                    startActivity(it)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            exportLauncher.launch(pendingIntent)
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_STORAGE = 200
    }
}

