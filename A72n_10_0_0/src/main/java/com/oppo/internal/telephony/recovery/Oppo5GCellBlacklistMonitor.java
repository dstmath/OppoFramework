package com.oppo.internal.telephony.recovery;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import java.util.ArrayList;
import java.util.Iterator;

public class Oppo5GCellBlacklistMonitor {
    private static final int EVENT_CHECK_BLACKLIST = 1;
    private static final int EVENT_DELAY_RESTORE_NETWORKTYPE = 4;
    private static final int EVENT_SET_CELL_BLACKLIST = 2;
    private static final int EVENT_UPDATE_CELL_CHANGE = 3;
    private static final int MAX_CELL_COUNT = 50;
    private static final String TAG = "Oppo5GCellBlacklistMonitor";
    private ArrayList<CellInfoLocal> mCellBlacklist = new ArrayList<>();
    private Handler mHandler;
    private OppoFastRecovery mOppoFastRecovery;

    public class CellInfoLocal {
        public int mCellid;
        public long mOccurTime;

        public CellInfoLocal(int cellid, Long time) {
            this.mCellid = cellid;
            this.mOccurTime = time.longValue();
        }
    }

    private class ProcessHandler extends Handler {
        public ProcessHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                Oppo5GCellBlacklistMonitor.this.doCheckProcess();
                Oppo5GCellBlacklistMonitor.this.mHandler.sendEmptyMessageDelayed(1, (long) (Oppo5GCellBlacklistMonitor.this.mOppoFastRecovery.mRecoveryConfig.m5gCellCheckIntvl * 1000));
            } else if (i == 2) {
                Oppo5GCellBlacklistMonitor.this.setCellidToBlacklistInner(msg);
            } else if (i == 3) {
                Oppo5GCellBlacklistMonitor.this.updateCellidChangeInner(msg.arg1, msg.arg2, ((Boolean) msg.obj).booleanValue());
            } else if (i == 4) {
                Oppo5GCellBlacklistMonitor.this.restoreNetworkTypeTimeout(msg);
            }
        }
    }

    public Oppo5GCellBlacklistMonitor(OppoFastRecovery recovery, Looper looper) {
        this.mOppoFastRecovery = recovery;
        this.mHandler = new ProcessHandler(looper);
        this.mHandler.sendEmptyMessageDelayed(1, (long) (this.mOppoFastRecovery.mRecoveryConfig.m5gCellCheckIntvl * 1000));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doCheckProcess() {
        int cellid;
        Rlog.d(TAG, "doCheckProcess start");
        if (checkBlackList() && (cellid = this.mOppoFastRecovery.getLteCellid()) != -1) {
            boolean isIn = isCellidInBlacklist(cellid);
            boolean haveRemoveMsg = this.mHandler.hasMessages(4);
            Rlog.d(TAG, "doCheckProcess cellid:" + cellid + " inblacklist:" + isIn + ", haveRemoveMsg:" + haveRemoveMsg);
            if (!isIn) {
                this.mHandler.removeMessages(4);
                this.mOppoFastRecovery.restorePreferredNetworkType();
            }
        }
    }

    private boolean checkBlackList() {
        boolean removeFlag = false;
        Iterator<CellInfoLocal> iter = this.mCellBlacklist.iterator();
        long curTime = System.currentTimeMillis();
        while (iter.hasNext()) {
            CellInfoLocal v = iter.next();
            if (curTime - v.mOccurTime >= ((long) this.mOppoFastRecovery.mRecoveryConfig.m5gCellBlacklistTimeout) * 1000) {
                Rlog.d(TAG, "remove cellid " + v.mCellid + " from list!");
                iter.remove();
                removeFlag = true;
            }
        }
        return removeFlag;
    }

    public void setCellidToBlacklist(int cellid) {
        this.mHandler.removeMessages(4);
        this.mHandler.obtainMessage(2, cellid, 0).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCellidToBlacklistInner(Message msg) {
        int cellid = msg.arg1;
        long time = System.currentTimeMillis();
        Iterator<CellInfoLocal> it = this.mCellBlacklist.iterator();
        while (it.hasNext()) {
            CellInfoLocal i = it.next();
            if (i.mCellid == cellid) {
                Rlog.d(TAG, "find cellid " + cellid + " already in list, so update time");
                i.mOccurTime = time;
                return;
            }
        }
        if (this.mCellBlacklist.size() >= MAX_CELL_COUNT) {
            Rlog.w(TAG, "setCellidToBlacklist cell count reach max count");
            return;
        }
        this.mCellBlacklist.add(new CellInfoLocal(cellid, Long.valueOf(time)));
        Rlog.d(TAG, "setCellidToBlacklist add cellid " + cellid + " time:" + time);
    }

    public boolean isCellidInBlacklist(int cellid) {
        Iterator<CellInfoLocal> it = this.mCellBlacklist.iterator();
        while (it.hasNext()) {
            if (it.next().mCellid == cellid) {
                return true;
            }
        }
        return false;
    }

    public void updateCellidChange(int oldCid, int newCid, boolean needStop) {
        this.mHandler.obtainMessage(3, oldCid, newCid, Boolean.valueOf(needStop)).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCellidChangeInner(int oldCid, int newCid, boolean needStop) {
        boolean oldin = isCellidInBlacklist(oldCid);
        boolean newin = isCellidInBlacklist(newCid);
        Rlog.d(TAG, "updateCellidChangeInner " + oldCid + " -> " + newCid + " instat:" + oldin + "->" + newin + ", need stop:" + needStop);
        if (!needStop) {
            return;
        }
        if (newin) {
            this.mHandler.removeMessages(4);
        } else if (this.mHandler.hasMessages(4)) {
            Rlog.d(TAG, "updateCellidChangeInner already has msg EVENT_DELAY_RESTORE_NETWORKTYPE");
        } else {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 4;
            msg.arg1 = oldCid;
            msg.arg2 = newCid;
            Rlog.d(TAG, "updateCellidChangeInner msg send!");
            this.mHandler.removeMessages(4);
            this.mHandler.sendMessageDelayed(msg, (long) (this.mOppoFastRecovery.mRecoveryConfig.m5gCellCheckIntvl * 1000));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreNetworkTypeTimeout(Message msg) {
        Rlog.d(TAG, "restoreNetworkTypeTimeout start! " + msg.arg1 + "->" + msg.arg2);
        this.mOppoFastRecovery.restorePreferredNetworkType();
    }

    public void clearCellidBlacklist() {
        Rlog.d(TAG, "clearCellidBlacklist");
        this.mCellBlacklist.clear();
    }
}
