package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.ComponentName;
import android.content.Context;
import android.os.Looper;
import java.io.PrintWriter;

public interface IColorAlarmManagerHelper extends IOppoCommonFeature {
    public static final long ALIGN_INTERVAL = 5;
    public static final IColorAlarmManagerHelper DEFAULT = new IColorAlarmManagerHelper() {
        /* class com.android.server.IColorAlarmManagerHelper.AnonymousClass1 */
    };
    public static final String NAME = "IColorAlarmManagerHelper";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAlarmManagerHelper;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(Context context, AlarmManagerService alarm) {
    }

    default void init(Context context, AlarmManagerService alarm, Looper looper) {
    }

    default long setInexactAlarm(long windowLength) {
        return windowLength;
    }

    default boolean inPackageNameWhiteList(String pkgName) {
        return false;
    }

    default boolean isFilterRemovePackage(String pkg) {
        return false;
    }

    default void dump(PrintWriter pw) {
    }

    default boolean isInAlignWhiteList(String pkgName) {
        return false;
    }

    default boolean isInAlignEnforcedWhiteList(String pkgName) {
        return false;
    }

    default boolean containKeyWord(String pkgName) {
        return false;
    }

    default long getAlignInterval() {
        return 5;
    }

    default long getAlignFirstDelay() {
        return 0;
    }

    default boolean isMatchDeepSleepRule(String pkg, String tag, int netStatus) {
        return false;
    }

    default boolean isMatchDeepSleepRule(ComponentName component) {
        return false;
    }

    default void processDied(String processName) {
    }

    default void removeAlarmLocked(int uid) {
    }

    default int getPendingJobCount() {
        return -1;
    }

    default boolean isBlackJobList(String pkgName, String shortName) {
        return false;
    }
}
