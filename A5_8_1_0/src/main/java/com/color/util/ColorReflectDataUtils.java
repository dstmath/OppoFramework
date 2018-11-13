package com.color.util;

import android.app.OppoActivityManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;

public class ColorReflectDataUtils {
    private static final String COLOR_DIRECT_CONFIG_DIR = "/data/oppo/coloros/colordirect";
    private static final String COLOR_DIRECT_CONFIG_FILE_PATH = "/data/oppo/coloros/colordirect/sys_direct_widget_config_list.xml";
    private static final String TAG = "ColorReflectDataUtils";
    private static final String TAG_ENABLE = "reflect_enable";
    private static final String TAG_RELECT_ATTR_CLASS = "className";
    private static final String TAG_RELECT_ATTR_FIELD = "field";
    private static final String TAG_RELECT_ATTR_LEVEL = "fieldLevel";
    private static final String TAG_RELECT_ATTR_PACKAGE = "packageName";
    private static final String TAG_RELECT_ATTR_VERSION = "versionCode";
    private static final String TAG_RELECT_WIDGET = "reflect_widget";
    private static ColorReflectData mReflectData = null;
    private static ColorReflectDataUtils mReflectUtils = null;
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private final Object mAccidentallyReflectLock = new Object();
    private boolean mHasInit = false;
    private FileObserverPolicy mReflectDataFileObserver = null;
    private boolean mReflectEnable = true;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(ColorReflectDataUtils.COLOR_DIRECT_CONFIG_FILE_PATH)) {
                synchronized (ColorReflectDataUtils.this.mAccidentallyReflectLock) {
                    ColorReflectDataUtils.this.readConfigFile();
                }
            }
        }
    }

    private ColorReflectDataUtils() {
    }

    public static ColorReflectDataUtils getInstance() {
        if (mReflectUtils == null) {
            mReflectUtils = new ColorReflectDataUtils();
        }
        return mReflectUtils;
    }

    public void init() {
        initDir();
        initFileObserver();
        if (mReflectData == null) {
            mReflectData = new ColorReflectData();
        }
        synchronized (this.mAccidentallyReflectLock) {
            readConfigFile();
        }
    }

    public ColorReflectData getInitData(Context context) {
        if (!this.mHasInit) {
            this.mHasInit = true;
            String packageName = context.getPackageName();
            int versionCode = -1;
            try {
                versionCode = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                mReflectData = null;
            }
            if (mReflectData != null && (TextUtils.isEmpty(packageName) ^ 1) != 0 && versionCode > 0) {
                mReflectData.initList(packageName, versionCode);
            } else if (mReflectData != null) {
                mReflectData.setReflectEnable(false);
            }
        }
        return mReflectData;
    }

    public ColorReflectData getData() {
        if (mReflectData == null) {
            mReflectData = new ColorReflectData();
        }
        return mReflectData;
    }

    private void initDir() {
        File directDir = new File(COLOR_DIRECT_CONFIG_DIR);
        File directConfigFile = new File(COLOR_DIRECT_CONFIG_FILE_PATH);
        try {
            if (!directDir.exists()) {
                directDir.mkdirs();
            }
            if (!directConfigFile.exists()) {
                directConfigFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        changeModFile(COLOR_DIRECT_CONFIG_FILE_PATH);
    }

    private void changeModFile(String fileName) {
        try {
            Runtime.getRuntime().exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Log.w(TAG, " " + e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0080 A:{SYNTHETIC, Splitter: B:36:0x0080} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00ca A:{SYNTHETIC, Splitter: B:55:0x00ca} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile() {
        Exception e;
        Throwable th;
        File file = new File(COLOR_DIRECT_CONFIG_FILE_PATH);
        if (file.exists()) {
            if (mReflectData == null) {
                mReflectData = new ColorReflectData();
            } else {
                mReflectData.clearList();
            }
            FileInputStream fileInputStream = null;
            try {
                FileInputStream stream = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream, null);
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            String tag = parser.getName();
                            if (TAG_ENABLE.equals(tag)) {
                                String reflectEnable = parser.nextText();
                                if ("true".equalsIgnoreCase(reflectEnable)) {
                                    this.mReflectEnable = true;
                                } else if ("false".equalsIgnoreCase(reflectEnable)) {
                                    this.mReflectEnable = false;
                                }
                                mReflectData.setReflectEnable(this.mReflectEnable);
                            } else if (TAG_RELECT_WIDGET.equals(tag)) {
                                ColorReflectWidget reflectWidget = new ColorReflectWidget();
                                int attrNum = parser.getAttributeCount();
                                for (int i = 0; i < attrNum; i++) {
                                    String name = parser.getAttributeName(i);
                                    String value = parser.getAttributeValue(i);
                                    if ("packageName".equals(name)) {
                                        reflectWidget.setPackageName(value);
                                    } else if (TAG_RELECT_ATTR_VERSION.equals(name)) {
                                        reflectWidget.setVersionCode(Integer.parseInt(value));
                                    } else if ("className".equals(name)) {
                                        reflectWidget.setClassName(value);
                                    } else if (TAG_RELECT_ATTR_LEVEL.equals(name)) {
                                        reflectWidget.setFieldLevel(Integer.parseInt(value));
                                    } else if (TAG_RELECT_ATTR_FIELD.equals(name)) {
                                        reflectWidget.setField(value);
                                    }
                                }
                                mReflectData.addReflectWidget(reflectWidget);
                            }
                        }
                    } while (type != 1);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    fileInputStream = stream;
                } catch (Exception e3) {
                    e = e3;
                    fileInputStream = stream;
                    try {
                        e.printStackTrace();
                        if (fileInputStream != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = stream;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                e.printStackTrace();
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            }
        }
    }

    private void initFileObserver() {
        this.mReflectDataFileObserver = new FileObserverPolicy(COLOR_DIRECT_CONFIG_FILE_PATH);
        this.mReflectDataFileObserver.startWatching();
    }

    public void initData() {
        try {
            mReflectData = new OppoActivityManager().getReflectData();
        } catch (RemoteException e) {
            mReflectData = null;
            Log.e(TAG, "init data error , " + e);
        }
    }
}
