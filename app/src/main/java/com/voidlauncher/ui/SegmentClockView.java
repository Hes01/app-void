package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import java.util.Calendar;

class SegmentClockView extends View {

    private static final int[][] SEGS = {
        {1,1,1,1,1,1,0}, // 0
        {0,1,1,0,0,0,0}, // 1
        {1,1,0,1,1,0,1}, // 2
        {1,1,1,1,0,0,1}, // 3
        {0,1,1,0,0,1,1}, // 4
        {1,0,1,1,0,1,1}, // 5
        {1,0,1,1,1,1,1}, // 6
        {1,1,1,0,0,0,0}, // 7
        {1,1,1,1,1,1,1}, // 8
        {1,1,1,1,0,1,1}, // 9
    };

    private final Paint onP  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint offP = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF  rf  = new RectF();
    private final boolean use24;

    SegmentClockView(Context ctx, boolean use24) {
        super(ctx);
        this.use24 = use24;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onP.setColor(VoidTheme.FG);
        offP.setColor((VoidTheme.FG & 0x00FFFFFF) | 0x0F000000);
        if (getWidth() == 0) return;
        Calendar cal = Calendar.getInstance();
        int h = use24 ? cal.get(Calendar.HOUR_OF_DAY) : cal.get(Calendar.HOUR);
        if (!use24 && h == 0) h = 12;
        int m = cal.get(Calendar.MINUTE);

        float W = getWidth(), H = getHeight();
        float dH = H * 0.85f, dW = dH * 0.55f, T = dW * 0.16f;
        float sp = dW * 0.20f, cW = T * 2.4f;
        float x0 = (W - (4*dW + 3*sp + cW)) / 2f, y0 = (H - dH) / 2f;

        digit(canvas, h/10, x0,                       y0, dW, dH, T);
        digit(canvas, h%10, x0+dW+sp,                 y0, dW, dH, T);
        colon(canvas,        x0+2*(dW+sp),             y0, dH, T);
        digit(canvas, m/10, x0+2*(dW+sp)+cW+sp,       y0, dW, dH, T);
        digit(canvas, m%10, x0+3*(dW+sp)+cW,          y0, dW, dH, T);
    }

    private void digit(Canvas c, int n, float x, float y, float dW, float dH, float T) {
        if (n == 1) x -= (dW - T) / 2f; // centra el 1 — b,c están en el borde derecho, los movemos al centro
        int[] s = SEGS[n];
        float g = T*0.12f, hL = dW-2*T-2*g, vL = dH/2-T-2*g, mid = y+dH/2;
        seg(c, s[0], x+T+g,  y,        hL, T );  // a top
        seg(c, s[1], x+dW-T, y+T+g,   T,  vL);  // b top-right
        seg(c, s[2], x+dW-T, mid+g,   T,  vL);  // c bot-right
        seg(c, s[3], x+T+g,  y+dH-T,  hL, T );  // d bottom
        seg(c, s[4], x,      mid+g,   T,  vL);  // e bot-left
        seg(c, s[5], x,      y+T+g,   T,  vL);  // f top-left
        seg(c, s[6], x+T+g,  mid-T/2, hL, T );  // g middle
    }

    private void seg(Canvas c, int on, float x, float y, float w, float h) {
        rf.set(x, y, x+w, y+h);
        c.drawRoundRect(rf, Math.min(w,h)/2f, Math.min(w,h)/2f, on==1 ? onP : offP);
    }

    private void colon(Canvas c, float x, float y, float dH, float T) {
        float r = T * 0.55f;
        c.drawCircle(x+r, y+dH*0.33f, r, onP);
        c.drawCircle(x+r, y+dH*0.67f, r, onP);
    }
}
