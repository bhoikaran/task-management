package com.example.taskmanagement.view.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralItemListener
import com.example.taskmanagement.businesslogic.interactors.GeneralListener
import com.example.taskmanagement.businesslogic.model.PojoDialogSearch
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.viewmodel.UserViewModel
import com.example.taskmanagement.databinding.FragmentUserHomeBinding
import com.example.taskmanagement.view.adapter.TaskAdapter
import com.example.taskmanagement.view.dialogsearchselect.AlertDialogSearch
import com.google.firebase.auth.FirebaseAuth


class UserHomeFragment : FragmentBase() {
    private lateinit var mBinding: FragmentUserHomeBinding
    private lateinit var mViewModel: UserViewModel
    private lateinit var adapterTask: TaskAdapter
    val currentUser = FirebaseAuth.getInstance().currentUser

    private var alertDialogSearch: AlertDialogSearch? = null
    private lateinit var pojoSearchSelect: PojoDialogSearch
    private var dialogFlag = 1

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
            R.layout.fragment_user_home,
            container,
            false
        )
        mViewModel = UserViewModel(requireActivity().applicationContext as MyApplication)
        mBinding.viewModel = mViewModel
        mBinding.generalListener = generalListener
        mBinding.lifecycleOwner = viewLifecycleOwner
        (requireActivity() as AppCompatActivity)
            .setSupportActionBar(mBinding.topAppBar)
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        if (TextUtils.isEmpty(mViewModel.mSharePreference?.getString(R.string.prefUserId))) {
            mActivityMain?.navigateToLogin()
            return
        }

        if (currentUser == null) {
            mActivityMain?.navigateToLogin()
        }
        val uid = currentUser?.uid
        mViewModel.applyFilters(uid, null)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponents()
        observeEvents()


    }

    private fun initComponents() {
        setupRecyclerView()
//        setupFilters()

    }

    private fun observeEvents() {
        mViewModel.callLogout.observe(
            getViewLifecycleOwner(),
            { it ->
                mViewModel.mSharePreference?.clear()
                mActivityMain?.navigateToLogin()
            })

        mViewModel.filteredTasks.observe(getViewLifecycleOwner()) { tasks ->
            adapterTask.tasks = tasks
        }
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showConfirmationDialog((getString(R.string.text_logout))) { dialog, which ->
                    mViewModel.logout()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapterTask = TaskAdapter(emptyList(), generalItemListener)
        adapterTask.isAdmin = false
        mBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        mBinding.recyclerView.adapter = adapterTask
    }


    /*  private fun setupFilters() {

          val statusNames = listOf("All") + Status.entries.map { it.name }
          mBinding.spinnerStatus.adapter = ArrayAdapter(
              requireActivity(), android.R.layout.simple_spinner_item, statusNames
          ).apply {
              setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
          }
          mBinding.spinnerStatus.onItemSelectedListener =
              object : AdapterView.OnItemSelectedListener {
                  override fun onItemSelected(
                      parent: AdapterView<*>, view: View?, position: Int, id: Long
                  ) {
                      val selectedStatus =
                          if (position == 0) null else Status.entries[position - 1].name


                      if (currentUser == null) {
                          // redirect to login
                          return
                      }
                      val uid = currentUser.uid
                      mViewModel.applyFilters(uid, selectedStatus)


                  }

                  override fun onNothingSelected(p0: AdapterView<*>?) {
                      TODO("Not yet implemented")
                  }
              }
      }
  */


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
           /* R.id.btnLogout -> {
                showConfirmationDialog((getString(R.string.text_logout))) { dialog, which ->
                    mViewModel.logout()
                }
            }*/

            R.id.actvStatus -> {
                mViewModel.addAlertSearchItem(myApplication?.applicationContext)
                showSearchDialog(1)
            }

            R.id.btnAlertCancel -> {
                alertDialogSearch?.dismissDialog()
            }

            R.id.btnAlertSave -> {
                setUpTextSelection()
                alertDialogSearch?.dismissDialog()
            }

        }
    }
    private val generalItemListener = GeneralItemListener { view, _, item ->
        if (item is TaskModel) {
            when (view?.id) {

                R.id.imageview_edit -> {
                    val bundle = Bundle().apply {
                        putString("task_id", item.id)
                    }

                    val taskFragment = FragmentUserTask().apply {
                        arguments = bundle
                    }
                    mActivityMain?.addFragment(
                        taskFragment,
                        FragmentUserTask().javaClass.simpleName
                    )
                }


            }
        }
    }


    fun changeFilterAlert() {
        Log.d("Tag","observableTaskAssignToUid")
        val userId = currentUser?.uid
//        val sPos = mViewModel.observableTaskStatus
        val sPos = mViewModel.observableTaskStatus.get()
        val status =
            if (sPos.equals("") || sPos.equals(myApplication?.applicationContext?.getString(R.string.text_all))) null else mViewModel.selectedStatus.name

        Log.d("Tag","changeFilterAlert sPos : $sPos ")
        Log.d("Tag","changeFilterAlert userId : $userId status : $status ")
        mViewModel.applyFilters(userId, status)
    }

}