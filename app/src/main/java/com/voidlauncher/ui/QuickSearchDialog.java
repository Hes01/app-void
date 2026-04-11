package com.voidlauncher.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.view.View;
import com.voidlauncher.core.AppLauncher;
import com.voidlauncher.data.ContextualApps;
import java.util.ArrayList;
import java.util.List;

public class QuickSearchDialog {

    private final LauncherActivity launcher;
    private final String[]         names;
    private final String[]         packages;
    private final ContextualApps   contextual;

    private final List<String> filteredNames = new ArrayList<>();
    private final List<String> filteredPkgs  = new ArrayList<>();

    private ArrayAdapter<String> adapter;
    private AlertDialog          dialog;

    public QuickSearchDialog(LauncherActivity launcher, String[] names,
                             String[] packages, ContextualApps contextual) {
        this.launcher   = launcher;
        this.names      = names;
        this.packages   = packages;
        this.contextual = contextual;
    }

    public void show() {
        LinearLayout layout = buildLayout();
        dialog = new AlertDialog.Builder(launcher)
                .setView(layout)
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
        adapter = new ArrayAdapter<>(launcher, android.R.layout.simple_list_item_1, filteredNames);
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
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }
}
