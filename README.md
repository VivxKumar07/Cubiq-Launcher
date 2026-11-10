<div align="center">

# Cubiq Launcher

**An open-source Minecraft: Java Edition launcher for Android with an authentic PC launcher experience.**

[![Release](https://img.shields.io/github/v/release/VivxKumar07/Cubiq-Launcher?style=flat-square&color=40c463)](https://github.com/VivxKumar07/Cubiq-Launcher/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-green?style=flat-square&logo=android&logoColor=white)](https://github.com/VivxKumar07/Cubiq-Launcher)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue?style=flat-square)](LICENSE)
[![Based on](https://img.shields.io/badge/Based%20on-ZalithLauncher2%20%26%20Pojav-orange?style=flat-square)](https://github.com/MovTery/ZalithLauncher2)

[Download APK](https://github.com/VivxKumar07/Cubiq-Launcher/releases/latest) • [Features](#-what-cubiq-offers) • [Getting Started](#-getting-started) • [Fork Terms & GPLv3](#-fork-terms--conditions-gplv3-section-7) • [Credits](#-credits--licenses)

</div>

---

> [!NOTE]
> **Notice:** Cubiq Launcher is an **unofficial modified version (fork)** built on top of [ZalithLauncher 2](https://github.com/MovTery/ZalithLauncher2) (by MovTery) and [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher). It is released under the **GNU General Public License v3.0**.

---

## 🎮 What is Cubiq Launcher?

Cubiq Launcher brings desktop **Minecraft: Java Edition** (from 1.7 all the way up to 1.21+) to your Android phone or tablet.

Most mobile launchers feel like Android utility tools. Cubiq is built differently: the entire interface is written from scratch in native **Jetpack Compose** to closely match the look, sound, and feel of the official desktop Minecraft Launcher on PC.

Whether you want to play vanilla singleplayer on the go, hop onto desktop multiplayer servers, run heavily modded Fabric or Forge setups, or try out the lightweight Horizon Rust edition, Cubiq is made to give you a smooth, responsive experience.

---

## ✨ What Cubiq Offers

- **Authentic Desktop UI**  
  Clean PC-style launcher layout with authentic sound effects, smooth animations, pixel typography, and customizable themes.

- **Dual Touch Controls & Controller Support**  
  - Switch freely between **Bedrock / Pocket Edition style touch controls** and classic on-screen keyboard controls.
  - Built-in on-screen layout editor: move, resize, adjust transparency, and assign keys anywhere on the screen.
  - Full support for physical controllers (Xbox, DualShock, DualSense, generic Bluetooth/OTG gamepads).
  - Virtual mouse touchpad and optional gyroscope-assisted camera aiming.

- **Fast GPU Renderers**  
  - **Krypton Wrapper (`NGGL4ES`)** *(Recommended)*: High-stability OpenGL ES 3 wrapper that works reliably across almost all devices.
  - **Kopper Zink**: Desktop OpenGL mapped over Vulkan 1.2+ for maximum framerates on modern GPUs (Snapdragon, Dimensity, Mali).
  - **GL4ES**: Lightweight OpenGL ES 2 wrapper for older or lower-end devices.
  - Hardware driver options including Freedreno, Panfrost, and VirGL.

- **One-Click Mod Loaders**  
  Built-in support for **Fabric**, **Forge**, **NeoForge**, and **Quilt**. Select your loader directly when creating a profile.

- **In-App Download Center**  
  Search and install mods, resource packs, shaders, and modpacks directly from **Modrinth** and **CurseForge** without leaving the app.

- **Multiplayer & Offline Play**  
  - Sign in with your official **Microsoft Account** to join PC Java servers and Realms.
  - Offline mode support for singleplayer or local play.
  - **Terracotta P2P LAN**: Play with friends nearby over local Wi-Fi without needing a dedicated server setup.
  - Custom skin support with 2D/3D preview.

- **Horizon Rust Edition Support**  
  Integrated support for the native Rust-based Minecraft client, featuring direct launch and custom touch controls.

---

## 📱 Requirements

| Requirement | Minimum | Recommended |
|---|---|---|
| **OS** | Android 8.0 (Oreo, API 26) | Android 11 or newer |
| **Architecture** | `arm64-v8a` / `x86_64` | `arm64-v8a` (64-bit) |
| **RAM** | 3 GB | 6 GB - 8 GB+ (for mods & shaders) |
| **Storage** | 2 GB free space | 5 GB+ for modpacks and assets |
| **GPU** | OpenGL ES 3.0 | Vulkan 1.2+ capable GPU |

---

## 🚀 Getting Started

1. **Download**: Grab the latest APK from the [Releases](https://github.com/VivxKumar07/Cubiq-Launcher/releases/latest) page.
2. **Install**: Open the APK file on your device and install it (enable *"Install unknown apps"* if prompted).
3. **Add Account**: Tap the account card on top-left. Sign in with your Microsoft account (for official multiplayer) or create an offline profile.
4. **Create Installation**: Go to the **Installations** tab, tap **+ CREATE INSTALLATION**, select your Minecraft version and mod loader, and save.
5. **Play**: Head back to the **Play** tab and tap the big green **PLAY** button. First launch will download game files and Java runtimes automatically.

---

## 🛠️ Building from Source

To build Cubiq Launcher yourself:

### Requirements
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17 or JDK 21
- Android SDK with API 34+

### Steps
```bash
# Clone the repository with all submodules
git clone --recursive https://github.com/VivxKumar07/Cubiq-Launcher.git
cd Cubiq-Launcher

# Build debug APK
./gradlew :CubiqLauncher:assembleDebug

# Or build release APK
./gradlew :CubiqLauncher:assembleRelease
```

The compiled APK will be located under:
`CubiqLauncher/build/outputs/apk/debug/` (or `release/`).

---

## ⚖️ Fork Terms & Conditions (GPLv3 Section 7)

Cubiq Launcher is a derivative work based on **[ZalithLauncher 2](https://github.com/MovTery/ZalithLauncher2)** by **MovTery** and **[PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)**.

In accordance with the **GNU General Public License v3.0 (GPL-3.0)** and the specific upstream conditions defined under **Section 7 of the GPLv3**, anyone who forks or distributes a modified version of this repository **must** strictly adhere to the following terms:

1. **Distinct Name & Version (GPLv3 § 7(c))**  
   If you distribute a modified version or fork, you **must** reasonably modify the project's name and version number to clearly distinguish it from Cubiq Launcher, ZalithLauncher, and PojavLauncher.
2. **Name Restrictions**  
   You **must not** use the name `"ZalithLauncher"`, its abbreviation `"ZL"`, or `"Cubiq Launcher"` in your application's public branding, package name, or title in any way that suggests endorsement or causes confusion with the original projects.
3. **Prominent Unofficial Declaration**  
   Any distributed fork or modified build **must explicitly and prominently display** on its startup screen or main user interface that it is an **"Unofficial Modified Version"** (非官方修改版).
4. **Preservation of Copyright Notices (GPLv3 § 7(b))**  
   You **must not** remove, alter, or hide original copyright notices, license headers, or author attributions belonging to MovTery, PojavLauncherTeam, or other upstream contributors.
5. **Open Source Requirement (GPLv3 Copyleft)**  
   Any derivative work or distributed build must be made publicly available under the exact same **GNU GPL-3.0** license, including the full source code.

---

## 📜 Credits & Licenses

Cubiq Launcher is licensed under the **[GNU General Public License v3.0 (GPL-3.0)](LICENSE)**.

Huge thanks to the upstream open-source projects and developers who made this possible:

| Project | Author / Team | License | Role |
|---|---|---|---|
| **[ZalithLauncher 2](https://github.com/MovTery/ZalithLauncher2)** | MovTery & contributors | GPL-3.0 | Core architecture, launcher structure & runtime manager |
| **[PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)** | PojavLauncherTeam | GPL-3.0 | Groundbreaking Android Java execution engine & GLFW bridge |
| **[LWJGL 3](https://www.lwjgl.org/)** | LWJGL Team | BSD-3-Clause | Lightweight Java Game Library bindings |
| **[GL4ES](https://github.com/ptitSeb/gl4es)** | ptitSeb | MIT | OpenGL 2.1/1.5 to GLES translation |
| **[Mesa / Zink](https://gitlab.freedesktop.org/mesa/mesa)** | Mesa Contributors | MIT | Desktop OpenGL implementation over Vulkan |
| **[Terracotta](https://github.com/muntashirakon/terracotta)** | Muntashir Al-Islam | Apache-2.0 / MIT | Local P2P LAN multiplayer tunnel |
| **[authlib-injector](https://github.com/yushijinhun/authlib-injector)** | yushijinhun | MIT | Offline authentication & skin reflection |
| **[Jetpack Compose](https://developer.android.com/jetpack/compose)** | Google LLC | Apache-2.0 | Modern Android UI toolkit |
| **Horizon Engine** | Horizon Team | Open Source | Native Rust Minecraft client engine |

---

## ⚠️ Legal Disclaimer

- **Minecraft** is a registered trademark of **Mojang Synergies AB** and **Microsoft Corporation**.
- Cubiq Launcher is an independent open-source project and is **NOT** an official Minecraft product.
- It is **not approved by, associated with, or affiliated with** Mojang Studios or Microsoft.
- To play on official online servers or Realms, you must own a legitimate copy of Minecraft: Java Edition.
- Please support the game developers by purchasing Minecraft from [minecraft.net](https://www.minecraft.net).
