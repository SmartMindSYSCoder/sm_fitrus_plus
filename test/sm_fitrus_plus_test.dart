import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:sm_fitrus_plus/sm_fitrus_plus_method_channel.dart';
import 'package:sm_fitrus_plus/sm_fitrus_plus_platform_interface.dart';

class MockSmFitrusPlusPlatform
    with MockPlatformInterfaceMixin
    implements SmFitrusPlusPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final SmFitrusPlusPlatform initialPlatform = SmFitrusPlusPlatform.instance;

  test('$MethodChannelSmFitrusPlus is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelSmFitrusPlus>());
  });
}
