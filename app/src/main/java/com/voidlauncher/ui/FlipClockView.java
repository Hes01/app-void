package com.voidlauncher.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import java.util.Calendar;

class FlipClockView extends View {

    private static final long HALF = 250; // ms por fase de volteo

    private final Paint   paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Camera  cam   = new Camera();
    private final Matrix  mat   = new Matrix();
    private final RectF   rf    = new RectF();
    private final boolean use24;

    private final int[]   cur = new int[4], nxt = new int[4], phase = new int[4];
    private final float[] ang = new float[4];

    FlipClockView(Context ctx, boolean use24) {
        super(ctx);
        this.use24 = use24;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
    }

    void tick() {
        Calendar c = Calendar.getInstance();
        int h = use24 ? c.get(Calendar.HOUR_OF_DAY) : c.get(Calendar.HOUR);
        if (!use24 && h == 0) h = 12;
        int m = c.get(Calendar.MINUTE);
        int[] v = {h/10, h%10, m/10, m%10};
        for (int i = 0; i < 4; i++) if (v[i] != cur[i] && phase[i] == 0) flip(i, v[i]);
    }

    private void flip(final int i, final int to) {
        nxt[i] = to; phase[i] = 1;
        anim(0, 90, new AccelerateInterpolator(), i,
             () -> anim(90, 0, new DecelerateInterpolator(), i,
                        () -> { cur[i] = nxt[i]; phase[i] = 0; ang[i] = 0; invalidate(); }));
    }

    private void anim(float a, float b, TimeInterpolator ip, int i, Runnable done) {
        ValueAnimator va = ValueAnimator.ofFloat(a, b);
        va.setDuration(HALF); va.setInterpolator(ip);
        va.addUpdateListener(v -> { ang[i] = (float) v.getAnimatedValue(); invalidate(); });
        va.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator ignored) { done.run(); }
        });
        va.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float W = getWidth(), H = getHeight();
        if (W == 0) return;
        float cH = H*0.88f, cW = cH*0.70f, gap = cH*0.08f, colW = cH*0.22f, rad = cW*0.10f;
        float x0 = (W - (4*cW + 4*gap + colW)) / 2f, y0 = (H - cH) / 2f;
        paint.setTextSize(cH * 0.80f);

        // H1  gap  H2  gap  [colon]  gap  M1  gap  M2
        float[] xs = {x0, x0+cW+gap, x0+2*cW+3*gap+colW, x0+3*cW+4*gap+colW};
        for (int i = 0; i < 4; i++) card(canvas, xs[i], y0, cW, cH, rad, i);

        paint.setColor(VoidTheme.FG); paint.setAlpha(150);
        float cr = cH*0.055f, cx = x0 + 2*cW + 2*gap + colW/2f;
        canvas.drawCircle(cx, y0 + cH*0.33f, cr, paint);
        canvas.drawCircle(cx, y0 + cH*0.67f, cr, paint);
        paint.setAlpha(255);
    }

    private void card(Canvas canvas, float x, float y, float cW, float cH, float rad, int i) {
        rf.set(x, y, x+cW, y+cH);
        paint.setColor(VoidTheme.BG_CARD); paint.setAlpha(255);
        canvas.drawRoundRect(rf, rad, rad, paint);

        if (phase[i] == 0) {
            half(canvas, x, y, cW, cH, cur[i], false);
            half(canvas, x, y, cW, cH, cur[i], true);
        } else {
            half(canvas, x, y, cW, cH, cur[i], true);
            half(canvas, x, y, cW, cH, nxt[i], false);
            if (phase[i] == 1) half3d(canvas, x, y, cW, cH, rad, cur[i], false, ang[i]);
            else                half3d(canvas, x, y, cW, cH, rad, nxt[i], true,  ang[i]);
        }

        paint.setColor(VoidTheme.BG); paint.setAlpha(200);
        canvas.drawRect(x, y + cH/2f - 1f, x+cW, y + cH/2f + 1f, paint);
        paint.setAlpha(255);
    }

    private void half(Canvas canvas, float x, float y, float cW, float cH, int n, boolean bot) {
        canvas.save();
        float cy = bot ? y + cH/2f : y;
        canvas.clipRect(x, cy, x+cW, cy + cH/2f);
        paint.setColor(VoidTheme.FG); paint.setAlpha(255);
        canvas.drawText(String.valueOf(n), x + cW/2f, y + cH*0.80f, paint);
        canvas.restore();
    }

    /** Top flap: rotateX 0→-90 (cae). Bottom flap: rotateX 90→0 (sube). Pivot = línea central. */
    private void half3d(Canvas canvas, float x, float y, float cW, float cH, float rad,
                         int n, boolean bot, float a) {
        float clipY = bot ? y + cH/2f : y, pivX = x + cW/2f, pivY = y + cH/2f;
        cam.save(); cam.rotateX(bot ? a : -a); cam.getMatrix(mat); cam.restore();
        mat.preTranslate(-pivX, -pivY); mat.postTranslate(pivX, pivY);
        canvas.save();
        canvas.clipRect(x, clipY, x+cW, clipY + cH/2f);
        canvas.concat(mat);
        paint.setColor(VoidTheme.BG_CARD); paint.setAlpha(255);
        rf.set(x, y, x+cW, y+cH);
        canvas.drawRoundRect(rf, rad, rad, paint);
        paint.setColor(VoidTheme.FG);
        canvas.drawText(String.valueOf(n), x + cW/2f, y + cH*0.80f, paint);
        canvas.restore();
    }
}
