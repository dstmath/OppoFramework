package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.os.Looper;
import com.android.server.AlarmManagerService;

public interface IColorAlarmAlignment extends IOppoCommonFeature {
    public static final IColorAlarmAlignment DEFAULT = new IColorAlarmAlignment() {
        /* class com.android.server.IColorAlarmAlignment.AnonymousClass1 */
    };
    public static final String NAME = "IColorAlarmAlignment";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAlarmAlignment;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void initArgs(Context context, Object lock, AlarmManagerService alarmMS, Looper loop) {
    }

    default void cancelAlignTickEvent() {
    }

    default void scheduleAlignFirstDelayEvent(long delay) {
    }

    default void onScreenOn() {
    }

    default void onScreenOff() {
    }

    default boolean isAlignTick(int flags) {
        return false;
    }

    default void reScheduleAlignTick() {
    }

    default void alignWithSys(AlarmManagerService.Alarm a) {
    }

    default boolean isNeedRebatch() {
        return false;
    }

    default void setNeedRebatch(boolean isNeed) {
    }
}
