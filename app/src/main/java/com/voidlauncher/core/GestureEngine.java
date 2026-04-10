package com.voidlauncher.core;

import android.graphics.PointF;
import java.util.ArrayList;
import java.util.List;

/**
 * Convierte puntos táctiles a una firma de direcciones y compara gestos.
 * Sin bitmaps, sin ML — vectores puros. Corre en <10ms en cualquier dispositivo.
 */
public class GestureEngine {

    // Movimiento mínimo (px) antes de registrar una dirección
    private static final float SEGMENT_PX = 20f;
    // Errores de dirección permitidos al comparar
    private static final int TOLERANCE = 1;

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
                if (dirs.isEmpty() || dirs.get(dirs.size() - 1) != dir) {
                    dirs.add(dir);
                }
                accumX = 0;
                accumY = 0;
            }
        }

        int[] sig = new int[dirs.size()];
        for (int i = 0; i < dirs.size(); i++) sig[i] = dirs.get(i);
        return sig;
    }

    public boolean matches(int[] stored, int[] drawn) {
        return matchDirectional(stored, drawn) || matchDirectional(stored, reverse(drawn));
    }

    private boolean matchDirectional(int[] stored, int[] drawn) {
        if (stored.length == 0 || drawn.length == 0) return false;
        // Longitud debe coincidir exactamente — segmentos distintos = gesto distinto
        if (stored.length != drawn.length) return false;

        for (int i = 0; i < stored.length; i++) {
            int diff = Math.abs(stored[i] - drawn[i]);
            // Solo se permite desviación de 1 posición (ruido del dedo)
            // diff=7 es el wrap-around del círculo (0 y 7 son adyacentes)
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
        // -dy: el eje Y de pantalla está invertido respecto al matemático
        double angle = Math.toDegrees(Math.atan2(-dy, dx));
        if (angle < 0) angle += 360;
        return (int) Math.round(angle / 45.0) % 8;
    }

    private float length(float dx, float dy) {
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
