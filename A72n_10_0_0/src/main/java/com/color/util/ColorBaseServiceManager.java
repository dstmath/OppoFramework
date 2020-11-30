package com.color.util;

import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;

public abstract class ColorBaseServiceManager {
    public static final boolean DBG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    protected final IBinder mRemote;

    /* access modifiers changed from: protected */
    public abstract void init(IBinder iBinder);

    public ColorBaseServiceManager(String name) {
        this(ServiceManager.getService(name));
    }

    public ColorBaseServiceManager(IBinder remote) {
        this.mRemote = remote;
        init(this.mRemote);
    }
}
