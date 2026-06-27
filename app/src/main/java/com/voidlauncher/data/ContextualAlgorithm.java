package com.voidlauncher.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ContextualAlgorithm {

    private static final int    TOP_N     = 15;
    private static final double W_ACTR    = 0.50;
    private static final double W_MARKOV  = 0.35;
    private static final double W_SLOT    = 0.15;
    private static final double ACT_DECAY = 0.5;

    static List<String> predict(List<LaunchRecord> records) {
        if (records.isEmpty()) return new ArrayList<>();

        long   now     = System.currentTimeMillis() / 1000L;
        String lastPkg = records.get(0).pkg;

        Map<String, List<Long>> tsByPkg    = new HashMap<>();
        Map<String, Integer>    totalCount = new HashMap<>();
        Map<String, Integer>    slotCount  = new HashMap<>();
        Map<String, Integer>    markovCnt  = new HashMap<>();
        int markovTot = 0;
        int todaySlot = slotOf(System.currentTimeMillis() / 1000L);

        for (LaunchRecord r : records) {
            // ACT-R: acumular timestamps por app
            List<Long> ts = tsByPkg.get(r.pkg);
            if (ts == null) { ts = new ArrayList<>(); tsByPkg.put(r.pkg, ts); }
            ts.add(r.ts);

            // Franja horaria: total y cuántos caen en la franja actual
            inc(totalCount, r.pkg);
            if (slotOf(r.ts) == todaySlot) inc(slotCount, r.pkg);

            // Markov: cuántas veces pkg vino después de lastPkg
            if (lastPkg.equals(r.prevPkg)) { inc(markovCnt, r.pkg); markovTot++; }
        }

        // ACT-R raw: ln(Σ delta^-0.5)
        Map<String, Double> actrRaw = new HashMap<>();
        double actrMin = Double.MAX_VALUE, actrMax = -Double.MAX_VALUE;
        for (Map.Entry<String, List<Long>> e : tsByPkg.entrySet()) {
            double bla = 0;
            for (long t : e.getValue()) bla += Math.pow(Math.max(1, now - t), -ACT_DECAY);
            double score = Math.log(bla);
            actrRaw.put(e.getKey(), score);
            if (score < actrMin) actrMin = score;
            if (score > actrMax) actrMax = score;
        }

        double actrRange = (actrMax > actrMin) ? (actrMax - actrMin) : 1.0;

        int V = tsByPkg.size();
        if (V == 0) V = 1;

        int totalLaunchesInSlot = 0;
        for (int count : slotCount.values()) {
            totalLaunchesInSlot += count;
        }

        // Score final
        List<Score> scores = new ArrayList<>();
        for (Map.Entry<String, Double> e : actrRaw.entrySet()) {
            String pkg      = e.getKey();
            double actrNorm = actrRange > 0 ? (e.getValue() - actrMin) / actrRange : 0.0;
            double slotN    = (double) (iget(slotCount, pkg) + 1) / (totalLaunchesInSlot + V);
            double mrkN     = (double) (iget(markovCnt, pkg) + 1) / (markovTot + V);
            scores.add(new Score(pkg, W_ACTR*actrNorm + W_SLOT*slotN + W_MARKOV*mrkN));
        }

        Collections.sort(scores, new Comparator<Score>() {
            @Override public int compare(Score a, Score b) { return Double.compare(b.value, a.value); }
        });

        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(TOP_N, scores.size()); i++) result.add(scores.get(i).pkg);
        return result;
    }

    // 0=madrugada 00-06 / 1=mañana 06-11 / 2=mediodía 11-14 / 3=tarde 14-19 / 4=noche 19-24
    private static int slotOf(long tsSec) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(tsSec * 1000L);
        int h = c.get(Calendar.HOUR_OF_DAY);
        if (h <  6) return 0;
        if (h < 11) return 1;
        if (h < 14) return 2;
        if (h < 19) return 3;
        return 4;
    }

    private static void inc(Map<String, Integer> map, String key) {
        Integer v = map.get(key); map.put(key, v == null ? 1 : v + 1);
    }

    private static int iget(Map<String, Integer> map, String key) {
        Integer v = map.get(key); return v == null ? 0 : v;
    }

    private static class Score {
        final String pkg; final double value;
        Score(String pkg, double value) { this.pkg = pkg; this.value = value; }
    }
}
