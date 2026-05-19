package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

class PatternsExtra {

    static void draw(Canvas c, int id, int w, int h, Context ctx) {
        switch (id) {
            case Patterns.SEIGAIHA: drawSeigaiha(c, w, h, ctx); break;
            case Patterns.ASANOHA:  drawAsanoha(c, w, h, ctx);  break;
            case Patterns.SHIPPO:   drawShippo(c, w, h, ctx);   break;
            case Patterns.KIKKO:    drawKikko(c, w, h, ctx);    break;
            case Patterns.KAGOME:   drawKagome(c, w, h, ctx);   break;
            case Patterns.TRUCHET:  drawTruchet(c, w, h, ctx);  break;
            case Patterns.ISO:      drawIso(c, w, h, ctx);      break;
            case Patterns.OCTAGRAM: drawOctagram(c, w, h, ctx); break;
        }
    }

    // ── Wagara ────────────────────────────────────────────────────────────────

    private static void drawSeigaiha(Canvas c, int w, int h, Context ctx) {
        float tile = Patterns.dp(ctx, 40); float r = tile / 2f;
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF1A2733));
        Paint ink = Patterns.stroke(0x52D4DDE3, Patterns.dp(ctx, 1f));
        for (float row = 0; row < h / tile + 2; row++) {
            float ox = (row % 2 == 0) ? 0 : r;
            for (float col = -1; col < w / tile + 2; col++) {
                float cx = col * tile + ox; float cy = row * tile * 0.75f;
                for (int a = 0; a < 3; a++) c.drawCircle(cx, cy, r - a * Patterns.dp(ctx, 4), ink);
            }
        }
    }

    private static void drawAsanoha(Canvas c, int w, int h, Context ctx) {
        float tw = Patterns.dp(ctx, 31.18f); float th = Patterns.dp(ctx, 54);
        c.drawRect(0, 0, w, h, Patterns.fill(0xFFECE6D8));
        Paint ink = Patterns.stroke(0x381A1A1A, Patterns.dp(ctx, 0.8f));
        Path star = new Path();
        for (float ty = -th; ty < h + th; ty += th)
            for (float tx = -tw; tx < w + tw; tx += tw) {
                star.reset();
                Patterns.drawStar6(star, tx + tw / 2, ty + th / 2, Patterns.dp(ctx, 14));
                c.drawPath(star, ink);
            }
    }

    private static void drawShippo(Canvas c, int w, int h, Context ctx) {
        float tile = Patterns.dp(ctx, 40); float r = tile / 2f;
        c.drawRect(0, 0, w, h, Patterns.fill(0xFFF0E3CC));
        Paint ink = Patterns.stroke(0x473A2E1F, Patterns.dp(ctx, 1f));
        for (float row = -1; row < h / tile + 2; row++)
            for (float col = -1; col < w / tile + 2; col++) {
                c.drawCircle(col * tile, row * tile, r, ink);
                c.drawCircle(col * tile + r, row * tile + r, r, ink);
            }
    }

    private static void drawKikko(Canvas c, int w, int h, Context ctx) {
        float tw = Patterns.dp(ctx, 24); float th = Patterns.dp(ctx, 42);
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF1A1A1A));
        Paint ink = Patterns.stroke(0x2EECE6D8, Patterns.dp(ctx, 0.8f));
        Path hex = new Path();
        for (float row = -1; row < h / th + 2; row++)
            for (float col = -1; col < w / tw + 2; col++) {
                float cx = col * tw + (row % 2 == 0 ? 0 : tw / 2f);
                Patterns.drawHex(hex, cx, row * th * 0.75f, tw / 2f);
                c.drawPath(hex, ink);
            }
    }

    // ── Optical ───────────────────────────────────────────────────────────────

    private static void drawKagome(Canvas c, int w, int h, Context ctx) {
        float tw = Patterns.dp(ctx, 28); float th = Patterns.dp(ctx, 24);
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF2D3A2E));
        Paint ink = Patterns.stroke(0x42E6E1CF, Patterns.dp(ctx, 0.8f));
        Path tri = new Path();
        for (float row = -1; row < h / th + 2; row++)
            for (float col = -1; col < w / tw + 2; col++) {
                float x = col * tw + (row % 2 == 0 ? 0 : tw / 2f); float y = row * th;
                tri.reset(); tri.moveTo(x, y); tri.lineTo(x + tw/2, y + th); tri.lineTo(x - tw/2, y + th); tri.close();
                c.drawPath(tri, ink);
                tri.reset(); tri.moveTo(x, y + th); tri.lineTo(x + tw/2, y); tri.lineTo(x - tw/2, y); tri.close();
                c.drawPath(tri, ink);
            }
    }

    private static void drawTruchet(Canvas c, int w, int h, Context ctx) {
        float tile = Patterns.dp(ctx, 40); float r = tile / 2f;
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF1A1A1A));
        Paint ink = Patterns.stroke(0xB2D97757, Patterns.dp(ctx, 2f));
        for (float row = -1; row < h / tile + 2; row++)
            for (float col = -1; col < w / tile + 2; col++) {
                float x = col * tile; float y = row * tile;
                boolean flip = ((int)(row + col)) % 2 == 0;
                if (flip) {
                    c.drawArc(x-r, y-r, x+r, y+r, 0, 90, false, ink);
                    c.drawArc(x+r, y+r, x+tile+r, y+tile+r, 180, 90, false, ink);
                } else {
                    c.drawArc(x+r, y-r, x+tile+r, y+r, 90, 90, false, ink);
                    c.drawArc(x-r, y+r, x+r, y+tile+r, 270, 90, false, ink);
                }
            }
    }

    private static void drawIso(Canvas c, int w, int h, Context ctx) {
        float tw = Patterns.dp(ctx, 48); float th = Patterns.dp(ctx, 83);
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF15151A));
        Paint ink = Patterns.stroke(0x729AA3B8, Patterns.dp(ctx, 0.7f));
        float dx = (float)(tw / Math.sqrt(3));
        for (float y = -th; y < h + th; y += th / 2f)
            for (float x = -tw; x < w + tw; x += tw) {
                c.drawLine(x, y, x + dx, y + th/2f, ink);
                c.drawLine(x + tw, y, x + tw - dx, y + th/2f, ink);
                c.drawLine(x, y + th/2f, x + tw, y + th/2f, ink);
            }
    }

    private static void drawOctagram(Canvas c, int w, int h, Context ctx) {
        float tile = Patterns.dp(ctx, 40); float s = tile * 0.35f;
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF0E2A3A));
        Paint ink = Patterns.stroke(0x59E6C989, Patterns.dp(ctx, 0.9f));
        Path sq = new Path();
        for (float row = -1; row < h / tile + 2; row++)
            for (float col = -1; col < w / tile + 2; col++) {
                float cx = col * tile + tile/2f; float cy = row * tile + tile/2f;
                Patterns.drawSquare(sq, cx, cy, s, 0);   c.drawPath(sq, ink);
                Patterns.drawSquare(sq, cx, cy, s, 45);  c.drawPath(sq, ink);
            }
    }
}
