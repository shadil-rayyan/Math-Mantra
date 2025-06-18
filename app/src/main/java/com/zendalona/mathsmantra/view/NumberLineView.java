package com.zendalona.mathsmantra.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityHelper;
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityUtils;

public class NumberLineView extends View {

    private static final String TAG = "NumberLineView";

    private int currentPosition;
    private int numberRangeStart;
    private int numberRangeEnd;
    private final String MASCOT_EMOJI = "\uD83E\uDDCD\u200D♂\uFE0F";

    private Paint linePaint;
    private Paint numberPaint;
    private Paint mascotPaint;
    private float gap;

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
        Log.d(TAG, "Initializing NumberLineView.");

        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.blue));
        linePaint.setStrokeWidth(12f);

        numberPaint = new Paint();
        numberPaint.setColor(ContextCompat.getColor(getContext(), R.color.lightBlue));
        numberPaint.setTextSize(40f);

        mascotPaint = new Paint();
        mascotPaint.setTextSize(200f);
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

        if (numberRangeEnd <= numberRangeStart) {
            Log.w(TAG, "Invalid number range: start=" + numberRangeStart + " end=" + numberRangeEnd);
            return;
        }

        gap = (endX - startX) / (numberRangeEnd - numberRangeStart);

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
        Log.d(TAG, "Updating number line: start=" + start + ", end=" + end + ", position=" + position);
        this.numberRangeStart = start;
        this.numberRangeEnd = end;
        this.currentPosition = position;
        invalidate();
        announcePosition();
    }

    private void announcePosition() {
        boolean talkBackEnabled = AccessibilityUtils.isMathsManthraAccessibilityServiceEnabled(getContext());
        if (talkBackEnabled) {
            announceForAccessibility("Current position: " + currentPosition);
        }

    }
//    public void onResume() {
//        Log.d(TAG, "DrawingView onResume called: disabling Explore-by-Touch");
//        // Disable Explore-by-Touch passthrough region so touch events work properly
//        AccessibilityHelper.disableExploreByTouch(AccessibilityHelper.getAccessibilityService());
//    }
//
//    public void onPause() {
//        Log.d(TAG, "DrawingView onPause called: resetting Explore-by-Touch");
//        // Reset Explore-by-Touch passthrough region
//        AccessibilityHelper.resetExploreByTouch(AccessibilityHelper.getAccessibilityService());
//    }
}
