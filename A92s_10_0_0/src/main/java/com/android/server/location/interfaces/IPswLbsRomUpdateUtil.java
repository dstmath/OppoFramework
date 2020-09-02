package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import java.util.ArrayList;

public interface IPswLbsRomUpdateUtil extends IOppoCommonFeature {
    public static final IPswLbsRomUpdateUtil DEFAULT = new IPswLbsRomUpdateUtil() {
        /* class com.android.server.location.interfaces.IPswLbsRomUpdateUtil.AnonymousClass1 */
    };
    public static final String Name = "IPswLbsRomUpdateUtil";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswLbsRomUpdateUtil;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default ArrayList<String> getStringArray(String key) {
        return null;
    }

    default ArrayList<ArrayList<String>> getMatchStringArray(String key) {
        return null;
    }

    default ArrayList<Integer> getIntegerArray(String key) {
        return null;
    }

    default String getString(String key) {
        return null;
    }

    default int getInt(String key) {
        return -1;
    }

    default float getFloat(String key) {
        return -1.0f;
    }

    default boolean getBoolean(String key) {
        return false;
    }
}
