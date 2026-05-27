package com.voidlauncher.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.voidlauncher.core.AppLauncher;
import com.voidlauncher.core.CommandRouter;
import com.voidlauncher.data.AppPreferences;
import com.voidlauncher.data.AliasRepository;
import com.voidlauncher.data.HiddenAppsRepository;
import com.voidlauncher.data.LaunchRepository;
import com.voidlauncher.data.WallpaperRepository;
import android.view.ViewConfiguration;
import java.util.ArrayList;
import java.util.List;

public class QuickSearchDialog {

    private final LauncherActivity launcher;
    private final String[] names, packages;
    private final LaunchRepository contextual; private final AliasRepository aliases;
    private final HiddenAppsRepository hidden;
    private final List<String> filteredNames = new ArrayList<>(), filteredPkgs = new ArrayList<>();
    private QuickSearchAdapter adapter; private QuickSearchPlugin plugin;
    private Dialog dialog; private TextView label; private EditText searchInput;
    private QuickSearchLayout layout;
    private boolean autoLaunch, contextualOn, vibrationOn;

    private List<String> topApps = new ArrayList<>();

    public QuickSearchDialog(LauncherActivity l, String[] n, String[] p,
                             LaunchRepository c, AliasRepository a, HiddenAppsRepository h) {
        launcher=l; names=n; packages=p; contextual=c; aliases=a; hidden=h;
        android.content.SharedPreferences cfg = AppPreferences.get(l);
        autoLaunch   = cfg.getBoolean(AppPreferences.KEY_AUTO_LAUNCH, true);
        contextualOn = cfg.getBoolean(AppPreferences.KEY_CONTEXTUAL,  true);
        vibrationOn  = cfg.getBoolean(AppPreferences.KEY_VIBRATION,   false);
    }

    public void show() {
        if (contextualOn) contextual.getTop(top -> {
            topApps = top;
            if (dialog != null && dialog.isShowing() && searchInput != null && searchInput.getText().toString().isEmpty()) filter("");
        });
        layout = QuickSearchLayout.build(launcher);
        label = layout.label; searchInput = layout.input;
        adapter = new QuickSearchAdapter(launcher, filteredNames, filteredPkgs, aliases, names, packages);
        layout.list.setAdapter(adapter);
        layout.list.setOnItemClickListener((p, v, pos, id) -> {
            String pkg = filteredPkgs.get(pos);
            if (!pkg.isEmpty()) { v.setAlpha(0.4f); v.postDelayed(() -> { v.setAlpha(1f); launch(pkg); }, 120); }
        });
        setupListBehavior(layout);
        layout.input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });
        layout.root.setBackgroundColor(0);
        layout.list.setBackgroundColor(0);
        FrameLayout frame = new FrameLayout(launcher);
        frame.setBackgroundColor(VoidTheme.BG);
        PatternView pat = new PatternView(launcher, new WallpaperRepository(launcher));
        pat.setAlpha(0.07f);
        frame.addView(pat, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        frame.addView(layout.root, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog = new Dialog(launcher, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(frame, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        plugin = new QuickSearchPlugin(launcher, aliases, dialog, filteredNames, filteredPkgs);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(VoidTheme.BG)); dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
            ImeInsets.attach(dialog.getWindow(), frame);
        }
        filter(""); dialog.show(); if (vibrationOn) VibrationFeedback.onOpen(hapticView());
        SearchHints.showIfNeeded(launcher, layout.hintRow, layout.hintText, layout.input);
        QuickSearchLayout.showKeyboard(launcher, layout.input);
    }

    private void setupListBehavior(QuickSearchLayout layout) {
        GestureDetector gd = new GestureDetector(launcher, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                int minFling = ViewConfiguration.get(launcher).getScaledMinimumFlingVelocity() * 3;
                if (vy > minFling && Math.abs(vy) > Math.abs(vx) * 1.5f && filteredNames.size() <= 8)
                    { dialog.dismiss(); return true; }
                return false;
            }
        });
        layout.list.setOnTouchListener((v, e) -> { gd.onTouchEvent(e); return false; });
        layout.list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView v, int s) { if (s != SCROLL_STATE_IDLE) hideKeyboard(); }
            @Override public void onScroll(AbsListView v, int f, int c, int t) {}
        });
    }

    private void filter(String query) {
        filteredNames.clear(); filteredPkgs.clear();
        String q = query.toLowerCase().trim();
        adapter.isTopMode = false; setInputColor(!q.isEmpty() && q.startsWith(".") ? VoidTheme.FG2 : VoidTheme.FG);
        if (q.isEmpty()) {
            adapter.activeSearch = false; adapter.isTopMode = contextualOn; setLabelColor(VoidTheme.FG4);
            if (contextualOn) for (String pkg : topApps)
                for (int i = 0; i < packages.length; i++)
                    if (packages[i].equals(pkg) && !hidden.isHidden(pkg)) { filteredNames.add(displayName(i)); filteredPkgs.add(pkg); break; }
        } else if (q.equals(".all")) {
            adapter.activeSearch = false; setLabelColor(VoidTheme.FG4);
            for (int i = names.length - 1; i >= 0; i--)
                if (!hidden.isHidden(packages[i])) { filteredNames.add(displayName(i)); filteredPkgs.add(packages[i]); }
        } else if (q.equals(".void")) {
            if (layout != null) layout.setGhost("", q);
            new SettingsDialog(launcher, aliases, hidden, dialog).show(); return;
        } else if (q.startsWith(".")) {
            setLabelColor(VoidTheme.FG4);
            if (layout != null) layout.setGhost(GhostText.compute(q), q);
            plugin.routeCommand(q.substring(1).trim(), adapter); return;
        } else if (q.matches(".*[a-z0-9].*")) {
            adapter.activeSearch = true;
            for (int i = 0; i < names.length; i++) {
                if (hidden.isHidden(packages[i])) continue;
                String alias = aliases.aliasOf(packages[i]);
                if ((alias != null && alias.contains(q)) || names[i].toLowerCase().contains(q))
                    { filteredNames.add(alias != null ? alias : names[i]); filteredPkgs.add(packages[i]); }
            }
            if (autoLaunch && filteredNames.size() == 1) { launch(filteredPkgs.get(0)); return; }
            if (filteredNames.isEmpty()) { if (vibrationOn) VibrationFeedback.onNoResults(hapticView()); setLabelColor(VoidTheme.ERROR); }
            else setLabelColor(VoidTheme.FG);
        }
        adapter.notifyDataSetChanged();
        if (layout != null) layout.setGhost(GhostText.compute(q), q);
    }

    private void launch(String pkgOrCmd) {
        if (pkgOrCmd.contains("\t")) { String[] p = pkgOrCmd.split("\t", 2); launchWithArgs(p[0], p[1]); return; }
        if (pkgOrCmd.contains(":")) {
            String[] p = pkgOrCmd.split(":", 2); dialog.dismiss(); launcher.onAppLaunched(p[0], contextualOn);
            Intent intent = launcher.getPackageManager().getLaunchIntentForPackage(p[0]);
            if (intent == null) return;
            try { intent.putExtra("void.extra.id", Integer.parseInt(p[1])); } catch (NumberFormatException ignored) {}
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            launcher.startActivity(intent); launcher.overridePendingTransition(0, 0); return;
        }
        if (vibrationOn) VibrationFeedback.onLaunch(hapticView()); dialog.dismiss();
        launcher.onAppLaunched(pkgOrCmd, contextualOn); AppLauncher.launch(launcher, pkgOrCmd);
    }

    private void launchWithArgs(String pkg, String args) {
        if (vibrationOn) VibrationFeedback.onLaunch(hapticView()); dialog.dismiss(); launcher.onAppLaunched(pkg, contextualOn);
        Intent intent = launcher.getPackageManager().getLaunchIntentForPackage(pkg);
        if (intent == null) return;
        if (args != null && !args.isEmpty()) intent.putExtra(CommandRouter.EXTRA_ARGS, args);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        launcher.startActivity(intent); launcher.overridePendingTransition(0, 0);
    }

    private void setLabelColor(int c) { if (label != null) label.setTextColor(c); }
    private void setInputColor(int c) { if (searchInput != null) searchInput.setTextColor(c); }
    private String displayName(int i) { String a = aliases.aliasOf(packages[i]); return a != null ? a : names[i]; }
    private View hapticView() { return launcher.getWindow().getDecorView(); }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) launcher.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && searchInput != null) imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
    }
}
