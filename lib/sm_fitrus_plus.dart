import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'fitrus_data_model.dart';

class SmFitrusPlus {
  static const _method = MethodChannel('fitrus/methods');
  static const _events = EventChannel('fitrus/events');

  static Stream<FitrusDataModel> get events =>
      _events.receiveBroadcastStream().map((e) {
        return FitrusDataModel.fromMap(Map<String, dynamic>.from(e as Map));
      });

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

  static Map<String, dynamic> _validateCompInputs({
    required String gender,
    required double heightCm,
    required double weightKg,
    required String birth,
  }) {
    final normalizedGender = gender.trim().toLowerCase();

    // 1️⃣ Gender: Must be 'male' or 'female'
    if (normalizedGender != 'male' && normalizedGender != 'female') {
      return {"status": false, "message": "Gender must be 'male' or 'female'."};
    }

    // 2️⃣ Height: Must be between 50 cm and 250 cm
    if (heightCm.isNaN || heightCm < 50 || heightCm > 250) {
      return {
        "status": false,
        "message": "Height must be between 50 and 250 cm.",
      };
    }

    // 3️⃣ Weight: Must be between 10 kg and 300 kg
    if (weightKg.isNaN || weightKg < 10 || weightKg > 300) {
      return {
        "status": false,
        "message": "Weight must be between 10 and 300 kg.",
      };
    }

    // 4️⃣ Birth Date: Must match the pattern yyyy/MM/dd
    final birthPattern = RegExp(r'^\d{4}/\d{2}/\d{2}$');
    if (!birthPattern.hasMatch(birth)) {
      return {
        "status": false,
        "message": "Birth must be in the format yyyy/MM/dd (e.g., 1990/01/01).",
      };
    }

    // 5️⃣ Validate Birth Date (yyyy/MM/dd format) and check if it's a valid date
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

    // 6️⃣ Ensure User is at least 5 years old
    final today = DateTime.now();
    final minAllowed = DateTime(today.year - 5, today.month, today.day);
    if (birthDate!.isAfter(minAllowed)) {
      return {"status": false, "message": "User must be at least 5 years old."};
    }

    // ✅ Everything is valid
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
