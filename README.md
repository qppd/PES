# 🏫 PES Android App (Panaon Elementary School)

A role-based native Android app built for **Panaon Elementary School** to streamline school operations, announcements, event sharing, and parent-teacher engagement.

![Platform](https://img.shields.io/badge/platform-Android-green)
![Language](https://img.shields.io/badge/language-Kotlin-purple)

## 📲 Overview

The PES App is a role-based school management system designed to improve communication, transparency, and access to information across the school community. It supports separate experiences for Admins, Teachers, Parents, and Guests, with powerful features like financial tracking, announcements, and event calendars.

## 👥 User Roles

- **Admin** – Full access to announcements, events, financial reports, and system management via an admin panel
- **Teacher** – Full access to announcements, events, announcements, financial reports
- **Parent** – Receive updates and view SPTA related information
- **Guest** – Limited access for viewing general announcements or public events

## ✨ Features

- 📢 Announcements board
- 📅 Events calendar for school activities
- 💰 Financial reports (role-restricted access)
- 👨‍💼 Admin panel for managing content and users
- 👤 Profile tab with account settings and logout options
- 🔐 Secure login and role-based navigation
- 📱 Bottom tab navigation for better UX

## 🛠 Tech Stack

- **Kotlin** – Native Android development
- **Jetpack Compose** – Modern UI toolkit
- **Firebase** – Authentication and Firestore database

## 📂 Project Structure

```
java/com/qppd/pesapp/
├── MainActivity.kt           # App entry point
├── DashboardActivity.kt      # Bottom tab dashboard
├── PESApplication.kt         # Firebase initialization
├── auth/                     # Login and AuthManager
├── models/                   # User data, roles, announcement models
└── screens/                  # UI screens and components
```

## 🚀 Getting Started

1. Clone the repository:
```bash
git clone https://github.com/qppd/PES.git
```

2. Open the project in **Android Studio**
3. Sync Gradle and install dependencies
4. Create a Firebase project and add your `google-services.json` (see Configuration section)
5. Run the app on an Android device or emulator

## ⚙️ Configuration

1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project:
   - Use package name: `com.qppd.pesapp`
   - Download `google-services.json`
3. Place `google-services.json` in the `app/` directory
4. Enable Authentication with Email/Password in Firebase Console
5. Create Firestore database in test mode

## 👨‍💻 Developer

**Sajed Mendoza**  
*Software & Hardware Developer @ QPPD*  
📧 [quezon.province.pd@gmail.com](mailto:qppdcontact@gmail.com)  
🌐 [github.com/qppd](https://github.com/qppd)

## 📄 License

This project is licensed under the **MIT License**. See the `LICENSE` file for more info.

---

<p align="center">Made with ❤️ for Panaon Elementary School</p>
