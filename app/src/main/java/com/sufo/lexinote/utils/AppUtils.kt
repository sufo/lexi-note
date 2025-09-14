package com.sufo.lexinote.utils

/**
 * Created by sufo on 2025/8/14.
 *
 */
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

object AppUtils {

    /**
     * 获取应用的 versionName (例如 "1.0.2")
     */
    fun getAppVersionName(context: Context): String {
        return try {
            getPackageInfo(context).versionName
        } catch (e: Exception) {
            // 如果出现异常，返回一个默认值
            "N/A"
        }.toString()
    }

    /**
     * 获取应用的 versionCode (例如 3)
     */
    fun getAppVersionCode(context: Context): Long {
        return try {
            val packageInfo = getPackageInfo(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            -1L
        }
    }

    private fun getPackageInfo(context: Context): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    }
}
