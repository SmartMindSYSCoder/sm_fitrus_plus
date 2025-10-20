import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:sm_fitrus_plus/sm_fitrus_plus.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String apiKey =
      "vrmCquCRjqTKGQNt3b9pEYy6NhjOL45Mi3d56I16RGTuCAeDNXW53kDaJGn7KUii5SAnHAdtcNoIlnJUk5M5HIj3mJpKAzsIIDilz0bKwdIekWot5X1KyCBMUXBGmICS";
  final _smFitrusPlusPlugin = SmFitrusPlus();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {} on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
  }

  String data = "";
  listenData() {
    SmFitrusPlus.events.listen((e) async {
      if (e.connected && e.bodyComposition == null) {
        SmFitrusPlus.startCompMeasure(
          gender: 'male',
          heightCm: 150,
          weightKg: 55,
          birth: "1997/12/03",
        );
      }

      data = "${e.toMap()}";
      print(data);
      setState(() {});
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Plugin example app')),
        body: Center(
          child: Column(
            children: [
              ElevatedButton(
                onPressed: () {
                  SmFitrusPlus.initialize(apiKey);
                  listenData();
                },
                child: Text("init"),
              ),
              SizedBox(height: 20),

              ElevatedButton(
                onPressed: () {
                  SmFitrusPlus.startScan();
                },
                child: Text("scan"),
              ),
              SizedBox(height: 20),
              ElevatedButton(
                onPressed: () {
                  SmFitrusPlus.startCompMeasure(
                    gender: 'male',
                    heightCm: 150,
                    weightKg: 55,
                    birth: "1997/12/03",
                  );
                },
                child: Text("Start measure"),
              ),
              SizedBox(height: 20),

              Text("Data:\n$data"),
            ],
          ),
        ),
      ),
    );
  }
}
