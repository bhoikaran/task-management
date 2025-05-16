package com.example.taskmanagement.utils



import android.annotation.SuppressLint
import android.text.Html
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar

open class Util {
    private val mIsAlertDialogShow = false
    var symbols: CharArray =
        "0123456789QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm".toCharArray()

    /**
     * Shows the snackbar
     *
     * @param toastMessage Message for the snackbar
     * @param viewLayout   View layout along which snackbar will be shown
     */

    @SuppressLint("ShowToast")
    open fun showSnackBar(viewLayout: View, toastMessage: String) {
        try {
            if (toastMessage != null) {
                Snackbar.make(viewLayout, Html.fromHtml(toastMessage), Snackbar.LENGTH_LONG)
                    .setTextMaxLines(5).show()
            }
        } catch (exception: Exception) {

            Log.e("log", exception.toString())
        }
    }


}