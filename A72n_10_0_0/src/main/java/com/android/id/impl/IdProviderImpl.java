package com.android.id.impl;

import android.app.ColorNotificationManager;
import android.app.IColorNotificationManager;
import android.content.Context;
import android.os.Binder;

public class IdProviderImpl {
    public String getOpenid(Context context, String type) {
        try {
            IColorNotificationManager icnm = new ColorNotificationManager();
            return (String) IColorNotificationManager.class.getDeclaredMethod("getOpenid", String.class, Integer.TYPE, String.class).invoke(icnm, context.getPackageName(), Integer.valueOf(Binder.getCallingUid()), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getGUID(Context context) {
        return getOpenid(context, "GUID");
    }

    public String getOUID(Context context) {
        return getOpenid(context, "OUID");
    }

    public String getDUID(Context context) {
        return getOpenid(context, "DUID");
    }

    public String getAUID(Context context) {
        return getOpenid(context, "AUID");
    }

    public String getAPID(Context context) {
        return getOpenid(context, "APID");
    }

    public boolean checkGetOpenid(Context context, String type) {
        try {
            IColorNotificationManager icnm = new ColorNotificationManager();
            return ((Boolean) IColorNotificationManager.class.getDeclaredMethod("checkGetOpenid", String.class, Integer.TYPE, String.class).invoke(icnm, context.getPackageName(), Integer.valueOf(Binder.getCallingUid()), type)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkGetGUID(Context context) {
        return checkGetOpenid(context, "GUID");
    }

    public boolean checkGetAPID(Context context) {
        return checkGetOpenid(context, "APID");
    }
}
