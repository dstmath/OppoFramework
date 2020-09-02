package com.android.server.location;

import com.android.server.location.LocationRequestStatistics;
import com.color.util.ColorTypeCastingHelper;

public class PackageStatisticsWrapper {
    public static long getLastDurationMs(LocationRequestStatistics.PackageStatistics stats) {
        OppoBasePackageStatistics baseStats = typeCasting(stats);
        if (baseStats != null) {
            return baseStats.getLastDurationMs();
        }
        return 0;
    }

    public static void setRecord(LocationRequestStatistics.PackageStatistics stats, boolean record) {
        OppoBasePackageStatistics baseStats = typeCasting(stats);
        if (baseStats != null) {
            baseStats.setRecord(record);
        }
    }

    public static boolean isRecord(LocationRequestStatistics.PackageStatistics stats) {
        OppoBasePackageStatistics baseStats = typeCasting(stats);
        if (baseStats != null) {
            return baseStats.isRecord();
        }
        return false;
    }

    public static OppoBasePackageStatistics typeCasting(LocationRequestStatistics.PackageStatistics stats) {
        return (OppoBasePackageStatistics) ColorTypeCastingHelper.typeCasting(OppoBasePackageStatistics.class, stats);
    }
}
