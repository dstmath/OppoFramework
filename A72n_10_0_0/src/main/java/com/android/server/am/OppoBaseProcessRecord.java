package com.android.server.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.CompatibilityInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.os.OppoBatteryStatsImpl;
import com.android.server.wm.WindowProcessListener;
import java.util.ArrayList;

public abstract class OppoBaseProcessRecord implements WindowProcessListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "OppoBaseProcessRecord";
    OppoBatteryStatsImpl.Uid.Proc curProcOppoBatteryStats;
    boolean isANR;
    int isSelected;
    volatile boolean isWaitingPermissionChoice;
    private boolean mIsRPLaunch;
    PackagePermission mPackagePermission;
    PackagePermission mPersistPackagePermission;
    final ArrayList<OppoReceiverRecord> mReceiverRecordsList = new ArrayList<>();
    private Object mRecordListLocker = new Object();

    /* access modifiers changed from: protected */
    public abstract void onRequestScheduleReceiver(Intent intent, ActivityInfo activityInfo, CompatibilityInfo compatibilityInfo, int i, String str, Bundle bundle, boolean z, int i2, int i3, int i4) throws RemoteException;

    public boolean getIsANR() {
        return this.isANR;
    }

    public void setIsANR(boolean ANR) {
        this.isANR = ANR;
    }

    public boolean isRPLaunch() {
        return this.mIsRPLaunch;
    }

    public void setRPLaunch(boolean rpLaunch) {
        this.mIsRPLaunch = rpLaunch;
    }

    public int getOppoReceiverRecordListSize() {
        return this.mReceiverRecordsList.size();
    }

    public OppoReceiverRecord getOppoReceiverRecordByIndex(int index) {
        if (index >= 0 && index < this.mReceiverRecordsList.size()) {
            return this.mReceiverRecordsList.get(index);
        }
        Slog.w(TAG, "getOppoReceiverRecordByIndex: index illegal, " + index);
        return null;
    }

    public void removeOppoReceiverRecord(OppoReceiverRecord record) {
        if (record == null) {
            Slog.w(TAG, "removeOppoReceiverRecord: record empty.");
            return;
        }
        synchronized (this.mRecordListLocker) {
            this.mReceiverRecordsList.remove(record);
        }
    }

    public void addOppoReceiverRecord(OppoReceiverRecord record) {
        if (record == null) {
            Slog.w(TAG, "addOppoReceiverRecord: record empty.");
            return;
        }
        synchronized (this.mRecordListLocker) {
            this.mReceiverRecordsList.add(record);
        }
    }

    public void skipCurrentReceiver(OppoBaseBroadcastQueue queue) {
        synchronized (this.mRecordListLocker) {
            int size = getOppoReceiverRecordListSize();
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "app.mReceiverRecordsList size:" + size);
            }
            for (int i = size - 1; i >= 0; i--) {
                OppoReceiverRecord receiverRecord = this.mReceiverRecordsList.get(i);
                if (receiverRecord != null && receiverRecord.mQueue == queue) {
                    receiverRecord.cancelBroadcastTimeoutLocked();
                    queue.removeOppoReceiverRecord(receiverRecord);
                    this.mReceiverRecordsList.remove(receiverRecord);
                }
            }
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "after remove, app.mReceiverRecordsList size:" + this.mReceiverRecordsList.size());
            }
        }
    }

    public boolean isReceivingBroadcast() {
        synchronized (this.mRecordListLocker) {
            int size = getOppoReceiverRecordListSize();
            for (int i = 0; i < size; i++) {
                OppoReceiverRecord receiverRecord = this.mReceiverRecordsList.get(i);
                if (!(receiverRecord == null || receiverRecord.mQueue == null)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void removeAllOppoReceiverRecords(OppoBaseBroadcastQueue queue) {
        synchronized (this.mRecordListLocker) {
            try {
                int size = getOppoReceiverRecordListSize();
                Slog.v(TAG, "curAppBase:" + this + ", curApp.receiverRecords.size:" + size);
                boolean removeFromQueue = queue != null;
                if (size > 0) {
                    for (int i = size - 1; i >= 0; i--) {
                        OppoReceiverRecord receiverRecord = getOppoReceiverRecordByIndex(i);
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG, "broadcastTimeoutLocked receiverRecord:" + receiverRecord);
                        }
                        if (receiverRecord != null) {
                            if (removeFromQueue) {
                                queue.removeOppoReceiverRecord(receiverRecord);
                            }
                            removeOppoReceiverRecord(receiverRecord);
                        }
                    }
                }
            } catch (Exception e) {
                Slog.w(TAG, "Exception in removeAllOppoReceiverRecords!", e);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void requestScheduleReceiver(Intent intent, ActivityInfo info, CompatibilityInfo compatInfo, int resultCode, String data, Bundle extras, boolean sync, int sendingUser, int processState, int hasCode) throws RemoteException {
        onRequestScheduleReceiver(intent, info, compatInfo, resultCode, data, extras, sync, sendingUser, processState, hasCode);
    }
}
