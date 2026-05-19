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
import com.voidlauncher.data.ContextualApps;
import com.voidlauncher.data.HiddenAppsRepository;
import com.voidlauncher.data.WallpaperRepository;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LauncherActivity extends Activity implements GestureView.Listener {

    private String[] appNames, appPackages;
    private ContextualApps contextual; private AliasRepository aliases;
    private HiddenAppsRepository hidden; private View launchBar;
    private PatternView patternView;
    private TextView tvClock, tvDate;
    private final Handler clockHandler = new Handler();
    private SimpleDateFormat timeFmt, dateFmt;

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            tvClock.setText(timeFmt.format(new Date()));
            tvDate.setText(dateFmt.format(new Date()).toUpperCase(Locale.getDefault()));
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        contextual = new ContextualApps(this); aliases = new AliasRepository(this); hidden = new HiddenAppsRepository(this);
        String timePattern = DateFormat.is24HourFormat(this) ? "HH:mm" : "hh:mm";
        timeFmt = new SimpleDateFormat(timePattern, Locale.getDefault());
        dateFmt = new SimpleDateFormat("EEE dd MMM", new Locale("es"));

        FrameLayout root = new FrameLayout(this); root.setBackgroundColor(VoidTheme.BG);
        patternView = new PatternView(this, new WallpaperRepository(this)); root.addView(patternView);
        GestureView gv = new GestureView(this); gv.setListener(this); root.addView(gv);

        TextView[] clockRef = new TextView[1], dateRef = new TextView[1];
        root.addView(ClockView.build(this, clockRef, dateRef));
        tvClock = clockRef[0]; tvDate = dateRef[0];

        launchBar = LaunchBar.attach(root);
        setContentView(root);
        loadInstalledApps();

        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_PACKAGE_ADDED); f.addAction(Intent.ACTION_PACKAGE_REMOVED);
        f.addDataScheme("package"); registerReceiver(packageReceiver, f);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    public void onAppLaunched(String pkg) {
        contextual.record(pkg);
        LaunchBar.show(launchBar);
    }

    public void refreshPattern() { patternView.refresh(); }

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
