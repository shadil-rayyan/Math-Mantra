package com.zendalona.mathsmantra.utility.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Region
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AlertDialog

object AccessibilityHelper {

    private var accessibilityService: MathsManthraAccessibilityService? = null

    // Always check and enforce the requirement every time it's called
    fun enforceAccessibilityRequirement(context: Context) {
        val isServiceEnabled = isMathsManthraAccessibilityServiceEnabled(context)
        val isTalkBackOn = AccessibilityUtils().isSystemExploreByTouchEnabled(context)

        Log.d("AccessibilityHelper", "Service Enabled: $isServiceEnabled, TalkBack On: $isTalkBackOn")

        if (!isServiceEnabled && isTalkBackOn) {
            showAccessibilityDialog(context)
        } else {
            Log.d("AccessibilityHelper", "Accessibility requirement satisfied or TalkBack is off.")
        }
    }



    fun showAccessibilityDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Enable Accessibility Service")
            .setMessage("MathsManthra requires Accessibility Service to function properly. Please enable it in the settings.")
            .setCancelable(true)
            .setPositiveButton("Enable") { _: DialogInterface, _: Int ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Do nothing else, just close the dialog
            }
            .show()
    }

    // Check if the custom accessibility service is enabled
    private fun isMathsManthraAccessibilityServiceEnabled(context: Context): Boolean {
        val targetServiceId = "${context.packageName}/${MathsManthraAccessibilityService::class.java.name}"
        Log.d("AccessibilityService", "Target ID: $targetServiceId")

        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false

        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )

        enabledServices.forEach {
            Log.d("AccessibilityService", "Enabled: ${it.id}")
        }

        return enabledServices.any { it.id == targetServiceId }
    }


    // Optional: control touch exploration passthrough
    @JvmStatic
    fun disableExploreByTouch(service: MathsManthraAccessibilityService?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && service != null) {
            val region = Region(
                0,
                0,
                service.resources.displayMetrics.widthPixels,
                service.resources.displayMetrics.heightPixels
            )
            service.setTouchExplorationPassthroughRegion(Display.DEFAULT_DISPLAY, region)
            service.setGestureDetectionPassthroughRegion(Display.DEFAULT_DISPLAY, region)
        }
    }

    @JvmStatic
    fun resetExploreByTouch(service: MathsManthraAccessibilityService?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && service != null) {
            val empty = Region()
            service.setTouchExplorationPassthroughRegion(Display.DEFAULT_DISPLAY, empty)
            service.setGestureDetectionPassthroughRegion(Display.DEFAULT_DISPLAY, empty)
        }
    }

    @JvmStatic
    fun getAccessibilityService(): MathsManthraAccessibilityService? {
        return accessibilityService
    }

    @JvmStatic
    fun setAccessibilityService(service: MathsManthraAccessibilityService) {
        accessibilityService = service
    }
}
