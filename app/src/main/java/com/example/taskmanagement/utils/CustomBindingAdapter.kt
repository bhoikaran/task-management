package com.example.taskmanagement.utils

import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.taskmanagement.R
import com.example.taskmanagement.model.Status
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CustomBindingAdapter {


    @JvmStatic
    @BindingAdapter("formattedDate")
    fun setFormattedDate(textView: TextView, timestamp: Long?) {
        timestamp?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = sdf.format(Date(it))
            textView.text = formattedDate
        } ?: run {
            textView.text = "-"
        }
    }

    /*@JvmStatic
    @BindingAdapter("statusColor")
    fun setStatusColor(view: LinearLayout, status: Status) {
        val colorRes = when (status) {

            Status.IN_PROGRESS -> R.color.status_in_progress
            Status.MARKED_DONE -> R.color.status_pending
            Status.VERIFIED_DONE -> R.color.status_completed
            else -> R.color.status_unknown
        }
        view.setBackgroundColor(ContextCompat.getColor(view.context, colorRes))
    }*/

    @JvmStatic
    @BindingAdapter("statusBackGround")
    fun setStatusBackGround(view: LinearLayout, status: Status) {
        val statusBg = when (status) {

            Status.IN_PROGRESS -> R.drawable.bg_in_progress
            Status.MARKED_DONE -> R.drawable.bg_pending
            Status.VERIFIED_DONE -> R.drawable.bg_completed

        }
        view.background = ContextCompat.getDrawable(view.context, statusBg)
    }


    @JvmStatic
    @BindingAdapter("statusText")
    fun setStatusText(view: TextView, status: Status) {
        val statusText = when (status) {
            Status.IN_PROGRESS -> R.string.status_in_progress
            Status.MARKED_DONE -> R.string.status_mark_done
            Status.VERIFIED_DONE -> R.string.status_verified_done
        }
        view.setText(statusText)
    }

    /* @JvmStatic
     @BindingAdapter(value = ["remarkWithIcon", "remarkIcon"], requireAll = false)
     fun setRemarkWithIcon(view: TextView, remark: String?, drawable: Drawable?) {
         if (drawable == null) {
             view.text = remark.orEmpty()
             return
         }

         drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

         val spannable = SpannableString("icon ${remark.orEmpty()}")
         spannable.setSpan(
             ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM),
             0,
             4, // length of "icon"
             Spannable.SPAN_INCLUSIVE_EXCLUSIVE
         )

         view.text = spannable
     }

 */


    @JvmStatic
    @BindingAdapter(value = ["remarkText", "remarkIconRes"], requireAll = false)
    fun setRemarkWithIcon(
        textView: TextView,
        remark: String?,
        @DrawableRes iconRes: Int?
    ) {
        // Set the remark text
        textView.text = remark.orEmpty()

        // Load the drawable (or clear if null/0)
        val drawable = iconRes
            ?.takeIf { it != 0 }
            ?.let { ContextCompat.getDrawable(textView.context, it) }
        if (drawable == null) {
            textView.setCompoundDrawablesRelative(null, null, null, null)
            return
        }

        // Measure the text height (including padding) in pixels
        // Use lineHeight if you want exact line spacing, or paint.textSize for font size
        val iconSize = textView.lineHeight

        // Apply bounds so the drawable scales to the text height
        drawable.setBounds(0, 0, iconSize, iconSize)

        // Set as the start (left) compound drawable
        textView.setCompoundDrawablesRelative(drawable, null, null, null)

        // Optional: small padding between icon & text (e.g. 4dp)
        val paddingPx = (4 * textView.context.resources.displayMetrics.density).toInt()
        textView.compoundDrawablePadding = paddingPx
    }

}