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
import com.voidlauncher.data.ThemeRepository;

class SettingsConfigPanel {
    private static final String PREFS = "void_config";
    private final Context            ctx;
    private final SharedPreferences  prefs;

    SettingsConfigPanel(Context ctx) {
        this.ctx   = ctx;
        this.prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    View build(Runnable onPatternChanged, Runnable onThemeChanged) {
        LinearLayout content = new LinearLayout(ctx);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), 0, dp(20), dp(20));

        content.addView(section("fondo"));
        content.addView(WallpaperSelector.build(ctx, onPatternChanged));

        content.addView(section("apariencia"));
        content.addView(themeRow(onThemeChanged));
        content.addView(toggleRow("nombre real en .all",   "show_real_name", true));

        content.addView(section("comportamiento"));
        content.addView(toggleRow("abrir si 1 resultado",  "auto_launch",    true));
        content.addView(toggleRow("aprendizaje horario",   "contextual",     true));
        content.addView(toggleRow("vibración al abrir",    "vibration",      false));

        content.addView(section("privacidad"));
        content.addView(actionRow("borrar historial horario", this::clearHistory));
        content.addView(actionRow("exportar alias",           () -> toast("próximamente")));
        content.addView(actionRow("importar alias",           () -> toast("próximamente")));

        ScrollView sv = new ScrollView(ctx);
        sv.addView(content); return sv;
    }

    private View themeRow(Runnable onThemeChanged) {
        ThemeRepository repo = new ThemeRepository(ctx);
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(13), 0, dp(13));
        TextView tvLabel = new TextView(ctx);
        tvLabel.setText("tema"); tvLabel.setTextColor(VoidTheme.FG3);
        tvLabel.setTextSize(VoidTheme.TEXT_MD); tvLabel.setTypeface(Typeface.MONOSPACE);
        TextView tvVal = new TextView(ctx);
        tvVal.setTypeface(Typeface.MONOSPACE); tvVal.setTextSize(VoidTheme.TEXT_SM);
        tvVal.setTextColor(VoidTheme.FG);
        tvVal.setText("[ " + ThemeRepository.LABELS[repo.getMode()] + " ]");
        tvVal.setOnClickListener(v -> {
            int next = (repo.getMode() + 1) % 3;
            repo.setMode(next);
            tvVal.setText("[ " + ThemeRepository.LABELS[next] + " ]");
            VoidTheme.apply(next);
            if (onThemeChanged != null) onThemeChanged.run();
        });
        row.addView(tvLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
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

    private View toggleRow(String label, String key, boolean def) {
        boolean on = prefs.getBoolean(key, def);
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(13), 0, dp(13));
        TextView tvLabel = new TextView(ctx);
        tvLabel.setText(label); tvLabel.setTextColor(VoidTheme.FG3);
        tvLabel.setTextSize(VoidTheme.TEXT_MD); tvLabel.setTypeface(Typeface.MONOSPACE);
        TextView tvToggle = toggle(on);
        tvToggle.setOnClickListener(v -> {
            boolean cur = prefs.getBoolean(key, def);
            prefs.edit().putBoolean(key, !cur).apply();
            styleToggle(tvToggle, !cur);
        });
        row.addView(tvLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvToggle);
        return withDivider(row);
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
        View d = new View(ctx); d.setBackgroundColor(VoidTheme.BG_CARD);
        d.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        wrap.addView(d); return wrap;
    }

    private void clearHistory() {
        ctx.getSharedPreferences("void_contextual", Context.MODE_PRIVATE).edit().clear().apply();
        toast("historial borrado");
    }

    private void toast(String msg) { Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show(); }

    private int dp(int v) { return QuickSearchLayout.dp(ctx, v); }
}
