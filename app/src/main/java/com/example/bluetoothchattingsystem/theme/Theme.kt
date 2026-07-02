package com.example.bluetoothchattingsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CustomColorScheme = lightColorScheme(
    primary = TheMint,
    onPrimary = Color.White,
    primaryContainer = MintLight,
    onPrimaryContainer = MintDark,
    secondary = LatteDark,
    onSecondary = NearBlack,
    background = SoftWhite,
    onBackground = NearBlack,
    surface = IceLatte,
    onSurface = NearBlack,
    surfaceVariant = Color.White,
    onSurfaceVariant = NearBlack,
    outline = LatteDark,
    error = AlertRed,
    onError = Color.White
)

@Composable
fun BluetoothChattingSystemTheme(
    darkTheme: Boolean = false, // Keep uniform light mode to preserve Ice Latte theme
    dynamicColor: Boolean = false, // Disable dynamic colors to prevent palette deviation
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustomColorScheme,
        typography = Typography,
        content = content
    )
}
