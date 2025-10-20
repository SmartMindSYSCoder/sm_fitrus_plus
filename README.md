# sm_fitrus_plus

# **SmFitrusPlus Flutter Plugin**

[![GitHub](https://img.shields.io/github/license/SmartMindSYSCoder/sm_fitrus_plus)](https://github.com/SmartMindSYSCoder/sm_fitrus_plus/blob/main/LICENSE)

This Flutter plugin allows integration with the **Fitrus device SDK** to perform various health-related measurements, including **body composition**, **heart rate**, **blood pressure**, **temperature**, and **stress levels**. It enables communication between Flutter and the native Android SDK for Fitrus devices.

---

## **Features**
- **Device Initialization**: Initialize the Fitrus device with an API key.
- **Measurement Support**:
    - Body composition (BMI, fat percentage, muscle mass, etc.)
    - Heart rate measurement (PPG)
    - Blood pressure measurement
    - Stress level measurement
    - Body and object temperature measurements
- **Event Handling**: Receive real-time results and events like measurements and errors from the device.

---

## **Installation**

To use the `sm_fitrus_plus` plugin in your Flutter project, you need to link to the GitHub repository directly, as it’s not published on `pub.dev`. Add the following to your `pubspec.yaml`:

```yaml
dependencies:
  sm_fitrus_plus:
    git:
      url: https://github.com/SmartMindSYSCoder/sm_fitrus_plus.git
```


Then, run flutter pub get to fetch the plugin.

Usage
1. Initialization

To initialize the Fitrus device, you'll need to call the initialize() method with your API key.

import 'package:sm_fitrus_plus/sm_fitrus_plus.dart';

// Initialize with the API key
await SmFitrusPlus.initialize('YOUR_API_KEY');

2. Start Scan

To start scanning for nearby Fitrus devices, use the startScan() method.

bool isScanning = await SmFitrusPlus.startScan();

3. Stop Scan

To stop the scan for nearby Fitrus devices, use the stopScan() method.

    await SmFitrusPlus.stopScan();

4. Disconnect

To disconnect from the Fitrus device, use the disconnect() method.

    await SmFitrusPlus.disconnect();

5. Get Device Info

To get information about the connected Fitrus device, use the getDeviceInfo() method.

await SmFitrusPlus.getDeviceInfo();

6. Get Battery Info

To get battery information from the Fitrus device, use the getBatteryInfo() method.

    await SmFitrusPlus.getBatteryInfo();

7. Start Body Composition Measurement

Start a body composition measurement (BMI, fat percentage, muscle mass, etc.) with the startCompMeasure() method.

await SmFitrusPlus.startCompMeasure(
  gender: 'male',
  heightCm: 170.0,
  weightKg: 70.0,
  birth: '19900101',
);

8. Start Heart Rate Measurement

To start a heart rate (PPG) measurement, use the startHeartRateMeasure() method.

await SmFitrusPlus.startHeartRateMeasure();

9. Start Blood Pressure Measurement

To start a blood pressure measurement, use the startBloodPressure() method.

await SmFitrusPlus.startBloodPressure(
  baseSystolic: 120.0,
  baseDiastolic: 80.0,
);

10. Start Stress Measure

To start a stress level measurement, use the startStressMeasure() method.

await SmFitrusPlus.startStressMeasure('19900101');

11. Start Body Temperature Measurement

To start a body temperature measurement, use the startTempBody() method.

await SmFitrusPlus.startTempBody();

12. Start Object Temperature Measurement

To start an object temperature measurement, use the startTempObject() method.

await SmFitrusPlus.startTempObject();

Event Streaming

The plugin streams measurement results and other events via the events stream. You can listen to the events stream to receive updates from the native Fitrus device.

SmFitrusPlus.events.listen((event) {
  // Handle the event
  print(event.message);
  if (event.bodyComposition != null) {
    print("BMI: ${event.bodyComposition?.bmi}");
  }
});

Event Types

Device Connection Events: Information on whether the device is connected or disconnected.

Measurement Results: Contains results of measurements such as body composition, heart rate, and more.

Errors: Any errors encountered while communicating with the device.

API Reference
Methods

initialize(String apiKey): Initializes the Fitrus device with the provided API key.

startScan(): Starts scanning for Fitrus devices.

stopScan(): Stops the scan for Fitrus devices.

disconnect(): Disconnects from the Fitrus device.

getDeviceInfo(): Retrieves device information.

getBatteryInfo(): Retrieves battery information.

startCompMeasure(): Starts a body composition measurement (BMI, fat percentage, etc.).

startHeartRateMeasure(): Starts a heart rate (PPG) measurement.

startBloodPressure(): Starts a blood pressure measurement.

startStressMeasure(String birth): Starts a stress level measurement.

startTempBody(): Starts a body temperature measurement.

startTempObject(): Starts an object temperature measurement.

Event Model (FitrusDataModel)

The FitrusDataModel contains the following fields:

connected: A bool indicating if the device is connected.

error: A bool indicating if there was an error.

message: A String message with information about the event.

bodyComposition: An optional BodyComposition object containing the results of a body composition measurement (BMI, fat percentage, etc.).

ppgData: Optional map of PPG data (e.g., heart rate).

temperatureData: Optional map of temperature data (body or object temperature).

Example

Here’s a full example of how to use the SmFitrusPlus plugin in a Flutter application:

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

Validation

When starting body composition measurements, the plugin validates the input values for height, weight, gender, and birth date. The _validateCompInputs function ensures:

Height is between 50 and 250 cm

Weight is between 10 and 300 kg

Gender is either 'male' or 'female'

Birth date is in the format yyyy/MM/dd and ensures the user is at least 5 years old

Contributing

If you find any bugs or want to improve the plugin, feel free to create a pull request! All contributions are welcome.

