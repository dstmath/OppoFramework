package com.mediatek.anr;

import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.util.Printer;
import java.util.HashMap;

public final class AnrAppManagerImpl extends AnrAppManager {
    private static final String TAG = "AnrAppManager";
    private static Object lock = new Object();
    private static AnrAppManagerImpl sInstance = null;
    protected static HashMap<String, MessageLogger> sMap = new HashMap<>();
    private static MessageLogger sSingletonLogger = null;

    public static AnrAppManagerImpl getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    sInstance = new AnrAppManagerImpl();
                }
            }
        }
        return sInstance;
    }

    public void setMessageLogger(Looper looper) {
        if ("eng".equals(Build.TYPE)) {
            looper.setMessageLogging(newMessageLogger(false));
        }
    }

    public void dumpMessage(boolean dumpAll) {
        if (dumpAll) {
            dumpAllMessageHistory();
        } else {
            dumpMessageHistory();
        }
    }

    public static Printer newMessageLogger(boolean mValue) {
        sSingletonLogger = new MessageLogger(mValue);
        return sSingletonLogger;
    }

    public static Printer newMessageLogger(boolean mValue, String name) {
        if (sMap.containsKey(name)) {
            sMap.remove(name);
        }
        MessageLogger logger = new MessageLogger(mValue, name);
        sMap.put(name, logger);
        return logger;
    }

    public static void dumpMessageHistory() {
        MessageLogger messageLogger = sSingletonLogger;
        if (messageLogger == null) {
            Log.i(TAG, "!!! It is not under singleton mode, U can't use it. !!!\n");
            try {
                AnrManagerNative.getDefault().informMessageDump("!!! It is not under singleton mode, U can't use it. !!!\n", Process.myPid());
            } catch (RemoteException ex) {
                Log.i(TAG, "informMessageDump exception " + ex);
            }
        } else {
            messageLogger.dumpMessageHistory();
        }
    }

    public static void dumpAllMessageHistory() {
        if (sSingletonLogger != null) {
            Log.i(TAG, "!!! It is under multiple instance mode, but you are in singleton usage style. !!!\n");
            try {
                AnrManagerNative.getDefault().informMessageDump("!!! It is under multiple instance mode,but you are in singleton usage style. !!!\n", Process.myPid());
            } catch (RemoteException ex) {
                Log.i(TAG, "informMessageDump exception " + ex);
            }
        } else {
            HashMap<String, MessageLogger> hashMap = sMap;
            if (hashMap == null) {
                Log.i(TAG, String.format("!!! DumpAll, sMap is null\n", new Object[0]));
                try {
                    AnrManagerNative.getDefault().informMessageDump("!!! DumpAll, sMap is null\n", Process.myPid());
                } catch (RemoteException ex2) {
                    Log.i(TAG, "informMessageDump exception " + ex2);
                }
            } else {
                for (String tmp_str : hashMap.keySet()) {
                    Log.i(TAG, String.format(">>> DumpByName, Thread name: %s dump is starting <<<\n", tmp_str));
                    sMap.get(tmp_str).setInitStr(String.format(">>> DumpByName, Thread name: %s dump is starting <<<\n", tmp_str));
                    sMap.get(tmp_str).dumpMessageHistory();
                }
            }
        }
    }
}
