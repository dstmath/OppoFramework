package com.android.server.appwidget;

import android.content.Context;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.SystemService;
import com.android.server.am.OppoProcessManager;

public class AppWidgetService extends SystemService {
    private final AppWidgetServiceImpl mImpl;

    public AppWidgetService(Context context) {
        super(context);
        this.mImpl = new AppWidgetServiceImpl(context);
    }

    public void onStart() {
        publishBinderService(OppoProcessManager.RESUME_REASON_APPWIDGET_CHANGE_STR, this.mImpl);
        AppWidgetBackupBridge.register(this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 600) {
            this.mImpl.setSafeMode(isSafeMode());
        }
    }

    public void onUnlockUser(int userHandle) {
        this.mImpl.onUserUnlocked(userHandle);
    }

    public void onStopUser(int userHandle) {
        this.mImpl.onUserStopped(userHandle);
    }

    public void onSwitchUser(int userHandle) {
        this.mImpl.reloadWidgetsMaskedStateForGroup(userHandle);
    }
}
