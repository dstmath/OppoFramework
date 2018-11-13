package com.oppo.statistics.util;

import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;

public class LogUtil {
    private static boolean D = false;
    private static boolean E = true;
    private static boolean I = false;
    public static final String TAG = "com.coloros.statistics--";
    private static boolean V = false;
    private static boolean W = true;
    private static boolean isDebug = false;
    public static boolean isDebugMode = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static String seprateor = "-->";
    private static String special = "NearmeStatistics-local";

    public static void e(String tag, Throwable e) {
        if (isDebug && E) {
            Log.e(tag, e.toString());
        }
    }

    public static void e(Exception e) {
        if (isDebug && E) {
            e.printStackTrace();
        }
    }

    public static void v(String tag, String debugInfo) {
        if (isDebug && V) {
            Log.v(tag, special + seprateor + debugInfo);
        }
    }

    public static void d(String tag, String debugInfo) {
        if (isDebug && D) {
            Log.d(tag, special + seprateor + debugInfo);
        }
    }

    public static void i(String tag, String debugInfo) {
        if (isDebug && I) {
            Log.i(tag, special + seprateor + debugInfo);
        }
    }

    public static void w(String tag, String debugInfo) {
        if (isDebug && W) {
            Log.w(tag, special + seprateor + debugInfo);
        }
    }

    public static void e(String tag, String debugInfo) {
        if (isDebug && E) {
            Log.e(tag, special + seprateor + debugInfo);
        }
    }

    public static void v(String debugInfo) {
        if (isDebug && V) {
            Log.v(TAG, special + seprateor + debugInfo);
        }
    }

    public static void d(String debugInfo) {
        if (isDebug && D) {
            Log.d(TAG, special + seprateor + debugInfo);
        }
    }

    public static void i(String debugInfo) {
        if (isDebug && I) {
            Log.i(TAG, special + seprateor + debugInfo);
        }
    }

    public static void w(String debugInfo) {
        if (isDebug && W) {
            Log.w(TAG, special + seprateor + debugInfo);
        }
    }

    public static void e(String debugInfo) {
        if (isDebug && E) {
            Log.e(TAG, special + seprateor + debugInfo);
        }
    }

    public static String getSpecial() {
        return special;
    }

    public static void setSpecial(String special) {
        special = special;
    }

    public static boolean isV() {
        return V;
    }

    public static void setV(boolean v) {
        V = v;
    }

    public static boolean isD() {
        return D;
    }

    public static void setD(boolean d) {
        D = d;
    }

    public static boolean isI() {
        return I;
    }

    public static void setI(boolean i) {
        I = i;
    }

    public static boolean isW() {
        return W;
    }

    public static void setW(boolean w) {
        W = w;
    }

    public static boolean isE() {
        return E;
    }

    public static void setE(boolean e) {
        E = e;
    }

    public static void setDebugs(boolean b) {
        isDebug = b;
        if (isDebug && isDebugMode) {
            V = true;
            D = true;
            I = true;
            W = true;
            E = true;
            return;
        }
        V = false;
        D = false;
        I = false;
        W = false;
        E = false;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static String getSeprateor() {
        return seprateor;
    }

    public static void setSeprateor(String seprateor) {
        seprateor = seprateor;
    }

    public static void reocrdExceptionInfo(Throwable e) {
        File file = new File("/data/data/com.nearme.statistics.rom/exception_info.txt");
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null) {
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    file.createNewFile();
                } else {
                    return;
                }
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
            StringWriter strWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(strWriter));
            raf.write(strWriter.toString().getBytes());
            raf.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
