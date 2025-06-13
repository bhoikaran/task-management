package com.example.taskmanagement.view.fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.taskmanagement.R
import com.example.taskmanagement.view.activity.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BottomSheetBase : BottomSheetDialogFragment() {
    protected var mContext: Context? = null
    protected var mActivity: BaseActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as BaseActivity?
        mContext = context
        setHasOptionsMenu(false)
    }

    /**
     * Use to Grant permission
     */
    fun requestWriteExternalStoragePermission(
        permission: String?, requestCode: Int,
        message: String?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(permission!!)) {
                showConfirmationDialog(permission, requestCode, message)
            } else {
                requestPermissions(arrayOf<String?>(permission), requestCode)
            }
        }
    }

    /**
     * Show message Dialog for grant permission
     */
    fun showConfirmationDialog(
        permission: String, requestCode: Int,
        message: String?
    ) {
        val builder = AlertDialog.Builder(
            mActivity!!, R.style.DialogTheme
        )
        builder.setTitle(mContext!!.resources.getString(R.string.text_alert))
        builder.setMessage(message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setCancelable(false)
        builder.setPositiveButton(
            resources.getString(R.string.text_ok)
        ) { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(permission), requestCode)
            }
        }
        builder.setNegativeButton(
            resources.getString(R.string.text_cancel)
        ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        val networkDialog = builder.create()
        networkDialog.show()
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
