package com.voidlauncher.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import com.voidlauncher.core.AppLauncher;
import com.voidlauncher.data.ContextualApps;
import com.voidlauncher.data.RecentApps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuickSearchDialog {

    private final LauncherActivity launcher;
    private final String[]         names;
    private final String[]         packages;
    private final ContextualApps   contextual;
    private final RecentApps       recents;
    private final Set<String>      newlyInstalled = new HashSet<>();

    private final List<String> filteredNames = new ArrayList<>();
    private final List<String> filteredPkgs  = new ArrayList<>();

    private ArrayAdapter<String> adapter;
    private AlertDialog          dialog;

    public QuickSearchDialog(LauncherActivity launcher, String[] names,
                             String[] packages, ContextualApps contextual,
                             RecentApps recents) {
        this.launcher   = launcher;
        this.names      = names;
        this.packages   = packages;
        this.contextual = contextual;
        this.recents    = recents;
        
        // Identificar apps instaladas en las últimas 24h
        long yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        for (String pkg : packages) {
            try {
                long installed = launcher.getPackageManager().getPackageInfo(pkg, 0).firstInstallTime;
                if (installed > yesterday) newlyInstalled.add(pkg);
            } catch (Exception ignored) {}
        }
    }

    public void show() {
        dialog = new AlertDialog.Builder(launcher)
                .setView(buildLayout())
                .create();
        dialog.show();
    }

    private LinearLayout buildLayout() {
        LinearLayout root = new LinearLayout(launcher);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(40, 40, 40, 0);

        EditText input = buildInput();
        ListView list  = buildList();

        root.addView(input);
        root.addView(list);

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });

        filter("");
        showKeyboard(input);
        return root;
    }

    private EditText buildInput() {
        EditText input = new EditText(launcher);
        input.setHint(launcher.getString(com.voidlauncher.R.string.search_hint));
        input.setHintTextColor(0xFF555555);
        input.setTextColor(Color.WHITE);
        input.setTextSize(18f);
        input.setBackgroundColor(Color.TRANSPARENT);
        return input;
    }

    private ListView buildList() {
        adapter = new ArrayAdapter<String>(launcher, android.R.layout.simple_list_item_1, filteredNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                String pkg = filteredPkgs.get(position);
                // Verde si es instalada recientemente (últimas 24h)
                if (newlyInstalled.contains(pkg)) {
                    tv.setTextColor(0xFF00FF00);
                } else {
                    tv.setTextColor(Color.WHITE);
                }
                return tv;
            }
        };
        ListView list = new ListView(launcher);
        list.setBackgroundColor(Color.BLACK);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                launch(filteredPkgs.get(pos));
            }
        });
        return list;
    }

    private void launch(String pkg) {
        dialog.dismiss();
        launcher.onAppLaunched(pkg);
        AppLauncher.launch(launcher, pkg);
    }

    private void filter(String query) {
        filteredNames.clear();
        filteredPkgs.clear();
        String q = query.toLowerCase().trim();

        if (q.isEmpty()) {
            for (String pkg : contextual.getTop(packages)) {
                for (int i = 0; i < packages.length; i++) {
                    if (packages[i].equals(pkg)) {
                        filteredNames.add(names[i]);
                        filteredPkgs.add(packages[i]);
                        break;
                    }
                }
            }
        } else if (q.equals("/all")) {
            for (int i = 0; i < names.length; i++) {
                filteredNames.add(names[i]);
                filteredPkgs.add(packages[i]);
            }
        } else if (q.equals("/xyzzy") || q.equals("/void")) {
            dialog.dismiss();
            launcher.startActivity(new Intent(launcher, SettingsActivity.class));
            return;
        } else {
            for (int i = 0; i < names.length; i++) {
                if (names[i].toLowerCase().contains(q)) {
                    filteredNames.add(names[i]);
                    filteredPkgs.add(packages[i]);
                }
            }
            if (filteredNames.size() == 1) {
                launch(filteredPkgs.get(0));
                return;
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showKeyboard(EditText input) {
        input.requestFocus();
        input.postDelayed(new Runnable() {
            @Override public void run() {
                InputMethodManager imm = (InputMethodManager)
                        launcher.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }
}
