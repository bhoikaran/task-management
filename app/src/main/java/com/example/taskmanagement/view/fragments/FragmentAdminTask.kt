package com.example.taskmanagement.view.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralItemListener
import com.example.taskmanagement.businesslogic.interactors.GeneralListener
import com.example.taskmanagement.businesslogic.model.PojoDialogSearch
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.viewmodel.AdminTaskViewModel
import com.example.taskmanagement.databinding.FragmentAdminTaskBinding
import com.example.taskmanagement.utils.Utils
import com.example.taskmanagement.view.dialogsearchselect.AlertDialogSearch
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FragmentAdminTask : FragmentBase() {
    private lateinit var mBinding: FragmentAdminTaskBinding
    private lateinit var mViewModel: AdminTaskViewModel
    private var taskId: String? = null
    private var isEdit = false

    private var selectedAssignDate = System.currentTimeMillis()
    private var selectedCompleteDate: Long? = null
    private lateinit var titleAdapter: ArrayAdapter<String>
    private lateinit var userAdapter: ArrayAdapter<String>
    private lateinit var statusAdapter: ArrayAdapter<String>
    val currentUser = FirebaseAuth.getInstance().currentUser

    private var bottomSheetAddTitle: BottomSheetAddTitle? = null

    //    private lateinit var alertDialogSearch: AlertDialogSearch
    private var alertDialogSearch: AlertDialogSearch? = null

    private lateinit var pojoSearchSelect: PojoDialogSearch
    private var dialogFlag = 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_admin_task, container, false
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
//        mBinding.actvAssignedUser.keyListener = null

        if (currentUser?.uid != null) {
            mViewModel.setAdminId(currentUser.uid)
        }

        taskId = arguments?.getString("task_id")

        if (!taskId.isNullOrEmpty()) {
            isEdit = true

            mViewModel.editMode.set(true)
            mViewModel.getTaskById(taskId!!).observe(getViewLifecycleOwner()) { task ->
                if (task != null) {

                    mViewModel.taskModel = task
                }

                task?.let { populateForEdit(it) }
            }
        }


//        setupSpinners()
        setupUserAutoComplete()


    }

    private fun observeEvents() {
        mBinding.atvTextTitle.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) mBinding.atvTextTitle.showDropDown()
        }
       /* mBinding.actvStatus.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) mBinding.actvStatus.showDropDown()
        }*/
        /* mBinding.actvAssignedUser.setOnFocusChangeListener { _, hasFocus ->
             if (hasFocus) mBinding.actvAssignedUser.showDropDown()
         }*/

        // 3) observe the suggestions LiveData
        mViewModel.titleSuggestions.observe(getViewLifecycleOwner()) { suggestions ->
            titleAdapter.clear()
            titleAdapter.addAll(suggestions)
            titleAdapter.notifyDataSetChanged()
        }


        mViewModel.allUsers.observe(getViewLifecycleOwner()) { users ->

            Log.d("Tag", "All Users : ${mViewModel.allUsers.value}")
//            val userNames = users.map { it.name }
//
//            userAdapter.clear()
//            userAdapter.addAll(userNames)
//            userAdapter.notifyDataSetChanged()
//
//            // When a user is selected
//            mBinding.actvAssignedUser.setOnItemClickListener { _, _, position, _ ->
//
//                mViewModel.observableTaskAssignTo.set(users[position].name)
//                mViewModel.observableTaskAssignToUid.set(users[position].uid)
//            }
//
//            // Pre-select user if editing
//            if (isEdit) {
//                val selectedIndex =
//                    users.indexOfFirst { it.uid == mViewModel.taskModel.assignPersonId }
//                if (selectedIndex != -1) {
//                    mBinding.actvAssignedUser.setText(users[selectedIndex].name, false)
//
//                    mViewModel.observableTaskAssignTo.set(users[selectedIndex].uid)
//                }
//            }
        }

    }


    private fun setupUserAutoComplete() {

        titleAdapter = ArrayAdapter(
            requireActivity(), android.R.layout.simple_dropdown_item_1line, mutableListOf()
        )
        mBinding.atvTextTitle.setAdapter(titleAdapter)

//        userAdapter = ArrayAdapter(
//            requireActivity(), android.R.layout.simple_dropdown_item_1line, mutableListOf()
//        )
//        mBinding.actvAssignedUser.setAdapter(userAdapter)


        val statuses = Status.entries.map { it.name }

        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, statuses
        )
//        mBinding.actvStatus.setAdapter(adapter)

       /* mBinding.actvStatus.setOnItemClickListener { _, _, position, _ ->
            mViewModel.selectedStatus = Status.entries[position]

            mViewModel.observableTaskStatus.set(Status.entries[position].toString())
        }*/

    }

    private fun populateForEdit(task: TaskModel) {


        mViewModel.observableTitle.set(task.title)
        mViewModel.observableTaskDescription.set(task.taskDetail)
        mViewModel.observableTaskAssignTo.set(mViewModel.getUserNameById(task.assignPersonId))

        mViewModel.observableTaskAssignToUid.set(task.assignPersonId)
        selectedAssignDate = task.assignDate
        mViewModel.observableAssignDate.set(formatDate(task.assignDate))

        selectedCompleteDate = task.completionDate
        if (selectedCompleteDate != null) {
            mViewModel.observableCompleteDate.set(formatDate(selectedCompleteDate!!))
        }

        mViewModel.observableTaskDescription.set(task.taskDetail)
        mViewModel.observableTaskAdminRemark.set(task.remark)
        mViewModel.observableTaskUserRemark.set(task.userRemark)
        // Assign user in spinner
        /* mViewModel.allUsers.observe(getViewLifecycleOwner()) { users ->
             val index = users.indexOfFirst { it.uid == task.assignPersonId }
             if (index != -1) {
                 mBinding.spinnerUser.setSelection(index) // +1 because of "Select User"
             }
         }*/


        // Remark

        // Completion Date


        val pos = task.status.ordinal
//        mBinding.actvStatus.setText(Status.entries[pos].name, false)
        mViewModel.selectedStatus = task.status
//        mViewModel.observableTaskStatus.set(Status.entries[pos].toString())
        mViewModel.observableTaskStatus.set(Status.entries[pos].name.replace("_", " ").lowercase())

        // user
        /*  mViewModel.allUsers.value?.let { users ->
              val idx = users.indexOfFirst { it.uid == task.assignPersonId }
              if (idx >= 0) mBinding.spinnerUser.setSelection(idx)
              selectedUserId = task.assignPersonId
          }*/


        // Store for update
    }


    private fun formatDate(ms: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ms))


    private fun showSearchDialog(dialogFlagData: Int) {
        dialogFlag = dialogFlagData
        if (alertDialogSearch == null) {
            alertDialogSearch =
                AlertDialogSearch(requireActivity(), generalListener, generalFilterItemListener)
        }
        setUpDialogData()
        setUpDialogTitleData()
    }

    private fun setUpDialogData() {
        /*mViewModel.observableSearchDataList.clear();
        for (int i = 0; i < 5; i++) {
            mViewModel.observableSearchDataList.add(new PojoDialogSearch("item " + (i + 1), "id" + (i + 1)));
        }*/
    }

    private fun setUpDialogTitleData() {
        var selectedTitle: Int = R.string.text_search
        var selectedId: String? = ""
        if (dialogFlag == 1) {
            selectedId = mViewModel.observableTaskAssignToUid.get()
            selectedTitle = R.string.select_user
        } else if (dialogFlag == 2) {
            selectedId = mViewModel.selectedStatus.name
            selectedTitle = R.string.select_status
        }
        alertDialogSearch?.setTitle(selectedTitle)
        alertDialogSearch?.showDialog(selectedId, mViewModel.observableSearchDataList)
    }

    private fun setUpTextSelection() {
        if (pojoSearchSelect != null && !TextUtils.isEmpty(pojoSearchSelect.title)) {
            if (dialogFlag == 1) {
                mViewModel.observableTaskAssignToUid.set(pojoSearchSelect.id)
                mViewModel.observableTaskAssignTo.set(pojoSearchSelect.title)
            } else if (dialogFlag == 2) {
                mViewModel.selectedStatus = Status.valueOf(pojoSearchSelect.id)
                mViewModel.observableTaskStatus.set(pojoSearchSelect.title)
            }
        }
    }

    private val generalFilterItemListener: GeneralItemListener = object : GeneralItemListener {
        override fun onItemClick(view: View?, position: Int, item: Any?) {
            if (item is PojoDialogSearch) {
                alertDialogSearch?.onItemClick(position)
                pojoSearchSelect = item
            }
        }
    }

    private val generalListener: GeneralListener = GeneralListener { view ->
        when (view?.id) {
            R.id.actvAssignedUser -> {
                mViewModel.addAlertSearchItem(1)
                showSearchDialog(1)
            }

            R.id.actvStatus -> {
                mViewModel.addAlertSearchItem(2)
                showSearchDialog(2)
            }

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

            R.id.btnAlertCancel -> {
                alertDialogSearch?.dismissDialog()
            }

            R.id.btnAlertSave -> {
                setUpTextSelection()
                alertDialogSearch?.dismissDialog()
            }

            R.id.btnSave -> {

//                mViewModel.saveTask()


                if (mViewModel.observableTitle.isEmptyData || mViewModel.observableTaskAssignTo.isEmptyData) {
                    Toast.makeText(mActivity, "Title and user required", Toast.LENGTH_SHORT).show()
                    return@GeneralListener
                }

                val model = TaskModel(
                    id = if (isEdit) taskId else null,
                    title = mViewModel.observableTitle.trimmed,
                    taskDetail = mViewModel.observableTaskDescription.trimmed,
                    assignPersonId = mViewModel.observableTaskAssignToUid.get(),
                    assignDate = selectedAssignDate,
                    completionDate = selectedCompleteDate,
                    remark = mViewModel.observableTaskAdminRemark.get(),
                    userRemark = mViewModel.taskModel.userRemark,
                    status = mViewModel.selectedStatus,
                    createdBy = mViewModel.mSharePreference?.getString(R.string.prefUserId) ?: ""

                )
                mViewModel.addTask(model)
                getParentFragmentManager().popBackStack();
            }

            R.id.add_title -> {
                bottomSheetAddTitle = BottomSheetAddTitle.newInstance(mViewModel)
                bottomSheetAddTitle!!.show(
                    mActivity!!.supportFragmentManager,
                    BottomSheetAddTitle::class.java.getSimpleName()
                )
            }

            R.id.cv_select_date -> {

                mActivity?.let {
                    Utils().openDatePicker(
                        it,
                    ) { millis, text ->
                        selectedAssignDate = millis
                        mViewModel.observableAssignDate.set(text)
                    }
                }

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