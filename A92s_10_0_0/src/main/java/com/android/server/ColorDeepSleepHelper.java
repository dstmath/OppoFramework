package com.android.server;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.AlarmManagerService;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.job.controllers.IColorJobStatusInner;
import com.android.server.job.controllers.JobStatus;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;

public class ColorDeepSleepHelper implements IColorDeepSleepHelper {
    private static final int CONSTRAINTS_OF_NET_INTEREST = 7;
    private static final long DEADLINE_WAITING_NET_CONNECT = 30000;
    public static final long DEEP_SLEEP_BROADCAST_INTERVAL = 40000;
    private static final int DEEP_SLEEP_RETRY_CNT = 3;
    private static final long NETWORK_ALARM_TIMEOUT = 10000;
    public static final int NETWORK_TYPE_MOBILE = 2;
    public static final int NETWORK_TYPE_NONE = 0;
    public static final int NETWORK_TYPE_WIFI = 1;
    private static final String STR_DEEPSLEEP_NET_TYPE = "oppoguardelf_deepsleep_network_type";
    private static final String STR_DEEPSLEEP_STATUS = "oppoguaedelf_deep_sleep_status";
    private static final String TAG = "ColorDeepSleepHelper";
    private static ColorDeepSleepHelper mInstance = null;
    private int CONSTRAINTS_OF_INTEREST = 0;
    private int SOFT_OVERRIDE_CONSTRAINTS = 0;
    boolean deepSleepAlarmMatched = false;
    /* access modifiers changed from: private */
    public Runnable deepSleepTask = new Runnable() {
        /* class com.android.server.ColorDeepSleepHelper.AnonymousClass3 */

        public void run() {
            synchronized (ColorDeepSleepHelper.this.mLock) {
                ColorDeepSleepHelper.this.resumeDelayedForNetworkAlarmsLocked();
                ColorDeepSleepHelper.this.releaseWakelockForDeepSleep();
            }
        }
    };
    private boolean deviceIdleMode;
    /* access modifiers changed from: private */
    public AlarmManagerService mAlarmMS;
    /* access modifiers changed from: private */
    public OppoBaseAlarmManagerService mBaseAlarmMS;
    private ConnectivityManager mConnManager = null;
    private Context mContext = null;
    private int mCountFail = 0;
    /* access modifiers changed from: private */
    public volatile int mDeepSleepStatus = 0;
    private PowerManager.WakeLock mDeepSleepWakeLock = null;
    private ArrayList<AlarmManagerService.Alarm> mDelayedForDeepSleepAlarms = new ArrayList<>();
    private volatile DeepSleepSettingsInfo mInfo = null;
    private boolean mIsReady = false;
    private IColorJobStatusInner mJSInner = null;
    private JobStatus mJobStatus = null;
    private long mLastBroadcastTime = 0;
    /* access modifiers changed from: private */
    public Object mLock = null;
    /* access modifiers changed from: private */
    public volatile int mNetWorkTypeBak = 0;
    private final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        /* class com.android.server.ColorDeepSleepHelper.AnonymousClass4 */

        public void onAvailable(Network network) {
            if (ColorDeepSleepHelper.this.isInDeepSleep()) {
                if (ColorDeepSleepHelper.this.mOppoDebug) {
                    Slog.d(ColorDeepSleepHelper.TAG, "Network available. in deepsleep.");
                }
                ColorDeepSleepHelper.this.mBaseAlarmMS.rescheduleDeepSleepTask(ColorDeepSleepHelper.this.deepSleepTask, 0);
            } else if (ColorDeepSleepHelper.this.mOppoDebug) {
                Slog.d(ColorDeepSleepHelper.TAG, "Network available. not in deepsleep.");
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mOppoDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private long mTimeRestoreNet = 0;
    private int requiredConstraints = 0;
    private int satisfiedConstraints = 0;

    public static ColorDeepSleepHelper getInstance() {
        if (mInstance == null) {
            synchronized (ColorDeepSleepHelper.class) {
                if (mInstance == null) {
                    mInstance = new ColorDeepSleepHelper();
                }
            }
        }
        return mInstance;
    }

    private ColorDeepSleepHelper() {
    }

    public void init(final Context ctx, Handler handler, AlarmManagerService ams, Object lock) {
        synchronized (this) {
            if (!this.mIsReady) {
                this.mIsReady = true;
                this.mContext = ctx;
                this.mAlarmMS = ams;
                this.mBaseAlarmMS = typeCasting(ams);
                this.mLock = lock;
                deepSleepInit();
                ctx.getContentResolver().registerContentObserver(Settings.System.getUriFor(STR_DEEPSLEEP_NET_TYPE), false, new ContentObserver(handler) {
                    /* class com.android.server.ColorDeepSleepHelper.AnonymousClass1 */

                    public void onChange(boolean selfChange, Uri uri) {
                        int history = ColorDeepSleepHelper.this.getNetworkHistory(ctx);
                        if (ColorDeepSleepHelper.this.mOppoDebug) {
                            Slog.d(ColorDeepSleepHelper.TAG, "onChang: netype=" + history);
                        }
                        synchronized (ColorDeepSleepHelper.this.mLock) {
                            ColorDeepSleepHelper.this.parseDeepSleepStatusLocked(history);
                        }
                        if (history != 0 && ColorDeepSleepHelper.this.mAlarmMS != null) {
                            ColorDeepSleepHelper.this.schedulePendWhileIdleDeepSleepAlarms();
                        }
                    }
                }, -1);
                ctx.getContentResolver().registerContentObserver(Settings.System.getUriFor(STR_DEEPSLEEP_STATUS), false, new ContentObserver(handler) {
                    /* class com.android.server.ColorDeepSleepHelper.AnonymousClass2 */

                    public void onChange(boolean selfChange, Uri uri) {
                        int unused = ColorDeepSleepHelper.this.mDeepSleepStatus = Settings.System.getInt(ctx.getContentResolver(), ColorDeepSleepHelper.STR_DEEPSLEEP_STATUS, 0);
                        if (1 == ColorDeepSleepHelper.this.mDeepSleepStatus) {
                            int unused2 = ColorDeepSleepHelper.this.mNetWorkTypeBak = Settings.System.getInt(ctx.getContentResolver(), ColorDeepSleepHelper.STR_DEEPSLEEP_NET_TYPE, 0);
                        } else if (ColorDeepSleepHelper.this.mAlarmMS != null) {
                            ColorDeepSleepHelper.this.scheduleRestoreDeepSleepPendingWhileIdleAlarms();
                        }
                        if (ColorDeepSleepHelper.this.mOppoDebug) {
                            Slog.d(ColorDeepSleepHelper.TAG, "onChang: status=" + ColorDeepSleepHelper.this.mDeepSleepStatus + ", NetWorkType=" + ColorDeepSleepHelper.this.mNetWorkTypeBak);
                        }
                    }
                }, -1);
            }
        }
    }

    public void init(JobStatus Jobs, IColorJobStatusInner inner) {
        this.mJobStatus = Jobs;
        this.mJSInner = inner;
        this.CONSTRAINTS_OF_INTEREST = this.mJSInner.getIntConstraintsOfInterestVal();
        this.SOFT_OVERRIDE_CONSTRAINTS = this.mJSInner.getIntSoftOverrideConstraintsVal();
    }

    public boolean filterDeepSleepAlarm(String pkg, String tag) {
        int net = 0;
        if (this.mInfo != null) {
            net = this.mInfo.getNetType();
        }
        return net != 0 && OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).isMatchDeepSleepRule(pkg, tag, net);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0036, code lost:
        if (r6.mOppoDebug == false) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0038, code lost:
        android.util.Slog.d(com.android.server.ColorDeepSleepHelper.TAG, "send deep sleep broadcast");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x003f, code lost:
        r2 = new android.content.Intent("oppo.intent.action.DEEP_SLEEP_RESTORE_NETWORK");
        r2.setPackage("com.coloros.oppoguardelf");
        r2.setFlags(268435456);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0050, code lost:
        if (r7 == null) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0052, code lost:
        r2.putExtra("pkg", r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0057, code lost:
        r3 = r6.mContext;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0059, code lost:
        if (r3 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005b, code lost:
        r3.sendBroadcastAsUser(r2, android.os.UserHandle.ALL, "oppo.permission.OPPO_COMPONENT_SAFE");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        return true;
     */
    public boolean sendDeepSleepBroadcast(String pkg) {
        long now = SystemClock.elapsedRealtime();
        synchronized (this) {
            if (now - this.mLastBroadcastTime < DEEP_SLEEP_BROADCAST_INTERVAL) {
                if (this.mOppoDebug) {
                    Slog.d(TAG, "send deep sleep broadcast failed, broadcast interval is " + (now - this.mLastBroadcastTime));
                }
                return false;
            }
            this.mLastBroadcastTime = now;
        }
    }

    /* access modifiers changed from: private */
    public int getNetworkHistory(Context ctx) {
        try {
            return Settings.System.getInt(ctx.getContentResolver(), STR_DEEPSLEEP_NET_TYPE, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean isInDeepSleep() {
        return this.mDeepSleepStatus == 1;
    }

    private int getDeepSleepNetTypeBak() {
        return this.mNetWorkTypeBak;
    }

    /* access modifiers changed from: private */
    public void parseDeepSleepStatusLocked(int history) {
        if (history != 0) {
            boolean z = true;
            if (this.mInfo == null) {
                if (history != 2) {
                    z = false;
                }
                this.mInfo = new DeepSleepSettingsInfo(history, z, false);
                return;
            }
            this.mInfo.setNetType(history);
            DeepSleepSettingsInfo deepSleepSettingsInfo = this.mInfo;
            if (history != 2) {
                z = false;
            }
            deepSleepSettingsInfo.setNetMetered(z);
            this.mInfo.setNetNotRoaming(false);
            return;
        }
        this.mInfo = null;
    }

    public class DeepSleepSettingsInfo {
        private boolean netMetered = false;
        private boolean netNotRoaming = false;
        private int netType = 0;

        public DeepSleepSettingsInfo(int type, boolean metered, boolean roaming) {
            this.netType = type;
            this.netMetered = metered;
            this.netNotRoaming = !roaming;
        }

        public int getNetType() {
            return this.netType;
        }

        public boolean isNetNotRoaming() {
            return this.netNotRoaming;
        }

        public void setNetNotRoaming(boolean netNotRoaming2) {
            this.netNotRoaming = netNotRoaming2;
        }

        public boolean isNetMetered() {
            return this.netMetered;
        }

        public void setNetMetered(boolean netMetered2) {
            this.netMetered = netMetered2;
        }

        public void setNetType(int netType2) {
            this.netType = netType2;
        }

        public String toString() {
            return "DeepSleepSettingsInfo(type : " + this.netType + ", metered : " + this.netMetered + ", notRoaming : " + this.netNotRoaming + ")";
        }
    }

    public boolean ruleMatchDeepSleepAlarm(AlarmManagerService.Alarm alarm) {
        String tag;
        this.deepSleepAlarmMatched = false;
        if (alarm.operation != null) {
            tag = alarm.operation.getTag("");
        } else {
            tag = alarm.listenerTag != null ? alarm.listenerTag : null;
        }
        boolean ruleMatched = filterDeepSleepAlarm(alarm.packageName, tag);
        if (this.mOppoDebug && ruleMatched) {
            Slog.d(TAG, "delay alarm for deep sleep, pkg : " + alarm.packageName + ", tag : " + tag + ", matched : " + ruleMatched);
        }
        if (ruleMatched) {
            this.mDelayedForDeepSleepAlarms.add(alarm);
            this.deepSleepAlarmMatched = true;
        }
        return ruleMatched;
    }

    public void handleMatchDeepSleepAlarm() {
        if (this.deepSleepAlarmMatched && this.mDelayedForDeepSleepAlarms.size() > 0) {
            acquireWakelockForDeepSleep();
            sendDeepSleepBroadcast(this.mDelayedForDeepSleepAlarms.get(0).packageName);
            this.mBaseAlarmMS.rescheduleDeepSleepTask(this.deepSleepTask, (long) NETWORK_ALARM_TIMEOUT);
        }
    }

    private void deepSleepInit() {
        this.mDeepSleepWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "*deep_sleep*");
        this.mConnManager = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
        this.mConnManager.registerDefaultNetworkCallback(this.mNetworkCallback);
    }

    /* access modifiers changed from: private */
    public void resumeDelayedForNetworkAlarmsLocked() {
        if (this.mDelayedForDeepSleepAlarms.size() > 0) {
            for (int i = 0; i < this.mDelayedForDeepSleepAlarms.size(); i++) {
                AlarmManagerService.Alarm a = this.mDelayedForDeepSleepAlarms.get(i);
                if (this.mOppoDebug) {
                    Slog.d(TAG, "deliver Network delayed Alarm: " + a.toString());
                }
                this.mAlarmMS.mDeliveryTracker.deliverLocked(a, SystemClock.elapsedRealtime(), (a.flags & 4) != 0);
            }
            this.mDelayedForDeepSleepAlarms.clear();
        }
    }

    private void acquireWakelockForDeepSleep() {
        if (!this.mDeepSleepWakeLock.isHeld()) {
            this.mDeepSleepWakeLock.acquire(NETWORK_ALARM_TIMEOUT);
        }
    }

    /* access modifiers changed from: private */
    public void releaseWakelockForDeepSleep() {
        if (this.mDeepSleepWakeLock.isHeld()) {
            this.mDeepSleepWakeLock.release();
        }
    }

    /* access modifiers changed from: package-private */
    public void schedulePendWhileIdleDeepSleepAlarms() {
        this.mBaseAlarmMS.schedulePendWhileIdleDeepSleepAlarms();
    }

    /* access modifiers changed from: package-private */
    public void scheduleRestoreDeepSleepPendingWhileIdleAlarms() {
        this.mBaseAlarmMS.scheduleRestoreDeepSleepPendingWhileIdleAlarms();
    }

    public void restoreDeepSleepPendingWhileIdleAlarmsLocked() {
        if (this.mAlarmMS.mPendingWhileIdleAlarms.size() > 0) {
            boolean removed = false;
            long nowElapsed = SystemClock.elapsedRealtime();
            for (int i = this.mAlarmMS.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
                AlarmManagerService.Alarm a = (AlarmManagerService.Alarm) this.mAlarmMS.mPendingWhileIdleAlarms.get(i);
                if ((a.flags & 14) != 0) {
                    if (this.mOppoDebug) {
                        Slog.d(TAG, "restore deep sleep " + a.toString());
                    }
                    OppoBaseAlarmManagerService oppoBaseAlarmManagerService = this.mBaseAlarmMS;
                    if (oppoBaseAlarmManagerService != null) {
                        oppoBaseAlarmManagerService.setRestoringFlagForAlarmAlignment(true);
                    }
                    this.mAlarmMS.reAddAlarmLocked(a, nowElapsed, false);
                    OppoBaseAlarmManagerService oppoBaseAlarmManagerService2 = this.mBaseAlarmMS;
                    if (oppoBaseAlarmManagerService2 != null) {
                        oppoBaseAlarmManagerService2.setRestoringFlagForAlarmAlignment(false);
                    }
                    this.mAlarmMS.mPendingWhileIdleAlarms.remove(i);
                    removed = true;
                }
            }
            if (removed) {
                this.mAlarmMS.rescheduleKernelAlarmsLocked();
                getInner().updateNextAlarmClockLocked();
                if (this.mAlarmMS.mPendingWhileIdleAlarms.size() <= 0) {
                    try {
                        this.mAlarmMS.mHandler.post(new Runnable() {
                            /* class com.android.server.$$Lambda$ColorDeepSleepHelper$WATP0IwkyqB5BOIDOI0kgUxwK0 */

                            public final void run() {
                                ColorDeepSleepHelper.this.lambda$restoreDeepSleepPendingWhileIdleAlarmsLocked$0$ColorDeepSleepHelper();
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public /* synthetic */ void lambda$restoreDeepSleepPendingWhileIdleAlarmsLocked$0$ColorDeepSleepHelper() {
        this.mContext.sendBroadcastAsUser(this.mAlarmMS.mTimeTickIntent, UserHandle.ALL);
    }

    public void setDeviceIdleMode(boolean isDeviceIdleMode) {
        this.deviceIdleMode = isDeviceIdleMode;
    }

    public boolean handleMatchDeepSleepRuleJob(boolean deadlineSatisfied, boolean notDozing) {
        IColorJobStatusInner iColorJobStatusInner = this.mJSInner;
        if (iColorJobStatusInner == null || this.mJobStatus == null) {
            return true;
        }
        this.requiredConstraints = iColorJobStatusInner.getIntRequiredConstraintsVal();
        this.satisfiedConstraints = this.mJSInner.getIntSatisfiedConstraintsVal();
        if (!notDozing) {
            this.mTimeRestoreNet = 0;
            this.mCountFail = 0;
            return false;
        }
        boolean sendBroadcast = false;
        long nowElapsed = SystemClock.elapsedRealtime();
        if (isInDeepSleep() && this.mJSInner.getJobInfo().getService() != null && OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).isMatchDeepSleepRule(this.mJSInner.getJobInfo().getService()) && isJobMathDeepSleepNetType()) {
            if (deadlineSatisfied) {
                if (!isNetConstraintsSatisfied()) {
                    if (!this.deviceIdleMode) {
                        long RestoreInterval = nowElapsed - this.mTimeRestoreNet;
                        if (this.mOppoDebug) {
                            Slog.d(TAG, "isReady: deadlineSatisfied, netNotsatisfied. job=" + this.mJobStatus.toShortString() + ", Interval =" + RestoreInterval + ", timeRestoreNet=" + this.mTimeRestoreNet + ", countFail=" + this.mCountFail);
                        }
                        if (this.mCountFail <= 3) {
                            sendBroadcast = sendDeepSleepBroadcast(this.mJSInner.getJobInfo().getService().getPackageName());
                        }
                        if (this.mTimeRestoreNet == 0) {
                            this.mTimeRestoreNet = nowElapsed;
                            return false;
                        } else if (RestoreInterval < 30000) {
                            return false;
                        } else {
                            if (this.mOppoDebug) {
                                Slog.d(TAG, "isReady: deadlineSatisfied, netNotsatisfied. interval long enough. job=" + this.mJobStatus.toShortString());
                            }
                        }
                    } else {
                        if (this.mOppoDebug) {
                            Slog.d(TAG, "isReady: 222 deadlineSatisfied. netNotsatisfied. job=" + this.mJobStatus.toShortString() + ", mCountFail=" + this.mCountFail);
                        }
                        return false;
                    }
                }
            } else if (!this.deviceIdleMode) {
                if (!this.mJobStatus.isConstraintsSatisfied() && isConstraintsSatisfiedWithoutNet()) {
                    if (this.mOppoDebug) {
                        Slog.d(TAG, "isReady: deadlineNotsatisfied. netNotsatisfied. job=" + this.mJobStatus.toShortString() + ", mCountFail=" + this.mCountFail);
                    }
                    if (this.mCountFail <= 3) {
                        sendBroadcast = sendDeepSleepBroadcast(this.mJSInner.getJobInfo().getService().getPackageName());
                    }
                    if (this.mTimeRestoreNet == 0) {
                        this.mTimeRestoreNet = nowElapsed;
                    }
                }
            }
        }
        if (!deadlineSatisfied || this.mJSInner.getJobInfo().getService() == null || !OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).isMatchDeepSleepRule(this.mJSInner.getJobInfo().getService()) || isNetConstraintsSatisfied() || nowElapsed - this.mTimeRestoreNet >= 30000) {
            boolean result = this.mJobStatus.isConstraintsSatisfied() || deadlineSatisfied;
            if (!result && sendBroadcast) {
                this.mCountFail++;
                return true;
            } else if (!result) {
                return true;
            } else {
                this.mCountFail = 0;
                this.mTimeRestoreNet = 0;
                return true;
            }
        } else {
            if (this.mOppoDebug) {
                Slog.d(TAG, "isReady: 333 deadlineSatisfied. netNotsatisfied. job=" + this.mJobStatus.toShortString() + ", mCountFail=" + this.mCountFail);
            }
            return false;
        }
    }

    private boolean isConstraintsSatisfiedWithoutNet() {
        if (this.mJobStatus.overrideState == 2) {
            return true;
        }
        int i = this.requiredConstraints;
        int i2 = this.CONSTRAINTS_OF_INTEREST;
        int req = i & i2 & -8;
        int sat = i2 & -8 & this.satisfiedConstraints;
        if (this.mJobStatus.overrideState == 1) {
            sat |= this.requiredConstraints & this.SOFT_OVERRIDE_CONSTRAINTS;
        }
        if ((sat & req) == req) {
            return true;
        }
        return false;
    }

    private boolean isNetConstraintsSatisfied() {
        if (this.mJobStatus.overrideState == 2) {
            return true;
        }
        int req = this.requiredConstraints & 7;
        int sat = this.satisfiedConstraints & 7;
        if (this.mJobStatus.overrideState == 1) {
            sat |= this.requiredConstraints & this.SOFT_OVERRIDE_CONSTRAINTS;
        }
        if ((sat & req) == req) {
            return true;
        }
        return false;
    }

    private boolean isJobMathDeepSleepNetType() {
        return false;
    }

    private static OppoBaseAlarmManagerService typeCasting(AlarmManagerService ams) {
        if (ams != null) {
            return (OppoBaseAlarmManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseAlarmManagerService.class, ams);
        }
        return null;
    }

    private IColorAlarmManagerServiceInner getInner() {
        OppoBaseAlarmManagerService baseAlarm = typeCasting(this.mAlarmMS);
        if (baseAlarm == null || baseAlarm.mColorAlarmMSInner == null) {
            return IColorAlarmManagerServiceInner.DEFAULT;
        }
        return baseAlarm.mColorAlarmMSInner;
    }
}
