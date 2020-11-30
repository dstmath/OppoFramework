package com.android.server.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.util.ArrayMap;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.permission.PermissionsState;
import java.io.File;
import java.util.List;

public interface IColorPackageManagerServiceInner {
    public static final IColorPackageManagerServiceInner DEFAULT = new IColorPackageManagerServiceInner() {
        /* class com.android.server.pm.IColorPackageManagerServiceInner.AnonymousClass1 */
    };

    default String[] getPackagesForUid(int uid) {
        return null;
    }

    default ApplicationInfo getApplicationInfoInternal(String packageName, int flags, int filterCallingUid, int userId) {
        return null;
    }

    default ResolveInfo resolveIntentInternal(Intent intent, String resolvedType, int flags, int userId, boolean resolveForStart, int filterCallingUid) {
        return null;
    }

    default List<ResolveInfo> queryIntentReceiversInternal(Intent intent, String resolvedType, int flags, int userId, boolean allowDynamicSplits) {
        return null;
    }

    default List<ResolveInfo> queryIntentServicesInternal(Intent intent, String resolvedType, int flags, int userId, int callingUid, boolean includeInstantApps) {
        return null;
    }

    default List<ResolveInfo> queryIntentContentProvidersInternal(Intent intent, String resolvedType, int flags, int userId) {
        return null;
    }

    default void onSystemAppPermissionRevoked(int uid, int userId) {
    }

    default PackageParser.Activity getPackageParserActivity(ComponentName cmp) {
        return null;
    }

    default ArrayMap getPackages() {
        return null;
    }

    default boolean getInstalled(PackageSetting ps, int userId) {
        return false;
    }

    default boolean isCustomDataApp(String pkgName) {
        return false;
    }

    default void scanDirTracedLI(File scanDir, int parseFlags, int scanFlags, long currentTime) {
    }

    default void setInstallerPackageName(PackageManagerService.ActiveInstallSession session, String newName) {
    }

    default PackageParser.Package installPackageFromSystemLIF(String codePathString, int[] allUserHandles, int[] origUserHandles, PermissionsState origPermissionState, boolean writeSettings) throws PackageManagerException {
        return null;
    }

    default List<ResolveInfo> queryIntentActivitiesInternal(Intent intent, String resolvedType, int flags, int userId) {
        return null;
    }
}
