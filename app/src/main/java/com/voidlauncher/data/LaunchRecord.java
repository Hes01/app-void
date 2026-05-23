package com.voidlauncher.data;

class LaunchRecord {
    final String pkg;
    final long   ts;      // Unix epoch segundos
    final String prevPkg; // nullable — app abierta justo antes

    LaunchRecord(String pkg, long ts, String prevPkg) {
        this.pkg     = pkg;
        this.ts      = ts;
        this.prevPkg = prevPkg;
    }
}
