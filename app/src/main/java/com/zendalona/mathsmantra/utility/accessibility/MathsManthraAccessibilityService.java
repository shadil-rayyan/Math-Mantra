package com.zendalona.mathsmantra.utility.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class MathsManthraAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Get the package name that triggered the event
        CharSequence packageNameSeq = event.getPackageName();
        if (packageNameSeq == null) return;

        String packageName = packageNameSeq.toString();

        // Ensure the service is always set
        if (AccessibilityHelper.getAccessibilityService() == null) {
            AccessibilityHelper.setAccessibilityService(this);
            Log.d("MathsMantraAccessible", "Service set via fallback in event.");
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (getPackageName().equals(packageName)) {
                Log.d("MathsMantraAccessible", "WINDOW_STATE_CHANGED: " + event);
            }
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
            if (getPackageName().equals(packageName)) {
                Log.d("MathsMantraAccessible", "TYPE_VIEW_ACCESSIBILITY_FOCUSED: " + event);

                // Call update logic
                MathsManthraAccessibilityService service = AccessibilityHelper.getAccessibilityService();
                if (service != null) {
                    service.updateWindowState();
                } else {
                    Log.w("MathsMantraAccessible", "Accessibility Service is not set.");
                }
            }
        }
    }

    private void updateWindowState() {
        Log.d("MathsMantraAccessible", "Window state updated by Accessibility Service");
    }

    @Override
    public void onInterrupt() {
        Log.w("AccessibilityService", "Service was interrupted.");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("AccessibilityService", "onServiceConnected triggered");

        AccessibilityHelper.setAccessibilityService(this);

        Toast.makeText(this, "Accessibility Service Activated", Toast.LENGTH_SHORT).show();
    }
}
