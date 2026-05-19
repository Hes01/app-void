package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

class Patterns {

    static final int DOTS      = 1;  static final int HAIRLINES = 2;
    static final int PLUS      = 3;  static final int TOPO      = 4;
    static final int SEIGAIHA  = 5;  static final int ASANOHA   = 6;
    static final int SHIPPO    = 7;  static final int KIKKO     = 8;
    static final int KAGOME    = 9;  static final int TRUCHET   = 10;
    static final int ISO       = 11; static final int OCTAGRAM  = 12;

    static final int[]    ALL   = { DOTS,HAIRLINES,PLUS,TOPO,SEIGAIHA,ASANOHA,SHIPPO,KIKKO,KAGOME,TRUCHET,ISO,OCTAGRAM };
    static final String[] NAMES = { "Dots","Hairlines","Plus","Topo","Seigaiha","Asanoha","Shippo","Kikko","Kagome","Truchet","Iso","Octagram" };

    static void draw(Canvas c, int id, int w, int h, Context ctx) {
        switch (id) {
            case DOTS:      drawDots(c, w, h, ctx);               break;
            case HAIRLINES: drawHairlines(c, w, h, ctx);          break;
            case PLUS:      drawPlus(c, w, h, ctx);               break;
            case TOPO:      drawTopo(c, w, h, ctx);               break;
            default:        PatternsExtra.draw(c, id, w, h, ctx); break;
        }
    }

    // ── Minimal ───────────────────────────────────────────────────────────────

    private static void drawDots(Canvas c, int w, int h, Context ctx) {
        float tile = dp(ctx, 18); float r = dp(ctx, 1.1f);
        Paint p = fill(0x80FFFFFF);
        for (float y = 0; y < h + tile; y += tile)
            for (float x = 0; x < w + tile; x += tile)
                c.drawCircle(x, y, r, p);
    }

    private static void drawHairlines(Canvas c, int w, int h, Context ctx) {
        float tile = dp(ctx, 14);
        Paint p = stroke(0x2E1A1A1A, dp(ctx, 0.5f));
        float len = (float) Math.sqrt(w * w + h * h);
        for (float i = -len; i < len; i += tile)
            c.drawLine(i, 0, i + h, h, p);
    }

    private static void drawPlus(Canvas c, int w, int h, Context ctx) {
        float tile = dp(ctx, 22); float arm = dp(ctx, 3);
        Paint p = stroke(0x661A1A1A, dp(ctx, 0.7f));
        for (float y = tile / 2; y < h + tile; y += tile)
            for (float x = tile / 2; x < w + tile; x += tile) {
                c.drawLine(x - arm, y, x + arm, y, p);
                c.drawLine(x, y - arm, x, y + arm, p);
            }
    }

    private static void drawTopo(Canvas c, int w, int h, Context ctx) {
        float tw = dp(ctx, 120); float th = dp(ctx, 60);
        Paint p = stroke(0x662A3B2E, dp(ctx, 0.8f));
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

    static void drawStar6(Path p, float cx, float cy, float r) {
        float r2 = r * 0.5f;
        for (int i = 0; i < 6; i++) {
            double a1 = Math.toRadians(i * 60 - 90); double a2 = Math.toRadians(i * 60 - 60);
            if (i == 0) p.moveTo(cx + r*(float)Math.cos(a1), cy + r*(float)Math.sin(a1));
            else        p.lineTo(cx + r*(float)Math.cos(a1), cy + r*(float)Math.sin(a1));
            p.lineTo(cx + r2*(float)Math.cos(a2), cy + r2*(float)Math.sin(a2));
        }
        p.close();
    }

    static void drawHex(Path p, float cx, float cy, float r) {
        p.reset();
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(i * 60);
            float x = cx + r*(float)Math.cos(a); float y = cy + r*(float)Math.sin(a);
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.close();
    }

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
