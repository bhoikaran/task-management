package com.example.taskmanagement.view.dialogsearchselect

import android.app.Activity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Window
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralItemListener
import com.example.taskmanagement.businesslogic.interactors.GeneralListener
import com.example.taskmanagement.businesslogic.model.PojoDialogSearch
import com.example.taskmanagement.databinding.DialogSearchSelectBinding


class AlertDialogSearch(
    activity: Activity,
    generalListener: GeneralListener?,
    generalItemListener: GeneralItemListener?
) {
    private val mActivity: Activity = activity

    private lateinit var mBinding: DialogSearchSelectBinding
    private lateinit var mViewModel: ViewModelDialogSearch
    private lateinit var mAlertDialogSelectAudio: AlertDialog
    private var observableSourceSpinnerItem: ObservableArrayList<PojoDialogSearch>? = null

    init {
        initComponents(generalListener, generalItemListener)
        observeLiveEvents()
    }

    private fun initComponents(
        generalListener: GeneralListener?,
        generalItemListener: GeneralItemListener?
    ) {
        val dialogBuilder = AlertDialog.Builder(mActivity)
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mActivity),
            R.layout.dialog_search_select, null, false
        )

        mViewModel = ViewModelDialogSearch()
        dialogBuilder.setView(mBinding.getRoot())
        mBinding.setViewModel(mViewModel)
        mBinding.setListenerGeneralClick(generalListener)
        mBinding.setGeneralItemListener(generalItemListener)

        mAlertDialogSelectAudio = dialogBuilder.create()
        mAlertDialogSelectAudio.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mAlertDialogSelectAudio.setCancelable(false)
        mAlertDialogSelectAudio.setCanceledOnTouchOutside(false)
        mViewModel.observableSearch.set("")
    }

    private fun observeLiveEvents() {
        mBinding.editTextSearchDialog.setOnEditorActionListener({ v, actionId, event ->
            if (actionId === EditorInfo.IME_ACTION_SEARCH) {
                setUpSearch(mBinding.recylarviewSearchList.adapter)
                return@setOnEditorActionListener true
            }
            false
        })

        mBinding.editTextSearchDialog.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                mViewModel.observableSearch.set(s.toString())
                setUpSearch(mBinding.recylarviewSearchList.adapter)
            }
        })
    }

    fun showDialog(
        selectSourceId: String?,
        observableSourceSpinnerItem: ObservableArrayList<PojoDialogSearch>
    ) {
        mViewModel.observableSearch.set("")
        mViewModel.observableSearchList.clear()
        mViewModel.observableSearchList.addAll(observableSourceSpinnerItem)
        this.observableSourceSpinnerItem = observableSourceSpinnerItem

        for (i in 0..<mViewModel.observableSearchList.size) {
            mViewModel.observableSearchList[i]?.is_checked = (
                    TextUtils.equals(
                        selectSourceId,
                        mViewModel.observableSearchList[i]?.id
                    )
                    )
        }

        if (!mAlertDialogSelectAudio.isShowing) {
            mAlertDialogSelectAudio.show()
        }
    }

    private fun setUpSearch(adapter: RecyclerView.Adapter<*>?) {
        mViewModel.observableSearchList.clear()

        val query = mViewModel.observableSearch.get().trim().lowercase()

        if (query.isNotEmpty()) {
            observableSourceSpinnerItem?.forEach { pojo ->
                if (pojo.title.trim().lowercase().contains(query)) {
                    mViewModel.observableSearchList.add(pojo)
                }
            }
            mViewModel.observerIsNoRecords.set(mViewModel.observableSearchList.isEmpty())
        } else {
            observableSourceSpinnerItem?.let {
                mViewModel.observableSearchList.addAll(it)
            }
        }

        adapter?.notifyDataSetChanged()
    }

    fun dismissDialog() {
        mAlertDialogSelectAudio.dismiss()
    }

    fun onItemClick(position: Int) {
        mViewModel.observableSearchList[position]?.is_checked?.let {
            if (!it) {
                mViewModel.observableSearchList[position]?.is_checked = true
            }
        }
    }

    fun setTitle(@StringRes res: Int) {
        mViewModel.observableTitle.set(res)
    }
}
