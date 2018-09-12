package client.util;

public class Log {

    public static boolean verbose = false;
    public static boolean debugging = false;

    public static void setVerbose() {
        verbose = true;
    }

    public static void setDebugging() {
        debugging = true;
    }

    public static int v(String tag, String msg) {
        if (verbose) System.out.println("VERBOSE: " + tag + ": " + msg);
        return 0;
    }

    public static int d(String tag, String msg) {
        if (debugging) System.out.println("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        System.out.println("INFO: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        System.err.println("WARN: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        System.err.println("ERROR: " + tag + ": " + msg);
        return 0;
    }
}
