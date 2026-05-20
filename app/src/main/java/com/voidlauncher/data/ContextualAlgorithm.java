package com.voidlauncher.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ContextualAlgorithm {

    private static final int    TOP_N     = 5;
    private static final double W_ACTR    = 0.50;
    private static final double W_MARKOV  = 0.35;
    private static final double W_DOW     = 0.15;
    private static final double ACT_DECAY = 0.5;

    static List<String> predict(List<LaunchRecord> records) {
        if (records.isEmpty()) return new ArrayList<>();

        long   now      = System.currentTimeMillis() / 1000L;
        int    todayDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        String lastPkg  = records.get(0).pkg;

        Map<String, List<Long>> tsByPkg    = new HashMap<>();
        Map<String, Integer>    totalCount = new HashMap<>();
        Map<String, Integer>    dowCount   = new HashMap<>();
        Map<String, Integer>    markovCnt  = new HashMap<>();
        int markovTot = 0;

        for (LaunchRecord r : records) {
            // ACT-R: acumular timestamps por app
            List<Long> ts = tsByPkg.get(r.pkg);
            if (ts == null) { ts = new ArrayList<>(); tsByPkg.put(r.pkg, ts); }
            ts.add(r.ts);

            // DoW: total y cuántos caen en el día de hoy
            inc(totalCount, r.pkg);
            if (dowOf(r.ts) == todayDow) inc(dowCount, r.pkg);

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

        // Score final
        List<Score> scores = new ArrayList<>();
        for (Map.Entry<String, Double> e : actrRaw.entrySet()) {
            String pkg      = e.getKey();
            double actrNorm = (e.getValue() - actrMin) / actrRange;
            int    tot      = iget(totalCount, pkg);
            double dowN     = tot > 0 ? (double) iget(dowCount, pkg) / tot : 0.0;
            double mrkN     = markovTot > 0 ? (double) iget(markovCnt, pkg) / markovTot : 0.0;
            scores.add(new Score(pkg, W_ACTR*actrNorm + W_DOW*dowN + W_MARKOV*mrkN));
        }

        Collections.sort(scores, new Comparator<Score>() {
            @Override public int compare(Score a, Score b) { return Double.compare(b.value, a.value); }
        });

        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(TOP_N, scores.size()); i++) result.add(scores.get(i).pkg);
        return result;
    }

    private static int dowOf(long tsSec) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(tsSec * 1000L);
        return c.get(Calendar.DAY_OF_WEEK);
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
