package com.zendalona.zmantra.utility.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Toast

class AccessibilityUtils {
    // Check if TalkBack (Explore by Touch) is enabled
    fun isSystemExploreByTouchEnabled(context: Context): Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
        if (accessibilityManager != null) {
            return accessibilityManager.isEnabled() && accessibilityManager.isTouchExplorationEnabled()
        }
        return false
    }

    companion object {
        // ✅ NEW METHOD - Check if MathsManthraAccessibilityService is enabled in Accessibility Settings
        @JvmStatic
        fun isMathsManthraAccessibilityServiceEnabled(context: Context): Boolean {
            val targetServiceId = "${context.packageName}/${MathsManthraAccessibilityService::class.java.name}"
            Log.d("AccessibilityService", "Target ID: $targetServiceId")

            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                ?: return false

            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )

            // Log the enabled services
            enabledServices.forEach {
                Log.d("AccessibilityService", "Enabled service: ${it.id}")
            }

            // Check if your custom service is enabled
            return enabledServices.any { it.id == targetServiceId }
        }

        // ⚠️ Optional - This is less reliable. You can keep or remove this.
//        fun isMathsManthraAccessibilityServiceRunning(context: Context): Boolean {
//            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//            for (service in manager.getRunningServices(Int.Companion.MAX_VALUE)) {
//                if (MathsManthraAccessibilityService::class.java.getName() == service.service.getClassName()) {
//                    return true
//                }
//            }
//            return false
//        }

        // Announce message to TalkBack/Screen Reader
        fun sendTextToScreenReader(context: Context, message: String?) {
            val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
            event.getText().add(message)

            val accessibilityManager =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
            if (accessibilityManager != null && accessibilityManager.isEnabled()) {
                accessibilityManager.sendAccessibilityEvent(event)
            }
        }

        // Redirect user to Accessibility settings
        fun redirectToAccessibilitySettings(context: Context) {
            Toast.makeText(
                context,
                "Please enable MathsManthra Accessibility Service",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
