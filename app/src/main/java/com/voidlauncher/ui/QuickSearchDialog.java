package com.voidlauncher.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import com.voidlauncher.core.AppLauncher;
import com.voidlauncher.data.AliasRepository;
import com.voidlauncher.data.ContextualApps;
import java.util.ArrayList;
import java.util.List;

public class QuickSearchDialog {

    private final LauncherActivity launcher;
    private final String[]         names;
    private final String[]         packages;
    private final ContextualApps   contextual;
    private final AliasRepository  aliases;

    private final List<String> filteredNames = new ArrayList<>();
    private final List<String> filteredPkgs  = new ArrayList<>();

    private ArrayAdapter<String> adapter;
    private Dialog               dialog;

    public QuickSearchDialog(LauncherActivity launcher, String[] names,
                             String[] packages, ContextualApps contextual,
                             AliasRepository aliases) {
        this.launcher   = launcher;
        this.names      = names;
        this.packages   = packages;
        this.contextual = contextual;
        this.aliases    = aliases;
    }

    public void show() {
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
    }

    private LinearLayout buildLayout() {
        LinearLayout root = new LinearLayout(launcher);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(16), dp(24), dp(16), 0);

        LinearLayout inputRow = new LinearLayout(launcher);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView promptSign = new TextView(launcher);
        promptSign.setText("> ");
        promptSign.setTextColor(0x66FFFFFF);
        promptSign.setTextSize(22f);
        promptSign.setTypeface(Typeface.create("monospace", Typeface.NORMAL));

        final EditText input = buildInput();
        inputRow.addView(promptSign);
        inputRow.addView(input, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        ListView list = buildList();

        root.addView(inputRow);
        root.addView(list);

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });

        filter("");
        
        input.requestFocus();
        input.postDelayed(new Runnable() {
            @Override public void run() {
                InputMethodManager imm = (InputMethodManager)
                        launcher.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);

        return root;
    }

    private EditText buildInput() {
        EditText input = new EditText(launcher);
        input.setHint("");
        input.setTextColor(Color.WHITE);
        input.setTextSize(22f);
        input.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        input.setPadding(0, 0, 0, 0);
        return input;
    }

    private ListView buildList() {
        adapter = new ArrayAdapter<String>(launcher, android.R.layout.simple_list_item_1, filteredNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(16f);
                tv.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
                tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                tv.setPadding(0, dp(12), 0, dp(12));
                return tv;
            }
        };
        ListView list = new ListView(launcher);
        list.setBackgroundColor(Color.BLACK);
        list.setDivider(null); 
        list.setDividerHeight(0);
        list.setSelector(android.R.color.transparent);
        list.setOverScrollMode(View.OVER_SCROLL_NEVER);
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
                        filteredNames.add(displayName(i));
                        filteredPkgs.add(packages[i]);
                        break;
                    }
                }
            }
        } else if (q.equals("/all")) {
            for (int i = 0; i < names.length; i++) {
                filteredNames.add(displayName(i));
                filteredPkgs.add(packages[i]);
            }
        } else if (q.equals("/void")) {
            new SettingsDialog(launcher, aliases, dialog).show();
            return;
        } else if (q.startsWith("/")) {
            String cmd = q.substring(1).trim();
            String pkg = aliases.resolve(cmd);
            if (pkg != null) { launch(pkg); return; }
        } else {
            for (int i = 0; i < names.length; i++) {
                String alias = aliases.aliasOf(packages[i]);
                boolean matchAlias = alias != null && alias.contains(q);
                boolean matchName  = names[i].toLowerCase().contains(q);
                if (matchAlias || matchName) {
                    filteredNames.add(alias != null ? alias : names[i]);
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

    private String displayName(int i) {
        String alias = aliases.aliasOf(packages[i]);
        return alias != null ? alias : names[i];
    }

    private int dp(int dp) {
        return Math.round(dp * launcher.getResources().getDisplayMetrics().density);
    }
}
