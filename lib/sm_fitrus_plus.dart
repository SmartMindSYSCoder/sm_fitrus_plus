import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class SmFitrusPlus {
  static const _method = MethodChannel('fitrus/methods');
  static const _events = EventChannel('fitrus/events');

  static Stream<Map> get events =>
      _events.receiveBroadcastStream().map((e) => Map.from(e as Map));

  static Future<void> initialize(String apiKey) =>
      _method.invokeMethod('initialize', {'apiKey': apiKey});

  static Future<bool> startScan() async =>
      await _method.invokeMethod('startScan') ?? false;

  static Future<void> stopScan() => _method.invokeMethod('stopScan');

  static Future<void> disconnect() => _method.invokeMethod('disconnect');

  static Future<void> getDeviceInfo() => _method.invokeMethod('getDeviceInfo');

  static Future<void> getBatteryInfo() =>
      _method.invokeMethod('getBatteryInfo');

  static Future<void> startCompMeasure({
    required String gender, // 'male' | 'female'
    required double heightCm,
    required double weightKg,
    required String birth, // e.g. '19900101'
    double correct = 0.0,
  }) async {
    final validation = _validateCompInputs(
      gender: gender,
      heightCm: heightCm,
      weightKg: weightKg,
      birth: birth,
    );
    if (validation["status"] == false) {
      // You can throw or just print / return the message
      debugPrint(validation["message"] ?? "Invalid input");
      return;
    }
    return _method.invokeMethod('startCompMeasure', {
      'gender': gender,
      'heightCm': heightCm,
      'weightKg': weightKg,
      'birth': birth,
      'correct': correct,
    });
  }

  /// Validates user input for body composition measurement.
  /// Returns:
  ///   { "status": true }  → valid
  ///   { "status": false, "message": "reason" }  → invalid
  static Map<String, dynamic> _validateCompInputs({
    required String gender,
    required double heightCm,
    required double weightKg,
    required String birth,
  }) {
    final normalizedGender = gender.trim().toLowerCase();

    // 1️⃣ Gender
    if (normalizedGender != 'male' && normalizedGender != 'female') {
      return {"status": false, "message": "Gender must be 'male' or 'female'."};
    }

    // 2️⃣ Height (reasonable range)
    if (heightCm.isNaN || heightCm < 50 || heightCm > 250) {
      return {
        "status": false,
        "message": "Height must be between 50 and 300 cm.",
      };
    }

    // 3️⃣ Weight (reasonable range)
    if (weightKg.isNaN || weightKg < 10 || weightKg > 300) {
      return {
        "status": false,
        "message": "Weight must be between 10 and 300 kg.",
      };
    }

    // 4️⃣ Birth format: yyyy/MM/dd
    final birthPattern = RegExp(r'^\d{4}/\d{2}/\d{2}$');
    if (!birthPattern.hasMatch(birth)) {
      return {
        "status": false,
        "message": "Birth must be in format yyyy/MM/dd (e.g. 1997/12/03).",
      };
    }

    // 5️⃣ Birth date validity
    DateTime? birthDate;
    try {
      final parts = birth.split('/');
      final year = int.parse(parts[0]);
      final month = int.parse(parts[1]);
      final day = int.parse(parts[2]);
      birthDate = DateTime(year, month, day);
      if (birthDate.year != year ||
          birthDate.month != month ||
          birthDate.day != day) {
        return {"status": false, "message": "Birth date is invalid."};
      }
    } catch (_) {
      return {"status": false, "message": "Birth date is invalid."};
    }

    // 6️⃣ Must be older than 5 years
    final today = DateTime.now();
    final minAllowed = DateTime(today.year - 5, today.month, today.day);
    if (birthDate.isAfter(minAllowed)) {
      return {"status": false, "message": "User must be at least 5 years old."};
    }

    // ✅ Everything ok
    return {"status": true};
  }

  static Future<void> startHeartRateMeasure() =>
      _method.invokeMethod('startHeartRateMeasure');

  static Future<void> startBloodPressure({
    required double baseSystolic,
    required double baseDiastolic,
  }) => _method.invokeMethod('startBloodPressure', {
    'baseSystolic': baseSystolic,
    'baseDiastolic': baseDiastolic,
  });

  static Future<void> startStressMeasure(String birth) =>
      _method.invokeMethod('startStressMeasure', {'birth': birth});

  static Future<void> startTempBody() => _method.invokeMethod('startTempBody');

  static Future<void> startTempObject() =>
      _method.invokeMethod('startTempObject');
}
