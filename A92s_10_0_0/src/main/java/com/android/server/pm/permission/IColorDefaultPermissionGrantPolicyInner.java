package com.android.server.pm.permission;

import android.content.pm.PackageInfo;
import java.util.Set;

public interface IColorDefaultPermissionGrantPolicyInner {
    default void grantRuntimePermissions(PackageInfo pkg, Set<String> set, boolean systemFixed, int userId) {
    }

    default void grantRuntimePermissions(PackageInfo pkg, Set<String> set, boolean systemFixed, boolean ignoreSystemPackage, boolean whitelistRestrictedPermissions, int userId) {
    }

    default PermissionManagerService getPermissionManagerService() {
        return null;
    }

    default BasePermission getPermission(String permission) {
        return null;
    }
}
