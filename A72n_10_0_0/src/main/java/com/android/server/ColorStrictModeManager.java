package com.android.server;

import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.server.AlarmManagerService;
import com.android.server.OppoBaseAlarmManagerService;
import com.android.server.am.ColorCommonListManager;
import com.android.server.am.IColorCommonListManager;
import com.android.server.am.IColorGameSpaceManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.content.SyncOperation;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.job.IColorJobSchedulerServiceInner;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.wm.ColorAppSwitchManager;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;

public class ColorStrictModeManager implements IColorStrictModeManager {
    private static final boolean DEBUG_PANIC = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG_PANIC);
    private static final int STATUS_GAME = 1;
    private static final int STATUS_NORMAL = 0;
    private static final String SYS_BPM_CONFIG_FILE = "/data/oppo/coloros/bpm/sys_elsa_config_list.xml";
    private static final String SYS_BPM_DIR = "/data/oppo/coloros/bpm";
    private static final String TAG = "ColorStrictModeManager";
    private static ColorStrictModeManager mInstance = null;
    private IColorAlarmManagerServiceInner mAlarmInner = null;
    private AlarmManagerService mAms = null;
    private IColorAlarmManagerServiceEx mAmsEx = null;
    private FileObserverPolicy mConfigFileObserver;
    private Context mContext = null;
    private ArrayList<String> mCustomizeList = new ArrayList<>();
    ArrayList<AlarmManagerService.Alarm> mDelayedForPurBackgroundAlarms = new ArrayList<>();
    private ArrayList<String> mGlobalWhiteList = new ArrayList<>();
    private WorkerHandler mHandler = null;
    private Handler mJobHandler = null;
    private IColorJobSchedulerServiceInner mJobInner = null;
    private JobSchedulerService mJss = null;
    private String mLastResumeApp;
    private Object mLock = null;
    private Looper mLooper = null;
    private int mModeStatus = 0;
    private boolean mPowerStatus = DEBUG_PANIC;
    private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        /* class com.android.server.ColorStrictModeManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    ColorStrictModeManager.this.mHandler.sendEmptyMessage(WorkerHandler.MSG_SCREEN_ON);
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    ColorStrictModeManager.this.mHandler.sendEmptyMessage(WorkerHandler.MSG_SCREEN_OFF);
                } else if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
                    ColorStrictModeManager.this.updatePowerStatus(true);
                } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                    ColorStrictModeManager.this.updatePowerStatus(ColorStrictModeManager.DEBUG_PANIC);
                }
            }
        }
    };
    private long mStrictEnterTime = 60000;
    private boolean mStrictMode = DEBUG_PANIC;
    private boolean mStrictSwitch = true;
    private ArrayList<String> mStrictWhiteList = new ArrayList<>();
    private ArrayList<String> mSysAppBlackList = new ArrayList<>();

    private ColorStrictModeManager() {
    }

    public static ColorStrictModeManager getInstance() {
        if (mInstance == null) {
            synchronized (ColorStrictModeManager.class) {
                if (mInstance == null) {
                    mInstance = new ColorStrictModeManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context, Object lock, AlarmManagerService ams, Looper loop, IColorAlarmManagerServiceInner inner) {
        this.mContext = context;
        this.mLock = lock;
        this.mAlarmInner = inner;
        this.mAms = ams;
        this.mLooper = loop;
        this.mHandler = new WorkerHandler(loop);
        this.mHandler.sendEmptyMessageDelayed(1001, ColorAppSwitchManager.INTERVAL);
    }

    public void init(Handler handler, JobSchedulerService jss, IColorJobSchedulerServiceInner inner) {
        this.mJobHandler = handler;
        this.mJss = jss;
        this.mJobInner = inner;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initData() {
        initFileData();
        initPowerStatus();
        updateCustomizeList();
        updateGlobalWhiteList();
        registerStateReceiver();
        registerFileObserver();
    }

    private void initFileData() {
        try {
            File hansDir = new File(SYS_BPM_DIR);
            if (!hansDir.exists()) {
                hansDir.mkdirs();
            }
            File hansFile = new File(SYS_BPM_CONFIG_FILE);
            if (hansFile.exists()) {
                updateConfigInfo();
            } else {
                hansFile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConfigInfo() {
        int type;
        File file = new File(SYS_BPM_CONFIG_FILE);
        FileInputStream stream = null;
        clearOldData();
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2) {
                    String tagName = parser.getName();
                    if ("strictConfig".equals(tagName)) {
                        setStrictConfig(parser.getAttributeValue(null, "strictSwitch"), parser.getAttributeValue(null, "strictEnterTime"));
                    } else if ("strictWhite".equals(tagName)) {
                        addStrictWhiteList(parser.getAttributeValue(null, "pkg"));
                    } else if ("strictSysAppBlack".equals(tagName)) {
                        addSysAppBlackList(parser.getAttributeValue(null, "pkg"));
                    }
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (0 != 0) {
                stream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
    }

    private void clearOldData() {
        synchronized (this.mStrictWhiteList) {
            this.mStrictWhiteList.clear();
        }
        synchronized (this.mSysAppBlackList) {
            this.mSysAppBlackList.clear();
        }
    }

    private void setStrictConfig(String strictSwitch, String strictEnterTime) {
        if (!TextUtils.isEmpty(strictSwitch) && !TextUtils.isEmpty(strictEnterTime)) {
            try {
                this.mStrictSwitch = Boolean.parseBoolean(strictSwitch);
                this.mStrictEnterTime = Long.parseLong(strictEnterTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addStrictWhiteList(String pkg) {
        if (!TextUtils.isEmpty(pkg)) {
            synchronized (this.mStrictWhiteList) {
                if (!this.mStrictWhiteList.contains(pkg)) {
                    this.mStrictWhiteList.add(pkg);
                }
            }
        }
    }

    private void addSysAppBlackList(String pkg) {
        if (!TextUtils.isEmpty(pkg)) {
            synchronized (this.mSysAppBlackList) {
                if (!this.mSysAppBlackList.contains(pkg)) {
                    this.mSysAppBlackList.add(pkg);
                }
            }
        }
    }

    private void updateCustomizeList() {
        synchronized (this.mCustomizeList) {
            this.mCustomizeList.clear();
            this.mCustomizeList.addAll(OppoListManager.getInstance().getCustomWhiteList());
        }
    }

    private void updateGlobalWhiteList() {
        synchronized (this.mGlobalWhiteList) {
            this.mGlobalWhiteList.clear();
            this.mGlobalWhiteList.addAll(OppoListManager.getInstance().getGlobalWhiteList(this.mContext));
        }
    }

    private void initPowerStatus() {
        Intent batteryStatus = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus != null) {
            boolean z = DEBUG_PANIC;
            if (batteryStatus.getIntExtra("plugged", 0) != 0) {
                z = true;
            }
            this.mPowerStatus = z;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePowerStatus(boolean status) {
        this.mPowerStatus = status;
    }

    private void registerStateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        this.mContext.registerReceiver(this.mStateReceiver, filter, null, this.mHandler);
    }

    private void registerFileObserver() {
        this.mConfigFileObserver = new FileObserverPolicy(SYS_BPM_CONFIG_FILE);
        this.mConfigFileObserver.startWatching();
    }

    /* access modifiers changed from: private */
    public class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && ColorStrictModeManager.SYS_BPM_CONFIG_FILE.equals(this.mFocusPath)) {
                ColorStrictModeManager.this.mHandler.sendEmptyMessage(1002);
            }
        }
    }

    public void handleApplicationSwitch(int prePkgUid, String prePkgName, int nextPkgUid, String nextPkgName) {
        this.mLastResumeApp = nextPkgName;
        if (!this.mStrictSwitch || !OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).inGameSpacePkgList(nextPkgName) || !OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).isBpmEnable()) {
            if (isStrictMode()) {
                stopStrictMode();
            } else {
                this.mHandler.removeMessages(1003);
            }
            this.mModeStatus = 0;
            return;
        }
        if (isStrictMode()) {
            stopStrictMode();
        }
        this.mModeStatus = 1;
        sendReadyStrictModeMsg();
    }

    private void sendReadyStrictModeMsg() {
        if (!isStrictMode()) {
            if (this.mHandler.hasMessages(1003)) {
                this.mHandler.removeMessages(1003);
            }
            if (DEBUG_PANIC) {
                Slog.i(TAG, "sendReadyStrictModeMsg!");
            }
            this.mHandler.sendEmptyMessageDelayed(1003, this.mStrictEnterTime);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEnterStrictMode() {
        if (this.mStrictSwitch) {
            if (DEBUG_PANIC) {
                Slog.i(TAG, "enterStrictMode!");
            }
            setStrictMode(true);
        }
    }

    private void setStrictMode(boolean mode) {
        this.mStrictMode = mode;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOn() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOff() {
        if (isStrictMode()) {
            stopStrictMode();
        } else {
            this.mHandler.removeMessages(1003);
        }
        this.mModeStatus = 0;
    }

    private boolean isNormalStatus() {
        if (this.mModeStatus == 0) {
            return true;
        }
        return DEBUG_PANIC;
    }

    private boolean isEnable() {
        if (!this.mPowerStatus || !isNormalStatus()) {
            return true;
        }
        return DEBUG_PANIC;
    }

    public boolean isDelayAppAlarm(int callerPid, int callerUid, int calledUid, String calledPkg) {
        boolean isDelay = DEBUG_PANIC;
        if ((!isStrictMode() && !isEnable()) || calledUid < 10000) {
            return DEBUG_PANIC;
        }
        if (callerUid != calledUid && !checkWhitePackage(calledPkg, calledUid) && !this.mLastResumeApp.equals(calledPkg)) {
            isDelay = true;
        }
        if (isDelay && DEBUG_PANIC) {
            Slog.i(TAG, "isDelayAppAlarm true, callerPid=" + callerPid + ", callerUid=" + callerUid + ", calledUid=" + calledUid + ", calledPkg=" + calledPkg);
        }
        return isDelay;
    }

    public boolean isDelayAppSync(int uid, String pkg) {
        boolean isDelay = DEBUG_PANIC;
        if (!isStrictMode() || uid < 10000) {
            return DEBUG_PANIC;
        }
        if (!checkWhitePackage(pkg, uid)) {
            isDelay = true;
        }
        if (isDelay && DEBUG_PANIC) {
            Slog.i(TAG, "isDelayAppSync true, uid=" + uid + ", pkg=" + pkg);
        }
        return isDelay;
    }

    public boolean isDelayAppJob(int uid, String pkg) {
        boolean isDelay = DEBUG_PANIC;
        if (!isStrictMode() || uid < 10000) {
            return DEBUG_PANIC;
        }
        if (!checkWhitePackage(pkg, uid) && !this.mLastResumeApp.equals(pkg)) {
            isDelay = true;
        }
        if (isDelay && DEBUG_PANIC) {
            Slog.i(TAG, "isDelayAppJob true, uid=" + uid + ", pkg=" + pkg);
        }
        return isDelay;
    }

    private boolean checkWhitePackage(String pkgName, int uid) {
        if (isInStrictModeWhiteList(pkgName)) {
            if (DEBUG_PANIC) {
                Slog.i(TAG, pkgName + "  is in white list");
            }
            return true;
        } else if (isInCustomizeList(pkgName) || isInAppWidgetList(uid) || isInDisplayDeviceList(uid) || isInGlobalWhiteList(pkgName) || OppoListManager.getInstance().isCtaPackage(pkgName)) {
            if (DEBUG_PANIC) {
                Slog.i(TAG, pkgName + "  is in other white list");
            }
            return true;
        } else if (OppoListManager.getInstance().isFromNotifyPkg(pkgName)) {
            if (DEBUG_PANIC) {
                Slog.i(TAG, pkgName + "  is from notify");
            }
            return true;
        } else if (!OppoListManager.getInstance().isSystemApp(pkgName) || isInSysAppBlackList(pkgName)) {
            return DEBUG_PANIC;
        } else {
            return true;
        }
    }

    private boolean isInStrictModeWhiteList(String pkgName) {
        boolean contains;
        synchronized (this.mStrictWhiteList) {
            contains = this.mStrictWhiteList.contains(pkgName);
        }
        return contains;
    }

    private boolean isInSysAppBlackList(String pkgName) {
        boolean contains;
        synchronized (this.mSysAppBlackList) {
            contains = this.mSysAppBlackList.contains(pkgName);
        }
        return contains;
    }

    private boolean isInCustomizeList(String pkgName) {
        boolean contains;
        synchronized (this.mCustomizeList) {
            contains = this.mCustomizeList.contains(pkgName);
        }
        return contains;
    }

    private boolean isInGlobalWhiteList(String pkgName) {
        boolean contains;
        synchronized (this.mGlobalWhiteList) {
            contains = this.mGlobalWhiteList.contains(pkgName);
        }
        return contains;
    }

    private boolean isInAppWidgetList(int uid) {
        return !OppoFeatureCache.get(IColorCommonListManager.DEFAULT).getAppInfo(ColorCommonListManager.CONFIG_WIDGET, uid).isEmpty();
    }

    private boolean isInDisplayDeviceList(int uid) {
        return !OppoFeatureCache.get(IColorCommonListManager.DEFAULT).getAppInfo(ColorCommonListManager.CONFIG_SCREEN_RECORDER, uid).isEmpty();
    }

    public boolean isStrictMode() {
        return this.mStrictMode;
    }

    public void filterTriggerListForStrictMode(ArrayList<AlarmManagerService.Alarm> triggerList) {
        Boolean isHasSameRepeatAlarm;
        if (isStrictMode()) {
            if (DEBUG_PANIC) {
                Slog.v("ColorProcessManager", "Befor delivering alarm check purbackground state : Now is in StrictMode !");
            }
            if (triggerList.size() > 0) {
                Iterator<AlarmManagerService.Alarm> triggerListIter = triggerList.iterator();
                boolean z = DEBUG_PANIC;
                Boolean.valueOf((boolean) DEBUG_PANIC);
                while (triggerListIter.hasNext()) {
                    AlarmManagerService.Alarm alarm = triggerListIter.next();
                    OppoBaseAlarmManagerService.BaseAlarm baseAlarm = typeCasting(alarm);
                    if (baseAlarm != null) {
                        if (isDelayAppAlarm(baseAlarm.callingPid, baseAlarm.callingUid, alarm.creatorUid, baseAlarm.operation != null ? baseAlarm.operation.getTargetPackage() : alarm.packageName)) {
                            Slog.v("ColorProcessManager", "The alarm delayed because of purbackground is " + alarm);
                            if (alarm.repeatInterval <= 0 || baseAlarm.listener != null || baseAlarm.operation == null || this.mDelayedForPurBackgroundAlarms.size() <= 0) {
                                this.mDelayedForPurBackgroundAlarms.add(alarm);
                            } else {
                                Boolean isHasSameRepeatAlarm2 = Boolean.valueOf(z);
                                int i = 0;
                                while (true) {
                                    if (i >= this.mDelayedForPurBackgroundAlarms.size()) {
                                        break;
                                    }
                                    AlarmManagerService.Alarm delayedAlarm = this.mDelayedForPurBackgroundAlarms.get(i);
                                    OppoBaseAlarmManagerService.BaseAlarm baseAlarmDelay = typeCasting(delayedAlarm);
                                    if (baseAlarmDelay != null && delayedAlarm.repeatInterval > 0) {
                                        isHasSameRepeatAlarm = isHasSameRepeatAlarm2;
                                        if (delayedAlarm.repeatInterval == alarm.repeatInterval && baseAlarmDelay.operation != null && baseAlarmDelay.operation.equals(baseAlarm.operation) && baseAlarmDelay.type == baseAlarm.type && delayedAlarm.packageName.equals(alarm.packageName)) {
                                            Slog.v("ColorProcessManager", "The alarm is a repeat alarm and it is already in DelayedForPurBackgroundAlarms list !");
                                            isHasSameRepeatAlarm2 = true;
                                            break;
                                        }
                                    } else {
                                        isHasSameRepeatAlarm = isHasSameRepeatAlarm2;
                                    }
                                    i++;
                                    isHasSameRepeatAlarm2 = isHasSameRepeatAlarm;
                                }
                                if (!isHasSameRepeatAlarm2.booleanValue()) {
                                    this.mDelayedForPurBackgroundAlarms.add(alarm);
                                } else if (this.mAlarmInner != null) {
                                    Slog.v("ColorProcessManager", "decrement alarm count for " + alarm.uid);
                                    this.mAlarmInner.decrementAlarmCount(alarm.uid, 1);
                                }
                            }
                            triggerListIter.remove();
                        }
                    }
                    z = DEBUG_PANIC;
                }
            } else if (DEBUG_PANIC) {
                Slog.v("ColorProcessManager", "Befor delivering alarm check purbackground state: Now is in StrictMode,But the triggerList is Null !");
            }
        }
    }

    public void stopStrictMode() {
        if (DEBUG_PANIC) {
            Slog.i(TAG, "stopStrictMode!");
        }
        setStrictMode(DEBUG_PANIC);
        this.mHandler.removeMessages(WorkerHandler.STOP_STRICTMODE);
        this.mHandler.sendEmptyMessage(WorkerHandler.STOP_STRICTMODE);
        stopStrictModeOnJob();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopStrictModeLocked() {
        if (this.mDelayedForPurBackgroundAlarms.size() > 0) {
            if (DEBUG_PANIC) {
                Slog.v("ColorProcessManager", "StopStrictMode ! And the mDelayedForPurBackgroundAlarms is not null,so deliver these Alarms !");
            }
            this.mAms.deliverAlarmsLocked(this.mDelayedForPurBackgroundAlarms, SystemClock.elapsedRealtime());
            this.mDelayedForPurBackgroundAlarms.clear();
        } else if (DEBUG_PANIC) {
            Slog.v("ColorProcessManager", "StopStrictMode ! And the mDelayedForPurBackgroundAlarms is null !");
        }
    }

    /* access modifiers changed from: private */
    public class WorkerHandler extends Handler {
        public static final int MSG_INIT_DATA = 1001;
        public static final int MSG_READY_ENTER_STRICT_MODE = 1003;
        public static final int MSG_SCREEN_OFF = 1005;
        public static final int MSG_SCREEN_ON = 1004;
        public static final int MSG_UPDATE_CONFIG = 1002;
        public static final int STOP_STRICTMODE = 1006;

        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    ColorStrictModeManager.this.initData();
                    return;
                case 1002:
                    ColorStrictModeManager.this.updateConfigInfo();
                    return;
                case 1003:
                    ColorStrictModeManager.this.handleEnterStrictMode();
                    return;
                case MSG_SCREEN_ON /* 1004 */:
                    ColorStrictModeManager.this.handleScreenOn();
                    return;
                case MSG_SCREEN_OFF /* 1005 */:
                    ColorStrictModeManager.this.handleScreenOff();
                    return;
                case STOP_STRICTMODE /* 1006 */:
                    synchronized (ColorStrictModeManager.this.mLock) {
                        ColorStrictModeManager.this.stopStrictModeLocked();
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public boolean isJobSatisfiedInStrictMode(boolean componentPresent, JobStatus job) {
        boolean satisfied = componentPresent;
        String pkgName = null;
        if (!(job.getJob() == null || job.getJob().getService() == null)) {
            pkgName = job.getJob().getService().getPackageName();
        }
        if (pkgName == null) {
            return satisfied;
        }
        if (satisfied && isStrictMode() && isDelayAppJob(job.getUid(), pkgName)) {
            satisfied = DEBUG_PANIC;
            if (DEBUG_PANIC) {
                Slog.d(TAG, "in strict mode. job: " + job);
            }
        }
        return satisfied;
    }

    public void stopStrictModeOnJob() {
        this.mJobHandler.obtainMessage(this.mJobInner.getMsgJobExpiredValue()).sendToTarget();
    }

    public boolean isDelayAppSync(SyncOperation op, ComponentName targetComponent, int targetUid) {
        if (op == null || op.reason != -4) {
            return true;
        }
        String pkg = "";
        if (targetComponent != null) {
            pkg = targetComponent.getPackageName();
        }
        if (!isDelayAppSync(targetUid, pkg)) {
            return true;
        }
        Slog.i(TAG, "dispatchSyncOperation: pkg{" + pkg + "} for bpm, skip sync");
        return DEBUG_PANIC;
    }

    private static OppoBaseAlarmManagerService.BaseAlarm typeCasting(AlarmManagerService.Alarm alarm) {
        if (alarm != null) {
            return (OppoBaseAlarmManagerService.BaseAlarm) ColorTypeCastingHelper.typeCasting(OppoBaseAlarmManagerService.BaseAlarm.class, alarm);
        }
        return null;
    }
}
