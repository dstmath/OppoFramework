package com.color.util;

import android.app.OppoActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
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
    /* access modifiers changed from: private */
    public final Object mAccidentallyReflectLock = new Object();
    private boolean mHasInit = false;
    private FileObserverPolicy mReflectDataFileObserver = null;
    private boolean mReflectEnable = true;

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
            initData();
            String packageName = context.getPackageName();
            int versionCode = -1;
            try {
                versionCode = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mReflectData = null;
            }
            if (mReflectData == null || TextUtils.isEmpty(packageName) || versionCode <= 0) {
                ColorReflectData colorReflectData = mReflectData;
                if (colorReflectData != null) {
                    colorReflectData.setReflectEnable(false);
                }
            } else {
                mReflectData.initList(packageName, versionCode);
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
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Log.w(TAG, WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + e);
        }
    }

    /* access modifiers changed from: private */
    public void readConfigFile() {
        int type;
        File file = new File(COLOR_DIRECT_CONFIG_FILE_PATH);
        if (file.exists()) {
            ColorReflectData colorReflectData = mReflectData;
            if (colorReflectData == null) {
                mReflectData = new ColorReflectData();
            } else {
                colorReflectData.clearList();
            }
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
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
                            continue;
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
                            continue;
                        } else {
                            continue;
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
                if (stream != null) {
                    stream.close();
                }
            } catch (Throwable th) {
                if (stream != null) {
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
        this.mReflectDataFileObserver = new FileObserverPolicy(COLOR_DIRECT_CONFIG_FILE_PATH);
        this.mReflectDataFileObserver.startWatching();
    }

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        @Override // android.os.FileObserver
        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(ColorReflectDataUtils.COLOR_DIRECT_CONFIG_FILE_PATH)) {
                synchronized (ColorReflectDataUtils.this.mAccidentallyReflectLock) {
                    ColorReflectDataUtils.this.readConfigFile();
                }
            }
        }
    }

    public void initData() {
        try {
            mReflectData = new OppoActivityManager().getReflectData();
        } catch (RemoteException e) {
            mReflectData = null;
            Log.e(TAG, "init data error , " + e);
        }
    }

    public Field getTextField(Context context, Class<? extends View> viewClass) {
        ColorReflectWidget reflectWidget;
        Field textField = null;
        ColorReflectData reflectData = getInitData(context);
        if (!(reflectData == null || !reflectData.isReflectEnable() || (reflectWidget = reflectData.findWidget(context, context.getPackageName(), viewClass.getName())) == null)) {
            try {
                int level = reflectWidget.getFieldLevel();
                String fieldName = reflectWidget.getField();
                if (level == 0) {
                    textField = viewClass.getDeclaredField(fieldName);
                } else if (level == 1) {
                    textField = viewClass.getSuperclass().getDeclaredField(fieldName);
                } else if (level == 2) {
                    textField = viewClass.getSuperclass().getSuperclass().getDeclaredField(fieldName);
                }
                if (textField != null) {
                    textField.setAccessible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return textField;
    }
}
