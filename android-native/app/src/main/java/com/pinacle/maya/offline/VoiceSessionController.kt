package com.pinacle.maya.offline

import com.pinacle.maya.security.OwnerSessionStore
import com.pinacle.maya.security.PrivacyGuardian
import com.pinacle.maya.security.PrivacyPolicyEngine
import com.pinacle.maya.security.SecurityProfileStore

class VoiceSessionController(
    private val inferenceCoordinator: OfflineInferenceCoordinator,
    private val deviceActionRouter: DeviceActionRouter,
    private val memoryStore: com.pinacle.maya.memory.ConversationMemoryStore,
    private val privacyPolicyEngine: PrivacyPolicyEngine,
    private val privacyGuardian: PrivacyGuardian
) {
    private val actionPlanner = AssistantActionPlanner()
    private val responseComposer = MayaResponseComposer(memoryStore)

    fun start() {
        // Wake-word detection and streaming audio orchestration live here.
    }

    fun handlePrompt(prompt: String): String {
        val plan = actionPlanner.plan(prompt)
        val privacyDecision = privacyPolicyEngine.evaluate(prompt, plan.requestedActions)
        if (privacyDecision.requireOwnerVerification) {
            privacyGuardian.markBystanderDetected("sensitive_request_without_owner_verification")
            val protectedResponse = privacyDecision.reason
            memoryStore.saveLastTurn(prompt, protectedResponse)
            return protectedResponse
        }
        val actionResults = mutableListOf<String>()
        plan.requestedActions.forEach { action ->
            actionResults += deviceActionRouter.execute(action, plan.actionArguments)
        }
        val enrichedPrompt = memoryStore.enrichPrompt(prompt)
        val response = inferenceCoordinator.generateResponse(
            prompt = enrichedPrompt,
            preferHeavyModel = plan.shouldEscalateToHeavyModel
        )
        val finalResponse = responseComposer.compose(
            prompt = prompt,
            modelOutput = if (actionResults.isEmpty()) response else actionResults.joinToString(prefix = "Actions: ", separator = ", ") + "\n" + response,
            actionResults = actionResults,
            blurResponse = privacyDecision.blurResponse,
            privacyPolicyEngine = privacyPolicyEngine
        )
        memoryStore.saveLastTurn(prompt, finalResponse)
        inferenceCoordinator.synthesizeSpeech(finalResponse)
        return finalResponse
    }
}
