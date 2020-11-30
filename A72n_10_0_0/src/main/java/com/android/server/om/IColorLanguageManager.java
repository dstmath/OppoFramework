package com.android.server.om;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import com.android.server.om.OppoBaseOverlayManagerService;
import java.util.List;
import java.util.Map;

public interface IColorLanguageManager extends IOppoCommonFeature {
    public static final IColorLanguageManager DEFAULT = new IColorLanguageManager() {
        /* class com.android.server.om.IColorLanguageManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorLanguageManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorLanguageManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(OverlayManagerServiceImpl overlayManagerService, OppoBaseOverlayManagerService.IOppoOMSPackageCache packageManager, Object lock, Context context) {
    }

    default void setLanguageEnable(String path, int userId) {
    }

    default void updateLanguagePath(String targetPackageName, int userId, Map<String, List<String>> map) {
    }

    default int checkSignaturesMatching(String overlay, String target, int fulfilledPolicies, int flag) {
        return fulfilledPolicies;
    }
}
