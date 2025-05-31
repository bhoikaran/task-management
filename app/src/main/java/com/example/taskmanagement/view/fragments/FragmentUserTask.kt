package com.example.taskmanagement.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
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
import com.example.taskmanagement.businesslogic.viewmodel.UserTaskViewModel
import com.example.taskmanagement.databinding.FragmentAdminTaskBinding
import com.example.taskmanagement.databinding.FragmentUserTaskBinding
import com.example.taskmanagement.utils.Utils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FragmentUserTask : FragmentBase() {
    private lateinit var mBinding: FragmentUserTaskBinding
    private lateinit var mViewModel: UserTaskViewModel
    private var taskId: String? = null
    private var isEdit = false
    private var taskModel: TaskModel = TaskModel()
    private var selectedAssignDate = System.currentTimeMillis()
    private var selectedCompleteDate: Long? = null
    private var selectedUserId: String? = null
    private var selectedStatus = Status.IN_PROGRESS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_user_task, container, false
        )
        mViewModel = UserTaskViewModel(requireActivity().applicationContext as MyApplication)
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

    }

    private fun observeEvents() {

    }


    /*  private fun populateForEdit(task: TaskModel) {

          mBinding.etTitle.setText(task.title)
          mBinding.etDescription.setText(task.taskDetail)

          selectedAssignDate = task.assignDate
  //        binding.tvAssignDate.text = formatDate(task.assignDate)
          mBinding.tvAssignDate.setText(formatDate(task.assignDate))
          // Assign user in spinner

          selectedUserId = task.assignPersonId

          // Remark
          mBinding.etRemark.setText(task.userRemark)

          // Completion Date
          selectedCompleteDate = task.completionDate ?: 0L
          if (selectedCompleteDate != 0L) {
              mBinding.tvPickCompleteDate.setText(formatDate(selectedCompleteDate!!))
          }

          val pos = task.status.ordinal

          selectedStatus = task.status

      }*/


    private fun populateForEdit(task: TaskModel) {
        mViewModel.observableTitle.set(task.title)
        mViewModel.observableTaskDescription.set(task.taskDetail)
        selectedAssignDate = task.assignDate
        mViewModel.observableAssignDate.set(formatDate(task.assignDate))

        selectedCompleteDate = task.completionDate
        if (selectedCompleteDate != null) {
            mViewModel.observableCompleteDate.set(formatDate(selectedCompleteDate!!))
        }
        mViewModel.observableTaskUserRemark.set(task.userRemark)
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

            R.id.backArrow -> {
                showConfirmationDialog((getString(R.string.text_cancel_task))) { dialog, which ->
                    getParentFragmentManager().popBackStack();
                }
            }

            R.id.btnSave -> {

                if (mViewModel.observableTaskUserRemark.isEmptyData == true || selectedCompleteDate == null) {
                    Toast.makeText(
                        mActivity, "Remark and Complete Date are required", Toast.LENGTH_LONG
                    ).show()
                    return@GeneralListener
                }

                val model = TaskModel(
                    /* id = if (isEdit) taskId else null,
                                        completionDate = selectedCompleteDate,
                                        userRemark = remark,
                                        status = Status.MARKED_DONE,*/

                    id = if (isEdit) taskId else null,
                    title = taskModel.title,
                    taskDetail = taskModel.taskDetail,
                    assignPersonId = taskModel.assignPersonId,
                    assignDate = taskModel.assignDate,
                    completionDate = selectedCompleteDate,
                    remark = taskModel.remark,
                    userRemark = mViewModel.observableTaskUserRemark.trimmed,
                    status = Status.MARKED_DONE,
                    createdBy = taskModel.createdBy,
                )
                mViewModel.updateTask(model)
                getParentFragmentManager().popBackStack();
            }

            R.id.cv_select_complete_date -> {

                mActivity?.let {
                    Utils().openDatePicker(
                        it, minDate = selectedAssignDate
                    ) { millis, text ->
                        selectedCompleteDate = millis
                        mViewModel.observableCompleteDate.set(text)
                    }
                }

            }
        }
    }
}