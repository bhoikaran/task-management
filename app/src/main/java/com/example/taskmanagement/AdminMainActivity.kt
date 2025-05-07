package com.example.taskmanagement


import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanagement.adapter.TaskAdapter
import com.example.taskmanagement.databinding.ActivityAdminMainBinding
import com.example.taskmanagement.interactors.GeneralItemListener
import com.example.taskmanagement.interactors.GeneralListener
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.TaskModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import com.example.taskmanagement.utils.ExportToExcel
import com.example.taskmanagement.utils.utility.UtilPreference
import com.example.taskmanagement.viewmodel.AdminViewModel
import com.example.taskmanagement.viewmodel.TaskViewModelFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class AdminMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMainBinding
    private lateinit var viewModel: AdminViewModel
    private lateinit var adapterTask: TaskAdapter
    private lateinit var mSharePreference: UtilPreference
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_main)
        mSharePreference = UtilPreference(this)
        val repo = FirestoreTaskRepository()
        viewModel = ViewModelProvider(this, TaskViewModelFactory(repo))[AdminViewModel::class.java]

        binding.viewModel = viewModel
        binding.generalListener = generalListener

        setupRecycler()
        setupFilters()
        observeData()
    }


    private fun setupRecycler() {
        adapterTask = TaskAdapter(emptyList(), itemListener)
        adapterTask.isAdmin = true
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapterTask
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
//            adapter.submitList(tasks)
            Log.d("filteredTasks", "filteredTasks : $tasks")
            adapterTask.tasks = tasks
        }
        viewModel.allUsers.observe(this) { users ->
            Log.d("allUsers", "allUsers : $users")
            adapterTask.users = users
        }
    }

    override fun onResume() {
        super.onResume()
        if (TextUtils.isEmpty(mSharePreference.getString(R.string.prefUserId))){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        viewModel.applyFilters(null, null)
    }

    private val generalListener = GeneralListener { view ->
        when (view?.id) {
            R.id.fabAddTask -> startActivity(
                Intent(this, AdminNewTaskActivity::class.java)
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
                R.id.imageview_edit -> Intent(this, AdminNewTaskActivity::class.java).also {
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

