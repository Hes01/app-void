package com.voidlauncher.ui;

import android.content.Context;

class UiUtil {
    static int dp(Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }
    static float dpf(Context ctx, float dp) {
        return dp * ctx.getResources().getDisplayMetrics().density;
    }
}
