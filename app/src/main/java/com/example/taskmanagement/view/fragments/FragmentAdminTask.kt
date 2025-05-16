package com.example.taskmanagement.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralListener
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.viewmodel.AdminTaskViewModel
import com.example.taskmanagement.databinding.FragmentAdminTaskBinding
import com.example.taskmanagement.utils.Utils
import com.example.taskmanagement.utils.utility.UtilPreference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FragmentAdminTask : FragmentBase() {
    private lateinit var mBinding: FragmentAdminTaskBinding
    private lateinit var mViewModel: AdminTaskViewModel
    private var taskId: String? = null
    private var isEdit = false
    private var taskModel: TaskModel = TaskModel()
    private var selectedAssignDate = System.currentTimeMillis()
    private var selectedCompleteDate: Long? = null
    private var selectedUserId: String? = null
    private var selectedStatus = Status.IN_PROGRESS
    private lateinit var titleAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_admin_task,
            container,
            false
        )
        mViewModel = AdminTaskViewModel(requireActivity().applicationContext as MyApplication)
        mBinding.viewModel = mViewModel
        mBinding.generalListener = generalListener
        mBinding.lifecycleOwner = viewLifecycleOwner

        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponents()
        observeEvents()


    }

    override fun onResume() {
        super.onResume()
    }

    private fun initComponents() {

        taskId = arguments?.getString("task_id")

        if (!taskId.isNullOrEmpty()) {
            isEdit = true

            mViewModel.editMode.set(true)
            mViewModel.getTaskById(taskId!!).observe(getViewLifecycleOwner()) { task ->
                if (task != null) {
                    taskModel = task
                }

                task?.let { populateForEdit(it) }
            }
        }
        titleAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        mBinding.etTitle.setAdapter(titleAdapter)

        setupSpinners()
        setupDatePickers()
        setupSave()

    }

    private fun observeEvents() {
        mBinding.etTitle.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) mBinding.etTitle.showDropDown()
        }
        mBinding.ivTitleDropdown.setOnClickListener {
            mBinding.etTitle.showDropDown()
        }

        // 3) observe the suggestions LiveData
        mViewModel.titleSuggestions.observe(getViewLifecycleOwner()) { suggestions ->
            titleAdapter.clear()
            titleAdapter.addAll(suggestions)
            titleAdapter.notifyDataSetChanged()
        }
    }

    private fun setupSpinners() {
        // status spinner
        val statuses = Status.entries.map { it.name }
        mBinding.spinnerStatus.adapter = ArrayAdapter(
            requireActivity(), android.R.layout.simple_spinner_item, statuses
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        mBinding.spinnerStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedStatus = mViewModel.allStatus[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        // user spinner
        mViewModel.allUsers.observe(getViewLifecycleOwner()) { users ->
            val names = users.map { it.name }
            val ids = users.map { it.uid }
            mBinding.spinnerUser.adapter = ArrayAdapter(
                requireActivity(), android.R.layout.simple_spinner_item, names
            ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }



            mBinding.spinnerUser.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedUserId = mViewModel.allUsers.value?.get(position)?.uid
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedUserId = null
                    }
                }
        }
    }

    private fun setupDatePickers() {
        mBinding.tvAssignDate.setOnClickListener {
            Utils().openDatePicker(requireActivity()) { millis, text ->
                selectedAssignDate = millis
                mBinding.tvAssignDate.setText(text)
            }
        }
        mBinding.tvPickCompleteDate.setOnClickListener {
            Utils().openDatePicker(
                requireActivity(),
                minDate = selectedAssignDate
            ) { millis, text ->
                selectedCompleteDate = millis
                mBinding.tvPickCompleteDate.setText(text)
            }
        }
    }

    private fun setupSave() {
        mBinding.btnSave.setOnClickListener {
            val title = mBinding.etTitle.text.toString().trim()
            if (title.isEmpty() || selectedUserId == null) {
                Toast.makeText(mActivity, "Title and user required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val detail = mBinding.etDescription.text.toString().trim()
            val remark = mBinding.etRemark.text.toString().trim().takeIf { it.isNotEmpty() }

            val model = TaskModel(
                id = if (isEdit) taskId else null,
                title = title,
                taskDetail = detail,
                assignPersonId = selectedUserId!!,
                assignDate = selectedAssignDate,
                completionDate = selectedCompleteDate,
                remark = remark,
                userRemark = taskModel.userRemark,
                status = selectedStatus,
                createdBy = mViewModel.mSharePreference?.getString(R.string.prefUserId) ?: ""

            )
            mViewModel.addTask(model)
            getParentFragmentManager().popBackStack();
        }
    }

    private fun populateForEdit(task: TaskModel) {

        mBinding.etTitle.setText(task.title)
        mBinding.etDescription.setText(task.taskDetail)

        selectedAssignDate = task.assignDate
//        binding.tvAssignDate.text = formatDate(task.assignDate)
        mBinding.tvAssignDate.setText(formatDate(task.assignDate))
        // Assign user in spinner
        mViewModel.allUsers.observe(getViewLifecycleOwner()) { users ->
            val index = users.indexOfFirst { it.uid == task.assignPersonId }
            if (index != -1) {
                mBinding.spinnerUser.setSelection(index) // +1 because of "Select User"
            }
        }

        selectedUserId = task.assignPersonId

        // Remark
        mBinding.etRemark.setText(task.remark)
        mBinding.etUserRemark.setText(task.userRemark)

        // Completion Date
        selectedCompleteDate = task.completionDate
        if (selectedCompleteDate != null) {
            mBinding.tvPickCompleteDate.setText(formatDate(selectedCompleteDate!!))
        }

        val pos = task.status.ordinal
        mBinding.spinnerStatus.setSelection(pos)
        selectedStatus = task.status
        // user
        mViewModel.allUsers.value?.let { users ->
            val idx = users.indexOfFirst { it.uid == task.assignPersonId }
            if (idx >= 0) mBinding.spinnerUser.setSelection(idx)
            selectedUserId = task.assignPersonId
        }


        // Store for update
    }


    private fun formatDate(ms: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ms))

    private val generalListener: GeneralListener = GeneralListener { view ->
        when (view?.id) {
            R.id.btnCancel -> {
                showConfirmationDialog((getString(R.string.text_cancel_task))) { dialog, which ->
                    getParentFragmentManager().popBackStack();
                }
            }

            R.id.btnSave ->{
                val title = mBinding.etTitle.text.toString().trim()
                if (title.isEmpty() || selectedUserId == null) {
                    Toast.makeText(mActivity, "Title and user required", Toast.LENGTH_SHORT).show()
                    return@GeneralListener
                }
                val detail = mBinding.etDescription.text.toString().trim()
                val remark = mBinding.etRemark.text.toString().trim().takeIf { it.isNotEmpty() }

                val model = TaskModel(
                    id = if (isEdit) taskId else null,
                    title = title,
                    taskDetail = detail,
                    assignPersonId = selectedUserId!!,
                    assignDate = selectedAssignDate,
                    completionDate = selectedCompleteDate,
                    remark = remark,
                    userRemark = taskModel.userRemark,
                    status = selectedStatus,
                    createdBy = mViewModel.mSharePreference?.getString(R.string.prefUserId) ?: ""

                )
                mViewModel.addTask(model)
                getParentFragmentManager().popBackStack();
            }

        }
    }
}