package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeRepository {

    public static final int NIGHT = 0;
    public static final int DAY   = 1;
    public static final int AUTO  = 2;

    public static final String[] LABELS = { "noche", "día", "auto" };

    private static final String PREFS = "void_theme";
    private static final String KEY   = "mode";

    private final SharedPreferences prefs;

    public ThemeRepository(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int  getMode() { return prefs.getInt(KEY, NIGHT); }
    public void setMode(int mode) { prefs.edit().putInt(KEY, mode).apply(); }
}
