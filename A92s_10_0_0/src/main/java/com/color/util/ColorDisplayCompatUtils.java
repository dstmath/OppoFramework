package com.color.util;

import android.app.ActivityManager;
import android.app.OppoActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class ColorDisplayCompatUtils {
    private static final String COLOR_DISPLAY_COMPAT_CONFIG_DIR = "/data/oppo/coloros/displaycompat";
    private static final String COLOR_DISPLAY_COMPAT_CONFIG_FILE_PATH = "/data/oppo/coloros/displaycompat/sys_display_compat_config.xml";
    public static final int COLOR_LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHOW = 3;
    private static final int CUTOUT_MODE_DEFAULT = 0;
    private static final int CUTOUT_MODE_HIDE = 2;
    private static final int CUTOUT_MODE_SHOW = 1;
    public static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final float DEFAULT_MAX_ASPECT_RATIO = 2.0f;
    private static final int DISPLAY_CUTOUT_POSITION_LEFT = 1;
    private static final int DISPLAY_CUTOUT_POSITION_MIDDLE = 2;
    private static final int DISPLAY_CUTOUT_POSITION_NONE = 0;
    private static final String FEATURE_CUTOUT = "com.oppo.feature.screen.heteromorphism";
    private static final String KEY_APP_LIST_CUTOUT_DEFAULT = "key_display_nonimmersive_local_apps";
    private static final String KEY_APP_LIST_CUTOUT_DEFAULT_OLD = "key_display_nonimmersive_local_apps";
    private static final String KEY_APP_LIST_CUTOUT_HIDE = "cutout_hide_app_list";
    private static final String KEY_APP_LIST_CUTOUT_SHOW = "key_display_immersive_local_apps";
    private static final String KEY_APP_LIST_CUTOUT_SHOW_OLD = "key_display_immersive_local_apps";
    private static final String KEY_LOCAL_COMPAT_APPS = "key_display_compat_local_apps_v1";
    private static final String KEY_LOCAL_FULLSCREEN_APPS = "key_display_fullscreen_local_apps_v1";
    private static final String KEY_SHOW_FULLSCREEN_DIALOG_APPS = "key_display_show_dialog_local_apps";
    private static final String PROP_CUTOUT_SIZE = "ro.oppo.screen.heteromorphism";
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
    /* access modifiers changed from: private */
    public static ColorDisplayCompatData sCompatData = null;
    private static volatile ColorDisplayCompatUtils sDisplayCompatUtils = null;
    private static List<String> sExcludeImmersivedList = new ArrayList();
    private static List<String> sIncludeImmersiveList = new ArrayList();
    private List<String> mAlreadyShowDialogAppsList = new ArrayList();
    private List<String> mBlackList = new ArrayList();
    private HashMap<String, String> mCompatPackageList = new HashMap<>();
    private Context mContext = null;
    private FileObserverPolicy mDisplayCompatFileObserver = null;
    private int mDisplayCutoutType = 0;
    private boolean mEnableDisplayCompat = true;
    private boolean mHasHeteromorphismFeature = false;
    private boolean mImmersiveDefault = false;
    /* access modifiers changed from: private */
    public List<String> mInstalledCompatList = new ArrayList();
    private List<String> mInstalledImeList = new ArrayList();
    /* access modifiers changed from: private */
    public List<String> mInstalledThirdPartyAppList = new ArrayList();
    private List<String> mLocalCompatAppsList = new ArrayList();
    private LocalCompatSettingsObserverPolicy mLocalCompatSettingsObserver = null;
    private LocalCutoutSettingsObserverPolicy mLocalCutoutSettingsObserver = null;
    private List<String> mLocalDefaultModeList = new ArrayList();
    private List<String> mLocalFullScreenAppsList = new ArrayList();
    private List<String> mLocalHideModeList = new ArrayList();
    private LocalShowDialogSettingsObserverPolicy mLocalShowDialogSettingsObserver = null;
    private List<String> mLocalShowModeList = new ArrayList();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private BroadcastReceiver mMultiUserReceiver = new BroadcastReceiver() {
        /* class com.color.util.ColorDisplayCompatUtils.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            ColorDisplayCompatUtils.this.loadLocalCompatAppList();
            ColorDisplayCompatUtils.this.loadLocalFullScreenAppList();
            ColorDisplayCompatUtils.this.refreshLocalDefaultModeList();
            ColorDisplayCompatUtils.this.refreshLocalShowModeList();
            ColorDisplayCompatUtils.this.refreshLocalHideModeList();
            ColorDisplayCompatUtils.this.loadInstalledImeAppList();
            ColorDisplayCompatUtils.this.loadLocalShowDialogAppList();
            ColorDisplayCompatUtils.this.loadInstalledCompatAppList();
            ColorDisplayCompatUtils.this.loadInstalledThirdPartyApps();
        }
    };
    private final MyPackageMonitor mMyPackageMonitor = new MyPackageMonitor();
    private List<String> mNeedAdjustSizeAppList = new ArrayList();
    private PackageManager mPackageManager = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.color.util.ColorDisplayCompatUtils.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ColorDisplayCompatUtils.this.isOnlyDisplayCompatEnabled() && Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                ColorDisplayCompatUtils.this.loadInstalledImeAppList();
            }
        }
    };
    private List<String> mRusImmersiveAppsList = new ArrayList();
    private List<String> mRusNonImmersiveAppsList = new ArrayList();
    private List<String> mWhiteList = new ArrayList();

    static {
        sIncludeImmersiveList.clear();
        sIncludeImmersiveList.add("com.nearme.gamecenter.ddz.nearme.gamecenter");
        sIncludeImmersiveList.add("com.oppo.reader");
        sIncludeImmersiveList.add("com.oppo.book");
        sIncludeImmersiveList.add("com.google.android.inputmethod.latin");
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
        if (context.getPackageManager().hasSystemFeature(FEATURE_CUTOUT)) {
            setDisplayCutoutType();
            synchronized (this.mLock) {
                this.mHasHeteromorphismFeature = true;
                sCompatData.setHasHeteromorphismFeature(this.mHasHeteromorphismFeature);
            }
        }
        initDir();
        initFileObserver();
        initLocalCompatSettingsObserver();
        initLocalCutoutSettingsObserver();
        initLocalShowDialogSettingsObserver();
        loadLocalCompatAppList();
        loadLocalFullScreenAppList();
        refreshLocalDefaultModeList();
        refreshLocalShowModeList();
        refreshLocalHideModeList();
        loadInstalledImeAppList();
        loadLocalShowDialogAppList();
        loadInstalledCompatAppList();
        loadInstalledThirdPartyApps();
        readDisplayCompatConfig();
        registerPackageMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        this.mContext.registerReceiver(this.mReceiver, filter);
        IntentFilter filterMultiUser = new IntentFilter();
        filterMultiUser.addAction(Intent.ACTION_USER_SWITCHED);
        filterMultiUser.addAction(Intent.ACTION_USER_ADDED);
        filterMultiUser.addAction(Intent.ACTION_USER_REMOVED);
        this.mContext.registerReceiver(this.mMultiUserReceiver, filterMultiUser);
    }

    public void initData(Context context) {
        this.mContext = context;
        Context context2 = this.mContext;
        if (context2 != null) {
            this.mPackageManager = context2.getPackageManager();
        }
        initData();
    }

    public void initData() {
        try {
            ColorDisplayCompatData data = new OppoActivityManager().getDisplayCompatData();
            sCompatData = data;
            this.mEnableDisplayCompat = data.getDisplayCompatEnabled();
            this.mHasHeteromorphismFeature = data.hasHeteromorphismFeature();
            if (this.mWhiteList != null) {
                this.mWhiteList.clear();
                this.mWhiteList = data.getWhiteList();
            }
            if (this.mBlackList != null) {
                this.mBlackList.clear();
                this.mBlackList = data.getBlackList();
            }
            if (this.mLocalCompatAppsList != null) {
                this.mLocalCompatAppsList.clear();
                this.mLocalCompatAppsList = data.getLocalCompatList();
            }
            if (this.mLocalFullScreenAppsList != null) {
                this.mLocalFullScreenAppsList.clear();
                this.mLocalFullScreenAppsList = data.getLocalFullScreenList();
            }
            if (this.mLocalDefaultModeList != null) {
                this.mLocalDefaultModeList.clear();
                this.mLocalDefaultModeList = data.getLocalCutoutDefaultList();
            }
            if (this.mLocalShowModeList != null) {
                this.mLocalShowModeList.clear();
                this.mLocalShowModeList = data.getLocalCutoutShowList();
            }
            if (this.mLocalHideModeList != null) {
                this.mLocalHideModeList.clear();
                this.mLocalHideModeList = data.getLocalCutoutHideList();
            }
            if (this.mCompatPackageList != null) {
                this.mCompatPackageList.clear();
                this.mCompatPackageList = data.getCompatPackageList();
            }
            if (this.mInstalledCompatList != null) {
                this.mInstalledCompatList.clear();
                this.mInstalledCompatList = data.getInstalledCompatList();
            }
            if (this.mInstalledImeList != null) {
                this.mInstalledImeList.clear();
                this.mInstalledImeList = data.getInstalledImeList();
            }
            if (this.mAlreadyShowDialogAppsList != null) {
                this.mAlreadyShowDialogAppsList.clear();
                this.mAlreadyShowDialogAppsList = data.getShowDialogAppList();
            }
            this.mImmersiveDefault = data.getRusImmersiveDefault();
            if (this.mRusImmersiveAppsList != null) {
                this.mRusImmersiveAppsList.clear();
                this.mRusImmersiveAppsList = data.getRusImmersiveList();
            }
            if (this.mRusNonImmersiveAppsList != null) {
                this.mRusNonImmersiveAppsList.clear();
                this.mRusNonImmersiveAppsList = data.getRusNonImmersiveList();
            }
            if (this.mInstalledThirdPartyAppList != null) {
                this.mInstalledThirdPartyAppList.clear();
                this.mInstalledThirdPartyAppList = data.getInstalledThirdPartyAppList();
            }
            if (this.mNeedAdjustSizeAppList != null) {
                this.mNeedAdjustSizeAppList.clear();
                this.mNeedAdjustSizeAppList = data.getNeedAdjustSizeAppList();
            }
            this.mDisplayCutoutType = data.getDisplayCutoutType();
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
        if (DEBUG) {
            Slog.i(TAG, "initDir start");
        }
        File displayCompatDir = new File(COLOR_DISPLAY_COMPAT_CONFIG_DIR);
        File displayCompatConfigFile = new File(COLOR_DISPLAY_COMPAT_CONFIG_FILE_PATH);
        try {
            if (!displayCompatDir.exists()) {
                displayCompatDir.mkdirs();
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
        Context context = this.mContext;
        if (context != null) {
            context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KEY_LOCAL_COMPAT_APPS), true, this.mLocalCompatSettingsObserver);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KEY_LOCAL_FULLSCREEN_APPS), true, this.mLocalCompatSettingsObserver);
        }
    }

    private void initLocalCutoutSettingsObserver() {
        this.mLocalCutoutSettingsObserver = new LocalCutoutSettingsObserverPolicy();
        Context context = this.mContext;
        if (context != null) {
            context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("key_display_nonimmersive_local_apps"), true, this.mLocalCutoutSettingsObserver);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("key_display_immersive_local_apps"), true, this.mLocalCutoutSettingsObserver);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KEY_APP_LIST_CUTOUT_HIDE), true, this.mLocalCutoutSettingsObserver);
        }
    }

    private void initLocalShowDialogSettingsObserver() {
        this.mLocalShowDialogSettingsObserver = new LocalShowDialogSettingsObserverPolicy();
        Context context = this.mContext;
        if (context != null) {
            context.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KEY_SHOW_FULLSCREEN_DIALOG_APPS), true, this.mLocalShowDialogSettingsObserver);
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

    public void readDisplayCompatConfig() {
        if (DEBUG) {
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

    /* JADX WARNING: Removed duplicated region for block: B:139:0x0235 A[SYNTHETIC, Splitter:B:139:0x0235] */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x0245 A[SYNTHETIC, Splitter:B:144:0x0245] */
    /* JADX WARNING: Removed duplicated region for block: B:152:? A[RETURN, SYNTHETIC] */
    private void readConfigFromFileLocked(File file) {
        IOException e;
        StringBuilder sb;
        if (DEBUG) {
            Slog.i(TAG, "readConfigFromFileLocked start");
        }
        List<String> whitePkglist = new ArrayList<>();
        List<String> blackPkglist = new ArrayList<>();
        HashMap<String, String> compatPkgList = new HashMap<>();
        List<String> immersivePkglist = new ArrayList<>();
        List<String> nonImmersivePkglist = new ArrayList<>();
        List<String> needAdjustSizeList = new ArrayList<>();
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
                        if (TAG_ENABLE.equals(tagName)) {
                            String enable = parser.nextText();
                            if (!enable.equals("")) {
                                synchronized (this.mLock) {
                                    this.mEnableDisplayCompat = Boolean.parseBoolean(enable);
                                    sCompatData.setDisplatOptEnabled(this.mEnableDisplayCompat);
                                }
                            }
                        } else if (TAG_WHITE.equals(tagName)) {
                            String pkg = parser.nextText();
                            if (!pkg.equals("")) {
                                whitePkglist.add(pkg);
                            }
                        } else if (TAG_BLACK.equals(tagName)) {
                            String pkg2 = parser.nextText();
                            if (!pkg2.equals("")) {
                                blackPkglist.add(pkg2);
                            }
                        } else if (TAG_COMPAT.equals(tagName)) {
                            String pkg3 = parser.getAttributeValue(null, "package");
                            if (pkg3 != null) {
                                String versionName = parser.getAttributeValue(null, "versionName");
                                if (TextUtils.isEmpty(versionName)) {
                                    versionName = VERSION_NAME_EMPTY;
                                }
                                compatPkgList.put(pkg3, versionName);
                            }
                        } else if (TAG_ENABLE_IMMERSIVE.equals(tagName)) {
                            String enable2 = parser.nextText();
                            if (!enable2.equals("")) {
                                synchronized (this.mLock) {
                                    this.mImmersiveDefault = Boolean.parseBoolean(enable2);
                                    sCompatData.setRusImmersiveDefault(this.mImmersiveDefault);
                                }
                            }
                        } else if (TAG_IMMERSIVE.equals(tagName)) {
                            String pkg4 = parser.nextText();
                            if (!pkg4.equals("")) {
                                immersivePkglist.add(pkg4);
                            }
                        } else if (TAG_NONIMMERSIVE.equals(tagName)) {
                            String pkg5 = parser.nextText();
                            if (!pkg5.equals("")) {
                                nonImmersivePkglist.add(pkg5);
                                if (DEBUG) {
                                    Slog.i(TAG, "readConfigFromFileLocked nonImmersive : " + pkg5);
                                }
                            }
                        } else if ("size".equals(tagName)) {
                            String pkg6 = parser.nextText();
                            if (!pkg6.equals("")) {
                                needAdjustSizeList.add(pkg6);
                            }
                        }
                    }
                    if (type == 1) {
                        synchronized (this.mLock) {
                            this.mWhiteList.clear();
                            this.mWhiteList.addAll(whitePkglist);
                            sCompatData.setWhiteList(this.mWhiteList);
                        }
                        synchronized (this.mLock) {
                            this.mBlackList.clear();
                            this.mBlackList.addAll(blackPkglist);
                            sCompatData.setBlackList(this.mBlackList);
                        }
                        synchronized (this.mLock) {
                            this.mCompatPackageList.clear();
                            this.mCompatPackageList.putAll(compatPkgList);
                            sCompatData.setCompatPackageList(this.mCompatPackageList);
                        }
                        synchronized (this.mLock) {
                            this.mRusImmersiveAppsList.clear();
                            this.mRusImmersiveAppsList.addAll(immersivePkglist);
                            sCompatData.setRusImmersiveList(this.mRusImmersiveAppsList);
                        }
                        synchronized (this.mLock) {
                            this.mRusNonImmersiveAppsList.clear();
                            this.mRusNonImmersiveAppsList.addAll(nonImmersivePkglist);
                            sCompatData.setRusNonImmersiveList(this.mRusNonImmersiveAppsList);
                        }
                        synchronized (this.mLock) {
                            this.mNeedAdjustSizeAppList.clear();
                            this.mNeedAdjustSizeAppList.addAll(needAdjustSizeList);
                            sCompatData.setNeedAdjustSizeAppList(this.mNeedAdjustSizeAppList);
                        }
                        try {
                            stream2.close();
                            return;
                        } catch (IOException e2) {
                            e = e2;
                            sb = new StringBuilder();
                        }
                    }
                }
            } catch (Exception e3) {
                e = e3;
                try {
                    Slog.e(TAG, "failed parsing ", e);
                    loadDefaultDisplayCompatList();
                    if (stream == null) {
                        try {
                            stream.close();
                            return;
                        } catch (IOException e4) {
                            e = e4;
                            sb = new StringBuilder();
                        }
                    } else {
                        return;
                    }
                } catch (Throwable th) {
                    th = th;
                    if (stream != null) {
                    }
                    throw th;
                }
            }
            sb.append("Failed to close state FileInputStream ");
            sb.append(e);
            Slog.e(TAG, sb.toString());
        } catch (Exception e5) {
            e = e5;
            Slog.e(TAG, "failed parsing ", e);
            loadDefaultDisplayCompatList();
            if (stream == null) {
            }
        } catch (Throwable th2) {
            th = th2;
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e6) {
                    Slog.e(TAG, "Failed to close state FileInputStream " + e6);
                }
            }
            throw th;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.internal.content.PackageMonitor.register(android.content.Context, android.os.Looper, android.os.UserHandle, boolean):void
     arg types: [android.content.Context, ?[OBJECT, ARRAY], android.os.UserHandle, int]
     candidates:
      com.android.internal.content.PackageMonitor.register(android.content.Context, android.os.UserHandle, boolean, android.os.Handler):void
      com.android.internal.content.PackageMonitor.register(android.content.Context, android.os.Looper, android.os.UserHandle, boolean):void */
    private void registerPackageMonitor() {
        Context context = this.mContext;
        if (context != null) {
            this.mMyPackageMonitor.register(context, (Looper) null, UserHandle.ALL, true);
        }
    }

    private String getThis() {
        return toString();
    }

    private String getVersionNameFromCompatPkg(String pkg) {
        return this.mCompatPackageList.get(pkg);
    }

    public boolean isOnlyDisplayCompatEnabled() {
        return this.mEnableDisplayCompat;
    }

    public boolean hasHeteromorphismFeature() {
        boolean enabled = false;
        synchronized (this.mLock) {
            if (this.mHasHeteromorphismFeature) {
                enabled = true;
            }
        }
        return enabled;
    }

    public boolean getImmersiveDefault() {
        boolean enabled;
        synchronized (this.mLock) {
            enabled = this.mImmersiveDefault;
        }
        return enabled;
    }

    public boolean inWhitePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mWhiteList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inBlackPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mBlackList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inRusImmersivePkgList(String pkg) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mRusImmersiveAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inRusNonImmersivePkgList(String pkg) {
        if (this.mDisplayCutoutType == 1) {
            boolean result = false;
            synchronized (this.mLock) {
                if (this.mRusNonImmersiveAppsList.contains(pkg)) {
                    Log.d(TAG, "inRusNonImmersivePkgList: " + pkg);
                    result = true;
                }
            }
            return result;
        } else if (!DEBUG) {
            return false;
        } else {
            Log.d(TAG, "because this is not a left cutout, the nonimmersive list is not working");
            return false;
        }
    }

    public boolean inLocalCompatPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mLocalCompatAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inLocalFullScreenPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mLocalFullScreenAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inLocalNonImmersivePkgList(String pkg) {
        return this.mLocalDefaultModeList.contains(pkg);
    }

    public boolean inLocalImmersivePkgList(String pkg) {
        return this.mLocalShowModeList.contains(pkg);
    }

    public boolean inInstalledCompatPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mInstalledCompatList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inInstalledThirdPartyAppList(String pkg) {
        return this.mInstalledThirdPartyAppList.contains(pkg);
    }

    public boolean inInstalledImeList(String pkg) {
        return this.mInstalledImeList.contains(pkg);
    }

    public boolean inAlreadyShowDialogList(String pkg) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mAlreadyShowDialogAppsList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inNeedAdujstSizeList(String pkg) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mNeedAdjustSizeAppList.contains(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean inCompatPkgList(String pkg) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mCompatPackageList.containsKey(pkg)) {
                result = true;
            }
        }
        return result;
    }

    public boolean needCompatPkgByVersionName(String pkg) {
        String versionName = getVersionNameFromCompatPkg(pkg);
        if (versionName == null || VERSION_NAME_EMPTY.equals(versionName)) {
            return true;
        }
        try {
            return compareVersion(getVersionNameFromInstalledPkg(pkg), versionName) < 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean shouldCompatAdjustForPkg(String pkg) {
        if (!isOnlyDisplayCompatEnabled() || inInstalledImeList(pkg) || inBlackPkgList(pkg)) {
            return false;
        }
        if (inWhitePkgList(pkg)) {
            return true;
        }
        if (!inLocalFullScreenPkgList(pkg) && inInstalledCompatPkgList(pkg)) {
            return true;
        }
        return false;
    }

    public boolean neverLayoutInDisplayCutout(String packageName) {
        if (!inRusNonImmersivePkgList(packageName) || inLocalImmersivePkgList(packageName)) {
            return false;
        }
        return true;
    }

    public boolean shouldNonImmersiveAdjustForPkg(String pkg) {
        boolean result;
        if (getImmersiveDefault()) {
            result = false;
        } else {
            result = inInstalledThirdPartyAppList(pkg);
        }
        if (inInstalledImeList(pkg)) {
            result = true;
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
        if (!inRusNonImmersivePkgList(pkg) && !shouldCompatAdjustForPkg(pkg)) {
            return result;
        }
        return true;
    }

    public int getAppCutoutMode(String pkg) {
        int mode = 1;
        if (inInstalledThirdPartyAppList(pkg) || inInstalledImeList(pkg)) {
            mode = 0;
        }
        if (this.mLocalDefaultModeList.contains(pkg)) {
            return 0;
        }
        if (this.mLocalShowModeList.contains(pkg)) {
            return 1;
        }
        if (this.mLocalHideModeList.contains(pkg)) {
            return 2;
        }
        if (inRusImmersivePkgList(pkg)) {
            return 1;
        }
        if (inRusNonImmersivePkgList(pkg)) {
            return 2;
        }
        return mode;
    }

    public float getmaxAspectRatio(ActivityInfo info) {
        float maxAspectRatio = info.maxAspectRatio;
        if (info.packageName.startsWith("android.server.cts") || info.packageName.startsWith("android.server.wm")) {
            return maxAspectRatio;
        }
        if (shouldCompatAdjustForPkg(info.packageName)) {
            Slog.d(TAG, info.packageName + ", maxAspectRatio: " + info.maxAspectRatio + " >>> " + 1.7778f);
            return 1.7778f;
        } else if (maxAspectRatio == 0.0f) {
            return maxAspectRatio;
        } else {
            Slog.d(TAG, info.packageName + ", maxAspectRatio: " + info.maxAspectRatio + " >>> " + 0.0f);
            return 0.0f;
        }
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
            synchronized (this.mLock) {
                if (this.mLocalCompatAppsList.contains(pkg)) {
                    this.mLocalCompatAppsList.remove(pkg);
                    StringBuilder pkgList = new StringBuilder();
                    for (String s : this.mLocalCompatAppsList) {
                        pkgList.append(s);
                        pkgList.append(SmsManager.REGEX_PREFIX_DELIMITER);
                    }
                    Settings.Global.putString(this.mContext.getContentResolver(), KEY_LOCAL_COMPAT_APPS, pkgList.toString());
                }
            }
            synchronized (this.mLock) {
                if (!this.mLocalFullScreenAppsList.contains(pkg)) {
                    this.mLocalFullScreenAppsList.add(pkg);
                    StringBuilder pkgList2 = new StringBuilder();
                    for (String s2 : this.mLocalFullScreenAppsList) {
                        pkgList2.append(s2);
                        pkgList2.append(SmsManager.REGEX_PREFIX_DELIMITER);
                    }
                    Settings.Global.putString(this.mContext.getContentResolver(), KEY_LOCAL_FULLSCREEN_APPS, pkgList2.toString());
                }
            }
        }
    }

    public void updateLocalImmersiveListForPkg(String pkg) {
    }

    public void updateLocalShowDialogListForPkg(String pkg) {
        if (this.mContext != null) {
            synchronized (this.mLock) {
                if (!this.mAlreadyShowDialogAppsList.contains(pkg)) {
                    this.mAlreadyShowDialogAppsList.add(pkg);
                    StringBuilder pkgList = new StringBuilder();
                    for (String s : this.mAlreadyShowDialogAppsList) {
                        pkgList.append(s);
                        pkgList.append(SmsManager.REGEX_PREFIX_DELIMITER);
                    }
                    Settings.Global.putString(this.mContext.getContentResolver(), KEY_SHOW_FULLSCREEN_DIALOG_APPS, pkgList.toString());
                }
            }
        }
    }

    public void removeLocalShowDialogListForPkg(String pkg) {
        if (this.mContext != null) {
            synchronized (this.mLock) {
                if (this.mAlreadyShowDialogAppsList.contains(pkg)) {
                    this.mAlreadyShowDialogAppsList.remove(pkg);
                    StringBuilder pkgList = new StringBuilder();
                    for (String s : this.mAlreadyShowDialogAppsList) {
                        pkgList.append(s);
                        pkgList.append(SmsManager.REGEX_PREFIX_DELIMITER);
                    }
                    Settings.Global.putString(this.mContext.getContentResolver(), KEY_SHOW_FULLSCREEN_DIALOG_APPS, pkgList.toString());
                }
            }
        }
    }

    private void loadDefaultDisplayCompatList() {
        synchronized (this.mLock) {
        }
        synchronized (this.mLock) {
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
        synchronized (this.mLock) {
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

    /* JADX WARNING: Removed duplicated region for block: B:17:0x004b  */
    private int compareVersion(String left, String right) {
        String str;
        int result;
        if (left.equals(right)) {
            return 0;
        }
        int leftStart = 0;
        int rightStart = 0;
        do {
            int leftEnd = left.indexOf(46, leftStart);
            int rightEnd = right.indexOf(46, rightStart);
            Integer leftValue = Integer.valueOf(Integer.parseInt(leftEnd < 0 ? left.substring(leftStart) : left.substring(leftStart, leftEnd)));
            if (rightEnd < 0) {
                str = right.substring(rightStart);
            } else {
                str = right.substring(rightStart, rightEnd);
            }
            result = leftValue.compareTo(Integer.valueOf(Integer.parseInt(str)));
            leftStart = leftEnd + 1;
            rightStart = rightEnd + 1;
            if (result != 0 || leftStart <= 0) {
                if (result == 0) {
                    if (leftStart > rightStart) {
                        return containsNonZeroValue(left, leftStart) ? 1 : 0;
                    }
                    if (leftStart < rightStart) {
                        if (containsNonZeroValue(right, rightStart)) {
                            return -1;
                        }
                        return 0;
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
        PackageManager packageManager = this.mPackageManager;
        if (packageManager == null) {
            return versionName;
        }
        try {
            return packageManager.getPackageInfo(pkg, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return versionName;
        }
    }

    /* access modifiers changed from: private */
    public boolean supportFullScreen(String packageName) {
        return supportFullScreen(packageName, null);
    }

    private boolean supportFullScreen(String packageName, PackageInfo pkgInfo) {
        float maxAspectRatio = DEFAULT_MAX_ASPECT_RATIO;
        int privateFlags = 0;
        boolean result = true;
        if (this.mPackageManager != null) {
            try {
                if (!TextUtils.isEmpty(packageName) && !inInstalledImeList(packageName) && (sIncludeImmersiveList.contains(packageName) || (!packageName.startsWith("com.oppo") && !packageName.startsWith("com.coloros") && !packageName.startsWith("com.nearme") && !packageName.startsWith("com.heytap") && !packageName.startsWith("com.cootek.smartinputv5.language") && !sExcludeImmersivedList.contains(packageName)))) {
                    if (pkgInfo == null) {
                        pkgInfo = this.mPackageManager.getPackageInfoAsUser(packageName, 8192, ActivityManager.getCurrentUser());
                    }
                    if (pkgInfo != null && (pkgInfo.applicationInfo.flags & 1) == 0) {
                        maxAspectRatio = pkgInfo.applicationInfo.maxAspectRatio;
                        if (pkgInfo.applicationInfo.targetSdkVersion >= 26 && maxAspectRatio <= 0.0f) {
                            maxAspectRatio = DEFAULT_MAX_ASPECT_RATIO;
                        }
                        privateFlags = pkgInfo.applicationInfo.privateFlags;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        if (maxAspectRatio < DEFAULT_MAX_ASPECT_RATIO && (privateFlags & 1024) == 0 && (privateFlags & 4096) == 0) {
            result = false;
        }
        return result;
    }

    /* access modifiers changed from: private */
    public boolean isInstalledThirdPartyApp(String packageName) {
        PackageInfo pkgInfo;
        if (this.mPackageManager == null) {
            return false;
        }
        try {
            if (TextUtils.isEmpty(packageName)) {
                return false;
            }
            if ((!sIncludeImmersiveList.contains(packageName) && (packageName.startsWith("com.oppo") || packageName.startsWith("com.coloros") || packageName.startsWith("com.nearme") || packageName.startsWith("com.cootek.smartinputv5.language") || packageName.startsWith("com.heytap") || sExcludeImmersivedList.contains(packageName))) || (pkgInfo = this.mPackageManager.getPackageInfoAsUser(packageName, 8192, ActivityManager.getCurrentUser())) == null) {
                return false;
            }
            if (sIncludeImmersiveList.contains(packageName) || (pkgInfo.applicationInfo.flags & 1) == 0) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        @Override // android.os.FileObserver
        public void onEvent(int event, String path) {
            if (event == 8 && this.mFocusPath.equals(ColorDisplayCompatUtils.COLOR_DISPLAY_COMPAT_CONFIG_FILE_PATH)) {
                Slog.i(ColorDisplayCompatUtils.TAG, "FileObserver: onEvent");
                ColorDisplayCompatUtils.this.readDisplayCompatConfig();
            }
        }
    }

    /* access modifiers changed from: private */
    public void loadLocalCompatAppList() {
        Context context = this.mContext;
        if (context != null) {
            String pkgList = Settings.Global.getString(context.getContentResolver(), KEY_LOCAL_COMPAT_APPS);
            synchronized (this.mLock) {
                this.mLocalCompatAppsList.clear();
                if (!TextUtils.isEmpty(pkgList)) {
                    this.mLocalCompatAppsList = new ArrayList(Arrays.asList(pkgList.split(SmsManager.REGEX_PREFIX_DELIMITER)));
                }
                sCompatData.setLocalCompatList(this.mLocalCompatAppsList);
            }
        }
    }

    /* access modifiers changed from: private */
    public void loadLocalFullScreenAppList() {
        Context context = this.mContext;
        if (context != null) {
            String fullScreenlist = Settings.Global.getString(context.getContentResolver(), KEY_LOCAL_FULLSCREEN_APPS);
            synchronized (this.mLock) {
                this.mLocalFullScreenAppsList.clear();
                if (!TextUtils.isEmpty(fullScreenlist)) {
                    this.mLocalFullScreenAppsList = new ArrayList(Arrays.asList(fullScreenlist.split(SmsManager.REGEX_PREFIX_DELIMITER)));
                }
                sCompatData.setLocalFullScreenList(this.mLocalFullScreenAppsList);
            }
        }
    }

    /* access modifiers changed from: private */
    public void refreshLocalDefaultModeList() {
        Context context = this.mContext;
        if (context != null) {
            String pkgList = Settings.Global.getString(context.getContentResolver(), "key_display_nonimmersive_local_apps");
            Log.d(TAG, "refreshLocalDefaultModeList:" + pkgList);
            synchronized (this.mLock) {
                this.mLocalDefaultModeList.clear();
                if (!TextUtils.isEmpty(pkgList)) {
                    this.mLocalDefaultModeList = new ArrayList(Arrays.asList(pkgList.split(SmsManager.REGEX_PREFIX_DELIMITER)));
                }
                sCompatData.setLocalCutoutDefaultList(this.mLocalDefaultModeList);
            }
        }
    }

    /* access modifiers changed from: private */
    public void refreshLocalShowModeList() {
        Context context = this.mContext;
        if (context != null) {
            String pkgList = Settings.Global.getString(context.getContentResolver(), "key_display_immersive_local_apps");
            Log.d(TAG, "refreshLocalShowModeList:" + pkgList);
            synchronized (this.mLock) {
                this.mLocalShowModeList.clear();
                if (!TextUtils.isEmpty(pkgList)) {
                    this.mLocalShowModeList = new ArrayList(Arrays.asList(pkgList.split(SmsManager.REGEX_PREFIX_DELIMITER)));
                }
                sCompatData.setLocalCutoutShowList(this.mLocalShowModeList);
            }
        }
    }

    /* access modifiers changed from: private */
    public void refreshLocalHideModeList() {
        Context context = this.mContext;
        if (context != null) {
            String pkgList = Settings.Global.getString(context.getContentResolver(), KEY_APP_LIST_CUTOUT_HIDE);
            Log.d(TAG, "refreshLocalHideModeList:" + pkgList);
            synchronized (this.mLock) {
                this.mLocalHideModeList.clear();
                if (!TextUtils.isEmpty(pkgList)) {
                    this.mLocalHideModeList = new ArrayList(Arrays.asList(pkgList.split(SmsManager.REGEX_PREFIX_DELIMITER)));
                }
                sCompatData.setLocalCutoutHideList(this.mLocalHideModeList);
            }
        }
    }

    private void updateOtherList(List<String> list, String oldKey, String newKey) {
        String oldValue = Settings.Global.getString(this.mContext.getContentResolver(), oldKey);
        String newValue = Settings.Global.getString(this.mContext.getContentResolver(), newKey);
        if (oldValue != null) {
            Log.d(TAG, "start updateOtherList: " + oldKey + ", " + oldValue);
            Log.d(TAG, "start updateOtherList: " + newKey + ", " + newValue);
            ContentResolver contentResolver = this.mContext.getContentResolver();
            StringBuilder sb = new StringBuilder();
            sb.append(oldValue);
            sb.append(newValue);
            Settings.Global.putString(contentResolver, newKey, sb.toString());
            Settings.Global.putString(this.mContext.getContentResolver(), oldKey, null);
            String oldValue1 = Settings.Global.getString(this.mContext.getContentResolver(), oldKey);
            String newValue1 = Settings.Global.getString(this.mContext.getContentResolver(), newKey);
            Log.d(TAG, "end updateOtherList: " + oldKey + ", " + oldValue1);
            Log.d(TAG, "end updateOtherList: " + newKey + ", " + newValue1);
        }
    }

    /* access modifiers changed from: private */
    public void loadLocalShowDialogAppList() {
        String pkgList;
        Context context = this.mContext;
        if (context != null && (pkgList = Settings.Global.getString(context.getContentResolver(), KEY_SHOW_FULLSCREEN_DIALOG_APPS)) != null) {
            synchronized (this.mLock) {
                this.mAlreadyShowDialogAppsList.clear();
                this.mAlreadyShowDialogAppsList = new ArrayList(Arrays.asList(pkgList.split(SmsManager.REGEX_PREFIX_DELIMITER)));
                sCompatData.setShowDialogAppList(this.mAlreadyShowDialogAppsList);
            }
        }
    }

    /* access modifiers changed from: private */
    public void loadInstalledImeAppList() {
        if (this.mContext != null) {
            List<String> imeList = new ArrayList<>();
            try {
                List<ResolveInfo> list = this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent(InputMethod.SERVICE_INTERFACE), 131200, ActivityManager.getCurrentUser());
                if (list != null) {
                    int listSize = list.size();
                    for (int i = 0; i < listSize; i++) {
                        ResolveInfo resolveInfo = list.get(i);
                        if (resolveInfo != null) {
                            imeList.add(resolveInfo.serviceInfo.packageName);
                        }
                    }
                }
            } catch (Exception e) {
            }
            synchronized (this.mLock) {
                this.mInstalledImeList.clear();
                this.mInstalledImeList.addAll(imeList);
                sCompatData.setInstalledImeList(this.mInstalledImeList);
            }
        }
    }

    /* access modifiers changed from: private */
    public void loadInstalledCompatAppList() {
        if (this.mContext != null) {
            long begin = System.currentTimeMillis();
            List<String> thirdPartyNeedCompatAppsList = new ArrayList<>();
            try {
                for (PackageInfo packageInfo : this.mContext.getPackageManager().getInstalledPackagesAsUser(0, ActivityManager.getCurrentUser())) {
                    if (packageInfo != null) {
                        try {
                            String packageName = packageInfo.packageName;
                            if (!supportFullScreen(packageName, packageInfo)) {
                                thirdPartyNeedCompatAppsList.add(packageName);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e2) {
            }
            synchronized (this.mLock) {
                this.mInstalledCompatList.clear();
                this.mInstalledCompatList.addAll(thirdPartyNeedCompatAppsList);
                sCompatData.setInstalledCompatList(this.mInstalledCompatList);
            }
            Slog.i(TAG, "loadInstalledCompatAppList time cost =" + (System.currentTimeMillis() - begin));
        }
    }

    /* access modifiers changed from: private */
    public void loadInstalledThirdPartyApps() {
        PackageInfo packageInfo;
        if (this.mContext != null) {
            List<String> thirdPartyAppsList = new ArrayList<>();
            try {
                Iterator<PackageInfo> it = this.mContext.getPackageManager().getInstalledPackagesAsUser(0, ActivityManager.getCurrentUser()).iterator();
                while (true) {
                    if (!it.hasNext() || (packageInfo = it.next()) == null) {
                        break;
                    } else if (packageInfo.packageName == null) {
                        break;
                    } else {
                        String packageName = packageInfo.packageName;
                        if (sIncludeImmersiveList.contains(packageName)) {
                            thirdPartyAppsList.add(packageName);
                            break;
                        }
                        boolean isColorOsApp = true;
                        boolean isDataApp = (packageInfo.applicationInfo.flags & 1) == 0;
                        if (!packageName.startsWith("com.oppo") && !packageName.startsWith("com.coloros") && !packageName.startsWith("com.nearme") && !packageName.startsWith("com.heytap")) {
                            if (!sExcludeImmersivedList.contains(packageName)) {
                                isColorOsApp = false;
                            }
                        }
                        if (isDataApp && !isColorOsApp) {
                            thirdPartyAppsList.add(packageName);
                            if (DEBUG) {
                                Log.d(TAG, "thirdPartyAppsList add : " + packageName);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "fail to loadInstalledThirdPartyApps: " + e.toString());
            }
            if (DEBUG) {
                Iterator<String> it2 = thirdPartyAppsList.iterator();
                while (it2.hasNext()) {
                    Log.d(TAG, "find list:" + it2.next());
                }
            }
            synchronized (this.mLock) {
                this.mInstalledThirdPartyAppList.clear();
                this.mInstalledThirdPartyAppList.addAll(thirdPartyAppsList);
                sCompatData.setInstalledThirdPartyAppList(this.mInstalledThirdPartyAppList);
            }
        }
    }

    private class LocalCompatSettingsObserverPolicy extends ContentObserver {
        public LocalCompatSettingsObserverPolicy() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            ColorDisplayCompatUtils.this.loadLocalCompatAppList();
            ColorDisplayCompatUtils.this.loadLocalFullScreenAppList();
            super.onChange(selfChange);
        }
    }

    private class LocalCutoutSettingsObserverPolicy extends ContentObserver {
        public LocalCutoutSettingsObserverPolicy() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            ColorDisplayCompatUtils.this.refreshLocalDefaultModeList();
            ColorDisplayCompatUtils.this.refreshLocalShowModeList();
            ColorDisplayCompatUtils.this.refreshLocalHideModeList();
            super.onChange(selfChange);
        }
    }

    private class LocalShowDialogSettingsObserverPolicy extends ContentObserver {
        public LocalShowDialogSettingsObserverPolicy() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            ColorDisplayCompatUtils.this.loadLocalShowDialogAppList();
            super.onChange(selfChange);
        }
    }

    private class MyPackageMonitor extends PackageMonitor {
        private MyPackageMonitor() {
        }

        @Override // com.android.internal.content.PackageMonitor
        public void onPackageRemoved(String packageName, int uid) {
            if (!TextUtils.isEmpty(packageName)) {
                ColorDisplayCompatUtils.this.loadInstalledImeAppList();
                if (ColorDisplayCompatUtils.this.inInstalledCompatPkgList(packageName)) {
                    synchronized (ColorDisplayCompatUtils.this.mLock) {
                        ColorDisplayCompatUtils.this.mInstalledCompatList.remove(packageName);
                        ColorDisplayCompatUtils.sCompatData.setInstalledCompatList(ColorDisplayCompatUtils.this.mInstalledCompatList);
                    }
                }
                if (ColorDisplayCompatUtils.this.inInstalledThirdPartyAppList(packageName)) {
                    synchronized (ColorDisplayCompatUtils.this.mLock) {
                        ColorDisplayCompatUtils.this.mInstalledThirdPartyAppList.remove(packageName);
                        ColorDisplayCompatUtils.sCompatData.setInstalledThirdPartyAppList(ColorDisplayCompatUtils.this.mInstalledThirdPartyAppList);
                    }
                }
                ColorDisplayCompatUtils.this.removeLocalShowDialogListForPkg(packageName);
            }
        }

        @Override // com.android.internal.content.PackageMonitor
        public void onPackageAdded(String packageName, int uid) {
            if (!TextUtils.isEmpty(packageName)) {
                ColorDisplayCompatUtils.this.loadInstalledImeAppList();
                if (!ColorDisplayCompatUtils.this.inInstalledImeList(packageName) && !ColorDisplayCompatUtils.this.supportFullScreen(packageName)) {
                    synchronized (ColorDisplayCompatUtils.this.mLock) {
                        ColorDisplayCompatUtils.this.mInstalledCompatList.add(packageName);
                        ColorDisplayCompatUtils.sCompatData.setInstalledCompatList(ColorDisplayCompatUtils.this.mInstalledCompatList);
                    }
                }
                if (!ColorDisplayCompatUtils.this.inInstalledThirdPartyAppList(packageName) && ColorDisplayCompatUtils.this.isInstalledThirdPartyApp(packageName)) {
                    synchronized (ColorDisplayCompatUtils.this.mLock) {
                        ColorDisplayCompatUtils.this.mInstalledThirdPartyAppList.add(packageName);
                        ColorDisplayCompatUtils.sCompatData.setInstalledThirdPartyAppList(ColorDisplayCompatUtils.this.mInstalledThirdPartyAppList);
                    }
                }
            }
        }

        @Override // com.android.internal.content.PackageMonitor
        public void onPackageModified(String packageName) {
            if (!TextUtils.isEmpty(packageName)) {
                ColorDisplayCompatUtils.this.loadInstalledImeAppList();
                if (!ColorDisplayCompatUtils.this.inInstalledImeList(packageName)) {
                    boolean supportFullscreen = ColorDisplayCompatUtils.this.supportFullScreen(packageName);
                    synchronized (ColorDisplayCompatUtils.this.mLock) {
                        if (supportFullscreen) {
                            try {
                                if (ColorDisplayCompatUtils.this.inInstalledCompatPkgList(packageName)) {
                                    ColorDisplayCompatUtils.this.mInstalledCompatList.remove(packageName);
                                }
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                        if (!supportFullscreen && !ColorDisplayCompatUtils.this.inInstalledCompatPkgList(packageName)) {
                            ColorDisplayCompatUtils.this.mInstalledCompatList.add(packageName);
                        }
                        ColorDisplayCompatUtils.sCompatData.setInstalledCompatList(ColorDisplayCompatUtils.this.mInstalledCompatList);
                    }
                }
            }
        }
    }

    private void setDisplayCutoutType() {
        try {
            String value = SystemProperties.get(PROP_CUTOUT_SIZE);
            Log.d(TAG, "cutout size: " + value);
            if (value != null) {
                String[] sizes = value.split("[,:]");
                if (sizes.length == 4) {
                    int x1 = Integer.parseInt(sizes[0]);
                    int x2 = Integer.parseInt(sizes[2]);
                    if (x1 >= 50 || x2 >= 300) {
                        this.mDisplayCutoutType = 2;
                    } else {
                        this.mDisplayCutoutType = 1;
                    }
                }
                sCompatData.setDisplayCutoutType(this.mDisplayCutoutType);
                Log.d(TAG, "set display cutout type : " + this.mDisplayCutoutType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "fail to set display cutout type");
        }
    }
}
