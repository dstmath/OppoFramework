package android.content.pm;

import android.Manifest;
import android.net.Uri;
import java.util.Arrays;
import java.util.List;

public class OppoPermissionManager {
    public static final int ACCEPT = 0;
    public static final String AUTHORITY = "com.coloros.provider.PermissionProvider";
    public static final int FIRST_MASK = 1;
    public static final int INVALID_RES = 3;
    public static final List<String> OPPO_DEFINED_PERMISSIONS = Arrays.asList(READ_MMS_PERMISSION, WRITE_MMS_PERMISSION, SEND_MMS_PERMISSION, PERMISSION_WR_EXTERNAL_STORAGE, PERMISSION_ACCESS_MEDIA_PROVIDER);
    public static final List<String> OPPO_DETECT_FREQUENT_CHECK_PERMISSIONS = Arrays.asList(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, "android.permission.READ_CONTACTS#com.callershow.colorcaller");
    public static final List<String> OPPO_PRIVACY_PROTECT_PERMISSIONS = Arrays.asList(Manifest.permission.READ_SMS, Manifest.permission.WRITE_SMS, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
    public static final Uri PERMISSIONS_PROVIDER_URI = Uri.parse("content://com.coloros.provider.PermissionProvider/pp_permission");
    public static final String PERMISSION_ACCESS_MEDIA_PROVIDER = "android.permission.ACCESS_MEDIA_PROVIDER";
    public static final String PERMISSION_CALL_FORWARDING = "oppo.permission.call.FORWARDING";
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
    public static final List<String> WRITE_PERMISSIONS = Arrays.asList(Manifest.permission.WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_SMS, WRITE_MMS_PERMISSION, Manifest.permission.WRITE_CALENDAR);
    public static List<String> sAlwaysInterceptingPermissions = Arrays.asList(Manifest.permission.SEND_SMS);
    public static List<String> sInterceptingPermissions = Arrays.asList(Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, SEND_MMS_PERMISSION, Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.NFC, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_SMS, WRITE_MMS_PERMISSION, READ_MMS_PERMISSION, Manifest.permission.READ_HISTORY_BOOKMARKS, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, PERMISSION_DELETE_CALL, PERMISSION_DELETE_CONTACTS, PERMISSION_DELETE_SMS, PERMISSION_DELETE_MMS, PERMISSION_DELETE_CALENDAR, Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ADD_VOICEMAIL, Manifest.permission.USE_SIP, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.RECEIVE_SMS, Manifest.permission.RECEIVE_MMS, Manifest.permission.RECEIVE_WAP_PUSH, Manifest.permission.BODY_SENSORS, PERMISSION_WR_EXTERNAL_STORAGE, PERMISSION_ACCESS_MEDIA_PROVIDER, Manifest.permission.BIND_VPN_SERVICE, PERMISSION_CALL_FORWARDING, Manifest.permission.ACTIVITY_RECOGNITION);
    public static int[] sPermissionsDefaultChoices = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
}
