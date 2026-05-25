package com.voidlauncher.ui;

import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;
import android.view.WindowManager;
import java.util.List;

final class ImeInsets {

    private ImeInsets() {}

    /**
     * Configura el diálogo para que su contenido suba correctamente cuando
     * aparece el teclado, en cualquier versión de Android.
     *
     * @param window ventana del diálogo (dialog.getWindow())
     * @param root   vista raíz cuyo paddingBottom se ajustará
     */
    static void attach(android.view.Window window, View root) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

            // Estado estático: teclado ya visible o ya oculto al recibir insets.
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                v.setPadding(0, 0, 0, bottom(insets));
                return insets;
            });

            // Animación: se llama en cada frame mientras el teclado sube/baja.
            // Garantiza que el layout siga al teclado en tiempo real, incluyendo
            // dispositivos donde ime().bottom excluye la barra de navegación.
            root.setWindowInsetsAnimationCallback(new WindowInsetsAnimation.Callback(
                    WindowInsetsAnimation.Callback.DISPATCH_MODE_STOP) {
                @Override
                public WindowInsets onProgress(WindowInsets insets,
                        List<WindowInsetsAnimation> running) {
                    root.setPadding(0, 0, 0, bottom(insets));
                    return insets;
                }
            });
        } else {
            // Android < 11: ADJUST_RESIZE funciona correctamente.
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    /**
     * Calcula el padding inferior necesario.
     * Math.max cubre el caso donde ime() ya incluye la nav bar (estándar AOSP)
     * y también el caso donde la devuelve por separado (algunos OEM).
     */
    private static int bottom(WindowInsets insets) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return 0;
        int ime = insets.getInsets(WindowInsets.Type.ime()).bottom;
        int nav = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
        return Math.max(ime, nav);
    }
}
