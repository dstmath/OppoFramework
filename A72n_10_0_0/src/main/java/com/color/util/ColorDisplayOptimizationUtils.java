package com.color.util;

import android.app.OppoActivityManager;
import android.content.Context;
import android.database.ContentObserver;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.FileObserver;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class ColorDisplayOptimizationUtils {
    private static final String COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH = "/data/oppo/coloros/gamespace/sys_display_opt_config.xml";
    private static final String COLOR_DISPLAY_OPTIMIZATION_DIR = "/data/oppo/coloros/gamespace";
    public static boolean DEBUG_SWITCH = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String GRAPHICS_ACCELERATION_FOR_GAME_SPACE_MODE = "graphics_acceleration_for_game_space_mode";
    private static final int POLICY_OTHERS = 2;
    private static final int POLICY_USE_BLACK_LIST = 1;
    private static final int POLICY_USE_WHITE_LIST = 0;
    private static final int SWITCH_DEFAULT = 1;
    private static final String TAG = "ColorDisplayOptimizationUtils";
    private static final String TAG_BLACK = "black";
    private static final String TAG_ENABLE = "enable_display_opt";
    private static final String TAG_ENABLE_POLICY = "enable_policy";
    private static final String TAG_EXCLUDE_PROCESS = "excludeProcess";
    private static final String TAG_EXCLUDE_WINDOW = "excludeWindow";
    private static final String TAG_SPECIAL = "special";
    private static final String TAG_WHITE = "white";
    private static volatile ColorDisplayOptimizationUtils sDisplayOptUtils = null;
    private static ColorDisplayOptimizationData sOptimizationData = null;
    private List<String> mBlackList = new ArrayList();
    private Context mContext = null;
    private final Object mDisplayOptBlackListLock = new Object();
    private final Object mDisplayOptEnableLock = new Object();
    private final Object mDisplayOptExcludeProcessListLock = new Object();
    private final Object mDisplayOptExcludeWindowListLock = new Object();
    private FileObserverPolicy mDisplayOptFileObserver = null;
    private final Object mDisplayOptPolicyLock = new Object();
    private final Object mDisplayOptSpeicalListLock = new Object();
    private final Object mDisplayOptWhiteListLock = new Object();
    private boolean mEnableDisplatOpt = true;
    private boolean mEnableGraphicAccelerationSwitch = true;
    private int mEnablePolicy = 0;
    private List<String> mExcludeProcessList = new ArrayList();
    private List<String> mExcludeWindowList = new ArrayList();
    private SwitchObserverPolicy mGraphicAccelerationSwitchObserver = null;
    private List<String> mSpecialList = new ArrayList();
    private List<String> mWhiteList = new ArrayList();

    private ColorDisplayOptimizationUtils() {
    }

    public static ColorDisplayOptimizationUtils getInstance() {
        if (sDisplayOptUtils == null) {
            synchronized (ColorDisplayOptimizationUtils.class) {
                if (sDisplayOptUtils == null) {
                    sDisplayOptUtils = new ColorDisplayOptimizationUtils();
                }
            }
        }
        return sDisplayOptUtils;
    }

    public void init(Context context) {
        this.mContext = context;
        if (sOptimizationData == null) {
            sOptimizationData = new ColorDisplayOptimizationData();
        }
        if (context.getPackageManager().hasSystemFeature("oppo.graphics.acceleration.not.support")) {
            synchronized (this.mDisplayOptEnableLock) {
                this.mEnableDisplatOpt = false;
                sOptimizationData.setDisplatOptEnabled(this.mEnableDisplatOpt);
            }
            return;
        }
        initDir();
        initFileObserver();
        initSwitchObserver();
        readDisplayOptConfig();
        updateGraphicAccelerationSwitch();
    }

    public ColorDisplayOptimizationData getOptimizationData() {
        if (sOptimizationData == null) {
            sOptimizationData = new ColorDisplayOptimizationData();
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

    private void initSwitchObserver() {
        this.mGraphicAccelerationSwitchObserver = new SwitchObserverPolicy();
        Context context = this.mContext;
        if (context != null) {
            context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(GRAPHICS_ACCELERATION_FOR_GAME_SPACE_MODE), true, this.mGraphicAccelerationSwitchObserver);
        }
    }

    private void changeModFile(String fileName) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Slog.w(TAG, WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + e);
        }
    }

    public void readDisplayOptConfig() {
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "readDisplayOptConfigFile");
        }
        File displayOptConfigFile = new File(COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH);
        if (!displayOptConfigFile.exists()) {
            Slog.i(TAG, "displayoptconfig file isn't exist!");
        } else if (displayOptConfigFile.length() == 0) {
            loadDefaultDisplayOptList();
        } else {
            readConfigFromFileLocked(displayOptConfigFile);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:141:0x02a0 A[SYNTHETIC, Splitter:B:141:0x02a0] */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x02b0 A[SYNTHETIC, Splitter:B:146:0x02b0] */
    /* JADX WARNING: Removed duplicated region for block: B:154:? A[RETURN, SYNTHETIC] */
    private void readConfigFromFileLocked(File file) {
        Exception e;
        IOException e2;
        StringBuilder sb;
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "readConfigFromFileLocked start");
        }
        List<String> whitePkglist = new ArrayList<>();
        List<String> blackPkglist = new ArrayList<>();
        List<String> specialPkglist = new ArrayList<>();
        List<String> excludeProcesslist = new ArrayList<>();
        List<String> excludeWindowlist = new ArrayList<>();
        FileInputStream stream = null;
        try {
            try {
                FileInputStream stream2 = new FileInputStream(file);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                while (true) {
                    int type = parser.next();
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (DEBUG_SWITCH) {
                            Slog.i(TAG, " readConfigFromFileLocked tagName=" + tagName);
                        }
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
                            String pkg = parser.nextText();
                            if (!pkg.equals("")) {
                                whitePkglist.add(pkg);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked white pkg = " + pkg);
                                }
                            }
                        } else if (TAG_BLACK.equals(tagName)) {
                            String pkg2 = parser.nextText();
                            if (!pkg2.equals("")) {
                                blackPkglist.add(pkg2);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked black pkg = " + pkg2);
                                }
                            }
                        } else if (TAG_SPECIAL.equals(tagName)) {
                            String pkg3 = parser.nextText();
                            if (!pkg3.equals("")) {
                                specialPkglist.add(pkg3);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked special pkg = " + pkg3);
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
                        } else if (TAG_EXCLUDE_WINDOW.equals(tagName)) {
                            String window = parser.nextText();
                            if (!window.equals("")) {
                                excludeWindowlist.add(window);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked exclude window = " + window);
                                }
                            }
                        }
                    }
                    if (type == 1) {
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
                        synchronized (this.mDisplayOptExcludeWindowListLock) {
                            this.mExcludeWindowList.clear();
                            this.mExcludeWindowList.addAll(excludeWindowlist);
                            sOptimizationData.setExcludeWindowList(excludeWindowlist);
                        }
                        try {
                            stream2.close();
                            return;
                        } catch (IOException e3) {
                            e2 = e3;
                            sb = new StringBuilder();
                        }
                    }
                }
            } catch (Exception e4) {
                e = e4;
                try {
                    Slog.e(TAG, "failed parsing ", e);
                    loadDefaultDisplayOptList();
                    if (0 == 0) {
                    }
                } catch (Throwable th) {
                    th = th;
                    if (0 != 0) {
                    }
                    throw th;
                }
            }
            sb.append("Failed to close state FileInputStream ");
            sb.append(e2);
            Slog.e(TAG, sb.toString());
        } catch (Exception e5) {
            e = e5;
            Slog.e(TAG, "failed parsing ", e);
            loadDefaultDisplayOptList();
            if (0 == 0) {
                try {
                    stream.close();
                } catch (IOException e6) {
                    e2 = e6;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th2) {
            th = th2;
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    Slog.e(TAG, "Failed to close state FileInputStream " + e7);
                }
            }
            throw th;
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
        return enabled;
    }

    public boolean isOnlyDisplayOptimizationEnabled() {
        boolean enabled = false;
        synchronized (this.mDisplayOptEnableLock) {
            if (this.mEnableDisplatOpt) {
                enabled = true;
            }
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
        return result;
    }

    public boolean inWhitePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayOptWhiteListLock) {
            if (this.mWhiteList.contains(pkg)) {
                result = true;
            }
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
        return result;
    }

    public boolean shouldOptimizeForPkg(String pkg) {
        boolean result = (isOnlyDisplayOptimizationEnabled() && inSpecialPkgList(pkg)) || (isDisplayOptimizationAndSwitchEnabled() && considerPkgAccordingPolicy(pkg));
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "shouldOptimize = " + result + ",pkg = " + pkg);
        }
        return result;
    }

    public boolean considerPkgAccordingPolicy(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayOptPolicyLock) {
            int i = this.mEnablePolicy;
            if (i == 0) {
                result = inWhitePkgList(pkg);
            } else if (i == 1) {
                result = !inBlackPkgList(pkg);
            }
        }
        return result;
    }

    public boolean inExcludeProcessList(String process) {
        boolean result = false;
        synchronized (this.mDisplayOptExcludeProcessListLock) {
            if (process != null) {
                Iterator<String> it = this.mExcludeProcessList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (process.contains(it.next())) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public boolean inExcludeWindowList(String window) {
        boolean result = false;
        synchronized (this.mDisplayOptExcludeWindowListLock) {
            if (window != null) {
                Iterator<String> it = this.mExcludeWindowList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (window.contains(it.next())) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public boolean shouldExcludeForProcess(String process) {
        return isOnlyDisplayOptimizationEnabled() && inExcludeProcessList(process);
    }

    public boolean shouldExcludeForWindow(String window) {
        return isOnlyDisplayOptimizationEnabled() && inExcludeWindowList(window);
    }

    private void loadDefaultDisplayOptList() {
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "loadDefaultDisplayOptList");
        }
        synchronized (this.mDisplayOptWhiteListLock) {
        }
        synchronized (this.mDisplayOptBlackListLock) {
            this.mBlackList.clear();
            this.mBlackList.add("com.oppo.launcher");
            this.mBlackList.add("com.android.launcher");
        }
        synchronized (this.mDisplayOptSpeicalListLock) {
        }
        synchronized (this.mDisplayOptExcludeProcessListLock) {
        }
        synchronized (this.mDisplayOptExcludeWindowListLock) {
        }
    }

    public void initData() {
        try {
            ColorDisplayOptimizationData data = new OppoActivityManager().getDisplayOptimizationData();
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
            if (this.mExcludeWindowList != null) {
                this.mExcludeWindowList.clear();
                this.mExcludeWindowList = data.getExcludeWindowList();
            }
            if (this.mExcludeProcessList != null) {
                this.mExcludeProcessList.clear();
                this.mExcludeProcessList = data.getExcludeProcessList();
            }
            this.mEnableDisplatOpt = data.getDisplatOptEnabled();
            this.mEnableGraphicAccelerationSwitch = data.getGraphicAccelerationSwitchEnabled();
            this.mEnablePolicy = data.getEnablePolicy();
        } catch (RemoteException e) {
            Slog.e(TAG, "init data error , " + e);
        }
    }

    /* access modifiers changed from: private */
    public class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        @Override // android.os.FileObserver
        public void onEvent(int event, String path) {
            if (event == 8 && this.mFocusPath.equals(ColorDisplayOptimizationUtils.COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH)) {
                Slog.i(ColorDisplayOptimizationUtils.TAG, "focusPath COLOR_DISPLAY_OPTIMIZATION_CONFIG_FILE_PATH!");
                ColorDisplayOptimizationUtils.this.readDisplayOptConfig();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateGraphicAccelerationSwitch() {
        int result;
        Context context = this.mContext;
        if (context != null) {
            try {
                result = Settings.Global.getInt(context.getContentResolver(), GRAPHICS_ACCELERATION_FOR_GAME_SPACE_MODE);
                if (DEBUG_SWITCH) {
                    Slog.i(TAG, "reading Settings result = " + result);
                }
            } catch (Settings.SettingNotFoundException e) {
                result = 1;
                if (DEBUG_SWITCH) {
                    Slog.i(TAG, "SettingNotFoundException");
                }
            }
            boolean z = true;
            if (result != 1) {
                z = false;
            }
            this.mEnableGraphicAccelerationSwitch = z;
            ColorDisplayOptimizationData colorDisplayOptimizationData = sOptimizationData;
            if (colorDisplayOptimizationData != null) {
                colorDisplayOptimizationData.setGraphicAccelerationSwitchEnabled(this.mEnableGraphicAccelerationSwitch);
            }
            if (DEBUG_SWITCH) {
                Slog.i(TAG, "updateGraphicAccelerationSwitch = " + this.mEnableGraphicAccelerationSwitch);
            }
        }
    }

    /* access modifiers changed from: private */
    public class SwitchObserverPolicy extends ContentObserver {
        public SwitchObserverPolicy() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            ColorDisplayOptimizationUtils.this.updateGraphicAccelerationSwitch();
            super.onChange(selfChange);
        }
    }
}
