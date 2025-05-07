package com.example.taskmanagement


import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanagement.databinding.ActivityAdminNewTaskBinding
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.TaskModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import com.example.taskmanagement.utils.Utils
import com.example.taskmanagement.utils.utility.UtilPreference
import com.example.taskmanagement.viewmodel.AdminNewTaskViewModel
import com.example.taskmanagement.viewmodel.NewTaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminNewTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminNewTaskBinding
    private lateinit var viewModel: AdminNewTaskViewModel
    private lateinit var mSharePreference: UtilPreference
    private var taskId: String? = null
    private var isEdit = false

    private var selectedAssignDate = System.currentTimeMillis()
    private var selectedCompleteDate: Long? = null
    private var selectedUserId: String? = null
    private var selectedStatus = Status.IN_PROGRESS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_new_task)
        mSharePreference = UtilPreference(this)
        // setup VM
        val repo = FirestoreTaskRepository()
        viewModel = ViewModelProvider(
            this,
            NewTaskViewModelFactory(repo)
        )[AdminNewTaskViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupSpinners()
        setupDatePickers()
        setupSave()

        // edit mode?
        taskId = intent.getStringExtra("task_id")
        if (!taskId.isNullOrEmpty()) {
            isEdit = true
            viewModel.editMode.set(true)
            viewModel.getTaskById(taskId!!).observe(this) { task ->
                task?.let { populateForEdit(it) }
            }
        }
    }

    private fun setupSpinners() {
        // status spinner
        val statuses = Status.entries.map { it.name }
        binding.spinnerStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, statuses
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedStatus = viewModel.allStatus[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        // user spinner
        viewModel.allUsers.observe(this) { users ->
            val names = users.map { it.name }
            val ids = users.map { it.uid }
            binding.spinnerUser.adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, names
            ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }



            binding.spinnerUser.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedUserId = viewModel.allUsers.value?.get(position)?.uid
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedUserId = null
                    }
                }
        }
    }

    private fun setupDatePickers() {
        binding.tvAssignDate.setOnClickListener {
            Utils().openDatePicker(this) { millis, text ->
                selectedAssignDate = millis
                binding.tvAssignDate.setText(text)
            }
        }
        binding.tvPickCompleteDate.setOnClickListener {
            Utils().openDatePicker(this, minDate = selectedAssignDate) { millis, text ->
                selectedCompleteDate = millis
                binding.tvPickCompleteDate.setText(text)
            }
        }
    }

    private fun setupSave() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            if (title.isEmpty() || selectedUserId == null) {
                Toast.makeText(this, "Title and user required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val detail = binding.etDescription.text.toString().trim()
            val remark = binding.etRemark.text.toString().trim().takeIf { it.isNotEmpty() }

            val model = TaskModel(
                id = if (isEdit) taskId else null,
                title = title,
                taskDetail = detail,
                assignPersonId = selectedUserId!!,
                assignDate = selectedAssignDate,
                completionDate = selectedCompleteDate,
                remark = remark,
                status = selectedStatus,
                createdBy = mSharePreference.getString(R.string.prefUserId)

            )
            viewModel.addTask(model)
            finish()
        }
    }

    private fun populateForEdit(task: TaskModel) {

        binding.etTitle.setText(task.title)
        binding.etDescription.setText(task.taskDetail)

        selectedAssignDate = task.assignDate
//        binding.tvAssignDate.text = formatDate(task.assignDate)
        binding.tvAssignDate.setText(formatDate(task.assignDate))
        // Assign user in spinner
        viewModel.allUsers.observe(this) { users ->
            val index = users.indexOfFirst { it.uid == task.assignPersonId }
            if (index != -1) {
                binding.spinnerUser.setSelection(index) // +1 because of "Select User"
            }
        }

        selectedUserId = task.assignPersonId

        // Remark
        binding.etRemark.setText(task.remark)

        // Completion Date
        selectedCompleteDate = task.completionDate ?: 0L
        if (selectedCompleteDate != 0L) {
            binding.tvPickCompleteDate.setText(formatDate(selectedCompleteDate!!))
        }

        val pos = task.status.ordinal
        binding.spinnerStatus.setSelection(pos)
        selectedStatus = task.status
        // user
        viewModel.allUsers.value?.let { users ->
            val idx = users.indexOfFirst { it.uid == task.assignPersonId }
            if (idx >= 0) binding.spinnerUser.setSelection(idx)
            selectedUserId = task.assignPersonId
        }


        // Store for update
    }


    private fun formatDate(ms: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ms))


}
