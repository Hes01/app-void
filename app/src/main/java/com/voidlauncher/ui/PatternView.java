package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import com.voidlauncher.data.WallpaperRepository;

public class PatternView extends View {

    private final WallpaperRepository repo;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PatternView(Context ctx, WallpaperRepository repo) {
        super(ctx);
        this.repo = repo;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int id = repo.getPattern();
        if (id == WallpaperRepository.NONE) return;
        Patterns.draw(canvas, id, getWidth(), getHeight(), getContext());
    }

    public void refresh() { invalidate(); }
}
