package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

class LaunchBar {

    private static final int EXPAND_MS = 220;
    private static final int FADE_MS   = 300;

    private static final LruCache<String, Integer> colorCache = new LruCache<>(30);

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
                .setDuration(EXPAND_MS)
                .withEndAction(() ->
                    bar.animate()
                        .alpha(0f)
                        .setDuration(FADE_MS)
                        .withEndAction(() -> bar.setScaleX(0f))
                        .start())
                .start();
    }

    private static int iconColor(Context ctx, String pkg) {
        Integer cached = colorCache.get(pkg);
        if (cached != null) return cached;
        try {
            Drawable icon = ctx.getPackageManager().getApplicationIcon(pkg);
            Bitmap bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
            try {
                icon.setBounds(0, 0, 32, 32);
                icon.draw(new Canvas(bmp));
                int color = avgColor(bmp);
                colorCache.put(pkg, color);
                return color;
            } finally {
                bmp.recycle();
            }
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
