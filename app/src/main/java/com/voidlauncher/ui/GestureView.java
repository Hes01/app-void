package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class GestureView extends View {

    private static final int HOLD_FINGERS = 2;

    public interface Listener {
        void onStroke(List<PointF> points, int maxPointers);
        void onHold(int fingers);
    }

    private final Paint        stroke      = new Paint();
    private final List<PointF> points      = new ArrayList<>();
    private Listener           listener;
    private int                maxPointers = 0;
    private boolean            holdFired   = false;

    public GestureView(Context context) { this(context, null); }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        stroke.setColor(Color.argb(100, 255, 255, 255));
        stroke.setStrokeWidth(6f);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setAntiAlias(true);
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setListener(Listener l) { this.listener = l; }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        maxPointers = Math.max(maxPointers, e.getPointerCount());

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                holdFired = false;
                points.clear();
                maxPointers = 1;
                points.add(new PointF(e.getX(), e.getY()));
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (e.getPointerCount() == HOLD_FINGERS) {
                    holdFired = true;
                    points.clear();
                    invalidate();
                    if (listener != null) listener.onHold(HOLD_FINGERS);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!holdFired) {
                    points.add(new PointF(e.getX(), e.getY()));
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!holdFired && listener != null) {
                    listener.onStroke(new ArrayList<>(points), maxPointers);
                }
                holdFired = false;
                points.clear();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 1; i < points.size(); i++) {
            PointF a = points.get(i - 1), b = points.get(i);
            canvas.drawLine(a.x, a.y, b.x, b.y, stroke);
        }
    }
}
