package com.investigatorsapp.logger;

import android.util.Log;

/**
 * Created by fenglei on 15-7-16.
 */
public class Logger {

    private static int LOG_LEVEL = Log.VERBOSE;

    private static boolean DEBUG = true;

    public static void setLogLevel(int logLevel) {
        LOG_LEVEL = logLevel;
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    public static void v(String tag, String msg) {
        if(LOG_LEVEL <= Log.VERBOSE && DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if(LOG_LEVEL <= Log.VERBOSE && DEBUG) {
            Log.v(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if(LOG_LEVEL <= Log.DEBUG && DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if(LOG_LEVEL <= Log.DEBUG && DEBUG) {
            Log.d(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        if(LOG_LEVEL <= Log.INFO && DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if(LOG_LEVEL <= Log.INFO && DEBUG) {
            Log.i(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg) {
        if(LOG_LEVEL <= Log.WARN && DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if(LOG_LEVEL <= Log.WARN && DEBUG) {
            Log.w(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg) {
        if(LOG_LEVEL <= Log.ERROR && DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if(LOG_LEVEL <= Log.ERROR && DEBUG) {
            Log.e(tag, msg, tr);
        }
    }

}
