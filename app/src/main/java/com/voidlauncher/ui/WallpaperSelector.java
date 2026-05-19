package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.voidlauncher.data.WallpaperRepository;

class WallpaperSelector {

    private static final int CELL = 80;

    static android.view.View build(Context ctx, Runnable onChanged) {
        WallpaperRepository repo = new WallpaperRepository(ctx);
        int[] ids = new int[Patterns.ALL.length + 1];
        ids[0] = WallpaperRepository.NONE;
        System.arraycopy(Patterns.ALL, 0, ids, 1, Patterns.ALL.length);

        LinearLayout grid = new LinearLayout(ctx);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(0, dp(ctx, 6), 0, dp(ctx, 6));

        LinearLayout[] wrappers = new LinearLayout[ids.length];
        int current = repo.getPattern();
        LinearLayout row = null;

        for (int i = 0; i < ids.length; i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row);
            }
            int id = ids[i];
            LinearLayout wrapper = makeWrapper(ctx, id == current);
            wrappers[i] = wrapper;
            wrapper.addView(makePreview(ctx, id));

            final int fi = i;
            wrapper.setOnClickListener(v -> {
                repo.setPattern(id);
                for (LinearLayout w : wrappers) w.setBackgroundColor(0);
                wrappers[fi].setBackgroundColor(VoidTheme.FG4);
                if (onChanged != null) onChanged.run();
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    dp(ctx, CELL + 4), dp(ctx, CELL + 4));
            lp.setMargins(dp(ctx, 3), dp(ctx, 3), dp(ctx, 3), dp(ctx, 3));
            row.addView(wrapper, lp);
        }
        return grid;
    }

    private static LinearLayout makeWrapper(Context ctx, boolean selected) {
        LinearLayout w = new LinearLayout(ctx);
        w.setPadding(dp(ctx, 2), dp(ctx, 2), dp(ctx, 2), dp(ctx, 2));
        w.setBackgroundColor(selected ? VoidTheme.FG4 : 0);
        return w;
    }

    private static ImageView makePreview(Context ctx, int id) {
        int size = dp(ctx, CELL);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(VoidTheme.BG);
        if (id != WallpaperRepository.NONE) Patterns.draw(c, id, size, size, ctx);
        ImageView iv = new ImageView(ctx);
        iv.setImageBitmap(bmp);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        return iv;
    }

    private static int dp(Context ctx, int v) { return QuickSearchLayout.dp(ctx, v); }
}
