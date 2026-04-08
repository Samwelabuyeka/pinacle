package com.pinacle.maya.runtime.llm

import android.content.Context
import java.io.File

data class MlcBindingStatus(
    val available: Boolean,
    val reason: String
)

class MlcAndroidBinding(private val context: Context) {
    fun status(): MlcBindingStatus {
        val nativeLibraryDir = File(context.applicationInfo.nativeLibraryDir ?: "")
        val hasRuntimeLibrary = nativeLibraryDir.resolve("libtvm4j_runtime_packed.so").exists()
        val hasConfigAsset = runCatching {
            context.assets.open("mlc-app-config.json").close()
            true
        }.getOrDefault(false)

        return if (hasRuntimeLibrary || hasConfigAsset) {
            MlcBindingStatus(true, "MLC Android assets are bundled in the installed app package.")
        } else {
            MlcBindingStatus(
                false,
                "MLC Android assets are not bundled in this build yet. Package the mlc4j output and model config into the app."
            )
        }
    }
}
