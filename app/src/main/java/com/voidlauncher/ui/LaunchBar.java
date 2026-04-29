package com.voidlauncher.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

class LaunchBar {

    static View attach(FrameLayout root) {
        Context ctx = root.getContext();
        View bar = new View(ctx);
        bar.setBackgroundColor(0xFF6A6A6A);
        bar.setAlpha(0f);
        bar.setScaleX(0f);
        bar.setPivotX(0f);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                QuickSearchLayout.dp(ctx, 1));
        lp.gravity = Gravity.BOTTOM | Gravity.START;
        root.addView(bar, lp);
        return bar;
    }

    static void show(View bar) {
        bar.animate().cancel();
        bar.setScaleX(0f);
        bar.setAlpha(1f);
        bar.animate()
                .scaleX(1f)
                .setDuration(220)
                .withEndAction(() ->
                    bar.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> bar.setScaleX(0f))
                        .start())
                .start();
    }
}
