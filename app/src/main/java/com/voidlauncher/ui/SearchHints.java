package com.voidlauncher.ui;

import android.content.Context;
import android.text.Editable;
import com.hes01.voidlauncher.R;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

class SearchHints {

    private static final String PREF = "void_search_hints";
    private static final String KEY  = "done";

    static void showIfNeeded(Context ctx, LinearLayout hintRow, TextView hintText, EditText input) {
        if (ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean(KEY, false)) return;

        View dot = hintRow.getChildAt(0);
        hintRow.setVisibility(View.VISIBLE);
        String text = ctx.getString(R.string.welcome);
        hintRow.post(() -> {
            breathe(dot);
            typewriter(hintText, text, 0, new boolean[]{true});
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

    private static void typewriter(TextView tv, String text, int i, boolean[] active) {
        if (!active[0] || i > text.length()) return;
        tv.setText(text.substring(0, i));
        tv.postDelayed(() -> typewriter(tv, text, i + 1, active), 55);
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
