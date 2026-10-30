# 🧲 Pulse - Ultimate Hardware Sensor Toolbox

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg?style=for-the-badge&logo=kotlin)
![Android](https://img.shields.io/badge/Android-API%2024%2B-green.svg?style=for-the-badge&logo=android)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-orange.svg?style=for-the-badge&logo=jetpack-compose)
![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

**Pulse** turns your Android phone into a pocket scientific instrument. It is a premium, 100% offline hardware sensor utility app that gives you real-time data from the physical world. 

No accounts. No cloud. No sensor data leaves your device. Just pure hardware magic.

---

## 🌟 What can you do with Pulse?

Pulse isn't just a toy; it's a practical tool used across many fields:

*   **🧰 DIY & Home Improvement:** Locate studs, metal pipes, and hidden screws in drywall before drilling. Find dropped metal parts on a dark carpet.
*   **🔬 Mechanics & 3D Printing:** Place your phone on a washing machine, engine block, or 3D printer bed to diagnose excessive vibration, loose bolts, or bearing wear.
*   **💡 Lighting & Plants:** Measure exact Lux levels to find the perfect spot for houseplants, optimize desk lighting for eye health, or prepare a photography location.
*   **🎓 STEM Education:** Demonstrate magnetic fields, gravity separation, and 3D vector physics in real-time for science projects.
*   **📱 Hardware Testing:** Buying a used phone? Check if the magnetometer, accelerometer, or light sensor is broken.

---

## ✨ Core Features

### 1. 🧲 Magnetic Flux Detector (Metal Detector)
*   Uses the `TYPE_MAGNETIC_FIELD` sensor.
*   Displays 3-axis vector components (X, Y, Z).
*   Features a beautiful analog gauge with a needle that spikes near ferromagnetic metals (iron, steel, nickel).
*   Includes a real-time baseline calibration system.

### 2. 📳 Seismic Vibration Meter
*   Uses the `TYPE_LINEAR_ACCELERATION` sensor.
*   Shows a real-time oscillating waveform graph (120 samples).
*   Calculates Peak Vibration, Average Oscillation, and exact G-force.
*   Filters out Earth's gravity (~9.81 m/s²) for accurate dynamic movement tracking.

### 3. 💡 Photometric Light Meter
*   Uses the `TYPE_LIGHT` sensor.
*   Measures ambient light in Lux (lx).
*   Features a dynamic glowing sun icon that scales with brightness.
*   Shows an "Environment Bracket" (e.g., Living Room, Direct Sun) based on the current Lux reading.

---

## 🛠️ Tech Stack & Architecture

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM + Clean Architecture
*   **Hardware Integration:** Android `SensorManager` with `DisposableEffect` to prevent battery drain in the background.
*   **Custom UI Components:** Built from scratch using Compose `Canvas` (Analog Gauge, Waveform Chart, Lux Sun).
*   **Monetization:** Google AdMob & UMP (User Messaging Platform) for EU consent compliance.
*   **Min SDK:** 24 (Android 7.0) | **Target SDK:** 34 (Android 14)

---

## 🚀 Installation & Build

Dawnload Direct From Link BELOW 👇 

---

## 🔒 Privacy Policy

Pulse is built with privacy as a priority. 
*   **100% Offline:** The app does not require an internet connection to function.
*   **No Data Collection:** We do not collect, store, or share any sensor data, location data, or personal information.
*   **Ads:** We use Google AdMob to display a single banner ad. AdMob may collect your device's advertising ID to serve personalized ads.

---

## 👨‍💻 Developer

**Mohd Arsh (CartoniQ)**
*   GitHub: [@CartonIQ](https://github.com/CartonIQ)
*   Android App Developer | Kotlin | Java | Termux 🚀

## ⚖️ License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
