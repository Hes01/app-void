package com.voidlauncher.ui;

import android.content.Context;
import com.hes01.voidlauncher.R;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

class QuickSearchLayout {

    final LinearLayout root;
    final EditText     input;
    final ListView     list;
    final TextView     label;
    final LinearLayout hintRow;
    final TextView     hintText;
    final BlinkCursor  cursor;

    private QuickSearchLayout(LinearLayout root, EditText input, ListView list,
                               TextView label, LinearLayout hintRow, TextView hintText,
                               BlinkCursor cursor) {
        this.root = root; this.input = input; this.list = list;
        this.label = label; this.hintRow = hintRow; this.hintText = hintText;
        this.cursor = cursor;
    }

    static QuickSearchLayout build(Context ctx) {
        EditText input = buildInput(ctx);
        ListView list  = buildList(ctx);

        TextView label = new TextView(ctx);
        label.setText(ctx.getString(R.string.search_label).toUpperCase());
        label.setTextColor(VoidTheme.FG4);
        label.setTextSize(VoidTheme.TEXT_XS);
        label.setTypeface(Typeface.MONOSPACE);
        label.setLetterSpacing(0.2f);

        TextView dot = new TextView(ctx);
        dot.setText("·"); dot.setTextColor(VoidTheme.FG3);
        dot.setTextSize(VoidTheme.TEXT_XL); dot.setTypeface(Typeface.MONOSPACE);
        dot.setPadding(0, 0, dp(ctx, 10), 0);

        TextView hintText = new TextView(ctx);
        hintText.setTextColor(VoidTheme.FG3);
        hintText.setTextSize(VoidTheme.TEXT_MD);
        hintText.setTypeface(Typeface.MONOSPACE);
        hintText.setLetterSpacing(0.04f);

        LinearLayout hintRow = new LinearLayout(ctx);
        hintRow.setOrientation(LinearLayout.HORIZONTAL);
        hintRow.setGravity(Gravity.CENTER_VERTICAL);
        hintRow.setPadding(0, dp(ctx, 10), 0, dp(ctx, 6));
        hintRow.addView(dot); hintRow.addView(hintText);
        hintRow.setVisibility(View.GONE);

        input.setCursorVisible(false);
        BlinkCursor cursor = new BlinkCursor(ctx);
        int cursorW = dp(ctx, 9), cursorH = dp(ctx, 18);
        FrameLayout inputFrame = new FrameLayout(ctx);
        inputFrame.addView(input, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL));
        FrameLayout.LayoutParams curLp = new FrameLayout.LayoutParams(cursorW, cursorH, Gravity.START | Gravity.CENTER_VERTICAL);
        inputFrame.addView(cursor, curLp);
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) cursor.getLayoutParams();
                lp.leftMargin = (int) input.getPaint().measureText(s.toString());
                cursor.setLayoutParams(lp);
            }
        });

        LinearLayout inputRow = new LinearLayout(ctx);
        inputRow.setOrientation(LinearLayout.VERTICAL);
        inputRow.setPadding(dp(ctx, 16), dp(ctx, 14), dp(ctx, 16), dp(ctx, 14));
        inputRow.addView(label); inputRow.addView(hintRow); inputRow.addView(inputFrame);

        View separator = new View(ctx);
        separator.setBackgroundColor(VoidTheme.LINE);

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(VoidTheme.BG);
        root.addView(list, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(separator, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 1)));
        root.addView(inputRow);

        return new QuickSearchLayout(root, input, list, label, hintRow, hintText, cursor);
    }

    private static EditText buildInput(Context ctx) {
        EditText et = new EditText(ctx);
        et.setTextColor(VoidTheme.FG);
        et.setTextSize(18f);
        et.setTypeface(Typeface.MONOSPACE);
        et.setBackgroundColor(0);
        et.setSingleLine(true);
        et.setHint("");
        et.setHintTextColor(VoidTheme.FG5);
        et.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        et.setPadding(0, 0, 0, 0);
        return et;
    }

    private static ListView buildList(Context ctx) {
        ListView lv = new ListView(ctx);
        lv.setBackgroundColor(VoidTheme.BG);
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

    static void showKeyboard(Context ctx, EditText et) {
        et.requestFocus();
        et.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }
}
