package com.example.taskmanagement.utils.utility

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralItemListener
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.utils.Util
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CustomBindingAdapter {

    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, url: String?) {
        Glide.with(view.context).load(url).into(view)
    }

    @JvmStatic
    @BindingAdapter(value = ["setImagePath", "setPlaceHolder"], requireAll = false)
    fun setImagePath(
        view: ImageView,
        source: String?,
        placeholder: Drawable?
    ) {
        if (!source.isNullOrEmpty()) {
            Glide.with(view.context)
                .load(source)
                .placeholder(placeholder ?: view.context.getDrawable(R.drawable.app_icon))
                .into(view)
        } else {
            view.setImageResource(R.drawable.app_icon)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["showSnackBarInt", "showSnackBarString"], requireAll = false)
    fun showSnackBar(
        viewLayout: View,
        snackMessageInt: ObservableInt?,
        snackMessageString: ObservableString?
    ) {
        var message = ""
        if (snackMessageString != null && !TextUtils.isEmpty(snackMessageString.trimmed)) {
            message = snackMessageString.trimmed
            snackMessageString.set("")
        } else if (snackMessageInt != null && snackMessageInt.get() != 0) {
            message = viewLayout.resources.getString(snackMessageInt.get())
            snackMessageInt.set(0)
        }

        if (!TextUtils.isEmpty(message)) {
            Util().showSnackBar(viewLayout, message)
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["CircleImagePath", "CirclePlaceHolder"])
    fun setCircleImagePath(
        view: ImageView,
        source: String?,
        placeholder: Drawable?
    ) {
        Glide.with(view.context)
            .load(source)
            .circleCrop()
            .placeholder(placeholder)
            .error(placeholder)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("formattedDate")
    fun setFormattedDate(textView: TextView, inputDate: String?) {
        inputDate?.let {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH)
            val formatted = try {
                val date = inputFormat.parse(it)
                date?.let { outputFormat.format(it) } ?: ""
            } catch (e: Exception) {
                ""
            }
            textView.text = formatted
        } ?: run {
            textView.text = ""
        }
    }

    @JvmStatic
    @BindingAdapter("addHtmlReqTextHint")
    fun addHtmlReqTextHint(textView: TextView, text: String?) {
        val modifiedText = "${text ?: ""} ${textView.resources.getString(R.string.text_mandatory)}"
        textView.text = Html.fromHtml(modifiedText, Html.FROM_HTML_MODE_COMPACT)
    }

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
}