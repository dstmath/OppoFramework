package com.android.internal.logging;

import com.android.framework.protobuf.nano.InternalNano;
import com.android.framework.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.framework.protobuf.nano.MessageNano;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public interface MetricsProto {

    public static final class MetricsEvent extends MessageNano {
        public static final int ABOUT_LEGAL_SETTINGS = 225;
        public static final int ACCESSIBILITY = 2;
        public static final int ACCESSIBILITY_CAPTION_PROPERTIES = 3;
        public static final int ACCESSIBILITY_FONT_SIZE = 340;
        public static final int ACCESSIBILITY_SERVICE = 4;
        public static final int ACCESSIBILITY_TOGGLE_AUTOCLICK = 335;
        public static final int ACCESSIBILITY_TOGGLE_DALTONIZER = 5;
        public static final int ACCESSIBILITY_TOGGLE_GLOBAL_GESTURE = 6;
        public static final int ACCESSIBILITY_TOGGLE_SCREEN_MAGNIFICATION = 7;
        public static final int ACCOUNT = 8;
        public static final int ACCOUNTS_ACCOUNT_SYNC = 9;
        public static final int ACCOUNTS_CHOOSE_ACCOUNT_ACTIVITY = 10;
        public static final int ACCOUNTS_MANAGE_ACCOUNTS = 11;
        public static final int ACCOUNTS_WORK_PROFILE_SETTINGS = 401;
        public static final int ACTION_ACTIVITY_CHOOSER_PICKED_APP_TARGET = 215;
        public static final int ACTION_ACTIVITY_CHOOSER_PICKED_SERVICE_TARGET = 216;
        public static final int ACTION_ACTIVITY_CHOOSER_PICKED_STANDARD_TARGET = 217;
        public static final int ACTION_ACTIVITY_CHOOSER_SHOWN = 214;
        public static final int ACTION_ADD_EMERGENCY_CONTACT = 281;
        public static final int ACTION_AIRPLANE_TOGGLE = 177;
        public static final int ACTION_AMBIENT_DISPLAY = 495;
        public static final int ACTION_AMBIENT_GESTURE = 411;
        public static final int ACTION_APP_ANR = 317;
        public static final int ACTION_APP_CRASH = 316;
        public static final int ACTION_APP_DISAMBIG_ALWAYS = 455;
        public static final int ACTION_APP_DISAMBIG_JUST_ONCE = 456;
        public static final int ACTION_APP_DISAMBIG_TAP = 457;
        public static final int ACTION_APP_NOTE_SETTINGS = 206;
        public static final int ACTION_ASSIST_LONG_PRESS = 239;
        public static final int ACTION_BAN_APP_NOTES = 147;
        public static final int ACTION_BLUETOOTH_FILES = 162;
        public static final int ACTION_BLUETOOTH_RENAME = 161;
        public static final int ACTION_BLUETOOTH_SCAN = 160;
        public static final int ACTION_BLUETOOTH_TOGGLE = 159;
        public static final int ACTION_BRIGHTNESS = 218;
        public static final int ACTION_BRIGHTNESS_AUTO = 219;
        public static final int ACTION_BUGREPORT_DETAILS_CANCELED = 304;
        public static final int ACTION_BUGREPORT_DETAILS_DESCRIPTION_CHANGED = 302;
        public static final int ACTION_BUGREPORT_DETAILS_NAME_CHANGED = 300;
        public static final int ACTION_BUGREPORT_DETAILS_SAVED = 303;
        public static final int ACTION_BUGREPORT_DETAILS_TITLE_CHANGED = 301;
        public static final int ACTION_BUGREPORT_FROM_POWER_MENU_FULL = 293;
        public static final int ACTION_BUGREPORT_FROM_POWER_MENU_INTERACTIVE = 292;
        public static final int ACTION_BUGREPORT_FROM_SETTINGS_FULL = 295;
        public static final int ACTION_BUGREPORT_FROM_SETTINGS_INTERACTIVE = 294;
        public static final int ACTION_BUGREPORT_NOTIFICATION_ACTION_CANCEL = 296;
        public static final int ACTION_BUGREPORT_NOTIFICATION_ACTION_DETAILS = 297;
        public static final int ACTION_BUGREPORT_NOTIFICATION_ACTION_SCREENSHOT = 298;
        public static final int ACTION_BUGREPORT_NOTIFICATION_ACTION_SHARE = 299;
        public static final int ACTION_CALL_EMERGENCY_CONTACT = 283;
        public static final int ACTION_CELL_DATA_TOGGLE = 178;
        public static final int ACTION_DATA_SAVER_BLACKLIST = 396;
        public static final int ACTION_DATA_SAVER_MODE = 394;
        public static final int ACTION_DATA_SAVER_WHITELIST = 395;
        public static final int ACTION_DEFAULT_SMS_APP_CHANGED = 266;
        public static final int ACTION_DELETE_EMERGENCY_CONTACT = 282;
        public static final int ACTION_DELETION_APPS_COLLAPSED = 464;
        public static final int ACTION_DELETION_DOWNLOADS_COLLAPSED = 466;
        public static final int ACTION_DELETION_HELPER_APPS_DELETION_FAIL = 471;
        public static final int ACTION_DELETION_HELPER_CANCEL = 468;
        public static final int ACTION_DELETION_HELPER_CLEAR = 467;
        public static final int ACTION_DELETION_HELPER_DOWNLOADS_DELETION_FAIL = 472;
        public static final int ACTION_DELETION_HELPER_PHOTOS_VIDEOS_DELETION_FAIL = 473;
        public static final int ACTION_DELETION_HELPER_REMOVE_CANCEL = 470;
        public static final int ACTION_DELETION_HELPER_REMOVE_CONFIRM = 469;
        public static final int ACTION_DELETION_SELECTION_ALL_APPS = 461;
        public static final int ACTION_DELETION_SELECTION_APP_OFF = 463;
        public static final int ACTION_DELETION_SELECTION_APP_ON = 462;
        public static final int ACTION_DELETION_SELECTION_DOWNLOADS = 465;
        public static final int ACTION_DELETION_SELECTION_PHOTOS = 460;
        public static final int ACTION_DISMISS_ALL_NOTES = 148;
        public static final int ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE = 255;
        public static final int ACTION_EDIT_EMERGENCY_INFO = 279;
        public static final int ACTION_EDIT_EMERGENCY_INFO_FIELD = 280;
        public static final int ACTION_EMERGENCY_CALL = 200;
        public static final int ACTION_FINGERPRINT_AUTH = 252;
        public static final int ACTION_FINGERPRINT_DELETE = 253;
        public static final int ACTION_FINGERPRINT_ENROLL = 251;
        public static final int ACTION_FINGERPRINT_RENAME = 254;
        public static final int ACTION_GENERIC_PACKAGE = 350;
        public static final int ACTION_HIDE_APP_DISAMBIG_APP_FEATURED = 452;
        public static final int ACTION_HIDE_APP_DISAMBIG_NONE_FEATURED = 454;
        public static final int ACTION_HIDE_SETTINGS_SUGGESTION = 385;
        public static final int ACTION_LS_CAMERA = 189;
        public static final int ACTION_LS_DIALER = 190;
        public static final int ACTION_LS_HINT = 188;
        public static final int ACTION_LS_LOCK = 191;
        public static final int ACTION_LS_NOTE = 192;
        public static final int ACTION_LS_QS = 193;
        public static final int ACTION_LS_SHADE = 187;
        public static final int ACTION_LS_UNLOCK = 186;
        public static final int ACTION_MODIFY_IMPORTANCE_SLIDER = 290;
        public static final int ACTION_NOTE_CONTROLS = 204;
        public static final int ACTION_NOTE_INFO = 205;
        public static final int ACTION_NOTIFICATION_EXPANDER = 407;
        public static final int ACTION_NOTIFICATION_GESTURE_EXPANDER = 409;
        public static final int ACTION_NOTIFICATION_GROUP_EXPANDER = 408;
        public static final int ACTION_NOTIFICATION_GROUP_GESTURE_EXPANDER = 410;
        public static final int ACTION_OVERVIEW_PAGE = 276;
        public static final int ACTION_OVERVIEW_SELECT = 277;
        public static final int ACTION_QS_COLLAPSED_SETTINGS_LAUNCH = 490;
        public static final int ACTION_QS_EDIT_ADD = 363;
        public static final int ACTION_QS_EDIT_ADD_SPEC = 362;
        public static final int ACTION_QS_EDIT_MOVE = 365;
        public static final int ACTION_QS_EDIT_MOVE_SPEC = 364;
        public static final int ACTION_QS_EDIT_REMOVE = 361;
        public static final int ACTION_QS_EDIT_REMOVE_SPEC = 360;
        public static final int ACTION_QS_EDIT_RESET = 359;
        public static final int ACTION_QS_EXPANDED_SETTINGS_LAUNCH = 406;
        public static final int ACTION_QS_LONG_PRESS = 366;
        public static final int ACTION_REMOTE_INPUT_CLOSE = 400;
        public static final int ACTION_REMOTE_INPUT_FAIL = 399;
        public static final int ACTION_REMOTE_INPUT_OPEN = 397;
        public static final int ACTION_REMOTE_INPUT_SEND = 398;
        public static final int ACTION_REVEAL_GEAR = 332;
        public static final int ACTION_RINGER_MODE = 213;
        public static final int ACTION_ROTATION_LOCK = 203;
        public static final int ACTION_SAVE_IMPORTANCE = 291;
        public static final int ACTION_SCOPED_DIRECTORY_ACCESS_ALREADY_DENIED_BY_FOLDER = 353;
        public static final int ACTION_SCOPED_DIRECTORY_ACCESS_ALREADY_DENIED_BY_PACKAGE = 354;
        public static final int ACTION_SCOPED_DIRECTORY_ACCESS_ALREADY_GRANTED_BY_FOLDER = 330;
        public static final int ACTION_SCOPED_DIRECTORY_ACCESS_ALREADY_GRANTED_BY_PACKAGE = 331;
        public static final int ACTION_SCOPED_DIRECTORY_ACCESS_DENIED_AND_PERSIST_BY_FOLDER = 355;
        public static final int ACTION_SCOPED_DIRECTORY_ACCESS_DENIED_AND_PERSIST_BY_PACKAGE = 356;
        public static final int ACTION_SCOPED_DIRECTORY_ACCESS_DENIED_BY_FOLDER = 327;
        public static final int ACTION_SCOPED_DIRECTORY_ACCESS_DENIED_BY_PACKAGE = 329;
        public static final int ACTION_SCOPED_DIRECTORY_ACCESS_GRANTED_BY_FOLDER = 326;
        public static final int ACTION_SCOPED_DIRECTORY_ACCESS_GRANTED_BY_PACKAGE = 328;
        public static final int ACTION_SEARCH_RESULTS = 226;
        public static final int ACTION_SELECT_SUMMARY = 476;
        public static final int ACTION_SELECT_SUPPORT_FRAGMENT = 477;
        public static final int ACTION_SETTINGS_CONDITION_BUTTON = 376;
        public static final int ACTION_SETTINGS_CONDITION_CLICK = 375;
        public static final int ACTION_SETTINGS_CONDITION_COLLAPSE = 374;
        public static final int ACTION_SETTINGS_CONDITION_DISMISS = 372;
        public static final int ACTION_SETTINGS_CONDITION_EXPAND = 373;
        public static final int ACTION_SETTINGS_DISMISS_SUGGESTION = 387;
        public static final int ACTION_SETTINGS_SUGGESTION = 386;
        public static final int ACTION_SETTING_HELP_AND_FEEDBACK = 496;
        public static final int ACTION_SHADE_QS_PULL = 194;
        public static final int ACTION_SHADE_QS_TAP = 195;
        public static final int ACTION_SHOW_APP_DISAMBIG_APP_FEATURED = 451;
        public static final int ACTION_SHOW_APP_DISAMBIG_NONE_FEATURED = 453;
        public static final int ACTION_SHOW_SETTINGS_SUGGESTION = 384;
        public static final int ACTION_SUPPORT_CHAT = 482;
        public static final int ACTION_SUPPORT_DAIL_TOLLFREE = 485;
        public static final int ACTION_SUPPORT_DIAL_TOLLED = 487;
        public static final int ACTION_SUPPORT_DISCLAIMER_CANCEL = 483;
        public static final int ACTION_SUPPORT_DISCLAIMER_OK = 484;
        public static final int ACTION_SUPPORT_HELP_AND_FEEDBACK = 479;
        public static final int ACTION_SUPPORT_PHONE = 481;
        public static final int ACTION_SUPPORT_SIGN_IN = 480;
        public static final int ACTION_SUPPORT_TIPS_AND_TRICKS = 478;
        public static final int ACTION_SUPPORT_VIEW_TRAVEL_ABROAD_DIALOG = 486;
        public static final int ACTION_SYSTEM_NAVIGATION_KEY_DOWN = 494;
        public static final int ACTION_SYSTEM_NAVIGATION_KEY_UP = 493;
        public static final int ACTION_TOGGLE_STORAGE_MANAGER = 489;
        public static final int ACTION_TOUCH_GEAR = 333;
        public static final int ACTION_TUNER_CALIBRATE_DISPLAY_CHANGED = 307;
        public static final int ACTION_TUNER_DO_NOT_DISTURB_VOLUME_PANEL = 314;
        public static final int ACTION_TUNER_DO_NOT_DISTURB_VOLUME_SHORTCUT = 315;
        public static final int ACTION_TUNER_NIGHT_MODE = 309;
        public static final int ACTION_TUNER_NIGHT_MODE_ADJUST_BRIGHTNESS = 313;
        public static final int ACTION_TUNER_NIGHT_MODE_ADJUST_DARK_THEME = 311;
        public static final int ACTION_TUNER_NIGHT_MODE_ADJUST_TINT = 312;
        public static final int ACTION_TUNER_NIGHT_MODE_AUTO = 310;
        public static final int ACTION_TUNER_POWER_NOTIFICATION_CONTROLS = 393;
        public static final int ACTION_VIEW_EMERGENCY_INFO = 278;
        public static final int ACTION_VOLUME_ICON = 212;
        public static final int ACTION_VOLUME_KEY = 211;
        public static final int ACTION_VOLUME_SLIDER = 209;
        public static final int ACTION_VOLUME_STREAM = 210;
        public static final int ACTION_WIFI_ADD_NETWORK = 134;
        public static final int ACTION_WIFI_CONNECT = 135;
        public static final int ACTION_WIFI_FORCE_SCAN = 136;
        public static final int ACTION_WIFI_FORGET = 137;
        public static final int ACTION_WIFI_OFF = 138;
        public static final int ACTION_WIFI_ON = 139;
        public static final int ACTION_WIGGLE_CAMERA_GESTURE = 256;
        public static final int ACTION_WINDOW_DOCK_DRAG_DROP = 270;
        public static final int ACTION_WINDOW_DOCK_LONGPRESS = 271;
        public static final int ACTION_WINDOW_DOCK_RESIZE = 389;
        public static final int ACTION_WINDOW_DOCK_SWIPE = 272;
        public static final int ACTION_WINDOW_DOCK_UNRESIZABLE = 391;
        public static final int ACTION_WINDOW_UNDOCK_LONGPRESS = 286;
        public static final int ACTION_WINDOW_UNDOCK_MAX = 390;
        public static final int ACTION_ZEN_ADD_RULE = 172;
        public static final int ACTION_ZEN_ADD_RULE_OK = 173;
        public static final int ACTION_ZEN_ALLOW_CALLS = 170;
        public static final int ACTION_ZEN_ALLOW_EVENTS = 168;
        public static final int ACTION_ZEN_ALLOW_LIGHTS = 264;
        public static final int ACTION_ZEN_ALLOW_MESSAGES = 169;
        public static final int ACTION_ZEN_ALLOW_REMINDERS = 167;
        public static final int ACTION_ZEN_ALLOW_REPEAT_CALLS = 171;
        public static final int ACTION_ZEN_ALLOW_WHEN_SCREEN_OFF = 263;
        public static final int ACTION_ZEN_ALLOW_WHEN_SCREEN_ON = 269;
        public static final int ACTION_ZEN_DELETE_RULE = 174;
        public static final int ACTION_ZEN_DELETE_RULE_OK = 175;
        public static final int ACTION_ZEN_ENABLE_RULE = 176;
        public static final int APN = 12;
        public static final int APN_EDITOR = 13;
        public static final int APPLICATION = 16;
        public static final int APPLICATIONS_ADVANCED = 130;
        public static final int APPLICATIONS_APP_LAUNCH = 17;
        public static final int APPLICATIONS_APP_PERMISSION = 18;
        public static final int APPLICATIONS_APP_STORAGE = 19;
        public static final int APPLICATIONS_DEFAULT_APPS = 181;
        public static final int APPLICATIONS_HIGH_POWER_APPS = 184;
        public static final int APPLICATIONS_INSTALLED_APP_DETAILS = 20;
        public static final int APPLICATIONS_MANAGE_ASSIST = 201;
        public static final int APPLICATIONS_PROCESS_STATS_DETAIL = 21;
        public static final int APPLICATIONS_PROCESS_STATS_MEM_DETAIL = 22;
        public static final int APPLICATIONS_PROCESS_STATS_UI = 23;
        public static final int APPLICATIONS_STORAGE_APPS = 182;
        public static final int APPLICATIONS_USAGE_ACCESS_DETAIL = 183;
        public static final int APP_DATA_USAGE = 343;
        public static final int APP_OPS_DETAILS = 14;
        public static final int APP_OPS_SUMMARY = 15;
        public static final int APP_TRANSITION_COMPONENT_NAME = 323;
        public static final int APP_TRANSITION_DELAY_MS = 319;
        public static final int APP_TRANSITION_DEVICE_UPTIME_SECONDS = 325;
        public static final int APP_TRANSITION_PROCESS_RUNNING = 324;
        public static final int APP_TRANSITION_REASON = 320;
        public static final int APP_TRANSITION_STARTING_WINDOW_DELAY_MS = 321;
        public static final int APP_TRANSITION_WINDOWS_DRAWN_DELAY_MS = 322;
        public static final int BACKGROUND_CHECK_SUMMARY = 258;
        public static final int BILLING_CYCLE = 342;
        public static final int BLUETOOTH = 24;
        public static final int BLUETOOTH_DEVICE_PICKER = 25;
        public static final int BLUETOOTH_DEVICE_PROFILES = 26;
        public static final int BOUNCER = 197;
        public static final int BRIGHTNESS_DIALOG = 220;
        public static final int CHOOSE_LOCK_GENERIC = 27;
        public static final int CHOOSE_LOCK_PASSWORD = 28;
        public static final int CHOOSE_LOCK_PATTERN = 29;
        public static final int CONFIGURE_NOTIFICATION = 337;
        public static final int CONFIGURE_WIFI = 338;
        public static final int CONFIRM_LOCK_PASSWORD = 30;
        public static final int CONFIRM_LOCK_PATTERN = 31;
        public static final int CONVERT_FBE = 402;
        public static final int CONVERT_FBE_CONFIRM = 403;
        public static final int CRYPT_KEEPER = 32;
        public static final int CRYPT_KEEPER_CONFIRM = 33;
        public static final int DASHBOARD_CONTAINER = 474;
        public static final int DASHBOARD_SEARCH_RESULTS = 34;
        public static final int DASHBOARD_SUMMARY = 35;
        public static final int DATA_SAVER_SUMMARY = 348;
        public static final int DATA_USAGE = 36;
        public static final int DATA_USAGE_LIST = 341;
        public static final int DATA_USAGE_SUMMARY = 37;
        public static final int DATA_USAGE_UNRESTRICTED_ACCESS = 349;
        public static final int DATE_TIME = 38;
        public static final int DEVELOPMENT = 39;
        public static final int DEVICEINFO = 40;
        public static final int DEVICEINFO_IMEI_INFORMATION = 41;
        public static final int DEVICEINFO_SIM_STATUS = 43;
        public static final int DEVICEINFO_STATUS = 44;
        public static final int DEVICEINFO_STORAGE = 42;
        public static final int DEVICEINFO_USB = 45;
        public static final int DISPLAY = 46;
        public static final int DISPLAY_SCREEN_ZOOM = 339;
        public static final int DOZING = 223;
        public static final int DREAM = 47;
        public static final int DREAMING = 222;
        public static final int ENABLE_VIRTUAL_KEYBOARDS = 347;
        public static final int ENCRYPTION = 48;
        public static final int FINGERPRINT = 49;
        public static final int FINGERPRINT_ENROLL = 50;
        public static final int FINGERPRINT_ENROLLING = 240;
        public static final int FINGERPRINT_ENROLLING_SETUP = 246;
        public static final int FINGERPRINT_ENROLL_FINISH = 242;
        public static final int FINGERPRINT_ENROLL_FINISH_SETUP = 248;
        public static final int FINGERPRINT_ENROLL_INTRO = 243;
        public static final int FINGERPRINT_ENROLL_INTRO_SETUP = 249;
        public static final int FINGERPRINT_ENROLL_ONBOARD = 244;
        public static final int FINGERPRINT_ENROLL_ONBOARD_SETUP = 250;
        public static final int FINGERPRINT_ENROLL_SIDECAR = 245;
        public static final int FINGERPRINT_FIND_SENSOR = 241;
        public static final int FINGERPRINT_FIND_SENSOR_SETUP = 247;
        public static final int FUELGAUGE_BATTERY_HISTORY_DETAIL = 51;
        public static final int FUELGAUGE_BATTERY_SAVER = 52;
        public static final int FUELGAUGE_HIGH_POWER_DETAILS = 185;
        public static final int FUELGAUGE_INACTIVE_APPS = 238;
        public static final int FUELGAUGE_POWER_USAGE_DETAIL = 53;
        public static final int FUELGAUGE_POWER_USAGE_SUMMARY = 54;
        public static final int HOME = 55;
        public static final int ICC_LOCK = 56;
        public static final int INPUTMETHOD_KEYBOARD = 58;
        public static final int INPUTMETHOD_LANGUAGE = 57;
        public static final int INPUTMETHOD_SPELL_CHECKERS = 59;
        public static final int INPUTMETHOD_SUBTYPE_ENABLER = 60;
        public static final int INPUTMETHOD_USER_DICTIONARY = 61;
        public static final int INPUTMETHOD_USER_DICTIONARY_ADD_WORD = 62;
        public static final int LOCATION = 63;
        public static final int LOCATION_MODE = 64;
        public static final int LOCATION_SCANNING = 131;
        public static final int LOCKSCREEN = 196;
        public static final int MAIN_SETTINGS = 1;
        public static final int MANAGE_APPLICATIONS = 65;
        public static final int MANAGE_APPLICATIONS_ALL = 132;
        public static final int MANAGE_APPLICATIONS_NOTIFICATIONS = 133;
        public static final int MANAGE_DOMAIN_URLS = 143;
        public static final int MANAGE_PERMISSIONS = 140;
        public static final int MASTER_CLEAR = 66;
        public static final int MASTER_CLEAR_CONFIRM = 67;
        public static final int NET_DATA_USAGE_METERED = 68;
        public static final int NFC_BEAM = 69;
        public static final int NFC_PAYMENT = 70;
        public static final int NIGHT_DISPLAY_SETTINGS = 488;
        public static final int NOTIFICATION = 71;
        public static final int NOTIFICATION_ACCESS = 179;
        public static final int NOTIFICATION_ALERT = 199;
        public static final int NOTIFICATION_APP_NOTIFICATION = 72;
        public static final int NOTIFICATION_ITEM = 128;
        public static final int NOTIFICATION_ITEM_ACTION = 129;
        public static final int NOTIFICATION_OTHER_SOUND = 73;
        public static final int NOTIFICATION_PANEL = 127;
        public static final int NOTIFICATION_REDACTION = 74;
        public static final int NOTIFICATION_STATION = 75;
        public static final int NOTIFICATION_TOPIC_NOTIFICATION = 265;
        public static final int NOTIFICATION_ZEN_MODE = 76;
        public static final int NOTIFICATION_ZEN_MODE_ACCESS = 180;
        public static final int NOTIFICATION_ZEN_MODE_AUTOMATION = 142;
        public static final int NOTIFICATION_ZEN_MODE_EVENT_RULE = 146;
        public static final int NOTIFICATION_ZEN_MODE_EXTERNAL_RULE = 145;
        public static final int NOTIFICATION_ZEN_MODE_PRIORITY = 141;
        public static final int NOTIFICATION_ZEN_MODE_SCHEDULE_RULE = 144;
        public static final int NOTIFICATION_ZEN_MODE_VISUAL_INTERRUPTIONS = 262;
        public static final int OVERVIEW_ACTIVITY = 224;
        public static final int OVERVIEW_DISMISS = 289;
        public static final int OVERVIEW_DISMISS_ALL = 357;
        public static final int OVERVIEW_HISTORY = 275;
        public static final int OVERVIEW_LAUNCH_PREVIOUS_TASK = 318;
        public static final int OVERVIEW_SCROLL = 287;
        public static final int OVERVIEW_SELECT_TIMEOUT = 288;
        public static final int OWNER_INFO = 77;
        public static final int PHYSICAL_KEYBOARDS = 346;
        public static final int PREMIUM_SMS_ACCESS = 388;
        public static final int PRINT_JOB_SETTINGS = 78;
        public static final int PRINT_SERVICE_SETTINGS = 79;
        public static final int PRINT_SETTINGS = 80;
        public static final int PRIVACY = 81;
        public static final int PROCESS_STATS_SUMMARY = 202;
        public static final int PROFILE_CHALLENGE = 273;
        public static final int PROXY_SELECTOR = 82;
        public static final int QS_AIRPLANEMODE = 112;
        public static final int QS_BATTERY_DETAIL = 274;
        public static final int QS_BATTERY_TILE = 261;
        public static final int QS_BLUETOOTH = 113;
        public static final int QS_BLUETOOTH_DETAILS = 150;
        public static final int QS_BLUETOOTH_TOGGLE = 154;
        public static final int QS_CAST = 114;
        public static final int QS_CAST_DETAILS = 151;
        public static final int QS_CAST_DISCONNECT = 158;
        public static final int QS_CAST_SELECT = 157;
        public static final int QS_CELLULAR = 115;
        public static final int QS_CELLULAR_TOGGLE = 155;
        public static final int QS_COLORINVERSION = 116;
        public static final int QS_COLOR_MATRIX = 267;
        public static final int QS_CUSTOM = 268;
        public static final int QS_DATAUSAGEDETAIL = 117;
        public static final int QS_DATA_SAVER = 284;
        public static final int QS_DND = 118;
        public static final int QS_DND_CONDITION_SELECT = 164;
        public static final int QS_DND_DETAILS = 149;
        public static final int QS_DND_TIME = 163;
        public static final int QS_DND_TOGGLE = 166;
        public static final int QS_DND_ZEN_SELECT = 165;
        public static final int QS_EDIT = 358;
        public static final int QS_FLASHLIGHT = 119;
        public static final int QS_HOTSPOT = 120;
        public static final int QS_INTENT = 121;
        public static final int QS_LOCATION = 122;
        public static final int QS_LOCK_TILE = 259;
        public static final int QS_NIGHT_DISPLAY = 491;
        public static final int QS_PANEL = 111;
        public static final int QS_ROTATIONLOCK = 123;
        public static final int QS_SWITCH_USER = 156;
        public static final int QS_USERDETAIL = 125;
        public static final int QS_USERDETAILITE = 124;
        public static final int QS_USER_TILE = 260;
        public static final int QS_WIFI = 126;
        public static final int QS_WIFI_DETAILS = 152;
        public static final int QS_WIFI_TOGGLE = 153;
        public static final int QS_WORKMODE = 257;
        public static final int RESET_NETWORK = 83;
        public static final int RESET_NETWORK_CONFIRM = 84;
        public static final int RUNNING_SERVICES = 404;
        public static final int RUNNING_SERVICE_DETAILS = 85;
        public static final int SCREEN = 198;
        public static final int SCREEN_PINNING = 86;
        public static final int SECURITY = 87;
        public static final int SETTINGS_CONDITION_AIRPLANE_MODE = 377;
        public static final int SETTINGS_CONDITION_BACKGROUND_DATA = 378;
        public static final int SETTINGS_CONDITION_BATTERY_SAVER = 379;
        public static final int SETTINGS_CONDITION_CELLULAR_DATA = 380;
        public static final int SETTINGS_CONDITION_DND = 381;
        public static final int SETTINGS_CONDITION_HOTSPOT = 382;
        public static final int SETTINGS_CONDITION_NIGHT_DISPLAY = 492;
        public static final int SETTINGS_CONDITION_WORK_MODE = 383;
        public static final int SETTINGS_GESTURES = 459;
        public static final int SIM = 88;
        public static final int SOUND = 336;
        public static final int SPECIAL_ACCESS = 351;
        public static final int STORAGE_MANAGER_SETTINGS = 458;
        public static final int SUPPORT_FRAGMENT = 475;
        public static final int SUW_ACCESSIBILITY = 367;
        public static final int SUW_ACCESSIBILITY_DISPLAY_SIZE = 370;
        public static final int SUW_ACCESSIBILITY_FONT_SIZE = 369;
        public static final int SUW_ACCESSIBILITY_TOGGLE_SCREEN_MAGNIFICATION = 368;
        public static final int SUW_ACCESSIBILITY_TOGGLE_SCREEN_READER = 371;
        public static final int SYSTEM_ALERT_WINDOW_APPS = 221;
        public static final int TESTING = 89;
        public static final int TETHER = 90;
        public static final int TRUSTED_CREDENTIALS = 92;
        public static final int TRUST_AGENT = 91;
        public static final int TTS_ENGINE_SETTINGS = 93;
        public static final int TTS_TEXT_TO_SPEECH = 94;
        public static final int TUNER = 227;
        public static final int TUNER_BATTERY_PERCENTAGE = 237;
        public static final int TUNER_CALIBRATE_DISPLAY = 305;
        public static final int TUNER_COLOR_AND_APPEARANCE = 306;
        public static final int TUNER_DEMO_MODE = 229;
        public static final int TUNER_DEMO_MODE_ENABLED = 235;
        public static final int TUNER_DEMO_MODE_ON = 236;
        public static final int TUNER_NIGHT_MODE = 308;
        public static final int TUNER_POWER_NOTIFICATION_CONTROLS = 392;
        public static final int TUNER_QS = 228;
        public static final int TUNER_QS_ADD = 231;
        public static final int TUNER_QS_REMOVE = 232;
        public static final int TUNER_QS_REORDER = 230;
        public static final int TUNER_STATUS_BAR_DISABLE = 234;
        public static final int TUNER_STATUS_BAR_ENABLE = 233;
        public static final int USAGE_ACCESS = 95;
        public static final int USER = 96;
        public static final int USERS_APP_RESTRICTIONS = 97;
        public static final int USER_CREDENTIALS = 285;
        public static final int USER_DETAILS = 98;
        public static final int USER_LOCALE_LIST = 344;
        public static final int VIEW_UNKNOWN = 0;
        public static final int VIRTUAL_KEYBOARDS = 345;
        public static final int VOICE_INPUT = 99;
        public static final int VOLUME_DIALOG = 207;
        public static final int VOLUME_DIALOG_DETAILS = 208;
        public static final int VPN = 100;
        public static final int VR_MANAGE_LISTENERS = 334;
        public static final int WALLPAPER_TYPE = 101;
        public static final int WEBVIEW_IMPLEMENTATION = 405;
        public static final int WFD_WIFI_DISPLAY = 102;
        public static final int WIFI = 103;
        public static final int WIFI_ADVANCED = 104;
        public static final int WIFI_APITEST = 107;
        public static final int WIFI_CALLING = 105;
        public static final int WIFI_INFO = 108;
        public static final int WIFI_P2P = 109;
        public static final int WIFI_SAVED_ACCESS_POINTS = 106;
        public static final int WINDOW_DOCK_SHORTCUTS = 352;
        public static final int WIRELESS = 110;
        private static volatile MetricsEvent[] _emptyArray;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.logging.MetricsProto.MetricsEvent.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public MetricsEvent() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.logging.MetricsProto.MetricsEvent.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.logging.MetricsProto.MetricsEvent.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.logging.MetricsProto.MetricsEvent.parseFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano):com.android.internal.logging.MetricsProto$MetricsEvent, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public static com.android.internal.logging.MetricsProto.MetricsEvent parseFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano r1) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.logging.MetricsProto.MetricsEvent.parseFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano):com.android.internal.logging.MetricsProto$MetricsEvent, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.logging.MetricsProto.MetricsEvent.parseFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano):com.android.internal.logging.MetricsProto$MetricsEvent");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.logging.MetricsProto.MetricsEvent.mergeFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano):com.android.framework.protobuf.nano.MessageNano, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ com.android.framework.protobuf.nano.MessageNano mergeFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano r1) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.logging.MetricsProto.MetricsEvent.mergeFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano):com.android.framework.protobuf.nano.MessageNano, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.logging.MetricsProto.MetricsEvent.mergeFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano):com.android.framework.protobuf.nano.MessageNano");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.logging.MetricsProto.MetricsEvent.mergeFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano):com.android.internal.logging.MetricsProto$MetricsEvent, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public com.android.internal.logging.MetricsProto.MetricsEvent mergeFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano r1) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.logging.MetricsProto.MetricsEvent.mergeFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano):com.android.internal.logging.MetricsProto$MetricsEvent, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.logging.MetricsProto.MetricsEvent.mergeFrom(com.android.framework.protobuf.nano.CodedInputByteBufferNano):com.android.internal.logging.MetricsProto$MetricsEvent");
        }

        public static MetricsEvent[] emptyArray() {
            if (_emptyArray == null) {
                synchronized (InternalNano.LAZY_INIT_LOCK) {
                    if (_emptyArray == null) {
                        _emptyArray = new MetricsEvent[0];
                    }
                }
            }
            return _emptyArray;
        }

        public MetricsEvent clear() {
            this.cachedSize = -1;
            return this;
        }

        public static MetricsEvent parseFrom(byte[] data) throws InvalidProtocolBufferNanoException {
            return (MetricsEvent) MessageNano.mergeFrom(new MetricsEvent(), data);
        }
    }
}
