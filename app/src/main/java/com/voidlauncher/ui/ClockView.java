package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

class ClockView {

    static FrameLayout build(Context ctx, TextView[] clockOut, TextView[] dateOut) {
        TextView clock = new TextView(ctx);
        clock.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        clock.setTextSize(VoidTheme.TEXT_DISPLAY);
        clock.setTextColor(VoidTheme.FG);
        clock.setAlpha(0.9f); clock.setLetterSpacing(0.05f); clock.setGravity(Gravity.CENTER);
        if (clockOut != null) clockOut[0] = clock;
        TextView date = makeDate(ctx, dateOut);
        LinearLayout stack = stack(ctx);
        stack.addView(clock, wrapLp()); stack.addView(date, gap(ctx, 8));
        return pill(ctx, stack, false);
    }

    static FrameLayout buildSegment(Context ctx, SegmentClockView[] segOut, TextView[] dateOut) {
        SegmentClockView seg = new SegmentClockView(ctx, is24h(ctx));
        if (segOut != null) segOut[0] = seg;
        TextView date = makeDate(ctx, dateOut);
        LinearLayout stack = stack(ctx);
        stack.addView(seg, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 88)));
        stack.addView(date, gap(ctx, 4));
        return pill(ctx, stack, true);
    }

    static FrameLayout buildFlip(Context ctx, FlipClockView[] flipOut, TextView[] dateOut) {
        FlipClockView flip = new FlipClockView(ctx, is24h(ctx));
        if (flipOut != null) flipOut[0] = flip;
        TextView date = makeDate(ctx, dateOut);
        LinearLayout stack = stack(ctx);
        stack.addView(flip, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 96)));
        stack.addView(date, gap(ctx, 4));
        return pill(ctx, stack, true);
    }

    private static TextView makeDate(Context ctx, TextView[] out) {
        TextView tv = new TextView(ctx);
        tv.setTextColor(VoidTheme.FG4); tv.setTextSize(VoidTheme.TEXT_SM);
        tv.setLetterSpacing(0.15f); tv.setTypeface(Typeface.MONOSPACE); tv.setGravity(Gravity.CENTER);
        if (out != null) out[0] = tv; return tv;
    }

    private static LinearLayout stack(Context ctx) {
        LinearLayout ll = new LinearLayout(ctx);
        ll.setOrientation(LinearLayout.VERTICAL); ll.setGravity(Gravity.CENTER_HORIZONTAL); return ll;
    }

    private static LinearLayout.LayoutParams wrapLp() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private static LinearLayout.LayoutParams gap(Context ctx, int dpVal) {
        LinearLayout.LayoutParams lp = wrapLp(); lp.topMargin = dp(ctx, dpVal); return lp;
    }

    private static FrameLayout pill(Context ctx, View content, boolean wide) {
        int padH = dp(ctx, wide ? 20 : 28), padV = dp(ctx, 18), rad = dp(ctx, 16);
        FrameLayout pill = new FrameLayout(ctx) {
            private final Paint pillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF pillRect  = new RectF();
            { pillPaint.setAlpha(0x99); }
            @Override protected void onDraw(Canvas canvas) {
                pillPaint.setColor(VoidTheme.BG);
                pillRect.set(0, 0, getWidth(), getHeight());
                canvas.drawRoundRect(pillRect, rad, rad, pillPaint);
            }
        };
        pill.setWillNotDraw(false); pill.setPadding(padH, padV, padH, padV);
        int cw = wide ? FrameLayout.LayoutParams.MATCH_PARENT : FrameLayout.LayoutParams.WRAP_CONTENT;
        pill.addView(content, new FrameLayout.LayoutParams(cw, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        int screenH = ctx.getResources().getDisplayMetrics().heightPixels;
        int screenW = ctx.getResources().getDisplayMetrics().widthPixels;
        FrameLayout container = new FrameLayout(ctx);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        lp.topMargin = screenH * 2 / 5; container.setLayoutParams(lp);
        int pw = wide ? screenW * 4 / 5 : FrameLayout.LayoutParams.WRAP_CONTENT;
        container.addView(pill, new FrameLayout.LayoutParams(pw, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
        return container;
    }

    // Lee la preferencia explícita del usuario antes de consultar el locale.
    // DateFormat.is24HourFormat() en locales 24h (es, es_PE, etc.) ignora la
    // preferencia del usuario. Settings.System.TIME_12_24 sí la respeta.
    static boolean is24h(Context ctx) {
        String pref = Settings.System.getString(ctx.getContentResolver(), Settings.System.TIME_12_24);
        if ("12".equals(pref)) return false;
        if ("24".equals(pref)) return true;
        return DateFormat.is24HourFormat(ctx); // sin preferencia explícita: usa locale
    }

    private static int dp(Context ctx, int v) { return QuickSearchLayout.dp(ctx, v); }
}
