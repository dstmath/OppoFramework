package com.android.server.am;

import android.common.OppoFeatureList;

public interface IPswActivityManagerServiceEx extends IOppoActivityManagerServiceEx {
    public static final IPswActivityManagerServiceEx DEFAULT = new IPswActivityManagerServiceEx() {
        /* class com.android.server.am.IPswActivityManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IPswActivityManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswActivityManagerServiceEx;
    }

    default IPswActivityManagerServiceEx getDefault() {
        return DEFAULT;
    }
}
