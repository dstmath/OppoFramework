package com.mediatek.common.voicecommand;

public abstract class VoiceCommandListener {
    private static final int ACTION_COMMON_INDEX = 1;
    public static final String ACTION_EXTRA_RESULT = "Result";
    public static final int ACTION_EXTRA_RESULT_ERROR = 10;
    public static final String ACTION_EXTRA_RESULT_INFO = "Result_Info";
    public static final String ACTION_EXTRA_RESULT_INFO1 = "Result_Info1";
    public static final String ACTION_EXTRA_RESULT_INFO2 = "Result_Info2";
    public static final String ACTION_EXTRA_RESULT_INFO3 = "Result_Info3";
    public static final String ACTION_EXTRA_RESULT_INFO4 = "Result_Info4";
    public static final String ACTION_EXTRA_RESULT_INFO5 = "Result_Info5";
    public static final int ACTION_EXTRA_RESULT_SUCCESS = 1;
    public static final String ACTION_EXTRA_SEND = "Send";
    public static final String ACTION_EXTRA_SEND_INFO = "Send_Info";
    public static final String ACTION_EXTRA_SEND_INFO1 = "Send_Info1";
    public static final String ACTION_EXTRA_SEND_INFO2 = "Send_Info2";
    public static final String ACTION_EXTRA_SEND_INFO3 = "Send_Info3";
    public static final int ACTION_MAIN_VOICE_COMMON = 1;
    public static final int ACTION_MAIN_VOICE_CONTACTS = 5;
    public static final int ACTION_MAIN_VOICE_RECOGNITION = 4;
    public static final int ACTION_MAIN_VOICE_TRAINING = 3;
    public static final int ACTION_MAIN_VOICE_TRIGGER = 7;
    public static final int ACTION_MAIN_VOICE_UI = 2;
    public static final int ACTION_MAIN_VOICE_WAKEUP = 6;
    public static final int ACTION_VOICE_COMMON_COMMAND_PATH = 2;
    public static final int ACTION_VOICE_COMMON_KEYWORD = 1;
    public static final int ACTION_VOICE_COMMON_PROCESS_STATE = 3;
    public static final int ACTION_VOICE_CONTACTS_DISABLE = 4;
    public static final int ACTION_VOICE_CONTACTS_ENABLE = 3;
    public static final int ACTION_VOICE_CONTACTS_INTENSITY = 5;
    public static final int ACTION_VOICE_CONTACTS_NAME = 8;
    public static final int ACTION_VOICE_CONTACTS_NOTIFY = 7;
    public static final int ACTION_VOICE_CONTACTS_ORIENTATION = 11;
    public static final int ACTION_VOICE_CONTACTS_RECOGNITION_DISABLE = 13;
    public static final int ACTION_VOICE_CONTACTS_RECOGNITION_ENABLE = 12;
    public static final int ACTION_VOICE_CONTACTS_SEARCH_COUNT = 10;
    public static final int ACTION_VOICE_CONTACTS_SELECTED = 6;
    public static final int ACTION_VOICE_CONTACTS_SPEECH_DETECTED = 9;
    public static final int ACTION_VOICE_CONTACTS_START = 1;
    public static final int ACTION_VOICE_CONTACTS_STOP = 2;
    public static final int ACTION_VOICE_RECOGNITION_INTENSITY = 2;
    public static final int ACTION_VOICE_RECOGNITION_NOTIFY = 3;
    public static final int ACTION_VOICE_RECOGNITION_START = 1;
    public static final int ACTION_VOICE_TRAINING_CHANGE_KEYPHRASE = 19;
    public static final int ACTION_VOICE_TRAINING_CONTINUE = 9;
    public static final int ACTION_VOICE_TRAINING_ENROLL_START = 17;
    public static final int ACTION_VOICE_TRAINING_FINISH = 8;
    public static final int ACTION_VOICE_TRAINING_GET_USER_LIST = 18;
    public static final int ACTION_VOICE_TRAINING_INIT = 11;
    public static final int ACTION_VOICE_TRAINING_INTENSITY = 3;
    public static final int ACTION_VOICE_TRAINING_MODIFY = 7;
    public static final int ACTION_VOICE_TRAINING_MODIFY_PARAM = 12;
    public static final int ACTION_VOICE_TRAINING_NOTIFY = 5;
    public static final int ACTION_VOICE_TRAINING_NOTIFY_FINISH = 16;
    public static final int ACTION_VOICE_TRAINING_NOTIFY_PROGRESS = 15;
    public static final int ACTION_VOICE_TRAINING_PASSWORD_FILE = 4;
    public static final int ACTION_VOICE_TRAINING_PAUSE = 10;
    public static final int ACTION_VOICE_TRAINING_QUERY_PARAM = 13;
    public static final int ACTION_VOICE_TRAINING_RESET = 6;
    public static final int ACTION_VOICE_TRAINING_START = 1;
    public static final int ACTION_VOICE_TRAINING_STOP = 2;
    public static final int ACTION_VOICE_TRAINING_UPDATE_MODEL = 14;
    public static final int ACTION_VOICE_TRIGGER_COMMAND_STATUS = 8;
    public static final int ACTION_VOICE_TRIGGER_DISABLE = 3;
    public static final int ACTION_VOICE_TRIGGER_ENABLE = 2;
    public static final int ACTION_VOICE_TRIGGER_GET_INITIAL_PARAMS = 14;
    public static final int ACTION_VOICE_TRIGGER_GET_LOCALE = 12;
    public static final int ACTION_VOICE_TRIGGER_INIT = 6;
    public static final int ACTION_VOICE_TRIGGER_MODE = 7;
    public static final int ACTION_VOICE_TRIGGER_MODIFY_PARAM = 9;
    public static final int ACTION_VOICE_TRIGGER_NOTIFY_ARRIVED = 4;
    public static final int ACTION_VOICE_TRIGGER_NOTIFY_DETECTED = 5;
    public static final int ACTION_VOICE_TRIGGER_NOTIFY_PACKAGE_CHANGED = 15;
    public static final int ACTION_VOICE_TRIGGER_QUERY_PARAM = 10;
    public static final int ACTION_VOICE_TRIGGER_READ_STATUS = 13;
    public static final int ACTION_VOICE_TRIGGER_SET_LOCALE = 11;
    public static final int ACTION_VOICE_TRIGGER_START = 1;
    public static final int ACTION_VOICE_UI_DISALBE = 4;
    public static final int ACTION_VOICE_UI_ENABLE = 3;
    public static final int ACTION_VOICE_UI_NOTIFY = 5;
    public static final int ACTION_VOICE_UI_START = 1;
    public static final int ACTION_VOICE_UI_STOP = 2;
    public static final int ACTION_VOICE_WAKEUP_COMMAND_STATUS = 7;
    public static final int ACTION_VOICE_WAKEUP_DISABLE = 3;
    public static final int ACTION_VOICE_WAKEUP_ENABLE = 2;
    public static final int ACTION_VOICE_WAKEUP_INIT = 5;
    public static final int ACTION_VOICE_WAKEUP_MODE = 6;
    public static final int ACTION_VOICE_WAKEUP_NOTIFY = 4;
    public static final int ACTION_VOICE_WAKEUP_READ_STATUS = 8;
    public static final int ACTION_VOICE_WAKEUP_START = 1;
    public static final String VOICE_COMMAND_SERVICE = "voicecommand";
    public static final int VOICE_CONFIDENCE_THRESHOLD_DEFAULT = 50;
    public static final int VOICE_CONFIDENCE_THRESHOLD_MAX = 100;
    public static final int VOICE_CONFIDENCE_THRESHOLD_MIN = 0;
    private static final int VOICE_ERROR_COMMON = 1000;
    public static final int VOICE_ERROR_COMMON_ILLEGAL_PROCESS = 1005;
    public static final int VOICE_ERROR_COMMON_INVALID_ACTION = 1007;
    public static final int VOICE_ERROR_COMMON_INVALID_DATA = 1008;
    public static final int VOICE_ERROR_COMMON_NOTIFY_FAIL = 1009;
    public static final int VOICE_ERROR_COMMON_NO_PERMISSION = 1002;
    public static final int VOICE_ERROR_COMMON_PROCESS_OFF = 1001;
    public static final int VOICE_ERROR_COMMON_REGISTERED = 1003;
    public static final int VOICE_ERROR_COMMON_SERVICE = 1006;
    public static final int VOICE_ERROR_COMMON_UNREGISTER = 1004;
    private static final int VOICE_ERROR_CONTACTS = 400;
    public static final int VOICE_ERROR_CONTACTS_SEND_INVALID = 402;
    public static final int VOICE_ERROR_CONTACTS_VOICE_INVALID = 401;
    private static final int VOICE_ERROR_RECOGNIZE = 0;
    public static final int VOICE_ERROR_RECOGNIZE_DENIED = 1;
    public static final int VOICE_ERROR_RECOGNIZE_LOWLY = 3;
    public static final int VOICE_ERROR_RECOGNIZE_NOISY = 2;
    private static final int VOICE_ERROR_SETTING = 200;
    public static final int VOICE_ERROR_SETTING_LANGUAGE_UPDATE = 201;
    public static final int VOICE_ERROR_SETTING_VALUE_OUT_OF_RANGE = 202;
    private static final int VOICE_ERROR_TRAINING = 100;
    public static final int VOICE_ERROR_TRAINING_NOISY = 102;
    public static final int VOICE_ERROR_TRAINING_NOT_ENOUGH = 101;
    public static final int VOICE_ERROR_TRAINING_PASSWORD_DIFF = 103;
    public static final int VOICE_ERROR_TRAINING_PASSWORD_EXIST = 104;
    private static final int VOICE_ERROR_UI = 300;
    public static final int VOICE_ERROR_UI_INVALID = 301;
    public static final int VOICE_NO_ERROR = 0;
    public static final String VOICE_SERVICE_ACTION = "com.mediatek.voicecommand";
    public static final String VOICE_SERVICE_CATEGORY = "com.mediatek.nativeservice";
    public static final String VOICE_SERVICE_PACKAGE_NAME = "com.mediatek.voicecommand";
    public static final int VOICE_TRAINING_PARAM_CONFIDENCE_THRESHOLD = 1;
    public static final int VOICE_TRAINING_PARAM_REPEAT_TIMES = 2;
    public static final int VOICE_TRAINING_PARAM_TIMEOUT = 0;
    public static final String VOICE_TRAINING_SERVICE_ACTION = "com.mediatek.intent.action.bindEnrollmentService";
    public static final String VOICE_TRAINING_SERVICE_PACKAGE_NAME = "com.mediatek.voicecommand.vis";
    public static final int VOICE_TRIGGER_ANTI_SPOOF_THRESHOLD = 2;
    public static final int VOICE_TRIGGER_PARAM_COARSE_CONFIDENCE = 0;
    public static final int VOICE_TRIGGER_SECOND_STAGE_THRESHOLD = 1;
    public static final String VOICE_WAKEUP_ACTIVTY_ACTION = "com.mediatek.voicecommand.VOW_INTERACT";
    public static final int VOICE_WAKEUP_MODE_SPEAKER_DEPENDENT = 2;
    public static final int VOICE_WAKEUP_MODE_SPEAKER_INDEPENDENT = 1;
    public static final int VOICE_WAKEUP_MODE_TRIGGER = 3;
    public static final int VOICE_WAKEUP_MODE_UNLOCK = 0;
    public static final String VOICE_WAKEUP_SERVICE_ACTION = "com.mediatek.voicecommand.VoiceWakeupInteractionService";
    public static final int VOICE_WAKEUP_STATUS_COMMAND_CHECKED = 2;
    public static final int VOICE_WAKEUP_STATUS_COMMAND_UNCHECKED = 1;
    public static final int VOICE_WAKEUP_STATUS_NOCOMMAND_UNCHECKED = 0;
    public static final String VOW_ENROLLMENT_BCP47_LOCALE = "en-US";
    public static final String VOW_ENROLLMENT_TEXT = "Hello There";
    private static String[] sActionMainToStr = {"EMPTY", "ACTION_MAIN_VOICE_COMMON", "ACTION_MAIN_VOICE_UI", "ACTION_MAIN_VOICE_TRAINING", "ACTION_MAIN_VOICE_RECOGNITION", "ACTION_MAIN_VOICE_CONTACTS", "ACTION_MAIN_VOICE_WAKEUP", "ACTION_MAIN_VOICE_TRIGGER"};
    private static String[][] sActionVoiceToStr = {new String[]{"EMPTY", "EMPTY"}, new String[]{"EMPTY", "ACTION_VOICE_COMMON_KEYWORD", "ACTION_VOICE_COMMON_COMMAND_PATH", "ACTION_VOICE_COMMON_PROCESS_STATE"}, new String[]{"EMPTY", "ACTION_VOICE_UI_START", "ACTION_VOICE_UI_STOP", "ACTION_VOICE_UI_ENABLE", "ACTION_VOICE_UI_DISALBE", "ACTION_VOICE_UI_NOTIFY"}, new String[]{"EMPTY", "ACTION_VOICE_TRAINING_START", "ACTION_VOICE_TRAINING_STOP", "ACTION_VOICE_TRAINING_INTENSITY", "ACTION_VOICE_TRAINING_PASSWORD_FILE", "ACTION_VOICE_TRAINING_NOTIFY", "ACTION_VOICE_TRAINING_RESET", "ACTION_VOICE_TRAINING_MODIFY", "ACTION_VOICE_TRAINING_FINISH", "ACTION_VOICE_TRAINING_CONTINUE", "ACTION_VOICE_TRAINING_PAUSE", "ACTION_VOICE_TRAINING_INIT", "ACTION_VOICE_TRAINING_MODIFY_PARAM", "ACTION_VOICE_TRAINING_QUERY_PARAM", "ACTION_VOICE_TRAINING_UPDATE_MODEL", "ACTION_VOICE_TRAINING_NOTIFY_PROGRESS", "ACTION_VOICE_TRAINING_NOTIFY_FINISH", "ACTION_VOICE_TRAINING_ENROLL_START", "ACTION_VOICE_TRAINING_GET_USER_LIST", "ACTION_VOICE_TRAINING_CHANGE_KEYPHRASE"}, new String[]{"EMPTY", "ACTION_VOICE_RECOGNITION_START", "ACTION_VOICE_RECOGNITION_INTENSITY", "ACTION_VOICE_RECOGNITION_NOTIFY"}, new String[]{"EMPTY", "ACTION_VOICE_CONTACTS_START", "ACTION_VOICE_CONTACTS_STOP", "ACTION_VOICE_CONTACTS_ENABLE", "ACTION_VOICE_CONTACTS_DISABLE", "ACTION_VOICE_CONTACTS_INTENSITY", "ACTION_VOICE_CONTACTS_SELECTED", "ACTION_VOICE_CONTACTS_NOTIFY", "ACTION_VOICE_CONTACTS_NAME", "ACTION_VOICE_CONTACTS_SPEECH_DETECTED", "ACTION_VOICE_CONTACTS_SEARCH_COUNT", "ACTION_VOICE_CONTACTS_ORIENTATION", "ACTION_VOICE_CONTACTS_RECOGNITION_ENABLE", "ACTION_VOICE_CONTACTS_RECOGNITION_DISABLE"}, new String[]{"EMPTY", "ACTION_VOICE_WAKEUP_START", "ACTION_VOICE_WAKEUP_ENABLE", "ACTION_VOICE_WAKEUP_DISABLE", "ACTION_VOICE_WAKEUP_NOTIFY", "ACTION_VOICE_WAKEUP_INIT", "ACTION_VOICE_WAKEUP_MODE", "ACTION_VOICE_WAKEUP_COMMAND_STATUS", "ACTION_VOICE_WAKEUP_READ_STATUS"}, new String[]{"EMPTY", "ACTION_VOICE_TRIGGER_START", "ACTION_VOICE_TRIGGER_ENABLE", "ACTION_VOICE_TRIGGER_DISABLE", "ACTION_VOICE_TRIGGER_NOTIFY_ARRIVED", "ACTION_VOICE_TRIGGER_NOTIFY_DETECTED", "ACTION_VOICE_TRIGGER_INIT", "ACTION_VOICE_TRIGGER_MODE", "ACTION_VOICE_TRIGGER_COMMAND_STATUS", "ACTION_VOICE_TRIGGER_MODIFY_PARAM", "ACTION_VOICE_TRIGGER_QUERY_PARAM", "ACTION_VOICE_TRIGGER_SET_LOCALE", "ACTION_VOICE_TRIGGER_GET_LOCALE", "ACTION_VOICE_TRIGGER_READ_STATUS", "ACTION_VOICE_TRIGGER_GET_INITIAL_PARAMS", "ACTION_VOICE_TRIGGER_NOTIFY_PACKAGE_CHANGED"}};
    private static String[] sTrainingParamToStr = {"VOICE_TRAINING_PARAM_TIMEOUT", "VOICE_TRAINING_PARAM_CONFIDENCE_THRESHOLD", "VOICE_TRAINING_PARAM_REPEAT_TIMES"};
    private static String[] sTriggerParamToStr = {"VOICE_TRIGGER_PARAM_COARSE_CONFIDENCE", "VOICE_TRIGGER_SECOND_STAGE_THRESHOLD", "VOICE_TRIGGER_ANTI_SPOOF_THRESHOLD"};
    private static String[] sWakeupModeToStr = {"VOICE_WAKEUP_MODE_UNLOCK", "VOICE_WAKEUP_MODE_SPEAKER_INDEPENDENT", "VOICE_WAKEUP_MODE_SPEAKER_DEPENDENT", "VOICE_WAKEUP_MODE_TRIGGER"};
    private static String[] sWakeupStatusToStr = {"VOICE_WAKEUP_STATUS_NOCOMMAND_UNCHECKED", "VOICE_WAKEUP_STATUS_COMMAND_UNCHECKED", "VOICE_WAKEUP_STATUS_COMMAND_CHECKED"};

    public static String getMainActionName(int action) {
        return sActionMainToStr[action];
    }

    public static String getSubActionName(int mainAction, int subAction) {
        return sActionVoiceToStr[mainAction][subAction];
    }

    public static String getWakeupModeStr(int wakeupMode) {
        return sWakeupModeToStr[wakeupMode];
    }

    public static String getWakeupStatusStr(int wakeupStatus) {
        return sWakeupStatusToStr[wakeupStatus];
    }

    public static String getTrainingParamStr(int param) {
        return sTrainingParamToStr[param];
    }

    public static String getTriggerParamStr(int param) {
        return sTriggerParamToStr[param];
    }
}
