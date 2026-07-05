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

## 🛠️ Tech Stack & Technical Architecture

The project is divided into two primary sub-systems: a native Android mobile application and a companion web-based UI prototype simulator:

### 📱 Native Android App (B-Chat)
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose with Material Design 3 (utilizing the custom Ice Latte & The Mint theme)
*   **Navigation**: Type-safe Jetpack Navigation 3 using Kotlin Serialization
*   **Data Persistence**: SQLite database mapped via Room Database for secure, robust local chat logs
*   **Network Layer**: Multi-threaded Bluetooth Classic RFCOMM sockets targeting the standard Serial Port Profile (SPP) UUID (`00001101-0000-1000-8000-00805F9B34FB`)
*   **Architecture Pattern**: MVVM (Model-View-ViewModel) with repositories serving as the single-source-of-truth
*   **System Integration**: Background socket persistence via an Android Foreground Service, ongoing system notifications, and automated runtime permission request flows
*   **Emulator Fallback**: Automatic hardware discovery detection that falls back to simulated device scans and automated chatbot agents inside Android Studio virtual devices

### 💻 Web UI Prototype Simulator
*   **Location**: [ui-prototype/](file:///c:/Users/sahib/AndroidStudioProjects/Bluetooth-Chatting-System/ui-prototype)
*   **Technologies**: Semantic HTML5, Vanilla CSS3 (custom CSS variables matching the app's palette), and Vanilla JavaScript
*   **Features**: Interactive phone screen simulator, 8-screen Figma canvas grid walkthrough, and a responsive component library explorer

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

### Iteration 3
*   **Permission Crash Fix**: Handled startup `SecurityException` thrown when instantiating the RFCOMM Bluetooth listener before Android 12+ runtime permissions are granted.
*   **Auto-start Listening Recovery**: Programmed the server socket listener to automatically bind and start listening as soon as permissions are active (upon initiating a scan/discovery).
*   **CPU / Thread Leak Resolution**: Addressed a busy-wait CPU loop inside `AcceptThread` when server socket creation fails due to missing permissions.

### Iteration 4
*   **Runtime Permission Request Flow**: Implemented automatic runtime permission dialog prompt in `MainActivity.kt` on startup for Bluetooth Scan, Connect, Advertise, coarse/fine Locations, and Notification posts.
*   **Background / Foreground Notification System**: Registered the `"Aether Chat Messages"` Notification Channel and updated `DataRepository` to automatically trigger high-priority, clickable system notifications in the Android system drawer whenever messages are received.

### Iteration 5
*   **Application Renaming**: Renamed the app name resource and wordmark to `"B-Chat"`, replacing all visible instances.
*   **Connection Failure Feedback**: Implemented main-thread Toast error notifications to explicitly alert users when a client socket fails to connect to a nearby device.

### Iteration 6
*   **Foreground Background Listening Service**: Implemented `ChatService.kt` as a Foreground Service of type `connectedDevice` (Android 10+) to keep the socket thread connection active in the background.
*   **Persistent Mesh Status Notification**: Configured the background service to post a sticky ongoing notification (*"B-Chat Mesh Active"*) to prevent the operating system from reclaiming resources.
*   **Application Scope Lifecycle binding**: Managed database and controller instances inside `BChatApplication.kt` and updated `MainActivity.kt` to fetch them from the application class, ensuring socket threads survive activity shutdown.

### Iteration 7
*   **Automatic Reconnection Monitoring**: Implemented a reactive auto-reconnect loop in `ChatViewModel.kt` that monitors the target device's connection status.
*   **Connection Recovery Delay**: If a chat link drops, the loop waits for a 3-second delay and automatically dispatches a new connection attempt to the target device.
*   **Screen Binding**: Wired the monitoring coroutines inside the `LaunchedEffect` in `ChatDetailScreen.kt` to automatically start when viewing a conversation thread.

### Iteration 8
*   **One-Time Onboarding**: Wired a SharedPreferences flag `has_completed_onboarding` in `Navigation.kt` to remember user onboarding state, bypassing it automatically on all subsequent launches.
*   **Message Edit & Delete Functionality**: Added targeting update/delete queries to `MessageDao`, `DataRepository`, and `ChatViewModel`.
*   **Context Dropdown Menu & Dialogs**: Integrated long-press event handlers on message bubbles to show context menus. Added editing dialog boxes and delete confirmations.
*   **UI Alignment Polishing**: Enhanced styling for message balloons, avatar borders, connection dot labels, and top bars for a clean Material 3 look.

### Iteration 9
*   **Editable Local Device Name**: Bound the "My Device" settings card to a reactive `localDeviceName` state flow that reads the phone's default Bluetooth hardware name on startup.
*   **System Name Updates (Over-the-Air visibility)**: Implemented `changeLocalDeviceName` to directly set the hardware's `bluetoothAdapter.name`, meaning nearby scanning devices will discover the phone under this new customized name.
*   **Rename dialog & Pencil edit icon**: Made the settings profile card clickable and added a pencil edit icon, showing a rename AlertDialog to input and save the new name.
*   **Settings Back Button**: Configured an `onBackClick` callback parameter and back navigation button inside the Settings `TopAppBar` to return safely to the conversation history.

### Iteration 10
*   **Automatic Discoverability Prompts**: Triggered a system discoverability request dialog (`ACTION_REQUEST_DISCOVERABLE` for 300 seconds) when starting discovery, making sure the phone's custom name is visible to other devices.
*   **Uncached Real-Time Name Scanning**: Updated the discovery broadcast receiver in `AndroidBluetoothController.kt` to lookup names via `intent.getStringExtra(BluetoothDevice.EXTRA_NAME)` first. This bypasses Android's local OS Bluetooth database cache, allowing newly set custom names to show up instantly on scans.

### Iteration 11
*   **Edge-to-Edge System Bar Redesign**: Enforced transparent status bars with high-contrast system icons inside `MainActivity.kt` and consumed nested window insets inside `Navigation.kt` to prevent draw overlapping.
*   **In-App Bluetooth Enabling**: Implemented custom system-level prompts (`ACTION_REQUEST_ENABLE`) to toggle Bluetooth hardware directly from the B-Chat UI without redirecting to settings.
*   **Chats Screen Overhaul**: Integrated pill-shaped search bars, scan FAB buttons, pulsing Radar empty states, swipe-to-dismiss (Delete / Pin) gestures, initials avatars, and unread badges.
*   **Nearby Screen Overhaul**: Created concentric pulsing waves, breathing skeleton shimmer loaders, dynamic signal/distance indicators, and long-press Device Diagnostics alerts.
*   **Settings Screen Overhaul**: Restructured Profile cards (showing MAC address and discoverability status), organized toggles into preference cards, separated danger zones, and created detailed About sections.

### Iteration 12
*   **Dynamic Peer Name Updates**: Observed connection state changes in the repository to update stored names in the Room database, resolving renamed peer display issues.
*   **Unread Badge Auto-Dismissal**: Added a reactive `isRead` database flag to `MessageEntity` and triggered Room updates to clear unread counts as soon as a conversation details view is opened.
*   **Connection Notifications**: Built push notifications in the repository to alert users when a connection with another node is successfully established.
*   **Online/Offline Badges**: Added status tags directly inside the Chat List view next to peer names indicating their live connection states.

### Iteration 13
*   **Profile Picture Mockup Selection**: Provided 16 HSL background and icon mockups inside `SettingsScreen.kt` for local profile customisation, storing selection preferences inside `SharedPreferences`.

### Iteration 14
*   **BLE Low Energy Discovery Transport**: Migrated B-Chat's core communication backend from classic RFCOMM sockets to Bluetooth Low Energy (BLE) Advertisers, Scanners, and bidirectional GATT Server/Client exchanges, fully eliminating "make discoverable" system popups.
*   **Real-time RSSI signal-to-distance mapping**: Read BLE signal levels directly in `NearbyScreen.kt` for live distance estimation and node sorting.

### Iteration 15
*   **Real-time Name & Avatar Sync (OS Cache Bypass)**: Packaged name and avatar configurations inside a formatted `Name|AvatarId` BLE scan response packet, bypassing Android's stale OS Bluetooth name cache.
*   **Dynamic Room Database Profile Updates**: Triggered automated database updates (`updateSenderProfile()`) upon any connection state changes to immediately propagate peer rename details and custom profile mockup pics across all logged messages.
*   **Rendered Custom Avatars**: Replaced generic initials avatars inside Chat List, Details App Bar, and Scanned Radar items with the peer's actual selected mockup profile avatar.

### Iteration 16
*   **Notification Icon Sizing & Color Corrections**: Replaced the solid app logo with the transparent vector `ic_launcher_foreground` for notification small icons, solving Android's grey-block forcing bug. Converted the full-color `logoo.png` app logo into a Bitmap to populate the notification's `largeIcon`, rendering B-Chat's actual logo in full color.

### Iteration 17
*   **Settings Switch State Persistence**: Saved user toggles for Push Notifications, Message Sound Alerts, and Auto Scan Nodes inside local `SharedPreferences` variables, resolving settings reset issues upon app relaunch or page exit.

### Iteration 18
*   **Settings Preferences Code Integrations**:
    *   **Notification Toggles**: Conditionalised message popups inside `DataRepository.kt` on the `settings_push_notifications` toggle.
    *   **Ringtone Sound Playback**: Added actual ringtone playback on incoming chat packets via the `RingtoneManager` TYPE_NOTIFICATION if `settings_sound_alerts` is enabled.
    *   **Auto Scan Nodes**: Configured background scans to run continuously across screens on app launch/settings toggle if `settings_auto_scan` is active, updating `NearbyScreen.kt` cleanup handlers to match.

### Iteration 19
*   **Message Edits & Deletions BLE Sync**:
    *   **Protocol Control Packets**: Formatted GATT message transmissions under prefixes `__MESSAGE_PAYLOAD__|`, `__MESSAGE_EDIT__|`, and `__MESSAGE_DELETE__|` containing the unique message timestamp.
    *   **Peer Database Synchronization**: Registered Room queries inside `MessageDao.kt` to update or delete message rows matching the peer's timestamp, reactively editing/deleting peer logs in real-time.

### Iteration 20
*   **Trusted Nodes Preferences Integration**: Created a persistent MAC address set inside `SharedPreferences` to track and load trusted nodes. Toggling "Add to trusted nodes" inside the details dialog saves settings instantly and renders a Star icon next to the peer's name on scanned device cards inside `NearbyScreen.kt` for visual confirmation.

### Iteration 21
*   **Dynamic Message Delivery Handshake (ACKs)**:
    *   **Delivery Status Column**: Extended the database model with an `isDelivered` column and updated Room DB to schema version 4.
    *   **GATT Handshake ACKs**: Programmed B-Chat to reply with a `__MESSAGE_ACK__|<timestamp>` packet when a message payload is successfully written.
    *   **Reactive Delivery Ticks**: Displays a grey "Sent" status indicator when sending, changing to a green "Delivered" state upon ACK packet receipt.

### Iteration 22
*   **Real-Time Signal & Distance Updates**: Tied the device info alert dialog to resolve matching items from the active scanned devices stream rather than using a static snapshot, updating RSSI and estimated meters in real-time as physical node distance changes.

### Iteration 23
*   **File & Image Attachment Transfers**:
    *   **Attachment UI & Native Photo Picker**: Integrated picker buttons next to message inputs, launching Android's system visual picker.
    *   **Size Restriction & Conversion**: Downscales selected pictures to 300px max dimensions, compresses to 70% JPEGs, and encodes to Base64 to respect BLE MTU structures.
    *   **BLE Stream Transfer & Cache Reassembly**: Packages files using `__IMAGE_TRANSFER__` GATT payloads. Receiver decodes Base64 data to cache files, updates database logs, and displays layouts dynamically.
    *   **Interactive Mock Bot Transfers**: Enables mockup chat bots to reply to incoming attachments and automatically transmit valid mock Base64 images when the file transfer test is simulated.

### Iteration 24
*   **Dead Boilerplate Code Cleanup**: Completely removed the unused legacy templated classes `MainScreen.kt` and `MainScreenViewModel.kt` under the `ui/main` package. Purged corresponding testing configurations `MainScreenTest.kt` and `MainScreenViewModelTest.kt` to optimize build sizes and eliminate structural noise.

### Iteration 25
*   **Stable Pseudo-Randomized Local MAC Addresses**: Instead of a hardcoded static dummy label, B-Chat now generates a unique and stable local MAC address on the first settings view load, saving it inside `SharedPreferences` to simulate a real hardware interface.

### Iteration 26
*   **Cleaned Web Prototype Assets**: Completely removed the legacy `ui-prototype` folder containing mock web HTML/JS/CSS source code files to reduce repository weight and maintain a clean production Android structure.

### Iteration 27
*   **Full-Screen Image Attachment Preview**: Configured image attachments inside chat conversation threads to be interactive. Tapping on an image view opens a beautiful, high-fidelity zoomed image preview dialog.

### Iteration 28
*   **Triple-Redundant User Profile Sync**:
    *   **Custom Name Sandboxing**: Saves setting names directly in SharedPreferences, decoupled from hardware security/permission states on app startup.
    *   **Advertisement Parsing**: Splitting raw scan names in BLE scanners extracts and renders clean display names and selected avatars in scanned Radar lists.
    *   **Embedded Payload Metadata**: Outbound chat messages and image attachments now carry sender profile details. Receiver decodes them on packet arrival, updating memory state and database mappings.

### Iteration 29
*   **BLE GATT Packet Chunking & Reassembly**: Implemented a transparent segmentation layer inside BLE controller pipelines. Payloads larger than 400 characters are automatically split and transmitted sequentially with structured frames (`__CHUNK_START__`, `__CHUNK_DATA__`, `__CHUNK_END__`) and a safe 60ms delay. The receiving end buffers, reassembles, and processes payloads, bypassing BLE MTU limits to enable large texts and images to deliver reliably.

### Iteration 30
*   **Active Chat Read Status Sync & Live Top-Bar Naming**:
    *   **Real-time Read Tracking**: Keeps track of the active chat address via a `DisposableEffect` mapping. Incoming messages received while actively on the chat screen are marked as read instantly, preventing unread indicator flashing on screen exit.
    *   **Live Top-Bar Resolution**: Binds the detail screen top bar to live-resolve and display the peer's display name from the active connected profile state in real-time.

### Iteration 31
*   **Empty BLE GATT Write Filtering & Case Normalization**:
    *   **Empty Packet Discarding**: Added blank checking in the repository flow (`messageText.trim().isBlank()`) to discard phantom empty writes sent by the Android Bluetooth stack upon GATT descriptor updates, preventing empty grey bubble rendering.
    *   **MAC Case Normalization**: Forced BLE connection addresses to uppercase at the controller boundary. This prevents case mismatch conflicts (e.g., lower vs uppercase MAC strings) from bypassing SQLite Room database updates, restoring reliable real-time display name and avatar synchronization.

### Iteration 32
*   **BLE GATT MTU Negotiation & Dynamic Write Limits**:
    *   **MTU Negotiation**: Implemented `gatt.requestMtu(517)` immediately upon client connection, initiating MTU negotiation to increase the payload size beyond the default 23-byte BLE limit.
    *   **Dynamic Limits**: Programmed B-Chat to track negotiated MTU via `onMtuChanged` callbacks for both server and client sides, updating the dynamic limit `currentWriteLimit = mtu - 3` for the chunking engine, enabling packets (profile sync, large messages) to transmit without truncation.













