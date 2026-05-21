package com.voidlauncher.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.voidlauncher.data.AliasRepository;
import java.util.List;

class QuickSearchAdapter extends ArrayAdapter<String> {

    private final LauncherActivity launcher;
    private final List<String>     filteredPkgs;
    private final AliasRepository  aliases;
    private final String[]         allNames;
    private final String[]         allPkgs;
    boolean activeSearch;
    boolean isTopMode;
    private final boolean showRealName;

    QuickSearchAdapter(LauncherActivity launcher, List<String> filteredNames,
                       List<String> filteredPkgs, AliasRepository aliases,
                       String[] allNames, String[] allPkgs) {
        super(launcher, 0, filteredNames);
        this.launcher     = launcher; this.filteredPkgs = filteredPkgs;
        this.aliases      = aliases;  this.allNames     = allNames; this.allPkgs = allPkgs;
        this.showRealName = launcher.getSharedPreferences("void_config", Context.MODE_PRIVATE).getBoolean("show_real_name", true);
    }

    @Override
    public View getView(int pos, View cv, ViewGroup parent) {
        TextView tv = cv instanceof TextView ? (TextView) cv : new TextView(launcher);
        tv.setText(getItem(pos));
        boolean empty = filteredPkgs.size() > pos && filteredPkgs.get(pos).isEmpty();
        boolean first = activeSearch && pos == 0 && !empty;
        tv.setTextSize(first ? VoidTheme.TEXT_XXL : 15f);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setLetterSpacing(0.05f);
        int h = QuickSearchLayout.dp(launcher, 11);
        int v = QuickSearchLayout.dp(launcher, 16);
        tv.setPadding(v, h, v, h);

        if (!activeSearch && !empty) {
            String pkg   = filteredPkgs.get(pos);
            String alias = aliases.aliasOf(pkg);
            if (alias != null) {
                int rankColor = isTopMode && pos < VoidTheme.TOP_COLORS.length
                        ? VoidTheme.TOP_COLORS[pos] : VoidTheme.FG4;
                if (!showRealName) {
                    tv.setTextColor(isTopMode ? rankColor : VoidTheme.FG3);
                    tv.setText(alias); return tv;
                }
                String full  = getRealName(pkg) + "   " + alias;
                int    start = full.length() - alias.length();
                SpannableString ss = new SpannableString(full);
                ss.setSpan(new AbsoluteSizeSpan(13, true), start, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (isTopMode) { tv.setTextColor(rankColor); }
                else {
                    ss.setSpan(new ForegroundColorSpan(rankColor), start, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tv.setTextColor(VoidTheme.FG3);
                }
                tv.setText(ss); return tv;
            }
            if (!isTopMode) { tv.setTextColor(VoidTheme.FG3); tv.setText(getItem(pos)); return tv; }
        }
        if (activeSearch && !empty && showRealName) {
            String pkg   = filteredPkgs.get(pos);
            String alias = aliases.aliasOf(pkg);
            if (alias != null) {
                String full  = getRealName(pkg) + "   " + alias;
                int    start = full.length() - alias.length();
                SpannableString ss = new SpannableString(full);
                ss.setSpan(new AbsoluteSizeSpan(13, true), start, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(VoidTheme.FG4), start, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setTextColor(first ? VoidTheme.FG : VoidTheme.FG3);
                tv.setText(ss); return tv;
            }
        }
        int color;
        if (isTopMode && !empty && pos < VoidTheme.TOP_COLORS.length) color = VoidTheme.TOP_COLORS[pos];
        else color = first ? VoidTheme.FG : VoidTheme.FG4;
        tv.setTextColor(color); tv.setText(getItem(pos)); return tv;
    }

    private String getRealName(String pkg) {
        for (int i = 0; i < allPkgs.length; i++)
            if (allPkgs[i].equals(pkg)) return allNames[i];
        return pkg;
    }
}
