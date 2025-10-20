# SmFitrusPlus Flutter Plugin

[![GitHub](https://img.shields.io/github/license/SmartMindSYSCoder/sm_fitrus_plus)](https://github.com/SmartMindSYSCoder/sm_fitrus_plus/blob/main/LICENSE)

This Flutter plugin allows integration with the **Fitrus device SDK** to perform various health-related measurements, including **body composition**, **heart rate**, **blood pressure**, **temperature**, and **stress levels**. It enables seamless communication between Flutter and the native Android SDK for Fitrus devices.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Event Streaming](#event-streaming)
- [API Reference](#api-reference)
- [Example](#example)
- [Validation](#validation)
- [Contributing](#contributing)

## Features

- **Device Initialization**: Initialize the Fitrus device with an API key
- **Measurement Support**:
  - Body composition (BMI, fat percentage, muscle mass, etc.)
  - Heart rate measurement (PPG)
  - Blood pressure measurement
  - Stress level measurement
  - Body and object temperature measurements
- **Event Handling**: Receive real-time results and events like measurements and errors from the device

## Installation

Since the `sm_fitrus_plus` plugin is not published on `pub.dev`, you need to link to the GitHub repository directly. Add the following to your `pubspec.yaml`:

```yaml
dependencies:
  sm_fitrus_plus:
    git:
      url: https://github.com/SmartMindSYSCoder/sm_fitrus_plus.git
```

Then, run the following command to fetch the plugin:

```bash
flutter pub get
```

## Usage

### 1. Initialization

Initialize the Fitrus device with your API key:

```dart
import 'package:sm_fitrus_plus/sm_fitrus_plus.dart';

await SmFitrusPlus.initialize('YOUR_API_KEY');
```

### 2. Start Scan

Start scanning for nearby Fitrus devices:

```dart
bool isScanning = await SmFitrusPlus.startScan();
```

### 3. Stop Scan

Stop the scan for nearby Fitrus devices:

```dart
await SmFitrusPlus.stopScan();
```

### 4. Disconnect

Disconnect from the Fitrus device:

```dart
await SmFitrusPlus.disconnect();
```

### 5. Get Device Info

Retrieve information about the connected Fitrus device:

```dart
await SmFitrusPlus.getDeviceInfo();
```

### 6. Get Battery Info

Get battery information from the Fitrus device:

```dart
await SmFitrusPlus.getBatteryInfo();
```

### 7. Start Body Composition Measurement

Start a body composition measurement (BMI, fat percentage, muscle mass, etc.):

```dart
await SmFitrusPlus.startCompMeasure(
  gender: 'male',
  heightCm: 170.0,
  weightKg: 70.0,
  birth: '19900101',
);
```

### 8. Start Heart Rate Measurement

Start a heart rate (PPG) measurement:

```dart
await SmFitrusPlus.startHeartRateMeasure();
```

### 9. Start Blood Pressure Measurement

Start a blood pressure measurement:

```dart
await SmFitrusPlus.startBloodPressure(
  baseSystolic: 120.0,
  baseDiastolic: 80.0,
);
```

### 10. Start Stress Measurement

Start a stress level measurement:

```dart
await SmFitrusPlus.startStressMeasure('19900101');
```

### 11. Start Body Temperature Measurement

Start a body temperature measurement:

```dart
await SmFitrusPlus.startTempBody();
```

### 12. Start Object Temperature Measurement

Start an object temperature measurement:

```dart
await SmFitrusPlus.startTempObject();
```

## Event Streaming

The plugin streams measurement results and other events via the `events` stream. Listen to these events to receive real-time updates from the Fitrus device:

```dart
SmFitrusPlus.events.listen((event) {
  print(event.message);
  
  if (event.bodyComposition != null) {
    print("BMI: ${event.bodyComposition?.bmi}");
  }
});
```

### Event Types

- **Device Connection Events**: Information about device connection or disconnection status
- **Measurement Results**: Results from measurements such as body composition, heart rate, temperature, and more
- **Errors**: Any errors encountered during communication with the device

## API Reference

### Methods

| Method | Description |
|--------|-------------|
| `initialize(String apiKey)` | Initializes the Fitrus device with the provided API key |
| `startScan()` | Starts scanning for Fitrus devices |
| `stopScan()` | Stops the scan for Fitrus devices |
| `disconnect()` | Disconnects from the Fitrus device |
| `getDeviceInfo()` | Retrieves device information |
| `getBatteryInfo()` | Retrieves battery information |
| `startCompMeasure()` | Starts a body composition measurement |
| `startHeartRateMeasure()` | Starts a heart rate (PPG) measurement |
| `startBloodPressure()` | Starts a blood pressure measurement |
| `startStressMeasure(String birth)` | Starts a stress level measurement |
| `startTempBody()` | Starts a body temperature measurement |
| `startTempObject()` | Starts an object temperature measurement |

### Event Model (FitrusDataModel)

The `FitrusDataModel` contains the following fields:

- **connected** (`bool`): Indicates if the device is connected
- **error** (`bool`): Indicates if there was an error
- **message** (`String`): A message with information about the event
- **bodyComposition** (`BodyComposition?`): Optional object containing body composition measurement results (BMI, fat percentage, etc.)
- **ppgData** (`Map?`): Optional map of PPG data (e.g., heart rate)
- **temperatureData** (`Map?`): Optional map of temperature data (body or object temperature)

## Example

Here's a complete example of how to use the SmFitrusPlus plugin in a Flutter application:

```dart
import 'package:flutter/material.dart';
import 'package:sm_fitrus_plus/sm_fitrus_plus.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Fitrus Plugin Demo',
      home: Scaffold(
        appBar: AppBar(
          title: Text('Fitrus Plugin Example'),
        ),
        body: Center(
          child: ElevatedButton(
            onPressed: () async {
              // Initialize the Fitrus device
              await SmFitrusPlus.initialize('YOUR_API_KEY');
              
              // Start scanning for nearby devices
              bool isScanning = await SmFitrusPlus.startScan();
              if (isScanning) {
                print("Scanning for devices...");
              }

              // Start body composition measurement
              await SmFitrusPlus.startCompMeasure(
                gender: 'male',
                heightCm: 170.0,
                weightKg: 70.0,
                birth: '19900101',
              );

              // Listen for events (measurement results, errors, etc.)
              SmFitrusPlus.events.listen((event) {
                print(event.message);
                if (event.bodyComposition != null) {
                  print("BMI: ${event.bodyComposition?.bmi}");
                }
              });
            },
            child: Text('Start Measurement'),
          ),
        ),
      ),
    );
  }
}
```

## Validation

When starting body composition measurements, the plugin validates the input values. The `_validateCompInputs` function ensures:

- **Height**: Between 50 and 250 cm
- **Weight**: Between 10 and 300 kg
- **Gender**: Either `'male'` or `'female'`
- **Birth date**: In the format `yyyy/MM/dd` and ensures the user is at least 5 years old

## Contributing

Contributions are welcome! If you find any bugs or have ideas for improvements, please feel free to create a pull request. We appreciate your help in making this plugin better.

---

**License**: This project is licensed under the [LICENSE](https://github.com/SmartMindSYSCoder/sm_fitrus_plus/blob/main/LICENSE) - see the LICENSE file for details.