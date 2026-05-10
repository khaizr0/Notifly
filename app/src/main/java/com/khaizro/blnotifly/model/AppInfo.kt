package com.khaizro.blnotifly.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    var isSelected: Boolean,
)
