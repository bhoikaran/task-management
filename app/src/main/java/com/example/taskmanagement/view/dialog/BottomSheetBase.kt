package com.example.taskmanagement.view.dialog

import android.R
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.taskmanagement.view.activity.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BottomSheetBase : BottomSheetDialogFragment() {
    protected lateinit var mContext: Context
    protected lateinit var mActivity: BaseActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as BaseActivity
        mContext = requireContext()
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
            mActivity!!,R.style.Theme_Dialog
        )
        builder.setTitle(resources.getString(R.string.dialog_alert_title))
        builder.setMessage(message)
        builder.setIcon(R.drawable.ic_dialog_alert)
        builder.setCancelable(false)
        builder.setPositiveButton(
            resources.getString(R.string.ok)
        ) { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(permission), requestCode)
            }
        }
        builder.setNegativeButton(
            getString(R.string.cancel)
        ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        val networkDialog = builder.create()
        networkDialog.show()
    }
}