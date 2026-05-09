package com.khaizro.notifly.data

import android.content.pm.PackageManager
import com.khaizro.notifly.model.AppInfo

class InstalledAppLoader(private val packageManager: PackageManager) {
    fun load(isFirstRun: Boolean, allowedPackages: Set<String>): List<AppInfo> {
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .map {
                AppInfo(
                    name = it.loadLabel(packageManager).toString(),
                    packageName = it.packageName,
                    icon = runCatching { it.loadIcon(packageManager) }.getOrNull(),
                    isSelected = isFirstRun || allowedPackages.contains(it.packageName),
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}
