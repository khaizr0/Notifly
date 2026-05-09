package com.khaizro.notifly.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    var isSelected: Boolean,
)
