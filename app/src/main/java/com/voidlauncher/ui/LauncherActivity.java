package com.voidlauncher.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.voidlauncher.data.ContextualApps;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LauncherActivity extends Activity implements GestureView.Listener {

    private String[]          appNames;
    private String[]          appPackages;
    private ContextualApps    contextual;

    private TextView          tvClock;
    private final Handler     clockHandler = new Handler();
    private SimpleDateFormat  timeFmt;

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            tvClock.setText(timeFmt.format(new Date()));
            clockHandler.postDelayed(this, 1000); // Actualizar cada segundo para mayor precisión si se desea, o cada 60s
        }
    };

    private final BroadcastReceiver packageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) { loadInstalledApps(); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        contextual = new ContextualApps(this);
        String timePattern = DateFormat.is24HourFormat(this) ? "HH:mm" : "h:mm";
        timeFmt = new SimpleDateFormat(timePattern, Locale.getDefault());

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        GestureView gestureView = new GestureView(this);
        gestureView.setListener(this);
        root.addView(gestureView);
        root.addView(buildTopInfo());
        setContentView(root);
        loadInstalledApps();

        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        pkgFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        pkgFilter.addDataScheme("package");
        registerReceiver(packageReceiver, pkgFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        clockHandler.post(clockTick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clockHandler.removeCallbacks(clockTick);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(packageReceiver); } catch (Exception ignored) {}
    }

    @Override
    public void onTap() {
        new QuickSearchDialog(this, appNames, appPackages, contextual).show();
    }

    public void onAppLaunched(String pkg) {
        contextual.record(pkg);
    }

    private View buildTopInfo() {
        tvClock = new TextView(this);
        tvClock.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        tvClock.setTextSize(64f);
        tvClock.setTextColor(Color.WHITE);
        tvClock.setAlpha(0.8f); // Un poco de transparencia para suavizar el contraste
        tvClock.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER); // Centrado total para mayor minimalismo
        tvClock.setLayoutParams(lp);
        return tvClock;
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        Collections.sort(infos, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo b) {
                return a.loadLabel(getPackageManager()).toString()
                        .compareToIgnoreCase(b.loadLabel(getPackageManager()).toString());
            }
        });
        appNames    = new String[infos.size()];
        appPackages = new String[infos.size()];
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
