package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.voidlauncher.data.WallpaperRepository;

class WallpaperSelector {

    private static final int COLS       = 3;
    private static final int MARGIN_DP  = 3;
    private static final int PADDING_DP = 2;

    static android.view.View build(Context ctx, Runnable onChanged) {
        WallpaperRepository repo = new WallpaperRepository(ctx);
        int[] ids = new int[Patterns.ALL.length + 1];
        ids[0] = WallpaperRepository.NONE;
        System.arraycopy(Patterns.ALL, 0, ids, 1, Patterns.ALL.length);

        int screenW     = ctx.getResources().getDisplayMetrics().widthPixels;
        int screenH     = ctx.getResources().getDisplayMetrics().heightPixels;
        int margin      = dp(ctx, MARGIN_DP);
        int previewSize = Math.max(1, screenW / COLS);

        LinearLayout grid = new LinearLayout(ctx);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(0, dp(ctx, 6), 0, dp(ctx, 6));

        LinearLayout[] wrappers = new LinearLayout[ids.length];
        Bitmap[]       bitmaps  = new Bitmap[ids.length];
        int current = repo.getPattern();
        LinearLayout row = null;

        for (int i = 0; i < ids.length; i++) {
            if (i % COLS == 0) {
                row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            int id = ids[i];
            LinearLayout wrapper = makeWrapper(ctx, id == current);
            wrappers[i] = wrapper;
            bitmaps[i] = makePreviewBitmap(ctx, id, previewSize);
            wrapper.addView(makePreview(ctx, bitmaps[i]));

            final int fi = i;
            wrapper.setOnClickListener(v -> {
                repo.setPattern(id);
                for (LinearLayout w : wrappers) w.setBackgroundColor(0);
                wrappers[fi].setBackgroundColor(VoidTheme.FG4);
                if (onChanged != null) onChanged.run();
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 0);
            lp.weight = 1f;
            lp.setMargins(margin, margin, margin, margin);
            row.addView(wrapper, lp);
            wrapper.post(() -> {
                int w = wrapper.getWidth();
                if (w > 0 && wrapper.getLayoutParams().height != w) {
                    wrapper.getLayoutParams().height = w;
                    wrapper.requestLayout();
                }
            });
        }

        ScrollView scroll = new ScrollView(ctx) {
            private int lastY;
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastY = (int) ev.getY();
                        getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dy = lastY - (int) ev.getY();
                        lastY = (int) ev.getY();
                        getParent().requestDisallowInterceptTouchEvent(
                                canScrollVertically(dy > 0 ? 1 : -1));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return super.dispatchTouchEvent(ev);
            }
        };
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, screenH * 2 / 5));
        scroll.addView(grid);
        scroll.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View v) {}
            @Override public void onViewDetachedFromWindow(View v) {
                for (Bitmap bmp : bitmaps) if (bmp != null && !bmp.isRecycled()) bmp.recycle();
            }
        });
        return scroll;
    }

    private static LinearLayout makeWrapper(Context ctx, boolean selected) {
        LinearLayout w = new LinearLayout(ctx);
        w.setPadding(dp(ctx, PADDING_DP), dp(ctx, PADDING_DP), dp(ctx, PADDING_DP), dp(ctx, PADDING_DP));
        w.setBackgroundColor(selected ? VoidTheme.FG4 : 0);
        return w;
    }

    private static Bitmap makePreviewBitmap(Context ctx, int id, int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(VoidTheme.BG);
        if (id != WallpaperRepository.NONE) Patterns.draw(c, id, size, size, ctx);
        return bmp;
    }

    private static ImageView makePreview(Context ctx, Bitmap bmp) {
        ImageView iv = new ImageView(ctx);
        iv.setImageBitmap(bmp);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        return iv;
    }

    private static int dp(Context ctx, int v) { return QuickSearchLayout.dp(ctx, v); }
}
