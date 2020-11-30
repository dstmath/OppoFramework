package com.android.server.notification;

import android.app.Notification;
import android.content.Context;
import android.media.IRingtonePlayer;
import android.net.Uri;
import android.service.notification.StatusBarNotification;
import com.android.server.notification.ManagedServices;
import com.android.server.notification.NotificationManagerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface IColorNotificationManagerServiceEx extends IOppoNotificationManagerServiceEx {
    void addToast(String str);

    boolean canListenNotificationChannelChange(String str);

    void cancelAllNotificationsInt(String str, int i, int i2, String str2, String str3, int i3, int i4, boolean z, int i5, int i6, ManagedServices.ManagedServiceInfo managedServiceInfo);

    void detectCancelAction(Context context, int i, String str, int i2);

    boolean dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    boolean enabledAndUserMatches(StatusBarNotification statusBarNotification, ManagedServices.ManagedServiceInfo managedServiceInfo);

    void enqueueNotificationInternal(String str, String str2, int i, int i2, String str3, int i3, Notification notification, int i4);

    Context getContext();

    IColorNotificationManagerServiceInner getNotificationManagerServiceInner();

    boolean isMutilAppUserid(int i);

    boolean isNotificationForCurrentUser(NotificationRecord notificationRecord, int i);

    boolean isShutdown();

    boolean isStowOptionKey(String str);

    void onBootPhase(int i);

    boolean playAsync(IRingtonePlayer iRingtonePlayer, NotificationRecord notificationRecord, Uri uri, boolean z);

    void removeToastQueue(String str);

    void setKeepAliveAppIfNeed(String str, int i, boolean z);

    void setNavigationStatus(String str, String str2, int i, int i2, int i3);

    void setShutdown(String str, boolean z);

    boolean shouldInterceptToast(String str);

    boolean shouldKeepNotifcationWhenForceStop(String str, NotificationRecord notificationRecord, int i);

    boolean shouldLimitChannels(PreferencesHelper preferencesHelper, String str, int i, int i2);

    boolean shouldLimitNotification(NotificationRecord notificationRecord, int i, int i2, NotificationManagerService.NotificationListeners notificationListeners, int i3);

    boolean shouldSuppressEffect(NotificationRecord notificationRecord);

    boolean shouldSuppressToast(String str);

    boolean shouldToast(String str);

    boolean vibrateLinearmotorIfNeed(long[] jArr, boolean z, Uri uri);
}
