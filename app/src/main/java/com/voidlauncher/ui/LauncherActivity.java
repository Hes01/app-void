package com.voidlauncher.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
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

    private GestureEngine     engine;
    private GestureRepository repo;
    private String[]          appNames;
    private String[]          appPackages;

    private TextView          tvClock;
    private final Handler     clockHandler = new Handler();
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            tvClock.setText(timeFmt.format(new Date()));
            clockHandler.postDelayed(this, 30_000); // refresca cada 30s
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        engine = new GestureEngine();
        repo   = new GestureRepository(this);

        FrameLayout root = new FrameLayout(this);

        GestureView gestureView = new GestureView(this);
        gestureView.setListener(this);
        root.addView(gestureView);

        tvClock = buildClock();
        root.addView(tvClock);

        setContentView(root);
        loadInstalledApps();
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
    public void onStroke(List<PointF> points, int maxPointers) {
        if (maxPointers >= FINGERS_SETTINGS) {
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }
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
        new QuickSearchDialog(this, appNames, appPackages).show();
    }

    private TextView buildClock() {
        TextView tv = new TextView(this);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextSize(52f);
        tv.setTextColor(Color.argb(38, 0, 255, 0)); // verde fósforo al ~15%
        tv.setLetterSpacing(0.15f);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        lp.bottomMargin = 0;
        tv.setLayoutParams(lp);
        return tv;
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
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
