package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import com.voidlauncher.data.WallpaperRepository;

public class PatternView extends View {

    private final WallpaperRepository repo;
    private Bitmap cache;
    private int    cachedId = -1;

    public PatternView(Context ctx, WallpaperRepository repo) {
        super(ctx);
        this.repo = repo;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int id = repo.getPattern();
        if (id == WallpaperRepository.NONE) return;
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        if (cache == null || cachedId != id || cache.getWidth() != w || cache.getHeight() != h) {
            if (cache != null) cache.recycle();
            cache = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Patterns.draw(new Canvas(cache), id, w, h, getContext());
            cachedId = id;
        }
        canvas.drawBitmap(cache, 0, 0, null);
    }

    public void refresh() { cachedId = -1; invalidate(); }
}
