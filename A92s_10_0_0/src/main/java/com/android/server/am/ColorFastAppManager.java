package com.android.server.am;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.util.Slog;
import com.android.server.coloros.OppoListManager;

public class ColorFastAppManager implements IColorFastAppManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "ColorFastAppManager";
    private static volatile ColorFastAppManager sColorFastAppManager = null;
    protected ActivityManagerService mAms = null;
    protected IColorActivityManagerServiceEx mColorAmsEx = null;

    private ColorFastAppManager() {
    }

    public static ColorFastAppManager getInstance() {
        if (sColorFastAppManager == null) {
            synchronized (ColorFastAppManager.class) {
                if (sColorFastAppManager == null) {
                    sColorFastAppManager = new ColorFastAppManager();
                }
            }
        }
        return sColorFastAppManager;
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx == null) {
            Slog.e(TAG, "init error, amsEx is null");
            return;
        }
        this.mColorAmsEx = amsEx;
        this.mAms = amsEx.getActivityManagerService();
    }

    public void fastWechatPayIfNeeded(Intent intent) {
        if (intent != null && intent.getComponent() != null) {
            String cpnClassName = intent.getComponent().getClassName();
            if (OppoListManager.getInstance().isFastAppWechatPayCpn(cpnClassName)) {
                intent.setComponent(OppoListManager.getInstance().replaceFastAppWechatPayCpn(cpnClassName));
            }
        }
    }

    public String fastThirdAppLoginPkgIfNeeded(String resultPkg, String callerPkg) {
        if (OppoListManager.getInstance().isFastAppThirdLoginPkg(resultPkg)) {
            return OppoListManager.getInstance().replaceFastAppThirdLoginPkg(callerPkg);
        }
        return resultPkg;
    }

    public PackageInfo getMiniProgramPkgInfoIfNeeded(String pkgName) {
        String miniProgramSignature = getMiniProgramSignature(pkgName);
        if (miniProgramSignature == null) {
            return null;
        }
        return getMiniProgramPackageInfo(pkgName, miniProgramSignature);
    }

    private String getMiniProgramSignature(String pkgName) {
        return OppoListManager.getInstance().getMiniProgramSignature(pkgName);
    }

    private Signature[] asArray(Signature... s) {
        return s;
    }

    private PackageInfo getMiniProgramPackageInfo(String miniProgramPkgName, String miniProgramSignature) {
        PackageInfo pi = new PackageInfo();
        pi.packageName = miniProgramPkgName;
        pi.applicationInfo = new ApplicationInfo();
        pi.applicationInfo.packageName = miniProgramPkgName;
        pi.signatures = asArray(new Signature(miniProgramSignature));
        return pi;
    }
}
