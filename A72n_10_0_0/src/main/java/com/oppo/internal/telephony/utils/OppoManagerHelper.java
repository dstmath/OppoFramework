package com.oppo.internal.telephony.utils;

import android.content.Context;
import android.telephony.Rlog;
import com.android.internal.telephony.util.OemTelephonyUtils;
import java.lang.reflect.Field;
import java.util.Map;

public class OppoManagerHelper {
    private static final String TAG = "OppoManagerHelper";

    public static int writeLogToPartition(Context context, String resName, String logstring, String issue) {
        String string = OemTelephonyUtils.getOemRes(context, resName, "");
        if (string.equals("")) {
            Rlog.e(TAG, "Can not get resource of identifier for : " + resName);
            return -1;
        }
        String[] log_array = string.split(",");
        return writeLogToPartition(Integer.valueOf(log_array[0]).intValue(), logstring, issue, log_array[1]);
    }

    public static int writeLogToPartition(int type, String logstring, String issue, String desc) {
        try {
            Class<?> OppoManager = Class.forName("android.os.OppoManager");
            Field field = OppoManager.getDeclaredField("NETWORK_TAG");
            field.setAccessible(true);
            Object obj = field.get(OppoManager);
            if (obj != null) {
                return writeLogToPartition(type, logstring, obj.toString(), issue, desc);
            }
            return -1;
        } catch (Exception e) {
            Rlog.e(TAG, "OppoManager writeLogToPartition" + e.getMessage());
            return -1;
        }
    }

    public static int writeLogToPartition(int type, String logstring, String tagString, String issue, String desc) {
        try {
            Object obj = Class.forName("android.os.OppoManager").getMethod("writeLogToPartition", Integer.TYPE, String.class, String.class, String.class, String.class).invoke(null, Integer.valueOf(type), logstring, tagString, issue, desc);
            if (obj != null) {
                return ((Integer) obj).intValue();
            }
            return -1;
        } catch (Exception e) {
            Rlog.e(TAG, "OppoManager writeLogToPartition" + e.getMessage());
            return -1;
        }
    }

    public static void onStamp(String eventId, Map<String, String> logMap) {
        try {
            Class.forName("android.os.OppoManager").getMethod("onStamp", String.class, Map.class).invoke(null, eventId, logMap);
        } catch (Exception e) {
            Rlog.e(TAG, "OppoManager ClassNotFoundException" + e.getMessage());
        }
    }
}
