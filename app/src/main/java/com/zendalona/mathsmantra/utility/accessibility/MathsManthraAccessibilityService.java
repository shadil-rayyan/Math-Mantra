package com.zendalona.mathsmantra.utility.accessibility;

import static java.sql.DriverManager.println;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Objects;

public class MathsManthraAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // This method is called when an accessibility event occurs
        Log.d("MathsManthraAccessible", "WINDOW_STATE_CHANGED " + event);

        // Check if the accessibility event is triggered due to a window state change
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            if ("com.zendalona.mathmantra".equals(packageName)) {
                Log.d("MathsManthraAccessible", "WINDOW_STATE_CHANGED " + event);
            }
        }

        // Handle the event when a view receives accessibility focus
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
            String packageName = event.getPackageName().toString();
            if ("com.zendalona.mathsmanthra".equals(packageName)) {
                Log.d("MathsManthraAccessible", "TYPE_VIEW_ACCESSIBILITY_FOCUSED " + event);

                // Correctly call updateWindowState from AccessibilityHelper
                MathsManthraAccessibilityService service = AccessibilityHelper.getAccessibilityService();
                if (service != null) {
                    // Now directly call updateWindowState on the service
                    service.updateWindowState();
                } else {
                    Log.w("MathsManthraAccessible", "Accessibility Service is not set.");
                }
            }
        }
    }

    private void updateWindowState() {
        println("Window state updated by Accessibility Service");

    }

    @Override
    public void onInterrupt() {
        // This method is called when the service is interrupted
        // Implement logic to handle interruptions, if needed
    }

    @Override
    protected void onServiceConnected() {
        // onServiceConnected() is called when the accessibility service is successfully enabled
        super.onServiceConnected();

        // Set this service in the AccessibilityHelper for use
        AccessibilityHelper.setAccessibilityService(this);

        // Notify user that the accessibility service is activated
        Toast.makeText(this, "Accessibility Service Activated", Toast.LENGTH_SHORT).show();
    }
}
