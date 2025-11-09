package com.sm.sm_fitrus_plus

import android.app.Activity
import android.content.Context
import android.util.Log
import java.util.Locale

import androidx.annotation.MainThread
import androidx.annotation.UiThread
import com.onesoftdigm.fitrus.device.sdk.FitrusBleDelegate
import com.onesoftdigm.fitrus.device.sdk.FitrusDevice
import com.onesoftdigm.fitrus.device.sdk.Gender
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

// Data classes to handle structured responses
// Data classes to handle structured responses
data class FitrusDataModel(
  val connected: Boolean,
  val error: Boolean,
  val message: String,
  val bodyComposition: BodyComposition? = null,
  val ppgData: Map<String, Any>? = null,
  val temperatureData: Map<String, Any>? = null
) {
  // Convert FitrusDataModel to Map for serialization
  fun toMap(): Map<String, Any?> {
    return mapOf(
      "connected" to connected,
      "error" to error,
      "message" to message,
      "bodyComposition" to bodyComposition?.toMap(),
      "ppgData" to ppgData,
      "temperatureData" to temperatureData
    )
  }
}

data class BodyComposition(
  val bmi: Double? = null,
  val bmr: Double? = null,
  val waterPercentage: Double? = null,
  val fatMass: Double? = null,
  val fatPercentage: Double? = null,
  val muscleMass: Double? = null,
  val protein: Double? = null,
  val calorie: Double? = null,
  val minerals: Double? = null
) {
  // Convert BodyComposition to Map for serialization
  fun toMap(): Map<String, Any?> {
    return mapOf(
      "bmi" to bmi,
      "bmr" to bmr,
      "waterPercentage" to waterPercentage,
      "fatMass" to fatMass,
      "fatPercentage" to fatPercentage,
      "muscleMass" to muscleMass,
      "protein" to protein,
      "calorie" to calorie,
      "minerals" to minerals
    )
  }
}

class SmFitrusPlusPlugin :
  FlutterPlugin,
  ActivityAware,
  MethodChannel.MethodCallHandler,
  EventChannel.StreamHandler,
  FitrusBleDelegate {

  companion object {
    private const val METHOD_CHANNEL = "fitrus/methods"
    private const val EVENT_CHANNEL = "fitrus/events"
    private const val TAG = "FitrusPlugin"
  }

  // Flutter plumbing
  private lateinit var methodChannel: MethodChannel
  private lateinit var eventChannel: EventChannel
  private var eventsSink: EventChannel.EventSink? = null

  // Android & SDK
  private var context: Context? = null
  private var activity: Activity? = null
  private var manager: FitrusDevice? = null

  private var lastHeightCm: Double? = null
  private var lastWeightKg: Double? = null

  // --- FlutterPlugin ---
  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    context = binding.applicationContext
    methodChannel = MethodChannel(binding.binaryMessenger, METHOD_CHANNEL)
    methodChannel.setMethodCallHandler(this)

    eventChannel = EventChannel(binding.binaryMessenger, EVENT_CHANNEL)
    eventChannel.setStreamHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    methodChannel.setMethodCallHandler(null)
    eventChannel.setStreamHandler(null)
    context = null
  }

  // --- ActivityAware ---
  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  // --- StreamHandler (events to Dart) ---
  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    eventsSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventsSink = null
  }

  // --- MethodCallHandler (commands from Dart) ---
  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when (call.method) {
      "initialize" -> {
        val apiKey = call.argument<String>("apiKey")
        if (apiKey.isNullOrBlank()) {
          result.error("ARG_ERROR", "apiKey is required", null)
          return
        }
        val ctx = context
        val act = activity
        if (ctx == null || act == null) {
          result.error("CTX_ERROR", "Context/Activity not available", null)
          return
        }
        manager = FitrusDevice(act, this, apiKey)
        result.success(null)
      }

      "startScan" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)
        if (m.fitrusScanState) {
          m.startFitrusScan()
          result.success(true)
        } else {
          result.success(false)
        }
      }

      "stopScan" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)
        if (m.fitrusScanState) m.stopFitrusScan()
        result.success(null)
      }

      "disconnect" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)
        if (m.fitrusConnectionState) m.disconnectFitrus()
        result.success(null)
      }

      "getDeviceInfo" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)
        m.getDeviceInfoAll()
        result.success(null)
      }

      "getBatteryInfo" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)
        m.getBatteryInfo()
        result.success(null)
      }

      "startCompMeasure" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)

        val genderStr = call.argument<String>("gender") ?: "male"
        val gender = if (genderStr.equals("female", true)) Gender.FEMALE else Gender.MALE

        val heightCm = call.argument<Double>("heightCm") ?: 170.0
        val weightKg = call.argument<Double>("weightKg") ?: 70.0
        val birth = call.argument<String>("birth") ?: "" // e.g., "19900101"
        val correct = (call.argument<Double>("correct") ?: 0.0).toFloat()
        lastHeightCm = heightCm
        lastWeightKg = weightKg
        m.startFitrusCompMeasure(
          gender,
          heightCm.toFloat(),
          weightKg.toFloat(),
          birth,
          correct
        )
        result.success(null)
      }

      else -> result.notImplemented()
    }
  }

  // --- FitrusBleDelegate (callbacks -> stream to Dart) ---
  @UiThread
  override fun handleFitrusConnected() {
    sendEvent(FitrusDataModel(
      connected = true,
      error = false,
      message = "Device connected",
      bodyComposition = null
    ))
  }

  @UiThread
  override fun handleFitrusDisconnected() {
    sendEvent(FitrusDataModel(
      connected = false,
      error = false,
      message = "Device disconnected",
      bodyComposition = null
    ))
  }

  @UiThread
  override fun handleFitrusDeviceInfo(result: Map<String, String>) {
    sendEvent(FitrusDataModel(
      connected = false,
      error = false,
      message = "Device Info received",
      bodyComposition = null
    ))
  }

  @UiThread
  override fun handleFitrusBatteryInfo(result: Map<String, Any>) {
    sendEvent(FitrusDataModel(
      connected = false,
      error = false,
      message = "Battery Info received",
      bodyComposition = null
    ))
  }

  @UiThread
  override fun handleFitrusCompMeasured(result: Map<String, String>) {
    // Helper function to safely extract a string or numerical value
    fun str(key: String): String? = result[key]
    fun num(key: String): Double? = result[key]?.toDoubleOrNull()

    // Round helper (two decimals)
//    fun round2(value: Double?): Double? =
//      value?.let { String.format("%.2f", it).toDouble() }
    fun round2(value: Double?): String? {
      return value?.let { String.format(Locale.US, "%.2f", it) }
    }
    // Raw SDK values from the result map
    val bfp = num("bfp")              // Body fat percentage
    val bfm = num("bfm")              // Fat mass (kg)
    val bmr = num("bmr")              // Basal metabolic rate
    val smm = num("smm")              // Skeletal muscle mass
    val icw = num("icw")              // Intracellular water
    val ecw = num("ecw")              // Extracellular water
    val protein = num("protein")      // Protein
    val mineral = num("mineral")      // Mineral

    val weight = lastWeightKg         // Use the last weight recorded
    val heightCm = lastHeightCm      // Use the last height recorded

    // Derived values
    val bmi: Double? = if (weight != null && heightCm != null && heightCm > 0.0) {
      val h = heightCm / 100.0
      weight / (h * h)  // BMI = weight / (height in meters)^2
    } else null

    val waterPercentage: Double? = if (weight != null && icw != null && ecw != null && weight > 0.0) {
      // Water percentage = (ICW + ECW) / weight * 100
      ((icw + ecw) / weight) * 100.0
    } else null

    // Build enriched map with rounded values
    val enriched: MutableMap<String, Any?> = mutableMapOf(
      "bmi" to round2(bmi),
      "bmr" to round2(bmr),
      "waterPercentage" to round2(waterPercentage),
      "fatMass" to round2(bfm),
      "fatPercentage" to round2(bfp),
      "muscleMass" to round2(smm),
      "protein" to round2(protein),
      "calorie" to round2(bmr),  // We use the same value for calories as BMR
      "minerals" to round2(mineral)
    )

    // Send the enriched data to Dart as part of the event
    sendEvent(FitrusDataModel(
      connected = false,
      error = false,
      message = "Body composition measured",
      bodyComposition = BodyComposition(
        bmi = enriched["bmi"] as? Double,
        bmr = enriched["bmr"] as? Double,
        waterPercentage = enriched["waterPercentage"] as? Double,
        fatMass = enriched["fatMass"] as? Double,
        fatPercentage = enriched["fatPercentage"] as? Double,
        muscleMass = enriched["muscleMass"] as? Double,
        protein = enriched["protein"] as? Double,
        calorie = enriched["calorie"] as? Double,
        minerals = enriched["minerals"] as? Double
      )
    ))

//    manager?.disconnectFitrus()

  }


  // --- Handle PPG (Heart Rate) Measurement ---
  @UiThread
  override fun handleFitrusPpgMeasured(result: Map<String, Any>) {
    // PPG data might include things like heart rate, SpO2, etc.
    val ppgData = result.mapValues { (_, v) -> v.toString() }

    sendEvent(FitrusDataModel(
      connected = false,
      error = false,
      message = "PPG measured",
      ppgData = ppgData
    ))
    manager?.disconnectFitrus()

  }
  // --- Handle Temperature Measurement ---
  @UiThread
  override fun handleFitrusTempMeasured(result: Map<String, String>) {
    // Handle temperature data (both body and object temperature)
    val tempData = result.mapValues { (_, v) -> v.toString() }

    sendEvent(FitrusDataModel(
      connected = true,
      error = false,
      message = "Temperature measured",
      temperatureData = tempData
    ))

    manager?.disconnectFitrus()

  }
  // Handle error from the SDK and send it to Dart
  @UiThread
  override fun fitrusDispatchError(error: String) {
    sendEvent(FitrusDataModel(
      connected = false,
      error = true,
      message = "Error: $error",
      bodyComposition = null
    ))
  }

  private fun sendEvent(data: FitrusDataModel) {
    eventsSink?.success(data.toMap())  // Convert to Map before sending
  }
}
