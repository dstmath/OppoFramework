package com.android.server.power;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.oppo.IElsaManager;
import java.util.ArrayList;

class OppoScreenOnWakeLockCheck {
    private static final String ACTION_OPPO_GUARD_ELF_SCREENON_WAKELOCK = "android.intent.action.OPPO_GUARD_ELF_SCREENON_WAKELOCK";
    private static final String ATAG = "OppoWakeLockCheck";
    private static final boolean DCS_MORE_DEBUG_INFO = true;
    private final boolean ADBG;
    private final Context mContext;
    private final Object mLock;
    private final PowerManagerService mPms;
    private ArrayList<WakeLockScreenOnRecord> mReportList = new ArrayList();
    private final CommonUtil mUtil;
    private final ArrayList<WakeLock> mWakeLocks;

    private class WakeLockScreenOnRecord {
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
            this.mPkgs = OppoScreenOnWakeLockCheck.this.mUtil.getPkgsForUid(this.mUid);
        }

        private String getReportString(String pkgName, String forgroundPkg, String topAppName, String layers, int foregroundPackageUid) {
            StringBuilder sb = new StringBuilder();
            sb.append("[ ").append(pkgName).append(" ]    ");
            sb.append("{ ").append(this.mTag).append(" }    ").append(this.mType).append("    uid(").append(this.mUid).append(")    ").append("forgroundPkg(").append(forgroundPkg).append(")    ").append("foregroundPackageUid(").append(foregroundPackageUid).append(")    ").append("topAppName(").append(topAppName).append(")    ").append("layers(").append(layers).append(")    ").append("mUserActivitySummary(0x").append(Integer.toHexString(OppoScreenOnWakeLockCheck.this.mPms.getUserActivitySummary())).append(")    ").append("mWakefulness(").append(OppoScreenOnWakeLockCheck.this.mPms.getwakefulness()).append(")    ");
            return sb.toString();
        }
    }

    public OppoScreenOnWakeLockCheck(ArrayList<WakeLock> wakeLocks, Object lock, Context context, PowerManagerService pms, CommonUtil util, boolean dbg) {
        this.mWakeLocks = wakeLocks;
        this.mLock = lock;
        this.mContext = context;
        this.mPms = pms;
        this.mUtil = util;
        this.ADBG = dbg;
    }

    public void check() {
        if (this.mPms.needScreenOnWakelockCheck()) {
            this.mReportList.clear();
            synchronized (this.mLock) {
                long now = SystemClock.uptimeMillis();
                int N = this.mWakeLocks.size();
                for (int i = 0; i < N; i++) {
                    WakeLock wl = (WakeLock) this.mWakeLocks.get(i);
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
            WakeLockScreenOnRecord rcd = (WakeLockScreenOnRecord) this.mReportList.get(i);
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
                    WakeLock wl = (WakeLock) this.mWakeLocks.get(i);
                    int type = wl.mFlags & 65535;
                    if (type == 6 || type == 10 || type == 26) {
                        if (wl.mWorkSource != null) {
                            int size = wl.mWorkSource.size();
                            for (int k = 0; k < size; k++) {
                                if (getScreenOnWakeLock(wl.mWorkSource.get(k)) != null) {
                                    wl.mFlags &= -536870913;
                                    break;
                                }
                            }
                        } else if (getScreenOnWakeLock(wl.mOwnerUid) != null) {
                            wl.mFlags &= -536870913;
                        }
                    }
                }
            }
        }
    }

    private void reportScreenOnWakelock() {
        int len = this.mReportList.size();
        if (len != 0) {
            String foregroundPackage = this.mUtil.getForegroundPackage();
            if (foregroundPackage == null || foregroundPackage.equals(IElsaManager.EMPTY_PACKAGE)) {
                if (this.ADBG) {
                    Slog.w("OppoWakeLockCheck", "reportScreenOnWakelock: foregroundPackage is null, return!!!");
                }
                return;
            }
            int i;
            String topAppName = IElsaManager.EMPTY_PACKAGE;
            String layers = IElsaManager.EMPTY_PACKAGE;
            int foregroundPackageUid = this.mUtil.getUidForPkgName(foregroundPackage);
            topAppName = this.mUtil.getTopAppName();
            layers = this.mUtil.getSurfceLayers();
            ArrayList<String> uidList = new ArrayList();
            boolean isForegroundPackage = false;
            boolean isScreenLocked = false;
            for (i = len - 1; i >= 0; i--) {
                WakeLockScreenOnRecord rcd = (WakeLockScreenOnRecord) this.mReportList.get(i);
                rcd.updatePkgNameNotLocked();
                if (rcd.mPkgs == null) {
                    if (this.ADBG) {
                        Slog.w("OppoWakeLockCheck", "reportScreenOnWakelock: wakeLockScreenOnRecord.mPkgs is null, ignore!!!  uid=" + rcd.mUid);
                    }
                    this.mReportList.remove(i);
                } else {
                    for (String pkg : rcd.mPkgs) {
                        if (foregroundPackage.equals(pkg)) {
                            isForegroundPackage = true;
                            break;
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
                    } else if (!isScreenLocked && isForegroundPackage) {
                        if (this.ADBG) {
                            Slog.w("OppoWakeLockCheck", "reportScreenOnWakelock: current package is foregroundPackage and screen is unlocked(" + foregroundPackage + "), ignore all pkgs, return!!!");
                        }
                        this.mReportList.clear();
                        uidList.clear();
                        return;
                    } else if (isScreenLocked || !this.mUtil.isWindowShownForUid(rcd.mUid)) {
                        for (String -wrap0 : rcd.mPkgs) {
                            uidList.add(rcd.getReportString(-wrap0, foregroundPackage, topAppName, layers, foregroundPackageUid));
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
            }
            for (i = 0; i < uidList.size(); i++) {
                if (this.ADBG) {
                    Slog.w("OppoWakeLockCheck", "reportScreenOnWakelock: reportString=" + ((String) uidList.get(i)));
                }
            }
            if (this.mPms.needScreenOnWakelockCheck()) {
                if (uidList.size() > 0) {
                    Intent intent = new Intent(ACTION_OPPO_GUARD_ELF_SCREENON_WAKELOCK);
                    intent.putStringArrayListExtra("data", uidList);
                    intent.putExtra("isScreenLocked", isScreenLocked);
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                }
                return;
            }
            this.mReportList.clear();
        }
    }

    private boolean isSkipForgroundPkg(WakeLockScreenOnRecord rcd) {
        for (String pkg : rcd.mPkgs) {
            if ("com.coloros.pictorial".equals(pkg) || "com.google.android.apps.maps".equals(pkg)) {
                return true;
            }
        }
        return false;
    }
}
