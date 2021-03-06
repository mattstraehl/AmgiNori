package com.matthias.android.amginori;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CustomLayout extends RelativeLayout {

    public final List<View> mViews = new ArrayList<>();
    public final List<Point> mPoints = new ArrayList<>();

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RenderScript mRenderScript;
    private final ScriptIntrinsicBlur mIntrinsicBlur;

    private Bitmap mCurrent = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    private long mLastUpdate = 0l;

    private static final ColorFilter BRIGHTNESS_FILTER = new PorterDuffColorFilter(Color.argb(31, 255, 255, 255), PorterDuff.Mode.SRC_OVER);

    public CustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_width));
        mRenderScript = RenderScript.create(context);
        mIntrinsicBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        long time = System.currentTimeMillis();
        if (time - mLastUpdate >= 16) {
            mLastUpdate = time;
            Bitmap original = createBitmapOfViews();
            if (original != null) {
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
                drawable.setColorFilter(BRIGHTNESS_FILTER);
                this.setBackground(drawable);
                mCurrent.recycle();
                mCurrent = original;
            }
        }
        quadTo(canvas);
    }

    private Bitmap createBitmapOfViews() {
        LinkedList<View> views = new LinkedList<>();
        int width = 0;
        for (View view : mViews) {
            if (view.getHeight() > 0) {
                views.add(view);
                width = Math.max(width, view.getWidth());
            }
        }
        if (views.isEmpty()) {
            return null;
        }
        int height = views.getFirst().getHeight();
        Bitmap result = Bitmap.createBitmap(width, height * views.size(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        int i = 0;
        for (View view : views) {
            Rect rect = new Rect(0, height * i, width, height * (i + 1));
            Bitmap bitmap = loadBitmapFromView(view);
            canvas.drawBitmap(bitmap, null, rect, null);
            bitmap.recycle();
            i++;
        }
        return result;
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

    protected static Bitmap loadBitmapFromView(View view) {
        View v = ((ViewGroup) view).getChildAt(0);
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(canvas);
        return bitmap;
    }
}
