package com.voidlauncher.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.voidlauncher.R;
import com.voidlauncher.core.GestureEngine;
import com.voidlauncher.data.GestureMapping;
import com.voidlauncher.data.GestureRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecordGestureActivity extends Activity implements GestureView.Listener {

    private GestureEngine     engine;
    private GestureRepository repo;
    private String            selectedPackage;
    private String            selectedAppName;
    private int[]             lastSignature;
    private Button            btnSave;
    private TextView          tvPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        engine  = new GestureEngine();
        repo    = new GestureRepository(this);
        btnSave = findViewById(R.id.btnSave);
        tvPrompt = findViewById(R.id.tvPrompt);

        GestureView gestureView = findViewById(R.id.gestureView);
        gestureView.setListener(this);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { saveAndFinish(); }
        });

        pickApp();
    }

    @Override
    public void onStroke(List<PointF> points, int maxPointers) {
        if (selectedPackage == null) return;
        lastSignature = engine.extractSignature(points);
        if (lastSignature.length > 0) {
            btnSave.setVisibility(View.VISIBLE);
            tvPrompt.setText("Gesto listo. Guarda o dibuja de nuevo.");
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
                        tvPrompt.setText(getString(R.string.draw_prompt));
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override public void onCancel(DialogInterface d) { finish(); }
                })
                .show();
    }

    private void saveAndFinish() {
        if (selectedPackage == null || lastSignature == null || lastSignature.length == 0) return;
        repo.save(new GestureMapping(
                GestureRepository.newId(), selectedPackage, selectedAppName, lastSignature));
        setResult(RESULT_OK);
        finish();
    }
}
