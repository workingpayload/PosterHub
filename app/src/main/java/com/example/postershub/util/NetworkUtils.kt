package com.example.postershub.util

import android.content.Context
import android.net.ConnectivityManager

/** True on mobile data / hotspot / any connection Android flags as metered. */
fun Context.isMeteredConnection(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    return cm.isActiveNetworkMetered
}
