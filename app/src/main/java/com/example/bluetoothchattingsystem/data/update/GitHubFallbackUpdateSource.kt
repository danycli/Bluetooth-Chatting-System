package com.example.bluetoothchattingsystem.data.update

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GitHubFallbackUpdateSource : UpdateSource {

    override suspend fun fetchLatestVersion(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://raw.githubusercontent.com/danycli/Bluetooth-Chatting-System/main/version.json")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "bchat-android-update")
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
                val versionName = json.optString("versionName", "").trim()
                val versionCode = json.optInt("versionCode", 0)
                val releaseNotes = json.optString("releaseNotes", "No release notes provided.")
                val releaseDate = json.optString("releaseDate", "")
                val downloadUrl = json.optString("downloadUrl", "https://github.com/danycli/Bluetooth-Chatting-System/releases")

                if (versionName.isNotEmpty()) {
                    AppUpdateInfo(
                        versionName = versionName,
                        versionCode = versionCode,
                        releaseNotes = releaseNotes,
                        releaseDate = releaseDate,
                        downloadUrl = downloadUrl
                    )
                } else null
            } else {
                Log.w("GitHubFallbackUpdate", "Failed to fetch fallback version, response code: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("GitHubFallbackUpdate", "Error fetching fallback version from raw GitHub link", e)
            null
        } finally {
            connection?.disconnect()
        }
    }
}
