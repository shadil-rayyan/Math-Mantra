package com.zendalona.mathsmantra.view;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.zendalona.mathsmantra.MainActivity;
import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityHelper;
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityUtils;
import com.zendalona.mathsmantra.utility.accessibility.MathsManthraAccessibilityService;

public class NumberLineView extends View {

    private int currentPosition;
    private int numberRangeStart;
    private int numberRangeEnd;
    private final String MASCOT_EMOJI = "\uD83E\uDDCD\u200D♂\uFE0F";

    private Paint linePaint;
    private Paint numberPaint;
    private Paint mascotPaint;

    private float gap;

    private GestureDetector gestureDetector;

    private boolean talkBackEnabled = false;

    private float initialX1, initialX2;
    private boolean isTwoFingerGesture = false;

    public NumberLineView(Context context) {
        super(context);
        init(context);
    }

    public NumberLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NumberLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(context, R.color.light_purple_button));
        linePaint.setStrokeWidth(12f);

        numberPaint = new Paint();
        numberPaint.setColor(ContextCompat.getColor(context, R.color.light_red_button));
        numberPaint.setTextSize(40f);

        mascotPaint = new Paint();
        mascotPaint.setTextSize(200f);

        talkBackEnabled = AccessibilityUtils.isMathsManthraAccessibilityServiceEnabled(context);

        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawNumberLine(canvas);
        drawMascot(canvas);
    }

    private void drawNumberLine(Canvas canvas) {
        float startX = getWidth() * 0.01f;
        float endX = getWidth() * 0.98f;
        float centerY = getHeight() / 2f;

        canvas.drawLine(0, centerY, getWidth(), centerY, linePaint);

        gap = (endX - startX) / (numberRangeEnd - numberRangeStart);

        int d = Log.d("Drawing number line range : ", numberRangeStart + " to " + numberRangeEnd);
        for (int number = numberRangeStart; number <= numberRangeEnd; number++) {
            float x = startX + (number - numberRangeStart) * gap;
            canvas.drawText(String.valueOf(number), x, centerY + 50f, numberPaint);
        }
    }

    private void drawMascot(Canvas canvas) {
        float centerY = getHeight() / 2f;
        float mascotPosition = (currentPosition - numberRangeStart - 0.4f) * gap;

        mascotPosition = Math.max(0, Math.min(mascotPosition, getWidth()));

        canvas.drawText(MASCOT_EMOJI, mascotPosition, centerY - 50f, mascotPaint);
    }

    public void updateNumberLine(int start, int end, int position) {
        this.numberRangeStart = start;
        this.numberRangeEnd = end;
        this.currentPosition = position;
        invalidate();
    }

    public int moveLeft() {
        if (currentPosition > numberRangeStart) {
            currentPosition--;
            invalidate();
            announcePosition();
        }
        return currentPosition;
    }

    public int moveRight() {
        if (currentPosition < numberRangeEnd) {
            currentPosition++;
            invalidate();
            announcePosition();
        }
        return currentPosition;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (talkBackEnabled) {
            handleTwoFingerSwipe(event);
            return true;
        } else {
            return gestureDetector.onTouchEvent(event);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(false);
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    private void handleTwoFingerSwipe(MotionEvent event) {
        int pointerCount = event.getPointerCount();

        if (pointerCount < 2) {
            isTwoFingerGesture = false;
            return;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerCount == 2) {
                    initialX1 = event.getX(0);
                    initialX2 = event.getX(1);
                    isTwoFingerGesture = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isTwoFingerGesture && pointerCount == 2) {
                    float currentX1 = event.getX(0);
                    float currentX2 = event.getX(1);

                    float diffX1 = currentX1 - initialX1;
                    float diffX2 = currentX2 - initialX2;

                    if (Math.abs(diffX1) > 100 && Math.abs(diffX2) > 100) {
                        if (diffX1 > 0 && diffX2 > 0) {
                            moveRight();
                        } else if (diffX1 < 0 && diffX2 < 0) {
                            moveLeft();
                        }
                        initialX1 = currentX1;
                        initialX2 = currentX2;
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTwoFingerGesture = false;
                break;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        moveRight();
                    } else {
                        moveLeft();
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private void announcePosition() {
        if (talkBackEnabled) {
            announceForAccessibility("Current position: " + currentPosition);
        }
    }

    public void onResume() {
        if (talkBackEnabled) {
            // Get the context from the view and cast it to an Activity
            Context context = getContext();
            if (context instanceof Activity) {
                // Access the system service for accessibility service
                AccessibilityService accessibilityService = (AccessibilityService) ((Activity) context).getSystemService(AccessibilityService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AccessibilityHelper.disableExploreByTouch((MathsManthraAccessibilityService) accessibilityService);
                }
            }
        }
    }

    public void onPause() {
        if (talkBackEnabled) {
            // Get the context from the view and cast it to an Activity
            Context context = getContext();
            if (context instanceof Activity) {
                // Access the system service for accessibility service
                AccessibilityService accessibilityService = (AccessibilityService) ((Activity) context).getSystemService(AccessibilityService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AccessibilityHelper.resetExploreByTouch((MathsManthraAccessibilityService) accessibilityService);
                }
            }
        }
    }
}
