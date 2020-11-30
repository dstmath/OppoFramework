package com.android.server.wm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.OppoManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.OppoGnssDuration;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class PswOppoAmsUtilsFeatrue implements IPswOppoAmsUtilsFeatrue {
    private static boolean DEBUG_AMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String FEATURE_ACT_FREQ_CONTROL = "oppo.ams.act.freqcontrol";
    private static final int MAX_AMKILL_LIST_SIZE = 8096;
    private static final int MAX_FRAME_SKIP_LIST_SIZE = 8096;
    private static final int MAX_LAUNCHTIME_LIST_SIZE = 8096;
    static final String TAG = "PswOppoAmsUtilsFeatrue";
    private static final String TAG_ActivityLaunchRecord = "ActivityLaunchRecord";
    private static final String TAG_AmKillRecord = "AmKillRecord";
    private static final String TAG_FrameSkipReporter = "FrameSkipReporter";
    private static final int TYPE_ANR = 1;
    private static final int TYPE_CRASH = 0;
    private static final int TYPE_INPUT_SLOW = 6;
    private static final int TYPE_LOW_MEMORY = 4;
    private static final int TYPE_MAIN_THREAD_BLOCKED = 5;
    private static final int TYPE_NONFLUENCY = 2;
    private static final int TYPE_START_SLOW = 3;
    private static List<AmKillEntry> mAmKillEntryListSelfLocked = new ArrayList();
    private static List<FrameSkipEntry> mFrameSkipEntryListSelfLocked = new ArrayList();
    private static PswOppoAmsUtilsFeatrue mInstance = null;
    private static List<LaunchTimeEntry> mLaunchTimeEntryListSelfLocked = new ArrayList();
    private static final Object mLock = new Object();
    private final String[] PROVIDER_KEYS_LIST = {"depends on provider", "in dying proc"};
    private final String TIMEOUT_PUBLISH_CONTENT_PROVIDERS = "timeout publishing content providers";
    private List<String> latestOIDTExceptionList = new ArrayList();
    private ActStartFreqControler mActStartFreqControler = null;
    private ActStartupAccelerator mActStartupAccelerator = null;
    private ActivityTaskManagerService mAtm;
    private boolean mIsSupportActFreqCon = false;
    private boolean mIsSystemReady = false;
    private KeyguardServiceRestartDelay mKeyguardServiceRestartDelay = null;

    public static PswOppoAmsUtilsFeatrue getInstance() {
        PswOppoAmsUtilsFeatrue pswOppoAmsUtilsFeatrue;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new PswOppoAmsUtilsFeatrue();
            }
            pswOppoAmsUtilsFeatrue = mInstance;
        }
        return pswOppoAmsUtilsFeatrue;
    }

    private PswOppoAmsUtilsFeatrue() {
    }

    public void init(ActivityTaskManagerService service) {
        Slog.d(TAG, "PswOppoAmsUtilsFeatrue init.");
        this.mAtm = service;
    }

    public void systemReady() {
        initActStartFreqControl();
        this.mActStartupAccelerator = new ActStartupAccelerator();
        this.mKeyguardServiceRestartDelay = new KeyguardServiceRestartDelay();
        this.mIsSystemReady = true;
    }

    private void initActStartFreqControl() {
        this.mIsSupportActFreqCon = this.mAtm.mContext.getPackageManager().hasSystemFeature(FEATURE_ACT_FREQ_CONTROL);
        Slog.i(TAG, "mIsSupportActFreqCon:" + this.mIsSupportActFreqCon);
        if (this.mIsSupportActFreqCon) {
            this.mActStartFreqControler = new ActStartFreqControler();
        }
    }

    public boolean needToControlActivityStartFreq(Intent intent) {
        ActStartFreqControler actStartFreqControler;
        if (!this.mIsSystemReady || !this.mIsSupportActFreqCon || (actStartFreqControler = this.mActStartFreqControler) == null) {
            return false;
        }
        return actStartFreqControler.doControlActivityStartFreq(intent);
    }

    private boolean accelerateActStartup(Intent intent) {
        ActStartupAccelerator actStartupAccelerator;
        if (this.mIsSystemReady && (actStartupAccelerator = this.mActStartupAccelerator) != null) {
            return actStartupAccelerator.doAccelerateActStartup(intent);
        }
        return false;
    }

    public boolean shouldDelayKeyguardServiceRestart(String shortName, boolean shouldDelay) {
        KeyguardServiceRestartDelay keyguardServiceRestartDelay;
        if (this.mIsSystemReady && (keyguardServiceRestartDelay = this.mKeyguardServiceRestartDelay) != null) {
            return keyguardServiceRestartDelay.shouldDelay(shortName, shouldDelay);
        }
        return false;
    }

    public void speedupSpecialAct(ActivityRecord actRec) {
        ActStartupAccelerator actStartupAccelerator = this.mActStartupAccelerator;
        if (actStartupAccelerator != null) {
            boolean res = actStartupAccelerator.speedupStartForSpecialApp(actRec);
            if (DEBUG_AMS) {
                Slog.d(TAG, "speedupSpecialAct res:" + res);
            }
        }
    }

    public void DumpEnvironment() {
        SystemProperties.set("sys.dumpenvironment.finished", "0");
        SystemProperties.set("ctl.start", "dumpenvironment");
        long begin = SystemClock.elapsedRealtime();
        int wait_time = SystemProperties.getInt("ro.dumpenvironment.time", 4000);
        while (SystemProperties.getInt("sys.dumpenvironment.finished", 0) != 1 && SystemClock.elapsedRealtime() - begin < ((long) wait_time)) {
            SystemClock.sleep(100);
        }
    }

    /* access modifiers changed from: private */
    public class ActStartFreqControler {
        private static final boolean DEBUG_EMULATE = false;
        private static final boolean DEBUG_FREQ_CONTROL = false;
        private static final long DECT_TIME_MAX_INTERVEL = 30000;
        private static final long DECT_TIME_MIN_INTERVEL = 4000;
        private static final int MIN_START_DECT_COUNT = 10;
        private ArrayList<ActStartFreqData> mFreqControlActList = new ArrayList<>();

        public ActStartFreqControler() {
            initActList();
        }

        private void initActList() {
            this.mFreqControlActList.add(new ActStartFreqData("com.android.permissioncontroller", "com.android.packageinstaller.permission.ui.GrantPermissionsActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("com.lianlian", "com.lianlian.activity.RetryAnonymousLoginActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("tv.danmaku.bili", "tv.danmaku.bili.ui.video.VideoDetailsActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("com.yy.hiyo", "com.yy.hiyo.MainActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("com.cashtoutiao", "com.cashtoutiao.account.ui.LoginOneKeyActivity"));
        }

        private ActStartFreqData getActStartFreqData(String pkgName, String className) {
            int size = this.mFreqControlActList.size();
            for (int index = 0; index < size; index++) {
                ActStartFreqData tmpData = this.mFreqControlActList.get(index);
                if (tmpData != null && tmpData.mPkgName.equals(pkgName) && tmpData.mClassName.equals(className)) {
                    return tmpData;
                }
            }
            return null;
        }

        public boolean doControlActivityStartFreq(Intent intent) {
            ComponentName cmpName;
            ActStartFreqData data;
            if (intent == null || (cmpName = intent.getComponent()) == null) {
                return false;
            }
            String pkgName = cmpName.getPackageName();
            String className = cmpName.getClassName();
            if (pkgName == null || className == null || (data = getActStartFreqData(pkgName, className)) == null) {
                return false;
            }
            long curTime = SystemClock.elapsedRealtime();
            long timeElapsed = curTime - data.mLastDectTime;
            data.mCount++;
            if (1 == data.mCount) {
                data.mLastDectTime = curTime;
                return false;
            } else if (!data.mIsFrequent) {
                if (timeElapsed > DECT_TIME_MIN_INTERVEL) {
                    data.mLastDectTime = curTime;
                    data.mCount = 0;
                } else if (data.mCount >= MIN_START_DECT_COUNT) {
                    data.mIsFrequent = true;
                    data.mLastDectTime = curTime;
                }
                return false;
            } else if (timeElapsed <= DECT_TIME_MAX_INTERVEL) {
                data.mLastDectTime = curTime;
                return true;
            } else {
                data.mIsFrequent = false;
                data.mCount = 0;
                data.mLastDectTime = curTime;
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ActStartFreqData {
        String mClassName = null;
        int mCount = 0;
        boolean mIsFrequent = false;
        long mLastDectTime = 0;
        String mPkgName = null;

        public ActStartFreqData(String pkgName, String className) {
            this.mPkgName = pkgName;
            this.mClassName = className;
            this.mLastDectTime = SystemClock.elapsedRealtime();
            this.mCount = 0;
        }

        public String toShortString() {
            return "mPkgName:" + this.mPkgName + ", mClassName" + this.mClassName + ", c:" + this.mCount;
        }
    }

    private class ActStartupAccelerator {
        private static final int ACCE_DURATION_TIME = 1000;
        private static final String DATA_PATH_FOR_GAME_01 = "Android/data/com.dzxy.cscy.nearme.gamecenter/files/main/data/res";
        private static final String GAME_01_MAIN_ACT_NAME = "com.dzxy.cscy.nearme.gamecenter/org.cocos2dx.lua.AppActivity";
        private static final int MAX_SPEEDUP_ACTION_REPEAT_COUNT_GAME01 = 5;
        private static final int MAX_TIME_FOR_ACTION_IO = 60000;
        private static final String PKGNAME_SPECIALGAME_01 = "com.dzxy.cscy.nearme.gamecenter";
        private static final String PROP_ENABLE_SPECIAL_SPEEDUP = "sys.oppo.speedup";
        private boolean DEBUG_ACT_ACCE = true;
        private ArrayList<String> mAccelerateActList = new ArrayList<>();
        private boolean mEnableSpeedUpForSpecialGame = SystemProperties.getBoolean(PROP_ENABLE_SPECIAL_SPEEDUP, true);
        private Handler mHandlerForSpecialSpeedup = null;
        private long mLastAcceTime = 0;
        private LocalPerformance mLocalPerformance = null;
        private int[] mPerformanceConfigList = {1077936128, 1, 1086324736, 1, 1090519040, 2, 1082130432, 1800};
        private int mRemainSpeedupTimeForGame01 = 0;
        private int mSpeedActionCountGame01 = 0;
        private Runnable mSpeepupCallbackForGame01 = new Runnable() {
            /* class com.android.server.wm.PswOppoAmsUtilsFeatrue.ActStartupAccelerator.AnonymousClass1 */

            public void run() {
                ActStartupAccelerator.access$208(ActStartupAccelerator.this);
                if (ActStartupAccelerator.this.mSpeedActionCountGame01 < ActStartupAccelerator.MAX_SPEEDUP_ACTION_REPEAT_COUNT_GAME01) {
                    ActStartupAccelerator.this.mHandlerForSpecialSpeedup.postDelayed(ActStartupAccelerator.this.mSpeepupCallbackForGame01, OppoGnssDuration.GPS_DURATION_THRESHOLD);
                }
            }
        };
        private boolean mSupportActAccelerate = true;

        static /* synthetic */ int access$208(ActStartupAccelerator x0) {
            int i = x0.mSpeedActionCountGame01;
            x0.mSpeedActionCountGame01 = i + 1;
            return i;
        }

        public ActStartupAccelerator() {
            initAccelaerateActList();
            initPerformaceConfig();
        }

        private void initAccelaerateActList() {
            if (this.mSupportActAccelerate) {
                this.mAccelerateActList.add("{com.tencent.mm/com.tencent.mm.plugin.voip.ui.VideoActivity}");
            }
        }

        private void initPerformaceConfig() {
            if (this.mSupportActAccelerate) {
                this.mLocalPerformance = new LocalPerformance();
            }
        }

        private boolean isInAcceList(String shortCmpName) {
            int size = this.mAccelerateActList.size();
            for (int index = 0; index < size; index++) {
                String tmpData = this.mAccelerateActList.get(index);
                if (this.DEBUG_ACT_ACCE) {
                    Slog.d(PswOppoAmsUtilsFeatrue.TAG, "isInAcceList, index:" + index + ", shortCmpName:" + shortCmpName + ", data:" + tmpData);
                }
                if (tmpData != null && tmpData.equals(shortCmpName)) {
                    return true;
                }
            }
            return false;
        }

        public boolean doAccelerateActStartup(Intent intent) {
            if (!this.mSupportActAccelerate) {
                return false;
            }
            ComponentName cmpName = intent != null ? intent.getComponent() : null;
            if (cmpName == null) {
                if (this.DEBUG_ACT_ACCE) {
                    Slog.d(PswOppoAmsUtilsFeatrue.TAG, "doAccelerateActStartup cmpName null, not control");
                }
                return false;
            }
            String shortName = cmpName.toShortString();
            if (shortName == null || shortName.length() <= 0) {
                if (this.DEBUG_ACT_ACCE) {
                    Slog.d(PswOppoAmsUtilsFeatrue.TAG, "doAccelerateActStartup shortName empty, not control");
                }
                return false;
            } else if (!isInAcceList(shortName)) {
                if (this.DEBUG_ACT_ACCE) {
                    Slog.d(PswOppoAmsUtilsFeatrue.TAG, "doAccelerateActStartup data not found in list.");
                }
                return false;
            } else {
                long curTime = SystemClock.elapsedRealtime();
                long j = this.mLastAcceTime;
                if (j > 0 && curTime - j <= 1000) {
                    return false;
                }
                this.mLastAcceTime = curTime;
                this.mLocalPerformance.perfLockAcquire(ACCE_DURATION_TIME, this.mPerformanceConfigList);
                return true;
            }
        }

        public boolean speedupStartForSpecialApp(ActivityRecord actRecord) {
            String pkgName;
            ComponentName realActivity;
            if (this.mEnableSpeedUpForSpecialGame && actRecord != null && (pkgName = actRecord.packageName) != null && !pkgName.isEmpty() && PKGNAME_SPECIALGAME_01.equals(pkgName) && (realActivity = actRecord.mActivityComponent) != null) {
                String actName = realActivity.flattenToString();
                if (PswOppoAmsUtilsFeatrue.DEBUG_AMS) {
                    Slog.d(PswOppoAmsUtilsFeatrue.TAG, "actName:" + actName);
                }
                if (actName != null && GAME_01_MAIN_ACT_NAME.equals(actName)) {
                    return speedupForSpecialGame01();
                }
            }
            return false;
        }

        private boolean speedupForSpecialGame01() {
            Context context = PswOppoAmsUtilsFeatrue.this.mAtm.mContext;
            File[] fileList = new Environment.UserEnvironment(0).getExternalDirs();
            if (PswOppoAmsUtilsFeatrue.DEBUG_AMS && fileList != null && fileList.length > 0) {
                for (File file : fileList) {
                    Slog.d(PswOppoAmsUtilsFeatrue.TAG, "speedupForGame, file path:" + file.getPath());
                }
            }
            if (fileList == null || fileList.length <= 0 || new File(fileList[0], DATA_PATH_FOR_GAME_01).exists()) {
                return false;
            }
            if (this.mHandlerForSpecialSpeedup == null) {
                this.mHandlerForSpecialSpeedup = PswOppoAmsUtilsFeatrue.this.mAtm.mH;
            }
            if (PswOppoAmsUtilsFeatrue.DEBUG_AMS) {
                Slog.d(PswOppoAmsUtilsFeatrue.TAG, "speedupForGame post first action.");
            }
            this.mSpeedActionCountGame01 = 0;
            this.mHandlerForSpecialSpeedup.postDelayed(this.mSpeepupCallbackForGame01, 500);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class LocalPerformance {
        public LocalPerformance() {
        }

        public int perfLockAcquire(int duration, int... list) {
            return 0;
        }
    }

    private class KeyguardServiceRestartDelay {
        private static final String KEYGUARD_SERVICE = "com.android.keyguard/.KeyguardService";
        private static final int MAX_DELAY_COUNT = 4;
        private static final long RESTART_DETECT_TIME = 6;
        private int mDelayCount = 0;
        private long mDelayDetectTime = 0;

        public KeyguardServiceRestartDelay() {
        }

        public boolean shouldDelay(String shortName, boolean shouldDelay) {
            if (!shouldDelay) {
                return false;
            }
            if (!KEYGUARD_SERVICE.equals(shortName)) {
                return shouldDelay;
            }
            if (this.mDelayCount == 0) {
                this.mDelayDetectTime = SystemClock.elapsedRealtime();
                this.mDelayCount++;
                return shouldDelay;
            }
            long curTime = SystemClock.elapsedRealtime();
            if (curTime - this.mDelayDetectTime <= RESTART_DETECT_TIME) {
                this.mDelayCount++;
                if (this.mDelayCount >= 4) {
                    this.mDelayDetectTime = curTime;
                    this.mDelayCount = 0;
                    return false;
                }
            } else {
                this.mDelayDetectTime = curTime;
                this.mDelayCount = 0;
            }
            return shouldDelay;
        }
    }

    /* access modifiers changed from: package-private */
    public enum FrameDropFlag {
        FG("fg#"),
        BG(StringUtils.EMPTY),
        ANIM("animation#");
        
        private String name;

        private FrameDropFlag(String name2) {
            this.name = name2;
        }

        public String toString() {
            return this.name;
        }
    }

    class FrameSkipEntry {
        private String dateTimeStr;
        private FrameDropFlag flag;
        private String processName;
        private long skippedFrames;

        FrameSkipEntry(String processName2, String dateTimeStr2, long skippedFrames2) {
            this.processName = processName2;
            this.dateTimeStr = dateTimeStr2;
            this.skippedFrames = skippedFrames2;
            this.flag = FrameDropFlag.BG;
        }

        FrameSkipEntry(String processName2, String dateTimeStr2, boolean isAnimation, boolean isForeground, long skippedFrames2) {
            this.processName = processName2;
            this.dateTimeStr = dateTimeStr2;
            this.skippedFrames = skippedFrames2;
            this.flag = isAnimation ? FrameDropFlag.ANIM : isForeground ? FrameDropFlag.FG : FrameDropFlag.BG;
        }

        public String toString() {
            return this.dateTimeStr + "#" + this.processName + "#" + this.flag + this.skippedFrames;
        }
    }

    public void saveSkippedFramesRecordToList(String processName, long dateTime, long skippedFrames) {
        FrameSkipEntry entry = new FrameSkipEntry(processName, new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date(dateTime)), skippedFrames);
        boolean dropItem = false;
        synchronized (mFrameSkipEntryListSelfLocked) {
            if (mFrameSkipEntryListSelfLocked.size() >= 8096) {
                dropItem = true;
            } else {
                mFrameSkipEntryListSelfLocked.add(entry);
            }
        }
        Slog.d(TAG_FrameSkipReporter, entry.toString() + "(dropped: " + dropItem + ")");
    }

    public void saveSkippedFramesRecordToList(String processName, long dateTime, boolean isAnimation, boolean isForeground, long skippedFrames) {
        FrameSkipEntry entry = new FrameSkipEntry(processName, new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date(dateTime)), isAnimation, isForeground, skippedFrames);
        boolean dropItem = false;
        synchronized (mFrameSkipEntryListSelfLocked) {
            if (mFrameSkipEntryListSelfLocked.size() >= 8096) {
                dropItem = true;
            } else {
                mFrameSkipEntryListSelfLocked.add(entry);
            }
        }
        Slog.d(TAG_FrameSkipReporter, entry.toString() + "(dropped: " + dropItem + ")");
    }

    public void dumpSkippedFrames(PrintWriter pw) {
        synchronized (mFrameSkipEntryListSelfLocked) {
            int size = mFrameSkipEntryListSelfLocked.size();
            pw.println("---FeatureImpl dump skipped frames info---" + size);
            for (int i = 0; i < size; i++) {
                pw.println(mFrameSkipEntryListSelfLocked.get(i).toString());
            }
            mFrameSkipEntryListSelfLocked.clear();
        }
    }

    public void detectExceptionsForOIDT(Context context, int type, String pkgName, String pkgVersion, String reason) {
        Log.d(TAG, "detectExceptionsForOIDT type:" + type);
        String packageName = pkgName == null ? StringUtils.EMPTY : pkgName;
        String packageVersion = pkgVersion == null ? StringUtils.EMPTY : pkgVersion;
        String exceptionReason = reason == null ? StringUtils.EMPTY : reason;
        String packageLabel = packageName;
        PackageManager pkgManager = context.getPackageManager();
        try {
            ApplicationInfo info = pkgManager.getApplicationInfoAsUser(packageName, 0, UserHandle.getCallingUserId());
            if (info != null) {
                packageLabel = info.loadLabel(pkgManager).toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HashMap<String, String> logMap = new HashMap<>();
        if (type < 2 && this.latestOIDTExceptionList != null && !StringUtils.EMPTY.equals(packageName)) {
            this.latestOIDTExceptionList.add(packageName);
            if (this.latestOIDTExceptionList.size() > TYPE_MAIN_THREAD_BLOCKED) {
                this.latestOIDTExceptionList.remove(0);
                int sameExceptionFromSamePackageCount = 0;
                for (int i = 0; i < this.latestOIDTExceptionList.size(); i++) {
                    if (packageName.equals(this.latestOIDTExceptionList.get(i))) {
                        sameExceptionFromSamePackageCount++;
                    }
                }
                if (sameExceptionFromSamePackageCount == this.latestOIDTExceptionList.size()) {
                    return;
                }
            }
        }
        if (type >= 2) {
            JSONObject jsonObject = str2json(reason);
            if (jsonObject != null) {
                json2map(jsonObject, logMap);
            }
            exceptionReason = StringUtils.EMPTY;
        }
        if (exceptionReason.length() > 2000) {
            exceptionReason = exceptionReason.substring(0, 2000);
        }
        logMap.put("timestamp", System.currentTimeMillis() + StringUtils.EMPTY);
        logMap.put("packageName", packageName);
        logMap.put("packageVersion", packageVersion);
        logMap.put("exceptionReason", exceptionReason);
        logMap.put("packageLabel", packageLabel == null ? packageName : packageLabel);
        switch (type) {
            case 0:
                OppoManager.onStamp("020201", logMap);
                return;
            case 1:
                OppoManager.onStamp("020202", logMap);
                return;
            case 2:
                OppoManager.onStamp("020101", logMap);
                return;
            case 3:
                OppoManager.onStamp("020103", logMap);
                return;
            case 4:
                OppoManager.onStamp("020102", logMap);
                return;
            case TYPE_MAIN_THREAD_BLOCKED /* 5 */:
                OppoManager.onStamp("020104", logMap);
                return;
            case TYPE_INPUT_SLOW /* 6 */:
                OppoManager.onStamp("020105", logMap);
                return;
            default:
                throw new IllegalArgumentException("wrong type type: " + type);
        }
    }

    private JSONObject str2json(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.e(TAG, "Reason parse to jsonObject failed !");
            e.printStackTrace();
            return null;
        }
    }

    private String getStrValFromJsonObj(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to get String value form json object, key is: " + key);
            e.printStackTrace();
            return StringUtils.EMPTY;
        }
    }

    private HashMap<String, String> json2map(JSONObject jsonObject, HashMap<String, String> logMap) {
        if (logMap == null) {
            logMap = new HashMap<>();
        }
        if (jsonObject != null) {
            try {
                if (jsonObject.length() != 0) {
                    Iterator iterator = jsonObject.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        logMap.put(key, getStrValFromJsonObj(jsonObject, key));
                    }
                    return logMap;
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception in function json2map");
                e.printStackTrace();
            }
        }
        Log.w(TAG, "jsonObject is null or empty, nothing put to OIDT.");
        return logMap;
    }

    private String getVersionName(Context context, String pkgName, int userId) {
        try {
            return context.getPackageManager().getPackageInfoAsUser(pkgName, 0, userId).versionName;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get versionName: " + e.getMessage());
            return null;
        }
    }

    private boolean containsWords(String inputString, String[] items) {
        if (inputString == null || items == null) {
            return false;
        }
        for (String item : items) {
            if (!inputString.contains(item)) {
                return false;
            }
        }
        return true;
    }

    public void detectForgroundExceptions(Context context, String pkgName, String reason) {
        if (reason == null) {
            return;
        }
        if ((reason.contains("timeout publishing content providers") || containsWords(reason, this.PROVIDER_KEYS_LIST)) && context != null) {
            detectExceptionsForOIDT(context, 0, pkgName, getVersionName(context, pkgName, context.getUserId()), reason);
        }
    }

    class LaunchTimeEntry {
        int activityLaunchTime;
        int activityRecordIdHashCode;
        String dateTimeStr;
        int launchType;
        String launchedActivityShortComponentName;
        int pid;
        int userId;

        LaunchTimeEntry(String _dateTimeStr, int _pid, int _userId, int _activityRecordIdHashCode, String _launchedActivityShortComponentName, int _activityLaunchTime, int _launchType) {
            this.dateTimeStr = _dateTimeStr;
            this.pid = _pid;
            this.userId = _userId;
            this.activityRecordIdHashCode = _activityRecordIdHashCode;
            this.launchedActivityShortComponentName = _launchedActivityShortComponentName;
            this.activityLaunchTime = _activityLaunchTime;
            this.launchType = _launchType;
        }

        public String toString() {
            return this.dateTimeStr + "#" + String.valueOf(this.userId) + "#" + String.valueOf(this.pid) + "#" + String.valueOf(this.activityRecordIdHashCode) + "#" + this.launchedActivityShortComponentName + "#" + String.valueOf(this.activityLaunchTime) + "#" + typeToString(this.launchType);
        }

        public String typeToString(int launchType2) {
            if (launchType2 == 7) {
                return "COLD";
            }
            if (launchType2 == 8) {
                return "WARM";
            }
            if (launchType2 != 9) {
                return null;
            }
            return "HOT";
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0099, code lost:
        r0 = th;
     */
    public void saveActivityLaunchRecordToList(long dateTime, int pid, int userId, int activityRecordIdHashCode, String launchedActivityShortComponentName, int activityLaunchTime, int launchType) {
        boolean launchItem = false;
        LaunchTimeEntry entry = new LaunchTimeEntry(new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date(dateTime)), pid, userId, activityRecordIdHashCode, launchedActivityShortComponentName, activityLaunchTime, launchType);
        synchronized (mLaunchTimeEntryListSelfLocked) {
            if (mLaunchTimeEntryListSelfLocked.size() >= 8096) {
                launchItem = true;
            } else {
                mLaunchTimeEntryListSelfLocked.add(entry);
            }
        }
        Slog.d(TAG_ActivityLaunchRecord, entry.userId + "#" + entry.pid + "#" + entry.activityRecordIdHashCode + "#" + entry.launchedActivityShortComponentName + "#" + entry.activityLaunchTime + "#" + entry.typeToString(launchType) + "(launchtime: " + launchItem + ")");
        return;
        while (true) {
        }
    }

    public void dumpActivityLaunchTime(PrintWriter pw) {
        synchronized (mLaunchTimeEntryListSelfLocked) {
            int size = mLaunchTimeEntryListSelfLocked.size();
            pw.println("---dump activitylaunchtime info---" + size);
            for (int i = 0; i < size; i++) {
                pw.println(mLaunchTimeEntryListSelfLocked.get(i).toString());
            }
            mLaunchTimeEntryListSelfLocked.clear();
        }
    }

    class AmKillEntry {
        String dateTimeStr;
        String pidStr;
        String processName;
        String reason;

        AmKillEntry(String _dateTimeStr, String _pidStr, String _processName, String _reason) {
            this.dateTimeStr = _dateTimeStr;
            this.pidStr = _pidStr;
            this.processName = _processName;
            this.reason = _reason;
        }

        public String toString() {
            return this.dateTimeStr + "#" + this.pidStr + "#" + this.processName + "#reason=" + this.reason;
        }
    }

    public void saveAmKillRecordToList(long dateTime, int pid, String processName, String reason) {
        boolean killItem = false;
        AmKillEntry entry = new AmKillEntry(new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date(dateTime)), String.valueOf(pid), processName, reason);
        synchronized (mAmKillEntryListSelfLocked) {
            if (mAmKillEntryListSelfLocked.size() >= 8096) {
                killItem = true;
            } else {
                mAmKillEntryListSelfLocked.add(entry);
            }
        }
        Slog.d(TAG_AmKillRecord, entry.pidStr + "#" + entry.processName + "#reason=" + entry.reason + "(amkill: " + killItem + ")");
    }

    public void dumpAmKill(PrintWriter pw) {
        synchronized (mAmKillEntryListSelfLocked) {
            int size = mAmKillEntryListSelfLocked.size();
            pw.println("---dump amkill info---" + size);
            for (int i = 0; i < size; i++) {
                pw.println(mAmKillEntryListSelfLocked.get(i).toString());
            }
            mAmKillEntryListSelfLocked.clear();
        }
    }
}
