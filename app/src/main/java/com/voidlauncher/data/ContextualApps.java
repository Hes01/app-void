package com.voidlauncher.data;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextualApps {

    private static final String PREFS   = "void_contextual";
    private static final String KEY     = "events";
    private static final String SEP     = "|";
    private static final String DELIM   = ":";
    private static final int    WINDOW  = 90;   // minutos a cada lado
    private static final int    MAX_LOG = 500;  // límite de eventos guardados
    private static final int    TOP     = 5;

    private final Context            ctx;
    private final SharedPreferences  prefs;

    public ContextualApps(Context context) {
        ctx   = context;
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Registra un lanzamiento con la hora actual. */
    public void record(String pkg) {
        int minuteOfDay = minuteOfDay();
        List<String> events = load();
        events.add(pkg + DELIM + minuteOfDay);
        if (events.size() > MAX_LOG) events.remove(0);
        save(events);
    }

    /**
     * Devuelve hasta TOP packages ordenados por frecuencia en la ventana
     * horaria actual (±90min). Si no hay datos propios, usa UsageStatsManager
     * como semilla.
     */
    public List<String> getTop(String[] allPkgs) {
        List<String> events = load();
        if (events.isEmpty()) return seedFromUsageStats(allPkgs);

        int now = minuteOfDay();
        Map<String, Integer> scores = new HashMap<>();

        for (String event : events) {
            int sep = event.lastIndexOf(DELIM);
            if (sep < 0) continue;
            String pkg = event.substring(0, sep);
            int minute;
            try { minute = Integer.parseInt(event.substring(sep + 1)); }
            catch (NumberFormatException e) { continue; }

            if (inWindow(minute, now)) {
                Integer prev = scores.get(pkg);
                scores.put(pkg, prev == null ? 1 : prev + 1);
            }
        }

        if (scores.isEmpty()) return seedFromUsageStats(allPkgs);

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(scores.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<String, Integer>>() {
            @Override public int compare(Map.Entry<String, Integer> a,
                                         Map.Entry<String, Integer> b) {
                return b.getValue() - a.getValue();
            }
        });

        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : sorted) {
            if (result.size() >= TOP) break;
            result.add(e.getKey());
        }
        return result;
    }

    // ── Ventana horaria circular (medianoche no rompe) ──────────────────────

    private boolean inWindow(int minute, int now) {
        int lo = (now - WINDOW + 1440) % 1440;
        int hi = (now + WINDOW) % 1440;
        if (lo <= hi) return minute >= lo && minute <= hi;
        return minute >= lo || minute <= hi;  // cruza medianoche
    }

    private int minuteOfDay() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }

    // ── Semilla inicial con UsageStatsManager ───────────────────────────────

    private List<String> seedFromUsageStats(String[] allPkgs) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            return new ArrayList<>();
        }
        try {
            UsageStatsManager usm = (UsageStatsManager)
                    ctx.getSystemService(Context.USAGE_STATS_SERVICE);
            long now  = System.currentTimeMillis();
            long week = now - 7L * 24 * 60 * 60 * 1000;
            List<UsageStats> stats = usm.queryUsageStats(
                    UsageStatsManager.INTERVAL_WEEKLY, week, now);
            if (stats == null || stats.isEmpty()) return new ArrayList<>();

            Collections.sort(stats, new Comparator<UsageStats>() {
                @Override public int compare(UsageStats a, UsageStats b) {
                    return Long.compare(b.getTotalTimeInForeground(),
                                        a.getTotalTimeInForeground());
                }
            });

            List<String> result = new ArrayList<>();
            for (UsageStats s : stats) {
                if (result.size() >= TOP) break;
                String pkg = s.getPackageName();
                if (isInList(pkg, allPkgs)) result.add(pkg);
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private boolean isInList(String pkg, String[] allPkgs) {
        for (String p : allPkgs) if (p.equals(pkg)) return true;
        return false;
    }

    // ── Persistencia ────────────────────────────────────────────────────────

    private List<String> load() {
        List<String> list = new ArrayList<>();
        String raw = prefs.getString(KEY, "");
        if (raw.isEmpty()) return list;
        for (String s : raw.split("\\" + SEP)) {
            if (!s.isEmpty()) list.add(s);
        }
        return list;
    }

    private void save(List<String> events) {
        StringBuilder sb = new StringBuilder();
        for (String s : events) sb.append(s).append(SEP);
        prefs.edit().putString(KEY, sb.toString()).apply();
    }
}
