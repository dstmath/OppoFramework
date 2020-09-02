package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.content.pm.PackageInfo;

public interface IColorFastAppManager extends IOppoCommonFeature {
    public static final IColorFastAppManager DEFAULT = new IColorFastAppManager() {
        /* class com.android.server.am.IColorFastAppManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorFastAppManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorFastAppManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default void fastWechatPayIfNeeded(Intent intent) {
    }

    default String fastThirdAppLoginPkgIfNeeded(String resultPkg, String callerPkg) {
        return null;
    }

    default PackageInfo getMiniProgramPkgInfoIfNeeded(String pkgName) {
        return null;
    }
}
