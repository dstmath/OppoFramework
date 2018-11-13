package android.os;

import android.bluetooth.BluetoothInputDevice;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.IOppoService.Stub;
import android.telephony.ColorOSTelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class OppoManager {
    public static final int ANDROID_MSG_INPUTMETHOD_FAILD = 1004;
    public static final int ANDROID_MSG_INSTALL_FAILD = 1003;
    public static final int ANDROID_MSG_LAUNCHACTIVITY = 1002;
    public static final int ANDROID_MSG_SKIPFRAMES = 1001;
    public static final String ANDROID_PANIC_TAG = "SYSTEM_SERVER";
    public static final String ANDROID_PANIC_TAG_BEGIN = "<android-panic-begin>\n";
    public static final String ANDROID_PANIC_TAG_END = "<android-panic-end>\n";
    public static final String ANDROID_TAG = "ANDROID";
    public static final String CAMERA_TAG = "CAMERA";
    public static final String CONNECT_TAG = "CONNECTIVITY";
    private static int DATA_SIZE = 16;
    private static final boolean DEBUG = true;
    public static final String ENGINEERINGMODE_TEST_BEGIN = "<engineeringmode-test-begin>\n";
    public static final String ENGINEERINGMODE_TEST_END = "<engineeringmode-test-end>\n";
    public static final String ENGINEERINGMODE_TEST_TAG = "ENGINEERINGMODE_TEST";
    public static final String GMAP_PNAME = "com.google.android.apps.maps";
    private static final int INIT_TRY_TIMES = 3;
    public static final String ISSUE_ANDROID_ADSP_CRASH = "adsp_crash";
    public static final String ISSUE_ANDROID_AVERAGE_CURRENT_EVENT = "average_current_event";
    public static final String ISSUE_ANDROID_CHARGER_PLUGIN_625 = "charger_plugin";
    public static final String ISSUE_ANDROID_CHARGER_PLUGOUT_626 = "charger_plugout";
    public static final String ISSUE_ANDROID_CRASH = "crash";
    public static final String ISSUE_ANDROID_FP_DIE = "fp_die";
    public static final String ISSUE_ANDROID_FP_HW_ERROR = "fp_hw_error";
    public static final String ISSUE_ANDROID_FP_RESET_BYHM = "fp_reset_byhm";
    public static final String ISSUE_ANDROID_INPUTMETHOD_FAIL = "inputmethod_fail";
    public static final String ISSUE_ANDROID_INSTALL_FAIL = "install_fail";
    public static final String ISSUE_ANDROID_LAUNCH_ACTIVITY = "launch_activity";
    public static final String ISSUE_ANDROID_MODEM_CRASH = "modem_crash";
    public static final String ISSUE_ANDROID_OTA_UPGRADE = "ota_upgrade";
    public static final String ISSUE_ANDROID_PM_50 = "scan_event";
    public static final String ISSUE_ANDROID_PM_51 = "wifi_discounnect_event";
    public static final String ISSUE_ANDROID_PM_52 = "key_exchange_event";
    public static final String ISSUE_ANDROID_PM_53 = "dhcp_relet_event";
    public static final String ISSUE_ANDROID_PM_54 = "data_call_count";
    public static final String ISSUE_ANDROID_PM_55 = "no_service_time";
    public static final String ISSUE_ANDROID_PM_56 = "reselect_per_min";
    public static final String ISSUE_ANDROID_PM_57 = "sms_send_count";
    public static final String ISSUE_ANDROID_PM_58 = "background_music";
    public static final String ISSUE_ANDROID_PM_59 = "background_download";
    public static final String ISSUE_ANDROID_PM_60 = "wifi_wakeup";
    public static final String ISSUE_ANDROID_PM_61 = "modem_wakeup";
    public static final String ISSUE_ANDROID_PM_62 = "alarm_wakeup";
    public static final String ISSUE_ANDROID_PM_63 = "base_subsystem";
    public static final String ISSUE_ANDROID_PM_64 = "power_other";
    public static final String ISSUE_ANDROID_REBOOT_FROM_BLOCKED = "reboot_from_blocked";
    public static final String ISSUE_ANDROID_SKIP_FRAMES = "skip_frames";
    public static final String ISSUE_ANDROID_SYSTEM_REBOOT_FROM_BLOCKED = "system_server_reboot_from_blocked";
    public static final String ISSUE_ANDROID_VENUS_CRASH = "venus_crash";
    public static final String ISSUE_ANDROID_WCN_CRASH = "wcn_crash";
    public static final String ISSUE_KERNEL_PANIC = "panic";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_AS_FAILED = "as_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_AUTHENTICATION_REJECT = "authentication_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_RX_BREAK = "card_drop_rx_break";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_CARD_DROP_TIME_OUT = "card_drop_time_out";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NOT_ALLOWED = "data_no_allowed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN = "data_no_acailable_apn";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR = "data_set_up_data_error";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_GSM_T3126_EXPIRED = "gsm_t3126_expired";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_AS_FAILED = "lte_as_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_REJECT = "ltc_reg_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_LTE_REG_WITHOUT_LTE = "lte_reg_without_lte";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MCFG_ICCID_FAILED = "mcfg_iccid_failed";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MO_DROP = "mo_drop";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_CSFB = "mt_csfb";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_PCH = "mt_pch";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RACH = "mt_rach";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_REJECT = "mt_reject";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RLF = "mt_rlf";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_MT_RRC = "mt_rrc";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_REG_REJECT = "reg_rejetc";
    public static final String ISSUE_SYS_OEM_NW_DIAG_CAUSE_RF_MIPI_HW_FAILED = "rf_mipi_hw_failed";
    public static final String ISSUE_WIFI_CONNECTING_FAILURE = "wifi_connecting_failure";
    public static final String ISSUE_WIFI_LOAD_DRIVER_FAILURE = "wifi_load_driver_failure";
    public static final String ISSUE_WIFI_TURN_ON_OFF_FAILURE = "wifi_turn_on_off_failure";
    public static final String KERNEL_PANIC_TAG = "SYSTEM_LAST_KMSG";
    public static final String KERNEL_PANIC_TAG_BEGIN = "<kernel-panic-begin>\n";
    public static final String KERNEL_PANIC_TAG_END = "<kernel-panic-end>\n";
    public static final String KERNEL_TAG = "KERNEL";
    public static final String MULTIMEDIA_TAG = "MULTIMEDIA";
    public static final String NETWORK_TAG = "NETWORK";
    public static final String SERVICE_NAME = "OPPO";
    public static final String SHUTDOWN_TAG = "SYSTEM_SHUTDOWN";
    public static final String SHUTDOWN_TAG_BEGIN = "<shutdown-begin>\n";
    public static final String SHUTDOWN_TAG_END = "<shutdown-end>\n";
    public static final String SPMI_BEGIN = "<spmi-begin>\n";
    public static final String SPMI_END = "<spmi-end>\n";
    public static final String SPMI_TAG = "SPMI";
    public static final String TAG = "OppoManager";
    public static int TYEP_Android_VER = 2;
    public static int TYEP_BUILD_VER = 3;
    public static int TYEP_DEVICE = 4;
    public static int TYEP_PHONE_IMEI = 1;
    public static int TYPE_ANDROID_ADSP_CRASH = 44;
    public static int TYPE_ANDROID_AVERAGE_CURRENT_EVENT = 37;
    public static int TYPE_ANDROID_BACK_KEY = 33;
    public static int TYPE_ANDROID_CAMERA = 28;
    public static final int TYPE_ANDROID_CHARGER_PLUGIN_625 = 625;
    public static final int TYPE_ANDROID_CHARGER_PLUGOUT_626 = 626;
    public static int TYPE_ANDROID_CRASH = 22;
    public static int TYPE_ANDROID_FP_DIE = 47;
    public static int TYPE_ANDROID_FP_HW_ERROR = 49;
    public static int TYPE_ANDROID_FP_RESET_BYHM = 48;
    public static int TYPE_ANDROID_HOME_KEY = 31;
    public static int TYPE_ANDROID_INPUTMETHOD_FAIL = 43;
    public static int TYPE_ANDROID_INSTALL_FAILD = 40;
    public static int TYPE_ANDROID_LAUNCH_ACTIVITY = 39;
    public static int TYPE_ANDROID_MENU_KEY = 32;
    public static int TYPE_ANDROID_OTA_FAILD = 41;
    public static int TYPE_ANDROID_OTA_UPGRADE = 29;
    public static int TYPE_ANDROID_PM_EVENT_50 = 50;
    public static int TYPE_ANDROID_PM_EVENT_51 = 51;
    public static int TYPE_ANDROID_PM_EVENT_52 = 52;
    public static int TYPE_ANDROID_PM_EVENT_53 = 53;
    public static int TYPE_ANDROID_PM_EVENT_54 = 54;
    public static int TYPE_ANDROID_PM_EVENT_55 = 55;
    public static int TYPE_ANDROID_PM_EVENT_56 = 56;
    public static int TYPE_ANDROID_PM_EVENT_57 = 57;
    public static int TYPE_ANDROID_PM_EVENT_58 = 58;
    public static int TYPE_ANDROID_PM_EVENT_59 = 59;
    public static int TYPE_ANDROID_PM_EVENT_60 = 60;
    public static int TYPE_ANDROID_PM_EVENT_61 = 61;
    public static int TYPE_ANDROID_PM_EVENT_62 = 62;
    public static int TYPE_ANDROID_PM_EVENT_63 = 63;
    public static int TYPE_ANDROID_PM_EVENT_64 = 64;
    public static int TYPE_ANDROID_POWER_KEY = 36;
    public static int TYPE_ANDROID_SKIPFRAMES = 38;
    public static int TYPE_ANDROID_SPMI = 24;
    public static int TYPE_ANDROID_SYSTEM_REBOOT_FROM_BLOCKED = 26;
    public static int TYPE_ANDROID_UNKNOWN_REBOOT = 42;
    public static int TYPE_ANDROID_USB = 30;
    public static int TYPE_ANDROID_VENUS_CRASH = 45;
    public static int TYPE_ANDROID_VOLDOWN_KEY = 35;
    public static int TYPE_ANDROID_VOLUP_KEY = 34;
    public static int TYPE_ANDROID_WCN_CRASH = 46;
    public static int TYPE_BATTERY_CHARGE_HISTORY = 8;
    public static int TYPE_CRITICAL_DATA_SIZE = 512;
    public static int TYPE_HW_SHUTDOWN = 5;
    public static int TYPE_LOGSIZE = 1022;
    public static int TYPE_LOGVER = 0;
    public static int TYPE_MODERN = 23;
    public static int TYPE_OTA_FLAG = 6;
    public static int TYPE_PANIC = 600;
    public static int TYPE_REBOOT = 21;
    public static int TYPE_REBOOT_FROM_BLOCKED = 27;
    public static int TYPE_RESMON = 25;
    public static int TYPE_ROOT_FLAG = 7;
    public static int TYPE_SHUTDOWN = 20;
    public static final int TYPE_SYMBOL_VERSION_DISAGREE = 803;
    public static final int TYPE_WDI_EXCEPTION = 804;
    public static int TYPE_WIFI_CONNECT_FAILED = 800;
    private static IOppoService sService;

    private static native int native_oppoManager_cleanItem(int i);

    private static native String native_oppoManager_readCriticalData(int i, int i2);

    private static native String native_oppoManager_readRawPartition(int i, int i2);

    private static native int native_oppoManager_syncCahceToEmmc();

    private static native String native_oppoManager_testFunc(int i, int i2);

    private static native int native_oppoManager_updateConfig();

    private static native int native_oppoManager_writeCriticalData(int i, String str);

    private static native int native_oppoManager_writeRawPartition(int i, String str, int i2);

    public static final boolean init() {
        if (sService != null) {
            return true;
        }
        int times = 3;
        do {
            Log.w(TAG, "Try to OppoService Instance! times = " + times);
            sService = Stub.asInterface(ServiceManager.getService("OPPO"));
            if (sService != null) {
                return true;
            }
            times--;
        } while (times > 0);
        return false;
    }

    public static String readRawPartition(int offset, int size) {
        String res = null;
        try {
            return native_oppoManager_readRawPartition(offset, size);
        } catch (Exception e) {
            Log.e(TAG, "read Raw Partition exception!");
            e.printStackTrace();
            return res;
        }
    }

    public static int writeRawPartition(int type, String content, int isAddToDropbox) {
        int res = -1;
        try {
            return native_oppoManager_writeRawPartition(type, content, isAddToDropbox);
        } catch (Exception e) {
            Log.e(TAG, "write Raw Partition exception!");
            e.printStackTrace();
            return res;
        }
    }

    public static int readCriticalData(int type) {
        int res = 0;
        String dataString = readCriticalData(type, DATA_SIZE);
        if (dataString == null) {
            return 0;
        }
        dataString = dataString.trim();
        if (dataString == null || dataString.length() == 0) {
            return 0;
        }
        try {
            res = Integer.parseInt(dataString) + 0;
        } catch (Exception e) {
            Log.e(TAG, "read critical data failed!! e = " + e.toString());
            e.printStackTrace();
        }
        return res;
    }

    public static String readCriticalData(int id, int size) {
        String res = null;
        try {
            return native_oppoManager_readCriticalData(id, size);
        } catch (Exception e) {
            Log.e(TAG, "read Critical Data exception!\n");
            e.printStackTrace();
            return res;
        }
    }

    public static int writeCriticalData(int id, String content) {
        if (content != null) {
            try {
                if (content.length() > TYPE_CRITICAL_DATA_SIZE - 10) {
                    content = content.substring(0, TYPE_CRITICAL_DATA_SIZE - 10);
                }
            } catch (Exception e) {
                Log.e(TAG, "write Critical Data exception!\n");
                e.printStackTrace();
                return -1;
            }
        }
        return native_oppoManager_writeCriticalData(id, content);
    }

    public static String getTime() {
        String strTime = "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())) + "\n";
    }

    public static String getIMEINums(Context context) {
        String imei = "";
        ColorOSTelephonyManager telephonyManager = ColorOSTelephonyManager.getDefault(context);
        if (context.getPackageManager().hasSystemFeature("oppo.qualcomm.gemini.support")) {
            try {
                imei = telephonyManager.colorGetQcomImeiGemini(0);
                Log.i(TAG, "get imei in qcom");
            } catch (Exception e) {
                Log.e(TAG, "Exception: ", e);
            }
        } else {
            imei = telephonyManager.getDeviceIdGemini(0);
            Log.i(TAG, "get imei in MTK or sim1");
        }
        if (imei == null) {
            return "null";
        }
        return imei;
    }

    public static String getVersionFOrAndroid() {
        if (TextUtils.isEmpty(VERSION.RELEASE)) {
            return "null";
        }
        return VERSION.RELEASE;
    }

    public static String getOppoRomVersion() {
        String ver = SystemProperties.get("ro.build.version.opporom");
        if (ver == null || ver.isEmpty()) {
            return "null";
        }
        return ver;
    }

    public static String getBuildVersion() {
        String ver = SystemProperties.get("ro.build.version.ota");
        if (ver == null || ver.isEmpty()) {
            return "null";
        }
        return ver;
    }

    public static void recordEventForLog(int event, String log) {
        if (sService != null || (init() ^ 1) == 0) {
            try {
                Log.v(TAG, "recordEventForLog event = " + event);
                sService.recordCriticalEvent(event, Process.myPid(), log);
            } catch (Exception e) {
                Log.v(TAG, "record exception e =" + e.toString());
            }
            return;
        }
        Log.d(TAG, "can not init the oppo service");
    }

    public static int writeLogToPartition(String logstring, String tagString, String issue) {
        Log.v(TAG, "this is the old api");
        return -1;
    }

    public static int writeLogToPartition(int type, String logstring, String tagString, String issue, int isOnlyAddToDropbox) {
        if (logstring == null) {
            return -1;
        }
        String tagbegin = "";
        String tagend = "";
        String time = "log-time: " + getTime();
        String buildTime = "log-buildTime: " + SystemProperties.get("ro.build.version.ota", "") + "\n";
        String colorOS = "log-colorOS: " + SystemProperties.get("ro.build.version.opporom", "") + "\n";
        String logType = String.format("LOGTYPE: %d\n", new Object[]{Integer.valueOf(type)});
        if (issue == null || issue.isEmpty()) {
            issue = tagString;
        }
        if (tagString.equals(ANDROID_TAG)) {
            tagbegin = "<android-" + issue + "-begin>\n";
            tagend = "\n<android-" + issue + "-end>\n";
        } else if (tagString.equals(MULTIMEDIA_TAG)) {
            tagbegin = "<multimedia-" + issue + "-begin>\n";
            tagend = "\n<multimedia-" + issue + "-end>\n";
        } else if (tagString.equals(NETWORK_TAG)) {
            tagbegin = "<network-" + issue + "-begin>\n";
            tagend = "\n<network-" + issue + "-end>\n";
        } else if (tagString.equals(KERNEL_TAG)) {
            tagbegin = "<kernel-" + issue + "-begin>\n";
            tagend = "\n<kernel-" + issue + "-end>\n";
        } else if (tagString.equals(CONNECT_TAG)) {
            tagbegin = "<connectivity-" + issue + "-begin>\n";
            tagend = "\n<connectivity-" + issue + "-end>\n";
        } else if (tagString.equals(CAMERA_TAG)) {
            tagbegin = "<camera-" + issue + "-begin>\n";
            tagend = "\n<camera-" + issue + "-end>\n";
        } else {
            Log.v(TAG, "the invalid tag");
            return -1;
        }
        return writeRawPartition(type, tagbegin + logType + time + buildTime + colorOS + logstring + tagend, isOnlyAddToDropbox);
    }

    public static int incrementCriticalData(int type, String desc) {
        return writeLogToPartition(type, null, null, null, desc);
    }

    public static int writeLogToPartition(int type, String logstring, String tagString, String issue, String desc) {
        int res;
        if (logstring == null) {
            res = 0;
        } else if (logstring.isEmpty()) {
            Log.v(TAG, "log is empty");
            res = 0;
        } else {
            res = writeLogToPartition(type, logstring, tagString, issue, -1);
        }
        int upRes = updateLogReference(type, desc, false);
        if (type > 19) {
            upRes = updateLogReference(type, desc, true);
        }
        if (upRes == -1 && res == -1) {
            return -3;
        }
        if (upRes != -1 || res == -1) {
            return (upRes == -1 || res != -1) ? 1 : -1;
        } else {
            return -2;
        }
    }

    private static int updateLogReference(int type, String desc, boolean isBackup) {
        String ref;
        int res;
        if (isBackup) {
            ref = readCriticalData(type + 1024, 256);
            Log.v(TAG, "updateLogReference read backup type=" + (type + 1024) + " ref=" + ref);
        } else {
            ref = readCriticalData(type, 256);
            Log.v(TAG, "updateLogReference read now type=" + type + " ref=" + ref);
        }
        if (ref == null || ref.isEmpty()) {
            ref = String.format("%d:%s:%d", new Object[]{Integer.valueOf(type), desc, Integer.valueOf(1)});
        } else {
            String[] refSplit = ref.split(":");
            if (refSplit == null || refSplit.length < 2) {
                Log.v(TAG, "update can not get any keyword");
                ref = String.format("%d:%s:%d", new Object[]{Integer.valueOf(type), desc, Integer.valueOf(1)});
            } else {
                try {
                    int count = Integer.parseInt(refSplit[2]);
                    if (desc.equals(refSplit[1])) {
                        ref = String.format("%d:%s:%d", new Object[]{Integer.valueOf(type), desc, Integer.valueOf(count + 1)});
                    } else {
                        ref = String.format("%d:%s:%d", new Object[]{Integer.valueOf(type), desc, Integer.valueOf(1)});
                    }
                } catch (Exception e) {
                    Log.v(TAG, "catch e = " + e.toString());
                    ref = String.format("%d:%s:%d", new Object[]{Integer.valueOf(type), desc, Integer.valueOf(1)});
                }
            }
        }
        if (isBackup) {
            res = writeCriticalData(type + 1024, ref);
        } else {
            res = writeCriticalData(type, ref);
        }
        Log.v(TAG, "updateLogReference res=" + res);
        return res;
    }

    public static boolean isEmmcLimit(int type) {
        try {
            String[] refSplit = readCriticalData(type + 1024, 256).split(":");
            if (refSplit == null || refSplit.length < 2) {
                Log.v(TAG, "the refs is not formative");
                return false;
            }
            try {
                if (Integer.parseInt(refSplit[2]) < BluetoothInputDevice.INPUT_DISCONNECT_FAILED_NOT_CONNECTED) {
                    return false;
                }
            } catch (Exception e) {
                Log.v(TAG, "catch e = " + e.toString());
            }
            Log.v(TAG, "limit to record type = " + type);
            return true;
        } catch (Exception e2) {
            Log.v(TAG, "isEmmcLimit exception e = " + e2.toString());
            return false;
        }
    }

    public static int cleanItem(int id) {
        return native_oppoManager_cleanItem(id);
    }

    public static int syncCacheToEmmc() {
        native_oppoManager_syncCahceToEmmc();
        return 0;
    }

    public static int updateConfig() {
        native_oppoManager_updateConfig();
        return 0;
    }

    public static int testFunc(int id, int size) {
        native_oppoManager_testFunc(id, size);
        return 0;
    }
}
