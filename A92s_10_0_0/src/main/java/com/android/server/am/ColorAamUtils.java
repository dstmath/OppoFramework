package com.android.server.am;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;
import com.color.settings.ColorSettings;
import com.color.settings.ColorSettingsChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ColorAamUtils {
    private static final String NOT_RESTRICT_PATH = "/data/oppo/coloros/oppoguardelf/not_restrict.xml";
    private static final String OPPO_GUARD_ELF_PATH = "/data/oppo/coloros/oppoguardelf";
    private static final String PERSIST_RESTRICT_PATH = "/data/oppo/coloros/oppoguardelf/persist_restrict.xml";
    private static final String SCREENOFF_RESTRICT_PATH = "/oppoguardelf/screenoff_restrict.xml";
    private static final String STARTINFO_WHITE_PATH = "/data/oppo/coloros/oppoguardelf/startinfo_white.xml";
    private static final String TAG = "ColorAbnormalAppManager";
    private static ColorAamUtils sAamUtils = null;
    ColorSettingsChangeListener mColorConfigChangeListener = new ColorSettingsChangeListener(new Handler()) {
        /* class com.android.server.am.ColorAamUtils.AnonymousClass1 */

        public void onSettingsChange(boolean selfChange, String path, int userId) {
            if (ColorAamUtils.this.mDebugSwitch) {
                Log.v(ColorAamUtils.TAG, "on config change and maybe read config, path=" + path + ", userId=" + userId);
            }
            if (ColorAamUtils.SCREENOFF_RESTRICT_PATH.equals(path)) {
                OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).updateScreenOffRestrictedList(ColorAamUtils.this.readScreenOffResrictFile(userId), userId);
            }
        }
    };
    private Context mContext;
    private boolean mDebugDetail = IColorAbnormalAppManager.DEBUG_DETAIL;
    /* access modifiers changed from: private */
    public boolean mDebugSwitch = this.mDebugDetail;
    private FileObserverPolicy mNotRestrictFileObserver = null;
    private FileObserverPolicy mStartInfoWhiteFileObserver = null;

    private ColorAamUtils() {
        initDir();
        initFileObserver();
    }

    public static ColorAamUtils getInstance() {
        if (sAamUtils == null) {
            sAamUtils = new ColorAamUtils();
        }
        return sAamUtils;
    }

    public void initCtx(Context context) {
        this.mContext = context;
        registerConfigChangeListener();
    }

    private void initDir() {
        Log.i(TAG, "initDir start");
        File oppoGuardElfFilePath = new File(OPPO_GUARD_ELF_PATH);
        File startInfoWhiteFile = new File(STARTINFO_WHITE_PATH);
        File persistRestrictFile = new File(PERSIST_RESTRICT_PATH);
        File notRestrictFile = new File(NOT_RESTRICT_PATH);
        try {
            if (!oppoGuardElfFilePath.exists()) {
                oppoGuardElfFilePath.mkdirs();
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
        this.mStartInfoWhiteFileObserver = new FileObserverPolicy(STARTINFO_WHITE_PATH);
        this.mStartInfoWhiteFileObserver.startWatching();
        this.mNotRestrictFileObserver = new FileObserverPolicy(NOT_RESTRICT_PATH);
        this.mNotRestrictFileObserver.startWatching();
    }

    private File ensureFileExist(String filePath, FileObserverPolicy fileObserver) {
        File file = new File(filePath);
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
                new FileObserverPolicy(filePath).startWatching();
            }
        }
        return file;
    }

    private void registerConfigChangeListener() {
        ColorSettings.registerChangeListenerForAll(this.mContext, SCREENOFF_RESTRICT_PATH, 0, this.mColorConfigChangeListener);
    }

    public List<String> readScreenOffResrictFile() {
        return readScreenOffResrictFile(0);
    }

    /* access modifiers changed from: private */
    public List<String> readScreenOffResrictFile(int userId) {
        List<String> readFromColorSettings;
        if (this.mDebugSwitch) {
            Log.i(TAG, "readScreenOffResrictFile start");
        }
        synchronized (sAamUtils) {
            readFromColorSettings = readFromColorSettings(SCREENOFF_RESTRICT_PATH, userId);
        }
        return readFromColorSettings;
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
        File file = ensureFileExist(STARTINFO_WHITE_PATH, this.mStartInfoWhiteFileObserver);
        synchronized (sAamUtils) {
            writeToFileLocked(file, list);
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
        File file = ensureFileExist(PERSIST_RESTRICT_PATH, null);
        synchronized (sAamUtils) {
            writeToFileLocked(file, list);
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

    private List<String> readFromFileLocked(File file) {
        StringBuilder sb;
        int type;
        String pkg;
        if (this.mDebugSwitch) {
            Log.i(TAG, "readFromFileLocked start");
        }
        FileInputStream stream = null;
        List<String> list = new ArrayList<>();
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName()) && (pkg = parser.getAttributeValue(null, "att")) != null) {
                    list.add(pkg);
                    if (this.mDebugSwitch) {
                        Log.i(TAG, "readFromFileLocked  pkg == " + pkg);
                    }
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
        } catch (NullPointerException e2) {
            Log.e(TAG, "failed parsing ", e2);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (NumberFormatException e4) {
            Log.e(TAG, "failed parsing ", e4);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e6) {
            Log.e(TAG, "failed parsing ", e6);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e = e7;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e8) {
            Log.e(TAG, "failed IOException ", e8);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e9) {
                    e = e9;
                    sb = new StringBuilder();
                }
            }
        } catch (IndexOutOfBoundsException e10) {
            Log.e(TAG, "failed parsing ", e10);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e11) {
                    e = e11;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e12) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e12);
                }
            }
            throw th;
        }
        return list;
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Log.e(TAG, sb.toString());
        return list;
    }

    private void writeToFileLocked(File file, List<String> list) {
        StringBuilder sb;
        if (this.mDebugSwitch) {
            Log.i(TAG, "writeToFileLocked!!!");
        }
        FileOutputStream stream = null;
        try {
            FileOutputStream stream2 = new FileOutputStream(file);
            XmlSerializer out = Xml.newSerializer();
            out.setOutput(stream2, "utf-8");
            out.startDocument(null, true);
            out.startTag(null, "gs");
            int length = list.size();
            for (int i = 0; i < length; i++) {
                String pkg = list.get(i);
                if (pkg != null) {
                    out.startTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                    out.attribute(null, "att", pkg);
                    out.endTag(null, ColorAppCrashClearManager.CRASH_CLEAR_NAME);
                }
            }
            out.endTag(null, "gs");
            out.endDocument();
            try {
                stream2.close();
                return;
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
            sb.append("Failed to close state FileInputStream ");
            sb.append(e);
            Log.e(TAG, sb.toString());
        } catch (IOException e2) {
            Log.e(TAG, "Failed to write IOException: " + e2);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e4);
                }
            }
            throw th;
        }
    }

    private List<String> readFromColorSettings(String filePath, int userId) {
        StringBuilder sb;
        int type;
        String pkg;
        List<String> list = new ArrayList<>();
        Context context = this.mContext;
        if (context == null) {
            Log.w(TAG, "readDataFromColorSettings, invalid context!");
            return list;
        }
        InputStream stream = null;
        try {
            InputStream stream2 = ColorSettings.readConfigAsUser(context, filePath, userId, 0);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, "UTF-8");
            do {
                type = parser.next();
                if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName()) && (pkg = parser.getAttributeValue(null, "att")) != null) {
                    list.add(pkg);
                }
            } while (type != 1);
            if (stream2 != null) {
                try {
                    stream2.close();
                } catch (IOException e) {
                    e = e;
                    sb = new StringBuilder();
                }
            }
        } catch (Exception e2) {
            Log.e(TAG, "failed parsing from ColorSettings: " + e2);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    Log.e(TAG, "Failed to close stream: " + e4);
                }
            }
            throw th;
        }
        return list;
        sb.append("Failed to close stream: ");
        sb.append(e);
        Log.e(TAG, sb.toString());
        return list;
    }

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
            if (this.mFocusPath.equals(ColorAamUtils.STARTINFO_WHITE_PATH)) {
                Log.i(ColorAamUtils.TAG, "onEvent: focusPath = STARTINFO_WHITE_PATH");
                OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).updateStartInfoWhiteList(ColorAamUtils.this.readStartInfoWhiteFile());
            } else if (this.mFocusPath.equals(ColorAamUtils.NOT_RESTRICT_PATH)) {
                Log.i(ColorAamUtils.TAG, "onEvent: focusPath = NOT_RESTRICT_PATH");
                OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).updateNotRestrictedList(ColorAamUtils.this.readNotRestrictFile());
            }
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDebugSwitch = this.mDebugDetail | on;
    }
}
