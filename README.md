<div align="center">

# 🟩 Cubiq Launcher
### The Premier Minecraft: Java Edition Launcher for Android

[![GitHub Release](https://img.shields.io/github/v/release/VivxKumar07/Cubiq-Launcher?style=for-the-badge&color=55FF55&logo=github)](https://github.com/VivxKumar07/Cubiq-Launcher/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/VivxKumar07/Cubiq-Launcher)
[![Minecraft](https://img.shields.io/badge/Minecraft-Java_Edition_1.7--1.21+-3C8527?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.minecraft.net)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge)](LICENSE)

<br/>

**Official Website of Cubiq Launcher :** https://cubiq.antideploy.app/

**Cubiq Launcher** is a modern, high-performance, open-source **Minecraft: Java Edition launcher for Android** designed from the ground up with an authentic, pixel-accurate PC Minecraft Launcher interface built natively in **Jetpack Compose**.

Experience seamless desktop Minecraft Java on mobile with hardware-accelerated Vulkan & OpenGL ES renderers, customizable touch controls, physical gamepad support, peer-to-peer LAN multiplayer, and one-click modding.

[📥 **Download Latest APK**](https://github.com/VivxKumar07/Cubiq-Launcher/releases/latest) • [✨ **Features**](#-features) • [📖 **Quick Start**](#-quick-start-guide) • [❓ **FAQ**](#-frequently-asked-questions-faq) • [🛠️ **Build from Source**](#-building-from-source)

</div>

---

## 🌟 Features

### 🖥️ Authentic Minecraft Desktop Launcher UI
- **Pixel-Accurate Interface**: Faithfully recreates the modern PC Minecraft Launcher aesthetic with raised beveled buttons, pixel borders, and tactile surfaces.
- **Audio & Haptic Feedback**: Authentic button click sounds with zero interference during scrolling or gesture navigation.
- **Pixel Typography**: Native Minecraft font family integrated across installations, settings, and menus.

### 🎮 Dual Control Options & Deep Controller Support
- **Java Classic & Bedrock Touch Layouts**: Choose between traditional keyboard-adapted touch controls (Pojav-inspired) or modern Bedrock Pocket Edition touch controls (`btn_pe_attack`, `btn_pe_use`, d-pad, joystick, sneak, jump, sprint).
- **On-Screen Control Editor**: Drag, drop, resize, reposition, modify opacity, and map custom keys for every on-screen button.
- **Physical Controller Support**: Connect Xbox, PlayStation DualShock/DualSense, or generic Bluetooth/USB controllers with full analog stick deadzone and sensitivity calibration.
- **Virtual Mouse & Gyro Aiming**: Smooth touchpad virtual mouse with configurable tap gestures and optional gyroscope-assisted camera aiming.

### 🚀 Cutting-Edge Graphics Engines & Java Runtimes
- **Multiple GPU Renderers**:
  - **Krypton Wrapper (`NGGL4ES`)** *(Recommended)*: High-performance OpenGL ES 3 wrapper for maximum stability across all devices.
  - **Kopper Zink**: Desktop OpenGL mapped over Vulkan 1.2+ for top-tier GPU performance.
  - **GL4ES**: Lightweight OpenGL ES 2 wrapper optimized for legacy versions and lower-end chipsets.
  - **Freedreno & Panfrost**: Hardware-level drivers tailored for Qualcomm Snapdragon (Adreno) and ARM Mali GPUs.
  - **VirGL**: Gallium rasterizer compatibility fallback.
- **Bundled Multi-Runtime Java**: Automated detection and isolated support for Java 8, 17, and 21+.
- **Dynamic Memory Allocation**: RAM allocation slider capped automatically by your device's physical memory to prevent system freezes.

### ⚡ Complete Version & Mod Management
- **Universal Version Support**: Play any release from early classic builds through Minecraft 1.12.2, 1.16.5, 1.20, and 1.21+.
- **One-Click Mod Loaders**: Automatic installation and launch support for **Fabric**, **Forge**, **NeoForge**, and **Quilt**.
- **Integrated Download Hub**: Discover, search, filter, and install **Mods**, **Modpacks**, **Resource Packs**, and **Shaders** directly through Modrinth and CurseForge APIs.
- **Per-Version Isolation**: Separate custom directories, configurations, and JVM flags per installation.

### 🌐 LAN Multiplayer & Offline Freedom
- **Peer-to-Peer LAN Sharing**: Integrated Terracotta LAN proxy lets you join or host local LAN worlds over Wi-Fi without configuring external servers.
- **Server Browser**: Bookmark public servers, monitor live latency (ping), and join multiplayer sessions.
- **Flexible Account System**:
  - **Microsoft OAuth**: Secure, device-code authentication via official Microsoft services.
  - **Instant Offline Accounts**: Play singleplayer or offline LAN worlds without an internet connection.
  - **Skins & Wardrobe**: Full 2D/3D skin preview with custom `.png` upload and local skin rendering in offline mode via authlib-injector.

---

## 📱 System Requirements

| Component | Minimum Specification | Recommended Specification |
|---|---|---|
| **Operating System** | Android 8.0 (Oreo, API 26) | Android 11 – 16 |
| **Architecture** | `arm64-v8a`, `armeabi-v7a`, `x86_64` | `arm64-v8a` (64-bit) |
| **RAM** | 3 GB RAM | 6 GB – 8 GB+ RAM (especially for mods/shaders) |
| **Storage** | 2 GB free internal storage | 5 GB+ for modpacks and assets |
| **Graphics** | OpenGL ES 3.0 support | Vulkan 1.2+ supported GPU |

---

## 🚀 Quick Start Guide

1. **Download the APK**: Grab the latest release APK from the [Releases Page](https://github.com/VivxKumar07/Cubiq-Launcher/releases/latest).
2. **Install on Device**: Open the `.apk` on your Android device and allow *"Install from Unknown Sources"* if prompted.
3. **Add an Account**:
   - Tap the top-left account card to sign in with **Microsoft** (for official multiplayer servers) or create an **Offline Account** for local play.
4. **Create an Installation**:
   - Navigate to the **Installations** tab.
   - Tap **+ CREATE INSTALLATION**, choose your Minecraft version (e.g., `1.21.4` or `1.20.1`) and select a mod loader (Fabric, Forge, or Vanilla).
5. **Hit Play**: Return to the **Play** tab and tap **PLAY** to download game assets and start your session!

---

## ❓ Frequently Asked Questions (FAQ)

<details>
<summary><b>1. Is Cubiq Launcher free and safe to use?</b></summary>
Yes. Cubiq Launcher is completely free, 100% open-source under the GNU GPL-3.0 license, and contains no ads, telemetry trackers, or malware. You can verify the entire codebase and build it yourself from source.
</details>

<details>
<summary><b>2. How does Cubiq Launcher compare to PojavLauncher?</b></summary>
Cubiq Launcher is built from the ground up with a native Jetpack Compose UI replicating the authentic Minecraft PC Launcher interface. It features modernized renderer switching (Krypton, Kopper Zink), integrated Modrinth/CurseForge downloaders, built-in Terracotta P2P LAN multiplayer, and customizable touch controls.
</details>

<details>
<summary><b>3. Which renderer should I choose?</b></summary>
For most devices, <b>Krypton Wrapper (Recommended)</b> offers the highest stability and smooth frame rates. If your device supports Vulkan 1.2+ (Snapdragon 8 Gen 1+, Dimensity, or modern Mali), <b>Kopper Zink</b> provides desktop OpenGL acceleration over Vulkan for maximum performance.
</details>

<details>
<summary><b>4. Can I play with my friends on PC?</b></summary>
Yes! Because Cubiq runs genuine Minecraft: Java Edition bytecode, you can connect to any Java Edition server, Realm (with Microsoft sign-in), or local Wi-Fi LAN world hosted by a PC running the same game version.
</details>

<details>
<summary><b>5. Can I use shaders and resource packs?</b></summary>
Yes. By installing Fabric with Iris/Sodium, or Forge with Embeddium/Oculus, you can load compatible shaderpacks and high-resolution resource packs directly from the in-game Video Settings or Cubiq's Download Hub.
</details>

---

## 🛠️ Building from Source

### Prerequisites
- **Android Studio** (Ladybug | 2024.2.1 or newer)
- **Android SDK** (Compile SDK 34+)
- **JDK 17 or JDK 21**
- **Git** with submodules support

### Clone & Compile

```bash
# Clone repository with submodules
git clone --recursive https://github.com/VivxKumar07/Cubiq-Launcher.git
cd Cubiq-Launcher

# Compile Debug APK
./gradlew CubiqLauncher:assembleDebug

# Compile Release APK
./gradlew CubiqLauncher:assembleRelease

# Install directly to an attached USB/Wi-Fi Android device
./gradlew CubiqLauncher:installDebug
```

Outputs will be saved in:
- `CubiqLauncher/build/outputs/apk/debug/Cubiq-Debug-1.0.0.apk`

---

## 🤝 Contributing

Contributions, bug reports, and pull requests are warmly welcomed! Please read our [Contributing Guidelines](CONTRIBUTING.md) and [Security Policy](SECURITY.md) before submitting pull requests.

---

## 📜 License & Acknowledgements

This project is licensed under the **[GNU General Public License v3.0 (GPL-3.0)](LICENSE)**.

### Acknowledgements & Upstream Projects
Cubiq Launcher stands on the shoulders of giants in the Android Minecraft Java emulation community:
- **[Zalith Launcher 2](https://github.com/MovTery/ZalithLauncher2)**: Foundation architecture, core process isolation, and runtime bridges.
- **[PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)**: Trailblazing mobile Java runtime execution, SDL display integration, and input bridging.
- **[LWJGL](https://www.lwjgl.org/)**: Lightweight Java Game Library bridging on Android.

*Minecraft is a registered trademark of Mojang Synergies AB. Cubiq Launcher is an independent open-source project and is not affiliated with, endorsed by, or associated with Mojang Studios or Microsoft.*
