package com.android.server.appwidget;

import android.content.Context;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.SystemService;

public class AppWidgetService extends SystemService {
    private final AppWidgetServiceImpl mImpl;

    public AppWidgetService(Context context) {
        super(context);
        this.mImpl = new AppWidgetServiceImpl(context);
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.appwidget.AppWidgetServiceImpl, android.os.IBinder] */
    @Override // com.android.server.SystemService
    public void onStart() {
        this.mImpl.onStart();
        publishBinderService("appwidget", this.mImpl);
        AppWidgetBackupBridge.register(this.mImpl);
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 550) {
            this.mImpl.setSafeMode(isSafeMode());
        }
    }

    @Override // com.android.server.SystemService
    public void onStopUser(int userHandle) {
        this.mImpl.onUserStopped(userHandle);
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int userHandle) {
        this.mImpl.reloadWidgetsMaskedStateForGroup(userHandle);
    }
}
