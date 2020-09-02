package oppo.util;

import android.util.Log;

public final class OppoCommonConstants {
    private static final int COLOR_CALL_TRANSACTION_INDEX = 10000;
    public static final int COLOR_FIRST_CALL_TRANSACTION = 10001;
    public static final int COLOR_FIRST_MESSAGE = 1001;
    public static final int COLOR_GROUP = 1;
    public static final int COLOR_LAST_CALL_TRANSACTION = 20000;
    public static final int COLOR_LAST_MESSAGE = 2000;
    public static final int COLOR_LAYOUT_IN_DISPLAY_CUTOUT_MODE_FORCE = 3;
    private static final int COLOR_MESSAGE_INDEX = 1000;
    private static final int OPPO_CALL_TRANSACTION_INDEX = 10000;
    private static final int OPPO_FIRST_CALL_TRANSACTION = 1;
    private static final int OPPO_FIRST_MESSAGE = 1;
    private static final int OPPO_MESSAGE_INDEX = 1000;
    private static final int PSW_CALL_TRANSACTION_INDEX = 20000;
    public static final int PSW_FIRST_CALL_TRANSACTION = 20001;
    public static final int PSW_FIRST_MESSAGE = 2001;
    public static final int PSW_GROUP = 2;
    public static final int PSW_LAST_CALL_TRANSACTION = 30000;
    public static final int PSW_LAST_MESSAGE = 3000;
    private static final int PSW_MESSAGE_INDEX = 2000;
    private static final String TAG = "OppoCommonConstants";
    public static final int TYPE_BINDER = 1;
    public static final int TYPE_MESSAGE = 2;

    public static boolean checkCodeValid(int code, int type, int group) {
        if (type == 1) {
            return checkBinderCodeValid(code, group);
        }
        if (type == 2) {
            return checkMessageCodeValie(code, group);
        }
        Log.i(TAG, "UNKNOW type = " + type);
        return false;
    }

    private static boolean checkBinderCodeValid(int code, int group) {
        if (group == 1) {
            return inside(code, 10001, 20000);
        }
        if (group == 2) {
            return inside(code, 20001, 30000);
        }
        Log.i(TAG, "UNKNOW group = " + group);
        return false;
    }

    private static boolean checkMessageCodeValie(int code, int group) {
        if (group == 1) {
            return inside(code, 1001, 2000);
        }
        if (group == 2) {
            return inside(code, 2001, PSW_LAST_MESSAGE);
        }
        Log.i(TAG, "Uknow group = " + group);
        return false;
    }

    private static boolean inside(int code, int first, int last) {
        return code >= first && code <= last;
    }
}
