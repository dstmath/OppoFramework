package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.Message;

public interface IColorSecurityPermissionManager extends IOppoCommonFeature {
    public static final IColorSecurityPermissionManager DEFAULT = new IColorSecurityPermissionManager() {
        /* class com.android.server.am.IColorSecurityPermissionManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorSecurityPermissionManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorSecurityPermissionManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default boolean handleServiceTimeOut(Message msg) {
        return false;
    }

    default boolean checkPermission(String permission, int pid, int uid) {
        return false;
    }

    default boolean isWaitingPermissionChoice(ProcessRecord proc) {
        return false;
    }

    default boolean needCheckPermission() {
        return false;
    }
}
