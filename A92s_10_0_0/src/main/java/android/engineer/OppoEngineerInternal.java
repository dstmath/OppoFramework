package android.engineer;

import android.content.Intent;
import android.content.pm.ActivityInfo;

public abstract class OppoEngineerInternal {
    public abstract boolean handleStartActivity(ActivityInfo activityInfo, String str, int i, int i2);

    public abstract boolean handleStartServiceOrBindService(Intent intent);
}
