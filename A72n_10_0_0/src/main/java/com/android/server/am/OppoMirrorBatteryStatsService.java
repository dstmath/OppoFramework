package com.android.server.am;

import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefObject;

public class OppoMirrorBatteryStatsService {
    public static Class<?> TYPE = RefClass.load(OppoMirrorBatteryStatsService.class, BatteryStatsService.class);
    public static RefObject<BatteryExternalStatsWorker> mWorker;
}
