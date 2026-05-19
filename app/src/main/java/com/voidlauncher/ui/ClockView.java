package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.Calendar;

class ClockView {

    static FrameLayout build(Context ctx, TextView[] clockOut, TextView[] dateOut) {
        float density    = ctx.getResources().getDisplayMetrics().density;
        int   circleSize = QuickSearchLayout.dp(ctx, 240);
        int   containerSz= QuickSearchLayout.dp(ctx, 300);
        float orbitR     = circleSize / 2f + QuickSearchLayout.dp(ctx, 6);

        TextView clock = new TextView(ctx);
        clock.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        clock.setTextSize(VoidTheme.TEXT_DISPLAY);
        clock.setTextColor(VoidTheme.FG);
        clock.setAlpha(0.9f);
        clock.setLetterSpacing(0.05f);
        clock.setGravity(Gravity.CENTER);
        if (clockOut != null) clockOut[0] = clock;

        TextView date = new TextView(ctx);
        date.setTextColor(VoidTheme.FG4);
        date.setTextSize(VoidTheme.TEXT_SM);
        date.setLetterSpacing(0.15f);
        date.setTypeface(Typeface.MONOSPACE);
        date.setGravity(Gravity.CENTER);
        if (dateOut != null) dateOut[0] = date;

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setStroke(1, VoidTheme.FG5);
        View circle = new View(ctx);
        circle.setBackground(shape);

        FrameLayout container = new FrameLayout(ctx) {
            private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            { dotPaint.setColor(VoidTheme.FG); }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                dotPaint.setColor(VoidTheme.FG);
                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f;
                Calendar c  = Calendar.getInstance();
                int active  = c.get(Calendar.HOUR) * 5 + c.get(Calendar.MINUTE) / 12;
                for (int i = 0; i < 60; i++) {
                    double angle = Math.toRadians(i * 6 - 90);
                    float  x     = cx + (float) (orbitR * Math.cos(angle));
                    float  y     = cy + (float) (orbitR * Math.sin(angle));
                    float  r     = (i % 5 == 0) ? density * 2f : density * 1.2f;
                    dotPaint.setAlpha((i == active) ? 200 : (i % 5 == 0) ? 40 : 15);
                    canvas.drawCircle(x, y, r, dotPaint);
                }
            }
        };

        FrameLayout.LayoutParams dateLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        dateLp.topMargin = QuickSearchLayout.dp(ctx, 40);

        container.setLayoutParams(new FrameLayout.LayoutParams(containerSz, containerSz, Gravity.CENTER));
        container.addView(circle, new FrameLayout.LayoutParams(circleSize, circleSize, Gravity.CENTER));
        container.addView(clock,  new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        container.addView(date, dateLp);
        return container;
    }
}
