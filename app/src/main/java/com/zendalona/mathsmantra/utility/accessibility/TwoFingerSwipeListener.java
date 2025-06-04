package com.zendalona.mathsmantra.utility;

import android.view.MotionEvent;
import android.view.View;

public class TwoFingerSwipeListener implements View.OnTouchListener {

    public interface OnTwoFingerSwipeListener {
        void onTwoFingerSwipeLeft();
        void onTwoFingerSwipeRight();
    }

    private static final int SWIPE_THRESHOLD = 100;

    private final OnTwoFingerSwipeListener listener;

    private float initialX1, initialX2;

    public TwoFingerSwipeListener(OnTwoFingerSwipeListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getPointerCount() != 2) {
            return false;  // Ignore if not exactly two fingers
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                initialX1 = event.getX(0);
                initialX2 = event.getX(1);
                break;

            case MotionEvent.ACTION_MOVE:
                float currentX1 = event.getX(0);
                float currentX2 = event.getX(1);

                // Check both fingers moving in the same direction
                float diffX1 = currentX1 - initialX1;
                float diffX2 = currentX2 - initialX2;

                if (Math.abs(diffX1) > SWIPE_THRESHOLD && Math.abs(diffX2) > SWIPE_THRESHOLD) {
                    if (diffX1 > 0 && diffX2 > 0) {
                        listener.onTwoFingerSwipeRight();
                    } else if (diffX1 < 0 && diffX2 < 0) {
                        listener.onTwoFingerSwipeLeft();
                    }
                    initialX1 = currentX1;
                    initialX2 = currentX2;
                    return true;
                }
                break;
        }
        return false;
    }
}
