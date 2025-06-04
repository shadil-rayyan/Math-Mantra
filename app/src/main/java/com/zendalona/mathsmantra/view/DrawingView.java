package com.zendalona.mathsmantra.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {
    private Paint paint;
    private Path path;
    private List<float[]> points; // Store points for shape recognition

    private boolean isDrawingComplete = false;
    private AccessibilityManager accessibilityManager;

    public DrawingView(Context context) {
        super(context);
        init(context);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setColor(0xFFD14D42); // Black color
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);

        path = new Path();
        points = new ArrayList<>();
        accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                points.clear();
                points.add(new float[]{x, y});
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                points.add(new float[]{x, y});
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                isDrawingComplete = true;
                points.add(new float[]{x, y});
                return true;
        }
        return false;
    }

    public void clearCanvas() {
        path.reset();
        points.clear();
        isDrawingComplete = false;
        invalidate();
    }

    public boolean isShapeCorrect(String expectedShape) {
        if (!isDrawingComplete) return false;

        int cornerCount = countCorners();
        boolean isClosed = isPathClosed();

        if (expectedShape.equals("triangle") && cornerCount == 3 && isClosed) {
            announceResult("Correct! You drew a triangle.");
            return true;
        } else if (expectedShape.equals("rectangle") && cornerCount == 4 && isClosed) {
            announceResult("Correct! You drew a rectangle.");
            return true;
        } else {
            announceResult("Incorrect shape. Try again.");
            return false;
        }
    }

    private int countCorners() {
        int corners = 0;
        for (int i = 1; i < points.size() - 1; i++) {
            float[] prev = points.get(i - 1);
            float[] curr = points.get(i);
            float[] next = points.get(i + 1);

            double angle = calculateAngle(prev, curr, next);
            if (angle < 140 && angle > 40) { // Angle detection for corners
                corners++;
            }
        }
        return corners;
    }

    private boolean isPathClosed() {
        if (points.size() < 3) return false;
        float[] first = points.get(0);
        float[] last = points.get(points.size() - 1);

        float distance = (float) Math.sqrt(Math.pow(last[0] - first[0], 2) + Math.pow(last[1] - first[1], 2));
        return distance < 50; // Closeness threshold
    }

    private double calculateAngle(float[] p1, float[] p2, float[] p3) {
        double angle = Math.toDegrees(
                Math.atan2(p3[1] - p2[1], p3[0] - p2[0]) -
                        Math.atan2(p1[1] - p2[1], p1[0] - p2[0])
        );
        return Math.abs(angle);
    }

    private void announceResult(String message) {
        if (accessibilityManager.isEnabled()) {
            announceForAccessibility(message);
        }
    }
}
