package com.oppo.internal.telephony;

import android.app.ActivityManager;
import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.internal.telephony.OppoRlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.util.ReflectionHelper;
import com.oppo.internal.telephony.nwdiagnose.NetworkDiagnoseUtils;
import java.util.Iterator;
import java.util.List;

public class OppoSmsCommonUtils {
    private static final String LOG = "OppoSmsCommonUtils";
    private static final String LOG_TAG = "OppoSmsCommonUtils";
    private static final String TAG = "OppoSmsCommonUtils";

    public static boolean isExpRegion(Context context) {
        if (context == null) {
            return false;
        }
        try {
            if (context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
                OppoRlog.Rlog.d("OppoSmsCommonUtils", "isExpRegion true");
                return true;
            }
            OppoRlog.Rlog.d("OppoSmsCommonUtils", "isExpRegion false");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean oemIsMtSmsBlock(Context context, String number) {
        return isInBlackLists(context, number);
    }

    private static boolean isInBlackLists(Context context, String number) {
        Cursor cursor = null;
        boolean ret = false;
        try {
            Cursor cursor2 = context.getContentResolver().query(Uri.withAppendedPath(Uri.withAppendedPath(Uri.withAppendedPath(ContentProvider.maybeAddUserId(Uri.parse("content://com.coloros.provider.BlackListProvider"), context.getUserId()), "bl_list"), "sms_and_call_block"), number), null, "block_type=1 OR block_type=3", null, null);
            if (cursor2 != null && cursor2.getCount() > 0) {
                ret = true;
            }
            if (cursor2 != null) {
                try {
                    cursor2.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
            throw th;
        }
        return ret;
    }

    public static boolean oemShouldWriteMessageWhenSafeDialogShow(Context context, PackageInfo packageInfo) {
        if (packageInfo == null) {
            return true;
        }
        boolean shouldWrite = true;
        Cursor cursor = null;
        try {
            if (SystemProperties.get("persist.sys.sms_cmcc", "0").equals("1")) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
            String packageName = packageInfo.packageName;
            if (context == null || TextUtils.isEmpty(packageName)) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                }
                return true;
            } else if (isExpRegion(context)) {
                OppoRlog.Rlog.d("OppoSmsCommonUtils", "except region");
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception ex3) {
                        ex3.printStackTrace();
                    }
                }
                return true;
            } else {
                Uri uri = Uri.parse("content://com.color.provider.SafeProvider/pp_permission");
                Cursor cursor2 = context.getContentResolver().query(uri, null, "pkg_name=? AND send_sms=1", new String[]{packageName}, null);
                if (cursor2 != null && cursor2.getCount() >= 1) {
                    shouldWrite = false;
                }
                if (cursor2 != null) {
                    try {
                        cursor2.close();
                    } catch (Exception ex4) {
                        ex4.printStackTrace();
                    }
                }
                OppoRlog.Rlog.d("OppoSmsCommonUtils", "shouldWrite=" + shouldWrite);
                return shouldWrite;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ex5) {
                    ex5.printStackTrace();
                }
            }
            throw th;
        }
    }

    /* JADX INFO: Multiple debug info for r5v9 int: [D('rsp' java.lang.String), D('packageName' java.lang.String)] */
    public static String oemGetPackageNameViaProcessId(Context context, String callingPackage) {
        String rsp;
        String rsp2;
        try {
            OppoRlog.Rlog.d("OppoSmsCommonUtils", "callingPackage=" + callingPackage);
            PackageManager pm = context.getPackageManager();
            String[] packageNames = pm.getPackagesForUid(Binder.getCallingUid());
            if (packageNames == null) {
                return callingPackage;
            }
            OppoRlog.Rlog.d("OppoSmsCommonUtils", "packageNames.length=" + packageNames.length);
            String packageName = null;
            String rsp3 = null;
            int i = 0;
            if (packageNames.length == 1) {
                packageName = packageNames[0];
                rsp = null;
            } else if (packageNames.length > 1) {
                int callingPid = Binder.getCallingPid();
                try {
                    List<ActivityManager.RunningAppProcessInfo> processList = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
                    if (processList != null) {
                        Iterator<ActivityManager.RunningAppProcessInfo> index = processList.iterator();
                        while (true) {
                            if (!index.hasNext()) {
                                rsp = null;
                                break;
                            }
                            ActivityManager.RunningAppProcessInfo processInfo = index.next();
                            if (callingPid == processInfo.pid) {
                                String[] strArr = processInfo.pkgList;
                                int length = strArr.length;
                                int i2 = i;
                                while (true) {
                                    if (i2 >= length) {
                                        break;
                                    }
                                    String pkgInProcess = strArr[i2];
                                    if (callingPackage != null) {
                                        if (callingPackage.equals(pkgInProcess)) {
                                            packageName = callingPackage;
                                            break;
                                        }
                                    }
                                    i2++;
                                }
                                if (packageName == null) {
                                    String[] strArr2 = processInfo.pkgList;
                                    int length2 = strArr2.length;
                                    String packageName2 = packageName;
                                    int i3 = 0;
                                    while (true) {
                                        if (i3 >= length2) {
                                            rsp = rsp3;
                                            packageName = packageName2;
                                            break;
                                        }
                                        String pkgInProcess2 = strArr2[i3];
                                        int length3 = packageNames.length;
                                        rsp = rsp3;
                                        int i4 = 0;
                                        while (true) {
                                            if (i4 >= length3) {
                                                break;
                                            }
                                            String pkg = packageNames[i4];
                                            if (pkg.equals(pkgInProcess2)) {
                                                packageName2 = pkg;
                                                break;
                                            }
                                            i4++;
                                            length3 = length3;
                                        }
                                        if (packageName2 != null) {
                                            packageName = packageName2;
                                            break;
                                        }
                                        i3++;
                                        pm = pm;
                                        rsp3 = rsp;
                                    }
                                } else {
                                    rsp = null;
                                }
                            } else {
                                i = 0;
                            }
                        }
                    } else {
                        rsp = null;
                    }
                } catch (Exception e) {
                    OppoRlog.Rlog.e("OppoSmsCommonUtils", "oemGetPackageNameViaProcessId-error");
                    return callingPackage;
                }
            } else {
                rsp = null;
            }
            if (packageName != null) {
                rsp2 = packageName;
            } else if (packageNames.length > 0) {
                rsp2 = packageNames[0];
            } else {
                rsp2 = rsp;
            }
            OppoRlog.Rlog.d("OppoSmsCommonUtils", "getPackageNameViaProcessId: " + rsp2);
            return rsp2;
        } catch (Exception e2) {
            OppoRlog.Rlog.e("OppoSmsCommonUtils", "oemGetPackageNameViaProcessId-error");
            return callingPackage;
        }
    }

    public static boolean oemAllowMmsWhenDataDisableInRoamingForDcTracker(ApnContext apnContext) {
        try {
            if (!isVersionMOVISTAR() || apnContext == null || apnContext.getApnType() == null || !apnContext.getApnType().equals("mms")) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean oemAllowMmsWhenDataDisableInRoamingForDataConnection(DataConnection.ConnectionParams cp, boolean isModemRoaming, boolean allowRoaming) {
        Object cp_getApnContext_getApnType;
        try {
            if (!isVersionMOVISTAR()) {
                return isModemRoaming;
            }
            String apntype = "";
            Object cp_getApnContext = ReflectionHelper.callMethod(cp, "com.android.internal.telephony.dataconnection.DataConnection$ConnectionParams", "getApnContext", new Class[0], new Object[0]);
            if (!(cp == null || cp_getApnContext == null || (cp_getApnContext_getApnType = ReflectionHelper.callMethod(cp_getApnContext, "com.android.internal.telephony.dataconnection.ApnContext", "getApnType", new Class[0], new Object[0])) == null)) {
                apntype = (String) cp_getApnContext_getApnType;
            }
            if (!isModemRoaming || allowRoaming || !apntype.equals("mms")) {
                return isModemRoaming;
            }
            OppoRlog.Rlog.d(NetworkDiagnoseUtils.INFO_OTHER_SMS, "allow mms in MOVISTAR");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return isModemRoaming;
        }
    }

    private static boolean isVersionMOVISTAR() {
        try {
            String operator = SystemProperties.get("ro.oppo.operator", "");
            String region = SystemProperties.get("ro.oppo.regionmark", "");
            String country = SystemProperties.get("ro.oppo.euex.country", "");
            if (TextUtils.isEmpty(operator) || TextUtils.isEmpty(region) || TextUtils.isEmpty(country) || !"EUEX".equals(region) || !"ES".equals(country)) {
                return false;
            }
            if ("MOVISTAR".equals(operator) || "MOVISTAR_LITE".equals(operator)) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean oemAllowDataWhenDataswitchOffOrDataroamingOffWhenMms(DcTracker dc, Phone phone) {
        if (dc == null || phone == null) {
            return true;
        }
        try {
            if (!phone.getDataEnabledSettings().isUserDataEnabled()) {
                return false;
            }
            if (!phone.getServiceState().getDataRoaming() || dc.getDataRoamingEnabled()) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
