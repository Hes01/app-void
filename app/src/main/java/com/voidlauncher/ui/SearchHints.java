package com.voidlauncher.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

class SearchHints {

    private static final String PREF = "void_search_hints";
    private static final String KEY  = "done";
    private static final String TEXT = "bienvenido a void";

    static void showIfNeeded(Context ctx, LinearLayout hintRow, TextView hintText, EditText input) {
        if (ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(KEY, false)) return;

        View dot = hintRow.getChildAt(0);
        hintRow.setVisibility(View.VISIBLE);
        hintRow.post(() -> {
            breathe(dot);
            typewriter(hintText, 0, new boolean[]{true});
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.length() == 0) return;
                input.removeTextChangedListener(this);
                dismiss(ctx, hintRow, dot);
            }
        });
    }

    private static void breathe(View dot) {
        dot.animate().alpha(0.15f).setDuration(900)
                .withEndAction(() -> dot.animate().alpha(1f).setDuration(900)
                        .withEndAction(() -> breathe(dot))
                        .start())
                .start();
    }

    private static void typewriter(TextView tv, int i, boolean[] active) {
        if (!active[0] || i > TEXT.length()) return;
        tv.setText(TEXT.substring(0, i));
        tv.postDelayed(() -> typewriter(tv, i + 1, active), 55);
    }

    private static void dismiss(Context ctx, LinearLayout hintRow, View dot) {
        dot.animate().cancel();
        hintRow.animate().alpha(0f).setDuration(220)
                .withEndAction(() -> hintRow.setVisibility(View.GONE))
                .start();
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
           .edit().putBoolean(KEY, true).apply();
    }
}
