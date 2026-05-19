package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

class LaunchBar {

    static View attach(FrameLayout root) {
        Context ctx = root.getContext();
        View bar = new View(ctx);
        bar.setBackgroundColor(VoidTheme.FG3);
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

    static void show(View bar, Context ctx, String pkg) {
        bar.setBackgroundColor(iconColor(ctx, pkg));
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

    private static int iconColor(Context ctx, String pkg) {
        try {
            Drawable icon = ctx.getPackageManager().getApplicationIcon(pkg);
            Bitmap bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
            icon.setBounds(0, 0, 32, 32);
            icon.draw(new Canvas(bmp));
            return avgColor(bmp);
        } catch (Exception e) {
            return VoidTheme.FG3;
        }
    }

    private static int avgColor(Bitmap bmp) {
        long r = 0, g = 0, b = 0, n = 0;
        for (int y = 6; y < 26; y += 2)
            for (int x = 6; x < 26; x += 2) {
                int px = bmp.getPixel(x, y);
                if ((px >>> 24) > 64) { r += (px >> 16) & 0xFF; g += (px >> 8) & 0xFF; b += px & 0xFF; n++; }
            }
        return n == 0 ? VoidTheme.FG3 : (0xFF000000 | ((int)(r/n) << 16) | ((int)(g/n) << 8) | (int)(b/n));
    }
}
