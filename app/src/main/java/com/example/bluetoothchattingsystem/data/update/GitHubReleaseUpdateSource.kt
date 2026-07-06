package com.example.bluetoothchattingsystem.data.update

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GitHubReleaseUpdateSource : UpdateSource {

    override suspend fun fetchLatestVersion(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://api.github.com/repos/danycli/Bluetooth-Chatting-System/releases/latest")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "bchat-android-update")
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val json = JSONObject(response.toString())
                val tagName = json.optString("tag_name", "")
                val versionName = tagName.removePrefix("v").trim()
                val releaseNotes = json.optString("body", "No release notes provided.")
                val releaseDateRaw = json.optString("published_at", "")
                val releaseDate = if (releaseDateRaw.length >= 10) releaseDateRaw.substring(0, 10) else releaseDateRaw
                val downloadUrl = json.optString("html_url", "https://github.com/danycli/Bluetooth-Chatting-System/releases")

                if (versionName.isNotEmpty()) {
                    AppUpdateInfo(
                        versionName = versionName,
                        versionCode = 0,
                        releaseNotes = releaseNotes,
                        releaseDate = releaseDate,
                        downloadUrl = downloadUrl
                    )
                } else null
            } else {
                Log.w("GitHubReleaseUpdate", "Failed to fetch update, response code: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("GitHubReleaseUpdate", "Error fetching latest release from GitHub", e)
            null
        } finally {
            connection?.disconnect()
        }
    }
}
