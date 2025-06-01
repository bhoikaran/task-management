package com.example.taskmanagement.view.fragments

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.Resource
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralItemListener
import com.example.taskmanagement.businesslogic.interactors.GeneralListener
import com.example.taskmanagement.businesslogic.model.PojoDialogSearch
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.viewmodel.AdminViewModel
import com.example.taskmanagement.databinding.FragmentAdminHomeBinding
import com.example.taskmanagement.utils.ExportToExcel
import com.example.taskmanagement.utils.Utils
import com.example.taskmanagement.view.adapter.TaskAdapter
import com.example.taskmanagement.view.dialogsearchselect.AlertDialogSearch
import org.apache.poi.xssf.usermodel.XSSFWorkbook


class AdminHomeFragment : FragmentBase() {


    private lateinit var mBinding: FragmentAdminHomeBinding
    private lateinit var mViewModel: AdminViewModel
    private lateinit var adapterTask: TaskAdapter

    private var userIds: List<String?> = listOf()
    private var workbook: XSSFWorkbook? = null
    private lateinit var pendingIntent: Intent
    private var alertDialogSearch: AlertDialogSearch? = null

    private lateinit var pojoSearchSelect: PojoDialogSearch
    private var dialogFlag = 1

    private val exportLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    workbook?.let { ExportToExcel().saveWorkbookToUri(requireActivity(), uri, it) }
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_admin_home,
            container,
            false
        )
        mViewModel = AdminViewModel(requireActivity().applicationContext as MyApplication)
        mBinding.viewModel = mViewModel
        mBinding.generalListener = generalListener
//        mBinding.spinnerItemSelectedListener = onItemSelectedListener
        mBinding.lifecycleOwner = viewLifecycleOwner
        (requireActivity() as AppCompatActivity)
            .setSupportActionBar(mBinding.topAppBar)

        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponents()
        observeEvents()


    }

    override fun onResume() {
        super.onResume()
        if (TextUtils.isEmpty(mViewModel.mSharePreference?.getString(R.string.prefUserId))) {
            mActivityMain?.navigateToLogin()
            return
        }


        mViewModel.applyFilters(null, null)
    }

    private fun initComponents() {
        setupRecyclerView()
//        setupFilters()
        setupMenuClicks()
    }

    private fun observeEvents() {

        mViewModel.callLogout.observe(
            getViewLifecycleOwner(),
            { it ->
                mViewModel.mSharePreference?.clear()
                mActivityMain?.navigateToLogin()
            })



        mViewModel.filteredTasks.observe(getViewLifecycleOwner()) { tasks ->
//            adapter.submitList(tasks)
            Log.d("filteredTasks", "filteredTasks : $tasks")

            mViewModel.observerNoRecords.set(if (tasks.isEmpty()) 2 else 1)
            adapterTask.tasks = tasks
        }
        mViewModel.allUsers.observe(getViewLifecycleOwner()) { users ->
            Log.d("allUsers", "allUsers : $users")
            adapterTask.users = users
        }
    }

    private fun setupMenuClicks() {
        mBinding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_filter -> {
                    // toggle your filter card
                    mBinding.filterSection.isVisible = !mBinding.filterSection.isVisible
                    true
                }

                R.id.action_logout -> {
                    showConfirmationDialog((getString(R.string.text_logout))) { dialog, which ->
                        mViewModel.logout()
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapterTask = TaskAdapter(emptyList(), generalItemListener)
        adapterTask.isAdmin = true
        mBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        mBinding.recyclerView.adapter = adapterTask
    }

    /* private fun setupFilters() {
         mViewModel.allUsers.observe(getViewLifecycleOwner()) { users ->
             val names = listOf("All") + users.map { it.name }
             userIds = listOf(null) + users.map { it.uid }

             mBinding.spinnerUser.adapter = ArrayAdapter(
                 requireActivity(), android.R.layout.simple_spinner_item, names
             ).apply {
                 setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
             }
         }

         val statusNames = listOf("All") + Status.entries.map { it.name }
         mBinding.spinnerStatus.adapter = ArrayAdapter(
             requireActivity(), android.R.layout.simple_spinner_item, statusNames
         ).apply {
             setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
         }

         mBinding.spinnerUser.onItemSelectedListener = onItemSelectedListener
         mBinding.spinnerStatus.onItemSelectedListener = onItemSelectedListener

     }*/


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
            if (TextUtils.isEmpty(mViewModel.observableTaskStatus.get()) || mViewModel.observableTaskStatus.equals(
                    "0"
                )
            ) {
                selectedId = "0"
            } else {
                selectedId = mViewModel.selectedStatus.name
            }
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
                if (pojoSearchSelect.id != "0") {
                    mViewModel.selectedStatus = Status.valueOf(pojoSearchSelect.id)
                }
                mViewModel.observableTaskStatus.set(pojoSearchSelect.title)
            }
        }
        changeFilterAlert()
    }

    private val generalFilterItemListener: GeneralItemListener =
        GeneralItemListener { _, position, item ->
            if (item is PojoDialogSearch) {
                alertDialogSearch?.onItemClick(position)
                pojoSearchSelect = item
            }
        }

    private val generalListener: GeneralListener = GeneralListener { view ->
        when (view?.id) {

            R.id.fabAddTask -> mActivityMain?.addFragment(
                FragmentAdminTask(),
                FragmentAdminTask().javaClass.simpleName
            )

            R.id.actvAssignedUser -> {
                mViewModel.addAlertSearchItem(myApplication?.applicationContext, 1)
                showSearchDialog(1)
            }

            R.id.actvStatus -> {
                mViewModel.addAlertSearchItem(myApplication?.applicationContext, 2)
                showSearchDialog(2)
            }

            R.id.btnAlertCancel -> {
                alertDialogSearch?.dismissDialog()
            }

            R.id.btnAlertSave -> {
                setUpTextSelection()
                alertDialogSearch?.dismissDialog()
            }

            /* R.id.exportToExcel -> {

                 Log.d("Tag","exportToExcel : ${mViewModel.filteredTasks.value}")
                 Log.d("Tag","exportToExcel : ${mViewModel.allUsers.value}")


                 mActivity?.initiateExcelExport(
                     mViewModel.filteredTasks.value!!,
                     mViewModel.allUsers.value!!
                 )
             }
 */
            R.id.btnLogout -> {
                showConfirmationDialog((getString(R.string.text_logout))) { dialog, which ->
                    mViewModel.logout()
                }
            }
        }
    }
    private val generalItemListener = GeneralItemListener { view, _, item ->
        if (item is TaskModel) {
            when (view?.id) {
                R.id.imageview_delete -> {

                    showConfirmationDialog((getString(R.string.text_delete_item))) { dialog, which ->
                        mViewModel.deleteTask(item)
                    }
                }

                R.id.imageview_edit -> {
                    val bundle = Bundle().apply {
                        putString("task_id", item.id)
                    }

                    val taskFragment = FragmentAdminTask().apply {
                        arguments = bundle
                    }
                    mActivityMain?.addFragment(
                        taskFragment,
                        FragmentAdminTask().javaClass.simpleName
                    )
                }

                R.id.imageview_share -> {
                    val user = mViewModel.allUsers.value?.find { it.uid == item.assignPersonId }?.name ?: "Unknown"
                    context?.let { ctx ->
                        if (ctx is Activity || ctx is ContextWrapper && ctx.baseContext is Activity) {
                            Utils().shareTaskDetails(ctx, item, user)

                        } else {
                            Utils().shareTaskDetails(ctx.applicationContext, item, user)
                        }
                    }
                    /*val user = mViewModel.allUsers.value?.find { it.uid == item.assignPersonId }?.name ?: "Unknown"
                    mActivityMain?.baseContext?.let { Utils().shareTaskDetails(it, item, user) }*/
                }


            }
        }
    }

    private val longClickListener = GeneralItemListener { view, _, item ->
        if (item is TaskModel) {
            when (view?.id) {
                R.id.cv_task_main -> {


                }
            }
        }
    }


    /*private val onItemSelectedListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                if (adapterView.id == R.id.spinnerUser) {
                  changeFilter()
                } else if (adapterView.id == R.id.spinnerStatus) {
                    changeFilter()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
            }
        }
*/
    /* fun changeFilter(){
         val uPos = mBinding.spinnerUser.selectedItemPosition
         val userId = userIds.getOrNull(uPos)
         val sPos = mBinding.spinnerStatus.selectedItemPosition
         val status = if (sPos == 0) null else Status.entries[sPos - 1].name
         Log.d("Tag","changeFilter userId : $userId status : $status ")
         mViewModel.applyFilters(userId, status)
     }*/

    fun changeFilterAlert() {
        Log.d("Tag", "observableTaskAssignToUid")
        val userId = if (mViewModel.observableTaskAssignToUid.get() == "0") {
            null
        } else mViewModel.observableTaskAssignToUid.get()
//        val sPos = mViewModel.observableTaskStatus
        val sPos = mViewModel.observableTaskStatus.get()
        val status =
            if (sPos.equals("") || sPos.equals(myApplication?.applicationContext?.getString(R.string.text_all))) null else mViewModel.selectedStatus.name

        Log.d("Tag", "changeFilterAlert sPos : $sPos ")
        Log.d("Tag", "changeFilterAlert userId : $userId status : $status ")
        mViewModel.applyFilters(userId, status)
    }

    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION = 200

    }
}