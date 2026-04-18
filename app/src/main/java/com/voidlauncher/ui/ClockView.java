package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

class ClockView {

    static FrameLayout build(Context ctx, TextView[] clockOut) {
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

        int size = (int) (ctx.getResources().getDisplayMetrics().density * 240);
        FrameLayout container = new FrameLayout(ctx);
        container.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        container.addView(circle, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
        container.addView(clock, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        return container;
    }
}
