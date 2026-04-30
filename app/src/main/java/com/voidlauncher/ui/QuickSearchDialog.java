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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    private TextView               label;
    private EditText               searchInput;

    public QuickSearchDialog(LauncherActivity launcher, String[] names,
                             String[] packages, ContextualApps contextual,
                             AliasRepository aliases) {
        this.launcher = launcher; this.names = names;
        this.packages = packages; this.contextual = contextual; this.aliases = aliases;
    }

    public void show() {
        QuickSearchLayout layout = QuickSearchLayout.build(launcher);
        label       = layout.label;
        searchInput = layout.input;
        adapter     = buildAdapter();
        layout.list.setAdapter(adapter);
        layout.list.setOnItemClickListener((p, v, pos, id) -> {
            String pkg = filteredPkgs.get(pos);
            if (pkg.isEmpty()) return;
            v.setAlpha(0.4f);
            v.postDelayed(() -> { v.setAlpha(1f); launch(pkg); }, 120);
        });
        GestureDetector gd = new GestureDetector(launcher,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override public boolean onFling(MotionEvent e1, MotionEvent e2,
                                                    float vx, float vy) {
                        if (vy > 800 && Math.abs(vy) > Math.abs(vx) * 1.5f
                                && filteredNames.size() <= 8) {
                            dialog.dismiss(); return true;
                        }
                        return false;
                    }
                });
        layout.list.setOnTouchListener((v, e) -> { gd.onTouchEvent(e); return false; });
        layout.input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });

        dialog = new Dialog(launcher, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(layout.root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
        }
        filter("");
        dialog.show();
        VibrationFeedback.onOpen(hapticView());
        SearchHints.showIfNeeded(launcher, layout.hintRow, layout.hintText, layout.input);
        layout.input.requestFocus();
        layout.input.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager)
                    launcher.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(layout.input, InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    private ArrayAdapter<String> buildAdapter() {
        return new ArrayAdapter<String>(launcher, 0, filteredNames) {
            @Override public View getView(int pos, View cv, ViewGroup parent) {
                TextView tv = cv instanceof TextView ? (TextView) cv : new TextView(launcher);
                tv.setText(getItem(pos));
                boolean empty = filteredPkgs.size() > pos && filteredPkgs.get(pos).isEmpty();
                boolean first = (pos == 0) && !empty;
                tv.setTextColor(first ? 0xFFE8E8E8 : 0xFF4A4A4A);
                tv.setTextSize(first ? 17f : 15f);
                tv.setTypeface(Typeface.MONOSPACE);
                tv.setLetterSpacing(0.05f);
                int h = QuickSearchLayout.dp(launcher, 11);
                int v = QuickSearchLayout.dp(launcher, 16);
                tv.setPadding(v, h, v, h);
                return tv;
            }
        };
    }

    private void filter(String query) {
        filteredNames.clear(); filteredPkgs.clear();
        String q = query.toLowerCase().trim();
        setInputColor(!q.isEmpty() && q.startsWith(".") ? 0xB3E8E8E8 : 0xFFE8E8E8);
        if (q.isEmpty()) {
            setLabelColor(0xFF4A4A4A);
            for (String pkg : contextual.getTop())
                for (int i = 0; i < packages.length; i++)
                    if (packages[i].equals(pkg)) { filteredNames.add(displayName(i)); filteredPkgs.add(pkg); break; }
        } else if (q.equals(".all")) {
            setLabelColor(0xFF4A4A4A);
            for (int i = names.length - 1; i >= 0; i--) { filteredNames.add(displayName(i)); filteredPkgs.add(packages[i]); }
        } else if (q.equals(".void")) {
            new SettingsDialog(launcher, aliases, dialog).show(); return;
        } else if (q.startsWith(".")) {
            setLabelColor(0xFF4A4A4A);
            routeCommand(q.substring(1).trim()); return;
        } else if (q.matches(".*[a-z0-9].*")) {
            for (int i = 0; i < names.length; i++) {
                String alias = aliases.aliasOf(packages[i]);
                if ((alias != null && alias.contains(q)) || names[i].toLowerCase().contains(q)) {
                    filteredNames.add(alias != null ? alias : names[i]); filteredPkgs.add(packages[i]);
                }
            }
            if (filteredNames.size() == 1) { launch(filteredPkgs.get(0)); return; }
            if (filteredNames.isEmpty()) {
                VibrationFeedback.onNoResults(hapticView());
                setLabelColor(0xFFCC4444);
            } else {
                setLabelColor(0xFFE8E8E8);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void routeCommand(String raw) {
        CommandRouter cmd = CommandRouter.parse(raw);
        String pkg = aliases.resolve(cmd.alias);
        if (pkg == null) { adapter.notifyDataSetChanged(); return; }
        VibrationFeedback.onCommand(hapticView());

        if (cmd.isUninstall()) {
            dialog.dismiss();
            launcher.startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + pkg)));
            return;
        }
        if (cmd.isList()) { queryPlugin(pkg); return; }
        if (cmd.isDeleteItem()) { deletePlugin(pkg, cmd.deleteId()); return; }

        String rawArgs = cmd.rawArgs();
        String entry = cmd.alias + (rawArgs != null ? "  " + rawArgs : "  _");
        filteredNames.add(entry);
        filteredPkgs.add(rawArgs != null ? pkg + "\t" + rawArgs : pkg);
        adapter.notifyDataSetChanged();
    }

    private void launchWithArgs(String pkg, String args) {
        VibrationFeedback.onLaunch(hapticView());
        dialog.dismiss(); launcher.onAppLaunched(pkg);
        Intent intent = launcher.getPackageManager().getLaunchIntentForPackage(pkg);
        if (intent == null) return;
        if (args != null && !args.isEmpty()) intent.putExtra(CommandRouter.EXTRA_ARGS, args);
        launcher.startActivity(intent);
        launcher.overridePendingTransition(0, 0);
    }

    private void queryPlugin(String pkg) {
        Uri uri = Uri.parse("content://" + pkg + ".provider/items");
        try (Cursor c = launcher.getContentResolver().query(uri, null, null, null, null)) {
            if (c == null) return;
            filteredNames.clear(); filteredPkgs.clear();
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow("_id"));
                String title = c.getString(c.getColumnIndexOrThrow("title"));
                filteredNames.add(id + "  →  " + title);
                filteredPkgs.add(pkg + ":" + id);
            }
        } catch (Exception ignored) {}
        adapter.notifyDataSetChanged();
    }

    private void deletePlugin(String pkg, String id) {
        Uri uri = Uri.parse("content://" + pkg + ".provider/items");
        try { launcher.getContentResolver().delete(uri, "_id=?", new String[]{id}); }
        catch (Exception ignored) {}
        queryPlugin(pkg);
    }

    private void launch(String pkgOrCmd) {
        if (pkgOrCmd.contains("\t")) {
            String[] parts = pkgOrCmd.split("\t", 2);
            launchWithArgs(parts[0], parts[1]);
        } else if (pkgOrCmd.contains(":")) {
            String[] parts = pkgOrCmd.split(":", 2);
            dialog.dismiss(); launcher.onAppLaunched(parts[0]);
            Intent intent = launcher.getPackageManager().getLaunchIntentForPackage(parts[0]);
            if (intent == null) return;
            try { intent.putExtra("void.extra.id", Integer.parseInt(parts[1])); }
            catch (NumberFormatException ignored) {}
            launcher.startActivity(intent);
            launcher.overridePendingTransition(0, 0);
        } else {
            VibrationFeedback.onLaunch(hapticView());
            dialog.dismiss(); launcher.onAppLaunched(pkgOrCmd); AppLauncher.launch(launcher, pkgOrCmd);
        }
    }
    private void setLabelColor(int color) {
        if (label == null) return;
        label.setTextColor(color);
    }
    private void setInputColor(int color) {
        if (searchInput == null) return;
        searchInput.setTextColor(color);
    }
    private String displayName(int i) { String a = aliases.aliasOf(packages[i]); return a != null ? a : names[i]; }
    private View hapticView() { return launcher.getWindow().getDecorView(); }
}
