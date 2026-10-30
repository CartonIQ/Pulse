# Pulse — Ultimate Sensor Toolbox

> **Turn your phone into a sci-fi tricorder.**  
> Package: `com.cartoniq.pulse` | Brand: CartonIQ  
> Stack: Kotlin 2.0.21 • Android Gradle Plugin 8.7.3 • Gradle 8.9 • Jetpack Compose Material 3  
> SDK Target: `compileSdk 34` | `targetSdk 34` | `minSdk 24` | Java/JVM 17

---

## 🛰️ Overview

Pulse is a premium, offline-first Android sensor utility that transforms hardware sensors into an approachable, high-precision measurement toolkit with a polished sci-fi instrument aesthetic.

### Release 1 Core Tools:
1. **Magnetic Flux Detector (`TYPE_MAGNETIC_FIELD`)**:
   - 3-axis geomagnetic field monitoring ($X, Y, Z$) and Euclidean magnitude ($\mu\text{T}$).
   - Analog gauge instrument with animated needle damping.
   - Dynamic baseline calibration ($\Delta\mu\text{T}$ deviation) and peak hold.
   - Throttled audio tone alerts and tactile haptic feedback when crossing user-configurable threshold.
   - Transparent disclosure: distinguishes magnetic flux disruptions from universal metal detection (ferromagnetic metals disrupt lines; non-ferrous metals like gold/pure copper do not).
2. **Seismic Vibration Meter (`TYPE_ACCELEROMETER` / `TYPE_LINEAR_ACCELERATION`)**:
   - Real-time rolling 120-sample oscilloscope waveform graph.
   - Low-pass gravity separation filter isolating dynamic vibration oscillations from static $1.0\text{g}$ Earth gravity.
   - Dual-readout in $\text{m/s}^2$ and standard $\text{g}$-force ($\div 9.80665$).
   - Peak hold and rolling average acceleration.
3. **Photometric Light Meter (`TYPE_LIGHT`)**:
   - Real-time illuminance in lux ($\text{lx}$).
   - Animated solar radial visualizer with intensity-scaled dynamic corona and rotating rays.
   - Environmental classification brackets (Extreme Darkness, Dim Interior, Normal Indoor, Bright Office, Overcast Daylight, Direct Sunlight).
   - Minimum and maximum lux tracking.
4. **Sensor Diagnostics & Configuration**:
   - Live hardware bus discovery listing all device sensors with vendor, power draw ($\text{mA}$), resolution, and maximum range.
   - User preferences: audio beeps, haptics, threshold limits, sampling rate (Normal vs Game).
   - Google UMP privacy consent form integration.

---

## 📱 How to Build in Termux on Android Phone

Pulse was engineered specifically for developers who build and package Android APKs directly on their ARM Android device using Termux.

### Prerequisites (Termux):
```bash
pkg update -y
pkg install -y openjdk-17 git android-tools
```

### Build Steps:
1. Extract or clone `Pulse/` to your Termux home directory:
   ```bash
   cd ~/Pulse
   ```
2. Make `gradlew` and `termux-build.sh` executable:
   ```bash
   chmod +x gradlew termux-build.sh
   ```
3. Run the automated build script:
   ```bash
   ./termux-build.sh
   ```
   Or invoke Gradle directly:
   ```bash
   ./gradlew clean assembleDebug
   ```

4. Once the build finishes, install the APK directly on your phone:
   ```bash
   termux-open app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 💻 How to Build in Android Studio

1. Open Android Studio (Ladybug 2024.2.1 or newer recommended).
2. Select **File -> Open...** and navigate to the `Pulse/` folder.
3. Let Gradle sync dependencies (Gradle 8.9 + AGP 8.7.3 + Kotlin 2.0.21).
4. Run `assembleDebug` or press the **Run** button to launch on your connected phone or emulator.

---

## 🧪 Unit Tests

Run the domain calculation verification test suite:
```bash
./gradlew testDebugUnitTest
```
Tests verify:
- `MT-01`: Pythagorean 3D vector magnitude calculations.
- `VT-01`: Standard $\text{g}$-force conversion ($9.80665\text{ m/s}^2 = 1.0\text{g}$).
- `VT-02`: Gravity filter convergence to zero dynamic acceleration at rest.
- `LT-01`: Environmental lux classification boundaries.
- Threshold limit crossing and baseline deviation logic.

---

## 🔒 Privacy & Monetization Configuration

- **Zero Unnecessary Permissions**: Only `INTERNET` (strictly for AdMob/UMP) and `VIBRATE` (for tactile feedback). No location, contacts, camera, or microphone permissions.
- **Offline Guarantee**: Sensor acquisition, calculations, and UI are 100% offline. Network connectivity failure never blocks sensor tools.
- **Official Google AdMob Test IDs**:
  - Sample App ID: `ca-app-pub-3940256099942544~3347511713`
  - Fixed Banner Ad Unit: `ca-app-pub-3940256099942544/6300978111`
  - Anchored Adaptive Banner Ad Unit: `ca-app-pub-3940256099942544/9214589741`
