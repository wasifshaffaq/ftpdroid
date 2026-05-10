# 📡 FTPDroid

[![Version](https://img.shields.io/github/v/release/yourusername/ftpdroid?color=%2300BCD4&label=Latest%20Release&style=for-the-badge)](https://github.com/yourusername/ftpdroid/releases/latest)
[![License](https://img.shields.io/github/license/yourusername/ftpdroid?color=orange&style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-blue?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)

**FTPDroid** is a complete, dual-mode FTP solution for Android, built with the latest **Material 3 Expressive** design system. Whether you need to turn your phone into a powerful FTP server for local file sharing or connect to remote servers as a client, FTPDroid provides a seamless, high-performance experience.

> [!IMPORTANT]
> This app is built with **Jetpack Compose** and targets **Android 15 (SDK 35)**, ensuring compatibility with the latest Android features and security standards.

---

## 📸 Preview

![FTPDroid Hero Image](https://via.placeholder.com/800x400?text=FTPDroid+Material+3+Expressive+UI)

---

## 🚀 Key Features

### 🖥️ SERVER MODE
Transform your Android device into a robust FTP server.
- **One-Tap Start:** Quickly toggle your server on/off.
- **Configurable Port:** Default to 2121 (no root required).
- **Flexible Auth:** Supports both Anonymous access and Username/Password authentication.
- **User Management:** Create multiple users with granular permissions (Read/Write/Delete) and custom root directories.
- **Connection QR:** Generate a QR code for instant laptop-to-phone connection.
- **Live Logs:** Monitor active connections and transfer history in real-time.

### 💻 CLIENT MODE
Connect to any remote storage with ease.
- **Multi-Protocol:** Supports **FTP, FTPS (Explicit/Implicit), and SFTP (via SSH)**.
- **Profile Manager:** Save and organize multiple server profiles for quick access.
- **Advanced File Browser:** Multi-select support, search, sorting, and hidden files toggle.
- **Background Transfers:** Continue your uploads and downloads even when the app is in the background.
- **Resume Support:** Interrupted transfers can be resumed from where they left off.

### ✨ MATERIAL 3 EXPRESSIVE UI
Experience the future of Android design.
- **Spring Physics:** Fluid, bouncy animations for a more tactile feel.
- **Dynamic Color:** Fully supports Monet theming (Android 12+ wallpaper-based colors).
- **Adaptive Layouts:** Optimized for phones, tablets, and foldables.
- **Dark Mode:** Gorgeous, high-contrast dark theme.

---

## 🛠️ Tech Stack & Architecture

FTPDroid is built using modern Android development best practices:

- **Language:** Kotlin 2.0 (with KSP)
- **UI:** Jetpack Compose with Material 3 Expressive (1.4.0-alpha)
- **Architecture:** Clean Architecture + MVVM + MVI for predictable UI state.
- **DI:** Hilt (Dagger)
- **Database:** Room for profiles and transfer history.
- **Networking:** 
    - Apache FtpServer (Server)
    - Apache Commons Net (FTP/FTPS Client)
    - Sshj (SFTP Client)
- **Storage:** DataStore Preferences & Scoped Storage (SAF).

---

## 🏗️ Build & Develop

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17
- Android SDK 35

### Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/ftpdroid.git
   ```
2. Open the project in Android Studio.
3. Sync Project with Gradle Files.
4. Run the `app` module on your device or emulator.

---

## 🎓 Documentation & Support

> [!TIP]
> For a deep dive into the technical specifications and architecture, refer to the [FTP Android App Blueprint](./FTP_Android_App_Blueprint.md).

- **Issues:** Found a bug? [Create an issue](https://github.com/yourusername/ftpdroid/issues).
- **Discussions:** Want to suggest a feature? [Start a discussion](https://github.com/yourusername/ftpdroid/discussions).

---

## 💖 Support the Project
- Leave a ⭐ on GitHub if you find this project useful!
- Contributions are welcome! Please read the [Contribution Guidelines](CONTRIBUTING.md) before submitting a PR.

---

## 🏅 Contributors
Thanks to everyone who has contributed to making FTPDroid better!

[![Contributors](https://contrib.rocks/image?repo=yourusername/ftpdroid)](https://github.com/yourusername/ftpdroid/graphs/contributors)

---

## 📊 Project Stats

![GitHub Repo stars](https://img.shields.io/github/stars/yourusername/ftpdroid?style=for-the-badge&color=yellow)
![GitHub forks](https://img.shields.io/github/forks/yourusername/ftpdroid?style=for-the-badge&color=blue)

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
