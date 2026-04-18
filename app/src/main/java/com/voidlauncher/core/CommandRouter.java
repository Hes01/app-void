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

    /** Parsea "nn -l", "nn titulo", "nn -dn 1", "fb -d", "fb" */
    public static CommandRouter parse(String query) {
        String[] parts = query.trim().split("\\s+", 2);
        String alias = parts[0].toLowerCase();
        if (parts.length == 1) return new CommandRouter(alias, null, null);
        String rest = parts[1].trim();
        if (rest.startsWith("-")) {
            String[] fp = rest.split("\\s+", 2);
            return new CommandRouter(alias, fp[0], fp.length > 1 ? fp[1] : null);
        }
        return new CommandRouter(alias, null, rest);
    }

    public boolean isUninstall() { return FLAG_UNINSTALL.equals(flag); }
    public boolean isList()      { return FLAG_LIST.equals(flag); }

    /** Args crudos para reenviar al plugin (flag incluido si no es -d ni -l) */
    public String rawArgs() {
        if (flag == null) return args;
        if (isUninstall() || isList()) return args;
        return args != null ? flag + " " + args : flag;
    }
}
