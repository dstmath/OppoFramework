package com.color.util;

import android.app.OppoActivityManager;
import android.content.Context;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
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
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private Context mContext;
    private ArrayList<String> mDefaultList1 = new ArrayList();
    private ArrayList<String> mDefaultList2 = new ArrayList();
    private boolean mEnable = true;
    private FileObserverPolicy mFileObserver = null;
    private ArrayList<String> mList1 = new ArrayList();
    private ArrayList<String> mList2 = new ArrayList();
    private final Object mLock = new Object();
    private String mXmlEnable;
    private ArrayList<String> mXmlList1 = new ArrayList();
    private ArrayList<String> mXmlList2 = new ArrayList();

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(ColorSecureKeyboardUtils.SECURE_KEYBOARD_FILE)) {
                synchronized (ColorSecureKeyboardUtils.this.mLock) {
                    ColorSecureKeyboardUtils.this.readConfigFile();
                }
            }
        }
    }

    private ColorSecureKeyboardUtils() {
    }

    public static ColorSecureKeyboardUtils getInstance() {
        if (mUtils == null) {
            mUtils = new ColorSecureKeyboardUtils();
        }
        return mUtils;
    }

    public void init(Context context) {
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
        this.mDefaultList1.add("com.smarteyes.network");
        this.mDefaultList1.add("android.autofillservice.cts");
        this.mDefaultList1.add("com.cmcc.hebao");
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
            Runtime.getRuntime().exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "changeModFile : " + e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x00f6 A:{SYNTHETIC, Splitter: B:49:0x00f6} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00ea A:{SYNTHETIC, Splitter: B:43:0x00ea} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile() {
        Exception e;
        Throwable th;
        File file = new File(SECURE_KEYBOARD_FILE);
        if (file.exists()) {
            this.mXmlList1.clear();
            this.mXmlList2.clear();
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, null);
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            String value;
                            String tag = parser.getName();
                            if (TAG_NORMAL_LIST.equals(tag)) {
                                value = parser.getAttributeValue(null, "att");
                                if (this.DEBUG) {
                                    Log.d(TAG, "normal-app list : " + value);
                                }
                                if (value != null) {
                                    this.mXmlList1.add(value);
                                }
                            }
                            if (TAG_INPUTMETHOD_LIST.equals(tag)) {
                                value = parser.getAttributeValue(null, "att");
                                if (this.DEBUG) {
                                    Log.d(TAG, "inputmethod-app list : " + value);
                                }
                                if (value != null) {
                                    this.mXmlList2.add(value);
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
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    stream = stream2;
                } catch (Exception e3) {
                    e = e3;
                    stream = stream2;
                    try {
                        e.printStackTrace();
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
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
            } catch (Exception e4) {
                e = e4;
                e.printStackTrace();
                if (stream != null) {
                }
            }
        }
    }

    private void initFileObserver() {
        this.mFileObserver = new FileObserverPolicy(SECURE_KEYBOARD_FILE);
        this.mFileObserver.startWatching();
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
