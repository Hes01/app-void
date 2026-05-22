package com.voidlauncher.ui;

import com.voidlauncher.data.ThemeRepository;

final class VoidTheme {

    static final String[] NAMES = {
        "pizarra", "papel & tinta", "oled", "ios", "solarized", "nord", "gruvbox", "catppuccin"
    };

    // [night: BG, BG_CARD, FG, ACCENT, ERROR | day: BG, BG_CARD, FG, ACCENT, ERROR]
    private static final int[][] THEMES = {
        { 0xFF0D1117,0xFF161B22,0xFFE6EDF3,0xFF58A6FF,0xFFF85149, 0xFFF1F4F8,0xFFE3E8EF,0xFF0D1117,0xFF0969DA,0xFFCF222E }, // pizarra
        { 0xFF1A1814,0xFF25221C,0xFFECE6D8,0xFFECE6D8,0xFFE07A5F, 0xFFF4F1EA,0xFFECE7D6,0xFF1F1C17,0xFF1F1C17,0xFFB5462E }, // papel & tinta
        { 0xFF000000,0xFF0A0A0A,0xFFE8E8E8,0xFFE8E8E8,0xFFFF5555, 0xFFFFFFFF,0xFFF4F4F4,0xFF0A0A0A,0xFF0A0A0A,0xFFCC4444 }, // oled
        { 0xFF000000,0xFF1C1C1E,0xFFFFFFFF,0xFF0A84FF,0xFFFF453A, 0xFFFFFFFF,0xFFF2F2F7,0xFF000000,0xFF007AFF,0xFFFF3B30 }, // ios
        { 0xFF002B36,0xFF073642,0xFFFDF6E3,0xFF268BD2,0xFFDC322F, 0xFFFDF6E3,0xFFEEE8D5,0xFF073642,0xFF268BD2,0xFFDC322F }, // solarized
        { 0xFF2E3440,0xFF3B4252,0xFFECEFF4,0xFF88C0D0,0xFFBF616A, 0xFFECEFF4,0xFFE5E9F0,0xFF2E3440,0xFF5E81AC,0xFFBF616A }, // nord
        { 0xFF282828,0xFF32302F,0xFFEBDBB2,0xFFFABD2F,0xFFFB4934, 0xFFFBF1C7,0xFFEBDBB2,0xFF3C3836,0xFF79740E,0xFF9D0006 }, // gruvbox
        { 0xFF1E1E2E,0xFF313244,0xFFCDD6F4,0xFFCBA6F7,0xFFF38BA8, 0xFFEFF1F5,0xFFE6E9EF,0xFF4C4F69,0xFF8839EF,0xFFD20F39 }, // catppuccin
    };

    static int BG, BG_CARD, FG, FG2, FG3, FG4, FG5, LINE, ACCENT, ERROR;
    static int[] TOP_COLORS;
    static boolean isDay = false;

    static final float TEXT_XS = 10f, TEXT_SM = 11f, TEXT_BASE = 12f, TEXT_MD = 13f;
    static final float TEXT_LG = 14f, TEXT_XL = 16f, TEXT_XXL  = 17f, TEXT_DISPLAY = 64f;

    static void apply(int themeId, int mode) {
        isDay   = (mode == ThemeRepository.DAY) || (mode == ThemeRepository.AUTO && isDaytime());
        int off = isDay ? 5 : 0;
        int[] t = THEMES[themeId];
        BG = t[off]; BG_CARD = t[off+1]; FG = t[off+2]; ACCENT = t[off+3]; ERROR = t[off+4];
        FG2 = a(FG,0xB8); FG3 = a(FG,0x99); FG4 = a(FG,0x73); FG5 = a(FG,0x4D); LINE = a(FG,0x14);
        TOP_COLORS = new int[]{ a(FG,0xCC), a(FG,0x99), a(FG,0x73), a(FG,0x52), a(FG,0x33) };
    }

    private static int a(int color, int alpha) { return (color & 0x00FFFFFF) | (alpha << 24); }

    static boolean isDaytime() {
        int h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        return h >= 6 && h < 20;
    }

    private VoidTheme() {}
}
