# 🌌 AETHER MUSIC — Cinematic Privacy-First Music Player

**AETHER MUSIC** is a native Android music player crafted with Kotlin, Jetpack Compose, Material Design 3, and AndroidX Media3 ExoPlayer. It features an atmospheric cosmic dark aesthetic, real on-device local playback, and free, legal streaming from open catalogs.

---

## 🌟 Key Features

### 🎧 Real Audio Playback & Media3 Engine
- **AndroidX Media3 ExoPlayer (v1.5.1)**: Full hardware-accelerated playback pipeline.
- **Background Playback & MediaSession**: Lock screen controls, notifications with artwork, Bluetooth playback support, and headphone unplug pause handling.
- **Cosmic Equalizer & Bass Boost**: Hardware audio effect routing with custom presets (*Flat*, *Cosmic Bass*, *Stellar Vocal*, *Cyber Electronic*, *Deep Nebula*) and bass boost slider.
- **Sleep Timer**: Auto-pause sleep timer with live countdown display (15, 30, 45, 60 minutes).
- **Playback Controls**: Seek scrubber, shuffle mode, repeat modes (Off / All / One), variable playback speed (0.75x – 2.0x).
- **Queue Management**: Play next, add to queue, reorder, remove individual items, or clear queue.

### 🌐 Free & Legal Streaming Sources (No API Keys, Zero Ads)
| Source | Type | Catalog Size | Quality | Legal / Licensing Status |
|---|---|---|---|---|
| **Local Audio** | Device Files (MediaStore + SAF) | User's collection | Lossless / MP3 / FLAC / AAC | User-owned |
| **Jamendo** | Free Legal Music API | 600,000+ tracks | MP3 192–320kbps | Creative Commons licenses |
| **Internet Archive** | Public Domain & Live Tapes | Millions of recordings | MP3 / VBR | Public Domain / CC |
| **Online Stream** | Invidious & Piped Audio Feeds | Multi-instance failover | Opus 160kbps / M4A | Public audio streams |
| **Previews** | Official Previews | Select catalog | AAC | 30-sec official samples |

### 🔒 Privacy-First Architecture
- **Zero Tracking**: No analytics SDKs, advertising IDs, or telemetry.
- **Guest-First**: No account creation, login, or password required.
- **Local Persistence**: Playlists, favorites, settings, and listening history remain strictly on the device.
- **Scoped Storage**: Reads only user-authorized media files without uploading metadata.

---

## 📱 Architecture

```
com.example/
├── AetherApp.kt                // Notification channel & application lifecycle
├── MainActivity.kt             // Edge-to-edge Compose host activity
├── data/
│   ├── model/
│   │   └── MusicModels.kt      // Song, AudioSource, Playlist, RepeatMode
│   ├── remote/
│   │   └── NetworkClient.kt    // OkHttp client with timeouts & JSON parsing
│   ├── provider/
│   │   ├── AudioSourceProvider.kt      // Modular provider interface
│   │   ├── JamendoSourceProvider.kt    // Jamendo CC music adapter
│   │   ├── InternetArchiveSourceProvider.kt // Internet Archive public domain
│   │   ├── OnlineStreamSourceProvider.kt   // Invidious/Piped audio feeds
│   │   ├── LocalAudioSourceProvider.kt // MediaStore & SAF audio scanner
│   │   └── ITunesPreviewSourceProvider.kt // 30-second official samples
│   └── repository/
│       └── MusicRepository.kt  // Cache, favorites, playlists, and history
├── playback/
│   ├── AudioFxManager.kt       // Android Equalizer & BassBoost effects
│   ├── PlaybackManager.kt      // Media3 ExoPlayer wrapper, queue & timers
│   └── MediaPlaybackService.kt // Android MediaSession foreground service
└── ui/
    ├── MainViewModel.kt        // Architecture state coordinator
    ├── MainApp.kt              // Root Compose container with navigation
    ├── components/
    │   ├── CosmicBackground.kt // Animated nebula aurora canvas
    │   ├── MiniPlayer.kt       // Collapsible floating bar with spinning vinyl
    │   ├── FullPlayerSheet.kt  // Immersive vinyl player with reactive aura rings
    │   ├── PlayerDialogs.kt    // Equalizer, Sleep Timer, Playlist & Privacy dialogs
    │   └── TrackItemView.kt    // Reusable track row with artwork and badges
    ├── screens/
    │   ├── HomeScreen.kt       // Editorial legal catalog, recently played, favorites
    │   ├── SearchScreen.kt     // Multi-source search & voice speech-to-text
    │   ├── LibraryScreen.kt    // Local music scanner, artist & album groupings
    │   ├── PlaylistsScreen.kt  // Playlist creator, track viewer, playlist manager
    │   ├── SettingsScreen.kt   // Audio engine specs, privacy disclosure & cache
    │   └── OnboardingScreen.kt // First-launch cinematic onboarding
    └── theme/
        ├── Color.kt            // Obsidian void, Electric Cyan, Plasma Violet
        └── Theme.kt            // Dark Material 3 theme configuration
```

---

## 🛠️ Building & Running

### Prerequisites
- JDK 11 or higher
- Android SDK 36 (minSdk 24)

### Build Debug APK
```bash
gradle assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk` and copied to `aether-music.apk` in the root directory.

### Run Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```
