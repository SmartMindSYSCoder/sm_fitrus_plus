import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sm_fitrus_plus/sm_fitrus_plus_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelSmFitrusPlus platform = MethodChannelSmFitrusPlus();
  const MethodChannel channel = MethodChannel('sm_fitrus_plus');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
