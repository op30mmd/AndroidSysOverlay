# AndroidSysOverlay

[![Build](https://github.com/op30mmd/AndroidSysOverlay/actions/workflows/android.yml/badge.svg)](https://github.com/op30mmd/AndroidSysOverlay/actions/workflows/android.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A comprehensive, lightweight system monitor overlay for rooted Android devices. AndroidSysOverlay provides real-time performance metrics directly on your screen, allowing you to monitor system health and resource usage while using other applications.

## Features

- **Real-time Monitoring**: Track essential system metrics including:
  - CPU Usage (Total and Per-Core)
  - RAM & SWAP Usage
  - GPU Load & Frequency
  - Thermal Data (CPU & Battery)
  - Battery Discharge Rate
- **Highly Customizable Overlay**:
  - **Layouts**: Choose between Vertical, Horizontal, or Grid layouts.
  - **Appearance**: Adjust font size, weight, colors, opacity, and text outlines for maximum legibility.
  - **Theming**: Supports Light/Dark modes and Material You dynamic colors (Android 12+).
  - **Visibility**: Toggle labels, units, and specific metrics.
- **Advanced Interaction**:
  - **Position Locking**: Lock the overlay in place to prevent accidental movement.
  - **Touch Pass-through**: Interact with apps behind the overlay without moving it.
- **Performance Optimized**: Built with `libsu` for efficient root-level stat collection from `/proc` and `/sys`.
- **Start on Boot**: Option to automatically start the monitoring service when the device boots.

## Requirements

- **Root Access**: Required to read detailed system statistics (CPU/GPU/Thermals) from protected system nodes.
- **Android 7.0 (API 24)** or higher.

## Installation

1. Download the latest APK from the [Releases](https://github.com/op30mmd/AndroidSysOverlay/releases) page.
2. Install the APK on your rooted device.
3. Grant Root permissions when prompted by your superuser manager (e.g., Magisk, KernelSU).
4. Enable the "Display over other apps" permission in system settings.

## Usage

1. Open the **Root Overlay** app.
2. Grant the necessary permissions (Root and System Overlay).
3. Configure your desired metrics and appearance settings in the main UI.
4. Toggle the service switch to start the overlay.
5. Long-press and drag the overlay to reposition it.
6. Enable **Lock Position** in settings if you want to prevent further movement.

## Development

The project is built using:
- **Kotlin** & **Jetpack Compose** for the configuration UI.
- **libsu** for shell management and root access.
- **Jetpack DataStore** for settings persistence.
- **Gradle Version Catalogs** for dependency management.

### Building from Source

```bash
./gradlew assembleDebug
```

## License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.

```text
Copyright 2024 AndroidSysOverlay Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
