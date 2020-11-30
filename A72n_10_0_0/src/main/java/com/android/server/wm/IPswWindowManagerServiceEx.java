package com.android.server.wm;

import android.common.OppoFeatureList;

public interface IPswWindowManagerServiceEx extends IOppoWindowManagerServiceEx {
    public static final IPswWindowManagerServiceEx DEFAULT = new IPswWindowManagerServiceEx() {
        /* class com.android.server.wm.IPswWindowManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IPswWindowManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswWindowManagerServiceEx;
    }

    default IPswWindowManagerServiceEx getDefault() {
        return DEFAULT;
    }
}
