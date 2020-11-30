package com.android.server.am;

import android.app.ActivityManager;
import android.app.IUidObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.OppoDynamicLogManager;
import com.android.server.am.ColorCommonListManager;
import com.android.server.am.ColorHansPackageSelector;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.ColorFreeformManagerService;
import com.color.app.IColorHansListener;
import com.color.zoomwindow.ColorZoomWindowManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ColorHansManager implements IColorHansManager {
    protected static final int ABNORMAL_SCENE_ID = 3;
    protected static final int DEEP_SLEEP_SCENE_ID = 5;
    protected static final int DEFAULT_SCENE_ID = 0;
    protected static final int FAST_FREEZER_SCENE_ID = 4;
    public static final String HANS_FZ_REASON_DEFAULT = "Default";
    public static final String HANS_UFZ_REASON_ACTIVITY = "Activity";
    public static final String HANS_UFZ_REASON_ALARM = "Alarm";
    public static final String HANS_UFZ_REASON_ASYNC_BINDER = "AsyncBinder";
    public static final String HANS_UFZ_REASON_BIND_SERVICE = "BindService";
    public static final String HANS_UFZ_REASON_BROADCAST = "Broadcast";
    public static final String HANS_UFZ_REASON_BUMP_SERVICE = "BumpService";
    public static final String HANS_UFZ_REASON_CHARGING = "Charging";
    public static final String HANS_UFZ_REASON_DEFAULT = "Default";
    public static final String HANS_UFZ_REASON_DEFAULT_APP = "defaultApp";
    public static final String HANS_UFZ_REASON_DISABLE = "Disable";
    public static final String HANS_UFZ_REASON_EXECUTING_COMPONENT = "ExecutingComponent";
    public static final String HANS_UFZ_REASON_FASTFREEZER_DIRECT = "F-direct";
    public static final String HANS_UFZ_REASON_FASTFREEZER_TIMEOUT = "F-timeout";
    public static final String HANS_UFZ_REASON_GMS = "Gms";
    public static final String HANS_UFZ_REASON_JOB = "Job";
    public static final String HANS_UFZ_REASON_MEDIAKEY = "MediaKey";
    public static final String HANS_UFZ_REASON_NAVIGATION_APP = "navigationApp";
    public static final String HANS_UFZ_REASON_PACKET = "Packet";
    public static final String HANS_UFZ_REASON_PROVIDER = "Provider";
    public static final String HANS_UFZ_REASON_REMOVE_APP = "removeApp";
    public static final String HANS_UFZ_REASON_SIGNAL = "Signal";
    public static final String HANS_UFZ_REASON_SPECIAL_CASE = "SpecialCase";
    public static final String HANS_UFZ_REASON_START_SERVICE = "StartService";
    public static final String HANS_UFZ_REASON_SYNC = "Sync";
    public static final String HANS_UFZ_REASON_SYNC_BINDER = "SyncBinder";
    public static final String HANS_UFZ_REASON_TOP_ACTIVITY = "TopActivity";
    public static final String HANS_UFZ_REASON_TRANSACTION_BINDER = "TransBinder";
    public static final String HANS_UFZ_REASON_WAKELOCK = "Wakelock";
    public static final String HANS_UFZ_REASON_WATCHDOG = "Watchdog";
    protected static final int INVALID_UID = -1;
    private static final String KEY_RECORD = "record";
    private static final String KEY_RECORD_FREEZETIME = "freezetime";
    private static final String KEY_RECORD_LIVETIME = "livetime";
    private static final String KEY_RECORD_NOTFREEZELIST = "notfreezelist";
    private static final String KEY_RECORD_PKGNAME = "pkgname";
    private static final String KEY_RECORD_REASONLIST = "reasonlist";
    protected static final int LCD_OFF_SCENE_ID = 2;
    protected static final int LCD_ON_SCENE_ID = 1;
    protected static final int PRELOAD_FREEZER_SCENE_ID = 6;
    public static ArrayList<String> mPendingUFZReasonList = new ArrayList<>(Arrays.asList(HANS_UFZ_REASON_JOB, HANS_UFZ_REASON_SYNC));
    public static ArrayList<String> mSkipFastFreezeList = new ArrayList<>(Arrays.asList("com.android.permissioncontroller", "com.google.android.permissioncontroller", "android"));
    private static volatile ColorHansManager sInstance = null;
    private final String ATHENA_PKG = "com.coloros.athena";
    private final String abnormalScene = "abnormal";
    private final String fastfreezerScene = "fastfreezer";
    HansAppAbnormalScene hansAppAbnormalScene = null;
    HansFastFreezerScene hansFastFreezerScene = null;
    HansLcdOffScene hansLcdOffScene = null;
    HansLcdOnScene hansLcdOnScene = null;
    HansNightScene hansNightScene = null;
    HansPreloadFreezerScene hansPreloadFreezerScene = null;
    private final String lcdoffScene = "lcdoff";
    private final String lcdonScene = "lcdon";
    private ActivityManagerService mAms = null;
    private ArrayMap<String, ArrayList> mBroadcastRecordMap = new ArrayMap<>();
    private IColorActivityManagerServiceEx mColorAmsEx = null;
    private CommonConfig mCommonConfig = null;
    private ColorCommonListManager mCommonListManager = null;
    private Context mContext = null;
    private String mCurResumedPkg = null;
    private int mCurResumedUid = -1;
    int mCurSceneId = -1;
    private ArrayList<String> mFreqUnFreeze = new ArrayList<>(Arrays.asList(HANS_UFZ_REASON_BUMP_SERVICE, HANS_UFZ_REASON_PROVIDER, HANS_UFZ_REASON_START_SERVICE, HANS_UFZ_REASON_BROADCAST, HANS_UFZ_REASON_BIND_SERVICE));
    private HansBroadcastProxy mHansBroadcastProxy = null;
    final RemoteCallbackList<IColorHansListener> mHansListener = new RemoteCallbackList<>();
    private Object mHansLock = new Object();
    private HansLogger mHansLogger = null;
    private boolean mHansManageEnable = true;
    private SparseArray<HansRecord> mHansRecordMap = new SparseArray<>();
    private HashSet<Integer> mHansRunningList = new HashSet<>();
    IHansScene mHansScene = null;
    private boolean mInitAmsEnable = true;
    private boolean mIsBootCompleted = false;
    private boolean mIsZoomWindowMode = false;
    final SparseArray<ArrayList<Integer>> mIsolatedUids = new SparseArray<>();
    private String mLastResumedPkgName = null;
    private int mLastResumedUid = -1;
    private LocationManager mLocationManager = null;
    private HansMainHandler mMainHandler = null;
    private INetworkManagementService mNMs = null;
    private Handler mNativeHandler = null;
    private HansNativeService mNativeService = null;
    private ArrayList<String> mPendingIntentList = new ArrayList<>();
    private ArrayMap<String, ArrayList> mReceiverMap = new ArrayMap<>();
    private ArrayList<String> mSpecialWindowList = new ArrayList<>();
    private StateMachineHandler mStateMachineHandler = null;
    private ArrayList<String> mStateToDList = new ArrayList<>(Arrays.asList(HANS_UFZ_REASON_ACTIVITY, HANS_UFZ_REASON_TOP_ACTIVITY, HANS_UFZ_REASON_CHARGING, HANS_UFZ_REASON_WATCHDOG, HANS_UFZ_REASON_DISABLE, HANS_UFZ_REASON_GMS, HANS_UFZ_REASON_DEFAULT_APP, HANS_UFZ_REASON_REMOVE_APP));
    private ArrayList<String> mStateToMList = new ArrayList<>(Arrays.asList(HANS_UFZ_REASON_BIND_SERVICE, HANS_UFZ_REASON_START_SERVICE, HANS_UFZ_REASON_PROVIDER, HANS_UFZ_REASON_BROADCAST, HANS_UFZ_REASON_ASYNC_BINDER, HANS_UFZ_REASON_SYNC_BINDER, HANS_UFZ_REASON_TRANSACTION_BINDER, HANS_UFZ_REASON_PACKET, HANS_UFZ_REASON_ALARM, HANS_UFZ_REASON_BUMP_SERVICE, HANS_UFZ_REASON_JOB, HANS_UFZ_REASON_SYNC, HANS_UFZ_REASON_EXECUTING_COMPONENT));
    private ArrayList<String> mStateToRList = new ArrayList<>(Arrays.asList(HANS_UFZ_REASON_WAKELOCK, HANS_UFZ_REASON_SIGNAL, HANS_UFZ_REASON_MEDIAKEY, HANS_UFZ_REASON_SPECIAL_CASE, HANS_UFZ_REASON_NAVIGATION_APP));
    private HansTrigger mTrigger = null;
    private ArrayList<String> mUnfreezeReason = new ArrayList<>(Arrays.asList(HANS_UFZ_REASON_PROVIDER, HANS_UFZ_REASON_SYNC, HANS_UFZ_REASON_MEDIAKEY, HANS_UFZ_REASON_PACKET, HANS_UFZ_REASON_JOB, HANS_UFZ_REASON_START_SERVICE, HANS_UFZ_REASON_BIND_SERVICE, HANS_UFZ_REASON_SPECIAL_CASE));
    private boolean mUserVersion = "user".equals(SystemProperties.get("ro.build.type"));
    private final String nightScene = "night";
    private Map<String, IHansScene> sceneMap = new HashMap();

    private ColorHansManager() {
    }

    public static ColorHansManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorHansManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorHansManager();
                }
            }
        }
        return sInstance;
    }

    public boolean isPreloadPkg(String pkg, int userId) {
        if (ColorResourcePreloadManager.getInstance().isPkgPreload(pkg, userId)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public class HansTrigger {
        final ColorCommonListManager.AppChangedListener listener = new ColorCommonListManager.AppChangedListener() {
            /* class com.android.server.am.ColorHansManager.HansTrigger.AnonymousClass4 */

            @Override // com.android.server.am.ColorCommonListManager.AppChangedListener
            public void onChanged(int uid, String pkgName, String cfgName, boolean isAdd) {
                if (isAdd) {
                    Bundle data = new Bundle();
                    data.putInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, uid);
                    data.putString("pkg", pkgName);
                    data.putString("cfg", cfgName);
                    ColorHansManager.this.mMainHandler.sendMessage(11, data, 0);
                }
            }
        };
        final ContentObserver mGmsObserver = new ContentObserver(ColorHansManager.this.mMainHandler) {
            /* class com.android.server.am.ColorHansManager.HansTrigger.AnonymousClass3 */

            public void onChange(boolean selfChange) {
                HansTrigger.this.setGmsRestrict();
            }
        };
        final GnssStatus.Callback mGnssStatusCallback = new GnssStatus.Callback() {
            /* class com.android.server.am.ColorHansManager.HansTrigger.AnonymousClass5 */

            public void onStarted() {
                ColorHansManager.this.mMainHandler.sendMessage(12, null, 0);
            }

            public void onStopped() {
            }

            public void onFirstFix(int ttffMillis) {
            }

            public void onSatelliteStatusChanged(GnssStatus status) {
            }
        };
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            /* class com.android.server.am.ColorHansManager.HansTrigger.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if ("android.intent.action.SCREEN_ON".equals(action)) {
                        ColorHansManager.this.mCommonConfig.setScreenOn(true);
                        ColorHansManager.this.mHansLogger.addSYSInfo("ScrOn");
                        ColorHansManager.this.hansChangeScene(1);
                        ColorHansManager.this.hansLcdOffScene.stopLcdOffTrigger();
                        ColorHansManager.this.handleUFZSpecialCase();
                        if (ColorHansManager.this.mCommonConfig.hasHansEnable() && !ColorHansManager.this.mCommonConfig.isCharging()) {
                            ColorHansManager.this.hansLcdOnScene.enterStateMachine(-1, "S-LcdOn");
                        }
                    } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        ColorHansManager.this.mCommonConfig.setScreenOn(false);
                        ColorHansManager.this.mCommonConfig.setScreenOffTime(SystemClock.elapsedRealtime());
                        ColorHansManager.this.mHansLogger.addSYSInfo("LcdOff");
                        ColorHansManager.this.hansChangeScene(2);
                        if (ColorHansManager.this.mCommonConfig.hasHansEnable() && !ColorHansManager.this.mCommonConfig.isCharging()) {
                            ColorHansManager.this.hansLcdOnScene.stopStateMachine(-1);
                            ColorHansManager.this.hansLcdOffScene.sendFirstFreeze();
                        }
                    } else if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
                        ColorHansManager.this.mCommonConfig.setCharging(true);
                        ColorHansManager.this.mHansLogger.addSYSInfo("plug");
                        ColorHansManager.this.hansLcdOnScene.stopStateMachine(-1);
                        ColorHansManager.this.hansLcdOffScene.stopLcdOffTrigger();
                        if (ColorHansManager.this.getHansScene() != null) {
                            ColorHansManager.this.getHansScene().hansUnFreeze(ColorHansManager.HANS_UFZ_REASON_CHARGING);
                        }
                        ColorHansManager.this.updateProcessBgTime(true);
                    } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                        ColorHansManager.this.mCommonConfig.setCharging(false);
                        ColorHansManager.this.mHansLogger.addSYSInfo("unplug");
                        if (ColorHansManager.this.mCommonConfig.hasHansEnable()) {
                            if (ColorHansManager.this.mCommonConfig.isScreenOn()) {
                                ColorHansManager.this.hansChangeScene(1);
                                ColorHansManager.this.hansLcdOnScene.enterStateMachine(-1, "S-Disconnect");
                            } else {
                                ColorHansManager.this.hansChangeScene(2);
                                ColorHansManager.this.hansLcdOffScene.sendFirstFreeze();
                            }
                        }
                        ColorHansManager.this.updateProcessBgTime(false);
                    } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action) || "android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                        boolean isDisableNet = intent.getBooleanExtra("deepsleepdisable", false);
                        boolean isDisableBefore = ColorHansManager.this.mCommonConfig.isDisableNetWork();
                        if (ColorHansManager.this.mCommonConfig.hasHansEnable() && isDisableNet != isDisableBefore) {
                            ColorHansManager.this.mCommonConfig.setDisableNetWork(isDisableNet);
                            if (isDisableNet) {
                                ColorHansManager.this.hansLcdOffScene.stopLcdOffTrigger();
                                ColorHansManager.this.hansChangeScene(5);
                                ColorHansManager.this.hansNightScene.sendRepeatFreeze();
                                return;
                            }
                            ColorHansManager.this.hansNightScene.stopNightTrigger();
                            if (!ColorHansManager.this.getCommonConfig().isScreenOn()) {
                                ColorHansManager.this.hansChangeScene(2);
                                ColorHansManager.this.hansLcdOffScene.sendRepeatFreeze();
                            }
                        }
                    } else if ("android.intent.action.DATE_CHANGED".equals(action)) {
                        ColorHansManager.this.handleDateChanged();
                    }
                }
            }
        };
        private final IUidObserver mUidObserver = new IUidObserver.Stub() {
            /* class com.android.server.am.ColorHansManager.HansTrigger.AnonymousClass1 */

            public void onUidGone(int uid, boolean disabled) throws RemoteException {
                synchronized (ColorHansManager.this.mHansLock) {
                    ColorHansManager.this.mHansRunningList.remove(Integer.valueOf(uid));
                    ColorHansPackageSelector.HansPackage hansPackage = (ColorHansPackageSelector.HansPackage) ColorHansManager.this.hansLcdOnScene.mManagedMap.get(uid);
                    if (hansPackage != null) {
                        hansPackage.getStateMachine().stateToD(uid);
                        if (ColorHansManager.this.mCommonConfig.isStatisticsEnable()) {
                            if (hansPackage.getEnterBgTime() > 0) {
                                ColorHansManager.this.notifyLiveTime(uid, hansPackage.getPkgName(), SystemClock.elapsedRealtime() - hansPackage.getEnterBgTime());
                            }
                            hansPackage.setEnterBgTime(0);
                        }
                    }
                }
            }

            public void onUidActive(int uid) throws RemoteException {
                synchronized (ColorHansManager.this.mHansLock) {
                    ColorHansManager.this.mHansRunningList.add(Integer.valueOf(uid));
                    if (ColorHansManager.this.mCommonConfig.isStatisticsEnable()) {
                        ColorHansPackageSelector.HansPackage hansPackage = (ColorHansPackageSelector.HansPackage) ColorHansManager.this.hansLcdOnScene.mManagedMap.get(uid);
                        if (hansPackage != null) {
                            if (ColorHansManager.this.mCommonConfig.isCharging() || uid == ColorHansManager.this.mCurResumedUid) {
                                hansPackage.setEnterBgTime(-1);
                            } else if (hansPackage.getEnterBgTime() == 0) {
                                hansPackage.setEnterBgTime(SystemClock.elapsedRealtime());
                            }
                        }
                    }
                }
            }

            public void onUidIdle(int uid, boolean disabled) throws RemoteException {
                synchronized (ColorHansManager.this.mHansLock) {
                    ColorHansManager.this.mHansRunningList.add(Integer.valueOf(uid));
                    if (ColorHansManager.this.mCommonConfig.isStatisticsEnable()) {
                        ColorHansPackageSelector.HansPackage hansPackage = (ColorHansPackageSelector.HansPackage) ColorHansManager.this.hansLcdOnScene.mManagedMap.get(uid);
                        if (hansPackage != null) {
                            if (ColorHansManager.this.mCommonConfig.isCharging() || uid == ColorHansManager.this.mCurResumedUid) {
                                hansPackage.setEnterBgTime(-1);
                            } else if (hansPackage.getEnterBgTime() == 0) {
                                hansPackage.setEnterBgTime(SystemClock.elapsedRealtime());
                            }
                        }
                    }
                }
            }

            public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            }

            public void onUidCachedChanged(int uid, boolean cached) {
            }
        };

        public HansTrigger() {
            registerUidObserver();
            registerBroadcastAction();
            initCharingStatus();
            initScreenStatus();
            registerGmsRestrictObserve();
            registerCommonListManagerAppChanged();
            registerGnssStatusCallback();
        }

        private void registerUidObserver() {
            try {
                ActivityManager.getService().registerUidObserver(this.mUidObserver, 14, -1, (String) null);
            } catch (Exception e) {
                HansLogger hansLogger = ColorHansManager.this.mHansLogger;
                hansLogger.e("registerUidObserver failed " + e);
            }
        }

        private void registerBroadcastAction() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
            filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            filter.addAction("android.intent.action.DATE_CHANGED");
            filter.setPriority(ColorMultiAppManagerService.USER_ID);
            ColorHansManager.this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter, null, ColorHansManager.this.mMainHandler);
        }

        private void initCharingStatus() {
            Intent batteryStatus = ColorHansManager.this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"), null, null);
            if (batteryStatus != null) {
                boolean z = false;
                int plugType = batteryStatus.getIntExtra("plugged", 0);
                CommonConfig commonConfig = ColorHansManager.this.mCommonConfig;
                if (plugType != 0) {
                    z = true;
                }
                commonConfig.setCharging(z);
            }
        }

        private void initScreenStatus() {
            PowerManager pm = (PowerManager) ColorHansManager.this.mContext.getSystemService("power");
            if (pm != null) {
                if (pm.isInteractive()) {
                    ColorHansManager.this.mHansLogger.addSYSInfo("ScrOn");
                    ColorHansManager.this.hansChangeScene(1);
                    return;
                }
                ColorHansManager.this.mHansLogger.addSYSInfo("LcdOff");
                ColorHansManager.this.hansChangeScene(2);
            }
        }

        private void registerGmsRestrictObserve() {
            if (ColorHansManager.this.mCommonConfig.isChinaRegion() && ColorHansPackageSelector.getInstance().isFreezeGmsEnable()) {
                setGmsRestrict();
                ColorHansManager.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("google_restric_info"), false, this.mGmsObserver, -1);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setGmsRestrict() {
            boolean z = false;
            int value = Settings.Secure.getInt(ColorHansManager.this.mContext.getContentResolver(), "google_restric_info", 0);
            CommonConfig commonConfig = ColorHansManager.this.mCommonConfig;
            if (value == 1) {
                z = true;
            }
            commonConfig.setGmsRestrict(z);
            ColorHansManager.this.mMainHandler.sendMessage(10, null, 0);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleUpdateGmsRestrict() {
            if (ColorHansPackageSelector.getInstance().isFreezeGmsEnable()) {
                HansLogger hansLogger = ColorHansManager.this.mHansLogger;
                hansLogger.d("handleUpdateGmsRestrict " + ColorHansManager.this.mCommonConfig.isRestrictGms());
                ColorHansManager.getInstance().updateTargetMapForScenes(16, null);
            }
        }

        private void registerCommonListManagerAppChanged() {
            ColorHansManager.this.mCommonListManager.addAppChangedListener(this.listener);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleAppChanged(Message msg) {
            int uid;
            if (msg != null && msg.getData() != null) {
                Bundle data = msg.getData();
                int uid2 = data.getInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, -1);
                String pkgName = data.getString("pkg", "");
                String cfgName = data.getString("cfg", "");
                if (!TextUtils.isEmpty(pkgName) && !TextUtils.isEmpty(cfgName)) {
                    if (uid2 < 1) {
                        uid = ColorHansManager.this.mCommonListManager.getPackageUid(pkgName, ColorHansManager.this.mAms.getCurrentUser().id);
                    } else {
                        uid = uid2;
                    }
                    synchronized (ColorHansManager.this.mHansLock) {
                        if (ColorHansManager.this.getHansScene() != null) {
                            ColorHansManager.this.getHansScene().hansUnFreeze(uid, ColorHansManager.HANS_UFZ_REASON_DEFAULT_APP);
                            ColorHansManager.this.hansLcdOnScene.stopStateMachine(uid);
                        }
                    }
                }
            }
        }

        private void registerGnssStatusCallback() {
            if (ColorHansManager.this.mLocationManager == null) {
                ColorHansManager colorHansManager = ColorHansManager.this;
                colorHansManager.mLocationManager = (LocationManager) colorHansManager.mContext.getSystemService("location");
            }
            ColorHansManager.this.mLocationManager.registerGnssStatusCallback(this.mGnssStatusCallback, ColorHansManager.this.mMainHandler);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleUfzNavigationApp() {
            List<String> gpsList = ColorHansManager.this.mCommonListManager.getNavigationList();
            HansLogger hansLogger = ColorHansManager.this.mHansLogger;
            hansLogger.d("UfzForNavigationApp " + gpsList);
            if (gpsList != null) {
                for (String pkgName : gpsList) {
                    int uid = ColorHansManager.this.mCommonListManager.getPackageUid(pkgName, ColorHansManager.this.mAms.getCurrentUser().id);
                    if (uid > 10000 && ColorHansManager.this.getHansScene() != null) {
                        ColorHansManager.this.getHansScene().hansUnFreeze(uid, ColorHansManager.HANS_UFZ_REASON_NAVIGATION_APP);
                    }
                }
            }
        }
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx != null) {
            this.mColorAmsEx = amsEx;
            this.mAms = amsEx.getActivityManagerService();
            this.mContext = this.mAms.mContext;
            initCore();
            return;
        }
        this.mInitAmsEnable = false;
    }

    public void setHansScene(IHansScene hansScene) {
        if (hansScene != null) {
            this.mHansScene = hansScene;
        }
    }

    public IHansScene getHansScene() {
        return this.mHansScene;
    }

    public String getHansSceneName() {
        if (this.mHansScene == null) {
            return " ";
        }
        return coverSceneIDtoStr(this.mCurSceneId);
    }

    public HansSceneBase getHansScene(int sceneId) {
        switch (sceneId) {
            case 1:
                return this.hansLcdOnScene;
            case 2:
                return this.hansLcdOffScene;
            case 3:
                return this.hansAppAbnormalScene;
            case 4:
                return this.hansFastFreezerScene;
            case 5:
                return this.hansNightScene;
            case 6:
                return this.hansPreloadFreezerScene;
            default:
                return null;
        }
    }

    public void hansChangeScene(int sceneId) {
        if (sceneId == 1) {
            setHansScene(this.hansLcdOnScene);
        } else if (sceneId == 2) {
            setHansScene(this.hansLcdOffScene);
        } else if (sceneId == 3) {
            setHansScene(this.hansAppAbnormalScene);
        } else if (sceneId == 4) {
            setHansScene(this.hansFastFreezerScene);
        } else if (sceneId == 5) {
            setHansScene(this.hansNightScene);
        }
        this.mCurSceneId = sceneId;
        this.mHansLogger.addSYSInfo(coverSceneIDtoStr(this.mCurSceneId));
    }

    public void startHansScene(IHansScene hansSceneBase) {
        String name = hansSceneBase.getClass().getName();
        registerHansScene(name.substring(name.lastIndexOf(".")), hansSceneBase);
    }

    public IHansScene getHansScene(String type) {
        Map<String, IHansScene> map = this.sceneMap;
        if (map == null) {
            return null;
        }
        return map.get(type);
    }

    private void registerHansScene(String type, IHansScene hansScene) {
        Map<String, IHansScene> map = this.sceneMap;
        if (map != null && type != null && hansScene != null) {
            map.put(type, hansScene);
        }
    }

    public boolean freezeForPreload(int uid) {
        if (!this.mCommonConfig.hasHansEnable()) {
            return false;
        }
        return this.hansPreloadFreezerScene.preloadFreeze(uid);
    }

    public void unFreezeForPreload(int uid) {
        if (this.mCommonConfig.hasHansEnable()) {
            this.hansPreloadFreezerScene.hansUnFreeze(uid, ColorResourcePreloadDatabaseHelper.PRELOAD_TABLE_NAME);
        }
    }

    public boolean isFreezeTarget(int uid) {
        synchronized (this.mHansLock) {
            HansSceneBase curScene = getHansScene(this.mCurSceneId);
            if (curScene == null) {
                return false;
            }
            SparseArray<ColorHansPackageSelector.HansPackage> managedMap = curScene.getManagedMap();
            if (managedMap == null) {
                return false;
            }
            if (managedMap.get(uid) == null) {
                return false;
            }
            return true;
        }
    }

    public void hansFreeze(String type, int uid) {
        IHansScene hansScene = getHansScene(type);
        if (hansScene != null) {
            hansScene.hansFreeze(uid);
        }
    }

    public void hansFreeze(int uid) {
        IHansScene hansScene = getHansScene();
        if (hansScene != null) {
            hansScene.hansFreeze(uid);
        }
    }

    public void hansFreeze() {
        IHansScene hansScene = getHansScene();
        if (hansScene != null) {
            hansScene.hansFreeze();
        }
    }

    public void hansUnFreeze(int uid, String reason) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage ps = getHansScene(this.mCurSceneId).getManagedMap().get(uid);
            if (ps != null) {
                if (isPreloadPkg(ps.getPkgName(), UserHandle.getUserId(uid))) {
                    this.hansPreloadFreezerScene.hansUnFreeze(uid, reason);
                } else {
                    getHansScene().hansUnFreeze(uid, reason);
                }
            }
        }
    }

    public void hansUnFreeze(String reason) {
        IHansScene hansScene = getHansScene();
        if (hansScene != null) {
            hansScene.hansUnFreeze(reason);
        }
    }

    private void setFirewallChainEnable(int chain, boolean enable) {
        long token = Binder.clearCallingIdentity();
        if (this.mNMs == null) {
            this.mNMs = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        }
        INetworkManagementService iNetworkManagementService = this.mNMs;
        if (iNetworkManagementService != null) {
            try {
                iNetworkManagementService.setFirewallChainEnabled(chain, enable);
            } catch (Exception e) {
                HansLogger hansLogger = this.mHansLogger;
                hansLogger.d("Error occured while setFirewallChainEnable: " + e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public void updateTargetMapForScenes(int updateType, ColorHansPackageSelector.HansPackage hansPackage) {
        for (IHansScene iHansScene : this.sceneMap.values()) {
            iHansScene.updateTargetMap(updateType, hansPackage);
        }
    }

    public void initScenes() {
        for (IHansScene iHansScene : this.sceneMap.values()) {
            iHansScene.onInit();
        }
    }

    private int getFreezeLevel(int uid) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage ps = getHansScene(this.mCurSceneId).getManagedMap().get(uid);
            if (ps == null) {
                return -1;
            }
            return ps.getFreezeLevel();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAllowCpn(int uid, int flag) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.HansPackage hansPackage = getHansScene(this.mCurSceneId).getManagedMap().get(uid);
            boolean z = false;
            if (hansPackage == null) {
                return false;
            }
            if ((hansPackage.getAllowCpnType() & flag) != 0) {
                z = true;
            }
            return z;
        }
    }

    public boolean hansActivityIfNeeded(int callingUid, String callingPackage, int uid, String pkgName, String cpnName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig == null || !commonConfig.hasHansEnable() || getHansScene() == null || !getHansScene().isFreezed(uid)) {
            return true;
        }
        int freezeLevel = getFreezeLevel(uid);
        if (freezeLevel <= 1 || isAllowCpn(uid, 2)) {
            hansUnFreeze(uid, HANS_UFZ_REASON_ACTIVITY);
            return true;
        } else if (!getHansScene().getHansRestriction().isAllowedActivity(callingUid, callingPackage, uid, pkgName, cpnName, freezeLevel)) {
            return false;
        } else {
            hansUnFreeze(uid, HANS_UFZ_REASON_ACTIVITY);
            return true;
        }
    }

    public boolean hansTopActivityIfNeeded(int uid, String pkgName, int windowMode) {
        if (!this.mIsBootCompleted) {
            return true;
        }
        updateResumedActivity(uid, pkgName, windowMode);
        return hansTopActivity(uid, pkgName);
    }

    public boolean hansSecondActivityIfNeeded(int uid, String pkgName, int windowMode) {
        if (1 == windowMode) {
            return hansTopActivity(uid, pkgName);
        }
        return true;
    }

    private boolean hansTopActivity(int uid, String packageName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig == null || !commonConfig.hasHansEnable() || getHansScene() == null) {
            return true;
        }
        if (!getHansScene().isFreezed(uid)) {
            this.hansLcdOnScene.resetStateMachineState(uid);
            return true;
        }
        hansUnFreeze(uid, HANS_UFZ_REASON_TOP_ACTIVITY);
        return true;
    }

    public boolean hansServiceIfNeeded(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, boolean isBind) {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig == null || !commonConfig.hasHansEnable() || getHansScene() == null || !getHansScene().isFreezed(uid)) {
            return true;
        }
        int freezeLevel = getFreezeLevel(uid);
        if (freezeLevel <= 1 || isAllowCpn(uid, 2)) {
            hansUnFreeze(uid, HANS_UFZ_REASON_START_SERVICE);
            return true;
        } else if (!getHansScene().getHansRestriction().isAllowedService(callingUid, callingPackage, uid, pkgName, cpnName, isBind, freezeLevel)) {
            return false;
        } else {
            if (isBind) {
                hansUnFreeze(uid, HANS_UFZ_REASON_BIND_SERVICE);
                return true;
            }
            hansUnFreeze(uid, HANS_UFZ_REASON_START_SERVICE);
            return true;
        }
    }

    public boolean hansProviderIfNeeded(int callingUid, String callingPackage, int uid, String pkgName, String cpnName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig == null || !commonConfig.hasHansEnable() || getHansScene() == null || !getHansScene().isFreezed(uid)) {
            return true;
        }
        int freezeLevel = getFreezeLevel(uid);
        if (freezeLevel <= 1 || isAllowCpn(uid, 2)) {
            hansUnFreeze(uid, HANS_UFZ_REASON_PROVIDER);
            return true;
        } else if (!getHansScene().getHansRestriction().isAllowedProvider(callingUid, callingPackage, uid, pkgName, cpnName, freezeLevel)) {
            return false;
        } else {
            hansUnFreeze(uid, HANS_UFZ_REASON_PROVIDER);
            return true;
        }
    }

    public boolean hansBroadcastIfNeeded(BroadcastRecord r, Object o) {
        int uid;
        String pkgName;
        String str;
        int uid2;
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig == null || !commonConfig.hasHansEnable() || getHansScene() == null) {
            return true;
        }
        if (o instanceof BroadcastFilter) {
            uid = ((BroadcastFilter) o).owningUid;
            pkgName = ((BroadcastFilter) o).packageName;
        } else if (o instanceof ResolveInfo) {
            uid = ((ResolveInfo) o).activityInfo.applicationInfo.uid;
            pkgName = ((ResolveInfo) o).activityInfo.packageName;
        } else if (o instanceof ActivityInfo) {
            uid = ((ActivityInfo) o).applicationInfo.uid;
            pkgName = ((ActivityInfo) o).packageName;
        } else {
            uid = -1;
            pkgName = "";
        }
        if (!getHansScene().isFreezed(uid)) {
            return true;
        }
        int freezeLevel = getFreezeLevel(uid);
        if (freezeLevel <= 1) {
            str = HANS_UFZ_REASON_BROADCAST;
            uid2 = uid;
        } else if (isAllowCpn(uid, 2)) {
            str = HANS_UFZ_REASON_BROADCAST;
            uid2 = uid;
        } else {
            int callingUid = r.callingUid;
            String callingPackage = r.callerPackage;
            String action = r.intent.getAction();
            if (!getHansScene().getHansRestriction().isAllowedBroadcast(callingUid, callingPackage, uid, pkgName, action, r.ordered, freezeLevel)) {
                return false;
            }
            if (this.mHansBroadcastProxy.isNeedProxy(action, uid, pkgName)) {
                this.mHansBroadcastProxy.proxyBroadcastRecord(action, r);
                this.mHansBroadcastProxy.proxyBroadcastReceiver(action, o);
                return false;
            }
            notifyUnFreezeReason(uid, pkgName, HANS_UFZ_REASON_BROADCAST, getHansSceneName(), action);
            hansUnFreeze(uid, HANS_UFZ_REASON_BROADCAST);
            return true;
        }
        hansUnFreeze(uid2, str);
        return true;
    }

    public boolean hansSyncIfNeeded(int uid, String pkgName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig == null || !commonConfig.hasHansEnable() || getHansScene() == null || !getHansScene().isFreezed(uid)) {
            return true;
        }
        int freezeLevel = getFreezeLevel(uid);
        if (freezeLevel <= 1 || isAllowCpn(uid, 18)) {
            hansUnFreeze(uid, HANS_UFZ_REASON_SYNC);
            return true;
        } else if (!getHansScene().getHansRestriction().isAllowedSync(uid, pkgName, freezeLevel)) {
            return false;
        } else {
            hansUnFreeze(uid, HANS_UFZ_REASON_SYNC);
            return true;
        }
    }

    public boolean hansJobIfNeeded(int uid, String pkgName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig == null || !commonConfig.hasHansEnable() || getHansScene() == null || !getHansScene().isFreezed(uid)) {
            return true;
        }
        int freezeLevel = getFreezeLevel(uid);
        if (freezeLevel <= 1 || isAllowCpn(uid, 2)) {
            hansUnFreeze(uid, HANS_UFZ_REASON_JOB);
            return true;
        } else if (!getHansScene().getHansRestriction().isAllowedJob(uid, pkgName, freezeLevel)) {
            return false;
        } else {
            hansUnFreeze(uid, HANS_UFZ_REASON_JOB);
            return true;
        }
    }

    public boolean hansAlarmIfNeeded(String action, int uid, String pkgName) {
        if (ColorResourcePreloadManager.getInstance().isPreloadEnable() && ColorResourcePreloadManager.getInstance().preloadAlarmBlock(action, uid, pkgName)) {
            return false;
        }
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig == null || !commonConfig.hasHansEnable() || getHansScene() == null || !getHansScene().isFreezed(uid)) {
            return true;
        }
        int freezeLevel = getFreezeLevel(uid);
        if (freezeLevel <= 1 || isAllowCpn(uid, 6)) {
            hansUnFreeze(uid, HANS_UFZ_REASON_ALARM);
            return true;
        } else if (!getHansScene().getHansRestriction().isAllowedAlarm(action, uid, pkgName, freezeLevel)) {
            return false;
        } else {
            notifyUnFreezeReason(uid, pkgName, HANS_UFZ_REASON_ALARM, getHansSceneName(), action);
            hansUnFreeze(uid, HANS_UFZ_REASON_ALARM);
            return true;
        }
    }

    public boolean hansMediaEventIfNeeded(int uid, String pkgName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig == null || !commonConfig.hasHansEnable() || getHansScene() == null || !getHansScene().isFreezed(uid)) {
            return true;
        }
        hansUnFreeze(uid, HANS_UFZ_REASON_MEDIAKEY);
        return true;
    }

    public boolean hansPackageTimeout(int uid, String pkgName) {
        return true;
    }

    public void hansBumpService(int uid, String pkgName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig != null && commonConfig.hasHansEnable() && getHansScene() != null && getHansScene().isFreezed(uid)) {
            hansUnFreeze(uid, HANS_UFZ_REASON_BUMP_SERVICE);
        }
    }

    public void unfreezeForKernel(int type, int callerPid, int uid, String rpcName, int code) {
        int i;
        Object obj;
        Throwable th;
        this.mHansLogger.fullLog("receiver from hans: (" + type + "," + callerPid + "," + uid + "), rpcName: " + rpcName + ", code: " + code);
        if ("disablehans".equals(rpcName)) {
            hansDeamonSwitch(false);
            return;
        }
        String reason = "Default";
        boolean flag = true;
        if (type == 0) {
            this.mHansLogger.addAsyncBinderInfo("" + callerPid + "|" + uid + "|" + rpcName + "|" + code);
            if ("free_buffer_full".equals(rpcName)) {
                flag = true;
                i = uid;
                reason = HANS_UFZ_REASON_ASYNC_BINDER;
            } else {
                Object obj2 = this.mHansLock;
                synchronized (obj2) {
                    try {
                        ColorHansPackageSelector.HansPackage ps = getHansScene(this.mCurSceneId).getManagedMap().get(uid);
                        if (ps == null) {
                            obj = obj2;
                            flag = false;
                            i = uid;
                        } else if (!ps.getFreezed()) {
                            obj = obj2;
                            flag = false;
                            i = uid;
                        } else if (!getHansScene().getHansRestriction().isAllowedBinder(callerPid, uid, ps.getPkgName(), rpcName, code, true)) {
                            obj = obj2;
                            flag = false;
                            i = uid;
                        } else {
                            String pkgName = ps.getPkgName();
                            obj = obj2;
                            i = uid;
                            try {
                                notifyUnFreezeReason(uid, pkgName, HANS_UFZ_REASON_ASYNC_BINDER, getHansSceneName(), rpcName + "|" + code);
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        reason = HANS_UFZ_REASON_ASYNC_BINDER;
                    } catch (Throwable th3) {
                        th = th3;
                        obj = obj2;
                        throw th;
                    }
                }
            }
        } else if (type == 1) {
            reason = HANS_UFZ_REASON_SYNC_BINDER;
            i = uid;
        } else if (type == 2) {
            reason = HANS_UFZ_REASON_TRANSACTION_BINDER;
            i = uid;
        } else if (type == 3) {
            reason = HANS_UFZ_REASON_SIGNAL;
            i = uid;
        } else if (type != 4) {
            i = uid;
        } else {
            reason = HANS_UFZ_REASON_PACKET;
            i = uid;
        }
        if (flag) {
            hansUnFreeze(i, reason);
        }
    }

    public void hansUpdateForegroundServiceState(int uid, String pkgName, boolean isForeground) {
        synchronized (this.mHansLock) {
            if (this.mCurSceneId != -1) {
                ColorHansPackageSelector.HansPackage ps = getHansScene(this.mCurSceneId).getManagedMap().get(uid);
                if (ps != null) {
                    if (isForeground) {
                        ps.setFgService(true);
                    } else {
                        ps.setFgService(false);
                    }
                }
            }
        }
    }

    public void hansDeamonSwitch(boolean enable) {
        if (enable) {
            SystemProperties.set("sys.hans.enable", "true");
            return;
        }
        SystemProperties.set("sys.hans.enable", "false");
        this.mCommonConfig.setHansEnable(false);
        this.hansLcdOffScene.stopLcdOffTrigger();
        this.hansLcdOnScene.stopStateMachine(-1);
        hansUnFreeze(HANS_UFZ_REASON_DISABLE);
        HansLogger hansLogger = this.mHansLogger;
        if (hansLogger != null) {
            hansLogger.addSYSInfo("disablehans");
        }
    }

    public void dumpHansInfo(FileDescriptor fd, PrintWriter pw, int sceneId) {
        HansLogger hansLogger;
        SparseArray<ColorHansPackageSelector.HansPackage> mManagedMap = getHansScene(sceneId).getManagedMap();
        StringBuffer sb = new StringBuffer();
        sb.append("Hans Info(dumpsys activity hans)\n");
        sb.append("\n");
        sb.append("Hans Manage apps -- size ");
        sb.append(mManagedMap.size() + "\n");
        for (int i = 0; i < mManagedMap.size(); i++) {
            ColorHansPackageSelector.HansPackage ps = mManagedMap.valueAt(i);
            sb.append("pkg: " + String.format("%-50s", ps.getPkgName()));
            sb.append(" uid: " + String.format("%8d", Integer.valueOf(ps.getUid())));
            sb.append(" type: " + String.format("%3d", Integer.valueOf(ps.getAppType())));
            sb.append(" level: " + String.format("%2d", Integer.valueOf(ps.getFreezeLevel())));
            sb.append(" cpnAllow: " + String.format("%2d", Integer.valueOf(ps.getAllowCpnType())));
            sb.append(" scene: " + String.format("%3d", Integer.valueOf(ps.getScene())));
            if (ps.getFreezed()) {
                sb.append(" --(F) ");
            } else if (this.mHansRunningList.contains(Integer.valueOf(ps.getUid()))) {
                sb.append(String.format(" --(R) --(%s) ", ps.getStateMachine().getState().toStr()));
                if (!"".equals(ps.getImportantReason())) {
                    sb.append(String.format(" --(IR|%s) ", ps.getImportantReason()));
                }
                if (!"".equals(ps.getUnFreezeReason())) {
                    sb.append(String.format(" --(UR|%s) ", ps.getUnFreezeReason()));
                }
            }
            sb.append("\n");
        }
        sb.append("\n");
        if (!this.mUserVersion || (hansLogger = this.mHansLogger) == null) {
            pw.println(sb.toString());
        } else {
            pw.println(hansLogger.hansLogEncrypt(sb.toString()));
        }
    }

    public void dumpHansInfo(FileDescriptor fd, PrintWriter pw) {
        dumpHansInfo(fd, pw, 2);
    }

    public void dumpHansHistory(FileDescriptor fd, PrintWriter pw) {
        HansLogger hansLogger;
        dumpHansInfo(fd, pw);
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append("Hans History(dumpsys activity hans history)\n");
        sb.append("currentTime: ");
        sb.append(formatDateTime(System.currentTimeMillis()) + "\n");
        sb.append("\n");
        if (!this.mUserVersion || (hansLogger = this.mHansLogger) == null) {
            pw.println(sb.toString());
        } else {
            pw.println(hansLogger.hansLogEncrypt(sb.toString()));
        }
        this.mHansLogger.dumpLogBuffer(fd, pw);
    }

    public void dumpHans(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args.length == 2 && "history".equals(args[1])) {
            dumpHansHistory(fd, pw);
        }
        if (SystemProperties.getBoolean("persist.sys.hans.dump", false)) {
            if (args.length == 1) {
                dumpHansInfo(fd, pw);
                return;
            }
            if ("scene".equals(args[1])) {
                try {
                    dumpHansInfo(fd, pw, Integer.parseInt(args[2]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            if ("chain".equals(args[1])) {
                if ("0".equals(args[2])) {
                    setFirewallChainEnable(4, false);
                } else if ("1".equals(args[2])) {
                    setFirewallChainEnable(4, true);
                }
            }
            if ("firewall".equals(args[1])) {
                try {
                    int uid = Integer.parseInt(args[2]);
                    if ("0".equals(args[3])) {
                        this.hansLcdOffScene.updateHansUidFirewall(uid, false);
                        this.hansLcdOffScene.closeSocketsForHansFirewallChain();
                    } else if ("1".equals(args[3])) {
                        this.hansLcdOffScene.updateHansUidFirewall(uid, true);
                    }
                } catch (NumberFormatException e2) {
                    e2.printStackTrace();
                }
            }
            if (OppoDynamicLogManager.INVOKE_DUMP_NAME.equals(args[1])) {
                try {
                    if ("dbconfig".equals(args[2])) {
                        ColorHansPackageSelector.getInstance().getDBConfig().dumpList();
                    }
                    if ("commonlist".equals(args[2])) {
                        ColorCommonListManager.getInstance().dumpCommonMap();
                    }
                    if ("hanslist".equals(args[2])) {
                        HansLogger hansLogger = this.mHansLogger;
                        hansLogger.i("mSpecialWindowList: " + this.mSpecialWindowList);
                    }
                } catch (Exception e3) {
                }
            }
            if ("freeze".equals(args[1])) {
                try {
                    hansFreeze(Integer.parseInt(args[2]));
                } catch (Exception e4) {
                }
            }
            if ("unfreeze".equals(args[1])) {
                try {
                    hansUnFreeze(Integer.parseInt(args[2]), "Default");
                } catch (Exception e5) {
                }
            }
            if ("freezeall".equals(args[1])) {
                hansFreeze();
            }
            if ("unfreezeall".equals(args[1])) {
                hansUnFreeze("Default");
            }
            if ("checkfreeze".equals(args[1])) {
                try {
                    int uid2 = Integer.parseInt(args[2]);
                    if (getHansScene() != null) {
                        pw.println("" + getHansScene().isFreezed(uid2));
                    }
                } catch (Exception e6) {
                }
            }
            if ("historyswitch".equals(args[1])) {
                if (Integer.parseInt(args[2]) == 1) {
                    this.mUserVersion = false;
                } else {
                    this.mUserVersion = true;
                }
            }
            if ("disable".equals(args[1])) {
                if (Integer.parseInt(args[2]) == 1) {
                    hansDeamonSwitch(true);
                } else {
                    hansDeamonSwitch(false);
                }
            }
            if ("log".equals(args[1])) {
                this.mHansLogger.setFullLog(Boolean.parseBoolean(args[2]));
            }
            if ("deepsleep".equals(args[1])) {
                boolean isDisableNet = false;
                try {
                    isDisableNet = Boolean.parseBoolean(args[2]);
                    this.mCommonConfig.setDisableNetWork(isDisableNet);
                } catch (Exception e7) {
                }
                if (isDisableNet) {
                    this.hansLcdOffScene.stopLcdOffTrigger();
                    hansChangeScene(5);
                    this.hansNightScene.sendRepeatFreeze();
                }
            }
        }
    }

    public void bootCompleted() {
        initOther();
        unFreezeForBoot();
        this.mIsBootCompleted = true;
    }

    private void unFreezeForBoot() {
        writePidsToDevFile(getFrozenPidsFromPath(), HansSceneBase.DEV_UNFREEZE_PATH);
    }

    private ArrayList<Integer> getFrozenPidsFromPath() {
        ArrayList<Integer> pidList = new ArrayList<>();
        BufferedReader br = null;
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(HansSceneBase.DEV_FREEZE_PATH));
            for (String result = br2.readLine(); result != null; result = br2.readLine()) {
                Integer pid = Integer.valueOf(result);
                if (!pidList.contains(pid)) {
                    pidList.add(pid);
                }
            }
            try {
                br2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (0 != 0) {
                br.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    br.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
        return pidList;
    }

    private void writePidsToDevFile(ArrayList<Integer> pidList, String devPath) {
        if (pidList != null) {
            FileOutputStream fos = null;
            try {
                FileOutputStream fos2 = new FileOutputStream(new File(devPath));
                for (int i = 0; i < pidList.size(); i++) {
                    fos2.write(String.valueOf(pidList.get(i).intValue()).getBytes("UTF-8"));
                    fos2.flush();
                }
                try {
                    fos2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                if (0 != 0) {
                    fos.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        fos.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    public void unfreezeForWatchdog() {
        writePidsToDevFile(getFrozenPidsFromPath(), HansSceneBase.DEV_UNFREEZE_PATH);
    }

    public void addIsolatedUid(int isolatedUid, int appUid) {
        synchronized (this.mIsolatedUids) {
            ArrayList<Integer> uids = this.mIsolatedUids.get(appUid);
            if (uids == null) {
                uids = new ArrayList<>();
                this.mIsolatedUids.put(appUid, uids);
            }
            uids.add(Integer.valueOf(isolatedUid));
        }
    }

    public void removeIsolatedUid(int isolatedUid, int appUid) {
        synchronized (this.mIsolatedUids) {
            ArrayList<Integer> uids = this.mIsolatedUids.get(appUid);
            if (uids != null) {
                if (uids.contains(Integer.valueOf(isolatedUid))) {
                    uids.remove(Integer.valueOf(isolatedUid));
                }
                if (uids.isEmpty()) {
                    this.mIsolatedUids.remove(appUid);
                }
            }
        }
    }

    public void hansHandleWindowsVisible(int curUid, String curPkgName) {
        HansMainHandler hansMainHandler;
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig != null && commonConfig.hasHansEnable() && this.mIsBootCompleted && this.hansFastFreezerScene.isFastFreezeOn() && (hansMainHandler = this.mMainHandler) != null) {
            hansMainHandler.post(new Runnable() {
                /* class com.android.server.am.$$Lambda$ColorHansManager$Y7SQD3_PZK6Oico_WOKA8Sm99eE */

                public final void run() {
                    ColorHansManager.this.lambda$hansHandleWindowsVisible$0$ColorHansManager();
                }
            });
        }
    }

    public /* synthetic */ void lambda$hansHandleWindowsVisible$0$ColorHansManager() {
        this.hansFastFreezerScene.exitFasterFreezer();
        if (getCommonConfig().isScreenOn()) {
            hansChangeScene(1);
            this.hansLcdOnScene.enterStateMachine(-1, "S-FDirect");
        }
    }

    /* access modifiers changed from: protected */
    public ArrayList<Integer> getIsolatedUids(int appUid) {
        synchronized (this.mIsolatedUids) {
            ArrayList<Integer> uids = this.mIsolatedUids.get(appUid);
            if (uids != null) {
                return uids;
            }
            return null;
        }
    }

    private void updateResumedActivity(int curUid, String curPkgName, int windowMode) {
        ColorHansPackageSelector.HansPackage lastResumedPkg;
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig != null && commonConfig.hasHansEnable()) {
            boolean isAppEnter = false;
            if (curPkgName != null) {
                if (this.mLastResumedPkgName == null) {
                    isAppEnter = true;
                    this.mLastResumedUid = curUid;
                    this.mLastResumedPkgName = curPkgName;
                } else if (!curPkgName.equals(this.mCurResumedPkg) || curUid != this.mCurResumedUid) {
                    isAppEnter = true;
                    this.mLastResumedUid = this.mCurResumedUid;
                    this.mLastResumedPkgName = this.mCurResumedPkg;
                }
                this.mCurResumedUid = curUid;
                this.mCurResumedPkg = curPkgName;
                int bgUid = this.mLastResumedUid;
                if (isAppEnter && this.mCommonConfig.isScreenOn() && !this.mCommonConfig.isCharging()) {
                    HansLogger hansLogger = this.mHansLogger;
                    hansLogger.i("ColorHansManager ", "resume pkg: " + curPkgName + ", uid:" + this.mCurResumedUid + ", prev pkg: " + this.mLastResumedPkgName + ", prev uid: " + this.mLastResumedUid);
                    HansMainHandler hansMainHandler = this.mMainHandler;
                    if (hansMainHandler != null) {
                        hansMainHandler.post(new Runnable(windowMode, curPkgName, curUid) {
                            /* class com.android.server.am.$$Lambda$ColorHansManager$JzJy7Mpx9No8_0PvNdZzDnFxpg */
                            private final /* synthetic */ int f$1;
                            private final /* synthetic */ String f$2;
                            private final /* synthetic */ int f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            public final void run() {
                                ColorHansManager.this.lambda$updateResumedActivity$1$ColorHansManager(this.f$1, this.f$2, this.f$3);
                            }
                        });
                        if (this.mCommonConfig.isStatisticsEnable()) {
                            synchronized (this.mHansLock) {
                                long now = SystemClock.elapsedRealtime();
                                if (windowMode == 1 && (lastResumedPkg = (ColorHansPackageSelector.HansPackage) this.hansLcdOnScene.mManagedMap.get(this.mLastResumedUid)) != null) {
                                    lastResumedPkg.setEnterBgTime(now);
                                }
                                ColorHansPackageSelector.HansPackage curResumedPkg = (ColorHansPackageSelector.HansPackage) this.hansLcdOnScene.mManagedMap.get(this.mCurResumedUid);
                                if (curResumedPkg != null && curResumedPkg.getEnterBgTime() > 0) {
                                    notifyLiveTime(this.mCurResumedUid, curPkgName, now - curResumedPkg.getEnterBgTime());
                                }
                            }
                        }
                    }
                    if (this.hansLcdOnScene.isLcdOnSceneTarget(this.mLastResumedUid)) {
                        this.hansLcdOnScene.sendEnterStateMachineMsg(bgUid, "S-Bg");
                    }
                }
            }
        }
    }

    public /* synthetic */ void lambda$updateResumedActivity$1$ColorHansManager(int windowMode, String curPkgName, int curUid) {
        updateSpecialWindow(this.mLastResumedPkgName, this.mCurResumedPkg, windowMode);
        if (!mSkipFastFreezeList.contains(curPkgName)) {
            hansChangeScene(4);
            this.hansFastFreezerScene.enterFastFreezer(curUid);
        }
    }

    public boolean isHansFreezed(int uid, String pkgName, int scene, String from) {
        boolean result = false;
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig == null || !commonConfig.hasHansEnable()) {
            return false;
        }
        if (scene == -1 || ((this.mCommonConfig.isScreenOn() && scene == 1) || (!this.mCommonConfig.isScreenOn() && scene == 2))) {
            HansSceneBase sceneBase = getHansScene(this.mCurSceneId);
            if (sceneBase != null) {
                result = sceneBase.isFreezed(uid);
            }
            if (result) {
                this.mHansLogger.d("frozen " + uid + " " + pkgName + " from " + from + " " + scene);
            }
        }
        return result;
    }

    private void initCore() {
        HandlerThread thread = new HandlerThread("HansManagerHandler", -2);
        thread.start();
        this.mMainHandler = new HansMainHandler(thread.getLooper());
        HandlerThread statethread = new HandlerThread("StateMachineHandler", -2);
        statethread.start();
        this.mStateMachineHandler = new StateMachineHandler(statethread.getLooper());
        HandlerThread nativeThread = new HandlerThread("HansNativeThreadHandler", -2);
        nativeThread.start();
        this.mNativeHandler = new Handler(nativeThread.getLooper());
    }

    private void initOther() {
        this.mHansManageEnable = SystemProperties.getBoolean("persist.vendor.enable.hans", false);
        this.mCommonListManager = ColorCommonListManager.getInstance();
        initLogBuffer();
        this.mCommonConfig = new CommonConfig();
        this.hansLcdOffScene = new HansLcdOffScene();
        this.hansLcdOnScene = new HansLcdOnScene();
        this.hansAppAbnormalScene = new HansAppAbnormalScene();
        this.hansFastFreezerScene = new HansFastFreezerScene();
        this.hansNightScene = new HansNightScene();
        this.hansPreloadFreezerScene = new HansPreloadFreezerScene();
        startHansScene(this.hansLcdOffScene);
        startHansScene(this.hansLcdOnScene);
        startHansScene(this.hansAppAbnormalScene);
        startHansScene(this.hansFastFreezerScene);
        startHansScene(this.hansNightScene);
        startHansScene(this.hansPreloadFreezerScene);
        initScenes();
        this.mTrigger = new HansTrigger();
        setFirewallChainEnable(4, true);
        this.mNativeService = new HansNativeService();
        this.mHansBroadcastProxy = new HansBroadcastProxy();
    }

    private void updateSpecialWindow(String prevPkg, String curPkg, int windowMode) {
        if (!TextUtils.isEmpty(prevPkg) && !prevPkg.equals(curPkg)) {
            if (windowMode == 3 || windowMode == 4) {
                synchronized (this.mSpecialWindowList) {
                    if (!this.mSpecialWindowList.contains(prevPkg)) {
                        this.mSpecialWindowList.add(prevPkg);
                    }
                }
            }
            if (windowMode == ColorZoomWindowManager.WINDOWING_MODE_ZOOM && !this.mIsZoomWindowMode) {
                this.mIsZoomWindowMode = true;
                synchronized (this.mSpecialWindowList) {
                    if (!this.mSpecialWindowList.contains(prevPkg)) {
                        this.mSpecialWindowList.add(prevPkg);
                    }
                }
            }
            if (windowMode == 1) {
                synchronized (this.mSpecialWindowList) {
                    if (!this.mSpecialWindowList.isEmpty()) {
                        this.mSpecialWindowList.clear();
                        this.mIsZoomWindowMode = false;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean inSpecialWindowMode(String packageName) {
        boolean contains;
        synchronized (this.mSpecialWindowList) {
            contains = this.mSpecialWindowList.contains(packageName);
        }
        return contains;
    }

    /* access modifiers changed from: protected */
    public int getLastResumeUid() {
        return this.mLastResumedUid;
    }

    /* access modifiers changed from: protected */
    public String getCurResumePkgName() {
        return this.mCurResumedPkg;
    }

    /* access modifiers changed from: protected */
    public int getCurResumeUid() {
        return this.mCurResumedUid;
    }

    /* access modifiers changed from: protected */
    public String getLastResumePkgName() {
        return this.mLastResumedPkgName;
    }

    /* access modifiers changed from: package-private */
    public class StateMachineHandler extends Handler {
        final int MSG_ENTER_MACHINE = 1;

        public StateMachineHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Bundle bundle;
            if (msg.what == 1 && (bundle = msg.getData()) != null) {
                int uid = bundle.getInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, -1);
                String reason = bundle.getString("reason", null);
                if (uid != -1 && reason != null) {
                    ColorHansManager.this.hansLcdOnScene.enterStateMachine(uid, reason);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class HansMainHandler extends Handler {
        public static final int HANS_MSG_KILL_ABNORMAL_APP = 20;
        public static final int MSG_CHECK_JOB_WAKELOCK = 21;
        static final int MSG_UNFREEZE_DEFAULT_APP = 11;
        static final int MSG_UNFREEZE_GMS = 10;
        static final int MSG_UNFREEZE_NAVIGATION_APP = 12;

        public HansMainHandler(Looper looper) {
            super(looper);
        }

        public void sendMessage(int what, int arg1, int arg2, long delay) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            ColorHansManager.this.mMainHandler.sendMessageDelayed(msg, delay);
        }

        public void sendMessage(int what, Bundle data, long delay) {
            Message msg = Message.obtain();
            msg.what = what;
            if (data != null) {
                msg.setData(data);
            }
            ColorHansManager.this.mMainHandler.sendMessageDelayed(msg, delay);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 20) {
                ColorHansManager.this.handleKillAbnormalApp(msg);
            } else if (i != 21) {
                switch (i) {
                    case 10:
                        if (ColorHansManager.this.mTrigger != null) {
                            ColorHansManager.this.mTrigger.handleUpdateGmsRestrict();
                            return;
                        }
                        return;
                    case 11:
                        if (ColorHansManager.this.mTrigger != null) {
                            ColorHansManager.this.mTrigger.handleAppChanged(msg);
                            return;
                        }
                        return;
                    case 12:
                        if (ColorHansManager.this.mTrigger != null) {
                            ColorHansManager.this.mTrigger.handleUfzNavigationApp();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            } else {
                ColorHansManager colorHansManager = ColorHansManager.this;
                if (colorHansManager.getHansScene(colorHansManager.mCurSceneId) != null) {
                    ColorHansManager colorHansManager2 = ColorHansManager.this;
                    colorHansManager2.getHansScene(colorHansManager2.mCurSceneId).freeze(msg.arg1, ColorHansManager.HANS_UFZ_REASON_WAKELOCK);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public String coverSceneIDtoStr(int id) {
        switch (id) {
            case 1:
                return "LcdOnScene";
            case 2:
                return "LcdOffScene";
            case 3:
                return "AbnormalScene";
            case 4:
                return "FastFreezerScene";
            case 5:
                return "DeepSleepScene";
            case 6:
                return "PreloadFreezerScene";
            default:
                return "DefaultScene";
        }
    }

    /* access modifiers changed from: protected */
    public void handleUFZSpecialCase() {
        synchronized (this.mHansLock) {
            SparseArray<ColorHansPackageSelector.HansPackage> managedMap = getHansScene(2).getManagedMap();
            if (managedMap != null) {
                for (int i = 0; i < managedMap.size(); i++) {
                    ColorHansPackageSelector.HansPackage ps = managedMap.valueAt(i);
                    if (ps != null) {
                        if (this.hansLcdOffScene.isSpecialImportantCase(ps)) {
                            this.hansLcdOffScene.hansUnFreeze(ps.getUid(), HANS_UFZ_REASON_SPECIAL_CASE);
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKillAbnormalApp(Message msg) {
        if (msg != null) {
            Bundle data = msg.getData();
            int uid = data.getInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, 0);
            String pkgName = data.getString("pkg", "");
            if (!TextUtils.isEmpty(pkgName) && getHansScene() != null && getHansScene().getHansRestriction() != null && !getHansScene().getHansRestriction().isAllowStart(pkgName, uid) && !ColorHansPackageSelector.getInstance().isGmsPackage(uid, pkgName) && !ColorHansPackageSelector.getInstance().isAbnormalKillWhiteList(pkgName)) {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("hans kill abnormal app ");
                    sb.append(uid);
                    sb.append(" ");
                    sb.append(pkgName);
                    this.mHansLogger.i(sb.toString());
                    this.mAms.forceStopPackage(pkgName, UserHandle.getUserId(uid));
                    this.mHansLogger.addSYSInfo(sb.toString());
                } catch (Exception e) {
                    HansLogger hansLogger = this.mHansLogger;
                    hansLogger.e("hans kill abnormal app error " + e);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class CommonConfig {
        private boolean mCharging = false;
        private boolean mChinaModel = true;
        private boolean mDisableNetwork = false;
        private boolean mGmsRestrict = false;
        private boolean mHansEnable = true;
        private long mScreenOffTime = 0;
        private boolean mScreenOn = true;
        private boolean mStatisticsEnable = false;

        public CommonConfig() {
            setChinaRegion(isChinaModel());
        }

        public boolean isChinaRegion() {
            return this.mChinaModel;
        }

        public void setHansEnable(boolean enable) {
            this.mHansEnable = enable;
        }

        public boolean hasHansEnable() {
            return this.mHansEnable & ColorHansManager.this.mInitAmsEnable & ColorHansManager.this.mHansManageEnable;
        }

        public void setStatisticsEnable(boolean mStatisticsEnable2) {
            this.mStatisticsEnable = mStatisticsEnable2;
        }

        public boolean isStatisticsEnable() {
            return this.mStatisticsEnable;
        }

        public void setCharging(boolean isCharging) {
            this.mCharging = isCharging;
        }

        public boolean isCharging() {
            return this.mCharging;
        }

        public void setScreenOn(boolean isScreenOn) {
            this.mScreenOn = isScreenOn;
        }

        public boolean isScreenOn() {
            return this.mScreenOn;
        }

        public void setGmsRestrict(boolean isRestrict) {
            this.mGmsRestrict = isRestrict;
        }

        public boolean isRestrictGms() {
            return this.mGmsRestrict;
        }

        public void setScreenOffTime(long mScreenOffTime2) {
            this.mScreenOffTime = mScreenOffTime2;
        }

        public long getScreenOffTime() {
            return this.mScreenOffTime;
        }

        public void setDisableNetWork(boolean isDisable) {
            this.mDisableNetwork = isDisable;
        }

        public boolean isDisableNetWork() {
            return this.mDisableNetwork;
        }

        private void setChinaRegion(boolean isChinaModel) {
            this.mChinaModel = isChinaModel;
        }

        private boolean isChinaModel() {
            try {
                if (ColorHansManager.this.mContext != null) {
                    return !ColorHansManager.this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class HansBroadcastProxy {
        public HansBroadcastProxy() {
        }

        public boolean isNeedProxy(String action, int uid, String pkgName) {
            boolean result;
            if (!ColorHansManager.this.mCommonConfig.isChinaRegion()) {
                return false;
            }
            synchronized (ColorHansManager.this.mHansLock) {
                result = ColorHansManager.this.mPendingIntentList.contains(action);
            }
            if (!result || !ColorHansManager.this.getHansScene().getHansRestriction().isAllowStart(pkgName, uid)) {
                return result;
            }
            return false;
        }

        public void proxyBroadcastRecord(String action, BroadcastRecord br) {
            synchronized (ColorHansManager.this.mHansLock) {
                if (ColorHansManager.this.mBroadcastRecordMap != null) {
                    HansLogger hansLogger = ColorHansManager.this.mHansLogger;
                    hansLogger.fullLog("ProxyBroadcast(" + action + ",br:" + br + ")");
                    ArrayList<BroadcastRecord> brList = (ArrayList) ColorHansManager.this.mBroadcastRecordMap.get(action);
                    if (brList == null) {
                        brList = new ArrayList<>();
                        ColorHansManager.this.mBroadcastRecordMap.put(action, brList);
                    }
                    int brCount = brList.size();
                    int i = 0;
                    while (true) {
                        if (i >= brCount) {
                            break;
                        }
                        BroadcastRecord broadcatRecord = brList.get(i);
                        if (br.ordered == broadcatRecord.ordered && br.userId == broadcatRecord.userId) {
                            brList.set(i, br);
                            break;
                        }
                        i++;
                    }
                    if (i >= brCount) {
                        brList.add(br);
                    }
                    ColorHansManager.this.mBroadcastRecordMap.put(action, brList);
                }
            }
        }

        public void proxyBroadcastReceiver(String action, Object obj) {
            synchronized (ColorHansManager.this.mHansLock) {
                if (ColorHansManager.this.mReceiverMap != null) {
                    HansLogger hansLogger = ColorHansManager.this.mHansLogger;
                    hansLogger.fullLog("ProxyReceiver(" + action + ",receiver:" + obj + ")");
                    ArrayList<Object> receiverList = (ArrayList) ColorHansManager.this.mReceiverMap.get(action);
                    if (receiverList == null) {
                        receiverList = new ArrayList<>();
                        ColorHansManager.this.mReceiverMap.put(action, receiverList);
                    }
                    int receiverCount = receiverList.size();
                    int i = 0;
                    while (true) {
                        if (i >= receiverCount) {
                            break;
                        } else if (obj.equals(receiverList.get(i))) {
                            receiverList.set(i, obj);
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (i >= receiverCount) {
                        receiverList.add(obj);
                    }
                    ColorHansManager.this.mReceiverMap.put(action, receiverList);
                }
            }
        }

        public void unProxyBroadcast(String pkgName, int userId) {
            ArrayMap<BroadcastRecord, ArrayList> dispatchedMap = new ArrayMap<>();
            synchronized (ColorHansManager.this.mHansLock) {
                int i = 0;
                int i2 = 0;
                while (i2 < ColorHansManager.this.mPendingIntentList.size()) {
                    String action = (String) ColorHansManager.this.mPendingIntentList.get(i2);
                    ArrayList<BroadcastRecord> broadcastRecords = (ArrayList) ColorHansManager.this.mBroadcastRecordMap.get(action);
                    ArrayList<Object> receivers = (ArrayList) ColorHansManager.this.mReceiverMap.get(action);
                    if (broadcastRecords != null) {
                        if (receivers != null) {
                            int k = i;
                            while (true) {
                                if (k >= broadcastRecords.size()) {
                                    break;
                                }
                                BroadcastRecord broadcastRecord = broadcastRecords.get(k);
                                ArrayList<Object> tempReceivers = new ArrayList<>();
                                for (int j = i; j < receivers.size(); j++) {
                                    Object receiver = receivers.get(j);
                                    if (receiver instanceof BroadcastFilter) {
                                        BroadcastFilter bf = (BroadcastFilter) receiver;
                                        if (bf.packageName.equals(pkgName) && bf.owningUserId == userId) {
                                            tempReceivers.add(bf);
                                        }
                                    } else if (receiver instanceof ResolveInfo) {
                                        ResolveInfo resolveInfo = (ResolveInfo) receiver;
                                        if (broadcastRecord != null && broadcastRecord.ordered && pkgName.equals(resolveInfo.activityInfo.applicationInfo.packageName) && userId == UserHandle.getUserId(resolveInfo.activityInfo.applicationInfo.uid)) {
                                            tempReceivers.add(resolveInfo);
                                        }
                                    }
                                }
                                if (tempReceivers.size() == 0) {
                                    break;
                                }
                                dispatchedMap.put(broadcastRecord, tempReceivers);
                                receivers.removeAll(tempReceivers);
                                k++;
                                i = 0;
                            }
                            if (receivers.size() == 0) {
                                broadcastRecords.remove(action);
                                receivers.remove(action);
                            }
                        }
                    }
                    i2++;
                    i = 0;
                }
            }
            Iterator<BroadcastRecord> iterator = dispatchedMap.keySet().iterator();
            while (iterator.hasNext()) {
                BroadcastRecord key = iterator.next();
                dispatchPendingBroadcast(key, dispatchedMap.get(key));
                iterator.remove();
            }
        }

        private void dispatchPendingBroadcast(BroadcastRecord br, ArrayList<Object> receivers) {
            synchronized (ColorHansManager.this.mAms) {
                if (br != null) {
                    HansLogger hansLogger = ColorHansManager.this.mHansLogger;
                    hansLogger.fullLog("unProxyBroadcast " + br.intent.getAction());
                    Intent intent = br.intent;
                    BroadcastQueue queue = ColorHansManager.this.mAms.broadcastQueueForIntent(intent);
                    BroadcastRecord r = new BroadcastRecord(queue, intent, br.callerApp, br.callerPackage, br.callingPid, br.callingUid, br.callerInstantApp, br.resolvedType, br.requiredPermissions, br.appOp, br.options, receivers, br.resultTo, br.resultCode, br.resultData, br.resultExtras, br.ordered, br.sticky, br.initialSticky, br.userId, br.allowBackgroundActivityStarts, br.timeoutExempt);
                    if (br.ordered) {
                        queue.enqueueOrderedBroadcastLocked(r);
                    } else {
                        queue.enqueueParallelBroadcastLocked(r);
                    }
                    queue.scheduleBroadcastsLocked();
                }
            }
        }

        public void updatePendingIntentList(ArrayList<String> list) {
            synchronized (ColorHansManager.this.mHansLock) {
                ColorHansManager.this.mPendingIntentList.clear();
                ColorHansManager.this.mPendingIntentList.addAll(list);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class HansNativeService {
        private static final String DESCRIPTOR = "oppo.hans.IHansComunication";
        private static final int HANS_BINDER_CODE_OFFSET = 1000;
        private static final int TRANSACTION_HANS_ADD_BINDER_TRANSACTION_UID = 1003;
        private static final int TRANSACTION_HANS_ADD_PACKET_MONITORED_UID = 1001;
        private static final int TRANSACTION_HANS_DELETE_ALL_PACKET_MONITORED_UID = 1002;
        private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
            /* class com.android.server.am.ColorHansManager.HansNativeService.AnonymousClass1 */

            public void binderDied() {
                HansNativeService.this.mRemoteHansService = null;
                ColorHansManager.this.mHansLogger.i("IHansComunication deathRecipient!");
            }
        };
        private IBinder mRemoteHansService = null;

        public HansNativeService() {
        }

        private void connectHansService() {
            int count = 0;
            do {
                count++;
                this.mRemoteHansService = ServiceManager.checkService(DESCRIPTOR);
                IBinder iBinder = this.mRemoteHansService;
                if (iBinder != null) {
                    try {
                        iBinder.linkToDeath(this.mDeathRecipient, 0);
                        return;
                    } catch (RemoteException e) {
                        this.mRemoteHansService = null;
                        ColorHansManager.this.mHansLogger.i("IHansComunication RemoteException!");
                        return;
                    }
                }
            } while (count <= 5);
            ColorHansManager.this.mHansLogger.addSYSInfo("connectHansService fail");
        }

        private boolean ensureConnect() {
            if (this.mRemoteHansService != null) {
                return true;
            }
            connectHansService();
            if (this.mRemoteHansService == null) {
                return false;
            }
            return true;
        }

        public int addPacketMonitoredUid(int uid) {
            if (!ensureConnect()) {
                return -1;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            int result = 0;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(uid);
                this.mRemoteHansService.transact(1001, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
            return result;
        }

        public int deletePacketMonitoredUids() {
            if (!ensureConnect()) {
                return -1;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            int result = 0;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                this.mRemoteHansService.transact(1002, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
            return result;
        }

        public int addBinderTransactionUid(int uid) {
            if (!ensureConnect()) {
                return -1;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            int result = 0;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(uid);
                this.mRemoteHansService.transact(1003, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
            return result;
        }
    }

    public boolean registerHansListener(String callerPkg, IColorHansListener listener) {
        boolean register;
        HansLogger hansLogger = this.mHansLogger;
        if (hansLogger != null) {
            hansLogger.d("registerHansListener " + callerPkg + " " + listener);
        }
        if (!"com.coloros.athena".equals(callerPkg) && !SystemProperties.getBoolean("persist.sys.hans.dump", false)) {
            return false;
        }
        synchronized (this.mHansListener) {
            register = this.mHansListener.register(listener);
        }
        return register;
    }

    public boolean unregisterHansListener(String callerPkg, IColorHansListener listener) {
        boolean unregister;
        HansLogger hansLogger = this.mHansLogger;
        if (hansLogger != null) {
            hansLogger.d("unregisterHansListener " + callerPkg + " " + listener);
        }
        if (!"com.coloros.athena".equals(callerPkg) && !SystemProperties.getBoolean("persist.sys.hans.dump", false)) {
            return false;
        }
        synchronized (this.mHansListener) {
            unregister = this.mHansListener.unregister(listener);
        }
        return unregister;
    }

    private void dispatchColorHansListener(Bundle data, String configName) {
        synchronized (this.mHansListener) {
            int i = this.mHansListener.beginBroadcast();
            while (i > 0) {
                i--;
                IColorHansListener listener = this.mHansListener.getBroadcastItem(i);
                if (listener != null) {
                    try {
                        listener.notifyRecordData(data, configName);
                    } catch (RemoteException e) {
                        this.mHansLogger.d("dispatchColorHansListener err " + e);
                    }
                }
            }
            this.mHansListener.finishBroadcast();
        }
    }

    public boolean setAppFreeze(String callerPkg, Bundle bundle) {
        if (callerPkg == null || bundle == null) {
            return false;
        }
        if (!("com.coloros.athena".equals(callerPkg) || SystemProperties.getBoolean("persist.sys.hans.dump", false))) {
            return false;
        }
        String type = bundle.getString("type");
        List<Bundle> list = bundle.getParcelableArrayList("list");
        if (type == null || list == null) {
            return false;
        }
        if ("add".equals(type)) {
            for (Bundle data : list) {
                int uid = data.getInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID);
                String pkgName = data.getString("pkg");
                int freezeLevel = data.getInt("level");
                if (freezeLevel > 4) {
                    freezeLevel = 4;
                }
                if (freezeLevel < 1) {
                    freezeLevel = 1;
                }
                if (uid > 10000 && !TextUtils.isEmpty(pkgName)) {
                    HansLogger hansLogger = this.mHansLogger;
                    if (hansLogger != null) {
                        hansLogger.d("addAppFreeze " + callerPkg + " " + uid + "/" + pkgName + " : " + freezeLevel);
                    }
                    return ColorHansPackageSelector.getInstance().updateHansPackage(uid, pkgName, freezeLevel, true);
                }
            }
        }
        if (!"rm".equals(type)) {
            return false;
        }
        for (Bundle data2 : list) {
            int uid2 = data2.getInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID);
            String pkgName2 = data2.getString("pkg");
            if (uid2 > 10000 && !TextUtils.isEmpty(pkgName2)) {
                HansLogger hansLogger2 = this.mHansLogger;
                if (hansLogger2 != null) {
                    hansLogger2.d("rmAppFreeze " + callerPkg + " " + uid2 + "/" + pkgName2);
                }
                return ColorHansPackageSelector.getInstance().updateHansPackage(uid2, pkgName2, -1, false);
            }
        }
        return false;
    }

    public String formatDateTime(long time) {
        return new SimpleDateFormat("MM/dd HH:mm:ss.SSS").format(new Date(time));
    }

    private void initLogBuffer() {
        int len = 0;
        String[] strs = SystemProperties.get("dalvik.vm.heapsize", "").split("m");
        if (strs != null && strs.length >= 1) {
            len = Integer.valueOf(strs[0]).intValue() > 128 ? 5000 : ColorFreeformManagerService.FREEFORM_CALLER_UID;
        }
        this.mHansLogger = new HansLogger(len);
    }

    /* access modifiers changed from: package-private */
    public class HansLogger {
        private static final String KEY = "hans_history_key";
        private static final String TAG = "ColorHansManager ";
        private final boolean LOG_DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
        private String key;
        private Key keySpec;
        private int mHead = 0;
        private boolean mIsFull = false;
        private boolean mIsFullLog = false;
        private String[] mLogBuffer = null;
        private int mSize = 0;

        public HansLogger(int size) {
            this.mLogBuffer = new String[size];
            this.mSize = size;
            validateKeyLen(KEY);
        }

        private void put(String s) {
            synchronized (this.mLogBuffer) {
                String[] strArr = this.mLogBuffer;
                int i = this.mHead;
                this.mHead = i + 1;
                strArr[i % this.mSize] = s;
                if (this.mHead == this.mSize) {
                    this.mHead = 0;
                    this.mIsFull = true;
                }
            }
        }

        public void addFZInfo(String reason, String pkgName, int uid) {
            StringBuffer sb = new StringBuffer();
            String freezeTime = ColorHansManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + freezeTime + "] [FRZ] [ " + reason + " ] [ " + uid + " " + pkgName + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void addAllFZInfo(String reason, ArrayList<String> uids, ArrayList<String> important) {
            StringBuffer sb = new StringBuffer();
            String freezeTime = ColorHansManager.this.formatDateTime(System.currentTimeMillis());
            String uidStr = "";
            String result = "";
            for (int i = 0; i < uids.size(); i++) {
                uidStr = uidStr + uids.get(i) + " ";
            }
            String importStr = "";
            for (int j = 0; j < important.size(); j++) {
                importStr = importStr + important.get(j) + " ";
            }
            if (uids.size() != 0 && important.size() != 0) {
                result = "[" + freezeTime + "] [FRA] [ " + reason + " ] [ " + uidStr + " ] [ " + importStr + " ]";
            } else if (uids.size() != 0 && important.size() == 0) {
                result = "[" + freezeTime + "] [FRA] [ " + reason + " ] [ " + uidStr + " ]";
            } else if (uids.size() == 0 && important.size() != 0) {
                result = "[" + freezeTime + "] [FRA] [ " + reason + " ] [ " + importStr + " ]";
            }
            sb.append(result);
            sb.append("\n");
            put(sb.toString());
        }

        public void addUFZInfo(String reason, int uid, String pkgName, long freezeTime) {
            StringBuffer sb = new StringBuffer();
            String unfreezeTime = ColorHansManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + unfreezeTime + "] [UFZ] [ " + reason + " ] [ " + uid + " " + pkgName + " ] [" + ColorHansManager.this.formatDateTime(freezeTime) + "]");
            sb.append("\n");
            put(sb.toString());
        }

        public void addAllUFZInfo(String reason, ArrayList<String> uids) {
            StringBuffer sb = new StringBuffer();
            String unfreezeTime = ColorHansManager.this.formatDateTime(System.currentTimeMillis());
            String uidStr = "";
            for (int i = 0; i < uids.size(); i++) {
                uidStr = uidStr + uids.get(i) + " ";
            }
            sb.append("[" + unfreezeTime + "] [UFA] [ " + reason + " ] [ " + uidStr + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void addSYSInfo(String reason) {
            StringBuffer sb = new StringBuffer();
            String triggerTime = ColorHansManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + triggerTime + "] [SYS] [ " + reason + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void addAsyncBinderInfo(String info) {
            StringBuffer sb = new StringBuffer();
            String time = ColorHansManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + time + "] [ABI] [ " + info + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void dumpLogBuffer(FileDescriptor fd, PrintWriter pw) {
            synchronized (this.mLogBuffer) {
                int i = 0;
                while (true) {
                    if (i < (this.mIsFull ? this.mSize : this.mHead)) {
                        if (!ColorHansManager.this.mUserVersion) {
                            pw.print(this.mLogBuffer[i]);
                        } else {
                            pw.println(hansLogEncrypt(this.mLogBuffer[i]));
                        }
                        i++;
                    } else {
                        pw.println();
                    }
                }
            }
        }

        private void validateKeyLen(String key2) {
            this.key = key2.substring(0, 16);
            byte[] keyBytes = new byte[16];
            byte[] b = key2.getBytes(StandardCharsets.UTF_8);
            int len = b.length;
            if (len > keyBytes.length) {
                len = keyBytes.length;
            }
            System.arraycopy(b, 0, keyBytes, 0, len);
            this.keySpec = new SecretKeySpec(keyBytes, "AES");
        }

        /* access modifiers changed from: package-private */
        public String hansLogEncrypt(String str) {
            try {
                Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
                c.init(1, this.keySpec, new IvParameterSpec(this.key.getBytes()));
                return new String(Base64.getEncoder().encode(c.doFinal(str.getBytes(StandardCharsets.UTF_8))));
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                e.printStackTrace();
                return "error encrypt\n";
            }
        }

        public void setFullLog(boolean enable) {
            this.mIsFullLog = enable;
        }

        public void fullLog(String tag, String msg) {
            if (this.mIsFullLog) {
                Log.d(TAG, tag + ": " + msg);
            }
        }

        public void fullLog(String msg) {
            if (this.mIsFullLog) {
                Log.d(TAG, msg);
            }
        }

        public void i(String tag, String msg) {
            Log.i(TAG, tag + ": " + msg);
        }

        public void i(String msg) {
            Log.i(TAG, msg);
        }

        public void d(String tag, String msg) {
            if (this.LOG_DEBUG || this.mIsFullLog) {
                Log.i(TAG, tag + ": " + msg);
            }
        }

        public void d(String msg) {
            if (this.LOG_DEBUG || this.mIsFullLog) {
                Log.i(TAG, msg);
            }
        }

        public void e(String msg) {
            if (this.LOG_DEBUG || this.mIsFullLog) {
                Log.e(TAG, msg);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class HansRecord {
        long freezeTime = 0;
        long liveTime = 0;
        Map<String, Integer> notFreezeList = new HashMap();
        String pkgName;
        Map<String, Integer> reasonList = new HashMap();

        public HansRecord(String pkgName2) {
            this.pkgName = pkgName2;
        }

        public String getPkgName() {
            return this.pkgName;
        }

        public void setPkgName(String pkgName2) {
            this.pkgName = pkgName2;
        }

        public long getFreezeTime() {
            return this.freezeTime;
        }

        public void setFreezeTime(long freezeTime2) {
            this.freezeTime = freezeTime2;
        }

        public void addFreezeTime(long freezeTime2) {
            this.freezeTime += freezeTime2;
        }

        public long getLiveTime() {
            return this.liveTime;
        }

        public void setLiveTime(long liveTime2) {
            this.liveTime = liveTime2;
        }

        public void addLiveTime(long liveTime2) {
            this.liveTime += liveTime2;
        }

        public Map<String, Integer> getReasonList() {
            return this.reasonList;
        }

        public void addReasonList(String reason) {
            if (this.reasonList.containsKey(reason)) {
                this.reasonList.put(reason, Integer.valueOf(this.reasonList.get(reason).intValue() + 1));
            } else {
                this.reasonList.put(reason, 1);
            }
        }

        public Map<String, Integer> getNotFreezeList() {
            return this.notFreezeList;
        }

        public void addNotFreezeList(String reason) {
            if (this.notFreezeList.containsKey(reason)) {
                this.notFreezeList.put(reason, Integer.valueOf(this.notFreezeList.get(reason).intValue() + 1));
            } else {
                this.notFreezeList.put(reason, 1);
            }
        }
    }

    public void notifyUnFreezeReason(int uid, String pkgName, String reason, String scene, String extra) {
        if (this.mCommonConfig.isStatisticsEnable()) {
            StringBuilder reasonStr = new StringBuilder(reason);
            synchronized (this.mHansLock) {
                ColorHansPackageSelector.HansPackage ps = (ColorHansPackageSelector.HansPackage) this.hansLcdOnScene.mManagedMap.get(uid);
                if (ps != null) {
                    if (!isImportantCaseForStatistics(ps)) {
                        reasonStr.append('#');
                        reasonStr.append(scene);
                        reasonStr.append('#');
                        reasonStr.append(extra);
                        HansRecord hansRecord = this.mHansRecordMap.get(uid);
                        if (hansRecord != null) {
                            hansRecord.addReasonList(reasonStr.toString());
                        } else {
                            HansRecord hansRecord2 = new HansRecord(pkgName);
                            hansRecord2.addReasonList(reasonStr.toString());
                            this.mHansRecordMap.put(uid, hansRecord2);
                        }
                    }
                }
            }
        }
    }

    public void notifyUnFreezeReason(int uid, String pkgName, String reason, String scene) {
        if (this.mCommonConfig.isStatisticsEnable() && this.mUnfreezeReason.contains(reason)) {
            StringBuilder reasonStr = new StringBuilder(reason);
            synchronized (this.mHansLock) {
                ColorHansPackageSelector.HansPackage ps = (ColorHansPackageSelector.HansPackage) this.hansLcdOnScene.mManagedMap.get(uid);
                if (ps != null) {
                    if (!isImportantCaseForStatistics(ps)) {
                        reasonStr.append('#');
                        reasonStr.append(scene);
                        reasonStr.append('#');
                        reasonStr.append(" ");
                        HansRecord hansRecord = this.mHansRecordMap.get(uid);
                        if (hansRecord != null) {
                            hansRecord.addReasonList(reasonStr.toString());
                        } else {
                            HansRecord hansRecord2 = new HansRecord(pkgName);
                            hansRecord2.addReasonList(reasonStr.toString());
                            this.mHansRecordMap.put(uid, hansRecord2);
                        }
                    }
                }
            }
        }
    }

    public void notifyNotFreezeReason(int uid, String pkgName, String reason) {
        if (this.mCommonConfig.isStatisticsEnable() && reason != "prev" && reason != "top") {
            synchronized (this.mHansLock) {
                ColorHansPackageSelector.HansPackage ps = (ColorHansPackageSelector.HansPackage) this.hansLcdOnScene.mManagedMap.get(uid);
                if (ps != null) {
                    if (!isImportantCaseForStatistics(ps)) {
                        HansRecord hansRecord = this.mHansRecordMap.get(uid);
                        if (hansRecord != null) {
                            hansRecord.addNotFreezeList(reason);
                        } else {
                            HansRecord hansRecord2 = new HansRecord(pkgName);
                            hansRecord2.addNotFreezeList(reason);
                            this.mHansRecordMap.put(uid, hansRecord2);
                        }
                    }
                }
            }
        }
    }

    public void notifyFreezeTime(int uid, String pkgName, long freezeTime) {
        if (this.mCommonConfig.isStatisticsEnable()) {
            synchronized (this.mHansLock) {
                ColorHansPackageSelector.HansPackage ps = (ColorHansPackageSelector.HansPackage) this.hansLcdOnScene.mManagedMap.get(uid);
                if (ps != null) {
                    if (!isImportantCaseForStatistics(ps)) {
                        HansRecord hansRecord = this.mHansRecordMap.get(uid);
                        if (hansRecord != null) {
                            hansRecord.addFreezeTime(freezeTime);
                        } else {
                            HansRecord hansRecord2 = new HansRecord(pkgName);
                            hansRecord2.addFreezeTime(freezeTime);
                            this.mHansRecordMap.put(uid, hansRecord2);
                        }
                    }
                }
            }
        }
    }

    public void notifyLiveTime(int uid, String pkgName, long liveTime) {
        if (this.mCommonConfig.isStatisticsEnable()) {
            synchronized (this.mHansLock) {
                ColorHansPackageSelector.HansPackage ps = (ColorHansPackageSelector.HansPackage) this.hansLcdOnScene.mManagedMap.get(uid);
                if (ps != null) {
                    if (!isImportantCaseForStatistics(ps)) {
                        HansRecord hansRecord = this.mHansRecordMap.get(uid);
                        if (hansRecord != null) {
                            hansRecord.addLiveTime(liveTime);
                        } else {
                            HansRecord hansRecord2 = new HansRecord(pkgName);
                            hansRecord2.addLiveTime(liveTime);
                            this.mHansRecordMap.put(uid, hansRecord2);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDateChanged() {
        if (this.mCommonConfig.isStatisticsEnable()) {
            synchronized (this.mHansLock) {
                long now = SystemClock.elapsedRealtime();
                for (int i = 0; i < this.hansLcdOnScene.mManagedMap.size(); i++) {
                    ColorHansPackageSelector.HansPackage ps = (ColorHansPackageSelector.HansPackage) this.hansLcdOnScene.mManagedMap.valueAt(i);
                    if (ps != null) {
                        if (ps.getFreezed()) {
                            notifyFreezeTime(ps.getUid(), ps.getPkgName(), now - ps.getFreezeElapsedTime());
                            ps.setFreezeElapsedTime(now);
                        }
                        if (ps.getEnterBgTime() > 0) {
                            notifyLiveTime(ps.getUid(), ps.getPkgName(), now - ps.getEnterBgTime());
                            ps.setEnterBgTime(now);
                        }
                    }
                }
            }
            uploadHansRecordData();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateProcessBgTime(boolean isPowerConnected) {
        synchronized (this.mHansLock) {
            SparseArray<ColorHansPackageSelector.HansPackage> mManagedAppStateMap = this.hansLcdOnScene.mManagedMap;
            for (int i = 0; i < mManagedAppStateMap.size(); i++) {
                ColorHansPackageSelector.HansPackage ps = mManagedAppStateMap.valueAt(i);
                if (ps != null) {
                    long now = SystemClock.elapsedRealtime();
                    if (isPowerConnected) {
                        if (ps.getEnterBgTime() > 0) {
                            notifyLiveTime(ps.getUid(), ps.getPkgName(), now - ps.getEnterBgTime());
                            ps.setEnterBgTime(0);
                        }
                    } else if (ps.getEnterBgTime() != 0) {
                        if (ps.getUid() != this.mCurResumedUid) {
                            ps.setEnterBgTime(now);
                        } else {
                            ps.setEnterBgTime(0);
                        }
                    }
                }
            }
        }
    }

    private void uploadHansRecordData() {
        ArrayList<Bundle> recordList = new ArrayList<>();
        try {
            Bundle recordData = new Bundle();
            synchronized (this.mHansRecordMap) {
                for (int i = 0; i < this.mHansRecordMap.size(); i++) {
                    Bundle bundle = new Bundle();
                    HansRecord hansRecord = this.mHansRecordMap.valueAt(i);
                    bundle.putString(KEY_RECORD_PKGNAME, hansRecord.getPkgName());
                    bundle.putLong(KEY_RECORD_LIVETIME, hansRecord.getLiveTime());
                    bundle.putLong(KEY_RECORD_FREEZETIME, hansRecord.getFreezeTime());
                    bundle.putStringArrayList(KEY_RECORD_REASONLIST, convertMapToString(hansRecord.getReasonList()));
                    bundle.putStringArrayList(KEY_RECORD_NOTFREEZELIST, convertMapToString(hansRecord.getNotFreezeList()));
                    recordList.add(bundle);
                }
                this.mHansRecordMap.clear();
            }
            recordData.putParcelableArrayList(KEY_RECORD, recordList);
            dispatchColorHansListener(recordData, KEY_RECORD);
        } catch (Error | Exception e) {
        }
    }

    private ArrayList<String> convertMapToString(Map<String, Integer> map) {
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            list.add(entry.getKey() + "#" + String.valueOf(entry.getValue()));
        }
        return list;
    }

    public boolean isImportantCaseForStatistics(ColorHansPackageSelector.HansPackage hansPackage) {
        return ColorHansImportance.getInstance().isHansImportantCase(hansPackage, ColorHansImportance.HANS_IMPORTANT_SCENE_FOR_STATISTICS, new DynamicImportantAppList());
    }

    public void updatePolicyConfig() {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig != null) {
            commonConfig.setHansEnable(ColorHansPackageSelector.getInstance().isHansFreezeEnable());
            this.mCommonConfig.setStatisticsEnable(ColorHansPackageSelector.getInstance().isFreezeStatisticsEnable());
            hansDeamonSwitch(this.mCommonConfig.hasHansEnable());
        }
    }

    public boolean updateHansConfig(Bundle data) {
        if (this.mAms != null && ColorHansPackageSelector.getInstance() != null) {
            return ColorHansPackageSelector.getInstance().updateHansConfig(data);
        }
        Log.e("ColorHansManager", "ColorHansManager is not init!");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isDeviceIdleList(int uid) {
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            return activityManagerService.isOnDeviceIdleWhitelistLocked(uid, false);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public CommonConfig getCommonConfig() {
        return this.mCommonConfig;
    }

    /* access modifiers changed from: protected */
    public HansLogger getHansLogger() {
        return this.mHansLogger;
    }

    /* access modifiers changed from: protected */
    public HashSet<Integer> getHansRunningList() {
        return this.mHansRunningList;
    }

    /* access modifiers changed from: protected */
    public Object getHansLock() {
        return this.mHansLock;
    }

    /* access modifiers changed from: protected */
    public HansMainHandler getMainHandler() {
        return this.mMainHandler;
    }

    /* access modifiers changed from: protected */
    public Handler getNativeHandler() {
        return this.mNativeHandler;
    }

    /* access modifiers changed from: protected */
    public HansNativeService getHansNativeService() {
        return this.mNativeService;
    }

    /* access modifiers changed from: protected */
    public ActivityManagerService getActivityManagerService() {
        return this.mAms;
    }

    /* access modifiers changed from: protected */
    public HansBroadcastProxy getHansBroadcastProxy() {
        return this.mHansBroadcastProxy;
    }

    /* access modifiers changed from: protected */
    public StateMachineHandler getStateMachineHandler() {
        return this.mStateMachineHandler;
    }

    /* access modifiers changed from: protected */
    public ArrayList<String> getStateToMList() {
        return this.mStateToMList;
    }

    /* access modifiers changed from: protected */
    public ArrayList<String> getStateToRList() {
        return this.mStateToRList;
    }

    /* access modifiers changed from: protected */
    public ArrayList<String> getStateToDList() {
        return this.mStateToDList;
    }

    /* access modifiers changed from: protected */
    public ArrayList<String> getFreqUnFreezeList() {
        return this.mFreqUnFreeze;
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mContext;
    }

    public boolean isOnDeviceIdleWhitelist(int uid) {
        boolean result = false;
        try {
            if (this.mAms != null) {
                result = Arrays.binarySearch(this.mAms.mDeviceIdleWhitelist, UserHandle.getAppId(uid)) > 0;
                if (result) {
                    HansLogger hansLogger = this.mHansLogger;
                    hansLogger.d("deviceidle uid " + uid);
                }
            }
        } catch (Exception e) {
        }
        return result;
    }
}
