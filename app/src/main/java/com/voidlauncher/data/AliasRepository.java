package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliasRepository {

    private static final String PREFS = "void_aliases";
    private final SharedPreferences prefs;

    public AliasRepository(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void set(String alias, String pkg) {
        prefs.edit().putString(alias.toLowerCase().trim(), pkg).apply();
    }

    public void remove(String alias) {
        prefs.edit().remove(alias.toLowerCase().trim()).apply();
    }

    /** alias → packageName, null si no existe */
    public String resolve(String alias) {
        return prefs.getString(alias.toLowerCase().trim(), null);
    }

    /** packageName → alias, null si no tiene */
    public String aliasOf(String pkg) {
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            if (pkg.equals(e.getValue())) return e.getKey();
        }
        return null;
    }

    public Map<String, String> getAll() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            result.put(e.getKey(), String.valueOf(e.getValue()));
        }
        return result;
    }

    public void cleanOrphans(List<String> installedPackages) {
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            if (!installedPackages.contains(e.getValue())) editor.remove(e.getKey());
        }
        editor.apply();
    }
}
