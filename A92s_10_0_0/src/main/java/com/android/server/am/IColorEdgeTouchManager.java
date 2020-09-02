package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import java.util.List;
import java.util.Map;

public interface IColorEdgeTouchManager extends IOppoCommonFeature {
    public static final IColorEdgeTouchManager DEFAULT = new IColorEdgeTouchManager() {
        /* class com.android.server.am.IColorEdgeTouchManager.AnonymousClass1 */
    };
    public static final String NAME = "ColorEdgeTouchManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorEdgeTouchManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(ActivityManagerService ams) {
    }

    default boolean isSupportEdgeTouchPrevent() {
        return false;
    }

    default boolean writeEdgeTouchPreventParam(String callPkg, String scenePkg, List<String> list) {
        return false;
    }

    default void setDefaultEdgeTouchPreventParam(String callPkg, List<String> list) {
    }

    default boolean resetDefaultEdgeTouchPreventParam(String callPkg) {
        return false;
    }

    default void setCallRules(String callPkg, Map<String, List<String>> map) {
    }

    default void updateRotation(int rotation) {
    }
}
