package com.voidlauncher.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class VoidDatabase extends SQLiteOpenHelper {

    static final String TABLE    = "launches";
    static final String COL_ID   = "id";
    static final String COL_PKG  = "package";
    static final String COL_TS   = "ts";
    static final String COL_PREV = "prev_pkg";

    private static final String DB_NAME    = "void_launches.db";
    private static final int    DB_VERSION = 1;

    private static VoidDatabase instance;

    static synchronized VoidDatabase get(Context ctx) {
        if (instance == null) instance = new VoidDatabase(ctx.getApplicationContext());
        return instance;
    }

    private VoidDatabase(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " (" +
            COL_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_PKG  + " TEXT NOT NULL, " +
            COL_TS   + " INTEGER NOT NULL, " +
            COL_PREV + " TEXT)"
        );
        db.execSQL("CREATE INDEX idx_ts  ON " + TABLE + "(" + COL_TS  + ")");
        db.execSQL("CREATE INDEX idx_pkg ON " + TABLE + "(" + COL_PKG + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}
