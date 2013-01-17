package cn.common;

public class Logger {
    public static void message(Object o) {
        System.out.println(o);
    }

    public static void log(Object o) {
        System.out.println("[ L O G ] " + o);
    }

    public static void info(Object o) {
        System.out.println("[I N F O] " + o);
    }

    public static void debug(Object o) {
        System.out.println("[ DEBUG ] " + o);
    }

    public static void warning(Object o) {
        System.out.println("[WARNING] " + o);
    }

    public static void error(Object o) {
        System.out.println("[ ERROR ] " + o);
    }

    public static void raw(Object o) {
        System.out.print(o);
    }
}
