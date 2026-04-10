package com.voidlauncher.data;

public class GestureMapping {

    public final String  id;
    public final String  appPackage;
    public final String  appName;
    public final int[][] signatures; // 3 grabaciones de la mano del usuario

    public GestureMapping(String id, String appPackage, String appName, int[][] signatures) {
        this.id         = id;
        this.appPackage = appPackage;
        this.appName    = appName;
        this.signatures = signatures;
    }
}
