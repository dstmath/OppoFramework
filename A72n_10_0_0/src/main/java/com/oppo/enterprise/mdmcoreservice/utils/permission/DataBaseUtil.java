package com.oppo.enterprise.mdmcoreservice.utils.permission;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Settings;

public class DataBaseUtil {
    public static final String ALL_APP_COUNT = "all_app_count";
    public static final String AUTHORITY = "com.coloros.provider.PermissionProvider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.coloros.provider.PermissionProvider");
    public static final int CODE_PACKAGEINSTALLER = 404;
    public static final int CODE_PP_PERMISSION = 202;
    public static final int CODE_PP_SUGGEST_PERMISSION = 204;
    public static final int CODE_PRIVACY_PROTECT = 501;
    public static final int CODE_PRIVACY_RECORD = 505;
    public static final int CODE_PRIVACY_SUGGEST = 503;
    public static final String COLUMN_ALLOWED = "allowed";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PKG_NAME = "pkg_name";
    public static final String NONE_VALUE = "null";
    public static final Uri OLD_URI_PERMISSION = Uri.withAppendedPath(SAFECENTER_AUTHORITY_URI, Permission.TABLE_PP_PERMISSION);
    public static final String SAFECENTER_AUTHORITY = "com.color.provider.SafeProvider";
    public static final Uri SAFECENTER_AUTHORITY_URI = Uri.parse("content://com.color.provider.SafeProvider");
    public static final int[] SECURE_COLUMN_INDEX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45};
    public static final String[] SECURE_COLUMN_NAME = {"_id", COLUMN_PKG_NAME, Permission.COLUMN_ACCEPT, "reject", Permission.COLUMN_PROMPT, Permission.COLUMN_STATE, Permission.COLUMN_TRUST, Permission.COLUMN_CALL, Permission.COLUMN_READ_CALL, Permission.COLUMN_WRITE_CALL, Permission.COLUMN_READ_CONTACTS, Permission.COLUMN_WRITE_CONTACTS, Permission.COLUMN_READ_SMS, Permission.COLUMN_READ_MMS, Permission.COLUMN_SEND_SMS, Permission.COLUMN_SEND_MMS, Permission.COLUMN_WRITE_SMS, Permission.COLUMN_WRITE_MMS, Permission.COLUMN_CHANGE_GPRS_STATE, Permission.COLUMN_CHANGE_WIFI_STATE, Permission.COLUMN_CHANGE_BT_STATE, Permission.COLUMN_GPS_LOCATION, Permission.COLUMN_CAMERA, Permission.COLUMN_RECORD_AUDIO, Permission.COLUMN_CHANGE_NFC_STATE, Permission.COLUMN_READ_BROWSER, Permission.COLUMN_READ_CALENDAR, Permission.COLUMN_WRITE_CALENDAR, Permission.COLUMN_DELETE_CALL, Permission.COLUMN_DELETE_CONTACTS, Permission.COLUMN_DELETE_SMS, Permission.COLUMN_DELETE_MMS, Permission.COLUMN_DELETE_CALENDAR, Permission.COLUMN_GET_ACCOUNTS, Permission.COLUMN_READ_PHONE_STATE, Permission.COLUMN_ADD_VOICEMAIL, Permission.COLUMN_USE_SIP, Permission.COLUMN_PROCESS_OUTGOING_CALLS, Permission.COLUMN_RECEIVE_SMS, Permission.COLUMN_RECEIVE_MMS, Permission.COLUMN_RECEIVE_WAP_PUSH, Permission.COLUMN_BODY_SENSORS, Permission.COLUMN_EXTERNAL_STORAGE, Permission.COLUMN_ACCESS_MEDIA_PROVIDER, Permission.COLUMN_BIND_VPN, Permission.COLUMN_CALL_FORWARDING};
    public static final String TABLE_PP_FLOAT_WINDOW = "pp_float_window";
    public static final Uri URI_FLOAT_WINDOW = Uri.withAppendedPath(SAFECENTER_AUTHORITY_URI, TABLE_PP_FLOAT_WINDOW);
    public static final Uri URI_MAIN = Uri.withAppendedPath(AUTHORITY_URI, Settings.TABLE_SETTINGS);
    public static final Uri URI_PACKAGEINSTALLER = Uri.withAppendedPath(AUTHORITY_URI, PackageInstaller.TABLE_PACKAGEINSTALLER_WHITELIST);
    public static final Uri URI_PERMISSION = Uri.withAppendedPath(AUTHORITY_URI, Permission.TABLE_PP_PERMISSION);
    public static final Uri URI_SAFE_STATUS_MANAGE = Uri.withAppendedPath(AUTHORITY_URI, SafeStatusManage.TABLE_SAFE_STATUS_MANAGE);
    public static final Uri URI_SUGGEST_PERMISSION = Uri.withAppendedPath(AUTHORITY_URI, Permission.TABLE_PP_SUGGEST_PERMISSION);

    public static final class PackageInstaller implements BaseColumns {
        public static final String COLUMN_PACKAGE_NAME = "packageName";
        public static final String COLUMN_SWITCH = "switch";
        public static final String TABLE_PACKAGEINSTALLER_CREATION_SQL = "create table if not exists packageinstaller_whitelist (_id INTEGER PRIMARY KEY AUTOINCREMENT,packageName TEXT NOT NULL,switch INTEGER);";
        public static final String TABLE_PACKAGEINSTALLER_WHITELIST = "packageinstaller_whitelist";
    }

    public static final class Permission implements BaseColumns {
        public static final String COLUMN_ACCEPT = "accept";
        public static final String COLUMN_ACCESS_MEDIA_PROVIDER = "external_img";
        public static final String COLUMN_ADD_VOICEMAIL = "add_voicemail";
        public static final String COLUMN_BIND_VPN = "bind_vpn";
        public static final String COLUMN_BODY_SENSORS = "body_sensors";
        public static final String COLUMN_CALL = "call";
        public static final String COLUMN_CALL_FORWARDING = "call_forwarding";
        public static final String COLUMN_CAMERA = "camera";
        public static final String COLUMN_CHANGE_BT_STATE = "change_bt_state";
        public static final String COLUMN_CHANGE_GPRS_STATE = "change_gprs_state";
        public static final String COLUMN_CHANGE_NFC_STATE = "change_nfc_state";
        public static final String COLUMN_CHANGE_WIFI_STATE = "change_wifi_state";
        public static final String COLUMN_DELETE_CALENDAR = "delete_calendar";
        public static final String COLUMN_DELETE_CALL = "delete_call";
        public static final String COLUMN_DELETE_CONTACTS = "delete_contacts";
        public static final String COLUMN_DELETE_MMS = "delete_mms";
        public static final String COLUMN_DELETE_SMS = "delete_sms";
        public static final String COLUMN_EXTERNAL_STORAGE = "external_storage";
        public static final String COLUMN_GET_ACCOUNTS = "get_accounts";
        public static final String COLUMN_GPS_LOCATION = "gps_location";
        public static final String COLUMN_PROCESS_OUTGOING_CALLS = "process_outgoing_calls";
        public static final String COLUMN_PROMPT = "prompt";
        public static final String COLUMN_READ_BROWSER = "read_browser";
        public static final String COLUMN_READ_CALENDAR = "read_calendar";
        public static final String COLUMN_READ_CALL = "read_call";
        public static final String COLUMN_READ_CONTACTS = "read_contacts";
        public static final String COLUMN_READ_MMS = "read_mms";
        public static final String COLUMN_READ_PHONE_STATE = "read_phone_state";
        public static final String COLUMN_READ_SMS = "read_sms";
        public static final String COLUMN_RECEIVE_MMS = "receive_mms";
        public static final String COLUMN_RECEIVE_SMS = "receive_sms";
        public static final String COLUMN_RECEIVE_WAP_PUSH = "receive_wap_push";
        public static final String COLUMN_RECORD_AUDIO = "record_audio";
        public static final String COLUMN_REJECT = "reject";
        public static final String COLUMN_SEND_MMS = "send_mms";
        public static final String COLUMN_SEND_SMS = "send_sms";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_SUGGEST_ACCEPT = "suggest_accept";
        public static final String COLUMN_SUGGEST_PROMPT = "suggest_prompt";
        public static final String COLUMN_SUGGEST_REJECT = "suggest_reject";
        public static final String COLUMN_TRUST = "trust";
        public static final String COLUMN_USE_SIP = "use_sip";
        public static final String COLUMN_WRITE_CALENDAR = "write_calendar";
        public static final String COLUMN_WRITE_CALL = "write_call";
        public static final String COLUMN_WRITE_CONTACTS = "write_contacts";
        public static final String COLUMN_WRITE_MMS = "write_mms";
        public static final String COLUMN_WRITE_SMS = "write_sms";
        public static final String TABLE_PP_PERMISSION = "pp_permission";
        public static final String TABLE_PP_PERMISSION_ADD_ADD_VOICEMAIL_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN add_voicemail INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_BIND_VPN_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN bind_vpn INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_BODY_SENSORS_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN body_sensors INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_BROWSER_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN read_browser INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_CALL_TRANSFER_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN call_forwarding INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_DELETE_CALENDAR_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN delete_calendar INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_DELETE_CALL_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN delete_call INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_DELETE_CONTACTS_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN delete_contacts INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_DELETE_MMS_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN delete_mms INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_DELETE_SMS_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN delete_sms INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_GET_ACCOUNTS_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN get_accounts INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_OPPO_DCIM_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN external_img INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_PROCESS_OUTGOING_CALLS_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN process_outgoing_calls INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_READ_CALENDAR_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN read_calendar INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_READ_PHONE_STATE_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN read_phone_state INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_RECEIVE_MMS_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN receive_mms INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_RECEIVE_SMS_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN receive_sms INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_RECEIVE_WAP_PUSH_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN receive_wap_push INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_USE_SIP_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN use_sip INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_WRITE_CALENDAR_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN write_calendar INTEGER;";
        public static final String TABLE_PP_PERMISSION_ADD_WR_EXTERNAL_STORAGE_COLUMN_SQL = "ALTER TABLE pp_permission ADD COLUMN external_storage INTEGER;";
        public static final String TABLE_PP_PERMISSION_CREATION_SQL = "create table if not exists pp_permission (_id INTEGER PRIMARY KEY AUTOINCREMENT,pkg_name TEXT NOT NULL,accept INTEGER,reject INTEGER,prompt INTEGER,state INTEGER,trust INTEGER,call INTEGER,read_call INTEGER,write_call INTEGER,read_contacts INTEGER,write_contacts INTEGER,read_sms INTEGER,read_mms INTEGER,send_sms INTEGER,send_mms INTEGER,write_sms INTEGER,write_mms INTEGER,change_gprs_state INTEGER,change_wifi_state INTEGER,change_bt_state INTEGER,gps_location INTEGER,camera INTEGER,record_audio INTEGER,change_nfc_state INTEGER,read_browser INTEGER,read_calendar INTEGER,write_calendar INTEGER,delete_call INTEGER,delete_contacts INTEGER,delete_sms INTEGER,delete_mms INTEGER,delete_calendar INTEGER,get_accounts INTEGER,read_phone_state INTEGER,add_voicemail INTEGER,use_sip INTEGER,process_outgoing_calls INTEGER,receive_sms INTEGER,receive_mms INTEGER,receive_wap_push INTEGER,body_sensors INTEGER,external_storage INTEGER,external_img INTEGER,bind_vpn INTEGER,call_forwarding INTEGER);";
        public static final String TABLE_PP_SUGGEST_PERMISSION = "pp_suggest_permission";
        public static final String TABLE_PP_SUGGEST_PERMISSION_CREATION_SQL = "create table if not exists pp_suggest_permission (_id INTEGER PRIMARY KEY AUTOINCREMENT,pkg_name TEXT NOT NULL,suggest_accept LONG,suggest_reject LONG,suggest_prompt LONG);";
    }

    public static final class SafeStatusManage implements BaseColumns {
        public static final String[] COLUMN_ARRAY = {COLUMN_MODULE_NAME, COLUMN_STATUS_DOT, COLUMN_STATUS_HINT, COLUMN_STATUS_ALERT};
        public static final String COLUMN_MODULE_NAME = "module_name";
        public static final String COLUMN_STATUS_ALERT = "status_alert";
        public static final String COLUMN_STATUS_DOT = "status_dot";
        public static final String COLUMN_STATUS_HINT = "status_hint";
        public static final String[] MODULE_ARRAY = {MODULE_TOTAL, MODULE_CLEAN, MODULE_TRAFFIC, MODULE_BLACKLIST, MODULE_PERMISSION, MODULE_VIRUS, MODULE_POWER};
        public static final String MODULE_BLACKLIST = "blacklist";
        public static final String MODULE_CLEAN = "clean";
        public static final String MODULE_PERMISSION = "permission";
        public static final String MODULE_POWER = "power";
        public static final String MODULE_TOTAL = "total";
        public static final String MODULE_TRAFFIC = "traffic";
        public static final String MODULE_VIRUS = "virus";
        public static final String TABLE_SAFE_STATUS_MANAGE = "ssm_status_track";
        public static final String TABLE_SAFE_STATUS_MANAGE_CREATION_SQL = "create table if not exists ssm_status_track (_id INTEGER PRIMARY KEY AUTOINCREMENT,module_name TEXT,status_dot INTEGER DEFAULT 0,status_hint INTEGER DEFAULT 0,status_alert INTEGER DEFAULT 0);";
    }

    public static final class Settings implements BaseColumns {
        public static final String COLUMN_MAIN_KEY = "key";
        public static final String COLUMN_MAIN_VALUE = "value";
        public static final String MAIN_FIRST_START_TIME = "main_first_start_time";
        public static final String MAIN_LAST_OP_TIME = "main_last_op_time";
        public static final String MAIN_NETWORK_PERMISSION = "main_network_permission";
        public static final String MAIN_NETWORK_PERMISSION_NO_ASK = "main_network_permission_no_ask";
        public static final String PP_ALERT_FOR_PERMISSIONS_DENIAL = "pp_alert_for_permission_denial";
        public static final String PP_PERMISSION_CONTROL = "pp_permission_control";
        public static final String TABLE_SETTINGS = "settings";
    }

    public static String getMainSettingsValue(Context context, String key) {
        Cursor cursor = null;
        String result = NONE_VALUE;
        try {
            Cursor cursor2 = context.getContentResolver().query(URI_MAIN, new String[]{Settings.COLUMN_MAIN_VALUE}, "key= ?", new String[]{key}, null);
            if (cursor2 != null && cursor2.moveToFirst()) {
                result = cursor2.getString(cursor2.getColumnIndex(Settings.COLUMN_MAIN_VALUE));
            }
            if (cursor2 != null) {
                cursor2.close();
                cursor2 = null;
            }
            if (cursor2 != null) {
                cursor2.close();
            }
            if (result != null) {
                return result;
            }
            return NONE_VALUE;
        } catch (Exception e) {
            if (0 != 0) {
                cursor.close();
                cursor = null;
            }
            if (cursor != null) {
                cursor.close();
            }
            if (result != null) {
                return result;
            }
            return NONE_VALUE;
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            if (result != null) {
                return result;
            }
            return NONE_VALUE;
        }
    }

    public static void setMainSettingsValue(Context context, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(Settings.COLUMN_MAIN_VALUE, value);
        try {
            if (context.getContentResolver().update(URI_MAIN, cv, "key= ?", new String[]{key}) == 0) {
                cv.put(Settings.COLUMN_MAIN_KEY, key);
                context.getContentResolver().insert(URI_MAIN, cv);
            }
            cv.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getCtaUpdateServiceSwitch(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), "oppo_cta_update_service") == 1;
        } catch (Settings.SettingNotFoundException snfe) {
            snfe.printStackTrace();
            return false;
        }
    }

    public static boolean getNetworkPermissionState(Context context) {
        String value = getMainSettingsValue(context, Settings.MAIN_NETWORK_PERMISSION);
        if (value.equals(NONE_VALUE) || Integer.parseInt(value) != 1) {
            return false;
        }
        return true;
    }

    public static void setNetworkPermissionState(Context context, Boolean state) {
        setMainSettingsValue(context, Settings.MAIN_NETWORK_PERMISSION, state.booleanValue() ? "1" : "0");
    }

    public static boolean getNetworkPermissionNotAskState(Context context) {
        String value = getMainSettingsValue(context, Settings.MAIN_NETWORK_PERMISSION_NO_ASK);
        if (value.equals(NONE_VALUE) || Integer.parseInt(value) != 1) {
            return false;
        }
        return true;
    }

    public static void setNetworkPermissionNotAskState(Context context, Boolean state) {
        setMainSettingsValue(context, Settings.MAIN_NETWORK_PERMISSION_NO_ASK, state.booleanValue() ? "1" : "0");
    }

    public static long getLastOpTime(Context context) {
        String value = getMainSettingsValue(context, Settings.MAIN_LAST_OP_TIME);
        if (!value.equals(NONE_VALUE)) {
            return Long.parseLong(value);
        }
        return 0;
    }

    public static void setLastOpTime(Context context, long time) {
        setMainSettingsValue(context, Settings.MAIN_LAST_OP_TIME, String.valueOf(time));
    }

    public static long getFirstStartTime(Context context) {
        String value = getMainSettingsValue(context, Settings.MAIN_FIRST_START_TIME);
        if (!value.equals(NONE_VALUE)) {
            return Long.parseLong(value);
        }
        return 0;
    }

    public static boolean getPermissionAlertForDenialState(Context context) {
        String value = getMainSettingsValue(context, Settings.PP_ALERT_FOR_PERMISSIONS_DENIAL);
        if (value.equals(NONE_VALUE) || Integer.parseInt(value) == 1) {
            return true;
        }
        return false;
    }

    public static void setPermissionAlertForDenialState(Context context, Boolean state) {
        setMainSettingsValue(context, Settings.PP_ALERT_FOR_PERMISSIONS_DENIAL, state.booleanValue() ? "1" : "0");
    }

    public static boolean getPpPermissionControl(Context context) {
        String value = getMainSettingsValue(context, Settings.PP_PERMISSION_CONTROL);
        if (value.equals(NONE_VALUE) || Integer.parseInt(value) != 1) {
            return false;
        }
        return true;
    }

    public static void setPpPermissionControl(Context context, Boolean state) {
        setMainSettingsValue(context, Settings.PP_PERMISSION_CONTROL, state.booleanValue() ? "1" : "0");
    }

    public static boolean getPpAlertForPermissionDenial(Context context) {
        String value = getMainSettingsValue(context, Settings.PP_ALERT_FOR_PERMISSIONS_DENIAL);
        if (value.equals(NONE_VALUE) || Integer.parseInt(value) != 1) {
            return false;
        }
        return true;
    }

    public static void setPpAlertForPermissionDenial(Context context, Boolean state) {
        setMainSettingsValue(context, Settings.PP_ALERT_FOR_PERMISSIONS_DENIAL, state.booleanValue() ? "1" : "0");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0043, code lost:
        if (0 == 0) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0045, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0049, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x003a, code lost:
        if (r1 != null) goto L_0x0045;
     */
    public static int getSafeStatusByModule(Context context, int module, int index) {
        int result = -1;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(URI_SAFE_STATUS_MANAGE, new String[]{SafeStatusManage.COLUMN_ARRAY[index]}, "module_name= ?", new String[]{SafeStatusManage.MODULE_ARRAY[module]}, null);
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getInt(cursor.getColumnIndex(SafeStatusManage.COLUMN_ARRAY[index]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    public static void setSafeStatusByModule(Context context, int module, int index, int value) {
        ContentValues cv = new ContentValues();
        try {
            cv.put(SafeStatusManage.COLUMN_ARRAY[index], Integer.valueOf(value));
            context.getContentResolver().update(URI_SAFE_STATUS_MANAGE, cv, "module_name= ?", new String[]{SafeStatusManage.MODULE_ARRAY[module]});
            cv.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
