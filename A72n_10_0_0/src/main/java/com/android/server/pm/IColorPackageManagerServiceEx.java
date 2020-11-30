package com.android.server.pm;

import android.common.OppoFeatureList;
import android.content.Context;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Message;
import com.android.server.pm.OppoBasePackageManagerService;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.permission.DefaultPermissionGrantPolicy;
import com.android.server.pm.permission.IColorDefaultPermissionGrantPolicyInner;
import java.util.List;

public interface IColorPackageManagerServiceEx extends IOppoPackageManagerServiceEx {
    public static final IColorPackageManagerServiceEx DEFAULT = new IColorPackageManagerServiceEx() {
        /* class com.android.server.pm.IColorPackageManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IColorPackageManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorPackageManagerServiceEx;
    }

    default IColorPackageManagerServiceEx getDefault() {
        return DEFAULT;
    }

    default Context getContext() {
        return null;
    }

    default IColorPackageManagerServiceInner getColorPackageManagerServiceInner() {
        return new IColorPackageManagerServiceInner() {
            /* class com.android.server.pm.IColorPackageManagerServiceEx.AnonymousClass2 */
        };
    }

    default IColorDefaultPermissionGrantPolicyInner getColorRuntimePermGrantPolicyManagerInner(DefaultPermissionGrantPolicy defaultPermissionGrantPolicy) {
        return new IColorDefaultPermissionGrantPolicyInner() {
            /* class com.android.server.pm.IColorPackageManagerServiceEx.AnonymousClass3 */
        };
    }

    default void extendApplyPolicy(PackageParser.Package pkg, int parseFlags, int scanFlags, PackageParser.Package platformPkg) {
    }

    default boolean inPmsWhiteList(int type, String verifyStr, List<String> list) {
        return false;
    }

    default void sortSystemAppInData(List<ResolveInfo> list) {
    }

    default boolean isSystemDataApp(String packageName) {
        return false;
    }

    default boolean needHideApp(String pkgName, boolean isSystemCaller, boolean debug) {
        return false;
    }

    default void uploadInstallAppInfos(Context context, PackageParser.Package pkg, String installerPackageName) {
    }

    default boolean isNewUserInstallPkg(String packageName) {
        return false;
    }

    default boolean isNewUserNotInstallPkg(String packageName) {
        return false;
    }

    default boolean duplicatePermCheck(OppoBasePackageManagerService.InstallArgsEx installArgsEx) {
        return false;
    }

    default void sendUpdateExtraAppInfoMessage(String packageName, Handler mExtraAppHandler) {
    }

    default void sendUpdateExtraAppInfoMessage(PackageManagerService.PackageInstalledInfo res, OppoBasePackageManagerService.InstallArgsEx installArgsEx, PackageManagerService.InstallArgs args, Handler mExtraAppHandler) {
    }

    default void handleOpooMarketMesage(Message msg) {
    }
}
