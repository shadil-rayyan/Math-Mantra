package com.zendalona.zmantra.presentation.features.game.drawing.util;

import java.util.ArrayList;
import java.util.List;

public class RDP {
    public static List<float[]> rdp(List<float[]> points, float epsilon) {
        if (points.size() < 3) return points;
        int index = -1;
        float maxDist = 0;
        float[] start = points.get(0);
        float[] end = points.get(points.size() - 1);

        for (int i = 1; i < points.size() - 1; i++) {
            float dist = perpendicularDistance(points.get(i), start, end);
            if (dist > maxDist) {
                index = i;
                maxDist = dist;
            }
        }

        if (maxDist > epsilon) {
            List<float[]> left = rdp(points.subList(0, index + 1), epsilon);
            List<float[]> right = rdp(points.subList(index, points.size()), epsilon);
            List<float[]> result = new ArrayList<>(left);
            result.addAll(right.subList(1, right.size()));
            return result;
        } else {
            List<float[]> result = new ArrayList<>();
            result.add(start);
            result.add(end);
            return result;
        }
    }

    private static float perpendicularDistance(float[] p, float[] start, float[] end) {
        float dx = end[0] - start[0];
        float dy = end[1] - start[1];
        float mag = (float) Math.hypot(dx, dy);
        if (mag == 0) return (float) Math.hypot(p[0] - start[0], p[1] - start[1]);

        float u = ((p[0] - start[0]) * dx + (p[1] - start[1]) * dy) / (mag * mag);
        float ix = start[0] + u * dx;
        float iy = start[1] + u * dy;
        return (float) Math.hypot(p[0] - ix, p[1] - iy);
    }

}