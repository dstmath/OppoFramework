package com.android.server.power;

import android.app.KeyguardManager;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.am.IColorAbnormalAppManager;
import com.android.server.power.PowerManagerService;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class ColorScreenOnWakeLockCheck {
    private static final String ACTION_OPPO_GUARD_ELF_SCREENON_WAKELOCK = "android.intent.action.OPPO_GUARD_ELF_SCREENON_WAKELOCK";
    private static final String ATAG = "OppoWakeLockCheck";
    private final boolean ADBG;
    private final Context mContext;
    private final Object mLock;
    private final PowerManagerService mPms;
    private ArrayList<WakeLockScreenOnRecord> mReportList = new ArrayList<>();
    private final CommonUtil mUtil;
    private final ArrayList<PowerManagerService.WakeLock> mWakeLocks;

    public ColorScreenOnWakeLockCheck(ArrayList<PowerManagerService.WakeLock> wakeLocks, Object lock, Context context, PowerManagerService pms, CommonUtil util, boolean dbg) {
        this.mWakeLocks = wakeLocks;
        this.mLock = lock;
        this.mContext = context;
        this.mPms = pms;
        this.mUtil = util;
        this.ADBG = dbg;
    }

    public void check() {
        if (getInner().needScreenOnWakelockCheck()) {
            this.mReportList.clear();
            synchronized (this.mLock) {
                long now = SystemClock.uptimeMillis();
                int N = this.mWakeLocks.size();
                for (int i = 0; i < N; i++) {
                    PowerManagerService.WakeLock wl = this.mWakeLocks.get(i);
                    int type = wl.mFlags & 65535;
                    if (type == 6 || type == 10 || type == 26) {
                        long hold = (now - wl.mAcquireTime) / 1000;
                        if (wl.mWorkSource != null) {
                            int size = wl.mWorkSource.size();
                            for (int k = 0; k < size; k++) {
                                addScreenOnReportListLocked(wl.mWorkSource.get(k), hold, wl.mTag, type);
                            }
                        } else {
                            addScreenOnReportListLocked(wl.mOwnerUid, hold, wl.mTag, type);
                        }
                    }
                }
            }
            reportScreenOnWakelock();
            removeFlagOnAfterRelease();
            this.mReportList.clear();
        }
    }

    private WakeLockScreenOnRecord getScreenOnWakeLock(int uid) {
        int len = this.mReportList.size();
        for (int i = 0; i < len; i++) {
            WakeLockScreenOnRecord rcd = this.mReportList.get(i);
            if (uid == rcd.mUid) {
                return rcd;
            }
        }
        return null;
    }

    private void addScreenOnReportListLocked(int uid, long hold, String tag, int type) {
        if (getScreenOnWakeLock(uid) == null) {
            this.mReportList.add(new WakeLockScreenOnRecord(uid, hold, tag, type));
        }
    }

    private void removeFlagOnAfterRelease() {
        if (this.mReportList.size() != 0) {
            synchronized (this.mLock) {
                int N = this.mWakeLocks.size();
                for (int i = 0; i < N; i++) {
                    PowerManagerService.WakeLock wl = this.mWakeLocks.get(i);
                    int type = wl.mFlags & 65535;
                    if (type == 6 || type == 10 || type == 26) {
                        if (wl.mWorkSource != null) {
                            int size = wl.mWorkSource.size();
                            int k = 0;
                            while (true) {
                                if (k >= size) {
                                    break;
                                } else if (shouldremoveFlagOnAfterRelease(wl.mWorkSource.get(k))) {
                                    wl.mFlags = -536870913 & wl.mFlags;
                                    if (this.ADBG) {
                                        Slog.d("OppoWakeLockCheck", "rmv flag ON_AFTER_RELEASE: wl=" + wl);
                                    }
                                } else {
                                    k++;
                                }
                            }
                        } else if (shouldremoveFlagOnAfterRelease(wl.mOwnerUid)) {
                            wl.mFlags &= -536870913;
                            if (this.ADBG) {
                                Slog.d("OppoWakeLockCheck", "rmv flag ON_AFTER_RELEASE: wl=" + wl);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean shouldremoveFlagOnAfterRelease(int uid) {
        WakeLockScreenOnRecord rcd = getScreenOnWakeLock(uid);
        if (rcd == null || rcd.mPkgs == null) {
            return false;
        }
        for (String pkg : rcd.mPkgs) {
            if (OppoFeatureCache.get(IColorAbnormalAppManager.DEFAULT).isNotRestrictApp(pkg)) {
                return false;
            }
        }
        return true;
    }

    private void reportScreenOnWakelock() {
        int len = this.mReportList.size();
        if (len != 0) {
            List<String> listTopApp = this.mUtil.getAllTopPkgName();
            if (listTopApp != null && !listTopApp.isEmpty()) {
                ArrayList<String> uidList = new ArrayList<>();
                boolean isForegroundPackage = false;
                boolean isScreenLocked = false;
                int i = len - 1;
                while (true) {
                    boolean z = false;
                    if (i >= 0) {
                        WakeLockScreenOnRecord rcd = this.mReportList.get(i);
                        rcd.updatePkgNameNotLocked();
                        if (rcd.mPkgs == null) {
                            if (this.ADBG) {
                                Slog.w("OppoWakeLockCheck", "reportScreenOnWakelock: wakeLockScreenOnRecord.mPkgs is null, ignore!!!  uid=" + rcd.mUid);
                            }
                            this.mReportList.remove(i);
                        } else {
                            String[] strArr = rcd.mPkgs;
                            int length = strArr.length;
                            int i2 = 0;
                            while (true) {
                                if (i2 >= length) {
                                    break;
                                } else if (listTopApp.contains(strArr[i2])) {
                                    isForegroundPackage = true;
                                    break;
                                } else {
                                    i2++;
                                }
                            }
                            if (((KeyguardManager) this.mContext.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
                                isScreenLocked = true;
                            }
                            if (isScreenLocked && isForegroundPackage && isSkipForgroundPkg(rcd)) {
                                if (this.ADBG) {
                                    Slog.d("OppoWakeLockCheck", "reportScreenOnWakelock: is Pictorial");
                                }
                                this.mReportList.remove(i);
                            } else if (isScreenLocked && isForegroundPackage && getInner().isForgroundUid(rcd.mUid)) {
                                if (this.ADBG) {
                                    Slog.d("OppoWakeLockCheck", "reportScreenOnWakelock: is ScreenLocked forg");
                                }
                                this.mReportList.remove(i);
                            } else if (!isScreenLocked && isForegroundPackage) {
                                if (this.ADBG) {
                                    Slog.w("OppoWakeLockCheck", "reportScreenOnWakelock: is foregroundPackage and screen is unlocked, ignore all pkgs, return!!!");
                                    Slog.d("OppoWakeLockCheck", "reportScreenOnWakelock: listTopApp=" + listTopApp);
                                }
                                this.mReportList.clear();
                                uidList.clear();
                                return;
                            } else if (isScreenLocked || !this.mUtil.isWindowShownForUid(rcd.mUid)) {
                                for (int m = 0; m < rcd.mPkgs.length; m++) {
                                    uidList.add(rcd.getReportString(rcd.mPkgs[m], listTopApp));
                                }
                            } else {
                                if (this.ADBG) {
                                    Slog.w("OppoWakeLockCheck", "reportScreenOnWakelock: current package has window shown and screen is unlocked(" + rcd.mPkgs[0] + "), ignore all pkgs, return!!!");
                                }
                                this.mReportList.clear();
                                uidList.clear();
                                return;
                            }
                        }
                        i--;
                    } else {
                        for (int i3 = 0; i3 < uidList.size(); i3++) {
                            if (this.ADBG) {
                                Slog.w("OppoWakeLockCheck", "reportScreenOnWakelock: reportString=" + uidList.get(i3));
                            }
                        }
                        if (!getInner().needScreenOnWakelockCheck()) {
                            this.mReportList.clear();
                            return;
                        } else if (uidList.size() > 0) {
                            Intent intent = new Intent(ACTION_OPPO_GUARD_ELF_SCREENON_WAKELOCK);
                            intent.putStringArrayListExtra("data", uidList);
                            if (isScreenLocked) {
                                z = true;
                            }
                            intent.putExtra("isScreenLocked", z);
                            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                            return;
                        } else {
                            return;
                        }
                    }
                }
            } else if (this.ADBG) {
                Slog.d("OppoWakeLockCheck", "reportScreenOnWakelock: top app is null!!!");
            }
        }
    }

    private boolean isSkipForgroundPkg(WakeLockScreenOnRecord rcd) {
        String[] strArr = rcd.mPkgs;
        for (String pkg : strArr) {
            if ("com.coloros.pictorial".equals(pkg) || "com.google.android.apps.maps".equals(pkg) || "com.heytap.pictorial".equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class WakeLockScreenOnRecord {
        long mHold;
        String[] mPkgs;
        String mTag;
        String mType;
        int mUid;

        public WakeLockScreenOnRecord(int uid, long hold, String tag, int type) {
            this.mUid = uid;
            this.mHold = hold;
            this.mTag = tag;
            if (type == 6) {
                this.mType = "SCREEN_DIM_WAKE_LOCK";
            } else if (type == 10) {
                this.mType = "SCREEN_BRIGHT_WAKE_LOCK";
            } else {
                this.mType = "FULL_WAKE_LOCK";
            }
        }

        public void updatePkgNameNotLocked() {
            this.mPkgs = ColorScreenOnWakeLockCheck.this.mUtil.getPkgsForUid(this.mUid);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getReportString(String pkgName, List<String> listTopApp) {
            StringBuilder sb = new StringBuilder();
            sb.append("[ ");
            sb.append(pkgName);
            sb.append(" ]    ");
            sb.append("{ ");
            sb.append(this.mTag);
            sb.append(" }    ");
            sb.append(this.mType);
            sb.append("    uid(");
            sb.append(this.mUid);
            sb.append(")    ");
            int i = 0;
            while (true) {
                if (i < listTopApp.size()) {
                    String pkg = listTopApp.get(i);
                    if (pkg != null && !"".equals(pkg)) {
                        sb.append("forgroundPkg(");
                        sb.append(pkg);
                        sb.append(")    ");
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
            sb.append("foregroundPackageUid( )    ");
            sb.append("mUserActivitySummary(0x");
            sb.append(Integer.toHexString(ColorScreenOnWakeLockCheck.this.getInner().getUserActivitySummary()));
            sb.append(")    ");
            sb.append("mWakefulness(");
            sb.append(ColorScreenOnWakeLockCheck.this.getInner().getwakefulness());
            sb.append(")    ");
            return sb.toString();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IColorPowerManagerServiceInner getInner() {
        OppoBasePowerManagerService basePms;
        PowerManagerService powerManagerService = this.mPms;
        if (powerManagerService == null || (basePms = (OppoBasePowerManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePowerManagerService.class, powerManagerService)) == null || basePms.mColorPowerMSInner == null) {
            return IColorPowerManagerServiceInner.DEFAULT;
        }
        return basePms.mColorPowerMSInner;
    }
}
