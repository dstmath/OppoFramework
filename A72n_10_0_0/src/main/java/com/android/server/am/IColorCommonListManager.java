package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import java.util.ArrayList;
import java.util.List;

public interface IColorCommonListManager extends IOppoCommonFeature {
    public static final IColorCommonListManager DEFAULT = new IColorCommonListManager() {
        /* class com.android.server.am.IColorCommonListManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorCommonListManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorCommonListManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default void putAppInfo(String pkgName, int uid, String config) {
    }

    default void removeAppInfo(String pkgName, int uid, String config) {
    }

    default void updateAppInfo(String action, int uid, String pkgName, boolean isBind) {
    }

    default void updateAppInfo(int callingUid, String action, int uid, String pkgName, boolean isBind) {
    }

    default void updateWindowState(int uid, int pid, int windowId, int windowType, boolean isVisible, boolean shown) {
    }

    default List<String> getAppInfo(String config, int uid) {
        return new ArrayList<>();
    }

    default String getDefaultAppInfo(String config) {
        return "";
    }
}
