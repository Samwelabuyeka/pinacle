package com.pinacle.maya.runtime.wake

import com.pinacle.maya.runtime.model.AssistantRuntimeProfile

class OpenWakeWordDetector(
    private val profile: AssistantRuntimeProfile
) : WakeWordDetectorRuntime {
    private var armed = false
    private var lastStatus = "Wake-word detector armed in debug mode."

    override fun arm() {
        armed = true
        lastStatus = "Wake-word detector is armed for ${profile.assistantPersona}. Bundle openWakeWord assets to activate always-listening wake detection."
    }

    fun status(): String = lastStatus
}
