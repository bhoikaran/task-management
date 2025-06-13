package com.example.taskmanagement.view.fragments

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralItemListener
import com.example.taskmanagement.businesslogic.interactors.GeneralListener
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.TitleModel
import com.example.taskmanagement.businesslogic.viewmodel.AdminTaskViewModel
import com.example.taskmanagement.databinding.BottomSheetAddTitleBinding
import com.example.taskmanagement.view.activity.main.MainActivity
import com.example.taskmanagement.view.adapter.TaskAdapter
import com.example.taskmanagement.view.adapter.TitleAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.Timestamp


class BottomSheetAddTitle : BottomSheetBase() {

    private lateinit var mBinding: BottomSheetAddTitleBinding
    private lateinit var viewModel: AdminTaskViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var titleAdapter: TitleAdapter
    fun setViewModel(viewModel: AdminTaskViewModel) {
        this.viewModel = viewModel
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater, R.layout.bottom_sheet_add_title, container,
            false
        )
        mBinding.setViewModel(viewModel)
        mBinding.setGeneralListener(generalListener)
        return mBinding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.constraintBusArea.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        bottomSheetBehavior = BottomSheetBehavior.from((mBinding.root.parent) as View)
        if (mActivity != null) {
            bottomSheetBehavior.setPeekHeight(
                getScreenHeight(mActivity!!) - statusBarHeight
            )
        }

        initComponents()
        observeEvent()


    }

    private fun initComponents() {
        setupRecyclerView()

    }

    private fun observeEvent() {
        viewModel.adminTitles.observe(viewLifecycleOwner) { titleList ->
            titleAdapter.titles = titleList
        }
    }

    private fun setupRecyclerView() {
        titleAdapter = TitleAdapter(generalItemListener)

        mBinding.rvTitles.layoutManager = LinearLayoutManager(activity)
        mBinding.rvTitles.adapter = titleAdapter
    }

    fun dismissDialog() {
        dismiss()
    }

    private val isValidFormData: Boolean
        get() {
            resetErrors()
            return true
        }

    private fun resetErrors() {

    }

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId: Int =
                getResources().getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId)
            }
            return result
        }


    private val generalListener: GeneralListener = GeneralListener { view ->
        if (view != null) {
            when (view.id) {


                R.id.btn_add_title -> {
                    Log.d("btn_add_title","btn_add_title :   vbdsdf")
//                    val title = mBinding.etTitleName.text.toString().trim()
                    val title = viewModel.observableBottomSheetTitle.get()
                    if (title.isEmpty()) {
                        Toast.makeText(mActivity, "Title is required", Toast.LENGTH_SHORT)
                            .show()
                        return@GeneralListener
                    }
                    val model = TitleModel(
                        name = title,
                        createdBy = viewModel.mSharePreference?.getString(R.string.prefUserId)
                            ?: "",
                        createdAt = Timestamp.now()
                    )
                    viewModel.addTitle(model)
                }R.id.img_close -> {
                dismissDialog()
            }
            }
        }
    }

    companion object {
        fun newInstance(
            viewModel: AdminTaskViewModel,
        ): BottomSheetAddTitle {
            val bottomSheet =
                BottomSheetAddTitle()
            bottomSheet.setViewModel(viewModel)
            return bottomSheet
        }

        fun getScreenHeight(activity: Activity): Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                return windowMetrics.bounds.height() - insets.top - insets.bottom
            } else {
                val displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                return displayMetrics.heightPixels
            }
        }
    }

    private val generalItemListener = GeneralItemListener { view, _, item ->
        if (item is TitleModel) {
            when (view?.id) {

                R.id.iv_delete -> {

                    showConfirmationDialog((getString(R.string.text_delete_item))) { dialog, which ->
                        viewModel.deleteTitle(item)
                    }
                }

            }
        }
    }

}