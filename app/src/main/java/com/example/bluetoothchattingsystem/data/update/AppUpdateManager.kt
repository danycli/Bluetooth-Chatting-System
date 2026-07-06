package com.example.bluetoothchattingsystem.data.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppUpdateManager(
    private val context: Context,
    private val primarySource: UpdateSource = GitHubReleaseUpdateSource(),
    private val fallbackSource: UpdateSource = GitHubFallbackUpdateSource()
) {

    private val prefs = context.getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_CHECK_TIME = "last_update_check_time"
        private const val KEY_CACHED_VERSION_NAME = "cached_update_version_name"
        private const val KEY_CACHED_VERSION_CODE = "cached_update_version_code"
        private const val KEY_CACHED_RELEASE_NOTES = "cached_update_release_notes"
        private const val KEY_CACHED_RELEASE_DATE = "cached_update_release_date"
        private const val KEY_CACHED_DOWNLOAD_URL = "cached_update_download_url"
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    suspend fun checkForUpdates(force: Boolean = false): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        val lastCheckTime = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)

        if (!force && (currentTime - lastCheckTime < CACHE_DURATION_MS)) {
            val cachedName = prefs.getString(KEY_CACHED_VERSION_NAME, null)
            if (cachedName != null) {
                val cachedCode = prefs.getInt(KEY_CACHED_VERSION_CODE, 0)
                val cachedNotes = prefs.getString(KEY_CACHED_RELEASE_NOTES, "") ?: ""
                val cachedDate = prefs.getString(KEY_CACHED_RELEASE_DATE, "") ?: ""
                val cachedUrl = prefs.getString(KEY_CACHED_DOWNLOAD_URL, "") ?: ""

                val cachedInfo = AppUpdateInfo(cachedName, cachedCode, cachedNotes, cachedDate, cachedUrl)
                if (isNewerThanInstalled(cachedInfo.versionName, cachedInfo.versionCode)) {
                    Log.d("AppUpdateManager", "Returning cached update info: ${cachedInfo.versionName}")
                    return@withContext cachedInfo
                }
            }
            Log.d("AppUpdateManager", "Cached update check is less than 24 hours old. Skipping.")
            return@withContext null
        }

        Log.d("AppUpdateManager", "Performing active update check (force=$force)...")

        var latestInfo = primarySource.fetchLatestVersion()
        if (latestInfo == null) {
            Log.w("AppUpdateManager", "Primary source failed. Trying fallback source...")
            latestInfo = fallbackSource.fetchLatestVersion()
        }

        prefs.edit().putLong(KEY_LAST_CHECK_TIME, currentTime).apply()

        if (latestInfo != null) {
            if (isNewerThanInstalled(latestInfo.versionName, latestInfo.versionCode)) {
                prefs.edit().apply {
                    putString(KEY_CACHED_VERSION_NAME, latestInfo.versionName)
                    putInt(KEY_CACHED_VERSION_CODE, latestInfo.versionCode)
                    putString(KEY_CACHED_RELEASE_NOTES, latestInfo.releaseNotes)
                    putString(KEY_CACHED_RELEASE_DATE, latestInfo.releaseDate)
                    putString(KEY_CACHED_DOWNLOAD_URL, latestInfo.downloadUrl)
                }.apply()
                return@withContext latestInfo
            }
        }

        prefs.edit().apply {
            remove(KEY_CACHED_VERSION_NAME)
            remove(KEY_CACHED_VERSION_CODE)
            remove(KEY_CACHED_RELEASE_NOTES)
            remove(KEY_CACHED_RELEASE_DATE)
            remove(KEY_CACHED_DOWNLOAD_URL)
        }.apply()

        return@withContext null
    }

    fun getInstalledVersionName(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    fun getInstalledVersionCode(): Int {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= 28) {
                packageInfo.longVersionCode.toInt()
            } else {
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    private fun isNewerThanInstalled(latestName: String, latestCode: Int): Boolean {
        val installedName = getInstalledVersionName()
        val installedCode = getInstalledVersionCode()

        val isSemanticNewer = isSemanticUpdateAvailable(installedName, latestName)
        if (isSemanticNewer) return true

        if (latestName.trim().removePrefix("v") == installedName.trim().removePrefix("v")) {
            return latestCode > installedCode
        }

        return false
    }

    fun isSemanticUpdateAvailable(installed: String, latest: String): Boolean {
        val instClean = installed.trim().removePrefix("v").split("-").firstOrNull() ?: ""
        val latClean = latest.trim().removePrefix("v").split("-").firstOrNull() ?: ""

        val instParts = instClean.split(".").mapNotNull { it.toIntOrNull() }
        val latParts = latClean.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(instParts.size, latParts.size)
        for (i in 0 until maxLength) {
            val instVal = instParts.getOrNull(i) ?: 0
            val latVal = latParts.getOrNull(i) ?: 0
            if (latVal > instVal) return true
            if (instVal > latVal) return false
        }
        return false
    }
}
