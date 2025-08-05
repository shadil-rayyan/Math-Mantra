package com.zendalona.zmantra.core.customView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import com.zendalona.zmantra.core.utility.accessibility.AccessibilityHelper;
import com.zendalona.zmantra.presentation.features.game.drawing.util.RDP;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private static final String TAG = "DrawingView";

    private Paint paint;
    private Path path;
    private List<float[]> points; // All points from all strokes
    private List<List<float[]>> strokes = new ArrayList<>();
    private List<float[]> currentStroke;

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
        paint.setColor(0xFFD14D42); // Your chosen color
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        path = new Path();
        points = new ArrayList<>();
        accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        Log.d(TAG, "DrawingView initialized");
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

        Log.d(TAG, "onTouchEvent action=" + event.getAction() + " at (" + x + ", " + y + ")");

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentStroke = new ArrayList<>();
                currentStroke.add(new float[]{x, y});
                strokes.add(currentStroke);

                path.moveTo(x, y);
                Log.d(TAG, "ACTION_DOWN - start stroke at (" + x + ", " + y + ")");
                return true;

            case MotionEvent.ACTION_MOVE:
                currentStroke.add(new float[]{x, y});
                path.lineTo(x, y);
                invalidate();
                Log.d(TAG, "ACTION_MOVE - added point (" + x + ", " + y + ")");
                return true;

            case MotionEvent.ACTION_UP:
                currentStroke.add(new float[]{x, y});
                isDrawingComplete = true;
                Log.d(TAG, "ACTION_UP - end stroke at (" + x + ", " + y + ")");
                return true;
        }
        return false;
    }

    public void clearCanvas() {
        path.reset();
        strokes.clear();
        points.clear();
        currentStroke = null;
        isDrawingComplete = false;
        invalidate();

        Log.d(TAG, "Canvas cleared");
    }

    public List<float[]> getAllPoints() {
        List<float[]> allPoints = new ArrayList<>();
        for (List<float[]> stroke : strokes) {
            allPoints.addAll(stroke);
        }
        Log.d(TAG, "getAllPoints returned " + allPoints.size() + " points");
        return allPoints;
    }

    public boolean isShapeCorrect(List<float[]> points, int expectedCorners, float epsilon) {
        Log.d(TAG, "isShapeCorrect called with expectedCorners=" + expectedCorners + ", epsilon=" + epsilon);

        if (points == null || points.size() < 3) {
            Log.d(TAG, "Too few points to form shape.");
            return false;
        }

        List<float[]> simplified = RDP.rdp(points, epsilon);
        Log.d(TAG, "RDP simplified points count: " + simplified.size());

        int n = simplified.size();
        if (n < 3) {
            Log.d(TAG, "Simplified points too few for polygon.");
            return false;
        }

        float[] first = simplified.get(0);
        float[] last = simplified.get(n - 1);
        double dist = Math.hypot(first[0] - last[0], first[1] - last[1]);
        Log.d(TAG, "Distance between first and last points: " + dist);
        if (dist > epsilon * 2) {
            Log.d(TAG, "Shape not closed enough.");
            return false;
        }

        int validCorners = 0;
        for (int i = 0; i < n; i++) {
            float[] p1 = simplified.get((i - 1 + n) % n);
            float[] p2 = simplified.get(i);
            float[] p3 = simplified.get((i + 1) % n);

            double angle = calculateAngle(p1, p2, p3);
            Log.d(TAG, "Corner " + i + " angle: " + angle);

            if (angle > 20 && angle < 160) {
                validCorners++;
            }
        }
        Log.d(TAG, "Valid corners counted: " + validCorners);

        if (!(validCorners >= expectedCorners - 1 && validCorners <= expectedCorners + 1)) {
            Log.d(TAG, "Corner count not in acceptable range. Expected around " + expectedCorners);
            return false;
        }

        if (expectedCorners == 4) {
            double perimeter = calculatePerimeter(simplified);
            double area = calculatePolygonArea(simplified);

            float[] bounds = getBoundingBox(simplified);
            double width = bounds[2] - bounds[0];
            double height = bounds[3] - bounds[1];
            double aspectRatio = width / height;

            Log.d(TAG, "Perimeter: " + perimeter + ", Area: " + area);
            Log.d(TAG, "Aspect ratio: " + aspectRatio);

            if (aspectRatio < 0.5 || aspectRatio > 2.0) {
                Log.d(TAG, "Aspect ratio outside allowed range.");
                return false;
            }

            double compactness = (4 * Math.PI * area) / (perimeter * perimeter);
            Log.d(TAG, "Compactness: " + compactness);
            if (compactness < 0.1) {
                Log.d(TAG, "Shape too irregular (low compactness).");
                return false;
            }
        }

        Log.d(TAG, "Shape passes all checks.");
        return true;
    }

    private double calculatePerimeter(List<float[]> points) {
        double perimeter = 0;
        int n = points.size();
        for (int i = 0; i < n; i++) {
            float[] p1 = points.get(i);
            float[] p2 = points.get((i + 1) % n);
            double d = Math.hypot(p1[0] - p2[0], p1[1] - p2[1]);
            perimeter += d;
            Log.d(TAG, "Edge " + i + " length: " + d);
        }
        Log.d(TAG, "Total perimeter: " + perimeter);
        return perimeter;
    }

    private double calculatePolygonArea(List<float[]> points) {
        double area = 0;
        int n = points.size();
        for (int i = 0; i < n; i++) {
            float[] p1 = points.get(i);
            float[] p2 = points.get((i + 1) % n);
            double term = p1[0] * p2[1] - p2[0] * p1[1];
            area += term;
            Log.d(TAG, "Area term " + i + ": " + term);
        }
        area = Math.abs(area / 2.0);
        Log.d(TAG, "Polygon area: " + area);
        return area;
    }

    private float[] getBoundingBox(List<float[]> points) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;

        for (float[] p : points) {
            if (p[0] < minX) minX = p[0];
            if (p[1] < minY) minY = p[1];
            if (p[0] > maxX) maxX = p[0];
            if (p[1] > maxY) maxY = p[1];
        }

        Log.d(TAG, "Bounding box: minX=" + minX + ", minY=" + minY + ", maxX=" + maxX + ", maxY=" + maxY);
        return new float[]{minX, minY, maxX, maxY};
    }

    private static double calculateAngle(float[] p1, float[] p2, float[] p3) {
        double dx1 = p1[0] - p2[0];
        double dy1 = p1[1] - p2[1];
        double dx2 = p3[0] - p2[0];
        double dy2 = p3[1] - p2[1];

        double angle1 = Math.atan2(dy1, dx1);
        double angle2 = Math.atan2(dy2, dx2);

        double result = Math.toDegrees(angle2 - angle1);
        if (result < 0) {
            result += 360;
        }

        Log.d(TAG, "Angle calculated between points: " + result);
        return result;
    }

    // === New lifecycle-like methods to call from your Activity ===

    public void onResume() {
        Log.d(TAG, "DrawingView onResume called: disabling Explore-by-Touch");
        // Disable Explore-by-Touch passthrough region so touch events work properly
        AccessibilityHelper.disableExploreByTouch(AccessibilityHelper.getAccessibilityService());
    }

    public void onPause() {
        Log.d(TAG, "DrawingView onPause called: resetting Explore-by-Touch");
        // Reset Explore-by-Touch passthrough region
        AccessibilityHelper.resetExploreByTouch(AccessibilityHelper.getAccessibilityService());
    }
}
