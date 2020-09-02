package com.android.server.notification;

import android.os.Message;
import com.android.server.IOppoCommonManagerServiceEx;

public interface IOppoNotificationManagerServiceEx extends IOppoCommonManagerServiceEx {
    NotificationManagerService getNotificationManagerService();

    void handleMessage(Message message, int i);
}
