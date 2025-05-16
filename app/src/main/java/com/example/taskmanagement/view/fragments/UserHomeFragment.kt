package com.example.taskmanagement.view.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralItemListener
import com.example.taskmanagement.businesslogic.interactors.GeneralListener
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.viewmodel.AdminViewModel
import com.example.taskmanagement.businesslogic.viewmodel.UserViewModel
import com.example.taskmanagement.databinding.FragmentAdminHomeBinding
import com.example.taskmanagement.databinding.FragmentUserHomeBinding
import com.example.taskmanagement.view.adapter.TaskAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlin.collections.get

class UserHomeFragment : FragmentBase() {
    private lateinit var mBinding: FragmentUserHomeBinding
    private lateinit var mViewModel: UserViewModel
    private lateinit var adapterTask: TaskAdapter
    val currentUser = FirebaseAuth.getInstance().currentUser

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
        setupFilters()

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


    private fun setupRecyclerView() {
        adapterTask = TaskAdapter(emptyList(), generalItemListener)
        adapterTask.isAdmin = false
        mBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        mBinding.recyclerView.adapter = adapterTask
    }


    private fun setupFilters() {

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

    private val generalListener: GeneralListener = GeneralListener { view ->
        when (view?.id) {
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

}