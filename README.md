# AndroidSysOverlay

AndroidSysOverlay is a lightweight, customizable system monitor overlay for rooted Android devices. It provides real-time statistics about your device's performance directly on your screen, with a focus on low overhead and high legibility.

## Features

- **Real-time System Metrics**:
  - CPU usage and temperature
  - GPU usage and frequency
  - RAM and Swap usage
  - Battery temperature and discharge rate
- **Highly Customizable Overlay**:
  - Adjust font size, weight, and opacity.
  - Enable text outlines and shadows for better legibility on any background.
  - Background scrim for enhanced contrast.
  - Customizable refresh rates.
- **Advanced Controls**:
  - **Position Locking**: Prevent accidental movement of the overlay.
  - **Touch Pass-Through**: Allow interactions with apps beneath the overlay.
  - **Start on Boot**: Automatically start the monitor when your device reboots.
- **Modern UI**:
  - Material 3 design with Dynamic Color (Material You) support.
  - Follows system light/dark mode settings.
- **Low Overhead**: Built with performance in mind, using `libsu` for efficient root access.

## Requirements

- **Root Access**: Required to read detailed system statistics from `/proc` and `/sys`.
- **Android 7.0 (API 24) or higher**.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/op30mmd/AndroidSysOverlay.git
   ```
2. Open the project in Android Studio.
3. Build and install the APK on your rooted device.

## Usage

1. Launch **AndroidSysOverlay** from your app drawer.
2. Grant the following permissions when prompted:
   - **Root Access**: Necessary for system metric collection.
   - **Display Over Other Apps**: Allows the overlay to appear on your screen.
   - **Notifications**: Required for the foreground service (Android 13+).
3. Configure your preferred metrics and appearance in the settings.
4. Tap **Start Overlay** to begin monitoring.
5. Drag the overlay to your preferred position. You can lock it in place via the settings.

## Configuration Options

- **Metrics**: Choose which stats to display (CPU, GPU, RAM, etc.).
- **Refresh Rate**: Set how often statistics are updated (100ms to 5s).
- **Appearance**: Customize font weight, size, outline width, and color.
- **Theming**: Toggle between Light, Dark, or System default themes. Enable wallpaper-based dynamic colors on supported devices.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

---

**GitHub**: [https://github.com/op30mmd/AndroidSysOverlay](https://github.com/op30mmd/AndroidSysOverlay)
