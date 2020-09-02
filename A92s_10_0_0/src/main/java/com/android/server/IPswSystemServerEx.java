package com.android.server;

import android.common.OppoFeatureList;

public interface IPswSystemServerEx extends IOppoSystemServerEx {
    public static final IPswSystemServerEx DEFAULT = new IPswSystemServerEx() {
        /* class com.android.server.IPswSystemServerEx.AnonymousClass1 */
    };
    public static final String NAME = "IPswSystemServerEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswSystemServerEx;
    }

    default IPswSystemServerEx getDefault() {
        return DEFAULT;
    }
}
