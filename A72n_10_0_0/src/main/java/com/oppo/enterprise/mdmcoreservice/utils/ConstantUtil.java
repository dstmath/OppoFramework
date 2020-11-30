package com.oppo.enterprise.mdmcoreservice.utils;

import android.os.SystemProperties;

public final class ConstantUtil {
    public static final int AUTO_SCREENOFF_TIME_10MIN = 600000;
    public static final int AUTO_SCREENOFF_TIME_15S = 15000;
    public static final int AUTO_SCREENOFF_TIME_1MIN = 60000;
    public static final int AUTO_SCREENOFF_TIME_2MIN = 120000;
    public static final int AUTO_SCREENOFF_TIME_30MIN = 1800000;
    public static final int AUTO_SCREENOFF_TIME_30S = 30000;
    public static final int AUTO_SCREENOFF_TIME_5MIN = 300000;
    public static final int AUTO_SCREENOFF_TIME_NOT_SET = 0;
    public static final String BLUETOOTH_POLICY = "persist.sys.bluetooth_policy";
    public static final String CUSTOM_FEATURE = "oppo.business.custom";
    public static final int GPS_POLICY_ALWAYS_ENABLE = 1;
    public static final int GPS_POLICY_CLOSE = 3;
    public static final int GPS_POLICY_DISABLE = 0;
    public static final int GPS_POLICY_NORMAL = 2;
    public static final int GPS_POLICY_OPEN = 4;
    public static final String MANUAL_TYPE_BADGE = "badge_option";
    public static final String MANUAL_TYPE_BANNER = "banner";
    public static final String MANUAL_TYPE_CHANNEL_NOTIFICATION = "channel";
    public static final String MANUAL_TYPE_LIGHT = "light";
    public static final String MANUAL_TYPE_LOCK_SCREEN = "lock_screen";
    public static final String MANUAL_TYPE_PRIORITY = "priority";
    public static final String MANUAL_TYPE_SHOW_ICON_STATUS_BAR = "show_icon";
    public static final String MANUAL_TYPE_SOUND = "sound";
    public static final String MANUAL_TYPE_VIBRATE = "vibrate";
    public static final int MODE_BT_CLOSE = 4;
    public static final int MODE_BT_DISABLED = 0;
    public static final int MODE_BT_ENABLED = 3;
    public static final int MODE_BT_NORMAL = 2;
    public static final int MODE_BT_OPEN = 5;
    public static final int MODE_BT_WHITELIST = 1;
    public static final String OPPO_CUSTOM_API_VERSION = (str.length() >= 11 ? str.substring(8, 11) : "0.0");
    public static final String SERVICE_NAME = "oppomdmservice";
    public static final String WIFI_AP_POLICY = "persist.sys.wifi_ap_policy";
    static String str = SystemProperties.get("ro.build.custom.mdmsdk.version", "");

    public static final class KeyguardPolicy {
        public static final int DISABLE_KEYGUARD_CAMERA = 1;
        public static final int DISABLE_KEYGUARD_FACE = 8;
        public static final int DISABLE_KEYGUARD_FINGERPRINT = 4;
        public static final int DISABLE_KEYGUARD_NOTIFICATION = 2;
    }

    public static final class QSConstant {
        public static final String CLICKABLE = "1";
        public static final String GREY = "1";
        public static final String NORMAL = "0";
        public static final String PERSIST_SYS_AIRPLANE_CLICKABLE = "persist.sys.airplane_clickable";
        public static final String PERSIST_SYS_AIRPLANE_GREY = "persist.sys.airplane_grey";
        public static final String PERSIST_SYS_ANDROIDBEAM_CLICKABLE = "persist.sys.androidbeam_clickable";
        public static final String PERSIST_SYS_ANDROIDBEAM_GREY = "persist.sys.androidbeam_grey";
        public static final String PERSIST_SYS_AP_CLICKABLE = "persist.sys.ap_clickable";
        public static final String PERSIST_SYS_AP_GREY = "persist.sys.ap_grey";
        public static final String PERSIST_SYS_BT_CLICKABLE = "persist.sys.bt_clickable";
        public static final String PERSIST_SYS_BT_GREY = "persist.sys.bt_grey";
        public static final String PERSIST_SYS_CELL_CLICKABLE = "persist.sys.cellular_clickable";
        public static final String PERSIST_SYS_CELL_GREY = "persist.sys.cellular_grey";
        public static final String PERSIST_SYS_GPS_ALWAYS_ENABLE = "persist.sys.gps.always_enable";
        public static final String PERSIST_SYS_GPS_CLICKABLE = "persist.sys.gps_clickable";
        public static final String PERSIST_SYS_GPS_DISABLE = "persist.sys.gps_disable";
        public static final String PERSIST_SYS_GPS_GREY = "persist.sys.gps_grey";
        public static final String PERSIST_SYS_GPS_MODE = "persist.sys.mdm_gps_mode";
        public static final String PERSIST_SYS_NFC_CLICKABLE = "persist.sys.nfc_clickable";
        public static final String PERSIST_SYS_NFC_GREY = "persist.sys.nfc_grey";
        public static final String PERSIST_SYS_P2P_CLICKABLE = "persist.sys.p2p_clickable";
        public static final String PERSIST_SYS_P2P_GREY = "persist.sys.p2p_grey";
        public static final String PERSIST_SYS_WIFI_CLICKABLE = "persist.sys.wifi_clickable";
        public static final String PERSIST_SYS_WIFI_GREY = "persist.sys.wifi_grey";
        public static final String UNCLICKABLE = "0";
    }

    public static final class SettingsMainConstant {
        public static final String KEY_ABOUT_PHONE = "about_phone";
        public static final String KEY_ACCOUNT_SETTINGS = "account_settings";
        public static final String KEY_APPLICATION_MANAGER = "application_manager";
        public static final String KEY_APP_USAGE_TIME = "app_usage_time";
        public static final String KEY_BATTERY_SETTINGS = "battery_settings";
        public static final String KEY_BLUETOOTH_SETTINGS = "bluetooth_settings";
        public static final String KEY_DATA_USAGE_SETTINGS = "data_usage_settings";
        public static final String KEY_DISPLAY_AND_BRIGHTNESS = "display_and_brightness";
        public static final String KEY_GAME_SPACE = "game_space";
        public static final String KEY_GOOGLE_SETTINGS = "google_settings";
        public static final String KEY_LANGUAGE_AND_REGION = "language_and_region";
        public static final String KEY_LEGAL_INFORMATION = "legal_information";
        public static final String KEY_MOBILE_DATA = "mobile_data";
        public static final String KEY_MORE_ABOUT_NETWORK = "more_about_network";
        public static final String KEY_MORE_ABOUT_SYSTEM = "more_about_system";
        public static final String KEY_MULTI_APP_SETTINGS = "multi_app_settings";
        public static final String KEY_NETWORK_DASHBOARD = "network_dashboard";
        public static final String KEY_NETWORK_SETTINGS = "network_settings";
        public static final String KEY_NOTIFICATION_AND_STATUSBAR = "notification_and_statusbar";
        public static final String KEY_OPPO_CLOUD_SERVICE = "oppo_cloud_service";
        public static final String KEY_OPPO_HOLIDAY_MODE = "oppo_holiday_mode";
        public static final String KEY_OTA_PREFERENCE = "ota_preference";
        public static final String KEY_OTHER_APPS = "other_apps";
        public static final String KEY_PERSONAL_ASSIST = "personal_assist";
        public static final String KEY_RESIZEABLE_SCREEN = "resizeable_screen";
        public static final String KEY_ROAMING_SETTINGS = "roaming_settings";
        public static final String KEY_SCREEN_LOCK = "screen_lock";
        public static final String KEY_SMART_CONVENIENT = "smart_convenient";
        public static final String KEY_SOUND_AND_VIBRATOR = "sound_and_vibrator";
        public static final String KEY_SWITCH_4G = "switch_4g";
        public static final String KEY_SYSTEM_LANGUAGE = "system_language";
        public static final String KEY_SYSTEM_SECURITY = "system_security";
        public static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
        public static final String KEY_USERCENTER_PREFERENCE = "usercenter_preference";
        public static final String KEY_USER_SETTINGS_TITLE = "user_settings_title";
        public static final String KEY_WALLPAPER = "wallpaper";
        public static final String KEY_WIFI_SETTINGS = "wifi_settings";
    }

    public static final class WifiApPolicy {
        public static final int DISABLED_AND_OPEN_ALLOWD = 3;
        public static final int DISABLED_AND_OPEN_NOT_ALLOWD = 1;
        public static final int ENABLED_AND_CLOSE_ALLOWED = 4;
        public static final int ENABLED_AND_CLOSE_NOT_ALLOWED = 2;
        public static final int NO_POLICY = 0;
    }

    public class NotificationSwitchType {
        public static final String CHANNEL = "channel";
        public static final String CHANNEL_BADGE = "channel_badge";
        public static final String CHANNEL_BANNER = "channel_banner";
        public static final String CHANNEL_BUBBLE = "channel_bubble";
        public static final String CHANNEL_BYPASS_DND = "channel_bypass_dnd";
        public static final String CHANNEL_LOCK_SCREEN = "channel_lock_screen";
        public static final String CHANNEL_SOUND = "channel_sound";
        public static final String CHANNEL_STATUS_BAR = "channel_status_bar";
        public static final String CHANNEL_VIBRATE = "channel_vibrate";

        public NotificationSwitchType() {
        }
    }
}
