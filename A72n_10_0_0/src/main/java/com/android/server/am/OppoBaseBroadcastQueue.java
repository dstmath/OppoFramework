package com.android.server.am;

import android.os.Handler;
import android.util.LogPrinter;
import android.util.Printer;
import android.util.Slog;
import android.util.SparseArray;
import com.color.util.ColorTypeCastingHelper;

public abstract class OppoBaseBroadcastQueue {
    static final int BROADCAST_NEXT_MSG = 202;
    protected static final int BROADCAST_NEXT_MSG_DELAY = 2000;
    private static final boolean DEBUG = false;
    private static final String TAG = "OppoBaseBroadcastQueue";
    protected int mAllowDebugTime = 1000;
    IColorBroadcastQueueEx mColorQueue = null;
    protected long mLastTimeForDispatchMsg = 0;
    protected Printer mLogPrinterForMsgDump = new LogPrinter(3, TAG);
    final SparseArray<OppoReceiverRecord> mReceiverRecordsArray = new SparseArray<>();

    public abstract Handler getBroadcastHandler();

    public abstract String getBroadcastQueueName();

    public abstract int getOrderedBroadcastsSize();

    public abstract void requestProcessNextBroadcastLocked(boolean z, boolean z2);

    public void removeNextMessages(BroadcastRecord record) {
        if (record == null) {
            Slog.w(TAG, "removeNextMessages, record null.");
            return;
        }
        IOppoBaseBroadcastRecord recordInterface = typeCastToParent(record);
        if (recordInterface == null) {
            Slog.w(TAG, "typeCastToParent failed:" + record);
        } else if (recordInterface.getMessageDelayFlag()) {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "removeMessages, BROADCAST_NEXT_MSG " + record);
            }
            Handler handler = getBroadcastHandler();
            if (handler != null) {
                handler.removeMessages(BROADCAST_NEXT_MSG, record);
            } else {
                Slog.w(TAG, "removeNextMessages: handler empty.");
            }
            recordInterface.setMessageDelayFlag(false);
        }
    }

    public void addOppoReceiverRecord(OppoReceiverRecord receiverRecord) {
        if (receiverRecord == null) {
            Slog.w(TAG, "addOppoReceiverRecord, record null.");
        } else {
            this.mReceiverRecordsArray.put(receiverRecord.hashCode(), receiverRecord);
        }
    }

    public void removeOppoReceiverRecord(OppoReceiverRecord receiverRecord) {
        if (receiverRecord == null) {
            Slog.w(TAG, "removeOppoReceiverRecord, record null.");
        } else {
            this.mReceiverRecordsArray.remove(receiverRecord.hashCode());
        }
    }

    public OppoReceiverRecord getOppoReceiverRecord(int hasCode) {
        return this.mReceiverRecordsArray.get(hasCode);
    }

    private IOppoBaseBroadcastRecord typeCastToParent(BroadcastRecord record) {
        return (IOppoBaseBroadcastRecord) ColorTypeCastingHelper.typeCasting(IOppoBaseBroadcastRecord.class, record);
    }

    public void setMessageDelayFlagForBroadcastRecord(BroadcastRecord record, boolean flagValue) {
        if (record == null) {
            Slog.w(TAG, "setMessageDelayFlagForBroadcastRecord, record null.");
            return;
        }
        IOppoBaseBroadcastRecord recordInterface = typeCastToParent(record);
        if (recordInterface == null) {
            Slog.w(TAG, "setMessageDelayFlagForBroadcastRecord typeCastToParent failed:" + record);
            return;
        }
        recordInterface.setMessageDelayFlag(flagValue);
    }

    public boolean getMessageDelayFlagOfBroadcastRecord(BroadcastRecord record) {
        if (record == null) {
            Slog.w(TAG, "getMessageDelayFlagOfBroadcastRecord, record null.");
            return false;
        }
        IOppoBaseBroadcastRecord recordInterface = typeCastToParent(record);
        if (recordInterface != null) {
            return recordInterface.getMessageDelayFlag();
        }
        Slog.w(TAG, "getMessageDelayFlagOfBroadcastRecord typeCastToParent failed:" + record);
        return false;
    }

    public OppoReceiverRecord getOppoReceiverRecord(BroadcastRecord record) {
        if (record == null) {
            Slog.w(TAG, "getOppoReceiverRecord, record null.");
            return null;
        }
        IOppoBaseBroadcastRecord recordInterface = typeCastToParent(record);
        if (recordInterface != null) {
            return recordInterface.getOppoReceiverRecord();
        }
        Slog.w(TAG, "getOppoReceiverRecord typeCastToParent failed:" + record);
        return null;
    }

    public void setOppoReceiverRecord(BroadcastRecord record, OppoReceiverRecord oppoRecord) {
        if (record == null) {
            Slog.w(TAG, "setOppoReceiverRecord, record null.");
            return;
        }
        IOppoBaseBroadcastRecord recordInterface = typeCastToParent(record);
        if (recordInterface == null) {
            Slog.w(TAG, "setOppoReceiverRecord typeCastToParent failed:" + record);
            return;
        }
        recordInterface.setOppoReceiverRecord(oppoRecord);
    }
}
