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

    public interface Listener {
        void onStroke(List<PointF> points, int maxPointers);
    }

    private final Paint stroke = new Paint();
    private final List<PointF> points = new ArrayList<>();
    private Listener listener;
    private int maxPointers = 0;

    public GestureView(Context context) {
        this(context, null);
    }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        stroke.setColor(Color.argb(100, 255, 255, 255));
        stroke.setStrokeWidth(6f);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setAntiAlias(true);
        setBackgroundColor(Color.BLACK);
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        maxPointers = Math.max(maxPointers, e.getPointerCount());

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                points.clear();
                maxPointers = 1;
                points.add(new PointF(e.getX(), e.getY()));
                break;
            case MotionEvent.ACTION_MOVE:
                points.add(new PointF(e.getX(), e.getY()));
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null) listener.onStroke(new ArrayList<>(points), maxPointers);
                points.clear();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        for (int i = 1; i < points.size(); i++) {
            PointF a = points.get(i - 1), b = points.get(i);
            canvas.drawLine(a.x, a.y, b.x, b.y, stroke);
        }
    }
}
