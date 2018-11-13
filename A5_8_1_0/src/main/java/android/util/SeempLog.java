package android.util;

import android.hardware.Sensor;
import android.net.Uri;
import android.provider.Settings.System;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import java.util.HashMap;
import java.util.Map;

public final class SeempLog {
    public static final int SEEMP_API_android_provider_Settings__get_ACCELEROMETER_ROTATION_ = 96;
    public static final int SEEMP_API_android_provider_Settings__get_ADB_ENABLED_ = 98;
    public static final int SEEMP_API_android_provider_Settings__get_AIRPLANE_MODE_ON_ = 101;
    public static final int SEEMP_API_android_provider_Settings__get_AIRPLANE_MODE_RADIOS_ = 102;
    public static final int SEEMP_API_android_provider_Settings__get_ALARM_ALERT_ = 103;
    public static final int SEEMP_API_android_provider_Settings__get_ALWAYS_FINISH_ACTIVITIES_ = 105;
    public static final int SEEMP_API_android_provider_Settings__get_ANDROID_ID_ = 7;
    public static final int SEEMP_API_android_provider_Settings__get_ANIMATOR_DURATION_SCALE_ = 107;
    public static final int SEEMP_API_android_provider_Settings__get_APPEND_FOR_LAST_AUDIBLE_ = 171;
    public static final int SEEMP_API_android_provider_Settings__get_AUTO_TIME_ = 123;
    public static final int SEEMP_API_android_provider_Settings__get_AUTO_TIME_ZONE_ = 124;
    public static final int SEEMP_API_android_provider_Settings__get_BLUETOOTH_DISCOVERABILITY_ = 127;
    public static final int SEEMP_API_android_provider_Settings__get_BLUETOOTH_DISCOVERABILITY_TIMEOUT_ = 128;
    public static final int SEEMP_API_android_provider_Settings__get_BLUETOOTH_ON_ = 129;
    public static final int SEEMP_API_android_provider_Settings__get_DATA_ROAMING_ = 142;
    public static final int SEEMP_API_android_provider_Settings__get_DATE_FORMAT_ = 125;
    public static final int SEEMP_API_android_provider_Settings__get_DEBUG_APP_ = 99;
    public static final int SEEMP_API_android_provider_Settings__get_DEVICE_PROVISIONED_ = 130;
    public static final int SEEMP_API_android_provider_Settings__get_DIM_SCREEN_ = 115;
    public static final int SEEMP_API_android_provider_Settings__get_DTMF_TONE_WHEN_DIALING_ = 132;
    public static final int SEEMP_API_android_provider_Settings__get_END_BUTTON_BEHAVIOR_ = 133;
    public static final int SEEMP_API_android_provider_Settings__get_FONT_SCALE_ = 109;
    public static final int SEEMP_API_android_provider_Settings__get_HAPTIC_FEEDBACK_ENABLED_ = 159;
    public static final int SEEMP_API_android_provider_Settings__get_HTTP_PROXY_ = 143;
    public static final int SEEMP_API_android_provider_Settings__get_INSTALL_NON_MARKET_APPS_ = 136;
    public static final int SEEMP_API_android_provider_Settings__get_LOCATION_PROVIDERS_ALLOWED_ = 137;
    public static final int SEEMP_API_android_provider_Settings__get_LOCK_PATTERN_ENABLED_ = 138;
    public static final int SEEMP_API_android_provider_Settings__get_LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED_ = 139;
    public static final int SEEMP_API_android_provider_Settings__get_LOCK_PATTERN_VISIBLE_ = 140;
    public static final int SEEMP_API_android_provider_Settings__get_LOGGING_ID_ = 106;
    public static final int SEEMP_API_android_provider_Settings__get_MODE_RINGER_ = 135;
    public static final int SEEMP_API_android_provider_Settings__get_MODE_RINGER_STREAMS_AFFECTED_ = 168;
    public static final int SEEMP_API_android_provider_Settings__get_MUTE_STREAMS_AFFECTED_ = 169;
    public static final int SEEMP_API_android_provider_Settings__get_NETWORK_PREFERENCE_ = 141;
    public static final int SEEMP_API_android_provider_Settings__get_NEXT_ALARM_FORMATTED_ = 104;
    public static final int SEEMP_API_android_provider_Settings__get_NOTIFICATION_SOUND_ = 170;
    public static final int SEEMP_API_android_provider_Settings__get_PARENTAL_CONTROL_ENABLED_ = 144;
    public static final int SEEMP_API_android_provider_Settings__get_PARENTAL_CONTROL_LAST_UPDATE_ = 145;
    public static final int SEEMP_API_android_provider_Settings__get_PARENTAL_CONTROL_REDIRECT_URL_ = 146;
    public static final int SEEMP_API_android_provider_Settings__get_RADIO_BLUETOOTH_ = 147;
    public static final int SEEMP_API_android_provider_Settings__get_RADIO_CELL_ = 148;
    public static final int SEEMP_API_android_provider_Settings__get_RADIO_NFC_ = 149;
    public static final int SEEMP_API_android_provider_Settings__get_RADIO_WIFI_ = 150;
    public static final int SEEMP_API_android_provider_Settings__get_RINGTONE_ = 134;
    public static final int SEEMP_API_android_provider_Settings__get_SCREEN_BRIGHTNESS_ = 110;
    public static final int SEEMP_API_android_provider_Settings__get_SCREEN_BRIGHTNESS_MODE_ = 111;
    public static final int SEEMP_API_android_provider_Settings__get_SCREEN_BRIGHTNESS_MODE_AUTOMATIC_ = 112;
    public static final int SEEMP_API_android_provider_Settings__get_SCREEN_BRIGHTNESS_MODE_MANUAL_ = 113;
    public static final int SEEMP_API_android_provider_Settings__get_SCREEN_OFF_TIMEOUT_ = 114;
    public static final int SEEMP_API_android_provider_Settings__get_SETTINGS_CLASSNAME_ = 152;
    public static final int SEEMP_API_android_provider_Settings__get_SETUP_WIZARD_HAS_RUN_ = 131;
    public static final int SEEMP_API_android_provider_Settings__get_SHOW_GTALK_SERVICE_STATUS_ = 121;
    public static final int SEEMP_API_android_provider_Settings__get_SHOW_PROCESSES_ = 119;
    public static final int SEEMP_API_android_provider_Settings__get_SHOW_WEB_SUGGESTIONS_ = 120;
    public static final int SEEMP_API_android_provider_Settings__get_SOUND_EFFECTS_ENABLED_ = 167;
    public static final int SEEMP_API_android_provider_Settings__get_STAY_ON_WHILE_PLUGGED_IN_ = 117;
    public static final int SEEMP_API_android_provider_Settings__get_SYS_PROP_SETTING_VERSION_ = 151;
    public static final int SEEMP_API_android_provider_Settings__get_TEXT_AUTO_CAPS_ = 153;
    public static final int SEEMP_API_android_provider_Settings__get_TEXT_AUTO_PUNCTUATE_ = 154;
    public static final int SEEMP_API_android_provider_Settings__get_TEXT_AUTO_REPLACE_ = 155;
    public static final int SEEMP_API_android_provider_Settings__get_TEXT_SHOW_PASSWORD_ = 156;
    public static final int SEEMP_API_android_provider_Settings__get_TIME_12_24_ = 126;
    public static final int SEEMP_API_android_provider_Settings__get_TRANSITION_ANIMATION_SCALE_ = 116;
    public static final int SEEMP_API_android_provider_Settings__get_USB_MASS_STORAGE_ENABLED_ = 157;
    public static final int SEEMP_API_android_provider_Settings__get_USER_ROTATION_ = 97;
    public static final int SEEMP_API_android_provider_Settings__get_USE_GOOGLE_MAIL_ = 122;
    public static final int SEEMP_API_android_provider_Settings__get_VIBRATE_ON_ = 158;
    public static final int SEEMP_API_android_provider_Settings__get_VOLUME_ALARM_ = 160;
    public static final int SEEMP_API_android_provider_Settings__get_VOLUME_BLUETOOTH_SCO_ = 161;
    public static final int SEEMP_API_android_provider_Settings__get_VOLUME_MUSIC_ = 162;
    public static final int SEEMP_API_android_provider_Settings__get_VOLUME_NOTIFICATION_ = 163;
    public static final int SEEMP_API_android_provider_Settings__get_VOLUME_RING_ = 164;
    public static final int SEEMP_API_android_provider_Settings__get_VOLUME_SYSTEM_ = 165;
    public static final int SEEMP_API_android_provider_Settings__get_VOLUME_VOICE_ = 166;
    public static final int SEEMP_API_android_provider_Settings__get_WAIT_FOR_DEBUGGER_ = 100;
    public static final int SEEMP_API_android_provider_Settings__get_WALLPAPER_ACTIVITY_ = 118;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_MAX_DHCP_RETRY_COUNT_ = 172;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_MOBILE_DATA_TRANSITION_WAKELOCK_TIMEOUT_MS_ = 173;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON_ = 174;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY_ = 175;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_NUM_OPEN_NETWORKS_KEPT_ = 176;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_ON_ = 177;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_SLEEP_POLICY_ = 178;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_SLEEP_POLICY_DEFAULT_ = 179;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_SLEEP_POLICY_NEVER_ = 180;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED_ = 181;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_STATIC_DNS1_ = 182;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_STATIC_DNS2_ = 183;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_STATIC_GATEWAY_ = 184;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_STATIC_IP_ = 185;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_STATIC_NETMASK_ = 186;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_USE_STATIC_IP_ = 187;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_ACCEPTABLE_PACKET_LOSS_PERCENTAGE_ = 188;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_AP_COUNT_ = 189;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_BACKGROUND_CHECK_DELAY_MS_ = 190;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_BACKGROUND_CHECK_ENABLED_ = 191;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_BACKGROUND_CHECK_TIMEOUT_MS_ = 192;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_INITIAL_IGNORED_PING_COUNT_ = 193;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_MAX_AP_CHECKS_ = 194;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_ON_ = 195;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_PING_COUNT_ = 196;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_PING_DELAY_MS_ = 197;
    public static final int SEEMP_API_android_provider_Settings__get_WIFI_WATCHDOG_PING_TIMEOUT_MS_ = 198;
    public static final int SEEMP_API_android_provider_Settings__get_WINDOW_ANIMATION_SCALE_ = 108;
    public static final int SEEMP_API_android_provider_Settings__put_ACCELEROMETER_ROTATION_ = 199;
    public static final int SEEMP_API_android_provider_Settings__put_ADB_ENABLED_ = 201;
    public static final int SEEMP_API_android_provider_Settings__put_AIRPLANE_MODE_ON_ = 204;
    public static final int SEEMP_API_android_provider_Settings__put_AIRPLANE_MODE_RADIOS_ = 205;
    public static final int SEEMP_API_android_provider_Settings__put_ALARM_ALERT_ = 206;
    public static final int SEEMP_API_android_provider_Settings__put_ALWAYS_FINISH_ACTIVITIES_ = 208;
    public static final int SEEMP_API_android_provider_Settings__put_ANDROID_ID_ = 209;
    public static final int SEEMP_API_android_provider_Settings__put_ANIMATOR_DURATION_SCALE_ = 211;
    public static final int SEEMP_API_android_provider_Settings__put_APPEND_FOR_LAST_AUDIBLE_ = 275;
    public static final int SEEMP_API_android_provider_Settings__put_AUTO_TIME_ = 227;
    public static final int SEEMP_API_android_provider_Settings__put_AUTO_TIME_ZONE_ = 228;
    public static final int SEEMP_API_android_provider_Settings__put_BLUETOOTH_DISCOVERABILITY_ = 231;
    public static final int SEEMP_API_android_provider_Settings__put_BLUETOOTH_DISCOVERABILITY_TIMEOUT_ = 232;
    public static final int SEEMP_API_android_provider_Settings__put_BLUETOOTH_ON_ = 233;
    public static final int SEEMP_API_android_provider_Settings__put_DATA_ROAMING_ = 246;
    public static final int SEEMP_API_android_provider_Settings__put_DATE_FORMAT_ = 229;
    public static final int SEEMP_API_android_provider_Settings__put_DEBUG_APP_ = 202;
    public static final int SEEMP_API_android_provider_Settings__put_DEVICE_PROVISIONED_ = 234;
    public static final int SEEMP_API_android_provider_Settings__put_DIM_SCREEN_ = 219;
    public static final int SEEMP_API_android_provider_Settings__put_DTMF_TONE_WHEN_DIALING_ = 236;
    public static final int SEEMP_API_android_provider_Settings__put_END_BUTTON_BEHAVIOR_ = 237;
    public static final int SEEMP_API_android_provider_Settings__put_FONT_SCALE_ = 213;
    public static final int SEEMP_API_android_provider_Settings__put_HAPTIC_FEEDBACK_ENABLED_ = 263;
    public static final int SEEMP_API_android_provider_Settings__put_HTTP_PROXY_ = 247;
    public static final int SEEMP_API_android_provider_Settings__put_INSTALL_NON_MARKET_APPS_ = 240;
    public static final int SEEMP_API_android_provider_Settings__put_LOCATION_PROVIDERS_ALLOWED_ = 241;
    public static final int SEEMP_API_android_provider_Settings__put_LOCK_PATTERN_ENABLED_ = 242;
    public static final int SEEMP_API_android_provider_Settings__put_LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED_ = 243;
    public static final int SEEMP_API_android_provider_Settings__put_LOCK_PATTERN_VISIBLE_ = 244;
    public static final int SEEMP_API_android_provider_Settings__put_LOGGING_ID_ = 210;
    public static final int SEEMP_API_android_provider_Settings__put_MODE_RINGER_ = 239;
    public static final int SEEMP_API_android_provider_Settings__put_MODE_RINGER_STREAMS_AFFECTED_ = 272;
    public static final int SEEMP_API_android_provider_Settings__put_MUTE_STREAMS_AFFECTED_ = 273;
    public static final int SEEMP_API_android_provider_Settings__put_NETWORK_PREFERENCE_ = 245;
    public static final int SEEMP_API_android_provider_Settings__put_NEXT_ALARM_FORMATTED_ = 207;
    public static final int SEEMP_API_android_provider_Settings__put_NOTIFICATION_SOUND_ = 274;
    public static final int SEEMP_API_android_provider_Settings__put_PARENTAL_CONTROL_ENABLED_ = 248;
    public static final int SEEMP_API_android_provider_Settings__put_PARENTAL_CONTROL_LAST_UPDATE_ = 249;
    public static final int SEEMP_API_android_provider_Settings__put_PARENTAL_CONTROL_REDIRECT_URL_ = 250;
    public static final int SEEMP_API_android_provider_Settings__put_RADIO_BLUETOOTH_ = 251;
    public static final int SEEMP_API_android_provider_Settings__put_RADIO_CELL_ = 252;
    public static final int SEEMP_API_android_provider_Settings__put_RADIO_NFC_ = 253;
    public static final int SEEMP_API_android_provider_Settings__put_RADIO_WIFI_ = 254;
    public static final int SEEMP_API_android_provider_Settings__put_RINGTONE_ = 238;
    public static final int SEEMP_API_android_provider_Settings__put_SCREEN_BRIGHTNESS_ = 214;
    public static final int SEEMP_API_android_provider_Settings__put_SCREEN_BRIGHTNESS_MODE_ = 215;
    public static final int SEEMP_API_android_provider_Settings__put_SCREEN_BRIGHTNESS_MODE_AUTOMATIC_ = 216;
    public static final int SEEMP_API_android_provider_Settings__put_SCREEN_BRIGHTNESS_MODE_MANUAL_ = 217;
    public static final int SEEMP_API_android_provider_Settings__put_SCREEN_OFF_TIMEOUT_ = 218;
    public static final int SEEMP_API_android_provider_Settings__put_SETTINGS_CLASSNAME_ = 256;
    public static final int SEEMP_API_android_provider_Settings__put_SETUP_WIZARD_HAS_RUN_ = 235;
    public static final int SEEMP_API_android_provider_Settings__put_SHOW_GTALK_SERVICE_STATUS_ = 225;
    public static final int SEEMP_API_android_provider_Settings__put_SHOW_PROCESSES_ = 223;
    public static final int SEEMP_API_android_provider_Settings__put_SHOW_WEB_SUGGESTIONS_ = 224;
    public static final int SEEMP_API_android_provider_Settings__put_SOUND_EFFECTS_ENABLED_ = 271;
    public static final int SEEMP_API_android_provider_Settings__put_STAY_ON_WHILE_PLUGGED_IN_ = 221;
    public static final int SEEMP_API_android_provider_Settings__put_SYS_PROP_SETTING_VERSION_ = 255;
    public static final int SEEMP_API_android_provider_Settings__put_TEXT_AUTO_CAPS_ = 257;
    public static final int SEEMP_API_android_provider_Settings__put_TEXT_AUTO_PUNCTUATE_ = 258;
    public static final int SEEMP_API_android_provider_Settings__put_TEXT_AUTO_REPLACE_ = 259;
    public static final int SEEMP_API_android_provider_Settings__put_TEXT_SHOW_PASSWORD_ = 260;
    public static final int SEEMP_API_android_provider_Settings__put_TIME_12_24_ = 230;
    public static final int SEEMP_API_android_provider_Settings__put_TRANSITION_ANIMATION_SCALE_ = 220;
    public static final int SEEMP_API_android_provider_Settings__put_USB_MASS_STORAGE_ENABLED_ = 261;
    public static final int SEEMP_API_android_provider_Settings__put_USER_ROTATION_ = 200;
    public static final int SEEMP_API_android_provider_Settings__put_USE_GOOGLE_MAIL_ = 226;
    public static final int SEEMP_API_android_provider_Settings__put_VIBRATE_ON_ = 262;
    public static final int SEEMP_API_android_provider_Settings__put_VOLUME_ALARM_ = 264;
    public static final int SEEMP_API_android_provider_Settings__put_VOLUME_BLUETOOTH_SCO_ = 265;
    public static final int SEEMP_API_android_provider_Settings__put_VOLUME_MUSIC_ = 266;
    public static final int SEEMP_API_android_provider_Settings__put_VOLUME_NOTIFICATION_ = 267;
    public static final int SEEMP_API_android_provider_Settings__put_VOLUME_RING_ = 268;
    public static final int SEEMP_API_android_provider_Settings__put_VOLUME_SYSTEM_ = 269;
    public static final int SEEMP_API_android_provider_Settings__put_VOLUME_VOICE_ = 270;
    public static final int SEEMP_API_android_provider_Settings__put_WAIT_FOR_DEBUGGER_ = 203;
    public static final int SEEMP_API_android_provider_Settings__put_WALLPAPER_ACTIVITY_ = 222;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_MAX_DHCP_RETRY_COUNT_ = 276;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_MOBILE_DATA_TRANSITION_WAKELOCK_TIMEOUT_MS_ = 277;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON_ = 278;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY_ = 279;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_NUM_OPEN_NETWORKS_KEPT_ = 280;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_ON_ = 281;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_SLEEP_POLICY_ = 282;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_SLEEP_POLICY_DEFAULT_ = 283;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_SLEEP_POLICY_NEVER_ = 284;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED_ = 285;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_STATIC_DNS1_ = 286;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_STATIC_DNS2_ = 287;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_STATIC_GATEWAY_ = 288;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_STATIC_IP_ = 289;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_STATIC_NETMASK_ = 290;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_USE_STATIC_IP_ = 291;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_ACCEPTABLE_PACKET_LOSS_PERCENTAGE_ = 292;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_AP_COUNT_ = 293;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_BACKGROUND_CHECK_DELAY_MS_ = 294;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_BACKGROUND_CHECK_ENABLED_ = 295;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_BACKGROUND_CHECK_TIMEOUT_MS_ = 296;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_INITIAL_IGNORED_PING_COUNT_ = 297;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_MAX_AP_CHECKS_ = 298;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_ON_ = 299;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_PING_COUNT_ = 300;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_PING_DELAY_MS_ = 301;
    public static final int SEEMP_API_android_provider_Settings__put_WIFI_WATCHDOG_PING_TIMEOUT_MS_ = 302;
    public static final int SEEMP_API_android_provider_Settings__put_WINDOW_ANIMATION_SCALE_ = 212;
    private static final Map<String, Integer> value_to_get_map = new HashMap(198);
    private static final Map<String, Integer> value_to_put_map = new HashMap(198);

    public static native int seemp_println_native(int i, String str);

    private SeempLog() {
    }

    public static int record(int api) {
        return seemp_println_native(api, "");
    }

    public static int record_str(int api, String msg) {
        if (msg != null) {
            return seemp_println_native(api, msg);
        }
        return seemp_println_native(api, "");
    }

    public static int record_sensor(int api, Sensor sensor) {
        if (sensor != null) {
            return seemp_println_native(api, "sensor=" + sensor.getType());
        }
        return seemp_println_native(api, "sensor=-1");
    }

    public static int record_sensor_rate(int api, Sensor sensor, int rate) {
        if (sensor != null) {
            return seemp_println_native(api, "sensor=" + sensor.getType() + ",rate=" + rate);
        }
        return seemp_println_native(api, "sensor=-1,rate=" + rate);
    }

    public static int record_uri(int api, Uri uri) {
        if (uri != null) {
            return seemp_println_native(api, "uri, " + uri.toString());
        }
        return seemp_println_native(api, "uri, null");
    }

    public static int record_vg_layout(int api, LayoutParams params) {
        try {
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) params;
            if (p != null) {
                return seemp_println_native(api, "window_type=" + p.type + ",window_flag=" + p.flags);
            }
            return seemp_println_native(api, "");
        } catch (ClassCastException e) {
            return seemp_println_native(api, "");
        }
    }

    static {
        value_to_get_map.put(System.NOTIFICATION_SOUND, Integer.valueOf(170));
        value_to_get_map.put(System.DTMF_TONE_WHEN_DIALING, Integer.valueOf(132));
        value_to_get_map.put("lock_pattern_autolock", Integer.valueOf(138));
        value_to_get_map.put("wifi_max_dhcp_retry_count", Integer.valueOf(172));
        value_to_get_map.put("auto_time", Integer.valueOf(123));
        value_to_get_map.put(System.SETUP_WIZARD_HAS_RUN, Integer.valueOf(131));
        value_to_get_map.put("wifi_watchdog_background_check_timeout_ms", Integer.valueOf(192));
        value_to_get_map.put("location_providers_allowed", Integer.valueOf(137));
        value_to_get_map.put(System.ALARM_ALERT, Integer.valueOf(103));
        value_to_get_map.put(System.VIBRATE_ON, Integer.valueOf(158));
        value_to_get_map.put("usb_mass_storage_enabled", Integer.valueOf(157));
        value_to_get_map.put("wifi_watchdog_ping_delay_ms", Integer.valueOf(197));
        value_to_get_map.put(System.FONT_SCALE, Integer.valueOf(109));
        value_to_get_map.put("wifi_watchdog_ap_count", Integer.valueOf(189));
        value_to_get_map.put("always_finish_activities", Integer.valueOf(105));
        value_to_get_map.put(System.ACCELEROMETER_ROTATION, Integer.valueOf(96));
        value_to_get_map.put("wifi_watchdog_ping_timeout_ms", Integer.valueOf(198));
        value_to_get_map.put(System.VOLUME_NOTIFICATION, Integer.valueOf(163));
        value_to_get_map.put("airplane_mode_on", Integer.valueOf(101));
        value_to_get_map.put("wifi_watchdog_background_check_delay_ms", Integer.valueOf(190));
        value_to_get_map.put(System.WIFI_STATIC_IP, Integer.valueOf(185));
        value_to_get_map.put("bluetooth", Integer.valueOf(147));
        value_to_get_map.put(System.BLUETOOTH_DISCOVERABILITY_TIMEOUT, Integer.valueOf(128));
        value_to_get_map.put(System.VOLUME_RING, Integer.valueOf(164));
        value_to_get_map.put(System.MODE_RINGER_STREAMS_AFFECTED, Integer.valueOf(168));
        value_to_get_map.put(System.VOLUME_SYSTEM, Integer.valueOf(165));
        value_to_get_map.put(System.SCREEN_OFF_TIMEOUT, Integer.valueOf(114));
        value_to_get_map.put("wifi", Integer.valueOf(150));
        value_to_get_map.put("auto_time_zone", Integer.valueOf(124));
        value_to_get_map.put(System.TEXT_AUTO_CAPS, Integer.valueOf(153));
        value_to_get_map.put(System.WALLPAPER_ACTIVITY, Integer.valueOf(118));
        value_to_get_map.put("animator_duration_scale", Integer.valueOf(107));
        value_to_get_map.put("wifi_num_open_networks_kept", Integer.valueOf(176));
        value_to_get_map.put("lock_pattern_visible_pattern", Integer.valueOf(140));
        value_to_get_map.put(System.VOLUME_VOICE, Integer.valueOf(166));
        value_to_get_map.put("debug_app", Integer.valueOf(99));
        value_to_get_map.put("wifi_on", Integer.valueOf(177));
        value_to_get_map.put(System.TEXT_SHOW_PASSWORD, Integer.valueOf(156));
        value_to_get_map.put("wifi_networks_available_repeat_delay", Integer.valueOf(175));
        value_to_get_map.put("wifi_sleep_policy", Integer.valueOf(178));
        value_to_get_map.put(System.VOLUME_MUSIC, Integer.valueOf(162));
        value_to_get_map.put("parental_control_last_update", Integer.valueOf(145));
        value_to_get_map.put("device_provisioned", Integer.valueOf(130));
        value_to_get_map.put("http_proxy", Integer.valueOf(143));
        value_to_get_map.put("android_id", Integer.valueOf(7));
        value_to_get_map.put("wifi_watchdog_max_ap_checks", Integer.valueOf(194));
        value_to_get_map.put(System.END_BUTTON_BEHAVIOR, Integer.valueOf(133));
        value_to_get_map.put(System.NEXT_ALARM_FORMATTED, Integer.valueOf(104));
        value_to_get_map.put("cell", Integer.valueOf(148));
        value_to_get_map.put("parental_control_enabled", Integer.valueOf(144));
        value_to_get_map.put("bluetooth_on", Integer.valueOf(129));
        value_to_get_map.put("window_animation_scale", Integer.valueOf(108));
        value_to_get_map.put("wifi_watchdog_background_check_enabled", Integer.valueOf(191));
        value_to_get_map.put(System.BLUETOOTH_DISCOVERABILITY, Integer.valueOf(127));
        value_to_get_map.put(System.WIFI_STATIC_DNS1, Integer.valueOf(182));
        value_to_get_map.put(System.WIFI_STATIC_DNS2, Integer.valueOf(183));
        value_to_get_map.put(System.HAPTIC_FEEDBACK_ENABLED, Integer.valueOf(159));
        value_to_get_map.put(System.SHOW_WEB_SUGGESTIONS, Integer.valueOf(120));
        value_to_get_map.put("parental_control_redirect_url", Integer.valueOf(146));
        value_to_get_map.put(System.DATE_FORMAT, Integer.valueOf(125));
        value_to_get_map.put("nfc", Integer.valueOf(149));
        value_to_get_map.put("airplane_mode_radios", Integer.valueOf(102));
        value_to_get_map.put("lock_pattern_tactile_feedback_enabled", Integer.valueOf(139));
        value_to_get_map.put(System.TIME_12_24, Integer.valueOf(126));
        value_to_get_map.put("wifi_watchdog_initial_ignored_ping_count", Integer.valueOf(193));
        value_to_get_map.put(System.VOLUME_BLUETOOTH_SCO, Integer.valueOf(161));
        value_to_get_map.put(System.USER_ROTATION, Integer.valueOf(97));
        value_to_get_map.put(System.WIFI_STATIC_GATEWAY, Integer.valueOf(184));
        value_to_get_map.put("stay_on_while_plugged_in", Integer.valueOf(117));
        value_to_get_map.put(System.SOUND_EFFECTS_ENABLED, Integer.valueOf(167));
        value_to_get_map.put("wifi_watchdog_ping_count", Integer.valueOf(196));
        value_to_get_map.put("data_roaming", Integer.valueOf(142));
        value_to_get_map.put("settings_classname", Integer.valueOf(152));
        value_to_get_map.put("transition_animation_scale", Integer.valueOf(116));
        value_to_get_map.put("wait_for_debugger", Integer.valueOf(100));
        value_to_get_map.put("install_non_market_apps", Integer.valueOf(136));
        value_to_get_map.put("adb_enabled", Integer.valueOf(98));
        value_to_get_map.put(System.WIFI_USE_STATIC_IP, Integer.valueOf(187));
        value_to_get_map.put(System.DIM_SCREEN, Integer.valueOf(115));
        value_to_get_map.put(System.VOLUME_ALARM, Integer.valueOf(160));
        value_to_get_map.put("wifi_watchdog_on", Integer.valueOf(195));
        value_to_get_map.put(System.WIFI_STATIC_NETMASK, Integer.valueOf(186));
        value_to_get_map.put("network_preference", Integer.valueOf(141));
        value_to_get_map.put("show_processes", Integer.valueOf(119));
        value_to_get_map.put(System.TEXT_AUTO_REPLACE, Integer.valueOf(155));
        value_to_get_map.put("wifi_networks_available_notification_on", Integer.valueOf(174));
        value_to_get_map.put(System.APPEND_FOR_LAST_AUDIBLE, Integer.valueOf(171));
        value_to_get_map.put(System.SHOW_GTALK_SERVICE_STATUS, Integer.valueOf(121));
        value_to_get_map.put(System.SCREEN_BRIGHTNESS, Integer.valueOf(110));
        value_to_get_map.put("use_google_mail", Integer.valueOf(122));
        value_to_get_map.put(System.RINGTONE, Integer.valueOf(134));
        value_to_get_map.put("logging_id", Integer.valueOf(106));
        value_to_get_map.put("mode_ringer", Integer.valueOf(135));
        value_to_get_map.put(System.MUTE_STREAMS_AFFECTED, Integer.valueOf(169));
        value_to_get_map.put("wifi_watchdog_acceptable_packet_loss_percentage", Integer.valueOf(188));
        value_to_get_map.put(System.TEXT_AUTO_PUNCTUATE, Integer.valueOf(154));
        value_to_get_map.put("wifi_mobile_data_transition_wakelock_timeout_ms", Integer.valueOf(173));
        value_to_get_map.put(System.SCREEN_BRIGHTNESS_MODE, Integer.valueOf(111));
        value_to_put_map.put(System.NOTIFICATION_SOUND, Integer.valueOf(274));
        value_to_put_map.put(System.DTMF_TONE_WHEN_DIALING, Integer.valueOf(236));
        value_to_put_map.put("lock_pattern_autolock", Integer.valueOf(242));
        value_to_put_map.put("wifi_max_dhcp_retry_count", Integer.valueOf(276));
        value_to_put_map.put("auto_time", Integer.valueOf(227));
        value_to_put_map.put(System.SETUP_WIZARD_HAS_RUN, Integer.valueOf(235));
        value_to_put_map.put("wifi_watchdog_background_check_timeout_ms", Integer.valueOf(296));
        value_to_put_map.put("location_providers_allowed", Integer.valueOf(241));
        value_to_put_map.put(System.ALARM_ALERT, Integer.valueOf(206));
        value_to_put_map.put(System.VIBRATE_ON, Integer.valueOf(262));
        value_to_put_map.put("usb_mass_storage_enabled", Integer.valueOf(261));
        value_to_put_map.put("wifi_watchdog_ping_delay_ms", Integer.valueOf(301));
        value_to_put_map.put(System.FONT_SCALE, Integer.valueOf(213));
        value_to_put_map.put("wifi_watchdog_ap_count", Integer.valueOf(293));
        value_to_put_map.put("always_finish_activities", Integer.valueOf(208));
        value_to_put_map.put(System.ACCELEROMETER_ROTATION, Integer.valueOf(199));
        value_to_put_map.put("wifi_watchdog_ping_timeout_ms", Integer.valueOf(302));
        value_to_put_map.put(System.VOLUME_NOTIFICATION, Integer.valueOf(267));
        value_to_put_map.put("airplane_mode_on", Integer.valueOf(204));
        value_to_put_map.put("wifi_watchdog_background_check_delay_ms", Integer.valueOf(294));
        value_to_put_map.put(System.WIFI_STATIC_IP, Integer.valueOf(289));
        value_to_put_map.put("bluetooth", Integer.valueOf(251));
        value_to_put_map.put(System.BLUETOOTH_DISCOVERABILITY_TIMEOUT, Integer.valueOf(232));
        value_to_put_map.put(System.VOLUME_RING, Integer.valueOf(268));
        value_to_put_map.put(System.MODE_RINGER_STREAMS_AFFECTED, Integer.valueOf(272));
        value_to_put_map.put(System.VOLUME_SYSTEM, Integer.valueOf(269));
        value_to_put_map.put(System.SCREEN_OFF_TIMEOUT, Integer.valueOf(218));
        value_to_put_map.put("wifi", Integer.valueOf(254));
        value_to_put_map.put("auto_time_zone", Integer.valueOf(228));
        value_to_put_map.put(System.TEXT_AUTO_CAPS, Integer.valueOf(257));
        value_to_put_map.put(System.WALLPAPER_ACTIVITY, Integer.valueOf(222));
        value_to_put_map.put("animator_duration_scale", Integer.valueOf(211));
        value_to_put_map.put("wifi_num_open_networks_kept", Integer.valueOf(280));
        value_to_put_map.put("lock_pattern_visible_pattern", Integer.valueOf(244));
        value_to_put_map.put(System.VOLUME_VOICE, Integer.valueOf(270));
        value_to_put_map.put("debug_app", Integer.valueOf(202));
        value_to_put_map.put("wifi_on", Integer.valueOf(281));
        value_to_put_map.put(System.TEXT_SHOW_PASSWORD, Integer.valueOf(260));
        value_to_put_map.put("wifi_networks_available_repeat_delay", Integer.valueOf(279));
        value_to_put_map.put("wifi_sleep_policy", Integer.valueOf(282));
        value_to_put_map.put(System.VOLUME_MUSIC, Integer.valueOf(266));
        value_to_put_map.put("parental_control_last_update", Integer.valueOf(249));
        value_to_put_map.put("device_provisioned", Integer.valueOf(234));
        value_to_put_map.put("http_proxy", Integer.valueOf(247));
        value_to_put_map.put("android_id", Integer.valueOf(209));
        value_to_put_map.put("wifi_watchdog_max_ap_checks", Integer.valueOf(298));
        value_to_put_map.put(System.END_BUTTON_BEHAVIOR, Integer.valueOf(237));
        value_to_put_map.put(System.NEXT_ALARM_FORMATTED, Integer.valueOf(207));
        value_to_put_map.put("cell", Integer.valueOf(252));
        value_to_put_map.put("parental_control_enabled", Integer.valueOf(248));
        value_to_put_map.put("bluetooth_on", Integer.valueOf(233));
        value_to_put_map.put("window_animation_scale", Integer.valueOf(212));
        value_to_put_map.put("wifi_watchdog_background_check_enabled", Integer.valueOf(295));
        value_to_put_map.put(System.BLUETOOTH_DISCOVERABILITY, Integer.valueOf(231));
        value_to_put_map.put(System.WIFI_STATIC_DNS1, Integer.valueOf(286));
        value_to_put_map.put(System.WIFI_STATIC_DNS2, Integer.valueOf(287));
        value_to_put_map.put(System.HAPTIC_FEEDBACK_ENABLED, Integer.valueOf(263));
        value_to_put_map.put(System.SHOW_WEB_SUGGESTIONS, Integer.valueOf(224));
        value_to_put_map.put("parental_control_redirect_url", Integer.valueOf(250));
        value_to_put_map.put(System.DATE_FORMAT, Integer.valueOf(229));
        value_to_put_map.put("nfc", Integer.valueOf(253));
        value_to_put_map.put("airplane_mode_radios", Integer.valueOf(205));
        value_to_put_map.put("lock_pattern_tactile_feedback_enabled", Integer.valueOf(243));
        value_to_put_map.put(System.TIME_12_24, Integer.valueOf(230));
        value_to_put_map.put("wifi_watchdog_initial_ignored_ping_count", Integer.valueOf(297));
        value_to_put_map.put(System.VOLUME_BLUETOOTH_SCO, Integer.valueOf(265));
        value_to_put_map.put(System.USER_ROTATION, Integer.valueOf(200));
        value_to_put_map.put(System.WIFI_STATIC_GATEWAY, Integer.valueOf(288));
        value_to_put_map.put("stay_on_while_plugged_in", Integer.valueOf(221));
        value_to_put_map.put(System.SOUND_EFFECTS_ENABLED, Integer.valueOf(271));
        value_to_put_map.put("wifi_watchdog_ping_count", Integer.valueOf(300));
        value_to_put_map.put("data_roaming", Integer.valueOf(246));
        value_to_put_map.put("settings_classname", Integer.valueOf(256));
        value_to_put_map.put("transition_animation_scale", Integer.valueOf(220));
        value_to_put_map.put("wait_for_debugger", Integer.valueOf(203));
        value_to_put_map.put("install_non_market_apps", Integer.valueOf(240));
        value_to_put_map.put("adb_enabled", Integer.valueOf(201));
        value_to_put_map.put(System.WIFI_USE_STATIC_IP, Integer.valueOf(291));
        value_to_put_map.put(System.DIM_SCREEN, Integer.valueOf(219));
        value_to_put_map.put(System.VOLUME_ALARM, Integer.valueOf(264));
        value_to_put_map.put("wifi_watchdog_on", Integer.valueOf(299));
        value_to_put_map.put(System.WIFI_STATIC_NETMASK, Integer.valueOf(290));
        value_to_put_map.put("network_preference", Integer.valueOf(245));
        value_to_put_map.put("show_processes", Integer.valueOf(223));
        value_to_put_map.put(System.TEXT_AUTO_REPLACE, Integer.valueOf(259));
        value_to_put_map.put("wifi_networks_available_notification_on", Integer.valueOf(278));
        value_to_put_map.put(System.APPEND_FOR_LAST_AUDIBLE, Integer.valueOf(275));
        value_to_put_map.put(System.SHOW_GTALK_SERVICE_STATUS, Integer.valueOf(225));
        value_to_put_map.put(System.SCREEN_BRIGHTNESS, Integer.valueOf(214));
        value_to_put_map.put("use_google_mail", Integer.valueOf(226));
        value_to_put_map.put(System.RINGTONE, Integer.valueOf(238));
        value_to_put_map.put("logging_id", Integer.valueOf(210));
        value_to_put_map.put("mode_ringer", Integer.valueOf(239));
        value_to_put_map.put(System.MUTE_STREAMS_AFFECTED, Integer.valueOf(273));
        value_to_put_map.put("wifi_watchdog_acceptable_packet_loss_percentage", Integer.valueOf(292));
        value_to_put_map.put(System.TEXT_AUTO_PUNCTUATE, Integer.valueOf(258));
        value_to_put_map.put("wifi_mobile_data_transition_wakelock_timeout_ms", Integer.valueOf(277));
        value_to_put_map.put(System.SCREEN_BRIGHTNESS_MODE, Integer.valueOf(215));
    }

    public static int getSeempGetApiIdFromValue(String v) {
        Integer result = (Integer) value_to_get_map.get(v);
        if (result == null) {
            result = Integer.valueOf(-1);
        }
        return result.intValue();
    }

    public static int getSeempPutApiIdFromValue(String v) {
        Integer result = (Integer) value_to_put_map.get(v);
        if (result == null) {
            result = Integer.valueOf(-1);
        }
        return result.intValue();
    }
}
