package com.pinacle.maya.memory

import android.content.Context

class ConversationMemoryStore(context: Context) {
    private val preferences = context.getSharedPreferences("maya_memory", Context.MODE_PRIVATE)

    fun saveLastTurn(userPrompt: String, assistantResponse: String) {
        preferences.edit()
            .putString("last_user_prompt", userPrompt)
            .putString("last_assistant_response", assistantResponse)
            .putString("last_turn_snapshot", "User: $userPrompt\nMaya: $assistantResponse")
            .apply()
    }

    fun lastTurnSummary(): String {
        val prompt = preferences.getString("last_user_prompt", "none") ?: "none"
        val response = preferences.getString("last_assistant_response", "none") ?: "none"
        return "Last prompt: $prompt\nLast response: $response"
    }

    fun enrichPrompt(userPrompt: String): String {
        val snapshot = preferences.getString("last_turn_snapshot", "No recent turn.") ?: "No recent turn."
        return buildString {
            appendLine("Maya persona: premium offline native assistant.")
            appendLine("Recent context:")
            appendLine(snapshot)
            appendLine()
            append("Current user request: $userPrompt")
        }
    }
}
