import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'sm_fitrus_plus_method_channel.dart';

abstract class SmFitrusPlusPlatform extends PlatformInterface {
  /// Constructs a SmFitrusPlusPlatform.
  SmFitrusPlusPlatform() : super(token: _token);

  static final Object _token = Object();

  static SmFitrusPlusPlatform _instance = MethodChannelSmFitrusPlus();

  /// The default instance of [SmFitrusPlusPlatform] to use.
  ///
  /// Defaults to [MethodChannelSmFitrusPlus].
  static SmFitrusPlusPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [SmFitrusPlusPlatform] when
  /// they register themselves.
  static set instance(SmFitrusPlusPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
