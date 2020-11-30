package android.secrecy;

import android.content.pm.ActivityInfo;

public abstract class SecrecyManagerInternal {
    public abstract boolean getSecrecyState(int i);

    public abstract boolean isInEncryptedAppList(ActivityInfo activityInfo, String str, int i, int i2);
}
