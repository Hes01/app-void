package com.voidlauncher.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hes01.voidlauncher.R;
import com.voidlauncher.data.AliasRepository;
import com.voidlauncher.data.HiddenAppsRepository;
import com.voidlauncher.data.LaunchRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsDialog {
    private final LauncherActivity     launcher;
    private final AliasRepository      aliases;
    private final HiddenAppsRepository hidden;
    private final Dialog               previous;
    private final List<String>         appNames = new ArrayList<>();
    private final List<String>         appPkgs  = new ArrayList<>();

    public SettingsDialog(LauncherActivity launcher, AliasRepository aliases,
                          HiddenAppsRepository hidden, Dialog previous) {
        this.launcher = launcher; this.aliases = aliases;
        this.hidden   = hidden;   this.previous = previous;
    }

    public void show() {
        loadApps();
        Dialog dialog = new Dialog(launcher, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(buildRoot());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(VoidTheme.BG));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
        if (previous != null) previous.dismiss();
    }

    private LinearLayout buildRoot() {
        TextView header = new TextView(launcher);
        header.setText(". void"); header.setTextColor(VoidTheme.FG5);
        header.setTextSize(VoidTheme.TEXT_MD); header.setTypeface(Typeface.MONOSPACE);
        header.setLetterSpacing(0.1f); header.setPadding(dp(20), dp(28), dp(20), dp(16));

        View appsPanel = new SettingsAppsPanel(launcher, appNames, appPkgs, aliases, hidden).build();
        FrameLayout configSlot = new FrameLayout(launcher);
        configSlot.setVisibility(View.GONE);

        FrameLayout panels = new FrameLayout(launcher);
        panels.addView(appsPanel,   new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        panels.addView(configSlot,  new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout root = new LinearLayout(launcher);
        root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(VoidTheme.BG);
        root.addView(header);
        root.addView(buildTabs(appsPanel, configSlot));
        root.addView(separator());
        root.addView(panels, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        return root;
    }

    private LinearLayout buildTabs(View appsPanel, FrameLayout configSlot) {
        TextView tApps = tab(launcher.getString(R.string.tab_apps),   true);
        TextView tConf = tab(launcher.getString(R.string.tab_config), false);
        tApps.setOnClickListener(v -> {
            appsPanel.setVisibility(View.VISIBLE); configSlot.setVisibility(View.GONE);
            tApps.setTextColor(VoidTheme.FG); tConf.setTextColor(VoidTheme.FG5);
        });
        tConf.setOnClickListener(v -> {
            if (configSlot.getChildCount() == 0) {
                View cp = new SettingsConfigPanel(launcher, new LaunchRepository(launcher)).build(launcher::applyUiChanges, launcher::applyUiChanges);
                configSlot.addView(cp, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            }
            appsPanel.setVisibility(View.GONE); configSlot.setVisibility(View.VISIBLE);
            tApps.setTextColor(VoidTheme.FG5); tConf.setTextColor(VoidTheme.FG);
        });
        LinearLayout tabs = new LinearLayout(launcher);
        tabs.setOrientation(LinearLayout.HORIZONTAL); tabs.setPadding(dp(20), 0, dp(20), 0);
        tabs.addView(tApps,  new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tabs.addView(tConf,  new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return tabs;
    }

    private TextView tab(String text, boolean active) {
        TextView tv = new TextView(launcher);
        tv.setText(text.toUpperCase()); tv.setTextColor(active ? VoidTheme.FG : VoidTheme.FG5);
        tv.setTextSize(VoidTheme.TEXT_SM); tv.setTypeface(Typeface.MONOSPACE);
        tv.setLetterSpacing(0.2f); tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dp(10), 0, dp(10)); tv.setBackgroundColor(0); return tv;
    }

    private View separator() {
        View v = new View(launcher); v.setBackgroundColor(VoidTheme.LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        return v;
    }

    private void loadApps() {
        appNames.clear(); appPkgs.clear();
        PackageManager pm = launcher.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        Collections.sort(infos, (a, b) -> {
            boolean ha = aliases.aliasOf(a.activityInfo.packageName) != null;
            boolean hb = aliases.aliasOf(b.activityInfo.packageName) != null;
            if (ha != hb) return ha ? -1 : 1;
            return a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());
        });
        for (ResolveInfo r : infos) {
            appNames.add(r.loadLabel(pm).toString()); appPkgs.add(r.activityInfo.packageName);
        }
        aliases.cleanOrphans(appPkgs);
    }

    private int dp(int v) { return QuickSearchLayout.dp(launcher, v); }
}
