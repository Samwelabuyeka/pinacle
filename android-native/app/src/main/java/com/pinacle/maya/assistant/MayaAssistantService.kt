package com.pinacle.maya.assistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.pinacle.maya.memory.ConversationMemoryStore
import com.pinacle.maya.offline.AssistantOrchestrator
import com.pinacle.maya.security.PrivacyGuardian

class MayaAssistantService : Service() {
    private lateinit var memoryStore: ConversationMemoryStore
    private lateinit var assistantOrchestrator: AssistantOrchestrator
    private lateinit var privacyGuardian: PrivacyGuardian

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        startForeground(
            1001,
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Maya assistant active")
                .setContentText("Offline assistant runtime for voice, context, and native actions.")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setVisibility(Notification.VISIBILITY_SECRET)
                .build()
        )

        memoryStore = ConversationMemoryStore(this)
        privacyGuardian = PrivacyGuardian(this)
        assistantOrchestrator = AssistantOrchestrator.create(this, memoryStore)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        assistantOrchestrator.start()
        privacyGuardian.markBystanderDetected("assistant_service_started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Maya Assistant Runtime",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    companion object {
        private const val CHANNEL_ID = "maya-assistant-runtime"
    }
}
