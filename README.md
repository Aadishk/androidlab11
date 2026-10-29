# Real-Time Geolocation & Embedded Google Maps App (`exp10map`)

An Android application demonstrating real-time geolocation retrieval using **Google Play Services Fused Location Provider** and rendering an interactive **Google Maps** view embedded directly inside a custom HTML `WebView` iframe wrapper (without requiring Google Maps API key billing restrictions).

---

## 📸 Screenshots

<img width="589" height="1280" alt="WhatsApp Image 2026-10-29 at 10 18 33 AM (1)" src="https://github.com/user-attachments/assets/da15d03b-7814-4175-bbc7-187941ff2847" />
<img width="589" height="1280" alt="WhatsApp Image 2026-10-29 at 10 18 33 AM" src="https://github.com/user-attachments/assets/294ec554-3ad3-4a86-ac42-44d833af06eb" />


<p align="center">
  <img src="docs/screenshots/location_map.png" alt="Main Location & Embedded Map View" width="300" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="docs/screenshots/live_updates.png" alt="Real-time Live Location Updates" width="300" />
</p>

> *Replace the placeholder paths above with your uploaded screenshot images on GitHub.*

---

## ✨ Features

- **Fused Location Provider Client**: Utilizes Google Play Services `FusedLocationProviderClient` with `PRIORITY_HIGH_ACCURACY` for fast and battery-efficient GPS coordinate retrieval.
- **Runtime Permissions**: Uses modern Jetpack Activity Result API (`ActivityResultContracts.RequestMultiplePermissions`) to seamlessly request fine and coarse location permissions (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`).
- **Real-Time Live Updates**: Toggle continuous location updates with configurable intervals and distance thresholds.
- **Embedded Google Maps WebView**: Renders interactive Google Maps without API key billing restrictions by injecting latitude and longitude into a responsive HTML `<iframe>` loaded inside a WebView.
- **Material Design 3 UI**: Modern card-based UI displaying Latitude, Longitude, Accuracy (meters), update timestamp, progress indicators, and an embedded map container.

---

## 🛠️ Tech Stack & Dependencies

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: Android XML Layouts & Material Components 3 (`com.google.android.material`)
- **Location Services**: Google Play Services Location (`com.google.android.gms:play-services-location`)
- **Web Rendering**: Android `WebView` with JavaScript enabled
- **Target SDK**: Android 15 (API 35+) / Android 16 (API 36/37)
- **Minimum SDK**: Android 7.0 (API 24)

---

## 📁 Project Architecture & Structure

```
exp10map/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/exp10map/
│   │       │   └── MainActivity.kt          # Location logic, permissions, and WebView injection
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml    # Material Card layout & WebView layout
│   │       │   └── values/
│   │       │       ├── strings.xml          # UI Strings
│   │       │       └── themes.xml           # App Theme
│   │       └── AndroidManifest.xml          # Internet & Location permissions
│   └── build.gradle.kts                     # App-level build config
└── gradle/
    └── libs.versions.toml                   # Version catalog dependencies
```

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio Ladybug (or newer)](https://developer.android.com/studio)
- JDK 11+
- Android Device or Emulator running Android 7.0 (API 24) or higher with Location Services / GPS enabled.

### Building & Running

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/exp10map.git
   cd exp10map
   ```

2. **Open in Android Studio**:
   - Select **File > Open...** and navigate to the `exp10map` directory.

3. **Build the Project**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run the App**:
   - Select your target device/emulator and click **Run** (Shift+F10) in Android Studio.

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
