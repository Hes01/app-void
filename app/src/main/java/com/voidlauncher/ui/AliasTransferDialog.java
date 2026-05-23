package com.voidlauncher.ui;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.hes01.voidlauncher.R;
import com.voidlauncher.data.AliasRepository;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;

class AliasTransferDialog {

    static void showExport(Context ctx) {
        AliasRepository repo = new AliasRepository(ctx);
        String json = buildJson(repo);

        TextView body = new TextView(ctx);
        body.setText(json.isEmpty() ? ctx.getString(R.string.label_no_aliases) : json); body.setTextIsSelectable(true);
        body.setTypeface(Typeface.MONOSPACE); body.setTextSize(VoidTheme.TEXT_SM); body.setTextColor(VoidTheme.FG2);
        body.setPadding(dp(ctx, 16), dp(ctx, 12), dp(ctx, 16), dp(ctx, 12));
        ScrollView sv = new ScrollView(ctx); sv.addView(body);

        TextView btnCopy = btn(ctx, ctx.getString(R.string.btn_copy)); TextView btnShare = btn(ctx, ctx.getString(R.string.btn_share));
        LinearLayout bar = hbar(ctx);
        bar.addView(btnCopy, flex()); bar.addView(btnShare, flex());

        LinearLayout root = root(ctx);
        root.addView(header(ctx, ctx.getString(R.string.action_export_aliases)));
        root.addView(sv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(sep(ctx)); root.addView(bar);

        Dialog d = mkDialog(ctx, root);
        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("aliases", json));
            Toast.makeText(ctx, ctx.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show();
        });
        btnShare.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_TEXT, json);
            ctx.startActivity(Intent.createChooser(i, null));
        });
        d.show();
    }

    static void showImport(Context ctx) {
        AliasRepository repo = new AliasRepository(ctx);

        EditText et = new EditText(ctx);
        et.setTypeface(Typeface.MONOSPACE); et.setTextSize(VoidTheme.TEXT_SM);
        et.setTextColor(VoidTheme.FG); et.setHintTextColor(VoidTheme.FG5);
        et.setHint(ctx.getString(R.string.hint_alias_json)); et.setMinLines(6); et.setBackgroundColor(0);
        et.setPadding(dp(ctx, 16), dp(ctx, 12), dp(ctx, 16), dp(ctx, 12));

        TextView btnOk = btn(ctx, ctx.getString(R.string.btn_import));
        LinearLayout bar = hbar(ctx); bar.addView(btnOk, flex());

        LinearLayout root = root(ctx);
        root.addView(header(ctx, ctx.getString(R.string.action_import_aliases))); root.addView(et);
        root.addView(sep(ctx)); root.addView(bar);

        Dialog d = mkDialog(ctx, root);
        if (d.getWindow() != null)
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        btnOk.setOnClickListener(v -> {
            int n = parseAndApply(ctx, et.getText().toString(), repo);
            Toast.makeText(ctx, ctx.getString(R.string.toast_aliases_imported, n), Toast.LENGTH_SHORT).show();
            d.dismiss();
        });
        d.show();
        QuickSearchLayout.showKeyboard(ctx, et);
    }

    // ── Lógica ────────────────────────────────────────────────────────────────

    private static String buildJson(AliasRepository repo) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, ?> e : repo.getAll().entrySet())
            sb.append("\"").append(e.getKey()).append("\":\"").append(e.getValue()).append("\",");
        if (sb.length() > 1) sb.setLength(sb.length() - 1);
        return sb.append("}").toString();
    }

    private static int parseAndApply(Context ctx, String input, AliasRepository repo) {
        int count = 0;
        try {
            JSONObject json = new JSONObject(input.trim());
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String alias = keys.next();
                String pkg   = json.getString(alias);
                if (!alias.isEmpty() && isInstalled(ctx, pkg)) { repo.set(alias, pkg); count++; }
            }
        } catch (Exception ignored) {}
        return count;
    }

    private static boolean isInstalled(Context ctx, String pkg) {
        try { ctx.getPackageManager().getPackageInfo(pkg, 0); return true; } catch (Exception e) { return false; }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private static Dialog mkDialog(Context ctx, LinearLayout root) {
        Dialog d = new Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        d.setContentView(root);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new ColorDrawable(VoidTheme.BG));
            d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            d.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
        return d;
    }

    private static LinearLayout root(Context ctx)   { LinearLayout ll = new LinearLayout(ctx); ll.setOrientation(LinearLayout.VERTICAL); ll.setBackgroundColor(VoidTheme.BG); return ll; }
    private static LinearLayout hbar(Context ctx)   { LinearLayout ll = new LinearLayout(ctx); ll.setOrientation(LinearLayout.HORIZONTAL); ll.setPadding(dp(ctx,16),dp(ctx,12),dp(ctx,16),dp(ctx,28)); return ll; }
    private static LinearLayout.LayoutParams flex() { return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f); }
    private static int dp(Context ctx, int v)       { return QuickSearchLayout.dp(ctx, v); }

    private static TextView header(Context ctx, String text) {
        TextView tv = new TextView(ctx);
        tv.setText(text.toUpperCase()); tv.setTextColor(VoidTheme.FG5);
        tv.setTextSize(VoidTheme.TEXT_XS); tv.setTypeface(Typeface.MONOSPACE);
        tv.setLetterSpacing(0.2f); tv.setPadding(dp(ctx,16), dp(ctx,28), dp(ctx,16), dp(ctx,16)); return tv;
    }

    private static View sep(Context ctx) {
        View v = new View(ctx); v.setBackgroundColor(VoidTheme.LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)); return v;
    }

    private static TextView btn(Context ctx, String text) {
        TextView tv = new TextView(ctx); tv.setText(text); tv.setGravity(Gravity.CENTER);
        tv.setTextColor(VoidTheme.FG2); tv.setTextSize(VoidTheme.TEXT_MD); tv.setTypeface(Typeface.MONOSPACE); return tv;
    }
}
