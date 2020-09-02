package com.android.server;

import android.app.PendingIntent;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseLongArray;
import com.android.server.AlarmManagerService;
import java.util.ArrayList;

public interface IPswAlarmManagerFeature extends IOppoCommonFeature {
    public static final IPswAlarmManagerFeature DEFAULT = new IPswAlarmManagerFeature() {
        /* class com.android.server.IPswAlarmManagerFeature.AnonymousClass1 */
    };
    public static final String NAME = "IPswAlarmManagerFeature";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswAlarmManagerFeature;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void setPswAlarmManagerCallback(IPswAlarmManagerCallback callback) {
        Log.d(NAME, "default setPswAlarmManagerCallback");
    }

    default void setMainVariables(ArrayList<AlarmManagerService.Alarm> arrayList, ArrayList<AlarmManagerService.Batch> arrayList2, SparseLongArray lastAllowWhileIdleDispatch, SparseArray<ArrayList<AlarmManagerService.Alarm>> sparseArray, AlarmManagerService.Alarm pendingIdleUntil, AlarmManagerService.Alarm nextWakeFromIdle, long startCurrentDelayTime, long totalDelayTime, long maxDelayTime) {
        Log.d(NAME, "default setMainVariables");
    }

    default boolean isPswAlarmManagerSupport() {
        Log.d(NAME, "default isPswAlarmManagerSupport");
        return false;
    }

    default void clearTriggerListNonWakeup() {
        Log.d(NAME, "default clearTriggerListNonWakeup");
    }

    default int convertType(int type, PendingIntent operation, String packageName) {
        Log.d(NAME, "default convertType");
        return type;
    }

    default int getPendingImportantNonWakeupAlarmsSize() {
        Log.d(NAME, "default getPendingImportantNonWakeupAlarmsSize");
        return 0;
    }

    default boolean interactiveStateChangedLocked(boolean interactive, ArrayList<AlarmManagerService.Alarm> arrayList, long mNonInteractiveStartTime, long mTotalDelayTime, long mMaxDelayTime, PendingIntent mTimeTickSender, long mNonInteractiveTime) {
        Log.d(NAME, "default interactiveStateChangedLocked");
        return false;
    }

    default void netStateChangedLocked(boolean b) {
        Log.d(NAME, "default netStateChangedLocked");
    }

    default boolean pswAlarmTrigger(ArrayList<AlarmManagerService.Alarm> arrayList) {
        Log.d(NAME, "default pswAlarmTrigger");
        return false;
    }
}
