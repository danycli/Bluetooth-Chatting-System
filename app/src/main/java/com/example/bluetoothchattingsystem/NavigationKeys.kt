package com.example.bluetoothchattingsystem

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Splash : NavKey
@Serializable data object Onboarding : NavKey
@Serializable data object Nearby : NavKey
@Serializable data object ChatList : NavKey
@Serializable data object Settings : NavKey

@Serializable
data class ChatDetail(
    val peerAddress: String,
    val peerName: String
) : NavKey
