package com.color.util;

import android.app.OppoActivityManager;
import android.content.Context;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class ColorFormatterCompatibilityUtils {
    private static final String COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH = "/data/oppo/coloros/formatercompact/formatter_compatibility_config_list.xml";
    private static final String COLOR_DISPLAY_OPTIMIZATION_DIR = "/data/oppo/coloros/formatercompact";
    public static boolean DEBUG_SWITCH = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int POLICY_OTHERS = 2;
    private static final int POLICY_USE_BLACK_LIST = 1;
    private static final int POLICY_USE_WHITE_LIST = 0;
    private static final String TAG = "ColorFormaterCompact";
    private static final String TAG_BLACK = "black";
    private static final String TAG_ENABLE = "enable_formater_opt";
    private static final String TAG_ENABLE_POLICY = "enable_policy";
    private static final String TAG_EXCLUDE_PROCESS = "excludeProcess";
    private static final String TAG_SPECIAL = "special";
    private static final String TAG_WHITE = "white";
    private static volatile ColorFormatterCompatibilityUtils sDisplayOptUtils = null;
    private static ColorFormatterCompatibilityData sOptimizationData = null;
    private List<String> mBlackList = new ArrayList();
    private Context mContext = null;
    private final Object mDisplayOptBlackListLock = new Object();
    private final Object mDisplayOptEnableLock = new Object();
    private final Object mDisplayOptExcludeProcessListLock = new Object();
    private FileObserverPolicy mDisplayOptFileObserver = null;
    private final Object mDisplayOptPolicyLock = new Object();
    private final Object mDisplayOptSpeicalListLock = new Object();
    private final Object mDisplayOptWhiteListLock = new Object();
    private boolean mEnableDisplatOpt = true;
    private boolean mEnableGraphicAccelerationSwitch = true;
    private int mEnablePolicy = 0;
    private List<String> mExcludeProcessList = new ArrayList();
    private List<String> mSpecialList = new ArrayList();
    private List<String> mWhiteList = new ArrayList();

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.mFocusPath.equals(ColorFormatterCompatibilityUtils.COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH)) {
                Slog.i(ColorFormatterCompatibilityUtils.TAG, "focusPath COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH!");
                ColorFormatterCompatibilityUtils.this.readDisplayOptConfig();
            }
        }
    }

    private ColorFormatterCompatibilityUtils() {
    }

    public static ColorFormatterCompatibilityUtils getInstance() {
        if (sDisplayOptUtils == null) {
            synchronized (ColorFormatterCompatibilityUtils.class) {
                if (sDisplayOptUtils == null) {
                    sDisplayOptUtils = new ColorFormatterCompatibilityUtils();
                }
            }
        }
        return sDisplayOptUtils;
    }

    public void init(Context context) {
        this.mContext = context;
        if (sOptimizationData == null) {
            sOptimizationData = new ColorFormatterCompatibilityData();
        }
        initDir();
        initFileObserver();
        readDisplayOptConfig();
    }

    public ColorFormatterCompatibilityData getOptimizationData() {
        if (sOptimizationData == null) {
            sOptimizationData = new ColorFormatterCompatibilityData();
        }
        return sOptimizationData;
    }

    private void initDir() {
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "initDir start");
        }
        File displayOptDir = new File(COLOR_DISPLAY_OPTIMIZATION_DIR);
        File displayOptConfigFile = new File(COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH);
        try {
            if (!displayOptDir.exists()) {
                displayOptDir.mkdirs();
            }
            if (!displayOptConfigFile.exists()) {
                displayOptConfigFile.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "initDir failed!!!");
            e.printStackTrace();
        }
        changeModFile(COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH);
    }

    private void initFileObserver() {
        this.mDisplayOptFileObserver = new FileObserverPolicy(COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH);
        this.mDisplayOptFileObserver.startWatching();
    }

    private void changeModFile(String fileName) {
        try {
            Runtime.getRuntime().exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Slog.w(TAG, " " + e);
        }
    }

    public void readDisplayOptConfig() {
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "readDisplayOptConfigFile");
        }
        File displayOptConfigFile = new File(COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH);
        if (!displayOptConfigFile.exists()) {
            loadDefaultDisplayOptList();
            Slog.i(TAG, "displayoptconfig file isn't exist!");
        } else if (displayOptConfigFile.length() == 0) {
            loadDefaultDisplayOptList();
        } else {
            readConfigFromFileLocked(displayOptConfigFile);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:145:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0177 A:{SYNTHETIC, Splitter: B:65:0x0177} */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0200 A:{SYNTHETIC, Splitter: B:89:0x0200} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFromFileLocked(File file) {
        Exception e;
        Throwable th;
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "readConfigFromFileLocked start");
        }
        List<String> whitePkglist = new ArrayList();
        List<String> blackPkglist = new ArrayList();
        List<String> specialPkglist = new ArrayList();
        List<String> excludeProcesslist = new ArrayList();
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
                        String tagName = parser.getName();
                        if (DEBUG_SWITCH) {
                            Slog.i(TAG, " readConfigFromFileLocked tagName=" + tagName);
                        }
                        String pkg;
                        if (TAG_ENABLE.equals(tagName)) {
                            String enable = parser.nextText();
                            if (!enable.equals("")) {
                                synchronized (this.mDisplayOptEnableLock) {
                                    this.mEnableDisplatOpt = Boolean.parseBoolean(enable);
                                    sOptimizationData.setDisplatOptEnabled(this.mEnableDisplatOpt);
                                }
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked enable displayopt = " + enable);
                                }
                            }
                        } else if (TAG_ENABLE_POLICY.equals(tagName)) {
                            String enablePolicy = parser.nextText();
                            if (!enablePolicy.equals("")) {
                                synchronized (this.mDisplayOptPolicyLock) {
                                    int policy = Integer.parseInt(enablePolicy);
                                    if (policy == 1 || policy == 0) {
                                        this.mEnablePolicy = policy;
                                        sOptimizationData.setEnablePolicy(this.mEnablePolicy);
                                    }
                                }
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked enable policy = " + enablePolicy);
                                }
                            }
                        } else if (TAG_WHITE.equals(tagName)) {
                            pkg = parser.nextText();
                            if (!pkg.equals("")) {
                                whitePkglist.add(pkg);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked white pkg = " + pkg);
                                }
                            }
                        } else if (TAG_BLACK.equals(tagName)) {
                            pkg = parser.nextText();
                            if (!pkg.equals("")) {
                                blackPkglist.add(pkg);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked black pkg = " + pkg);
                                }
                            }
                        } else if (TAG_SPECIAL.equals(tagName)) {
                            pkg = parser.nextText();
                            if (!pkg.equals("")) {
                                specialPkglist.add(pkg);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked special pkg = " + pkg);
                                }
                            }
                        } else if (TAG_EXCLUDE_PROCESS.equals(tagName)) {
                            String process = parser.nextText();
                            if (!process.equals("")) {
                                excludeProcesslist.add(process);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked exclude process = " + process);
                                }
                            }
                        }
                    }
                } while (type != 1);
                synchronized (this.mDisplayOptWhiteListLock) {
                    this.mWhiteList.clear();
                    this.mWhiteList.addAll(whitePkglist);
                    sOptimizationData.setWhiteList(whitePkglist);
                }
                synchronized (this.mDisplayOptBlackListLock) {
                    this.mBlackList.clear();
                    this.mBlackList.addAll(blackPkglist);
                    sOptimizationData.setBlackList(blackPkglist);
                }
                synchronized (this.mDisplayOptSpeicalListLock) {
                    this.mSpecialList.clear();
                    this.mSpecialList.addAll(specialPkglist);
                    sOptimizationData.setSpecialList(specialPkglist);
                }
                synchronized (this.mDisplayOptExcludeProcessListLock) {
                    this.mExcludeProcessList.clear();
                    this.mExcludeProcessList.addAll(excludeProcesslist);
                    sOptimizationData.setExcludeProcessList(excludeProcesslist);
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e2) {
                        Slog.e(TAG, "Failed to close state FileInputStream " + e2);
                    }
                }
                fileInputStream = stream;
            } catch (Exception e3) {
                e = e3;
                fileInputStream = stream;
                try {
                    Slog.e(TAG, "failed parsing ", e);
                    loadDefaultDisplayOptList();
                    if (fileInputStream == null) {
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
                        Slog.e(TAG, "Failed to close state FileInputStream " + e22);
                    }
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Slog.e(TAG, "failed parsing ", e);
            loadDefaultDisplayOptList();
            if (fileInputStream == null) {
                try {
                    fileInputStream.close();
                } catch (IOException e222) {
                    Slog.e(TAG, "Failed to close state FileInputStream " + e222);
                }
            }
        }
    }

    private String getThis() {
        return toString();
    }

    public boolean isDisplayOptimizationAndSwitchEnabled() {
        boolean enabled = false;
        synchronized (this.mDisplayOptEnableLock) {
            if (this.mEnableDisplatOpt && this.mEnableGraphicAccelerationSwitch) {
                enabled = true;
            }
        }
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "isDisplayOptimizationEnabled = " + enabled);
        }
        return enabled;
    }

    public boolean isOnlyDisplayOptimizationEnabled() {
        boolean enabled = false;
        synchronized (this.mDisplayOptEnableLock) {
            if (this.mEnableDisplatOpt) {
                enabled = true;
            }
        }
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "isOnlyDisplayOptimizationEnabled = " + enabled);
        }
        return enabled;
    }

    public boolean inBlackPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayOptBlackListLock) {
            if (this.mBlackList.contains(pkg)) {
                result = true;
            }
        }
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "inBlackPkgList result = " + result + ",pkg = " + pkg);
        }
        return result;
    }

    public boolean inWhitePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayOptWhiteListLock) {
            if (this.mWhiteList.contains(pkg)) {
                result = true;
            }
        }
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "inWhitePkgList result = " + result + ",pkg = " + pkg);
        }
        return result;
    }

    public boolean inSpecialPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayOptSpeicalListLock) {
            if (this.mSpecialList.contains(pkg)) {
                result = true;
            }
        }
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "inSpecialPkgList result = " + result + ",pkg = " + pkg);
        }
        return result;
    }

    public boolean shouldOptimizeForPkg(String pkg) {
        boolean result = (isOnlyDisplayOptimizationEnabled() && inSpecialPkgList(pkg)) ? true : isDisplayOptimizationAndSwitchEnabled() ? considerPkgAccordingPolicy(pkg) : false;
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "shouldOptimize = " + result + ",pkg = " + pkg);
        }
        return result;
    }

    public boolean considerPkgAccordingPolicy(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayOptPolicyLock) {
            switch (this.mEnablePolicy) {
                case 0:
                    result = inWhitePkgList(pkg);
                    break;
                case 1:
                    result = inBlackPkgList(pkg) ^ 1;
                    break;
            }
        }
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "considerPkgAccordingPolicy = " + result + ",pkg = " + pkg);
        }
        return result;
    }

    public boolean inExcludeProcessList(String process) {
        boolean result = false;
        synchronized (this.mDisplayOptExcludeProcessListLock) {
            if (process != null) {
                for (String p : this.mExcludeProcessList) {
                    if (process.contains(p)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "inExcludeProcessList result = " + result + ",process = " + process);
        }
        return result;
    }

    public boolean shouldExcludeForProcess(String process) {
        boolean result = isOnlyDisplayOptimizationEnabled() ? inExcludeProcessList(process) : false;
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "shouldExcludeForProcess result = " + result + ",process = " + process);
        }
        return result;
    }

    private void loadDefaultDisplayOptList() {
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "loadDefaultDisplayOptList");
        }
        synchronized (this.mDisplayOptWhiteListLock) {
        }
        synchronized (this.mDisplayOptBlackListLock) {
            this.mBlackList.clear();
            this.mBlackList.add("com.sucsoft.RiverMonitorApp");
            this.mBlackList.add("com.yinyuetai.ui");
            this.mBlackList.add("com.gzfns.ecar");
            this.mBlackList.add("com.zjsl.hezzjb");
            sOptimizationData.setBlackList(this.mBlackList);
        }
        synchronized (this.mDisplayOptSpeicalListLock) {
        }
        synchronized (this.mDisplayOptExcludeProcessListLock) {
        }
    }

    public void initData() {
        try {
            ColorFormatterCompatibilityData data = new OppoActivityManager().getFormatterCompatData();
            if (this.mWhiteList != null) {
                this.mWhiteList.clear();
                this.mWhiteList = data.getWhiteList();
            }
            if (this.mBlackList != null) {
                this.mBlackList.clear();
                this.mBlackList = data.getBlackList();
            }
            if (this.mSpecialList != null) {
                this.mSpecialList.clear();
                this.mSpecialList = data.getSpecialList();
            }
            if (this.mExcludeProcessList != null) {
                this.mExcludeProcessList.clear();
                this.mExcludeProcessList = data.getExcludeProcessList();
            }
            this.mEnableDisplatOpt = data.getDisplatOptEnabled();
            this.mEnablePolicy = data.getEnablePolicy();
        } catch (RemoteException e) {
            Slog.e(TAG, "init data error , " + e);
        }
    }
}
