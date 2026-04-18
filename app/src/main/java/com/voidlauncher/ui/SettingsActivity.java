package com.voidlauncher.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.voidlauncher.data.AliasRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsActivity extends Activity {

    private AliasRepository   aliases;
    private List<String>      appNames    = new ArrayList<>();
    private List<String>      appPackages = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aliases = new AliasRepository(this);
        loadApps();
        setContentView(buildLayout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private LinearLayout buildLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(48, 72, 48, 0);

        TextView title = new TextView(this);
        title.setText("/ void");
        title.setTextColor(0x55FFFFFF);
        title.setTextSize(14f);
        title.setTypeface(Typeface.MONOSPACE);
        title.setPadding(0, 0, 0, 40);

        root.addView(title);
        root.addView(buildList());
        return root;
    }

    private ListView buildList() {
        adapter = new ArrayAdapter<String>(this, 0, appNames) {
            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                LinearLayout row = new LinearLayout(getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 28, 0, 28);

                String alias = aliases.aliasOf(appPackages.get(pos));
                String aliasText = alias != null ? alias : "  ";

                TextView tvAlias = new TextView(getContext());
                tvAlias.setText(aliasText);
                tvAlias.setTextColor(alias != null ? Color.WHITE : 0x22FFFFFF);
                tvAlias.setTextSize(14f);
                tvAlias.setTypeface(Typeface.MONOSPACE);
                tvAlias.setWidth(dpToPx(64));

                TextView tvName = new TextView(getContext());
                tvName.setText(appNames.get(pos));
                tvName.setTextColor(0x88FFFFFF);
                tvName.setTextSize(14f);
                tvName.setTypeface(Typeface.MONOSPACE);

                row.addView(tvAlias);
                row.addView(tvName);
                return row;
            }
        };

        ListView list = new ListView(this);
        list.setBackgroundColor(Color.BLACK);
        list.setDivider(null);
        list.setDividerHeight(0);
        list.setSelector(android.R.color.transparent);
        list.setOverScrollMode(View.OVER_SCROLL_NEVER);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                showEditDialog(pos);
            }
        });
        return list;
    }

    private void showEditDialog(int pos) {
        String pkg     = appPackages.get(pos);
        String current = aliases.aliasOf(pkg);

        EditText input = new EditText(this);
        input.setTypeface(Typeface.MONOSPACE);
        input.setText(current != null ? current : "");
        input.setHint("alias  (vacío = quitar)");
        input.setGravity(Gravity.START);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(0x44FFFFFF);
        int pad = dpToPx(24);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
            .setTitle(appNames.get(pos))
            .setView(input)
            .setPositiveButton("ok", (d, w) -> {
                String val = input.getText().toString().trim();
                if (val.isEmpty()) aliases.remove(current != null ? current : "");
                else               aliases.set(val, pkg);
                adapter.notifyDataSetChanged();
            })
            .setNeutralButton("desinstalar", (d, w) -> {
                startActivity(new Intent(Intent.ACTION_DELETE,
                        Uri.parse("package:" + pkg)));
            })
            .setNegativeButton("cancelar", null)
            .show();
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        Collections.sort(infos, (a, b) ->
                a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));
        for (ResolveInfo r : infos) {
            appNames.add(r.loadLabel(pm).toString());
            appPackages.add(r.activityInfo.packageName);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
