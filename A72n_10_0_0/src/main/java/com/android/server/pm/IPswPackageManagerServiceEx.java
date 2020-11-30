package com.android.server.pm;

import android.common.OppoFeatureList;

public interface IPswPackageManagerServiceEx extends IOppoPackageManagerServiceEx {
    public static final IPswPackageManagerServiceEx DEFAULT = new IPswPackageManagerServiceEx() {
        /* class com.android.server.pm.IPswPackageManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IPswPackageManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswPackageManagerServiceEx;
    }

    default IPswPackageManagerServiceEx getDefault() {
        return DEFAULT;
    }
}
