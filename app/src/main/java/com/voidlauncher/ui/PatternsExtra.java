package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

class PatternsExtra {

    static void draw(Canvas c, int id, int w, int h, Context ctx) {
        switch (id) {
            case Patterns.SHIPPO:  drawShippo(c, w, h, ctx);  break;
            case Patterns.TRUCHET: drawTruchet(c, w, h, ctx); break;
            case Patterns.ULAM:    drawUlam(c, w, h, ctx);    break;
            case Patterns.HILBERT: drawHilbert(c, w, h);      break;
        }
    }

    private static void drawShippo(Canvas c, int w, int h, Context ctx) {
        float tile = Patterns.dp(ctx, 40); float r = tile / 2f;
        Paint ink = Patterns.stroke(VoidTheme.FG4, Patterns.dp(ctx, 1f));
        for (float row = -1; row < h / tile + 2; row++)
            for (float col = -1; col < w / tile + 2; col++) {
                c.drawCircle(col * tile, row * tile, r, ink);
                c.drawCircle(col * tile + r, row * tile + r, r, ink);
            }
    }

    private static void drawTruchet(Canvas c, int w, int h, Context ctx) {
        float tile = Patterns.dp(ctx, 40); float r = tile / 2f;
        Paint ink = Patterns.stroke(VoidTheme.FG3, Patterns.dp(ctx, 2f));
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

    // Espiral de Ulam: los primos dibujan diagonales ocultas sobre el vacío
    private static void drawUlam(Canvas c, int w, int h, Context ctx) {
        float cell = Patterns.dp(ctx, 8);
        Paint dot = Patterns.fill(VoidTheme.FG4);
        float cx = w / 2f, cy = h / 2f;
        float x = cx, y = cy, r = cell * 0.32f;
        float[] dx = { cell, 0, -cell, 0 };
        float[] dy = { 0, -cell, 0, cell };
        int num = 1, dir = 0, segLen = 1, steps = 0, turns = 0;
        int maxN = ((int)(w / cell) + 4) * ((int)(h / cell) + 4) * 3;
        while (num <= maxN) {
            if (x >= -cell && x <= w + cell && y >= -cell && y <= h + cell)
                if (isPrime(num)) c.drawCircle(x, y, r, dot);
            x += dx[dir]; y += dy[dir];
            if (++steps == segLen) {
                steps = 0; dir = (dir + 1) % 4;
                if (++turns % 2 == 0) segLen++;
            }
            num++;
        }
    }

    private static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; (long) i * i <= n; i += 6)
            if (n % i == 0 || n % (i + 2) == 0) return false;
        return true;
    }

    // Curva de Hilbert — fractal de relleno de espacio: pistas de cobre con fade radial
    private static void drawHilbert(Canvas c, int w, int h) {
        int n = 64;
        float cell = (float) Math.max(w, h) / n;
        float offX = (w - cell * n) / 2f, offY = (h - cell * n) / 2f;
        float cx = w / 2f, cy = h / 2f, maxR = (float) Math.hypot(w / 2f, h / 2f);
        Paint ink = new Paint(Paint.ANTI_ALIAS_FLAG);
        ink.setStyle(Paint.Style.STROKE);
        ink.setStrokeCap(Paint.Cap.SQUARE);
        ink.setStrokeJoin(Paint.Join.BEVEL);
        ink.setColor(VoidTheme.FG);
        int[] p0 = {0, 0}, p1 = {0, 0};
        hilbert(n, 0, p0);
        for (int i = 1; i < n * n; i++) {
            hilbert(n, i, p1);
            float x0 = offX+(p0[0]+0.5f)*cell, y0 = offY+(p0[1]+0.5f)*cell;
            float x1 = offX+(p1[0]+0.5f)*cell, y1 = offY+(p1[1]+0.5f)*cell;
            float mx = (x0+x1)*0.5f, my = (y0+y1)*0.5f;
            float dist = (float) Math.sqrt((mx-cx)*(mx-cx)+(my-cy)*(my-cy));
            float fade = (float) Math.pow(Math.max(0.12f, 1f - dist/maxR), 1.0f);
            ink.setAlpha((int)(fade * 200));
            ink.setStrokeWidth(0.4f + fade * 1.4f);
            c.drawLine(x0, y0, x1, y1, ink);
            p0[0] = p1[0]; p0[1] = p1[1];
        }
    }

    private static void hilbert(int n, int d, int[] xy) {
        xy[0] = xy[1] = 0;
        for (int s = 1, t = d; s < n; s *= 2) {
            int rx = 1 & (t >> 1), ry = 1 & (t ^ rx);
            if (ry == 0) {
                if (rx == 1) { xy[0] = s-1-xy[0]; xy[1] = s-1-xy[1]; }
                int tmp = xy[0]; xy[0] = xy[1]; xy[1] = tmp;
            }
            xy[0] += s * rx; xy[1] += s * ry; t >>= 2;
        }
    }
}
