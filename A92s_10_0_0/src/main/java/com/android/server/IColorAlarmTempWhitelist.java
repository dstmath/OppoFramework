package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import com.android.server.AlarmManagerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface IColorAlarmTempWhitelist extends IOppoCommonFeature {
    public static final IColorAlarmTempWhitelist DEFAULT = new IColorAlarmTempWhitelist() {
        /* class com.android.server.IColorAlarmTempWhitelist.AnonymousClass1 */
    };
    public static final String NAME = "IColorAlarmTempWhitelist";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAlarmTempWhitelist;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean addRestrictTempWhitelist(String pkgName) {
        return false;
    }

    default boolean removeRestrictTempWhitelist(String pkgName) {
        return false;
    }

    default void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    default boolean isAlarmTempWhitelist(PendingIntent operation, String callingPackage, int flags, AlarmManager.AlarmClockInfo alarmClock) {
        return false;
    }

    default boolean isAlarmTempWhitelist(AlarmManagerService.Alarm a, boolean interactive) {
        return false;
    }
}
