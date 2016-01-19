package com.matthias.android.amginori;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class CustomLayout extends RelativeLayout {

    public List<Point> mPoints = new ArrayList<>();

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setWillNotDraw(false);
        //mPaint.setColor(Color.argb(128, 200, 200, 200));
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        quadTo(canvas);
    }

    private void quadTo(Canvas canvas) {
        Path path = new Path();
        boolean first = true;
        // TODO 20 may mean more or less on other devices
        for (int i = Math.max(mPoints.size() - 20, 0); i < mPoints.size(); i += 2) {
            Point point = mPoints.get(i);
            if (first) {
                first = false;
                path.moveTo(point.x, point.y);
            } else if (i < mPoints.size() - 1) {
                Point next = mPoints.get(i + 1);
                path.quadTo(point.x, point.y, next.x, next.y);
            } else {
                path.lineTo(point.x, point.y);
            }
        }
        canvas.drawPath(path, mPaint);
    }

    @SuppressWarnings("unused")
    private void lineTo(Canvas canvas) {
        Path path = new Path();
        boolean first = true;
        for (Point point : mPoints) {
            if (first) {
                first = false;
                path.moveTo(point.x, point.y);
            } else {
                path.lineTo(point.x, point.y);
            }
        }
        canvas.drawPath(path, mPaint);
    }

    /*@Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.w("myApp", "intercept: " + event.getAction());
        return false;
    }*/
}
