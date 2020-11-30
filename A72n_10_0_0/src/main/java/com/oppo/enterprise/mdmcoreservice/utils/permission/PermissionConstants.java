package com.oppo.enterprise.mdmcoreservice.utils.permission;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import com.oppo.enterprise.mdmcoreservice.manager.DeviceRestrictionManager;
import com.oppo.enterprise.mdmcoreservice.utils.permission.DataBaseUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PermissionConstants {
    public static final int ACCEPT = 0;
    public static final String ACTION_OTA_UPDATE = "oppo.intent.action.OPPO_OTA_UPDATE_SUCCESSED";
    public static final String ACTION_RECOVER_UPDATE = "oppo.intent.action.OPPO_RECOVER_UPDATE_SUCCESSED";
    public static final String ACTION_ROM_UPDATE_COMPLETE = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    public static final List<String> BACKGROUND_INTERCEPT_PERMS = new ArrayList();
    public static final List<String> CALENDAR_PERMISSIONS = new ArrayList();
    public static final List<String> CALL_LOG_PERMISSIONS = new ArrayList();
    public static final List<String> CAMERA_PERMISSIONS = new ArrayList();
    public static final String CLOSE_PERMISSION_ACTION = "oppo.intent.action.VIRUS_APK_INSTALLED";
    public static final List<String> CONTACTS_PERMISSIONS = new ArrayList();
    public static final List<String> DELETE_PERMISSIONS = Collections.unmodifiableList(Arrays.asList("android.permission.WRITE_CALL_LOG_DELETE", "android.permission.WRITE_CONTACTS_DELETE", "android.permission.WRITE_SMS_DELETE", "android.permission.WRITE_MMS_DELETE", "android.permission.WRITE_CALENDAR_DELETE"));
    public static final List<String> EXTRA_RUNTIME_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(PERMISSION_PROCESS_OUTGOING_CALLS, PERMISSION_RECEIVE_MMS, PERMISSION_RECEIVE_WAP_PUSH, PERMISSION_SENSORS, PERMISSION_GET_ACCOUNTS, "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"));
    public static final String GROUP_CALENDAR = "android.permission-group.CALENDAR";
    public static final String GROUP_CALL_LOG = "android.permission-group.CALL_LOG";
    public static final String GROUP_CAMERA = "android.permission-group.CAMERA";
    public static final String GROUP_CONTACTS = "android.permission-group.CONTACTS";
    public static final String GROUP_LOCATION = "android.permission-group.LOCATION";
    public static final String GROUP_MICROPHONE = "android.permission-group.MICROPHONE";
    public static final String GROUP_PHONE = "android.permission-group.PHONE";
    public static final String GROUP_SENSORS = "android.permission-group.SENSORS";
    public static final String GROUP_SMS = "android.permission-group.SMS";
    public static final String GROUP_STORAGE = "android.permission-group.STORAGE";
    public static final List<String> HIDED_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(PERMISSION_PROCESS_OUTGOING_CALLS, PERMISSION_RECEIVE_MMS, PERMISSION_RECEIVE_WAP_PUSH, PERMISSION_GET_ACCOUNTS, PERMISSION_USE_SIP, PERMISSION_ADD_VOICEMAIL));
    public static final String KNOWN_MARKET_DIR = (File.separator + STATISTIC_DATA + File.separator + "oppo" + File.separator + "coloros" + File.separator + DataBaseUtil.SafeStatusManage.MODULE_PERMISSION + File.separator);
    public static final String KNOWN_MARKET_FILE_NAME = "known_markets.xml";
    public static final List<String> LOCATION_PERMISSIONS = new ArrayList();
    public static final Map<String, List<String>> MAP_DANGEROUS_PERMISSION_GROUP_WITH_HIDE = new HashMap();
    public static final Map<String, List<String>> MAP_DANGEROUS_PERMISSON_GROUP = new HashMap();
    public static final Map<String, String> MAP_OPPO_DEFINED_TO_ORIGINAL = new HashMap();
    public static final String MARKET_ACTION = "oppo.intent.action.SafeCenter.FILTER_MARKET";
    public static final String MARKET_EXTRA_IS_ADDNEW = "is_addnew";
    public static final String MARKET_EXTRA_IS_BLACK = "is_black";
    public static final String MARKET_EXTRA_IS_MANUALOPEN = "is_manualopen";
    public static final String MARKET_EXTRA_IS_WHITE = "is_white";
    public static final String MARKET_EXTRA_NEW_LIST = "new_list";
    public static final String MARKET_EXTRA_PACKAGE_NAME = "package_name";
    public static final String MARKET_FILTER_FILE_NAME = "market_filter.xml";
    public static final long MASK_BIT_FIRST = 1;
    public static final List<String> MICROPHONE_PERMISSIONS = new ArrayList();
    public static final String[] OPPO_PERMISSION = {PERMISSION_CALL_PHONE, PERMISSION_READ_CALL_LOG, PERMISSION_READ_CONTACTS, PERMISSION_READ_SMS, PERMISSION_SEND_SMS, "android.permission.SEND_MMS", PERMISSION_GPRS, PERMISSION_WIFI, PERMISSION_BLUETOOTH, PERMISSION_ACCESS, PERMISSION_CAMERA, PERMISSION_RECORD_AUDIO, PERMISSION_NFC, PERMISSION_WRITE_CALL_LOG, PERMISSION_WRITE_CONTACTS, PERMISSION_WRITE_SMS, "android.permission.WRITE_MMS", "android.permission.READ_MMS", PERMISSION_READ_HISTORY_BOOKMARKS, PERMISSION_READ_CALENDAR, PERMISSION_WRITE_CALENDAR, "android.permission.WRITE_CALL_LOG_DELETE", "android.permission.WRITE_CONTACTS_DELETE", "android.permission.WRITE_SMS_DELETE", "android.permission.WRITE_MMS_DELETE", "android.permission.WRITE_CALENDAR_DELETE", PERMISSION_GET_ACCOUNTS, PERMISSION_READ_PHONE_STATE, PERMISSION_ADD_VOICEMAIL, PERMISSION_USE_SIP, PERMISSION_PROCESS_OUTGOING_CALLS, PERMISSION_RECEIVE_SMS, PERMISSION_RECEIVE_MMS, PERMISSION_RECEIVE_WAP_PUSH, PERMISSION_SENSORS, "android.permission.WR_EXTERNAL_STORAGE", "android.permission.ACCESS_MEDIA_PROVIDER", PERMISSION_BIND_VPN_SERVICE, "oppo.permission.call.FORWARDING"};
    public static final List<String> OPPO_PERMISSION_LIST = Collections.unmodifiableList(Arrays.asList(OPPO_PERMISSION));
    public static final String PERMISSION_ACCESS = "android.permission.ACCESS_FINE_LOCATION";
    public static final String PERMISSION_ACCESS_MEDIA_PROVIDER = "android.permission.ACCESS_MEDIA_PROVIDER";
    public static final String PERMISSION_ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL";
    public static final String PERMISSION_BIND_VPN_SERVICE = "android.permission.BIND_VPN_SERVICE";
    public static final String PERMISSION_BLUETOOTH = "android.permission.BLUETOOTH_ADMIN";
    public static final String PERMISSION_CALL_FORWARDING = "oppo.permission.call.FORWARDING";
    public static final String PERMISSION_CALL_PHONE = "android.permission.CALL_PHONE";
    public static final String PERMISSION_CAMERA = "android.permission.CAMERA";
    public static final String PERMISSION_DELETE_CALENDAR = "android.permission.WRITE_CALENDAR_DELETE";
    public static final String PERMISSION_DELETE_CALL = "android.permission.WRITE_CALL_LOG_DELETE";
    public static final String PERMISSION_DELETE_CONTACTS = "android.permission.WRITE_CONTACTS_DELETE";
    public static final String PERMISSION_DELETE_MMS = "android.permission.WRITE_MMS_DELETE";
    public static final String PERMISSION_DELETE_POSTFIX = "_DELETE";
    public static final String PERMISSION_DELETE_SMS = "android.permission.WRITE_SMS_DELETE";
    public static final String PERMISSION_EXTERNAL_STORAGE = "android.permission.WR_EXTERNAL_STORAGE";
    public static final String PERMISSION_GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";
    public static final String PERMISSION_GPRS = "android.permission.CHANGE_NETWORK_STATE";
    public static final List<String> PERMISSION_INDEX_LIST = Collections.unmodifiableList(Arrays.asList(PERMISSION_TO_DB_INDEX));
    public static final String PERMISSION_NFC = "android.permission.NFC";
    public static final String PERMISSION_PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";
    public static final String PERMISSION_READ_CALENDAR = "android.permission.READ_CALENDAR";
    public static final String PERMISSION_READ_CALL_LOG = "android.permission.READ_CALL_LOG";
    public static final String PERMISSION_READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static final String PERMISSION_READ_HISTORY_BOOKMARKS = "com.android.browser.permission.READ_HISTORY_BOOKMARKS";
    public static final String PERMISSION_READ_MMS = "android.permission.READ_MMS";
    public static final String PERMISSION_READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    public static final String PERMISSION_READ_SMS = "android.permission.READ_SMS";
    public static final String PERMISSION_RECEIVE_MMS = "android.permission.RECEIVE_MMS";
    public static final String PERMISSION_RECEIVE_SMS = "android.permission.RECEIVE_SMS";
    public static final String PERMISSION_RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH";
    public static final String PERMISSION_RECORD_AUDIO = "android.permission.RECORD_AUDIO";
    public static final String PERMISSION_SEND_MMS = "android.permission.SEND_MMS";
    public static final String PERMISSION_SEND_MMS_INTERNET = "android.permission.INTERNET";
    public static final String PERMISSION_SEND_SMS = "android.permission.SEND_SMS";
    public static final String PERMISSION_SENSORS = "android.permission.BODY_SENSORS";
    public static final String[] PERMISSION_TO_DB_INDEX = {DataBaseUtil.Permission.COLUMN_CALL, DataBaseUtil.Permission.COLUMN_READ_CALL, DataBaseUtil.Permission.COLUMN_READ_CONTACTS, DataBaseUtil.Permission.COLUMN_READ_SMS, DataBaseUtil.Permission.COLUMN_SEND_SMS, DataBaseUtil.Permission.COLUMN_SEND_MMS, DataBaseUtil.Permission.COLUMN_CHANGE_GPRS_STATE, DataBaseUtil.Permission.COLUMN_CHANGE_WIFI_STATE, DataBaseUtil.Permission.COLUMN_CHANGE_BT_STATE, DataBaseUtil.Permission.COLUMN_GPS_LOCATION, DataBaseUtil.Permission.COLUMN_CAMERA, DataBaseUtil.Permission.COLUMN_RECORD_AUDIO, DataBaseUtil.Permission.COLUMN_CHANGE_NFC_STATE, DataBaseUtil.Permission.COLUMN_WRITE_CALL, DataBaseUtil.Permission.COLUMN_WRITE_CONTACTS, DataBaseUtil.Permission.COLUMN_WRITE_SMS, DataBaseUtil.Permission.COLUMN_WRITE_MMS, DataBaseUtil.Permission.COLUMN_READ_MMS, DataBaseUtil.Permission.COLUMN_READ_BROWSER, DataBaseUtil.Permission.COLUMN_READ_CALENDAR, DataBaseUtil.Permission.COLUMN_WRITE_CALENDAR, DataBaseUtil.Permission.COLUMN_DELETE_CALL, DataBaseUtil.Permission.COLUMN_DELETE_CONTACTS, DataBaseUtil.Permission.COLUMN_DELETE_SMS, DataBaseUtil.Permission.COLUMN_DELETE_MMS, DataBaseUtil.Permission.COLUMN_DELETE_CALENDAR, DataBaseUtil.Permission.COLUMN_GET_ACCOUNTS, DataBaseUtil.Permission.COLUMN_READ_PHONE_STATE, DataBaseUtil.Permission.COLUMN_ADD_VOICEMAIL, DataBaseUtil.Permission.COLUMN_USE_SIP, DataBaseUtil.Permission.COLUMN_PROCESS_OUTGOING_CALLS, DataBaseUtil.Permission.COLUMN_RECEIVE_SMS, DataBaseUtil.Permission.COLUMN_RECEIVE_MMS, DataBaseUtil.Permission.COLUMN_RECEIVE_WAP_PUSH, DataBaseUtil.Permission.COLUMN_BODY_SENSORS, DataBaseUtil.Permission.COLUMN_EXTERNAL_STORAGE, DataBaseUtil.Permission.COLUMN_ACCESS_MEDIA_PROVIDER, DataBaseUtil.Permission.COLUMN_BIND_VPN, DataBaseUtil.Permission.COLUMN_CALL_FORWARDING};
    public static final String PERMISSION_USE_SIP = "android.permission.USE_SIP";
    public static final String PERMISSION_WIFI = "android.permission.CHANGE_WIFI_STATE";
    public static final String PERMISSION_WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";
    public static final String PERMISSION_WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG";
    public static final String PERMISSION_WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    public static final String PERMISSION_WRITE_MMS = "android.permission.WRITE_MMS";
    public static final String PERMISSION_WRITE_SMS = "android.permission.WRITE_SMS";
    public static final List<String> PHONE_PERMISSIONS = new ArrayList();
    public static final String PRESET_APP_FILE_NAME = "preset_apps.xml";
    public static final HashMap<String, String> PRIVACY_PERMISSIONS = new HashMap<>();
    public static final int PROMPT = 2;
    public static final String PROPERTY_PERMISSION_ENABLED = "persist.sys.permission.enable";
    public static final int REJECT = 1;
    public static final String[] SECURE_COLUMN_NAME = {"_id", DataBaseUtil.COLUMN_PKG_NAME, DataBaseUtil.Permission.COLUMN_ACCEPT, "reject", DataBaseUtil.Permission.COLUMN_PROMPT, DataBaseUtil.Permission.COLUMN_STATE, DataBaseUtil.Permission.COLUMN_TRUST, DataBaseUtil.Permission.COLUMN_CALL, DataBaseUtil.Permission.COLUMN_READ_CALL, DataBaseUtil.Permission.COLUMN_WRITE_CALL, DataBaseUtil.Permission.COLUMN_READ_CONTACTS, DataBaseUtil.Permission.COLUMN_WRITE_CONTACTS, DataBaseUtil.Permission.COLUMN_READ_SMS, DataBaseUtil.Permission.COLUMN_READ_MMS, DataBaseUtil.Permission.COLUMN_SEND_SMS, DataBaseUtil.Permission.COLUMN_SEND_MMS, DataBaseUtil.Permission.COLUMN_WRITE_SMS, DataBaseUtil.Permission.COLUMN_WRITE_MMS, DataBaseUtil.Permission.COLUMN_CHANGE_GPRS_STATE, DataBaseUtil.Permission.COLUMN_CHANGE_WIFI_STATE, DataBaseUtil.Permission.COLUMN_CHANGE_BT_STATE, DataBaseUtil.Permission.COLUMN_GPS_LOCATION, DataBaseUtil.Permission.COLUMN_CAMERA, DataBaseUtil.Permission.COLUMN_RECORD_AUDIO, DataBaseUtil.Permission.COLUMN_CHANGE_NFC_STATE, DataBaseUtil.Permission.COLUMN_READ_BROWSER, DataBaseUtil.Permission.COLUMN_READ_CALENDAR, DataBaseUtil.Permission.COLUMN_WRITE_CALENDAR, DataBaseUtil.Permission.COLUMN_DELETE_CALL, DataBaseUtil.Permission.COLUMN_DELETE_CONTACTS, DataBaseUtil.Permission.COLUMN_DELETE_SMS, DataBaseUtil.Permission.COLUMN_DELETE_MMS, DataBaseUtil.Permission.COLUMN_DELETE_CALENDAR, DataBaseUtil.Permission.COLUMN_GET_ACCOUNTS, DataBaseUtil.Permission.COLUMN_READ_PHONE_STATE, DataBaseUtil.Permission.COLUMN_ADD_VOICEMAIL, DataBaseUtil.Permission.COLUMN_USE_SIP, DataBaseUtil.Permission.COLUMN_PROCESS_OUTGOING_CALLS, DataBaseUtil.Permission.COLUMN_RECEIVE_SMS, DataBaseUtil.Permission.COLUMN_RECEIVE_MMS, DataBaseUtil.Permission.COLUMN_RECEIVE_WAP_PUSH, DataBaseUtil.Permission.COLUMN_BODY_SENSORS, DataBaseUtil.Permission.COLUMN_EXTERNAL_STORAGE, DataBaseUtil.Permission.COLUMN_ACCESS_MEDIA_PROVIDER, DataBaseUtil.Permission.COLUMN_BIND_VPN, DataBaseUtil.Permission.COLUMN_CALL_FORWARDING};
    public static final List<String> SENSORS_PERMISSIONS = new ArrayList();
    public static final List<String> SMS_PERMISSIONS = new ArrayList();
    public static final String SP_PERMISSION_INSTALL = "oppo.permission.install";
    public static final String STATE_CLOSE = "0";
    public static final String STATE_OPEN = "1";
    public static final String STATISTIC_CHECKED = "checked";
    public static final String STATISTIC_DATA = "data";
    public static final String STATISTIC_FRAMEWORK_ACTION = "coloros.safecenter.permission.STATISTIC_FRAMEWORK";
    public static final String STATISTIC_REJECT_STATUS = "reject";
    public static final String STATISTIC_TYPE = "type";
    public static final int STATISTIC_TYPE_NORMAL = 0;
    public static final int STATISTIC_TYPE_NORMAL_SMS = 2;
    public static final int STATISTIC_TYPE_SMS = 1;
    public static final List<String> STORAGE_PERMISSIONS = new ArrayList();
    public static final List<String> SWITCH_PERMISSION_LIST = Collections.unmodifiableList(Arrays.asList(PERMISSION_WIFI, PERMISSION_GPRS, PERMISSION_BLUETOOTH, PERMISSION_NFC));
    public static final String VPN_FILTER_FILE_NAME = "vpn_filter.xml";
    public static final List<String> WRITE_PERMISSIONS = Collections.unmodifiableList(Arrays.asList(PERMISSION_WRITE_CALL_LOG, PERMISSION_WRITE_CONTACTS, PERMISSION_WRITE_SMS, "android.permission.WRITE_MMS", PERMISSION_WRITE_CALENDAR));

    public static final class SecureColumnIndex {
        public static final int ACCEPT = 2;
        public static final int ACCESS_MEDIA_PROVIDER = 43;
        public static final int ADD_VOICEMAIL = 35;
        public static final int BIND_VPN = 44;
        public static final int BODY_SENSORS = 41;
        public static final int CALL_TRANSFER = 45;
        public static final int CAMERA = 22;
        public static final int CHANGE_BT_STATE = 20;
        public static final int CHANGE_GPRS_STATE = 18;
        public static final int CHANGE_NFC_STATE = 24;
        public static final int CHANGE_WIFI_STATE = 19;
        public static final int DELETE_CALENDAR = 32;
        public static final int DELETE_CALL = 28;
        public static final int DELETE_CONTACTS = 29;
        public static final int DELETE_MMS = 31;
        public static final int DELETE_SMS = 30;
        public static final int GET_ACCOUNTS = 33;
        public static final int GPS = 21;
        public static final int ID = 0;
        public static final int PACKAGE_NAME = 1;
        public static final int PHONE_CALL = 7;
        public static final int PROCESS_OUTGOING_CALLS = 37;
        public static final int PROMPT = 4;
        public static final int READ_BROWSER = 25;
        public static final int READ_CALENDAR = 26;
        public static final int READ_CALL_LOG = 8;
        public static final int READ_CONTACTS = 10;
        public static final int READ_MMS = 13;
        public static final int READ_PHONE_STATE = 34;
        public static final int READ_SMS = 12;
        public static final int RECEIVE_MMS = 39;
        public static final int RECEIVE_SMS = 38;
        public static final int RECEIVE_WAP_PUSH = 40;
        public static final int RECORD_AUDIO = 23;
        public static final int REJECT = 3;
        public static final int SEND_MMS = 15;
        public static final int SEND_SMS = 14;
        public static final int STATE = 5;
        public static final int TRUST = 6;
        public static final int USE_SIP = 36;
        public static final int WRITE_CALENDAR = 27;
        public static final int WRITE_CALL_LOG = 9;
        public static final int WRITE_CONTACTS = 11;
        public static final int WRITE_MMS = 17;
        public static final int WRITE_SMS = 16;
        public static final int WR_EXTERNAL_STORAGE = 42;
    }

    static {
        PHONE_PERMISSIONS.add(PERMISSION_CALL_PHONE);
        PHONE_PERMISSIONS.add(PERMISSION_READ_PHONE_STATE);
        PHONE_PERMISSIONS.add(PERMISSION_ADD_VOICEMAIL);
        PHONE_PERMISSIONS.add(PERMISSION_USE_SIP);
        PHONE_PERMISSIONS.add("oppo.permission.call.FORWARDING");
        CALL_LOG_PERMISSIONS.add(PERMISSION_READ_CALL_LOG);
        CALL_LOG_PERMISSIONS.add(PERMISSION_WRITE_CALL_LOG);
        CALL_LOG_PERMISSIONS.add(PERMISSION_PROCESS_OUTGOING_CALLS);
        CONTACTS_PERMISSIONS.add(PERMISSION_READ_CONTACTS);
        CONTACTS_PERMISSIONS.add(PERMISSION_WRITE_CONTACTS);
        CONTACTS_PERMISSIONS.add(PERMISSION_GET_ACCOUNTS);
        LOCATION_PERMISSIONS.add(PERMISSION_ACCESS);
        CALENDAR_PERMISSIONS.add(PERMISSION_READ_CALENDAR);
        CALENDAR_PERMISSIONS.add(PERMISSION_WRITE_CALENDAR);
        SMS_PERMISSIONS.add(PERMISSION_SEND_SMS);
        SMS_PERMISSIONS.add("android.permission.SEND_MMS");
        SMS_PERMISSIONS.add(PERMISSION_SEND_MMS_INTERNET);
        SMS_PERMISSIONS.add(PERMISSION_WRITE_SMS);
        SMS_PERMISSIONS.add("android.permission.WRITE_MMS");
        SMS_PERMISSIONS.add(PERMISSION_READ_SMS);
        SMS_PERMISSIONS.add("android.permission.READ_MMS");
        SMS_PERMISSIONS.add(PERMISSION_RECEIVE_SMS);
        SMS_PERMISSIONS.add(PERMISSION_RECEIVE_MMS);
        SMS_PERMISSIONS.add(PERMISSION_RECEIVE_WAP_PUSH);
        MICROPHONE_PERMISSIONS.add(PERMISSION_RECORD_AUDIO);
        CAMERA_PERMISSIONS.add(PERMISSION_CAMERA);
        SENSORS_PERMISSIONS.add(PERMISSION_SENSORS);
        STORAGE_PERMISSIONS.add("android.permission.WR_EXTERNAL_STORAGE");
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.CALENDAR", CALENDAR_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.CAMERA", CAMERA_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.CONTACTS", CONTACTS_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.LOCATION", LOCATION_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.MICROPHONE", MICROPHONE_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.PHONE", PHONE_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.CALL_LOG", CALL_LOG_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.SENSORS", SENSORS_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.SMS", SMS_PERMISSIONS);
        MAP_DANGEROUS_PERMISSON_GROUP.put("android.permission-group.STORAGE", STORAGE_PERMISSIONS);
        for (Map.Entry<String, List<String>> entry : MAP_DANGEROUS_PERMISSON_GROUP.entrySet()) {
            List<String> value = new ArrayList<>(entry.getValue());
            value.removeAll(HIDED_PERMISSIONS);
            MAP_DANGEROUS_PERMISSION_GROUP_WITH_HIDE.put(entry.getKey(), value);
        }
        MAP_OPPO_DEFINED_TO_ORIGINAL.put("android.permission.SEND_MMS", PERMISSION_SEND_SMS);
        MAP_OPPO_DEFINED_TO_ORIGINAL.put(PERMISSION_SEND_MMS_INTERNET, PERMISSION_SEND_SMS);
        MAP_OPPO_DEFINED_TO_ORIGINAL.put("android.permission.WRITE_MMS", PERMISSION_READ_SMS);
        MAP_OPPO_DEFINED_TO_ORIGINAL.put("android.permission.READ_MMS", PERMISSION_READ_SMS);
        MAP_OPPO_DEFINED_TO_ORIGINAL.put("android.permission.WR_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE");
        MAP_OPPO_DEFINED_TO_ORIGINAL.put("android.permission.WRITE_CALL_LOG_DELETE", PERMISSION_WRITE_CALL_LOG);
        MAP_OPPO_DEFINED_TO_ORIGINAL.put("android.permission.WRITE_CONTACTS_DELETE", PERMISSION_WRITE_CONTACTS);
        MAP_OPPO_DEFINED_TO_ORIGINAL.put("android.permission.WRITE_SMS_DELETE", PERMISSION_WRITE_SMS);
        MAP_OPPO_DEFINED_TO_ORIGINAL.put("android.permission.WRITE_MMS_DELETE", PERMISSION_SEND_SMS);
        MAP_OPPO_DEFINED_TO_ORIGINAL.put("android.permission.WRITE_CALENDAR_DELETE", PERMISSION_WRITE_CALENDAR);
        MAP_OPPO_DEFINED_TO_ORIGINAL.put(PERMISSION_WRITE_SMS, PERMISSION_READ_SMS);
        MAP_OPPO_DEFINED_TO_ORIGINAL.put("oppo.permission.call.FORWARDING", PERMISSION_CALL_PHONE);
        BACKGROUND_INTERCEPT_PERMS.add(PERMISSION_CAMERA);
        BACKGROUND_INTERCEPT_PERMS.add(PERMISSION_RECORD_AUDIO);
        PRIVACY_PERMISSIONS.put(PERMISSION_READ_CALL_LOG, DataBaseUtil.SECURE_COLUMN_NAME[8]);
        PRIVACY_PERMISSIONS.put(PERMISSION_WRITE_CALL_LOG, DataBaseUtil.SECURE_COLUMN_NAME[9]);
        PRIVACY_PERMISSIONS.put(PERMISSION_READ_CONTACTS, DataBaseUtil.SECURE_COLUMN_NAME[10]);
        PRIVACY_PERMISSIONS.put(PERMISSION_WRITE_CONTACTS, DataBaseUtil.SECURE_COLUMN_NAME[11]);
        PRIVACY_PERMISSIONS.put(PERMISSION_READ_SMS, DataBaseUtil.SECURE_COLUMN_NAME[12]);
        PRIVACY_PERMISSIONS.put("android.permission.READ_MMS", DataBaseUtil.SECURE_COLUMN_NAME[13]);
        PRIVACY_PERMISSIONS.put(PERMISSION_SEND_SMS, DataBaseUtil.SECURE_COLUMN_NAME[14]);
        PRIVACY_PERMISSIONS.put("android.permission.SEND_MMS", DataBaseUtil.SECURE_COLUMN_NAME[15]);
        PRIVACY_PERMISSIONS.put("android.permission.WRITE_CALL_LOG_DELETE", DataBaseUtil.SECURE_COLUMN_NAME[28]);
        PRIVACY_PERMISSIONS.put("android.permission.WRITE_CONTACTS_DELETE", DataBaseUtil.SECURE_COLUMN_NAME[29]);
        PRIVACY_PERMISSIONS.put("android.permission.WRITE_SMS_DELETE", DataBaseUtil.SECURE_COLUMN_NAME[30]);
        PRIVACY_PERMISSIONS.put("android.permission.WRITE_MMS_DELETE", DataBaseUtil.SECURE_COLUMN_NAME[31]);
    }

    public static long getPermissionMark(String permission) {
        int index = Arrays.asList(OPPO_PERMISSION).indexOf(permission);
        if (index == -1) {
            return 0;
        }
        return 1 << index;
    }

    public static ArrayList<String> tranPermissionToOppoInner(Context mContext, ArrayList<String> permissions, String packageName) {
        int index;
        new ArrayList();
        int targetSdk = 0;
        PackageInfo pkgInfo = null;
        List<String> requestPermissionList = null;
        try {
            pkgInfo = mContext.getPackageManager().getPackageInfo(packageName, DeviceRestrictionManager.ENABLE_APP_IN_LAUNCHER_FLAG);
            if (!(pkgInfo == null || pkgInfo.applicationInfo == null)) {
                targetSdk = pkgInfo.applicationInfo.targetSdkVersion;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int i = 0;
        for (int i2 = 0; i2 < OPPO_PERMISSION.length; i2++) {
            if (0 != 0 && requestPermissionList.contains(OPPO_PERMISSION[i2])) {
                if (!EXTRA_RUNTIME_PERMISSIONS.contains(OPPO_PERMISSION[i2])) {
                    permissions.add(OPPO_PERMISSION[i2]);
                } else if (targetSdk > 22) {
                    permissions.add(OPPO_PERMISSION[i2]);
                }
            }
        }
        if (permissions.contains(PERMISSION_SEND_SMS)) {
            int index2 = permissions.indexOf(PERMISSION_SEND_SMS);
            if (index2 != -1 && !permissions.contains("android.permission.SEND_MMS")) {
                permissions.add(index2 + 1, "android.permission.SEND_MMS");
            }
        } else if (0 != 0 && requestPermissionList.contains(PERMISSION_SEND_MMS_INTERNET)) {
            if (permissions.contains(PERMISSION_CALL_PHONE)) {
                i = permissions.indexOf(PERMISSION_CALL_PHONE);
            }
            int index3 = i;
            if (index3 != -1 && !permissions.contains("android.permission.SEND_MMS")) {
                if (index3 != 0) {
                    index3++;
                }
                permissions.add(index3, "android.permission.SEND_MMS");
            }
        }
        if (permissions.contains(PERMISSION_WRITE_SMS) && (index = permissions.indexOf(PERMISSION_WRITE_SMS)) != -1 && !permissions.contains("android.permission.WRITE_MMS")) {
            permissions.add(index + 1, "android.permission.WRITE_MMS");
        }
        if (permissions.contains(PERMISSION_READ_SMS) && !permissions.contains("android.permission.READ_MMS")) {
            permissions.add(permissions.size(), "android.permission.READ_MMS");
        }
        if (NfcAdapter.getDefaultAdapter(mContext) == null && permissions.contains(PERMISSION_NFC)) {
            permissions.remove(PERMISSION_NFC);
        }
        if (0 != 0 && ((requestPermissionList.contains("android.permission.WRITE_EXTERNAL_STORAGE") || requestPermissionList.contains("android.permission.READ_EXTERNAL_STORAGE")) && targetSdk > 22)) {
            permissions.add("android.permission.WR_EXTERNAL_STORAGE");
        }
        if (pkgInfo != null && hasVpnService(mContext, pkgInfo.packageName)) {
            permissions.add(permissions.size(), PERMISSION_BIND_VPN_SERVICE);
        }
        for (String switchPermission : SWITCH_PERMISSION_LIST) {
            if (permissions.contains(switchPermission)) {
                permissions.remove(switchPermission);
            }
        }
        if (permissions.contains(PERMISSION_CALL_PHONE) && !permissions.contains("oppo.permission.call.FORWARDING")) {
            permissions.add(permissions.size(), "oppo.permission.call.FORWARDING");
        }
        dealSpecialPermissions(mContext, packageName, permissions);
        return permissions;
    }

    public static ArrayList<String> getPkgAllPermissionFromDb(Context mContext, String packageName) {
        int index;
        ArrayList<String> mPermissionList = new ArrayList<>();
        int targetSdk = 0;
        PackageInfo pkgInfo = null;
        List<String> requestPermissionList = null;
        try {
            pkgInfo = mContext.getPackageManager().getPackageInfo(packageName, DeviceRestrictionManager.ENABLE_APP_IN_LAUNCHER_FLAG);
            if (pkgInfo != null) {
                if (pkgInfo.requestedPermissions != null) {
                    requestPermissionList = Arrays.asList(pkgInfo.requestedPermissions);
                }
                if (pkgInfo.applicationInfo != null) {
                    targetSdk = pkgInfo.applicationInfo.targetSdkVersion;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int i = 0;
        for (int i2 = 0; i2 < OPPO_PERMISSION.length; i2++) {
            if (requestPermissionList != null && requestPermissionList.contains(OPPO_PERMISSION[i2])) {
                if (!EXTRA_RUNTIME_PERMISSIONS.contains(OPPO_PERMISSION[i2])) {
                    mPermissionList.add(OPPO_PERMISSION[i2]);
                } else if (targetSdk > 22) {
                    mPermissionList.add(OPPO_PERMISSION[i2]);
                }
            }
        }
        if (mPermissionList.contains(PERMISSION_SEND_SMS)) {
            int index2 = mPermissionList.indexOf(PERMISSION_SEND_SMS);
            if (index2 != -1 && !mPermissionList.contains("android.permission.SEND_MMS")) {
                mPermissionList.add(index2 + 1, "android.permission.SEND_MMS");
            }
        } else if (requestPermissionList != null && requestPermissionList.contains(PERMISSION_SEND_MMS_INTERNET)) {
            if (mPermissionList.contains(PERMISSION_CALL_PHONE)) {
                i = mPermissionList.indexOf(PERMISSION_CALL_PHONE);
            }
            int index3 = i;
            if (index3 != -1 && !mPermissionList.contains("android.permission.SEND_MMS")) {
                if (index3 != 0) {
                    index3++;
                }
                mPermissionList.add(index3, "android.permission.SEND_MMS");
            }
        }
        if (mPermissionList.contains(PERMISSION_WRITE_SMS) && (index = mPermissionList.indexOf(PERMISSION_WRITE_SMS)) != -1 && !mPermissionList.contains("android.permission.WRITE_MMS")) {
            mPermissionList.add(index + 1, "android.permission.WRITE_MMS");
        }
        if (mPermissionList.contains(PERMISSION_READ_SMS) && !mPermissionList.contains("android.permission.READ_MMS")) {
            mPermissionList.add(mPermissionList.size(), "android.permission.READ_MMS");
        }
        if (NfcAdapter.getDefaultAdapter(mContext) == null && mPermissionList.contains(PERMISSION_NFC)) {
            mPermissionList.remove(PERMISSION_NFC);
        }
        if (requestPermissionList != null && ((requestPermissionList.contains("android.permission.WRITE_EXTERNAL_STORAGE") || requestPermissionList.contains("android.permission.READ_EXTERNAL_STORAGE")) && targetSdk > 22)) {
            mPermissionList.add("android.permission.WR_EXTERNAL_STORAGE");
        }
        if (pkgInfo != null && hasVpnService(mContext, pkgInfo.packageName)) {
            mPermissionList.add(mPermissionList.size(), PERMISSION_BIND_VPN_SERVICE);
        }
        for (String switchPermission : SWITCH_PERMISSION_LIST) {
            if (mPermissionList.contains(switchPermission)) {
                mPermissionList.remove(switchPermission);
            }
        }
        if (mPermissionList.contains(PERMISSION_CALL_PHONE) && !mPermissionList.contains("oppo.permission.call.FORWARDING")) {
            mPermissionList.add(mPermissionList.size(), "oppo.permission.call.FORWARDING");
        }
        dealSpecialPermissions(mContext, packageName, mPermissionList);
        return mPermissionList;
    }

    public static boolean hasVpnService(Context context, String packageName) {
        try {
            ServiceInfo[] serviceInfos = context.getPackageManager().getPackageInfo(packageName, 4).services;
            if (serviceInfos == null) {
                return false;
            }
            for (ServiceInfo serviceInfo : serviceInfos) {
                if (serviceInfo != null && PERMISSION_BIND_VPN_SERVICE.equals(serviceInfo.permission)) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static ArrayList<String> getPkgAllPermissionWithFilter(ArrayList<String> allPermissions) {
        ArrayList<String> result = new ArrayList<>();
        if (allPermissions != null) {
            result.addAll(allPermissions);
        }
        result.removeAll(HIDED_PERMISSIONS);
        return result;
    }

    private static void dealSpecialPermissions(Context context, String packageName, ArrayList<String> arrayList) {
    }

    private static void dealInstallPermissions(Context context, String packageName, ArrayList<String> permissionList) {
        Cursor cursor = null;
        ArrayList<String> packageList = new ArrayList<>();
        if (!"com.android.packageinstaller".equals(packageName)) {
            try {
                Cursor cursor2 = context.getContentResolver().query(DataBaseUtil.URI_PACKAGEINSTALLER, new String[]{DataBaseUtil.PackageInstaller.COLUMN_PACKAGE_NAME, DataBaseUtil.PackageInstaller.COLUMN_SWITCH}, null, null, null);
                if (cursor2 != null) {
                    if (cursor2.getCount() != 0) {
                        while (cursor2.moveToNext()) {
                            packageList.add(cursor2.getString(cursor2.getColumnIndex(DataBaseUtil.PackageInstaller.COLUMN_PACKAGE_NAME)));
                        }
                        if (packageList.contains(packageName)) {
                            permissionList.add(SP_PERMISSION_INSTALL);
                        }
                        if (cursor2 != null) {
                            try {
                                cursor2.close();
                                return;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return;
                            }
                        } else {
                            return;
                        }
                    }
                }
                if (cursor2 != null) {
                    try {
                        cursor2.close();
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                }
            } catch (Exception ex3) {
                ex3.printStackTrace();
                if (0 != 0) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        cursor.close();
                    } catch (Exception ex4) {
                        ex4.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }
}
