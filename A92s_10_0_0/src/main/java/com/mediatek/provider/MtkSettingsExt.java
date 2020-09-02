package com.mediatek.provider;

import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

public final class MtkSettingsExt {
    private static final String TAG = "MtkSettingsProviderExt";

    public static final class System {
        public static final String ACCELEROMETER_ROTATION_RESTORE = "accelerometer_rotation_restore";
        public static final String AUTO_TIME_GPS = "auto_time_gps";
        public static String BASE_VOICE_WAKEUP_COMMAND_KEY = "voice_wakeup_app";
        public static final String BG_POWER_SAVING_ENABLE = "background_power_saving_enable";
        public static final long DEFAULT_SIM_NOT_SET = -5;
        public static final long DEFAULT_SIM_SETTING_ALWAYS_ASK = -2;
        public static final String HDMI_AUDIO_OUTPUT_MODE = "hdmi_audio_output_mode";
        public static final String HDMI_CABLE_PLUGGED = "hdmi_cable_plugged";
        public static final String HDMI_ENABLE_STATUS = "hdmi_enable_status";
        public static final String HDMI_VIDEO_RESOLUTION = "hdmi_video_resolution";
        public static final String HDMI_VIDEO_SCALE = "hdmi_video_scale";
        public static final String LAST_SIMID_BEFORE_WIFI_DISCONNECTED = "last_simid_before_wifi_disconnected";
        public static final String MSIM_MODE_SETTING = "msim_mode_setting";
        public static final String SMS_SIM_SETTING = "sms_sim_setting";
        public static final long SMS_SIM_SETTING_AUTO = -3;
        public static final long VOICE_CALL_SIM_SETTING_INTERNET = -2;
        public static String VOICE_TRIGGER_ACTIVE_COMMAND_ID = "voice_trigger_active_command_id";
        public static String VOICE_TRIGGER_COMMAND_STATUS = "voice_trigger_command_status";
        public static String VOICE_TRIGGER_MODE = "voice_trigger_mode";
        public static String VOICE_WAKEUP_ACTIVE_COMMAND_ID = "voice_wakeup_active_command_id";
        public static String VOICE_WAKEUP_COMMAND_STATUS = "voice_wakeup_command_status";
        public static String VOICE_WAKEUP_MODE = "voice_wakeup_mode";
        public static final String VOLTE_DMYK_STATE_0 = "volte_dmyk_state_0";
        public static final String VOLTE_DMYK_STATE_1 = "volte_dmyk_state_1";
        public static final String WIFI_CONNECT_AP_TYPE = "wifi_ap_connect_type";
        public static final int WIFI_CONNECT_AP_TYPE_AUTO = 0;
        public static final String WIFI_CONNECT_REMINDER = "wifi_connect_reminder";
        public static final String WIFI_CONNECT_TYPE = "wifi_connect_type";
        public static final int WIFI_CONNECT_TYPE_ASK = 2;
        public static final int WIFI_CONNECT_TYPE_AUTO = 0;
        public static final int WIFI_CONNECT_TYPE_MANUL = 1;
        public static final String WIFI_HOTSPOT_AUTO_DISABLE = "wifi_hotspot_auto_disable";
        public static final int WIFI_HOTSPOT_AUTO_DISABLE_FOR_FIVE_MINS = 1;
        public static final String WIFI_HOTSPOT_IS_ALL_DEVICES_ALLOWED = "wifi_hotspot_is_all_devices_allowed";
        public static final String WIFI_HOTSPOT_MAX_CLIENT_NUM = "wifi_hotspot_max_client_num";
        public static final String WIFI_PRIORITY_TYPE = "wifi_priority_type";
        public static final int WIFI_PRIORITY_TYPE_DEFAULT = 0;
        public static final int WIFI_PRIORITY_TYPE_MAMUAL = 1;
        public static final int WIFI_SELECT_SSID_AUTO = 0;
        public static final String WIFI_SELECT_SSID_TYPE = "wifi_select_ssid_type";

        public static void setVoiceCommandValue(ContentResolver cr, String baseCommand, int commandId, String launchApp) {
            Settings.System.putString(cr, baseCommand + commandId, launchApp);
        }

        public static String getVoiceCommandValue(ContentResolver cr, String baseCommand, int commandId) {
            return Settings.System.getString(cr, baseCommand + commandId);
        }

        public static void putInPublicSettings(Set<String> PUBLIC_SETTINGS) {
            Log.d(MtkSettingsExt.TAG, "putInPublicSettings");
            PUBLIC_SETTINGS.add("msim_mode_setting");
            PUBLIC_SETTINGS.add("auto_time_gps");
            PUBLIC_SETTINGS.add(ACCELEROMETER_ROTATION_RESTORE);
            PUBLIC_SETTINGS.add(BG_POWER_SAVING_ENABLE);
            PUBLIC_SETTINGS.add(HDMI_ENABLE_STATUS);
            PUBLIC_SETTINGS.add(HDMI_VIDEO_RESOLUTION);
            PUBLIC_SETTINGS.add(HDMI_VIDEO_SCALE);
            PUBLIC_SETTINGS.add(HDMI_CABLE_PLUGGED);
            PUBLIC_SETTINGS.add(HDMI_AUDIO_OUTPUT_MODE);
            PUBLIC_SETTINGS.add(WIFI_CONNECT_TYPE);
            PUBLIC_SETTINGS.add(WIFI_CONNECT_AP_TYPE);
            PUBLIC_SETTINGS.add(WIFI_CONNECT_REMINDER);
            PUBLIC_SETTINGS.add(WIFI_PRIORITY_TYPE);
            PUBLIC_SETTINGS.add(WIFI_SELECT_SSID_TYPE);
            PUBLIC_SETTINGS.add(LAST_SIMID_BEFORE_WIFI_DISCONNECTED);
            PUBLIC_SETTINGS.add(VOLTE_DMYK_STATE_0);
            PUBLIC_SETTINGS.add(VOLTE_DMYK_STATE_1);
            PUBLIC_SETTINGS.add(BASE_VOICE_WAKEUP_COMMAND_KEY);
            PUBLIC_SETTINGS.add(VOICE_WAKEUP_MODE);
            PUBLIC_SETTINGS.add(VOICE_WAKEUP_COMMAND_STATUS);
            PUBLIC_SETTINGS.add(VOICE_WAKEUP_ACTIVE_COMMAND_ID);
            PUBLIC_SETTINGS.add(VOICE_TRIGGER_MODE);
            PUBLIC_SETTINGS.add(VOICE_TRIGGER_COMMAND_STATUS);
            PUBLIC_SETTINGS.add(VOICE_TRIGGER_ACTIVE_COMMAND_ID);
        }

        public static void moveToGlobal(HashSet<String> MOVED_TO_GLOBAL) {
            Log.d(MtkSettingsExt.TAG, "System moveToGlobal");
            MOVED_TO_GLOBAL.add("msim_mode_setting");
            MOVED_TO_GLOBAL.add("auto_time_gps");
        }

        public static void moveToSecure(HashSet<String> hashSet) {
            Log.d(MtkSettingsExt.TAG, "System moveToSecure");
        }
    }

    public static final class Secure {
        public static void moveToGlobal(HashSet<String> hashSet) {
            Log.d(MtkSettingsExt.TAG, "Secure moveToGlobal");
        }
    }

    public static final class Global {
        public static final String AUTO_TIME_GPS = "auto_time_gps";
        public static final String CURRENT_NETWORK_CALL = "current_network_call";
        public static final String CURRENT_NETWORK_SMS = "current_network_sms";
        public static final String DATA_SERVICE_ENABLED = "data_service_enabled";
        public static final String DOMESTIC_DATA_ROAMING = "domestic_data_roaming";
        public static final String DOMESTIC_DATA_ROAMING_GUARD = "domestic_data_roaming_guard";
        public static final String DOMESTIC_LTE_DATA_ROAMING = "domestic_data_roaming";
        public static final String DOMESTIC_VOICE_TEXT_ROAMING = "domestic_voice_text_roaming";
        public static final String DOMESTIC_VOICE_TEXT_ROAMING_GUARD = "domestic_voice_text_roaming_guard";
        public static final String INTERNATIONAL_DATA_ROAMING = "international_data_roaming";
        public static final String INTERNATIONAL_DATA_ROAMING_GUARD = "international_data_roaming_guard";
        public static final String INTERNATIONAL_TEXT_ROAMING_GUARD = "international_text_roaming_guard";
        public static final String INTERNATIONAL_VOICE_ROAMING_GUARD = "international_voice_roaming_guard";
        public static final String INTERNATIONAL_VOICE_TEXT_ROAMING = "international_voice_text_roaming";
        public static final String MSIM_MODE_SETTING = "msim_mode_setting";
        public static final String NFC_HCE_ON = "nfc_hce_on";
        public static final String NFC_MULTISE_ACTIVE = "nfc_multise_active";
        public static final String NFC_MULTISE_IN_SWITCHING = "nfc_multise_in_switching";
        public static final String NFC_MULTISE_IN_TRANSACTION = "nfc_multise_in_transation";
        public static final String NFC_MULTISE_LIST = "nfc_multise_list";
        public static final String NFC_MULTISE_ON = "nfc_multise_on";
        public static final String NFC_MULTISE_PREVIOUS = "nfc_multise_previous";
        public static final String NFC_RF_FIELD_ACTIVE = "nfc_rf_field_active";
        public static final String PRIMARY_SIM = "primary_sim";
        public static final String SMART_RAT_SWITCH_DEBUG = "smart_rat_switch_debug";
        public static final String SMART_RAT_SWITCH_ENABLED = "smart_rat_switch_enabled";
        public static final String TELECOM_RTT_AUDIO_MODE = "telecom_rtt_adudio_mode";
        public static final String TELEPHONY_MISC_FEATURE_CONFIG = "telephony_misc_feature_config";
        public static final String WIFI_DISPLAY_AUTO_CHANNEL_SELECTION = "wifi_display_auto_channel_selection";
        public static final String WIFI_DISPLAY_CHOSEN_CAPABILITY = "wifi_display_chosen_capability";
        public static final String WIFI_DISPLAY_DISPLAY_NOTIFICATION_TIME = "wifi_display_notification_time";
        public static final String WIFI_DISPLAY_DISPLAY_TOAST_TIME = "wifi_display_display_toast_time";
        public static final String WIFI_DISPLAY_LATENCY_PROFILING = "wifi_display_latency_profiling";
        public static final String WIFI_DISPLAY_PORTRAIT_RESOLUTION = "wifi_display_portrait_resolution";
        public static final String WIFI_DISPLAY_POWER_SAVING_DELAY = "wifi_display_power_saving_delay";
        public static final String WIFI_DISPLAY_POWER_SAVING_OPTION = "wifi_display_power_saving_option";
        public static final String WIFI_DISPLAY_QE_ON = "wifi_display_qe_on";
        public static final String WIFI_DISPLAY_RESOLUTION = "wifi_display_max_resolution";
        public static final String WIFI_DISPLAY_RESOLUTION_DONOT_REMIND = "wifi_display_change_resolution_remind";
        public static final String WIFI_DISPLAY_SECURITY_OPTION = "wifi_display_security_option";
        public static final String WIFI_DISPLAY_SOUND_PATH_DONOT_REMIND = "wifi_display_sound_path_do_not_remind";
        public static final String WIFI_DISPLAY_SQC_INFO_ON = "wifi_display_sqc_info_on";
        public static final String WIFI_DISPLAY_WFD_LATENCY = "wifi_display_wfd_latency";
        public static final String WIFI_DISPLAY_WIFI_INFO = "wifi_display_wifi_info";

        public static void moveToSecure(HashSet<String> hashSet) {
            Log.d(MtkSettingsExt.TAG, "Global moveToSecure");
        }
    }
}
