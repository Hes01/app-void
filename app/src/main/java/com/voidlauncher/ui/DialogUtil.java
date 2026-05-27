package com.voidlauncher.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.WindowManager;

class DialogUtil {

    static Dialog makeFullscreen(Context ctx, View content) {
        Dialog d = new Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        d.setContentView(content);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new ColorDrawable(VoidTheme.BG));
            d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            d.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
        return d;
    }
}
