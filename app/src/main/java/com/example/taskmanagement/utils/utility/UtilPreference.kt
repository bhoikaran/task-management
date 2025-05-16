package com.example.taskmanagement.utils.utility


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.taskmanagement.R


class UtilPreference(context: Context) {
    private val mPreferences: SharedPreferences? =
        context.getSharedPreferences(
            context.resources.getString(
                R.string
                    .prefApplication
            ), 0
        )
    private val mContext: Context = context

    fun getBoolean(prefName: Int): Boolean {
        return mPreferences!!.getBoolean(mContext.resources.getString(prefName), false)
    }

    fun getBooleanDefault(prefName: Int, defaultValue: Boolean): Boolean {
        return mPreferences!!.getBoolean(mContext.resources.getString(prefName), defaultValue)
    }

    fun setBoolean(prefName: Int, value: Boolean) {
        mPreferences!!.edit() { putBoolean(mContext.resources.getString(prefName), value) }
    }

    fun getString(prefName: Int): String {
        return mPreferences!!.getString(mContext.resources.getString(prefName), "")!!
    }

    fun getStringDefault(prefName: Int, defaultValue: String?): String? {
        return mPreferences!!.getString(mContext.resources.getString(prefName), defaultValue)
    }

    fun setString(prefName: Int, value: String?) {
        mPreferences!!.edit() { putString(mContext.resources.getString(prefName), value) }
    }

    fun getInteger(prefName: Int): Int {
        return mPreferences!!.getInt(mContext.resources.getString(prefName), 0)
    }

    fun getIntegerDefault(prefName: Int, defaultValue: Int): Int {
        return mPreferences!!.getInt(mContext.resources.getString(prefName), defaultValue)
    }

    fun setInteger(prefName: Int, value: Int) {
        mPreferences!!.edit() { putInt(mContext.resources.getString(prefName), value) }
    }


    fun getLong(prefName: Int): Long {
        return mPreferences!!.getLong(mContext.resources.getString(prefName), 0L)
    }

    fun getLongDefault(prefName: Int, defaultValue: Long): Long {
        return mPreferences!!.getLong(mContext.resources.getString(prefName), defaultValue)
    }

    fun setLong(prefName: Int, value: Long) {
        mPreferences!!.edit() { putLong(mContext.resources.getString(prefName), value) }
    }

    fun clear() {
        mPreferences?.edit() {
                remove(mContext.resources.getString(R.string.prefUserId))
                remove(mContext.resources.getString(R.string.prefUserName))
                remove(mContext.resources.getString(R.string.prefUserRole))

    //            editor.remove(mContext.resources.getString(R.string.prefUserToken))
    //            editor.remove(mContext.resources.getString(R.string.prefUserName))
    //            editor.remove(mContext.resources.getString(R.string.prefFirstName))
    //            editor.remove(mContext.resources.getString(R.string.prefMiddleName))
    //            editor.remove(mContext.resources.getString(R.string.prefLastName))
    //            editor.remove(mContext.resources.getString(R.string.prefUserEmail))
    //            editor.remove(mContext.resources.getString(R.string.prefUserPhoto))
    //            editor.remove(mContext.resources.getString(R.string.prefIsCaptainPlayershow))
    //            editor.remove(mContext.resources.getString(R.string.prefIsCaptain))
    //            editor.remove(mContext.resources.getString(R.string.prefSchoolid))
    //            editor.remove(mContext.resources.getString(R.string.prefTeamId))
    //            editor.remove(mContext.resources.getString(R.string.prefCoachId))
    //            editor.putBoolean(mContext.resources.getString(R.string.prefIsUserLogIn), false)
    //            editor.remove(mContext.resources.getString(R.string.prefDistrictId))
    //            editor.remove(mContext.resources.getString(R.string.prefDistrictName))
    //            editor.remove(mContext.resources.getString(R.string.prefTalukaId))
    //            editor.remove(mContext.resources.getString(R.string.prefTalukaName))
    //            editor.remove(mContext.resources.getString(R.string.prefVillageId))
    //            editor.remove(mContext.resources.getString(R.string.prefVillageName))
        }
    }
}
