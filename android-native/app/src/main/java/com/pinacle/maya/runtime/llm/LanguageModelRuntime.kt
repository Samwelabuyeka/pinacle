package com.pinacle.maya.runtime.llm

interface LanguageModelRuntime {
    fun generate(prompt: String): String
}
