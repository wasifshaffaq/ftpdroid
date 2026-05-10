# 📡 FTPDroid

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/Kotlin-2.0%2B-blue?style=for-the-badge&logo=kotlin" alt="Kotlin">
</p>

---

**FTPDroid** is a high-performance, dual-mode FTP solution for Android, meticulously crafted with the **Material 3 Expressive** design system. It serves as both a powerful FTP/SFTP client and a robust FTP server, enabling seamless file management and sharing directly from your mobile device.

> [!IMPORTANT]
> This application is built with **Jetpack Compose** and leverages **Android 15 (SDK 35)** features, ensuring top-tier performance, modern security standards, and a future-proof architecture.

---

## 📸 visual Preview

<p align="center">
  <img src="https://via.placeholder.com/800x450.png?text=FTPDroid+Material+3+Expressive+UI+Overview" alt="FTPDroid UI Preview" width="800">
</p>

---

## 🚀 core Features

### 🛠️ Professional Client Mode
Connect and manage remote files with ease.
- **Multi-Protocol Support:** Full support for **FTP, FTPS (Explicit/Implicit), and SFTP (SSH)**.
- **Advanced Profile Management:** Securely store and organize unlimited server profiles.
- **Background Transfer Engine:** Reliable uploads and downloads that persist even when the app is minimized.
- **Intelligent Resume:** Automatically resume interrupted transfers to save time and bandwidth.
- **Enhanced Navigation:** Multi-select operations, deep search, and intuitive sorting.

### 📡 Robust Server Mode
Turn your device into a localized cloud.
- **Instant Deployment:** Single-tap server activation.
- **Granular Security:** Manage multiple users with specific permissions (Read/Write/Delete) and isolated root directories.
- **Rootless Operation:** Defaults to port `2121` for full functionality without requiring system root.
- **QR Connection:** Generate instant connection tokens for rapid desktop-to-mobile pairing.
- **Real-time Monitoring:** Live connection logs and active transfer tracking.

### ✨ Material 3 Expressive Design
Experience a refined, tactile interface.
- **Adaptive UI:** Fluid layouts optimized for phones, foldables, and tablets.
- **Dynamic Theming:** Deep integration with Android 12+ Monet engine for wallpaper-based colors.
- **Haptic Feedback & Physics:** Spring-loaded animations and intuitive tactile responses.
- **Biometric Security:** Optional app-level locking via fingerprint or face recognition.

---

## 🏗️ Architecture & Tech Stack

FTPDroid follows **Clean Architecture** principles combined with **MVI (Model-View-Intent)** for a predictable and robust state management.

| Layer | Technologies |
| :--- | :--- |
| **UI Framework** | Jetpack Compose (Material 3 Expressive) |
| **Dependency Injection** | Hilt (Dagger) |
| **Local Storage** | Room Database, DataStore Preferences |
| **Networking (Client)** | Apache Commons Net, SSHJ |
| **Networking (Server)** | Apache FtpServer, Apache MINA |
| **Concurrency** | Kotlin Coroutines & Flow |

### Project Structure
```text
ftpdroid
├── app/src/main/kotlin/com/ftpdroid/app
│   ├── data/           # Repositories, Database, Network implementations
│   ├── di/             # Dependency Injection modules
│   ├── domain/         # Use cases and domain models (Business Logic)
│   ├── service/        # Background services for Server & Transfers
│   └── ui/             # Compose Screens, Components, and Theme
```

---

## 🛠️ Installation & Setup

### Prerequisites
- Android Studio **Ladybug** (or newer)
- JDK **17**
- Android SDK **35**

### Build from Source
1. **Clone the repository:**
   ```bash
   git clone https://github.com/wasifshaffaq/ftpdroid.git
   ```
2. **Open in Android Studio:**
   Import the project and allow Gradle to sync.
3. **Configure `local.properties`:**
   Ensure your SDK path is correctly set.
4. **Run:**
   Select the `app` configuration and deploy to your device or emulator.

---

## 📊 Project Insights

<p align="center">
  <img src="https://img.shields.io/github/stars/wasifshaffaq/ftpdroid?style=for-the-badge&color=yellow" alt="Stars">
  <img src="https://img.shields.io/github/forks/wasifshaffaq/ftpdroid?style=for-the-badge&color=blue" alt="Forks">
  <img src="https://img.shields.io/github/issues/wasifshaffaq/ftpdroid?style=for-the-badge&color=red" alt="Issues">
</p>

### Contribution Graph
[![Contributors](https://contrib.rocks/image?repo=wasifshaffaq/ftpdroid)](https://github.com/wasifshaffaq/ftpdroid/graphs/contributors)

---

## 📄 License
Distributed under the **MIT License**. See `LICENSE` for more information.

---

<p align="center">
  Developed with ❤️ by <a href="https://github.com/wasifshaffaq">Wasif Shaffaq</a>
</p>
