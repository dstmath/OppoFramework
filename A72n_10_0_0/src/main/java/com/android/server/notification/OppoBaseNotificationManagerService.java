package com.android.server.notification;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.SystemService;

public abstract class OppoBaseNotificationManagerService extends SystemService {
    protected static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    protected static final int DELAY_FOR_POST = 200;
    private static final String TAG = "OppoBaseNotificationManagerService";
    protected IColorNotificationManagerServiceEx mColorNmsEx;
    protected IColorNotificationManagerServiceInner mColorNmsInner;

    public OppoBaseNotificationManagerService(Context context) {
        super(context);
    }

    public IColorNotificationManagerServiceInner createColorNotificationManagerServiceInner() {
        return null;
    }
}
