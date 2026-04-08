package com.pinacle.maya.runtime.tts

data class VoiceProfile(
    val id: String,
    val displayName: String,
    val style: String,
    val backendTarget: String,
    val genderPresentation: String,
    val notes: String
)
