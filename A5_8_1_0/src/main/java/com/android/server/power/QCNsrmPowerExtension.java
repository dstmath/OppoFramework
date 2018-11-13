package com.android.server.power;

import android.net.util.NetworkConstants;
import android.os.Binder;
import android.os.Process;
import java.util.ArrayList;

public final class QCNsrmPowerExtension {
    static final String TAG = "QCNsrmPowerExtn";
    static final boolean localLOGV = false;
    private final ArrayList<Integer> mPmsBlockedUids = new ArrayList();
    private PowerManagerService pmHandle;

    public QCNsrmPowerExtension(PowerManagerService handle) {
        this.pmHandle = handle;
    }

    protected void checkPmsBlockedWakelocks(int uid, int pid, int flags, String tag, WakeLock pMwakeLock) {
        if (this.mPmsBlockedUids.contains(new Integer(uid)) && uid != Process.myUid()) {
            updatePmsBlockedWakelock(pMwakeLock, true);
        }
    }

    private boolean checkWorkSourceObjectId(int uid, WakeLock wl) {
        int index = 0;
        while (index < wl.mWorkSource.size()) {
            try {
                if (uid == wl.mWorkSource.get(index)) {
                    return true;
                }
                index++;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    protected boolean processPmsBlockedUid(int uid, boolean isBlocked, ArrayList<WakeLock> mWakeLocks) {
        boolean changed = false;
        if (updatePmsBlockedUidAllowed(uid, isBlocked)) {
            return false;
        }
        for (int index = 0; index < mWakeLocks.size(); index++) {
            WakeLock wl = (WakeLock) mWakeLocks.get(index);
            if (wl != null && ((wl.mOwnerUid == uid || checkWorkSourceObjectId(uid, wl) || (wl.mTag.startsWith("*sync*") && wl.mOwnerUid == 1000)) && updatePmsBlockedWakelock(wl, isBlocked))) {
                changed = true;
            }
        }
        if (changed) {
            PowerManagerService powerManagerService = this.pmHandle;
            powerManagerService.mDirty |= 1;
            this.pmHandle.updatePowerStateLocked();
        }
        return changed;
    }

    protected boolean updatePmsBlockedUidAllowed(int uid, boolean isBlocked) {
        if (Binder.getCallingUid() != 1000) {
            return true;
        }
        updatePmsBlockedUids(uid, isBlocked);
        return false;
    }

    private void updatePmsBlockedUids(int uid, boolean isBlocked) {
        if (isBlocked) {
            this.mPmsBlockedUids.add(new Integer(uid));
        } else {
            this.mPmsBlockedUids.clear();
        }
    }

    private boolean updatePmsBlockedWakelock(WakeLock wakeLock, boolean update) {
        if (wakeLock == null || (wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) != 1 || wakeLock.mDisabled == update || this.pmHandle == null) {
            return false;
        }
        wakeLock.mDisabled = update;
        if (wakeLock.mDisabled) {
            this.pmHandle.notifyWakeLockReleasedLocked(wakeLock);
        } else {
            this.pmHandle.notifyWakeLockAcquiredLocked(wakeLock);
        }
        return true;
    }
}
