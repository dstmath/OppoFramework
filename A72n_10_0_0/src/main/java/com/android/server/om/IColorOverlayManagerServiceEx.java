package com.android.server.om;

import android.common.OppoFeatureList;
import com.android.server.IOppoCommonManagerServiceEx;

public interface IColorOverlayManagerServiceEx extends IOppoCommonManagerServiceEx {
    public static final IColorOverlayManagerServiceEx DEFAULT = new IColorOverlayManagerServiceEx() {
        /* class com.android.server.om.IColorOverlayManagerServiceEx.AnonymousClass1 */
    };

    default IColorOverlayManagerServiceEx getDefault() {
        return DEFAULT;
    }

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorOverlayManagerServiceEx;
    }

    default void init() {
    }
}
