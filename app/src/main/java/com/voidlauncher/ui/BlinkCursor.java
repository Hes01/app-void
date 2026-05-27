package com.voidlauncher.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

class BlinkCursor extends View {

    private final Handler  handler = new Handler(Looper.getMainLooper());
    private       boolean  running;

    BlinkCursor(Context ctx) {
        super(ctx);
        setBackgroundColor(VoidTheme.FG);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    private void start() {
        if (running) return;
        running = true;
        setVisibility(VISIBLE);
        handler.postDelayed(tick, 500);
    }

    private void stop() {
        running = false;
        handler.removeCallbacks(tick);
    }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            setVisibility(getVisibility() == VISIBLE ? INVISIBLE : VISIBLE);
            handler.postDelayed(this, 500);
        }
    };
}
