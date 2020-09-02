package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import java.util.ArrayList;

public interface IColorRuntimePermGrantPolicyManager extends IOppoCommonFeature {
    public static final IColorRuntimePermGrantPolicyManager DEFAULT = new IColorRuntimePermGrantPolicyManager() {
        /* class com.android.server.pm.IColorRuntimePermGrantPolicyManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorRuntimePermGrantPolicyManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorRuntimePermGrantPolicyManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default void systemReady() {
    }

    default void grantDefaultRuntimePermission() {
    }

    default void grantDefaultRuntimePermissionNewUser(int userId) {
    }

    default void grantOppoPermissionByGroup(PackageParser.Package pkg, String permName, String packageName, int callingUid) {
    }

    default void revokeOppoPermissionByGroup(PackageParser.Package pkg, String permName, String packageName, int callingUid) {
    }

    default void grantOppoPermissionByGroupAsUser(PackageParser.Package pkg, String permName, String packageName, int callingUid, int userId) {
    }

    default void revokeOppoPermissionByGroupAsUser(PackageParser.Package pkg, String permName, String packageName, int callingUid, int userId) {
    }

    default ArrayList<String> getIgnoreAppList() {
        return new ArrayList<>();
    }

    default boolean onPermissionRevoked(ApplicationInfo applicationInfo, int userId) {
        return false;
    }

    default boolean isRuntimePermissionFingerprintNew(int userId) {
        return false;
    }
}
