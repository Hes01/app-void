package com.voidlauncher.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PointF;
import android.os.Bundle;
import android.widget.TextView;
import com.voidlauncher.R;
import com.voidlauncher.core.GestureEngine;
import com.voidlauncher.data.GestureMapping;
import com.voidlauncher.data.GestureRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecordGestureActivity extends Activity implements GestureView.Listener {

    private static final int TOTAL_RECORDINGS = 3;

    private GestureEngine      engine;
    private GestureRepository  repo;
    private String             selectedPackage;
    private String             selectedAppName;
    private final List<int[]>  recordings = new ArrayList<>();
    private TextView           tvPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        engine   = new GestureEngine();
        repo     = new GestureRepository(this);
        tvPrompt = findViewById(R.id.tvPrompt);

        GestureView gestureView = findViewById(R.id.gestureView);
        gestureView.setListener(this);

        pickApp();
    }

    @Override
    public void onHold(int fingers) { /* no aplica en grabación */ }

    @Override
    public void onStroke(List<PointF> points, int maxPointers) {
        if (selectedPackage == null) return;

        int[] sig = engine.extractSignature(points);
        if (sig.length == 0) return;

        recordings.add(sig);
        int done = recordings.size();

        if (done < TOTAL_RECORDINGS) {
            tvPrompt.setText(getString(R.string.record_progress,
                    done, TOTAL_RECORDINGS, getString(R.string.draw_again)));
        } else {
            saveAndFinish();
        }
    }

    private void pickApp() {
        final PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);

        Collections.sort(infos, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo b) {
                return a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());
            }
        });

        final String[] names = new String[infos.size()];
        final String[] pkgs  = new String[infos.size()];
        for (int i = 0; i < infos.size(); i++) {
            names[i] = infos.get(i).loadLabel(pm).toString();
            pkgs[i]  = infos.get(i).activityInfo.packageName;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.pick_app)
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        selectedPackage = pkgs[which];
                        selectedAppName = names[which];
                        tvPrompt.setText(getString(R.string.record_progress,
                                1, TOTAL_RECORDINGS, getString(R.string.draw_prompt)));
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override public void onCancel(DialogInterface d) { finish(); }
                })
                .show();
    }

    private void saveAndFinish() {
        int[][] sigs = recordings.toArray(new int[0][]);
        repo.save(new GestureMapping(
                GestureRepository.newId(), selectedPackage, selectedAppName, sigs));
        setResult(RESULT_OK);
        finish();
    }
}
