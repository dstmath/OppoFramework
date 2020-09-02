package com.android.server.notification;

import android.service.notification.NotificationListenerService;

public abstract class ColorNotificationListenerService extends NotificationListenerService {
    public static final int REASON_COLOROS_BPMSUSPEND = 10021;
    public static final int REASON_COLOROS_FORCESTOP = 10020;
}
