package com.voidlauncher.ui;

final class VoidTheme {

    // --- Fondo ---
    static final int BG      = 0xFF0A0A0A;
    static final int BG_CARD = 0xFF111111;

    // --- Texto — jerarquía de opacidad sobre base #EDEDED ---
    static final int FG      = 0xFFEDEDED; // 100% — texto principal
    static final int FG2     = 0xB8EDEDED; //  72% — secundario
    static final int FG3     = 0x7AEDEDED; //  48% — dim
    static final int FG4     = 0x4DEDEDED; //  30% — muted
    static final int FG5     = 0x2EEDEDED; //  18% — ghost

    // --- Separadores / bordes ---
    static final int LINE    = 0x14FFFFFF; //   8% — hairline

    // --- Acento (configurable a futuro) ---
    static final int ACCENT  = 0xFFEDEDED;

    // --- Estado ---
    static final int ERROR   = 0xFFCC4444; // prompt "?" sin resultados

    // --- Degradado contextual top-5 (rank 1 más visible → rank 5 casi invisible) ---
    static final int[] TOP_COLORS = {
        0xFF888888,  // rank 1
        0xFF4A4A4A,  // rank 2
        0xFF363636,  // rank 3
        0xFF282828,  // rank 4
        0xFF1E1E1E,  // rank 5
    };

    // --- Tamaños de texto (sp) ---
    static final float TEXT_XS      = 10f;
    static final float TEXT_SM      = 11f;
    static final float TEXT_BASE    = 12f;
    static final float TEXT_MD      = 13f;
    static final float TEXT_LG      = 14f;
    static final float TEXT_XL      = 16f;
    static final float TEXT_XXL     = 17f;
    static final float TEXT_DISPLAY = 64f; // reloj — no tocar tamaño

    private VoidTheme() {}
}
