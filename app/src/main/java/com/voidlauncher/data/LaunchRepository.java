package com.voidlauncher.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LaunchRepository {

    private static final int QUERY_LIMIT = 500;
    private static final int MAX_RECORDS = 5000;

    private final VoidDatabase    db;
    private final Handler         main     = new Handler(Looper.getMainLooper());
    private final ExecutorService writeExec = Executors.newSingleThreadExecutor();

    public LaunchRepository(Context ctx) {
        db = VoidDatabase.get(ctx);
    }

    /** Registra un lanzamiento. Llamar desde cualquier hilo. */
    public void record(final String pkg) {
        writeExec.execute(() -> {
            SQLiteDatabase w = db.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(VoidDatabase.COL_PKG,  pkg);
            cv.put(VoidDatabase.COL_TS,   System.currentTimeMillis() / 1000L);
            cv.put(VoidDatabase.COL_PREV, lastPackage(w));
            w.insert(VoidDatabase.TABLE, null, cv);
            rotate(w);
        });
    }

    /** Predice top apps en background, entrega resultado en el hilo principal. */
    public void getTop(final Callback callback) {
        new Thread(() -> {
            final List<String> result = ContextualAlgorithm.predict(loadRecent());
            main.post(() -> callback.onResult(result));
        }).start();
    }

    public interface Callback {
        void onResult(List<String> top);
    }

    // ── privados ──────────────────────────────────────────────────────────────

    private String lastPackage(SQLiteDatabase r) {
        Cursor c = r.rawQuery(
            "SELECT " + VoidDatabase.COL_PKG +
            " FROM "  + VoidDatabase.TABLE   +
            " ORDER BY " + VoidDatabase.COL_TS + " DESC LIMIT 1", null);
        try { return c.moveToFirst() ? c.getString(0) : null; }
        finally { c.close(); }
    }

    private List<LaunchRecord> loadRecent() {
        SQLiteDatabase r = db.getReadableDatabase();
        Cursor c = r.rawQuery(
            "SELECT " + VoidDatabase.COL_PKG  + "," +
                        VoidDatabase.COL_TS   + "," +
                        VoidDatabase.COL_PREV +
            " FROM "  + VoidDatabase.TABLE    +
            " ORDER BY " + VoidDatabase.COL_TS + " DESC LIMIT " + QUERY_LIMIT, null);
        List<LaunchRecord> list = new ArrayList<>();
        try { while (c.moveToNext()) list.add(new LaunchRecord(c.getString(0), c.getLong(1), c.getString(2))); }
        finally { c.close(); }
        return list;
    }

    public void clearAll() {
        writeExec.execute(() -> db.getWritableDatabase().execSQL("DELETE FROM " + VoidDatabase.TABLE));
    }

    private void rotate(SQLiteDatabase w) {
        w.execSQL(
            "DELETE FROM " + VoidDatabase.TABLE +
            " WHERE "      + VoidDatabase.COL_ID + " = (SELECT MIN(" + VoidDatabase.COL_ID + ") FROM " + VoidDatabase.TABLE + ")" +
            " AND (SELECT COUNT(*) FROM " + VoidDatabase.TABLE + ") > " + MAX_RECORDS);
    }
}
