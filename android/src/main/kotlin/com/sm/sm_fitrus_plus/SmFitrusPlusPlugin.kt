package com.sm.sm_fitrus_plus

import android.app.Activity
import android.content.Context
import android.util.Log
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
          // In your sample, you show warning when BT is not available.
          // Here we just return false to let Dart decide what to do.
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

      "startHeartRateMeasure" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)
        m.startFitrusHeartRateMeasure()
        result.success(null)
      }

      "startBloodPressure" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)
        val baseSys = (call.argument<Double>("baseSystolic") ?: 120.0).toFloat()
        val baseDia = (call.argument<Double>("baseDiastolic") ?: 80.0).toFloat()
        m.StartFitrusBloodPressure(baseSys, baseDia)
        result.success(null)
      }

      "startStressMeasure" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)
        val birth = call.argument<String>("birth") ?: ""
        m.startFitrusStressMeasure(birth)
        result.success(null)
      }

      "startTempBody" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)
        m.startFitrusTempBodyMeasure()
        result.success(null)
      }

      "startTempObject" -> {
        val m = manager ?: return result.error("NOT_INIT", "Call initialize first", null)
        m.startFitrusTempObjectMeasure()
        result.success(null)
      }

      else -> result.notImplemented()
    }
  }

  // --- FitrusBleDelegate (callbacks -> stream to Dart) ---
  @UiThread
  override fun handleFitrusConnected() {
    sendEvent(mapOf("type" to "connection","status" to true))
  }

  @UiThread
  override fun handleFitrusDisconnected() {
    sendEvent(mapOf("type" to "connection","status" to false ))
  }

  @UiThread
  override fun handleFitrusDeviceInfo(result: Map<String, String>) {
    sendEvent(mapOf("type" to "deviceInfo", "data" to result))
  }

  @UiThread
  override fun handleFitrusBatteryInfo(result: Map<String, Any>) {
    sendEvent(mapOf("type" to "batteryInfo", "data" to result))
  }

  @UiThread
  override fun handleFitrusCompMeasured(result: Map<String, String>) {
//    sendEvent(mapOf("type" to "compMeasured", "data" to result))


    fun str(key: String): String? = result[key]
    fun num(key: String): Double? = result[key]?.toDoubleOrNull()

//     Round helper (two decimals)
    fun round2(value: Double?): Double? =
      value?.let { String.format("%.2f", it).toDouble() }

    // Raw SDK values
    val bfp = num("bfp")              // Body fat percentage
    val bfm = num("bfm")              // Fat mass (kg)
    val bmr = num("bmr")              // Basal metabolic rate
    val smm = num("smm")              // Skeletal muscle mass
    val icw = num("icw")              // Intracellular water
    val ecw = num("ecw")              // Extracellular water
    val protein = num("protein")      // Protein
    val mineral = num("mineral")      // Mineral

    val weight = lastWeightKg
    val heightCm = lastHeightCm

    // Derived
    val bmi: Double? = if (weight != null && heightCm != null && heightCm > 0.0) {
      val h = heightCm / 100.0
      weight / (h * h)
    } else null

    val waterPercentage: Double? = if (weight != null && icw != null && ecw != null && weight > 0.0) {
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
      "calorie" to round2(bmr),
      "minerals" to round2(mineral)
    )

//    // Include original values (also rounded) under "raw"
//    val roundedRaw = result.mapValues { (_, v) ->
//      v.toDoubleOrNull()?.let { String.format("%.2f", it).toDouble() } ?: v
//    }
//
//    enriched["raw"] = roundedRaw
//
    sendEvent(mapOf("type" to "compMeasured", "data" to enriched))

  }

  @UiThread
  override fun handleFitrusPpgMeasured(result: Map<String, Any>) {
    sendEvent(mapOf("type" to "ppgMeasured", "data" to result))
  }

  @UiThread
  override fun handleFitrusTempMeasured(result: Map<String, String>) {
    sendEvent(mapOf("type" to "tempMeasured", "data" to result))
  }

  @UiThread
  override fun fitrusDispatchError(error: String) {
    sendEvent(mapOf("type" to "error", "message" to error))
  }

  // --- helpers ---
  @MainThread
  private fun sendEvent(payload: Map<String, Any?>) {
    try {
      eventsSink?.success(payload)
    } catch (t: Throwable) {
      Log.e(TAG, "sendEvent failed", t)
    }
  }
}
