package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;

public class WallpaperRepository {

    private static final String PREFS = "void_wallpaper";
    private static final String KEY   = "pattern";
    public  static final int    NONE  = 0;

    private final SharedPreferences prefs;

    public WallpaperRepository(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int getPattern() { return prefs.getInt(KEY, NONE); }

    public void setPattern(int id) { prefs.edit().putInt(KEY, id).apply(); }
}
