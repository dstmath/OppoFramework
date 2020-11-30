package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.SmsApplication;

public final class OppoSmsApplication {
    private static final String LOG_TAG = "OppoSmsApplication";
    private static final String[] OEM_PACKAGE_ALLOW_WRITE_SMS_CN = {"com.oppo.engineermode", "com.coloros.backuprestore", "com.heytap.speechassist.engine", "com.android.settings"};
    private static final String[] OEM_PACKAGE_ALLOW_WRITE_SMS_EXP = {"com.oppo.engineermode", "com.coloros.backuprestore", "com.android.settings"};
    private static final String[][] OEM_PACKAGE_ALLOW_WRITE_SMS_THIRD_APP = {new String[]{"swp0", "com.cyz_telecom.apn_vpdn"}};
    private static final String[] OEM_PACKAGE_MO_SMS_NOT_SHOW_IN_UI = {"com.color.safecenter", "com.oppo.trafficmonitor", "com.oppo.activation", "com.nxp.wallet.oppo", "com.oppo.yellowpage", "com.oppo.systemhelper", "com.heytap.usercenter", "com.coloros.activation", "com.coloros.findmyphone", "com.oppo.oppopowermonitor", "com.oppo.usercenter"};

    public static boolean shouldWriteMessageForPackage(String packageName) {
        try {
            Rlog.d(LOG_TAG, "sw pkg=" + packageName);
            if (OEM_PACKAGE_MO_SMS_NOT_SHOW_IN_UI == null) {
                return true;
            }
            String[] strArr = OEM_PACKAGE_MO_SMS_NOT_SHOW_IN_UI;
            for (String smsPackage : strArr) {
                if (smsPackage != null && smsPackage.equals(packageName)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public static void oemAssignExclusiveSmsPermissionsToSystemApp(Context context, PackageManager packageManager, AppOpsManager appOps) {
        try {
            if (context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
                if (OEM_PACKAGE_ALLOW_WRITE_SMS_EXP != null) {
                    for (int i = 0; i < OEM_PACKAGE_ALLOW_WRITE_SMS_EXP.length; i++) {
                        String oempackage = OEM_PACKAGE_ALLOW_WRITE_SMS_EXP[i];
                        if (!TextUtils.isEmpty(oempackage)) {
                            SmsApplication.assignExclusiveSmsPermissionsToSystemAppPublic(context, packageManager, appOps, oempackage);
                        }
                    }
                }
            } else if (OEM_PACKAGE_ALLOW_WRITE_SMS_CN != null) {
                for (int i2 = 0; i2 < OEM_PACKAGE_ALLOW_WRITE_SMS_CN.length; i2++) {
                    String oempackage2 = OEM_PACKAGE_ALLOW_WRITE_SMS_CN[i2];
                    if (!TextUtils.isEmpty(oempackage2)) {
                        SmsApplication.assignExclusiveSmsPermissionsToSystemAppPublic(context, packageManager, appOps, oempackage2);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String propGetEnable(String prop, String defval) {
        try {
            return SystemProperties.get(prop, defval);
        } catch (Exception ex) {
            Rlog.e("oem", "getProp error :" + ex.getMessage());
            return defval;
        }
    }

    private static String getFeatureEnable(String key, String defval) {
        return propGetEnable("persist.sys.oem_" + key, defval);
    }

    private static boolean getFeatureEnable(String key) {
        return !getFeatureEnable(key, CallerInfo.UNKNOWN_NUMBER).equals(WifiEnterpriseConfig.ENGINE_DISABLE);
    }

    public static void oemAssignExclusiveSmsPermissionsToNonSystemApp(Context context, PackageManager packageManager, AppOpsManager appOps) {
        try {
            if (OEM_PACKAGE_ALLOW_WRITE_SMS_THIRD_APP != null) {
                for (int i = 0; i < OEM_PACKAGE_ALLOW_WRITE_SMS_THIRD_APP.length; i++) {
                    String prefix = OEM_PACKAGE_ALLOW_WRITE_SMS_THIRD_APP[i][0];
                    String pkgName = OEM_PACKAGE_ALLOW_WRITE_SMS_THIRD_APP[i][1];
                    if (!TextUtils.isEmpty(prefix) && getFeatureEnable(prefix) && !TextUtils.isEmpty(pkgName)) {
                        assignExclusiveSmsPermissionsToNonSystemApp(context, packageManager, appOps, pkgName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void assignExclusiveSmsPermissionsToNonSystemApp(Context context, PackageManager packageManager, AppOpsManager appOps, String packageName) {
        boolean isPoliceVersion = false;
        if (packageManager != null) {
            try {
                if (packageManager.hasSystemFeature("oppo.customize.function.mdpoe")) {
                    isPoliceVersion = true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Rlog.d(LOG_TAG, "Package not found: " + packageName);
                return;
            } catch (Exception e2) {
                Rlog.e(LOG_TAG, "exception, packageName=" + packageName);
                return;
            }
        }
        if (isPoliceVersion) {
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            if (appOps.checkOp(15, info.applicationInfo.uid, packageName) != 0) {
                Rlog.w(LOG_TAG, packageName + " does not have OP_WRITE_SMS:  (fixing)");
                SmsApplication.setExclusiveAppopsPublic(packageName, appOps, info.applicationInfo.uid, 0);
            }
        }
    }

    public static void oemDefaultSmsAppChanged(Context context, SmsApplication.SmsApplicationData applicationData, int userId) {
        if (context != null && applicationData != null) {
            try {
                Rlog.d(LOG_TAG, "mPackageName=" + applicationData.mPackageName + ",userId=" + userId);
                Settings.Secure.putStringForUser(context.getContentResolver(), Settings.Secure.SMS_DEFAULT_APPLICATION, applicationData.mPackageName, userId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
