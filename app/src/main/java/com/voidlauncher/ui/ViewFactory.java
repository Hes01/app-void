package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

class ViewFactory {

    static TextView mono(Context ctx, String text, int color, float sizeSp) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(sizeSp);
        tv.setTypeface(Typeface.MONOSPACE);
        return tv;
    }

    static View divider(Context ctx) {
        View v = new View(ctx);
        v.setBackgroundColor(VoidTheme.LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        return v;
    }
}
