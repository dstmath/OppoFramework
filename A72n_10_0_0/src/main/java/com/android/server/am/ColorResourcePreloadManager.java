package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.IUidObserver;
import android.app.OppoBaseActivityOptions;
import android.common.ColorFrameworkFactory;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.app.IAppOpsService;
import com.android.server.ColorDeviceIdleHelper;
import com.android.server.am.ColorResourcePreloadManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.IColorAppQuickFreezeManager;
import com.android.server.policy.OppoPhoneWindowManager;
import com.android.server.storage.ColorDeviceStorageMonitorService;
import com.android.server.wm.ColorFreeformManagerService;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import com.color.app.ColorAppEnterInfo;
import com.color.app.ColorAppExitInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.ColorAppSwitchManager;
import com.color.settings.ColorSettings;
import com.color.util.ColorTypeCastingHelper;
import com.coloros.deepthinker.IColorDeepThinkerManager;
import com.coloros.deepthinker.ServiceStateObserver;
import com.coloros.eventhub.sdk.EventCallback;
import com.coloros.eventhub.sdk.aidl.DeviceEvent;
import com.coloros.eventhub.sdk.aidl.DeviceEventResult;
import com.coloros.eventhub.sdk.aidl.EventRequestConfig;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class ColorResourcePreloadManager implements IColorResourcePreloadManager {
    private static final String ACTION_PRELOAD_ANSWER_MN = "com.android.server.am.PRELOAD_ANSWER_MAX_NUM";
    private static final String ACTION_PRELOAD_APP = "com.android.server.am.PRELOAD_APP";
    private static final String ACTION_PRELOAD_ASK_MN = "com.android.server.am.PRELOAD_ASK_MAX_NUM";
    private static final String ACTION_PRELOAD_STATUS = "com.android.server.am.PRELOAD_STATUS";
    private static final String ACTION_RELEASE_APP = "com.android.server.am.RELEASE_APP";
    private static final String ACTION_TESTAPP_LAUNCHED = "com.android.server.am.TESTAPP_LAUNCHED";
    private static final String ACTION_TESTAPP_STOPPED = "com.android.server.am.TESTAPP_STOPPED";
    private static final int APP_SWITCH_DELAY_TIME = 5000;
    private static final long CPU_USAGE_THRESHOLD = 60;
    private static final int DEFAULT_PROTECT_TIME = 7200000;
    private static final int FREEZE_DELAY_TIME = 10000;
    private static final long GB_IN_BYTE = 1073741824;
    private static final int HOTNESS_COUNT = 5;
    private static final long HOTNESS_PRELOAD_COLD_TIME = 60000;
    private static final long KB_IN_BYTE = 1024;
    private static final long LONG_TIME_PRELOAD_NOT_USE_MS = 3600000;
    private static final long MB_IN_BYTE = 1048576;
    private static final long MB_IN_KB = 1024;
    private static final int PERIOD_DELAY_TIME = 60000;
    private static final String PRELOAD_CONFIG_NAME = "preload_protect_list";
    private static final String PRELOAD_EXTRA_NAME = "preload_extra_name";
    private static final String PRELOAD_EXTRA_UID = "preload_extra_uid";
    private static final String PRELOAD_HOSTING_TYPE = "rplaunch";
    private static final String PRELOAD_LAUNCH_KEY = "android.preload_launch";
    private static final int PRELOAD_NEXT_TIME = 5000;
    private static final String PRELOAD_ORDER_KEY = "android.preload_order";
    private static final String PRELOAD_REASON_AI = "AI";
    private static final String PRELOAD_REASON_BOOT_COMPLETED = "boot_completed";
    private static final String PRELOAD_REASON_DEBUG = "debug";
    private static final String PRELOAD_REASON_KEY = "android.preload_reason";
    private static final String PRELOAD_REASON_VIP = "vip";
    private static final String PRELOAD_RESET_REASON_ALLOW_COMPONENT = "allow_component";
    private static final String PRELOAD_RESET_REASON_GLOBAL_POLICY = "global_model";
    private static final String PRELOAD_RESET_REASON_PROCESS_DIED = "process_died";
    private static final String PRELOAD_RESET_REASON_PROCESS_KILLED = "process_killed";
    private static final String PRELOAD_RESET_REASON_USER_START = "user_start";
    private static final String PRELOAD_THREAD_NAME = "RPMHandler";
    private static final String PROHIBIT_FOREGROUND_LIST_FILE = "oppoguardelf/prohibit_foreground_app.xml";
    private static final int REQUEST_TYPE_KILL_OR_STOP = 11;
    private static final long SHORT_TIME_PRELOAD_NOT_USE_MS = 900000;
    private static final int[] SYSTEM_CPU_FORMAT = {288, 8224, 8224, 8224, 8224, 8224, 8224, 8224};
    private static final String TAG = ColorResourcePreloadManager.class.getSimpleName();
    private static final String TAG_PROHIBIT_FORE_RUN = "prohibit";
    private static final int UPLOAD_INFO_DELAY_TIME = 1800000;
    private static boolean mPreloadTestSwitch = true;
    private static volatile ColorResourcePreloadManager sInstance = null;
    CpuData cpuData1 = new CpuData();
    CpuData cpuData2 = new CpuData();
    private AIPreload mAIPreload = null;
    private boolean mAIPreloadSwitch = false;
    private ActivityManagerService mAms = null;
    private BootPreload mBootPreload = null;
    private boolean mBootPreloadSwitch = false;
    private boolean mChinaModel = false;
    private IColorAppQuickFreezeManager mColorAppQuickFreezeManager = null;
    private Context mContext = null;
    private boolean mDebugAISwitch = false;
    ArrayList<String> mDefaultPkgList = new ArrayList<>();
    private boolean mGamePreloadSwitch = false;
    private Handler mKillHandler = null;
    private Object mLock = new Object();
    private boolean mLogDebugSwitch = false;
    private long mLowAvailMemThreshold;
    private Handler mMainHandler = null;
    private PackageManagerInternal mPackageManagerInternal = null;
    private BroadcastReceiver mPkgIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.am.ColorResourcePreloadManager.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int uid = intent.getIntExtra("android.intent.extra.UID", 0);
            boolean isReInstall = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
            Uri data = intent.getData();
            if (data != null) {
                ColorResourcePreloadManager.this.onPackageStatusChange(data.getSchemeSpecificPart(), action, isReInstall, uid);
            }
        }
    };
    private boolean mPreloadEnable = false;
    private PreloadHistoryLog mPreloadHistoryLog = null;
    private PreloadInfoCollector mPreloadInfoCollector = null;
    private PreloadPkgMap<PreloadPkgState> mPreloadPkgs = new PreloadPkgMap<>();
    private PreloadRUSController mPreloadRUSController = null;
    private PreloadReceiver mPreloadReceiver = new PreloadReceiver();
    private PreloadTest mPreloadTest = null;
    private volatile boolean mPreloadTestAppLaunched = false;
    private final long[] mSystemCpuData = new long[7];
    private final IUidObserver mUidObserver = new IUidObserver.Stub() {
        /* class com.android.server.am.ColorResourcePreloadManager.AnonymousClass1 */

        public void onUidGone(int uid, boolean disabled) throws RemoteException {
            synchronized (ColorResourcePreloadManager.this.mLock) {
                ColorResourcePreloadManager.this.mUidRunningList.remove(Integer.valueOf(uid));
            }
        }

        public void onUidActive(int uid) throws RemoteException {
            synchronized (ColorResourcePreloadManager.this.mLock) {
                ColorResourcePreloadManager.this.mUidRunningList.add(Integer.valueOf(uid));
            }
        }

        public void onUidIdle(int uid, boolean disabled) throws RemoteException {
            synchronized (ColorResourcePreloadManager.this.mLock) {
                ColorResourcePreloadManager.this.mUidRunningList.add(Integer.valueOf(uid));
            }
        }

        public void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException {
        }

        public void onUidCachedChanged(int uid, boolean cached) throws RemoteException {
        }
    };
    private HashSet<Integer> mUidRunningList = new HashSet<>();
    private boolean mUserVersion = "user".equals(SystemProperties.get("ro.build.type"));
    private PriorityQueue<PreloadPkgState> minHeap = null;
    private int minHeapSize = 10;
    private Runnable periodRunnable = new Runnable() {
        /* class com.android.server.am.ColorResourcePreloadManager.AnonymousClass6 */

        public void run() {
            ColorResourcePreloadManager.this.memoryStatusMonitor();
            ColorResourcePreloadManager.this.processStatusMonitor();
            ColorResourcePreloadManager.this.mMainHandler.postDelayed(ColorResourcePreloadManager.this.periodRunnable, ColorResourcePreloadManager.HOTNESS_PRELOAD_COLD_TIME);
        }
    };
    private int vipDelayTime = 0;
    ArrayList<String> vipPkgList = new ArrayList<>(Arrays.asList(ColorStartingWindowContants.WECHAT_PACKAGE_NAME));

    /* access modifiers changed from: package-private */
    public enum ComponentBlockResult {
        ALLOW_NONE_PRELOAD_TARGET,
        ALLOW_NOT_PRELOAD_STATE,
        ALLOW_AUTO_ASSOCIATE_START,
        ALLOW_MAIN_PROCESS_START,
        BLOCK_COMPONENT
    }

    /* access modifiers changed from: package-private */
    public class PreloadPkgMap<E> {
        final ArrayMap<String, SparseArray<E>> mMap = new ArrayMap<>();

        PreloadPkgMap() {
        }

        public E get(String name, int userId) {
            SparseArray<E> userIds = this.mMap.get(name);
            if (userIds == null) {
                return null;
            }
            return userIds.get(userId);
        }

        public E put(String name, int userId, E value) {
            SparseArray<E> userIds = this.mMap.get(name);
            if (userIds == null) {
                userIds = new SparseArray<>(2);
                this.mMap.put(name, userIds);
            }
            userIds.put(userId, value);
            return value;
        }

        public E remove(String name, int userId) {
            SparseArray<E> userIds = this.mMap.get(name);
            if (userIds == null) {
                return null;
            }
            E old = (E) userIds.removeReturnOld(userId);
            if (userIds.size() == 0) {
                this.mMap.remove(name);
            }
            return old;
        }

        public ArrayMap<String, SparseArray<E>> getMap() {
            return this.mMap;
        }

        public int size() {
            return this.mMap.size();
        }

        public void clear() {
            this.mMap.clear();
        }

        public int totalSize() {
            int size = 0;
            for (int i = 0; i < this.mMap.size(); i++) {
                for (int j = 0; j < this.mMap.valueAt(i).size(); j++) {
                    size++;
                }
            }
            return size;
        }
    }

    /* access modifiers changed from: package-private */
    public class PkgInfo {
        private long mOrder = 0;
        private long mPSS = 0;
        private String mPkgName = "";
        private int mUid = 0;

        public PkgInfo(String pkgName, int uid, long order) {
            this.mPkgName = pkgName;
            this.mUid = uid;
            this.mOrder = order;
        }

        public PkgInfo(String pkgName, int uid, long pss, long order) {
            this.mPkgName = pkgName;
            this.mUid = uid;
            this.mPSS = pss;
            this.mOrder = order;
        }

        public String getPkgName() {
            return this.mPkgName;
        }

        public int getUid() {
            return this.mUid;
        }

        public long getPss() {
            return this.mPSS;
        }

        public long getOrder() {
            return this.mOrder;
        }
    }

    /* access modifiers changed from: package-private */
    public class PreloadPkgState {
        private boolean isColdLaunch = true;
        private boolean isFasEnable = false;
        private boolean isHit = false;
        private boolean isLastDisabled = false;
        private boolean isLastStopped = false;
        private boolean isMemoryCompressed = false;
        private boolean isPreload = false;
        private long mAveragePss = 0;
        private long mHotnessCnt = 0;
        private long mLastPreloadTime = 0;
        private long mLastPss = 0;
        private long mNormalLaunchTime = 0;
        private int mPid = -1;
        private ArrayList<Integer> mPids = new ArrayList<>();
        private String mPkgName;
        private long mPreloadLaunchTime = 0;
        private long mPreloadOrder = 0;
        private String mPreloadReason = "";
        private String mProcessName = "";
        private ArrayList<String> mProcessNameList = new ArrayList<>();
        private int mUid;
        private int mUserId;
        private String mVersionName = "";

        public PreloadPkgState(String pkgName, int uid) {
            this.mPkgName = pkgName;
            this.mUid = uid;
            this.mUserId = UserHandle.getUserId(uid);
        }

        public String getPkgName() {
            return this.mPkgName;
        }

        public int getUid() {
            return this.mUid;
        }

        public int getUserId() {
            return this.mUserId;
        }

        public int getPid() {
            return this.mPid;
        }

        public void setPid(int pid) {
            this.mPid = pid;
        }

        public ArrayList<Integer> getPids() {
            return this.mPids;
        }

        public String getProcessName() {
            return this.mProcessName;
        }

        public ArrayList<String> getProcessNameList() {
            return this.mProcessNameList;
        }

        public void setProcessName(String processName) {
            this.mProcessName = processName;
        }

        public boolean isPreload() {
            return this.isPreload;
        }

        public void setPreload(boolean preload) {
            this.isPreload = preload;
        }

        public boolean isMemoryCompressed() {
            return this.isMemoryCompressed;
        }

        public void setMemoryCompressed(boolean memoryCompressed) {
            this.isMemoryCompressed = memoryCompressed;
        }

        public boolean isLastStopped() {
            return this.isLastStopped;
        }

        public void setLastStopped(boolean stopped) {
            this.isLastStopped = stopped;
        }

        public boolean isLastDisabled() {
            return this.isLastDisabled;
        }

        public void setLastDisabled(boolean disabled) {
            this.isLastDisabled = disabled;
        }

        public long getLastPreloadTime() {
            return this.mLastPreloadTime;
        }

        public void setLastPreloadTime(long lastPreloadTime) {
            this.mLastPreloadTime = lastPreloadTime;
        }

        public String getPreloadReason() {
            return this.mPreloadReason;
        }

        public void setPreloadReason(String preloadReason) {
            this.mPreloadReason = preloadReason;
        }

        public long getPreloadOrder() {
            return this.mPreloadOrder;
        }

        public void setPreloadOrder(long preloadOrder) {
            this.mPreloadOrder = preloadOrder;
        }

        public boolean isHit() {
            return this.isHit;
        }

        public void setHit(boolean hit) {
            this.isHit = hit;
        }

        public long getPreloadLaunchTime() {
            return this.mPreloadLaunchTime;
        }

        public void setPreloadLaunchTime(long launchTime) {
            this.mPreloadLaunchTime = launchTime;
        }

        public long getNormalLaunchTime() {
            return this.mNormalLaunchTime;
        }

        public void setNormalLaunchTime(long launchTime) {
            this.mNormalLaunchTime = launchTime;
        }

        public boolean isColdLaunch() {
            return this.isColdLaunch;
        }

        public void setColdLaunch(boolean coldLaunch) {
            this.isColdLaunch = coldLaunch;
        }

        public long getLastPss() {
            return this.mLastPss;
        }

        public void setLastPss(long lastPss) {
            this.mLastPss = lastPss;
        }

        public long getAveragePss() {
            return this.mAveragePss;
        }

        public void setAveragePss(long averagePss) {
            this.mAveragePss = averagePss;
        }

        public long getHotnessCnt() {
            return this.mHotnessCnt;
        }

        public void setHotnessCnt(long hotnessCnt) {
            this.mHotnessCnt = hotnessCnt;
        }

        public String getVersionName() {
            return this.mVersionName;
        }

        public void setVersionName(String versionName) {
            this.mVersionName = versionName;
        }

        public boolean isFasEnable() {
            return this.isFasEnable;
        }

        public void setFasEnable(boolean fasEnable) {
            this.isFasEnable = fasEnable;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PreloadPkgState pkgState = (PreloadPkgState) o;
            if (this.mUid != pkgState.mUid || !Objects.equals(this.mPkgName, pkgState.mPkgName)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mPkgName, Integer.valueOf(this.mUid));
        }

        public String toString() {
            return "PreloadPkgState{mPkgName='" + this.mPkgName + "', mUid=" + this.mUid + ", mHotnessCnt=" + this.mHotnessCnt + '}';
        }
    }

    private ColorResourcePreloadManager() {
    }

    public static ColorResourcePreloadManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorResourcePreloadManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorResourcePreloadManager();
                }
            }
        }
        return sInstance;
    }

    /* access modifiers changed from: package-private */
    public class PreloadHistoryLog {
        private static final String PRELOAD_KEY = "preload_history_key";
        private final boolean LOG_DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
        private String key;
        private Key keySpec;
        private int mHead = 0;
        private boolean mIsDynamicLog = false;
        private boolean mIsFull = false;
        private String[] mLogBuffer;
        private int mSize;

        public PreloadHistoryLog(int size) {
            this.mLogBuffer = new String[size];
            this.mSize = size;
            validateKeyLen(PRELOAD_KEY);
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

        public void addLowMemoryInfo(long needMemory, long lowAvailMem, long availMem, long totalMem) {
            StringBuffer sb = new StringBuffer();
            String currentTime = ColorResourcePreloadManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + currentTime + "] [LowMem] [ " + needMemory + " " + lowAvailMem + " am: " + availMem + " tm: " + totalMem + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void addPreloadInfo(String reason, String pkgName, int uid) {
            StringBuffer sb = new StringBuffer();
            String currentTime = ColorResourcePreloadManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + currentTime + "] [Preload] [ " + reason + " ] [ " + uid + " " + pkgName + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void addPreloadAllInfo(String reason, String uidStr) {
            StringBuffer sb = new StringBuffer();
            String currentTime = ColorResourcePreloadManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + currentTime + "] [PreloadAll] [ " + reason + " ] [ " + uidStr + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void addProcessInfo(String reason, String pkgName, int uid) {
            StringBuffer sb = new StringBuffer();
            String currentTime = ColorResourcePreloadManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + currentTime + "] [ " + reason + " ] [ " + uid + " " + pkgName + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void addLaunchInfo(ApplicationInfo appInfo, String shortComponentName, long launchTime, String type) {
            StringBuffer sb = new StringBuffer();
            String currentTime = ColorResourcePreloadManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + currentTime + "] [ Launch:" + type + " ] [ " + appInfo.uid + " " + appInfo.packageName + " " + shortComponentName + " " + launchTime + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void addBindApplicationFinishedInfo(String pkgName, int uid, int pid, long costTime) {
            StringBuffer sb = new StringBuffer();
            String currentTime = ColorResourcePreloadManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + currentTime + "] [ BAF ] [ " + pkgName + " " + uid + " " + pid + " " + costTime + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void addKeyInfo(String info) {
            StringBuffer sb = new StringBuffer();
            String triggerTime = ColorResourcePreloadManager.this.formatDateTime(System.currentTimeMillis());
            sb.append("[" + triggerTime + "] [INFO] [ " + info + " ]");
            sb.append("\n");
            put(sb.toString());
        }

        public void dumpLogBuffer(FileDescriptor fd, PrintWriter pw) {
            synchronized (this.mLogBuffer) {
                int i = 0;
                while (true) {
                    if (i < (this.mIsFull ? this.mSize : this.mHead)) {
                        if (!ColorResourcePreloadManager.this.mUserVersion) {
                            pw.print(this.mLogBuffer[i]);
                        } else {
                            pw.println(historyLogEncrypt(this.mLogBuffer[i]));
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
        public String historyLogEncrypt(String str) {
            try {
                Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
                c.init(1, this.keySpec, new IvParameterSpec(this.key.getBytes()));
                return new String(Base64.getEncoder().encode(c.doFinal(str.getBytes(StandardCharsets.UTF_8))));
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                e.printStackTrace();
                return "error encrypt\n";
            }
        }

        public void setDynamicLog(boolean enable) {
            this.mIsDynamicLog = enable;
        }

        public void i(String TAG, String msg) {
            if (this.LOG_DEBUG || this.mIsDynamicLog) {
                Log.i(TAG, msg);
            }
        }

        public void e(String TAG, String msg) {
            if (this.LOG_DEBUG || this.mIsDynamicLog) {
                Log.e(TAG, msg);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class PreloadInfoCollector {
        private static final String KEY_CLIENTTIME = "ctime";
        private static final String KEY_HITCOUNT = "hitcount";
        private static final String KEY_LAUNCHTIME = "launchtime";
        private static final String KEY_NORMAL_LAUNCHTIME = "normal_launchtime";
        private static final String KEY_PKGNAME = "pkgname";
        private static final String KEY_REASON_AI = "reason_ai";
        private static final String KEY_REASON_BOOT = "reason_boot";
        private static final String KEY_REASON_HOTNESS = "reason_hotness";
        private static final String KEY_TOTALCOUNT = "totalcount";
        private static final String KEY_USERID = "userid";
        private static final String PRELOAD_EVENT_ID = "preload";
        private static final String PRELOAD_LOG_TAG = "preloadAbility";
        ArrayMap<Integer, ArrayList<Long>> mMemInfoMap;
        ArrayMap<Integer, ArrayList<Long>> mNormalLaunchTimeMap;
        ArrayMap<Integer, PreloadInfo> mPreloadInfoMap;
        ArrayMap<Integer, ArrayList<Long>> mPreloadLaunchTimeMap;
        Object mPreloadLock;

        public PreloadInfoCollector() {
            this.mPreloadInfoMap = null;
            this.mPreloadLaunchTimeMap = null;
            this.mNormalLaunchTimeMap = null;
            this.mMemInfoMap = null;
            this.mPreloadLock = new Object();
            this.mPreloadInfoMap = new ArrayMap<>();
            this.mPreloadLaunchTimeMap = new ArrayMap<>();
            this.mNormalLaunchTimeMap = new ArrayMap<>();
            this.mMemInfoMap = new ArrayMap<>();
        }

        public void updatePreloadPkgInfo(int uid, String pkgName, String reason, String versionName) {
            synchronized (this.mPreloadLock) {
                PreloadInfo info = getPreloadInfo(uid);
                info.setUid(uid);
                info.setPkgName(pkgName);
                info.setTotalCount(info.getTotalCount() + 1);
                if (reason.contains(ColorResourcePreloadManager.PRELOAD_REASON_AI)) {
                    info.setReasonAICount(info.getReasonAICount() + 1);
                } else if (reason.contains(ColorResourcePreloadManager.PRELOAD_REASON_BOOT_COMPLETED)) {
                    info.setReasonBootCount(info.getReasonBootCount() + 1);
                }
                info.setAppVersion(versionName);
                this.mPreloadInfoMap.put(Integer.valueOf(uid), info);
            }
        }

        public void updatePreloadHotnessCnt(int uid, String pkgName, long hotnessCnt) {
            synchronized (this.mPreloadLock) {
                PreloadInfo info = getPreloadInfo(uid);
                info.setHotnessCnt(hotnessCnt);
                this.mPreloadInfoMap.put(Integer.valueOf(uid), info);
            }
        }

        public void recordPreloadLaunchTime(int uid, long time) {
            synchronized (this.mPreloadLock) {
                ArrayList<Long> list = getPreloadLaunchTimeList(uid);
                list.add(Long.valueOf(time));
                this.mPreloadLaunchTimeMap.put(Integer.valueOf(uid), list);
            }
        }

        private void updatePreloadLaunchTime() {
            synchronized (this.mPreloadLock) {
                for (int i = 0; i < this.mPreloadLaunchTimeMap.size(); i++) {
                    int sum = 0;
                    Integer uid = this.mPreloadLaunchTimeMap.keyAt(i);
                    ArrayList<Long> timeList = this.mPreloadLaunchTimeMap.valueAt(i);
                    Iterator<Long> it = timeList.iterator();
                    while (it.hasNext()) {
                        sum = (int) (((long) sum) + it.next().longValue());
                    }
                    PreloadInfo info = getPreloadInfo(uid.intValue());
                    info.setPreloadLaunchTime((long) (sum / timeList.size()));
                    this.mPreloadInfoMap.put(uid, info);
                }
                this.mPreloadLaunchTimeMap.clear();
            }
        }

        public void recordNormalLaunchTime(int uid, long time) {
            synchronized (this.mPreloadLock) {
                ArrayList<Long> list = getNormalLaunchTimeList(uid);
                list.add(Long.valueOf(time));
                this.mNormalLaunchTimeMap.put(Integer.valueOf(uid), list);
            }
        }

        private void updateNormalLaunchTime() {
            synchronized (this.mPreloadLock) {
                for (int i = 0; i < this.mNormalLaunchTimeMap.size(); i++) {
                    int sum = 0;
                    Integer uid = this.mNormalLaunchTimeMap.keyAt(i);
                    ArrayList<Long> timeList = this.mNormalLaunchTimeMap.valueAt(i);
                    Iterator<Long> it = timeList.iterator();
                    while (it.hasNext()) {
                        sum = (int) (((long) sum) + it.next().longValue());
                    }
                    PreloadInfo info = getPreloadInfo(uid.intValue());
                    info.setNormalLaunchTime((long) (sum / timeList.size()));
                    this.mPreloadInfoMap.put(uid, info);
                }
                this.mNormalLaunchTimeMap.clear();
            }
        }

        public void recordMemInfo(int uid, long mem) {
            synchronized (this.mPreloadLock) {
                ArrayList<Long> list = getMemInfoList(uid);
                list.add(Long.valueOf(mem));
                this.mMemInfoMap.put(Integer.valueOf(uid), list);
            }
        }

        private void calculateAverageMemInfo() {
            synchronized (this.mPreloadLock) {
                for (int i = 0; i < this.mMemInfoMap.size(); i++) {
                    int sum = 0;
                    Integer uid = this.mMemInfoMap.keyAt(i);
                    ArrayList<Long> memInfoList = this.mMemInfoMap.valueAt(i);
                    Iterator<Long> it = memInfoList.iterator();
                    while (it.hasNext()) {
                        sum = (int) (((long) sum) + it.next().longValue());
                    }
                    PreloadInfo info = getPreloadInfo(uid.intValue());
                    info.setMemoryInfo((long) (sum / memInfoList.size()));
                    this.mPreloadInfoMap.put(uid, info);
                }
                this.mMemInfoMap.clear();
            }
        }

        public void updatePreloadHitCount(int uid) {
            synchronized (this.mPreloadLock) {
                PreloadInfo info = getPreloadInfo(uid);
                info.setHitCount(info.getHitCount() + 1);
                this.mPreloadInfoMap.put(Integer.valueOf(uid), info);
            }
        }

        private void updatePreloadClientTime(PreloadInfo info, long time) {
            if (info != null) {
                info.setClientTime(time);
            }
        }

        private PreloadInfo getPreloadInfo(int uid) {
            PreloadInfo info = this.mPreloadInfoMap.get(Integer.valueOf(uid));
            if (info != null) {
                return info;
            }
            PreloadInfo info2 = new PreloadInfo();
            this.mPreloadInfoMap.put(Integer.valueOf(uid), info2);
            return info2;
        }

        private ArrayList<Long> getPreloadLaunchTimeList(int uid) {
            ArrayList<Long> timeList = this.mPreloadLaunchTimeMap.get(Integer.valueOf(uid));
            if (timeList != null) {
                return timeList;
            }
            ArrayList<Long> timeList2 = new ArrayList<>();
            this.mPreloadLaunchTimeMap.put(Integer.valueOf(uid), timeList2);
            return timeList2;
        }

        private ArrayList<Long> getNormalLaunchTimeList(int uid) {
            ArrayList<Long> timeList = this.mNormalLaunchTimeMap.get(Integer.valueOf(uid));
            if (timeList != null) {
                return timeList;
            }
            ArrayList<Long> timeList2 = new ArrayList<>();
            this.mNormalLaunchTimeMap.put(Integer.valueOf(uid), timeList2);
            return timeList2;
        }

        private ArrayList<Long> getMemInfoList(int uid) {
            ArrayList<Long> memInfoList = this.mMemInfoMap.get(Integer.valueOf(uid));
            if (memInfoList != null) {
                return memInfoList;
            }
            ArrayList<Long> memInfoList2 = new ArrayList<>();
            this.mMemInfoMap.put(Integer.valueOf(uid), memInfoList2);
            return memInfoList2;
        }

        public void sendDataToDCS() {
            synchronized (this.mPreloadLock) {
                if (ColorResourcePreloadManager.this.mChinaModel) {
                    List<Map<String, String>> uploadList = new ArrayList<>();
                    for (int i = 0; i < this.mPreloadInfoMap.size(); i++) {
                        PreloadInfo info = this.mPreloadInfoMap.valueAt(i);
                        if (info.getUid() != -1) {
                            if (info.getPreloadLaunchTime() != 0) {
                                updatePreloadClientTime(info, System.currentTimeMillis());
                                Map<String, String> eventMap = new HashMap<>();
                                eventMap.put(KEY_REASON_BOOT, String.valueOf(info.getReasonBootCount()));
                                eventMap.put(KEY_REASON_AI, String.valueOf(info.getReasonAICount()));
                                eventMap.put(KEY_REASON_HOTNESS, String.valueOf(info.getHotnessCnt()));
                                eventMap.put(KEY_USERID, String.valueOf(UserHandle.getUserId(info.getUid())));
                                eventMap.put(KEY_PKGNAME, info.getPkgName());
                                eventMap.put(KEY_LAUNCHTIME, String.valueOf(info.getPreloadLaunchTime()));
                                eventMap.put(KEY_NORMAL_LAUNCHTIME, String.valueOf(info.getNormalLaunchTime()));
                                eventMap.put(KEY_TOTALCOUNT, String.valueOf(info.getTotalCount()));
                                eventMap.put(KEY_HITCOUNT, String.valueOf(info.getHitCount()));
                                eventMap.put(KEY_CLIENTTIME, ColorResourcePreloadManager.this.formatDateTime(info.getClientTime()));
                                uploadList.add(eventMap);
                            }
                        }
                    }
                    OppoStatistics.onCommon(ColorResourcePreloadManager.this.mContext, PRELOAD_LOG_TAG, "preload", uploadList, false);
                }
                this.mPreloadInfoMap.clear();
            }
            uploadPreloadInfo();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sendPreloadInfoToDB() {
            PreloadInfoCollector preloadInfoCollector = this;
            ColorResourcePreloadDBManager dbManager = ColorResourcePreloadDBManager.getInstance();
            if (dbManager != null && dbManager.isDBOpen()) {
                updatePreloadLaunchTime();
                updateNormalLaunchTime();
                calculateAverageMemInfo();
                synchronized (ColorResourcePreloadDBManager.mDBLock) {
                    dbManager.beginTransaction();
                    try {
                        synchronized (preloadInfoCollector.mPreloadLock) {
                            int i = 0;
                            while (i < preloadInfoCollector.mPreloadInfoMap.size()) {
                                PreloadInfo info = preloadInfoCollector.mPreloadInfoMap.valueAt(i);
                                String pkgName = info.getPkgName();
                                int uid = info.getUid();
                                if (uid != -1) {
                                    String name = "";
                                    int pkg_uid = -1;
                                    int hotnessCnt = 0;
                                    int bootCnt = 0;
                                    int aiCnt = 0;
                                    int totalCnt = 0;
                                    int hitCnt = 0;
                                    long normalLaunchTime = 0;
                                    boolean isFound = false;
                                    Cursor cursor = dbManager.fetch("preload", pkgName, uid);
                                    if (cursor != null) {
                                        while (true) {
                                            if (!cursor.moveToNext()) {
                                                break;
                                            }
                                            name = cursor.getString(1);
                                            pkg_uid = cursor.getInt(2);
                                            int hotnessCnt2 = cursor.getInt(3);
                                            bootCnt = cursor.getInt(4);
                                            aiCnt = cursor.getInt(5);
                                            totalCnt = cursor.getInt(6);
                                            hitCnt = cursor.getInt(7);
                                            cursor.getLong(8);
                                            normalLaunchTime = cursor.getLong(9);
                                            if (name.equals(pkgName) && pkg_uid == uid) {
                                                isFound = true;
                                                hotnessCnt = hotnessCnt2;
                                                break;
                                            }
                                            hotnessCnt = hotnessCnt2;
                                        }
                                        cursor.close();
                                    }
                                    ContentValues values = new ContentValues();
                                    values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NAME, info.getPkgName());
                                    values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, Integer.valueOf(info.getUid()));
                                    values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_HOTNESS_COUNT, Long.valueOf(info.getHotnessCnt()));
                                    values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_BOOT_COUNT, Long.valueOf(info.getReasonBootCount() + ((long) bootCnt)));
                                    values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_AI_COUNT, Long.valueOf(info.getReasonAICount() + ((long) aiCnt)));
                                    values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_TOTAL_COUNT, Long.valueOf(info.getTotalCount() + ((long) totalCnt)));
                                    values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_HIT_COUNT, Long.valueOf(info.getHitCount() + ((long) hitCnt)));
                                    values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_PRELOAD_LAUNCH_TIME, Long.valueOf(info.getPreloadLaunchTime()));
                                    if (normalLaunchTime == 0) {
                                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NORMAL_LAUNCH_TIME, Long.valueOf(info.getNormalLaunchTime()));
                                    } else if (info.getNormalLaunchTime() > 0) {
                                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NORMAL_LAUNCH_TIME, Long.valueOf(info.getNormalLaunchTime()));
                                    }
                                    values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_MEMORY, Long.valueOf(info.getMemoryInfo()));
                                    values.put("version", info.getAppVersion());
                                    if (isFound) {
                                        dbManager.update("preload", values, info.getPkgName(), info.getUid());
                                    } else {
                                        dbManager.insert("preload", values);
                                    }
                                }
                                i++;
                                preloadInfoCollector = this;
                            }
                        }
                        dbManager.setTransactionSuccessful();
                    } catch (Exception e) {
                        Log.e(ColorResourcePreloadManager.TAG, e.toString());
                    } finally {
                        dbManager.endTransaction();
                    }
                }
            }
        }

        public void uploadPreloadInfo() {
            ColorResourcePreloadDBManager.getInstance().getDBHandler().postDelayed(new Runnable() {
                /* class com.android.server.am.$$Lambda$ColorResourcePreloadManager$PreloadInfoCollector$jIWpq41tzx9A9bha0QPYsPZvPz0 */

                public final void run() {
                    ColorResourcePreloadManager.PreloadInfoCollector.this.lambda$uploadPreloadInfo$0$ColorResourcePreloadManager$PreloadInfoCollector();
                }
            }, ColorDeviceIdleHelper.DEFAULT_TOTAL_INTERVAL_TO_IDLE);
        }

        public /* synthetic */ void lambda$uploadPreloadInfo$0$ColorResourcePreloadManager$PreloadInfoCollector() {
            sendPreloadInfoToDB();
            sendDataToDCS();
        }

        public void dumpPreloadInfo() {
            synchronized (this.mPreloadLock) {
                sendPreloadInfoToDB();
                for (int i = 0; i < this.mPreloadInfoMap.size(); i++) {
                    PreloadHistoryLog preloadHistoryLog = ColorResourcePreloadManager.this.mPreloadHistoryLog;
                    String str = ColorResourcePreloadManager.TAG;
                    preloadHistoryLog.i(str, i + ": " + this.mPreloadInfoMap.valueAt(i).toString());
                }
            }
        }

        /* access modifiers changed from: package-private */
        public class PreloadInfo {
            private String appVersion = "";
            private long clientTime = 0;
            private long hitCount = 0;
            private long hotnessCnt = 0;
            private long memoryInfo = 0;
            private long normalLaunchTime = 0;
            private String pkgName = "";
            private long preloadLaunchTime = 0;
            private long reasonAICount = 0;
            private long reasonBootCount = 0;
            private long totalCount = 0;
            private int uid = -1;

            public PreloadInfo() {
            }

            public String getPkgName() {
                return this.pkgName;
            }

            public void setPkgName(String pkgName2) {
                this.pkgName = pkgName2;
            }

            public int getUid() {
                return this.uid;
            }

            public void setUid(int uid2) {
                this.uid = uid2;
            }

            public long getHotnessCnt() {
                return this.hotnessCnt;
            }

            public void setHotnessCnt(long hotnessCnt2) {
                this.hotnessCnt = hotnessCnt2;
            }

            public long getReasonBootCount() {
                return this.reasonBootCount;
            }

            public void setReasonBootCount(long reasonBootCount2) {
                this.reasonBootCount = reasonBootCount2;
            }

            public long getReasonAICount() {
                return this.reasonAICount;
            }

            public void setReasonAICount(long reasonAICount2) {
                this.reasonAICount = reasonAICount2;
            }

            public long getTotalCount() {
                return this.totalCount;
            }

            public void setTotalCount(long totalCount2) {
                this.totalCount = totalCount2;
            }

            public long getHitCount() {
                return this.hitCount;
            }

            public void setHitCount(long hitCount2) {
                this.hitCount = hitCount2;
            }

            public long getPreloadLaunchTime() {
                return this.preloadLaunchTime;
            }

            public void setPreloadLaunchTime(long launchTime) {
                this.preloadLaunchTime = launchTime;
            }

            public long getNormalLaunchTime() {
                return this.normalLaunchTime;
            }

            public void setNormalLaunchTime(long launchTime) {
                this.normalLaunchTime = launchTime;
            }

            public long getClientTime() {
                return this.clientTime;
            }

            public void setClientTime(long clientTime2) {
                this.clientTime = clientTime2;
            }

            public long getMemoryInfo() {
                return this.memoryInfo;
            }

            public void setMemoryInfo(long memoryInfo2) {
                this.memoryInfo = memoryInfo2;
            }

            public String getAppVersion() {
                return this.appVersion;
            }

            public void setAppVersion(String appVersion2) {
                this.appVersion = appVersion2;
            }

            public String toString() {
                return "PreloadInfo{pkgName='" + this.pkgName + "', uid=" + this.uid + ", hotnessCnt=" + this.hotnessCnt + ", reasonBootCount=" + this.reasonBootCount + ", reasonAICount=" + this.reasonAICount + ", totalCount=" + this.totalCount + ", hitCount=" + this.hitCount + ", preloadLaunchTime=" + this.preloadLaunchTime + ", normalLaunchTime=" + this.normalLaunchTime + ", clientTime=" + this.clientTime + ", memoryInfo=" + this.memoryInfo + ", appVersion='" + this.appVersion + "'}";
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class RestrictionInfo {
        private String mAction = "";
        private String mCalleePkg = "";
        private String mCallerPkg = "";
        private String mCpnName = "";

        public RestrictionInfo(String callerPkg, String calleePkg, String cpnName, String action) {
            this.mCallerPkg = callerPkg;
            this.mCalleePkg = calleePkg;
            this.mCpnName = cpnName;
            this.mAction = action;
        }

        public String getCallerPkg() {
            return this.mCallerPkg;
        }

        public String getCalleePkg() {
            return this.mCalleePkg;
        }

        public String getCpnName() {
            return this.mCpnName;
        }

        public String getAction() {
            return this.mAction;
        }
    }

    /* access modifiers changed from: package-private */
    public class PreloadRUSController {
        private static final String ACTION_ROM_UPDATE_CONFIG = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
        private static final String ATTR_AIPRELOAD_SWITCH = "aipreload_switch";
        private static final String ATTR_BOOTPRELOAD_SWITCH = "bootpreload_switch";
        private static final String ATTR_DEBUGLOG_SWITCH = "debuglog_switch";
        private static final String ATTR_GAMEPRELOAD_SWITCH = "gamepreload_switch";
        private static final String ATTR_MEM12_AI_SLOT = "mem12_ai_slot";
        private static final String ATTR_MEM12_BOOT_SLOT = "mem12_boot_slot";
        private static final String ATTR_MEM12_LOW_MEM = "mem12_lowmem";
        private static final String ATTR_MEM6_AI_SLOT = "mem6_ai_slot";
        private static final String ATTR_MEM6_BOOT_SLOT = "mem6_boot_slot";
        private static final String ATTR_MEM6_LOW_MEM = "mem6_lowmem";
        private static final String ATTR_MEM8_AI_SLOT = "mem8_ai_slot";
        private static final String ATTR_MEM8_BOOT_SLOT = "mem8_boot_slot";
        private static final String ATTR_MEM8_LOW_MEM = "mem8_lowmem";
        private static final String COLUMN_NAME_VERSION = "version";
        private static final String COLUMN_NAME_XML = "xml";
        private static final String OPPO_COMPONENT_SAFE_PERMISSION = "oppo.permission.OPPO_COMPONENT_SAFE";
        private static final String PKG_ALARM_RESTRICTION = "alarm_restriction";
        private static final String PKG_BROADCAST_RESTRICTION = "broadcast_restriction";
        private static final String PKG_JOB_RESTRICTION = "job_restriction";
        private static final String PKG_PRELOAD_BOOTUP_LIST = "preload_bootuplist";
        private static final String PKG_PRELOAD_WHITELIST = "preload_whitelist";
        private static final String PKG_PROVIDER_RESTRICTION = "provider_restriction";
        private static final String PKG_SERVICE_RESTRICTION = "service_restriction";
        private static final String PKG_SYNC_RESTRICTION = "sync_restriction";
        private static final String PRELOAD_CONFIG_FILE_NAME = "sys_preload_config_list";
        private static final String PRELOAD_CONFIG_FILE_PATH = "/data/system/sys_preload_config_list.xml";
        private static final String ROM_UPDATE_CONFIG_LIST = "ROM_UPDATE_CONFIG_LIST";
        private static final String TAG_ATTRIBUTE = "attr";
        private static final String TAG_PKG = "pkg";
        private static final String TAG_VERSION = "version";
        private final Uri ROM_UPDATE_URI = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
        private int aiSlot_Mem12 = 5;
        private int aiSlot_Mem6 = 2;
        private int aiSlot_Mem8 = 3;
        private int bootSlot_Mem12 = 5;
        private int bootSlot_Mem6 = 2;
        private int bootSlot_Mem8 = 3;
        private int lowMem_Mem12 = 2048;
        private int lowMem_Mem6 = OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE;
        private int lowMem_Mem8 = 2048;
        private boolean mAISwitch = false;
        private boolean mBootSwitch = false;
        private ArrayList<String> mBootupList = new ArrayList<>();
        private boolean mDebugLog = false;
        private boolean mGameSwitch = false;
        private Object mRUSLock = new Object();
        private ArrayMap<String, ArrayList<RestrictionInfo>> mRestrictionInfoListMap = new ArrayMap<>();
        BroadcastReceiver mRomUpdateReceiver = new BroadcastReceiver() {
            /* class com.android.server.am.ColorResourcePreloadManager.PreloadRUSController.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (PreloadRUSController.ACTION_ROM_UPDATE_CONFIG.equals(intent.getAction())) {
                    ArrayList<String> list = intent.getStringArrayListExtra(PreloadRUSController.ROM_UPDATE_CONFIG_LIST);
                    if (list == null || list.isEmpty()) {
                        Log.i(ColorResourcePreloadManager.TAG, "get the rom update list is null");
                    } else if (list.contains(PreloadRUSController.PRELOAD_CONFIG_FILE_NAME)) {
                        PreloadRUSController.this.loadRomUpdateConfigXML();
                    }
                }
            }
        };
        private int mVersion = 0;
        private ArrayMap<String, ArrayList<String>> mWhiteListMap = new ArrayMap<>();

        public PreloadRUSController() {
            if (!initConfigValuesFromFile()) {
                initDefaultConfigValues();
            }
            registerRomUpdateBroadcast();
        }

        private void initDefaultConfigValues() {
            synchronized (this.mRUSLock) {
                this.mAISwitch = true;
                this.mBootSwitch = false;
                this.mGameSwitch = false;
                this.mDebugLog = true;
                this.aiSlot_Mem6 = 2;
                this.bootSlot_Mem6 = 2;
                this.lowMem_Mem6 = OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE;
                this.aiSlot_Mem8 = 3;
                this.bootSlot_Mem8 = 3;
                this.lowMem_Mem8 = 2048;
                this.aiSlot_Mem12 = 5;
                this.bootSlot_Mem12 = 5;
                this.lowMem_Mem12 = 2048;
                this.mBootupList.clear();
                this.mBootupList.addAll(Arrays.asList(ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.tencent.mobileqq", "com.sina.weibo", "com.eg.android.AlipayGphone", "com.taobao.taobao", "com.tmall.wireless", "com.jingdong.app.mall", "com.sankuai.meituan", "com.baidu.BaiduMap", "com.autonavi.minimap", "com.mobike.mobikeapp", "com.jingyao.easybike", "com.youku.phone", "com.tencent.qqlive", "com.netease.cloudmusic", "com.zhihu.android", "com.MobileTicket", "com.ss.android.ugc.aweme", "tv.danmaku.bili", "com.tencent.tmgp.sgame"));
                ArrayList<String> whitelist = getWhiteList(PKG_PRELOAD_WHITELIST);
                if (whitelist != null) {
                    whitelist.add("com.hogezq.azsjgj");
                    whitelist.add("com.tencent.android.qqdownloader");
                    whitelist.add("com.abchina.ebizbtob");
                    whitelist.add("com.yitantech.gaigai");
                    this.mWhiteListMap.put(PKG_PRELOAD_WHITELIST, whitelist);
                }
            }
        }

        private void registerRomUpdateBroadcast() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_ROM_UPDATE_CONFIG);
            ColorResourcePreloadManager.this.mContext.registerReceiver(this.mRomUpdateReceiver, intentFilter, "oppo.permission.OPPO_COMPONENT_SAFE", ColorResourcePreloadManager.this.mMainHandler);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x004c, code lost:
            if (r0 != null) goto L_0x005f;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x005d, code lost:
            if (0 == 0) goto L_0x0063;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x005f, code lost:
            r0.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0063, code lost:
            if (r10 == 0) goto L_0x00bc;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0065, code lost:
            if (r9 != null) goto L_0x0068;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0068, code lost:
            r1 = false;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
            r1 = parseContentFromXML(r9);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x006e, code lost:
            if (r1 == false) goto L_0x008a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0070, code lost:
            r11.mBootupList.clear();
            r11.mBootupList.addAll(getPreloadBootupList(com.android.server.am.ColorResourcePreloadManager.PreloadRUSController.PKG_PRELOAD_BOOTUP_LIST));
            android.util.Log.i(com.android.server.am.ColorResourcePreloadManager.TAG, "rom update config success!");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x008a, code lost:
            android.util.Log.i(com.android.server.am.ColorResourcePreloadManager.TAG, "rom update config failed!");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0095, code lost:
            android.util.Log.e(com.android.server.am.ColorResourcePreloadManager.TAG, "parsing the xml content error!");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x00bc, code lost:
            android.util.Log.i(com.android.server.am.ColorResourcePreloadManager.TAG, "get the xml content is wrong!!!");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00c5, code lost:
            return;
         */
        private void loadRomUpdateConfigXML() {
            boolean ret;
            Cursor cursor = null;
            String[] projection = {"version", COLUMN_NAME_XML};
            String xml = null;
            int version = 0;
            try {
                if (ColorResourcePreloadManager.this.mContext != null) {
                    cursor = ColorResourcePreloadManager.this.mContext.getContentResolver().query(this.ROM_UPDATE_URI, projection, "filtername=\"sys_preload_config_list\"", null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        int versioncolumnIndex = cursor.getColumnIndex("version");
                        int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_XML);
                        cursor.moveToNext();
                        version = cursor.getInt(versioncolumnIndex);
                        xml = cursor.getString(xmlcolumnIndex);
                    }
                } else if (0 != 0) {
                    cursor.close();
                    return;
                } else {
                    return;
                }
            } catch (Exception e) {
                Log.e(ColorResourcePreloadManager.TAG, "get the update config from database fail!");
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
            if (ret) {
                if (saveToFile(xml, PRELOAD_CONFIG_FILE_PATH)) {
                    Log.i(ColorResourcePreloadManager.TAG, "save config xml success!");
                }
                ColorResourcePreloadManager.this.updatePreloadConfigValues();
                ColorResourcePreloadManager.this.setLowAvailMemThreshold();
            }
        }

        private boolean saveToFile(String content, String filePath) {
            if (isStrEmpty(content)) {
                return false;
            }
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(new File(filePath));
                fileOutputStream.write(content.getBytes());
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e(ColorResourcePreloadManager.TAG, "IOException: ", e);
                }
                return true;
            } catch (Exception e2) {
                Log.e(ColorResourcePreloadManager.TAG, "Exception: ", e2);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e3) {
                        Log.e(ColorResourcePreloadManager.TAG, "IOException: ", e3);
                    }
                }
                return false;
            } catch (Throwable th) {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e4) {
                        Log.e(ColorResourcePreloadManager.TAG, "IOException: ", e4);
                    }
                }
                throw th;
            }
        }

        public boolean isAIPreloadEnable() {
            boolean z;
            synchronized (this.mRUSLock) {
                z = this.mAISwitch;
            }
            return z;
        }

        public boolean isBootPreloadEnable() {
            boolean z;
            synchronized (this.mRUSLock) {
                z = this.mBootSwitch;
            }
            return z;
        }

        public boolean isGamePreloadEnable() {
            boolean z;
            synchronized (this.mRUSLock) {
                z = this.mGameSwitch;
            }
            return z;
        }

        public boolean isDebugLogEnable() {
            boolean z;
            synchronized (this.mRUSLock) {
                z = this.mDebugLog;
            }
            return z;
        }

        public int getAiSlot_Mem6() {
            int i;
            synchronized (this.mRUSLock) {
                i = this.aiSlot_Mem6;
            }
            return i;
        }

        public int getBootSlot_Mem6() {
            int i;
            synchronized (this.mRUSLock) {
                i = this.bootSlot_Mem6;
            }
            return i;
        }

        public int getLowMem_Mem6() {
            int i;
            synchronized (this.mRUSLock) {
                i = this.lowMem_Mem6;
            }
            return i;
        }

        public int getAiSlot_Mem8() {
            int i;
            synchronized (this.mRUSLock) {
                i = this.aiSlot_Mem8;
            }
            return i;
        }

        public int getBootSlot_Mem8() {
            int i;
            synchronized (this.mRUSLock) {
                i = this.bootSlot_Mem8;
            }
            return i;
        }

        public int getLowMem_Mem8() {
            int i;
            synchronized (this.mRUSLock) {
                i = this.lowMem_Mem8;
            }
            return i;
        }

        public int getAiSlot_Mem12() {
            int i;
            synchronized (this.mRUSLock) {
                i = this.aiSlot_Mem12;
            }
            return i;
        }

        public int getBootSlot_Mem12() {
            int i;
            synchronized (this.mRUSLock) {
                i = this.bootSlot_Mem12;
            }
            return i;
        }

        public int getLowMem_Mem12() {
            int i;
            synchronized (this.mRUSLock) {
                i = this.lowMem_Mem12;
            }
            return i;
        }

        public boolean isPreloadWhiteList(String Key, String pkgName) {
            boolean ret = false;
            synchronized (this.mRUSLock) {
                ArrayList<String> whiteList = this.mWhiteListMap.get(Key);
                if (whiteList != null && whiteList.contains(pkgName)) {
                    ret = true;
                }
            }
            return ret;
        }

        public boolean isPreloadRestrictWhiteList(String key, String callerPackage, String calleePackage, String cpnName, String action) {
            if (isStrEmpty(key)) {
                return false;
            }
            synchronized (this.mRUSLock) {
                ArrayList<RestrictionInfo> restrictionInfoList = this.mRestrictionInfoListMap.get(key);
                if (restrictionInfoList != null) {
                    if (restrictionInfoList.size() != 0) {
                        for (int index = 0; index < restrictionInfoList.size(); index++) {
                            RestrictionInfo tempInfo = restrictionInfoList.get(index);
                            if (tempInfo != null && (isStrEmpty(tempInfo.mCalleePkg) || calleePackage.equals(tempInfo.mCalleePkg))) {
                                String config_callerPackage = tempInfo.getCallerPkg();
                                String config_cpnName = tempInfo.getCpnName();
                                String config_action = tempInfo.getAction();
                                if (!key.equals(PKG_SERVICE_RESTRICTION)) {
                                    if (!key.equals(PKG_PROVIDER_RESTRICTION)) {
                                        if (key.equals(PKG_BROADCAST_RESTRICTION)) {
                                            if ((isStrEmpty(config_action) || action.equals(config_action)) && (isStrEmpty(config_callerPackage) || callerPackage.equals(config_callerPackage))) {
                                                return true;
                                            }
                                        } else if ((key.equals(PKG_SYNC_RESTRICTION) || key.equals(PKG_JOB_RESTRICTION) || key.equals(PKG_ALARM_RESTRICTION)) && (isStrEmpty(config_callerPackage) || callerPackage.equals(config_callerPackage))) {
                                            return true;
                                        }
                                    }
                                }
                                if ((isStrEmpty(config_cpnName) || cpnName.equals(config_cpnName)) && (isStrEmpty(config_callerPackage) || callerPackage.equals(config_callerPackage))) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                }
                return false;
            }
        }

        public ArrayList<String> getPreloadBootupList(String key) {
            ArrayList<String> bootupList;
            synchronized (this.mRUSLock) {
                bootupList = this.mWhiteListMap.get(key);
            }
            return bootupList;
        }

        public ArrayList<String> getBootupList() {
            return this.mBootupList;
        }

        private boolean initConfigValuesFromFile() {
            File file = new File(PRELOAD_CONFIG_FILE_PATH);
            if (!file.exists()) {
                return false;
            }
            Log.i(ColorResourcePreloadManager.TAG, "init RUS config values from xml file!");
            return parseContentFromXML(readFromFile(file));
        }

        private String readFromFile(File file) {
            if (file == null || !file.exists()) {
                return null;
            }
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                InputStream inputStream2 = new FileInputStream(file);
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
                StringBuffer buffer = new StringBuffer();
                while (true) {
                    String line = bufferedReader2.readLine();
                    if (line == null) {
                        break;
                    }
                    buffer.append(line + "\n");
                }
                String stringBuffer = buffer.toString();
                try {
                    inputStream2.close();
                    bufferedReader2.close();
                } catch (IOException e) {
                    Log.e(ColorResourcePreloadManager.TAG, "IOException: ", e);
                }
                return stringBuffer;
            } catch (FileNotFoundException e2) {
                Log.e(ColorResourcePreloadManager.TAG, "FileNotFoundException: ", e2);
                if (0 != 0) {
                    inputStream.close();
                }
                if (0 != 0) {
                    bufferedReader.close();
                }
            } catch (IOException e3) {
                Log.e(ColorResourcePreloadManager.TAG, "IOException: ", e3);
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                        Log.e(ColorResourcePreloadManager.TAG, "IOException: ", e4);
                        return null;
                    }
                }
                if (0 != 0) {
                    bufferedReader.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        Log.e(ColorResourcePreloadManager.TAG, "IOException: ", e5);
                        throw th;
                    }
                }
                if (0 != 0) {
                    bufferedReader.close();
                }
                throw th;
            }
        }

        private boolean isStrEmpty(String s) {
            return s == null || s.length() == 0;
        }

        /* JADX WARN: Type inference failed for: r3v0 */
        /* JADX WARN: Type inference failed for: r3v2, types: [int, boolean] */
        /* JADX WARN: Type inference failed for: r3v3 */
        /* JADX WARNING: Code restructure failed: missing block: B:233:0x031a, code lost:
            if (r15.equals(com.android.server.am.ColorResourcePreloadManager.PreloadRUSController.PKG_JOB_RESTRICTION) != false) goto L_0x0336;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:278:0x040c, code lost:
            r9 = r9;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:324:0x046d, code lost:
            continue;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:325:0x046d, code lost:
            continue;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x00e3, code lost:
            if (r20.equals(com.android.server.am.ColorResourcePreloadManager.PreloadRUSController.ATTR_GAMEPRELOAD_SWITCH) != false) goto L_0x0151;
         */
        /* JADX WARNING: Unknown variable types count: 1 */
        public boolean parseContentFromXML(String content) {
            Object obj;
            XmlPullParserFactory factory;
            ArrayList<String> arrayList;
            String attributeValue;
            Throwable th;
            String callerPkg;
            String str;
            String attributeValue2;
            String attributeValue3;
            ?? r3 = 0;
            if (content == null) {
                Log.i(ColorResourcePreloadManager.TAG, "parse content is null!");
                return false;
            }
            this.mRestrictionInfoListMap.clear();
            StringReader stringReader = null;
            try {
                XmlPullParserFactory factory2 = XmlPullParserFactory.newInstance();
                int i = 1;
                factory2.setNamespaceAware(true);
                XmlPullParser parser = factory2.newPullParser();
                StringReader stringReader2 = new StringReader(content);
                parser.setInput(stringReader2);
                parser.nextTag();
                int eventType = parser.getEventType();
                Object obj2 = null;
                ArrayList<String> whitelist = null;
                while (eventType != i) {
                    if (eventType != 0) {
                        int i2 = 2;
                        if (eventType != 2) {
                            factory = factory2;
                            obj = obj2;
                            arrayList = whitelist;
                        } else {
                            String tagName = parser.getName();
                            if (tagName.equals("version")) {
                                int version = Integer.parseInt(parser.nextText());
                                if (this.mVersion > version) {
                                    Log.i(ColorResourcePreloadManager.TAG, "config xml version is old, no need to update!");
                                    stringReader2.close();
                                    return r3;
                                }
                                this.mVersion = version;
                                factory = factory2;
                                obj = obj2;
                                arrayList = whitelist;
                            } else {
                                char c = 6;
                                if (tagName.equals(TAG_ATTRIBUTE)) {
                                    if (parser.getAttributeCount() > 0) {
                                        String attributeValue4 = parser.getAttributeValue(r3);
                                        switch (attributeValue4.hashCode()) {
                                            case -2126534878:
                                                if (attributeValue4.equals(ATTR_DEBUGLOG_SWITCH)) {
                                                    i2 = 3;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case -1627645321:
                                                if (attributeValue4.equals(ATTR_MEM6_AI_SLOT)) {
                                                    i2 = 4;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case -1448179486:
                                                if (attributeValue4.equals(ATTR_MEM12_BOOT_SLOT)) {
                                                    i2 = 11;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case -1169865316:
                                                if (attributeValue4.equals(ATTR_BOOTPRELOAD_SWITCH)) {
                                                    i2 = i;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case -947586951:
                                                if (attributeValue4.equals(ATTR_MEM8_AI_SLOT)) {
                                                    i2 = 7;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case 13508797:
                                                if (attributeValue4.equals(ATTR_MEM8_LOW_MEM)) {
                                                    i2 = 9;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case 68553516:
                                                if (attributeValue4.equals(ATTR_MEM12_AI_SLOT)) {
                                                    i2 = 10;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case 323382186:
                                                if (attributeValue4.equals(ATTR_MEM12_LOW_MEM)) {
                                                    i2 = 12;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case 550868636:
                                                break;
                                            case 592310637:
                                                if (attributeValue4.equals(ATTR_MEM6_BOOT_SLOT)) {
                                                    i2 = 5;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case 822855423:
                                                attributeValue3 = attributeValue4;
                                                if (attributeValue3.equals(ATTR_MEM6_LOW_MEM)) {
                                                    i2 = 6;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case 1293375215:
                                                attributeValue3 = attributeValue4;
                                                if (attributeValue3.equals(ATTR_MEM8_BOOT_SLOT)) {
                                                    i2 = 8;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            case 1314766930:
                                                attributeValue3 = attributeValue4;
                                                if (attributeValue3.equals(ATTR_AIPRELOAD_SWITCH)) {
                                                    i2 = r3;
                                                    break;
                                                }
                                                i2 = -1;
                                                break;
                                            default:
                                                i2 = -1;
                                                break;
                                        }
                                        switch (i2) {
                                            case 0:
                                                String aiSwitch = parser.nextText();
                                                if (!isStrEmpty(aiSwitch)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.mAISwitch = Boolean.parseBoolean(aiSwitch);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case 1:
                                                String bootSwitch = parser.nextText();
                                                if (!isStrEmpty(bootSwitch)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.mBootSwitch = Boolean.parseBoolean(bootSwitch);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case 2:
                                                String gameSwitch = parser.nextText();
                                                if (!isStrEmpty(gameSwitch)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.mGameSwitch = Boolean.parseBoolean(gameSwitch);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case 3:
                                                String logSwitch = parser.nextText();
                                                if (!isStrEmpty(logSwitch)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.mDebugLog = Boolean.parseBoolean(logSwitch);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case 4:
                                                String aiSlotMem6 = parser.nextText();
                                                if (!isStrEmpty(aiSlotMem6)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.aiSlot_Mem6 = Integer.parseInt(aiSlotMem6);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case 5:
                                                String bootSlotMem6 = parser.nextText();
                                                if (!isStrEmpty(bootSlotMem6)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.bootSlot_Mem6 = Integer.parseInt(bootSlotMem6);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case 6:
                                                String lowMem6 = parser.nextText();
                                                if (!isStrEmpty(lowMem6)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.lowMem_Mem6 = Integer.parseInt(lowMem6);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case ColorStartingWindowRUSHelper.TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER /* 7 */:
                                                String aiSlotMem8 = parser.nextText();
                                                if (!isStrEmpty(aiSlotMem8)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.aiSlot_Mem8 = Integer.parseInt(aiSlotMem8);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case 8:
                                                String bootSlotMem8 = parser.nextText();
                                                if (!isStrEmpty(bootSlotMem8)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.bootSlot_Mem8 = Integer.parseInt(bootSlotMem8);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case ColorStartingWindowRUSHelper.FORCE_USE_COLOR_DRAWABLE_WHEN_SPLASH_WINDOW_TRANSLUCENT /* 9 */:
                                                String lowMem8 = parser.nextText();
                                                if (!isStrEmpty(lowMem8)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.lowMem_Mem8 = Integer.parseInt(lowMem8);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case ColorStartingWindowRUSHelper.STARTING_WINDOW_EXIT_LONG_DURATION_PACKAGE /* 10 */:
                                                String aiSlotMem12 = parser.nextText();
                                                if (!isStrEmpty(aiSlotMem12)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.aiSlot_Mem12 = Integer.parseInt(aiSlotMem12);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case 11:
                                                String bootSlotMem12 = parser.nextText();
                                                if (!isStrEmpty(bootSlotMem12)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.bootSlot_Mem12 = Integer.parseInt(bootSlotMem12);
                                                    }
                                                    break;
                                                }
                                                break;
                                            case ColorStartingWindowRUSHelper.USE_TRANSLUCENT_DRAWABLE_FOR_SPLASH_WINDOW /* 12 */:
                                                String lowMem12 = parser.nextText();
                                                if (!isStrEmpty(lowMem12)) {
                                                    synchronized (this.mRUSLock) {
                                                        this.lowMem_Mem12 = Integer.parseInt(lowMem12);
                                                    }
                                                    break;
                                                }
                                                break;
                                        }
                                    }
                                    factory = factory2;
                                    obj = obj2;
                                    arrayList = whitelist;
                                } else if (!tagName.equals(TAG_PKG)) {
                                    factory = factory2;
                                    obj = obj2;
                                    arrayList = whitelist;
                                } else if (parser.getAttributeCount() > 0) {
                                    int i3 = r3 == true ? 1 : 0;
                                    int i4 = r3 == true ? 1 : 0;
                                    int i5 = r3 == true ? 1 : 0;
                                    String attributeValue5 = parser.getAttributeValue(i3);
                                    switch (attributeValue5.hashCode()) {
                                        case -1793819608:
                                            attributeValue = attributeValue5;
                                            if (attributeValue.equals(PKG_SYNC_RESTRICTION)) {
                                                c = 5;
                                                break;
                                            }
                                            c = 65535;
                                            break;
                                        case -1588670142:
                                            attributeValue2 = attributeValue5;
                                            if (attributeValue2.equals(PKG_SERVICE_RESTRICTION)) {
                                                c = 2;
                                                attributeValue = attributeValue2;
                                                break;
                                            }
                                            attributeValue = attributeValue2;
                                            c = 65535;
                                            break;
                                        case -981592450:
                                            attributeValue2 = attributeValue5;
                                            if (attributeValue2.equals(PKG_PROVIDER_RESTRICTION)) {
                                                attributeValue = attributeValue2;
                                                c = 4;
                                                break;
                                            }
                                            attributeValue = attributeValue2;
                                            c = 65535;
                                            break;
                                        case -267560374:
                                            attributeValue2 = attributeValue5;
                                            break;
                                        case 40546081:
                                            attributeValue2 = attributeValue5;
                                            if (attributeValue2.equals(PKG_PRELOAD_BOOTUP_LIST)) {
                                                attributeValue = attributeValue2;
                                                c = 1;
                                                break;
                                            }
                                            attributeValue = attributeValue2;
                                            c = 65535;
                                            break;
                                        case 1032483217:
                                            attributeValue2 = attributeValue5;
                                            if (attributeValue2.equals(PKG_PRELOAD_WHITELIST)) {
                                                char c2 = r3 == true ? 1 : 0;
                                                Object[] objArr = r3 == true ? 1 : 0;
                                                Object[] objArr2 = r3 == true ? 1 : 0;
                                                c = c2;
                                                attributeValue = attributeValue2;
                                                break;
                                            }
                                            attributeValue = attributeValue2;
                                            c = 65535;
                                            break;
                                        case 1719139038:
                                            attributeValue2 = attributeValue5;
                                            if (attributeValue2.equals(PKG_ALARM_RESTRICTION)) {
                                                attributeValue = attributeValue2;
                                                c = 7;
                                                break;
                                            }
                                            attributeValue = attributeValue2;
                                            c = 65535;
                                            break;
                                        case 1921925454:
                                            attributeValue2 = attributeValue5;
                                            if (attributeValue2.equals(PKG_BROADCAST_RESTRICTION)) {
                                                attributeValue = attributeValue2;
                                                c = 3;
                                                break;
                                            }
                                            attributeValue = attributeValue2;
                                            c = 65535;
                                            break;
                                        default:
                                            attributeValue = attributeValue5;
                                            c = 65535;
                                            break;
                                    }
                                    switch (c) {
                                        case 0:
                                        case 1:
                                            factory = factory2;
                                            obj = obj2;
                                            String pkgName = parser.nextText();
                                            synchronized (this.mRUSLock) {
                                                try {
                                                    whitelist = getWhiteList(attributeValue);
                                                    if (!whitelist.contains(pkgName)) {
                                                        whitelist.add(pkgName);
                                                        this.mWhiteListMap.put(attributeValue, whitelist);
                                                        break;
                                                    }
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    throw th;
                                                }
                                            }
                                            break;
                                        case 2:
                                        case 3:
                                        case 4:
                                        case 5:
                                        case 6:
                                        case ColorStartingWindowRUSHelper.TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER /* 7 */:
                                            String callerPkg2 = parser.getAttributeValue(null, "callerpkg");
                                            String calleePkg = parser.getAttributeValue(null, "calleepkg");
                                            String cpnName = parser.getAttributeValue(null, "cpnname");
                                            String action = parser.getAttributeValue(null, "action");
                                            if (isStrEmpty(callerPkg2)) {
                                                callerPkg = "";
                                            } else {
                                                callerPkg = callerPkg2;
                                            }
                                            if (isStrEmpty(calleePkg)) {
                                                calleePkg = "";
                                            }
                                            if (isStrEmpty(cpnName)) {
                                                cpnName = "";
                                            }
                                            if (isStrEmpty(action)) {
                                                action = "";
                                            }
                                            factory = factory2;
                                            synchronized (this.mRUSLock) {
                                                try {
                                                    PreloadHistoryLog preloadHistoryLog = ColorResourcePreloadManager.this.mPreloadHistoryLog;
                                                    try {
                                                        str = ColorResourcePreloadManager.TAG;
                                                        obj = obj2;
                                                    } catch (Throwable th3) {
                                                        th = th3;
                                                        throw th;
                                                    }
                                                    try {
                                                        StringBuilder sb = new StringBuilder();
                                                        sb.append("attributeValue: ");
                                                        sb.append(attributeValue);
                                                        sb.append("callerPkg:");
                                                        sb.append(callerPkg);
                                                        sb.append(", calleePkg:");
                                                        sb.append(calleePkg);
                                                        sb.append(", cpnName:");
                                                        sb.append(cpnName);
                                                        sb.append(", action:");
                                                        sb.append(action);
                                                        preloadHistoryLog.i(str, sb.toString());
                                                        RestrictionInfo restrictionInfo = new RestrictionInfo(callerPkg, calleePkg, cpnName, action);
                                                        ArrayList<RestrictionInfo> uid_restrictionInfo = this.mRestrictionInfoListMap.get(attributeValue);
                                                        if (uid_restrictionInfo == null) {
                                                            uid_restrictionInfo = new ArrayList<>();
                                                        }
                                                        uid_restrictionInfo.add(restrictionInfo);
                                                        this.mRestrictionInfoListMap.put(attributeValue, uid_restrictionInfo);
                                                        break;
                                                    } catch (Throwable th4) {
                                                        th = th4;
                                                        throw th;
                                                    }
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    throw th;
                                                }
                                            }
                                        default:
                                            factory = factory2;
                                            obj = obj2;
                                            arrayList = whitelist;
                                            break;
                                    }
                                } else {
                                    factory = factory2;
                                    obj = obj2;
                                    arrayList = whitelist;
                                }
                            }
                        }
                    } else {
                        factory = factory2;
                        obj = obj2;
                        arrayList = whitelist;
                    }
                    whitelist = arrayList;
                    eventType = parser.next();
                    factory2 = factory;
                    obj2 = obj;
                    i = 1;
                    r3 = 0;
                }
                stringReader2.close();
                return true;
            } catch (Exception e) {
                Log.e(ColorResourcePreloadManager.TAG, "parsing failed: ", e);
                if (0 == 0) {
                    return false;
                }
                stringReader.close();
                return false;
            } catch (Throwable th6) {
                if (0 != 0) {
                    stringReader.close();
                }
                throw th6;
            }
        }

        private ArrayList<String> getWhiteList(String key) {
            ArrayList whiteList = this.mWhiteListMap.get(key);
            if (whiteList != null) {
                return whiteList;
            }
            ArrayList whiteList2 = new ArrayList();
            this.mWhiteListMap.put(key, whiteList2);
            return whiteList2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePreloadConfigValues() {
        Log.i(TAG, "updatePreloadConfigValues");
        this.mBootPreloadSwitch = this.mPreloadRUSController.isBootPreloadEnable();
        this.mAIPreloadSwitch = this.mPreloadRUSController.isAIPreloadEnable();
        this.mGamePreloadSwitch = this.mPreloadRUSController.isGamePreloadEnable();
        this.mLogDebugSwitch = this.mPreloadRUSController.isDebugLogEnable();
    }

    public boolean isPreloadEnable() {
        return this.mBootPreloadSwitch || this.mAIPreloadSwitch || this.mGamePreloadSwitch;
    }

    public void disablePreload() {
        this.mBootPreloadSwitch = false;
        this.mAIPreloadSwitch = false;
        this.mGamePreloadSwitch = false;
    }

    private void initHistoryLogBuffer() {
        int len = 0;
        String[] strs = SystemProperties.get("dalvik.vm.heapsize", "").split("m");
        if (strs != null && strs.length >= 1) {
            len = Integer.valueOf(strs[0]).intValue() > 128 ? 5000 : ColorFreeformManagerService.FREEFORM_CALLER_UID;
        }
        this.mPreloadHistoryLog = new PreloadHistoryLog(len);
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx != null) {
            this.mAms = amsEx.getActivityManagerService();
            this.mContext = this.mAms.mContext;
            this.mPreloadEnable = SystemProperties.getBoolean("persist.vendor.enable.preload", false);
            if (!this.mPreloadEnable || !isPreloadMatchedMem()) {
                String str = TAG;
                Log.d(str, "preload disable(mPreloadEnable: " + this.mPreloadEnable + ",isPreloadMatchedMem: " + isPreloadMatchedMem() + ")");
                return;
            }
            initHistoryLogBuffer();
            HandlerThread thread = new HandlerThread(PRELOAD_THREAD_NAME, -2);
            thread.start();
            this.mMainHandler = new Handler(thread.getLooper());
            HandlerThread killThread = new HandlerThread("RPMHandler: kill", -2);
            killThread.start();
            this.mKillHandler = new Handler(killThread.getLooper());
            initPreloadPkgs();
            ColorResourcePreloadDBManager mDbManager = ColorResourcePreloadDBManager.getInstance();
            if (mDbManager != null) {
                mDbManager.init(this.mContext);
            }
            getPreloadInfoFromDB();
            this.mPreloadRUSController = new PreloadRUSController();
            setLowAvailMemThreshold();
            this.mPreloadInfoCollector = new PreloadInfoCollector();
            this.mPreloadInfoCollector.uploadPreloadInfo();
            initConfig(this.mPreloadRUSController);
            try {
                ActivityManager.getService().registerUidObserver(this.mUidObserver, 14, -1, (String) null);
            } catch (Exception e) {
                String str2 = TAG;
                Log.e(str2, "registerUidObserver failed " + e);
            }
            if (this.mBootPreloadSwitch) {
                this.mBootPreload = new BootPreload();
                this.mBootPreload.onInit();
            }
            if (this.mAIPreloadSwitch) {
                this.mAIPreload = new AIPreload();
                initHotnessMinHeap(this.minHeapSize);
            }
            if (mPreloadTestSwitch) {
                this.mPreloadTest = new PreloadTest();
            }
        }
    }

    private void initConfig(PreloadRUSController mPreloadRUSController2) {
        if (mPreloadRUSController2 != null) {
            this.mBootPreloadSwitch = mPreloadRUSController2.isBootPreloadEnable();
            this.mAIPreloadSwitch = mPreloadRUSController2.isAIPreloadEnable();
            this.mGamePreloadSwitch = mPreloadRUSController2.isGamePreloadEnable();
            this.mLogDebugSwitch = mPreloadRUSController2.isDebugLogEnable();
            this.mDefaultPkgList.clear();
            this.mDefaultPkgList.addAll(mPreloadRUSController2.getBootupList());
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.REBOOT");
        filter.addAction(BrightnessConstants.ACTION_BOOT_COMPLETED);
        this.mContext.registerReceiver(this.mPreloadReceiver, filter, null, this.mMainHandler);
        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction("android.intent.action.PACKAGE_ADDED");
        pkgFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        pkgFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        pkgFilter.addDataScheme(BrightnessConstants.AppSplineXml.TAG_PACKAGE);
        this.mContext.registerReceiver(this.mPkgIntentReceiver, pkgFilter, null, this.mMainHandler);
    }

    private void initHotnessMinHeap(int heapSize) {
        synchronized (this.mLock) {
            if (this.mPreloadPkgs != null) {
                this.minHeap = new PriorityQueue<>(heapSize, new Comparator<PreloadPkgState>() {
                    /* class com.android.server.am.ColorResourcePreloadManager.AnonymousClass2 */

                    public int compare(PreloadPkgState o1, PreloadPkgState o2) {
                        return Long.compare(o1.getHotnessCnt(), o2.getHotnessCnt());
                    }
                });
                for (int i = 0; i < this.mPreloadPkgs.getMap().size(); i++) {
                    SparseArray<PreloadPkgState> userIdsPkgStatus = this.mPreloadPkgs.getMap().valueAt(i);
                    for (int j = 0; j < userIdsPkgStatus.size(); j++) {
                        PreloadPkgState preloadPkgState = userIdsPkgStatus.valueAt(j);
                        if (this.minHeap.size() < heapSize) {
                            this.minHeap.offer(preloadPkgState);
                        } else if (this.minHeap.peek() != null && this.minHeap.peek().getHotnessCnt() <= preloadPkgState.getHotnessCnt()) {
                            this.minHeap.poll();
                            this.minHeap.offer(preloadPkgState);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMinHeapLocked(PreloadPkgState ps) {
        PriorityQueue<PreloadPkgState> priorityQueue = this.minHeap;
        if (priorityQueue != null) {
            if (priorityQueue.contains(ps)) {
                this.minHeap.remove(ps);
                this.minHeap.offer(ps);
            } else if (this.minHeap.size() < this.minHeapSize) {
                this.minHeap.offer(ps);
            } else if (this.minHeap.peek() != null && this.minHeap.peek().getHotnessCnt() <= ps.getHotnessCnt()) {
                this.minHeap.poll();
                this.minHeap.offer(ps);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<String> getTopHotnessApp(int size) {
        PreloadPkgState pkgState;
        ArrayList<PreloadPkgState> result = new ArrayList<>();
        ArrayList<String> tempList = new ArrayList<>(size);
        int count = 0;
        synchronized (this.mLock) {
            Iterator<PreloadPkgState> iterator = this.minHeap.iterator();
            while (iterator.hasNext()) {
                PreloadPkgState ps = iterator.next();
                if (!(ps == null || (pkgState = this.mPreloadPkgs.get(ps.getPkgName(), ps.getUserId())) == null || ((this.mUidRunningList.contains(Integer.valueOf(pkgState.getUid())) && !pkgState.isPreload()) || pkgState.getHotnessCnt() < 5))) {
                    if (System.currentTimeMillis() - pkgState.getLastPreloadTime() < HOTNESS_PRELOAD_COLD_TIME) {
                        PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                        String str = TAG;
                        preloadHistoryLog.i(str, "getTopHotnessApp: skip to preload " + pkgState.getPkgName() + ", uid: " + pkgState.getUid() + ", because of cold time");
                    } else {
                        result.add(pkgState);
                    }
                }
            }
            Collections.sort(result, new Comparator<PreloadPkgState>() {
                /* class com.android.server.am.ColorResourcePreloadManager.AnonymousClass3 */

                public int compare(PreloadPkgState o1, PreloadPkgState o2) {
                    return Long.compare(o2.getHotnessCnt(), o1.getHotnessCnt());
                }
            });
            int i = 0;
            while (true) {
                if (i >= result.size()) {
                    break;
                } else if (count == size) {
                    break;
                } else {
                    tempList.add(result.get(i).getPkgName());
                    count++;
                    i++;
                }
            }
        }
        return tempList;
    }

    /* access modifiers changed from: package-private */
    public class PreloadReceiver extends BroadcastReceiver {
        PreloadReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN") || intent.getAction().equals("android.intent.action.REBOOT")) {
                    String str = ColorResourcePreloadManager.TAG;
                    Log.i(str, "onReceive " + intent.getAction());
                    ColorResourcePreloadManager.this.mPreloadInfoCollector.sendPreloadInfoToDB();
                } else if (intent.getAction().equals(BrightnessConstants.ACTION_BOOT_COMPLETED)) {
                    Log.i(ColorResourcePreloadManager.TAG, "onReceive ACTION_BOOT_COMPLETED");
                    if (ColorResourcePreloadManager.this.mAIPreload != null) {
                        for (int i = 0; i < 5 && !ColorResourcePreloadManager.this.mAIPreload.registerDeepThinker(); i++) {
                        }
                        ColorResourcePreloadManager.this.mAIPreload.registerDeepThinkerServiceState();
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r4 = r8.mContext.getPackageManager().getPackageInfoAsUser(r9, 4111, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004d, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004e, code lost:
        r5 = com.android.server.am.ColorResourcePreloadManager.TAG;
        android.util.Log.e(r5, "exception: " + r4);
        r4 = null;
     */
    public void onPackageStatusChange(String pkgName, String action, boolean isReInstall, int uid) {
        int userId;
        PackageInfo pi;
        if (pkgName != null && action != null) {
            userId = UserHandle.getUserId(uid);
            if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                String str = TAG;
                Log.i(str, "onPackageStatusChange ACTION_PACKAGE_ADDED pkgName: " + pkgName + " uid: " + uid);
                synchronized (this.mLock) {
                    if (this.mPreloadPkgs.get(pkgName, userId) != null) {
                        return;
                    }
                }
            } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                String str2 = TAG;
                Log.i(str2, "onPackageStatusChange ACTION_PACKAGE_REMOVED pkgName: " + pkgName + " uid: " + uid);
                if (isReInstall) {
                    Log.i(TAG, "onPackageStatusChange isReInstall don't care!");
                    return;
                }
                synchronized (this.mLock) {
                    PreloadPkgState ps = this.mPreloadPkgs.get(pkgName, userId);
                    if (ps != null) {
                        this.mPreloadPkgs.remove(pkgName, userId);
                        this.minHeap.remove(ps);
                        return;
                    }
                    return;
                }
            } else if (action.equals("android.intent.action.PACKAGE_DATA_CLEARED")) {
                String str3 = TAG;
                Log.i(str3, "onPackageStatusChange ACTION_PACKAGE_DATA_CLEARED pkgName: " + pkgName + " uid: " + uid);
                return;
            } else {
                return;
            }
        } else {
            return;
        }
        updatePreloadPkgs(pi);
        synchronized (this.mLock) {
            PreloadPkgState preloadPkgState = this.mPreloadPkgs.get(pkgName, userId);
            if (preloadPkgState != null) {
                updateMinHeapLocked(preloadPkgState);
            }
        }
    }

    private String isIconPackage(String pkgname, int userId) {
        ResolveInfo packageResolveInfo;
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(pkgname);
        List<ResolveInfo> packages = this.mContext.getPackageManager().queryIntentActivitiesAsUser(intent, 786944, userId);
        if (packages == null || packages.size() <= 0 || (packageResolveInfo = packages.get(0)) == null || packageResolveInfo.activityInfo == null) {
            return null;
        }
        PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
        String str = TAG;
        preloadHistoryLog.i(str, "main process name: " + packageResolveInfo.activityInfo.packageName + " " + packageResolveInfo.activityInfo.processName);
        return packageResolveInfo.activityInfo.processName;
    }

    private int getAppUid(PackageInfo pkgInfo) {
        if (pkgInfo == null || pkgInfo.applicationInfo == null) {
            return 0;
        }
        return pkgInfo.applicationInfo.uid;
    }

    private int getVipDelayTime() {
        return this.vipDelayTime;
    }

    private void setVipDelayTime(int time) {
        this.vipDelayTime = time;
    }

    private void parseForegroundProhibitList(XmlPullParser parser, List<String> listProhibt) {
        try {
            int eventType = parser.getEventType();
            String strText = null;
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String strName = parser.getName();
                        if (parser.next() != 1) {
                            strText = parser.getText();
                        }
                        if (strText != null && TAG_PROHIBIT_FORE_RUN.equals(strName) && !listProhibt.contains(strText)) {
                            listProhibt.add(strText);
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
            String str = TAG;
            preloadHistoryLog.i(str, "parseXml: Got exception e:" + e);
        }
    }

    private InputStream getFileStreamFromProvider(String path) {
        try {
            return ColorSettings.readConfig(this.mContext, path, 0);
        } catch (Throwable th) {
            this.mPreloadHistoryLog.i(TAG, "Fail to getFileStreamFromProvider");
            return null;
        }
    }

    private List<String> getForegroundProhibitList() {
        PreloadHistoryLog preloadHistoryLog;
        String str;
        StringBuilder sb;
        List<String> result = new ArrayList<>();
        InputStream stream = getFileStreamFromProvider(PROHIBIT_FOREGROUND_LIST_FILE);
        if (stream == null) {
            this.mPreloadHistoryLog.i(TAG, "Fail to getForegroundProhibitList");
            return result;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            parseForegroundProhibitList(parser, result);
            try {
                stream.close();
            } catch (IOException e) {
                e = e;
                preloadHistoryLog = this.mPreloadHistoryLog;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (Exception e2) {
            PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
            String str2 = TAG;
            preloadHistoryLog2.i(str2, "getForegroundProhibitList: Got exception e=" + e2);
            try {
                stream.close();
            } catch (IOException e3) {
                e = e3;
                preloadHistoryLog = this.mPreloadHistoryLog;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            try {
                stream.close();
            } catch (IOException e4) {
                PreloadHistoryLog preloadHistoryLog3 = this.mPreloadHistoryLog;
                String str3 = TAG;
                preloadHistoryLog3.i(str3, "getForegroundProhibitList: Got exception close stream IOException e=" + e4);
            }
            throw th;
        }
        return result;
        sb.append("getForegroundProhibitList: Got exception close stream IOException e=");
        sb.append(e);
        preloadHistoryLog.i(str, sb.toString());
        return result;
    }

    private void updatePreloadPkgs(PackageInfo pi) {
        if (pi != null) {
            String versionName = pi.versionName;
            String sharedUidName = pi.sharedUserId;
            String pkgName = pi.packageName;
            int uid = getAppUid(pi);
            int userId = UserHandle.getUserId(uid);
            if (uid < 100000 && !UserHandle.isApp(uid)) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "pkgName: " + pkgName + "uid: " + uid + " is not app uid");
            } else if (sharedUidName != null) {
                PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
                String str2 = TAG;
                preloadHistoryLog2.i(str2, "pkgName: " + pkgName + "uid: " + uid + " is share uid");
            } else if ((pi.applicationInfo.flags & 1) != 0) {
                PreloadHistoryLog preloadHistoryLog3 = this.mPreloadHistoryLog;
                String str3 = TAG;
                preloadHistoryLog3.i(str3, "pkgName: " + pkgName + "uid: " + uid + " is system");
            } else {
                String processName = isIconPackage(pkgName, userId);
                if (processName == null) {
                    PreloadHistoryLog preloadHistoryLog4 = this.mPreloadHistoryLog;
                    String str4 = TAG;
                    preloadHistoryLog4.i(str4, "pkgName: " + pkgName + "uid: " + uid + " is not icon package");
                    return;
                }
                synchronized (this.mLock) {
                    if (this.mPreloadPkgs.get(pkgName, userId) == null) {
                        PreloadPkgState preloadPkgState = new PreloadPkgState(pkgName, uid);
                        preloadPkgState.setVersionName(versionName);
                        preloadPkgState.setProcessName(processName);
                        this.mPreloadPkgs.put(pkgName, userId, preloadPkgState);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: updateFASByUI */
    public void lambda$initPreloadPkgs$0$ColorResourcePreloadManager(String pkgName, int uid, List<String> foregroundProhibitList) {
        if (foregroundProhibitList.contains(pkgName)) {
            if (!isFASEnabled(uid, pkgName)) {
                updateFasState(pkgName, uid, 1);
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                preloadHistoryLog.addKeyInfo(uid + ", update FAS mode:1");
                PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog2.i(str, "pkgName: " + pkgName + ", uid: " + uid + ", set FAS on");
                return;
            }
            PreloadHistoryLog preloadHistoryLog3 = this.mPreloadHistoryLog;
            String str2 = TAG;
            preloadHistoryLog3.i(str2, "pkgName: " + pkgName + ", uid: " + uid + ", FAS is already on");
        } else if (isFASEnabled(uid, pkgName)) {
            updateFasState(pkgName, uid, 0);
            PreloadHistoryLog preloadHistoryLog4 = this.mPreloadHistoryLog;
            preloadHistoryLog4.addKeyInfo(uid + ", update FAS mode:0");
            PreloadHistoryLog preloadHistoryLog5 = this.mPreloadHistoryLog;
            String str3 = TAG;
            preloadHistoryLog5.i(str3, "pkgName: " + pkgName + ", uid: " + uid + ", set FAS off");
        } else {
            PreloadHistoryLog preloadHistoryLog6 = this.mPreloadHistoryLog;
            String str4 = TAG;
            preloadHistoryLog6.i(str4, "pkgName: " + pkgName + ", uid: " + uid + ", FAS is already off");
        }
    }

    private void initPreloadPkgs() {
        List<String> foregroundProhibitList = getForegroundProhibitList();
        try {
            List<PackageInfo> installedPackagesList = this.mContext.getPackageManager().getInstalledPackagesAsUser(4111, this.mContext.getUserId());
            for (int i = 0; i < installedPackagesList.size(); i++) {
                updatePreloadPkgs(installedPackagesList.get(i));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception when call initPreloadPkgs : " + e);
            e.printStackTrace();
        }
        synchronized (this.mLock) {
            for (int i2 = 0; i2 < this.mPreloadPkgs.getMap().size(); i2++) {
                SparseArray<PreloadPkgState> userIdsPkgStatus = this.mPreloadPkgs.getMap().valueAt(i2);
                for (int j = 0; j < userIdsPkgStatus.size(); j++) {
                    PreloadPkgState preloadPkgState = userIdsPkgStatus.valueAt(j);
                    this.mMainHandler.post(new Runnable(preloadPkgState.getPkgName(), preloadPkgState.getUid(), foregroundProhibitList) {
                        /* class com.android.server.am.$$Lambda$ColorResourcePreloadManager$ictSBZjwQ_KNtgStzdscL3kK2k */
                        private final /* synthetic */ String f$1;
                        private final /* synthetic */ int f$2;
                        private final /* synthetic */ List f$3;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                        }

                        public final void run() {
                            ColorResourcePreloadManager.this.lambda$initPreloadPkgs$0$ColorResourcePreloadManager(this.f$1, this.f$2, this.f$3);
                        }
                    });
                }
            }
        }
    }

    private void freezeAppForPreload(String pkgName, int uid) {
        if (ColorHansManager.getInstance() != null) {
            this.mMainHandler.post(new Runnable(uid, pkgName) {
                /* class com.android.server.am.$$Lambda$ColorResourcePreloadManager$HjO1UmWR0gz4lQhMNqW4ZWWCM3Q */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    ColorResourcePreloadManager.this.lambda$freezeAppForPreload$1$ColorResourcePreloadManager(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$freezeAppForPreload$1$ColorResourcePreloadManager(int uid, String pkgName) {
        PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
        String str = TAG;
        preloadHistoryLog.i(str, "freezeAppForPreload uid: " + uid);
        if (!ColorHansManager.getInstance().freezeForPreload(uid)) {
            stopPreloadPkg(pkgName, uid);
        }
    }

    private boolean isPreloadMatchedMem() {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        this.mAms.getMemoryInfo(memInfo);
        if ((memInfo.totalMem / GB_IN_BYTE) + 1 < 6) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getTotalMemGB() {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        this.mAms.getMemoryInfo(memInfo);
        PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
        String str = TAG;
        preloadHistoryLog.i(str, "totalMem = " + (memInfo.totalMem / 1048576) + "MB");
        return (memInfo.totalMem / GB_IN_BYTE) + 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setLowAvailMemThreshold() {
        int totalMemGB = (int) getTotalMemGB();
        if (totalMemGB <= 6) {
            this.mLowAvailMemThreshold = (long) this.mPreloadRUSController.getLowMem_Mem6();
        } else if (totalMemGB <= 6 || totalMemGB > 8) {
            this.mLowAvailMemThreshold = (long) this.mPreloadRUSController.getLowMem_Mem12();
        } else {
            this.mLowAvailMemThreshold = (long) this.mPreloadRUSController.getLowMem_Mem8();
        }
        PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
        String str = TAG;
        preloadHistoryLog.i(str, "mLowAvailMemThreshold = " + this.mLowAvailMemThreshold + "MB");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSystemLowMemory(long needMemMB) {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        this.mAms.getMemoryInfo(memInfo);
        boolean flag = (memInfo.availMem / 1048576) - needMemMB < this.mLowAvailMemThreshold;
        if (flag) {
            this.mPreloadHistoryLog.i(TAG, "system is low memory, pending the AI preload");
            this.mPreloadHistoryLog.addLowMemoryInfo(needMemMB, this.mLowAvailMemThreshold, memInfo.availMem / 1048576, memInfo.totalMem / 1048576);
        }
        return flag;
    }

    private void killPreloadPkg(int uid, int userId, String pkgName) {
        try {
            Intent intent = new Intent("oppo.intent.action.REQUEST_CLEAR_SPEC_APP");
            intent.setPackage("com.coloros.athena");
            intent.putExtra(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, uid);
            intent.putExtra("user_id", userId);
            intent.putExtra("p_name", pkgName);
            intent.putExtra("caller_package", this.mContext.getPackageName());
            intent.putExtra("type", 11);
            this.mContext.startService(intent);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "Exception: " + e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopPreloadPkg(String pkgName, int uid) {
        this.mKillHandler.post(new Runnable(uid, UserHandle.getUserId(uid), pkgName) {
            /* class com.android.server.am.$$Lambda$ColorResourcePreloadManager$IAhwkMxSNsxf9orXNRl8jRPNSPc */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                ColorResourcePreloadManager.this.lambda$stopPreloadPkg$2$ColorResourcePreloadManager(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$stopPreloadPkg$2$ColorResourcePreloadManager(int uid, int userId, String pkgName) {
        killPreloadPkg(uid, userId, pkgName);
        PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
        String str = TAG;
        preloadHistoryLog.i(str, "forceStop " + pkgName + " " + uid);
        setPkgPreload(pkgName, uid, "process_killed: force-stop", false);
        PreloadTest preloadTest = this.mPreloadTest;
        if (preloadTest != null) {
            preloadTest.reportPreloadStatus(pkgName, PreloadReportStatus.process_killed.getStatus());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopAllPreloadPkgs() {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mPreloadPkgs.getMap().size(); i++) {
                SparseArray<PreloadPkgState> userIdsPkgStatus = this.mPreloadPkgs.getMap().valueAt(i);
                for (int j = 0; j < userIdsPkgStatus.size(); j++) {
                    PreloadPkgState preloadPkgState = userIdsPkgStatus.valueAt(j);
                    if (preloadPkgState.isPreload()) {
                        stopPreloadPkg(preloadPkgState.getPkgName(), preloadPkgState.getUid());
                    }
                }
            }
        }
    }

    class BootPreload {
        private int BOOT_SLOT_MAX = 3;
        private PreloadTriggerReceiver mPreloadTriggerReceiver = new PreloadTriggerReceiver();

        public BootPreload() {
        }

        public void onInit() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BrightnessConstants.ACTION_BOOT_COMPLETED);
            filter.setPriority(-1000);
            ColorResourcePreloadManager.this.mContext.registerReceiver(this.mPreloadTriggerReceiver, filter, null, ColorResourcePreloadManager.this.mMainHandler);
            int totalMemGB = (int) ColorResourcePreloadManager.this.getTotalMemGB();
            if (totalMemGB < 6) {
                this.BOOT_SLOT_MAX = ColorResourcePreloadManager.this.mPreloadRUSController.getBootSlot_Mem6();
            } else if (totalMemGB < 6 || totalMemGB > 8) {
                this.BOOT_SLOT_MAX = ColorResourcePreloadManager.this.mPreloadRUSController.getBootSlot_Mem12();
            } else {
                this.BOOT_SLOT_MAX = ColorResourcePreloadManager.this.mPreloadRUSController.getBootSlot_Mem8();
            }
        }

        /* access modifiers changed from: package-private */
        public class PreloadTriggerReceiver extends BroadcastReceiver {
            PreloadTriggerReceiver() {
            }

            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null && intent.getAction().equals(BrightnessConstants.ACTION_BOOT_COMPLETED)) {
                    Log.i(ColorResourcePreloadManager.TAG, "onReceive ACTION_BOOT_COMPLETED");
                    ColorResourcePreloadManager.this.preloadPkgs(ColorResourcePreloadManager.this.mDefaultPkgList, ColorResourcePreloadManager.PRELOAD_REASON_BOOT_COMPLETED);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class AIPreload {
        private int AI_SLOT = 3;
        private Runnable appSwitchRunnable = new Runnable() {
            /* class com.android.server.am.ColorResourcePreloadManager.AIPreload.AnonymousClass4 */

            public void run() {
                int index;
                ArrayList<String> pkgList = new ArrayList<>();
                System.currentTimeMillis();
                synchronized (ColorResourcePreloadManager.this.mLock) {
                    for (int i = 0; i < ColorResourcePreloadManager.this.mPreloadPkgs.getMap().size(); i++) {
                        SparseArray<PreloadPkgState> userIdsPkgStatus = (SparseArray) ColorResourcePreloadManager.this.mPreloadPkgs.getMap().valueAt(i);
                        for (int j = 0; j < userIdsPkgStatus.size(); j++) {
                            pkgList.add(userIdsPkgStatus.valueAt(j).getPkgName());
                        }
                    }
                }
                int size = pkgList.size();
                if (size != 0) {
                    ArrayList<String> tempList = new ArrayList<>();
                    ArraySet<Integer> indexSet = new ArraySet<>();
                    for (int i2 = 0; i2 < AIPreload.this.AI_SLOT; i2++) {
                        double random = Math.random();
                        while (true) {
                            index = (int) (random * ((double) size));
                            if (!indexSet.contains(Integer.valueOf(index))) {
                                break;
                            }
                            random = Math.random();
                        }
                        indexSet.add(Integer.valueOf(index));
                    }
                    Iterator<Integer> iterator = indexSet.iterator();
                    while (iterator.hasNext()) {
                        int index2 = iterator.next().intValue();
                        String pkgNameInfo = pkgList.get(index2);
                        ColorResourcePreloadManager.this.mPreloadHistoryLog.i(ColorResourcePreloadManager.TAG, "preload index = " + index2 + " pkgNameInfo: " + pkgNameInfo);
                        tempList.add(pkgNameInfo);
                    }
                    ColorResourcePreloadManager.getInstance().preloadPkgsForAI(tempList);
                }
            }
        };
        IColorDeepThinkerManager deepThinkerManager = null;
        private ArrayList<PreloadPkgState> mAIPreloadPkgs = new ArrayList<>();
        private ColorAppSwitchManager.OnAppSwitchObserver mAppSwitchObserver = new ColorAppSwitchManager.OnAppSwitchObserver() {
            /* class com.android.server.am.ColorResourcePreloadManager.AIPreload.AnonymousClass3 */

            public void onAppEnter(ColorAppEnterInfo info) {
                if (info != null && info.extension != null) {
                    AIPreload.this.updateLastResumePackage(info.extension.getInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, -1), info.targetName);
                }
            }

            public void onAppExit(ColorAppExitInfo info) {
            }

            public void onActivityEnter(ColorAppEnterInfo info) {
            }

            public void onActivityExit(ColorAppExitInfo info) {
            }
        };
        EventCallback mCallback = new EventCallback() {
            /* class com.android.server.am.ColorResourcePreloadManager.AIPreload.AnonymousClass1 */

            public void onEventStateChanged(DeviceEventResult deviceEventResult) {
                if (!ColorResourcePreloadManager.this.mPreloadTestAppLaunched) {
                    ColorResourcePreloadManager.this.mMainHandler.removeCallbacks(AIPreload.this.preloadRunnable);
                    ColorResourcePreloadManager.this.mMainHandler.removeCallbacks(AIPreload.this.nextAppRunnable);
                    AIPreload.this.nextAppRunnable.setDeviceEventResult(deviceEventResult);
                    ColorResourcePreloadManager.this.mMainHandler.postDelayed(AIPreload.this.nextAppRunnable, 5000);
                }
            }
        };
        private String mLastResumePkgName = "";
        private int mLastResumeUid = -1;
        private long mNeedMemMB = 0;
        private ArrayList<PkgInfo> mPendingAIList = new ArrayList<>();
        ServiceStateObserver mStateObserver = new ServiceStateObserver() {
            /* class com.android.server.am.ColorResourcePreloadManager.AIPreload.AnonymousClass2 */

            public void onServiceDied() {
                Log.i(ColorResourcePreloadManager.TAG, "DeepThinker is died, register callback again");
                for (int i = 0; i < 5 && !AIPreload.this.registerDeepThinker(); i++) {
                }
            }
        };
        private NextAppRunnable nextAppRunnable = new NextAppRunnable();
        private Runnable preloadRunnable = new Runnable() {
            /* class com.android.server.am.ColorResourcePreloadManager.AIPreload.AnonymousClass5 */

            public void run() {
                synchronized (ColorResourcePreloadManager.this.mLock) {
                    if (!ColorResourcePreloadManager.this.checkCPUBusy()) {
                        if (!ColorResourcePreloadManager.this.isSystemLowMemory(AIPreload.this.mNeedMemMB)) {
                            for (int i = 0; i < AIPreload.this.mPendingAIList.size(); i++) {
                                PkgInfo info = (PkgInfo) AIPreload.this.mPendingAIList.get(i);
                                ColorResourcePreloadManager.this.mMainHandler.post(new Runnable(info.getPkgName(), info.getUid(), info.getOrder()) {
                                    /* class com.android.server.am.$$Lambda$ColorResourcePreloadManager$AIPreload$5$u4Py7S5CfyJ0eg7BQ0HFWG9b0A */
                                    private final /* synthetic */ String f$1;
                                    private final /* synthetic */ int f$2;
                                    private final /* synthetic */ long f$3;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                        this.f$3 = r4;
                                    }

                                    public final void run() {
                                        ColorResourcePreloadManager.AIPreload.AnonymousClass5.this.lambda$run$0$ColorResourcePreloadManager$AIPreload$5(this.f$1, this.f$2, this.f$3);
                                    }
                                });
                            }
                        }
                    }
                    if (ColorResourcePreloadManager.this.mPreloadTest != null) {
                        ArrayList<String> pkgList = new ArrayList<>();
                        for (int i2 = 0; i2 < AIPreload.this.mPendingAIList.size(); i2++) {
                            pkgList.add(((PkgInfo) AIPreload.this.mPendingAIList.get(i2)).getPkgName());
                        }
                        ColorResourcePreloadManager.this.mPreloadTest.reportPreloadStatus(pkgList, PreloadReportStatus.pending_low_memory.getStatus());
                    }
                    ColorResourcePreloadManager.this.mMainHandler.postDelayed(AIPreload.this.preloadRunnable, 5000);
                }
            }

            public /* synthetic */ void lambda$run$0$ColorResourcePreloadManager$AIPreload$5(String pkgName, int uid, long order) {
                if (ColorResourcePreloadManager.this.preloadPkgInternel(pkgName, uid, order, ColorResourcePreloadManager.PRELOAD_REASON_AI)) {
                    ColorResourcePreloadManager.this.mPreloadHistoryLog.addPreloadInfo(ColorResourcePreloadManager.PRELOAD_REASON_AI, pkgName, uid);
                }
            }
        };

        public AIPreload() {
        }

        public void onInit() {
            initAppSwitch();
        }

        /* access modifiers changed from: package-private */
        public class NextAppRunnable implements Runnable {
            private DeviceEventResult mDeviceEventResult = null;

            NextAppRunnable() {
            }

            public DeviceEventResult getDeviceEventResult() {
                return this.mDeviceEventResult;
            }

            public void setDeviceEventResult(DeviceEventResult deviceEventResult) {
                this.mDeviceEventResult = deviceEventResult;
            }

            public void run() {
                if (this.mDeviceEventResult != null && !ColorResourcePreloadManager.this.mPreloadTestAppLaunched) {
                    ArrayList<String> result = this.mDeviceEventResult.getExtraData().getStringArrayList("next_app");
                    if (result != null) {
                        PreloadHistoryLog preloadHistoryLog = ColorResourcePreloadManager.this.mPreloadHistoryLog;
                        String str = ColorResourcePreloadManager.TAG;
                        preloadHistoryLog.i(str, "AI result list: " + result + ", size: " + result.size());
                        if (result.size() != 0) {
                            ColorResourcePreloadManager.getInstance().preloadPkgsForAI(result);
                            return;
                        }
                        ArrayList<String> hotnessList = ColorResourcePreloadManager.this.getTopHotnessApp(AIPreload.this.getAiSlotsMax());
                        PreloadHistoryLog preloadHistoryLog2 = ColorResourcePreloadManager.this.mPreloadHistoryLog;
                        String str2 = ColorResourcePreloadManager.TAG;
                        preloadHistoryLog2.i(str2, "hotness result list: " + hotnessList + ", size: " + hotnessList.size());
                        ColorResourcePreloadManager.getInstance().preloadPkgsForAI(hotnessList);
                        return;
                    }
                    ArrayList<String> hotnessList2 = ColorResourcePreloadManager.this.getTopHotnessApp(AIPreload.this.getAiSlotsMax());
                    PreloadHistoryLog preloadHistoryLog3 = ColorResourcePreloadManager.this.mPreloadHistoryLog;
                    String str3 = ColorResourcePreloadManager.TAG;
                    preloadHistoryLog3.i(str3, "hotness result list: " + hotnessList2 + ", size: " + hotnessList2.size());
                    ColorResourcePreloadManager.getInstance().preloadPkgsForAI(hotnessList2);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean registerDeepThinker() {
            this.deepThinkerManager = ColorFrameworkFactory.getInstance().getColorDeepThinkerManager(ColorResourcePreloadManager.this.mContext);
            if (this.deepThinkerManager == null) {
                return false;
            }
            ArraySet<DeviceEvent> deviceEvents = new ArraySet<>();
            deviceEvents.add(new DeviceEvent.Builder().setEventType(10001).setEventStateType(0).build());
            boolean ret = this.deepThinkerManager.registerCallback(this.mCallback, new EventRequestConfig(deviceEvents));
            String str = ColorResourcePreloadManager.TAG;
            Log.i(str, "registerDeepThinker...ret:" + ret);
            return ret;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void registerDeepThinkerServiceState() {
            this.deepThinkerManager.registerServiceStateObserver(this.mStateObserver);
        }

        private void initAppSwitch() {
            ColorAppSwitchConfig config = new ColorAppSwitchConfig();
            config.addAppConfig(2, (List) null);
            ColorAppSwitchManager.getInstance().registerAppSwitchObserver(ColorResourcePreloadManager.this.mContext, this.mAppSwitchObserver, config);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateLastResumePackage(int uid, String pkgName) {
            String str = ColorResourcePreloadManager.TAG;
            Log.i(str, "resume pkg: " + pkgName + ", uid:" + uid + ", prev pkg: " + this.mLastResumePkgName + ", prev uid: " + this.mLastResumeUid);
            synchronized (ColorResourcePreloadManager.this.mLock) {
                PreloadPkgState ps = (PreloadPkgState) ColorResourcePreloadManager.this.mPreloadPkgs.get(pkgName, UserHandle.getUserId(uid));
                if (ps != null) {
                    ps.setHotnessCnt(ps.getHotnessCnt() + 1);
                    ColorResourcePreloadManager.this.mPreloadInfoCollector.updatePreloadHotnessCnt(uid, pkgName, ps.getHotnessCnt());
                    ColorResourcePreloadManager.this.updateMinHeapLocked(ps);
                }
            }
            if (ColorResourcePreloadManager.this.mDebugAISwitch) {
                ColorResourcePreloadManager.this.mMainHandler.removeCallbacks(this.preloadRunnable);
                ColorResourcePreloadManager.this.mMainHandler.removeCallbacks(this.appSwitchRunnable);
                ColorResourcePreloadManager.this.mMainHandler.postDelayed(this.appSwitchRunnable, 5000);
            }
            this.mLastResumePkgName = pkgName;
            this.mLastResumeUid = uid;
        }

        private void freeMemoryLocked(long order) {
            ColorResourcePreloadManager.this.mPreloadHistoryLog.i(ColorResourcePreloadManager.TAG, "mAIPreloadPkgs.size() = " + this.mAIPreloadPkgs.size());
            for (int i = this.mAIPreloadPkgs.size() + -1; i >= 0; i--) {
                PreloadPkgState preloadPkgState = this.mAIPreloadPkgs.get(i);
                if (preloadPkgState.getPreloadOrder() < order) {
                    String pkgName = preloadPkgState.getPkgName();
                    ColorResourcePreloadManager.this.stopPreloadPkg(pkgName, preloadPkgState.getUid());
                    ColorResourcePreloadManager.this.mPreloadHistoryLog.i(ColorResourcePreloadManager.TAG, "freeMemoryLocked stopPreloadPkg " + pkgName + ", it's order=" + preloadPkgState.getPreloadOrder() + ", new order=" + order);
                    this.mAIPreloadPkgs.remove(i);
                }
            }
        }

        public void preloadPkgsForAI(ArrayList<PkgInfo> pkgList) {
            if (!(pkgList == null || pkgList.size() == 0)) {
                synchronized (ColorResourcePreloadManager.this.mLock) {
                    freeMemoryLocked(pkgList.get(0).getOrder());
                    int toIndex = Math.min(pkgList.size(), getAiSlotsMax() - this.mAIPreloadPkgs.size());
                    this.mPendingAIList.clear();
                    this.mPendingAIList.addAll(pkgList.subList(0, toIndex));
                    if (toIndex < pkgList.size()) {
                        Log.i(ColorResourcePreloadManager.TAG, "preloadPkgsForAI toIndex < pkgList.size()");
                    }
                    this.mNeedMemMB = 0;
                    for (int i = 0; i < this.mPendingAIList.size(); i++) {
                        this.mNeedMemMB += this.mPendingAIList.get(i).getPss();
                    }
                    this.mNeedMemMB /= ColorDeviceStorageMonitorService.KB_BYTES;
                    ColorResourcePreloadManager.this.mPreloadHistoryLog.i(ColorResourcePreloadManager.TAG, "mNeedMemMB = " + this.mNeedMemMB);
                    ColorResourcePreloadManager.this.mMainHandler.removeCallbacks(this.preloadRunnable);
                    ColorResourcePreloadManager.this.mMainHandler.post(this.preloadRunnable);
                }
            }
        }

        public void updateAIPreloadPkgs(PreloadPkgState ps, boolean isPreload) {
            synchronized (ColorResourcePreloadManager.this.mLock) {
                if (ps.getPreloadReason().contains(ColorResourcePreloadManager.PRELOAD_REASON_AI)) {
                    if (isPreload) {
                        this.mAIPreloadPkgs.add(ps);
                    } else {
                        this.mAIPreloadPkgs.remove(ps);
                    }
                    ArrayList<String> extraNameList = new ArrayList<>();
                    ArrayList<Integer> extraUidList = new ArrayList<>();
                    Iterator<PreloadPkgState> it = this.mAIPreloadPkgs.iterator();
                    while (it.hasNext()) {
                        PreloadPkgState preloadPkgState = it.next();
                        extraNameList.add(preloadPkgState.getPkgName());
                        extraUidList.add(Integer.valueOf(preloadPkgState.getUid()));
                    }
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(ColorResourcePreloadManager.PRELOAD_EXTRA_NAME, extraNameList);
                    bundle.putIntegerArrayList(ColorResourcePreloadManager.PRELOAD_EXTRA_UID, extraUidList);
                    OppoListManager.getInstance().putConfigInfo(ColorResourcePreloadManager.PRELOAD_CONFIG_NAME, bundle);
                }
            }
        }

        public int getAiSlotsMax() {
            int defaultAiSlotMax;
            int totalMemGB = (int) ColorResourcePreloadManager.this.getTotalMemGB();
            if (totalMemGB < 6) {
                defaultAiSlotMax = ColorResourcePreloadManager.this.mPreloadRUSController.getAiSlot_Mem6();
            } else if (totalMemGB < 6 || totalMemGB > 8) {
                defaultAiSlotMax = ColorResourcePreloadManager.this.mPreloadRUSController.getAiSlot_Mem12();
            } else {
                defaultAiSlotMax = ColorResourcePreloadManager.this.mPreloadRUSController.getAiSlot_Mem8();
            }
            String str = ColorResourcePreloadManager.TAG;
            Log.i(str, "getAiSlotMax, defaultAiSlotMax = " + defaultAiSlotMax);
            return defaultAiSlotMax;
        }
    }

    /* access modifiers changed from: package-private */
    public class PreloadTest {
        private PreloadAppReceiver mPreloadAppReceiver = new PreloadAppReceiver();

        public PreloadTest() {
        }

        public void onInit() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ColorResourcePreloadManager.ACTION_PRELOAD_APP);
            filter.addAction(ColorResourcePreloadManager.ACTION_RELEASE_APP);
            filter.addAction(ColorResourcePreloadManager.ACTION_PRELOAD_ASK_MN);
            filter.addAction(ColorResourcePreloadManager.ACTION_TESTAPP_LAUNCHED);
            filter.addAction(ColorResourcePreloadManager.ACTION_TESTAPP_STOPPED);
            ColorResourcePreloadManager.this.mContext.registerReceiver(this.mPreloadAppReceiver, filter, null, ColorResourcePreloadManager.this.mMainHandler);
        }

        public void reportPreloadStatus(ArrayList<String> pkgList, int status) {
            Intent intent = new Intent(ColorResourcePreloadManager.ACTION_PRELOAD_STATUS);
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("pkgList", pkgList);
            bundle.putInt("status", status);
            intent.putExtras(bundle);
            ColorResourcePreloadManager.this.mMainHandler.post(new Runnable(intent) {
                /* class com.android.server.am.$$Lambda$ColorResourcePreloadManager$PreloadTest$s3qS6ZmkE8nCjfDGF5Dl4Kh8pNc */
                private final /* synthetic */ Intent f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ColorResourcePreloadManager.PreloadTest.this.lambda$reportPreloadStatus$0$ColorResourcePreloadManager$PreloadTest(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$reportPreloadStatus$0$ColorResourcePreloadManager$PreloadTest(Intent intent) {
            ColorResourcePreloadManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }

        public void reportPreloadStatus(String packageName, int status) {
            ArrayList<String> pkgList = new ArrayList<>();
            pkgList.add(packageName);
            reportPreloadStatus(pkgList, status);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void answerPreloadMaxNum() {
            Intent intent = new Intent(ColorResourcePreloadManager.ACTION_PRELOAD_ANSWER_MN);
            int max_num = 0;
            if (ColorResourcePreloadManager.this.mAIPreload != null) {
                max_num = ColorResourcePreloadManager.this.mAIPreload.getAiSlotsMax();
            }
            intent.putExtra("preload_max_num", max_num);
            ColorResourcePreloadManager.this.mMainHandler.post(new Runnable(intent) {
                /* class com.android.server.am.$$Lambda$ColorResourcePreloadManager$PreloadTest$ooa8y7woooSIG3bVnUkuOPoML78 */
                private final /* synthetic */ Intent f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ColorResourcePreloadManager.PreloadTest.this.lambda$answerPreloadMaxNum$1$ColorResourcePreloadManager$PreloadTest(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$answerPreloadMaxNum$1$ColorResourcePreloadManager$PreloadTest(Intent intent) {
            ColorResourcePreloadManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }

        /* access modifiers changed from: package-private */
        public class PreloadAppReceiver extends BroadcastReceiver {
            PreloadAppReceiver() {
            }

            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    if (intent.getAction().equals(ColorResourcePreloadManager.ACTION_PRELOAD_APP)) {
                        ColorResourcePreloadManager.this.mPreloadHistoryLog.i(ColorResourcePreloadManager.TAG, "onReceive com.android.server.am.PRELOAD_APP");
                        ColorResourcePreloadManager.this.preloadPkgsForAI(intent.getStringArrayListExtra("preload_app_list"));
                    } else if (intent.getAction().equals(ColorResourcePreloadManager.ACTION_RELEASE_APP)) {
                        ColorResourcePreloadManager.this.mPreloadHistoryLog.i(ColorResourcePreloadManager.TAG, "onReceive com.android.server.am.RELEASE_APP");
                        ColorResourcePreloadManager.this.stopAllPreloadPkgs();
                    } else if (intent.getAction().equals(ColorResourcePreloadManager.ACTION_PRELOAD_ASK_MN)) {
                        ColorResourcePreloadManager.this.mPreloadHistoryLog.i(ColorResourcePreloadManager.TAG, "onReceive com.android.server.am.PRELOAD_ASK_MN");
                        PreloadTest.this.answerPreloadMaxNum();
                    } else if (intent.getAction().equals(ColorResourcePreloadManager.ACTION_TESTAPP_LAUNCHED)) {
                        ColorResourcePreloadManager.this.mPreloadHistoryLog.i(ColorResourcePreloadManager.TAG, ColorResourcePreloadManager.ACTION_TESTAPP_LAUNCHED);
                        ColorResourcePreloadManager.this.mPreloadTestAppLaunched = true;
                        ColorResourcePreloadManager.this.stopAllPreloadPkgs();
                    } else if (intent.getAction().equals(ColorResourcePreloadManager.ACTION_TESTAPP_STOPPED)) {
                        ColorResourcePreloadManager.this.mPreloadHistoryLog.i(ColorResourcePreloadManager.TAG, "onReceive com.android.server.am.TESTAPP_STOPPED");
                        ColorResourcePreloadManager.this.mPreloadTestAppLaunched = false;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public enum PreloadReportStatus {
        pending_low_memory(1),
        skip_cannot_preload(2),
        skip_already_preloaded(3),
        skip_process_exist(4),
        skip_not_ever_launched(5),
        skip_in_preload_whitelist(6),
        process_preloaded(7),
        bind_app_finished(8),
        process_killed(9),
        process_died(10),
        user_start(11),
        allow_boot_ass_start(12);
        
        private int status;

        private PreloadReportStatus(int value) {
            this.status = value;
        }

        /* access modifiers changed from: package-private */
        public int getStatus() {
            return this.status;
        }
    }

    /* access modifiers changed from: package-private */
    public static class MemoryCompress {
        static final String COMPRESS_INTERFACE = "/proc/process_reclaim";
        static final String COMPRESS_PAGE_ALL = "all";
        static final String COMPRESS_PAGE_ANON = "anon";
        static final String COMPRESS_PAGE_FILE = "file";
        static final String COMPRESS_PAGE_INACTIVE = "inactive";
        static final int MIN_SWAP_FREE_SIZE = 100;

        MemoryCompress() {
        }

        private static boolean hasCompressFeature() {
            return new File(COMPRESS_INTERFACE).exists();
        }

        private static int getSwapFreeSize() {
            long[] values = {-1};
            Process.readProcLines("/proc/meminfo", new String[]{"SwapFree:"}, values);
            if (values[0] < 0) {
                return -1;
            }
            return (int) (values[0] / ColorDeviceStorageMonitorService.KB_BYTES);
        }

        private static boolean hasEnoughSwap() {
            return getSwapFreeSize() >= 100;
        }

        private static boolean isValidCompressType(String compressPage) {
            return COMPRESS_PAGE_INACTIVE.equals(compressPage) || COMPRESS_PAGE_ANON.equals(compressPage) || COMPRESS_PAGE_FILE.equals(compressPage) || COMPRESS_PAGE_ALL.equals(compressPage);
        }

        private static void compressInternal(int pid, String compressPage) {
            String str;
            StringBuilder sb;
            FileWriter fileWriter = null;
            BufferedWriter bufferedWriter = null;
            try {
                fileWriter = new FileWriter(COMPRESS_INTERFACE);
                bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(pid + " " + compressPage);
                try {
                    bufferedWriter.close();
                    fileWriter.close();
                } catch (Exception e) {
                    e = e;
                    str = ColorResourcePreloadManager.TAG;
                    sb = new StringBuilder();
                }
            } catch (Exception e2) {
                String str2 = ColorResourcePreloadManager.TAG;
                Log.e(str2, "compress error " + e2.toString());
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (Exception e3) {
                        e = e3;
                        str = ColorResourcePreloadManager.TAG;
                        sb = new StringBuilder();
                        sb.append("close file stream error ");
                        sb.append(e.toString());
                        Log.e(str, sb.toString());
                        return;
                    }
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (Throwable th) {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (Exception e4) {
                        String str3 = ColorResourcePreloadManager.TAG;
                        Log.e(str3, "close file stream error " + e4.toString());
                        throw th;
                    }
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
                throw th;
            }
        }

        public static void compress(int pid, String compressPage) {
            if (!hasCompressFeature() || !hasEnoughSwap()) {
                Log.i(ColorResourcePreloadManager.TAG, "has no compress feature or has no enough swap");
            } else if (isValidCompressType(compressPage)) {
                compressInternal(pid, compressPage);
            } else {
                String str = ColorResourcePreloadManager.TAG;
                Log.i(str, "unsupported compress page " + compressPage);
            }
        }
    }

    private void updatePkgPss(PreloadPkgState ps) {
        final String pkgName = ps.getPkgName();
        final int uid = ps.getUid();
        final ArrayList<String> nameList = new ArrayList<>();
        nameList.add(ps.getProcessName());
        nameList.addAll(ps.getProcessNameList());
        this.mMainHandler.post(new Runnable() {
            /* class com.android.server.am.ColorResourcePreloadManager.AnonymousClass5 */

            public void run() {
                long pss = 0;
                synchronized (ColorResourcePreloadManager.this.mAms) {
                    Iterator it = nameList.iterator();
                    while (it.hasNext()) {
                        ProcessRecord pr = (ProcessRecord) ColorResourcePreloadManager.this.mAms.mProcessList.mProcessNames.get((String) it.next(), uid);
                        if (pr != null) {
                            pss += pr.lastPss;
                        }
                    }
                }
                synchronized (ColorResourcePreloadManager.this.mLock) {
                    PreloadPkgState ps = (PreloadPkgState) ColorResourcePreloadManager.this.mPreloadPkgs.get(pkgName, UserHandle.getUserId(uid));
                    if (ps != null) {
                        if (ps.isPreload()) {
                            PreloadHistoryLog preloadHistoryLog = ColorResourcePreloadManager.this.mPreloadHistoryLog;
                            String str = ColorResourcePreloadManager.TAG;
                            preloadHistoryLog.i(str, "PkgName: " + pkgName + ", PSS = " + pss + "kB");
                            ps.setLastPss(pss);
                            ColorResourcePreloadManager.this.mPreloadInfoCollector.recordMemInfo(ps.getUid(), pss / ColorDeviceStorageMonitorService.KB_BYTES);
                        }
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void memoryStatusMonitor() {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        this.mAms.getMemoryInfo(memInfo);
        this.mPreloadHistoryLog.i(TAG, "availMem = " + (memInfo.availMem / 1048576) + "MB, lowAvailMem = " + this.mLowAvailMemThreshold + "MB, totalMem = " + (memInfo.totalMem / 1048576) + "MB");
        if (memInfo.availMem / 1048576 < this.mLowAvailMemThreshold) {
            stopAllPreloadPkgs();
            return;
        }
        synchronized (this.mLock) {
            for (int i = 0; i < this.mPreloadPkgs.getMap().size(); i++) {
                SparseArray<PreloadPkgState> userIdsPkgStatus = this.mPreloadPkgs.getMap().valueAt(i);
                for (int j = 0; j < userIdsPkgStatus.size(); j++) {
                    PreloadPkgState preloadPkgState = userIdsPkgStatus.valueAt(j);
                    if (preloadPkgState.isPreload()) {
                        updatePkgPss(preloadPkgState);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processStatusMonitor() {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mPreloadPkgs.getMap().size(); i++) {
                SparseArray<PreloadPkgState> userIdsPkgStatus = this.mPreloadPkgs.getMap().valueAt(i);
                for (int j = 0; j < userIdsPkgStatus.size(); j++) {
                    PreloadPkgState preloadPkgState = userIdsPkgStatus.valueAt(j);
                    if (preloadPkgState.isPreload()) {
                        long preloadedTime = System.currentTimeMillis() - preloadPkgState.getLastPreloadTime();
                        if (preloadPkgState.isMemoryCompressed()) {
                            if (preloadedTime > LONG_TIME_PRELOAD_NOT_USE_MS) {
                                stopPreloadPkg(preloadPkgState.getPkgName(), preloadPkgState.getUid());
                            }
                        } else if (preloadedTime > 900000) {
                            MemoryCompress.compress(preloadPkgState.getPid(), "all");
                            preloadPkgState.setMemoryCompressed(true);
                        }
                    }
                }
            }
        }
    }

    public void bootCompleted() {
        if (isPreloadEnable()) {
            Log.i(TAG, "bootCompleted");
            if (this.mAIPreloadSwitch) {
                this.mAIPreload.onInit();
            }
            if (mPreloadTestSwitch) {
                this.mPreloadTest.onInit();
            }
            if (this.mBootPreloadSwitch || this.mAIPreloadSwitch) {
                registerReceiver();
            }
            this.mChinaModel = isChinaModel();
            this.mMainHandler.postDelayed(this.periodRunnable, HOTNESS_PRELOAD_COLD_TIME);
        }
    }

    public void launchEmptyProcess(ActivityInfo aInfo, Intent intent) {
        boolean flag;
        Throwable th;
        Exception e;
        if (isPreloadEnable() && aInfo != null && aInfo.applicationInfo != null) {
            String reason = intent.getStringExtra(PRELOAD_REASON_KEY);
            long order = intent.getLongExtra(PRELOAD_ORDER_KEY, 0);
            int userId = UserHandle.getUserId(aInfo.applicationInfo.uid);
            if (this.mColorAppQuickFreezeManager == null) {
                this.mColorAppQuickFreezeManager = OppoFeatureCache.get(IColorAppQuickFreezeManager.DEFAULT);
            }
            IColorAppQuickFreezeManager iColorAppQuickFreezeManager = this.mColorAppQuickFreezeManager;
            if (iColorAppQuickFreezeManager != null && iColorAppQuickFreezeManager.inOppoFreezePackageList(aInfo.packageName, userId)) {
                synchronized (this.mLock) {
                    PreloadPkgState ps = this.mPreloadPkgs.get(aInfo.packageName, userId);
                    if (ps != null) {
                        ps.setLastDisabled(true);
                    }
                }
                this.mColorAppQuickFreezeManager.autoUnfreezePackage(aInfo.packageName, userId, reason);
            }
            if (isFASEnabled(aInfo.applicationInfo.uid, aInfo.packageName)) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "FAS is enabled pkgName: " + aInfo.packageName + " uid: " + aInfo.applicationInfo.uid);
                flag = true;
            } else {
                PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
                String str2 = TAG;
                preloadHistoryLog2.i(str2, "FAS is not enabled pkgName: " + aInfo.packageName + " uid: " + aInfo.applicationInfo.uid);
                updateFasState(aInfo.packageName, aInfo.applicationInfo.uid, 1);
                PreloadHistoryLog preloadHistoryLog3 = this.mPreloadHistoryLog;
                preloadHistoryLog3.addKeyInfo(aInfo.applicationInfo.uid + ", mode:1");
                flag = false;
            }
            synchronized (this.mLock) {
                PreloadPkgState ps2 = this.mPreloadPkgs.get(aInfo.packageName, userId);
                if (ps2 != null) {
                    ps2.setFasEnable(flag);
                }
            }
            synchronized (this.mAms) {
                HostingRecord hostingRecord = new HostingRecord("rplaunch:" + reason, new ComponentName(aInfo.packageName, aInfo.name));
                OppoBaseHostingRecord baseHostingRecord = typeCasting(hostingRecord);
                if (baseHostingRecord != null) {
                    baseHostingRecord.setRPLaunch(true);
                    baseHostingRecord.setOrder(order);
                    long token = Binder.clearCallingIdentity();
                    try {
                        try {
                            this.mAms.startProcessLocked(aInfo.processName, aInfo.applicationInfo, false, 0, hostingRecord, false, false, false);
                            Binder.restoreCallingIdentity(token);
                        } catch (Exception e2) {
                            e = e2;
                            try {
                                String str3 = TAG;
                                Log.e(str3, "startProcessLocked exception:" + e);
                                Binder.restoreCallingIdentity(token);
                            } catch (Throwable th2) {
                                th = th2;
                                Binder.restoreCallingIdentity(token);
                                throw th;
                            }
                        }
                    } catch (Exception e3) {
                        e = e3;
                        String str32 = TAG;
                        Log.e(str32, "startProcessLocked exception:" + e);
                        Binder.restoreCallingIdentity(token);
                    } catch (Throwable th3) {
                        th = th3;
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                }
            }
        }
    }

    private Intent getLaunchIntentForPackage(String packageName, int userId) {
        PackageManager packageManager = this.mContext.getPackageManager();
        Intent intentToResolve = new Intent("android.intent.action.MAIN");
        intentToResolve.addCategory("android.intent.category.INFO");
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = packageManager.queryIntentActivitiesAsUser(intentToResolve, 0, userId);
        if (ris == null || ris.size() <= 0) {
            intentToResolve.removeCategory("android.intent.category.INFO");
            intentToResolve.addCategory("android.intent.category.LAUNCHER");
            intentToResolve.setPackage(packageName);
            ris = packageManager.queryIntentActivitiesAsUser(intentToResolve, 0, userId);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(268435456);
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return intent;
    }

    private boolean isPreloadSkip(String packageName, int uid) {
        if (!ColorHansManager.getInstance().isFreezeTarget(uid)) {
            PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
            String str = TAG;
            preloadHistoryLog.i(str, "skip Non-freeze target pkgName: " + packageName + " uid: " + uid);
            return true;
        } else if (!ColorCommonListManager.getInstance().getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_SMS, uid).isEmpty()) {
            PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
            String str2 = TAG;
            preloadHistoryLog2.i(str2, "skip default sms pkgName: " + packageName + " uid: " + uid);
            return true;
        } else if (!ColorCommonListManager.getInstance().getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_DIALER, uid).isEmpty()) {
            PreloadHistoryLog preloadHistoryLog3 = this.mPreloadHistoryLog;
            String str3 = TAG;
            preloadHistoryLog3.i(str3, "skip default dialer pkgName: " + packageName + " uid: " + uid);
            return true;
        } else if (!ColorCommonListManager.getInstance().getAppInfo(ColorCommonListManager.CONFIG_DEFAULT_INPUT, uid).isEmpty()) {
            PreloadHistoryLog preloadHistoryLog4 = this.mPreloadHistoryLog;
            String str4 = TAG;
            preloadHistoryLog4.i(str4, "skip default input pkgName: " + packageName + " uid: " + uid);
            return true;
        } else if (ColorCommonListManager.getInstance().getAppInfo(ColorCommonListManager.CONFIG_WIDGET, uid).isEmpty()) {
            return false;
        } else {
            PreloadHistoryLog preloadHistoryLog5 = this.mPreloadHistoryLog;
            String str5 = TAG;
            preloadHistoryLog5.i(str5, "skip widget pkgName: " + packageName + " uid: " + uid);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e1, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00e4, code lost:
        if (r10.mPackageManagerInternal != null) goto L_0x00f0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00e6, code lost:
        r10.mPackageManagerInternal = (android.content.pm.PackageManagerInternal) com.android.server.LocalServices.getService(android.content.pm.PackageManagerInternal.class);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f2, code lost:
        if (r10.mPackageManagerInternal == null) goto L_0x00fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f4, code lost:
        r1 = r10.mPackageManagerInternal.wasPackageEverLaunched(r11, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00fb, code lost:
        if (r1 != false) goto L_0x013e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00fd, code lost:
        r3 = r10.mPreloadHistoryLog;
        r4 = com.android.server.am.ColorResourcePreloadManager.TAG;
        r3.i(r4, r11 + ": isPkgEverLaunched = " + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x011a, code lost:
        if (r10.mPreloadTest == null) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x011c, code lost:
        r10.mPreloadTest.reportPreloadStatus(r11, com.android.server.am.ColorResourcePreloadManager.PreloadReportStatus.skip_not_ever_launched.getStatus());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0127, code lost:
        r3 = r10.mPreloadHistoryLog;
        r3.addPreloadInfo(r15 + ": notEverLaunch", r11, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x013d, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x013e, code lost:
        r2 = getLaunchIntentForPackage(r11, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0144, code lost:
        if (r2 == null) goto L_0x0196;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0146, code lost:
        r4 = android.app.ActivityOptions.makeBasic();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x014a, code lost:
        if (r4 == null) goto L_0x019d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x014c, code lost:
        r5 = typeCasting(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0150, code lost:
        if (r5 == null) goto L_0x019d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0152, code lost:
        r5.setRPLaunch(true);
        r2.putExtra(com.android.server.am.ColorResourcePreloadManager.PRELOAD_REASON_KEY, r15);
        r2.putExtra(com.android.server.am.ColorResourcePreloadManager.PRELOAD_ORDER_KEY, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
        r10.mContext.startActivityAsUser(r2, r4.toBundle(), android.os.UserHandle.of(r0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x016d, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x016e, code lost:
        android.util.Log.e(com.android.server.am.ColorResourcePreloadManager.TAG, "No activity to handle assist action.", r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0196, code lost:
        android.util.Log.i(com.android.server.am.ColorResourcePreloadManager.TAG, "Received intent is null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x019e, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x019f, code lost:
        r4 = com.android.server.am.ColorResourcePreloadManager.TAG;
        android.util.Log.e(r4, "Exception: " + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x01b5, code lost:
        return false;
     */
    private boolean preloadPkgInternel(String packageName, int uid, long order, String reason) {
        ArrayList<String> arrayList;
        int userId = UserHandle.getUserId(uid);
        if (!isNotAllowedPreload(packageName, uid) || (arrayList = this.vipPkgList) == null || arrayList.contains(packageName)) {
            PreloadRUSController preloadRUSController = this.mPreloadRUSController;
            if (preloadRUSController != null && preloadRUSController.isPreloadWhiteList("preload_whitelist", packageName)) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "preloadPkgInternel: WL-" + packageName + ", don't preload");
                PreloadTest preloadTest = this.mPreloadTest;
                if (preloadTest != null) {
                    preloadTest.reportPreloadStatus(packageName, PreloadReportStatus.skip_in_preload_whitelist.getStatus());
                }
                return false;
            } else if (isPreloadSkip(packageName, uid)) {
                PreloadTest preloadTest2 = this.mPreloadTest;
                if (preloadTest2 != null) {
                    preloadTest2.reportPreloadStatus(packageName, PreloadReportStatus.skip_in_preload_whitelist.getStatus());
                }
                return false;
            } else {
                synchronized (this.mLock) {
                    if (this.mUidRunningList.contains(Integer.valueOf(uid)) && this.vipPkgList != null && !this.vipPkgList.contains(packageName)) {
                        PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
                        String str2 = TAG;
                        preloadHistoryLog2.i(str2, "Process already start pkgName: " + packageName + " uid: " + uid);
                        if (this.mPreloadTest != null) {
                            this.mPreloadTest.reportPreloadStatus(packageName, PreloadReportStatus.skip_process_exist.getStatus());
                        }
                        PreloadHistoryLog preloadHistoryLog3 = this.mPreloadHistoryLog;
                        preloadHistoryLog3.addPreloadInfo(reason + ": AlreadyStart", packageName, uid);
                        return false;
                    }
                }
            }
        } else {
            PreloadHistoryLog preloadHistoryLog4 = this.mPreloadHistoryLog;
            String str3 = TAG;
            preloadHistoryLog4.i(str3, "preloadPkgInternel: BootStart&AssistStart-" + packageName + ", don't preload");
            return false;
        }
        PreloadHistoryLog preloadHistoryLog5 = this.mPreloadHistoryLog;
        String str4 = TAG;
        preloadHistoryLog5.i(str4, "starting RPLaunch pkgName: " + packageName + " userId: " + userId);
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003e, code lost:
        if (preloadPkgInternel(r11, r3, java.lang.System.currentTimeMillis(), r13) == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0040, code lost:
        r10.mPreloadHistoryLog.addPreloadInfo(r13, r11, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        return;
     */
    private void preloadPkg(String packageName, int userId, String reason) {
        synchronized (this.mLock) {
            PreloadPkgState preloadPkgState = this.mPreloadPkgs.get(packageName, userId);
            if (preloadPkgState != null) {
                if (!isAllowStart(packageName, preloadPkgState.getUid()) && PRELOAD_REASON_VIP.equals(reason)) {
                    return;
                }
                if (!preloadPkgState.isPreload()) {
                    int uid = preloadPkgState.getUid();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b9, code lost:
        r0 = th;
     */
    private void preloadPkgs(String reason) {
        ArrayList<PkgInfo> pkgList = new ArrayList<>();
        long order = System.currentTimeMillis();
        String uidStr = "";
        synchronized (this.mLock) {
            int i = 0;
            int i2 = 0;
            while (i2 < this.mPreloadPkgs.getMap().size()) {
                SparseArray<PreloadPkgState> userIdsPkgStatus = this.mPreloadPkgs.getMap().valueAt(i2);
                for (int j = i; j < userIdsPkgStatus.size(); j++) {
                    PreloadPkgState preloadPkgState = userIdsPkgStatus.valueAt(j);
                    if (!preloadPkgState.isPreload()) {
                        pkgList.add(new PkgInfo(preloadPkgState.getPkgName(), preloadPkgState.getUid(), order));
                    }
                }
                i2++;
                i = 0;
            }
        }
        for (int i3 = 0; i3 < pkgList.size(); i3++) {
            PkgInfo info = pkgList.get(i3);
            if (preloadPkgInternel(info.getPkgName(), info.getUid(), info.getOrder(), reason)) {
                uidStr = uidStr + info.getUid() + " ";
            }
        }
        if (!uidStr.equals("")) {
            this.mPreloadHistoryLog.addPreloadAllInfo(reason, uidStr);
            return;
        }
        return;
        while (true) {
        }
    }

    private ArrayList<PkgInfo> preloadPkgsFilter(ArrayList<String> pkgStrList, String reason) {
        ArrayList<PreloadPkgState> targetPkgs;
        long pss;
        ArrayList<PreloadPkgState> targetPkgs2 = new ArrayList<>();
        ArrayList<PkgInfo> pkgList = new ArrayList<>();
        long order = System.currentTimeMillis();
        synchronized (this.mLock) {
            char c = 0;
            int i = 0;
            while (i < pkgStrList.size()) {
                try {
                    try {
                        String packageInfo = pkgStrList.get(i);
                        String packageName = null;
                        int userId = this.mContext.getUserId();
                        PreloadPkgState packageStatus = null;
                        if (packageInfo == null || !packageInfo.contains(",")) {
                            packageName = packageInfo;
                            packageStatus = this.mPreloadPkgs.get(packageName, userId);
                        } else {
                            String[] packageSplitList = packageInfo.split(",");
                            if (packageSplitList != null && packageSplitList.length == 2) {
                                if (packageSplitList[c] != null) {
                                    packageName = packageSplitList[c];
                                }
                                try {
                                    if (packageSplitList[1] != null) {
                                        userId = Integer.parseInt(packageSplitList[1]);
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "preloadPkgsFilter parseInt error!");
                                }
                                packageStatus = this.mPreloadPkgs.get(packageName, userId);
                            }
                        }
                        if (packageStatus != null) {
                            targetPkgs2.add(packageStatus);
                        } else if (this.mPreloadTest != null) {
                            this.mPreloadTest.reportPreloadStatus(packageName, PreloadReportStatus.skip_cannot_preload.getStatus());
                        }
                        i++;
                        c = 0;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
            int i2 = 0;
            while (i2 < targetPkgs2.size()) {
                PreloadPkgState preloadPkgState = targetPkgs2.get(i2);
                if (!preloadPkgState.isPreload()) {
                    try {
                        String packageName2 = preloadPkgState.getPkgName();
                        int uid = preloadPkgState.getUid();
                        long lastPss = preloadPkgState.getLastPss();
                        long pss2 = lastPss > 0 ? lastPss : preloadPkgState.getAveragePss();
                        if (pss2 <= 0) {
                            pss = 102400;
                        } else {
                            pss = pss2;
                        }
                        targetPkgs = targetPkgs2;
                        pkgList.add(new PkgInfo(packageName2, uid, pss, order));
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                } else {
                    targetPkgs = targetPkgs2;
                    preloadPkgState.setPreloadOrder(order);
                    PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                    StringBuilder sb = new StringBuilder();
                    sb.append(reason);
                    sb.append(": AlreadyPreloaded");
                    preloadHistoryLog.addPreloadInfo(sb.toString(), preloadPkgState.getPkgName(), preloadPkgState.getUid());
                    if (this.mPreloadTest != null) {
                        this.mPreloadTest.reportPreloadStatus(preloadPkgState.getPkgName(), PreloadReportStatus.skip_already_preloaded.getStatus());
                    }
                }
                i2++;
                targetPkgs2 = targetPkgs;
            }
            return pkgList;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void preloadPkgs(ArrayList<String> pkgStrList, String reason) {
        ArrayList<PkgInfo> pkgList = new ArrayList<>();
        String uidStr = "";
        if (!(pkgStrList == null || pkgStrList.size() == 0)) {
            pkgList.addAll(preloadPkgsFilter(pkgStrList, reason));
            for (int i = 0; i < pkgList.size(); i++) {
                PkgInfo info = pkgList.get(i);
                if (preloadPkgInternel(info.getPkgName(), info.getUid(), info.getOrder(), reason)) {
                    uidStr = uidStr + info.getUid() + " ";
                }
            }
            this.mPreloadHistoryLog.addPreloadAllInfo(reason, uidStr);
        }
    }

    public void preloadPkgsForAI(ArrayList<String> pkgStrList) {
        if (this.mAIPreloadSwitch) {
            if (ColorAppStartupManager.getInstance().isInSuperPowerSavingMode()) {
                this.mPreloadHistoryLog.i(TAG, "don't preload apps in superPowerSaving mode");
                return;
            }
            ArrayList<PkgInfo> pkgList = new ArrayList<>();
            String uidStr = "";
            if (!(pkgStrList == null || pkgStrList.size() == 0)) {
                pkgList.addAll(preloadPkgsFilter(pkgStrList, PRELOAD_REASON_AI));
                for (int i = 0; i < pkgList.size(); i++) {
                    uidStr = uidStr + pkgList.get(i).getUid() + " ";
                }
                this.mPreloadHistoryLog.addPreloadAllInfo("AI pending", uidStr);
                this.mAIPreload.preloadPkgsForAI(pkgList);
            }
        }
    }

    public boolean isPkgPreload(String pkgName, int userId) {
        if (!isPreloadEnable()) {
            return false;
        }
        synchronized (this.mLock) {
            PreloadPkgState preloadPkgState = this.mPreloadPkgs.get(pkgName, userId);
            if (preloadPkgState == null) {
                return false;
            }
            if (preloadPkgState.isPreload()) {
                return true;
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0083, code lost:
        if (r1 == false) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0085, code lost:
        if (r12 != false) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0087, code lost:
        com.android.server.am.ColorHansManager.getInstance().unFreezeForPreload(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x008e, code lost:
        return r1;
     */
    private boolean setPkgPreload(String pkgName, int uid, String reason, boolean isPreload) {
        int userId = UserHandle.getUserId(uid);
        synchronized (this.mLock) {
            PreloadPkgState ps = this.mPreloadPkgs.get(pkgName, userId);
            if (ps == null) {
                return false;
            }
            boolean isChanged = ps.isPreload() ^ isPreload;
            ps.setPreload(isPreload);
            if (isChanged) {
                if (isPreload) {
                    ps.setLastPreloadTime(System.currentTimeMillis());
                    ps.setPreloadReason(reason);
                    ps.setColdLaunch(false);
                    this.mPreloadInfoCollector.updatePreloadPkgInfo(uid, pkgName, reason, ps.getVersionName());
                } else {
                    this.mMainHandler.post(new Runnable(ps.isFasEnable(), pkgName, uid) {
                        /* class com.android.server.am.$$Lambda$ColorResourcePreloadManager$_aXQ6z08eASc2_4PZMmDDvlCqQw */
                        private final /* synthetic */ boolean f$1;
                        private final /* synthetic */ String f$2;
                        private final /* synthetic */ int f$3;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                        }

                        public final void run() {
                            ColorResourcePreloadManager.this.lambda$setPkgPreload$3$ColorResourcePreloadManager(this.f$1, this.f$2, this.f$3);
                        }
                    });
                    ps.setMemoryCompressed(false);
                    this.mPreloadHistoryLog.addProcessInfo(reason, pkgName, uid);
                    if (reason.equals(PRELOAD_RESET_REASON_USER_START)) {
                        ps.setHit(true);
                        this.mPreloadInfoCollector.updatePreloadHitCount(ps.getUid());
                    }
                }
                if (this.mAIPreloadSwitch) {
                    this.mAIPreload.updateAIPreloadPkgs(ps, isPreload);
                }
            }
            if (!isPreload && (reason.contains(PRELOAD_RESET_REASON_PROCESS_DIED) || reason.contains(PRELOAD_RESET_REASON_PROCESS_KILLED))) {
                ps.setColdLaunch(false);
            }
        }
    }

    public /* synthetic */ void lambda$setPkgPreload$3$ColorResourcePreloadManager(boolean flag, String pkgName, int uid) {
        if (!flag) {
            updateFasState(pkgName, uid, 0);
            PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
            preloadHistoryLog.addKeyInfo(uid + ", mode:0");
        }
    }

    public void handleProcessStart(ProcessRecord app) {
        if (isPreloadEnable()) {
            if (app.hostingRecord != null) {
                OppoBaseProcessRecord baseProcessRecord = typeCasting(app);
                OppoBaseHostingRecord baseHostingRecord = typeCasting(app.hostingRecord);
                if (baseProcessRecord != null && baseHostingRecord != null && baseHostingRecord.isRPLaunch()) {
                    baseProcessRecord.setRPLaunch(true);
                    baseHostingRecord.setRPLaunch(false);
                    handlePreloadSucceed(app.info.packageName, app.uid, app.pid, app.hostingRecord);
                    return;
                }
                return;
            }
            handleMainProcessStart(app.info.packageName, app.processName, app.uid, app.pid);
        }
    }

    private void handlePreloadSucceed(String pkgName, int uid, int pid, HostingRecord hostingRecord) {
        if (setPkgPreload(pkgName, uid, hostingRecord.getType(), true)) {
            PreloadTest preloadTest = this.mPreloadTest;
            if (preloadTest != null) {
                preloadTest.reportPreloadStatus(pkgName, PreloadReportStatus.process_preloaded.getStatus());
            }
            synchronized (this.mLock) {
                PreloadPkgState ps = this.mPreloadPkgs.get(pkgName, UserHandle.getUserId(uid));
                if (ps != null) {
                    ps.setPid(pid);
                    OppoBaseHostingRecord baseHostingRecord = typeCasting(hostingRecord);
                    if (baseHostingRecord != null) {
                        ps.setPreloadOrder(baseHostingRecord.getOrder());
                    }
                }
            }
        }
    }

    private void handleMainProcessStart(String pkgName, String processName, int uid, int pid) {
        synchronized (this.mLock) {
            PreloadPkgState ps = this.mPreloadPkgs.get(pkgName, UserHandle.getUserId(uid));
            if (ps != null) {
                if (ps.isPreload()) {
                    ArrayList<Integer> pids = ps.getPids();
                    ArrayList<String> processNameList = ps.getProcessNameList();
                    if (!pids.contains(Integer.valueOf(pid))) {
                        pids.add(Integer.valueOf(pid));
                    }
                    if (!processNameList.contains(processName)) {
                        processNameList.add(processName);
                    }
                    PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                    String str = TAG;
                    preloadHistoryLog.i(str, "handleProcessStart pid: " + pid + " processName: " + processName);
                } else if (ps.getProcessName().equals(processName)) {
                    PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
                    String str2 = TAG;
                    preloadHistoryLog2.i(str2, "handleMainProcessStart processName: " + processName + " uid: " + uid);
                    ps.setColdLaunch(true);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00d1, code lost:
        setPkgPreload(r2, r4, "process_killed : " + r15, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00e5, code lost:
        if (r5 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00e7, code lost:
        preloadForVip(r2, android.os.UserHandle.getUserId(r4));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        return;
     */
    public void handleProcessKilled(ProcessRecord app, int pid, String reason) {
        if (isPreloadEnable()) {
            OppoBaseProcessRecord baseProcessRecord = typeCasting(app);
            if (baseProcessRecord != null && baseProcessRecord.isRPLaunch()) {
                baseProcessRecord.setRPLaunch(false);
            }
            String pkgName = app.info.packageName;
            String processName = app.processName;
            int uid = app.uid;
            boolean isVipPkg = false;
            synchronized (this.mLock) {
                PreloadPkgState ps = this.mPreloadPkgs.get(pkgName, UserHandle.getUserId(uid));
                if (ps != null) {
                    if (this.vipPkgList != null && this.vipPkgList.contains(pkgName) && ps.getProcessName().equals(processName)) {
                        isVipPkg = true;
                    }
                    if (ps.getPid() != pid) {
                        if (ps.getPids().remove(Integer.valueOf(pid))) {
                            ps.getProcessNameList().remove(processName);
                            PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                            String str = TAG;
                            preloadHistoryLog.i(str, "handleProcessKilled pid: " + pid + " processName: " + processName);
                        }
                        if (isVipPkg) {
                            preloadForVip(pkgName, UserHandle.getUserId(uid));
                        }
                    } else if (!ps.isPreload()) {
                        if (isVipPkg) {
                            preloadForVip(pkgName, UserHandle.getUserId(uid));
                        }
                    } else {
                        PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
                        String str2 = TAG;
                        preloadHistoryLog2.i(str2, "handleProcessKilled pkgName: " + pkgName + " processName: " + processName);
                        if (this.mPreloadTest != null) {
                            this.mPreloadTest.reportPreloadStatus(pkgName, PreloadReportStatus.process_killed.getStatus());
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00d3, code lost:
        setPkgPreload(r2, r4, com.android.server.am.ColorResourcePreloadManager.PRELOAD_RESET_REASON_PROCESS_DIED, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00d8, code lost:
        if (r6 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00da, code lost:
        preloadForVip(r2, android.os.UserHandle.getUserId(r4));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        return;
     */
    public void handleProcessDied(ProcessRecord app) {
        if (isPreloadEnable()) {
            OppoBaseProcessRecord baseProcessRecord = typeCasting(app);
            if (baseProcessRecord != null && baseProcessRecord.isRPLaunch()) {
                baseProcessRecord.setRPLaunch(false);
            }
            String pkgName = app.info.packageName;
            String processName = app.processName;
            int uid = app.uid;
            int pid = app.pid;
            boolean isVipPkg = false;
            synchronized (this.mLock) {
                PreloadPkgState ps = this.mPreloadPkgs.get(pkgName, UserHandle.getUserId(uid));
                if (ps != null) {
                    if (this.vipPkgList != null && this.vipPkgList.contains(pkgName) && ps.getProcessName().equals(processName)) {
                        isVipPkg = true;
                    }
                    if (ps.getPid() != pid) {
                        if (ps.getPids().remove(Integer.valueOf(pid))) {
                            ps.getProcessNameList().remove(processName);
                            PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                            String str = TAG;
                            preloadHistoryLog.i(str, "handleProcessDied pid: " + pid + " processName: " + processName);
                        }
                        if (isVipPkg) {
                            preloadForVip(pkgName, UserHandle.getUserId(uid));
                        }
                    } else if (!ps.isPreload()) {
                        if (isVipPkg) {
                            preloadForVip(pkgName, UserHandle.getUserId(uid));
                        }
                    } else {
                        PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
                        String str2 = TAG;
                        preloadHistoryLog2.i(str2, "handleProcessDied pkgName: " + pkgName + " processName: " + processName);
                        if (this.mPreloadTest != null) {
                            this.mPreloadTest.reportPreloadStatus(pkgName, PreloadReportStatus.process_died.getStatus());
                        }
                    }
                }
            }
        }
    }

    private void preloadForVip(String pkgName, int userId) {
        PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
        String str = TAG;
        preloadHistoryLog.i(str, "preload for vip: " + pkgName + ", delay " + getVipDelayTime() + "S to preload");
        this.mMainHandler.postDelayed(new Runnable(pkgName, userId) {
            /* class com.android.server.am.$$Lambda$ColorResourcePreloadManager$jwykCjbxf22pvnIpA4x1VZeC0A */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ColorResourcePreloadManager.this.lambda$preloadForVip$4$ColorResourcePreloadManager(this.f$1, this.f$2);
            }
        }, (long) (getVipDelayTime() * ColorFreeformManagerService.FREEFORM_CALLER_UID));
    }

    public /* synthetic */ void lambda$preloadForVip$4$ColorResourcePreloadManager(String pkgName, int userId) {
        preloadPkg(pkgName, userId, PRELOAD_REASON_VIP);
    }

    public void handleActivityStart(String pkgName, String processName, int uid) {
        if (isPreloadEnable() && setPkgPreload(pkgName, uid, PRELOAD_RESET_REASON_USER_START, false)) {
            PreloadTest preloadTest = this.mPreloadTest;
            if (preloadTest != null) {
                preloadTest.reportPreloadStatus(pkgName, PreloadReportStatus.user_start.getStatus());
            }
            this.mMainHandler.post(new Runnable(processName, uid) {
                /* class com.android.server.am.$$Lambda$ColorResourcePreloadManager$wWcGzU4zc4prnPOqlHZXtaGlHg */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    ColorResourcePreloadManager.this.lambda$handleActivityStart$5$ColorResourcePreloadManager(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$handleActivityStart$5$ColorResourcePreloadManager(String processName, int uid) {
        OppoBaseProcessRecord baseProcessRecord;
        synchronized (this.mAms) {
            PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
            String str = TAG;
            preloadHistoryLog.i(str, "handleActivityStart: " + processName + " " + uid);
            ProcessRecord app = (ProcessRecord) this.mAms.mProcessList.mProcessNames.get(processName, uid);
            if (!(app == null || (baseProcessRecord = typeCasting(app)) == null || !baseProcessRecord.isRPLaunch())) {
                baseProcessRecord.setRPLaunch(false);
            }
        }
    }

    public void notifyLaunchTime(ApplicationInfo appInfo, String className, long launchTime) {
        if (isPreloadEnable()) {
            String pkgName = appInfo.packageName;
            int uid = appInfo.uid;
            synchronized (this.mLock) {
                PreloadPkgState ps = this.mPreloadPkgs.get(pkgName, UserHandle.getUserId(uid));
                if (ps != null) {
                    if (ps.isHit()) {
                        PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                        String str = TAG;
                        preloadHistoryLog.i(str, "Preload hit pkgName: " + pkgName + " uid: " + uid + " activity: " + className + " launchTime: " + launchTime);
                        ps.setPreloadLaunchTime(launchTime);
                        ps.setHit(false);
                        this.mPreloadHistoryLog.addLaunchInfo(appInfo, className, launchTime, "Preload");
                        this.mPreloadInfoCollector.recordPreloadLaunchTime(ps.getUid(), launchTime);
                    } else if (ps.isColdLaunch()) {
                        PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
                        String str2 = TAG;
                        preloadHistoryLog2.i(str2, "Normal pkgName: " + pkgName + " uid: " + uid + " activity: " + className + " launchTime: " + launchTime);
                        ps.setNormalLaunchTime(launchTime);
                        ps.setColdLaunch(false);
                        this.mPreloadHistoryLog.addLaunchInfo(appInfo, className, launchTime, "Normal");
                        this.mPreloadInfoCollector.recordNormalLaunchTime(ps.getUid(), launchTime);
                    }
                }
            }
        }
    }

    private boolean isFASEnabled(int uid, String packageName) {
        boolean flag = false;
        long token = Binder.clearCallingIdentity();
        try {
            IAppOpsService appOpsService = this.mAms.getAppOpsService();
            if (appOpsService != null) {
                boolean z = true;
                if (appOpsService.checkOperation(70, uid, packageName) != 1) {
                    z = false;
                }
                flag = z;
            }
        } catch (RemoteException e) {
            String str = TAG;
            Log.e(str, "updateFasState exception:" + e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
        Binder.restoreCallingIdentity(token);
        return flag;
    }

    private void updateFasState(String pkgName, int uid, int mode) {
        long token = Binder.clearCallingIdentity();
        try {
            IAppOpsService appOpsService = this.mAms.getAppOpsService();
            if (appOpsService != null) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "updateFasState: pkgName = " + pkgName + ", uid = " + uid + ", mode = " + mode);
                appOpsService.setMode(70, uid, pkgName, mode);
            }
        } catch (RemoteException e) {
            String str2 = TAG;
            Log.e(str2, "updateFasState exception:" + e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
        Binder.restoreCallingIdentity(token);
    }

    public void notifyBindApplicationFinished(String pkgName, int userId, int pid) {
        if (isPreloadEnable()) {
            synchronized (this.mLock) {
                PreloadPkgState ps = this.mPreloadPkgs.get(pkgName, userId);
                if (ps != null) {
                    if (ps.isPreload()) {
                        this.mPreloadHistoryLog.addBindApplicationFinishedInfo(pkgName, ps.getUid(), pid, System.currentTimeMillis() - ps.getLastPreloadTime());
                        if (ps.getPid() == pid) {
                            freezeAppForPreload(ps.getPkgName(), ps.getUid());
                            if (this.mPreloadTest != null) {
                                this.mPreloadTest.reportPreloadStatus(pkgName, PreloadReportStatus.bind_app_finished.getStatus());
                            }
                        } else {
                            MemoryCompress.compress(pid, "all");
                        }
                    }
                }
            }
        }
    }

    public boolean isAllowStart(String pkgName, int uid) {
        PreloadTest preloadTest;
        boolean allowStart = isAllowBootStart(pkgName, uid) || isAllowAssStart(pkgName, uid);
        if (allowStart && (preloadTest = this.mPreloadTest) != null) {
            preloadTest.reportPreloadStatus(pkgName, PreloadReportStatus.allow_boot_ass_start.getStatus());
        }
        return allowStart;
    }

    private boolean isAllowBootStart(String pkgName, int uid) {
        return OppoListManager.getInstance().isAutoStartWhiteList(pkgName, UserHandle.getUserId(uid));
    }

    private boolean isAllowAssStart(String pkgName, int uid) {
        return ColorAppStartupManager.getInstance().inAssociateStartWhiteList(pkgName, UserHandle.getUserId(uid)) || ColorAppStartupManager.getInstance().inProtectWhiteList(pkgName);
    }

    private ComponentBlockResult isBlockedByPreload(String pkgName, int uid) {
        synchronized (this.mLock) {
            PreloadPkgState preloadPkgState = this.mPreloadPkgs.get(pkgName, UserHandle.getUserId(uid));
            if (preloadPkgState == null) {
                return ComponentBlockResult.ALLOW_NONE_PRELOAD_TARGET;
            } else if (!preloadPkgState.isPreload()) {
                return ComponentBlockResult.ALLOW_NOT_PRELOAD_STATE;
            } else if (isAllowStart(pkgName, uid)) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "pkgName = " + pkgName + " uid = " + uid + " isAllowStart");
                return ComponentBlockResult.ALLOW_AUTO_ASSOCIATE_START;
            } else {
                return ComponentBlockResult.BLOCK_COMPONENT;
            }
        }
    }

    private ComponentBlockResult isBlockedByPreload(int callerPid, int callerUid, String callerPackage, int calleeUid, String calleePackage, String calleeProcessName) {
        synchronized (this.mLock) {
            PreloadPkgState preloadPkgState = this.mPreloadPkgs.get(calleePackage, UserHandle.getUserId(calleeUid));
            if (preloadPkgState == null) {
                return ComponentBlockResult.ALLOW_NONE_PRELOAD_TARGET;
            } else if (!preloadPkgState.isPreload()) {
                return ComponentBlockResult.ALLOW_NOT_PRELOAD_STATE;
            } else if (isAllowStart(calleePackage, calleeUid)) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "pkgName = " + calleePackage + " uid = " + calleeUid + " isAllowStart");
                return ComponentBlockResult.ALLOW_AUTO_ASSOCIATE_START;
            } else if (callerUid == calleeUid && callerPackage.equals(calleePackage) && (callerPid == preloadPkgState.getPid() || preloadPkgState.getProcessName().equals(calleeProcessName))) {
                return ComponentBlockResult.ALLOW_MAIN_PROCESS_START;
            } else if (callerPackage.equals(ColorFreeformManagerService.FREEFORM_CALLER_PKG)) {
                return ComponentBlockResult.ALLOW_AUTO_ASSOCIATE_START;
            } else {
                return ComponentBlockResult.BLOCK_COMPONENT;
            }
        }
    }

    public boolean preloadServiceBlock(int callerPid, int callerUid, String callerPackage, int calleeUid, String calleePackage, String calleeProcessName, String cpnName, boolean isBind) {
        int i;
        if (!isPreloadEnable() || !preloadBlockedForGlobal(calleeUid, calleePackage, PRELOAD_RESET_REASON_GLOBAL_POLICY)) {
            return false;
        }
        ComponentBlockResult ret = isBlockedByPreload(callerPid, callerUid, callerPackage, calleeUid, calleePackage, calleeProcessName);
        if (ret == ComponentBlockResult.BLOCK_COMPONENT) {
            PreloadRUSController preloadRUSController = this.mPreloadRUSController;
            if (preloadRUSController == null) {
                i = calleeUid;
            } else if (preloadRUSController.isPreloadRestrictWhiteList("service_restriction", callerPackage, calleePackage, cpnName, "")) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "preloadServiceBlock: WL-" + calleePackage);
                StringBuilder sb = new StringBuilder();
                sb.append("allow_component ");
                sb.append(ret);
                sb.append(isBind ? " bind_service" : " start_service");
                sb.append(" [ ");
                sb.append(callerPid);
                sb.append(" ");
                sb.append(callerUid);
                sb.append(" ");
                sb.append(callerPackage);
                sb.append(" ");
                sb.append(cpnName);
                sb.append(" ]");
                setPkgPreload(calleePackage, calleeUid, sb.toString(), false);
                return false;
            } else {
                i = calleeUid;
            }
            PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
            String str2 = TAG;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("preloadServiceBlock: ");
            sb2.append(i);
            sb2.append("|");
            sb2.append(calleePackage);
            sb2.append("|");
            sb2.append(cpnName);
            sb2.append("|");
            sb2.append(calleeProcessName);
            sb2.append(" from ");
            sb2.append(callerPid);
            sb2.append("|");
            sb2.append(callerPackage);
            sb2.append(isBind ? " bind_service" : " start_service");
            preloadHistoryLog2.i(str2, sb2.toString());
            return true;
        } else if (ret == ComponentBlockResult.ALLOW_AUTO_ASSOCIATE_START) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("allow_component ");
            sb3.append(ret);
            sb3.append(isBind ? " bind_service" : " start_service");
            sb3.append(" [ ");
            sb3.append(callerPid);
            sb3.append(" ");
            sb3.append(callerUid);
            sb3.append(" ");
            sb3.append(callerPackage);
            sb3.append(" ");
            sb3.append(cpnName);
            sb3.append(" ]");
            setPkgPreload(calleePackage, calleeUid, sb3.toString(), false);
            return false;
        } else if (ret != ComponentBlockResult.ALLOW_MAIN_PROCESS_START) {
            return false;
        } else {
            StringBuilder sb4 = new StringBuilder();
            sb4.append("allow_component ");
            sb4.append(ret);
            sb4.append(isBind ? " bind_service" : " start_service");
            sb4.append(" [ ");
            sb4.append(callerPid);
            sb4.append(" ");
            sb4.append(callerUid);
            sb4.append(" ");
            sb4.append(callerPackage);
            sb4.append(" ");
            sb4.append(cpnName);
            sb4.append(" ]");
            this.mPreloadHistoryLog.addProcessInfo(sb4.toString(), calleePackage, calleeUid);
            return false;
        }
    }

    public boolean preloadProviderBlock(int callerPid, int callerUid, String callerPackage, int calleeUid, String calleePackage, String calleeProcessName, String cpnName) {
        int i;
        if (!isPreloadEnable() || !preloadBlockedForGlobal(calleeUid, calleePackage, PRELOAD_RESET_REASON_GLOBAL_POLICY)) {
            return false;
        }
        ComponentBlockResult ret = isBlockedByPreload(callerPid, callerUid, callerPackage, calleeUid, calleePackage, calleeProcessName);
        if (ret == ComponentBlockResult.BLOCK_COMPONENT) {
            PreloadRUSController preloadRUSController = this.mPreloadRUSController;
            if (preloadRUSController == null) {
                i = calleeUid;
            } else if (preloadRUSController.isPreloadRestrictWhiteList("provider_restriction", callerPackage, calleePackage, cpnName, "")) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "preloadProviderBlock: WL-" + calleePackage);
                setPkgPreload(calleePackage, calleeUid, "allow_component " + ret + " provider [ " + callerPid + " " + callerUid + " " + callerPackage + " " + cpnName + " ]", false);
                return false;
            } else {
                i = calleeUid;
            }
            PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
            String str2 = TAG;
            preloadHistoryLog2.i(str2, "preloadProviderBlock: " + i + "|" + calleePackage + "|" + cpnName + "|" + calleeProcessName + " from " + callerPid + "|" + callerPackage);
            return true;
        } else if (ret == ComponentBlockResult.ALLOW_AUTO_ASSOCIATE_START) {
            setPkgPreload(calleePackage, calleeUid, "allow_component " + ret + " provider [ " + callerPid + " " + callerUid + " " + callerPackage + " " + cpnName + " ]", false);
            return false;
        } else if (ret != ComponentBlockResult.ALLOW_MAIN_PROCESS_START) {
            return false;
        } else {
            this.mPreloadHistoryLog.addProcessInfo("allow_component " + ret + " provider [ " + callerPid + " " + callerUid + " " + callerPackage + " " + cpnName + " ]", calleePackage, calleeUid);
            return false;
        }
    }

    public boolean preloadBroadcastBlock(BroadcastRecord r, Object o) {
        String calleePackage;
        boolean order;
        int calleeUid;
        if (!isPreloadEnable()) {
            return false;
        }
        int calleeUid2 = -1;
        if (o instanceof BroadcastFilter) {
            calleeUid2 = ((BroadcastFilter) o).owningUid;
            calleePackage = ((BroadcastFilter) o).packageName;
        } else if (o instanceof ResolveInfo) {
            calleeUid2 = ((ResolveInfo) o).activityInfo.applicationInfo.uid;
            calleePackage = ((ResolveInfo) o).activityInfo.packageName;
        } else {
            calleePackage = "";
        }
        int callerUid = r.callingUid;
        String callerPackage = r.callerPackage;
        String action = r.intent.getAction();
        boolean order2 = r.ordered;
        if (!preloadBlockedForGlobal(calleeUid2, calleePackage, PRELOAD_RESET_REASON_GLOBAL_POLICY)) {
            return false;
        }
        ComponentBlockResult ret = isBlockedByPreload(calleePackage, calleeUid2);
        if (ret == ComponentBlockResult.BLOCK_COMPONENT) {
            PreloadRUSController preloadRUSController = this.mPreloadRUSController;
            if (preloadRUSController != null) {
                order = order2;
                if (preloadRUSController.isPreloadRestrictWhiteList("broadcast_restriction", callerPackage, calleePackage, "", action)) {
                    PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                    String str = TAG;
                    preloadHistoryLog.i(str, "preloadBroadcastBlock: WL-" + calleePackage);
                    setPkgPreload(calleePackage, calleeUid2, "allow_component " + ret + " broadcast [ " + callerUid + " " + callerPackage + " " + action + " ]", false);
                    return false;
                }
                calleeUid = calleeUid2;
            } else {
                calleeUid = calleeUid2;
                order = order2;
            }
            PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
            String str2 = TAG;
            preloadHistoryLog2.i(str2, "preloadBroadcastBlock: " + calleeUid + "|" + calleePackage + "|" + action + "|" + order + " from " + callerPackage);
            return true;
        } else if (ret != ComponentBlockResult.ALLOW_AUTO_ASSOCIATE_START) {
            return false;
        } else {
            setPkgPreload(calleePackage, calleeUid2, "allow_component " + ret + " broadcast [ " + callerUid + " " + callerPackage + " " + action + " ]", false);
            return false;
        }
    }

    public boolean preloadSyncBlock(int uid, String pkgName) {
        if (!isPreloadEnable() || !preloadBlockedForGlobal(uid, pkgName, PRELOAD_RESET_REASON_GLOBAL_POLICY)) {
            return false;
        }
        ComponentBlockResult ret = isBlockedByPreload(pkgName, uid);
        if (ret == ComponentBlockResult.BLOCK_COMPONENT) {
            PreloadRUSController preloadRUSController = this.mPreloadRUSController;
            if (preloadRUSController == null || !preloadRUSController.isPreloadRestrictWhiteList("sync_restriction", "", pkgName, "", "")) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "preloadSyncBlock: " + uid + "|" + pkgName);
                return true;
            }
            PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
            String str2 = TAG;
            preloadHistoryLog2.i(str2, "preloadSyncBlock: WL-" + pkgName);
            setPkgPreload(pkgName, uid, "allow_component " + ret + " sync", false);
            return false;
        }
        if (ret == ComponentBlockResult.ALLOW_AUTO_ASSOCIATE_START) {
            setPkgPreload(pkgName, uid, "allow_component " + ret + " sync", false);
        }
        return false;
    }

    public boolean preloadJobBlock(int uid, String pkgName) {
        if (!isPreloadEnable() || !preloadBlockedForGlobal(uid, pkgName, PRELOAD_RESET_REASON_GLOBAL_POLICY)) {
            return false;
        }
        ComponentBlockResult ret = isBlockedByPreload(pkgName, uid);
        if (ret == ComponentBlockResult.BLOCK_COMPONENT) {
            PreloadRUSController preloadRUSController = this.mPreloadRUSController;
            if (preloadRUSController == null || !preloadRUSController.isPreloadRestrictWhiteList("job_restriction", "", pkgName, "", "")) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "preloadJobBlock true: " + uid + "|" + pkgName);
                return true;
            }
            PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
            String str2 = TAG;
            preloadHistoryLog2.i(str2, "preloadJobBlock: WL-" + pkgName);
            setPkgPreload(pkgName, uid, "allow_component " + ret + " job", false);
            return false;
        }
        if (ret == ComponentBlockResult.ALLOW_AUTO_ASSOCIATE_START) {
            setPkgPreload(pkgName, uid, "allow_component " + ret + " job", false);
        }
        return false;
    }

    public boolean preloadAlarmBlock(String action, int uid, String pkgName) {
        if (!isPreloadEnable() || !preloadBlockedForGlobal(uid, pkgName, PRELOAD_RESET_REASON_GLOBAL_POLICY)) {
            return false;
        }
        ComponentBlockResult ret = isBlockedByPreload(pkgName, uid);
        if (ret == ComponentBlockResult.BLOCK_COMPONENT) {
            PreloadRUSController preloadRUSController = this.mPreloadRUSController;
            if (preloadRUSController == null || !preloadRUSController.isPreloadRestrictWhiteList("alarm_restriction", "", pkgName, "", "")) {
                PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
                String str = TAG;
                preloadHistoryLog.i(str, "preloadAlarmBlock true: " + uid + "|" + pkgName);
                return true;
            }
            PreloadHistoryLog preloadHistoryLog2 = this.mPreloadHistoryLog;
            String str2 = TAG;
            preloadHistoryLog2.i(str2, "preloadAlarmBlock: WL-" + pkgName);
            setPkgPreload(pkgName, uid, "allow_component " + ret + " alarm " + action, false);
            return false;
        }
        if (ret == ComponentBlockResult.ALLOW_AUTO_ASSOCIATE_START) {
            setPkgPreload(pkgName, uid, "allow_component " + ret + " alarm " + action, false);
        }
        return false;
    }

    private boolean preloadBlockedForGlobal(int uid, String pkgName, String reason) {
        if (this.mChinaModel) {
            return true;
        }
        setPkgPreload(pkgName, uid, reason, false);
        return false;
    }

    private boolean isNotAllowedPreload(String pkgName, int uid) {
        return isAllowBootStart(pkgName, uid) && isAllowAssStart(pkgName, uid);
    }

    private boolean isChinaModel() {
        try {
            if (this.mContext == null || this.mContext.getPackageManager() == null) {
                return true;
            }
            return !this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkCPUBusy() {
        long threshold = getCpuPercent();
        if (threshold <= CPU_USAGE_THRESHOLD) {
            return false;
        }
        this.mPreloadHistoryLog.i(TAG, "CPU usage is high, stop preload");
        PreloadHistoryLog preloadHistoryLog = this.mPreloadHistoryLog;
        preloadHistoryLog.addKeyInfo("CpuBusy " + threshold);
        return true;
    }

    private final long getCpuPercent() {
        if (getSample(this.cpuData1) != 0) {
            return -1;
        }
        try {
            Thread.sleep(20);
            if (getSample(this.cpuData2) != 0) {
                return -1;
            }
            return 100 - (((this.cpuData2.mIdleTickTime - this.cpuData1.mIdleTickTime) * 100) / (this.cpuData2.mTotalTickTime - this.cpuData1.mTotalTickTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int getSample(CpuData cpuData) {
        long[] sysCpu = this.mSystemCpuData;
        if (!Process.readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null)) {
            Log.e(TAG, "read /proc/stat errror");
            return -1;
        }
        cpuData.mTotalTickTime = sysCpu[0] + sysCpu[1] + sysCpu[2] + sysCpu[3] + sysCpu[4] + sysCpu[5] + sysCpu[6];
        cpuData.mIdleTickTime = sysCpu[3];
        return 0;
    }

    /* access modifiers changed from: private */
    public class CpuData {
        long mIdleTickTime = 0;
        long mTotalTickTime = 0;

        public CpuData() {
        }
    }

    private void getPreloadInfoFromDB() {
        ColorResourcePreloadDBManager dbManager = ColorResourcePreloadDBManager.getInstance();
        if (dbManager != null && dbManager.isDBOpen()) {
            dbManager.beginTransaction();
            try {
                Cursor cursor = dbManager.fetch(ColorResourcePreloadDatabaseHelper.PRELOAD_TABLE_NAME);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String pkgName = cursor.getString(cursor.getColumnIndex(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NAME));
                        int uid = cursor.getInt(cursor.getColumnIndex(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID));
                        long meminfo = cursor.getLong(cursor.getColumnIndex(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_MEMORY));
                        long hotnessCnt = cursor.getLong(cursor.getColumnIndex(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_HOTNESS_COUNT));
                        synchronized (this.mLock) {
                            PreloadPkgState pkgState = this.mPreloadPkgs.get(pkgName, UserHandle.getUserId(uid));
                            if (pkgState != null) {
                                pkgState.setAveragePss(meminfo);
                                pkgState.setHotnessCnt(hotnessCnt);
                            }
                        }
                    }
                    cursor.close();
                }
                dbManager.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } catch (Throwable th) {
                dbManager.endTransaction();
                throw th;
            }
            dbManager.endTransaction();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String formatDateTime(long time) {
        return new SimpleDateFormat("MM/dd HH:mm:ss.SSS").format(new Date(time));
    }

    private long getTotalPss(PreloadPkgState ps) {
        long pss = Debug.getPss(ps.getPid(), null, null);
        ArrayList<Integer> pids = ps.getPids();
        if (pids == null) {
            return pss;
        }
        Iterator<Integer> it = pids.iterator();
        while (it.hasNext()) {
            pss += Debug.getPss(it.next().intValue(), null, null);
        }
        return pss;
    }

    private void dumpPreloadInfo(FileDescriptor fd, PrintWriter pw) {
        PreloadPkgState preloadPkgState;
        StringBuilder sb;
        int i;
        int i2;
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        this.mAms.getMemoryInfo(memInfo);
        long availMem = memInfo.availMem / 1048576;
        long totalMem = memInfo.totalMem / 1048576;
        long totalPss = 0;
        synchronized (this.mLock) {
            try {
                StringBuffer sb2 = new StringBuffer();
                sb2.append("Preload Info(dumpsys activity preload)\n");
                sb2.append("\n");
                sb2.append("LowAvailMemThreshold = " + this.mLowAvailMemThreshold + "MB, availMem = " + availMem + "MB, totalMem = " + totalMem + "MB\n");
                sb2.append("\n");
                sb2.append("preload apps -- size ");
                StringBuilder sb3 = new StringBuilder();
                sb3.append(this.mPreloadPkgs.totalSize());
                sb3.append("\n");
                sb2.append(sb3.toString());
                int i3 = 0;
                int i4 = 0;
                while (i4 < this.mPreloadPkgs.getMap().size()) {
                    SparseArray<PreloadPkgState> userIdsPkgStatus = this.mPreloadPkgs.getMap().valueAt(i4);
                    int j = i3;
                    long totalPss2 = totalPss;
                    while (j < userIdsPkgStatus.size()) {
                        try {
                            preloadPkgState = userIdsPkgStatus.valueAt(j);
                            sb = new StringBuilder();
                            sb.append("pkg: ");
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                        try {
                            sb.append(String.format("%-40s", preloadPkgState.getPkgName()));
                            sb2.append(sb.toString());
                            sb2.append(" --uid: " + String.format("%4d", Integer.valueOf(preloadPkgState.getUid())));
                            if (this.mUidRunningList.contains(Integer.valueOf(preloadPkgState.getUid()))) {
                                sb2.append(" --(UR)");
                            }
                            if (preloadPkgState.isPreload()) {
                                StringBuilder sb4 = new StringBuilder();
                                sb4.append(" --Disable: ");
                                sb4.append(preloadPkgState.isLastDisabled ? "Y" : "N");
                                sb2.append(sb4.toString());
                                sb2.append(" --pid: " + String.format("%4d", Integer.valueOf(preloadPkgState.getPid())));
                                StringBuilder sb5 = new StringBuilder();
                                sb5.append(" --time: ");
                                i = i4;
                                sb5.append(String.format("%18s", formatDateTime(preloadPkgState.getLastPreloadTime())));
                                sb2.append(sb5.toString());
                                sb2.append(" --(P: " + preloadPkgState.getPreloadReason().substring(9) + ")");
                                sb2.append(" --(O: " + preloadPkgState.getPreloadOrder() + ")");
                                long pss = getTotalPss(preloadPkgState);
                                sb2.append(" --(PSS: " + String.format("%3d", Long.valueOf(pss / ColorDeviceStorageMonitorService.KB_BYTES)) + ")");
                                totalPss2 += pss;
                                i2 = 0;
                            } else {
                                i = i4;
                                StringBuilder sb6 = new StringBuilder();
                                sb6.append(" --(Launch: ");
                                sb6.append(String.format("%4d", Long.valueOf(preloadPkgState.getPreloadLaunchTime())));
                                sb6.append("/");
                                i2 = 0;
                                sb6.append(String.format("%4d", Long.valueOf(preloadPkgState.getNormalLaunchTime())));
                                sb6.append(")");
                                sb2.append(sb6.toString());
                            }
                            sb2.append("\n");
                            j++;
                            i3 = i2;
                            memInfo = memInfo;
                            i4 = i;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                    i4++;
                    totalPss = totalPss2;
                }
                sb2.append("\n");
                sb2.append("TotalPSS: " + (totalPss / ColorDeviceStorageMonitorService.KB_BYTES) + " MB");
                if (this.mUserVersion) {
                    if (this.mPreloadHistoryLog != null) {
                        pw.println(this.mPreloadHistoryLog.historyLogEncrypt(sb2.toString()));
                    }
                }
                pw.println(sb2.toString());
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private void dumpPreloadHistoryInfo(FileDescriptor fd, PrintWriter pw) {
        PreloadHistoryLog preloadHistoryLog;
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append("Preload History(dumpsys activity preload history)\n");
        sb.append("currentTime: ");
        sb.append(formatDateTime(System.currentTimeMillis()) + "\n");
        sb.append("\n");
        if (!this.mUserVersion || (preloadHistoryLog = this.mPreloadHistoryLog) == null) {
            pw.println(sb.toString());
        } else {
            pw.println(preloadHistoryLog.historyLogEncrypt(sb.toString()));
        }
        this.mPreloadHistoryLog.dumpLogBuffer(fd, pw);
    }

    static OppoBaseActivityOptions typeCasting(ActivityOptions activityOptions) {
        if (activityOptions != null) {
            return (OppoBaseActivityOptions) ColorTypeCastingHelper.typeCasting(OppoBaseActivityOptions.class, activityOptions);
        }
        return null;
    }

    static OppoBaseProcessRecord typeCasting(ProcessRecord pr) {
        if (pr != null) {
            return (OppoBaseProcessRecord) ColorTypeCastingHelper.typeCasting(OppoBaseProcessRecord.class, pr);
        }
        return null;
    }

    static OppoBaseHostingRecord typeCasting(HostingRecord hostingRecord) {
        if (hostingRecord != null) {
            return (OppoBaseHostingRecord) ColorTypeCastingHelper.typeCasting(OppoBaseHostingRecord.class, hostingRecord);
        }
        return null;
    }

    public void dumpPreload(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (isPreloadEnable()) {
            if (args.length == 1) {
                dumpPreloadInfo(fd, pw);
                dumpPreloadHistoryInfo(fd, pw);
            }
            if (SystemProperties.getBoolean("persist.sys.preload.dump", false) && args.length > 1) {
                if ("history".equals(args[1])) {
                    dumpPreloadHistoryInfo(fd, pw);
                }
                if ("add".equals(args[1])) {
                    synchronized (this.mLock) {
                        try {
                            String pkgName = args[2];
                            int uid = this.mContext.getPackageManager().getPackageUidAsUser(pkgName, Integer.parseInt(args[3]));
                            this.mPreloadPkgs.put(pkgName, UserHandle.getUserId(uid), new PreloadPkgState(pkgName, uid));
                        } catch (Exception e) {
                            Log.e(TAG, "dumpPreload exception: " + e);
                        }
                    }
                }
                if ("execute".equals(args[1])) {
                    preloadPkgs(PRELOAD_REASON_DEBUG);
                }
                if ("killall".equals(args[1])) {
                    stopAllPreloadPkgs();
                }
                if ("insert".equals(args[1])) {
                    test_database("insert", args[2], Integer.parseInt(args[3]), args[4]);
                }
                if ("delete".equals(args[1])) {
                    test_database("delete", args[2], Integer.parseInt(args[3]), args[4]);
                }
                if ("compress".equals(args[1]) && args.length >= 2) {
                    MemoryCompress.compress(Integer.parseInt(args[2]), "all");
                }
                if ("debugAI".equals(args[1])) {
                    this.mDebugAISwitch = Integer.parseInt(args[2]) == 1;
                }
                if ("forcestop".equals(args[1])) {
                    synchronized (this.mLock) {
                        for (int i = 0; i < this.mPreloadPkgs.getMap().size(); i++) {
                            SparseArray<PreloadPkgState> userIdsPkgStatus = this.mPreloadPkgs.getMap().valueAt(i);
                            for (int j = 0; j < userIdsPkgStatus.size(); j++) {
                                PreloadPkgState preloadPkgState = userIdsPkgStatus.valueAt(j);
                                if (this.mUidRunningList.contains(Integer.valueOf(preloadPkgState.getUid()))) {
                                    stopPreloadPkg(preloadPkgState.getPkgName(), preloadPkgState.getUid());
                                }
                            }
                        }
                    }
                }
                if ("preloadlog".equals(args[1])) {
                    this.mPreloadHistoryLog.setDynamicLog(true);
                }
                if ("info".equals(args[1])) {
                    this.mPreloadInfoCollector.dumpPreloadInfo();
                }
                if ("hotness".equals(args[1]) && this.minHeap != null) {
                    pw.println("MinHeap ---- size:" + this.minHeap.size());
                    pw.println(this.minHeap);
                    pw.println("Hotness List");
                    pw.println(getTopHotnessApp(this.mAIPreload.getAiSlotsMax()));
                }
                if ("deepthinker".equals(args[1])) {
                    pw.println("deepthinker:" + this.mAIPreload.registerDeepThinker());
                }
                if ("historyswitch".equals(args[1])) {
                    if (Integer.parseInt(args[2]) == 1) {
                        this.mUserVersion = false;
                    } else {
                        this.mUserVersion = true;
                    }
                }
                if ("vippreload".equals(args[1])) {
                    int delayTime = Integer.parseInt(args[2]);
                    setVipDelayTime(delayTime);
                    pw.println("vip preload delay " + delayTime + "S");
                }
                if ("disable".equals(args[1])) {
                    pw.println("disable ai preload");
                    disablePreload();
                }
            }
        }
    }

    private void test_database(String cmd, String pkgName, int uid, String version) {
        ColorResourcePreloadDBManager dbManager = ColorResourcePreloadDBManager.getInstance();
        if (dbManager != null && dbManager.isDBOpen()) {
            synchronized (ColorResourcePreloadDBManager.mDBLock) {
                dbManager.beginTransaction();
                try {
                    String str = TAG;
                    Log.i(str, "test_database...pkgName:" + pkgName + ", uid:" + uid + ", version:" + version);
                    if ("insert".equals(cmd)) {
                        String name = "";
                        int pkg_uid = -1;
                        int hotnessCnt = 0;
                        int bootCnt = 0;
                        int aiCnt = 0;
                        int totalCnt = 0;
                        int hitCnt = 0;
                        boolean isFound = false;
                        Cursor cursor = dbManager.fetch(ColorResourcePreloadDatabaseHelper.PRELOAD_TABLE_NAME, pkgName, uid);
                        if (cursor != null) {
                            DatabaseUtils.dumpCursor(cursor);
                            while (cursor.moveToNext()) {
                                name = cursor.getString(1);
                                pkg_uid = cursor.getInt(2);
                                hotnessCnt = cursor.getInt(3);
                                bootCnt = cursor.getInt(4);
                                aiCnt = cursor.getInt(5);
                                totalCnt = cursor.getInt(6);
                                hitCnt = cursor.getInt(7);
                                if (name.equals(pkgName) && uid == pkg_uid) {
                                    isFound = true;
                                }
                            }
                            cursor.close();
                        }
                        ContentValues values = new ContentValues();
                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NAME, pkgName);
                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, Integer.valueOf(uid));
                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_HOTNESS_COUNT, Integer.valueOf(hotnessCnt + 1));
                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_BOOT_COUNT, Integer.valueOf(bootCnt + 1));
                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_AI_COUNT, Integer.valueOf(aiCnt + 1));
                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_TOTAL_COUNT, Integer.valueOf(totalCnt + 1));
                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_HIT_COUNT, Integer.valueOf(hitCnt + 1));
                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_PRELOAD_LAUNCH_TIME, (Integer) 11);
                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NORMAL_LAUNCH_TIME, (Integer) 11);
                        values.put(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_MEMORY, (Integer) 11);
                        values.put("version", version);
                        if (isFound) {
                            Log.i(TAG, "test_database...update");
                            dbManager.update(ColorResourcePreloadDatabaseHelper.PRELOAD_TABLE_NAME, values, pkgName, uid);
                        } else {
                            Log.i(TAG, "test_database...insert");
                            dbManager.insert(ColorResourcePreloadDatabaseHelper.PRELOAD_TABLE_NAME, values);
                        }
                    } else if ("delete".equals(cmd)) {
                        Log.i(TAG, "test_database...delete");
                        dbManager.delete(ColorResourcePreloadDatabaseHelper.PRELOAD_TABLE_NAME, pkgName, uid);
                    }
                    dbManager.setTransactionSuccessful();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                } finally {
                    dbManager.endTransaction();
                }
            }
        }
    }
}
