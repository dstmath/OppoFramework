package org.ifaa.android.manager;

import android.content.Context;

public abstract class IFAAManager {
    public static final int AUTH_TYPE_FINGERPRINT = 1;
    public static final int AUTH_TYPE_IRIS = 2;
    public static final int AUTH_TYPE_NOT_SUPPORT = 0;
    public static final int COMMAND_FAIL = -1;
    public static final int COMMAND_OK = 0;

    public abstract String getDeviceModel();

    public abstract int getSupportBIOTypes(Context context);

    public abstract int getVersion();

    public native byte[] processCmd(Context context, byte[] bArr);

    public abstract int startBIOManager(Context context, int i);
}
