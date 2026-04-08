# Android Runtime Packaging

Use [package_android_native_runtimes.ps1](/Users/Marlyne/Documents/New%20project/pinacle/scripts/package_android_native_runtimes.ps1) to stage local native runtime artifacts into the Android app before building.

What it stages:

- `MLC`
  - `android-native/dist/lib/mlc4j/src/main/assets/mlc-app-config.json`
  - `android-native/dist/lib/mlc4j/output/arm64-v8a/libtvm4j_runtime_packed.so`
- `whisper.cpp`
  - first matching local speech model into `app/src/main/assets/whisper-model.bin`
  - first Android `libwhisper.so` into `app/src/main/jniLibs/arm64-v8a/`
- `openWakeWord`
  - first matching wake model into `app/src/main/assets/wake-word-model.tflite`
  - first Android `libopenwakeword.so` into `app/src/main/jniLibs/arm64-v8a/`

After staging, rebuild the APK so Maya can detect the bundled runtimes from inside the installed app package.

The script also writes `runtime_bundle_manifest.json` into app assets so you can inspect what was found locally during packaging.
