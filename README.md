# Cubiq Launcher
![Platform](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Edition](https://img.shields.io/badge/Minecraft-Java%20Edition-3C8527)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)
![License](https://img.shields.io/badge/License-GPL--3.0-blue)
![Author](https://img.shields.io/badge/Author-Vivek%20Kumar-55FF55)

**Cubiq Launcher** is a premier, high-performance **Minecraft: Java Edition launcher for Android**, featuring an authentic, pixel-accurate PC Minecraft Launcher interface design built natively with **Jetpack Compose**.

Cubiq delivers a seamless desktop-grade Minecraft experience on mobile and touch devices with fine-grained performance controls, deep gamepad remapping, a drag-and-drop on-screen touch control editor, and multi-loader modding support.

---

## 🌟 Key Features

### 🖥️ Authentic Minecraft Desktop UI
* **Pixel-Accurate Interface**: Crafted to replicate the modern Minecraft PC Launcher with beveled pixel buttons, 3D raised surfaces, and subtle borders.
* **Audio Feedback**: Authentic button click sound effects with zero interference during scrolling or gestures.
* **Pixel Typography**: Integrated Minecraft typeface alongside crisp, responsive layout scaling across phones, foldables, and tablets.

### 🎮 Comprehensive Controls & Gamepad Remapping
* **Bedrock Pocket Edition Touch Controls**: Native Bedrock Edition styled on-screen control layout (`bedrock_touch_layout.json`) with official action buttons (jump, sneak, sprint, attack, interact, d-pad, joystick, F3, F5, chat, inventory, menu).
* **On-Screen Touch Control Editor**: Open the built-in control editor directly to create, reposition, resize, change textures, and configure opacity and button mappings.
* **Full Controller Support**: Direct SDL passthrough or customizable mapped input with physical gamepad support (Xbox, PlayStation DualShock/DualSense, Generic Bluetooth/USB).
* **Per-Button Remapping**: Individually bind D-pad, Analog sticks, face buttons (A/B/X/Y), triggers (L1/R1, L2/R2), stick buttons (L3/R3), Start, Select, and Guide.
* **Stick Deadzone & Sensitivity**: Calibrate analog stick deadzone (50%–200%) and independent cursor/camera speeds.
* **Virtual Mouse & Gyroscope**: Smooth virtual touchpad mouse with configurable tap/long-press actions and gyroscope aiming.

### 🌐 Multiplayer & LAN Sharing
* **Peer-to-Peer LAN Multiplayer**: Integrated Terracotta LAN proxy allowing you to host or join local LAN Minecraft worlds directly over Wi-Fi without configuring external dedicated servers.
* **Public Server Browser**: Add, bookmark, and track server status with ping indicators.

### ⚡ Complete Game & Version Management
* **Universal Version Support**: Play any Minecraft: Java Edition release from classic versions up to the latest releases (1.10, 1.16.5, 1.20, 1.21+).
* **Two-Bar Download Progress**: Real-time status displaying overall task progress, current active downloading file, and live network transfer speed.
* **Mod Loaders**: One-click installation and management for **Fabric**, **Forge**, **NeoForge**, and **Quilt**.
* **Integrated Download Hub**: Search, filter, and download **Mods**, **Modpacks**, **Resource Packs**, and **Shaders** directly via CurseForge and Modrinth APIs.
* **Isolation & JVM Customization**: Per-instance game directories, custom JVM arguments, and automated Java runtime detection.

### 🚀 High-Performance Rendering & Java Runtimes
* **Modern Graphics Engines**: Select from Vulkan (Zink), Freedreno, VirGL, Panfrost, and GL4ES renderers for optimal GPU acceleration.
* **Multi-Runtime Java**: Bundled and custom Java runtimes (Java 8, 17, 21, and beyond).
* **Dynamic Memory Slider**: Custom RAM allocation slider dynamically capped by your device's physical memory.

### 👤 Account & Skin Management
* **Microsoft OAuth Login**: Fast, secure device-code authentication via official Microsoft services.
* **Instant Offline Accounts**: Quick local player profile creation accessible right from the top bar dropdown or settings.
* **Skins & Capes**: 2D and 3D avatar rendering, local skin importing, and Slim (Alex) / Classic (Steve) model customization.

---

## 📱 System Requirements

* **Operating System**: Android 8.0 (Oreo / API 26) or newer (Android 11–16 recommended)
* **Architecture**: `arm64-v8a` (recommended), `armeabi-v7a`, `x86_64`, `x86`
* **RAM**: 4 GB+ recommended for modern Minecraft releases & modpacks
* **Storage**: 2 GB+ free storage space

---

## 📦 Building from Source

### Prerequisites
* Android Studio (Ladybug or newer)
* Android SDK (Compile SDK 34+)
* JDK 17 or JDK 21

### Compilation

```bash
# Clone the repository
git clone https://github.com/VivxKumar07/Cubiq-Launcher.git
cd Cubiq-Launcher

# Build Debug APK
./gradlew CubiqLauncher:assembleDebug

# Build Release APK
./gradlew CubiqLauncher:assembleRelease

# Install directly to connected device / emulator
./gradlew CubiqLauncher:installDebug
```

Compiled APKs will be located at:
`CubiqLauncher/build/outputs/apk/debug/` and `CubiqLauncher/build/outputs/apk/release/`

---

## 👥 Contributors & Maintainers

* **Vivek Kumar** ([@VivxKumar07](https://github.com/VivxKumar07)) — Lead Developer & Maintainer

---

## 📜 License & Open Source Attribution

This project is licensed under the **[GNU General Public License v3.0 (GPL-3.0)](LICENSE)**.

### Upstream Attribution
Cubiq Launcher is built upon the open-source foundations of **Zalith Launcher 2** and **PojavLauncher**, redesigned with an authentic Minecraft PC launcher interface, modernized Compose UI architecture, and restored modular settings. All upstream copyright notices, contributions, and license obligations are preserved in full compliance with the GNU GPLv3 license.