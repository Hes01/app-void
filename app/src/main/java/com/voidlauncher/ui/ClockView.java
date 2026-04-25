package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.Calendar;

class ClockView {

    static FrameLayout build(Context ctx, TextView[] clockOut) {
        float density     = ctx.getResources().getDisplayMetrics().density;
        int   circleSize  = Math.round(density * 240);
        int   containerSz = Math.round(density * 280);
        float orbitR      = circleSize / 2f + density * 13;

        TextView clock = new TextView(ctx);
        clock.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        clock.setTextSize(64f);
        clock.setTextColor(Color.WHITE);
        clock.setAlpha(0.8f);
        clock.setGravity(Gravity.CENTER);
        if (clockOut != null) clockOut[0] = clock;

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setStroke(2, Color.WHITE);
        View circle = new View(ctx);
        circle.setBackground(shape);
        circle.setAlpha(0.15f);

        FrameLayout container = new FrameLayout(ctx) {
            private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            { dotPaint.setColor(Color.WHITE); }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);

                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f;

                Calendar c    = Calendar.getInstance();
                int hour      = c.get(Calendar.HOUR);
                int minute    = c.get(Calendar.MINUTE);
                int active    = hour * 5 + minute / 12;

                for (int i = 0; i < 60; i++) {
                    double angle = Math.toRadians(i * 6 - 90);
                    float  x     = cx + (float) (orbitR * Math.cos(angle));
                    float  y     = cy + (float) (orbitR * Math.sin(angle));

                    float r;
                    int   alpha;
                    if (i == active) {
                        r = density * 3f;   alpha = 255;
                    } else if (i % 5 == 0) {
                        r = density * 2.5f; alpha = 60;
                    } else {
                        r = density * 1.4f; alpha = 25;
                    }
                    dotPaint.setAlpha(alpha);
                    canvas.drawCircle(x, y, r, dotPaint);
                }
            }
        };

        container.setLayoutParams(new FrameLayout.LayoutParams(
                containerSz, containerSz, Gravity.CENTER));
        container.addView(circle, new FrameLayout.LayoutParams(
                circleSize, circleSize, Gravity.CENTER));
        container.addView(clock, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        return container;
    }
}
