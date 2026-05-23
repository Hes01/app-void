package com.voidlauncher.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.hes01.voidlauncher.R;
import com.voidlauncher.data.LaunchRepository;
import com.voidlauncher.data.ThemeRepository;

class SettingsConfigPanel {
    private static final String PREFS = "void_config";
    private final Context            ctx;
    private final SharedPreferences  prefs;
    private final LaunchRepository   launchRepo;

    SettingsConfigPanel(Context ctx, LaunchRepository launchRepo) {
        this.ctx        = ctx;
        this.prefs      = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        this.launchRepo = launchRepo;
    }

    View build(Runnable onPatternChanged, Runnable onThemeChanged) {
        LinearLayout content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), 0, dp(20), dp(20));

        content.addView(section(ctx.getString(R.string.section_background)));
        content.addView(WallpaperSelector.build(ctx, onPatternChanged));

        content.addView(section(ctx.getString(R.string.section_appearance)));
        content.addView(paletteRow(onThemeChanged));
        content.addView(themeRow(onThemeChanged));
        content.addView(clockModeRow(onThemeChanged));
        content.addView(toggleRow(ctx.getString(R.string.pref_real_name),    "show_real_name", true,  null));

        content.addView(section(ctx.getString(R.string.section_behavior)));
        content.addView(toggleRow(ctx.getString(R.string.pref_auto_launch),  "auto_launch",    true,  null));
        content.addView(toggleRow(ctx.getString(R.string.pref_contextual),   "contextual",     true,  null));
        content.addView(toggleRow(ctx.getString(R.string.pref_vibration),    "vibration",      false, null));

        content.addView(section(ctx.getString(R.string.section_privacy)));
        content.addView(actionRow(ctx.getString(R.string.action_clear_history),  this::clearHistory));
        content.addView(actionRow(ctx.getString(R.string.action_export_aliases), () -> AliasTransferDialog.showExport(ctx)));
        content.addView(actionRow(ctx.getString(R.string.action_import_aliases), () -> AliasTransferDialog.showImport(ctx)));

        ScrollView sv = new ScrollView(ctx);
        sv.addView(content); return sv;
    }

    private View paletteRow(Runnable onThemeChanged) {
        ThemeRepository repo = new ThemeRepository(ctx);
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(13), 0, dp(13));
        TextView tvVal = new TextView(ctx);
        tvVal.setTypeface(Typeface.MONOSPACE); tvVal.setTextSize(VoidTheme.TEXT_SM);
        tvVal.setTextColor(VoidTheme.FG);
        Runnable[] refresh = {null};
        refresh[0] = () -> tvVal.setText("[ " + VoidTheme.NAMES[repo.getTheme()] + " ]");
        refresh[0].run();
        tvVal.setOnClickListener(v -> {
            int next = (repo.getTheme() + 1) % VoidTheme.NAMES.length;
            repo.setTheme(next);
            refresh[0].run();
            VoidTheme.apply(next, repo.getMode());
            if (onThemeChanged != null) onThemeChanged.run();
        });
        row.addView(label(ctx.getString(R.string.pref_palette)), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvVal);
        return withDivider(row);
    }

    private View themeRow(Runnable onThemeChanged) {
        ThemeRepository repo = new ThemeRepository(ctx);
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(13), 0, dp(13));
        TextView tvVal = new TextView(ctx);
        tvVal.setTypeface(Typeface.MONOSPACE); tvVal.setTextSize(VoidTheme.TEXT_SM);
        tvVal.setTextColor(VoidTheme.FG);
        tvVal.setText("[ " + ThemeRepository.label(ctx, repo.getMode()) + " ]");
        tvVal.setOnClickListener(v -> {
            int next = (repo.getMode() + 1) % 3;
            repo.setMode(next);
            tvVal.setText("[ " + ThemeRepository.label(ctx, next) + " ]");
            VoidTheme.apply(repo.getTheme(), next);
            if (onThemeChanged != null) onThemeChanged.run();
        });
        row.addView(label(ctx.getString(R.string.pref_theme)), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvVal);
        return withDivider(row);
    }

    private View section(String label) {
        TextView tv = new TextView(ctx);
        tv.setText(label.toUpperCase()); tv.setTextColor(VoidTheme.FG5);
        tv.setTextSize(VoidTheme.TEXT_XS); tv.setTypeface(Typeface.MONOSPACE);
        tv.setLetterSpacing(0.2f); tv.setPadding(0, dp(24), 0, dp(4));
        return tv;
    }

    private View clockModeRow(Runnable cb) {
        String[] L = {ctx.getString(R.string.clock_none), ctx.getString(R.string.clock_text),
                       ctx.getString(R.string.clock_seg),  ctx.getString(R.string.clock_flip)};
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(13), 0, dp(13));
        TextView tvLabel = label(ctx.getString(R.string.pref_clock)); row.addView(tvLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView tvVal = new TextView(ctx); tvVal.setTypeface(Typeface.MONOSPACE); tvVal.setTextSize(VoidTheme.TEXT_SM); tvVal.setTextColor(VoidTheme.FG);
        Runnable[] refresh = {null};
        refresh[0] = () -> tvVal.setText("[ " + L[prefs.getInt("clock_mode", 1)] + " ]");
        refresh[0].run();
        tvVal.setOnClickListener(v -> { int n = (prefs.getInt("clock_mode", 1) + 1) % L.length; prefs.edit().putInt("clock_mode", n).apply(); refresh[0].run(); if (cb != null) cb.run(); });
        row.addView(tvVal); return withDivider(row);
    }

    private View toggleRow(String label, String key, boolean def, Runnable onChange) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0, dp(13), 0, dp(13));
        TextView tvToggle = toggle(prefs.getBoolean(key, def));
        tvToggle.setOnClickListener(v -> { boolean cur = prefs.getBoolean(key, def); prefs.edit().putBoolean(key, !cur).apply(); styleToggle(tvToggle, !cur); if (onChange != null) onChange.run(); });
        row.addView(label(label), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvToggle); return withDivider(row);
    }

    private TextView label(String text) {
        TextView tv = new TextView(ctx); tv.setText(text); tv.setTextColor(VoidTheme.FG3);
        tv.setTextSize(VoidTheme.TEXT_MD); tv.setTypeface(Typeface.MONOSPACE); return tv;
    }

    private View actionRow(String label, Runnable action) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(13), 0, dp(13)); row.setOnClickListener(v -> action.run());
        TextView tvLabel = new TextView(ctx);
        tvLabel.setText(label); tvLabel.setTextColor(VoidTheme.FG3);
        tvLabel.setTextSize(VoidTheme.TEXT_MD); tvLabel.setTypeface(Typeface.MONOSPACE);
        TextView tvVal = new TextView(ctx);
        tvVal.setText("→"); tvVal.setTextColor(VoidTheme.FG5);
        tvVal.setTextSize(VoidTheme.TEXT_BASE); tvVal.setTypeface(Typeface.MONOSPACE);
        row.addView(tvLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvVal);
        return withDivider(row);
    }

    private TextView toggle(boolean on) {
        TextView tv = new TextView(ctx);
        tv.setTypeface(Typeface.MONOSPACE); tv.setTextSize(VoidTheme.TEXT_SM);
        styleToggle(tv, on); return tv;
    }

    private void styleToggle(TextView tv, boolean on) {
        tv.setText(on ? "[ on ]" : "[ -- ]");
        tv.setTextColor(on ? VoidTheme.FG : VoidTheme.FG5);
    }

    private View withDivider(View content) {
        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setOrientation(LinearLayout.VERTICAL); wrap.addView(content);
        View d = new View(ctx); d.setBackgroundColor(VoidTheme.LINE);
        d.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        wrap.addView(d); return wrap;
    }

    private void clearHistory() {
        launchRepo.clearAll();
        toast(ctx.getString(R.string.toast_history_cleared));
    }

    private void toast(String msg) { Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show(); }

    private int dp(int v) { return QuickSearchLayout.dp(ctx, v); }
}
