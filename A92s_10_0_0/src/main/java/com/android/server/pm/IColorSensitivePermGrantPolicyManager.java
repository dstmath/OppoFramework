package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.PackageParser;

public interface IColorSensitivePermGrantPolicyManager extends IOppoCommonFeature {
    public static final IColorSensitivePermGrantPolicyManager DEFAULT = new IColorSensitivePermGrantPolicyManager() {
        /* class com.android.server.pm.IColorSensitivePermGrantPolicyManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorSensitivePermGrantPolicyManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorSensitivePermGrantPolicyManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default void systemReady() {
    }

    default boolean grantPermissionOppoPolicy(PackageParser.Package pkg, String perm, boolean allowed) {
        return false;
    }

    default boolean allowAddInstallPermForDataApp(String packageName) {
        return false;
    }

    default boolean allowSilentUninstall(int callingUid) {
        return false;
    }
}
