package com.pinacle.maya.assistant

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MayaNotificationListenerService : NotificationListenerService() {
    private val stateStore by lazy { AssistantDeviceStateStore(this) }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val packageName = sbn?.packageName ?: return
        val title = sbn.notification.extras?.getCharSequence("android.title")?.toString().orEmpty()
        val text = sbn.notification.extras?.getCharSequence("android.text")?.toString().orEmpty()
        val visibility = if (sbn.notification.visibility == android.app.Notification.VISIBILITY_SECRET) {
            "secret"
        } else {
            "visible"
        }
        stateStore.saveLastNotification("$packageName | $title | $text | visibility=$visibility")
    }
}
