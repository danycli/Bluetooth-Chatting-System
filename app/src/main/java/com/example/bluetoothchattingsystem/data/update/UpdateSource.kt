package com.example.bluetoothchattingsystem.data.update

data class AppUpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val releaseNotes: String,
    val releaseDate: String,
    val downloadUrl: String
)

interface UpdateSource {
    suspend fun fetchLatestVersion(): AppUpdateInfo?
}
