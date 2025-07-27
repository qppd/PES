<h1 align="center">🏫 PES Android App (Panaon Elementary School)</h1>

<p align="center">
  A role-based native Android app built for <strong>Panaon Elementary School</strong> to streamline school operations, announcements, event sharing, and parent-teacher engagement.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-green" alt="Platform">
  <img src="https://img.shields.io/badge/language-Kotlin-purple" alt="Language">
  <img src="https://img.shields.io/github/repo-size/qppd/PES" alt="Repo size">
</p>

<hr>

<h2>📲 Overview</h2>

<p>
  The PES App is a role-based school management system designed to improve communication, transparency, and access to information across the school community. It supports separate experiences for Admins, Teachers, Parents, and Guests, with powerful features like financial tracking, announcements, and event calendars.
</p>

<h2>👥 User Roles</h2>

<ul>
  <li><strong>Admin</strong> – Full access to announcements, financial reports, and system management via an admin panel</li>
  <li><strong>Teacher</strong> – Post events, announcements, and view class reports</li>
  <li><strong>Parent</strong> – Receive updates and view student-related information</li>
  <li><strong>Guest</strong> – Limited access for viewing general announcements or public events</li>
</ul>

<h2>✨ Features</h2>

<ul>
  <li>📢 Announcements board (via Firebase Firestore)</li>
  <li>📅 Events calendar for school-wide activities</li>
  <li>💰 Financial reports (role-restricted access)</li>
  <li>👨‍💼 Admin panel for managing content and users</li>
  <li>👤 Profile tab with account settings and logout options</li>
  <li>🔐 Secure login and role-based navigation</li>
  <li>📱 Bottom tab navigation for better UX</li>
</ul>

<h2>🛠 Tech Stack</h2>

<ul>
  <li><strong>Kotlin</strong> – Native Android development</li>
  <li><strong>Jetpack Compose</strong> – Modern UI toolkit</li>
  <li><strong>Firebase</strong> – Authentication and Firestore database</li>
</ul>

<h2>📂 Folder Structure</h2>

<pre>
java/com/qppd/pesapp/
├── MainActivity.kt           # App entry point
├── DashboardActivity.kt      # Bottom tab dashboard
├── PESApplication.kt         # Firebase initialization
├── auth/                     # Login and AuthManager
├── models/                   # User data, roles, announcement models
└── features/                 # Event, Announcement, Financial modules
</pre>

<h2>🚀 Getting Started</h2>

<ol>
  <li>Clone the repository:
    <pre><code>git clone https://github.com/qppd/PES.git</code></pre>
  </li>
  <li>Open the project in <strong>Android Studio</strong></li>
  <li>Sync Gradle and install dependencies</li>
  <li>Add your <code>google-services.json</code> to connect to Firebase</li>
  <li>Run the app on an Android device or emulator</li>
</ol>

<h2>👨‍💻 Developer</h2>

<p>
  <strong>Sajed Mendoza</strong><br>
  <em>Software & Hardware Developer @ QPPD</em><br>
  📧 <a href="mailto:qppdcontact@gmail.com">quezon.province.pd@gmail.com</a><br>
  🌐 <a href="https://github.com/qppd" target="_blank">github.com/qppd</a>
</p>

<h2>📄 License</h2>

<p>
  This project is licensed under the <strong>MIT License</strong>. See the <code>LICENSE</code> file for more info.
</p>

<hr>

<p align="center">
  Made with ❤️ for Panaon Elementary School
</p>
