package com.lchli.loaderlib.android;

import android.util.Log;

/**
 * Created by lchli on 2016/4/23.
 */
public class LogHelper {

    public static boolean isDebug = true;

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, msg);
        }
    }
}
