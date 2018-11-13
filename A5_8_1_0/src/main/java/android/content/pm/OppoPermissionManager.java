package android.content.pm;

import android.Manifest.permission;
import android.net.Uri;
import java.util.Arrays;
import java.util.List;

public class OppoPermissionManager {
    public static final int ACCEPT = 0;
    public static final String AUTHORITY = "com.coloros.provider.PermissionProvider";
    public static final int FIRST_MASK = 1;
    public static final int INVALID_RES = 3;
    public static final List<String> OPPO_DEFINED_PERMISSIONS = Arrays.asList(new String[]{READ_MMS_PERMISSION, WRITE_MMS_PERMISSION, SEND_MMS_PERMISSION, PERMISSION_WR_EXTERNAL_STORAGE, PERMISSION_ACCESS_MEDIA_PROVIDER});
    public static final Uri PERMISSIONS_PROVIDER_URI = Uri.parse("content://com.coloros.provider.PermissionProvider/pp_permission");
    public static final String PERMISSION_ACCESS_MEDIA_PROVIDER = "android.permission.ACCESS_MEDIA_PROVIDER";
    public static final String PERMISSION_DELETE_CALENDAR = "android.permission.WRITE_CALENDAR_DELETE";
    public static final String PERMISSION_DELETE_CALL = "android.permission.WRITE_CALL_LOG_DELETE";
    public static final String PERMISSION_DELETE_CONTACTS = "android.permission.WRITE_CONTACTS_DELETE";
    public static final String PERMISSION_DELETE_MMS = "android.permission.WRITE_MMS_DELETE";
    public static final String PERMISSION_DELETE_SMS = "android.permission.WRITE_SMS_DELETE";
    public static final String PERMISSION_WR_EXTERNAL_STORAGE = "android.permission.WR_EXTERNAL_STORAGE";
    public static final int PROMPT = 2;
    public static final String READ_MMS_PERMISSION = "android.permission.READ_MMS";
    public static final int REJECT = 1;
    public static final String SEND_MMS_PERMISSION = "android.permission.SEND_MMS";
    public static final String WRITE_MMS_PERMISSION = "android.permission.WRITE_MMS";
    public static final List<String> WRITE_PERMISSIONS = Arrays.asList(new String[]{permission.WRITE_CALL_LOG, permission.WRITE_CONTACTS, permission.WRITE_SMS, WRITE_MMS_PERMISSION, permission.WRITE_CALENDAR});
    public static List<String> sAlwaysInterceptingPermissions = Arrays.asList(new String[]{permission.SEND_SMS});
    public static List<String> sInterceptingPermissions = Arrays.asList(new String[]{permission.CALL_PHONE, permission.READ_CALL_LOG, permission.READ_CONTACTS, permission.READ_SMS, permission.SEND_SMS, SEND_MMS_PERMISSION, permission.CHANGE_NETWORK_STATE, permission.CHANGE_WIFI_STATE, permission.BLUETOOTH_ADMIN, permission.ACCESS_FINE_LOCATION, permission.CAMERA, permission.RECORD_AUDIO, permission.NFC, permission.WRITE_CALL_LOG, permission.WRITE_CONTACTS, permission.WRITE_SMS, WRITE_MMS_PERMISSION, READ_MMS_PERMISSION, permission.READ_HISTORY_BOOKMARKS, permission.READ_CALENDAR, permission.WRITE_CALENDAR, PERMISSION_DELETE_CALL, PERMISSION_DELETE_CONTACTS, PERMISSION_DELETE_SMS, PERMISSION_DELETE_MMS, PERMISSION_DELETE_CALENDAR, permission.GET_ACCOUNTS, permission.READ_PHONE_STATE, permission.ADD_VOICEMAIL, permission.USE_SIP, permission.PROCESS_OUTGOING_CALLS, permission.RECEIVE_SMS, permission.RECEIVE_MMS, permission.RECEIVE_WAP_PUSH, permission.BODY_SENSORS, PERMISSION_WR_EXTERNAL_STORAGE, PERMISSION_ACCESS_MEDIA_PROVIDER, permission.BIND_VPN_SERVICE});
    public static int[] sPermissionsDefaultChoices = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
}
