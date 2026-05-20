package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

class ClockView {

    static FrameLayout build(Context ctx, TextView[] clockOut, TextView[] dateOut) {
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

        FrameLayout.LayoutParams dateLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        dateLp.topMargin = QuickSearchLayout.dp(ctx, 40);

        FrameLayout container = new FrameLayout(ctx);
        container.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        container.addView(clock, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        container.addView(date, dateLp);
        return container;
    }
}
