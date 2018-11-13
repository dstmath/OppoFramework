package com.android.server.am;

import android.os.FileObserver;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class OppoAamUtils {
    private static final String NOT_RESTRICT_PATH = "/data/oppo/coloros/oppoguardelf/not_restrict.xml";
    private static final String OPPO_GUARD_ELF_PATH = "/data/oppo/coloros/oppoguardelf";
    private static final String PERSIST_RESTRICT_PATH = "/data/oppo/coloros/oppoguardelf/persist_restrict.xml";
    private static final String SCREENOFF_RESTRICT_PATH = "/data/oppo/coloros/oppoguardelf/screenoff_restrict.xml";
    private static final String STARTINFO_WHITE_PATH = "/data/oppo/coloros/oppoguardelf/startinfo_white.xml";
    private static final String TAG = OppoAbnormalAppManager.TAG;
    private static OppoAamUtils sAamUtils = null;
    private boolean mDebugDetail = OppoAbnormalAppManager.DEBUG_DETAIL;
    private boolean mDebugSwitch = this.mDebugDetail;
    private FileObserverPolicy mNotRestrictFileObserver = null;
    private FileObserverPolicy mScreenOffRestrictFileObserver = null;
    private FileObserverPolicy mStartInfoWhiteFileObserver = null;

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event != 8) {
                return;
            }
            if (this.mFocusPath.equals(OppoAamUtils.SCREENOFF_RESTRICT_PATH)) {
                Log.i(OppoAamUtils.TAG, "onEvent: focusPath = SCREENOFF_RESTRICT_PATH");
                OppoAbnormalAppManager.getInstance().updateScreenOffRestrictedList(OppoAamUtils.this.readScreenOffResrictFile());
            } else if (this.mFocusPath.equals(OppoAamUtils.STARTINFO_WHITE_PATH)) {
                Log.i(OppoAamUtils.TAG, "onEvent: focusPath = STARTINFO_WHITE_PATH");
                OppoAbnormalAppManager.getInstance().updateStartInfoWhiteList(OppoAamUtils.this.readStartInfoWhiteFile());
            } else if (this.mFocusPath.equals(OppoAamUtils.NOT_RESTRICT_PATH)) {
                Log.i(OppoAamUtils.TAG, "onEvent: focusPath = NOT_RESTRICT_PATH");
                OppoAbnormalAppManager.getInstance().updateNotRestrictedList(OppoAamUtils.this.readNotRestrictFile());
            }
        }
    }

    private OppoAamUtils() {
        initDir();
        initFileObserver();
    }

    public static OppoAamUtils getInstance() {
        if (sAamUtils == null) {
            sAamUtils = new OppoAamUtils();
        }
        return sAamUtils;
    }

    private void initDir() {
        Log.i(TAG, "initDir start");
        File oppoGuardElfFilePath = new File(OPPO_GUARD_ELF_PATH);
        File screenOffRestrictFile = new File(SCREENOFF_RESTRICT_PATH);
        File startInfoWhiteFile = new File(STARTINFO_WHITE_PATH);
        File persistRestrictFile = new File(PERSIST_RESTRICT_PATH);
        File notRestrictFile = new File(NOT_RESTRICT_PATH);
        try {
            if (!oppoGuardElfFilePath.exists()) {
                oppoGuardElfFilePath.mkdirs();
            }
            if (!screenOffRestrictFile.exists()) {
                screenOffRestrictFile.createNewFile();
            }
            if (!startInfoWhiteFile.exists()) {
                startInfoWhiteFile.createNewFile();
            }
            if (!persistRestrictFile.exists()) {
                persistRestrictFile.createNewFile();
            }
            if (!notRestrictFile.exists()) {
                notRestrictFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "initDir failed!!!");
            e.printStackTrace();
        }
    }

    private void initFileObserver() {
        this.mScreenOffRestrictFileObserver = new FileObserverPolicy(SCREENOFF_RESTRICT_PATH);
        this.mScreenOffRestrictFileObserver.startWatching();
        this.mStartInfoWhiteFileObserver = new FileObserverPolicy(STARTINFO_WHITE_PATH);
        this.mStartInfoWhiteFileObserver.startWatching();
        this.mNotRestrictFileObserver = new FileObserverPolicy(NOT_RESTRICT_PATH);
        this.mNotRestrictFileObserver.startWatching();
    }

    private void ensureFileExist(File file, FileObserverPolicy fileObserver) {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (fileObserver != null) {
                new FileObserverPolicy(SCREENOFF_RESTRICT_PATH).startWatching();
            }
        }
    }

    public List<String> readScreenOffResrictFile() {
        List<String> readFromFileLocked;
        if (this.mDebugSwitch) {
            Log.i(TAG, "readScreenOffResrictFile start");
        }
        File screenOffResrictFile = new File(SCREENOFF_RESTRICT_PATH);
        synchronized (sAamUtils) {
            readFromFileLocked = readFromFileLocked(screenOffResrictFile);
        }
        return readFromFileLocked;
    }

    public void saveScreenOffResrictFile(List<String> list) {
        if (this.mDebugSwitch) {
            Log.i(TAG, "saveScreenOffResrictFile start");
        }
        File screenOffResrictFile = new File(SCREENOFF_RESTRICT_PATH);
        ensureFileExist(screenOffResrictFile, this.mScreenOffRestrictFileObserver);
        synchronized (sAamUtils) {
            writeToFileLocked(screenOffResrictFile, list);
        }
    }

    public List<String> readStartInfoWhiteFile() {
        List<String> readFromFileLocked;
        if (this.mDebugSwitch) {
            Log.i(TAG, "readStartInfoWhiteFile start");
        }
        File startInfoWhiteFile = new File(STARTINFO_WHITE_PATH);
        synchronized (sAamUtils) {
            readFromFileLocked = readFromFileLocked(startInfoWhiteFile);
        }
        return readFromFileLocked;
    }

    public void saveStartInfoWhiteFile(List<String> list) {
        if (this.mDebugSwitch) {
            Log.i(TAG, "saveStartInfoWhiteFile start");
        }
        File startInfoWhiteFile = new File(STARTINFO_WHITE_PATH);
        ensureFileExist(startInfoWhiteFile, this.mStartInfoWhiteFileObserver);
        synchronized (sAamUtils) {
            writeToFileLocked(startInfoWhiteFile, list);
        }
    }

    public List<String> readPersistRestrictFile() {
        List<String> readFromFileLocked;
        if (this.mDebugSwitch) {
            Log.i(TAG, "readPersistRestrictFile start");
        }
        File persistRestrictFile = new File(PERSIST_RESTRICT_PATH);
        synchronized (sAamUtils) {
            readFromFileLocked = readFromFileLocked(persistRestrictFile);
        }
        return readFromFileLocked;
    }

    public void savePersistRestrictFile(List<String> list) {
        if (this.mDebugSwitch) {
            Log.i(TAG, "savePersistRestrictFile start");
        }
        File persistRestrictFile = new File(PERSIST_RESTRICT_PATH);
        ensureFileExist(persistRestrictFile, null);
        synchronized (sAamUtils) {
            writeToFileLocked(persistRestrictFile, list);
        }
    }

    public List<String> readNotRestrictFile() {
        List<String> readFromFileLocked;
        if (this.mDebugSwitch) {
            Log.i(TAG, "readNotRestrictFile start");
        }
        File file = new File(NOT_RESTRICT_PATH);
        synchronized (sAamUtils) {
            readFromFileLocked = readFromFileLocked(file);
        }
        return readFromFileLocked;
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:0x013f A:{SYNTHETIC, Splitter: B:62:0x013f} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0113 A:{SYNTHETIC, Splitter: B:54:0x0113} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00e8 A:{SYNTHETIC, Splitter: B:46:0x00e8} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00be A:{SYNTHETIC, Splitter: B:38:0x00be} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0094 A:{SYNTHETIC, Splitter: B:30:0x0094} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0163 A:{SYNTHETIC, Splitter: B:68:0x0163} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<String> readFromFileLocked(File file) {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        Throwable th;
        if (this.mDebugSwitch) {
            Log.i(TAG, "readFromFileLocked start");
        }
        FileInputStream stream = null;
        List<String> list = new ArrayList();
        try {
            FileInputStream stream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                            String pkg = parser.getAttributeValue(null, "att");
                            if (pkg != null) {
                                list.add(pkg);
                                if (this.mDebugSwitch) {
                                    Log.i(TAG, "readFromFileLocked  pkg == " + pkg);
                                }
                            }
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e6) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6);
                    }
                }
                stream = stream2;
            } catch (NullPointerException e7) {
                e2 = e7;
                stream = stream2;
                Log.e(TAG, "failed parsing ", e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e62);
                    }
                }
                return list;
            } catch (NumberFormatException e8) {
                e3 = e8;
                stream = stream2;
                Log.e(TAG, "failed parsing ", e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e622);
                    }
                }
                return list;
            } catch (XmlPullParserException e9) {
                e4 = e9;
                stream = stream2;
                Log.e(TAG, "failed parsing ", e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6222);
                    }
                }
                return list;
            } catch (IOException e10) {
                e6222 = e10;
                stream = stream2;
                Log.e(TAG, "failed IOException ", e6222);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e62222);
                    }
                }
                return list;
            } catch (IndexOutOfBoundsException e11) {
                e5 = e11;
                stream = stream2;
                try {
                    Log.e(TAG, "failed parsing ", e5);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e622222) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e622222);
                        }
                    }
                    return list;
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6222222);
                    }
                }
                throw th;
            }
        } catch (NullPointerException e12) {
            e2 = e12;
            Log.e(TAG, "failed parsing ", e2);
            if (stream != null) {
            }
            return list;
        } catch (NumberFormatException e13) {
            e3 = e13;
            Log.e(TAG, "failed parsing ", e3);
            if (stream != null) {
            }
            return list;
        } catch (XmlPullParserException e14) {
            e4 = e14;
            Log.e(TAG, "failed parsing ", e4);
            if (stream != null) {
            }
            return list;
        } catch (IOException e15) {
            e6222222 = e15;
            Log.e(TAG, "failed IOException ", e6222222);
            if (stream != null) {
            }
            return list;
        } catch (IndexOutOfBoundsException e16) {
            e5 = e16;
            Log.e(TAG, "failed parsing ", e5);
            if (stream != null) {
            }
            return list;
        }
        return list;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x009c A:{SYNTHETIC, Splitter: B:24:0x009c} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00be A:{SYNTHETIC, Splitter: B:30:0x00be} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeToFileLocked(File file, List<String> list) {
        IOException e;
        Throwable th;
        if (this.mDebugSwitch) {
            Log.i(TAG, "writeToFileLocked!!!");
        }
        FileOutputStream stream = null;
        try {
            FileOutputStream stream2 = new FileOutputStream(file);
            try {
                XmlSerializer out = Xml.newSerializer();
                out.setOutput(stream2, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "gs");
                int length = list.size();
                for (int i = 0; i < length; i++) {
                    String pkg = (String) list.get(i);
                    if (pkg != null) {
                        out.startTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                        out.attribute(null, "att", pkg);
                        out.endTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                    }
                }
                out.endTag(null, "gs");
                out.endDocument();
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e2);
                    }
                }
                stream = stream2;
            } catch (IOException e3) {
                e2 = e3;
                stream = stream2;
                try {
                    Log.e(TAG, "Failed to write IOException: " + e2);
                    if (stream == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e22);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e22 = e4;
            Log.e(TAG, "Failed to write IOException: " + e22);
            if (stream == null) {
                try {
                    stream.close();
                } catch (IOException e222) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e222);
                }
            }
        }
    }

    public void setDynamicDebugSwitch() {
        this.mDebugSwitch = this.mDebugDetail | OppoAppStartupManager.getInstance().mDynamicDebug;
    }
}
