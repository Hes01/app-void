package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.hes01.voidlauncher.R;

public class ThemeRepository {

    public static final int NIGHT = 0;
    public static final int DAY   = 1;
    public static final int AUTO  = 2;

    private static final String PREFS     = "void_theme";
    private static final String KEY_MODE  = "mode";
    private static final String KEY_THEME = "theme";

    private final SharedPreferences prefs;

    public ThemeRepository(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int  getMode()  { return prefs.getInt(KEY_MODE,  NIGHT); }
    public void setMode(int mode) { prefs.edit().putInt(KEY_MODE,  mode).apply(); }

    public int  getTheme() { return prefs.getInt(KEY_THEME, 0); }
    public void setTheme(int id)  { prefs.edit().putInt(KEY_THEME, id).apply(); }

    private static final int[] LABEL_IDS = {R.string.theme_night, R.string.theme_day, R.string.theme_auto};
    public static String label(Context ctx, int mode) { return ctx.getString(LABEL_IDS[mode]); }
}
