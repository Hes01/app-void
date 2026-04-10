package com.voidlauncher.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.voidlauncher.core.AppLauncher;
import com.voidlauncher.core.GestureEngine;
import com.voidlauncher.data.GestureMapping;
import com.voidlauncher.data.GestureRepository;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LauncherActivity extends Activity implements GestureView.Listener {

    private static final int FINGERS_SETTINGS = 4;

    private GestureEngine        engine;
    private GestureRepository    repo;
    private String[]             appNames;
    private String[]             appPackages;

    private TextView             tvClock;
    private TextView             tvDate;
    private TextView             tvBattery;
    private final Handler        clockHandler = new Handler();
    private SimpleDateFormat     timeFmt;
    private SimpleDateFormat     dateFmt;

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            Date now = new Date();
            tvClock.setText(timeFmt.format(now));
            tvDate.setText(dateFmt.format(now));
            clockHandler.postDelayed(this, 30_000);
        }
    };

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            int level  = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale  = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                            || status == BatteryManager.BATTERY_STATUS_FULL;
            if (charging && level >= 0 && scale > 0) {
                tvBattery.setText(Math.round(level * 100f / scale) + "%");
                tvBattery.setVisibility(View.VISIBLE);
            } else {
                tvBattery.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        engine  = new GestureEngine();
        repo    = new GestureRepository(this);
        String timePattern = DateFormat.is24HourFormat(this) ? "HH:mm" : "h:mm";
        timeFmt = new SimpleDateFormat(timePattern, Locale.getDefault());
        dateFmt = new SimpleDateFormat("EEE d MMM yyyy", Locale.getDefault());

        FrameLayout root = new FrameLayout(this);
        GestureView gestureView = new GestureView(this);
        gestureView.setListener(this);
        root.addView(gestureView);
        root.addView(buildTopInfo());
        setContentView(root);
        loadInstalledApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        clockHandler.post(clockTick);
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        clockHandler.removeCallbacks(clockTick);
        try { unregisterReceiver(batteryReceiver); } catch (Exception ignored) {}
    }

    @Override
    public void onStroke(List<PointF> points, int maxPointers) {
        int[] drawn = engine.extractSignature(points);
        for (GestureMapping m : repo.getAll()) {
            if (engine.matches(m.signatures, drawn)) {
                AppLauncher.launch(this, m.appPackage);
                return;
            }
        }
    }

    @Override
    public void onHold(int fingers) {
        if (fingers >= FINGERS_SETTINGS) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else {
            new QuickSearchDialog(this, appNames, appPackages).show();
        }
    }

    // Reloj grande + fecha + batería (solo al cargar), todos arriba centrados
    private LinearLayout buildTopInfo() {
        tvClock = new TextView(this);
        tvClock.setTypeface(Typeface.MONOSPACE);
        tvClock.setTextSize(58f);
        tvClock.setTextColor(Color.WHITE);
        tvClock.setGravity(Gravity.CENTER);

        tvDate = new TextView(this);
        tvDate.setTypeface(Typeface.MONOSPACE);
        tvDate.setTextSize(13f);
        tvDate.setTextColor(Color.argb(160, 255, 255, 255));
        tvDate.setGravity(Gravity.CENTER);

        tvBattery = new TextView(this);
        tvBattery.setTypeface(Typeface.MONOSPACE);
        tvBattery.setTextSize(13f);
        tvBattery.setTextColor(Color.argb(160, 255, 255, 255));
        tvBattery.setGravity(Gravity.CENTER);
        tvBattery.setVisibility(View.GONE);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(tvClock);
        ll.addView(tvDate);
        ll.addView(tvBattery);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        lp.topMargin = 52;
        ll.setLayoutParams(lp);
        return ll;
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
