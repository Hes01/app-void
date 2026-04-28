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

    private QuickSearchLayout(LinearLayout root, EditText input, ListView list) {
        this.root  = root;
        this.input = input;
        this.list  = list;
    }

    static QuickSearchLayout build(Context ctx) {
        EditText input = buildInput(ctx);
        ListView list  = buildList(ctx);

        TextView prompt = new TextView(ctx);
        prompt.setText("> ");
        prompt.setTextColor(0x55FFFFFF);
        prompt.setTextSize(18f);
        prompt.setTypeface(Typeface.MONOSPACE);

        LinearLayout inputRow = new LinearLayout(ctx);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setPadding(dp(ctx, 16), dp(ctx, 14), dp(ctx, 16), dp(ctx, 14));
        inputRow.addView(prompt);
        inputRow.addView(input, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        View separator = new View(ctx);
        separator.setBackgroundColor(0x22FFFFFF);

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.addView(list, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(separator, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 1)));
        root.addView(inputRow);

        return new QuickSearchLayout(root, input, list);
    }

    private static EditText buildInput(Context ctx) {
        EditText et = new EditText(ctx);
        et.setTextColor(Color.WHITE);
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
        lv.setBackgroundColor(Color.BLACK);
        lv.setDivider(null);
        lv.setSelector(android.R.color.transparent);
        lv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        lv.setStackFromBottom(true);
        return lv;
    }

    static int dp(Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }
}
