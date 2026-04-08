# MLC Android Binding

Maya is wired to the official `MLC LLM Android` packaging shape.

According to the official MLC Android docs, the generated Android package should place a Gradle subproject under:

- `dist/lib/mlc4j/build.gradle`
- `dist/lib/mlc4j/output/arm64-v8a/libtvm4j_runtime_packed.so`
- `dist/lib/mlc4j/output/tvm4j_core.jar`
- `dist/lib/mlc4j/src/main/assets/mlc-app-config.json`

The Android project now:

- conditionally includes `:mlc4j` from `dist/lib/mlc4j`
- conditionally adds `implementation(project(":mlc4j"))`
- probes for the generated package in `MlcAndroidBinding.kt`

This means the current APK builds without the package, and it is ready to attach the real runtime as soon as the MLC packaging step is completed.
