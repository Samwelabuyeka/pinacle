package com.pinacle.maya.runtime

import android.content.Context

class ModelManifestStore(private val context: Context) {
    fun loadRaw(): String {
        return context.assets.open("maya_model_manifest.json").bufferedReader().use { it.readText() }
    }
}
