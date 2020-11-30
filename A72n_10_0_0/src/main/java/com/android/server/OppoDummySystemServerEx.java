package com.android.server;

import android.content.Context;

public class OppoDummySystemServerEx implements IOppoSystemServerEx {
    protected final String TAG = getClass().getSimpleName();
    protected final Context mSystemContext;
    protected final SystemServiceManager mSystemServiceManager;

    public OppoDummySystemServerEx(Context context) {
        this.mSystemContext = context;
        this.mSystemServiceManager = (SystemServiceManager) LocalServices.getService(SystemServiceManager.class);
    }

    @Override // com.android.server.IOppoSystemServerEx
    public void startBootstrapServices() {
    }

    @Override // com.android.server.IOppoSystemServerEx
    public void startCoreServices() {
    }

    @Override // com.android.server.IOppoSystemServerEx
    public void startOtherServices() {
    }

    @Override // com.android.server.IOppoSystemServerEx
    public void systemReady() {
    }

    @Override // com.android.server.IOppoSystemServerEx
    public void systemRunning() {
    }
}
