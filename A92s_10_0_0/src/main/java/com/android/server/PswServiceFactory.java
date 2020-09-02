package com.android.server;

import android.common.OppoFeatureList;
import android.content.Context;
import android.util.Slog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.IPswActivityManagerServiceEx;
import com.android.server.am.PswDummyActivityManagerServiceEx;

public class PswServiceFactory extends OppoCommonServiceFactory {
    private static final String CLASSNAME = "com.android.server.PswServiceFactoryImpl";
    private static final String TAG = "PswServiceFactory";
    private static PswServiceFactory sInstance;

    public static PswServiceFactory getInstance() {
        if (sInstance == null) {
            synchronized (PswServiceFactory.class) {
                try {
                    sInstance = (PswServiceFactory) newInstance(CLASSNAME);
                } catch (Exception e) {
                    Slog.e(TAG, " Reflect exception getInstance: " + e.toString());
                    sInstance = new PswServiceFactory();
                }
            }
        }
        return sInstance;
    }

    public boolean isValid(int index) {
        return index < OppoFeatureList.OppoIndex.EndPswServiceFactory.ordinal() && index > OppoFeatureList.OppoIndex.StartPswServiceFactory.ordinal();
    }

    public IPswActivityManagerServiceEx getPswActivityManagerServiceEx(Context context, ActivityManagerService ams) {
        warn("getPswActivityManagerServiceEx dummy");
        return new PswDummyActivityManagerServiceEx(context, ams);
    }
}
