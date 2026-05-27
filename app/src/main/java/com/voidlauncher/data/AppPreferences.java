package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    private static final String PREFS = "void_config";

    public static final String KEY_CLOCK_MODE     = "clock_mode";
    public static final String KEY_AUTO_LAUNCH    = "auto_launch";
    public static final String KEY_CONTEXTUAL     = "contextual";
    public static final String KEY_VIBRATION      = "vibration";
    public static final String KEY_SHOW_REAL_NAME = "show_real_name";

    public static SharedPreferences get(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
