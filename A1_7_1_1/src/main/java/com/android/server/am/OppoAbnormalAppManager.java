package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.oppo.IElsaManager;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
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
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoAbnormalAppManager {
    private static final int BROADCAST_MSG = 101;
    private static final int BROADCAST_RECEIVER_MSG = 104;
    private static final int BROADCAST_TOP_MSG = 103;
    public static boolean DEBUG_DETAIL = false;
    public static int MAX_LIST_COUNT = 0;
    private static final int SERVICE_MSG = 102;
    public static String TAG = null;
    static final String TYPE_ACTIVITY = "activity";
    static final String TYPE_BROADCAST = "service";
    static final String TYPE_OTHER = "other";
    static final String TYPE_PROVIDER = "content provider";
    static final String TYPE_REPEAT = "repeat";
    static final String TYPE_RESTART = "restart";
    static final String TYPE_SERVICE = "broadcast";
    private static OppoAbnormalAppManager mOppoAbnormalAppManager;
    final String ACTION_OPPO_GUARD_ELF_COUNT_RESTRICT_LIST;
    final String ACTION_OPPO_GUARD_ELF_MONITOR;
    final String ACTION_OPPO_GUARD_ELF_SET_RESTRICT;
    final String ACTION_OPPO_GUARD_TIME_INFO;
    boolean DEBUG;
    boolean DEBUG_SWITCH;
    boolean DynamicDebug;
    final String GUARD_ELF_FEATURE_NAME;
    final int RESTRICT_ABNORMAL;
    final int RESTRICT_NO;
    final int RESTRICT_REPEAT;
    final BroadcastReceiver dateChangedReceiver;
    ActivityManagerService mAms;
    private Comparator<OppoAnrAppInfo> mBroadcastComparator;
    private final Object mBroadcastLock;
    private final Object mBroadcastReceiver;
    ArrayList<String> mBroadcastReceiverInfo;
    List<OppoAnrAppInfo> mBroadcastReceiverList;
    ArrayList<String> mBroadcastTimeInfo;
    List<OppoAnrAppInfo> mBroadcastTimeList;
    ArrayList<String> mCountRestrictedList;
    private Handler mHandler;
    protected boolean mHandlerReady;
    long mLastCheckBroadcastTime;
    long mLastCheckServiceTime;
    long mLastCheckTime;
    List<String> mNotRestrictAppList;
    List<String> mPersistRestrictAppList;
    List<String> mScreenOffRestrictAppList;
    ArrayList<String> mServiceTimeInfo;
    List<OppoAnrAppInfo> mServiceTimeList;
    List<OppoAppStartInfo> mStartAppList;
    List<String> mStartInfoWhiteList;
    boolean mSwitch;
    ArrayList<String> mTopBroadcastInfo;
    List<OppoAnrAppInfo> mTopBroadcastList;
    List<OppoAnrAppInfo> mTopThirdBroadcastList;
    ArrayList<String> mUploadInfoList;

    private class AbnormalAppManagerHandler extends Handler {
        /* synthetic */ AbnormalAppManagerHandler(OppoAbnormalAppManager this$0, AbnormalAppManagerHandler abnormalAppManagerHandler) {
            this();
        }

        private AbnormalAppManagerHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    OppoAbnormalAppManager.this.sendTimeBroadcast(OppoAbnormalAppManager.this.mBroadcastTimeList, OppoAbnormalAppManager.this.mBroadcastTimeInfo, "BroadcastTime", OppoAbnormalAppManager.this.mLastCheckBroadcastTime, 101);
                    return;
                case 102:
                    OppoAbnormalAppManager.this.sendTimeBroadcast(OppoAbnormalAppManager.this.mServiceTimeList, OppoAbnormalAppManager.this.mServiceTimeInfo, "ServicesTime", OppoAbnormalAppManager.this.mLastCheckServiceTime, 102);
                    return;
                case 103:
                    OppoAbnormalAppManager.this.sendTopBroadcast();
                    return;
                case 104:
                    OppoAbnormalAppManager.this.sendBroadcastReceiver(OppoAbnormalAppManager.this.mBroadcastReceiverList, OppoAbnormalAppManager.this.mBroadcastReceiverInfo);
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAbnormalAppManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAbnormalAppManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoAbnormalAppManager.<clinit>():void");
    }

    public OppoAbnormalAppManager() {
        this.RESTRICT_NO = 0;
        this.RESTRICT_REPEAT = 1;
        this.RESTRICT_ABNORMAL = 2;
        this.mLastCheckTime = 0;
        this.DEBUG = true;
        this.DynamicDebug = false;
        this.DEBUG_SWITCH = DEBUG_DETAIL | this.DynamicDebug;
        this.mSwitch = false;
        this.ACTION_OPPO_GUARD_ELF_MONITOR = "android.intent.action.OPPO_GUARD_ELF_MONITOR";
        this.ACTION_OPPO_GUARD_ELF_SET_RESTRICT = "android.intent.action.OPPO_GUARD_ELF_SET_RESTRICT";
        this.ACTION_OPPO_GUARD_ELF_COUNT_RESTRICT_LIST = "android.intent.action.OPPO_GUARD_ELF_COUNT_RESTRICT_LIST";
        this.GUARD_ELF_FEATURE_NAME = "oppo.guard.elf.support";
        this.mScreenOffRestrictAppList = new ArrayList();
        this.mPersistRestrictAppList = new ArrayList();
        this.mStartInfoWhiteList = new ArrayList();
        this.mUploadInfoList = new ArrayList();
        this.mCountRestrictedList = new ArrayList();
        this.mStartAppList = new ArrayList();
        this.mNotRestrictAppList = new ArrayList();
        this.mAms = null;
        this.ACTION_OPPO_GUARD_TIME_INFO = "android.intent.action.OPPO_GUARD_TIME_INFO";
        this.mBroadcastTimeList = new ArrayList();
        this.mBroadcastTimeInfo = new ArrayList();
        this.mLastCheckBroadcastTime = 0;
        this.mServiceTimeList = new ArrayList();
        this.mServiceTimeInfo = new ArrayList();
        this.mLastCheckServiceTime = 0;
        this.mTopBroadcastList = new ArrayList();
        this.mTopThirdBroadcastList = new ArrayList();
        this.mBroadcastReceiverList = new ArrayList();
        this.mBroadcastReceiverInfo = new ArrayList();
        this.mTopBroadcastInfo = new ArrayList();
        this.mBroadcastLock = new Object();
        this.mBroadcastReceiver = new Object();
        this.mHandlerReady = false;
        this.mHandler = null;
        this.mBroadcastComparator = new Comparator<OppoAnrAppInfo>() {
            public int compare(OppoAnrAppInfo lhs, OppoAnrAppInfo rhs) {
                if (lhs.getCount() == rhs.getCount()) {
                    return 0;
                }
                return lhs.getCount() > rhs.getCount() ? -1 : 1;
            }
        };
        this.dateChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (OppoAbnormalAppStatistics.getInstance().isNeedUpload()) {
                    OppoAbnormalAppStatistics.getInstance().uploadAbnormalAppInfoList();
                }
            }
        };
    }

    public static final OppoAbnormalAppManager getInstance() {
        if (mOppoAbnormalAppManager == null) {
            mOppoAbnormalAppManager = new OppoAbnormalAppManager();
        }
        return mOppoAbnormalAppManager;
    }

    public void initData() {
        Log.d(TAG, "initData");
        this.mScreenOffRestrictAppList = OppoAamUtils.getInstance().readScreenOffResrictFile();
        this.mStartInfoWhiteList = OppoAamUtils.getInstance().readStartInfoWhiteFile();
        this.mPersistRestrictAppList = OppoAamUtils.getInstance().readPersistRestrictFile();
        this.mNotRestrictAppList = OppoAamUtils.getInstance().readNotRestrictFile();
    }

    public void setLastCheckTime(long lastCheckTime) {
        this.mLastCheckTime = lastCheckTime;
    }

    public void cleanStartAppList() {
        if (this.DynamicDebug) {
            Log.d(TAG, "cleanAbnormalAppList");
        }
        this.mStartAppList.clear();
    }

    public void cleanUploadInfoList() {
        if (this.DynamicDebug) {
            Log.d(TAG, "cleanUploadInfoList");
        }
        this.mUploadInfoList.clear();
    }

    public void notifyAbnormalInfo() {
        boolean hasAbnormalApp = false;
        for (OppoAppStartInfo appinfo : this.mStartAppList) {
            if (appinfo.getStartCount() > OppoGuardElfConfigUtil.getInstance().getAbnormalStartCount()) {
                if (this.DEBUG) {
                    Log.d(TAG, "#################  AbnormalInfo app  ##########################");
                    appinfo.dumpInfo("abnormal appinfo");
                    Log.d(TAG, "###############################################################");
                }
                if (handleAbnormalApp(appinfo)) {
                    this.mUploadInfoList.add(appinfo.infoToString("abnormal appinfo"));
                    hasAbnormalApp = true;
                }
            } else if (appinfo.getStartCount() > OppoGuardElfConfigUtil.getInstance().getCollectStartCount()) {
                if (this.DynamicDebug) {
                    Log.d(TAG, "---------------------  warning app  -----------------------------");
                    appinfo.dumpInfo("warning appinfo");
                    Log.d(TAG, "-----------------------------------------------------------------");
                }
                this.mUploadInfoList.add(appinfo.infoToString("warning appinfo"));
            } else if (this.DynamicDebug) {
                Log.d(TAG, "-----------------  normalInfo app  -----------------------------");
                appinfo.dumpInfo("normal appinfo");
                Log.d(TAG, "-----------------------------------------------------------------");
            }
        }
        sendAbnormalNotify();
        cleanStartAppList();
        cleanUploadInfoList();
        if (hasAbnormalApp) {
            if (this.DEBUG_SWITCH) {
                Log.d(TAG, "hasAbnormalApp savePersistRestrictedFile!");
            }
            savePersistRestrictedFile(this.mPersistRestrictAppList);
        }
    }

    public void collectStartAppInfo(String processName, String pkgName, String startType, boolean isThird) {
        OppoAppStartInfo appStartInfo = getAppInfoInList(processName);
        if (appStartInfo == null) {
            this.mStartAppList.add(OppoAppStartInfo.builder(processName, pkgName, startType, isThird));
            return;
        }
        appStartInfo.increaseStartCount(startType);
        appStartInfo.setCurStartTime(SystemClock.elapsedRealtime());
    }

    public OppoAppStartInfo getAppInfoInList(String processName) {
        for (OppoAppStartInfo appinfo : this.mStartAppList) {
            if (appinfo.getProcessName().equals(processName)) {
                return appinfo;
            }
        }
        return null;
    }

    public void handleStartAppInfo(ProcessRecord app, String startType) {
        if (this.mSwitch) {
            boolean isThird = false;
            String pkgName = IElsaManager.EMPTY_PACKAGE;
            if (app.info != null) {
                pkgName = app.info.packageName;
                if ((app.info.flags & 1) == 0) {
                    isThird = true;
                }
            }
            handleStartAppInfo(app.processName, pkgName, startType, isThird);
            if (OppoAbnormalAppStatistics.getInstance().isNeedUpload()) {
                if (1 == validNewProcForUpload(pkgName, startType)) {
                    OppoAbnormalAppStatistics.getInstance().collectAbnormalAppInfo(pkgName, TYPE_REPEAT, false);
                } else if (2 == validNewProcForUpload(pkgName, startType)) {
                    OppoAbnormalAppStatistics.getInstance().collectAbnormalAppInfo(pkgName, startType, false);
                }
            }
        }
    }

    public void handleStartAppInfo(String processName, String pkgName, String startType, boolean isThird) {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "handleStartAppInfo processName == " + processName + "  startType == " + startType);
        }
        long time = SystemClock.elapsedRealtime();
        if (this.mStartAppList.isEmpty()) {
            this.mLastCheckTime = time;
            Log.d(TAG, "Now Begin Monitor App StartInfo's Time == " + time);
        }
        collectStartAppInfo(processName, pkgName, startType, isThird);
        if (Math.abs(time - this.mLastCheckTime) > OppoGuardElfConfigUtil.getInstance().getCheckStartTimeInterval()) {
            notifyAbnormalInfo();
        }
        if (this.DynamicDebug) {
            Log.d(TAG, "handleStartAppInfo cost time ==  " + (SystemClock.elapsedRealtime() - time));
        }
    }

    public boolean isPackageRestricted(String packageName) {
        synchronized (mOppoAbnormalAppManager) {
            if (this.mPersistRestrictAppList.contains(packageName)) {
                return true;
            }
            return false;
        }
    }

    public boolean setPackageRestricted(String packageName, boolean isRestrict) {
        boolean isChange = false;
        if (isPackageRestricted(packageName)) {
            if (!isRestrict) {
                if (this.DEBUG) {
                    Log.d(TAG, "setPackageRestricted remove packageName  " + packageName);
                }
                synchronized (mOppoAbnormalAppManager) {
                    this.mPersistRestrictAppList.remove(packageName);
                    isChange = true;
                }
            }
        } else if (isRestrict) {
            if (this.DEBUG) {
                Log.d(TAG, "setPackageRestricted add packageName  " + packageName);
            }
            synchronized (mOppoAbnormalAppManager) {
                this.mPersistRestrictAppList.add(packageName);
                isChange = true;
            }
        }
        return isChange;
    }

    public void setPackageUnRestricted(String packageName) {
        if (this.mSwitch && setPackageRestricted(packageName, false)) {
            Log.d(TAG, "setPackageUnRestricted updateRestrictedFile!!!!");
            savePersistRestrictedFile(this.mPersistRestrictAppList);
        }
    }

    private boolean validNewProc(String packageName, String type) {
        boolean result = false;
        if (!this.mSwitch || packageName == null) {
            return false;
        }
        OppoAbnormalAppManager oppoAbnormalAppManager;
        if (TYPE_ACTIVITY.equals(type) || !isPackageRestricted(packageName)) {
            if (OppoSysStateManager.getInstance().restrictStartupBg()) {
                oppoAbnormalAppManager = mOppoAbnormalAppManager;
                synchronized (oppoAbnormalAppManager) {
                    if (this.mScreenOffRestrictAppList.contains(packageName)) {
                        if (!this.mCountRestrictedList.contains(packageName)) {
                            Log.d(TAG, "mCountRestrictedList add packageName == " + packageName);
                            this.mCountRestrictedList.add(packageName);
                        }
                        OppoAbnormalAppStatistics.getInstance().collectAbnormalAppInfo(packageName, type, true);
                        result = true;
                    }
                }
            }
            return result;
        }
        oppoAbnormalAppManager = mOppoAbnormalAppManager;
        synchronized (oppoAbnormalAppManager) {
            if (!this.mStartInfoWhiteList.contains(packageName)) {
                OppoAbnormalAppStatistics.getInstance().collectAbnormalAppInfo(packageName, TYPE_REPEAT, true);
                result = true;
            }
        }
        return result;
    }

    private int validNewProcForUpload(String packageName, String type) {
        int result = 0;
        if (!this.mSwitch || packageName == null) {
            return 0;
        }
        OppoAbnormalAppManager oppoAbnormalAppManager;
        if (TYPE_ACTIVITY.equals(type) || !isPackageRestricted(packageName)) {
            if (OppoSysStateManager.getInstance().restrictStartupBg()) {
                oppoAbnormalAppManager = mOppoAbnormalAppManager;
                synchronized (oppoAbnormalAppManager) {
                    if (this.mScreenOffRestrictAppList.contains(packageName)) {
                        result = 2;
                    }
                }
            }
            return result;
        }
        oppoAbnormalAppManager = mOppoAbnormalAppManager;
        synchronized (oppoAbnormalAppManager) {
            if (!this.mStartInfoWhiteList.contains(packageName)) {
                result = 1;
            }
        }
        return result;
    }

    public boolean handleAbnormalApp(OppoAppStartInfo appinfo) {
        String pkgName = appinfo.getPkgName();
        if (appinfo.getIsThird()) {
            ActivityRecord r = this.mAms.mStackSupervisor.getTopRunningActivityLocked();
            if (r != null && pkgName.equals(r.packageName)) {
                Log.d(TAG, "don't handle! return for top activity");
                return false;
            } else if (this.mAms == null || !this.mAms.mContext.getPackageManager().isFullFunctionMode()) {
                Log.d(TAG, "handleAbnormalApp setPackageRestricted true!");
                setPackageRestricted(pkgName, true);
                if (!this.mStartInfoWhiteList.contains(pkgName)) {
                    killAbnormalApp(pkgName);
                }
            } else {
                Log.d(TAG, "CTS TEST! don't handle!");
                return false;
            }
        }
        Log.d(TAG, "handleAbnormalApp! pkg isn't third app    " + pkgName);
        return true;
    }

    public void killAbnormalApp(String packageName) {
        Log.d(TAG, "killAbnormalApp  " + packageName);
        if (this.mAms != null) {
            this.mAms.forceStopPackage(packageName, -2);
        }
    }

    public void sendAbnormalNotify() {
        Intent intent = new Intent("android.intent.action.OPPO_GUARD_ELF_MONITOR");
        intent.putExtra(SoundModelContract.KEY_TYPE, "startinfo");
        intent.putStringArrayListExtra("data", new ArrayList(this.mUploadInfoList));
        if (this.mAms != null) {
            this.mAms.mContext.sendBroadcast(intent);
        }
    }

    public void sendUnRestrictNotify(String packageName) {
        Intent intent = new Intent("android.intent.action.OPPO_GUARD_ELF_SET_RESTRICT");
        intent.putExtra(SoundModelContract.KEY_TYPE, "unrestrict");
        intent.putExtra("pkgName", packageName);
        if (this.mAms != null) {
            this.mAms.mContext.sendBroadcast(intent);
        }
    }

    public void updateScreenOffRestrictedList(List<String> screenoffRestrictList) {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateScreenOffRestrictedList!!!!");
        }
        if (this.DynamicDebug) {
            for (String str : screenoffRestrictList) {
                Log.d(TAG, "updateScreenOffRestrictedList str == " + str);
            }
        }
        synchronized (mOppoAbnormalAppManager) {
            this.mScreenOffRestrictAppList.clear();
            this.mScreenOffRestrictAppList.addAll(screenoffRestrictList);
        }
    }

    public void updateStartInfoWhiteList(List<String> startInfoWhiteList) {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateStartInfoWhiteList!!!!");
        }
        if (this.DynamicDebug) {
            for (String str : startInfoWhiteList) {
                Log.d(TAG, "updateStartInfoWhiteList str == " + str);
            }
        }
        List<String> cancelList = new ArrayList();
        synchronized (mOppoAbnormalAppManager) {
            for (String str2 : this.mStartInfoWhiteList) {
                if (!startInfoWhiteList.contains(str2)) {
                    cancelList.add(str2);
                }
            }
            this.mStartInfoWhiteList.clear();
            this.mStartInfoWhiteList.addAll(startInfoWhiteList);
        }
        for (String str22 : cancelList) {
            if (this.mAms != null) {
                Log.d(TAG, "forceStopPackage " + str22 + "  cancel from startinfo whitelist!");
                this.mAms.forceStopPackage(str22, -2);
            }
        }
    }

    public void updateScreenStatus(boolean isScreenOn) {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateScreenStatus isScreenOn = " + isScreenOn);
        }
        if (isScreenOn) {
            handleCountRestrictedList();
        }
    }

    public void savePersistRestrictedFile(List<String> restrictList) {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "savePersistRestrictedFile!");
        }
        OppoAamUtils.getInstance().savePersistRestrictFile(restrictList);
    }

    public void updateNotRestrictedList(List<String> list) {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateNotRestrictedList!!!!");
        }
        if (this.DynamicDebug) {
            for (String str : list) {
                Log.d(TAG, "updateNotRestrictedList str == " + str);
            }
        }
        synchronized (mOppoAbnormalAppManager) {
            this.mNotRestrictAppList.clear();
            this.mNotRestrictAppList.addAll(list);
        }
    }

    private void handleCountRestrictedList() {
        int count;
        ArrayList<String> list = new ArrayList();
        synchronized (mOppoAbnormalAppManager) {
            count = this.mCountRestrictedList.size();
            list.addAll(this.mCountRestrictedList);
            this.mCountRestrictedList.clear();
        }
        Log.d(TAG, "send ACTION_OPPO_GUARD_ELF_COUNT_RESTRICT_LIST! count == " + count);
        Intent intent = new Intent("android.intent.action.OPPO_GUARD_ELF_COUNT_RESTRICT_LIST");
        intent.putExtra("count", count);
        intent.putStringArrayListExtra("data", list);
        if (this.mAms != null) {
            this.mAms.mContext.sendBroadcast(intent);
        }
    }

    public void setAms(ActivityManagerService ams) {
        this.mAms = ams;
        initStatus(this.mAms.mContext);
        this.mHandler = new AbnormalAppManagerHandler(this, null);
        this.mHandlerReady = true;
        OppoAbnormalAppStatistics.getInstance().init(this, this.mHandler, this.mAms);
    }

    public void initStatus(Context context) {
        boolean hasGuardElfFeature = context.getPackageManager().hasSystemFeature("oppo.guard.elf.support");
        Log.d(TAG, "initStatus hasGuardElfFeature is " + hasGuardElfFeature);
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
                if (queue == null || (!("background".equals(queue.mQueueName) || "oppobackground".equals(queue.mQueueName)) || isTimeout || broadcastName.equals("android.intent.action.BOOT_COMPLETED"))) {
                    synchronized (this.mBroadcastLock) {
                        if (!(proc == null || broadcastName == null)) {
                            OppoAnrAppInfo info = new OppoAnrAppInfo();
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
                    }
                    long delay = OppoGuardElfConfigUtil.getInstance().getCheckBroadcastServiceTime();
                    if (!this.mHandler.hasMessages(101)) {
                        this.mHandler.sendEmptyMessageDelayed(101, delay);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:3:0x0006, code:
            return;
     */
    /* JADX WARNING: Missing block: B:9:0x0016, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleBroadcastReceiver(ProcessRecord proc, Intent intent, String packageName, String callerName, BroadcastQueue queue, boolean isStart, boolean ordered) {
        if (this.mHandlerReady && proc != null && OppoGuardElfConfigUtil.getInstance().getCloseFlag() && intent != null && packageName != null && queue != null && ordered) {
            String broadcastName = intent.getAction();
            if (!"android.intent.action.OPPO_GUARD_TIME_INFO".equals(broadcastName)) {
                synchronized (this.mBroadcastReceiver) {
                    OppoAnrAppInfo broadcastInfo = getBroadcastReceiverInList(this.mBroadcastReceiverList, broadcastName, packageName);
                    if (packageName != null && broadcastInfo == null) {
                        OppoAnrAppInfo info = new OppoAnrAppInfo();
                        info.setBroadcastName(broadcastName);
                        if (proc != null) {
                            info.setPid(proc.pid);
                        }
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
                        int pid = -1;
                        if (proc != null) {
                            pid = proc.pid;
                        }
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

    private void sendBroadcastReceiver(List<OppoAnrAppInfo> list, ArrayList<String> info) {
        if (this.mHandlerReady) {
            this.mHandler.removeMessages(104);
            if (info != null) {
                info.clear();
            }
            synchronized (this.mBroadcastReceiver) {
                for (OppoAnrAppInfo appinfo : list) {
                    if (appinfo != null) {
                        info.add(appinfo.broadcastReceiverToString());
                    }
                }
            }
            if (this.mAms != null) {
                Intent intent = new Intent("android.intent.action.OPPO_GUARD_TIME_INFO");
                intent.putStringArrayListExtra("data", new ArrayList(info));
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
                    if (r != null) {
                        OppoAnrAppInfo info = new OppoAnrAppInfo();
                        info.setProcessName(r.toString());
                        info.setBroadcastReceiverTime(time - r.executingStart);
                        info.setForeground(r.app.execServicesFg);
                        info.setGuardTimeout(isTimeout);
                        info.mCpu = cpuSumInfo();
                        this.mServiceTimeList.add(info);
                    }
                    long delay = OppoGuardElfConfigUtil.getInstance().getCheckBroadcastServiceTime();
                    if (!this.mHandler.hasMessages(102)) {
                        this.mHandler.sendEmptyMessageDelayed(102, delay);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x001b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleTopBroadcast(String callerPackage, int callingPid, int callingUid, String action, boolean ordered, boolean sticky, BroadcastQueue queue) {
        if (this.mHandlerReady && OppoGuardElfConfigUtil.getInstance().getCloseFlag() && callerPackage != null && !"android".equals(callerPackage) && !"android.intent.action.OPPO_GUARD_TIME_INFO".equals(action)) {
            OppoAnrAppInfo broadcastInfo;
            OppoAnrAppInfo info;
            if (callingPid >= 0 && callingUid < 10000 && (action != null || action != IElsaManager.EMPTY_PACKAGE)) {
                broadcastInfo = getBroadcastInfoInList(this.mTopBroadcastList, action, callingUid);
                if (broadcastInfo == null) {
                    info = new OppoAnrAppInfo();
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
            } else if (callingPid >= 0 && callingUid > 10000 && !(action == null && action == IElsaManager.EMPTY_PACKAGE)) {
                broadcastInfo = getBroadcastInfoInList(this.mTopThirdBroadcastList, action, callingUid);
                if (broadcastInfo == null) {
                    info = new OppoAnrAppInfo();
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
                    this.mTopThirdBroadcastList.add(info);
                } else {
                    broadcastInfo.setCount(broadcastInfo.getCount() + 1);
                }
            }
            long delay = OppoGuardElfConfigUtil.getInstance().getCheckTopBroadcastTime();
            if (!this.mHandler.hasMessages(103)) {
                this.mHandler.sendEmptyMessageDelayed(103, delay);
            }
        }
    }

    private void sendTopBroadcast() {
        OppoAnrAppInfo anrApp;
        this.mTopBroadcastInfo.clear();
        Collections.sort(this.mTopBroadcastList, this.mBroadcastComparator);
        Collections.sort(this.mTopThirdBroadcastList, this.mBroadcastComparator);
        int topSize = this.mTopBroadcastList.size();
        int thirdSize = this.mTopThirdBroadcastList.size();
        int size = OppoGuardElfConfigUtil.getInstance().getTopBroadcastNumber();
        int i = 0;
        while (i < topSize && i < size) {
            anrApp = (OppoAnrAppInfo) this.mTopBroadcastList.get(i);
            if (anrApp != null && ((long) anrApp.getCount()) > OppoGuardElfConfigUtil.getInstance().getBroadcastSendCount()) {
                this.mTopBroadcastInfo.add(anrApp.topBroadcastToString());
            }
            i++;
        }
        i = 0;
        while (i < thirdSize && i < size) {
            anrApp = (OppoAnrAppInfo) this.mTopThirdBroadcastList.get(i);
            if (anrApp != null && ((long) anrApp.getCount()) > OppoGuardElfConfigUtil.getInstance().getBroadcastSendCount()) {
                this.mTopBroadcastInfo.add(anrApp.topBroadcastToString());
            }
            i++;
        }
        if (this.mAms != null) {
            ArrayList<String> list = new ArrayList(this.mTopBroadcastInfo);
            if (!(list == null || list.isEmpty())) {
                Intent intent = new Intent("android.intent.action.OPPO_GUARD_TIME_INFO");
                intent.putStringArrayListExtra("data", list);
                intent.putExtra("eventId", "TopBroadcastTime");
                this.mAms.mContext.sendBroadcast(intent);
            }
        }
        this.mTopBroadcastList.clear();
        this.mTopThirdBroadcastList.clear();
    }

    private void sendTimeBroadcast(List<OppoAnrAppInfo> list, ArrayList<String> info, String type, long time, int msg) {
        if (this.mHandlerReady && list != null && info != null && type != null) {
            this.mHandler.removeMessages(msg);
            info.clear();
            synchronized (this.mBroadcastLock) {
                if ("ServicesTime".equals(type)) {
                    for (OppoAnrAppInfo appinfo : list) {
                        if (appinfo != null) {
                            info.add(appinfo.serviceToString());
                        }
                    }
                } else if ("BroadcastTime".equals(type)) {
                    for (OppoAnrAppInfo appinfo2 : list) {
                        if (appinfo2 != null) {
                            info.add(appinfo2.broadcastToString());
                        }
                    }
                }
            }
            if (this.mAms != null) {
                Intent intent = new Intent("android.intent.action.OPPO_GUARD_TIME_INFO");
                intent.putStringArrayListExtra("data", new ArrayList(info));
                intent.putExtra("eventId", type);
                this.mAms.mContext.sendBroadcast(intent);
            }
            list.clear();
        }
    }

    public OppoAnrAppInfo getBroadcastInfoInList(List<OppoAnrAppInfo> list, String action, int callingUid) {
        if (list == null) {
            return null;
        }
        for (OppoAnrAppInfo appinfo : list) {
            if (appinfo != null) {
                String name = appinfo.getBroadcastName();
                if (name != null && action != null && name.equals(action) && appinfo.getUid() == callingUid) {
                    return appinfo;
                }
            }
        }
        return null;
    }

    public OppoAnrAppInfo getBroadcastReceiverInList(List<OppoAnrAppInfo> list, String action, String packageName) {
        if (list == null || packageName == null) {
            return null;
        }
        for (OppoAnrAppInfo appinfo : list) {
            if (appinfo != null) {
                String broadcastName = appinfo.getBroadcastName();
                if (broadcastName != null && broadcastName.equals(action) && packageName != null && packageName.equals(appinfo.mProcessName)) {
                    return appinfo;
                }
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x006c A:{SYNTHETIC, Splitter: B:28:0x006c} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0063 A:{SYNTHETIC, Splitter: B:23:0x0063} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private long readCpu(String fileName) {
        Exception e;
        Throwable th;
        long freq = 0;
        InputStream inStream = null;
        try {
            if (!new File(fileName.toString()).exists()) {
                return 0;
            }
            InputStream inStream2 = new FileInputStream(fileName.toString());
            if (inStream2 != null) {
                try {
                    BufferedReader buffReader = new BufferedReader(new InputStreamReader(inStream2));
                    for (String line = buffReader.readLine(); line != null; line = buffReader.readLine()) {
                        freq = (long) Integer.valueOf(line).intValue();
                    }
                } catch (Exception e2) {
                    e = e2;
                    inStream = inStream2;
                    try {
                        Log.w(TAG, "readCpu exception=" + e);
                        if (inStream != null) {
                            try {
                                inStream.close();
                            } catch (IOException e3) {
                            }
                        }
                        return freq;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inStream != null) {
                            try {
                                inStream.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inStream = inStream2;
                    if (inStream != null) {
                    }
                    throw th;
                }
            }
            if (inStream2 != null) {
                try {
                    inStream2.close();
                } catch (IOException e5) {
                }
            }
            inStream = inStream2;
            return freq;
        } catch (Exception e6) {
            e = e6;
            Log.w(TAG, "readCpu exception=" + e);
            if (inStream != null) {
            }
            return freq;
        }
    }

    private long cpuSumInfo() {
        long sum_freq = 0;
        for (int i = 0; i < 8; i++) {
            long begintime = SystemClock.uptimeMillis();
            sum_freq += readCpu(new String("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq"));
            long endtime = SystemClock.uptimeMillis();
        }
        return sum_freq;
    }

    protected boolean inRestrictAppList(String packageName) {
        boolean result = false;
        synchronized (mOppoAbnormalAppManager) {
            if (this.mScreenOffRestrictAppList.contains(packageName)) {
                result = true;
            }
        }
        return result;
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.DynamicDebug = on;
        this.DEBUG_SWITCH = DEBUG_DETAIL | this.DynamicDebug;
        OppoAamUtils.getInstance().setDynamicDebugSwitch();
    }

    public void openLog(boolean on) {
        Log.i(TAG, "#####openlog####");
        Log.i(TAG, "DynamicDebug = " + getInstance().DynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Log.i(TAG, "DynamicDebug = " + getInstance().DynamicDebug);
    }

    public void registerLogModule() {
        try {
            Log.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Log.i(TAG, "invoke " + cls);
            Class[] clsArr = new Class[1];
            clsArr[0] = String.class;
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", clsArr);
            Log.i(TAG, "invoke " + m);
            Object newInstance = cls.newInstance();
            Object[] objArr = new Object[1];
            objArr[0] = OppoAbnormalAppManager.class.getName();
            m.invoke(newInstance, objArr);
            Log.i(TAG, "invoke end!");
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

    public boolean isScreenOffRestrictApp(String packageName) {
        boolean result = false;
        if (OppoSysStateManager.getInstance().restrictStartupBg()) {
            synchronized (mOppoAbnormalAppManager) {
                if (this.mScreenOffRestrictAppList.contains(packageName)) {
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean isScreenOffRestrict() {
        return OppoSysStateManager.getInstance().restrictStartupBg();
    }

    public boolean isNotRestrictApp(String packageName) {
        boolean result = false;
        synchronized (mOppoAbnormalAppManager) {
            if (this.mNotRestrictAppList.contains(packageName)) {
                result = true;
            }
        }
        return result;
    }

    public boolean validStartProvider(ContentProviderRecord cpr) {
        boolean result = false;
        if (cpr == null) {
            return false;
        }
        ComponentName cpn = cpr.name;
        if (cpn != null) {
            String pkgName = cpn.getPackageName();
            if (OppoAppStartupManagerUtils.getInstance().isInAamProviderWhitePkgList(pkgName)) {
                if (this.DynamicDebug) {
                    Log.i(TAG, "provider is ok " + pkgName);
                }
                return false;
            }
            result = validNewProc(pkgName, TYPE_PROVIDER);
        }
        return result;
    }

    public boolean validStartActivity(ActivityInfo aInfo) {
        boolean result = false;
        if (aInfo == null) {
            return false;
        }
        if (aInfo.applicationInfo != null) {
            if (aInfo.applicationInfo.uid < 10000) {
                return false;
            }
            if (OppoAppStartupManagerUtils.getInstance().isInAamActivityWhitePkgList(aInfo.packageName)) {
                if (this.DynamicDebug) {
                    Log.i(TAG, "activity is ok " + aInfo.packageName);
                }
                return false;
            } else if (validNewProc(aInfo.packageName, TYPE_ACTIVITY) && !isInLruProcessesLocked(aInfo.applicationInfo.uid)) {
                result = true;
            }
        }
        return result;
    }

    public boolean validRestartProcess(ProcessRecord app) {
        int i = 0;
        boolean result = false;
        if (app == null || app.uid < 10000) {
            return false;
        }
        String[] pkgList = app.getPackageList();
        if (pkgList == null || pkgList.length <= 0) {
            return false;
        }
        try {
            int length = pkgList.length;
            while (i < length) {
                String pkgName = pkgList[i];
                if (OppoAppStartupManagerUtils.getInstance().isInAamActivityWhitePkgList(pkgName)) {
                    if (this.DynamicDebug) {
                        Log.i(TAG, "restart is ok " + pkgName);
                    }
                } else if (validNewProc(pkgName, TYPE_RESTART)) {
                    if (this.DynamicDebug) {
                        Log.i(TAG, "validRestartProcess false " + pkgName);
                    }
                    result = true;
                } else {
                    i++;
                }
                return result;
            }
            return result;
        } catch (Exception e) {
            Log.w(TAG, "validRestartProcess has exception! ", e);
            return false;
        }
    }

    public boolean validStartService(String pkgName) {
        return validNewProc(pkgName, "broadcast");
    }

    public boolean validStartBroadcast(String pkgName) {
        return validNewProc(pkgName, "service");
    }

    protected boolean isInLruProcessesLocked(int uid) {
        try {
            for (int i = this.mAms.mLruProcesses.size() - 1; i >= 0; i--) {
                ProcessRecord rec = (ProcessRecord) this.mAms.mLruProcesses.get(i);
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
