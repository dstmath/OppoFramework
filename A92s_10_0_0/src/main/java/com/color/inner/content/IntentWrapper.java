package com.color.inner.content;

import android.content.Intent;
import android.content.OppoBaseIntent;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class IntentWrapper {
    public static final String ACTION_CALL_PRIVILEGED = "android.intent.action.CALL_PRIVILEGED";
    public static final String ACTION_MEDIA_SCANNER_SCAN_ALL = "oppo.intent.action.MEDIA_SCAN_ALL";
    public static final String ACTION_PRE_MEDIA_SHARED = "android.intent.action.MEDIA_PRE_SHARED";
    public static final String ACTION_SKIN_CHANGED = "oppo.intent.action.SKIN_CHANGED";
    public static final int FLAG_RECEIVER_INCLUDE_BACKGROUND = 16777216;
    public static final int OPPO_FLAG_MUTIL_APP = 1024;
    public static final int OPPO_FLAG_MUTIL_CHOOSER = 512;
    private static final String TAG = "IntentWrapper";

    private IntentWrapper() {
    }

    public static int getColorUserId(Intent o) {
        try {
            OppoBaseIntent baseIntent = typeCasting(o);
            if (baseIntent != null) {
                return baseIntent.getOppoUserId();
            }
            return -1;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void setColorUserId(Intent o, int colorUserId) {
        try {
            OppoBaseIntent baseIntent = typeCasting(o);
            if (baseIntent != null) {
                baseIntent.setOppoUserId(colorUserId);
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static int getIsFromGameSpace(Intent o) {
        try {
            OppoBaseIntent baseIntent = typeCasting(o);
            if (baseIntent != null) {
                return baseIntent.getIsFromGameSpace();
            }
            return -1;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static void setIsFromGameSpace(Intent o, int isFromGameSpace) {
        try {
            OppoBaseIntent baseIntent = typeCasting(o);
            if (baseIntent != null) {
                baseIntent.setIsFromGameSpace(isFromGameSpace);
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void setOppoFlags(Intent o, int flag) {
        try {
            OppoBaseIntent baseIntent = typeCasting(o);
            if (baseIntent != null) {
                baseIntent.setOppoFlags(flag);
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static int getOppoFlags(Intent o) {
        try {
            OppoBaseIntent baseIntent = typeCasting(o);
            if (baseIntent != null) {
                return baseIntent.getOppoFlags();
            }
            return -1;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int getCallingUid(Intent o) {
        try {
            OppoBaseIntent baseIntent = typeCasting(o);
            if (baseIntent != null) {
                return baseIntent.getCallingUid();
            }
            return -1;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    private static OppoBaseIntent typeCasting(Intent i) {
        return (OppoBaseIntent) ColorTypeCastingHelper.typeCasting(OppoBaseIntent.class, i);
    }
}
