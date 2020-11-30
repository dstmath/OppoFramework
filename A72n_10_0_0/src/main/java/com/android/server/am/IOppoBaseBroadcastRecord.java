package com.android.server.am;

import android.util.Slog;

public interface IOppoBaseBroadcastRecord {
    public static final String ITAG = "IOppoBaseBroadcastRecord";

    default void setOppoReceiverRecord(OppoReceiverRecord record) {
        Slog.d(ITAG, "defalut setOppoReceiverRecord record:" + record);
    }

    default OppoReceiverRecord getOppoReceiverRecord() {
        Slog.d(ITAG, "defalut getOppoReceiverRecord, return null");
        return null;
    }

    default void setMessageDelayFlag(boolean delay) {
        Slog.d(ITAG, "defalut setMessageDelayFlag delay:" + delay);
    }

    default boolean getMessageDelayFlag() {
        Slog.d(ITAG, "defalut getMessageDelayFlag, return false");
        return false;
    }
}
