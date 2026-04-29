package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

class QuickSearchLayout {

    final LinearLayout root;
    final EditText     input;
    final ListView     list;
    final TextView     label;

    private QuickSearchLayout(LinearLayout root, EditText input, ListView list, TextView label) {
        this.root  = root;
        this.input = input;
        this.list  = list;
        this.label = label;
    }

    static QuickSearchLayout build(Context ctx) {
        EditText input = buildInput(ctx);
        ListView list  = buildList(ctx);

        TextView label = new TextView(ctx);
        label.setText("BUSCAR");
        label.setTextColor(0xFF4A4A4A);
        label.setTextSize(10f);
        label.setTypeface(Typeface.MONOSPACE);
        label.setLetterSpacing(0.2f);

        LinearLayout inputRow = new LinearLayout(ctx);
        inputRow.setOrientation(LinearLayout.VERTICAL);
        inputRow.setPadding(dp(ctx, 16), dp(ctx, 14), dp(ctx, 16), dp(ctx, 14));

        inputRow.addView(label);
        inputRow.addView(input);

        View separator = new View(ctx);
        separator.setBackgroundColor(0xFF1A1A1A);

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0A0A0A);
        root.addView(list, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(separator, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 1)));
        root.addView(inputRow);

        return new QuickSearchLayout(root, input, list, label);
    }

    private static EditText buildInput(Context ctx) {
        EditText et = new EditText(ctx);
        et.setTextColor(0xFFE8E8E8);
        et.setTextSize(18f);
        et.setTypeface(Typeface.MONOSPACE);
        et.setBackgroundColor(Color.TRANSPARENT);
        et.setSingleLine(true);
        et.setHint(".all  →  todo");
        et.setHintTextColor(0x28FFFFFF);
        et.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        et.setPadding(0, 0, 0, 0);
        return et;
    }

    private static ListView buildList(Context ctx) {
        ListView lv = new ListView(ctx);
        lv.setBackgroundColor(0xFF0A0A0A);
        lv.setDivider(null);
        lv.setSelector(android.R.color.transparent);
        lv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        lv.setVerticalScrollBarEnabled(false);
        lv.setStackFromBottom(true);
        lv.setPadding(0, dp(ctx, 16), 0, 0);
        lv.setClipToPadding(false);
        return lv;
    }

    static int dp(Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }
}
