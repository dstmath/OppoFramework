package com.android.server.notification;

import android.content.Context;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import com.android.server.OppoDummyCommonManagerServiceEx;

public class OppoDummyNotificationManagerServiceEx extends OppoDummyCommonManagerServiceEx implements IOppoNotificationManagerServiceEx {
    protected static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    protected NotificationManagerService mNotificationManagerService;

    public OppoDummyNotificationManagerServiceEx(Context context, NotificationManagerService nms) {
        super(context);
        this.mNotificationManagerService = nms;
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void onStart() {
        super.onStart();
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void systemReady() {
        super.systemReady();
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return super.onTransact(code, data, reply, flags);
    }

    @Override // com.android.server.notification.IOppoNotificationManagerServiceEx
    public void handleMessage(Message msg, int whichHandler) {
    }

    @Override // com.android.server.notification.IOppoNotificationManagerServiceEx
    public NotificationManagerService getNotificationManagerService() {
        return this.mNotificationManagerService;
    }
}
