package com.voidlauncher.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import com.voidlauncher.core.AppLauncher;
import com.voidlauncher.core.CommandRouter;
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

    private final List<String>     filteredNames = new ArrayList<>();
    private final List<String>     filteredPkgs  = new ArrayList<>();
    private ArrayAdapter<String>   adapter;
    private Dialog                 dialog;

    public QuickSearchDialog(LauncherActivity launcher, String[] names,
                             String[] packages, ContextualApps contextual,
                             AliasRepository aliases) {
        this.launcher = launcher; this.names = names;
        this.packages = packages; this.contextual = contextual; this.aliases = aliases;
    }

    public void show() {
        QuickSearchLayout layout = QuickSearchLayout.build(launcher);
        adapter = buildAdapter();
        layout.list.setAdapter(adapter);
        layout.list.setOnItemClickListener((p, v, pos, id) -> launch(filteredPkgs.get(pos)));
        layout.input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });

        dialog = new Dialog(launcher, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(layout.root);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
        }
        filter("");
        dialog.show();
        layout.input.requestFocus();
        layout.input.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager)
                    launcher.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(layout.input, InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    private ArrayAdapter<String> buildAdapter() {
        return new ArrayAdapter<String>(launcher, android.R.layout.simple_list_item_1, filteredNames) {
            @Override public View getView(int pos, View cv, ViewGroup parent) {
                TextView tv = (TextView) super.getView(pos, cv, parent);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(16f);
                tv.setTypeface(Typeface.MONOSPACE);
                tv.setPadding(0, QuickSearchLayout.dp(launcher, 12), 0, QuickSearchLayout.dp(launcher, 12));
                return tv;
            }
        };
    }

    private void filter(String query) {
        filteredNames.clear(); filteredPkgs.clear();
        String q = query.toLowerCase().trim();
        if (q.isEmpty()) {
            for (String pkg : contextual.getTop(packages))
                for (int i = 0; i < packages.length; i++)
                    if (packages[i].equals(pkg)) { filteredNames.add(displayName(i)); filteredPkgs.add(pkg); break; }
        } else if (q.equals("/all")) {
            for (int i = 0; i < names.length; i++) { filteredNames.add(displayName(i)); filteredPkgs.add(packages[i]); }
        } else if (q.equals("/void")) {
            new SettingsDialog(launcher, aliases, dialog).show(); return;
        } else if (q.startsWith("/")) {
            routeCommand(q.substring(1).trim()); return;
        } else {
            for (int i = 0; i < names.length; i++) {
                String alias = aliases.aliasOf(packages[i]);
                if ((alias != null && alias.contains(q)) || names[i].toLowerCase().contains(q)) {
                    filteredNames.add(alias != null ? alias : names[i]); filteredPkgs.add(packages[i]);
                }
            }
            if (filteredNames.size() == 1) { launch(filteredPkgs.get(0)); return; }
        }
        adapter.notifyDataSetChanged();
    }

    private void routeCommand(String raw) {
        CommandRouter cmd = CommandRouter.parse(raw);
        String pkg = aliases.resolve(cmd.alias);
        if (pkg == null) { adapter.notifyDataSetChanged(); return; }
        if (cmd.isUninstall()) {
            dialog.dismiss();
            launcher.startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + pkg)));
        } else if (cmd.isList()) {
            queryPlugin(pkg);
        } else {
            launchWithArgs(pkg, cmd.rawArgs());
        }
    }

    private void launchWithArgs(String pkg, String args) {
        dialog.dismiss(); launcher.onAppLaunched(pkg);
        Intent intent = launcher.getPackageManager().getLaunchIntentForPackage(pkg);
        if (intent == null) return;
        if (args != null && !args.isEmpty()) intent.putExtra(CommandRouter.EXTRA_ARGS, args);
        launcher.startActivity(intent);
    }

    private void queryPlugin(String pkg) {
        Uri uri = Uri.parse("content://" + pkg + ".provider/items");
        try (Cursor c = launcher.getContentResolver().query(uri, null, null, null, null)) {
            if (c == null) return;
            filteredNames.clear(); filteredPkgs.clear();
            while (c.moveToNext()) {
                filteredNames.add(c.getString(c.getColumnIndexOrThrow("title")));
                filteredPkgs.add(pkg + ":" + c.getInt(c.getColumnIndexOrThrow("_id")));
            }
        } catch (Exception ignored) {}
        adapter.notifyDataSetChanged();
    }

    private void launch(String pkg) { dialog.dismiss(); launcher.onAppLaunched(pkg); AppLauncher.launch(launcher, pkg); }
    private String displayName(int i) { String a = aliases.aliasOf(packages[i]); return a != null ? a : names[i]; }
}
