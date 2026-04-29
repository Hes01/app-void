package com.voidlauncher.ui;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

class VibrationFeedback {

    static void onLaunch(Context ctx) {
        vibrate(ctx, 28);
    }

    static void onNoResults(Context ctx) {
        Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(new long[]{0, 20, 50, 20}, -1));
        } else {
            //noinspection deprecation
            v.vibrate(new long[]{0, 20, 50, 20}, -1);
        }
    }

    static void onCommand(Context ctx) {
        vibrate(ctx, 14);
    }

    private static void vibrate(Context ctx, long ms) {
        Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //noinspection deprecation
            v.vibrate(ms);
        }
    }
}
