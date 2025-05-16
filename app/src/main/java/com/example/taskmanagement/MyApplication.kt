package com.example.taskmanagement

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions


class MyApplication : Application() {

    lateinit var localBroadcastManager: LocalBroadcastManager

    override fun onCreate() {
        super.onCreate()
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
    }

    /**
     * Checks Internet Connectivity.
     *
     * @return returns true if connected else false.
     */
    fun isInternetConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    fun glideBaseOptions(drawable: Int): RequestOptions {
        return RequestOptions()
            .placeholder(drawable)
            .error(drawable)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
    }

    fun glideCenterCropOptions(drawable: Int): RequestOptions {
        return glideBaseOptions(drawable).centerCrop()
    }

    fun glideOptionRoundCorner(radius: Int, placeholder: Int): RequestOptions {
        return glideCenterCropOptions(placeholder)
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(radius)))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
    }


}
