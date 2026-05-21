package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class HiddenAppsRepository {
    private static final String PREFS = "void_hidden";
    private static final String KEY   = "pkgs";
    private final SharedPreferences prefs;
    private final Set<String> cache;

    public HiddenAppsRepository(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        cache = new HashSet<>(prefs.getStringSet(KEY, new HashSet<>()));
    }

    public boolean isHidden(String pkg) {
        return cache.contains(pkg);
    }

    public void toggle(String pkg) {
        if (!cache.remove(pkg)) cache.add(pkg);
        prefs.edit().putStringSet(KEY, new HashSet<>(cache)).apply();
    }
}
