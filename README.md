# Aether - Offline Bluetooth Messenger

A native Android application that enables nearby devices to discover each other and chat directly over Bluetooth, with zero internet or server dependency. Designed as a "disaster-ready WhatsApp" — utilitarian, trustworthy, and visually beautiful.

---

## 🎨 Visual Identity & Color Palette

The user interface adheres to a strict design aesthetic with high legibility for outdoor use cases:

*   **Ice Latte (`#E4DDD3`)**: Base background, surface cards, and inactive message bubbles.
*   **The Mint (`#00A198`)**: Primary action accent, active states, radar sweeps, and sent message bubbles.
*   **Mint Dark (`#007A73`)**: Active pressed states.
*   **Mint Light (`#D6F0EE`)**: Connection indicator highlights and badge backgrounds.
*   **Latte Dark (`#B8AFA1`)**: Secondary borders, secondary text, and disabled states.
*   **Near Black (`#2A2A28`)**: High-contrast warm text.

---

## 🏗️ Technical Architecture

Built following modern Android development guidelines:

*   **Language & UI**: Kotlin & Jetpack Compose (Material Design 3).
*   **Navigation**: Type-safe Jetpack Navigation3 using Kotlin Serialization.
*   **Data Persistence**: Room Database SQLite mapping for secure local conversation logs.
*   **Network Layer**: Multi-threaded Bluetooth Classic RFCOMM sockets on standard SPP UUID (`00001101-0000-1000-8000-00805F9B34FB`).
*   **MVVM Pattern**: ViewModels manage UI states and communicate with a single-source-of-truth repository.
*   **Emulator Fallback**: Automatic device detection switching to simulated discovery scans and automated chat bots when running inside Android Studio virtual devices.

---

## 📂 Project Structure

```
app/src/main/java/com/example/bluetoothchattingsystem/
│
├── data/
│   ├── DataRepository.kt           # Coordinates DB writes & Bluetooth streams
│   ├── local/                      # SQLite Room Data Layer
│   │   ├── ChatDatabase.kt
│   │   ├── MessageDao.kt
│   │   └── MessageEntity.kt
│   └── bluetooth/                  # RFCOMM socket thread management
│       ├── BluetoothController.kt
│       ├── AndroidBluetoothController.kt
│       ├── MockBluetoothController.kt
│       └── BluetoothDeviceDomain.kt
│
├── ui/
│   ├── BluetoothViewModel.kt       # Discoverability scans & settings
│   ├── ChatViewModel.kt            # Chat thread logs & text routing
│   └── screens/                    # Compose Screens
│       ├── SplashScreen.kt
│       ├── OnboardingScreen.kt
│       ├── NearbyScreen.kt
│       ├── ChatListScreen.kt
│       ├── ChatDetailScreen.kt
│       ├── SettingsScreen.kt
│       └── PairingBottomSheet.kt
│
├── theme/                          # Ice Latte & The Mint theme rules
│   ├── Color.kt
│   ├── Type.kt
│   └── Theme.kt
│
├── NavigationKeys.kt               # Serializable Navigation3 route definitions
├── Navigation.kt                   # Bottom Scaffold Navigation Display mapping
└── MainActivity.kt                 # Launcher class (handles emulator detection)
```

---

## 🚀 Building & Running

### Using Android Studio
1.  Open Android Studio and click **File -> Open**. Select the `Bluetooth-Chatting-System` directory.
2.  Once opened, click **Sync Project with Gradle Files** in the banner or toolbar.
3.  Go to **File -> Reload All from Disk** if newly created files are missing from the package trees.
4.  Run the application on a connected device or virtual device by clicking the green **Run** button.

### Using Antigravity CLI / Terminal
Compile the debug build:
```powershell
.\gradlew assembleDebug
```

Deploy and launch on a connected device:
```powershell
android run --device=33071JEHN06517 --apks=app/build/outputs/apk/debug/app-debug.apk
```

---

## 📈 Changelog

### Iteration 1
*   Built core Kotlin codebase: Room Database, Bluetooth Classic threads, MVVM ViewModels, Compose screens, and Navigation display.
*   Enforced the custom Ice Latte & The Mint theme.

### Iteration 2
*   **App Logo integration**: Set `logoo.png` as the Android Launcher icon and updated `SplashScreen.kt` to draw the app logo dynamically inside pulsing concentric rings.

