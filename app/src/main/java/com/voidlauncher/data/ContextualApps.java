package com.voidlauncher.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lógica de auto-aprendizaje local. 
 * Aprende qué apps usas según la hora del día sin pedir permisos al sistema.
 */
public class ContextualApps {

    private static final String PREFS   = "void_contextual";
    private static final String KEY     = "events";
    private static final String SEP     = "|";
    private static final String DELIM   = ":";
    private static final int    WINDOW  = 90;   // ventana de ±90 minutos
    private static final int    MAX_LOG = 500;  // límite de memoria
    private static final int    TOP     = 5;

    private final SharedPreferences  prefs;

    public ContextualApps(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Registra un lanzamiento con la hora actual en el log local. */
    public void record(String pkg) {
        int minuteOfDay = minuteOfDay();
        List<String> events = load();
        events.add(pkg + DELIM + minuteOfDay);
        if (events.size() > MAX_LOG) events.remove(0);
        save(events);
    }

    /** Devuelve las apps más frecuentes en esta franja horaria. */
    public List<String> getTop() {
        List<String> events = load();
        if (events.isEmpty()) return new ArrayList<>();

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

    private boolean inWindow(int minute, int now) {
        int lo = (now - WINDOW + 1440) % 1440;
        int hi = (now + WINDOW) % 1440;
        if (lo <= hi) return minute >= lo && minute <= hi;
        return minute >= lo || minute <= hi;
    }

    private int minuteOfDay() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }

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
