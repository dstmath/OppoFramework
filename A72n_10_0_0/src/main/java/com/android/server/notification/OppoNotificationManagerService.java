package com.android.server.notification;

import android.app.Notification;
import android.content.Context;
import android.util.Slog;
import com.android.server.notification.ManagedServices;
import com.color.util.ColorTypeCastingHelper;

public class OppoNotificationManagerService extends NotificationManagerService {
    static final String TAG = "OppoNotificationManagerService";
    private OppoBaseNotificationManagerService mObnms = ((OppoBaseNotificationManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseNotificationManagerService.class, this));

    public OppoNotificationManagerService(Context context) {
        super(context);
        OppoBaseNotificationManagerService oppoBaseNotificationManagerService = this.mObnms;
        if (oppoBaseNotificationManagerService != null) {
            oppoBaseNotificationManagerService.mColorNmsInner = oppoBaseNotificationManagerService.createColorNotificationManagerServiceInner();
        }
    }

    @Override // com.android.server.SystemService, com.android.server.notification.NotificationManagerService
    public void onStart() {
        OppoBaseNotificationManagerService oppoBaseNotificationManagerService = this.mObnms;
        if (!(oppoBaseNotificationManagerService == null || oppoBaseNotificationManagerService.mColorNmsEx == null)) {
            this.mObnms.mColorNmsEx.onStart();
        }
        super.onStart();
    }

    @Override // com.android.server.SystemService, com.android.server.notification.NotificationManagerService
    public void onBootPhase(int phase) {
        super.onBootPhase(phase);
        OppoBaseNotificationManagerService oppoBaseNotificationManagerService = this.mObnms;
        if (oppoBaseNotificationManagerService != null && oppoBaseNotificationManagerService.mColorNmsEx != null) {
            this.mObnms.mColorNmsEx.onBootPhase(phase);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.notification.NotificationManagerService
    public void enqueueNotificationInternal(String oriPkg, String oriOpPkg, int oriCallingUid, int callingPid, String tag, int id, Notification notification, int incomingUserId) {
        OppoBaseNotificationManagerService oppoBaseNotificationManagerService = this.mObnms;
        if (oppoBaseNotificationManagerService != null && oppoBaseNotificationManagerService.mColorNmsEx != null) {
            this.mObnms.mColorNmsEx.enqueueNotificationInternal(oriPkg, oriOpPkg, oriCallingUid, callingPid, tag, id, notification, incomingUserId);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.notification.NotificationManagerService
    public void cancelAllLocked(int callingUid, int callingPid, int userId, int reason, ManagedServices.ManagedServiceInfo listener, boolean includeCurrentProfiles) {
        OppoBaseNotificationManagerService oppoBaseNotificationManagerService = this.mObnms;
        if (OppoBaseNotificationManagerService.DEBUG) {
            Slog.v(TAG, "Notification--cancelAllLocked: callingUid=" + callingUid + ",callingPid:" + callingPid + ",userId=" + userId + ",includeCurrentProfiles:" + includeCurrentProfiles);
        }
        super.cancelAllLocked(callingUid, callingPid, userId, reason, listener, includeCurrentProfiles);
    }
}
