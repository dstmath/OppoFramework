package com.android.server.pm;

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

public class ColorDummyPackageManagerServiceEx extends OppoDummyPackageManagerServiceEx implements IColorPackageManagerServiceEx {
    private static final String TAG = "ColorDummyPackageManagerServiceEx";
    private Context mContext;

    public ColorDummyPackageManagerServiceEx(Context context, PackageManagerService pms) {
        super(context, pms);
        this.mContext = context;
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public Context getContext() {
        return this.mContext;
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void onStart() {
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public IColorDefaultPermissionGrantPolicyInner getColorRuntimePermGrantPolicyManagerInner(DefaultPermissionGrantPolicy defaultPermissionGrantPolicy) {
        return null;
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void systemReady() {
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public IColorPackageManagerServiceInner getColorPackageManagerServiceInner() {
        return null;
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public void extendApplyPolicy(PackageParser.Package pkg, int parseFlags, int scanFlags, PackageParser.Package platformPkg) {
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public boolean inPmsWhiteList(int type, String verifyStr, List<String> list) {
        return false;
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public void sortSystemAppInData(List<ResolveInfo> list) {
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public boolean isSystemDataApp(String packageName) {
        return false;
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public boolean needHideApp(String pkgName, boolean isSystemCaller, boolean debug) {
        return false;
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public void uploadInstallAppInfos(Context context, PackageParser.Package pkg, String installerPackageName) {
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public boolean isNewUserInstallPkg(String packageName) {
        return false;
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public boolean isNewUserNotInstallPkg(String packageName) {
        return false;
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public boolean duplicatePermCheck(OppoBasePackageManagerService.InstallArgsEx installArgsEx) {
        return false;
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public void sendUpdateExtraAppInfoMessage(String packageName, Handler mExtraAppHandler) {
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public void sendUpdateExtraAppInfoMessage(PackageManagerService.PackageInstalledInfo res, OppoBasePackageManagerService.InstallArgsEx installArgsEx, PackageManagerService.InstallArgs args, Handler mExtraAppHandler) {
    }

    @Override // com.android.server.pm.IColorPackageManagerServiceEx
    public void handleOpooMarketMesage(Message msg) {
    }
}
