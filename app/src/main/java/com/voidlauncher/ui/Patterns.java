package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

class Patterns {

    static final int DOTS      = 1;  static final int HAIRLINES = 2;
    static final int TOPO      = 4;  static final int SHIPPO    = 7;
    static final int TRUCHET   = 10; static final int DAMASK    = 14;
    static final int GRAVURE   = 15; static final int ULAM      = 18;
    static final int AIZAWA    = 19; static final int HILBERT   = 20;

    static final int[]    ALL   = { DOTS, HAIRLINES, TOPO, SHIPPO, TRUCHET, DAMASK, GRAVURE, ULAM, AIZAWA, HILBERT };
    static final String[] NAMES = { "Dots", "Hairlines", "Topo", "Shippo", "Truchet", "Damasco", "Gravure", "Ulam", "Aizawa", "Hilbert" };

    static void draw(Canvas c, int id, int w, int h, Context ctx) {
        switch (id) {
            case DOTS:      drawDots(c, w, h, ctx);                break;
            case HAIRLINES: drawHairlines(c, w, h, ctx);           break;
            case TOPO:      drawTopo(c, w, h, ctx);                break;
            case DAMASK:
            case GRAVURE:
            case AIZAWA:    PatternsExtra2.draw(c, id, w, h, ctx); break;
            default:        PatternsExtra.draw(c, id, w, h, ctx);  break;
        }
    }

    // ── Minimal ───────────────────────────────────────────────────────────────

    private static void drawDots(Canvas c, int w, int h, Context ctx) {
        float tile = dp(ctx, 18); float r = dp(ctx, 1.1f);
        Paint p = fill(VoidTheme.FG4);
        for (float y = 0; y < h + tile; y += tile)
            for (float x = 0; x < w + tile; x += tile)
                c.drawCircle(x, y, r, p);
    }

    private static void drawHairlines(Canvas c, int w, int h, Context ctx) {
        float tile = dp(ctx, 14);
        Paint p = stroke(VoidTheme.FG5, dp(ctx, 0.5f));
        float len = (float) Math.sqrt(w * w + h * h);
        for (float i = -len; i < len; i += tile)
            c.drawLine(i, 0, i + h, h, p);
    }

    private static void drawTopo(Canvas c, int w, int h, Context ctx) {
        float tw = dp(ctx, 120); float th = dp(ctx, 60);
        Paint p = stroke(VoidTheme.FG4, dp(ctx, 0.8f));
        Path path = new Path();
        for (float ty = 0; ty < h + th; ty += th)
            for (float tx = 0; tx < w + tw; tx += tw) {
                path.reset();
                path.moveTo(tx, ty + th * 0.5f);
                path.cubicTo(tx + tw*0.25f, ty, tx + tw*0.5f, ty + th, tx + tw*0.75f, ty);
                path.lineTo(tx + tw, ty + th * 0.5f);
                c.drawPath(path, p);
            }
    }

    // ── Shared utils (package-private for PatternsExtra) ──────────────────────

    static Paint fill(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setStyle(Paint.Style.FILL); p.setColor(color); return p;
    }
    static Paint stroke(int color, float w) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setStyle(Paint.Style.STROKE); p.setColor(color); p.setStrokeWidth(w); return p;
    }
    static float dp(Context ctx, float dp) { return dp * ctx.getResources().getDisplayMetrics().density; }

    static void drawSquare(Path p, float cx, float cy, float half, float deg) {
        p.reset();
        double rad = Math.toRadians(deg); float cos = (float)Math.cos(rad); float sin = (float)Math.sin(rad);
        float[][] pts = { {-half,-half},{half,-half},{half,half},{-half,half} };
        for (int i = 0; i < 4; i++) {
            float x = cx + pts[i][0]*cos - pts[i][1]*sin;
            float y = cy + pts[i][0]*sin + pts[i][1]*cos;
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.close();
    }
}
