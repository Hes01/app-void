package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class RecentApps {

    private static final String PREFS   = "void_recents";
    private static final String KEY     = "recent";
    private static final int    MAX     = 5;
    private static final String SEP     = "|";

    private final SharedPreferences prefs;

    public RecentApps(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void record(String packageName) {
        List<String> list = load();
        list.remove(packageName);          // quita si ya estaba (para subirlo al frente)
        list.add(0, packageName);
        if (list.size() > MAX) list.remove(list.size() - 1);
        save(list);
    }

    public List<String> get() {
        return load();
    }

    private List<String> load() {
        List<String> list = new ArrayList<>();
        String raw = prefs.getString(KEY, "");
        if (raw.isEmpty()) return list;
        for (String s : raw.split("\\" + SEP)) {
            if (!s.isEmpty()) list.add(s);
        }
        return list;
    }

    private void save(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s).append(SEP);
        prefs.edit().putString(KEY, sb.toString()).apply();
    }
}
