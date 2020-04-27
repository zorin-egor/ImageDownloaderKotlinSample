package com.sample.imagedownloader.manager

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkManager {

    fun isOnline(context: Context): Boolean {
        return (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).let { manager ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = manager.activeNetwork ?: return false
                val networkCapabilities = manager.getNetworkCapabilities(activeNetwork) ?: return false
                when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } else {
                manager.activeNetworkInfo.let {
                    when (it.type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }
    }

}