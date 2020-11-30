package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Slog;
import com.android.server.ServiceThread;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public interface IColorBroadcastManager extends IOppoCommonFeature {
    public static final IColorBroadcastManager DEFAULT = new IColorBroadcastManager() {
        /* class com.android.server.am.IColorBroadcastManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorBroadcastManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorBroadcastManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(ActivityManagerService ams) {
        Slog.d(NAME, "default init");
    }

    default void systemReady() {
        Slog.d(NAME, "default systemReady");
    }

    default boolean skipSpecialBroadcast(ProcessRecord app, String pkgName, Intent intent, String processName, int uid, ApplicationInfo info) {
        Slog.d(NAME, "default skipSpecialBroadcast");
        return false;
    }

    default void informReadyToBootComplete() {
        Slog.d(NAME, "default informReadyToBootComplete");
    }

    default void handleDynamicLog(boolean on) {
        Slog.d(NAME, "default handleDynamicLog");
    }

    default boolean dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage, boolean needSep) {
        Slog.d(NAME, "default dumpLocked");
        return false;
    }

    default void addBroadcastThread() {
        Slog.d(NAME, "default addBroadcastThread");
    }

    default void instanceBroadcast(BroadcastConstants foreConstants, BroadcastConstants backConstants) {
        Slog.d(NAME, "default instanceBroadcast");
    }

    default ServiceThread getBroadcastThread() {
        Slog.d(NAME, "default getBroadcastThread");
        return null;
    }

    default void instanceBroadcastThread(OppoBaseBroadcastQueue queue) {
        Slog.d(NAME, "default instanceBroadcastThread");
    }

    default void oppoScheduleReceiver(OppoBaseBroadcastQueue queue, Intent intent, BroadcastRecord r, ActivityManagerService Service, ProcessRecord app) {
        Slog.d(NAME, "default oppoScheduleReceiver:" + r + ", intent:" + intent);
    }

    default void skipCurrentReceiverLocked(OppoBaseBroadcastQueue queue, ProcessRecord app) {
        Slog.d(NAME, "default skipCurrentReceiverLocked");
    }

    default void informReadyToCheckJumpQueue() {
        Slog.d(NAME, "default informReadyToCheckJumpQueue");
    }

    default void adjustQueueIfNecessary(ArrayList<BroadcastRecord> arrayList, BroadcastRecord r) {
        Slog.d(NAME, "default adjustQueueIfNecessary");
    }

    default void adjustParallelBroadcastReceiversQueue(BroadcastRecord broadcastRecord) {
        Slog.d(NAME, "default adjustParallelBroadcastReceiversQueue");
    }

    default void adjustOrderedBroadcastReceiversQueue(BroadcastRecord broadcastRecord, int numReceivers) {
        Slog.d(NAME, "default adjustOrderedBroadcastReceiversQueue");
    }

    default boolean isPendingBroadcastProcessLocked(int pid) {
        Slog.d(NAME, "default isPendingBroadcastProcessLocked");
        return false;
    }

    default BroadcastQueue broadcastQueueForIntent(Intent intent) {
        Slog.d(NAME, "default broadcastQueueForIntent");
        return null;
    }

    default BroadcastQueue getQueueFromFlag(int flags) {
        Slog.d(NAME, "default getQueueFromFlag");
        return null;
    }

    default List adjustReceiverList(List receivers, Intent intent) {
        Slog.d(NAME, "default adjustReceiverList");
        return null;
    }

    default void finishNotOrderReceiver(IBinder who, int hasCode, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort) {
        Slog.d(NAME, "default finishNotOrderReceiver");
    }

    default OppoReceiverRecord broadcastRecordForNotOrderReceiverLocked(IBinder receiver, int hasCode) {
        Slog.d(NAME, "default broadcastRecordForNotOrderReceiverLocked");
        return null;
    }

    default boolean isReceivingBroadcastLocked(ProcessRecord app) {
        Slog.d(NAME, "default isReceivingBroadcastLocked");
        return false;
    }

    default void broadcastOppoBootComleteLocked(Intent intent, IIntentReceiver resultTo, int userId) {
        Slog.d(NAME, "default broadcastOppoBootComleteLocked");
    }

    default BroadcastQueue getOppoFgBroadcastQueue() {
        Slog.d(NAME, "default getOppoFgBroadcastQueue");
        return null;
    }

    default boolean hasOppoBroadcastManager() {
        Slog.d(NAME, "default hasOppoBroadcastManager");
        return false;
    }
}
