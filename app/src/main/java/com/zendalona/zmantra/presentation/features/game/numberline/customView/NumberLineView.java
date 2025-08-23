package com.zendalona.zmantra.presentation.features.game.numberline.customView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.content.ContextCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;

import com.zendalona.zmantra.R;

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

        // Set accessibility delegate to customize accessibility behavior locally for this view
        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegateCompat() {
            @Override
            public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
                    // Announce current position when view receives accessibility focus
                    announceForAccessibility("Number line current position is " + currentPosition);
                    return true;
                }
                return super.dispatchPopulateAccessibilityEvent(host, event);
            }
        });
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

        // Announce position only locally for this view
        announcePosition();
    }

    private void announcePosition() {
        // Announce for accessibility: only when this view updates
        String message = getContext().getString(R.string.current_position, currentPosition);
        announceForAccessibility(message);
    }


    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        String desc = "Number line from " + numberRangeStart + " to " + numberRangeEnd + ", current position is " + currentPosition;
        info.setContentDescription(desc);
    }
}
