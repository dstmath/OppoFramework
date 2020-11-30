package com.android.server.am;

import android.os.UserHandle;
import com.android.server.am.ColorHansManager;
import com.android.server.coloros.OppoListManager;

public class ColorHansRestriction {
    public static final int HANS_RESTRICTION_BLOCK_ACTIVITY = 1;
    public static final int HANS_RESTRICTION_BLOCK_ALARM = 16;
    public static final int HANS_RESTRICTION_BLOCK_BINDER = 128;
    public static final int HANS_RESTRICTION_BLOCK_BROADCAST = 8;
    public static final int HANS_RESTRICTION_BLOCK_JOB = 64;
    public static final int HANS_RESTRICTION_BLOCK_NONE = 0;
    public static final int HANS_RESTRICTION_BLOCK_PROVIDER = 4;
    public static final int HANS_RESTRICTION_BLOCK_SERVICE = 2;
    public static final int HANS_RESTRICTION_BLOCK_SYNC = 32;
    private static final String TAG = "ColorHansRestriction";
    private int mRestrictions = 0;

    public ColorHansRestriction(int restrictions) {
        this.mRestrictions = restrictions;
    }

    public boolean isBlockedActivityPolicy(int callingUid, String callingPackage, int uid, String pkgName, String cpnName) {
        return false;
    }

    public boolean isBlockedServicePolicy(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, boolean isBind, int freezeLevel) {
        if (!isAllowStart(pkgName, uid)) {
            return true;
        }
        return false;
    }

    public boolean isBlockedProviderPolicy(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, int freezeLevel) {
        if (!isAllowStart(pkgName, uid)) {
            return true;
        }
        return false;
    }

    public boolean isBlockedBroadcastPolicy(int callingUid, String callingPackage, int uid, String pkgName, String action, boolean order, int freezeLevel) {
        if (!isAllowStart(pkgName, uid)) {
            return true;
        }
        return false;
    }

    public boolean isBlockedAlarmPolicy(String action, int uid, String pkgName, int freezeLevel) {
        if (isAllowStart(pkgName, uid) || ColorHansManager.getInstance().isOnDeviceIdleWhitelist(uid)) {
            return false;
        }
        return true;
    }

    public boolean isBlockedSyncPolicy(int uid, String pkgName, int freezeLevel) {
        if (isAllowStart(pkgName, uid) || isAllowAccountSync(pkgName, uid)) {
            return false;
        }
        return true;
    }

    public boolean isBlockedJobPolicy(int uid, String pkgName, int freezeLevel) {
        if (!isAllowStart(pkgName, uid)) {
            return true;
        }
        return false;
    }

    public boolean isBlockedBinderPolicy(int callingPid, int uid, String pkgName, String aidlName, int code, boolean oneway) {
        if (!oneway || isAllowStart(pkgName, uid)) {
            return false;
        }
        return true;
    }

    public boolean isAllowedActivity(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, int freezeLevel) {
        if (ColorHansPackageSelector.getInstance().isInActivityCallerBlackList(callingPackage) || ColorHansPackageSelector.getInstance().isInActivityCpnBlackList(pkgName, cpnName)) {
            ColorHansManager.HansLogger hansLogger = ColorHansManager.getInstance().getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + "/" + cpnName + " from " + callingUid + " t: activity/b  r: hans");
            return false;
        } else if (ColorHansPackageSelector.getInstance().isInActivityCallerWhiteList(callingPackage) || ColorHansPackageSelector.getInstance().isInActivityCpnWhiteList(pkgName, cpnName)) {
            return true;
        } else {
            if (freezeLevel == 4 && callingUid > 10000 && callingUid != ColorHansManager.getInstance().getCurResumeUid()) {
                ColorHansManager.HansLogger hansLogger2 = ColorHansManager.getInstance().getHansLogger();
                hansLogger2.d("prevent start " + uid + " " + pkgName + "/" + cpnName + " from " + callingUid + " t: activity/level-4  r: hans");
                return false;
            } else if ((this.mRestrictions & 1) == 0 || !isBlockedActivityPolicy(callingUid, callingPackage, uid, pkgName, cpnName)) {
                return true;
            } else {
                ColorHansManager.HansLogger hansLogger3 = ColorHansManager.getInstance().getHansLogger();
                hansLogger3.d("prevent start " + uid + " " + pkgName + "/" + cpnName + " from " + callingUid + " t: activity  r: hans");
                return false;
            }
        }
    }

    public boolean isAllowedService(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, boolean isBind, int freezeLevel) {
        if (ColorHansPackageSelector.getInstance().isInServiceCallerBlackList(callingPackage) || ColorHansPackageSelector.getInstance().isInServiceCpnBlackList(pkgName, cpnName)) {
            ColorHansManager.HansLogger hansLogger = ColorHansManager.getInstance().getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + "/" + cpnName + " from " + callingUid + " t: service/b  r: hans");
            return false;
        } else if (ColorHansPackageSelector.getInstance().isInServiceCallerWhiteList(callingPackage) || ColorHansPackageSelector.getInstance().isInServiceCpnWhiteList(pkgName, cpnName) || (isBind && isAllowBindService(cpnName))) {
            return true;
        } else {
            if (freezeLevel == 4 && callingUid > 10000 && callingUid != ColorHansManager.getInstance().getCurResumeUid()) {
                ColorHansManager.HansLogger hansLogger2 = ColorHansManager.getInstance().getHansLogger();
                hansLogger2.d("prevent start " + uid + " " + pkgName + "/" + cpnName + " from " + callingUid + " t: service/level-4  r: hans");
                return false;
            } else if ((this.mRestrictions & 2) == 0 || !isBlockedServicePolicy(callingUid, callingPackage, uid, pkgName, cpnName, isBind, freezeLevel)) {
                return true;
            } else {
                ColorHansManager.HansLogger hansLogger3 = ColorHansManager.getInstance().getHansLogger();
                hansLogger3.d("prevent start " + uid + " " + pkgName + "/" + cpnName + " from " + callingUid + " t: service  r: hans");
                return false;
            }
        }
    }

    public boolean isAllowedProvider(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, int freezeLevel) {
        if (ColorHansPackageSelector.getInstance().isInProviderCallerBlackList(callingPackage) || ColorHansPackageSelector.getInstance().isInProviderCpnBlackList(pkgName, cpnName)) {
            ColorHansManager.HansLogger hansLogger = ColorHansManager.getInstance().getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + "/" + cpnName + " from " + callingUid + " t: provider/b  r: hans");
            return false;
        } else if (ColorHansPackageSelector.getInstance().isInProviderCallerWhiteList(callingPackage) || ColorHansPackageSelector.getInstance().isInProviderCpnWhiteList(pkgName, cpnName) || isAllowProviderCpn(cpnName)) {
            return true;
        } else {
            if (freezeLevel == 4 && callingUid > 10000 && callingUid != ColorHansManager.getInstance().getCurResumeUid()) {
                ColorHansManager.HansLogger hansLogger2 = ColorHansManager.getInstance().getHansLogger();
                hansLogger2.d("prevent start " + uid + " " + pkgName + "/" + cpnName + " from " + callingUid + " t: provider/level-4  r: hans");
                return false;
            } else if ((4 & this.mRestrictions) == 0 || !isBlockedProviderPolicy(callingUid, callingPackage, uid, pkgName, cpnName, freezeLevel)) {
                return true;
            } else {
                ColorHansManager.HansLogger hansLogger3 = ColorHansManager.getInstance().getHansLogger();
                hansLogger3.d("prevent start " + uid + " " + pkgName + "/" + cpnName + " from " + callingUid + " t: provider  r: hans");
                return false;
            }
        }
    }

    public boolean isAllowedBroadcast(int callingUid, String callingPackage, int uid, String pkgName, String action, boolean order, int freezeLevel) {
        if (ColorHansPackageSelector.getInstance().isInBroadcastCallerBlackList(callingPackage) || ColorHansPackageSelector.getInstance().isInBroadcastCpnBlackList(pkgName, action)) {
            ColorHansManager.HansLogger hansLogger = ColorHansManager.getInstance().getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + "/" + action + " from " + callingUid + " t: broadcast/b  r: hans");
            return false;
        } else if (ColorHansPackageSelector.getInstance().isInBroadcastCallerWhiteList(callingPackage) || ColorHansPackageSelector.getInstance().isInBroadcastCpnWhiteList(pkgName, action)) {
            return true;
        } else {
            if (freezeLevel == 4 && callingUid > 10000 && callingUid != ColorHansManager.getInstance().getCurResumeUid()) {
                ColorHansManager.HansLogger hansLogger2 = ColorHansManager.getInstance().getHansLogger();
                hansLogger2.d("prevent start " + uid + " " + pkgName + "/" + action + " from " + callingUid + " t: broadcast/level-4  r: hans");
                return false;
            } else if ((this.mRestrictions & 8) == 0 || !isBlockedBroadcastPolicy(callingUid, callingPackage, uid, pkgName, action, order, freezeLevel)) {
                return true;
            } else {
                ColorHansManager.HansLogger hansLogger3 = ColorHansManager.getInstance().getHansLogger();
                hansLogger3.d("prevent start " + uid + " " + pkgName + "/" + action + " from " + callingUid + " t: broadcast  r: hans");
                return false;
            }
        }
    }

    public boolean isAllowedAlarm(String action, int uid, String pkgName, int freezeLevel) {
        if (ColorHansPackageSelector.getInstance().isInAlarmWhiteList(pkgName, action) || ColorHansPackageSelector.getInstance().isInAlarmKeyWhiteList(pkgName, action) || ColorHansPackageSelector.getInstance().isInNetPacketWhiteList(pkgName)) {
            return true;
        }
        if (freezeLevel == 4) {
            ColorHansManager.HansLogger hansLogger = ColorHansManager.getInstance().getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + "/" + action + " t: alarm/level-4  r: hans");
            return false;
        } else if ((this.mRestrictions & 16) == 0 || !isBlockedAlarmPolicy(action, uid, pkgName, freezeLevel)) {
            return true;
        } else {
            ColorHansManager.HansLogger hansLogger2 = ColorHansManager.getInstance().getHansLogger();
            hansLogger2.d("prevent start " + uid + " " + pkgName + "/" + action + " t: alarm  r: hans");
            return false;
        }
    }

    public boolean isAllowedSync(int uid, String pkgName, int freezeLevel) {
        if (ColorHansPackageSelector.getInstance().isInSyncWhiteList(pkgName)) {
            return true;
        }
        if (freezeLevel == 4) {
            ColorHansManager.HansLogger hansLogger = ColorHansManager.getInstance().getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + " t: sync/level-4  r: hans");
            return false;
        } else if ((this.mRestrictions & 32) == 0 || !isBlockedSyncPolicy(uid, pkgName, freezeLevel)) {
            return true;
        } else {
            ColorHansManager.HansLogger hansLogger2 = ColorHansManager.getInstance().getHansLogger();
            hansLogger2.d("prevent start " + uid + " " + pkgName + " t: sync  r: hans");
            return false;
        }
    }

    public boolean isAllowedJob(int uid, String pkgName, int freezeLevel) {
        if (ColorHansPackageSelector.getInstance().isInJobWhiteList(pkgName)) {
            return true;
        }
        if (freezeLevel == 4) {
            ColorHansManager.HansLogger hansLogger = ColorHansManager.getInstance().getHansLogger();
            hansLogger.d("prevent start " + uid + " " + pkgName + " t: job/level-4  r: hans");
            return false;
        } else if ((this.mRestrictions & 64) == 0 || !isBlockedJobPolicy(uid, pkgName, freezeLevel)) {
            return true;
        } else {
            ColorHansManager.HansLogger hansLogger2 = ColorHansManager.getInstance().getHansLogger();
            hansLogger2.d("prevent start " + uid + " " + pkgName + " t: job  r: hans");
            return false;
        }
    }

    public boolean isAllowedBinder(int callingPid, int uid, String pkgName, String aidlName, int code, boolean oneway) {
        if (ColorHansPackageSelector.getInstance().isInAsyncBinderPkgWhiteList(aidlName, pkgName) || ColorHansPackageSelector.getInstance().isInAsyncBinderCodeWhiteList(aidlName, String.valueOf(code)) || (this.mRestrictions & HANS_RESTRICTION_BLOCK_BINDER) == 0 || !isBlockedBinderPolicy(callingPid, uid, pkgName, aidlName, code, oneway)) {
            return true;
        }
        ColorHansManager.HansLogger hansLogger = ColorHansManager.getInstance().getHansLogger();
        hansLogger.d("prevent start " + uid + " " + pkgName + " " + aidlName + "/" + code + " from " + callingPid + " t: binder  r: hans");
        return false;
    }

    public boolean isAllowStart(String pkgName, int uid) {
        return isAllowBootStart(pkgName, uid) || isAllowAssStart(pkgName, uid);
    }

    private boolean isAllowBootStart(String pkgName, int uid) {
        return OppoListManager.getInstance().isAutoStartWhiteList(pkgName, UserHandle.getUserId(uid));
    }

    public boolean isFromControlCenterPkg(String pkgName) {
        return OppoListManager.getInstance().isFromControlCenterPkg(pkgName);
    }

    private boolean isAllowAssStart(String pkgName, int uid) {
        return ColorAppStartupManager.getInstance().inAssociateStartWhiteList(pkgName, UserHandle.getUserId(uid)) || ColorAppStartupManager.getInstance().inProtectWhiteList(pkgName);
    }

    private boolean isAllowBindService(String cpnName) {
        return ColorAppStartupManager.getInstance().inBindServiceCpnWhiteList(cpnName);
    }

    private boolean isAllowProviderCpn(String providerName) {
        return ColorAppStartupManager.getInstance().inProviderCpnWhiteList(providerName);
    }

    private boolean isAllowAccountSync(String pkgName, int uid) {
        return OppoListManager.getInstance().isInAccountSyncWhiteList(pkgName, UserHandle.getUserId(uid));
    }

    public boolean isGameApp(String pkgName, int uid) {
        return ColorGameSpaceManager.getInstance().inGameSpacePkgList(pkgName);
    }
}
