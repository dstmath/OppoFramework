package com.android.server.am;

import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.pm.IColorFullmodeManager;
import com.android.server.wm.ActivityRecord;
import com.android.server.wm.ColorAppStartupManagerHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ColorAbnormalAppManager implements IColorAbnormalAppManager {
    private static final int BROADCAST_MSG = 101;
    private static final int BROADCAST_RECEIVER_MSG = 104;
    private static final int BROADCAST_TOP_MSG = 103;
    private static final int SERVICE_MSG = 102;
    private static final String TYPE_ACTIVITY = "activity";
    private static final String TYPE_BROADCAST = "service";
    private static final String TYPE_OTHER = "other";
    private static final String TYPE_PROVIDER = "content provider";
    private static final String TYPE_REPEAT = "repeat";
    private static final String TYPE_RESTART = "restart";
    private static final String TYPE_SERVICE = "broadcast";
    private static ColorAbnormalAppManager mColorAbnormalAppManager = null;
    private final String ACTION_OPPO_GUARD_ELF_COUNT_RESTRICT_LIST = "android.intent.action.OPPO_GUARD_ELF_COUNT_RESTRICT_LIST";
    private final String ACTION_OPPO_GUARD_ELF_MONITOR = "android.intent.action.OPPO_GUARD_ELF_MONITOR";
    private final String ACTION_OPPO_GUARD_ELF_SET_RESTRICT = "android.intent.action.OPPO_GUARD_ELF_SET_RESTRICT";
    private final String ACTION_OPPO_GUARD_TIME_INFO = "android.intent.action.OPPO_GUARD_TIME_INFO";
    private boolean DEBUG = true;
    private boolean DEBUG_SWITCH = (DEBUG_DETAIL | this.DynamicDebug);
    boolean DynamicDebug = false;
    private final String GUARD_ELF_FEATURE_NAME = "oppo.guard.elf.support";
    private final int RESTRICT_ABNORMAL = 2;
    private final int RESTRICT_NO = 0;
    private final int RESTRICT_REPEAT = 1;
    private final BroadcastReceiver dateChangedReceiver = new BroadcastReceiver() {
        /* class com.android.server.am.ColorAbnormalAppManager.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if (ColorAbnormalAppStatistics.getInstance().isNeedUpload()) {
                ColorAbnormalAppStatistics.getInstance().uploadAbnormalAppInfoList();
            }
        }
    };
    private ActivityManagerService mAms = null;
    private Comparator<ColorAnrAppInfo> mBroadcastComparator = new Comparator<ColorAnrAppInfo>() {
        /* class com.android.server.am.ColorAbnormalAppManager.AnonymousClass1 */

        public int compare(ColorAnrAppInfo lhs, ColorAnrAppInfo rhs) {
            if (lhs.getCount() != rhs.getCount()) {
                return lhs.getCount() > rhs.getCount() ? -1 : 1;
            }
            return 0;
        }
    };
    private final Object mBroadcastLock = new Object();
    private final Object mBroadcastReceiver = new Object();
    /* access modifiers changed from: private */
    public ArrayList<String> mBroadcastReceiverInfo = new ArrayList<>();
    /* access modifiers changed from: private */
    public List<ColorAnrAppInfo> mBroadcastReceiverList = new ArrayList();
    /* access modifiers changed from: private */
    public ArrayList<String> mBroadcastTimeInfo = new ArrayList<>();
    /* access modifiers changed from: private */
    public List<ColorAnrAppInfo> mBroadcastTimeList = new ArrayList();
    private ArrayList<String> mCountRestrictedList = new ArrayList<>();
    private Handler mHandler = null;
    private boolean mHandlerReady = false;
    /* access modifiers changed from: private */
    public long mLastCheckBroadcastTime = 0;
    /* access modifiers changed from: private */
    public long mLastCheckServiceTime = 0;
    private long mLastCheckTime = 0;
    private List<String> mNotRestrictAppList = new ArrayList();
    private List<String> mPersistRestrictAppList = new ArrayList();
    private SparseArray<List<String>> mScreenOffRestrictAppMap = new SparseArray<>();
    /* access modifiers changed from: private */
    public ArrayList<String> mServiceTimeInfo = new ArrayList<>();
    /* access modifiers changed from: private */
    public List<ColorAnrAppInfo> mServiceTimeList = new ArrayList();
    private List<ColorAppStartInfo> mStartAppList = new ArrayList();
    private List<String> mStartInfoWhiteList = new ArrayList();
    private boolean mSwitch = false;
    private ArrayList<String> mTopBroadcastInfo = new ArrayList<>();
    private List<ColorAnrAppInfo> mTopBroadcastList = new ArrayList();
    private List<ColorAnrAppInfo> mTopThirdBroadcastList = new ArrayList();
    private ArrayList<String> mUploadInfoList = new ArrayList<>();

    private class AbnormalAppManagerHandler extends Handler {
        private AbnormalAppManagerHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    ColorAbnormalAppManager colorAbnormalAppManager = ColorAbnormalAppManager.this;
                    colorAbnormalAppManager.sendTimeBroadcast(colorAbnormalAppManager.mBroadcastTimeList, ColorAbnormalAppManager.this.mBroadcastTimeInfo, "BroadcastTime", ColorAbnormalAppManager.this.mLastCheckBroadcastTime, 101);
                    return;
                case 102:
                    ColorAbnormalAppManager colorAbnormalAppManager2 = ColorAbnormalAppManager.this;
                    colorAbnormalAppManager2.sendTimeBroadcast(colorAbnormalAppManager2.mServiceTimeList, ColorAbnormalAppManager.this.mServiceTimeInfo, "ServicesTime", ColorAbnormalAppManager.this.mLastCheckServiceTime, 102);
                    return;
                case 103:
                    ColorAbnormalAppManager.this.sendTopBroadcast();
                    return;
                case 104:
                    ColorAbnormalAppManager colorAbnormalAppManager3 = ColorAbnormalAppManager.this;
                    colorAbnormalAppManager3.sendBroadcastReceiver(colorAbnormalAppManager3.mBroadcastReceiverList, ColorAbnormalAppManager.this.mBroadcastReceiverInfo);
                    return;
                default:
                    return;
            }
        }
    }

    private ColorAbnormalAppManager() {
    }

    public static final ColorAbnormalAppManager getInstance() {
        if (mColorAbnormalAppManager == null) {
            mColorAbnormalAppManager = new ColorAbnormalAppManager();
        }
        return mColorAbnormalAppManager;
    }

    public boolean getDynamicDebug() {
        return this.DynamicDebug;
    }

    private void initData() {
        Log.d("ColorAbnormalAppManager", "initData");
        ColorAamUtils.getInstance().initCtx(this.mAms.mContext);
        updateScreenOffRestrictedList(ColorAamUtils.getInstance().readScreenOffResrictFile(), 0);
        this.mStartInfoWhiteList = ColorAamUtils.getInstance().readStartInfoWhiteFile();
        this.mPersistRestrictAppList = ColorAamUtils.getInstance().readPersistRestrictFile();
        this.mNotRestrictAppList = ColorAamUtils.getInstance().readNotRestrictFile();
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.mLastCheckTime = lastCheckTime;
    }

    private void cleanStartAppList() {
        if (this.DynamicDebug) {
            Log.d("ColorAbnormalAppManager", "cleanAbnormalAppList");
        }
        this.mStartAppList.clear();
    }

    private void cleanUploadInfoList() {
        if (this.DynamicDebug) {
            Log.d("ColorAbnormalAppManager", "cleanUploadInfoList");
        }
        this.mUploadInfoList.clear();
    }

    private void notifyAbnormalInfo() {
        boolean hasAbnormalApp = false;
        for (ColorAppStartInfo appinfo : this.mStartAppList) {
            if (appinfo.getStartCount() > OppoGuardElfConfigUtil.getInstance().getAbnormalStartCount()) {
                if (this.DEBUG) {
                    Log.d("ColorAbnormalAppManager", "#################  AbnormalInfo app  ##########################");
                    appinfo.dumpInfo("abnormal appinfo");
                    Log.d("ColorAbnormalAppManager", "###############################################################");
                }
                if (handleAbnormalApp(appinfo)) {
                    this.mUploadInfoList.add(appinfo.infoToString("abnormal appinfo"));
                    hasAbnormalApp = true;
                }
            } else if (appinfo.getStartCount() > OppoGuardElfConfigUtil.getInstance().getCollectStartCount()) {
                if (this.DynamicDebug) {
                    Log.d("ColorAbnormalAppManager", "---------------------  warning app  -----------------------------");
                    appinfo.dumpInfo("warning appinfo");
                    Log.d("ColorAbnormalAppManager", "-----------------------------------------------------------------");
                }
                this.mUploadInfoList.add(appinfo.infoToString("warning appinfo"));
            } else if (this.DynamicDebug) {
                Log.d("ColorAbnormalAppManager", "-----------------  normalInfo app  -----------------------------");
                appinfo.dumpInfo("normal appinfo");
                Log.d("ColorAbnormalAppManager", "-----------------------------------------------------------------");
            }
        }
        sendAbnormalNotify();
        cleanStartAppList();
        cleanUploadInfoList();
        if (hasAbnormalApp) {
            if (this.DEBUG_SWITCH) {
                Log.d("ColorAbnormalAppManager", "hasAbnormalApp savePersistRestrictedFile!");
            }
            savePersistRestrictedFile(this.mPersistRestrictAppList);
        }
    }

    private void collectStartAppInfo(String processName, String pkgName, String startType, boolean isThird) {
        ColorAppStartInfo appStartInfo = getAppInfoInList(processName);
        if (appStartInfo == null) {
            this.mStartAppList.add(ColorAppStartInfo.builder(processName, pkgName, startType, isThird));
            return;
        }
        appStartInfo.increaseStartCount(startType);
        appStartInfo.setCurStartTime(SystemClock.elapsedRealtime());
    }

    private ColorAppStartInfo getAppInfoInList(String processName) {
        for (ColorAppStartInfo appinfo : this.mStartAppList) {
            if (appinfo.getProcessName().equals(processName)) {
                return appinfo;
            }
        }
        return null;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.am.ColorAbnormalAppStatistics.collectAbnormalAppInfo(java.lang.String, java.lang.String, boolean):void
     arg types: [java.lang.String, java.lang.String, int]
     candidates:
      com.android.server.am.ColorAbnormalAppStatistics.collectAbnormalAppInfo(boolean, java.lang.String, java.lang.String):void
      com.android.server.am.ColorAbnormalAppStatistics.collectAbnormalAppInfo(java.lang.String, java.lang.String, boolean):void */
    public void handleStartAppInfo(ProcessRecord app, String startType) {
        if (this.mSwitch) {
            boolean isThird = false;
            String pkgName = "";
            if (app.info != null) {
                pkgName = app.info.packageName;
                if ((app.info.flags & 1) == 0) {
                    isThird = true;
                }
            }
            handleStartAppInfo(app.processName, pkgName, startType, isThird);
            if (ColorAbnormalAppStatistics.getInstance().isNeedUpload()) {
                int restrictType = validNewProcForUpload(pkgName, app.userId, startType);
                if (1 == restrictType) {
                    ColorAbnormalAppStatistics.getInstance().collectAbnormalAppInfo(pkgName, TYPE_REPEAT, false);
                } else if (2 == restrictType) {
                    ColorAbnormalAppStatistics.getInstance().collectAbnormalAppInfo(pkgName, startType, false);
                }
            }
        }
    }

    private void handleStartAppInfo(String processName, String pkgName, String startType, boolean isThird) {
        if (this.DEBUG_SWITCH) {
            Log.d("ColorAbnormalAppManager", "handleStartAppInfo processName == " + processName + "  startType == " + startType);
        }
        long time = SystemClock.elapsedRealtime();
        if (this.mStartAppList.isEmpty()) {
            this.mLastCheckTime = time;
            Log.d("ColorAbnormalAppManager", "Now Begin Monitor App StartInfo's Time == " + time);
        }
        collectStartAppInfo(processName, pkgName, startType, isThird);
        if (Math.abs(time - this.mLastCheckTime) > OppoGuardElfConfigUtil.getInstance().getCheckStartTimeInterval()) {
            notifyAbnormalInfo();
        }
        if (this.DynamicDebug) {
            Log.d("ColorAbnormalAppManager", "handleStartAppInfo cost time ==  " + (SystemClock.elapsedRealtime() - time));
        }
    }

    public boolean isPackageRestricted(String packageName) {
        synchronized (mColorAbnormalAppManager) {
            if (this.mPersistRestrictAppList.contains(packageName)) {
                return true;
            }
            return false;
        }
    }

    private boolean setPackageRestricted(String packageName, boolean isRestrict) {
        boolean isChange = false;
        if (isPackageRestricted(packageName)) {
            if (!isRestrict) {
                if (this.DEBUG) {
                    Log.d("ColorAbnormalAppManager", "setPackageRestricted remove packageName  " + packageName);
                }
                synchronized (mColorAbnormalAppManager) {
                    this.mPersistRestrictAppList.remove(packageName);
                    isChange = true;
                }
            }
        } else if (isRestrict) {
            if (this.DEBUG) {
                Log.d("ColorAbnormalAppManager", "setPackageRestricted add packageName  " + packageName);
            }
            synchronized (mColorAbnormalAppManager) {
                this.mPersistRestrictAppList.add(packageName);
                isChange = true;
            }
        }
        return isChange;
    }

    public void setPackageUnRestricted(String packageName) {
        if (this.mSwitch && setPackageRestricted(packageName, false)) {
            Log.d("ColorAbnormalAppManager", "setPackageUnRestricted updateRestrictedFile!!!!");
            savePersistRestrictedFile(this.mPersistRestrictAppList);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.am.ColorAbnormalAppStatistics.collectAbnormalAppInfo(java.lang.String, java.lang.String, boolean):void
     arg types: [java.lang.String, java.lang.String, int]
     candidates:
      com.android.server.am.ColorAbnormalAppStatistics.collectAbnormalAppInfo(boolean, java.lang.String, java.lang.String):void
      com.android.server.am.ColorAbnormalAppStatistics.collectAbnormalAppInfo(java.lang.String, java.lang.String, boolean):void */
    private boolean validNewProc(String packageName, int userId, String type) {
        boolean result = false;
        if (!this.mSwitch || packageName == null) {
            return false;
        }
        if (!TYPE_ACTIVITY.equals(type) && isPackageRestricted(packageName)) {
            synchronized (mColorAbnormalAppManager) {
                if (!this.mStartInfoWhiteList.contains(packageName)) {
                    ColorAbnormalAppStatistics.getInstance().collectAbnormalAppInfo(packageName, TYPE_REPEAT, true);
                    result = true;
                }
            }
        } else if (OppoSysStateManager.getInstance().restrictStartupBg()) {
            synchronized (mColorAbnormalAppManager) {
                if (inRestrictAppList(packageName, userId) && !hasLruProcess(packageName)) {
                    if (!this.mCountRestrictedList.contains(packageName)) {
                        Log.d("ColorAbnormalAppManager", "mCountRestrictedList add packageName == " + packageName);
                        this.mCountRestrictedList.add(packageName);
                    }
                    ColorAbnormalAppStatistics.getInstance().collectAbnormalAppInfo(packageName, type, true);
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean hasLruProcess(String packageName) {
        try {
            return isInLruProcessesLocked(this.mAms.mContext.getPackageManager().getApplicationInfo(packageName, 0).uid);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int validNewProcForUpload(String packageName, int userId, String type) {
        int result = 0;
        if (!this.mSwitch || packageName == null) {
            return 0;
        }
        if (!TYPE_ACTIVITY.equals(type) && isPackageRestricted(packageName)) {
            synchronized (mColorAbnormalAppManager) {
                if (!this.mStartInfoWhiteList.contains(packageName)) {
                    result = 1;
                }
            }
            return result;
        } else if (!OppoSysStateManager.getInstance().restrictStartupBg() || !inRestrictAppList(packageName, userId)) {
            return 0;
        } else {
            return 2;
        }
    }

    private boolean handleAbnormalApp(ColorAppStartInfo appinfo) {
        String pkgName = appinfo.getPkgName();
        if (appinfo.getIsThird()) {
            ActivityRecord r = ColorAppStartupManagerHelper.getInstance().getTopRunningActivityLocked(this.mAms);
            if (r != null && pkgName.equals(r.packageName)) {
                Log.d("ColorAbnormalAppManager", "don't handle! return for top activity");
                return false;
            } else if (OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall()) {
                Log.d("ColorAbnormalAppManager", "CTS TEST! don't handle!");
                return false;
            } else {
                Log.d("ColorAbnormalAppManager", "handleAbnormalApp setPackageRestricted true!");
                setPackageRestricted(pkgName, true);
                if (!this.mStartInfoWhiteList.contains(pkgName)) {
                    killAbnormalApp(pkgName);
                }
            }
        } else {
            Log.d("ColorAbnormalAppManager", "handleAbnormalApp! pkg isn't third app    " + pkgName);
        }
        return true;
    }

    private void killAbnormalApp(String packageName) {
        Log.d("ColorAbnormalAppManager", "killAbnormalApp  " + packageName);
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            activityManagerService.forceStopPackage(packageName, -2);
        }
    }

    private void sendAbnormalNotify() {
        Intent intent = new Intent("android.intent.action.OPPO_GUARD_ELF_MONITOR");
        intent.putExtra("type", "startinfo");
        intent.putStringArrayListExtra("data", new ArrayList<>(this.mUploadInfoList));
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            activityManagerService.mContext.sendBroadcast(intent);
        }
    }

    public void sendUnRestrictNotify(String packageName) {
        Intent intent = new Intent("android.intent.action.OPPO_GUARD_ELF_SET_RESTRICT");
        intent.putExtra("type", "unrestrict");
        intent.putExtra("pkgName", packageName);
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            activityManagerService.mContext.sendBroadcast(intent);
        }
    }

    private List<String> getScreenOffRestrictedList(int userId) {
        return this.mScreenOffRestrictAppMap.get(userId);
    }

    public void updateScreenOffRestrictedList(List<String> screenoffRestrictList, int userId) {
        if (this.DEBUG_SWITCH) {
            Log.d("ColorAbnormalAppManager", "updateScreenOffRestrictedList!!!!");
        }
        if (this.DynamicDebug) {
            Iterator<String> it = screenoffRestrictList.iterator();
            while (it.hasNext()) {
                Log.d("ColorAbnormalAppManager", "updateScreenOffRestrictedList str == " + it.next());
            }
        }
        if (screenoffRestrictList == null || screenoffRestrictList.isEmpty()) {
            Log.d("ColorAbnormalAppManager", "updateScreenOffRestrictedList, invalid source list");
            return;
        }
        synchronized (mColorAbnormalAppManager) {
            List<String> restrictedList = getScreenOffRestrictedList(userId);
            if (restrictedList == null) {
                restrictedList = new ArrayList();
                this.mScreenOffRestrictAppMap.put(userId, screenoffRestrictList);
            }
            restrictedList.clear();
            restrictedList.addAll(screenoffRestrictList);
        }
    }

    public void updateStartInfoWhiteList(List<String> startInfoWhiteList) {
        if (this.DEBUG_SWITCH) {
            Log.d("ColorAbnormalAppManager", "updateStartInfoWhiteList!!!!");
        }
        if (this.DynamicDebug) {
            Iterator<String> it = startInfoWhiteList.iterator();
            while (it.hasNext()) {
                Log.d("ColorAbnormalAppManager", "updateStartInfoWhiteList str == " + it.next());
            }
        }
        List<String> cancelList = new ArrayList<>();
        synchronized (mColorAbnormalAppManager) {
            for (String str : this.mStartInfoWhiteList) {
                if (!startInfoWhiteList.contains(str)) {
                    cancelList.add(str);
                }
            }
            this.mStartInfoWhiteList.clear();
            this.mStartInfoWhiteList.addAll(startInfoWhiteList);
        }
        for (String str2 : cancelList) {
            if (this.mAms != null) {
                Log.d("ColorAbnormalAppManager", "forceStopPackage " + str2 + "  cancel from startinfo whitelist!");
                this.mAms.forceStopPackage(str2, -2);
            }
        }
    }

    public void updateScreenStatus(boolean isScreenOn) {
        if (this.DEBUG_SWITCH) {
            Log.d("ColorAbnormalAppManager", "updateScreenStatus isScreenOn = " + isScreenOn);
        }
        if (isScreenOn) {
            handleCountRestrictedList();
        }
    }

    private void savePersistRestrictedFile(List<String> restrictList) {
        if (this.DEBUG_SWITCH) {
            Log.d("ColorAbnormalAppManager", "savePersistRestrictedFile!");
        }
        ColorAamUtils.getInstance().savePersistRestrictFile(restrictList);
    }

    public void updateNotRestrictedList(List<String> list) {
        if (this.DEBUG_SWITCH) {
            Log.d("ColorAbnormalAppManager", "updateNotRestrictedList!!!!");
        }
        if (this.DynamicDebug) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                Log.d("ColorAbnormalAppManager", "updateNotRestrictedList str == " + it.next());
            }
        }
        synchronized (mColorAbnormalAppManager) {
            this.mNotRestrictAppList.clear();
            this.mNotRestrictAppList.addAll(list);
        }
    }

    private void handleCountRestrictedList() {
        int count;
        ArrayList<String> list = new ArrayList<>();
        synchronized (mColorAbnormalAppManager) {
            count = this.mCountRestrictedList.size();
            list.addAll(this.mCountRestrictedList);
            this.mCountRestrictedList.clear();
        }
        Log.d("ColorAbnormalAppManager", "send ACTION_OPPO_GUARD_ELF_COUNT_RESTRICT_LIST! count == " + count);
        Intent intent = new Intent("android.intent.action.OPPO_GUARD_ELF_COUNT_RESTRICT_LIST");
        intent.putExtra("count", count);
        intent.putStringArrayListExtra("data", list);
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            activityManagerService.mContext.sendBroadcast(intent);
        }
    }

    public void setAms(ActivityManagerService ams) {
        this.mAms = ams;
        initStatus(this.mAms.mContext);
        this.mHandler = new AbnormalAppManagerHandler();
        this.mHandlerReady = true;
        ColorAbnormalAppStatistics.getInstance().init(this, this.mHandler, this.mAms);
    }

    private void initStatus(Context context) {
        boolean hasGuardElfFeature = context.getPackageManager().hasSystemFeature("oppo.guard.elf.support");
        Log.d("ColorAbnormalAppManager", "initStatus hasGuardElfFeature is " + hasGuardElfFeature);
        if (hasGuardElfFeature) {
            this.mSwitch = true;
            initData();
            registerDateChangedReceiver();
            registerLogModule();
        }
    }

    public void handleBroadcastTimeInfo(ProcessRecord proc, long receiverTime, String broadcastName, boolean isTimeout, boolean ordered, boolean sticky, String callerPackage, BroadcastQueue queue) {
        if (this.mHandlerReady && OppoGuardElfConfigUtil.getInstance().getCloseFlag()) {
            if (this.mBroadcastTimeList.isEmpty() || this.mLastCheckBroadcastTime == -1) {
                this.mLastCheckBroadcastTime = SystemClock.uptimeMillis();
            }
            if (!"android.intent.action.OPPO_GUARD_TIME_INFO".equals(broadcastName) && proc != null && broadcastName != null && queue != null) {
                if ((!"background".equals(queue.mQueueName) && !"oppobackground".equals(queue.mQueueName)) || isTimeout || broadcastName.equals(BrightnessConstants.ACTION_BOOT_COMPLETED)) {
                    synchronized (this.mBroadcastLock) {
                        ColorAnrAppInfo info = new ColorAnrAppInfo();
                        info.setProcessName(proc.processName);
                        info.setBroadcastReceiverTime(receiverTime);
                        info.setBroadcastName(broadcastName);
                        if (ordered) {
                            info.setBroadcastType("ordered");
                        } else if (sticky) {
                            info.setBroadcastType("sticky");
                        } else {
                            info.setBroadcastType("any");
                        }
                        if ("foreground".equals(queue.mQueueName)) {
                            info.setForeground(true);
                        } else {
                            info.setForeground(false);
                        }
                        info.mCpu = cpuSumInfo();
                        info.setCallerPackage(callerPackage);
                        info.setGuardTimeout(isTimeout);
                        this.mBroadcastTimeList.add(info);
                    }
                    long delay = OppoGuardElfConfigUtil.getInstance().getCheckBroadcastServiceTime();
                    if (!this.mHandler.hasMessages(101)) {
                        this.mHandler.sendEmptyMessageDelayed(101, delay);
                    }
                }
            }
        }
    }

    public void handleBroadcastReceiver(ProcessRecord proc, Intent intent, String packageName, String callerName, BroadcastQueue queue, boolean isStart, boolean ordered) {
        if (this.mHandlerReady && proc != null && OppoGuardElfConfigUtil.getInstance().getCloseFlag() && intent != null && packageName != null && queue != null && ordered) {
            String broadcastName = intent.getAction();
            if (!"android.intent.action.OPPO_GUARD_TIME_INFO".equals(broadcastName)) {
                synchronized (this.mBroadcastReceiver) {
                    ColorAnrAppInfo broadcastInfo = getBroadcastReceiverInList(this.mBroadcastReceiverList, broadcastName, packageName);
                    if (broadcastInfo == null) {
                        ColorAnrAppInfo info = new ColorAnrAppInfo();
                        info.setBroadcastName(broadcastName);
                        info.setPid(proc.pid);
                        info.setProcessName(packageName);
                        info.setCallerPackage(callerName);
                        if ("foreground".equals(queue.mQueueName)) {
                            info.setForeground(true);
                        } else {
                            info.setForeground(false);
                        }
                        info.mFirstPro = isStart;
                        info.mOrdered = ordered;
                        this.mBroadcastReceiverList.add(info);
                        info.mCount++;
                        info.mChangeCount++;
                    } else if (broadcastInfo != null) {
                        int pid = proc.pid;
                        if (pid >= 0 && pid != broadcastInfo.getPid()) {
                            broadcastInfo.mChangeCount++;
                        }
                        broadcastInfo.mCount++;
                    }
                }
                long delay = OppoGuardElfConfigUtil.getInstance().getCheckTopBroadcastTime();
                if (!this.mHandler.hasMessages(104)) {
                    this.mHandler.sendEmptyMessageDelayed(104, delay);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendBroadcastReceiver(List<ColorAnrAppInfo> list, ArrayList<String> info) {
        if (this.mHandlerReady) {
            this.mHandler.removeMessages(104);
            if (info != null) {
                info.clear();
            }
            synchronized (this.mBroadcastReceiver) {
                for (ColorAnrAppInfo appinfo : list) {
                    if (appinfo != null) {
                        info.add(appinfo.broadcastReceiverToString());
                    }
                }
            }
            if (this.mAms != null) {
                Intent intent = new Intent("android.intent.action.OPPO_GUARD_TIME_INFO");
                intent.putStringArrayListExtra("data", new ArrayList<>(info));
                intent.putExtra("eventId", "TopBroadcastTime");
                this.mAms.mContext.sendBroadcast(intent);
            }
            synchronized (this.mBroadcastReceiver) {
                list.clear();
            }
        }
    }

    public void handleServicesTimeInfo(ServiceRecord r, boolean isTimeout) {
        if (this.mHandlerReady && OppoGuardElfConfigUtil.getInstance().getCloseFlag()) {
            long time = SystemClock.uptimeMillis();
            if (this.mServiceTimeList.isEmpty() || this.mLastCheckServiceTime == -1) {
                this.mLastCheckServiceTime = time;
            }
            if (r != null && r.app != null) {
                if (r.app.execServicesFg || isTimeout) {
                    ColorAnrAppInfo info = new ColorAnrAppInfo();
                    info.setProcessName(r.toString());
                    info.setBroadcastReceiverTime(time - r.executingStart);
                    info.setForeground(r.app.execServicesFg);
                    info.setGuardTimeout(isTimeout);
                    info.mCpu = cpuSumInfo();
                    this.mServiceTimeList.add(info);
                    long delay = OppoGuardElfConfigUtil.getInstance().getCheckBroadcastServiceTime();
                    if (!this.mHandler.hasMessages(102)) {
                        this.mHandler.sendEmptyMessageDelayed(102, delay);
                    }
                }
            }
        }
    }

    public void handleTopBroadcast(String callerPackage, int callingPid, int callingUid, String action, boolean ordered, boolean sticky, BroadcastQueue queue) {
        if (this.mHandlerReady && OppoGuardElfConfigUtil.getInstance().getCloseFlag() && callerPackage != null && !"android".equals(callerPackage) && !"android.intent.action.OPPO_GUARD_TIME_INFO".equals(action)) {
            if (callingPid >= 0 && callingUid < 10000 && (action != null || action != "")) {
                ColorAnrAppInfo broadcastInfo = getBroadcastInfoInList(this.mTopBroadcastList, action, callingUid);
                if (broadcastInfo == null) {
                    ColorAnrAppInfo info = new ColorAnrAppInfo();
                    info.setCallerPackage(callerPackage);
                    info.setPid(callingPid);
                    info.setUid(callingUid);
                    info.setBroadcastName(action);
                    if (ordered) {
                        info.setBroadcastType("ordered");
                    } else if (sticky) {
                        info.setBroadcastType("sticky");
                    } else {
                        info.setBroadcastType("any");
                    }
                    if ("foreground".equals(queue.mQueueName) || "oppoforeground".equals(queue.mQueueName)) {
                        info.setForeground(true);
                    } else {
                        info.setForeground(false);
                    }
                    info.setCount(1);
                    this.mTopBroadcastList.add(info);
                } else {
                    broadcastInfo.setCount(broadcastInfo.getCount() + 1);
                }
            } else if (callingPid >= 0 && callingUid > 10000 && !(action == null && action == "")) {
                ColorAnrAppInfo broadcastInfo2 = getBroadcastInfoInList(this.mTopThirdBroadcastList, action, callingUid);
                if (broadcastInfo2 == null) {
                    ColorAnrAppInfo info2 = new ColorAnrAppInfo();
                    info2.setCallerPackage(callerPackage);
                    info2.setPid(callingPid);
                    info2.setUid(callingUid);
                    info2.setBroadcastName(action);
                    if (ordered) {
                        info2.setBroadcastType("ordered");
                    } else if (sticky) {
                        info2.setBroadcastType("sticky");
                    } else {
                        info2.setBroadcastType("any");
                    }
                    if ("foreground".equals(queue.mQueueName) || "oppoforeground".equals(queue.mQueueName)) {
                        info2.setForeground(true);
                    } else {
                        info2.setForeground(false);
                    }
                    info2.setCount(1);
                    this.mTopThirdBroadcastList.add(info2);
                } else {
                    broadcastInfo2.setCount(broadcastInfo2.getCount() + 1);
                }
            }
            long delay = OppoGuardElfConfigUtil.getInstance().getCheckTopBroadcastTime();
            if (!this.mHandler.hasMessages(103)) {
                this.mHandler.sendEmptyMessageDelayed(103, delay);
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendTopBroadcast() {
        this.mTopBroadcastInfo.clear();
        Collections.sort(this.mTopBroadcastList, this.mBroadcastComparator);
        Collections.sort(this.mTopThirdBroadcastList, this.mBroadcastComparator);
        int topSize = this.mTopBroadcastList.size();
        int thirdSize = this.mTopThirdBroadcastList.size();
        int size = OppoGuardElfConfigUtil.getInstance().getTopBroadcastNumber();
        int i = 0;
        while (i < topSize && i < size) {
            ColorAnrAppInfo anrApp = this.mTopBroadcastList.get(i);
            if (anrApp != null && ((long) anrApp.getCount()) > OppoGuardElfConfigUtil.getInstance().getBroadcastSendCount()) {
                this.mTopBroadcastInfo.add(anrApp.topBroadcastToString());
            }
            i++;
        }
        int i2 = 0;
        while (i2 < thirdSize && i2 < size) {
            ColorAnrAppInfo anrApp2 = this.mTopThirdBroadcastList.get(i2);
            if (anrApp2 != null && ((long) anrApp2.getCount()) > OppoGuardElfConfigUtil.getInstance().getBroadcastSendCount()) {
                this.mTopBroadcastInfo.add(anrApp2.topBroadcastToString());
            }
            i2++;
        }
        if (this.mAms != null) {
            ArrayList<String> list = new ArrayList<>(this.mTopBroadcastInfo);
            if (!list.isEmpty()) {
                Intent intent = new Intent("android.intent.action.OPPO_GUARD_TIME_INFO");
                intent.putStringArrayListExtra("data", list);
                intent.putExtra("eventId", "TopBroadcastTime");
                this.mAms.mContext.sendBroadcast(intent);
            }
        }
        this.mTopBroadcastList.clear();
        this.mTopThirdBroadcastList.clear();
    }

    /* access modifiers changed from: private */
    public void sendTimeBroadcast(List<ColorAnrAppInfo> list, ArrayList<String> info, String type, long time, int msg) {
        if (this.mHandlerReady && list != null && info != null && type != null) {
            this.mHandler.removeMessages(msg);
            info.clear();
            synchronized (this.mBroadcastLock) {
                if ("ServicesTime".equals(type)) {
                    for (ColorAnrAppInfo appinfo : list) {
                        if (appinfo != null) {
                            info.add(appinfo.serviceToString());
                        }
                    }
                } else if ("BroadcastTime".equals(type)) {
                    for (ColorAnrAppInfo appinfo2 : list) {
                        if (appinfo2 != null) {
                            info.add(appinfo2.broadcastToString());
                        }
                    }
                }
            }
            if (this.mAms != null) {
                Intent intent = new Intent("android.intent.action.OPPO_GUARD_TIME_INFO");
                intent.putStringArrayListExtra("data", new ArrayList<>(info));
                intent.putExtra("eventId", type);
                this.mAms.mContext.sendBroadcast(intent);
            }
            list.clear();
        }
    }

    private ColorAnrAppInfo getBroadcastInfoInList(List<ColorAnrAppInfo> list, String action, int callingUid) {
        String name;
        if (list == null) {
            return null;
        }
        for (ColorAnrAppInfo appinfo : list) {
            if (appinfo != null && (name = appinfo.getBroadcastName()) != null && action != null && name.equals(action) && appinfo.getUid() == callingUid) {
                return appinfo;
            }
        }
        return null;
    }

    private ColorAnrAppInfo getBroadcastReceiverInList(List<ColorAnrAppInfo> list, String action, String packageName) {
        String broadcastName;
        if (list == null || packageName == null) {
            return null;
        }
        for (ColorAnrAppInfo appinfo : list) {
            if (appinfo != null && (broadcastName = appinfo.getBroadcastName()) != null && broadcastName.equals(action) && packageName.equals(appinfo.mProcessName)) {
                return appinfo;
            }
        }
        return null;
    }

    private long readCpu(String fileName) {
        long freq = 0;
        InputStream inStream = null;
        try {
            if (!new File(fileName.toString()).exists()) {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException e) {
                    }
                }
                return 0;
            }
            InputStream inStream2 = new FileInputStream(fileName.toString());
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(inStream2));
            for (String line = buffReader.readLine(); line != null; line = buffReader.readLine()) {
                freq = (long) Integer.valueOf(line).intValue();
            }
            try {
                inStream2.close();
            } catch (IOException e2) {
            }
            return freq;
        } catch (Exception e3) {
            Log.w("ColorAbnormalAppManager", "readCpu exception=" + e3);
            if (inStream != null) {
                inStream.close();
            }
        } catch (Throwable th) {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    private long cpuSumInfo() {
        long sum_freq = 0;
        for (int i = 0; i < 8; i++) {
            SystemClock.uptimeMillis();
            sum_freq += readCpu(new String("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq"));
            SystemClock.uptimeMillis();
        }
        return sum_freq;
    }

    public boolean inRestrictAppList(String packageName, int userId) {
        if (userId == 999) {
            userId = 0;
        }
        boolean result = false;
        synchronized (mColorAbnormalAppManager) {
            List<String> screenOffRestrictedList = getScreenOffRestrictedList(userId);
            if (screenOffRestrictedList != null && screenOffRestrictedList.contains(packageName)) {
                result = true;
            }
        }
        return result;
    }

    private void setDynamicDebugSwitch(boolean on) {
        this.DynamicDebug = on;
        this.DEBUG_SWITCH = DEBUG_DETAIL | this.DynamicDebug;
        ColorAamUtils.getInstance().setDynamicDebugSwitch(this.DynamicDebug);
    }

    public void openLog(boolean on) {
        Log.i("ColorAbnormalAppManager", "#####openlog####");
        Log.i("ColorAbnormalAppManager", "DynamicDebug = " + getInstance().DynamicDebug);
        setDynamicDebugSwitch(on);
        Log.i("ColorAbnormalAppManager", "DynamicDebug = " + getInstance().DynamicDebug);
    }

    private void registerLogModule() {
        try {
            Log.i("ColorAbnormalAppManager", "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Log.i("ColorAbnormalAppManager", "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Log.i("ColorAbnormalAppManager", "invoke " + m);
            m.invoke(cls.newInstance(), ColorAbnormalAppManager.class.getName());
            Log.i("ColorAbnormalAppManager", "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    public boolean isScreenOffRestrictApp(String packageName, int userId) {
        if (OppoSysStateManager.getInstance().restrictStartupBg()) {
            return inRestrictAppList(packageName, userId);
        }
        return false;
    }

    public boolean isScreenOffRestrict() {
        return OppoSysStateManager.getInstance().restrictStartupBg();
    }

    public boolean isNotRestrictApp(String packageName) {
        boolean result = false;
        synchronized (mColorAbnormalAppManager) {
            if (this.mNotRestrictAppList.contains(packageName)) {
                result = true;
            }
        }
        return result;
    }

    public boolean validStartProvider(ContentProviderRecord cpr) {
        ComponentName cpn;
        if (cpr == null || (cpn = cpr.name) == null) {
            return false;
        }
        String pkgName = cpn.getPackageName();
        if (!ColorAppStartupManagerUtils.getInstance().isInAamProviderWhitePkgList(pkgName)) {
            return validNewProc(pkgName, UserHandle.getUserId(cpr.uid), TYPE_PROVIDER);
        }
        if (this.DynamicDebug) {
            Log.i("ColorAbnormalAppManager", "provider is ok " + pkgName);
        }
        return false;
    }

    public boolean validStartActivity(ActivityInfo aInfo) {
        if (aInfo == null || aInfo.applicationInfo == null || aInfo.applicationInfo.uid < 10000) {
            return false;
        }
        if (ColorAppStartupManagerUtils.getInstance().isInAamActivityWhitePkgList(aInfo.packageName)) {
            if (this.DynamicDebug) {
                Log.i("ColorAbnormalAppManager", "activity is ok " + aInfo.packageName);
            }
            return false;
        } else if (!validNewProc(aInfo.packageName, UserHandle.getUserId(aInfo.applicationInfo.uid), TYPE_ACTIVITY) || isInLruProcessesLocked(aInfo.applicationInfo.uid)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean validRestartProcess(int uid, ArraySet<String> pkgList) {
        if (uid < 10000 || pkgList.isEmpty()) {
            return false;
        }
        try {
            Iterator<String> it = pkgList.iterator();
            while (it.hasNext()) {
                String pkgName = it.next();
                if (ColorAppStartupManagerUtils.getInstance().isInAamActivityWhitePkgList(pkgName)) {
                    if (!this.DynamicDebug) {
                        return false;
                    }
                    Log.i("ColorAbnormalAppManager", "restart is ok " + pkgName);
                    return false;
                } else if (validNewProc(pkgName, UserHandle.getUserId(uid), TYPE_RESTART)) {
                    if (this.DynamicDebug) {
                        Log.i("ColorAbnormalAppManager", "validRestartProcess false " + pkgName);
                    }
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.w("ColorAbnormalAppManager", "validRestartProcess has exception! ", e);
            return false;
        }
    }

    public boolean validStartService(String pkgName, int userId) {
        return validNewProc(pkgName, userId, TYPE_SERVICE);
    }

    public boolean validStartBroadcast(String pkgName, int userId) {
        return validNewProc(pkgName, userId, TYPE_BROADCAST);
    }

    private boolean isInLruProcessesLocked(int uid) {
        try {
            for (int i = this.mAms.mProcessList.mLruProcesses.size() - 1; i >= 0; i--) {
                ProcessRecord rec = (ProcessRecord) this.mAms.mProcessList.mLruProcesses.get(i);
                if (rec.thread != null && rec.uid == uid) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void registerDateChangedReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.DATE_CHANGED");
        this.mAms.mContext.registerReceiver(this.dateChangedReceiver, filter, null, this.mHandler);
    }
}
