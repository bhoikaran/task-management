package com.example.taskmanagement

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanagement.adapter.TaskAdapter
import com.example.taskmanagement.databinding.ActivityUserMainBinding
import com.example.taskmanagement.interactors.GeneralItemListener
import com.example.taskmanagement.interactors.GeneralListener
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.TaskModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import com.example.taskmanagement.utils.ExportToExcel
import com.example.taskmanagement.utils.utility.UtilPreference
import com.example.taskmanagement.viewmodel.UserViewModel
import com.example.taskmanagement.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import org.apache.poi.xssf.usermodel.XSSFWorkbook


class UserMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserMainBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var adapter: TaskAdapter
    private lateinit var mSharePreference: UtilPreference
    private var userIds: List<String?> = listOf()
    private var workbook: XSSFWorkbook? = null
    private lateinit var pendingIntent: Intent
    val currentUser = FirebaseAuth.getInstance().currentUser

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_main)
        mSharePreference = UtilPreference(this)
        val repo = FirestoreTaskRepository()
        viewModel = ViewModelProvider(this, UserViewModelFactory(repo))[UserViewModel::class.java]

        binding.viewModel = viewModel
        binding.generalListener = generalListener
        setupRecycler()
        setupFilters()
        observeData()
    }

    private fun setupRecycler() {
        adapter = TaskAdapter(emptyList(), itemListener)
        adapter.isAdmin = false
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFilters() {

        val statusNames = listOf("All") + Status.entries.map { it.name }
        binding.spinnerStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, statusNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedStatus = if (position == 0) null else Status.entries[position - 1].name


                if (currentUser == null) {
                    // redirect to login
                    return
                }
                val uid = currentUser.uid
                viewModel.applyFilters(uid, selectedStatus)


            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun observeData() {
        viewModel.filteredTasks.observe(this) { tasks ->
//            adapter.submitList(tasks)
            Log.d("filteredTasks", "filteredTasks : $tasks")
            adapter.tasks = tasks
        }
    }

    override fun onResume() {
        super.onResume()
        if (TextUtils.isEmpty(mSharePreference.getString(R.string.prefUserId))) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        if (currentUser == null) {
            // redirect to login
            return
        }
        val uid = currentUser.uid
        viewModel.applyFilters(uid, null)

    }

    private val generalListener = GeneralListener { view ->
        when (view?.id) {

//            R.id.exportToExcel -> exportTasks()
        }
    }

    private val itemListener = GeneralItemListener { view, _, item ->
        if (item is TaskModel) {
            when (view.id) {
                R.id.imageview_delete -> viewModel.deleteTask(item)
                R.id.imageview_edit -> Intent(this, UserNewTaskActivity::class.java).also {
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
