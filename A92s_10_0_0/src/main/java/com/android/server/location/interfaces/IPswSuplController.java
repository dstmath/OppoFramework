package com.android.server.location.interfaces;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import java.util.Properties;

public interface IPswSuplController extends IOppoCommonFeature {
    public static final IPswSuplController DEFAULT = new IPswSuplController() {
        /* class com.android.server.location.interfaces.IPswSuplController.AnonymousClass1 */
    };
    public static final String Name = "IPswSuplController";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswSuplController;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void updateProperties(Properties properties) {
    }
}
