package com.example.taskmanagement

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanagement.databinding.ActivityNewTaskBinding
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.utils.Utils
import com.example.taskmanagement.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NewTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewTaskBinding
    private lateinit var viewModel: TaskViewModel
    private var selectedUserId: Int? = null
    private var taskId: Int? = null
    private var selectedStatus: Status = Status.IN_PROGRESS
    private var selectedDate: Long = System.currentTimeMillis()
    private var editingTask: Task? = null
    private var completeDate: Long? = 0L
    private var assignDateInMillis: Long = 0L
    private var completionDateInMillis: Long = 0L

    /* override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)

         setContentView(binding.root)

         viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
         binding = DataBindingUtil.setContentView(this, com.example.taskmanagement.R.layout.activity_new_task)
         setupUserSpinner()
         setupDatePicker()
         setupSaveButton()
     }*/

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.taskmanagement.R.layout.activity_new_task)

        // Initialize ViewModel and RecyclerView
        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        binding = DataBindingUtil.setContentView(
            this,
            com.example.taskmanagement.R.layout.activity_new_task
        )
        setupUserSpinner()
        setupDatePicker()
        setupSaveButton()
    }*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.taskmanagement.R.layout.activity_new_task)

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        binding = DataBindingUtil.setContentView(
            this,
            com.example.taskmanagement.R.layout.activity_new_task
        )
        binding.viewModel = viewModel
        setupSpinner()
        setupDatePicker()
        setupSaveButton()

         taskId = intent.getIntExtra("task_id", -1)
        if (taskId != -1) {
            viewModel.editMode.set(true)
            Log.d("editMode", "EditMode : ${viewModel.editMode.get()}")
            viewModel.getTaskById(taskId!!).observe(this) { task ->
                task?.let {
                    populateTaskDetails(it)
                }
            }
        }
    }

    /*  private fun populateTaskDetails(task: Task) {
          binding.etTitle.setText(task.title)
          binding.etDescription.setText(task.taskDetail)

          selectedDate = task.assignDate
          binding.tvPickDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate))

          // Assign user in spinner when loaded
          viewModel.allUsers.observe(this) { users ->
              val index = users.indexOfFirst { it.id == task.assignPersonId }
              if (index != -1) {
                  binding.spinnerUser.setSelection(index + 1) // +1 because of "Select User"
              }
          }

          selectedUserId = task.assignPersonId
      }
  */

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData() {
        binding.spinnerUser.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedUserId = viewModel.allUsers.value?.get(position)?.id
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedUserId = null
                }
            }

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
    }

    private fun populateTaskDetails(task: Task) {

        binding.etTitle.setText(task.title)
        binding.etDescription.setText(task.taskDetail)

        selectedDate = task.assignDate
//        binding.tvAssignDate.text = formatDate(task.assignDate)
        binding.tvAssignDate.setText(formatDate(task.assignDate))
        // Assign user in spinner
        viewModel.allUsers.observe(this) { users ->
            val index = users.indexOfFirst { it.id == task.assignPersonId }
            if (index != -1) {
                binding.spinnerUser.setSelection(index) // +1 because of "Select User"
            }
        }

        selectedUserId = task.assignPersonId

        // Remark
        binding.etRemark.setText(task.remark)

        // Completion Date
        completeDate = task.completionDate ?: 0L
        if (completeDate != 0L) {
            binding.tvPickCompleteDate.setText( formatDate(completeDate!!))
        }
        Log.d("spinnerStatus", " spinnerStatus: ${getStatusIndex(task.status)}")
        // Status (assuming a spinner or a radio group)
        binding.spinnerStatus.setSelection(getStatusIndex(task.status))

        // Store for update
        editingTask = task
    }


    private var userIdList: List<Int?> = listOf()

    private fun setupSpinner() {
        val statusList = viewModel.allStatus
        val adapterStatus = ArrayAdapter(this, R.layout.simple_spinner_item, statusList)
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapterStatus


        //User Spinners
        viewModel.allUsers.observe(this) { users ->
//            val userNames = listOf("Select User") + users.map { it.name }
            val userNames = users.map { it.name }
            userIdList = users.map { it.id }

            val adapter = ArrayAdapter(
                this,
                R.layout.simple_spinner_item,
                userNames
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerUser.adapter = adapter

            /* binding.spinnerUser.onItemSelectedListener =
                 object : AdapterView.OnItemSelectedListener {
                     override fun onItemSelected(
                         parent: AdapterView<*>,
                         view: View?,
                         position: Int,
                         id: Long
                     ) {
                         selectedUserId = users[position].id
                     }

                     override fun onNothingSelected(parent: AdapterView<*>?) {
                         selectedUserId = null
                     }
                 }*/


            //


        }
    }

    private fun setupDatePicker() {
        /*    binding.tvPickDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                val datePicker = DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        selectedDate = calendar.timeInMillis
                        binding.tvPickDate.text =
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                                Date(selectedDate)
                            )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            }*/


        binding.tvAssignDate.setOnClickListener {
            Utils().openDatePicker(this) { selectedMillis, formattedDate ->
                completionDateInMillis = selectedMillis
                binding.tvAssignDate.setText(formattedDate)
                selectedDate = selectedMillis
            }
        }

        binding.tvPickCompleteDate.setOnClickListener {
            Utils().openDatePicker(
                this,
                minDate = selectedDate
            ) { selectedMillis, formattedDate ->
                completionDateInMillis = selectedMillis
                binding.tvPickCompleteDate.setText(formattedDate)
                completeDate = selectedMillis
            }
        }

    }

 /*   private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()
            val remarks = binding.etRemark.text.toString().trim()


            if (title.isEmpty()) {
                Toast.makeText(this, "Add Title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (desc.isEmpty()) {
                return@setOnClickListener
            } else if (selectedUserId == null || selectedUserId == 0) {
                return@setOnClickListener
            }

            if (viewModel.editMode.get()) {
                val editTask = Task(
                    id = taskId!!,
                    title = title,
                    taskDetail = desc,
                    assignPersonId = selectedUserId!!,
                    assignDate = selectedDate,
                    completionDate = if(completeDate?.equals(null) == true) null else completeDate,
                    remark = if(TextUtils.isEmpty(remarks)) null else remarks,
                    status = selectedStatus
                )

                viewModel.updateTask(editTask)
            } else {
                val newTask = Task(
                    title = title,
                    taskDetail = desc,
                    assignPersonId = selectedUserId!!,
                    assignDate = selectedDate,
                    completionDate = null,
                    remark = null,
                    status = selectedStatus
                )

                viewModel.addTask(newTask)
            }

            finish()
        }
    }*/


    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val remark = binding.etRemark.text.toString().trim()

            if (TextUtils.isEmpty(title) || selectedUserId == null) {
                Toast.makeText(this, "Title and user must be selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val completionDate: Long? =
                if (binding.tvPickCompleteDate.text.isNullOrEmpty()) null else completeDate

            val task = Task(
                id = editingTask?.id ?: 0,
                title = title,
                taskDetail = description,
                assignDate = selectedDate,
                assignPersonId = selectedUserId!!,
                remark = if(TextUtils.isEmpty(remark)) null else remark,
                completionDate = completionDate,
                status = selectedStatus
            )

            if (viewModel.editMode.get()) {
                viewModel.updateTask(task)
            } else {
                viewModel.addTask(task)
            }

            finish()
        }
    }


    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
    }

    private fun getStatusIndex(status: Status): Int {

        return when (status) {

            Status.IN_PROGRESS -> 0
            Status.COMPLETED -> 1
        }

    }


}
