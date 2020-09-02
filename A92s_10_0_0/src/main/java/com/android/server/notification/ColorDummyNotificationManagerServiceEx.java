package com.android.server.notification;

import android.app.Notification;
import android.content.Context;
import android.media.IRingtonePlayer;
import android.net.Uri;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import com.android.server.notification.ManagedServices;
import com.android.server.notification.NotificationManagerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ColorDummyNotificationManagerServiceEx extends OppoDummyNotificationManagerServiceEx implements IColorNotificationManagerServiceEx {
    public ColorDummyNotificationManagerServiceEx(Context context, NotificationManagerService nms) {
        super(context, nms);
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public Context getContext() {
        return this.mNotificationManagerService.getContext();
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public IColorNotificationManagerServiceInner getNotificationManagerServiceInner() {
        return null;
    }

    @Override // com.android.server.notification.IOppoNotificationManagerServiceEx, com.android.server.notification.OppoDummyNotificationManagerServiceEx
    public void handleMessage(Message msg, int whichHandler) {
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public void cancelAllNotificationsInt(String action, int callingUid, int callingPid, String pkg, String channelId, int mustHaveFlags, int mustNotHaveFlags, boolean doit, int userId, int reason, ManagedServices.ManagedServiceInfo listener) {
        this.mNotificationManagerService.cancelAllNotificationsInt(callingUid, callingPid, pkg, channelId, mustHaveFlags, mustNotHaveFlags, doit, userId, reason, listener);
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public void onBootPhase(int phase) {
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean shouldSuppressToast(String pkg) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean shouldInterceptToast(String pkg) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean shouldToast(String pkg) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public void addToast(String pkg) {
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public void enqueueNotificationInternal(String oriPkg, String oriOpPkg, int oriCallingUid, int callingPid, String tag, int id, Notification notification, int incomingUserId) {
        this.mNotificationManagerService.enqueueNotificationInternal(oriPkg, oriOpPkg, oriCallingUid, callingPid, tag, id, notification, incomingUserId);
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean shouldLimitChannels(PreferencesHelper preferencesHelper, String pkg, int uid, int channelsSize) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean isMutilAppUserid(int userid) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean shouldLimitNotification(NotificationRecord r, int callingUid, int userId, NotificationManagerService.NotificationListeners listeners, int max) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean playAsync(IRingtonePlayer player, NotificationRecord record, Uri soundUri, boolean looping) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean isNotificationForCurrentUser(NotificationRecord record, int userId) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public void removeToastQueue(String pkg) {
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public void setNavigationStatus(String pkg, String channelId, int callingUid, int callingPid, int reason) {
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean shouldKeepNotifcationWhenForceStop(String pkg, NotificationRecord r, int reason) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean isShutdown() {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean canListenNotificationChannelChange(String pkg) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean enabledAndUserMatches(StatusBarNotification sbn, ManagedServices.ManagedServiceInfo listener) {
        return listener.enabledAndUserMatches(sbn.getUserId());
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public void setShutdown(String action, boolean shutdown) {
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public void setKeepAliveAppIfNeed(String pkgName, int id, boolean isKeepAlive) {
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public void detectCancelAction(Context context, int id, String pkg, int uid) {
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean shouldSuppressEffect(NotificationRecord record) {
        return false;
    }

    @Override // com.android.server.notification.IColorNotificationManagerServiceEx
    public boolean isStowOptionKey(String key) {
        return false;
    }
}
