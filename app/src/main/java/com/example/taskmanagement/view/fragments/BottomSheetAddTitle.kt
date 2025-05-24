package com.example.taskmanagement.view.fragments

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.databinding.DataBindingUtil
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.viewmodel.AdminTaskViewModel
import com.example.taskmanagement.databinding.BottomSheetAddTitleBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior


class BottomSheetAddTitle : Fragment() {

    private lateinit var mBinding: BottomSheetAddTitleBinding
    private lateinit var viewModel: AdminTaskViewModel
    private lateinit var bottomSheetBehavior : BottomSheetBehavior<View>
    private lateinit var  selectedCountryCode : String


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
        bottomSheetBehavior.setPeekHeight(
            getScreenHeight(mActivity) - toolBarHeight - statusBarHeight
        )
        initComponents()
        observeEvent()

        selectedCountryCode = mBinding.ccp.selectedCountryCodeWithPlus
    }

    private fun initComponents() {

    }

    private fun observeEvent() {

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

    val toolBarHeight: Int
        get() = (mActivity as ActivityScheduleSession).getToolbarHeight()

    private val generalListener: GeneralListener = GeneralListener { view ->
        if (view != null) {
            if (view.id == R.id.img_close) {
                viewModel.setMutableDialog("close")
            }
        }
    }

    companion object {
        fun newInstance(
            viewModel: ViewModelScheduleSession,
            isNational: Boolean
        ):BottomSheetAddPatients {
            val bottomSheet =
                BottomSheetAddPatients()
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
}