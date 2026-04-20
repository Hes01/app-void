package com.voidlauncher.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.voidlauncher.data.AliasRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsDialog {

    private final LauncherActivity launcher;
    private final AliasRepository  aliases;
    private final Dialog           previous;
    private final List<String>     appNames    = new ArrayList<>();
    private final List<String>     appPackages = new ArrayList<>();
    private ArrayAdapter<String>   adapter;
    private ListView               list;
    private Dialog                 dialog;

    public SettingsDialog(LauncherActivity launcher, AliasRepository aliases, Dialog previous) {
        this.launcher = launcher;
        this.aliases  = aliases;
        this.previous = previous;
    }

    public void show() {
        loadApps();
        dialog = new Dialog(launcher, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(buildLayout());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
        }
        dialog.show();
        if (previous != null) previous.dismiss();
    }

    private LinearLayout buildLayout() {
        LinearLayout root = new LinearLayout(launcher);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(48, 72, 48, 0);

        TextView title = new TextView(launcher);
        title.setText(". void");
        title.setTextColor(0x55FFFFFF);
        title.setTextSize(14f);
        title.setTypeface(Typeface.MONOSPACE);
        title.setPadding(0, 0, 0, 40);
        root.addView(title);
        root.addView(buildList());
        return root;
    }

    private ListView buildList() {
        adapter = new ArrayAdapter<String>(launcher, 0, appNames) {
            @Override public View getView(int pos, View cv, ViewGroup parent) {
                LinearLayout row = new LinearLayout(launcher);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, dp(11), 0, dp(11));
                String alias = aliases.aliasOf(appPackages.get(pos));
                row.addView(mono(alias != null ? alias : "", alias != null ? Color.WHITE : 0x33FFFFFF, dp(72)));
                row.addView(mono(appNames.get(pos), alias != null ? 0xFFFFFFFF : 0x99FFFFFF, 0));
                return row;
            }
        };
        list = new ListView(launcher);
        list.setBackgroundColor(Color.BLACK);
        list.setDivider(null);
        list.setSelector(android.R.color.transparent);
        list.setOverScrollMode(View.OVER_SCROLL_NEVER);
        list.setAdapter(adapter);
        list.setOnItemClickListener((p, v, pos, id) -> showEditDialog(pos));
        return list;
    }

    private void showEditDialog(int pos) {
        String pkg = appPackages.get(pos);
        String current = aliases.aliasOf(pkg);
        EditText input = new EditText(launcher);
        input.setTypeface(Typeface.MONOSPACE);
        input.setText(current != null ? current : "");
        input.setHint("alias  (vacío = quitar)");
        input.setGravity(Gravity.START);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(0x44FFFFFF);
        input.setPadding(dp(24), dp(24), dp(24), dp(24));
        new AlertDialog.Builder(launcher)
            .setTitle(appNames.get(pos))
            .setView(input)
            .setPositiveButton("ok", (d, w) -> {
                String val = input.getText().toString().trim();
                if (current != null) aliases.remove(current);
                if (!val.isEmpty()) aliases.set(val, pkg);
                list.invalidateViews();
            })
            .setNeutralButton("desinstalar", (d, w) -> {
                dialog.dismiss();
                launcher.startActivity(new Intent(Intent.ACTION_DELETE,
                        Uri.parse("package:" + pkg)));
            })
            .setNegativeButton("cancelar", null)
            .show();
    }

    private void loadApps() {
        appNames.clear();
        appPackages.clear();
        PackageManager pm = launcher.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        Collections.sort(infos, (a, b) ->
                a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));
        for (ResolveInfo r : infos) {
            appNames.add(r.loadLabel(pm).toString());
            appPackages.add(r.activityInfo.packageName);
        }
        aliases.cleanOrphans(appPackages);
    }

    private TextView mono(String text, int color, int fixedPx) {
        TextView tv = new TextView(launcher);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(14f);
        tv.setTypeface(Typeface.MONOSPACE);
        if (fixedPx > 0) tv.setWidth(fixedPx);
        return tv;
    }

    private int dp(int dp) {
        return QuickSearchLayout.dp(launcher, dp);
    }
}
