package com.android.server.am;

import android.common.OppoFeatureList;

public interface IColorActivityManagerServiceEx extends IOppoActivityManagerServiceEx {
    public static final IColorActivityManagerServiceEx DEFAULT = new IColorActivityManagerServiceEx() {
        /* class com.android.server.am.IColorActivityManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IColorActivityManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorActivityManagerServiceEx;
    }

    default IColorActivityManagerServiceEx getDefault() {
        return DEFAULT;
    }

    default IColorBroadcastQueueEx getColorBroadcastQueueEx(BroadcastQueue queue) {
        return null;
    }

    default IColorActivityManagerServiceInner getColorActivityManagerServiceInner() {
        return null;
    }

    default int getProcPid(int pid) {
        return 0;
    }

    default void putProcInfoArray(int pid, int uid) {
    }

    default void deleteProcInfoArray(int pid) {
    }

    default ProcessRecord getProcessRecordLocked(String processName, int uid, boolean keepIfLarge) {
        return null;
    }
}
