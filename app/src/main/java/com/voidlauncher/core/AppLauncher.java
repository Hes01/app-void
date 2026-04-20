package com.voidlauncher.core;

import android.content.Context;
import android.content.Intent;

public class AppLauncher {

    public static boolean launch(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) return false;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return true;
    }

}
