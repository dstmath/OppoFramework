package com.android.server.pm;

import android.content.Context;
import com.android.server.SystemService;

public class CrossProfileAppsService extends SystemService {
    private CrossProfileAppsServiceImpl mServiceImpl;

    public CrossProfileAppsService(Context context) {
        super(context);
        this.mServiceImpl = new CrossProfileAppsServiceImpl(context);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.pm.CrossProfileAppsServiceImpl, android.os.IBinder] */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("crossprofileapps", this.mServiceImpl);
    }
}
