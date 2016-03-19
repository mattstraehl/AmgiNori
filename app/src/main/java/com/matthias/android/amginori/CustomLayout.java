package com.matthias.android.amginori;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class CustomLayout extends RelativeLayout {

    public final List<Point> mPoints = new ArrayList<>();
    public View mView0;
    public View mView1;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RenderScript mRenderScript;
    private final ScriptIntrinsicBlur mIntrinsicBlur;
    private Bitmap mOriginal = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

    public CustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setWillNotDraw(false);
        //mPaint.setColor(Color.argb(128, 200, 200, 200));
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
        mRenderScript = RenderScript.create(context);
        mIntrinsicBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mPoints.isEmpty() && mView0.getHeight() > 0) {
            Bitmap original = Bitmap.createBitmap(Math.max(mView0.getWidth(), mView1.getWidth()),
                    mView0.getHeight() + mView1.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(original);
            c.drawBitmap(loadBitmapFromView(mView0), Math.max(0f, mView1.getWidth() - mView0.getWidth()) / 2, 0f, null);
            c.drawBitmap(loadBitmapFromView(mView1), Math.max(0f, mView0.getWidth() - mView1.getWidth()) / 2, mView0.getHeight(), null);

            Allocation input = Allocation.createFromBitmap(mRenderScript, original);
            Allocation output = Allocation.createTyped(mRenderScript, input.getType());
            mIntrinsicBlur.setRadius(25f);
            mIntrinsicBlur.setInput(input);
            mIntrinsicBlur.forEach(output);
            output.copyTo(original);
            input.destroy();
            output.destroy();

            BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), original);
            drawable.setAlpha(63);
            this.setBackground(drawable);
            mOriginal.recycle();
            mOriginal = original;
        }
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

    private static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }
}
