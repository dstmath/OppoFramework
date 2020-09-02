package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;

public interface IColorSecurePayManager extends IOppoCommonFeature {
    public static final IColorSecurePayManager DEFAULT = new IColorSecurePayManager() {
        /* class com.android.server.pm.IColorSecurePayManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorSecurePayManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorSecurePayManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean isSecurePayApp(String pkgName) {
        return false;
    }

    default void initSecurePay(Context context) {
    }

    default boolean isSystemAppCall() {
        return false;
    }

    default boolean isSupportSecurePay() {
        return false;
    }

    default boolean isSpecialSecureApp(String pkgName) {
        return false;
    }

    default void startSecurityPayService(String prevPkgName, String nextPkgName, String preClsName, String nextClsName) {
    }

    default void systemReady() {
    }
}
