package com.android.server.wm;

import android.common.OppoFeatureList;

public interface IPswActivityTaskManagerServiceEx extends IOppoActivityTaskManagerServiceEx {
    public static final IPswActivityTaskManagerServiceEx DEFAULT = new IPswActivityTaskManagerServiceEx() {
        /* class com.android.server.wm.IPswActivityTaskManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IPswActivityTaskManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswActivityTaskManagerServiceEx;
    }

    default IPswActivityTaskManagerServiceEx getDefault() {
        return DEFAULT;
    }
}
