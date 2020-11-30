package com.oppo.enterprise.mdmcoreservice.utils.permission;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.database.Cursor;
import android.net.IConnectivityManager;
import android.net.Uri;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceRestrictionManager;
import com.oppo.enterprise.mdmcoreservice.utils.permission.DataBaseUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PermissionUtils {
    public static final int ACCEPT = 0;
    public static final int ACCESS_MEDIA_PROVIDER = 43;
    public static final int ADD_VOICEMAIL = 35;
    public static final String AUTHORITY = "com.coloros.provider.PermissionProvider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.coloros.provider.PermissionProvider");
    public static final int BIND_VPN = 44;
    public static final int BODY_SENSORS = 41;
    public static final int CALL_TRANSFER = 45;
    public static final int CAMERA = 22;
    public static final int CHANGE_BT_STATE = 20;
    public static final int CHANGE_GPRS_STATE = 18;
    public static final int CHANGE_NFC_STATE = 24;
    public static final int CHANGE_WIFI_STATE = 19;
    private static final int CHILDREN_MODE_OFF = 0;
    private static final int CHILDREN_MODE_ON = 1;
    private static final String DCIMPROTECT_MANGER_FILE = (File.separator + PermissionConstants.STATISTIC_DATA + File.separator + "oppo" + File.separator + "coloros" + File.separator + DataBaseUtil.SafeStatusManage.MODULE_PERMISSION + File.separator + "oppo_dcim_allow.txt");
    private static final String DCIMPROTECT_MANGER_PATH = (File.separator + PermissionConstants.STATISTIC_DATA + File.separator + "oppo" + File.separator + "coloros" + File.separator + DataBaseUtil.SafeStatusManage.MODULE_PERMISSION + File.separator);
    public static final int DELETE_CALENDAR = 32;
    public static final int DELETE_CALL = 28;
    public static final int DELETE_CONTACTS = 29;
    public static final int DELETE_MMS = 31;
    public static final int DELETE_SMS = 30;
    public static final int FILE_MODE_APPEND = 1;
    public static final int FILE_MODE_DELELE = 2;
    public static final int GET_ACCOUNTS = 33;
    public static final int GPS = 21;
    public static final int GROUP_ACCEPT = 0;
    public static final int GROUP_NONE = -1;
    public static final int GROUP_PROMPT = 2;
    public static final int GROUP_REJECT = 1;
    public static final int ID = 0;
    public static final int NONE = -1;
    public static final String PACKAGE_INSTALLER_NAME = "com.android.packageinstaller";
    public static final int PACKAGE_NAME = 1;
    public static final String PERMISSION_ACCESS_MEDIA_PROVIDER = "android.permission.ACCESS_MEDIA_PROVIDER";
    public static final String PERMISSION_CALL_FORWARDING = "oppo.permission.call.FORWARDING";
    public static final String PERMISSION_DELETE_CALENDAR = "android.permission.WRITE_CALENDAR_DELETE";
    public static final String PERMISSION_DELETE_CALL = "android.permission.WRITE_CALL_LOG_DELETE";
    public static final String PERMISSION_DELETE_CONTACTS = "android.permission.WRITE_CONTACTS_DELETE";
    public static final String PERMISSION_DELETE_MMS = "android.permission.WRITE_MMS_DELETE";
    public static final String PERMISSION_DELETE_SMS = "android.permission.WRITE_SMS_DELETE";
    public static final String PERMISSION_WR_EXTERNAL_STORAGE = "android.permission.WR_EXTERNAL_STORAGE";
    public static final int PHONE_CALL = 7;
    public static final int PROCESS_OUTGOING_CALLS = 37;
    public static final int PROMPT = 2;
    public static final int READ_BROWSER = 25;
    public static final int READ_CALENDAR = 26;
    public static final int READ_CALL_LOG = 8;
    public static final int READ_CONTACTS = 10;
    public static final int READ_MMS = 13;
    public static final String READ_MMS_PERMISSION = "android.permission.READ_MMS";
    public static final int READ_PHONE_STATE = 34;
    public static final int READ_SMS = 12;
    public static final int RECEIVE_MMS = 39;
    public static final int RECEIVE_SMS = 38;
    public static final int RECEIVE_WAP_PUSH = 40;
    public static final int RECORD_AUDIO = 23;
    public static final int REJECT = 1;
    public static final int SEND_MMS = 15;
    public static final String SEND_MMS_PERMISSION = "android.permission.SEND_MMS";
    public static final int SEND_SMS = 14;
    private static final String SETTING_CHILDREN_MODE = "children_mode_on";
    private static final String SETTING_CHILDREN_MODE_FORBID_SMS = "children_mode_forbid_sms_on";
    public static final int STATE = 5;
    public static final int STATE_NONE = -1;
    public static final int STATE_OK = 1;
    private static final String TAG = "PermissionUtils";
    public static final int TRUST = 6;
    public static final Uri URI_PACKAGEINSTALLER = Uri.withAppendedPath(AUTHORITY_URI, DataBaseUtil.PackageInstaller.TABLE_PACKAGEINSTALLER_WHITELIST);
    public static final int USE_SIP = 36;
    public static final int WRITE_CALENDAR = 27;
    public static final int WRITE_CALL_LOG = 9;
    public static final int WRITE_CONTACTS = 11;
    public static final int WRITE_MMS = 17;
    public static final String WRITE_MMS_PERMISSION = "android.permission.WRITE_MMS";
    public static final int WRITE_SMS = 16;
    public static final int WR_EXTERNAL_STORAGE = 42;
    public static List<String> sInterceptingPermissions = Arrays.asList(PermissionConstants.PERMISSION_CALL_PHONE, PermissionConstants.PERMISSION_READ_CALL_LOG, PermissionConstants.PERMISSION_READ_CONTACTS, PermissionConstants.PERMISSION_READ_SMS, PermissionConstants.PERMISSION_SEND_SMS, "android.permission.SEND_MMS", PermissionConstants.PERMISSION_GPRS, PermissionConstants.PERMISSION_WIFI, PermissionConstants.PERMISSION_BLUETOOTH, PermissionConstants.PERMISSION_ACCESS, PermissionConstants.PERMISSION_CAMERA, PermissionConstants.PERMISSION_RECORD_AUDIO, PermissionConstants.PERMISSION_NFC, PermissionConstants.PERMISSION_WRITE_CALL_LOG, PermissionConstants.PERMISSION_WRITE_CONTACTS, PermissionConstants.PERMISSION_WRITE_SMS, "android.permission.WRITE_MMS", "android.permission.READ_MMS", PermissionConstants.PERMISSION_READ_HISTORY_BOOKMARKS, PermissionConstants.PERMISSION_READ_CALENDAR, PermissionConstants.PERMISSION_WRITE_CALENDAR, "android.permission.WRITE_CALL_LOG_DELETE", "android.permission.WRITE_CONTACTS_DELETE", "android.permission.WRITE_SMS_DELETE", "android.permission.WRITE_MMS_DELETE", "android.permission.WRITE_CALENDAR_DELETE", PermissionConstants.PERMISSION_GET_ACCOUNTS, PermissionConstants.PERMISSION_READ_PHONE_STATE, PermissionConstants.PERMISSION_ADD_VOICEMAIL, PermissionConstants.PERMISSION_USE_SIP, PermissionConstants.PERMISSION_PROCESS_OUTGOING_CALLS, PermissionConstants.PERMISSION_RECEIVE_SMS, PermissionConstants.PERMISSION_RECEIVE_MMS, PermissionConstants.PERMISSION_RECEIVE_WAP_PUSH, PermissionConstants.PERMISSION_SENSORS, "android.permission.WR_EXTERNAL_STORAGE", "android.permission.ACCESS_MEDIA_PROVIDER", PermissionConstants.PERMISSION_BIND_VPN_SERVICE, "oppo.permission.call.FORWARDING");

    public static void grantAllRuntimePermissions(Context mContext, String packageName) {
        ArrayList<String> list;
        if (packageName != null && !packageName.equals("") && (list = PermissionConstants.getPkgAllPermissionWithFilter(PermissionConstants.getPkgAllPermissionFromDb(mContext, packageName))) != null && list.size() > 0) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String s = it.next();
                if (getPermissionState(mContext, packageName, s) != 0) {
                    grantAppPermissionWithChoice(mContext, packageName, s, 0);
                }
            }
        }
    }

    public static void grantOrForbidAllRuntimePermissions(Context mContext, String packageName, int forbidStatus) {
        ArrayList<String> list;
        if (packageName != null && !packageName.trim().equals("") && (list = PermissionConstants.getPkgAllPermissionWithFilter(PermissionConstants.getPkgAllPermissionFromDb(mContext, packageName))) != null && list.size() > 0) {
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String permission = it.next();
                if (getPermissionState(mContext, packageName, permission) != forbidStatus) {
                    if (forbidStatus == 0) {
                        grantAppPermissionWithChoice(mContext, packageName, permission, 0);
                    } else if (forbidStatus == 1) {
                        grantAppPermissionWithChoice(mContext, packageName, permission, 1);
                    } else if (forbidStatus == 2) {
                        grantAppPermissionWithChoice(mContext, packageName, permission, 2);
                    }
                }
            }
        }
    }

    public static boolean hasPermissionRequested(String permission, long valueAccept, long valueReject, long valuePrompt) {
        int value = 2;
        if (0 != (PermissionConstants.getPermissionMark(permission) & valueAccept)) {
            value = 0;
        } else if (0 != (PermissionConstants.getPermissionMark(permission) & valueReject)) {
            value = 1;
        } else if (0 != (PermissionConstants.getPermissionMark(permission) & valuePrompt)) {
            value = 2;
        }
        if (value == 0 || value == 1) {
            return true;
        }
        return false;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static int getColumnIndexByPermission(String permission) {
        char c;
        switch (permission.hashCode()) {
            case -2062392374:
                if (permission.equals("android.permission.READ_MMS")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -2062386608:
                if (permission.equals(PermissionConstants.PERMISSION_READ_SMS)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1928411001:
                if (permission.equals(PermissionConstants.PERMISSION_READ_CALENDAR)) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case -1921431796:
                if (permission.equals(PermissionConstants.PERMISSION_READ_CALL_LOG)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -1888586689:
                if (permission.equals(PermissionConstants.PERMISSION_ACCESS)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1479758289:
                if (permission.equals(PermissionConstants.PERMISSION_RECEIVE_WAP_PUSH)) {
                    c = '!';
                    break;
                }
                c = 65535;
                break;
            case -1324895669:
                if (permission.equals(PermissionConstants.PERMISSION_NFC)) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1258973065:
                if (permission.equals("android.permission.WRITE_MMS_DELETE")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case -1238066820:
                if (permission.equals(PermissionConstants.PERMISSION_SENSORS)) {
                    c = '\"';
                    break;
                }
                c = 65535;
                break;
            case -1157035023:
                if (permission.equals(PermissionConstants.PERMISSION_GPRS)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -895679497:
                if (permission.equals(PermissionConstants.PERMISSION_RECEIVE_MMS)) {
                    c = ' ';
                    break;
                }
                c = 65535;
                break;
            case -895673731:
                if (permission.equals(PermissionConstants.PERMISSION_RECEIVE_SMS)) {
                    c = 31;
                    break;
                }
                c = 65535;
                break;
            case -863921796:
                if (permission.equals("oppo.permission.call.FORWARDING")) {
                    c = '&';
                    break;
                }
                c = 65535;
                break;
            case -773959417:
                if (permission.equals("android.permission.ACCESS_MEDIA_PROVIDER")) {
                    c = '$';
                    break;
                }
                c = 65535;
                break;
            case -558143690:
                if (permission.equals(PermissionConstants.PERMISSION_READ_HISTORY_BOOKMARKS)) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -508034306:
                if (permission.equals(PermissionConstants.PERMISSION_BLUETOOTH)) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -309971444:
                if (permission.equals("android.permission.WRITE_CALENDAR_DELETE")) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case -272536472:
                if (permission.equals(PermissionConstants.PERMISSION_BIND_VPN_SERVICE)) {
                    c = '%';
                    break;
                }
                c = 65535;
                break;
            case -5573545:
                if (permission.equals(PermissionConstants.PERMISSION_READ_PHONE_STATE)) {
                    c = 27;
                    break;
                }
                c = 65535;
                break;
            case 33282955:
                if (permission.equals("android.permission.WR_EXTERNAL_STORAGE")) {
                    c = '#';
                    break;
                }
                c = 65535;
                break;
            case 52596924:
                if (permission.equals("android.permission.SEND_MMS")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 52602690:
                if (permission.equals(PermissionConstants.PERMISSION_SEND_SMS)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 112197485:
                if (permission.equals(PermissionConstants.PERMISSION_CALL_PHONE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 169391859:
                if (permission.equals("android.permission.WRITE_MMS")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 169397625:
                if (permission.equals(PermissionConstants.PERMISSION_WRITE_SMS)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 214526995:
                if (permission.equals(PermissionConstants.PERMISSION_WRITE_CONTACTS)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 272779126:
                if (permission.equals(PermissionConstants.PERMISSION_WIFI)) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 463403621:
                if (permission.equals(PermissionConstants.PERMISSION_CAMERA)) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 603653886:
                if (permission.equals(PermissionConstants.PERMISSION_WRITE_CALENDAR)) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 610633091:
                if (permission.equals(PermissionConstants.PERMISSION_WRITE_CALL_LOG)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 784519842:
                if (permission.equals(PermissionConstants.PERMISSION_USE_SIP)) {
                    c = 29;
                    break;
                }
                c = 65535;
                break;
            case 952819282:
                if (permission.equals(PermissionConstants.PERMISSION_PROCESS_OUTGOING_CALLS)) {
                    c = 30;
                    break;
                }
                c = 65535;
                break;
            case 1271781903:
                if (permission.equals(PermissionConstants.PERMISSION_GET_ACCOUNTS)) {
                    c = 26;
                    break;
                }
                c = 65535;
                break;
            case 1307461607:
                if (permission.equals("android.permission.WRITE_CALL_LOG_DELETE")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case 1831139720:
                if (permission.equals(PermissionConstants.PERMISSION_RECORD_AUDIO)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 1856913201:
                if (permission.equals("android.permission.WRITE_SMS_DELETE")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case 1977429404:
                if (permission.equals(PermissionConstants.PERMISSION_READ_CONTACTS)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 2099568471:
                if (permission.equals("android.permission.WRITE_CONTACTS_DELETE")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 2133799037:
                if (permission.equals(PermissionConstants.PERMISSION_ADD_VOICEMAIL)) {
                    c = 28;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 7;
            case 1:
                return 8;
            case 2:
                return 9;
            case 3:
                return 10;
            case 4:
                return 11;
            case 5:
                return 12;
            case 6:
                return 13;
            case 7:
                return 14;
            case '\b':
                return 15;
            case '\t':
                return 16;
            case '\n':
                return 17;
            case 11:
                return 18;
            case '\f':
                return 19;
            case '\r':
                return 20;
            case 14:
                return 21;
            case 15:
                return 22;
            case 16:
                return 23;
            case 17:
                return 24;
            case 18:
                return 25;
            case 19:
                return 26;
            case 20:
                return 27;
            case 21:
                return 28;
            case 22:
                return 29;
            case 23:
                return 30;
            case 24:
                return 31;
            case 25:
                return 32;
            case 26:
                return 33;
            case 27:
                return 34;
            case 28:
                return 35;
            case 29:
                return 36;
            case 30:
                return 37;
            case 31:
                return 38;
            case ' ':
                return 39;
            case '!':
                return 40;
            case '\"':
                return 41;
            case '#':
                return 42;
            case '$':
                return 43;
            case '%':
                return 44;
            case '&':
                return 45;
            default:
                return -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x004a, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0054, code lost:
        if (0 == 0) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0057, code lost:
        return r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0048, code lost:
        if (r8 != null) goto L_0x004a;
     */
    private static PackagePermission queryPackagePermissions(Context context, String packageName) {
        Cursor cr = null;
        PackagePermission pkgPermission = null;
        try {
            cr = context.getContentResolver().query(DataBaseUtil.URI_PERMISSION, null, "pkg_name=?", new String[]{packageName}, null);
            if (cr != null && cr.getCount() == 1 && cr.moveToNext()) {
                pkgPermission = new PackagePermission();
                pkgPermission.mPackageName = cr.getString(1);
                pkgPermission.mAccept = cr.getLong(2);
                pkgPermission.mReject = cr.getLong(3);
                pkgPermission.mPrompt = cr.getLong(4);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            if (0 != 0) {
                cr.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007b, code lost:
        if (r2 != null) goto L_0x007d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007d, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008e, code lost:
        if (0 == 0) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0091, code lost:
        return r1;
     */
    public static int getPermissionState(Context context, String packageName, String permission) {
        int i;
        int retValue = -1;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(DataBaseUtil.URI_PERMISSION, null, "pkg_name= ?", new String[]{packageName}, null);
            if (cursor != null && cursor.getCount() >= 1) {
                if (cursor.moveToFirst()) {
                    int permissionCol = getColumnIndexByPermission(permission);
                    int permissionAcceptCol = cursor.getColumnIndex(DataBaseUtil.Permission.COLUMN_ACCEPT);
                    int permissionRejectCol = cursor.getColumnIndex("reject");
                    int permissionPromptCol = cursor.getColumnIndex(DataBaseUtil.Permission.COLUMN_PROMPT);
                    int permissionFlag = -1;
                    if (permissionCol != -1) {
                        permissionFlag = cursor.getInt(permissionCol);
                    }
                    if (permissionFlag == 1) {
                        long valueAccept = cursor.getLong(permissionAcceptCol);
                        long valueReject = cursor.getLong(permissionRejectCol);
                        long valuePrompt = cursor.getLong(permissionPromptCol);
                        if (0 != (PermissionConstants.getPermissionMark(permission) & valueAccept)) {
                            i = 0;
                        } else if (0 != (PermissionConstants.getPermissionMark(permission) & valueReject)) {
                            i = 1;
                        } else if (0 != (PermissionConstants.getPermissionMark(permission) & valuePrompt)) {
                            i = 2;
                        }
                        retValue = i;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    public static boolean isAppSupportsRuntimePermissions(Context context, String packageName) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appInfo == null || appInfo.targetSdkVersion <= 22) {
            return false;
        }
        return true;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:102:0x007a */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:80:0x0122 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:104:0x007a */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r9v3 */
    /* JADX WARN: Type inference failed for: r9v13 */
    public static void syncRuntimePermissionState(Context context, String packageName, String permission, boolean grant, boolean userFixed, boolean policyFixed, boolean systemFixed) {
        Iterator<PermissionInfo> it;
        List<PermissionInfo> permissionInfosByGroup;
        int permissionCount;
        int i;
        Exception e;
        syncOppoHidedPermissionState(context, packageName, permission, grant, userFixed);
        PermissionInfo permissionInfo = null;
        PackageItemInfo groupInfo = null;
        List<PermissionInfo> permissionInfosByGroup2 = null;
        PackageInfo packageInfo = null;
        PackageManager pm = context.getPackageManager();
        String str = permission;
        String nativePermission = PermissionConstants.MAP_OPPO_DEFINED_TO_ORIGINAL.get(str);
        if (nativePermission == null) {
            nativePermission = str;
        }
        try {
            packageInfo = pm.getPackageInfo(packageName, DeviceRestrictionManager.ENABLE_APP_IN_LAUNCHER_FLAG);
        } catch (PackageManager.NameNotFoundException e2) {
            e2.printStackTrace();
        }
        if (packageInfo == null || packageInfo.applicationInfo.targetSdkVersion > 22) {
            int i2 = 0;
            try {
                permissionInfo = pm.getPermissionInfo(nativePermission, 0);
            } catch (PackageManager.NameNotFoundException e3) {
                e3.printStackTrace();
            }
            int i3 = 1;
            if (permissionInfo != null && (permissionInfo.protectionLevel & 15) == 1) {
                groupInfo = permissionInfo;
                if (permissionInfo.group != null) {
                    try {
                        groupInfo = pm.getPermissionGroupInfo(permissionInfo.group, 0);
                    } catch (PackageManager.NameNotFoundException e4) {
                        e4.printStackTrace();
                    }
                }
            }
            if (groupInfo instanceof PermissionGroupInfo) {
                try {
                    permissionInfosByGroup2 = pm.queryPermissionsByGroup(groupInfo.name, 0);
                } catch (PackageManager.NameNotFoundException e5) {
                    e5.printStackTrace();
                }
            }
            List<PermissionInfo> permissionInfosByGroup3 = permissionInfosByGroup2;
            if (!(packageInfo == null || permissionInfosByGroup3 == null)) {
                Iterator<PermissionInfo> it2 = permissionInfosByGroup3.iterator();
                while (it2.hasNext()) {
                    PermissionInfo onePermissionInfo = it2.next();
                    if ((onePermissionInfo.protectionLevel & 15) == i3) {
                        int permissionCount2 = packageInfo.requestedPermissions.length;
                        int i4 = i2;
                        PermissionInfo onePermissionInfo2 = str;
                        while (i4 < permissionCount2) {
                            if (packageInfo.requestedPermissions[i4].equals(onePermissionInfo.name)) {
                                try {
                                    UserHandle userHandle = new UserHandle(context.getUserId());
                                    int permissionFlags = pm.getPermissionFlags(onePermissionInfo.name, packageName, userHandle);
                                    if ((permissionFlags & 16) != 0) {
                                        i = i4;
                                        onePermissionInfo2 = onePermissionInfo;
                                        it = it2;
                                        permissionCount = permissionCount2;
                                        permissionInfosByGroup = permissionInfosByGroup3;
                                    } else if ((permissionFlags & 4) != 0) {
                                        i = i4;
                                        onePermissionInfo2 = onePermissionInfo;
                                        it = it2;
                                        permissionCount = permissionCount2;
                                        permissionInfosByGroup = permissionInfosByGroup3;
                                    } else {
                                        if (grant) {
                                            try {
                                                pm.grantRuntimePermission(packageName, onePermissionInfo.name, userHandle);
                                            } catch (Exception e6) {
                                                e = e6;
                                                i = i4;
                                                onePermissionInfo2 = onePermissionInfo;
                                                it = it2;
                                                permissionCount = permissionCount2;
                                                permissionInfosByGroup = permissionInfosByGroup3;
                                            }
                                        } else {
                                            pm.revokeRuntimePermission(packageName, onePermissionInfo.name, userHandle);
                                        }
                                        if (systemFixed) {
                                            i = i4;
                                            try {
                                                it = it2;
                                                permissionCount = permissionCount2;
                                                permissionInfosByGroup = permissionInfosByGroup3;
                                                try {
                                                    pm.updatePermissionFlags(onePermissionInfo.name, packageName, 16, 16, userHandle);
                                                    onePermissionInfo2 = onePermissionInfo;
                                                } catch (Exception e7) {
                                                    e = e7;
                                                    onePermissionInfo2 = onePermissionInfo;
                                                    e.printStackTrace();
                                                    i4 = i + 1;
                                                    onePermissionInfo = onePermissionInfo2;
                                                    permissionCount2 = permissionCount;
                                                    permissionInfosByGroup3 = permissionInfosByGroup;
                                                    it2 = it;
                                                    onePermissionInfo2 = permission;
                                                }
                                            } catch (Exception e8) {
                                                e = e8;
                                                it = it2;
                                                permissionCount = permissionCount2;
                                                permissionInfosByGroup = permissionInfosByGroup3;
                                                onePermissionInfo2 = onePermissionInfo;
                                                e.printStackTrace();
                                                i4 = i + 1;
                                                onePermissionInfo = onePermissionInfo2;
                                                permissionCount2 = permissionCount;
                                                permissionInfosByGroup3 = permissionInfosByGroup;
                                                it2 = it;
                                                onePermissionInfo2 = permission;
                                            }
                                        } else {
                                            i = i4;
                                            it = it2;
                                            permissionCount = permissionCount2;
                                            permissionInfosByGroup = permissionInfosByGroup3;
                                            if (policyFixed) {
                                                try {
                                                    onePermissionInfo2 = onePermissionInfo;
                                                    try {
                                                        pm.updatePermissionFlags(onePermissionInfo.name, packageName, 4, 4, userHandle);
                                                    } catch (Exception e9) {
                                                        e = e9;
                                                        onePermissionInfo2 = onePermissionInfo2;
                                                        e.printStackTrace();
                                                        i4 = i + 1;
                                                        onePermissionInfo = onePermissionInfo2;
                                                        permissionCount2 = permissionCount;
                                                        permissionInfosByGroup3 = permissionInfosByGroup;
                                                        it2 = it;
                                                        onePermissionInfo2 = permission;
                                                    }
                                                } catch (Exception e10) {
                                                    e = e10;
                                                    onePermissionInfo2 = onePermissionInfo;
                                                    e.printStackTrace();
                                                    i4 = i + 1;
                                                    onePermissionInfo = onePermissionInfo2;
                                                    permissionCount2 = permissionCount;
                                                    permissionInfosByGroup3 = permissionInfosByGroup;
                                                    it2 = it;
                                                    onePermissionInfo2 = permission;
                                                }
                                            } else {
                                                onePermissionInfo2 = onePermissionInfo;
                                                if (userFixed) {
                                                    pm.updatePermissionFlags(onePermissionInfo2.name, packageName, 3, 2, userHandle);
                                                } else {
                                                    pm.updatePermissionFlags(onePermissionInfo2.name, packageName, 2, 0, userHandle);
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e11) {
                                    e = e11;
                                    i = i4;
                                    onePermissionInfo2 = onePermissionInfo;
                                    it = it2;
                                    permissionCount = permissionCount2;
                                    permissionInfosByGroup = permissionInfosByGroup3;
                                    e.printStackTrace();
                                    i4 = i + 1;
                                    onePermissionInfo = onePermissionInfo2;
                                    permissionCount2 = permissionCount;
                                    permissionInfosByGroup3 = permissionInfosByGroup;
                                    it2 = it;
                                    onePermissionInfo2 = permission;
                                }
                            } else {
                                i = i4;
                                onePermissionInfo2 = onePermissionInfo;
                                it = it2;
                                permissionCount = permissionCount2;
                                permissionInfosByGroup = permissionInfosByGroup3;
                            }
                            i4 = i + 1;
                            onePermissionInfo = onePermissionInfo2;
                            permissionCount2 = permissionCount;
                            permissionInfosByGroup3 = permissionInfosByGroup;
                            it2 = it;
                            onePermissionInfo2 = permission;
                        }
                        str = permission;
                        i2 = 0;
                        i3 = 1;
                    }
                }
            }
        }
    }

    private static void syncOppoHidedPermissionState(Context context, String packageName, String permission, boolean grant, boolean userFixed) {
        int state;
        if (!PermissionConstants.HIDED_PERMISSIONS.contains(permission)) {
            List<String> permissionGroup = new ArrayList<>();
            Iterator<Map.Entry<String, List<String>>> it = PermissionConstants.MAP_DANGEROUS_PERMISSON_GROUP.entrySet().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                List<String> group = it.next().getValue();
                if (group.contains(permission)) {
                    permissionGroup = group;
                    break;
                }
            }
            List<String> packagePermissions = PermissionConstants.getPkgAllPermissionFromDb(context, packageName);
            packagePermissions.retainAll(permissionGroup);
            packagePermissions.retainAll(PermissionConstants.HIDED_PERMISSIONS);
            if (!packagePermissions.isEmpty()) {
                if (grant) {
                    state = 0;
                } else if (userFixed) {
                    state = 1;
                } else {
                    state = 2;
                }
                for (String hidedPermission : packagePermissions) {
                    updatePermissionChoice(context, packageName, hidedPermission, state);
                }
            }
        }
    }

    public static void doSyncForRuntimePermission(Context context, String packageName, String permission) {
        if (context != null && packageName != null && permission != null) {
            switch (getPermissionGroupState(context, packageName, permission)) {
                case -1:
                default:
                    return;
                case 0:
                    syncRuntimePermissionState(context, packageName, permission, true, false, false, false);
                    return;
                case 1:
                    syncRuntimePermissionState(context, packageName, permission, false, true, false, false);
                    return;
                case 2:
                    syncRuntimePermissionState(context, packageName, permission, false, false, false, false);
                    return;
            }
        }
    }

    public static int getPermissionGroupState(Context context, String packageName, String permission) {
        int permissionState;
        List<Integer> listGroupStates = new ArrayList<>();
        Iterator<Map.Entry<String, List<String>>> it = PermissionConstants.MAP_DANGEROUS_PERMISSON_GROUP.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            List<String> listPermission = it.next().getValue();
            if (listPermission.contains(permission)) {
                for (int index = 0; index < listPermission.size(); index++) {
                    String onePermission = listPermission.get(index);
                    if (!PermissionConstants.HIDED_PERMISSIONS.contains(onePermission) && (permissionState = getPermissionState(context, packageName, onePermission)) != -1 && !listGroupStates.contains(Integer.valueOf(permissionState))) {
                        listGroupStates.add(Integer.valueOf(permissionState));
                    }
                }
            }
        }
        if (listGroupStates.size() > 1) {
            return 0;
        }
        if (listGroupStates.size() != 1) {
            return -1;
        }
        int permissionState2 = listGroupStates.get(0).intValue();
        if (permissionState2 == 0) {
            return 0;
        }
        if (permissionState2 == 1) {
            return 1;
        }
        if (permissionState2 == 2) {
            return 2;
        }
        return -1;
    }

    public static int getPermissionGroupState(Context context, String packageName, String groupName, List<String> permissions) {
        int permissionState;
        if (context == null || packageName == null || groupName == null || permissions == null) {
            return -1;
        }
        List<Integer> listGroupStates = new ArrayList<>();
        if (PermissionConstants.MAP_DANGEROUS_PERMISSON_GROUP.containsKey(groupName)) {
            List<String> allGroupPermissions = PermissionConstants.MAP_DANGEROUS_PERMISSON_GROUP.get(groupName);
            for (String singlePermission : permissions) {
                if (allGroupPermissions.contains(singlePermission) && (permissionState = getPermissionState(context, packageName, singlePermission)) != -1 && !listGroupStates.contains(Integer.valueOf(permissionState))) {
                    listGroupStates.add(Integer.valueOf(permissionState));
                }
            }
        }
        if (listGroupStates.size() > 1) {
            return 0;
        }
        if (listGroupStates.size() != 1) {
            return -1;
        }
        int permissionState2 = listGroupStates.get(0).intValue();
        if (permissionState2 == 0) {
            return 0;
        }
        if (permissionState2 == 1) {
            return 1;
        }
        if (permissionState2 == 2) {
            return 2;
        }
        return -1;
    }

    public static void doSyncForRuntimePermissionEx(Context context, String packageName, String currentPermission, int currentPermissionState) {
        if (context != null && packageName != null && currentPermission != null) {
            switch (getPremissionGroupStateEx(context, packageName, currentPermission, currentPermissionState)) {
                case -1:
                default:
                    return;
                case 0:
                    syncRuntimePermissionState(context, packageName, currentPermission, true, false, false, false);
                    return;
                case 1:
                    syncRuntimePermissionState(context, packageName, currentPermission, false, true, false, false);
                    return;
                case 2:
                    syncRuntimePermissionState(context, packageName, currentPermission, false, false, false, false);
                    return;
            }
        }
    }

    public static void doSyncForRuntimePermissionWithFix(Context context, String packageName, String currentPermission, int currentPermissionState, boolean policyFixed, boolean systemFixed) {
        if (context != null && packageName != null && currentPermission != null) {
            switch (currentPermissionState) {
                case -1:
                default:
                    return;
                case 0:
                    syncRuntimePermissionState(context, packageName, currentPermission, true, false, policyFixed, systemFixed);
                    return;
                case 1:
                    syncRuntimePermissionState(context, packageName, currentPermission, false, false, policyFixed, systemFixed);
                    return;
                case 2:
                    syncRuntimePermissionState(context, packageName, currentPermission, false, false, policyFixed, systemFixed);
                    return;
            }
        }
    }

    public static void doSyncForRuntimePermissionByGroup(Context context, String packageName, String currentPermission, int groupState) {
        if (context != null && packageName != null && currentPermission != null) {
            switch (groupState) {
                case -1:
                default:
                    return;
                case 0:
                    syncRuntimePermissionState(context, packageName, currentPermission, true, false, false, false);
                    return;
                case 1:
                    syncRuntimePermissionState(context, packageName, currentPermission, false, true, false, false);
                    return;
                case 2:
                    syncRuntimePermissionState(context, packageName, currentPermission, false, false, false, false);
                    return;
            }
        }
    }

    public static void doSyncForRuntimePermissionFromSuggestPermission(Context context, String packageName, List<String> listPermissions, boolean isNewInstall) {
        int groupState;
        List<Integer> listGroupStates = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : PermissionConstants.MAP_DANGEROUS_PERMISSION_GROUP_WITH_HIDE.entrySet()) {
            List<String> listOneGroupPermission = entry.getValue();
            listGroupStates.clear();
            for (String permission : listPermissions) {
                if (listOneGroupPermission.contains(permission)) {
                    int permissionState = getPermissionState(context, packageName, permission);
                    if (permissionState != -1 && !listGroupStates.contains(Integer.valueOf(permissionState))) {
                        listGroupStates.add(Integer.valueOf(permissionState));
                    }
                }
            }
            if (listGroupStates.size() > 1) {
                groupState = 0;
            } else if (listGroupStates.size() == 1) {
                int permissionState2 = listGroupStates.get(0).intValue();
                if (permissionState2 == 0) {
                    groupState = 0;
                } else if (permissionState2 == 1) {
                    groupState = 1;
                } else if (permissionState2 == 2) {
                    groupState = 2;
                } else {
                    groupState = -1;
                }
            } else {
                groupState = -1;
            }
            switch (groupState) {
                case 0:
                    syncRuntimePermissionState(context, packageName, listOneGroupPermission.get(0), true, false, false, false);
                    break;
                case 1:
                    syncRuntimePermissionState(context, packageName, listOneGroupPermission.get(0), false, true, false, false);
                    break;
                case 2:
                    if (!isNewInstall) {
                        syncRuntimePermissionState(context, packageName, listOneGroupPermission.get(0), false, false, false, false);
                        break;
                    } else {
                        break;
                    }
            }
        }
    }

    public static int getPremissionGroupStateEx(Context context, String packageName, String currentPermission, int currentPermissionState) {
        int permissionState;
        List<Integer> listGroupStates = new ArrayList<>();
        Iterator<Map.Entry<String, List<String>>> it = PermissionConstants.MAP_DANGEROUS_PERMISSION_GROUP_WITH_HIDE.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            List<String> listPermission = it.next().getValue();
            if (listPermission.contains(currentPermission)) {
                for (int index = 0; index < listPermission.size(); index++) {
                    String onePermission = listPermission.get(index);
                    if (onePermission.equals(currentPermission)) {
                        permissionState = currentPermissionState;
                    } else {
                        permissionState = getPermissionState(context, packageName, onePermission);
                    }
                    if (permissionState != -1 && !listGroupStates.contains(Integer.valueOf(permissionState))) {
                        listGroupStates.add(Integer.valueOf(permissionState));
                    }
                }
            }
        }
        if (listGroupStates.size() > 1) {
            return 0;
        }
        if (listGroupStates.size() != 1) {
            return -1;
        }
        int permissionState2 = listGroupStates.get(0).intValue();
        if (permissionState2 == 0) {
            return 0;
        }
        if (permissionState2 == 1) {
            return 1;
        }
        if (permissionState2 == 2) {
            return 2;
        }
        return -1;
    }

    public static void doSyncForRuntimePermissionOKO(Context context, String packageName) {
        int permissionGroupState;
        List<Integer> listGroupStates = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : PermissionConstants.MAP_DANGEROUS_PERMISSON_GROUP.entrySet()) {
            List<String> listPermission = entry.getValue();
            listGroupStates.clear();
            String onePermission = null;
            for (int index = 0; index < listPermission.size(); index++) {
                onePermission = listPermission.get(index);
                int permissionState = getPermissionState(context, packageName, onePermission);
                if (permissionState != -1 && !listGroupStates.contains(Integer.valueOf(permissionState))) {
                    listGroupStates.add(Integer.valueOf(permissionState));
                }
            }
            if (listGroupStates.size() > 1) {
                permissionGroupState = 0;
            } else if (listGroupStates.size() == 1) {
                int permissionState2 = listGroupStates.get(0).intValue();
                if (permissionState2 == 0) {
                    permissionGroupState = 0;
                } else if (permissionState2 == 1) {
                    permissionGroupState = 1;
                } else if (permissionState2 == 2) {
                    permissionGroupState = 2;
                } else {
                    permissionGroupState = -1;
                }
            } else {
                permissionGroupState = -1;
            }
            syncForRuntimePermissionOKO(context, packageName, onePermission, permissionGroupState);
        }
    }

    public static void syncForRuntimePermissionOKO(Context context, String packageName, String currentPermission, int permissionGroupState) {
        if (context != null && packageName != null && currentPermission != null) {
            switch (permissionGroupState) {
                case -1:
                default:
                    return;
                case 0:
                    syncRuntimePermissionState(context, packageName, currentPermission, true, false, false, false);
                    return;
                case 1:
                    syncRuntimePermissionState(context, packageName, currentPermission, false, true, false, false);
                    return;
                case 2:
                    syncRuntimePermissionState(context, packageName, currentPermission, false, false, false, false);
                    return;
            }
        }
    }

    public static int checkDB(Context context, String mPackageName, String permission) {
        int mPermissionValue = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(DataBaseUtil.URI_PERMISSION, null, "pkg_name= ?", new String[]{mPackageName}, null);
            if (cursor == null) {
                return 0;
            }
            while (cursor.moveToNext()) {
                int permissionAcceptCol = cursor.getColumnIndex(DataBaseUtil.Permission.COLUMN_ACCEPT);
                int permissionRejectCol = cursor.getColumnIndex("reject");
                int permissionPromptCol = cursor.getColumnIndex(DataBaseUtil.Permission.COLUMN_PROMPT);
                long valueAccept = cursor.getLong(permissionAcceptCol);
                long valueReject = cursor.getLong(permissionRejectCol);
                long valuePrompt = cursor.getLong(permissionPromptCol);
                if (0 != (PermissionConstants.getPermissionMark(permission) & valueAccept)) {
                    mPermissionValue = 0;
                }
                if (0 != (PermissionConstants.getPermissionMark(permission) & valueReject)) {
                    mPermissionValue = 1;
                }
                if (0 != (PermissionConstants.getPermissionMark(permission) & valuePrompt)) {
                    mPermissionValue = 2;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return mPermissionValue;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004d, code lost:
        if (0 != 0) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004f, code lost:
        r0.clear();
        r0.addAll(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0069, code lost:
        if (1 != 0) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006c, code lost:
        return r0;
     */
    public static List<String> readDCIMProtectAllowListFile() {
        List<String> mDCIMProtectAllowList = new ArrayList<>();
        File dcimProtectAllowFile = new File(DCIMPROTECT_MANGER_FILE);
        if (!dcimProtectAllowFile.exists()) {
            Log.e(TAG, "dcimProtectAllowFile isn't exist!");
        }
        List<String> allowList = new ArrayList<>();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(dcimProtectAllowFile);
            BufferedReader reader = new BufferedReader(fr2);
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    try {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (!TextUtils.isEmpty(line)) {
                    allowList.add(line.trim());
                }
            }
            fr2.close();
        } catch (Exception e2) {
            e2.printStackTrace();
            if (0 != 0) {
                try {
                    fr.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fr.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
            if (1 == 0) {
                mDCIMProtectAllowList.clear();
                mDCIMProtectAllowList.addAll(allowList);
            }
            throw th;
        }
    }

    public static void saveDCIMProtectAllowOption(String packageName, int mode) {
        List<String> mDCIMProtectAllowList = readDCIMProtectAllowListFile();
        if (mDCIMProtectAllowList.contains(packageName) && (2 & (~mode)) == 0) {
            mDCIMProtectAllowList.remove(packageName);
        } else if (!mDCIMProtectAllowList.contains(packageName) && (1 & (~mode)) == 0) {
            mDCIMProtectAllowList.add(packageName);
        }
        FileOutputStream fos = null;
        try {
            File path = new File(DCIMPROTECT_MANGER_PATH);
            File file = new File(DCIMPROTECT_MANGER_FILE);
            if (!file.exists()) {
                path.mkdirs();
                file = new File(DCIMPROTECT_MANGER_FILE);
            }
            FileOutputStream fos2 = new FileOutputStream(file);
            Iterator<String> iterator = mDCIMProtectAllowList.iterator();
            while (iterator.hasNext()) {
                fos2.write((iterator.next() + "\n").getBytes());
            }
            try {
                fos2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            if (0 != 0) {
                fos.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fos.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static void updatePermissionChoice(Context context, String packageName, String permission, int choice) {
        if (packageName != null && permission != null && choice >= 0 && choice <= 2) {
            long permissionMask = getPermissionMask(permission);
            PackagePermission pp = queryPackagePermissions(context, packageName);
            int oldChoice = getPermissionState(context, packageName, permission);
            if (pp != null) {
                changePermissionChoice(pp, permissionMask, choice);
                savePermissionChoice(context, pp);
            }
            Map<String, String> eventMap = new HashMap<>();
            int eventCount = -1;
            if (choice == 0) {
                eventCount = 5;
                try {
                    if (permission.equals("android.permission.ACCESS_MEDIA_PROVIDER")) {
                        saveDCIMProtectAllowOption(packageName, 1);
                        if (oldChoice != choice) {
                            PackageManager pm = context.getPackageManager();
                            ActivityManager am = (ActivityManager) context.getSystemService("activity");
                            int uid = pm.getPackageUid(packageName, 0);
                            if (uid != -1) {
                                am.killUid(uid, "OppoDCIMPermissionChanged");
                            }
                        }
                    } else if (permission.equals(PermissionConstants.PERMISSION_BIND_VPN_SERVICE)) {
                        setVpnPackageAuthorization(packageName, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (choice == 1) {
                eventCount = 6;
                if (permission.equals("android.permission.ACCESS_MEDIA_PROVIDER")) {
                    saveDCIMProtectAllowOption(packageName, 2);
                    if (oldChoice != choice && oldChoice == 0) {
                        PackageManager pm2 = context.getPackageManager();
                        ActivityManager am2 = (ActivityManager) context.getSystemService("activity");
                        int uid2 = pm2.getPackageUid(packageName, 0);
                        if (uid2 != -1) {
                            am2.killUid(uid2, "OppoDCIMPermissionChanged");
                        }
                    }
                } else if (permission.equals(PermissionConstants.PERMISSION_BIND_VPN_SERVICE)) {
                    setVpnPackageAuthorization(packageName, false);
                }
            } else if (permission.equals("android.permission.ACCESS_MEDIA_PROVIDER")) {
                saveDCIMProtectAllowOption(packageName, 2);
                if (oldChoice != choice && oldChoice == 0) {
                    PackageManager pm3 = context.getPackageManager();
                    ActivityManager am3 = (ActivityManager) context.getSystemService("activity");
                    int uid3 = pm3.getPackageUid(packageName, 0);
                    if (uid3 != -1) {
                        am3.killUid(uid3, "OppoDCIMPermissionChanged");
                    }
                }
            } else if (permission.equals(PermissionConstants.PERMISSION_BIND_VPN_SERVICE)) {
                setVpnPackageAuthorization(packageName, false);
            }
            eventMap.put("pkgName", packageName);
            eventMap.put(DataBaseUtil.SafeStatusManage.MODULE_PERMISSION, permission);
            eventMap.put("eventCount", eventCount + "");
        }
    }

    public static void updatePermissionChoiceNoKillUid(Context context, String packageName, String permission, int choice) {
        if (packageName != null && permission != null && choice >= 0 && choice <= 2) {
            long permissionMask = getPermissionMask(permission);
            PackagePermission pp = queryPackagePermissions(context, packageName);
            if (pp != null) {
                changePermissionChoice(pp, permissionMask, choice);
                savePermissionChoice(context, pp);
            }
            Map<String, String> eventMap = new HashMap<>();
            int eventCount = -1;
            if (choice == 0) {
                eventCount = 5;
                try {
                    if (permission.equals("android.permission.ACCESS_MEDIA_PROVIDER")) {
                        saveDCIMProtectAllowOption(packageName, 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (choice == 1) {
                eventCount = 6;
                if (permission.equals("android.permission.ACCESS_MEDIA_PROVIDER")) {
                    saveDCIMProtectAllowOption(packageName, 2);
                }
            } else if (permission.equals("android.permission.ACCESS_MEDIA_PROVIDER")) {
                saveDCIMProtectAllowOption(packageName, 2);
            }
            eventMap.put("pkgName", packageName);
            eventMap.put(DataBaseUtil.SafeStatusManage.MODULE_PERMISSION, permission);
            eventMap.put("eventCount", eventCount + "");
        }
    }

    public static long getPermissionMask(String permission) {
        int index = sInterceptingPermissions.indexOf(permission);
        if (index > -1) {
            return 1 << index;
        }
        return 0;
    }

    private static void changePermissionChoice(PackagePermission packagePermission, long permissionMask, int choice) {
        if (packagePermission != null) {
            if (choice == 0) {
                packagePermission.mAccept |= permissionMask;
                packagePermission.mReject &= ~permissionMask;
                packagePermission.mPrompt &= ~permissionMask;
            } else if (1 == choice) {
                packagePermission.mAccept &= ~permissionMask;
                packagePermission.mReject |= permissionMask;
                packagePermission.mPrompt &= ~permissionMask;
            } else if (2 == choice) {
                packagePermission.mAccept &= ~permissionMask;
                packagePermission.mReject &= ~permissionMask;
                packagePermission.mPrompt |= permissionMask;
            }
        }
    }

    /* access modifiers changed from: private */
    public static class PackagePermission {
        long mAccept;
        String mPackageName;
        long mPrompt;
        long mReject;

        private PackagePermission() {
        }
    }

    private static void savePermissionChoice(Context context, PackagePermission packagePermission) {
        String[] selectionArgs = {packagePermission.mPackageName};
        ContentValues values = new ContentValues();
        values.put(DataBaseUtil.Permission.COLUMN_ACCEPT, Long.valueOf(packagePermission.mAccept));
        values.put("reject", Long.valueOf(packagePermission.mReject));
        values.put(DataBaseUtil.Permission.COLUMN_PROMPT, Long.valueOf(packagePermission.mPrompt));
        try {
            context.getContentResolver().update(DataBaseUtil.URI_PERMISSION, values, "pkg_name=?", selectionArgs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean grantAppPermissionWithChoice(Context context, String packageName, String permission, int choice) {
        if (context == null || packageName == null || packageName.equals("")) {
            return false;
        }
        if (choice < 0 || choice > 2) {
            if (choice == -1) {
                return true;
            }
            return false;
        } else if (permission == null || permission.equals("")) {
            return false;
        } else {
            ArrayList<String> transPermissions = new ArrayList<>();
            transPermissions.add(permission);
            ArrayList<String> transPermissions2 = transPermissionToOppoStyle(context, transPermissions, packageName);
            if (transPermissions2.size() > 0) {
                Iterator<String> it = transPermissions2.iterator();
                while (it.hasNext()) {
                    String spermission = it.next();
                    updatePermissionChoice(context, packageName, spermission, choice);
                    doSyncForRuntimePermissionByGroup(context, packageName, spermission, choice);
                }
            }
            return true;
        }
    }

    public static ArrayList<String> transPermissionToOppoStyle(Context context, ArrayList<String> permissions, String packagename) {
        if (permissions == null || permissions.size() == 0) {
            return new ArrayList<>();
        }
        return PermissionConstants.getPkgAllPermissionWithFilter(PermissionConstants.tranPermissionToOppoInner(context, permissions, packagename));
    }

    public static int getPkgPid(Context context, String packageName) {
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) {
            String[] strArr = runningAppProcessInfo.pkgList;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i < length) {
                    if (strArr[i].equals(packageName)) {
                        return runningAppProcessInfo.pid;
                    }
                    i++;
                }
            }
        }
        return -1;
    }

    public static void setVpnPackageAuthorization(String packageName, boolean allowed) {
        try {
            IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity")).setVpnPackageAuthorization(packageName, UserHandle.myUserId(), allowed);
        } catch (Exception ex) {
            Log.e(TAG, "setVpnPackageAuthorization error");
            ex.printStackTrace();
        }
    }

    public static String permissionToRuntimePermission(String oppoPermission) {
        if ("android.permission.WR_EXTERNAL_STORAGE".equals(oppoPermission)) {
            return "android.permission.WRITE_EXTERNAL_STORAGE";
        }
        if (oppoPermission.contains(PermissionConstants.PERMISSION_WRITE_CALL_LOG)) {
            return PermissionConstants.PERMISSION_WRITE_CALL_LOG;
        }
        if (oppoPermission.contains(PermissionConstants.PERMISSION_WRITE_CONTACTS)) {
            return PermissionConstants.PERMISSION_WRITE_CONTACTS;
        }
        if (oppoPermission.contains(PermissionConstants.PERMISSION_WRITE_SMS)) {
            return PermissionConstants.PERMISSION_WRITE_SMS;
        }
        if (oppoPermission.contains(PermissionConstants.PERMISSION_WRITE_CALENDAR)) {
            return PermissionConstants.PERMISSION_WRITE_CALENDAR;
        }
        return oppoPermission;
    }
}
