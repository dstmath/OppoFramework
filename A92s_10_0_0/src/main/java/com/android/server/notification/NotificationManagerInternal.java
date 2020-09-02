package com.android.server.notification;

import android.annotation.OppoHook;
import android.app.Notification;
import android.app.NotificationChannel;

public interface NotificationManagerInternal {
    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "baoqibiao@ROM.SysApp.Notification, add for don't clear notification when bpm suspend", property = OppoHook.OppoRomType.ROM)
    void cancelAllNotificationsFromBMP(String str, int i);

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoHook.OppoRomType.ROM)
    boolean checkProcessToast(int i);

    void enqueueNotification(String str, String str2, int i, int i2, String str3, int i3, Notification notification, int i4);

    NotificationChannel getNotificationChannel(String str, int i, String str2);

    void removeForegroundServiceFlagFromNotification(String str, int i, int i2);
}
