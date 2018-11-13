package com.android.server.appwidget;

import android.content.Context;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.FgThread;
import com.android.server.SystemService;
import com.android.server.am.OppoProcessManager;

public class AppWidgetService extends SystemService {
    private final AppWidgetServiceImpl mImpl;

    public AppWidgetService(Context context) {
        super(context);
        this.mImpl = new AppWidgetServiceImpl(context);
    }

    public void onStart() {
        this.mImpl.onStart();
        publishBinderService(OppoProcessManager.RESUME_REASON_APPWIDGET_CHANGE_STR, this.mImpl);
        AppWidgetBackupBridge.register(this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
            this.mImpl.setSafeMode(isSafeMode());
        }
    }

    /* renamed from: lambda$-com_android_server_appwidget_AppWidgetService_1563 */
    /* synthetic */ void m114lambda$-com_android_server_appwidget_AppWidgetService_1563(int userHandle) {
        this.mImpl.onUserUnlocked(userHandle);
    }

    public void onUnlockUser(int userHandle) {
        FgThread.getHandler().post(new -$Lambda$sqLvMpiiaBhtxQ03rM0wbe7Vez0(userHandle, this));
    }

    public void onStopUser(int userHandle) {
        this.mImpl.onUserStopped(userHandle);
    }

    public void onSwitchUser(int userHandle) {
        this.mImpl.reloadWidgetsMaskedStateForGroup(userHandle);
    }
}
