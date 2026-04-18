package com.voidlauncher.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.voidlauncher.data.AliasRepository;

public class PluginRegistry {

    private static final String META_ALIAS = "void.plugin.alias";

    /** Llama al instalar un paquete. Si es plugin void, registra su alias. */
    public static void onInstalled(Context ctx, String pkg) {
        String alias = readAlias(ctx, pkg);
        if (alias != null) new AliasRepository(ctx).set(alias, pkg);
    }

    /** Llama al desinstalar un paquete. Limpia su alias si era plugin. */
    public static void onRemoved(Context ctx, String pkg, AliasRepository aliases) {
        String alias = aliases.aliasOf(pkg);
        if (alias != null) aliases.remove(alias);
    }

    private static String readAlias(Context ctx, String pkg) {
        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA);
            Bundle meta = ai.metaData;
            if (meta != null) return meta.getString(META_ALIAS);
        } catch (PackageManager.NameNotFoundException ignored) {}
        return null;
    }
}
