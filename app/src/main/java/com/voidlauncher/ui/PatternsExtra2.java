package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

class PatternsExtra2 {

    static void draw(Canvas c, int id, int w, int h, Context ctx) {
        switch (id) {
            case Patterns.DAMASK:  drawDamask(c, w, h, ctx);  break;
            case Patterns.GRAVURE: drawGravure(c, w, h, ctx); break;
            case Patterns.AIZAWA:  drawAstroid(c, w, h, ctx); break;
        }
    }

    // Damasco — geometría polar en hexágonos: 8 círculos orbitales + diamante exterior
    private static void drawDamask(Canvas c, int w, int h, Context ctx) {
        float tile = Patterns.dp(ctx, 62);
        Paint petal   = Patterns.stroke(VoidTheme.FG4, Patterns.dp(ctx, 0.7f));
        Paint diamond = Patterns.stroke(VoidTheme.FG5, Patterns.dp(ctx, 0.6f));
        Path sq = new Path();
        for (int row = -1; row * tile * 0.866f < h + tile; row++) {
            float oy     = row * tile * 0.866f;
            float ox_off = (row % 2 == 0) ? 0 : tile / 2f;
            for (int col = -1; col * tile + ox_off < w + tile; col++) {
                float ccx = col * tile + ox_off;
                float r   = tile * 0.40f;
                for (int i = 0; i < 8; i++) {
                    double a  = i * Math.PI / 4;
                    float pcx = ccx + (float) Math.cos(a) * r * 0.35f;
                    float pcy = oy  + (float) Math.sin(a) * r * 0.35f;
                    c.drawCircle(pcx, pcy, r * 0.22f, petal);
                }
                Patterns.drawSquare(sq, ccx, oy, r * 0.65f, 45);
                c.drawPath(sq, diamond);
            }
        }
    }

    // Gravure — campo gravitacional: líneas horizontales dobladas hacia el centro
    private static void drawGravure(Canvas c, int w, int h, Context ctx) {
        float cx      = w / 2f, cy = h / 2f;
        float spacing = Patterns.dp(ctx, 11);
        float A       = Math.min(w, h) * 0.18f;
        float lambda  = Math.min(w, h) * 0.38f;
        float omega   = Math.min(w, h) * 0.10f;
        Paint ink     = Patterns.stroke(VoidTheme.FG4, Patterns.dp(ctx, 0.7f));
        int   stepX   = Math.max(3, w / 80);
        Path  line    = new Path();
        for (float baseY = 0; baseY < h; baseY += spacing) {
            line.reset();
            boolean started = false;
            for (int xi = -(int) A; xi <= w + (int) A; xi += stepX) {
                float dist = (float) Math.sqrt((xi - cx) * (xi - cx) + (baseY - cy) * (baseY - cy));
                float pull = A * (float) Math.exp(-dist / lambda);
                float py = baseY + pull * (float) Math.sin(dist / omega);
                float px = xi    + pull * (float) Math.cos(dist / omega);
                if (!started) { line.moveTo(px, py); started = true; } else line.lineTo(px, py);
            }
            c.drawPath(line, ink);
        }
    }

    // Astroide — hipocicloide de 4 cúspides en malla: x=r·cos³(t), y=r·sin³(t)
    private static void drawAstroid(Canvas c, int w, int h, Context ctx) {
        float tile = Patterns.dp(ctx, 44);
        float r    = tile * 0.38f;
        Paint ink  = Patterns.stroke(VoidTheme.FG4, Patterns.dp(ctx, 0.9f));
        Path  path = new Path();
        int   steps = 48;
        for (float row = -1; row * tile < h + tile; row++) {
            float cy = row * tile + tile / 2f;
            float ox  = (((int) row % 2) == 0) ? 0 : tile / 2f;
            for (float col = -1; col * tile + ox < w + tile; col++) {
                float cx = col * tile + ox + tile / 2f;
                path.reset();
                for (int i = 0; i <= steps; i++) {
                    double t = 2 * Math.PI * i / steps;
                    float cos = (float) Math.cos(t), sin = (float) Math.sin(t);
                    float x = cx + r * cos * cos * cos;
                    float y = cy + r * sin * sin * sin;
                    if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
                }
                path.close();
                c.drawPath(path, ink);
            }
        }
    }
}
