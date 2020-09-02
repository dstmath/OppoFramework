package com.oppo.debug;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.FileObserver;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class InputLog {
    public static boolean DEBUG = (LOGCAT_LEVEL <= 2);
    private static boolean ERROR = false;
    private static boolean INFO = (LOGCAT_LEVEL <= 4);
    private static final boolean IS_DEBUGING = false;
    public static final int KEY_DISPATCHING_TIMEOUT = 5000;
    private static int LOGCAT_LEVEL = 16;
    private static final String LOGSWITCH_DIR_PATH = "/data/logswitch";
    private static final String LOGSWITCH_FILE_NAME = "switch.xml";
    private static final String LOGSWITCH_FILE_PATH = "/data/logswitch/switch.xml";
    private static final int LOG_LEVEL_ALL = 0;
    private static final int LOG_LEVEL_DEBUG = 2;
    private static final int LOG_LEVEL_DEFAULT = 16;
    private static final int LOG_LEVEL_ERROR = 16;
    private static final int LOG_LEVEL_INFO = 4;
    private static final int LOG_LEVEL_VERBOSE = 0;
    private static final int LOG_LEVEL_WARN = 8;
    private static final String LOG_TAG_STRING = "InputLog";
    private static final String SWITCH_OFF_VALUE = "off";
    private static final String SWITCH_ON_VALUE = "on";
    private static boolean VERBOSE = (LOGCAT_LEVEL <= 0);
    private static boolean WARN = (LOGCAT_LEVEL <= 8);
    private static boolean sInited = false;
    private static LogSwitchObserver sLogSwitchObserver;

    static {
        boolean z = false;
        if (LOGCAT_LEVEL <= 16) {
            z = true;
        }
        ERROR = z;
        startWatching();
    }

    static class LogSwitchObserver extends FileObserver {
        public LogSwitchObserver(String path) {
            super(path, 10);
        }

        @Override // android.os.FileObserver
        public void onEvent(int event, String path) {
            InputLog.updateLogLevel();
        }
    }

    public static boolean isOpenAllLog() {
        return LOGCAT_LEVEL == 0;
    }

    private static void dumpEventType(int eventType) {
        int i = eventType & 4095;
        if (i == 1) {
            Log.d(LOG_TAG_STRING, "FileObserver.ACCESS ");
        } else if (i == 2) {
            Log.d(LOG_TAG_STRING, "FileObserver.MODIFY ");
        } else if (i == 4) {
            Log.d(LOG_TAG_STRING, "FileObserver.ATTRIB ");
        } else if (i == 8) {
            Log.d(LOG_TAG_STRING, "FileObserver.CLOSE_WRITE ");
        } else if (i == 16) {
            Log.d(LOG_TAG_STRING, "FileObserver.CLOSE_NOWRITE ");
        } else if (i == 32) {
            Log.d(LOG_TAG_STRING, "FileObserver.OPEN ");
        } else if (i == 64) {
            Log.d(LOG_TAG_STRING, "FileObserver.MOVED_FROM ");
        } else if (i == 128) {
            Log.d(LOG_TAG_STRING, "FileObserver.MOVED_TO ");
        } else if (i == 256) {
            Log.d(LOG_TAG_STRING, "FileObserver.CREATE ");
        } else if (i == 512) {
            Log.d(LOG_TAG_STRING, "FileObserver.DELETE ");
        } else if (i == 1024) {
            Log.d(LOG_TAG_STRING, "FileObserver.DELETE_SELF ");
        } else if (i == 2048) {
            Log.d(LOG_TAG_STRING, "FileObserver.MOVE_SELF ");
        }
    }

    public static void startWatching() {
        if (!sInited && checkLogSwitchDirExist() && "on".equals(getCurrentLogSwitchValue())) {
            sLogSwitchObserver = new LogSwitchObserver(LOGSWITCH_DIR_PATH);
            LogSwitchObserver logSwitchObserver = sLogSwitchObserver;
            if (logSwitchObserver != null) {
                logSwitchObserver.startWatching();
                sInited = true;
                updateLogLevel();
            }
        }
    }

    public static void restoreToDefaltLogLevel() {
        Log.d(LOG_TAG_STRING, " restoreToDefaltLogLevel");
        File file = new File(LOGSWITCH_FILE_PATH);
        if (file.exists()) {
            file.delete();
        } else {
            Log.d(LOG_TAG_STRING, file + " still not exists");
        }
    }

    public static boolean checkLogSwitchDirExist() {
        if (!new File(LOGSWITCH_DIR_PATH).exists()) {
            return false;
        }
        return true;
    }

    private static boolean changeFileAttr(String attr, String filePath) {
        String command = "chmod " + attr + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + filePath;
        Log.d(LOG_TAG_STRING, "changeFileAttr command = " + command);
        try {
            Runtime.getRuntime().exec(command);
            return true;
        } catch (IOException e) {
            Log.i(LOG_TAG_STRING, command + " exec failed !!!!");
            return false;
        }
    }

    public static boolean initLogSwitchDir() {
        Log.d(LOG_TAG_STRING, "initLogSwitchDir Begin");
        File dir = new File(LOGSWITCH_DIR_PATH);
        if (dir.exists()) {
            return true;
        }
        boolean ok = dir.mkdir();
        boolean success = true;
        Log.d(LOG_TAG_STRING, dir + "initLogSwitchDir  mkdir , ok = " + ok);
        if (ok) {
            success = changeFileAttr("777", LOGSWITCH_DIR_PATH);
        }
        if (!ok || !success) {
            return false;
        }
        return true;
    }

    public static boolean initLogSwitch() {
        if (initLogSwitchDir()) {
            return initLogSwitchFile();
        }
        return false;
    }

    public static boolean initLogSwitchFile() {
        boolean ok;
        File file;
        Log.d(LOG_TAG_STRING, " initLogSwitchFile ");
        File file2 = new File(LOGSWITCH_FILE_PATH);
        if (file2.exists()) {
            return true;
        }
        try {
            ok = file2.createNewFile();
        } catch (IOException e) {
            Log.d(LOG_TAG_STRING, file2 + " createNewFile failed in initLogSwitchFile(), " + e);
            ok = file;
        } finally {
        }
        if (!ok) {
            return false;
        }
        changeFileAttr("777", LOGSWITCH_FILE_PATH);
        return true;
    }

    public static void OpenAllLogLevel() {
        Log.d(LOG_TAG_STRING, " OpenAllLogLevel 22");
        File file = new File(LOGSWITCH_FILE_PATH);
        try {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                Log.d(LOG_TAG_STRING, file + " still exists or open failed, ");
            }
        } catch (IOException e) {
            Log.d(LOG_TAG_STRING, file + " createNewFile failed in OpenAllLogLevel(), " + e);
        } catch (Throwable th) {
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static void updateLogLevel() {
        boolean z = false;
        LOGCAT_LEVEL = "on".equals(readString(LOGSWITCH_FILE_PATH)) ? 0 : 16;
        VERBOSE = LOGCAT_LEVEL <= 0;
        DEBUG = LOGCAT_LEVEL <= 2;
        INFO = LOGCAT_LEVEL <= 4;
        WARN = LOGCAT_LEVEL <= 8;
        if (LOGCAT_LEVEL <= 16) {
            z = true;
        }
        ERROR = z;
    }

    public static String getLogLevelString() {
        return ((((("LOGCAT_LEVEL = " + LOGCAT_LEVEL) + ", VERBOSE = " + VERBOSE) + ", DEBUG = " + DEBUG) + ", INFO = " + INFO) + ", WARN = " + WARN) + ", ERROR = " + ERROR;
    }

    public static void i(String tag, String msg) {
        if (INFO) {
            Log.i(LOG_TAG_STRING, tag + " : " + msg);
        }
    }

    public static void i(String tag, String msg, Throwable error) {
        if (INFO) {
            Log.i(LOG_TAG_STRING, tag + " : " + msg, error);
        }
    }

    public static void v(String tag, String msg) {
        if (VERBOSE) {
            Log.v(LOG_TAG_STRING, tag + " : " + msg);
        }
    }

    public static void v(String tag, String msg, Throwable error) {
        if (VERBOSE) {
            Log.v(LOG_TAG_STRING, tag + " : " + msg, error);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(LOG_TAG_STRING, tag + " : " + msg);
        }
    }

    public static void d(String tag, String msg, Throwable error) {
        if (DEBUG) {
            Log.d(LOG_TAG_STRING, tag + " : " + msg, error);
        }
    }

    public static void w(String tag, String msg) {
        if (WARN) {
            Log.w(LOG_TAG_STRING, tag + " : " + msg);
        }
    }

    public static void w(String tag, String msg, Throwable error) {
        if (WARN) {
            Log.w(LOG_TAG_STRING, tag + " : " + msg, error);
        }
    }

    public static void e(String tag, String msg) {
        if (ERROR) {
            Log.e(LOG_TAG_STRING, tag + " : " + msg);
        }
    }

    public static void e(String tag, String msg, Throwable error) {
        if (ERROR) {
            Log.e(LOG_TAG_STRING, tag + " : " + msg, error);
        }
    }

    public static void wtf(String tag, String msg) {
        if (ERROR) {
            Log.wtf(LOG_TAG_STRING, tag + " : " + msg);
        }
    }

    public static void wtf(String tag, String msg, Throwable error) {
        if (ERROR) {
            Log.wtf(LOG_TAG_STRING, tag + " : " + msg, error);
        }
    }

    public static void dynamicLog(boolean openAll) {
        Log.d(LOGSWITCH_FILE_PATH, "dynamicLog ,  openAll = " + openAll);
        writeString(LOGSWITCH_FILE_PATH, openAll ? "on" : "off");
    }

    private static boolean writeString(String filePath, String value) {
        Log.d(LOG_TAG_STRING, "writeString value = " + value + " to " + filePath);
        FileWriter fw = null;
        boolean sucuess = true;
        try {
            fw = new FileWriter(filePath);
            fw.write(value);
            Log.d(LOG_TAG_STRING, "writeString " + value + " to " + filePath + " ok");
            try {
                fw.close();
            } catch (IOException e) {
                sucuess = false;
            }
        } catch (Exception e2) {
            sucuess = false;
            Log.d(LOG_TAG_STRING, "writeString failed, ", e2);
            if (fw != null) {
                fw.close();
            }
        } catch (Throwable th) {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        Log.d(LOG_TAG_STRING, "writeString sucuess = " + sucuess);
        return sucuess;
    }

    private static String readString(String filePath) {
        BufferedReader reader = null;
        String resString = "";
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(filePath)));
            while (true) {
                String tempString = reader2.readLine();
                if (tempString == null) {
                    break;
                }
                resString = resString + tempString;
            }
            reader2.close();
            try {
                reader2.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            Log.d(LOG_TAG_STRING, "readString failed ");
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        return resString;
    }

    public static String getCurrentLogSwitchValue() {
        return readString(LOGSWITCH_FILE_PATH);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        LogSwitchObserver logSwitchObserver = sLogSwitchObserver;
        if (logSwitchObserver != null) {
            logSwitchObserver.stopWatching();
            Log.d(LOG_TAG_STRING, this + " finalized, and sLogSwitchObserver.stopWatching ");
        } else {
            Log.d(LOG_TAG_STRING, " when " + this + " finalized, sLogSwitchObserver already been null.");
        }
        super.finalize();
    }

    public static boolean isVolumeKey(int keyCode) {
        if (keyCode == 24 || keyCode == 25 || keyCode == 164) {
            return true;
        }
        return false;
    }
}
