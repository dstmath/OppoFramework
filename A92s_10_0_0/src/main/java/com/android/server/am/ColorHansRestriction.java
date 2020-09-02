package com.android.server.am;

import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.os.UserHandle;
import com.android.server.am.ColorHansManager;
import com.android.server.coloros.OppoListManager;

public class ColorHansRestriction {
    private static final String TAG = "ColorHansRestriction";
    private Context mContext = null;
    private ColorHansManager mHansManager = null;
    private IPackageManager mPackageManager = null;

    public ColorHansRestriction(ColorHansManager hansManager, Context context) {
        this.mHansManager = hansManager;
        this.mContext = context;
    }

    public boolean isAllowedActivity(int callingUid, String callingPackage, int uid, String pkgName, String cpnName) {
        boolean ret = true;
        if (isDefaultAllow(pkgName, uid)) {
            return true;
        }
        if (this.mHansManager.getHansPkgSelector().isInActivityCallerBlackList(callingPackage) || this.mHansManager.getHansPkgSelector().isInActivityCpnBlackList(pkgName, cpnName)) {
            ret = false;
        } else if (this.mHansManager.getHansPkgSelector().isInActivityCallerWhiteList(callingPackage) || this.mHansManager.getHansPkgSelector().isInActivityCpnWhiteList(pkgName, cpnName)) {
            ret = true;
        }
        if (!ret) {
            ColorHansManager.HansLogger hansLogger = this.mHansManager.getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + "/" + cpnName + "  t: activity  r: hans");
        }
        return ret;
    }

    public boolean isAllowedService(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, boolean isBind) {
        boolean ret = true;
        if (isDefaultAllow(pkgName, uid)) {
            return true;
        }
        if (this.mHansManager.getHansPkgSelector().isInServiceCallerBlackList(callingPackage) || this.mHansManager.getHansPkgSelector().isInServiceCpnBlackList(pkgName, cpnName)) {
            ret = false;
        } else if (this.mHansManager.getHansPkgSelector().isInServiceCallerWhiteList(callingPackage) || this.mHansManager.getHansPkgSelector().isInServiceCpnWhiteList(pkgName, cpnName) || (isBind && isAllowBindService(cpnName))) {
            ret = true;
        } else if (this.mHansManager.getCommonConfig().isChinaRegion() && !this.mHansManager.getCommonConfig().isScreenOn() && !isAllowStart(pkgName, uid)) {
            ret = false;
        } else if (this.mHansManager.getCommonConfig().isDisableNetWork()) {
            ret = false;
        }
        if (!ret) {
            ColorHansManager.HansLogger hansLogger = this.mHansManager.getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + "/" + cpnName + "  t: service  r: hans");
        }
        return ret;
    }

    public boolean isAllowedProvider(int callingUid, String callingPackage, int uid, String pkgName, String cpnName) {
        boolean ret = true;
        if (isDefaultAllow(pkgName, uid)) {
            return true;
        }
        if (this.mHansManager.getHansPkgSelector().isInProviderCallerBlackList(callingPackage) || this.mHansManager.getHansPkgSelector().isInProviderCpnBlackList(pkgName, cpnName)) {
            ret = false;
        } else if (this.mHansManager.getHansPkgSelector().isInProviderCallerWhiteList(callingPackage) || this.mHansManager.getHansPkgSelector().isInProviderCpnWhiteList(pkgName, cpnName) || isAllowProviderCpn(cpnName)) {
            ret = true;
        } else if (this.mHansManager.getCommonConfig().isChinaRegion() && !this.mHansManager.getCommonConfig().isScreenOn() && !isAllowStart(pkgName, uid)) {
            ret = false;
        }
        if (!ret) {
            ColorHansManager.HansLogger hansLogger = this.mHansManager.getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + "/" + cpnName + "  t: provider  r: hans");
        }
        return ret;
    }

    public boolean isAllowedBroadcast(int callingUid, String callingPackage, int uid, String pkgName, String action, boolean order) {
        boolean ret = true;
        if (isDefaultAllow(pkgName, uid)) {
            return true;
        }
        if (this.mHansManager.getHansPkgSelector().isInBroadcastCallerBlackList(callingPackage) || this.mHansManager.getHansPkgSelector().isInBroadcastCpnBlackList(pkgName, action)) {
            ret = false;
        } else if (this.mHansManager.getHansPkgSelector().isInBroadcastCallerWhiteList(callingPackage) || this.mHansManager.getHansPkgSelector().isInBroadcastCpnWhiteList(pkgName, action)) {
            ret = true;
        } else if (this.mHansManager.getCommonConfig().isChinaRegion() && !this.mHansManager.getCommonConfig().isScreenOn() && !isAllowStart(pkgName, uid)) {
            ret = false;
        } else if (this.mHansManager.getCommonConfig().isDisableNetWork()) {
            ret = false;
        }
        if (!ret) {
            ColorHansManager.HansLogger hansLogger = this.mHansManager.getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + "/" + action + "  t: broadcast  r: hans");
        }
        return ret;
    }

    public boolean isAllowedAlarm(String action, int uid, String pkgName) {
        boolean ret = true;
        if (isDefaultAllow(pkgName, uid)) {
            return true;
        }
        if (this.mHansManager.getHansPkgSelector().isInAlarmWhiteList(pkgName, action) || this.mHansManager.getHansPkgSelector().isInAlarmKeyWhiteList(pkgName, action) || this.mHansManager.getHansPkgSelector().isInNetPacketWhiteList(pkgName)) {
            ret = true;
        } else if (this.mHansManager.getCommonConfig().isChinaRegion() && !this.mHansManager.getCommonConfig().isScreenOn() && !isAllowStart(pkgName, uid) && !this.mHansManager.isDeviceIdleList(uid)) {
            ret = false;
        }
        if (!ret) {
            ColorHansManager.HansLogger hansLogger = this.mHansManager.getHansLogger();
            hansLogger.d("prevent trigger alarm " + uid + " " + pkgName + "/" + action + "  r: hans");
        }
        return ret;
    }

    public boolean isAllowedSync(int uid, String pkgName) {
        boolean ret = true;
        if (isDefaultAllow(pkgName, uid)) {
            return true;
        }
        if (this.mHansManager.getHansPkgSelector().isInSyncWhiteList(pkgName)) {
            ret = true;
        } else if (this.mHansManager.getCommonConfig().isChinaRegion() && !this.mHansManager.getCommonConfig().isScreenOn() && !isAllowStart(pkgName, uid) && !isAllowAccountSync(pkgName, uid)) {
            ret = false;
        }
        if (!ret) {
            ColorHansManager.HansLogger hansLogger = this.mHansManager.getHansLogger();
            hansLogger.d("prevent trigger sync " + uid + " " + pkgName + "  r: hans");
        }
        return ret;
    }

    public boolean isAllowedJob(int uid, String pkgName) {
        boolean ret = true;
        if (isDefaultAllow(pkgName, uid)) {
            return true;
        }
        if (this.mHansManager.getHansPkgSelector().isInJobWhiteList(pkgName)) {
            ret = true;
        } else if (this.mHansManager.getCommonConfig().isChinaRegion() && !this.mHansManager.getCommonConfig().isScreenOn() && !isAllowStart(pkgName, uid)) {
            ret = false;
        }
        if (!ret) {
            ColorHansManager.HansLogger hansLogger = this.mHansManager.getHansLogger();
            hansLogger.d("prevent trigger job " + uid + " " + pkgName + "  r: hans");
        }
        return ret;
    }

    public boolean isAllowedBinder(int callingPid, int uid, String pkgName, String aidlName, int code, boolean oneway) {
        boolean ret = true;
        if (isDefaultAllow(pkgName, uid)) {
            return true;
        }
        if (this.mHansManager.getHansPkgSelector().isInAsyncBinderPkgWhiteList(aidlName, pkgName)) {
            ret = true;
        } else if (this.mHansManager.getHansPkgSelector().isInAsyncBinderCodeWhiteList(aidlName, String.valueOf(code))) {
            ret = true;
        } else if (oneway && this.mHansManager.getCommonConfig().isChinaRegion() && !isAllowStart(pkgName, uid)) {
            ret = false;
        }
        if (!ret) {
            ColorHansManager.HansLogger hansLogger = this.mHansManager.getHansLogger();
            hansLogger.d("prevent binder  " + uid + " " + pkgName + " " + aidlName + "/" + code + "  r: hans");
        }
        return ret;
    }

    public boolean isDefaultAllow(String pkgName, int uid) {
        return OppoListManager.getInstance().isInCustomWhiteList(pkgName) || OppoListManager.getInstance().isCtaPackage(pkgName) || isCtsRunning();
    }

    private boolean isCtsRunning() {
        try {
            if (this.mPackageManager == null) {
                this.mPackageManager = AppGlobals.getPackageManager();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isAllowStart(String pkgName, int uid) {
        return isAllowBootStart(pkgName, uid) || isAllowAssStart(pkgName, uid);
    }

    private boolean isAllowBootStart(String pkgName, int uid) {
        return OppoListManager.getInstance().isAutoStartWhiteList(pkgName, UserHandle.getUserId(uid));
    }

    private boolean isAllowAssStart(String pkgName, int uid) {
        return ColorAppStartupManager.getInstance().inAssociateStartWhiteList(pkgName, UserHandle.getUserId(uid)) || ColorAppStartupManager.getInstance().inProtectWhiteList(pkgName);
    }

    private boolean isAllowAccountSync(String pkgName, int uid) {
        return OppoListManager.getInstance().isInAccountSyncWhiteList(pkgName, UserHandle.getUserId(uid));
    }

    private boolean isAllowBindService(String cpnName) {
        return ColorAppStartupManager.getInstance().inBindServiceCpnWhiteList(cpnName);
    }

    private boolean isAllowProviderCpn(String providerName) {
        return ColorAppStartupManager.getInstance().inProviderCpnWhiteList(providerName);
    }

    public boolean isGameApp(String pkgName, int uid) {
        return ColorGameSpaceManager.getInstance().inGameSpacePkgList(pkgName);
    }
}
