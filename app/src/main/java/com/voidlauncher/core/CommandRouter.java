package com.voidlauncher.core;

public class CommandRouter {

    public static final String EXTRA_ARGS     = "void.extra.args";
    public static final String FLAG_UNINSTALL = "-d";
    public static final String FLAG_LIST      = "-l";

    public final String alias;
    public final String flag;
    public final String args;

    private CommandRouter(String alias, String flag, String args) {
        this.alias = alias;
        this.flag  = flag;
        this.args  = args;
    }

    public static CommandRouter parse(String query) {
        String[] parts = query.trim().split("\\s+", 2);
        String alias = parts[0].toLowerCase();
        if (parts.length == 1) return new CommandRouter(alias, null, null);
        String rest = parts[1].trim();
        if (rest.startsWith("-")) {
            String[] fp = rest.split("\\s+", 2);
            String argPart = fp.length > 1 ? unquote(fp[1]) : null;
            return new CommandRouter(alias, fp[0], argPart);
        }
        return new CommandRouter(alias, null, unquote(rest));
    }

    private static String unquote(String s) {
        if (s == null || s.length() < 2) return s;
        char f = s.charAt(0), l = s.charAt(s.length() - 1);
        if ((f == '"' && l == '"') || (f == '\'' && l == '\''))
            return s.substring(1, s.length() - 1);
        return s;
    }

    public boolean isUninstall()  { return FLAG_UNINSTALL.equals(flag); }
    public boolean isList() {
        return FLAG_LIST.equals(flag) || (flag == null && "l".equalsIgnoreCase(args));
    }
    public boolean isDeleteItem() {
        if (flag != null || args == null) return false;
        String[] p = args.trim().split("[^a-zA-Z0-9]+", 2);
        return p.length == 2 && p[0].equalsIgnoreCase("del");
    }
    public String deleteId() {
        if (args == null) return null;
        String[] p = args.trim().split("[^a-zA-Z0-9]+", 2);
        return p.length == 2 ? p[1] : null;
    }

    /** Args crudos para reenviar al plugin (flag incluido si no es -d ni -l) */
    public String rawArgs() {
        if (flag == null) return args;
        if (isUninstall() || isList()) return args;
        return args != null ? flag + " " + args : flag;
    }
}
