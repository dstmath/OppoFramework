package com.android.server.wm;

import android.os.FileObserver;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import com.android.server.display.ai.utils.ColorAILog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class ColorSplitWindowAppReader {
    private static final String DATA_CONFIG_PATH = "/data/oppo/coloros/config";
    private static boolean DEBUG = false;
    /* access modifiers changed from: private */
    public static boolean DEBUG_DETAIL = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static final String DEFAULT_SPLIT_WINDOW_FILE_PATH = "/system/oppo/sys_wms_split_app.xml";
    private static final String FORCE_PKG_NAME = "bp";
    private static final String FULL_NAME = "full";
    private static final String PKG_ATTR = "attr";
    private static final String PKG_NAME = "p";
    private static final String SPLIT_WINDOW_APP_FILE_PATH = "/data/oppo/coloros/config/sys_wms_split_app.xml";
    private static final String TAG = "ColorSplitWindowAppReader";
    private static volatile ColorSplitWindowAppReader mIns = null;
    private ArrayList<String> mAllowPackageName = new ArrayList<>();
    private DataFileListener mDataFileListener;
    private ArrayList<String> mForcePackageName = new ArrayList<>();
    private List<String> mFullPackageName = new ArrayList();
    private Object mLock = new Object();

    public static ColorSplitWindowAppReader getInstance() {
        if (mIns == null) {
            synchronized (ColorSplitWindowAppReader.class) {
                if (mIns == null) {
                    mIns = new ColorSplitWindowAppReader();
                }
            }
        }
        return mIns;
    }

    private ColorSplitWindowAppReader() {
        initFile();
        this.mDataFileListener = new DataFileListener(SPLIT_WINDOW_APP_FILE_PATH);
        this.mDataFileListener.startWatching();
        getSplitWindowApp();
    }

    public boolean isInConfigList(String packageName) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mAllowPackageName != null) {
                result = this.mAllowPackageName.contains(packageName);
            }
        }
        return result;
    }

    public boolean isInBlackList(String packageName) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mForcePackageName != null) {
                result = this.mForcePackageName.contains(packageName);
            }
        }
        return result;
    }

    public boolean isInForbidActivityList(String activityName) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mFullPackageName != null) {
                result = this.mFullPackageName.contains(activityName);
            }
        }
        return result;
    }

    private class DataFileListener extends FileObserver {
        public DataFileListener(String path) {
            super(path, 12);
        }

        public void onEvent(int event, String path) {
            if (ColorSplitWindowAppReader.DEBUG_DETAIL) {
                Log.i(ColorSplitWindowAppReader.TAG, "readAppList onEvent: " + event);
            }
            if (event == 8) {
                ColorSplitWindowAppReader.this.getSplitWindowApp();
            }
        }
    }

    private void initFile() {
        File dataConfigPath = new File(DATA_CONFIG_PATH);
        File splitAppFile = new File(SPLIT_WINDOW_APP_FILE_PATH);
        File defaultFile = new File(DEFAULT_SPLIT_WINDOW_FILE_PATH);
        try {
            if (!dataConfigPath.exists()) {
                dataConfigPath.mkdirs();
            }
            if (!splitAppFile.exists()) {
                splitAppFile.createNewFile();
                FileUtils.copyFile(defaultFile, splitAppFile);
            }
        } catch (Exception e) {
            Log.e(TAG, "initFile failed!");
        }
    }

    /* access modifiers changed from: private */
    public void getSplitWindowApp() {
        String str;
        String str2;
        synchronized (this.mLock) {
            FileInputStream inputStream = null;
            try {
                File file = new File(SPLIT_WINDOW_APP_FILE_PATH);
                if (file.exists()) {
                    inputStream = new FileInputStream(file);
                    this.mAllowPackageName.clear();
                    this.mForcePackageName.clear();
                    this.mFullPackageName.clear();
                    readAppListFromXML(inputStream);
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        str2 = TAG;
                        str = "getSplitWindowApp() close inputStream error!";
                    }
                }
            } catch (Exception e2) {
                Log.e(TAG, "getSplitWindowApp() error!");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                        str2 = TAG;
                        str = "getSplitWindowApp() close inputStream error!";
                    }
                }
            } catch (Throwable e4) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "getSplitWindowApp() close inputStream error!");
                    }
                }
                throw e4;
            }
        }
        Log.e(str2, str);
    }

    private void readAppListFromXML(FileInputStream stream) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType == 2 && "p".equals(parser.getName())) {
                    String pName = parser.getAttributeValue(null, PKG_ATTR);
                    this.mAllowPackageName.add(pName);
                    if (DEBUG) {
                        Log.i(TAG, "readAppList: " + pName);
                    }
                } else if (eventType == 2 && FORCE_PKG_NAME.equals(parser.getName())) {
                    String pName2 = parser.getAttributeValue(null, PKG_ATTR);
                    this.mForcePackageName.add(pName2);
                    if (DEBUG) {
                        Log.i(TAG, "readForceAppList: " + pName2);
                    }
                } else if (eventType == 2 && FULL_NAME.equals(parser.getName())) {
                    String pName3 = parser.getAttributeValue(null, PKG_ATTR);
                    this.mFullPackageName.add(pName3);
                    if (DEBUG) {
                        Log.i(TAG, "readFullAppList: " + pName3);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "readAppListFromXML() error!");
        }
    }
}
