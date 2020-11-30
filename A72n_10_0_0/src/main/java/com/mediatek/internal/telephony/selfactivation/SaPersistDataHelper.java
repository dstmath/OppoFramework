package com.mediatek.internal.telephony.selfactivation;

import android.content.Context;
import android.content.SharedPreferences;

public class SaPersistDataHelper {
    public static final String DATA_KEY_SA_STATE = "dataKeySaState";
    private static final String DATA_NAME_PREFIX = "selfActivationData";

    public static void putIntData(Context context, int phoneId, String key, int value) {
        getSharedPreference(context, phoneId).edit().putInt(key, value).apply();
    }

    public static int getIntData(Context context, int phoneId, String key, int defaultValue) {
        return getSharedPreference(context, phoneId).getInt(key, defaultValue);
    }

    public static void putStringData(Context context, int phoneId, String key, String value) {
        getSharedPreference(context, phoneId).edit().putString(key, value).apply();
    }

    public static String getStringData(Context context, int phoneId, String key, String defaultValue) {
        return getSharedPreference(context, phoneId).getString(key, defaultValue);
    }

    private static SharedPreferences getSharedPreference(Context context, int phoneId) {
        return context.getSharedPreferences(getDataName(phoneId), 0);
    }

    public static String getDataName(int phoneId) {
        return DATA_NAME_PREFIX + phoneId;
    }

    public static String toString(Context context, int phoneId) {
        StringBuilder s = new StringBuilder();
        s.append("SaPersistDataHelper {");
        s.append(" mDataName = " + getDataName(phoneId));
        s.append(" dataKeySaState = " + getIntData(context, phoneId, DATA_KEY_SA_STATE, -1));
        s.append("}");
        return s.toString();
    }
}
