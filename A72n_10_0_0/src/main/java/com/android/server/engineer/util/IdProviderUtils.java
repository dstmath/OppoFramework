package com.android.server.engineer.util;

import android.content.Context;
import java.lang.reflect.Method;

public class IdProviderUtils {
    public static String getOppoID(Context context) {
        Object result;
        if (context == null) {
            return "";
        }
        try {
            Class<?> tClass = Class.forName("com.android.id.impl.IdProviderImpl");
            Object tIdProivderImpl = tClass.newInstance();
            Method tGetUDID = tClass.getMethod("getGUID", Context.class);
            if (tIdProivderImpl == null || tGetUDID == null || (result = tGetUDID.invoke(tIdProivderImpl, context)) == null) {
                return "";
            }
            return (String) result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
