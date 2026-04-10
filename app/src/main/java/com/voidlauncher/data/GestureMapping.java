package com.voidlauncher.data;

public class GestureMapping {

    public final String id;
    public final String appPackage;
    public final String appName;
    public final int[]  signature;

    public GestureMapping(String id, String appPackage, String appName, int[] signature) {
        this.id         = id;
        this.appPackage = appPackage;
        this.appName    = appName;
        this.signature  = signature;
    }
}
