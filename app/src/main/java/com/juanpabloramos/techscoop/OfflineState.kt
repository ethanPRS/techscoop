package com.juanpabloramos.techscoop

import android.content.Context

object OfflineState {
    fun isActive(context: Context): Boolean = !NetworkUtils.isOnline(context)
}
