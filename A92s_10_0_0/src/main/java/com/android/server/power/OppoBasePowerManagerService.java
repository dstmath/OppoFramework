package com.android.server.power;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.ArrayMap;
import android.util.LongArrayQueue;
import android.util.Pair;
import android.util.Slog;
import com.android.server.ColorServiceFactory;
import com.android.server.IColorAlarmManagerHelper;
import com.android.server.LocalServices;
import com.android.server.PswServiceFactory;
import com.android.server.SystemService;
import com.android.server.job.ColorJobSchedulerInternal;
import com.android.server.job.JobSchedulerInternal;
import com.android.server.power.PowerManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public abstract class OppoBasePowerManagerService extends SystemService {
    public static final String AodUserSetEnable = "Setting_AodEnable";
    public static final String FINGERPRINT_UNLOCK = "show_fingerprint_when_screen_off";
    public static final String FINGERPRINT_UNLOCK_SWITCH = "coloros_fingerprint_unlock_switch";
    private static final int HANS_NO_PARTIAL_WAKE_LOCK = 3;
    private static final int HANS_SET_PARTIAL_WAKELOCK_SUCCESS = 1;
    private static final int HANS_SET_WAKELOCK_FAIL = 0;
    private static final int HANS_WORKSOURCE_WAKELOCK = 2;
    private static final String JOB_WAKELOCK_PREFIX = "*job*/";
    protected static final int MSG_SCREENOFF_TIMEOUT_KEYGUARD_LOCKED = 101;
    public static final int MSG_STOP_DEAM = 102;
    public static final int PENDING_FAIL = 1;
    public static final int PENDING_PROCESSING = 2;
    private static final int STATUS_HAS_OTHER_SYSTEM_WAKELOCK = 2;
    private static final int STATUS_HAS_SYSTEM_WAKELOCK = 1;
    private static final String TAG = "OppoBasePowerManagerService";
    public static boolean mOppoAodSupport = false;
    public final int RESTORE_IGNORE = 1;
    public final int RESTORE_SUCCESS = 2;
    public int mAodUserSetEnable = 0;
    IColorPowerManagerServiceEx mColorPowerMSEx = null;
    IColorPowerManagerServiceInner mColorPowerMSInner = null;
    public HashMap<Integer, Integer> mDozeStateMap = new HashMap<>();
    public boolean mFingerprintOpticalSupport = false;
    public int mFingerprintUnlock = 0;
    public int mFingerprintUnlockswitch = 0;
    public IBinder mFlinger;
    JobRestoreHistory mJobRestoreHistory = new JobRestoreHistory();
    JobSchedulerInternal mLocalJobScheduler = null;
    /* access modifiers changed from: private */
    public Object mLock;
    public final Object mMapLock = new Object();
    OppoLocalService mOppoLocalService = null;
    protected OppoPowerFuncHelper mOppoPowerFuncHelper = null;
    private PowerMonitor mPowerMonitor;
    IPswPowerManagerServiceEx mPswPowerMs = null;
    public int mScreenState = 0;
    /* access modifiers changed from: private */
    public ArrayList<PowerManagerService.WakeLock> mWakeLocks;

    /* access modifiers changed from: protected */
    public abstract void onTmpGotoSleepWhenScreenOnBlocked(String str);

    /* access modifiers changed from: protected */
    public abstract boolean onTmpIsBiometricsWakeUpReason(String str);

    /* access modifiers changed from: protected */
    public abstract boolean onTmpIsBlockedByFace();

    /* access modifiers changed from: protected */
    public abstract boolean onTmpIsBlockedByFingerprint();

    /* access modifiers changed from: protected */
    public abstract boolean onTmpIsFaceWakeUpReason(String str);

    /* access modifiers changed from: protected */
    public abstract boolean onTmpIsFingerprintWakeUpReason(String str);

    /* access modifiers changed from: protected */
    public abstract boolean onTmpIsStartGoToSleep();

    /* access modifiers changed from: protected */
    public abstract void onTmpNotifyMotionGameAppForeground(String str, boolean z);

    /* access modifiers changed from: protected */
    public abstract void onTmpUnblockScreenOn(String str);

    /* access modifiers changed from: protected */
    public abstract void onTmpWakeUpAndBlockScreenOn(String str);

    public OppoBasePowerManagerService(Context context) {
        super(context);
        this.mColorPowerMSEx = ColorServiceFactory.getInstance().getFeature(IColorPowerManagerServiceEx.DEFAULT, new Object[]{context, this});
        this.mPswPowerMs = PswServiceFactory.getInstance().getFeature(IPswPowerManagerServiceEx.DEFAULT, new Object[]{context, this});
        this.mOppoLocalService = new OppoLocalService();
    }

    public void onOppoInit(ArrayList<PowerManagerService.WakeLock> wakeLocks, Object lock, IColorPowerManagerServiceInner colorPowerMSInner) {
        Slog.i(TAG, "onOppoInit");
        this.mWakeLocks = wakeLocks;
        this.mLock = lock;
        this.mColorPowerMSInner = colorPowerMSInner;
    }

    public void onOppoStart() {
        IColorPowerManagerServiceEx iColorPowerManagerServiceEx = this.mColorPowerMSEx;
        if (iColorPowerManagerServiceEx != null) {
            iColorPowerManagerServiceEx.onStart();
        }
        IPswPowerManagerServiceEx iPswPowerManagerServiceEx = this.mPswPowerMs;
        if (iPswPowerManagerServiceEx != null) {
            iPswPowerManagerServiceEx.onStart();
        }
        publishLocalService(OppoPowerManagerInternal.class, this.mOppoLocalService);
    }

    public void onOppoSystemReady() {
        IColorPowerManagerServiceEx iColorPowerManagerServiceEx = this.mColorPowerMSEx;
        if (iColorPowerManagerServiceEx != null) {
            iColorPowerManagerServiceEx.systemReady();
        }
        IPswPowerManagerServiceEx iPswPowerManagerServiceEx = this.mPswPowerMs;
        if (iPswPowerManagerServiceEx != null) {
            iPswPowerManagerServiceEx.systemReady();
            this.mPswPowerMs.initOppoNwPowerStateManager();
        }
    }

    final class OppoLocalService extends OppoPowerManagerInternal {
        OppoLocalService() {
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public String getShortScreenOnStatus() {
            return OppoBasePowerManagerService.this.mOppoPowerFuncHelper.getShortScreenOnStatusInternal();
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public String getScreenOnReason() {
            return OppoBasePowerManagerService.this.mOppoPowerFuncHelper.mWakeupReason;
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public String getSleepReason() {
            return OppoBasePowerManagerService.this.mOppoPowerFuncHelper.sleepReasonToString();
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public ArrayList<Integer> getMusicPlayerList() {
            return OppoFeatureCache.get(IColorWakeLockCheck.DEFAULT).getMusicPlayerList();
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public int[] getWakeLockedPids() {
            int[] res;
            if (OppoBasePowerManagerService.this.mLock == null || OppoBasePowerManagerService.this.mWakeLocks == null) {
                return new int[1];
            }
            synchronized (OppoBasePowerManagerService.this.mLock) {
                int N = OppoBasePowerManagerService.this.mWakeLocks.size();
                res = new int[N];
                for (int i = 0; i < N; i++) {
                    res[i] = ((PowerManagerService.WakeLock) OppoBasePowerManagerService.this.mWakeLocks.get(i)).mOwnerPid;
                }
            }
            return res;
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public int setWakeLockStateForHans(int uid, boolean disable) {
            if (OppoBasePowerManagerService.this.mLock == null || OppoBasePowerManagerService.this.mWakeLocks == null || OppoBasePowerManagerService.this.mColorPowerMSInner == null) {
                return 0;
            }
            synchronized (OppoBasePowerManagerService.this.mLock) {
                boolean changed = false;
                Iterator it = OppoBasePowerManagerService.this.mWakeLocks.iterator();
                while (it.hasNext()) {
                    PowerManagerService.WakeLock wl = (PowerManagerService.WakeLock) it.next();
                    if (wl.mOwnerUid == uid) {
                        if ((wl.mFlags & 65535) != 1) {
                            Slog.i(OppoBasePowerManagerService.TAG, "setWakeLockStateForHans HANS_NO_PARTIAL_WAKE_LOCK : " + wl + " disable: " + disable + " changed: " + changed);
                            return 3;
                        } else if (OppoBasePowerManagerService.this.mColorPowerMSInner.getDisabledByHans(wl) != disable) {
                            OppoBasePowerManagerService.this.mColorPowerMSInner.setDisableByHans(wl, disable);
                            if (OppoBasePowerManagerService.this.mColorPowerMSInner.setWakeLockDisabledStateLocked(wl)) {
                                changed = true;
                                if (wl.mDisabled) {
                                    OppoBasePowerManagerService.this.mColorPowerMSInner.notifyWakeLockReleasedLocked(wl);
                                } else {
                                    OppoBasePowerManagerService.this.mColorPowerMSInner.notifyWakeLockAcquiredLocked(wl);
                                }
                            }
                        }
                    } else if (wl.mWorkSource != null) {
                        int wsSize = wl.mWorkSource.size();
                        int i = 0;
                        while (i < wsSize) {
                            if (wl.mWorkSource.get(i) != uid || wl.mDisabled == disable) {
                                i++;
                            } else {
                                Slog.i(OppoBasePowerManagerService.TAG, "setWakeLockStateForHans find out in ws: " + wl + " disable: " + disable);
                                return 2;
                            }
                        }
                        continue;
                    } else {
                        continue;
                    }
                }
                if (!changed) {
                    return 0;
                }
                OppoBasePowerManagerService.this.mColorPowerMSInner.updateDirtyByHans();
                OppoBasePowerManagerService.this.mColorPowerMSInner.updatePowerStateLocked();
                return 1;
            }
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public int pendingJobs(int uid, boolean screenOff, boolean charging, String reason) {
            return OppoBasePowerManagerService.this.pendingJobs(uid, screenOff, charging, reason);
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public int restoreJobs(int uid, boolean screenOff, boolean charging, String reason) {
            return OppoBasePowerManagerService.this.restoreJobs(uid, screenOff, charging, reason);
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public boolean isStartGoToSleep() {
            return OppoBasePowerManagerService.this.onTmpIsStartGoToSleep();
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public void wakeUpAndBlockScreenOn(String reason) {
            OppoBasePowerManagerService.this.onTmpWakeUpAndBlockScreenOn(reason);
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public void unblockScreenOn(String reason) {
            OppoBasePowerManagerService.this.onTmpUnblockScreenOn(reason);
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public void gotoSleepWhenScreenOnBlocked(String reason) {
            OppoBasePowerManagerService.this.onTmpGotoSleepWhenScreenOnBlocked(reason);
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public boolean isBiometricsWakeUpReason(String reason) {
            return OppoBasePowerManagerService.this.onTmpIsBiometricsWakeUpReason(reason);
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public boolean isFingerprintWakeUpReason(String reason) {
            return OppoBasePowerManagerService.this.onTmpIsFingerprintWakeUpReason(reason);
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public boolean isFaceWakeUpReason(String reason) {
            return OppoBasePowerManagerService.this.onTmpIsFaceWakeUpReason(reason);
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public boolean isBlockedByFace() {
            return OppoBasePowerManagerService.this.onTmpIsBlockedByFace();
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public boolean isBlockedByFingerprint() {
            return OppoBasePowerManagerService.this.onTmpIsBlockedByFingerprint();
        }

        @Override // com.android.server.power.OppoPowerManagerInternal
        public void notifyMotionGameAppForeground(String packageName, boolean foregrounds) {
            OppoBasePowerManagerService.this.onTmpNotifyMotionGameAppForeground(packageName, foregrounds);
        }
    }

    /* access modifiers changed from: package-private */
    public void onBootComplete() {
        this.mLocalJobScheduler = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
    }

    public int pendingJobs(int uid, boolean screenOff, boolean charging, String reason) {
        int status;
        if (this.mLocalJobScheduler == null) {
            Slog.d(TAG, "pending job but not complete");
            return 1;
        } else if (!preparePendingJob(screenOff, charging)) {
            Slog.d(TAG, "pending job for " + uid + " fail for charge and screen");
            return 1;
        } else {
            synchronized (this.mLock) {
                status = getWakelockStatusLocked(uid, JOB_WAKELOCK_PREFIX);
            }
            Slog.d(TAG, "getWakelockStatusLocked = " + status);
            if ((status & 1) == 0 || (status & 2) != 0) {
                return 1;
            }
            ColorJobSchedulerInternal colorJobScheduler = typeCastingColorJobSchedulerInternal(this.mLocalJobScheduler);
            if (colorJobScheduler != null) {
                Slog.d(TAG, "do pending jobs");
                return colorJobScheduler.pendingJobs(uid);
            }
            Slog.d(TAG, "do pending jobs fail");
            return 1;
        }
    }

    public int restoreJobs(int uid, boolean screenOff, boolean charging, String reason) {
        if (this.mLocalJobScheduler == null) {
            Slog.d(TAG, "restore job but not complete");
            return 2;
        }
        if (screenOff && !charging) {
            synchronized (this.mJobRestoreHistory) {
                int count = OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).getPendingJobCount();
                if (count <= 0 || this.mJobRestoreHistory.getTotalRestoreCountInWindow(uid, UserHandle.getUserId(uid)) <= count) {
                    this.mJobRestoreHistory.recordJobRestoreForPackage(uid, UserHandle.getUserId(uid), SystemClock.elapsedRealtime());
                } else {
                    Slog.d(TAG, "restore job for " + uid + " fail for frequency");
                    return 1;
                }
            }
        }
        ColorJobSchedulerInternal colorJobScheduler = typeCastingColorJobSchedulerInternal(this.mLocalJobScheduler);
        if (colorJobScheduler != null) {
            Slog.d(TAG, "do restore jobs");
            return colorJobScheduler.restoreJobs(uid);
        }
        Slog.d(TAG, "do restore jobs fail");
        return 1;
    }

    private static ColorJobSchedulerInternal typeCastingColorJobSchedulerInternal(JobSchedulerInternal jobInternal) {
        return (ColorJobSchedulerInternal) ColorTypeCastingHelper.typeCasting(ColorJobSchedulerInternal.class, jobInternal);
    }

    private static class JobRestoreHistory {
        private static final long DEFAULT_WINDOW_SIZE = 180000;
        private ArrayMap<Pair<Integer, Integer>, LongArrayQueue> mPackageHistory;
        private long mWindowSize;

        private JobRestoreHistory() {
            this.mPackageHistory = new ArrayMap<>();
            this.mWindowSize = DEFAULT_WINDOW_SIZE;
        }

        /* access modifiers changed from: package-private */
        public void setWindowSize(long windowSize) {
            this.mWindowSize = windowSize;
        }

        /* access modifiers changed from: package-private */
        public void recordJobRestoreForPackage(int uid, int userId, long nowElapsed) {
            Pair<Integer, Integer> packageUser = Pair.create(Integer.valueOf(uid), Integer.valueOf(userId));
            LongArrayQueue history = this.mPackageHistory.get(packageUser);
            if (history == null) {
                history = new LongArrayQueue();
                this.mPackageHistory.put(packageUser, history);
            }
            if (history.size() == 0 || history.peekLast() < nowElapsed) {
                history.addLast(nowElapsed);
            }
            snapToWindow(history);
        }

        /* access modifiers changed from: package-private */
        public void removeForUser(int userId) {
            for (int i = this.mPackageHistory.size() - 1; i >= 0; i--) {
                if (((Integer) this.mPackageHistory.keyAt(i).second).intValue() == userId) {
                    this.mPackageHistory.removeAt(i);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void removeForUid(int uid, int userId) {
            this.mPackageHistory.remove(Pair.create(Integer.valueOf(uid), Integer.valueOf(userId)));
        }

        private void snapToWindow(LongArrayQueue history) {
            while (history.peekFirst() + this.mWindowSize < history.peekLast()) {
                history.removeFirst();
            }
        }

        /* access modifiers changed from: package-private */
        public int getTotalRestoreCountInWindow(int uid, int userId) {
            LongArrayQueue history = this.mPackageHistory.get(Pair.create(Integer.valueOf(uid), Integer.valueOf(userId)));
            if (history == null) {
                return 0;
            }
            return history.size();
        }

        /* access modifiers changed from: package-private */
        public long getNthLastRestoreForPackage(int uid, int userId, int n) {
            int i;
            LongArrayQueue history = this.mPackageHistory.get(Pair.create(Integer.valueOf(uid), Integer.valueOf(userId)));
            if (history != null && (i = history.size() - n) >= 0) {
                return history.get(i);
            }
            return 0;
        }
    }

    private boolean preparePendingJob(boolean screenoff, boolean charging) {
        return screenoff && !charging;
    }

    private int getWakelockStatusLocked(int targetUid, String prefix) {
        List<WorkSource.WorkChain> workChains;
        int flag = 0;
        Iterator<PowerManagerService.WakeLock> it = this.mWakeLocks.iterator();
        while (it.hasNext()) {
            PowerManagerService.WakeLock wl = it.next();
            if ((wl.mFlags & 65535) == 1 && !wl.mDisabled && wl.mOwnerUid == 1000) {
                boolean hasSameUid = false;
                if (wl.mWorkSource != null) {
                    int k = 0;
                    while (true) {
                        if (k >= wl.mWorkSource.size()) {
                            break;
                        } else if (wl.mWorkSource.get(k) == targetUid) {
                            hasSameUid = true;
                            break;
                        } else {
                            k++;
                        }
                    }
                    if (!hasSameUid && (workChains = wl.mWorkSource.getWorkChains()) != null) {
                        int k2 = 0;
                        while (true) {
                            if (k2 >= workChains.size()) {
                                break;
                            } else if (workChains.get(k2).getAttributionUid() == targetUid) {
                                hasSameUid = true;
                                break;
                            } else {
                                k2++;
                            }
                        }
                    }
                }
                if (!hasSameUid) {
                    continue;
                } else {
                    flag |= 1;
                    if (!wl.mTag.startsWith(prefix)) {
                        Slog.d(TAG, "add STATUS_HAS_OTHER_SYSTEM_WAKELOCK because of non prefix");
                        return flag | 2;
                    } else if (!black(wl.mTag, prefix)) {
                        Slog.d(TAG, "add STATUS_HAS_OTHER_SYSTEM_WAKELOCK because of black");
                        return flag | 2;
                    }
                }
            }
        }
        return flag;
    }

    private boolean black(String tag, String prefix) {
        String component;
        int split;
        if (prefix.length() >= tag.length() || (split = (component = tag.substring(prefix.length())).indexOf(SliceClientPermissions.SliceAuthority.DELIMITER)) <= 0) {
            return false;
        }
        String pkgName = component.substring(0, split);
        boolean result = OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).isBlackJobList(pkgName, component);
        Slog.d(TAG, "black got component = " + component + ", pkgName = " + pkgName + ", result = " + result);
        return result;
    }
}
