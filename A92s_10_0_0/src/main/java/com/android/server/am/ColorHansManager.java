package com.android.server.am;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IUidObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.util.SparseArray;
import com.android.server.LocalServices;
import com.android.server.OppoDynamicLogManager;
import com.android.server.OppoNetworkManagementInternal;
import com.android.server.am.ColorCommonListManager;
import com.android.server.am.ColorHansPackageSelector;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.policy.OppoPhoneWindowManager;
import com.android.server.power.OppoPowerManagerInternal;
import com.android.server.wm.ColorFreeformManagerService;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import com.color.app.ColorAppEnterInfo;
import com.color.app.ColorAppExitInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.ColorAppSwitchManager;
import com.color.app.IColorHansController;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ColorHansManager implements IColorHansManager {
    private static final String DEV_FREEZE_PATH = "/dev/freezer/frozen/cgroup.procs";
    private static final String DEV_UNFREEZE_PATH = "/dev/freezer/thaw/cgroup.procs";
    private static final String HANS_IMPORTANT_CASE_AUDIO_FOCUS = "audiofocus";
    private static final String HANS_IMPORTANT_CASE_BT = "bt";
    private static final String HANS_IMPORTANT_CASE_FLOAT_WINDOW = "floatwindow";
    private static final String HANS_IMPORTANT_CASE_GPS = "gps";
    private static final String HANS_IMPORTANT_CASE_TRAFFIC = "traffic";
    private static final int MAX_RECORD_CACHE = 100;
    private static final String RECORD_TYPE_LIVE_TIME = "liveTime";
    private static final String RECORD_TYPE_NOT_FREEZE = "notFreeze";
    private static final String RECORD_TYPE_UNFREEZE = "unfreeze";
    private static final int SIGNAL_FREEZE = 1;
    private static final int SIGNAL_UNFREEZE = 2;
    private static final int TYPE_ADD_UID = 1;
    private static final int TYPE_CONFIG_CHECKING_UID = 3;
    private static final int TYPE_DELETE_UIDS = 2;
    private static volatile ColorHansManager sInstance = null;
    private int HANS_FREQ_UNFREEZE = 3146764;
    private final int HANS_FZ_REASON_DEFAULT = 0;
    private final int HANS_FZ_REASON_LCD_OFF = -268435454;
    private final int HANS_FZ_REASON_LCD_ON = -268435455;
    private int HANS_STATE_TRANSFER_TO_M_CASES = 20711356;
    private int HANS_STATE_TRANSFER_TO_R_CASES = 12820544;
    private final int HANS_UFZ_REASON_ACTIVITY = 1;
    private final int HANS_UFZ_REASON_ALARM = 512;
    private final int HANS_UFZ_REASON_ASYNC_BINDER = 16;
    private final int HANS_UFZ_REASON_BIND_SERVICE = 1048576;
    private final int HANS_UFZ_REASON_BROADCAST = 8;
    private final int HANS_UFZ_REASON_BUMP_SERVICE = OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE;
    private final int HANS_UFZ_REASON_CHARGING = 2048;
    private final int HANS_UFZ_REASON_DEFAULT = 0;
    private final int HANS_UFZ_REASON_DEFAULT_APP = 16777217;
    private final int HANS_UFZ_REASON_DISABLE = 16384;
    private final int HANS_UFZ_REASON_DUMPSATE = 8192;
    private final int HANS_UFZ_REASON_EXCEPTION = 65536;
    private final int HANS_UFZ_REASON_EXECUTING_COMPONENT = 16777224;
    private final int HANS_UFZ_REASON_GMS = 16777216;
    private final int HANS_UFZ_REASON_JOB = 262144;
    private final int HANS_UFZ_REASON_MEDIAKEY = 32768;
    private final int HANS_UFZ_REASON_NAVIGATION_APP = 16777220;
    private final int HANS_UFZ_REASON_PACKET = 256;
    private final int HANS_UFZ_REASON_PROVIDER = 4;
    private final int HANS_UFZ_REASON_REMOVE_APP = 16777218;
    private final int HANS_UFZ_REASON_SIGNAL = 64;
    private final int HANS_UFZ_REASON_SPECIAL_CASE = 4194304;
    private final int HANS_UFZ_REASON_START_SERVICE = 2097152;
    private final int HANS_UFZ_REASON_SYNC = 524288;
    private final int HANS_UFZ_REASON_SYNC_BINDER = 32;
    private final int HANS_UFZ_REASON_TOP_ACTIVITY = 2;
    private final int HANS_UFZ_REASON_TRANSACTION_BINDER = 128;
    private final int HANS_UFZ_REASON_UID_ACTIVE = 131072;
    private final int HANS_UFZ_REASON_WAKELOCK = 8388608;
    private final int HANS_UFZ_REASON_WATCHDOG = 4096;
    private ColorCommonListManager.AppChangedListener listener = new ColorCommonListManager.AppChangedListener() {
        /* class com.android.server.am.ColorHansManager.AnonymousClass3 */

        @Override // com.android.server.am.ColorCommonListManager.AppChangedListener
        public void onChanged(int uid, String pkgName, String cfgName, boolean isAdd) {
            if (isAdd) {
                Bundle data = new Bundle();
                data.putInt("uid", uid);
                data.putString("pkg", pkgName);
                data.putString("cfg", cfgName);
                ColorHansManager.this.mMainHandler.sendMessage(7, data, 0);
            }
        }
    };
    private ActivityManagerService mAms = null;
    private ColorAppSwitchManager.OnAppSwitchObserver mAppSwitchObserver = new ColorAppSwitchManager.OnAppSwitchObserver() {
        /* class com.android.server.am.ColorHansManager.AnonymousClass1 */

        public void onAppEnter(ColorAppEnterInfo info) {
            if (info != null && info.extension != null) {
                ColorHansManager.this.mTrigger.setLastResumePackage(info.extension.getInt("uid", -1), info.targetName);
            }
        }

        public void onAppExit(ColorAppExitInfo info) {
            if (info != null) {
                ColorHansManager.this.updateSpecialWindow(info.targetName, info.resumingWindowMode);
            }
        }

        public void onActivityEnter(ColorAppEnterInfo info) {
        }

        public void onActivityExit(ColorAppExitInfo info) {
        }
    };
    private ArrayMap<String, ArrayList> mBroadcastRecordMap = new ArrayMap<>();
    private CheckComponentHandler mCheckComponentHandler = null;
    private IColorActivityManagerServiceEx mColorAmsEx = null;
    /* access modifiers changed from: private */
    public CommonConfig mCommonConfig = null;
    /* access modifiers changed from: private */
    public ColorCommonListManager mCommonListManager = null;
    /* access modifiers changed from: private */
    public Context mContext = null;
    private ArrayList<String> mFreezeTimeList = new ArrayList<>();
    private boolean mGmsFreezeSwitch = true;
    private ContentObserver mGmsObserver = new ContentObserver(this.mMainHandler) {
        /* class com.android.server.am.ColorHansManager.AnonymousClass2 */

        public void onChange(boolean selfChange) {
            ColorHansManager.this.setGmsRestrict();
        }
    };
    private GnssStatus.Callback mGnssStatusCallback = new GnssStatus.Callback() {
        /* class com.android.server.am.ColorHansManager.AnonymousClass4 */

        public void onStarted() {
            ColorHansManager.this.mMainHandler.sendMessage(8, 0);
        }

        public void onStopped() {
        }

        public void onFirstFix(int ttffMillis) {
        }

        public void onSatelliteStatusChanged(GnssStatus status) {
        }
    };
    private IColorHansController mHansController;
    /* access modifiers changed from: private */
    public long mHansFreezeInterval = 5000;
    /* access modifiers changed from: private */
    public Object mHansLock = new Object();
    /* access modifiers changed from: private */
    public HansLogger mHansLogger = null;
    /* access modifiers changed from: private */
    public boolean mHansManageEnable = true;
    /* access modifiers changed from: private */
    public ColorHansPackageSelector mHansPackageSelector = null;
    private ColorHansRestriction mHansRestriction = null;
    /* access modifiers changed from: private */
    public HashSet<Integer> mHansRunningList = new HashSet<>();
    /* access modifiers changed from: private */
    public boolean mInitAmsEnable = true;
    private boolean mIsZoomWindowMode = false;
    private ArrayList<String> mLiveTimeList = new ArrayList<>();
    private LocationManager mLocationManager = null;
    /* access modifiers changed from: private */
    public HansMainHandler mMainHandler = null;
    /* access modifiers changed from: private */
    public SparseArray<ColorHansPackageSelector.PackageState> mManagedAppStateMap = new SparseArray<>();
    private INetworkManagementService mNMs = null;
    private HansNativeService mNativeService = null;
    private ArrayList<String> mNotFreezeList = new ArrayList<>();
    private OppoPowerManagerInternal mOppoLocalPowerManager = null;
    private OppoNetworkManagementInternal mOppoNetworkManagementInternal = null;
    private ArrayList<String> mPendingIntentList = new ArrayList<>();
    private ArrayMap<String, ArrayList> mReceiverMap = new ArrayMap<>();
    private HansRegisterBrdAction mRegisterAction = null;
    private String mResumePkg = "";
    /* access modifiers changed from: private */
    public int mResumeUid = -1;
    private ArrayList<String> mSpecialWindowList = new ArrayList<>();
    /* access modifiers changed from: private */
    public HansStateMachine mStateMachine = null;
    private boolean mStatisticsSwitch = false;
    /* access modifiers changed from: private */
    public HansTrigger mTrigger = null;
    /* access modifiers changed from: private */
    public boolean mUserVersion = "user".equals(SystemProperties.get("ro.build.type"));

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

    public boolean hansActivityIfNeeded(int callingUid, String callingPackage, int uid, String pkgName, String cpnName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if ((commonConfig != null && !commonConfig.hasHansEnable()) || !isFreezed(uid)) {
            return true;
        }
        ColorHansRestriction colorHansRestriction = this.mHansRestriction;
        if (colorHansRestriction != null && !colorHansRestriction.isAllowedActivity(callingUid, callingPackage, uid, pkgName, cpnName)) {
            return false;
        }
        hansUnFreeze(uid, 1);
        return true;
    }

    public boolean hansTopActivityIfNeeded(int uid, String pkgName) {
        this.mResumeUid = uid;
        this.mResumePkg = pkgName;
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig != null && !commonConfig.hasHansEnable()) {
            return true;
        }
        if (!isFreezed(uid)) {
            resetStateMachineState(uid);
            return true;
        }
        hansUnFreeze(uid, 2);
        return true;
    }

    public boolean hansServiceIfNeeded(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, boolean isBind) {
        CommonConfig commonConfig = this.mCommonConfig;
        if ((commonConfig != null && !commonConfig.hasHansEnable()) || !isFreezed(uid)) {
            return true;
        }
        ColorHansRestriction colorHansRestriction = this.mHansRestriction;
        if (colorHansRestriction != null && !colorHansRestriction.isAllowedService(callingUid, callingPackage, uid, pkgName, cpnName, isBind)) {
            return false;
        }
        if (isBind) {
            hansUnFreeze(uid, 1048576);
            return true;
        }
        hansUnFreeze(uid, 2097152);
        return true;
    }

    public boolean hansProviderIfNeeded(int callingUid, String callingPackage, int uid, String pkgName, String cpnName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if ((commonConfig != null && !commonConfig.hasHansEnable()) || !isFreezed(uid)) {
            return true;
        }
        ColorHansRestriction colorHansRestriction = this.mHansRestriction;
        if (colorHansRestriction != null && !colorHansRestriction.isAllowedProvider(callingUid, callingPackage, uid, pkgName, cpnName)) {
            return false;
        }
        hansUnFreeze(uid, 4);
        return true;
    }

    public boolean hansBroadcastIfNeeded(BroadcastRecord r, Object o) {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig != null && !commonConfig.hasHansEnable()) {
            return true;
        }
        int uid = -1;
        String pkgName = "";
        if (o instanceof BroadcastFilter) {
            uid = ((BroadcastFilter) o).owningUid;
            pkgName = ((BroadcastFilter) o).packageName;
        } else if (o instanceof ResolveInfo) {
            uid = ((ResolveInfo) o).activityInfo.applicationInfo.uid;
            pkgName = ((ResolveInfo) o).activityInfo.packageName;
        }
        if (!isFreezed(uid)) {
            return true;
        }
        int callingUid = r.callingUid;
        String callingPackage = r.callerPackage;
        String action = r.intent.getAction();
        boolean order = r.ordered;
        ColorHansRestriction colorHansRestriction = this.mHansRestriction;
        if (colorHansRestriction != null) {
            if (!colorHansRestriction.isAllowedBroadcast(callingUid, callingPackage, uid, pkgName, action, order)) {
                return false;
            }
        }
        if (isPendingIntent(action, uid, pkgName)) {
            cacheBroadcast(action, r);
            cacheReceivers(action, o);
            return false;
        }
        hansUnFreeze(uid, 8);
        return true;
    }

    public boolean hansSyncIfNeeded(int uid, String pkgName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if ((commonConfig != null && !commonConfig.hasHansEnable()) || !isFreezed(uid)) {
            return true;
        }
        ColorHansRestriction colorHansRestriction = this.mHansRestriction;
        if (colorHansRestriction != null && !colorHansRestriction.isAllowedSync(uid, pkgName)) {
            return false;
        }
        hansUnFreeze(uid, 524288);
        return true;
    }

    public boolean hansJobIfNeeded(int uid, String pkgName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if ((commonConfig != null && !commonConfig.hasHansEnable()) || !isFreezed(uid)) {
            return true;
        }
        ColorHansRestriction colorHansRestriction = this.mHansRestriction;
        if (colorHansRestriction != null && !colorHansRestriction.isAllowedJob(uid, pkgName)) {
            return false;
        }
        hansUnFreeze(uid, 262144);
        return true;
    }

    public boolean hansAlarmIfNeeded(String action, int uid, String pkgName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if ((commonConfig != null && !commonConfig.hasHansEnable()) || !isFreezed(uid)) {
            return true;
        }
        ColorHansRestriction colorHansRestriction = this.mHansRestriction;
        if (colorHansRestriction != null && !colorHansRestriction.isAllowedAlarm(action, uid, pkgName)) {
            return false;
        }
        hansUnFreeze(uid, 512);
        return true;
    }

    public boolean hansMediaEventIfNeeded(int uid, String pkgName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if ((commonConfig == null || commonConfig.hasHansEnable()) && isFreezed(uid)) {
            return hansUnFreeze(uid, 32768);
        }
        return true;
    }

    public boolean hansPackageTimeout(int uid, String pkgName) {
        return true;
    }

    public void hansBumpService(int uid, String pkgName) {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig != null && commonConfig.hasHansEnable() && isFreezed(uid)) {
            hansUnFreeze(uid, OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE);
        }
    }

    public void unfreezeForKernel(int type, int callerPid, int uid, String rpcName, int code) {
        Object obj;
        this.mHansLogger.fullLog("receiver from hans: (" + type + "," + callerPid + "," + uid + "), rpcName: " + rpcName + ", code: " + code);
        if ("disablehans".equals(rpcName)) {
            hansDeamonSwitch(false);
            return;
        }
        int reason = 0;
        boolean flag = true;
        if (type == 0) {
            this.mHansLogger.addAsyncBinderInfo("" + callerPid + "|" + uid + "|" + rpcName + "|" + code);
            if ("free_buffer_full".equals(rpcName)) {
                flag = true;
                reason = 16;
            } else {
                Object obj2 = this.mHansLock;
                synchronized (obj2) {
                    try {
                        ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
                        if (ps == null || !ps.getFreezed()) {
                            obj = obj2;
                        } else {
                            obj = obj2;
                            try {
                                if (!this.mHansRestriction.isAllowedBinder(callerPid, uid, ps.getPkgName(), rpcName, code, true)) {
                                    flag = false;
                                }
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        }
                        reason = 16;
                    } catch (Throwable th2) {
                        th = th2;
                        obj = obj2;
                        throw th;
                    }
                }
            }
        } else if (type == 1) {
            reason = 32;
        } else if (type == 2) {
            reason = 128;
        } else if (type == 3) {
            reason = 64;
        } else if (type == 4) {
            reason = 256;
        }
        if (flag) {
            hansUnFreeze(uid, reason);
        }
    }

    public void hansDeamonSwitch(boolean enable) {
        if (enable) {
            SystemProperties.set("sys.hans.enable", "true");
            return;
        }
        SystemProperties.set("sys.hans.enable", "false");
        this.mCommonConfig.setHansEnable(false);
        this.mTrigger.cancelAlarm();
        this.mStateMachine.exitStateMachine();
        hansUnFreeze(16384);
        HansLogger hansLogger = this.mHansLogger;
        if (hansLogger != null) {
            hansLogger.addSYSInfo("disablehans");
        }
    }

    private void resetStateMachineState(int uid) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
            if (ps != null && ps.getFreezeType() == -268435455 && ps.getCurState() == 2) {
                this.mStateMachine.stopStateMachine(ps.getCurState(), ps.getStrUid());
                ps.setCurState(0);
            }
        }
    }

    public void bootCompleted() {
        initOther();
        ColorAppSwitchConfig config = new ColorAppSwitchConfig();
        config.addAppConfig(2, (List) null);
        ColorAppSwitchManager.getInstance().registerAppSwitchObserver(this.mContext, this.mAppSwitchObserver, config);
    }

    private ArrayList<Integer> getFrozenPidsFromPath() {
        ArrayList<Integer> pidList = new ArrayList<>();
        BufferedReader br = null;
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(DEV_FREEZE_PATH));
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
            if (br != null) {
                br.close();
            }
        } catch (Throwable th) {
            if (br != null) {
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
                if (fos != null) {
                    fos.close();
                }
            } catch (Throwable th) {
                if (fos != null) {
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
        writePidsToDevFile(getFrozenPidsFromPath(), DEV_UNFREEZE_PATH);
    }

    private void initCore() {
        HandlerThread thread = new HandlerThread("HansManagerHandler", -2);
        thread.start();
        this.mMainHandler = new HansMainHandler(thread.getLooper());
        HandlerThread checkThread = new HandlerThread("HansCheckComponentHandler", -2);
        checkThread.start();
        this.mCheckComponentHandler = new CheckComponentHandler(checkThread.getLooper());
        this.mHansPackageSelector = new ColorHansPackageSelector(this, this.mContext);
        this.mHansRestriction = new ColorHansRestriction(this, this.mContext);
        this.mCommonListManager = ColorCommonListManager.getInstance();
    }

    private void initOther() {
        this.mHansManageEnable = SystemProperties.getBoolean("persist.vendor.enable.hans", false);
        initLogBuffer();
        this.mCommonConfig = new CommonConfig();
        this.mTrigger = new HansTrigger();
        this.mStateMachine = new HansStateMachine();
        this.mRegisterAction = new HansRegisterBrdAction();
        setFirewallChainEnable(4, true);
        registerGmsRestrictObserve();
        registerAppChangedListener();
        registerGnssStatusCallback();
        this.mNativeService = new HansNativeService();
        bootThawingApp();
    }

    private void bootThawingApp() {
        writePidsToDevFile(getFrozenPidsFromPath(), DEV_UNFREEZE_PATH);
    }

    /* access modifiers changed from: protected */
    public ColorHansPackageSelector getHansPkgSelector() {
        return this.mHansPackageSelector;
    }

    /* access modifiers changed from: protected */
    public ColorHansRestriction getHansRestriction() {
        return this.mHansRestriction;
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
    public boolean isFreezed(int uid) {
        boolean frozen = false;
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.PackageState packageState = this.mManagedAppStateMap.get(uid);
            if (packageState != null) {
                frozen = packageState.getFreezed();
            }
        }
        return frozen;
    }

    /* access modifiers changed from: protected */
    public boolean updateFreezing(int uid, boolean isFrozen) {
        boolean success = false;
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.PackageState packageState = this.mManagedAppStateMap.get(uid);
            if (packageState != null) {
                packageState.setFreezed(isFrozen);
                success = true;
            }
        }
        return success;
    }

    /* access modifiers changed from: protected */
    public void addPackageState(int uid, ColorHansPackageSelector.PackageState state) {
        if (this.mCommonConfig != null) {
            synchronized (this.mHansLock) {
                this.mManagedAppStateMap.put(uid, state);
                if (this.mHansRunningList.contains(Integer.valueOf(uid))) {
                    if (this.mCommonConfig.isCharging() || uid == this.mTrigger.getLastResumeUid()) {
                        state.setEnterBgTime(-1);
                    } else {
                        state.setEnterBgTime(SystemClock.elapsedRealtime());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removePackageState(int uid) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
            if (ps != null) {
                hansUnFreeze(uid, 16777218);
                if (ps.getEnterBgTime() > 0) {
                    notifyProcessBgExit(uid, ps.getPkgName(), SystemClock.elapsedRealtime() - ps.getEnterBgTime());
                }
                this.mManagedAppStateMap.remove(uid);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addGmsPkgState(int uid, ColorHansPackageSelector.PackageState state) {
        synchronized (this.mHansLock) {
            if (this.mManagedAppStateMap.get(uid) == null) {
                addPackageState(uid, state);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeGmsPkgState(int uid) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
            if (ps != null) {
                hansUnFreeze(uid, 16777216);
                if (ps.getEnterBgTime() > 0) {
                    notifyProcessBgExit(uid, ps.getPkgName(), SystemClock.elapsedRealtime() - ps.getEnterBgTime());
                }
                this.mManagedAppStateMap.remove(uid);
            }
        }
    }

    public String covertReasonToStr(int reason) {
        switch (reason) {
            case -268435455:
                return "LcdOn";
            case -268435454:
                return "LcdOff";
            case 1:
                return "Activity";
            case 2:
                return "TopActivity";
            case 4:
                return "Provider";
            case ColorStartingWindowRUSHelper.SPLASH_SNAPSHOT_WHITE_THIRD_PARTY_APP /*{ENCODED_INT: 8}*/:
                return "Broadcast";
            case 16:
                return "AsyncBinder";
            case 32:
                return "SyncBinder";
            case 64:
                return "Signal";
            case 128:
                return "TransBinder";
            case 256:
                return "Packet";
            case 512:
                return "Alarm";
            case OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE /*{ENCODED_INT: 1024}*/:
                return "BumpService";
            case 2048:
                return "Charging";
            case 4096:
                return "Watchdog";
            case 8192:
                return "Dumpstate";
            case 16384:
                return "Disable";
            case 32768:
                return "MediaKey";
            case 65536:
                return "Exception";
            case 131072:
                return "UidActive";
            case 262144:
                return "Job";
            case 524288:
                return "Sync";
            case 1048576:
                return "BindService";
            case 2097152:
                return "StartService";
            case 4194304:
                return "SpecialCase";
            case 8388608:
                return "Wakelock";
            case 16777216:
                return "Gms";
            case 16777217:
                return "defaultApp";
            case 16777218:
                return "removeApp";
            case 16777220:
                return "navigationApp";
            case 16777224:
                return "executingComponent";
            default:
                return "deafult";
        }
    }

    private void unfreezeForFrozenPids(ColorHansPackageSelector.PackageState ps) {
        ArrayList<Integer> runningPids = ps.getFrozenPidList();
        if (runningPids != null) {
            for (int j = runningPids.size() - 1; j >= 0; j--) {
                writePidToDevFile(runningPids.get(j).intValue(), DEV_UNFREEZE_PATH);
                runningPids.remove(j);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00d2, code lost:
        if (r1 == false) goto L_0x00d7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00d4, code lost:
        closeSocketsForHansFirewallChain();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00d7, code lost:
        return r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00d9, code lost:
        return false;
     */
    public boolean hansFreeze(int uid, int reason) {
        boolean isNeedCloseSocket = false;
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
            if (ps != null) {
                if (!ps.getFreezed()) {
                    boolean isSuccessful = sendHansSignal(ps, 1);
                    if (isSuccessful) {
                        HansLogger hansLogger = this.mHansLogger;
                        hansLogger.i("freeze uid: " + ps.getUid() + " package: " + ps.getPkgName() + " reason: " + covertReasonToStr(reason));
                        this.mHansLogger.addFZInfo(covertReasonToStr(reason), ps.getPkgName(), ps.getUid());
                        sendCheckExecutingComponentMsg(ps.getUid());
                        ps.setFreezed(true);
                        ps.setFreezeTime(System.currentTimeMillis());
                        ps.setFreezeElapsedTime(SystemClock.elapsedRealtime());
                        ps.setFreezeType(reason);
                        if (reason == -268435454) {
                            ps.setCurState(3);
                        }
                        boolean isMonitoredUid = false;
                        if (isPkgMonitoredUid(uid, ps.getPkgName())) {
                            isMonitoredUid = true;
                        } else {
                            isNeedCloseSocket = true;
                            updateHansUidFirewall(uid, false);
                        }
                        sendCheckingMessageForHans(uid, isMonitoredUid, true);
                    } else {
                        HansLogger hansLogger2 = this.mHansLogger;
                        hansLogger2.i("freeze failed uid: " + ps.getUid() + " package: " + ps.getPkgName() + " reason: " + covertReasonToStr(reason));
                        unfreezeForFrozenPids(ps);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void hansFreeze(int reason) {
        boolean isNeedCloseSocket = false;
        ArrayList<String> freezedUids = new ArrayList<>();
        ArrayList<String> importantUids = new ArrayList<>();
        List<String> navigationList = this.mCommonListManager.getNavigationList();
        List<Integer> audioList = this.mCommonListManager.getAudioFocus();
        synchronized (this.mHansLock) {
            Iterator<Integer> it = this.mHansRunningList.iterator();
            while (it.hasNext()) {
                int uid = it.next().intValue();
                ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
                if (ps != null) {
                    if (!ps.getFreezed()) {
                        if (isHansImportantCase(ps, navigationList, audioList)) {
                            HansLogger hansLogger = this.mHansLogger;
                            hansLogger.d("isHansImportantCase uid: " + uid + " pkg: " + ps.getPkgName() + " reason: " + ps.getImportantReason());
                            notifyNotFreeze(uid, ps.getPkgName(), ps.getImportantReason());
                            StringBuilder sb = new StringBuilder();
                            sb.append(ps.getImportantReason());
                            sb.append("|");
                            sb.append(ps.getUid());
                            importantUids.add(sb.toString());
                        } else {
                            if (reason == -268435454) {
                                ps.setCurState(0);
                            }
                            if (sendHansSignal(ps, 1)) {
                                HansLogger hansLogger2 = this.mHansLogger;
                                hansLogger2.i("freeze uid: " + ps.getUid() + " package: " + ps.getPkgName() + " reason: " + covertReasonToStr(reason));
                                freezedUids.add(String.valueOf(ps.getUid()));
                                sendCheckExecutingComponentMsg(ps.getUid());
                                ps.setFreezed(true);
                                ps.setFreezeTime(System.currentTimeMillis());
                                ps.setFreezeElapsedTime(SystemClock.elapsedRealtime());
                                ps.setFreezeType(reason);
                                if (reason == -268435454) {
                                    ps.setCurState(3);
                                }
                                boolean isMonitoredUid = false;
                                if (isPkgMonitoredUid(ps.getUid(), ps.getPkgName())) {
                                    isMonitoredUid = true;
                                } else {
                                    isNeedCloseSocket = true;
                                    updateHansUidFirewall(uid, false);
                                }
                                sendCheckingMessageForHans(ps.getUid(), isMonitoredUid, true);
                            } else {
                                HansLogger hansLogger3 = this.mHansLogger;
                                hansLogger3.i("freeze failed uid: " + ps.getUid() + " package: " + ps.getPkgName() + " reason: " + covertReasonToStr(reason));
                                unfreezeForFrozenPids(ps);
                            }
                        }
                    }
                }
            }
        }
        if (isNeedCloseSocket) {
            closeSocketsForHansFirewallChain();
        }
        if (freezedUids.size() != 0 || importantUids.size() != 0) {
            this.mHansLogger.addAllFZInfo(covertReasonToStr(reason), freezedUids, importantUids);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00e7, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00e9, code lost:
        return false;
     */
    public boolean hansUnFreeze(int uid, int reason) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.PackageState packageState = this.mManagedAppStateMap.get(uid);
            if (packageState != null) {
                if (packageState.getFreezed()) {
                    boolean isSuccessful = sendHansSignal(packageState, 2);
                    if (!isSuccessful) {
                        unfreezeForFrozenPids(packageState);
                        isSuccessful = true;
                    }
                    if (isSuccessful) {
                        HansLogger hansLogger = this.mHansLogger;
                        hansLogger.i("unfreeze uid: " + uid + " package: " + packageState.getPkgName() + " reason: " + covertReasonToStr(reason));
                        this.mHansLogger.addUFZInfo(covertReasonToStr(reason), uid, packageState.getPkgName(), packageState.getFreezeTime());
                        packageState.setFreezed(false);
                        packageState.setUnFreezeTime(System.currentTimeMillis());
                        packageState.setUnFreezeReason(covertReasonToStr(reason));
                        sendCheckingMessageForHans(uid, false, false);
                        notifyUnfreeze(uid, packageState.getPkgName(), SystemClock.elapsedRealtime() - packageState.getFreezeElapsedTime(), packageState.getUnFreezeReason());
                        updateHansUidFirewall(uid, true);
                        handleUnfreezeForStateMachine(packageState, reason);
                        sendPendingIntentMsgForHans(packageState.getPkgName(), UserHandle.getUserId(packageState.getUid()));
                        if (this.mCommonConfig.isChinaRegion()) {
                            if (this.mCommonConfig.isScreenOn() || (this.HANS_FREQ_UNFREEZE & reason) == 0) {
                                packageState.clearUnFreezeQueueTime();
                            } else if (packageState.recordUnFreezeQueueTime(SystemClock.elapsedRealtime())) {
                                packageState.clearUnFreezeQueueTime();
                                Bundle data = new Bundle();
                                data.putInt("uid", uid);
                                data.putString("pkg", packageState.getPkgName());
                                this.mMainHandler.sendMessage(30, data, 1000);
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void hansUnFreeze(int reason) {
        ArrayList<String> uids = new ArrayList<>();
        synchronized (this.mHansLock) {
            for (int i = 0; i < this.mManagedAppStateMap.size(); i++) {
                ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.valueAt(i);
                if (ps != null && ps.getFreezed()) {
                    if (reason != 4194304 || isHansImportantCaseForSpecialApps(ps)) {
                        boolean isSuccessful = sendHansSignal(ps, 2);
                        if (!isSuccessful) {
                            unfreezeForFrozenPids(ps);
                            isSuccessful = true;
                        }
                        if (isSuccessful) {
                            this.mHansLogger.i("unfreeze uid: " + ps.getUid() + " package: " + ps.getPkgName() + " reason: " + covertReasonToStr(reason));
                            uids.add(String.valueOf(ps.getUid()));
                            ps.setFreezed(false);
                            ps.setUnFreezeTime(System.currentTimeMillis());
                            ps.setUnFreezeReason(covertReasonToStr(reason));
                            sendCheckingMessageForHans(ps.getUid(), false, false);
                            notifyUnfreeze(ps.getUid(), ps.getPkgName(), SystemClock.elapsedRealtime() - ps.getFreezeElapsedTime(), ps.getUnFreezeReason());
                            updateHansUidFirewall(ps.getUid(), true);
                            handleUnfreezeForStateMachine(ps, reason);
                            sendPendingIntentMsgForHans(ps.getPkgName(), UserHandle.getUserId(ps.getUid()));
                        }
                    }
                }
            }
        }
        if (uids.size() != 0) {
            this.mHansLogger.addAllUFZInfo(covertReasonToStr(reason), uids);
        }
    }

    private void handleUnfreezeForStateMachine(ColorHansPackageSelector.PackageState ps, int reason) {
        if (this.mCommonConfig.isScreenOn()) {
            int preState = ps.getCurState();
            if ((this.HANS_STATE_TRANSFER_TO_R_CASES & reason) != 0 || isHansImportantCaseForSpecialApps(ps)) {
                ps.setCurState(1);
                this.mStateMachine.enterStateMachine(ps, reason);
            } else if ((this.HANS_STATE_TRANSFER_TO_M_CASES & reason) != 0) {
                ps.setCurState(2);
                this.mStateMachine.stepHansState(preState, ps.getCurState(), reason, ps, true, null, null);
            }
        }
    }

    private boolean isHansImportantCaseForSpecialApps(ColorHansPackageSelector.PackageState ps) {
        int uid = ps.getUid();
        ps.getPkgName();
        if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_LIVE_WALLPAPER, uid).isEmpty()) {
            ps.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_LIVE_WALLPAPER);
            return true;
        } else if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_INPUT, uid).isEmpty()) {
            ps.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_INPUT);
            return true;
        } else if (this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_WIDGET, uid).isEmpty()) {
            return false;
        } else {
            ps.setImportantReason(ColorCommonListManager.CONFIG_WIDGET);
            return true;
        }
    }

    public boolean isHansImportantCaseForFrozenState(int uid) {
        List<String> navigationList = this.mCommonListManager.getNavigationList();
        List<Integer> audioList = this.mCommonListManager.getAudioFocus();
        synchronized (this.mHansLock) {
            try {
                ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
                if (ps == null) {
                    try {
                        return false;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } else {
                    String pkg = ps.getPkgName();
                    if (audioList != null && audioList.contains(Integer.valueOf(uid))) {
                        ps.setImportantReason(HANS_IMPORTANT_CASE_AUDIO_FOCUS);
                        HansLogger hansLogger = this.mHansLogger;
                        hansLogger.d("isHansImportantCaseForFrozenState uid: " + uid + " pkg: " + pkg + " reason: " + ps.getImportantReason());
                        return true;
                    } else if (navigationList == null || !navigationList.contains(pkg)) {
                        if (this.mCommonListManager.isProtectedByTraffic(ps.getLastUsedTime(), uid, ps.getFgService(), this.mCommonConfig.isScreenOn(), this.mCommonConfig.getScreenOffTime() + this.mHansFreezeInterval, this.mTrigger.getLastResumeUid())) {
                            ps.setImportantReason(HANS_IMPORTANT_CASE_TRAFFIC);
                            HansLogger hansLogger2 = this.mHansLogger;
                            hansLogger2.d("isHansImportantCaseForFrozenState uid: " + uid + " pkg: " + pkg + " reason: " + ps.getImportantReason());
                            return true;
                        } else if (!this.mCommonListManager.getBluetoothList().contains(Integer.valueOf(uid))) {
                            return false;
                        } else {
                            ps.setImportantReason(HANS_IMPORTANT_CASE_BT);
                            HansLogger hansLogger3 = this.mHansLogger;
                            hansLogger3.d("isHansImportantCaseForFrozenState uid: " + uid + " pkg: " + pkg + " reason: " + ps.getImportantReason());
                            return true;
                        }
                    } else {
                        ps.setImportantReason(HANS_IMPORTANT_CASE_GPS);
                        HansLogger hansLogger4 = this.mHansLogger;
                        hansLogger4.d("isHansImportantCaseForFrozenState uid: " + uid + " pkg: " + pkg + " reason: " + ps.getImportantReason());
                        return true;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    public boolean isHansImportantCase(ColorHansPackageSelector.PackageState pkgState, List<String> navigationList, List<Integer> audioList) {
        int uid = pkgState.getUid();
        String pkg = pkgState.getPkgName();
        if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_LAUNCHER, uid).isEmpty()) {
            pkgState.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_LAUNCHER);
            return true;
        } else if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_SMS, uid).isEmpty()) {
            pkgState.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_SMS);
            return true;
        } else if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_DIALER, uid).isEmpty()) {
            pkgState.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_DIALER);
            return true;
        } else if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_VPN, uid).isEmpty()) {
            pkgState.setImportantReason(ColorCommonListManager.CONFIG_VPN);
            return true;
        } else if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_SCREEN_RECORDER, uid).isEmpty()) {
            pkgState.setImportantReason(ColorCommonListManager.CONFIG_SCREEN_RECORDER);
            return true;
        } else if (this.mCommonListManager.isProtectedByTraffic(pkgState.getLastUsedTime(), uid, pkgState.getFgService(), this.mCommonConfig.isScreenOn(), this.mCommonConfig.getScreenOffTime() + this.mHansFreezeInterval, this.mTrigger.getLastResumeUid())) {
            pkgState.setImportantReason(HANS_IMPORTANT_CASE_TRAFFIC);
            return true;
        } else if (this.mCommonListManager.isFloatWindow(uid)) {
            pkgState.setImportantReason(HANS_IMPORTANT_CASE_FLOAT_WINDOW);
            return true;
        } else if (audioList != null && audioList.contains(Integer.valueOf(uid))) {
            pkgState.setImportantReason(HANS_IMPORTANT_CASE_AUDIO_FOCUS);
            return true;
        } else if (navigationList != null && navigationList.contains(pkg)) {
            pkgState.setImportantReason(HANS_IMPORTANT_CASE_GPS);
            return true;
        } else if (this.mCommonListManager.getBluetoothList().contains(Integer.valueOf(uid))) {
            pkgState.setImportantReason(HANS_IMPORTANT_CASE_BT);
            return true;
        } else if (!this.mCommonListManager.getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_INPUT, uid).isEmpty()) {
            pkgState.setImportantReason(ColorCommonListManager.CONFIG_DEFAULT_INPUT);
            return true;
        } else if (!this.mCommonConfig.isScreenOn() || !isHansImportantCaseForSpecialApps(pkgState)) {
            return false;
        } else {
            return true;
        }
    }

    public void updatePendingIntentList(ArrayList<String> list) {
        synchronized (this.mHansLock) {
            this.mPendingIntentList.clear();
            this.mPendingIntentList.addAll(list);
        }
    }

    public boolean isPendingIntent(String action, int uid, String pkgName) {
        boolean result;
        if (!this.mCommonConfig.mChinaModel) {
            return false;
        }
        synchronized (this.mHansLock) {
            result = this.mPendingIntentList.contains(action);
        }
        if (!result || !this.mHansRestriction.isAllowStart(pkgName, uid)) {
            return result;
        }
        return false;
    }

    public void cacheBroadcast(String action, BroadcastRecord br) {
        synchronized (this.mHansLock) {
            if (this.mBroadcastRecordMap != null) {
                HansLogger hansLogger = this.mHansLogger;
                hansLogger.fullLog("cacheBroadcast--action:" + action + ",br:" + br);
                ArrayList<BroadcastRecord> brList = this.mBroadcastRecordMap.get(action);
                if (brList == null) {
                    brList = new ArrayList<>();
                    this.mBroadcastRecordMap.put(action, brList);
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
                this.mBroadcastRecordMap.put(action, brList);
            }
        }
    }

    public void cacheReceivers(String action, Object obj) {
        synchronized (this.mHansLock) {
            if (this.mReceiverMap != null) {
                HansLogger hansLogger = this.mHansLogger;
                hansLogger.fullLog("cacheReceivers--action:" + action + ",receiver:" + obj);
                ArrayList<Object> receiverList = this.mReceiverMap.get(action);
                if (receiverList == null) {
                    receiverList = new ArrayList<>();
                    this.mReceiverMap.put(action, receiverList);
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
                this.mReceiverMap.put(action, receiverList);
            }
        }
    }

    public void sendPendingIntent(String pkgName, int userId) {
        ArrayMap<BroadcastRecord, ArrayList> dispatchedMap = new ArrayMap<>();
        synchronized (this.mHansLock) {
            HansLogger hansLogger = this.mHansLogger;
            hansLogger.fullLog("sendPendingIntent pkgName:" + pkgName + ",userId:" + userId);
            int i = 0;
            int i2 = 0;
            while (i2 < this.mPendingIntentList.size()) {
                String action = this.mPendingIntentList.get(i2);
                ArrayList<BroadcastRecord> broadcastRecords = this.mBroadcastRecordMap.get(action);
                ArrayList<Object> receivers = this.mReceiverMap.get(action);
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
                                    if (bf != null) {
                                        if (bf.packageName.equals(pkgName) && bf.owningUserId == userId) {
                                            tempReceivers.add(bf);
                                        }
                                    }
                                } else if (receiver instanceof ResolveInfo) {
                                    ResolveInfo resolveInfo = (ResolveInfo) receiver;
                                    if (resolveInfo != null) {
                                        if (broadcastRecord != null && broadcastRecord.ordered && pkgName.equals(resolveInfo.activityInfo.applicationInfo.packageName) && userId == UserHandle.getUserId(resolveInfo.activityInfo.applicationInfo.uid)) {
                                            tempReceivers.add(resolveInfo);
                                        }
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
        synchronized (this.mAms) {
            if (br != null) {
                Intent intent = br.intent;
                BroadcastQueue queue = this.mAms.broadcastQueueForIntent(intent);
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

    private boolean sendHansSignal(ColorHansPackageSelector.PackageState ps, int signal) {
        if (signal == 1) {
            return writeDevFile(ps, DEV_FREEZE_PATH);
        }
        if (signal == 2) {
            return writeDevFile(ps, DEV_UNFREEZE_PATH);
        }
        this.mHansLogger.i("no matched signal");
        return false;
    }

    private boolean writeDevFile(ColorHansPackageSelector.PackageState ps, String devPath) {
        ArrayList<Integer> runningPids;
        boolean ret = false;
        File file = new File(devPath);
        FileOutputStream fos = null;
        ArrayList<Integer> frozenList = new ArrayList<>();
        if (!file.exists() || !file.canWrite()) {
            this.mHansLogger.d("file.exists() == false or file.canWrite() == false");
            return false;
        }
        if (DEV_FREEZE_PATH.equals(devPath)) {
            runningPids = getRunningPidsByUid(ps.getUid());
        } else {
            runningPids = ps.getFrozenPidList();
        }
        try {
            FileOutputStream fos2 = new FileOutputStream(file);
            for (int i = runningPids.size() - 1; i >= 0; i--) {
                Integer pid = runningPids.get(i);
                fos2.write(String.valueOf(pid.intValue()).getBytes("UTF-8"));
                fos2.flush();
                ret = true;
                if (DEV_FREEZE_PATH.equals(devPath) && !frozenList.contains(pid)) {
                    frozenList.add(pid);
                } else if (DEV_UNFREEZE_PATH.equals(devPath)) {
                    ps.getFrozenPidList().remove(pid);
                }
            }
            if (DEV_FREEZE_PATH.equals(devPath)) {
                ps.setFrozenPidList(frozenList);
            }
            try {
                fos2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            ret = false;
            ret = false;
            e2.printStackTrace();
            if (DEV_FREEZE_PATH.equals(devPath)) {
                ps.setFrozenPidList(frozenList);
            }
            if (fos != null) {
                fos.close();
            }
        } catch (Throwable th) {
            if (DEV_FREEZE_PATH.equals(devPath)) {
                ps.setFrozenPidList(frozenList);
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
        return ret;
    }

    private boolean isExecutingComponent(ArrayList<Integer> runningPids) {
        boolean ret = false;
        for (int i = 0; i < runningPids.size(); i++) {
            synchronized (this.mAms.mPidsSelfLocked) {
                ProcessRecord pr = getProcessRecordFromPidLocked(runningPids.get(i).intValue());
                if (pr != null && (!pr.executingServices.isEmpty() || !pr.curReceivers.isEmpty() || this.mAms.checkAppInLaunchingProvidersLocked(pr))) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private ProcessRecord getProcessRecordFromPidLocked(int pid) {
        return this.mAms.mPidsSelfLocked.get(pid);
    }

    private ArrayList<Integer> getRunningPidsByUid(int uid) {
        String[] files;
        ArrayList<Integer> pidList = new ArrayList<>();
        String acctPath = "/acct/uid_" + uid;
        File file = new File(acctPath);
        if (file.isDirectory() && (files = file.list()) != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].contains("pid")) {
                    BufferedReader br = null;
                    try {
                        BufferedReader br2 = new BufferedReader(new FileReader(acctPath + "/" + files[i] + "/cgroup.procs"));
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
                        if (br != null) {
                            br.close();
                        }
                    } catch (Throwable th) {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        throw th;
                    }
                }
            }
        }
        return pidList;
    }

    private boolean isPkgMonitoredUid(int uid, String pkgName) {
        return this.mHansRestriction.isAllowStart(pkgName, uid) || this.mHansRestriction.isGameApp(pkgName, uid) || isDeviceIdleList(uid) || this.mHansPackageSelector.isInNetPacketWhiteList(pkgName);
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

    private void updateHansUidFirewall(int uid, boolean allow) {
        long token = Binder.clearCallingIdentity();
        if (this.mNMs == null) {
            this.mNMs = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        }
        INetworkManagementService iNetworkManagementService = this.mNMs;
        if (iNetworkManagementService != null) {
            if (allow) {
                try {
                    iNetworkManagementService.setFirewallUidRule(4, uid, 1);
                } catch (Exception e) {
                    HansLogger hansLogger = this.mHansLogger;
                    hansLogger.d("Error occured while updateHansUidFirewall: " + e);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } else {
                iNetworkManagementService.setFirewallUidRule(4, uid, 2);
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    private void closeSocketsForHansFirewallChain() {
        if (this.mOppoNetworkManagementInternal == null) {
            this.mOppoNetworkManagementInternal = (OppoNetworkManagementInternal) LocalServices.getService(OppoNetworkManagementInternal.class);
        }
        OppoNetworkManagementInternal oppoNetworkManagementInternal = this.mOppoNetworkManagementInternal;
        if (oppoNetworkManagementInternal != null) {
            oppoNetworkManagementInternal.closeSocketsForHans(4, "hans");
        }
    }

    public void hansTalkWithNative(int uid, int type) {
        if (type == 1) {
            this.mNativeService.addPacketMonitoredUid(uid);
        } else if (type == 2) {
            this.mNativeService.deletePacketMonitoredUids();
        } else if (type != 3) {
            this.mHansLogger.i("no matchded type.");
        } else {
            this.mNativeService.addBinderTransactionUid(uid);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0097, code lost:
        r2 = r7.mOppoLocalPowerManager.restoreJobs(r8, true ^ r7.mCommonConfig.isScreenOn(), r7.mCommonConfig.isCharging(), "hansFreeze");
        r3 = r7.mHansLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ae, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        r1 = r7.mManagedAppStateMap.get(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00b7, code lost:
        if (r1 == null) goto L_0x00be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00b9, code lost:
        r1.setJobWakelock(r2 + 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00be, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        return;
     */
    public void handleWakeLockForHans(int uid, boolean disable) {
        if (this.mOppoLocalPowerManager == null) {
            this.mOppoLocalPowerManager = (OppoPowerManagerInternal) LocalServices.getService(OppoPowerManagerInternal.class);
        }
        OppoPowerManagerInternal oppoPowerManagerInternal = this.mOppoLocalPowerManager;
        if (oppoPowerManagerInternal != null) {
            int num = oppoPowerManagerInternal.setWakeLockStateForHans(uid, disable);
            if (num == 1) {
                HansLogger hansLogger = this.mHansLogger;
                StringBuilder sb = new StringBuilder();
                sb.append(disable ? "disable" : "enable");
                sb.append(" app (");
                sb.append(uid);
                sb.append(") wakelock for hans.");
                hansLogger.d(sb.toString());
            } else if (num == 2 || num == 3) {
                hansUnFreeze(uid, 8388608);
            }
            if (disable && num == 2 && this.mOppoLocalPowerManager.pendingJobs(uid, !this.mCommonConfig.isScreenOn(), this.mCommonConfig.isCharging(), "hansFreeze") == 2) {
                synchronized (this.mHansLock) {
                    ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
                    if (ps != null) {
                        ps.setJobWakelock(1);
                    }
                }
                sendCheckJobWakelock(uid, 500);
            }
            if (!disable) {
                synchronized (this.mHansLock) {
                    ColorHansPackageSelector.PackageState ps2 = this.mManagedAppStateMap.get(uid);
                    if (ps2 != null && ps2.getJobWakelock() != 1) {
                    }
                }
            }
        }
    }

    public String formatDateTime(long time) {
        return new SimpleDateFormat("MM/dd HH:mm:ss.SSS").format(new Date(time));
    }

    private void sendPendingIntentMsgForHans(String pkgName, int userId) {
        Message msg = Message.obtain();
        msg.what = 5;
        Bundle bundle = new Bundle();
        bundle.putString("pkgName", pkgName);
        bundle.putInt("userId", userId);
        msg.setData(bundle);
        this.mMainHandler.sendMessage(msg);
    }

    public void sendCheckingMessageForHans(int uid, boolean isMonitoredUid, boolean isFreezeAction) {
        Message msg = Message.obtain();
        msg.what = 20;
        Bundle bundle = new Bundle();
        bundle.putInt("uid", uid);
        bundle.putBoolean("isMonitoredUid", isMonitoredUid);
        bundle.putBoolean("isFreezeAction", isFreezeAction);
        msg.setData(bundle);
        this.mMainHandler.sendMessage(msg);
    }

    public void sendFZActionDelayMsg(int uid) {
        Message msg = Message.obtain();
        msg.what = 21;
        Bundle bundle = new Bundle();
        bundle.putInt("uid", uid);
        msg.setData(bundle);
        this.mMainHandler.sendMessageDelayed(msg, 2000);
    }

    /* access modifiers changed from: private */
    public void sendRepeatFZActionMsg() {
        this.mMainHandler.removeMessages(22);
        Message msg = Message.obtain();
        msg.what = 22;
        this.mMainHandler.sendMessageDelayed(msg, (long) this.mHansPackageSelector.getPolicyConfig().mScreenOffPeriodCheckTime);
    }

    /* access modifiers changed from: private */
    public void handleFreezeDelayAction(int uid) {
        hansTalkWithNative(uid, 3);
    }

    private void checkPidReuse(int uid, ArrayList<Integer> frozenList) {
        ArrayList<Integer> originalList = getRunningPidsByUid(uid);
        for (int i = 0; i < frozenList.size(); i++) {
            boolean flag = false;
            for (int j = 0; j < originalList.size(); j++) {
                if (originalList.get(j).intValue() == frozenList.get(i).intValue()) {
                    flag = true;
                }
            }
            if (!flag) {
                writePidToDevFile(frozenList.get(i).intValue(), DEV_UNFREEZE_PATH);
            }
        }
    }

    private void writePidToDevFile(int pid, String devPath) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(devPath));
            fos.write(String.valueOf(pid).getBytes("UTF-8"));
            fos.flush();
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (fos != null) {
                fos.close();
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void handleFreezeCheckingAction(int uid, boolean isMonitoredUid, boolean isFreezeAction) {
        if (isFreezeAction) {
            if (isMonitoredUid) {
                hansTalkWithNative(uid, 1);
            }
            handleWakeLockForHans(uid, true);
            return;
        }
        handleWakeLockForHans(uid, false);
    }

    public void dumpHansInfo(FileDescriptor fd, PrintWriter pw) {
        HansLogger hansLogger;
        StringBuffer sb = new StringBuffer();
        sb.append("Hans Info(dumpsys activity hans)\n");
        sb.append("\n");
        sb.append("Hans Manage apps -- size ");
        sb.append(this.mManagedAppStateMap.size() + "\n");
        for (int i = 0; i < this.mManagedAppStateMap.size(); i++) {
            ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.valueAt(i);
            sb.append("pkg: " + String.format("%-50s", ps.getPkgName()));
            sb.append(" uid: " + String.format("%8d", Integer.valueOf(ps.getUid())));
            if (ps.getFreezed()) {
                sb.append(" --(F) ");
            } else if (this.mHansRunningList.contains(Integer.valueOf(ps.getUid()))) {
                sb.append(String.format(" --(R) --(S|%d) ", Integer.valueOf(ps.getCurState())));
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
        pw.println("Hans StateMachine Message");
        this.mStateMachine.dumpHandler(pw);
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
                        updateHansUidFirewall(uid, false);
                        closeSocketsForHansFirewallChain();
                    } else if ("1".equals(args[3])) {
                        updateHansUidFirewall(uid, true);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            if (OppoDynamicLogManager.INVOKE_DUMP_NAME.equals(args[1])) {
                try {
                    if ("common".equals(args[2])) {
                        this.mCommonListManager.dumpCommonMap();
                    } else if ("common_log".equals(args[2])) {
                        this.mCommonListManager.openDynamicLog();
                    } else if ("managerList".equals(args[2])) {
                        synchronized (this.mHansLock) {
                            for (int i = 0; i < this.mManagedAppStateMap.size(); i++) {
                                this.mHansLogger.d("" + this.mManagedAppStateMap.valueAt(i));
                            }
                        }
                    } else if ("config".equals(args[2])) {
                        this.mHansLogger.d("isHansEnable " + this.mCommonConfig.hasHansEnable());
                        this.mHansLogger.d("isCharging " + this.mCommonConfig.isCharging());
                        this.mHansLogger.d("isChinaRegion " + this.mCommonConfig.isChinaRegion());
                        this.mHansLogger.d("isGmsRestrict " + this.mCommonConfig.isRestrictGms());
                        this.mHansLogger.d("isDisableNet " + this.mCommonConfig.isDisableNetWork());
                    } else if ("hanslog".equals(args[2])) {
                        this.mHansLogger.setFullLog(true);
                    }
                } catch (Exception e2) {
                }
            }
            if ("addpkg".equals(args[1])) {
                try {
                    String pkgName = args[2];
                    int uid2 = Integer.parseInt(args[3]);
                    ColorHansPackageSelector colorHansPackageSelector = this.mHansPackageSelector;
                    Objects.requireNonNull(colorHansPackageSelector);
                    this.mManagedAppStateMap.put(uid2, new ColorHansPackageSelector.PackageState(pkgName, uid2));
                } catch (Exception e3) {
                }
            }
            if ("freeze".equals(args[1])) {
                try {
                    hansFreeze(Integer.parseInt(args[2]), 0);
                } catch (Exception e4) {
                }
            }
            if (RECORD_TYPE_UNFREEZE.equals(args[1])) {
                try {
                    hansUnFreeze(Integer.parseInt(args[2]), 0);
                } catch (Exception e5) {
                }
            }
            if ("freezeall".equals(args[1])) {
                hansFreeze(0);
            }
            if ("unfreezeall".equals(args[1])) {
                hansUnFreeze(0);
            }
            if ("checkfreeze".equals(args[1])) {
                try {
                    int uid3 = Integer.parseInt(args[2]);
                    synchronized (this.mHansLock) {
                        ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid3);
                        if (ps != null) {
                            pw.println("" + ps.getFreezed());
                        }
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
            if (!"disable".equals(args[1])) {
                return;
            }
            if (Integer.parseInt(args[2]) == 1) {
                hansDeamonSwitch(true);
            } else {
                hansDeamonSwitch(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendCheckExecutingComponentMsg(int uid) {
        Message msg = Message.obtain();
        msg.what = 1;
        Bundle bundle = new Bundle();
        bundle.putInt("uid", uid);
        msg.setData(bundle);
        this.mCheckComponentHandler.sendMessage(msg);
    }

    /* access modifiers changed from: package-private */
    public void sendCheckJobWakelock(int uid, long delay) {
        Message msg = Message.obtain();
        msg.what = 2;
        msg.arg1 = uid;
        this.mCheckComponentHandler.sendMessageDelayed(msg, delay);
    }

    class CheckComponentHandler extends Handler {
        static final int MSG_CHECK_EXECUTING_COMPONENT = 1;
        static final int MSG_CHECK_JOB_WAKELOCK = 2;

        public CheckComponentHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == 1) {
                Bundle bundle = msg.getData();
                if (bundle != null) {
                    ColorHansManager.this.handleExecutingComponent(bundle.getInt("uid"));
                }
            } else if (what == 2) {
                ColorHansManager.this.hansFreeze(msg.arg1, 8388608);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleExecutingComponent(int uid) {
        synchronized (this.mAms) {
            synchronized (this.mHansLock) {
                ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
                if (ps != null) {
                    if (isExecutingComponent(ps.getFrozenPidList())) {
                        hansUnFreeze(uid, 16777224);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleKillAbnormalApp(Message msg) {
        if (msg != null) {
            Bundle data = msg.getData();
            int uid = data.getInt("uid", 0);
            String pkgName = data.getString("pkg", "");
            if (!TextUtils.isEmpty(pkgName) && !this.mHansRestriction.isAllowStart(pkgName, uid) && !this.mHansPackageSelector.isGmsPackage(uid, pkgName) && !this.mHansPackageSelector.isAbnormalKillWhiteList(pkgName)) {
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
    public class HansMainHandler extends Handler {
        static final int HANS_MSG_FREEZE_CHECKING_ACTION = 20;
        static final int HANS_MSG_FREEZE_CHECKING_DELAY_ACTION = 21;
        public static final int HANS_MSG_FREEZE_REPEAT_ACTION = 22;
        public static final int HANS_MSG_KILL_ABNORMAL_APP = 30;
        static final int HANS_MSG_UNFREEZE_DEFAULT_APP = 7;
        static final int HANS_MSG_UNFREEZE_GMS = 6;
        static final int HANS_MSG_UNFREEZE_NAVIGATION_APP = 8;
        static final int MSG_DATE_CHANGED = 9;
        static final int MSG_PENDING_INTENT = 5;
        static final int MSG_POWER_CONNECTED = 3;
        static final int MSG_POWER_DISCONNECTED = 4;
        static final int MSG_SCREEN_OFF = 1;
        static final int MSG_SCREEN_ON = 2;

        public HansMainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            handlePackageState(msg);
        }

        public void sendMessage(int what, int uid, long delay) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = uid;
            ColorHansManager.this.mMainHandler.sendMessageDelayed(msg, delay);
        }

        public void sendMessage(int what, Bundle data, long delay) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.setData(data);
            ColorHansManager.this.mMainHandler.sendMessageDelayed(msg, delay);
        }

        public void sendMessage(int what, long delay) {
            Message msg = Message.obtain();
            msg.what = what;
            ColorHansManager.this.mMainHandler.sendMessageDelayed(msg, delay);
        }

        private void handlePackageState(Message msg) {
            int what = msg.what;
            if (what != 30) {
                switch (what) {
                    case 1:
                        if (ColorHansManager.this.mCommonConfig.hasHansEnable() && !ColorHansManager.this.mCommonConfig.isCharging()) {
                            ColorHansManager.this.mStateMachine.exitStateMachine();
                            ColorHansManager.this.mTrigger.setAlarm(ColorHansManager.this.mHansFreezeInterval);
                        }
                        ColorHansManager.this.mHansLogger.addSYSInfo("ScrOff");
                        return;
                    case 2:
                        ColorHansManager.this.mTrigger.cancelAlarm();
                        ColorHansManager.this.mMainHandler.removeMessages(22);
                        ColorHansManager.this.hansUnFreeze(4194304);
                        if (ColorHansManager.this.mCommonConfig.hasHansEnable() && !ColorHansManager.this.mCommonConfig.isCharging()) {
                            ColorHansManager.this.mStateMachine.enterStateMachine(-268435455);
                        }
                        ColorHansManager.this.mHansLogger.addSYSInfo("ScrOn");
                        return;
                    case 3:
                        ColorHansManager.this.mTrigger.cancelAlarm();
                        ColorHansManager.this.mMainHandler.removeMessages(22);
                        ColorHansManager.this.hansUnFreeze(2048);
                        ColorHansManager.this.mStateMachine.exitStateMachine();
                        ColorHansManager.this.updateProcessBgTime(true);
                        ColorHansManager.this.mHansLogger.addSYSInfo("plug");
                        return;
                    case 4:
                        if (ColorHansManager.this.mCommonConfig.hasHansEnable() && ColorHansManager.this.mCommonConfig.isScreenOn()) {
                            ColorHansManager.this.mStateMachine.enterStateMachine(-268435455);
                        }
                        ColorHansManager.this.updateProcessBgTime(false);
                        ColorHansManager.this.mHansLogger.addSYSInfo("unplug");
                        return;
                    case 5:
                        Bundle bundle = msg.getData();
                        if (bundle != null) {
                            ColorHansManager.this.sendPendingIntent(bundle.getString("pkgName"), bundle.getInt("userId"));
                            return;
                        }
                        return;
                    case 6:
                        ColorHansManager.this.handleUpdateGmsRestrict();
                        return;
                    case 7:
                        ColorHansManager.this.handleAppChanged(msg);
                        return;
                    case 8:
                        ColorHansManager.this.handleUfzNavigationApp();
                        return;
                    case 9:
                        ColorHansManager.this.handleDateChanged();
                        return;
                    default:
                        switch (what) {
                            case HANS_MSG_FREEZE_CHECKING_ACTION /*{ENCODED_INT: 20}*/:
                                Bundle bundle2 = msg.getData();
                                if (bundle2 != null) {
                                    int uid = bundle2.getInt("uid");
                                    boolean isMonitoredUid = bundle2.getBoolean("isMonitoredUid");
                                    boolean isFreezeAction = bundle2.getBoolean("isFreezeAction");
                                    ColorHansManager.this.handleFreezeCheckingAction(uid, isMonitoredUid, isFreezeAction);
                                    if (isFreezeAction) {
                                        ColorHansManager.this.sendFZActionDelayMsg(uid);
                                        return;
                                    }
                                    return;
                                }
                                return;
                            case HANS_MSG_FREEZE_CHECKING_DELAY_ACTION /*{ENCODED_INT: 21}*/:
                                Bundle delayBundle = msg.getData();
                                if (delayBundle != null) {
                                    ColorHansManager.this.handleFreezeDelayAction(delayBundle.getInt("uid"));
                                    return;
                                }
                                return;
                            case HANS_MSG_FREEZE_REPEAT_ACTION /*{ENCODED_INT: 22}*/:
                                if (!ColorHansManager.this.mCommonConfig.isScreenOn()) {
                                    ColorHansManager.this.hansFreeze(-268435454);
                                    ColorHansManager.this.sendRepeatFZActionMsg();
                                    return;
                                }
                                return;
                            default:
                                return;
                        }
                }
            } else {
                ColorHansManager.this.handleKillAbnormalApp(msg);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class HansTrigger {
        private final AlarmManager.OnAlarmListener mHansListener = new AlarmManager.OnAlarmListener() {
            /* class com.android.server.am.ColorHansManager.HansTrigger.AnonymousClass1 */

            public void onAlarm() {
                if (!ColorHansManager.this.mCommonConfig.mCharging && !ColorHansManager.this.mCommonConfig.isScreenOn()) {
                    ColorHansManager.this.hansFreeze(-268435454);
                    ColorHansManager.this.sendRepeatFZActionMsg();
                }
            }
        };
        private String mLastResumePkgName = "";
        /* access modifiers changed from: private */
        public int mLastResumeUid = -1;
        private final IUidObserver mUidObserver = new IUidObserver.Stub() {
            /* class com.android.server.am.ColorHansManager.HansTrigger.AnonymousClass2 */

            public void onUidGone(int uid, boolean disabled) throws RemoteException {
                synchronized (ColorHansManager.this.mHansLock) {
                    ColorHansManager.this.mHansRunningList.remove(Integer.valueOf(uid));
                    ColorHansPackageSelector.PackageState hansPackage = (ColorHansPackageSelector.PackageState) ColorHansManager.this.mManagedAppStateMap.get(uid);
                    if (hansPackage != null) {
                        ColorHansManager.this.mStateMachine.exitStateMachine(hansPackage.getStrUid());
                        hansPackage.setCurState(0);
                        if (hansPackage.getEnterBgTime() > 0) {
                            ColorHansManager.this.notifyProcessBgExit(uid, hansPackage.getPkgName(), SystemClock.elapsedRealtime() - hansPackage.getEnterBgTime());
                        }
                        hansPackage.setEnterBgTime(0);
                    }
                }
            }

            public void onUidActive(int uid) throws RemoteException {
                synchronized (ColorHansManager.this.mHansLock) {
                    ColorHansManager.this.mHansRunningList.add(Integer.valueOf(uid));
                    ColorHansPackageSelector.PackageState hansPackage = (ColorHansPackageSelector.PackageState) ColorHansManager.this.mManagedAppStateMap.get(uid);
                    if (hansPackage != null && hansPackage.getCurState() == 2) {
                        ColorHansManager.this.mStateMachine.stopStateMachine(hansPackage.getCurState(), hansPackage.getStrUid());
                        hansPackage.setCurState(1);
                        ColorHansManager.this.mStateMachine.enterStateMachine(hansPackage, 131072);
                    }
                    if (hansPackage != null) {
                        if (ColorHansManager.this.mCommonConfig.isCharging() || uid == HansTrigger.this.mLastResumeUid) {
                            hansPackage.setEnterBgTime(-1);
                        } else if (hansPackage.getEnterBgTime() == 0) {
                            hansPackage.setEnterBgTime(SystemClock.elapsedRealtime());
                        }
                    }
                }
            }

            public void onUidIdle(int uid, boolean disabled) throws RemoteException {
                synchronized (ColorHansManager.this.mHansLock) {
                    ColorHansManager.this.mHansRunningList.add(Integer.valueOf(uid));
                    ColorHansPackageSelector.PackageState hansPackage = (ColorHansPackageSelector.PackageState) ColorHansManager.this.mManagedAppStateMap.get(uid);
                    if (hansPackage != null) {
                        if (ColorHansManager.this.mCommonConfig.isCharging()) {
                            hansPackage.setEnterBgTime(-1);
                        } else if (hansPackage.getEnterBgTime() == 0) {
                            hansPackage.setEnterBgTime(SystemClock.elapsedRealtime());
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
            try {
                ActivityManager.getService().registerUidObserver(this.mUidObserver, 14, -1, (String) null);
            } catch (Exception e) {
                HansLogger access$700 = ColorHansManager.this.mHansLogger;
                access$700.e("registerUidObserver failed " + e);
            }
        }

        /* access modifiers changed from: private */
        public void setLastResumePackage(int uid, String pkgName) {
            HansLogger access$700 = ColorHansManager.this.mHansLogger;
            access$700.i("ColorHansManager ", "resume pkg: " + pkgName + ", uid:" + uid + ", prev pkg: " + this.mLastResumePkgName + ", prev uid: " + this.mLastResumeUid);
            synchronized (ColorHansManager.this.mHansLock) {
                ColorHansPackageSelector.PackageState packageState = (ColorHansPackageSelector.PackageState) ColorHansManager.this.mManagedAppStateMap.get(this.mLastResumeUid);
                if (packageState != null) {
                    packageState.setLastUsedTime(SystemClock.elapsedRealtime());
                    if (ColorHansManager.this.mCommonConfig.hasHansEnable() && !ColorHansManager.this.mCommonConfig.isCharging()) {
                        ColorHansManager.this.mStateMachine.enterStateMachine(packageState, -268435455);
                        packageState.setEnterBgTime(SystemClock.elapsedRealtime());
                    }
                }
                ColorHansPackageSelector.PackageState resumePackage = (ColorHansPackageSelector.PackageState) ColorHansManager.this.mManagedAppStateMap.get(uid);
                if (resumePackage != null && !ColorHansManager.this.mCommonConfig.isCharging() && ColorHansManager.this.mCommonConfig.hasHansEnable()) {
                    if (resumePackage.getEnterBgTime() > 0) {
                        ColorHansManager.this.notifyProcessBgExit(uid, pkgName, SystemClock.elapsedRealtime() - resumePackage.getEnterBgTime());
                    }
                    resumePackage.setEnterBgTime(-1);
                }
            }
            this.mLastResumeUid = uid;
            this.mLastResumePkgName = pkgName;
        }

        /* access modifiers changed from: protected */
        public int getLastResumeUid() {
            return this.mLastResumeUid;
        }

        /* access modifiers changed from: protected */
        public String getLastResumePkgName() {
            return this.mLastResumePkgName;
        }

        public void setAlarm(long alarmTime) {
            AlarmManager alarmManager = (AlarmManager) ColorHansManager.this.mContext.getSystemService("alarm");
            if (alarmManager != null) {
                alarmManager.setExact(0, System.currentTimeMillis() + alarmTime, "hans", this.mHansListener, ColorHansManager.this.mMainHandler);
            }
        }

        public void cancelAlarm() {
            AlarmManager alarmManager = (AlarmManager) ColorHansManager.this.mContext.getSystemService("alarm");
            if (alarmManager != null) {
                alarmManager.cancel(this.mHansListener);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateSpecialWindow(String exitPkg, int windowMode) {
        if (!TextUtils.isEmpty(exitPkg)) {
            if (windowMode == 3 || windowMode == 4) {
                synchronized (this.mSpecialWindowList) {
                    if (!this.mSpecialWindowList.contains(exitPkg)) {
                        this.mSpecialWindowList.add(exitPkg);
                    }
                }
            }
            if (windowMode == ColorZoomWindowManager.WINDOWING_MODE_ZOOM && !this.mIsZoomWindowMode) {
                this.mIsZoomWindowMode = true;
                synchronized (this.mSpecialWindowList) {
                    if (!this.mSpecialWindowList.contains(exitPkg)) {
                        this.mSpecialWindowList.add(exitPkg);
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

    /* access modifiers changed from: private */
    public boolean inSpecialWindowMode(String packageName) {
        boolean contains;
        synchronized (this.mSpecialWindowList) {
            contains = this.mSpecialWindowList.contains(packageName);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public class HansStateMachine {
        static final int HANS_STATE_DEFAULT = 0;
        static final int HANS_STATE_FROZEN = 3;
        static final int HANS_STATE_MIDDLE = 2;
        static final int HANS_STATE_RUNNING = 1;
        public static final int MSG_HANS_EXECUTE_FREEZE = 4;
        public static final int MSG_HANS_FIRST_EXECUTE = 1;
        public static final int MSG_HANS_TRANSFER_STATE_FROZEN = 3;
        public static final int MSG_HANS_TRANSFER_STATE_MIDDLE = 2;
        static final String TAG = "HansStateMachine";
        /* access modifiers changed from: private */
        public long firstStageRepeatTime = ((long) ColorHansManager.this.mHansPackageSelector.getPolicyConfig().mRepeatTime);
        /* access modifiers changed from: private */
        public long firstStageTime = ((long) ColorHansManager.this.mHansPackageSelector.getPolicyConfig().mFirstCheckTime);
        /* access modifiers changed from: private */
        public long secondStageTime = ((long) ColorHansManager.this.mHansPackageSelector.getPolicyConfig().mSecondCheckTime);
        StateMachineHandler stateMachineHandler = null;

        public HansStateMachine() {
            HandlerThread thread = new HandlerThread("StateMachineHandler", -2);
            thread.start();
            this.stateMachineHandler = new StateMachineHandler(thread.getLooper());
        }

        public void stepHansState(int preState, int curState, int reason, ColorHansPackageSelector.PackageState hansPackage, boolean isDelay, List<String> navigationList, List<Integer> audioList) {
            if (hansPackage != null) {
                if (preState != curState) {
                    HansLogger access$700 = ColorHansManager.this.mHansLogger;
                    access$700.d(TAG, "packageName: " + hansPackage.getPkgName() + ",state: " + coverStateToStr(preState) + " -> " + coverStateToStr(curState) + ",Reason: " + ColorHansManager.this.covertReasonToStr(reason));
                }
                if (curState == 1) {
                    if (ColorHansManager.this.isHansImportantCase(hansPackage, navigationList, audioList)) {
                        HansLogger access$7002 = ColorHansManager.this.mHansLogger;
                        access$7002.d("delay freeze " + hansPackage.getPkgName() + " which is important case: " + hansPackage.getImportantReason());
                        ColorHansManager.this.notifyNotFreeze(hansPackage.getUid(), hansPackage.getPkgName(), hansPackage.getImportantReason());
                        sendStateMachineMessage(hansPackage.getPkgName(), hansPackage.getUid(), 2, true, this.firstStageRepeatTime, reason, hansPackage.getStrUid());
                        return;
                    }
                    sendStateMachineMessage(hansPackage.getPkgName(), hansPackage.getUid(), 2, false, 0, reason, hansPackage.getStrUid());
                } else if (curState == 2) {
                    if (isDelay) {
                        sendStateMachineMessage(hansPackage.getPkgName(), hansPackage.getUid(), 3, true, this.secondStageTime, reason, hansPackage.getStrUid());
                    } else {
                        sendStateMachineMessage(hansPackage.getPkgName(), hansPackage.getUid(), 3, false, 0, reason, hansPackage.getStrUid());
                    }
                } else if (curState == 3) {
                    sendExecuteFreezeMessage(hansPackage.getPkgName(), hansPackage.getUid(), 4, reason, hansPackage.getStrUid());
                }
            }
        }

        private String coverStateToStr(int preState) {
            if (preState == 1) {
                return "R";
            }
            if (preState == 2) {
                return "M";
            }
            if (preState != 3) {
                return "";
            }
            return "F";
        }

        private void sendExecuteFreezeMessage(String packageName, int uid, int what, int reason, String strUid) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.obj = strUid;
            Bundle bundle = new Bundle();
            bundle.putString("pkgName", packageName);
            bundle.putInt("uid", uid);
            bundle.putInt("reason", reason);
            bundle.putString("strUid", strUid);
            msg.setData(bundle);
            if (this.stateMachineHandler.hasMessages(what, strUid)) {
                this.stateMachineHandler.removeMessages(what, strUid);
            }
            this.stateMachineHandler.sendMessage(msg);
        }

        private void sendFirstExecuteMessage(String packageName, int uid, int what, long time, int reason, String strUid) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.obj = strUid;
            Bundle bundle = new Bundle();
            bundle.putString("pkgName", packageName);
            bundle.putInt("uid", uid);
            bundle.putInt("reason", reason);
            bundle.putString("strUid", strUid);
            msg.setData(bundle);
            if (this.stateMachineHandler.hasMessages(what, strUid)) {
                this.stateMachineHandler.removeMessages(what, strUid);
            }
            this.stateMachineHandler.sendMessageDelayed(msg, time);
        }

        private void sendStateMachineMessage(String packageName, int uid, int what, boolean isDelay, long time, int reason, String strUid) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.obj = strUid;
            Bundle bundle = new Bundle();
            bundle.putString("pkgName", packageName);
            bundle.putInt("uid", uid);
            bundle.putInt("reason", reason);
            bundle.putBoolean("isDelay", isDelay);
            bundle.putString("strUid", strUid);
            msg.setData(bundle);
            if (this.stateMachineHandler.hasMessages(what, strUid)) {
                this.stateMachineHandler.removeMessages(what, strUid);
            }
            this.stateMachineHandler.sendMessageDelayed(msg, time);
        }

        /* access modifiers changed from: private */
        public void scheduleHansStateMsg(Message msg) {
            Bundle bundle;
            if (ColorHansManager.this.mCommonConfig.isScreenOn() && !ColorHansManager.this.mCommonConfig.isCharging() && (bundle = msg.getData()) != null) {
                String pkgName = bundle.getString("pkgName", null);
                int uid = bundle.getInt("uid", -1);
                int reason = bundle.getInt("reason", -1);
                boolean isDelay = bundle.getBoolean("isDelay", false);
                synchronized (ColorHansManager.this.mHansLock) {
                    ColorHansPackageSelector.PackageState pkg = (ColorHansPackageSelector.PackageState) ColorHansManager.this.mManagedAppStateMap.get(uid);
                    if (!(pkg == null || pkgName == null || uid == -1 || reason == -1)) {
                        stateSwitch(pkgName, uid, reason, pkg.getCurState(), isDelay, pkg);
                    }
                }
            }
        }

        private void stateSwitch(String pkgName, int uid, int reason, int state, boolean isDelay, ColorHansPackageSelector.PackageState pkg) {
            int preState = pkg.getCurState();
            if (state != 1) {
                if (state == 2 && state == 2) {
                    pkg.setCurState(3);
                }
            } else if (state == 1 && !isDelay) {
                pkg.setCurState(2);
            }
            stepHansState(preState, pkg.getCurState(), reason, pkg, isDelay, null, null);
        }

        public void stopStateMachine(int type, String strUid) {
            HansLogger access$700 = ColorHansManager.this.mHansLogger;
            access$700.d(TAG, "stop StateMachine (uid: " + strUid + ", type: " + type + ")");
            if (type == 0) {
                removeStateMachineMsg(1, strUid);
            } else if (1 == type) {
                removeStateMachineMsg(2, strUid);
            } else if (2 == type) {
                removeStateMachineMsg(3, strUid);
            } else if (3 == type) {
                removeStateMachineMsg(4, strUid);
            }
        }

        public void exitStateMachine() {
            ColorHansManager.this.mHansLogger.d(TAG, "exitStateMachine..");
            this.stateMachineHandler.removeMessages(1);
            this.stateMachineHandler.removeMessages(2);
            this.stateMachineHandler.removeMessages(3);
            this.stateMachineHandler.removeMessages(4);
        }

        public void exitStateMachine(String strUid) {
            HansLogger access$700 = ColorHansManager.this.mHansLogger;
            access$700.d(TAG, "uid: " + strUid + " exitStateMachine..");
            this.stateMachineHandler.removeMessages(1, strUid);
            this.stateMachineHandler.removeMessages(2, strUid);
            this.stateMachineHandler.removeMessages(3, strUid);
            this.stateMachineHandler.removeMessages(4, strUid);
        }

        public void enterStateMachine(ColorHansPackageSelector.PackageState hansPackage, int reason) {
            HansLogger access$700 = ColorHansManager.this.mHansLogger;
            access$700.d(TAG, "package: " + hansPackage.getPkgName() + " enterStateMachine..");
            hansPackage.setCurState(1);
            sendFirstExecuteMessage(hansPackage.getPkgName(), hansPackage.getUid(), 1, this.firstStageTime, reason, hansPackage.getStrUid());
        }

        private void removeStateMachineMsg(int what, String strUid) {
            this.stateMachineHandler.removeMessages(what, strUid);
        }

        /* access modifiers changed from: private */
        public void enterStateMachine(int reason) {
            synchronized (ColorHansManager.this.mHansLock) {
                Iterator<Integer> it = ColorHansManager.this.mHansRunningList.iterator();
                while (it.hasNext()) {
                    ColorHansPackageSelector.PackageState ps = (ColorHansPackageSelector.PackageState) ColorHansManager.this.mManagedAppStateMap.get(it.next().intValue());
                    if (ps != null && !ps.getFreezed()) {
                        enterStateMachine(ps, reason);
                    }
                }
            }
        }

        class StateMachineHandler extends Handler {
            public StateMachineHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    HansStateMachine.this.scheduleHansFirstExecuteMsg(msg);
                } else if (i == 2 || i == 3) {
                    HansStateMachine.this.scheduleHansStateMsg(msg);
                } else if (i == 4) {
                    HansStateMachine.this.scheduleExecuteFreezeMsg(msg);
                }
            }
        }

        /* access modifiers changed from: private */
        public void scheduleHansFirstExecuteMsg(Message msg) {
            Bundle bundle;
            if (ColorHansManager.this.mCommonConfig.isScreenOn() && !ColorHansManager.this.mCommonConfig.isCharging() && (bundle = msg.getData()) != null) {
                String pkgName = bundle.getString("pkgName", null);
                int uid = bundle.getInt("uid", -1);
                int reason = bundle.getInt("reason", -1);
                List<String> navigationList = ColorHansManager.this.mCommonListManager.getNavigationList();
                List<Integer> audioList = ColorHansManager.this.mCommonListManager.getAudioFocus();
                synchronized (ColorHansManager.this.mHansLock) {
                    ColorHansPackageSelector.PackageState pkg = (ColorHansPackageSelector.PackageState) ColorHansManager.this.mManagedAppStateMap.get(uid);
                    if (!(pkgName == null || uid == -1 || reason == -1)) {
                        stepHansState(1, 1, reason, pkg, false, navigationList, audioList);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        public void scheduleExecuteFreezeMsg(Message msg) {
            if (ColorHansManager.this.mCommonConfig.isScreenOn() && !ColorHansManager.this.mCommonConfig.isCharging()) {
                Bundle bundle = msg.getData();
                if (bundle != null) {
                    String pkgName = bundle.getString("pkgName", null);
                    int uid = bundle.getInt("uid", -1);
                    int reason = bundle.getInt("reason", -1);
                    bundle.getString("strUid");
                    if (pkgName != null && uid != -1 && reason != -1 && isValidStateForFreeze(uid)) {
                        if (ColorHansManager.this.mResumeUid == uid || (ColorHansManager.this.mTrigger != null && ColorHansManager.this.mTrigger.mLastResumeUid == uid)) {
                            HansLogger access$700 = ColorHansManager.this.mHansLogger;
                            access$700.i("top pkg " + pkgName + " don't freeze.");
                        } else if (ColorHansManager.this.inSpecialWindowMode(pkgName)) {
                            HansLogger access$7002 = ColorHansManager.this.mHansLogger;
                            access$7002.i("visible window pkg " + pkgName + " don't freeze.");
                        } else if (ColorHansManager.this.isHansImportantCaseForFrozenState(uid) || !ColorHansManager.this.hansFreeze(uid, reason)) {
                            synchronized (ColorHansManager.this.mHansLock) {
                                ColorHansPackageSelector.PackageState ps = (ColorHansPackageSelector.PackageState) ColorHansManager.this.mManagedAppStateMap.get(uid);
                                if (ps != null) {
                                    int preState = ps.getCurState();
                                    ps.setCurState(2);
                                    ColorHansManager.this.mStateMachine.stepHansState(preState, ps.getCurState(), reason, ps, true, null, null);
                                }
                            }
                        }
                    }
                }
            }
        }

        private boolean isValidStateForFreeze(int uid) {
            boolean ret = false;
            synchronized (ColorHansManager.this.mHansLock) {
                ColorHansPackageSelector.PackageState ps = (ColorHansPackageSelector.PackageState) ColorHansManager.this.mManagedAppStateMap.get(uid);
                if (ps != null) {
                    if (ps.getCurState() == 3) {
                        ret = true;
                    } else {
                        HansLogger access$700 = ColorHansManager.this.mHansLogger;
                        access$700.d("current state is " + ps.getCurState() + ", not HANS_STATE_FROZEN");
                    }
                }
            }
            return ret;
        }

        public void dumpHandler(PrintWriter pw) {
            StateMachineHandler stateMachineHandler2 = this.stateMachineHandler;
            if (stateMachineHandler2 != null) {
                stateMachineHandler2.dump(new PrintWriterPrinter(pw), "Hans");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class CommonConfig {
        /* access modifiers changed from: private */
        public boolean mCharging = false;
        /* access modifiers changed from: private */
        public boolean mChinaModel = true;
        private boolean mDisableNetwork = false;
        private boolean mGmsRestrict = false;
        private boolean mHansEnable = true;
        private long mScreenOffTime = 0;
        private boolean mScreenOn = true;

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

        public void setDisableNetWork(boolean isDisable) {
            this.mDisableNetwork = isDisable;
        }

        public boolean isDisableNetWork() {
            return this.mDisableNetwork;
        }

        public void setScreenOffTime(long mScreenOffTime2) {
            this.mScreenOffTime = mScreenOffTime2;
        }

        public long getScreenOffTime() {
            return this.mScreenOffTime;
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

    class HansNativeService {
        private static final String DESCRIPTOR = "oppo.hans.IHansComunication";
        private static final int HANS_BINDER_CODE_OFFSET = 1000;
        private static final int TRANSACTION_HANS_ADD_BINDER_TRANSACTION_UID = 1003;
        private static final int TRANSACTION_HANS_ADD_PACKET_MONITORED_UID = 1001;
        private static final int TRANSACTION_HANS_DELETE_ALL_PACKET_MONITORED_UID = 1002;
        private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
            /* class com.android.server.am.ColorHansManager.HansNativeService.AnonymousClass1 */

            public void binderDied() {
                IBinder unused = HansNativeService.this.mRemoteHansService = null;
                ColorHansManager.this.mHansLogger.i("IHansComunication deathRecipient!");
            }
        };
        /* access modifiers changed from: private */
        public IBinder mRemoteHansService = null;

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
            } catch (RemoteException e) {
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
            } catch (RemoteException e) {
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
            } catch (RemoteException e) {
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

    class HansRegisterBrdAction extends BroadcastReceiver {
        public HansRegisterBrdAction() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
            filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            filter.addAction("android.intent.action.DATE_CHANGED");
            filter.setPriority(ColorMultiAppManagerService.USER_ID);
            ColorHansManager.this.mContext.registerReceiver(this, filter, null, ColorHansManager.this.mMainHandler);
            initCharingStatus();
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    ColorHansManager.this.mCommonConfig.setScreenOn(true);
                    ColorHansManager.this.mMainHandler.sendMessage(2, 0);
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    ColorHansManager.this.mCommonConfig.setScreenOn(false);
                    ColorHansManager.this.mCommonConfig.setScreenOffTime(SystemClock.elapsedRealtime());
                    ColorHansManager.this.mMainHandler.sendMessage(1, 0);
                } else if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
                    ColorHansManager.this.mCommonConfig.setCharging(true);
                    ColorHansManager.this.mMainHandler.sendMessage(3, 0);
                } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                    ColorHansManager.this.mCommonConfig.setCharging(false);
                    ColorHansManager.this.mMainHandler.sendMessage(4, 0);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action) || "android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    ColorHansManager.this.mCommonConfig.setDisableNetWork(intent.getBooleanExtra("deepsleepdisable", false));
                } else if ("android.intent.action.DATE_CHANGED".equals(action)) {
                    ColorHansManager.this.mMainHandler.sendMessage(9, 10000);
                }
            }
        }

        private void initCharingStatus() {
            Intent batteryStatus = ColorHansManager.this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"), null, null);
            if (batteryStatus != null) {
                boolean z = false;
                int plugType = batteryStatus.getIntExtra("plugged", 0);
                CommonConfig access$300 = ColorHansManager.this.mCommonConfig;
                if (plugType != 0) {
                    z = true;
                }
                access$300.setCharging(z);
            }
        }
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

    public void updatePolicyConfig() {
        CommonConfig commonConfig = this.mCommonConfig;
        if (commonConfig != null && this.mStateMachine != null) {
            commonConfig.setHansEnable(this.mHansPackageSelector.getPolicyConfig().mHansFeature);
            long unused = this.mStateMachine.firstStageTime = (long) this.mHansPackageSelector.getPolicyConfig().mFirstCheckTime;
            long unused2 = this.mStateMachine.firstStageRepeatTime = (long) this.mHansPackageSelector.getPolicyConfig().mRepeatTime;
            long unused3 = this.mStateMachine.secondStageTime = (long) this.mHansPackageSelector.getPolicyConfig().mSecondCheckTime;
            this.mStatisticsSwitch = this.mHansPackageSelector.getPolicyConfig().mStatisticsSwitch;
            this.mGmsFreezeSwitch = this.mHansPackageSelector.getPolicyConfig().mGmsFeature;
            hansDeamonSwitch(this.mCommonConfig.hasHansEnable());
        }
    }

    public boolean updateHansConfig(Bundle data) {
        ColorHansPackageSelector colorHansPackageSelector;
        if (this.mAms != null && (colorHansPackageSelector = this.mHansPackageSelector) != null) {
            return colorHansPackageSelector.updateHansConfig(data);
        }
        Log.e("ColorHansManager", "ColorHansManager is not init!");
        return false;
    }

    /* access modifiers changed from: private */
    public void updateProcessBgTime(boolean isPowerConnected) {
        synchronized (this.mHansLock) {
            for (int i = 0; i < this.mManagedAppStateMap.size(); i++) {
                ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.valueAt(i);
                if (ps != null) {
                    if (isPowerConnected) {
                        if (ps.getEnterBgTime() > 0) {
                            notifyProcessBgExit(ps.getUid(), ps.getPkgName(), SystemClock.elapsedRealtime() - ps.getEnterBgTime());
                            ps.setEnterBgTime(-1);
                        }
                    } else if (ps.getEnterBgTime() != 0) {
                        if (ps.getUid() != this.mTrigger.getLastResumeUid()) {
                            ps.setEnterBgTime(SystemClock.elapsedRealtime());
                        } else {
                            ps.setEnterBgTime(-1);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleDateChanged() {
        if (this.mStatisticsSwitch) {
            synchronized (this.mHansLock) {
                long now = SystemClock.elapsedRealtime();
                for (int i = 0; i < this.mManagedAppStateMap.size(); i++) {
                    ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.valueAt(i);
                    if (ps != null) {
                        if (ps.getFreezed()) {
                            notifyUnfreeze(ps.getUid(), ps.getPkgName(), now - ps.getFreezeElapsedTime(), "?");
                            ps.setFreezeElapsedTime(now);
                        }
                        if (ps.getEnterBgTime() > 0) {
                            notifyProcessBgExit(ps.getUid(), ps.getPkgName(), now - ps.getEnterBgTime());
                            ps.setEnterBgTime(now);
                        }
                    }
                }
            }
            synchronized (this.mFreezeTimeList) {
                if (this.mFreezeTimeList.size() != 0) {
                    notifyRecordData(new ArrayList(this.mFreezeTimeList), RECORD_TYPE_UNFREEZE);
                    this.mFreezeTimeList.clear();
                }
            }
            synchronized (this.mLiveTimeList) {
                if (this.mLiveTimeList.size() != 0) {
                    notifyRecordData(new ArrayList(this.mLiveTimeList), RECORD_TYPE_LIVE_TIME);
                    this.mLiveTimeList.clear();
                }
            }
            synchronized (this.mNotFreezeList) {
                if (this.mNotFreezeList.size() != 0) {
                    notifyRecordData(new ArrayList(this.mNotFreezeList), RECORD_TYPE_NOT_FREEZE);
                    this.mNotFreezeList.clear();
                }
            }
        }
    }

    public void setHansController(IColorHansController controller) {
        if (this.mAms == null) {
            Log.e("ColorHansManager", "ColorHansManager is not init!");
        } else {
            this.mHansController = controller;
        }
    }

    private void notifyRecordData(List<String> dataList, String type) {
        IColorHansController iColorHansController = this.mHansController;
        if (iColorHansController != null) {
            try {
                iColorHansController.notifyRecordData(dataList, type);
            } catch (Exception e) {
                this.mHansController = null;
                this.mHansLogger.d("notifyRecordData error!");
            }
        }
    }

    private void notifyUnfreeze(int uid, String pkgName, long freezeTime, String reason) {
        if (this.mStatisticsSwitch) {
            String item = "" + uid + "#" + freezeTime + "#" + reason;
            synchronized (this.mFreezeTimeList) {
                this.mFreezeTimeList.add(item);
                if (this.mFreezeTimeList.size() >= 100) {
                    notifyRecordData(new ArrayList(this.mFreezeTimeList), RECORD_TYPE_UNFREEZE);
                    this.mFreezeTimeList.clear();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyProcessBgExit(int uid, String pkgName, long liveTime) {
        if (this.mStatisticsSwitch) {
            String item = "" + uid + "#" + liveTime;
            synchronized (this.mLiveTimeList) {
                this.mLiveTimeList.add(item);
                if (this.mLiveTimeList.size() >= 100) {
                    notifyRecordData(new ArrayList(this.mLiveTimeList), RECORD_TYPE_LIVE_TIME);
                    this.mLiveTimeList.clear();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyNotFreeze(int uid, String pkgName, String reason) {
        if (this.mStatisticsSwitch) {
            String item = "" + uid + "#" + reason;
            synchronized (this.mNotFreezeList) {
                this.mNotFreezeList.add(item);
                if (this.mNotFreezeList.size() >= 100) {
                    notifyRecordData(new ArrayList(this.mNotFreezeList), RECORD_TYPE_NOT_FREEZE);
                    this.mNotFreezeList.clear();
                }
            }
        }
    }

    private void registerGmsRestrictObserve() {
        if (this.mCommonConfig.isChinaRegion() && this.mGmsFreezeSwitch) {
            setGmsRestrict();
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("google_restric_info"), false, this.mGmsObserver, -1);
        }
    }

    /* access modifiers changed from: private */
    public void setGmsRestrict() {
        boolean z = false;
        int value = Settings.Secure.getInt(this.mContext.getContentResolver(), "google_restric_info", 0);
        CommonConfig commonConfig = this.mCommonConfig;
        if (value == 1) {
            z = true;
        }
        commonConfig.setGmsRestrict(z);
        this.mMainHandler.sendMessage(6, 0);
    }

    /* access modifiers changed from: private */
    public void handleUpdateGmsRestrict() {
        if (this.mGmsFreezeSwitch) {
            SparseArray<String> gmsList = this.mHansPackageSelector.getGmsList();
            HansLogger hansLogger = this.mHansLogger;
            hansLogger.d("handleUpdateGmsRestrict " + this.mCommonConfig.isRestrictGms());
            for (int i = 0; i < gmsList.size(); i++) {
                int uid = gmsList.keyAt(i);
                String pkg = gmsList.valueAt(i);
                if (this.mCommonConfig.isRestrictGms()) {
                    ColorHansPackageSelector colorHansPackageSelector = this.mHansPackageSelector;
                    Objects.requireNonNull(colorHansPackageSelector);
                    addGmsPkgState(uid, new ColorHansPackageSelector.PackageState(pkg, uid));
                } else {
                    removeGmsPkgState(uid);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDeviceIdleList(int uid) {
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

    private void registerAppChangedListener() {
        this.mCommonListManager.addAppChangedListener(this.listener);
    }

    /* access modifiers changed from: private */
    public void handleAppChanged(Message msg) {
        int uid;
        if (msg != null && msg.getData() != null) {
            Bundle data = msg.getData();
            int uid2 = data.getInt("uid", -1);
            String pkgName = data.getString("pkg", "");
            String cfgName = data.getString("cfg", "");
            if (!TextUtils.isEmpty(pkgName) && !TextUtils.isEmpty(cfgName)) {
                if (uid2 < 1) {
                    uid = this.mCommonListManager.getPackageUid(pkgName, this.mAms.getCurrentUser().id);
                } else {
                    uid = uid2;
                }
                synchronized (this.mHansLock) {
                    ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
                    if (ps != null) {
                        if (ps.getFreezed()) {
                            hansUnFreeze(uid, 16777217);
                        }
                        this.mStateMachine.stopStateMachine(ps.getCurState(), ps.getStrUid());
                    }
                }
            }
        }
    }

    public void hansUpdateForegroundServiceState(int uid, String pkgName, boolean isForeground) {
        synchronized (this.mHansLock) {
            ColorHansPackageSelector.PackageState ps = this.mManagedAppStateMap.get(uid);
            if (ps != null) {
                if (isForeground) {
                    ps.setFgService(true);
                } else {
                    ps.setFgService(false);
                }
            }
        }
    }

    private void registerGnssStatusCallback() {
        if (this.mLocationManager == null) {
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        }
        this.mLocationManager.registerGnssStatusCallback(this.mGnssStatusCallback, this.mMainHandler);
    }

    /* access modifiers changed from: private */
    public void handleUfzNavigationApp() {
        List<String> gpsList = this.mCommonListManager.getNavigationList();
        HansLogger hansLogger = this.mHansLogger;
        hansLogger.fullLog("handleUfzNavigationApp " + gpsList);
        if (gpsList != null) {
            for (String pkgName : gpsList) {
                int uid = this.mCommonListManager.getPackageUid(pkgName, this.mAms.getCurrentUser().id);
                if (uid != -1) {
                    hansUnFreeze(uid, 16777220);
                }
            }
        }
    }
}
