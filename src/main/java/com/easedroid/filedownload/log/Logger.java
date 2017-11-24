package com.easedroid.filedownload.log;

public class Logger {

    private static ILog systemLog = new SystemLog();

    private static ILog getOut() {
        return systemLog;
    }

    public static void d(String tag, String msg) {
        getOut().d(tag, msg);
    }

    public static void e(String tag, String msg) {
        getOut().e(tag, msg);
    }

    public static void i(String tag, String msg) {
        getOut().i(tag, msg);
    }

    public static void wtf(String tag, String msg) {
        getOut().wtf(tag, msg);
    }

}
