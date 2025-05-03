package com.example.taskmanagement.interactors

import android.text.TextUtils
import androidx.databinding.ObservableField

class ObservableString : ObservableField<String> {
    constructor(s: String?) {
        set(s)
    }

    constructor()

    override fun get(): String {
        return (if (super.get() == null) "" else super.get()!!)
    }

    val trimmed: String
        /**
         * Method for get trimmed data
         *
         * @return trimmed data
         */
        get() {
            val stringData = get()
            return (if (!TextUtils.isEmpty(stringData) && !TextUtils.isEmpty(
                    stringData.trim { it <= ' ' })
            ) stringData.trim { it <= ' ' } else "")
        }

    val trimmedLength: Int
        /**
         * Get String length
         *
         * @return length
         */
        get() {
            val trimmedData = trimmed
            return (if (!TextUtils.isEmpty(trimmedData)) trimmedData.length else 0)
        }

    val isEmptyData: Boolean
        /**
         * Check is Empty String
         *
         * @return is Empty
         */
        get() = (TextUtils.isEmpty(trimmed))
}
