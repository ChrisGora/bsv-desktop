package client.util;

public class Log {

    public static boolean verbose = false;
    public static boolean debugging = false;
    private static boolean disabled = false;

    public static void setVerbose() {
        verbose = true;
    }

    public static void setDebugging() {
        debugging = true;
    }

    public static void disable() {
        disabled = true;
    }

    public static int v(String tag, String msg) {
        if (verbose && !disabled) System.out.println("VERBOSE: " + tag + ": " + msg);
        return 0;
    }

    public static int d(String tag, String msg) {
        if (debugging && !disabled) System.out.println("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        if (!disabled) System.out.println("INFO: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        if (!disabled) System.err.println("WARN: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        if (!disabled) System.err.println("ERROR: " + tag + ": " + msg);
        return 0;
    }
}
