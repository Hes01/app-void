package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.hes01.voidlauncher.R;
import com.voidlauncher.data.AliasRepository;
import com.voidlauncher.data.HiddenAppsRepository;
import java.util.ArrayList;
import java.util.List;

class SettingsAppsPanel {
    private final LauncherActivity     launcher;
    private final List<String>         names;
    private final List<String>         pkgs;
    private final AliasRepository      aliases;
    private final HiddenAppsRepository hidden;
    private final List<Integer>        filtered = new ArrayList<>();
    private String      filterMode  = "all";
    private String      query       = "";
    private BaseAdapter adapter;
    private LinearLayout editingRow = null;
    private int          editingIdx = -1;

    SettingsAppsPanel(LauncherActivity launcher, List<String> names, List<String> pkgs,
                      AliasRepository aliases, HiddenAppsRepository hidden) {
        this.launcher = launcher; this.names = names; this.pkgs = pkgs;
        this.aliases  = aliases;  this.hidden = hidden;
    }

    View build() {
        LinearLayout root = new LinearLayout(launcher);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(buildSearch());
        root.addView(buildFilters());
        ListView list = buildList();
        root.addView(list, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        applyFilter();
        return root;
    }

    private View buildSearch() {
        EditText et = new EditText(launcher);
        et.setTypeface(Typeface.MONOSPACE); et.setTextColor(VoidTheme.FG2);
        et.setHintTextColor(VoidTheme.FG5); et.setHint(launcher.getString(R.string.search_app_hint));
        et.setTextSize(VoidTheme.TEXT_LG); et.setBackgroundColor(0);
        et.setPadding(dp(20), dp(14), dp(20), dp(10));
        et.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable e) { query = e.toString().toLowerCase().trim(); applyFilter(); }
        });
        return et;
    }

    private View buildFilters() {
        TextView fAll   = filterBtn(launcher.getString(R.string.filter_all));
        TextView fAlias = filterBtn(launcher.getString(R.string.filter_aliased));
        TextView fNo    = filterBtn(launcher.getString(R.string.filter_no_alias));
        setFilterActive(fAll, true);
        fAll.setOnClickListener(v   -> { filterMode = "all";     setFilterActive(fAll, true);  setFilterActive(fAlias, false); setFilterActive(fNo, false); applyFilter(); });
        fAlias.setOnClickListener(v -> { filterMode = "alias";   setFilterActive(fAll, false); setFilterActive(fAlias, true);  setFilterActive(fNo, false); applyFilter(); });
        fNo.setOnClickListener(v    -> { filterMode = "noalias"; setFilterActive(fAll, false); setFilterActive(fAlias, false); setFilterActive(fNo, true);  applyFilter(); });
        LinearLayout row = new LinearLayout(launcher);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(12), dp(20), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dp(8);
        row.addView(fAll, lp); row.addView(fAlias, lp); row.addView(fNo);
        return row;
    }

    private TextView filterBtn(String text) {
        TextView tv = new TextView(launcher);
        tv.setText(text); tv.setTextColor(VoidTheme.FG5); tv.setTextSize(VoidTheme.TEXT_XS);
        tv.setTypeface(Typeface.MONOSPACE); tv.setLetterSpacing(0.15f);
        tv.setPadding(dp(10), dp(4), dp(10), dp(4));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0); bg.setStroke(1, VoidTheme.LINE); bg.setCornerRadius(dp(2));
        tv.setBackground(bg); return tv;
    }

    private void setFilterActive(TextView tv, boolean on) {
        tv.setTextColor(on ? VoidTheme.FG : VoidTheme.FG5);
        ((GradientDrawable) tv.getBackground()).setStroke(1, on ? VoidTheme.FG3 : VoidTheme.LINE);
    }

    private ListView buildList() {
        adapter = new BaseAdapter() {
            @Override public int getCount() { return filtered.size(); }
            @Override public Object getItem(int i) { return filtered.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int pos, View cv, ViewGroup p) {
                int idx = filtered.get(pos);
                if (cv instanceof LinearLayout && ((LinearLayout) cv).getChildCount() == 3) {
                    populateRow((LinearLayout) cv, idx); return cv;
                }
                return buildRow(idx);
            }
        };
        ListView lv = new ListView(launcher);
        lv.setBackgroundColor(VoidTheme.BG); GradientDrawable div = new GradientDrawable();
        div.setColor(VoidTheme.LINE); lv.setDivider(div); lv.setDividerHeight(dp(1));
        lv.setSelector(android.R.color.transparent);
        lv.setOverScrollMode(View.OVER_SCROLL_NEVER); lv.setVerticalScrollBarEnabled(false);
        lv.setAdapter(adapter); return lv;
    }

    private View buildRow(int i) {
        String pkg = pkgs.get(i); String alias = aliases.aliasOf(pkg);
        boolean isHid = hidden.isHidden(pkg);
        LinearLayout row = new LinearLayout(launcher);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(11), dp(20), dp(11));
        TextView tvAlias = mono(alias != null ? alias : "—", alias != null ? VoidTheme.FG4 : VoidTheme.FG5, VoidTheme.TEXT_MD, dp(56));
        TextView tvName  = mono(names.get(i), alias != null ? VoidTheme.FG2 : VoidTheme.FG5, VoidTheme.TEXT_LG, 0);
        ImageView ivEye  = makeEye(isHid);
        ivEye.setOnClickListener(v -> { hidden.toggle(pkg); refresh(); });
        row.setOnClickListener(v -> makeEditable(row, pkg, i));
        row.addView(tvAlias);
        row.addView(tvName, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(ivEye); return row;
    }

    private void populateRow(LinearLayout row, int i) {
        if (row.getChildCount() < 3) return;
        String pkg = pkgs.get(i); String alias = aliases.aliasOf(pkg); boolean isHid = hidden.isHidden(pkg);
        TextView tvAlias   = (TextView)  row.getChildAt(0);
        TextView tvName    = (TextView)  row.getChildAt(1);
        ImageView ivEye    = (ImageView) row.getChildAt(2);
        if (tvAlias == null || tvName == null || ivEye == null) return;
        tvAlias.setText(alias != null ? alias : "—"); tvAlias.setTextColor(alias != null ? VoidTheme.FG4 : VoidTheme.FG5);
        tvName.setText(names.get(i)); tvName.setTextColor(alias != null ? VoidTheme.FG2 : VoidTheme.FG5);
        ivEye.setImageResource(isHid ? R.drawable.ic_eye_off : R.drawable.ic_eye);
        ivEye.setColorFilter(isHid ? VoidTheme.ERROR : VoidTheme.FG5, PorterDuff.Mode.SRC_IN);
        ivEye.setOnClickListener(v -> { hidden.toggle(pkg); refresh(); });
        row.setBackgroundColor(0); row.setOnClickListener(v -> makeEditable(row, pkg, i));
    }

    private ImageView makeEye(boolean isHid) {
        ImageView iv = new ImageView(launcher);
        int size = dp(18);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.leftMargin = dp(8);
        iv.setLayoutParams(lp);
        iv.setImageResource(isHid ? R.drawable.ic_eye_off : R.drawable.ic_eye);
        iv.setColorFilter(isHid ? VoidTheme.ERROR : VoidTheme.FG5, PorterDuff.Mode.SRC_IN);
        return iv;
    }

    private void applyFilter() {
        editingRow = null; editingIdx = -1;
        filtered.clear();
        for (int i = 0; i < names.size(); i++) {
            String alias = aliases.aliasOf(pkgs.get(i));
            if (filterMode.equals("alias")   && alias == null) continue;
            if (filterMode.equals("noalias") && alias != null) continue;
            if (!query.isEmpty()) {
                String n = names.get(i).toLowerCase();
                if (!n.contains(query) && (alias == null || !alias.contains(query))) continue;
            }
            filtered.add(i);
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void closeEditingRow() {
        if (editingRow == null) return;
        LinearLayout row = editingRow; int idx = editingIdx;
        editingRow = null; editingIdx = -1;
        String pkg = pkgs.get(idx); String alias = aliases.aliasOf(pkg); boolean isHid = hidden.isHidden(pkg);
        row.removeAllViews(); row.setBackgroundColor(0);
        TextView tvAlias = mono(alias != null ? alias : "—", alias != null ? VoidTheme.FG4 : VoidTheme.FG5, VoidTheme.TEXT_MD, dp(56));
        TextView tvName  = mono(names.get(idx), alias != null ? VoidTheme.FG2 : VoidTheme.FG5, VoidTheme.TEXT_LG, 0);
        ImageView ivEye  = makeEye(isHid);
        ivEye.setOnClickListener(v -> { hidden.toggle(pkg); refresh(); });
        row.setOnClickListener(v -> makeEditable(row, pkg, idx));
        row.addView(tvAlias);
        row.addView(tvName, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(ivEye);
    }

    private void makeEditable(LinearLayout row, String pkg, int idx) {
        if (editingRow != null && editingRow != row) closeEditingRow();
        editingRow = row; editingIdx = idx;
        row.removeAllViews(); row.setBackgroundColor(VoidTheme.BG_CARD);
        String current = aliases.aliasOf(pkg);
        EditText et = new EditText(launcher); et.setTypeface(Typeface.MONOSPACE);
        et.setTextColor(VoidTheme.FG); et.setTextSize(VoidTheme.TEXT_LG); et.setBackgroundColor(0);
        et.setPadding(dp(20), dp(11), dp(8), dp(11)); et.setSingleLine(true);
        et.setHint(launcher.getString(R.string.hint_alias)); et.setHintTextColor(VoidTheme.FG5);
        if (current != null) { et.setText(current); }
        TextView ok = mono("aceptar", VoidTheme.FG, VoidTheme.TEXT_SM, 0);
        ok.setGravity(Gravity.CENTER); ok.setPadding(dp(16), dp(11), dp(16), dp(11));
        ok.setLetterSpacing(0.1f);
        GradientDrawable okBg = new GradientDrawable();
        okBg.setColor(VoidTheme.FG5);
        ok.setBackground(okBg);
        ok.setOnClickListener(v -> {
            String val = et.getText().toString().trim();
            if (current != null) aliases.remove(current);
            if (!val.isEmpty()) aliases.set(val, pkg);
            InputMethodManager imm = (InputMethodManager) launcher.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
            editingRow = null; editingIdx = -1;
            refresh();
        });
        row.addView(et, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(ok);
        et.post(() -> { et.requestFocus(); if (current != null) et.setSelection(et.getText().length()); QuickSearchLayout.showKeyboard(launcher, et); });
    }

    private void refresh() { applyFilter(); }
    private TextView mono(String text, int color, float size, int fixedPx) {
        TextView tv = new TextView(launcher);
        tv.setText(text); tv.setTextColor(color); tv.setTextSize(size); tv.setTypeface(Typeface.MONOSPACE);
        if (fixedPx > 0) tv.setWidth(fixedPx); return tv;
    }

    private int dp(int v) { return QuickSearchLayout.dp(launcher, v); }
}
