package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

class PatternsExtra2 {

    static void draw(Canvas c, int id, int w, int h, Context ctx) {
        switch (id) {
            case Patterns.GUILLOCHE: drawGuilloche(c, w, h, ctx); break;
            case Patterns.DAMASK:    drawDamask(c, w, h, ctx);    break;
            case Patterns.GRAVURE:   drawGravure(c, w, h, ctx);   break;
            case Patterns.MOIRE:     drawMoire(c, w, h, ctx);     break;
            case Patterns.ISLAMIC:   drawIslamic(c, w, h, ctx);   break;
        }
    }

    // Guilloché — epicicloide: x = A·cos(t) + D·cos(8t), y = A·sin(t) − D·sin(8t)
    // Paleta: negro profundo + oro antiguo, 4 curvas anidadas de opacidad gradual
    private static void drawGuilloche(Canvas c, int w, int h, Context ctx) {
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF070D08));
        float cx = w / 2f, cy = h / 2f;
        float base = Math.min(w, h) * 0.44f;
        float A = base * 0.08f;
        float[] dFactors = { 0.50f, 0.65f, 0.78f, 0.88f };
        int[]   alphas   = { 0x22,   0x3C,   0x52,   0x2E  };
        int steps = 600;
        Path path = new Path();
        for (int ci = 0; ci < dFactors.length; ci++) {
            float D = base * dFactors[ci];
            Paint ink = Patterns.stroke((alphas[ci] << 24) | 0xC4A94A, Patterns.dp(ctx, 0.6f));
            path.reset();
            for (int i = 0; i <= steps; i++) {
                double t = 2 * Math.PI * i / steps;
                float x = cx + A * (float) Math.cos(t) + D * (float) Math.cos(8 * t);
                float y = cy + A * (float) Math.sin(t) - D * (float) Math.sin(8 * t);
                if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
            }
            path.close();
            c.drawPath(path, ink);
        }
    }

    // Damasco — geometría polar en hexágonos: 8 círculos orbitales + diamante exterior
    // Paleta: negro violáceo + malva empolvado
    private static void drawDamask(Canvas c, int w, int h, Context ctx) {
        float tile = Patterns.dp(ctx, 62);
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF0E080C));
        Paint petal   = Patterns.stroke(0x4A9B7BAC, Patterns.dp(ctx, 0.7f));
        Paint diamond = Patterns.stroke(0x309B7BAC, Patterns.dp(ctx, 0.6f));
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
    // pull = A·e^(−dist/λ), desplazamiento radial con frecuencia ω
    // Paleta: negro azulado + azul eléctrico
    private static void drawGravure(Canvas c, int w, int h, Context ctx) {
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF060810));
        float cx      = w / 2f, cy = h / 2f;
        float spacing = Patterns.dp(ctx, 11);
        float A       = Math.min(w, h) * 0.18f;
        float lambda  = Math.min(w, h) * 0.38f;
        float omega   = Math.min(w, h) * 0.10f;
        Paint ink     = Patterns.stroke(0x4A3B82F6, Patterns.dp(ctx, 0.7f));
        int   stepX   = Math.max(3, w / 80);
        Path  line    = new Path();
        for (float baseY = 0; baseY < h; baseY += spacing) {
            line.reset();
            boolean started = false;
            for (int xi = 0; xi <= w; xi += stepX) {
                float dist = (float) Math.sqrt((xi - cx) * (xi - cx) + (baseY - cy) * (baseY - cy));
                float pull = A * (float) Math.exp(-dist / lambda);
                float py = baseY + pull * (float) Math.sin(dist / omega);
                float px = xi    + pull * (float) Math.cos(dist / omega);
                if (!started) { line.moveTo(px, py); started = true; } else line.lineTo(px, py);
            }
            c.drawPath(line, ink);
        }
    }

    // Moiré — 3 grillas superpuestas: vertical 0°, diagonal 5°, círculos concéntricos
    // Paleta: negro puro + blanco/azul plata, efecto óptico emergente
    private static void drawMoire(Canvas c, int w, int h, Context ctx) {
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF050505));
        float s    = Patterns.dp(ctx, 5);
        float diag = (float) Math.sqrt(w * w + h * h);
        Paint g1   = Patterns.stroke(0x18FFFFFF, Patterns.dp(ctx, 0.5f));
        Paint g2   = Patterns.stroke(0x16AACFFF, Patterns.dp(ctx, 0.5f));
        Paint g3   = Patterns.stroke(0x10FFFFFF, Patterns.dp(ctx, 0.4f));
        for (float x = 0; x < w; x += s) c.drawLine(x, 0, x, h, g1);
        c.save(); c.translate(w / 2f, h / 2f); c.rotate(5);
        for (float x = -diag; x < diag; x += s) c.drawLine(x, -diag, x, diag, g2);
        c.restore();
        for (float r = s; r < diag; r += s) c.drawCircle(w / 2f, h / 2f, r, g3);
    }

    // Islámico — estrella de 8 puntas: puntas externas en R, internas en R·(√2−1), octágono en R·0.38
    // Paleta: azul medianoche + oro antiguo
    private static void drawIslamic(Canvas c, int w, int h, Context ctx) {
        float tile = Patterns.dp(ctx, 54);
        c.drawRect(0, 0, w, h, Patterns.fill(0xFF0A1020));
        Paint starP = Patterns.stroke(0x70C9A832, Patterns.dp(ctx, 0.8f));
        Paint octP  = Patterns.stroke(0x40C9A832, Patterns.dp(ctx, 0.6f));
        float R = tile * 0.43f, rIn = R * 0.414f, rOct = R * 0.38f;
        Path star = new Path(), oct = new Path();
        for (int row = -1; row * tile < h + tile; row++) {
            for (int col = -1; col * tile < w + tile; col++) {
                float scx = col * tile + tile / 2f, scy = row * tile + tile / 2f;
                star.reset();
                for (int i = 0; i < 8; i++) {
                    double ao = i * Math.PI / 4 - Math.PI / 2;
                    double ai = (i + 0.5) * Math.PI / 4 - Math.PI / 2;
                    float ox = scx + R * (float) Math.cos(ao), oy = scy + R * (float) Math.sin(ao);
                    float ix = scx + rIn * (float) Math.cos(ai), iy = scy + rIn * (float) Math.sin(ai);
                    if (i == 0) star.moveTo(ox, oy); else star.lineTo(ox, oy);
                    star.lineTo(ix, iy);
                }
                star.close(); c.drawPath(star, starP);
                oct.reset();
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4 - Math.PI / 2;
                    float x = scx + rOct * (float) Math.cos(a), y = scy + rOct * (float) Math.sin(a);
                    if (i == 0) oct.moveTo(x, y); else oct.lineTo(x, y);
                }
                oct.close(); c.drawPath(oct, octP);
            }
        }
    }
}
