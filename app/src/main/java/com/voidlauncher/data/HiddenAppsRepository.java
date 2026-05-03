package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class HiddenAppsRepository {
    private static final String PREFS = "void_hidden";
    private static final String KEY   = "pkgs";
    private final SharedPreferences prefs;

    public HiddenAppsRepository(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isHidden(String pkg) {
        return prefs.getStringSet(KEY, new HashSet<>()).contains(pkg);
    }

    public void toggle(String pkg) {
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY, new HashSet<>()));
        if (!set.remove(pkg)) set.add(pkg);
        prefs.edit().putStringSet(KEY, set).apply();
    }
}
