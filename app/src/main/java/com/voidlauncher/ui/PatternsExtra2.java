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
            case Patterns.AIZAWA:  drawAizawa(c, w, h);       break;
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

    // Atractor de Aizawa — caos determinista 3D: Euler + rotación + perspectiva + z-fading
    private static void drawAizawa(Canvas c, int w, int h) {
        double a=0.95, b=0.7, c2=0.6, d=3.5, e=0.25, f=0.1, dt=0.01;
        double cosX=Math.cos(1.20), sinX=Math.sin(1.20);
        double cosY=Math.cos(0.50), sinY=Math.sin(0.50);
        // paso 1: rango Z para normalización
        double x=0.1, y=0, z=0; float minZ=9999, maxZ=-9999;
        for (int i=-1000; i<45000; i++) {
            double rx=(z-b)*x-d*y, ry=d*x+(z-b)*y;
            double rz=c2+a*z-z*z*z/3.0-(x*x+y*y)*(1+e*z)+f*z*x*x*x;
            x+=rx*dt; y+=ry*dt; z+=rz*dt; if (i<0) continue;
            double y1=y*cosX-z*sinX, z1=y*sinX+z*cosX, z2=-x*sinY+z1*cosY;
            if (z2<minZ) minZ=(float)z2; if (z2>maxZ) maxZ=(float)z2;
        }
        // paso 2: dibujar con z-fading
        float zR=maxZ-minZ; if (zR==0) zR=1;
        float sc=Math.min(w,h)/5.5f, cx=w/2f, cy=h*0.45f, P=8f;
        Paint ink=new Paint(Paint.ANTI_ALIAS_FLAG);
        ink.setStyle(Paint.Style.STROKE); ink.setColor(VoidTheme.FG);
        x=0.1; y=0; z=0; float px=0, py=0;
        for (int i=-1000; i<45000; i++) {
            double rx=(z-b)*x-d*y, ry=d*x+(z-b)*y;
            double rz=c2+a*z-z*z*z/3.0-(x*x+y*y)*(1+e*z)+f*z*x*x*x;
            x+=rx*dt; y+=ry*dt; z+=rz*dt; if (i<0) continue;
            double y1=y*cosX-z*sinX, z1=y*sinX+z*cosX;
            double x2=x*cosY+z1*sinY, z2=-x*sinY+z1*cosY;
            float fov=P/(P-(float)z2);
            float sx=cx+(float)x2*sc*fov, sy=cy+(float)y1*sc*fov;
            if (i>0) {
                float zN=((float)z2-minZ)/zR;
                ink.setAlpha((int)(5+zN*58)); ink.setStrokeWidth(0.5f+zN*1.0f);
                c.drawLine(px,py,sx,sy,ink);
            }
            px=sx; py=sy;
        }
    }
}
