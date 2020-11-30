package com.android.server.wm;

import android.content.Context;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.pm.Settings;
import com.android.server.voiceinteraction.DatabaseHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorAppSwitchSettings {
    private static final String APP_SWITCH_CONFIG_DIR_PATH = "data/oppo/coloros/appswitch/";
    private static final String APP_SWITCH_CONFIG_FILE_PATH = "data/oppo/coloros/appswitch/sys_app_switch_config.xml";
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String DEFAULT_APP_SWITCH_CONFIG_DIR_PATH = "system/oppo/sys_app_switch_config.xml";
    private static final int OTA_VERSION_CHECK_DELAY_TIME = 3000;
    private static final String TAG = "ColorAppSwitchSettings";
    private boolean mAppSwitchEnable = true;
    private List<String> mBlackList = new ArrayList();
    private Context mContext;
    FileObserverPolicy mFileObserverPolicy;
    private OnConfigChangedListener mListener;
    private List<String> mMatchActivityDefaultList = new ArrayList();
    private List<String> mMatchAppDefaultList = new ArrayList();
    private File mSettingsFile;
    private Object mSettingsLock = new Object();
    private List<ColorAppSwitchRuleInfo> mStaticRuleList = new ArrayList();
    private int mVersion;
    private Handler mWorkHandler;
    XmlPullParser mXmlPullParser;

    public interface OnConfigChangedListener {
        void onConfigChanged();
    }

    public ColorAppSwitchSettings(OnConfigChangedListener listener, Context context) {
        this.mListener = listener;
        this.mContext = context;
    }

    public Map<String, ColorAppSwitchRuleInfo> getConfigRuleInfos() {
        Map<String, ColorAppSwitchRuleInfo> staticRules = new HashMap<>();
        for (ColorAppSwitchRuleInfo staticRule : this.mStaticRuleList) {
            staticRules.put(staticRule.pkgName, staticRule);
        }
        return staticRules;
    }

    public List<String> getBlackList() {
        return this.mBlackList;
    }

    public List<String> getMatchAppDefaultList() {
        return this.mMatchAppDefaultList;
    }

    public List<String> getMatchActivityDefaultList() {
        return this.mMatchActivityDefaultList;
    }

    public void init() {
        this.mWorkHandler = new Handler();
        createFile();
        readConfigFromFile(true);
        this.mFileObserverPolicy = new FileObserverPolicy(APP_SWITCH_CONFIG_FILE_PATH);
        this.mFileObserverPolicy.startWatching();
    }

    private void createFile() {
        File dataConfigPath = new File(APP_SWITCH_CONFIG_DIR_PATH);
        File configFile = new File(APP_SWITCH_CONFIG_FILE_PATH);
        File defaultFile = new File(DEFAULT_APP_SWITCH_CONFIG_DIR_PATH);
        try {
            if (!dataConfigPath.exists()) {
                dataConfigPath.mkdirs();
            }
            if (!configFile.exists()) {
                configFile.createNewFile();
                FileUtils.copyFile(defaultFile, configFile);
                return;
            }
            checkOtaVersion();
        } catch (Exception e) {
            e.printStackTrace();
            Slog.e(TAG, " init config dir error");
        }
    }

    private void checkOtaVersion() {
        try {
            this.mWorkHandler.postDelayed(new Runnable() {
                /* class com.android.server.wm.ColorAppSwitchSettings.AnonymousClass1 */

                public void run() {
                    int version;
                    try {
                        File defaultFile = new File(ColorAppSwitchSettings.DEFAULT_APP_SWITCH_CONFIG_DIR_PATH);
                        if (defaultFile.exists() && (version = ColorAppSwitchSettings.this.parseXmlVersion(defaultFile)) > ColorAppSwitchSettings.this.mVersion && ColorAppSwitchSettings.this.mVersion > 0) {
                            Slog.e(ColorAppSwitchSettings.TAG, "checkOtaVersion current = " + ColorAppSwitchSettings.this.mVersion + " ota version = " + version);
                            FileUtils.copyFile(defaultFile, new File(ColorAppSwitchSettings.APP_SWITCH_CONFIG_FILE_PATH));
                            ColorAppSwitchSettings.this.readConfigFromFile(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Slog.e(ColorAppSwitchSettings.TAG, "checkOtaVersion checkOtaVersion error ");
                    }
                }
            }, BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
        } catch (Exception e) {
            e.printStackTrace();
            Slog.e(TAG, "checkOtaVersion error");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readConfigFromFile(boolean notify) {
        OnConfigChangedListener onConfigChangedListener;
        loadRuleList();
        if (notify && (onConfigChangedListener = this.mListener) != null) {
            onConfigChangedListener.onConfigChanged();
        }
    }

    /* access modifiers changed from: private */
    public class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (ColorAppSwitchSettings.DEBUG) {
                Slog.d(ColorAppSwitchSettings.TAG, "onEvent event = " + event + "path = " + path);
            }
            if (event == 8 && this.mFocusPath.equals(ColorAppSwitchSettings.APP_SWITCH_CONFIG_FILE_PATH)) {
                Slog.i(ColorAppSwitchSettings.TAG, "mFocusPath APP_SWITCH_CONFIG_FILE_PATH!");
                ColorAppSwitchSettings.this.readConfigFromFile(true);
            }
        }
    }

    private void loadRuleList() {
        reset();
        File file = new File(APP_SWITCH_CONFIG_FILE_PATH);
        if (DEBUG) {
            Slog.i(TAG, " file = data/oppo/coloros/appswitch/sys_app_switch_config.xml exist = " + file.exists());
        }
        if (file.exists()) {
            if (DEBUG) {
                Slog.i(TAG, "load file rule list");
            }
            parseXml(file);
            return;
        }
        Slog.i(TAG, "load file failed, file not found");
    }

    private void reset() {
        this.mBlackList.clear();
        this.mMatchAppDefaultList.clear();
        this.mMatchActivityDefaultList.clear();
        this.mStaticRuleList.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int parseXmlVersion(File file) {
        StringBuilder sb;
        int type;
        FileInputStream fis = null;
        int xmlVersion = -1;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            this.mXmlPullParser = Xml.newPullParser();
            this.mXmlPullParser.setInput(fis2, StandardCharsets.UTF_8.name());
            do {
                type = this.mXmlPullParser.next();
                if (!(type == 0 || type == 1)) {
                    if (type != 2) {
                        if (type != 3) {
                            continue;
                        } else {
                            String tag1 = this.mXmlPullParser.getName();
                            if (DEBUG) {
                                Slog.i(TAG, "parseXmlVersion     END_TAG---" + tag1);
                                continue;
                            } else {
                                continue;
                            }
                        }
                    } else if ("version".equalsIgnoreCase(this.mXmlPullParser.getName())) {
                        String version = getValue("version");
                        Slog.i(TAG, "parseXmlVersion     version = " + version);
                        if (!TextUtils.isEmpty(version)) {
                            xmlVersion = Integer.parseInt(version);
                        }
                        try {
                            fis2.close();
                        } catch (IOException e) {
                            Slog.e(TAG, "close config file failed: " + e.getMessage());
                            e.printStackTrace();
                        }
                        return xmlVersion;
                    }
                }
            } while (type != 1);
            try {
                fis2.close();
            } catch (IOException e2) {
                e = e2;
                sb = new StringBuilder();
            }
        } catch (IOException e3) {
            Slog.e(TAG, "parseXmlVersion config IOException: " + e3.getMessage());
            e3.printStackTrace();
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e4) {
                    e = e4;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e5) {
            e5.printStackTrace();
            Slog.e(TAG, "parseXmlVersion config XmlPullParserException: " + e5.getMessage());
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e6) {
                    e = e6;
                    sb = new StringBuilder();
                }
            }
        } catch (Exception e7) {
            e7.printStackTrace();
            Slog.e(TAG, "parseXmlVersion config Exception: " + e7.getMessage());
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e8) {
                    e = e8;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e9) {
                    Slog.e(TAG, "close config file failed: " + e9.getMessage());
                    e9.printStackTrace();
                }
            }
            throw th;
        }
        return -1;
        sb.append("close config file failed: ");
        sb.append(e.getMessage());
        Slog.e(TAG, sb.toString());
        e.printStackTrace();
        return -1;
    }

    private void parseXml(File file) {
        StringBuilder sb;
        int type;
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            this.mXmlPullParser = Xml.newPullParser();
            this.mXmlPullParser.setInput(fis2, StandardCharsets.UTF_8.name());
            do {
                type = this.mXmlPullParser.next();
                if (!(type == 0 || type == 1)) {
                    if (type == 2) {
                        String tag = this.mXmlPullParser.getName();
                        if ("blackItem".equalsIgnoreCase(tag)) {
                            String blackItem = getValue("blackItem");
                            if (!TextUtils.isEmpty(blackItem)) {
                                this.mBlackList.add(blackItem);
                            }
                            if (DEBUG) {
                                Slog.i(TAG, "parsexml  blackItem START_ attr == " + blackItem);
                            }
                        } else if ("matchAppDefault".equalsIgnoreCase(tag)) {
                            String matchAppDefault = getValue("matchAppDefault");
                            if (!TextUtils.isEmpty(matchAppDefault)) {
                                this.mMatchAppDefaultList.add(matchAppDefault);
                            }
                            if (DEBUG) {
                                Slog.i(TAG, "parsexml  whileItem START_ attr == " + matchAppDefault);
                            }
                        } else if ("matchActivityDefault".equalsIgnoreCase(tag)) {
                            String matchActivityDefault = getValue("matchActivityDefault");
                            if (!TextUtils.isEmpty(matchActivityDefault)) {
                                this.mMatchActivityDefaultList.add(matchActivityDefault);
                            }
                            if (DEBUG) {
                                Slog.i(TAG, "parsexml  whileItem START_ attr == " + matchActivityDefault);
                            }
                        } else if ("version".equalsIgnoreCase(tag)) {
                            String version = getValue("version");
                            if (!TextUtils.isEmpty(version)) {
                                int v = Integer.parseInt(version);
                                if (v < this.mVersion) {
                                    Slog.e(TAG, "version downgraded mVersion = " + this.mVersion + " ,new version = " + version);
                                    reset();
                                    try {
                                        fis2.close();
                                        return;
                                    } catch (IOException e) {
                                        Slog.e(TAG, "close config file failed: " + e.getMessage());
                                        e.printStackTrace();
                                        return;
                                    }
                                } else {
                                    this.mVersion = v;
                                    Slog.i(TAG, "current ColorAppSwitchConfig version: " + this.mVersion);
                                }
                            } else {
                                reset();
                                try {
                                    fis2.close();
                                    return;
                                } catch (IOException e2) {
                                    Slog.e(TAG, "close config file failed: " + e2.getMessage());
                                    e2.printStackTrace();
                                    return;
                                }
                            }
                        }
                        getRules();
                        continue;
                    } else if (type != 3) {
                        continue;
                    } else {
                        String tag1 = this.mXmlPullParser.getName();
                        if (DEBUG) {
                            Slog.i(TAG, "parsexml     END_TAG---" + tag1);
                            continue;
                        } else {
                            continue;
                        }
                    }
                }
            } while (type != 1);
            try {
                fis2.close();
                return;
            } catch (IOException e3) {
                e = e3;
                sb = new StringBuilder();
            }
            sb.append("close config file failed: ");
            sb.append(e.getMessage());
            Slog.e(TAG, sb.toString());
            e.printStackTrace();
        } catch (IOException e4) {
            Slog.e(TAG, "parse xml config IOException: " + e4.getMessage());
            e4.printStackTrace();
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e6) {
            e6.printStackTrace();
            Slog.e(TAG, "parse xml config XmlPullParserException: " + e6.getMessage());
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e7) {
                    e = e7;
                    sb = new StringBuilder();
                }
            }
        } catch (Exception e8) {
            e8.printStackTrace();
            Slog.e(TAG, "parse xml config Exception: " + e8.getMessage());
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e9) {
                    e = e9;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e10) {
                    Slog.e(TAG, "close config file failed: " + e10.getMessage());
                    e10.printStackTrace();
                }
            }
            throw th;
        }
    }

    private String getValue(String key) {
        try {
            if (!key.equalsIgnoreCase(this.mXmlPullParser.getName())) {
                return null;
            }
            String value = this.mXmlPullParser.nextText();
            if (DEBUG) {
                Slog.i(TAG, "parsexml    key =" + key + " value =" + value);
            }
            return value;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private void getConfiguration(ColorAppSwitchRuleInfo ruleInfo) throws Exception {
        try {
            if ("config".equalsIgnoreCase(this.mXmlPullParser.getName())) {
                if (DEBUG) {
                    Slog.i(TAG, "parsexml    getObject = config");
                }
                int type = 1;
                List<String> packageList = new ArrayList<>();
                int eventType = this.mXmlPullParser.getEventType();
                String value = this.mXmlPullParser.getName();
                while (true) {
                    if (eventType == 3) {
                        if ("config".equalsIgnoreCase(value)) {
                            break;
                        }
                    }
                    if (eventType == 2) {
                        String temp = getValue(DatabaseHelper.SoundModelContract.KEY_TYPE);
                        if (!TextUtils.isEmpty(temp) && 2 == Integer.parseInt(temp)) {
                            type = 2;
                        }
                        String pkg = getValue(Settings.ATTR_NAME);
                        if (!TextUtils.isEmpty(pkg)) {
                            packageList.add(pkg);
                        }
                    }
                    eventType = this.mXmlPullParser.next();
                    value = this.mXmlPullParser.getName();
                }
                if (packageList.size() > 0) {
                    ruleInfo.config.addAppConfig(type, packageList);
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    private void getRules() throws Exception {
        try {
            if ("rule".equalsIgnoreCase(this.mXmlPullParser.getName())) {
                if (DEBUG) {
                    Slog.i(TAG, "parsexml getObject = rule");
                }
                ColorAppSwitchRuleInfo ruleInfo = ColorAppSwitchRuleInfo.buildStaticRuleInfo(this.mContext);
                String packageName = null;
                int eventType = this.mXmlPullParser.getEventType();
                String value = this.mXmlPullParser.getName();
                while (true) {
                    if (eventType == 3) {
                        if ("rule".equalsIgnoreCase(value)) {
                            break;
                        }
                    }
                    if (eventType == 2) {
                        if (packageName == null) {
                            packageName = getValue("pkg");
                        }
                        getConfiguration(ruleInfo);
                    }
                    eventType = this.mXmlPullParser.next();
                    value = this.mXmlPullParser.getName();
                }
                if (!TextUtils.isEmpty(packageName)) {
                    ruleInfo.setStaticPackageAndObserver(packageName);
                    this.mStaticRuleList.add(ruleInfo);
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}
