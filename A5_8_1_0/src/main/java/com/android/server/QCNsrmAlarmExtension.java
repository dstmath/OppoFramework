package com.android.server;

import android.os.Binder;
import android.os.PowerManager.WakeLock;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public final class QCNsrmAlarmExtension {
    private static final int BLOCKED_UID_CHECK_INTERVAL = 1000;
    static final String TAG = "QCNsrmAlarmExtn";
    static final boolean localLOGV = false;
    private static final ArrayList<Integer> mBlockedUids = new ArrayList();
    private static final ArrayList<Integer> mTriggeredUids = new ArrayList();
    private AlarmManagerService almHandle;

    class CheckBlockedUidTimerTask extends TimerTask {
        private int mUid;
        WakeLock mWakeLock;

        CheckBlockedUidTimerTask(int uid, WakeLock lWakeLock) {
            this.mUid = uid;
            this.mWakeLock = lWakeLock;
        }

        public void run() {
            if (QCNsrmAlarmExtension.mBlockedUids.contains(Integer.valueOf(this.mUid)) && QCNsrmAlarmExtension.mTriggeredUids.contains(Integer.valueOf(this.mUid))) {
                synchronized (QCNsrmAlarmExtension.this.almHandle.mLock) {
                    if (this.mWakeLock.isHeld()) {
                        this.mWakeLock.release();
                    }
                }
            }
        }
    }

    public QCNsrmAlarmExtension(AlarmManagerService handle) {
        this.almHandle = handle;
    }

    protected void processBlockedUids(int uid, boolean isBlocked, WakeLock mWakeLock) {
        if (Binder.getCallingUid() == 1000) {
            if (isBlocked) {
                mBlockedUids.add(new Integer(uid));
                new Timer().schedule(new CheckBlockedUidTimerTask(uid, mWakeLock), 1000);
            } else {
                mBlockedUids.clear();
            }
        }
    }

    protected void addTriggeredUid(int uid) {
        mTriggeredUids.add(new Integer(uid));
    }

    protected void removeTriggeredUid(int uid) {
        mTriggeredUids.remove(new Integer(uid));
    }

    protected boolean hasBlockedUid(int uid) {
        return mBlockedUids.contains(Integer.valueOf(uid));
    }
}
