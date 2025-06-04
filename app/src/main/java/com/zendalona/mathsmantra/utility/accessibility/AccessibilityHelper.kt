package com.zendalona.mathsmantra.utility.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Region
import android.os.Build
import android.provider.Settings
import android.view.Display
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

object AccessibilityHelper {

    // Reference to the Accessibility Service
    private var accessibilityService: MathsManthraAccessibilityService? = null

    // Check and show the accessibility dialog if needed
    fun checkAndShowAccessibilityDialog(context: Context) {
        val isTalkBackOn = isSystemExploreByTouchEnabled(context)
        val isServiceEnabled = isMathsManthraAccessibilityServiceEnabled(context)

        if (isTalkBackOn && !isServiceEnabled) {
            showAccessibilityDialog(context)
        }
    }

    private fun showAccessibilityDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Enable Accessibility Service")
            .setMessage("MathsManthra needs Accessibility Service to function properly. Would you like to enable it?")
            .setPositiveButton(
                "Enable",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                })
            .setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> dialog!!.dismiss() })
            .show()
    }

    // Check if TalkBack (Explore by Touch) is enabled
    private fun isSystemExploreByTouchEnabled(context: Context): Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
        return accessibilityManager != null && accessibilityManager.isEnabled() && accessibilityManager.isTouchExplorationEnabled()
    }

    // Check if MathsManthraAccessibilityService is enabled in Accessibility Settings
    private fun isMathsManthraAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?

        if (accessibilityManager == null) {
            return false
        }

        val enabledServices =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        for (serviceInfo in enabledServices) {
            if (serviceInfo.getId()
                    .contains("com.zendalona.mathmanthra/.utils.MathsManthraAccessibilityService")
            ) {
                return true
            }
        }
        return false
    }

    // Enable explore-by-touch passthrough region
    @JvmStatic
    fun disableExploreByTouch(appAccessibilityService: MathsManthraAccessibilityService?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && appAccessibilityService != null) {
            val fullScreenRegion = Region(
                0,
                0,
                appAccessibilityService.resources.displayMetrics.widthPixels,
                appAccessibilityService.resources.displayMetrics.heightPixels
            )
            appAccessibilityService.setTouchExplorationPassthroughRegion(
                Display.DEFAULT_DISPLAY,
                fullScreenRegion
            )
            appAccessibilityService.setGestureDetectionPassthroughRegion(
                Display.DEFAULT_DISPLAY,
                fullScreenRegion
            )
        }
    }

    // Reset explore-by-touch passthrough region
    @JvmStatic
    fun resetExploreByTouch(appAccessibilityService: MathsManthraAccessibilityService?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && appAccessibilityService != null) {
            val emptyRegion = Region()
            appAccessibilityService.setTouchExplorationPassthroughRegion(
                Display.DEFAULT_DISPLAY,
                emptyRegion
            )
            appAccessibilityService.setGestureDetectionPassthroughRegion(
                Display.DEFAULT_DISPLAY,
                emptyRegion
            )
        }
    }

    // Update window state, based on your requirements (e.g., UI updates)


    // Get the stored instance of the Accessibility Service
    @JvmStatic
    fun getAccessibilityService(): MathsManthraAccessibilityService? {
        return accessibilityService
    }

    // Static method to set the Accessibility Service instance
    @JvmStatic
    fun setAccessibilityService(service: MathsManthraAccessibilityService) {
        accessibilityService = service
    }
}
