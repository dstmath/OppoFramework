package com.mediatek.cta;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.util.ArrayMap;
import java.util.List;
import java.util.Map;

public class CtaManager {
    private static String PLATFORM_PACKAGE_NAME = "android";

    public enum ActionType {
        CALL_RECORDING,
        LOCAL_RECORDING,
        BACKGROUND_SCREENSHOTS,
        TAKE_PICTUREORVIDEO,
        RECEIVE_SMS,
        READ_SMS,
        SEND_SMS,
        MODIFY_SMS,
        DELETE_SMS,
        READ_MMS,
        DELETE_MMS,
        MODIFY_MMS,
        SEND_MMS,
        USE_LOCATION,
        READ_LOCATION_INFO,
        READ_LOCAL_ACCOUNTS,
        READ_CONTACTS,
        MODIFY_CONTACTS,
        DELETE_CONTACTS,
        READ_CALLLOG,
        MODIFY_CALLLOG,
        DELETE_CALLLOG,
        READ_CALENDAR,
        DELEATE_CALENDAR,
        MODIFY_CALENDAR,
        DIRECTLY_CALL_PHONE,
        READ_BROWSER_HISTORY,
        READ_BROWSER_BOOKMARK,
        WIFI_DATATRANSTER,
        MOBILE_DATATRANSTER,
        WIRELESS_TRANSMITDATA,
        ENABLE_WIFI_NETWORKCONNECT,
        ENABLE_MOBILE_NETWORKCONNECT,
        READ_PICTURES,
        READ_VIDEOS,
        READ_AUDIOS
    }

    public enum KeywordType {
        MICROPHONE,
        SCREENSHOTS,
        CAMERA,
        SMS,
        MMS,
        LOCATION,
        LOCALACCOUNT,
        CONTACTS,
        CALLLOG,
        CALENDAR,
        PHONE,
        BROWSER,
        DATATRANSTER,
        NETWORKCONNECT,
        PICTURES,
        VIDEOS,
        AUDIOS
    }

    public void createCtaPermsController(Context context) {
    }

    public void linkCtaPermissions(PackageParser.Package pkg) {
    }

    public void reportPermRequestUsage(String permName, int uid) {
    }

    public void shutdown() {
    }

    public void systemReady() {
    }

    public boolean isPermissionReviewRequired(PackageParser.Package pkg, int userId, boolean reviewRequiredByCache) {
        return false;
    }

    public List<String> getPermRecordPkgs() {
        return null;
    }

    public List<String> getPermRecordPerms(String packageName) {
        return null;
    }

    public List<Long> getRequestTimes(String packageName, String permName) {
        return null;
    }

    public boolean showPermErrorDialog(Context context, int uid, String processName, String pkgName, String exceptionMsg) {
        return false;
    }

    public void filterReceiver(Context context, Intent intent, List<ResolveInfo> list, int userId) {
    }

    public boolean isCtaSupported() {
        return false;
    }

    public void setCtaSupported(boolean enable) {
    }

    public boolean isCtaOnlyPermission(String perm) {
        return false;
    }

    public boolean isCtaAddedPermGroup(String group) {
        return false;
    }

    public boolean isCtaMonitoredPerms(String perm) {
        return false;
    }

    public boolean isPlatformPermissionGroup(String pkgName, String groupName) {
        if (pkgName == null || !PLATFORM_PACKAGE_NAME.equals(pkgName)) {
            return false;
        }
        return true;
    }

    public String[] getCtaAddedPermissionGroups() {
        return null;
    }

    public boolean enforceCheckPermission(String permission, String action) {
        return false;
    }

    public boolean enforceCheckPermission(String pkgName, String permission, String action) {
        return false;
    }

    public boolean isPlatformPermission(String pkgName, String permName) {
        if (pkgName == null || !PLATFORM_PACKAGE_NAME.equals(pkgName)) {
            return false;
        }
        return true;
    }

    public boolean isSystemApp(Context context, String pkgName) {
        return false;
    }

    public boolean needGrantCtaRuntimePerm(boolean isUpdated, int targetSdkVersion) {
        return false;
    }

    public String[] getCtaOnlyPermissions() {
        return null;
    }

    public ArrayMap<String, String> getCtaPlatformPerms() {
        return null;
    }

    public int opToSwitch(int op) {
        return -1;
    }

    public String opToName(int op) {
        return null;
    }

    public String opToPublicName(int op) {
        return null;
    }

    public int strDebugOpToOp(String op) {
        return -1;
    }

    public String opToPermission(int op) {
        return null;
    }

    public String opToRestriction(int op) {
        return null;
    }

    public int permissionToOpCode(String permission) {
        return -1;
    }

    public boolean opAllowSystemBypassRestriction(int op) {
        return false;
    }

    public int opToDefaultMode(int op) {
        return -1;
    }

    public boolean opAllowsReset(int op) {
        return false;
    }

    public String permissionToOp(String permission) {
        return null;
    }

    public int strOpToOp(String op) {
        return -1;
    }

    public String getsOpToString(int op) {
        return null;
    }

    public String[] getOpStrs() {
        return null;
    }

    public boolean needClearReviewFlagAfterUpgrade(boolean updatedPkgReviewRequired, String pkg, String name) {
        return false;
    }

    public int getOpNum() {
        return 0;
    }

    public boolean checkAutoBootPermission(Context context, String packageName, int userId) {
        return true;
    }

    public void printCtaInfor(Context context, KeywordType keyWordType, String functionName, ActionType actionType, String parameter) {
    }

    public void changeAppAutoBootStatus(Context context, String packageName, boolean status, int userId) {
    }

    public Map<String, Boolean> queryAutoBootRecords(Context context, int userId) {
        return null;
    }

    public void addAutoBootService(Context context) {
    }

    public void printCtaInfor(int callingPid, int callingUid, KeywordType keyWordType, String functionName, ActionType actionType, String parameter) {
    }

    public void printCtaInfor(int appUid, KeywordType keyWordType, String functionName, ActionType actionType, String parameter) {
    }
}
