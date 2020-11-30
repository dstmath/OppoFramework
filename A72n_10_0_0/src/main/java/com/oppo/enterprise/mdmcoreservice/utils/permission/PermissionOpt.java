package com.oppo.enterprise.mdmcoreservice.utils.permission;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PermissionOpt {
    public static final int ACCEPT = 0;
    public static final int GROUP_ACCEPT = 0;
    public static final String GROUP_CALENDAR = "android.permission-group.CALENDAR";
    public static final String GROUP_CALL_LOG = "android.permission-group.CALL_LOG";
    public static final String GROUP_CAMERA = "android.permission-group.CAMERA";
    public static final String GROUP_CONTACTS = "android.permission-group.CONTACTS";
    public static final String GROUP_LOCATION = "android.permission-group.LOCATION";
    public static final String GROUP_MICROPHONE = "android.permission-group.MICROPHONE";
    public static final int GROUP_NONE = -1;
    public static final String GROUP_PHONE = "android.permission-group.PHONE";
    public static final int GROUP_PROMPT = 2;
    public static final int GROUP_REJECT = 1;
    public static final String GROUP_SENSORS = "android.permission-group.SENSORS";
    public static final String GROUP_SMS = "android.permission-group.SMS";
    public static final String GROUP_STORAGE = "android.permission-group.STORAGE";
    public static final Map<String, List<String>> MAP_DANGEROUS_PERMISSON_GROUP = new HashMap();
    public static final int NONE = -1;
    public static final String PACKAGE_INSTALLER_NAME = "com.android.packageinstaller";
    public static final int PROMPT = 2;
    public static final int REJECT = 1;
    public static final int STATE_NONE = -1;
    public static final int STATE_OK = 1;
    private static final String TAG = "PermissionUtils";

    static {
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.CALENDAR", PermissionConstants.CALENDAR_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.CAMERA", PermissionConstants.CAMERA_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.CONTACTS", PermissionConstants.CONTACTS_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.LOCATION", PermissionConstants.LOCATION_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.MICROPHONE", PermissionConstants.MICROPHONE_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.PHONE", PermissionConstants.PHONE_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.CALL_LOG", PermissionConstants.CALL_LOG_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.SENSORS", PermissionConstants.SENSORS_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.SMS", PermissionConstants.SMS_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.STORAGE", PermissionConstants.STORAGE_PERMISSIONS);
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
                    if (!PermissionConstants.HIDED_PERMISSIONS.contains(onePermission) && (permissionState = PermissionUtils.getPermissionState(context, packageName, onePermission)) != -1 && !listGroupStates.contains(Integer.valueOf(permissionState))) {
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
                if (allGroupPermissions.contains(singlePermission) && (permissionState = PermissionUtils.getPermissionState(context, packageName, singlePermission)) != -1 && !listGroupStates.contains(Integer.valueOf(permissionState))) {
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
}
