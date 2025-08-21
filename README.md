
# PES Android App – Panaon Elementary School

A native Android application designed for **Panaon Elementary School** to streamline school operations, announcements, event management, and parent-teacher engagement. The app provides a secure, role-based experience for Admins, Teachers, Parents, and Guests, leveraging modern Android technologies and Firebase integration.

---

## Table of Contents

- [Description](#description)
- [Features](#features)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Usage](#usage)
- [Running Tests](#running-tests)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)
- [Authors](#authors)
- [Acknowledgements](#acknowledgements)

---

## Description

The PES Android App is a comprehensive school management system that enhances communication, transparency, and access to information within the school community. It offers tailored interfaces and permissions for different user roles, ensuring that each stakeholder—Admin, Teacher, Parent, or Guest—has access to the tools and information relevant to them.

---

## Features

- **Role-Based Access:** Distinct experiences for Admins, Teachers, Parents, and Guests.
- **Announcements Board:** Centralized platform for school-wide updates.
- **Events Calendar:** View and manage upcoming school activities.
- **Financial Reports:** Secure, role-restricted access to financial data.
- **Admin Panel:** Manage users, content, and system settings.
- **Profile Management:** Update account settings and secure logout.
- **Secure Authentication:** Firebase-backed login and user management.
- **Modern UI:** Built with Jetpack Compose and bottom tab navigation for intuitive UX.

---

## Project Structure

```
java/com/qppd/pesapp/
├── MainActivity.kt           # App entry point
├── DashboardActivity.kt      # Bottom tab dashboard
├── PESApplication.kt         # Firebase initialization
├── auth/                     # Login and AuthManager
├── models/                   # User data, roles, announcement models
└── features/                 # Event, Announcement, Financial modules
```

---

## Installation

1. **Clone the Repository**
  ```sh
  git clone https://github.com/qppd/PES.git
  ```
2. **Open in Android Studio**
  - Launch Android Studio and select `Open an existing project`.
3. **Sync Gradle**
  - Allow Gradle to sync and download dependencies.
4. **Configure Firebase**
  - Add your `google-services.json` file to the `app/` directory for Firebase integration.
5. **Build & Run**
  - Connect an Android device or start an emulator, then click **Run**.

---

## Usage

- **Login:** Sign in with your assigned credentials. Access is determined by your user role.
- **Navigation:** Use the bottom tab bar to switch between Announcements, Events, Financials, and Profile.
- **Admin Functions:** Admins can manage users, content, and view detailed reports via the admin panel.
- **Parents & Guests:** View public announcements and events; parents can access SPTA-related information.

---

## Running Tests

> **Note:** Automated testing is in progress. To run available unit tests:

1. Open the project in Android Studio.
2. Right-click the `test` or `androidTest` directory.
3. Select **Run Tests**.

---

## Documentation

- **Code Documentation:** Inline comments and KDoc are provided throughout the codebase.
- **Architecture:** The app follows a modular structure with clear separation of concerns.
- **Further Reading:** See the [Wiki](https://github.com/qppd/PES/wiki) (if available) for detailed guides.

---

## Contributing

Contributions are welcome! To contribute:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes with clear messages.
4. Push to your fork and submit a pull request.

Please read the [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

## Authors

- **Sajed Mendoza**  
  Software & Hardware Developer @ QPPD  
  [quezon.province.pd@gmail.com](mailto:quezon.province.pd@gmail.com)  
  [github.com/qppd](https://github.com/qppd)

---

## Acknowledgements

- Panaon Elementary School for project inspiration and feedback.
- Jetpack Compose and Firebase teams for their robust tools and documentation.
- All contributors and testers supporting the app’s development.

---

_Made with ❤️ for Panaon Elementary School_
