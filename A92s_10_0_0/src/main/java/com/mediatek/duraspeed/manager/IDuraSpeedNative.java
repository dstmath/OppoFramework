package com.mediatek.duraspeed.manager;

import android.content.Context;
import android.content.Intent;
import com.android.server.wm.ActivityRecord;

public interface IDuraSpeedNative {
    boolean isDuraSpeedEnabled();

    void onActivityIdle(Context context, Intent intent);

    void onAppProcessDied(String str, String str2);

    void onBeforeActivitySwitch(ActivityRecord activityRecord, ActivityRecord activityRecord2, boolean z, int i);

    void onSystemReady();

    void onWakefulnessChanged(int i);

    void startDuraSpeedService(Context context);

    void triggerMemory(int i, int i2);
}
