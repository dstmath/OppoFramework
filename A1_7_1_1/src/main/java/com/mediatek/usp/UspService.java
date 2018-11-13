package com.mediatek.usp;

import android.content.Context;
import android.util.Log;
import com.android.server.SystemService;

public class UspService extends SystemService {
    private final boolean DEBUG = true;
    private final String TAG = "UspService";
    final UspServiceImpl mImpl;

    public UspService(Context context) {
        super(context);
        this.mImpl = new UspServiceImpl(context);
        Log.i("UspService", "UspServiceImpl" + this.mImpl.toString());
    }

    public void onStart() {
        Log.i("UspService", "Registering service uniservice-pack");
        publishBinderService("uniservice-pack", this.mImpl);
        this.mImpl.start();
    }

    public void onBootPhase(int phase) {
        Log.i("UspService", "phase " + phase);
        if (phase == 1000) {
            Log.i("UspService", "Boot completed: unfreezed");
            this.mImpl.unfreezeScreen();
        }
    }
}
