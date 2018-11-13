package com.oppo.debug;

import android.os.FileObserver;
import android.util.Log;
import com.oppo.media.MediaFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.microedition.khronos.opengles.GL10;

public class InputLog {
    public static boolean DEBUG = false;
    private static boolean ERROR = false;
    private static boolean INFO = false;
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
    private static boolean WARN;
    private static boolean sInited = false;
    private static LogSwitchObserver sLogSwitchObserver;

    static class LogSwitchObserver extends FileObserver {
        public LogSwitchObserver(String path) {
            super(path, 10);
        }

        public void onEvent(int event, String path) {
            InputLog.updateLogLevel();
        }
    }

    static {
        boolean z;
        boolean z2 = true;
        if (LOGCAT_LEVEL <= 2) {
            z = true;
        } else {
            z = false;
        }
        DEBUG = z;
        if (LOGCAT_LEVEL <= 4) {
            z = true;
        } else {
            z = false;
        }
        INFO = z;
        if (LOGCAT_LEVEL <= 8) {
            z = true;
        } else {
            z = false;
        }
        WARN = z;
        if (LOGCAT_LEVEL > 16) {
            z2 = false;
        }
        ERROR = z2;
        startWatching();
    }

    public static boolean isOpenAllLog() {
        return LOGCAT_LEVEL == 0;
    }

    private static void dumpEventType(int eventType) {
        switch (eventType & 4095) {
            case 1:
                Log.d(LOG_TAG_STRING, "FileObserver.ACCESS ");
                return;
            case 2:
                Log.d(LOG_TAG_STRING, "FileObserver.MODIFY ");
                return;
            case 4:
                Log.d(LOG_TAG_STRING, "FileObserver.ATTRIB ");
                return;
            case 8:
                Log.d(LOG_TAG_STRING, "FileObserver.CLOSE_WRITE ");
                return;
            case 16:
                Log.d(LOG_TAG_STRING, "FileObserver.CLOSE_NOWRITE ");
                return;
            case 32:
                Log.d(LOG_TAG_STRING, "FileObserver.OPEN ");
                return;
            case 64:
                Log.d(LOG_TAG_STRING, "FileObserver.MOVED_FROM ");
                return;
            case 128:
                Log.d(LOG_TAG_STRING, "FileObserver.MOVED_TO ");
                return;
            case GL10.GL_DEPTH_BUFFER_BIT /*256*/:
                Log.d(LOG_TAG_STRING, "FileObserver.CREATE ");
                return;
            case 512:
                Log.d(LOG_TAG_STRING, "FileObserver.DELETE ");
                return;
            case 1024:
                Log.d(LOG_TAG_STRING, "FileObserver.DELETE_SELF ");
                return;
            case 2048:
                Log.d(LOG_TAG_STRING, "FileObserver.MOVE_SELF ");
                return;
            default:
                return;
        }
    }

    public static void startWatching() {
        if (!sInited && checkLogSwitchDirExist() && SWITCH_ON_VALUE.equals(getCurrentLogSwitchValue())) {
            sLogSwitchObserver = new LogSwitchObserver(LOGSWITCH_DIR_PATH);
            if (sLogSwitchObserver != null) {
                sLogSwitchObserver.startWatching();
                sInited = true;
                updateLogLevel();
            }
        }
    }

    public static void restoreToDefaltLogLevel() {
        Log.d(LOG_TAG_STRING, " restoreToDefaltLogLevel");
        File file = new File(LOGSWITCH_FILE_PATH);
        if (file == null || !file.exists()) {
            Log.d(LOG_TAG_STRING, file + " still not exists");
        } else {
            file.delete();
        }
    }

    public static boolean checkLogSwitchDirExist() {
        File dir = new File(LOGSWITCH_DIR_PATH);
        if (dir != null && dir.exists()) {
            return true;
        }
        return false;
    }

    private static boolean changeFileAttr(String attr, String filePath) {
        String command = "chmod " + attr + " " + filePath;
        Log.d(LOG_TAG_STRING, "changeFileAttr command = " + command);
        try {
            Process proc = Runtime.getRuntime().exec(command);
            return true;
        } catch (IOException e) {
            Log.i(LOG_TAG_STRING, command + " exec failed !!!!");
            return false;
        }
    }

    public static boolean initLogSwitchDir() {
        Log.d(LOG_TAG_STRING, "initLogSwitchDir Begin");
        File dir = new File(LOGSWITCH_DIR_PATH);
        if (dir == null) {
            Log.d(LOG_TAG_STRING, "initLogSwitchDir failed ,dir = null ");
            return false;
        } else if (dir.exists()) {
            return true;
        } else {
            boolean ok = dir.mkdir();
            boolean success = true;
            Log.d(LOG_TAG_STRING, dir + "initLogSwitchDir  mkdir , ok = " + ok);
            if (ok) {
                success = changeFileAttr("777", LOGSWITCH_DIR_PATH);
            }
            if (!ok) {
                success = false;
            }
            return success;
        }
    }

    public static boolean initLogSwitch() {
        if (initLogSwitchDir()) {
            return initLogSwitchFile();
        }
        return false;
    }

    public static boolean initLogSwitchFile() {
        Log.d(LOG_TAG_STRING, " initLogSwitchFile ");
        File file = new File(LOGSWITCH_FILE_PATH);
        if (file == null) {
            Log.d(LOG_TAG_STRING, "initLogSwitchFile failed , file =null ");
            return false;
        } else if (file.exists()) {
            return true;
        } else {
            boolean ok;
            try {
                ok = file.createNewFile();
            } catch (IOException e) {
                ok = false;
                Log.d(LOG_TAG_STRING, file + " createNewFile failed in initLogSwitchFile(), " + e);
            } catch (Throwable th) {
                throw th;
            }
            if (!ok) {
                return false;
            }
            changeFileAttr("777", LOGSWITCH_FILE_PATH);
            return true;
        }
    }

    public static void OpenAllLogLevel() {
        Log.d(LOG_TAG_STRING, " OpenAllLogLevel 22");
        File file = new File(LOGSWITCH_FILE_PATH);
        if (file != null) {
            try {
                if ((file.exists() ^ 1) != 0) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                Log.d(LOG_TAG_STRING, file + " createNewFile failed in OpenAllLogLevel(), " + e);
            } catch (Throwable th) {
            }
        }
        Log.d(LOG_TAG_STRING, file + " still exists or open failed, ");
    }

    private static void updateLogLevel() {
        boolean z;
        boolean z2 = true;
        LOGCAT_LEVEL = SWITCH_ON_VALUE.equals(readString(LOGSWITCH_FILE_PATH)) ? 0 : 16;
        if (LOGCAT_LEVEL <= 0) {
            z = true;
        } else {
            z = false;
        }
        VERBOSE = z;
        if (LOGCAT_LEVEL <= 2) {
            z = true;
        } else {
            z = false;
        }
        DEBUG = z;
        if (LOGCAT_LEVEL <= 4) {
            z = true;
        } else {
            z = false;
        }
        INFO = z;
        if (LOGCAT_LEVEL <= 8) {
            z = true;
        } else {
            z = false;
        }
        WARN = z;
        if (LOGCAT_LEVEL > 16) {
            z2 = false;
        }
        ERROR = z2;
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
        writeString(LOGSWITCH_FILE_PATH, openAll ? SWITCH_ON_VALUE : SWITCH_OFF_VALUE);
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x00c6 A:{SYNTHETIC, Splitter: B:28:0x00c6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean writeString(String filePath, String value) {
        Exception e;
        Throwable th;
        Log.d(LOG_TAG_STRING, "writeString value = " + value + " to " + filePath);
        FileWriter fw = null;
        boolean sucuess = true;
        try {
            FileWriter fw2 = new FileWriter(filePath);
            if (fw2 != null) {
                try {
                    fw2.write(value);
                    Log.d(LOG_TAG_STRING, "writeString " + value + " to " + filePath + " ok");
                } catch (Exception e2) {
                    e = e2;
                    fw = fw2;
                } catch (Throwable th2) {
                    th = th2;
                    fw = fw2;
                    if (fw != null) {
                    }
                    throw th;
                }
            }
            Log.d(LOG_TAG_STRING, "writeString " + value + " to " + filePath + " faildd ,because fw = null");
            if (fw2 != null) {
                try {
                    fw2.close();
                } catch (IOException e3) {
                    sucuess = false;
                }
            }
            fw = fw2;
        } catch (Exception e4) {
            e = e4;
            sucuess = false;
            try {
                Log.d(LOG_TAG_STRING, "writeString failed, ", e);
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e5) {
                        sucuess = false;
                    }
                }
                Log.d(LOG_TAG_STRING, "writeString sucuess = " + sucuess);
                return sucuess;
            } catch (Throwable th3) {
                th = th3;
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        }
        Log.d(LOG_TAG_STRING, "writeString sucuess = " + sucuess);
        return sucuess;
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x005d A:{SYNTHETIC, Splitter: B:27:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0066 A:{SYNTHETIC, Splitter: B:32:0x0066} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readString(String filePath) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        String tempString = "";
        String resString = "";
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(filePath)));
            if (reader2 == null) {
                try {
                    Log.d(LOG_TAG_STRING, "readString failed , because reader = null ");
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e2) {
                        }
                    }
                    return resString;
                } catch (IOException e3) {
                    e = e3;
                    reader = reader2;
                    try {
                        e.printStackTrace();
                        Log.d(LOG_TAG_STRING, "readString failed ");
                        if (reader != null) {
                        }
                        return resString;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            while (true) {
                tempString = reader2.readLine();
                if (tempString == null) {
                    break;
                }
                resString = resString + tempString;
            }
            reader2.close();
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e5) {
                }
            }
            reader = reader2;
            return resString;
        } catch (IOException e6) {
            e = e6;
            e.printStackTrace();
            Log.d(LOG_TAG_STRING, "readString failed ");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e7) {
                }
            }
            return resString;
        }
    }

    public static String getCurrentLogSwitchValue() {
        return readString(LOGSWITCH_FILE_PATH);
    }

    protected void finalize() throws Throwable {
        if (sLogSwitchObserver != null) {
            sLogSwitchObserver.stopWatching();
            Log.d(LOG_TAG_STRING, this + " finalized, and sLogSwitchObserver.stopWatching ");
        } else {
            Log.d(LOG_TAG_STRING, " when " + this + " finalized, sLogSwitchObserver already been null.");
        }
        super.finalize();
    }

    public static boolean isVolumeKey(int keyCode) {
        switch (keyCode) {
            case MediaFile.FILE_TYPE_3GPP2 /*24*/:
            case MediaFile.FILE_TYPE_WMV /*25*/:
            case 164:
                return true;
            default:
                return false;
        }
    }
}
