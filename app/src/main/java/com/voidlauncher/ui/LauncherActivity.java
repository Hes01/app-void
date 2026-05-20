package com.voidlauncher.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.voidlauncher.core.PluginRegistry;
import com.voidlauncher.data.AliasRepository;
import com.voidlauncher.data.LaunchRepository;
import com.voidlauncher.data.HiddenAppsRepository;
import com.voidlauncher.data.ThemeRepository;
import com.voidlauncher.data.WallpaperRepository;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LauncherActivity extends Activity implements GestureView.Listener {

    private String[] appNames, appPackages;
    private LaunchRepository contextual; private AliasRepository aliases;
    private HiddenAppsRepository hidden; private View launchBar; private PatternView patternView;
    private FrameLayout root; private View clockView; private SegmentClockView segClock;
    private FlipClockView flipClock;
    private TextView tvClock, tvDate;
    private final Handler clockHandler = new Handler();
    private SimpleDateFormat timeFmt, dateFmt;

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            Date now = new Date();
            if (tvClock != null) tvClock.setText(timeFmt.format(now));
            if (segClock != null) segClock.invalidate();
            if (flipClock != null) flipClock.tick();
            tvDate.setText(dateFmt.format(now).toUpperCase(Locale.getDefault()));
            clockHandler.postDelayed(this, 1000);
        }
    };

    private final BroadcastReceiver packageReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context ctx, Intent intent) {
            String pkg = intent.getData() != null ? intent.getData().getSchemeSpecificPart() : null;
            if (pkg == null) return;
            if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) PluginRegistry.onInstalled(ctx, pkg);
            else PluginRegistry.onRemoved(ctx, pkg, aliases);
            loadInstalledApps();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VoidTheme.apply(new ThemeRepository(this).getMode());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        contextual = new LaunchRepository(this); aliases = new AliasRepository(this); hidden = new HiddenAppsRepository(this);
        timeFmt = new SimpleDateFormat(DateFormat.is24HourFormat(this) ? "HH:mm" : "hh:mm", Locale.getDefault());
        dateFmt = new SimpleDateFormat("EEE dd MMM", Locale.getDefault());

        root = new FrameLayout(this); root.setBackgroundColor(VoidTheme.BG);
        patternView = new PatternView(this, new WallpaperRepository(this)); root.addView(patternView);
        GestureView gv = new GestureView(this); gv.setListener(this); root.addView(gv);

        TextView[] dateRef = new TextView[1];
        int cm = getSharedPreferences("void_config", MODE_PRIVATE).getInt("clock_mode", 1);
        if (cm == 2) { SegmentClockView[] sr = {null}; clockView = ClockView.buildSegment(this, sr, dateRef); segClock = sr[0]; }
        else if (cm == 3) { FlipClockView[] fr = {null}; clockView = ClockView.buildFlip(this, fr, dateRef); flipClock = fr[0]; }
        else { TextView[] cr = {null}; clockView = ClockView.build(this, cr, dateRef); tvClock = cr[0]; }
        if (cm == 0) clockView.setVisibility(View.GONE);
        root.addView(clockView); tvDate = dateRef[0];

        launchBar = LaunchBar.attach(root);
        setContentView(root);
        loadInstalledApps();

        IntentFilter f = new IntentFilter(); f.addAction(Intent.ACTION_PACKAGE_ADDED); f.addAction(Intent.ACTION_PACKAGE_REMOVED); f.addDataScheme("package"); registerReceiver(packageReceiver, f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemeRepository tr = new ThemeRepository(this);
        if (tr.getMode() == ThemeRepository.AUTO && VoidTheme.isDaytime() != VoidTheme.isDay) {
            VoidTheme.apply(ThemeRepository.AUTO); recreate(); return;
        }
        hideSystemUI();
        clockHandler.post(clockTick);
        verifyPlugins();
    }

    @Override protected void onPause()   { super.onPause();   clockHandler.removeCallbacks(clockTick); }
    @Override protected void onDestroy() { super.onDestroy(); try { unregisterReceiver(packageReceiver); } catch (Exception ignored) {} }
    @Override public void onBackPressed() {}

    @Override
    public void onTap() {
        new QuickSearchDialog(this, appNames, appPackages, contextual, aliases, hidden).show();
    }

    public void onAppLaunched(String pkg, boolean record) {
        if (record) contextual.record(pkg);
        LaunchBar.show(launchBar, this, pkg);
    }

    public void applyUiChanges() {
        int cm = getSharedPreferences("void_config", MODE_PRIVATE).getInt("clock_mode", 1);
        boolean wantSeg = cm == 2, hasSeg = segClock != null;
        boolean wantFlip = cm == 3, hasFlip = flipClock != null;
        boolean wasHidden = clockView.getVisibility() == View.GONE;
        if (wantSeg != hasSeg || wantFlip != hasFlip || (cm == 0) != wasHidden) {
            clockHandler.removeCallbacks(clockTick);
            int idx = root.indexOfChild(clockView); root.removeView(clockView);
            segClock = null; flipClock = null; tvClock = null;
            TextView[] dateRef = new TextView[1];
            if (cm == 2) { SegmentClockView[] sr={null}; clockView=ClockView.buildSegment(this,sr,dateRef); segClock=sr[0]; }
            else if (cm == 3) { FlipClockView[] fr={null}; clockView=ClockView.buildFlip(this,fr,dateRef); flipClock=fr[0]; }
            else { TextView[] cr={null}; clockView=ClockView.build(this,cr,dateRef); tvClock=cr[0]; }
            if (cm == 0) clockView.setVisibility(View.GONE);
            tvDate = dateRef[0]; root.addView(clockView, idx);
            clockHandler.post(clockTick);
        }
        root.setBackgroundColor(VoidTheme.BG);
        if (tvClock != null) tvClock.setTextColor(VoidTheme.FG);
        tvDate.setTextColor(VoidTheme.FG4);
        patternView.refresh(); clockView.invalidate();
    }

    private void verifyPlugins() {
        Intent main = new Intent(Intent.ACTION_MAIN, null); main.addCategory(Intent.CATEGORY_LAUNCHER);
        for (ResolveInfo r : getPackageManager().queryIntentActivities(main, PackageManager.GET_META_DATA)) {
            String pkg   = r.activityInfo.packageName;
            String alias = PluginRegistry.readAlias(this, pkg);
            if (alias != null && aliases.resolve(alias) == null && aliases.aliasOf(pkg) == null)
                aliases.set(alias, pkg);
        }
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null); main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        Collections.sort(infos, (a, b) -> a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));
        appNames = new String[infos.size()]; appPackages = new String[infos.size()];
        for (int i = 0; i < infos.size(); i++) {
            appNames[i]    = infos.get(i).loadLabel(pm).toString();
            appPackages[i] = infos.get(i).activityInfo.packageName;
        }
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
