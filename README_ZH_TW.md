# Cubiq Launcher
![Platform](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Edition](https://img.shields.io/badge/Minecraft-Java%20Edition-3C8527)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)
![License](https://img.shields.io/badge/License-GPL--3.0-blue)

[English](README.md) | [繁體中文](README_ZH_TW.md)

**Cubiq Launcher** 是一款專為 Android 平台打造的高效能 **Minecraft: Java Edition** 啟動器，具備像素級還原的 PC 原版 Minecraft 啟動器設計與極致流暢的操作體驗。

基於 **現代 Jetpack Compose** 全面構建，Cubiq Launcher 在觸控與行動裝置上帶來高度還原的原版 Minecraft Java 版桌面客戶端體驗。

---

## 🌟 核心特色

* **經典 Minecraft PC 原版啟動器介面**: 
  - 像素級斜切邊按鈕、3D 立體按壓回饋與深色質感邊框。
  - 原版按鈕點擊音效，滑動滾動與點擊精確分離。
  - 原生 Minecraft 字型排版與清晰沉浸的深色主題。
* **完整 Java 版支援**:
  - 完美相容各版本 Minecraft Java 版，支援 Forge、Fabric、Quilt 與 NeoForge 模組載入器。
  - 多版本 Java 執行時期自動切換與管理（Java 8、17、21 及更高版本）。
* **微軟帳號與離線帳號極速登入**:
  - 快速流暢的 Microsoft OAuth 裝置授權登入流程。
  - 本地離線玩家個人檔案管理，支援自訂 3D 頭像與外觀渲染。
* **豐富全面的進階遊戲設定**:
  - 精細記憶體（RAM）分配調節。
  - 自訂行動虛擬控制器佈局與完整手把支援。
  - 高效能圖形算繪引擎（Vulkan Zink、VirGL、Holy、ANGLE、GL4ES）。

---

## 📦 構建指南（開發者）

### 環境要求

* Android Studio Ladybug 或更高版本
* Android SDK:
  * **最低 API 等級**: 26 (Android 8.0)
  * **目標 API 等級**: 34+
* JDK 17 或 JDK 21

### 構建 APK

```bash
# 複製儲存庫
git clone https://github.com/CubiqLauncher/Cubiq.git

# 構建 Debug APK
./gradlew assembleDebug

# 直接安裝至已連線的 Android 裝置
./gradlew installDebug
```

---

## 📜 開源許可協議

本專案遵循 **[GPL-3.0 協議](LICENSE)** 開源。

### 開源致謝與歸屬
Cubiq Launcher 基於 Zalith Launcher 2 與 PojavLauncher 開源核心進行二次開發，客製化打造了全新的原創 Minecraft PC 啟動器視覺體驗與功能強化。依據 GPL-3.0 協議要求，所有上游版權聲明與授權資訊均完整保留。