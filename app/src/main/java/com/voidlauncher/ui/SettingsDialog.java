package com.voidlauncher.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
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
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF0A0A0A));
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
        root.setBackgroundColor(0xFF0A0A0A);
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
        list.setBackgroundColor(0xFF0A0A0A);
        list.setDivider(null);
        list.setSelector(android.R.color.transparent);
        list.setOverScrollMode(View.OVER_SCROLL_NEVER);
        list.setAdapter(adapter);
        list.setOnItemClickListener((p, v, pos, id) -> showEditDialog(pos));
        return list;
    }

    private void showEditDialog(int pos) {
        String pkg     = appPackages.get(pos);
        String current = aliases.aliasOf(pkg);

        EditText input = new EditText(launcher);
        input.setTypeface(Typeface.MONOSPACE);
        input.setText(current != null ? current : "");
        input.setHint("alias");
        input.setHintTextColor(0x28FFFFFF);
        input.setTextColor(Color.WHITE);
        input.setTextSize(16f);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setPadding(0, dp(14), 0, dp(14));

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xFF0A0A0A);
        cardBg.setStroke(1, 0x18FFFFFF);
        cardBg.setCornerRadius(dp(8));

        LinearLayout card = new LinearLayout(launcher);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(cardBg);
        card.setPadding(dp(20), dp(20), dp(20), dp(16));

        TextView appLabel = new TextView(launcher);
        appLabel.setText(appNames.get(pos));
        appLabel.setTextColor(0x55FFFFFF);
        appLabel.setTextSize(14f);
        appLabel.setTypeface(Typeface.MONOSPACE);
        appLabel.setLetterSpacing(0.02f);
        appLabel.setPadding(0, 0, 0, dp(16));
        card.addView(appLabel);
        card.addView(divider());
        card.addView(input);
        card.addView(divider());

        Dialog d = new Dialog(launcher);

        LinearLayout actions = new LinearLayout(launcher);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView btnCancel = actionBtn("cancelar", 0x40FFFFFF);
        TextView btnOk     = actionBtn("ok", 0xCCFFFFFF);
        btnCancel.setOnClickListener(v -> d.dismiss());
        btnOk.setOnClickListener(v -> {
            String val = input.getText().toString().trim();
            if (current != null) aliases.remove(current);
            if (!val.isEmpty()) aliases.set(val, pkg);
            list.invalidateViews();
            d.dismiss();
        });
        actions.addView(btnCancel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        actions.addView(btnOk,     new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(actions);

        TextView btnUninstall = actionBtn("desinstalar", 0x44CC4444);
        btnUninstall.setOnClickListener(v -> {
            d.dismiss(); dialog.dismiss();
            launcher.startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + pkg)));
        });
        card.addView(btnUninstall);

        LinearLayout wrapper = new LinearLayout(launcher);
        wrapper.setPadding(dp(80), 0, dp(80), 0);
        wrapper.addView(card);

        d.setContentView(wrapper);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        d.show();
    }

    private void loadApps() {
        appNames.clear(); appPackages.clear();
        PackageManager pm = launcher.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        Collections.sort(infos, (a, b) -> {
            boolean ha = aliases.aliasOf(a.activityInfo.packageName) != null;
            boolean hb = aliases.aliasOf(b.activityInfo.packageName) != null;
            if (ha != hb) return ha ? -1 : 1;
            return a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());
        });
        for (ResolveInfo r : infos) {
            appNames.add(r.loadLabel(pm).toString());
            appPackages.add(r.activityInfo.packageName);
        }
        aliases.cleanOrphans(appPackages);
    }

    private View divider() {
        View v = new View(launcher);
        v.setBackgroundColor(0x12FFFFFF);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        return v;
    }

    private TextView actionBtn(String text, int color) {
        TextView tv = new TextView(launcher);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(12f);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dp(16), 0, dp(12));
        return tv;
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

    private int dp(int dp) { return QuickSearchLayout.dp(launcher, dp); }
}
