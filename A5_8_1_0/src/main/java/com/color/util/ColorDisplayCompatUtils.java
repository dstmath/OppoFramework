package com.color.util;

import android.app.OppoActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.os.FileObserver;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.BrowserContract;
import android.provider.CalendarContract;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import android.view.inputmethod.InputMethod;
import com.android.internal.content.PackageMonitor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class ColorDisplayCompatUtils {
    private static final String COLOR_DISPLAY_COMPAT_CONFIG_DIR = "/data/oppo/coloros/displaycompat";
    private static final String COLOR_DISPLAY_COMPAT_CONFIG_FILE_PATH = "/data/oppo/coloros/displaycompat/sys_display_compat_config.xml";
    public static boolean DEBUG_SWITCH = false;
    private static final float DEFAULT_PRE_O_MAX_ASPECT_RATIO = 1.86f;
    private static final String KEY_LOCAL_COMPAT_APPS = "key_display_compat_local_apps_v1";
    private static final String KEY_LOCAL_FULLSCREEN_APPS = "key_display_fullscreen_local_apps_v1";
    private static final String KEY_LOCAL_IMMERSIVE_APPS = "key_display_immersive_local_apps";
    private static final String KEY_LOCAL_NONIMMERSIVE_APPS = "key_display_nonimmersive_local_apps";
    private static final String KEY_SHOW_FULLSCREEN_DIALOG_APPS = "key_display_show_dialog_local_apps";
    private static final String TAG = "ColorDisplayCompatUtils";
    private static final String TAG_BLACK = "black";
    private static final String TAG_COMPAT = "compat";
    private static final String TAG_ENABLE = "enable_display_compat";
    private static final String TAG_ENABLE_IMMERSIVE = "enable_display_immersive";
    private static final String TAG_IMMERSIVE = "immersive";
    private static final String TAG_NONIMMERSIVE = "nonimmersive";
    private static final String TAG_SIZE = "size";
    private static final String TAG_WHITE = "white";
    private static final String VERSION_NAME_EMPTY = "empty";
    private static ColorDisplayCompatData sCompatData = null;
    private static volatile ColorDisplayCompatUtils sDisplayCompatUtils = null;
    private static List<String> sExcludeImmersivedList = new ArrayList();
    private static List<String> sIncludeImmersiveList = new ArrayList();
    private List<String> mAlreadyShowDialogAppsList = new ArrayList();
    private List<String> mBlackList = new ArrayList();
    private HashMap<String, String> mCompatPackageList = new HashMap();
    private Context mContext = null;
    private final Object mDisplayCompatBlackListLock = new Object();
    private final Object mDisplayCompatEnableLock = new Object();
    private FileObserverPolicy mDisplayCompatFileObserver = null;
    private final Object mDisplayCompatPackageListLock = new Object();
    private final Object mDisplayCompatWhiteListLock = new Object();
    private final Object mDisplayImmersiveDefaultLock = new Object();
    private final Object mDisplayInstalledCompatAppsListLock = new Object();
    private final Object mDisplayInstalledImeListLock = new Object();
    private final Object mDisplayInstalledThirdPartyAppListLock = new Object();
    private final Object mDisplayLocalCompatAppsListLock = new Object();
    private final Object mDisplayLocalFullscreenAppsListLock = new Object();
    private final Object mDisplayLocalImmersiveAppsListLock = new Object();
    private final Object mDisplayLocalNonImmersiveAppsListLock = new Object();
    private final Object mDisplayNeedAdjustSizeListLock = new Object();
    private final Object mDisplayRusImmersiveListLock = new Object();
    private final Object mDisplayRusNonImmersiveListLock = new Object();
    private final Object mDisplayShowDialogListLock = new Object();
    private boolean mEnableDisplayCompat = true;
    private boolean mHasHeteromorphismFeature = false;
    private final Object mHeteromorphismFeatureLock = new Object();
    private boolean mImmersiveDefault = true;
    private List<String> mInstalledCompatList = new ArrayList();
    private List<String> mInstalledImeList = new ArrayList();
    private List<String> mInstalledThirdPartyAppList = new ArrayList();
    private List<String> mLocalCompatAppsList = new ArrayList();
    private LocalCompatSettingsObserverPolicy mLocalCompatSettingsObserver = null;
    private List<String> mLocalFullScreenAppsList = new ArrayList();
    private List<String> mLocalImmersiveAppsList = new ArrayList();
    private LocalImmersiveSettingsObserverPolicy mLocalImmersiveSettingsObserver = null;
    private List<String> mLocalNonImmersiveAppsList = new ArrayList();
    private LocalShowDialogSettingsObserverPolicy mLocalShowDialogSettingsObserver = null;
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor(this, null);
    private List<String> mNeedAdjustSizeAppList = new ArrayList();
    private PackageManager mPackageManager = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ColorDisplayCompatUtils.this.isOnlyDisplayCompatEnabled() && "android.intent.action.BOOT_COMPLETED".equals(action)) {
                ColorDisplayCompatUtils.this.loadInstalledImeAppList();
            }
        }
    };
    private List<String> mRusImmersiveAppsList = new ArrayList();
    private List<String> mRusNonImmersiveAppsList = new ArrayList();
    private List<String> mWhiteList = new ArrayList();

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.mFocusPath.equals(ColorDisplayCompatUtils.COLOR_DISPLAY_COMPAT_CONFIG_FILE_PATH)) {
                Slog.i(ColorDisplayCompatUtils.TAG, "focusPath COLOR_DISPLAY_COMPAT_CONFIG_FILE_PATH!");
                ColorDisplayCompatUtils.this.readDisplayCompatConfig();
            }
        }
    }

    private class LocalCompatSettingsObserverPolicy extends ContentObserver {
        public LocalCompatSettingsObserverPolicy() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            ColorDisplayCompatUtils.this.loadLocalCompatAppList();
            ColorDisplayCompatUtils.this.loadLocalFullScreenAppList();
            super.onChange(selfChange);
        }
    }

    private class LocalImmersiveSettingsObserverPolicy extends ContentObserver {
        public LocalImmersiveSettingsObserverPolicy() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            ColorDisplayCompatUtils.this.loadLocalImmersiveAppList();
            ColorDisplayCompatUtils.this.loadLocalNonImmersiveAppList();
            super.onChange(selfChange);
        }
    }

    private class LocalShowDialogSettingsObserverPolicy extends ContentObserver {
        public LocalShowDialogSettingsObserverPolicy() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            ColorDisplayCompatUtils.this.loadLocalShowDialogAppList();
            super.onChange(selfChange);
        }
    }

    private class MyPackageMonitor extends PackageMonitor {
        /* synthetic */ MyPackageMonitor(ColorDisplayCompatUtils this$0, MyPackageMonitor -this1) {
            this();
        }

        private MyPackageMonitor() {
        }

        public void onPackageRemoved(String packageName, int uid) {
            if (!TextUtils.isEmpty(packageName)) {
                ColorDisplayCompatUtils.this.loadInstalledImeAppList();
                if (ColorDisplayCompatUtils.this.inInstalledCompatPkgList(packageName)) {
                    synchronized (ColorDisplayCompatUtils.this.mDisplayInstalledCompatAppsListLock) {
                        ColorDisplayCompatUtils.this.mInstalledCompatList.remove(packageName);
                        ColorDisplayCompatUtils.sCompatData.setInstalledCompatList(ColorDisplayCompatUtils.this.mInstalledCompatList);
                    }
                }
                if (ColorDisplayCompatUtils.this.inInstalledThirdPartyAppList(packageName)) {
                    synchronized (ColorDisplayCompatUtils.this.mDisplayInstalledThirdPartyAppListLock) {
                        ColorDisplayCompatUtils.this.mInstalledThirdPartyAppList.remove(packageName);
                        ColorDisplayCompatUtils.sCompatData.setInstalledThirdPartyAppList(ColorDisplayCompatUtils.this.mInstalledThirdPartyAppList);
                    }
                }
                ColorDisplayCompatUtils.this.removeLocalShowDialogListForPkg(packageName);
            }
        }

        public void onPackageAdded(String packageName, int uid) {
            if (!TextUtils.isEmpty(packageName)) {
                ColorDisplayCompatUtils.this.loadInstalledImeAppList();
                if (!(ColorDisplayCompatUtils.this.inInstalledImeList(packageName) || (ColorDisplayCompatUtils.this.supportFullScreen(packageName) ^ 1) == 0)) {
                    synchronized (ColorDisplayCompatUtils.this.mDisplayInstalledCompatAppsListLock) {
                        ColorDisplayCompatUtils.this.mInstalledCompatList.add(packageName);
                        ColorDisplayCompatUtils.sCompatData.setInstalledCompatList(ColorDisplayCompatUtils.this.mInstalledCompatList);
                    }
                }
                if (!ColorDisplayCompatUtils.this.inInstalledThirdPartyAppList(packageName) && ColorDisplayCompatUtils.this.isInstalledThirdPartyApp(packageName)) {
                    synchronized (ColorDisplayCompatUtils.this.mDisplayInstalledThirdPartyAppListLock) {
                        ColorDisplayCompatUtils.this.mInstalledThirdPartyAppList.add(packageName);
                        ColorDisplayCompatUtils.sCompatData.setInstalledThirdPartyAppList(ColorDisplayCompatUtils.this.mInstalledThirdPartyAppList);
                    }
                }
            }
        }

        public void onPackageModified(String packageName) {
            if (!TextUtils.isEmpty(packageName)) {
                ColorDisplayCompatUtils.this.loadInstalledImeAppList();
                if (!ColorDisplayCompatUtils.this.inInstalledImeList(packageName)) {
                    boolean supportFullscreen = ColorDisplayCompatUtils.this.supportFullScreen(packageName);
                    synchronized (ColorDisplayCompatUtils.this.mDisplayInstalledCompatAppsListLock) {
                        if (supportFullscreen) {
                            if (ColorDisplayCompatUtils.this.inInstalledCompatPkgList(packageName)) {
                                ColorDisplayCompatUtils.this.mInstalledCompatList.remove(packageName);
                            }
                        }
                        if (!(supportFullscreen || (ColorDisplayCompatUtils.this.inInstalledCompatPkgList(packageName) ^ 1) == 0)) {
                            ColorDisplayCompatUtils.this.mInstalledCompatList.add(packageName);
                        }
                        ColorDisplayCompatUtils.sCompatData.setInstalledCompatList(ColorDisplayCompatUtils.this.mInstalledCompatList);
                    }
                }
            }
        }
    }

    static {
        sIncludeImmersiveList.clear();
        sIncludeImmersiveList.add("com.nearme.gamecenter.ddz.nearme.gamecenter");
        sIncludeImmersiveList.add("com.oppo.reader");
        sIncludeImmersiveList.add("com.oppo.book");
        sIncludeImmersiveList.add(BrowserContract.AUTHORITY);
        sIncludeImmersiveList.add("com.google.android.inputmethod.latin");
        sIncludeImmersiveList.add("com.emoji.keyboard.touchpal");
        sIncludeImmersiveList.add("com.oppo.cameracom.android.ctslocker");
        sExcludeImmersivedList.clear();
        sExcludeImmersivedList.add("com.android.calculator2");
        sExcludeImmersivedList.add(CalendarContract.AUTHORITY);
        sExcludeImmersivedList.add("com.ctsi.emm");
        sExcludeImmersivedList.add("com.justsy.launcher");
        sExcludeImmersivedList.add("com.justsy.portal");
        sExcludeImmersivedList.add("com.justsy.mdm");
        sExcludeImmersivedList.add("com.ss.android.ugc.aweme");
    }

    private ColorDisplayCompatUtils() {
    }

    public static ColorDisplayCompatUtils getInstance() {
        if (sDisplayCompatUtils == null) {
            synchronized (ColorDisplayCompatUtils.class) {
                if (sDisplayCompatUtils == null) {
                    sDisplayCompatUtils = new ColorDisplayCompatUtils();
                }
            }
        }
        return sDisplayCompatUtils;
    }

    public void init(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        if (sCompatData == null) {
            sCompatData = new ColorDisplayCompatData();
        }
        if (context.getPackageManager().hasSystemFeature("oppo.display.compat.support")) {
            if (context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism")) {
                synchronized (this.mHeteromorphismFeatureLock) {
                    this.mHasHeteromorphismFeature = true;
                    sCompatData.setHasHeteromorphismFeature(this.mHasHeteromorphismFeature);
                }
            }
            initDir();
            initFileObserver();
            initLocalCompatSettingsObserver();
            initLocalImmersiveSettingsObserver();
            initLocalShowDialogSettingsObserver();
            loadLocalCompatAppList();
            loadLocalFullScreenAppList();
            loadLocalImmersiveAppList();
            loadLocalNonImmersiveAppList();
            loadInstalledImeAppList();
            loadLocalShowDialogAppList();
            loadInstalledCompatAppList();
            loadInstalledThirdPartyApps();
            readDisplayCompatConfig();
            registerPackageMonitor();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            this.mContext.registerReceiver(this.mReceiver, filter);
            return;
        }
        synchronized (this.mDisplayCompatEnableLock) {
            this.mEnableDisplayCompat = false;
            sCompatData.setDisplatOptEnabled(this.mEnableDisplayCompat);
        }
    }

    public void initData(Context context) {
        this.mContext = context;
        if (this.mContext != null) {
            this.mPackageManager = this.mContext.getPackageManager();
        }
        initData();
    }

    public void initData() {
        try {
            ColorDisplayCompatData data = new OppoActivityManager().getDisplayCompatData();
            synchronized (this.mDisplayCompatEnableLock) {
                this.mEnableDisplayCompat = data.getDisplayCompatEnabled();
            }
            synchronized (this.mHeteromorphismFeatureLock) {
                this.mHasHeteromorphismFeature = data.hasHeteromorphismFeature();
            }
            synchronized (this.mDisplayCompatWhiteListLock) {
                if (this.mWhiteList != null) {
                    this.mWhiteList.clear();
                    this.mWhiteList = data.getWhiteList();
                }
            }
            synchronized (this.mDisplayCompatBlackListLock) {
                if (this.mBlackList != null) {
                    this.mBlackList.clear();
                    this.mBlackList = data.getBlackList();
                }
            }
            synchronized (this.mDisplayLocalCompatAppsListLock) {
                if (this.mLocalCompatAppsList != null) {
                    this.mLocalCompatAppsList.clear();
                    this.mLocalCompatAppsList = data.getLocalCompatList();
                }
            }
            synchronized (this.mDisplayLocalFullscreenAppsListLock) {
                if (this.mLocalFullScreenAppsList != null) {
                    this.mLocalFullScreenAppsList.clear();
                    this.mLocalFullScreenAppsList = data.getLocalFullScreenList();
                }
            }
            synchronized (this.mDisplayLocalImmersiveAppsListLock) {
                if (this.mLocalImmersiveAppsList != null) {
                    this.mLocalImmersiveAppsList.clear();
                    this.mLocalImmersiveAppsList = data.getLocalImmersiveList();
                }
            }
            synchronized (this.mDisplayLocalNonImmersiveAppsListLock) {
                if (this.mLocalNonImmersiveAppsList != null) {
                    this.mLocalNonImmersiveAppsList.clear();
                    this.mLocalNonImmersiveAppsList = data.getLocalNonImmersiveList();
                }
            }
            synchronized (this.mDisplayCompatPackageListLock) {
                if (this.mCompatPackageList != null) {
                    this.mCompatPackageList.clear();
                    this.mCompatPackageList = data.getCompatPackageList();
                }
            }
            synchronized (this.mDisplayInstalledCompatAppsListLock) {
                if (this.mInstalledCompatList != null) {
                    this.mInstalledCompatList.clear();
                    this.mInstalledCompatList = data.getInstalledCompatList();
                }
            }
            synchronized (this.mDisplayInstalledImeListLock) {
                if (this.mInstalledImeList != null) {
                    this.mInstalledImeList.clear();
                    this.mInstalledImeList = data.getInstalledImeList();
                }
            }
            synchronized (this.mDisplayShowDialogListLock) {
                if (this.mAlreadyShowDialogAppsList != null) {
                    this.mAlreadyShowDialogAppsList.clear();
                    this.mAlreadyShowDialogAppsList = data.getShowDialogAppList();
                }
            }
            synchronized (this.mDisplayImmersiveDefaultLock) {
                this.mImmersiveDefault = data.getRusImmersiveDefault();
            }
            synchronized (this.mDisplayRusImmersiveListLock) {
                if (this.mRusImmersiveAppsList != null) {
                    this.mRusImmersiveAppsList.clear();
                    this.mRusImmersiveAppsList = data.getRusImmersiveList();
                }
            }
            synchronized (this.mDisplayRusNonImmersiveListLock) {
                if (this.mRusNonImmersiveAppsList != null) {
                    this.mRusNonImmersiveAppsList.clear();
                    this.mRusNonImmersiveAppsList = data.getRusNonImmersiveList();
                }
            }
            synchronized (this.mDisplayInstalledThirdPartyAppListLock) {
                if (this.mInstalledThirdPartyAppList != null) {
                    this.mInstalledThirdPartyAppList.clear();
                    this.mInstalledThirdPartyAppList = data.getInstalledThirdPartyAppList();
                }
            }
            synchronized (this.mDisplayNeedAdjustSizeListLock) {
                if (this.mNeedAdjustSizeAppList != null) {
                    this.mNeedAdjustSizeAppList.clear();
                    this.mNeedAdjustSizeAppList = data.getNeedAdjustSizeAppList();
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "init data error , " + e);
        }
    }

    public ColorDisplayCompatData getDisplayCompatData() {
        if (sCompatData == null) {
            sCompatData = new ColorDisplayCompatData();
        }
        return sCompatData;
    }

    private void initDir() {
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "initDir start");
        }
        File dislayCompatDir = new File(COLOR_DISPLAY_COMPAT_CONFIG_DIR);
        File displayCompatConfigFile = new File(COLOR_DISPLAY_COMPAT_CONFIG_FILE_PATH);
        try {
            if (!dislayCompatDir.exists()) {
                dislayCompatDir.mkdirs();
            }
            if (!displayCompatConfigFile.exists()) {
                displayCompatConfigFile.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "initDir failed!!!");
            e.printStackTrace();
        }
        changeModFile(COLOR_DISPLAY_COMPAT_CONFIG_FILE_PATH);
    }

    private void initFileObserver() {
        this.mDisplayCompatFileObserver = new FileObserverPolicy(COLOR_DISPLAY_COMPAT_CONFIG_FILE_PATH);
        this.mDisplayCompatFileObserver.startWatching();
    }

    private void initLocalCompatSettingsObserver() {
        this.mLocalCompatSettingsObserver = new LocalCompatSettingsObserverPolicy();
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(KEY_LOCAL_COMPAT_APPS), true, this.mLocalCompatSettingsObserver);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(KEY_LOCAL_FULLSCREEN_APPS), true, this.mLocalCompatSettingsObserver);
        }
    }

    private void initLocalImmersiveSettingsObserver() {
        this.mLocalImmersiveSettingsObserver = new LocalImmersiveSettingsObserverPolicy();
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(KEY_LOCAL_NONIMMERSIVE_APPS), true, this.mLocalImmersiveSettingsObserver);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(KEY_LOCAL_IMMERSIVE_APPS), true, this.mLocalImmersiveSettingsObserver);
        }
    }

    private void initLocalShowDialogSettingsObserver() {
        this.mLocalShowDialogSettingsObserver = new LocalShowDialogSettingsObserverPolicy();
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(KEY_SHOW_FULLSCREEN_DIALOG_APPS), true, this.mLocalShowDialogSettingsObserver);
        }
    }

    private void changeModFile(String fileName) {
        try {
            Runtime.getRuntime().exec("chmod 766 " + fileName);
        } catch (IOException e) {
            Slog.w(TAG, " " + e);
        }
    }

    public void readDisplayCompatConfig() {
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "readDisplayCompatConfigFile");
        }
        File displayCompatConfigFile = new File(COLOR_DISPLAY_COMPAT_CONFIG_FILE_PATH);
        if (!displayCompatConfigFile.exists()) {
            Slog.i(TAG, "displaycompatconfig file isn't exist!");
        } else if (displayCompatConfigFile.length() == 0) {
            loadDefaultDisplayCompatList();
        } else {
            readConfigFromFileLocked(displayCompatConfigFile);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:178:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x01f0 A:{SYNTHETIC, Splitter: B:77:0x01f0} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0257 A:{SYNTHETIC, Splitter: B:92:0x0257} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFromFileLocked(File file) {
        Exception e;
        Throwable th;
        if (DEBUG_SWITCH) {
            Slog.i(TAG, "readConfigFromFileLocked start");
        }
        List<String> whitePkglist = new ArrayList();
        List<String> blackPkglist = new ArrayList();
        HashMap<String, String> compatPkgList = new HashMap();
        List<String> immersivePkglist = new ArrayList();
        List<String> nonImmersivePkglist = new ArrayList();
        List<String> needAdjustSizeList = new ArrayList();
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
                        String enable;
                        String pkg;
                        if (TAG_ENABLE.equals(tagName)) {
                            enable = parser.nextText();
                            if (!enable.equals("")) {
                                synchronized (this.mDisplayCompatEnableLock) {
                                    this.mEnableDisplayCompat = Boolean.parseBoolean(enable);
                                    sCompatData.setDisplatOptEnabled(this.mEnableDisplayCompat);
                                }
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked enable displaycompat = " + enable);
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
                        } else if (TAG_COMPAT.equals(tagName)) {
                            pkg = parser.getAttributeValue(null, "package");
                            if (pkg != null) {
                                String versionName = parser.getAttributeValue(null, "versionName");
                                if (TextUtils.isEmpty(versionName)) {
                                    versionName = VERSION_NAME_EMPTY;
                                }
                                compatPkgList.put(pkg, versionName);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked compat pkg = " + pkg + ",versionName = " + versionName);
                                }
                            }
                        } else if (TAG_ENABLE_IMMERSIVE.equals(tagName)) {
                            enable = parser.nextText();
                            if (!enable.equals("")) {
                                synchronized (this.mDisplayImmersiveDefaultLock) {
                                    this.mImmersiveDefault = Boolean.parseBoolean(enable);
                                    sCompatData.setRusImmersiveDefault(this.mImmersiveDefault);
                                }
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked enable mImmersiveDefault = " + enable);
                                }
                            }
                        } else if (TAG_IMMERSIVE.equals(tagName)) {
                            pkg = parser.nextText();
                            if (!pkg.equals("")) {
                                immersivePkglist.add(pkg);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked immersive pkg = " + pkg);
                                }
                            }
                        } else if (TAG_NONIMMERSIVE.equals(tagName)) {
                            pkg = parser.nextText();
                            if (!pkg.equals("")) {
                                nonImmersivePkglist.add(pkg);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked nonImmersive pkg = " + pkg);
                                }
                            }
                        } else if ("size".equals(tagName)) {
                            pkg = parser.nextText();
                            if (!pkg.equals("")) {
                                needAdjustSizeList.add(pkg);
                                if (DEBUG_SWITCH) {
                                    Slog.i(TAG, " readConfigFromFileLocked needAdjustSize pkg = " + pkg);
                                }
                            }
                        }
                    }
                } while (type != 1);
                synchronized (this.mDisplayCompatWhiteListLock) {
                    this.mWhiteList.clear();
                    this.mWhiteList.addAll(whitePkglist);
                    sCompatData.setWhiteList(this.mWhiteList);
                }
                synchronized (this.mDisplayCompatBlackListLock) {
                    this.mBlackList.clear();
                    this.mBlackList.addAll(blackPkglist);
                    sCompatData.setBlackList(this.mBlackList);
                }
                synchronized (this.mDisplayCompatPackageListLock) {
                    this.mCompatPackageList.clear();
                    this.mCompatPackageList.putAll(compatPkgList);
                    sCompatData.setCompatPackageList(this.mCompatPackageList);
                }
                synchronized (this.mDisplayRusImmersiveListLock) {
                    this.mRusImmersiveAppsList.clear();
                    this.mRusImmersiveAppsList.addAll(immersivePkglist);
                    sCompatData.setRusImmersiveList(this.mRusImmersiveAppsList);
                }
                synchronized (this.mDisplayRusNonImmersiveListLock) {
                    this.mRusNonImmersiveAppsList.clear();
                    this.mRusNonImmersiveAppsList.addAll(nonImmersivePkglist);
                    sCompatData.setRusNonImmersiveList(this.mRusNonImmersiveAppsList);
                }
                synchronized (this.mDisplayNeedAdjustSizeListLock) {
                    this.mNeedAdjustSizeAppList.clear();
                    this.mNeedAdjustSizeAppList.addAll(needAdjustSizeList);
                    sCompatData.setNeedAdjustSizeAppList(this.mNeedAdjustSizeAppList);
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
                    loadDefaultDisplayCompatList();
                    if (fileInputStream == null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            Slog.e(TAG, "Failed to close state FileInputStream " + e22);
                        }
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
                    } catch (IOException e222) {
                        Slog.e(TAG, "Failed to close state FileInputStream " + e222);
                    }
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Slog.e(TAG, "failed parsing ", e);
            loadDefaultDisplayCompatList();
            if (fileInputStream == null) {
            }
        }
    }

    private void registerPackageMonitor() {
        if (this.mContext != null) {
            this.mMyPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
        }
    }

    private String getThis() {
        return toString();
    }

    private String getVersionNameFromCompatPkg(String pkg) {
        String versionName = VERSION_NAME_EMPTY;
        return (String) this.mCompatPackageList.get(pkg);
    }

    public boolean isOnlyDisplayCompatEnabled() {
        boolean enabled = false;
        synchronized (this.mDisplayCompatEnableLock) {
            if (this.mEnableDisplayCompat) {
                enabled = true;
            }
        }
        return enabled;
    }

    public boolean hasHeteromorphismFeature() {
        boolean enabled = false;
        synchronized (this.mHeteromorphismFeatureLock) {
            if (this.mHasHeteromorphismFeature) {
                enabled = true;
            }
        }
        return enabled;
    }

    public boolean getImmersiveDefault() {
        boolean enabled;
        synchronized (this.mDisplayImmersiveDefaultLock) {
            enabled = this.mImmersiveDefault;
        }
        return enabled;
    }

    public boolean inWhitePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayCompatWhiteListLock) {
            if (this.mWhiteList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inBlackPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayCompatBlackListLock) {
            if (this.mBlackList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inRusImmersivePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayRusImmersiveListLock) {
            if (this.mRusImmersiveAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inRusNonImmersivePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayRusNonImmersiveListLock) {
            if (this.mRusNonImmersiveAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inLocalCompatPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayLocalCompatAppsListLock) {
            if (this.mLocalCompatAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inLocalFullScreenPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayLocalFullscreenAppsListLock) {
            if (this.mLocalFullScreenAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inLocalNonImmersivePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayLocalNonImmersiveAppsListLock) {
            if (this.mLocalNonImmersiveAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inLocalImmersivePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayLocalImmersiveAppsListLock) {
            if (this.mLocalImmersiveAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inInstalledCompatPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayInstalledCompatAppsListLock) {
            if (this.mInstalledCompatList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inInstalledThirdPartyAppList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayInstalledThirdPartyAppListLock) {
            if (this.mInstalledThirdPartyAppList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inInstalledImeList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayInstalledImeListLock) {
            if (this.mInstalledImeList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inAlreadyShowDialogList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayShowDialogListLock) {
            if (this.mAlreadyShowDialogAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inNeedAdujstSizeList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayNeedAdjustSizeListLock) {
            if (this.mNeedAdjustSizeAppList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inCompatPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mDisplayCompatPackageListLock) {
            if (this.mCompatPackageList.containsKey(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean needCompatPkgByVersionName(String pkg) {
        String versionName = getVersionNameFromCompatPkg(pkg);
        if (versionName == null) {
            return true;
        }
        if (VERSION_NAME_EMPTY.equals(versionName)) {
            return true;
        }
        try {
            return compareVersion(getVersionNameFromInstalledPkg(pkg), versionName) < 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean shouldCompatAdjustForPkg(String pkg) {
        if (!isOnlyDisplayCompatEnabled()) {
            return false;
        }
        if (inInstalledImeList(pkg)) {
            return false;
        }
        if (inBlackPkgList(pkg)) {
            return false;
        }
        if (inWhitePkgList(pkg)) {
            return true;
        }
        if (inLocalFullScreenPkgList(pkg)) {
            return false;
        }
        if (inInstalledCompatPkgList(pkg)) {
            return true;
        }
        return false;
    }

    public boolean shouldNonImmersiveAdjustForPkg(String pkg) {
        boolean result;
        if (getImmersiveDefault()) {
            result = false;
        } else {
            result = inInstalledThirdPartyAppList(pkg);
        }
        if (inLocalImmersivePkgList(pkg)) {
            return false;
        }
        if (inLocalNonImmersivePkgList(pkg)) {
            return true;
        }
        if (inRusImmersivePkgList(pkg)) {
            return false;
        }
        if (inRusNonImmersivePkgList(pkg)) {
            return true;
        }
        if (shouldCompatAdjustForPkg(pkg)) {
            return true;
        }
        return result;
    }

    public boolean shouldHideFullscreenButtonForPkg(String pkg) {
        if (inWhitePkgList(pkg)) {
            return true;
        }
        return false;
    }

    public boolean shouldShowFullscreenDialogForPkg(String pkg) {
        if (inAlreadyShowDialogList(pkg)) {
            return false;
        }
        return true;
    }

    public boolean shouldAdjustRealSizeForPkg(String pkg) {
        if (inNeedAdujstSizeList(pkg)) {
            return true;
        }
        return false;
    }

    public void updateLocalAppsListForPkg(String pkg) {
        if (this.mContext != null) {
            StringBuilder pkgList;
            synchronized (this.mDisplayLocalCompatAppsListLock) {
                if (this.mLocalCompatAppsList.contains(pkg)) {
                    this.mLocalCompatAppsList.remove(pkg);
                    pkgList = new StringBuilder();
                    for (String s : this.mLocalCompatAppsList) {
                        pkgList.append(s).append(",");
                    }
                    Global.putString(this.mContext.getContentResolver(), KEY_LOCAL_COMPAT_APPS, pkgList.toString());
                }
            }
            synchronized (this.mDisplayLocalFullscreenAppsListLock) {
                if (!this.mLocalFullScreenAppsList.contains(pkg)) {
                    this.mLocalFullScreenAppsList.add(pkg);
                    pkgList = new StringBuilder();
                    for (String s2 : this.mLocalFullScreenAppsList) {
                        pkgList.append(s2).append(",");
                    }
                    Global.putString(this.mContext.getContentResolver(), KEY_LOCAL_FULLSCREEN_APPS, pkgList.toString());
                }
            }
        }
    }

    public void updateLocalImmersiveListForPkg(String pkg) {
        if (this.mContext != null) {
            synchronized (this.mDisplayLocalNonImmersiveAppsListLock) {
                if (!this.mLocalNonImmersiveAppsList.contains(pkg)) {
                    this.mLocalNonImmersiveAppsList.remove(pkg);
                    StringBuilder pkgList = new StringBuilder();
                    for (String s : this.mLocalNonImmersiveAppsList) {
                        pkgList.append(s).append(",");
                    }
                    Global.putString(this.mContext.getContentResolver(), KEY_LOCAL_NONIMMERSIVE_APPS, pkgList.toString());
                }
            }
        }
    }

    public void updateLocalShowDialogListForPkg(String pkg) {
        if (this.mContext != null) {
            synchronized (this.mDisplayShowDialogListLock) {
                if (!this.mAlreadyShowDialogAppsList.contains(pkg)) {
                    this.mAlreadyShowDialogAppsList.add(pkg);
                    StringBuilder pkgList = new StringBuilder();
                    for (String s : this.mAlreadyShowDialogAppsList) {
                        pkgList.append(s).append(",");
                    }
                    Global.putString(this.mContext.getContentResolver(), KEY_SHOW_FULLSCREEN_DIALOG_APPS, pkgList.toString());
                }
            }
        }
    }

    public void removeLocalShowDialogListForPkg(String pkg) {
        if (this.mContext != null) {
            synchronized (this.mDisplayShowDialogListLock) {
                if (this.mAlreadyShowDialogAppsList.contains(pkg)) {
                    this.mAlreadyShowDialogAppsList.remove(pkg);
                    StringBuilder pkgList = new StringBuilder();
                    for (String s : this.mAlreadyShowDialogAppsList) {
                        pkgList.append(s).append(",");
                    }
                    Global.putString(this.mContext.getContentResolver(), KEY_SHOW_FULLSCREEN_DIALOG_APPS, pkgList.toString());
                }
            }
        }
    }

    private void loadDefaultDisplayCompatList() {
        synchronized (this.mDisplayCompatWhiteListLock) {
        }
        synchronized (this.mDisplayCompatBlackListLock) {
            if (this.mBlackList != null) {
                this.mBlackList.clear();
                this.mBlackList.add("com.justsy.launcher");
                this.mBlackList.add("com.justsy.portal");
                this.mBlackList.add("com.justsy.mdm");
                this.mBlackList.add("com.ctsi.emm");
                if (sCompatData != null) {
                    sCompatData.setBlackList(this.mBlackList);
                }
            }
        }
        synchronized (this.mDisplayRusNonImmersiveListLock) {
            if (this.mRusNonImmersiveAppsList != null) {
                this.mRusNonImmersiveAppsList.clear();
                this.mRusNonImmersiveAppsList.add("com.walkgame.ismarttv");
                this.mRusNonImmersiveAppsList.add("net.fetnet.fetvod");
                this.mRusNonImmersiveAppsList.add("com.justsy.launcher");
                this.mRusNonImmersiveAppsList.add("com.justsy.portal");
                this.mRusNonImmersiveAppsList.add("com.justsy.mdm");
                if (sCompatData != null) {
                    sCompatData.setRusNonImmersiveList(this.mRusNonImmersiveAppsList);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0041  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int compareVersion(String left, String right) {
        int i = 0;
        if (left.equals(right)) {
            return 0;
        }
        int result;
        int leftStart = 0;
        int rightStart = 0;
        do {
            String substring;
            int leftEnd = left.indexOf(46, leftStart);
            int rightEnd = right.indexOf(46, rightStart);
            Integer leftValue = Integer.valueOf(Integer.parseInt(leftEnd < 0 ? left.substring(leftStart) : left.substring(leftStart, leftEnd)));
            if (rightEnd < 0) {
                substring = right.substring(rightStart);
            } else {
                substring = right.substring(rightStart, rightEnd);
            }
            result = leftValue.compareTo(Integer.valueOf(Integer.parseInt(substring)));
            leftStart = leftEnd + 1;
            rightStart = rightEnd + 1;
            if (result != 0 || leftStart <= 0) {
                if (result == 0) {
                    if (leftStart > rightStart) {
                        int i2;
                        if (containsNonZeroValue(left, leftStart)) {
                            i2 = 1;
                        } else {
                            i2 = 0;
                        }
                        return i2;
                    } else if (leftStart < rightStart) {
                        if (containsNonZeroValue(right, rightStart)) {
                            i = -1;
                        }
                        return i;
                    }
                }
            }
        } while (rightStart > 0);
        if (result == 0) {
        }
        return result;
    }

    private boolean containsNonZeroValue(String str, int beginIndex) {
        for (int i = beginIndex; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c != '0' && c != '.') {
                return true;
            }
        }
        return false;
    }

    private String getVersionNameFromInstalledPkg(String pkg) {
        String versionName = String.valueOf(Integer.MAX_VALUE);
        if (this.mPackageManager == null) {
            return versionName;
        }
        try {
            return this.mPackageManager.getPackageInfo(pkg, 0).versionName;
        } catch (NameNotFoundException e) {
            return versionName;
        }
    }

    /* JADX WARNING: Missing block: B:19:0x004b, code:
            if ((r5 ^ 1) != 0) goto L_0x004d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean supportFullScreen(String packageName) {
        float maxAspectRatio = DEFAULT_PRE_O_MAX_ASPECT_RATIO;
        int privateFlags = 0;
        if (this.mPackageManager != null) {
            try {
                if (!(TextUtils.isEmpty(packageName) || (inInstalledImeList(packageName) ^ 1) == 0)) {
                    if (!sIncludeImmersiveList.contains(packageName)) {
                        int i;
                        if (packageName.startsWith("com.oppo") || packageName.startsWith("com.coloros") || packageName.startsWith("com.nearme") || packageName.startsWith("com.cootek.smartinputv5.language")) {
                            i = 1;
                        } else {
                            i = sExcludeImmersivedList.contains(packageName);
                        }
                    }
                    PackageInfo pkgInfo = this.mPackageManager.getPackageInfo(packageName, 8192);
                    if (pkgInfo != null && (pkgInfo.applicationInfo.flags & 1) == 0) {
                        maxAspectRatio = pkgInfo.applicationInfo.maxAspectRatio;
                        privateFlags = pkgInfo.applicationInfo.privateFlags;
                    }
                }
            } catch (NameNotFoundException e) {
            }
        }
        if (maxAspectRatio >= DEFAULT_PRE_O_MAX_ASPECT_RATIO || (privateFlags & 1024) != 0) {
            return true;
        }
        return (privateFlags & 4096) != 0;
    }

    private boolean isInstalledThirdPartyApp(String packageName) {
        if (this.mPackageManager == null) {
            return false;
        }
        try {
            if (TextUtils.isEmpty(packageName)) {
                return false;
            }
            if (!sIncludeImmersiveList.contains(packageName)) {
                int i;
                if (packageName.startsWith("com.oppo") || packageName.startsWith("com.coloros") || packageName.startsWith("com.nearme") || packageName.startsWith("com.cootek.smartinputv5.language")) {
                    i = 1;
                } else {
                    i = sExcludeImmersivedList.contains(packageName);
                }
                if ((i ^ 1) == 0) {
                    return false;
                }
            }
            PackageInfo pkgInfo = this.mPackageManager.getPackageInfo(packageName, 8192);
            if (pkgInfo == null) {
                return false;
            }
            if (sIncludeImmersiveList.contains(packageName) || (pkgInfo.applicationInfo.flags & 1) == 0) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void loadLocalCompatAppList() {
        if (this.mContext != null) {
            String pkglist = Global.getString(this.mContext.getContentResolver(), KEY_LOCAL_COMPAT_APPS);
            if (pkglist != null) {
                synchronized (this.mDisplayLocalCompatAppsListLock) {
                    this.mLocalCompatAppsList.clear();
                    this.mLocalCompatAppsList = new ArrayList(Arrays.asList(pkglist.split(",")));
                    sCompatData.setLocalCompatList(this.mLocalCompatAppsList);
                }
            }
        }
    }

    private void loadLocalFullScreenAppList() {
        if (this.mContext != null) {
            String fullScreenlist = Global.getString(this.mContext.getContentResolver(), KEY_LOCAL_FULLSCREEN_APPS);
            if (fullScreenlist != null) {
                synchronized (this.mDisplayLocalFullscreenAppsListLock) {
                    this.mLocalFullScreenAppsList.clear();
                    this.mLocalFullScreenAppsList = new ArrayList(Arrays.asList(fullScreenlist.split(",")));
                    sCompatData.setLocalFullScreenList(this.mLocalFullScreenAppsList);
                }
            }
        }
    }

    private void loadLocalImmersiveAppList() {
        if (this.mContext != null) {
            String pkgList = Global.getString(this.mContext.getContentResolver(), KEY_LOCAL_IMMERSIVE_APPS);
            if (pkgList != null) {
                synchronized (this.mDisplayLocalImmersiveAppsListLock) {
                    this.mLocalImmersiveAppsList.clear();
                    this.mLocalImmersiveAppsList = new ArrayList(Arrays.asList(pkgList.split(",")));
                    sCompatData.setLocalImmersiveList(this.mLocalImmersiveAppsList);
                }
            }
        }
    }

    private void loadLocalNonImmersiveAppList() {
        if (this.mContext != null) {
            String pkgList = Global.getString(this.mContext.getContentResolver(), KEY_LOCAL_NONIMMERSIVE_APPS);
            if (pkgList != null) {
                synchronized (this.mDisplayLocalNonImmersiveAppsListLock) {
                    this.mLocalNonImmersiveAppsList.clear();
                    this.mLocalNonImmersiveAppsList = new ArrayList(Arrays.asList(pkgList.split(",")));
                    sCompatData.setLocalNonImmersiveList(this.mLocalNonImmersiveAppsList);
                }
            }
        }
    }

    private void loadLocalShowDialogAppList() {
        if (this.mContext != null) {
            String pkgList = Global.getString(this.mContext.getContentResolver(), KEY_SHOW_FULLSCREEN_DIALOG_APPS);
            if (pkgList != null) {
                synchronized (this.mDisplayShowDialogListLock) {
                    this.mAlreadyShowDialogAppsList.clear();
                    this.mAlreadyShowDialogAppsList = new ArrayList(Arrays.asList(pkgList.split(",")));
                    sCompatData.setShowDialogAppList(this.mAlreadyShowDialogAppsList);
                }
            }
        }
    }

    private void loadInstalledImeAppList() {
        if (this.mContext != null) {
            long begin = System.currentTimeMillis();
            List<String> imeList = new ArrayList();
            try {
                List<ResolveInfo> list = this.mContext.getPackageManager().queryIntentServices(new Intent(InputMethod.SERVICE_INTERFACE), 131200);
                if (list != null) {
                    int listSize = list.size();
                    for (int i = 0; i < listSize; i++) {
                        ResolveInfo resolveInfo = (ResolveInfo) list.get(i);
                        if (resolveInfo != null) {
                            imeList.add(resolveInfo.serviceInfo.packageName);
                        }
                    }
                }
            } catch (Exception e) {
            }
            synchronized (this.mDisplayInstalledImeListLock) {
                this.mInstalledImeList.clear();
                this.mInstalledImeList.addAll(imeList);
                sCompatData.setInstalledImeList(this.mInstalledImeList);
            }
            Slog.i(TAG, "loadInstalledImeAppList time cost =" + (System.currentTimeMillis() - begin));
        }
    }

    private void loadInstalledCompatAppList() {
        if (this.mContext != null) {
            long begin = System.currentTimeMillis();
            List<String> thirdPartyNeedCompatAppsList = new ArrayList();
            try {
                for (PackageInfo packageInfo : this.mContext.getPackageManager().getInstalledPackages(0)) {
                    if (packageInfo != null) {
                        try {
                            String packageName = packageInfo.packageName;
                            if (!(TextUtils.isEmpty(packageName) || (inInstalledImeList(packageName) ^ 1) == 0)) {
                                if (!sIncludeImmersiveList.contains(packageName)) {
                                    int i;
                                    if (packageName.startsWith("com.oppo") || packageName.startsWith("com.coloros") || packageName.startsWith("com.nearme") || packageName.startsWith("com.cootek.smartinputv5.language")) {
                                        i = 1;
                                    } else {
                                        i = sExcludeImmersivedList.contains(packageName);
                                    }
                                    if ((i ^ 1) == 0) {
                                    }
                                }
                                PackageInfo pkg = packageInfo;
                                if (packageInfo != null && (packageInfo.applicationInfo.flags & 1) == 0) {
                                    float maxAspectRatio = packageInfo.applicationInfo.maxAspectRatio;
                                    int privateFlags = packageInfo.applicationInfo.privateFlags;
                                    boolean supportFullscreen = (maxAspectRatio >= DEFAULT_PRE_O_MAX_ASPECT_RATIO || (privateFlags & 1024) != 0) ? true : (privateFlags & 4096) != 0;
                                    if (!supportFullscreen) {
                                        thirdPartyNeedCompatAppsList.add(packageName);
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e2) {
            }
            synchronized (this.mDisplayInstalledCompatAppsListLock) {
                this.mInstalledCompatList.clear();
                this.mInstalledCompatList.addAll(thirdPartyNeedCompatAppsList);
                sCompatData.setInstalledCompatList(this.mInstalledCompatList);
            }
            Slog.i(TAG, "loadInstalledCompatAppList time cost =" + (System.currentTimeMillis() - begin));
        }
    }

    private void loadInstalledThirdPartyApps() {
        if (this.mContext != null) {
            List<String> thirdPartyAppsList = new ArrayList();
            try {
                for (PackageInfo packageInfo : this.mContext.getPackageManager().getInstalledPackages(0)) {
                    if (packageInfo != null) {
                        try {
                            String packageName = packageInfo.packageName;
                            if (!TextUtils.isEmpty(packageName)) {
                                if (!sIncludeImmersiveList.contains(packageName)) {
                                    int i;
                                    if (packageName.startsWith("com.oppo") || packageName.startsWith("com.coloros") || packageName.startsWith("com.nearme") || packageName.startsWith("com.cootek.smartinputv5.language")) {
                                        i = 1;
                                    } else {
                                        i = sExcludeImmersivedList.contains(packageName);
                                    }
                                    if ((i ^ 1) == 0) {
                                    }
                                }
                                PackageInfo pkg = packageInfo;
                                if (packageInfo != null && (sIncludeImmersiveList.contains(packageName) || (packageInfo.applicationInfo.flags & 1) == 0)) {
                                    thirdPartyAppsList.add(packageName);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e2) {
            }
            synchronized (this.mDisplayInstalledThirdPartyAppListLock) {
                this.mInstalledThirdPartyAppList.clear();
                this.mInstalledThirdPartyAppList.addAll(thirdPartyAppsList);
                sCompatData.setInstalledThirdPartyAppList(this.mInstalledThirdPartyAppList);
            }
        }
    }
}
