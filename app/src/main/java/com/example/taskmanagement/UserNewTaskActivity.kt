package com.example.taskmanagement

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanagement.databinding.ActivityAdminNewTaskBinding
import com.example.taskmanagement.databinding.ActivityUserNewTaskBinding
import com.example.taskmanagement.interactors.GeneralListener
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.TaskModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import com.example.taskmanagement.utils.Utils
import com.example.taskmanagement.utils.utility.UtilPreference
import com.example.taskmanagement.viewmodel.AdminNewTaskViewModel
import com.example.taskmanagement.viewmodel.NewTaskViewModelFactory
import com.example.taskmanagement.viewmodel.UserNewTaskViewModel
import com.example.taskmanagement.viewmodel.UserTaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UserNewTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserNewTaskBinding
    private lateinit var viewModel: UserNewTaskViewModel
    private lateinit var mSharePreference: UtilPreference
    private var taskId: String? = null
    private var isEdit = false

    private var selectedAssignDate = System.currentTimeMillis()
    private var selectedCompleteDate: Long? = null
    private var selectedUserId: String? = null
    private var selectedStatus = Status.IN_PROGRESS


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_new_task)
        mSharePreference = UtilPreference(this)
        // setup VM
        val repo = FirestoreTaskRepository()
        viewModel = ViewModelProvider(
            this,
            UserTaskViewModelFactory(repo)
        )[UserNewTaskViewModel::class.java]
        binding.viewModel = viewModel
        binding.generalListener = generalListener
        binding.lifecycleOwner = this


        setupDatePickers()

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



    private fun populateForEdit(task: TaskModel) {

        binding.etTitle.setText(task.title)
        binding.etDescription.setText(task.taskDetail)

        selectedAssignDate = task.assignDate
//        binding.tvAssignDate.text = formatDate(task.assignDate)
        binding.tvAssignDate.setText(formatDate(task.assignDate))
        // Assign user in spinner

        selectedUserId = task.assignPersonId

        // Remark
        binding.etRemark.setText(task.remark)

        // Completion Date
        selectedCompleteDate = task.completionDate ?: 0L
        if (selectedCompleteDate != 0L) {
            binding.tvPickCompleteDate.setText(formatDate(selectedCompleteDate!!))
        }

        val pos = task.status.ordinal

        selectedStatus = task.status
        // user


        // Store for update
    }


    private fun formatDate(ms: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ms))



    private val generalListener: GeneralListener = GeneralListener { view ->
        when (view?.id) {
            R.id.btnSave -> {
                val remark = binding.etRemark.text.toString().trim().takeIf { it.isNotEmpty() }

                if (remark?.isEmpty() == true || selectedCompleteDate == null) {
                    Toast.makeText(this, "Remark and Complete Date are required", Toast.LENGTH_LONG).show()
                    return@GeneralListener
                }

                val model = TaskModel(
                    id = if (isEdit) taskId else null,
                    completionDate = selectedCompleteDate,
                    userRemark = remark,
                    status = Status.MARKED_DONE,
                )
                viewModel.updateTask(model)
                finish()
            }

        }
    }

}