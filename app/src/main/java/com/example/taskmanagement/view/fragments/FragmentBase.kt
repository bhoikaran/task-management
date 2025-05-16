package com.example.taskmanagement.view.fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.utils.utility.UtilPreference
import com.example.taskmanagement.view.activity.BaseActivity
import com.example.taskmanagement.view.activity.main.MainActivity


open class FragmentBase : Fragment() {
    protected var mActivity: BaseActivity? = null

    protected var mActivityMain: MainActivity? = null

    protected var myApplication: MyApplication? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mActivity = activity as BaseActivity?
        myApplication = (mActivity?.application as MyApplication)
        if (activity is MainActivity) {
            mActivityMain = activity as MainActivity
        }
    }
    fun showConfirmationDialog(message: String?, okListener: DialogInterface.OnClickListener?) {
        val mAlertDialog = AlertDialog.Builder(mActivity!!, R.style.DialogTheme)
        mAlertDialog.setMessage(message)
            .setPositiveButton(resources.getString(R.string.text_yes), okListener)
            .setNegativeButton(resources.getString(R.string.text_no), null)
            .create()
            .show()
    }
}