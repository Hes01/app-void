package com.voidlauncher.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import com.voidlauncher.core.AppLauncher;
import com.voidlauncher.core.GestureEngine;
import com.voidlauncher.data.GestureMapping;
import com.voidlauncher.data.GestureRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LauncherActivity extends Activity implements GestureView.Listener {

    private static final int FINGERS_SETTINGS = 4;

    private GestureEngine     engine;
    private GestureRepository repo;
    private String[]          appNames;
    private String[]          appPackages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        engine = new GestureEngine();
        repo   = new GestureRepository(this);

        GestureView view = new GestureView(this);
        view.setListener(this);
        setContentView(view);

        loadInstalledApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    public void onStroke(List<PointF> points, int maxPointers) {
        if (maxPointers >= FINGERS_SETTINGS) {
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }
        int[] drawn = engine.extractSignature(points);
        for (GestureMapping m : repo.getAll()) {
            if (engine.matches(m.signature, drawn)) {
                AppLauncher.launch(this, m.appPackage);
                return;
            }
        }
    }

    @Override
    public void onHold(int fingers) {
        new QuickSearchDialog(this, appNames, appPackages).show();
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
