package com.matthias.android.amginori.utils;

import android.graphics.Point;

import java.util.LinkedList;
import java.util.List;

public class Interpolation {

    public static List<Point> interpolate(Point start, Point end, int stepSize) {
        List<Point> result = new LinkedList<>();
        if (start != null) {
            int dx = end.x - start.x;
            int dy = end.y - start.y;
            int interpolateSteps = Math.abs(dy / stepSize);
            Point newIndex;
            for (int step = 0; step < interpolateSteps; step++) {
                newIndex = new Point();
                newIndex.x = start.x + ((dx * step) / interpolateSteps);
                newIndex.y = start.y + ((dy * step) / interpolateSteps);
                result.add(newIndex);
            }
        }
        result.add(end);
        return result;
    }
}
