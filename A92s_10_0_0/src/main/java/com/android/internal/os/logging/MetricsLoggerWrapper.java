package com.android.internal.os.logging;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Pair;
import android.util.StatsLog;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;

public class MetricsLoggerWrapper {
    private static final int METRIC_VALUE_DISMISSED_BY_DRAG = 1;
    private static final int METRIC_VALUE_DISMISSED_BY_TAP = 0;

    public static void logPictureInPictureDismissByTap(Context context, Pair<ComponentName, Integer> topActivityInfo) {
        MetricsLogger.action(context, 822, 0);
        StatsLog.write(52, getUid(context, topActivityInfo.first, topActivityInfo.second.intValue()), topActivityInfo.first.flattenToString(), 4);
    }

    public static void logPictureInPictureDismissByDrag(Context context, Pair<ComponentName, Integer> topActivityInfo) {
        MetricsLogger.action(context, 822, 1);
        StatsLog.write(52, getUid(context, topActivityInfo.first, topActivityInfo.second.intValue()), topActivityInfo.first.flattenToString(), 4);
    }

    public static void logPictureInPictureMinimize(Context context, boolean isMinimized, Pair<ComponentName, Integer> topActivityInfo) {
        MetricsLogger.action(context, 821, isMinimized);
        StatsLog.write(52, getUid(context, topActivityInfo.first, topActivityInfo.second.intValue()), topActivityInfo.first.flattenToString(), 3);
    }

    private static int getUid(Context context, ComponentName componentName, int userId) {
        if (componentName == null) {
            return -1;
        }
        try {
            return context.getPackageManager().getApplicationInfoAsUser(componentName.getPackageName(), 0, userId).uid;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public static void logPictureInPictureMenuVisible(Context context, boolean menuStateFull) {
        MetricsLogger.visibility(context, 823, menuStateFull);
    }

    public static void logPictureInPictureEnter(Context context, int uid, String shortComponentName, boolean supportsEnterPipOnTaskSwitch) {
        MetricsLogger.action(context, (int) MetricsProto.MetricsEvent.ACTION_PICTURE_IN_PICTURE_ENTERED, supportsEnterPipOnTaskSwitch);
        StatsLog.write(52, uid, shortComponentName, 1);
    }

    public static void logPictureInPictureFullScreen(Context context, int uid, String shortComponentName) {
        MetricsLogger.action(context, (int) MetricsProto.MetricsEvent.ACTION_PICTURE_IN_PICTURE_EXPANDED_TO_FULLSCREEN);
        StatsLog.write(52, uid, shortComponentName, 2);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.util.StatsLogInternal.write(int, int, java.lang.String, boolean, int):int
     arg types: [int, int, java.lang.String, int, int]
     candidates:
      android.util.StatsLogInternal.write(int, int, int, int, int):int
      android.util.StatsLogInternal.write(int, int, int, int, byte[]):int
      android.util.StatsLogInternal.write(int, int, int, long, boolean):int
      android.util.StatsLogInternal.write(int, int, int, boolean, boolean):int
      android.util.StatsLogInternal.write(int, int, java.lang.String, int, int):int
      android.util.StatsLogInternal.write(int, int, java.lang.String, int, java.lang.String):int
      android.util.StatsLogInternal.write(int, int, java.lang.String, int, boolean):int
      android.util.StatsLogInternal.write(int, int, java.lang.String, java.lang.String, int):int
      android.util.StatsLogInternal.write(int, int, java.lang.String, java.lang.String, long):int
      android.util.StatsLogInternal.write(int, int, boolean, int, int):int
      android.util.StatsLogInternal.write(int, long, int, java.lang.String, int):int
      android.util.StatsLogInternal.write(int, long, int, java.lang.String, java.lang.String):int
      android.util.StatsLogInternal.write(int, long, long, int, boolean):int
      android.util.StatsLogInternal.write(int, java.lang.String, int, int, float):int
      android.util.StatsLogInternal.write(int, java.lang.String, long, long, boolean):int
      android.util.StatsLogInternal.write(int, java.lang.String, java.lang.String, boolean, boolean):int
      android.util.StatsLogInternal.write(int, boolean, java.lang.String, long, long):int
      android.util.StatsLogInternal.write(int, byte[], int, int, int):int
      android.util.StatsLogInternal.write(int, byte[], int, int, byte[]):int
      android.util.StatsLogInternal.write(int, int[], java.lang.String[], int, int):int
      android.util.StatsLogInternal.write(int, int[], java.lang.String[], int, long):int
      android.util.StatsLogInternal.write(int, int[], java.lang.String[], int, java.lang.String):int
      android.util.StatsLogInternal.write(int, int[], java.lang.String[], java.lang.String, int):int
      android.util.StatsLogInternal.write(int, android.os.WorkSource, int, int, java.lang.String):void
      android.util.StatsLogInternal.write(int, android.os.WorkSource, int, java.lang.String, int):void
      android.util.StatsLogInternal.write(int, android.os.WorkSource, java.lang.String, int, int):void
      android.util.StatsLogInternal.write(int, android.os.WorkSource, java.lang.String, java.lang.String, int):void
      android.util.StatsLogInternal.write(int, int, java.lang.String, boolean, int):int */
    public static void logAppOverlayEnter(int uid, String packageName, boolean changed, int type, boolean usingAlertWindow) {
        if (!changed) {
            return;
        }
        if (type != 2038) {
            StatsLog.write(59, uid, packageName, true, 1);
        } else if (!usingAlertWindow) {
            StatsLog.write(59, uid, packageName, false, 1);
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.util.StatsLogInternal.write(int, int, java.lang.String, boolean, int):int
     arg types: [int, int, java.lang.String, int, int]
     candidates:
      android.util.StatsLogInternal.write(int, int, int, int, int):int
      android.util.StatsLogInternal.write(int, int, int, int, byte[]):int
      android.util.StatsLogInternal.write(int, int, int, long, boolean):int
      android.util.StatsLogInternal.write(int, int, int, boolean, boolean):int
      android.util.StatsLogInternal.write(int, int, java.lang.String, int, int):int
      android.util.StatsLogInternal.write(int, int, java.lang.String, int, java.lang.String):int
      android.util.StatsLogInternal.write(int, int, java.lang.String, int, boolean):int
      android.util.StatsLogInternal.write(int, int, java.lang.String, java.lang.String, int):int
      android.util.StatsLogInternal.write(int, int, java.lang.String, java.lang.String, long):int
      android.util.StatsLogInternal.write(int, int, boolean, int, int):int
      android.util.StatsLogInternal.write(int, long, int, java.lang.String, int):int
      android.util.StatsLogInternal.write(int, long, int, java.lang.String, java.lang.String):int
      android.util.StatsLogInternal.write(int, long, long, int, boolean):int
      android.util.StatsLogInternal.write(int, java.lang.String, int, int, float):int
      android.util.StatsLogInternal.write(int, java.lang.String, long, long, boolean):int
      android.util.StatsLogInternal.write(int, java.lang.String, java.lang.String, boolean, boolean):int
      android.util.StatsLogInternal.write(int, boolean, java.lang.String, long, long):int
      android.util.StatsLogInternal.write(int, byte[], int, int, int):int
      android.util.StatsLogInternal.write(int, byte[], int, int, byte[]):int
      android.util.StatsLogInternal.write(int, int[], java.lang.String[], int, int):int
      android.util.StatsLogInternal.write(int, int[], java.lang.String[], int, long):int
      android.util.StatsLogInternal.write(int, int[], java.lang.String[], int, java.lang.String):int
      android.util.StatsLogInternal.write(int, int[], java.lang.String[], java.lang.String, int):int
      android.util.StatsLogInternal.write(int, android.os.WorkSource, int, int, java.lang.String):void
      android.util.StatsLogInternal.write(int, android.os.WorkSource, int, java.lang.String, int):void
      android.util.StatsLogInternal.write(int, android.os.WorkSource, java.lang.String, int, int):void
      android.util.StatsLogInternal.write(int, android.os.WorkSource, java.lang.String, java.lang.String, int):void
      android.util.StatsLogInternal.write(int, int, java.lang.String, boolean, int):int */
    public static void logAppOverlayExit(int uid, String packageName, boolean changed, int type, boolean usingAlertWindow) {
        if (!changed) {
            return;
        }
        if (type != 2038) {
            StatsLog.write(59, uid, packageName, true, 2);
        } else if (!usingAlertWindow) {
            StatsLog.write(59, uid, packageName, false, 2);
        }
    }
}
