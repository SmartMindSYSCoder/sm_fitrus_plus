class FitrusDataModel {
  final bool connected;
  final bool error;
  final String message;
  final BodyComposition? bodyComposition;

  FitrusDataModel({
    required this.connected,
    required this.error,
    required this.message,
    this.bodyComposition,
  });

  // Factory constructor to create an instance from a Map (for JSON or event data)
  factory FitrusDataModel.fromMap(Map<String, dynamic> map) {
    // Ensure that the map keys and values are properly cast
    return FitrusDataModel(
      connected: map['connected'] ?? false,
      error: map['error'] ?? false,
      message: map['message'] ?? '',
      bodyComposition: map['bodyComposition'] != null
          ? BodyComposition.fromMap(
              Map<String, dynamic>.from(map['bodyComposition']),
            )
          : null,
    );
  }

  // Convert FitrusDataModel to Map (for JSON or event data)
  Map<String, dynamic> toMap() {
    return {
      'connected': connected,
      'error': error,
      'message': message,
      'bodyComposition': bodyComposition?.toMap(),
    };
  }
}

class BodyComposition {
  final double? bmi;
  final double? bmr;
  final double? waterPercentage;
  final double? fatMass;
  final double? fatPercentage;
  final double? muscleMass;
  final double? protein;
  final double? calorie;
  final double? minerals;

  BodyComposition({
    this.bmi,
    this.bmr,
    this.waterPercentage,
    this.fatMass,
    this.fatPercentage,
    this.muscleMass,
    this.protein,
    this.calorie,
    this.minerals,
  });

  // Factory constructor to create a BodyComposition from Map (for JSON or event data)
  factory BodyComposition.fromMap(Map<String, dynamic> map) {
    return BodyComposition(
      bmi: map['bmi'],
      bmr: map['bmr'],
      waterPercentage: map['waterPercentage'],
      fatMass: map['fatMass'],
      fatPercentage: map['fatPercentage'],
      muscleMass: map['muscleMass'],
      protein: map['protein'],
      calorie: map['calorie'],
      minerals: map['minerals'],
    );
  }

  // Convert BodyComposition to Map (for JSON or event data)
  Map<String, dynamic> toMap() {
    return {
      'bmi': bmi,
      'bmr': bmr,
      'waterPercentage': waterPercentage,
      'fatMass': fatMass,
      'fatPercentage': fatPercentage,
      'muscleMass': muscleMass,
      'protein': protein,
      'calorie': calorie,
      'minerals': minerals,
    };
  }
}
