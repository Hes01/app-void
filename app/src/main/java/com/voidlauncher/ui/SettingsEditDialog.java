package com.voidlauncher.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hes01.voidlauncher.R;
import com.voidlauncher.data.AliasRepository;

class SettingsEditDialog {
    private final LauncherActivity launcher;
    private final AliasRepository  aliases;
    private final String           pkg;
    private final String           appName;
    private final Runnable         onChanged;

    SettingsEditDialog(LauncherActivity launcher, AliasRepository aliases,
                       String pkg, String appName, Runnable onChanged) {
        this.launcher  = launcher; this.aliases   = aliases;
        this.pkg       = pkg;      this.appName   = appName; this.onChanged = onChanged;
    }

    void show() {
        String current = aliases.aliasOf(pkg);
        EditText input = new EditText(launcher);
        input.setTypeface(Typeface.MONOSPACE); input.setText(current != null ? current : "");
        input.setHint(launcher.getString(R.string.hint_alias)); input.setHintTextColor(VoidTheme.FG5);
        input.setTextColor(VoidTheme.FG); input.setTextSize(VoidTheme.TEXT_XL);
        input.setBackgroundColor(Color.TRANSPARENT); input.setPadding(0, dp(14), 0, dp(14));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(VoidTheme.BG); bg.setStroke(1, VoidTheme.LINE); bg.setCornerRadius(dp(8));

        LinearLayout card = new LinearLayout(launcher);
        card.setOrientation(LinearLayout.VERTICAL); card.setBackground(bg);
        card.setPadding(dp(20), dp(20), dp(20), dp(16));

        TextView label = new TextView(launcher);
        label.setText(appName); label.setTextColor(VoidTheme.FG4);
        label.setTextSize(VoidTheme.TEXT_LG); label.setTypeface(Typeface.MONOSPACE);
        label.setPadding(0, 0, 0, dp(16));
        card.addView(label); card.addView(divider()); card.addView(input); card.addView(divider());

        Dialog d = new Dialog(launcher);
        LinearLayout actions = new LinearLayout(launcher);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView cancel = btn(launcher.getString(R.string.btn_cancel), VoidTheme.FG4);
        TextView ok     = btn(launcher.getString(R.string.btn_ok),     VoidTheme.FG2);
        cancel.setOnClickListener(v -> d.dismiss());
        ok.setOnClickListener(v -> {
            String val = input.getText().toString().trim();
            if (current != null) aliases.remove(current);
            if (!val.isEmpty()) aliases.set(val, pkg);
            onChanged.run(); d.dismiss();
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        actions.addView(ok,     new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(actions);

        TextView uninstall = btn("desinstalar", 0x44CC4444);
        uninstall.setOnClickListener(v -> {
            d.dismiss();
            launcher.startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + pkg)));
        });
        card.addView(uninstall);

        LinearLayout wrapper = new LinearLayout(launcher);
        wrapper.setPadding(dp(80), 0, dp(80), 0); wrapper.addView(card);
        d.setContentView(wrapper);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        d.show();
    }

    private View divider() {
        View v = new View(launcher); v.setBackgroundColor(VoidTheme.LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        return v;
    }

    private TextView btn(String text, int color) {
        TextView tv = new TextView(launcher);
        tv.setText(text); tv.setTextColor(color); tv.setTextSize(VoidTheme.TEXT_BASE);
        tv.setTypeface(Typeface.MONOSPACE); tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dp(16), 0, dp(12)); return tv;
    }

    private int dp(int v) { return QuickSearchLayout.dp(launcher, v); }
}
