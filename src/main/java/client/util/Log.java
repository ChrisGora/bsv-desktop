package client.util;

import java.io.*;

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

    private static FileWriter fileWriter;
    private static BufferedWriter bufferedWriter;
    private static PrintWriter printWriter;
    private static boolean logToFile = false;

    public static void logToFile(String path) throws IOException {
        fileWriter = new FileWriter(path, true);
        bufferedWriter = new BufferedWriter(fileWriter);
        printWriter = new PrintWriter(bufferedWriter);
        logToFile = true;
    }

    public static void stopLoggingToFile() {
        close();
        fileWriter = null;
        bufferedWriter = null;
        printWriter = null;
        logToFile = false;
    }

    public static void close() {
        try {
            if (printWriter != null) printWriter.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (fileWriter != null) fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disable() {
        disabled = true;
    }

    public static int v(String tag, String msg) {
        if (verbose && !disabled) System.out.println("VERBOSE: " + tag + ": " + msg);
        else if (verbose && logToFile)  printWriter.println("VERBOSE: " + tag + ": " + msg);
        return 0;
    }

    public static int d(String tag, String msg) {
        if (debugging && !disabled) System.out.println("DEBUG: " + tag + ": " + msg);
        else if (debugging && logToFile) printWriter.println("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        if (!disabled) System.out.println("INFO: " + tag + ": " + msg);
        else if (logToFile) printWriter.println("INFO: " + tag + ": " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        if (!disabled) System.err.println("WARN: " + tag + ": " + msg);
        else if (logToFile) printWriter.println("WARN: " + tag + ": " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        if (!disabled) System.err.println("ERROR: " + tag + ": " + msg);
        else if (logToFile) printWriter.println("ERROR: " + tag + ": " + msg);
        return 0;
    }
}
