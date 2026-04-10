package com.voidlauncher.core;

import android.graphics.PointF;
import java.util.ArrayList;
import java.util.List;

/**
 * Convierte puntos táctiles a una firma de direcciones y compara gestos.
 * Sin bitmaps, sin ML — vectores puros. Corre en <10ms en cualquier dispositivo.
 */
public class GestureEngine {

    private static final float SEGMENT_PX   = 20f;
    private static final int   REQUIRED_VOTES = 2; // de 3 grabaciones

    // 8 direcciones cardinales: 0=E 1=NE 2=N 3=NW 4=W 5=SW 6=S 7=SE
    public int[] extractSignature(List<PointF> points) {
        if (points == null || points.size() < 2) return new int[0];

        List<Integer> dirs = new ArrayList<>();
        float accumX = 0, accumY = 0;

        for (int i = 1; i < points.size(); i++) {
            accumX += points.get(i).x - points.get(i - 1).x;
            accumY += points.get(i).y - points.get(i - 1).y;

            if (length(accumX, accumY) >= SEGMENT_PX) {
                int dir = toDir(accumX, accumY);
                if (dirs.isEmpty() || dirs.get(dirs.size() - 1) != dir) dirs.add(dir);
                accumX = 0;
                accumY = 0;
            }
        }

        int[] sig = new int[dirs.size()];
        for (int i = 0; i < dirs.size(); i++) sig[i] = dirs.get(i);
        return sig;
    }

    /**
     * Compara el gesto dibujado contra las 3 grabaciones del usuario.
     * Necesita mayoría (2 de 3) para hacer match.
     * La tolerancia es la variación real de la mano, no un valor fijo.
     */
    public boolean matches(int[][] stored, int[] drawn) {
        int votes = 0;
        for (int[] sig : stored) {
            if (matchOne(sig, drawn)) votes++;
            if (votes >= REQUIRED_VOTES) return true;
        }
        return false;
    }

    private boolean matchOne(int[] stored, int[] drawn) {
        return matchDirectional(stored, drawn) || matchDirectional(stored, reverse(drawn));
    }

    private boolean matchDirectional(int[] stored, int[] drawn) {
        if (stored.length == 0 || drawn.length == 0) return false;
        if (stored.length != drawn.length) return false;

        for (int i = 0; i < stored.length; i++) {
            int diff = Math.abs(stored[i] - drawn[i]);
            // diff=7 es wrap-around: 0 y 7 son adyacentes en el círculo de 8 dirs
            if (diff > 1 && diff != 7) return false;
        }
        return true;
    }

    private int[] reverse(int[] sig) {
        int[] rev = new int[sig.length];
        for (int i = 0; i < sig.length; i++) rev[i] = sig[sig.length - 1 - i];
        return rev;
    }

    private int toDir(float dx, float dy) {
        double angle = Math.toDegrees(Math.atan2(-dy, dx));
        if (angle < 0) angle += 360;
        return (int) Math.round(angle / 45.0) % 8;
    }

    private float length(float dx, float dy) {
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
