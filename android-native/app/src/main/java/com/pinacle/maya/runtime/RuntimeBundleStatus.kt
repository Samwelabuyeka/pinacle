package com.pinacle.maya.runtime

import android.content.Context
import java.io.File

data class RuntimeBundleStatus(
    val mlcReady: Boolean,
    val mlcNotes: String,
    val whisperReady: Boolean,
    val whisperNotes: String,
    val wakeWordReady: Boolean,
    val wakeWordNotes: String
) {
    fun describe(): String {
        return buildString {
            appendLine("Native runtime bundles:")
            appendLine("MLC: ${if (mlcReady) "bundled" else "not bundled"} - $mlcNotes")
            appendLine("Whisper: ${if (whisperReady) "bundled" else "not bundled"} - $whisperNotes")
            append("Wake word: ${if (wakeWordReady) "bundled" else "not bundled"} - $wakeWordNotes")
        }
    }
}

class RuntimeBundleStatusStore(private val context: Context) {
    fun load(): RuntimeBundleStatus {
        val nativeLibraryDir = File(context.applicationInfo.nativeLibraryDir ?: "")
        return RuntimeBundleStatus(
            mlcReady = assetExists("mlc-app-config.json") || nativeLibraryDir.resolve("libtvm4j_runtime_packed.so").exists(),
            mlcNotes = if (assetExists("mlc-app-config.json")) {
                "MLC config asset is packaged with the app."
            } else {
                "Bundle dist/lib/mlc4j output so Maya can run the phone-primary model on device."
            },
            whisperReady = assetExists("whisper-model.bin") || nativeLibraryDir.resolve("libwhisper.so").exists(),
            whisperNotes = if (assetExists("whisper-model.bin") || nativeLibraryDir.resolve("libwhisper.so").exists()) {
                "Offline speech runtime assets are present."
            } else {
                "Package whisper.cpp native library and a local speech model for microphone transcription."
            },
            wakeWordReady = assetExists("wake-word-model.tflite") || nativeLibraryDir.resolve("libopenwakeword.so").exists(),
            wakeWordNotes = if (assetExists("wake-word-model.tflite") || nativeLibraryDir.resolve("libopenwakeword.so").exists()) {
                "Wake-word detector assets are present."
            } else {
                "Package openWakeWord or an equivalent wake model for always-on activation."
            }
        )
    }

    private fun assetExists(name: String): Boolean {
        return runCatching {
            context.assets.open(name).close()
            true
        }.getOrDefault(false)
    }
}
