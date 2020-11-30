package com.color.util;

import android.app.OppoActivityManager;
import android.content.Context;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import com.android.server.display.ai.utils.ColorAILog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

public class ColorSecureKeyboardUtils {
    private static final String SECURE_KEYBOARD_FILE = "/data/oppo/coloros/oppoguardelf/sys_secure_keyboard_config.xml";
    private static final String TAG = "ColorSecureKeyboard";
    private static final String TAG_ENABLE = "enable";
    private static final String TAG_INPUTMETHOD_LIST = "inputmethod-app";
    private static final String TAG_NORMAL_LIST = "normal-app";
    private static ColorSecureKeyboardData mData = null;
    private static ColorSecureKeyboardUtils mUtils = null;
    private boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private Context mContext;
    private ArrayList<String> mDefaultList1 = new ArrayList<>();
    private ArrayList<String> mDefaultList2 = new ArrayList<>();
    private boolean mEnable = true;
    private FileObserverPolicy mFileObserver = null;
    private ArrayList<String> mList1 = new ArrayList<>();
    private ArrayList<String> mList2 = new ArrayList<>();
    private final Object mLock = new Object();
    private String mXmlEnable;
    private ArrayList<String> mXmlList1 = new ArrayList<>();
    private ArrayList<String> mXmlList2 = new ArrayList<>();

    private ColorSecureKeyboardUtils() {
    }

    public static ColorSecureKeyboardUtils getInstance() {
        if (mUtils == null) {
            mUtils = new ColorSecureKeyboardUtils();
        }
        return mUtils;
    }

    public void init(Context context) {
        this.mContext = context;
        initDir();
        initFileObserver();
        if (mData == null) {
            mData = new ColorSecureKeyboardData();
        }
        this.mXmlList1 = mData.getNormalAppList();
        this.mXmlList2 = mData.getInputMethodAppList();
        synchronized (this.mLock) {
            readConfigFile();
        }
    }

    private void initDefaultList() {
        this.mDefaultList1.clear();
        this.mDefaultList1.add("com.tvt.network");
        this.mDefaultList1.add("org.videolan.vlc");
        this.mDefaultList1.add("com.bookmate");
        this.mDefaultList1.add("com.facebook.orca");
        this.mDefaultList2.clear();
    }

    public ColorSecureKeyboardData getData() {
        if (mData == null) {
            mData = new ColorSecureKeyboardData();
        }
        return mData;
    }

    private void initDir() {
        File file = new File(SECURE_KEYBOARD_FILE);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        changeModFile(SECURE_KEYBOARD_FILE);
    }

    private void changeModFile(String fileName) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "changeModFile : " + e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readConfigFile() {
        int type;
        File file = new File(SECURE_KEYBOARD_FILE);
        if (file.exists()) {
            this.mXmlList1.clear();
            this.mXmlList2.clear();
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        if (TAG_NORMAL_LIST.equals(tag)) {
                            String value = parser.getAttributeValue(null, "att");
                            if (this.DEBUG) {
                                Log.d(TAG, "normal-app list : " + value);
                            }
                            if (value != null) {
                                this.mXmlList1.add(value);
                            }
                        }
                        if (TAG_INPUTMETHOD_LIST.equals(tag)) {
                            String value2 = parser.getAttributeValue(null, "att");
                            if (this.DEBUG) {
                                Log.d(TAG, "inputmethod-app list : " + value2);
                            }
                            if (value2 != null) {
                                this.mXmlList2.add(value2);
                            }
                        }
                        if (TAG_ENABLE.equals(tag)) {
                            this.mXmlEnable = parser.nextText();
                            if (this.DEBUG) {
                                Log.d(TAG, "mEnable = " + this.mXmlEnable);
                            }
                            mData.setEnable(this.mXmlEnable);
                        }
                    }
                } while (type != 1);
                try {
                    stream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                if (0 != 0) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    private void initFileObserver() {
        this.mFileObserver = new FileObserverPolicy(SECURE_KEYBOARD_FILE);
        this.mFileObserver.startWatching();
    }

    /* access modifiers changed from: private */
    public class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (ColorSecureKeyboardUtils.this.DEBUG) {
                Log.d(ColorSecureKeyboardUtils.TAG, "FileObserver:" + event + "," + path);
            }
            if (event == 8) {
                synchronized (ColorSecureKeyboardUtils.this.mLock) {
                    ColorSecureKeyboardUtils.this.readConfigFile();
                }
            }
        }
    }

    public void initData(Context context) {
        try {
            ColorSecureKeyboardData data = new OppoActivityManager().getSecureKeyboardData();
            this.mList1 = data.getNormalAppList();
            this.mList2 = data.getInputMethodAppList();
            initDefaultList();
            if ("false".equals(data.getEnable())) {
                this.mEnable = false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "init data RemoteException , " + e);
        } catch (Exception e2) {
            Log.e(TAG, "init data Exception , " + e2);
        }
    }

    public boolean inBlackList(String name1, String name2) {
        initData(this.mContext);
        if (this.DEBUG) {
            Log.d(TAG, "packageName: " + name1 + ", " + name2);
            Log.d(TAG, "mList1: " + this.mList1 + ", mList2:" + this.mList2);
        }
        if (!this.mEnable) {
            return true;
        }
        if (!this.mList1.isEmpty() && this.mList1.contains(name1)) {
            return true;
        }
        if (!this.mList2.isEmpty() && this.mList2.contains(name2)) {
            return true;
        }
        if (!this.mDefaultList1.isEmpty() && this.mDefaultList1.contains(name1)) {
            return true;
        }
        if (this.mDefaultList2.isEmpty() || !this.mDefaultList2.contains(name2)) {
            return false;
        }
        return true;
    }
}
