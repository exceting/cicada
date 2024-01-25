package io.github.exceting.cicada.common.logging;

public class LogFormat {

    public static String error(String msg, Object... args) {
        return String.format("%s %s", LogPrefix.CICADA_ERROR, String.format(msg, args));
    }

    public static String warn(String msg, Object... args) {
        return String.format("%s %s", LogPrefix.CICADA_WARN, String.format(msg, args));
    }

    public static String info(String msg, Object... args) {
        return String.format("%s %s", LogPrefix.CICADA_INFO, String.format(msg, args));
    }

    public static String debug(String msg, Object... args) {
        return String.format("%s %s", LogPrefix.CICADA_DEBUG, String.format(msg, args));
    }

    public static String trace(String msg, Object... args) {
        return String.format("%s %s", LogPrefix.CICADA_TRACE, String.format(msg, args));
    }
}
