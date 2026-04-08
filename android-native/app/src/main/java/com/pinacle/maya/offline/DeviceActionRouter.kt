package com.pinacle.maya.offline

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import com.pinacle.maya.assistant.AssistantDeviceStateStore
import com.pinacle.maya.capabilities.SystemCapabilityManager

class DeviceActionRouter(private val context: Context) {
    private val capabilityManager = SystemCapabilityManager(context)
    private val deviceStateStore = AssistantDeviceStateStore(context)

    fun execute(intentName: String, arguments: Map<String, String> = emptyMap()): String {
        return when (intentName) {
            "create_reminder" -> launchReminder(arguments)
            "create_alarm" -> launchAlarm(arguments)
            "send_sms" -> launchSms(arguments)
            "call_contact" -> launchCall(arguments)
            "open_app" -> launchApp(arguments)
            "browse_web" -> browseWeb(arguments)
            "search_web" -> searchWeb(arguments)
            "open_maps" -> openMaps(arguments)
            "send_email" -> sendEmail(arguments)
            "open_settings" -> openSettings()
            "open_default_apps" -> openDefaultApps()
            "read_notifications" -> openNotificationAccess()
            "summarize_capabilities" -> capabilityManager.siriParityMatrix()
            "check_system_access" -> capabilityManager.describe()
            "check_privileges" -> capabilityManager.privilegeSummary()
            "check_hyperos_access" -> capabilityManager.hyperOsReadiness()
            else -> "I do not have a mapped device action for '$intentName' yet."
        }
    }

    private fun launchReminder(arguments: Map<String, String>): String {
        val title = arguments["title"].orEmpty().ifBlank { "Maya reminder" }
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "Reminder draft opened")
    }

    private fun launchAlarm(arguments: Map<String, String>): String {
        val message = arguments["title"].orEmpty().ifBlank { "Maya alarm" }
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "Alarm flow opened")
    }

    private fun launchSms(arguments: Map<String, String>): String {
        val phone = arguments["phoneNumber"].orEmpty()
        val message = arguments["message"].orEmpty()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phone")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "SMS draft opened")
    }

    private fun launchCall(arguments: Map<String, String>): String {
        val phone = arguments["phoneNumber"].orEmpty()
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "Dialer opened")
    }

    private fun launchApp(arguments: Map<String, String>): String {
        val appName = arguments["appName"].orEmpty()
        val packageManager = context.packageManager
        val match = packageManager.getInstalledApplications(0)
            .firstOrNull { app ->
                packageManager.getApplicationLabel(app).toString().contains(appName, ignoreCase = true)
            }
        val launchIntent = match?.let { packageManager.getLaunchIntentForPackage(it.packageName) }?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (launchIntent != null) {
            context.startActivity(launchIntent)
            "Opened ${packageManager.getApplicationLabel(match)}"
        } else {
            "I could not find an installed app matching '$appName'."
        }
    }

    private fun openNotificationAccess(): String {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "Notification access settings opened.\n${deviceStateStore.summarize()}")
    }

    private fun browseWeb(arguments: Map<String, String>): String {
        val target = arguments["webTarget"].orEmpty()
        val normalized = if (target.startsWith("http://") || target.startsWith("https://")) target else "https://$target"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "Browser opened for $normalized")
    }

    private fun searchWeb(arguments: Map<String, String>): String {
        val query = arguments["query"].orEmpty()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "Search opened for '$query'")
    }

    private fun openMaps(arguments: Map<String, String>): String {
        val query = arguments["query"].orEmpty().ifBlank { arguments["webTarget"].orEmpty() }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "Maps opened for '$query'")
    }

    private fun sendEmail(arguments: Map<String, String>): String {
        val body = arguments["emailBody"].orEmpty()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "Email composer opened")
    }

    private fun openSettings(): String {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "System settings opened")
    }

    private fun openDefaultApps(): String {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return safeStart(intent, "Default apps settings opened")
    }

    private fun safeStart(intent: Intent, successMessage: String): String {
        return try {
            context.startActivity(intent)
            successMessage
        } catch (_: ActivityNotFoundException) {
            "That action is not available on this phone yet."
        } catch (_: Exception) {
            "Maya could not complete that device action yet."
        }
    }
}
