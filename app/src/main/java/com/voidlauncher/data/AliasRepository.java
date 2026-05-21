package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliasRepository {

    private static final String PREFS = "void_aliases";
    private final SharedPreferences prefs;
    private final Map<String, String> reverseCache = new HashMap<>();

    public AliasRepository(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet())
            reverseCache.put((String) e.getValue(), e.getKey());
    }

    public void set(String alias, String pkg) {
        String key = alias.toLowerCase().trim();
        reverseCache.put(pkg, key);
        prefs.edit().putString(key, pkg).apply();
    }

    public void remove(String alias) {
        String key = alias.toLowerCase().trim();
        String pkg = prefs.getString(key, null);
        if (pkg != null) reverseCache.remove(pkg);
        prefs.edit().remove(key).apply();
    }

    /** alias → packageName, null si no existe */
    public String resolve(String alias) {
        return prefs.getString(alias.toLowerCase().trim(), null);
    }

    /** packageName → alias, null si no tiene */
    public String aliasOf(String pkg) {
        return reverseCache.get(pkg);
    }

    public Map<String, ?> getAll() { return prefs.getAll(); }

    public void cleanOrphans(List<String> installedPackages) {
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            if (!installedPackages.contains(e.getValue())) {
                editor.remove(e.getKey());
                reverseCache.remove(e.getValue());
            }
        }
        editor.apply();
    }
}
