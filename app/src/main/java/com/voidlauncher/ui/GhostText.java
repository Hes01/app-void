package com.voidlauncher.ui;


class GhostText {

    private static final String[] COMMANDS = {".all", ".svel"};

    static String compute(String query) {
        if (!query.startsWith(".")) return "";
        for (String cmd : COMMANDS)
            if (cmd.startsWith(query) && !cmd.equals(query))
                return cmd.substring(query.length());
        return "";
    }
}
