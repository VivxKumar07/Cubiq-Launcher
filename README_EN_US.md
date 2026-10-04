# Cubiq Launcher
![Platform](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Edition](https://img.shields.io/badge/Minecraft-Java%20Edition-3C8527)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)
![License](https://img.shields.io/badge/License-GPL--3.0-blue)

[English](README.md) | [Chinese](README_ZH_TW.md)

**Cubiq Launcher** is a specialized, high-performance **Minecraft: Java Edition launcher for Android**, featuring an authentic, pixel-accurate PC Minecraft Launcher interface design.

Built entirely with **Modern Jetpack Compose**, Cubiq Launcher delivers a faithful 1:1 recreation of the official Minecraft Java Edition desktop client on touch and mobile devices.

---

## 🌟 Key Features

* **Authentic Minecraft Desktop Launcher UI**: 
  - Beveled pixel buttons, 3D raise effects, and stone border styling.
  - Authentic button click audio with zero touch-scroll interference.
  - Minecraft font typography and clean dark theme.
* **Full Java Edition Support**:
  - Run Minecraft Java Edition versions with Forge, Fabric, Quilt, and NeoForge mod loaders.
  - Multi-runtime Java management (Java 8, 17, 21, and beyond).
* **Direct Microsoft & Offline Account Support**:
  - Quick, native Microsoft OAuth device authorization.
  - Fast offline player profiles with custom 3D avatar & skin rendering.
* **Complete In-App Configuration**:
  - Fine-grained memory/RAM allocation.
  - Custom mobile touch controller layouts and full gamepad support.
  - High-performance renderer engines (Vulkan Zink, VirGL, Holy, ANGLE, GL4ES).

---

## 📦 Build Instructions (For Developers)

### Requirements

* Android Studio Ladybug or newer
* Android SDK:
  * **Minimum API level**: 26 (Android 8.0)
  * **Target API level**: 34+
* JDK 17 or JDK 21

### Building the APK

```bash
# Clone the repository
git clone https://github.com/CubiqLauncher/Cubiq.git

# Build debug APK
./gradlew assembleDebug

# Install directly to a connected device
./gradlew installDebug
```

---

## 📜 License

This project is licensed under the **[GPL-3.0 license](LICENSE)**.

### Open Source Attribution
Cubiq Launcher is based upon the open-source launcher foundation of Zalith Launcher 2 and PojavLauncher, customized with an original Minecraft PC Launcher design and enhanced feature set. All upstream copyright notices and open-source licenses are preserved in accordance with GPLv3.
