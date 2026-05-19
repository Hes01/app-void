package com.voidlauncher.ui;

import com.voidlauncher.data.ThemeRepository;

final class VoidTheme {

    // Campos mutables — actualizados por apply() al cambiar tema
    static int BG      = 0xFF0A0A0A;
    static int BG_CARD = 0xFF111111;
    static int FG      = 0xFFEDEDED;
    static int FG2     = 0xB8EDEDED;
    static int FG3     = 0x99EDEDED;
    static int FG4     = 0x73EDEDED;
    static int FG5     = 0x4DEDEDED;
    static int LINE    = 0x14FFFFFF;
    static int ACCENT  = 0xFFEDEDED;
    static int ERROR   = 0xFFCC4444;
    static int[] TOP_COLORS = { 0xFF888888, 0xFF4A4A4A, 0xFF363636, 0xFF282828, 0xFF1E1E1E };

    // Tamaños de texto (sp) — siempre fijos
    static final float TEXT_XS = 10f, TEXT_SM = 11f, TEXT_BASE = 12f, TEXT_MD = 13f;
    static final float TEXT_LG = 14f, TEXT_XL = 16f, TEXT_XXL  = 17f, TEXT_DISPLAY = 64f;

    // Estado actual
    static boolean isDay = false;

    static void apply(int mode) {
        isDay = (mode == ThemeRepository.DAY) || (mode == ThemeRepository.AUTO && isDaytime());
        if (isDay) {
            BG      = 0xFFF8F8F8; BG_CARD = 0xFFEEEEEE;
            FG      = 0xFF111111; FG2     = 0xB8111111;
            FG3     = 0xA6111111; FG4     = 0x7A111111;
            FG5     = 0x4D111111; LINE    = 0x14000000;
            ACCENT  = 0xFF111111;
            TOP_COLORS = new int[]{ 0xFF333333, 0xFF555555, 0xFF777777, 0xFF999999, 0xFFBBBBBB };
        } else {
            BG      = 0xFF0A0A0A; BG_CARD = 0xFF111111;
            FG      = 0xFFEDEDED; FG2     = 0xB8EDEDED;
            FG3     = 0x99EDEDED; FG4     = 0x73EDEDED;
            FG5     = 0x4DEDEDED; LINE    = 0x14FFFFFF;
            ACCENT  = 0xFFEDEDED;
            TOP_COLORS = new int[]{ 0xFF888888, 0xFF4A4A4A, 0xFF363636, 0xFF282828, 0xFF1E1E1E };
        }
    }

    static boolean isDaytime() {
        int h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        return h >= 6 && h < 20;
    }

    private VoidTheme() {}
}
