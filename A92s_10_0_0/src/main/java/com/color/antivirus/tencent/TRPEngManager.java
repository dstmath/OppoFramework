package com.color.antivirus.tencent;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.CallLog;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Singleton;
import com.android.internal.telephony.PhoneConstants;
import com.color.antivirus.AntivirusLog;
import com.color.antivirus.tencent.ITRPEng;
import com.oppo.luckymoney.LMManager;

public class TRPEngManager {
    public static final int ACTION_ACCESSIBILITY_onBind = 114;
    public static final int ACTION_AMS_BI_BATTERY_CHANGED = 1104;
    public static final int ACTION_AMS_BI_INSTALL_SHORTCUT = 1106;
    public static final int ACTION_AMS_BI_PACKAGE_ADDED = 1105;
    public static final int ACTION_AMS_BI_PACKAGE_CHANGED = 1100;
    public static final int ACTION_AMS_BI_PACKAGE_REMOVED = 1101;
    public static final int ACTION_AMS_BI_TIME_SET = 1103;
    public static final int ACTION_AMS_BI_TIME_TICK = 1102;
    public static final int ACTION_AMS_BI_other = 1107;
    public static final int ACTION_AMS_GETP_CALLLOG = 110;
    public static final int ACTION_AMS_GETP_CONTACTS = 109;
    public static final int ACTION_AMS_GETP_MMS = 112;
    public static final int ACTION_AMS_GETP_MMSSMS = 113;
    public static final int ACTION_AMS_GETP_SMS = 111;
    public static final int ACTION_AMS_bindService = 11;
    public static final int ACTION_AMS_broadcastIntent = 81;
    public static final int ACTION_AMS_closeSystemDialogs = 7;
    public static final int ACTION_AMS_finishActivity = 6;
    public static final int ACTION_AMS_getAppTasks = 78;
    public static final int ACTION_AMS_getProcessMemoryInfo = 76;
    public static final int ACTION_AMS_getProcessesInErrorState = 75;
    public static final int ACTION_AMS_killBackgroundProcesses = 77;
    public static final int ACTION_AMS_moveTaskToFront = 3;
    public static final int ACTION_AMS_navigateUpTo = 1;
    public static final int ACTION_AMS_setRequestedOrientation = 5;
    public static final int ACTION_AMS_startActivities = 4;
    public static final int ACTION_AMS_startActivity = 2;
    public static final int ACTION_AMS_unbindService = 10;
    public static final int ACTION_AMS_unregisterReceiver = 9;
    public static final int ACTION_AMS_updateConfiguration = 8;
    public static final int ACTION_ASt_SAMW_ADD_DEVICE_ADMIN = 1004;
    public static final int ACTION_ASt_SAMW_ATTACH_DATA = 1005;
    public static final int ACTION_ASt_SAMW_AlipayGphone = 1036;
    public static final int ACTION_ASt_SAMW_CALL = 1006;
    public static final int ACTION_ASt_SAMW_DELETE = 1007;
    public static final int ACTION_ASt_SAMW_DIAL = 1013;
    public static final int ACTION_ASt_SAMW_EDIT = 1003;
    public static final int ACTION_ASt_SAMW_GET_CONTENT = 1012;
    public static final int ACTION_ASt_SAMW_INSERT = 1015;
    public static final int ACTION_ASt_SAMW_MAIN = 1014;
    public static final int ACTION_ASt_SAMW_PICK = 1009;
    public static final int ACTION_ASt_SAMW_PICK_ACTIVITY = 1010;
    public static final int ACTION_ASt_SAMW_SEARCH = 1008;
    public static final int ACTION_ASt_SAMW_SEND = 1001;
    public static final int ACTION_ASt_SAMW_SENDTO = 1011;
    public static final int ACTION_ASt_SAMW_VIEW_browser = 1022;
    public static final int ACTION_ASt_SAMW_VIEW_calendar = 1023;
    public static final int ACTION_ASt_SAMW_VIEW_calendar2 = 1029;
    public static final int ACTION_ASt_SAMW_VIEW_chrome = 1030;
    public static final int ACTION_ASt_SAMW_VIEW_contacts = 1024;
    public static final int ACTION_ASt_SAMW_VIEW_dialer = 1019;
    public static final int ACTION_ASt_SAMW_VIEW_gallery = 1025;
    public static final int ACTION_ASt_SAMW_VIEW_gallery3d = 1020;
    public static final int ACTION_ASt_SAMW_VIEW_htmlviewer = 1016;
    public static final int ACTION_ASt_SAMW_VIEW_market = 1026;
    public static final int ACTION_ASt_SAMW_VIEW_mms = 1017;
    public static final int ACTION_ASt_SAMW_VIEW_mobileqq_pay = 1032;
    public static final int ACTION_ASt_SAMW_VIEW_music = 1027;
    public static final int ACTION_ASt_SAMW_VIEW_other = 1033;
    public static final int ACTION_ASt_SAMW_VIEW_packageinstaller = 1028;
    public static final int ACTION_ASt_SAMW_VIEW_phone = 1018;
    public static final int ACTION_ASt_SAMW_VIEW_settings = 1031;
    public static final int ACTION_ASt_SAMW_VIEW_vending = 1021;
    public static final int ACTION_ASt_SAMW_WEB_SEARCH = 1002;
    public static final int ACTION_ASt_SAMW_mm_WXPayEntryActivity = 1035;
    public static final int ACTION_ASt_SAMW_mm_WalletPayUI = 1034;
    public static final int ACTION_ASt_SAMW_other = 1037;
    public static final int ACTION_ASt_startActivityMayWait = 80;
    public static final int ACTION_AStk_minimalResumeActivityLocked = 601;
    public static final int ACTION_AStk_resumeTopActivityInnerLocked = 600;
    public static final int ACTION_AUDIORECORD_startRecording = 150;
    public static final int ACTION_AUDIORECORD_stop = 151;
    public static final int ACTION_AdM_setStreamVolume = 84;
    public static final int ACTION_AdS_requestAudioFocus = 14;
    public static final int ACTION_AdS_setMode = 13;
    public static final int ACTION_BLUETOOTH_cancelDiscovery = 118;
    public static final int ACTION_BLUETOOTH_disable = 116;
    public static final int ACTION_BLUETOOTH_enable = 115;
    public static final int ACTION_BLUETOOTH_startDiscovery = 117;
    public static final int ACTION_BSS_enforceCallingPermission = 15;
    public static final int ACTION_CALLLOGP_delete = 103;
    public static final int ACTION_CALLLOGP_insert = 102;
    public static final int ACTION_CALLLOGP_query = 101;
    public static final int ACTION_CALLLOGP_update = 104;
    public static final int ACTION_CAMERAMANAGER_openCamera = 154;
    public static final int ACTION_CAMERA_open = 153;
    public static final int ACTION_CONTACTSP_delete = 107;
    public static final int ACTION_CONTACTSP_insert = 106;
    public static final int ACTION_CONTACTSP_query = 105;
    public static final int ACTION_CONTACTSP_update = 108;
    public static final int ACTION_CS_prepareVpn = 55;
    public static final int ACTION_ClpS_setPrimaryClip = 85;
    public static final int ACTION_ConS_getActiveNetworkInfo = 19;
    public static final int ACTION_ConS_getAllNetworkInfo = 18;
    public static final int ACTION_ConS_getLinkProperties = 16;
    public static final int ACTION_ConS_getNetworkInfo = 20;
    public static final int ACTION_ConS_isActiveNetworkMetered = 17;
    public static final int ACTION_DPMS_resetPassword = 83;
    public static final int ACTION_DPM_createAndManageUser = 22;
    public static final int ACTION_DPM_removeActiveAdmin = 21;
    public static final int ACTION_FINGER_authenticate = 148;
    public static final int ACTION_LMS_getBestProvider = 25;
    public static final int ACTION_LMS_getProviders = 24;
    public static final int ACTION_LMS_isProviderEnabled = 74;
    public static final int ACTION_LMS_registerGnssStatusCallback = 23;
    public static final int ACTION_LMS_removeUpdates = 27;
    public static final int ACTION_LMS_requestLocationUpdates = 26;
    public static final int ACTION_LMS_sendExtraCommand = 28;
    public static final int ACTION_LSS_getLong = 32;
    public static final int ACTION_LSS_getString = 31;
    public static final int ACTION_LSS_setLong = 29;
    public static final int ACTION_LSS_setString = 30;
    public static final int ACTION_MEDIARECORD_prepare = 152;
    public static final int ACTION_MMSP_delete = 95;
    public static final int ACTION_MMSP_insert = 94;
    public static final int ACTION_MMSP_query = 93;
    public static final int ACTION_MMSP_update = 96;
    public static final int ACTION_MMSSMSP_delete = 99;
    public static final int ACTION_MMSSMSP_insert = 98;
    public static final int ACTION_MMSSMSP_query = 97;
    public static final int ACTION_MMSSMSP_update = 100;
    public static final int ACTION_NFC_setForegroundDispatch = 149;
    public static final int ACTION_NMS_addRoute = 34;
    public static final int ACTION_NMS_registerObserver = 33;
    public static final int ACTION_NPMS_registerListener = 35;
    public static final int ACTION_NtfMS_enqueueNotificationWithTag = 79;
    public static final int ACTION_OpS_setMode = 12;
    public static final int ACTION_PIM_getDeviceId = 53;
    public static final int ACTION_PIM_getImei = 52;
    public static final int ACTION_PIM_getLine1Number = 51;
    public static final int ACTION_PIS_uninstall = 36;
    public static final int ACTION_PMS_addPreferredActivity = 39;
    public static final int ACTION_PMS_clearPackagePreferredActivities = 40;
    public static final int ACTION_PMS_setApplicationEnabledSetting = 37;
    public static final int ACTION_PMS_setComponentEnabledSetting = 38;
    public static final int ACTION_PMS_setComponentEnabledSetting_default = 1301;
    public static final int ACTION_PMS_setComponentEnabledSetting_disable = 1303;
    public static final int ACTION_PMS_setComponentEnabledSetting_disable_user = 1304;
    public static final int ACTION_PMS_setComponentEnabledSetting_enable = 1302;
    public static final int ACTION_PSIC_getGroupIdLevel1ForSubscriber = 42;
    public static final int ACTION_PSIC_getLine1NumberForSubscriber = 45;
    public static final int ACTION_PSIC_getSubscriberIdForSubscriber = 41;
    public static final int ACTION_PSIC_getVoiceMailAlphaTagForSubscriber = 44;
    public static final int ACTION_PSIC_getVoiceMailNumberForSubscriber = 43;
    public static final int ACTION_SBMS_disable = 47;
    public static final int ACTION_SBMS_setIcon = 46;
    public static final int ACTION_SBMS_setSystemUiVisibility = 48;
    public static final int ACTION_SC_getActiveSubscriptionInfoForSimSlotIndex = 50;
    public static final int ACTION_SC_getActiveSubscriptionInfoList = 49;
    public static final int ACTION_SENSOR_6DOF = 138;
    public static final int ACTION_SENSOR_ACCELEROMETER = 119;
    public static final int ACTION_SENSOR_ACCELEROMETER_UNCALIBRATED = 120;
    public static final int ACTION_SENSOR_AMBIENT_TEMPERATURE = 122;
    public static final int ACTION_SENSOR_GAME_ROTATION_VECTOR = 124;
    public static final int ACTION_SENSOR_GEOMAGNETIC_ROTATION_VECTOR = 125;
    public static final int ACTION_SENSOR_GRAVITY = 126;
    public static final int ACTION_SENSOR_GYROSCOPE = 127;
    public static final int ACTION_SENSOR_GYROSCOPE_UNCALIBRATED = 128;
    public static final int ACTION_SENSOR_HEART_BEAT = 129;
    public static final int ACTION_SENSOR_HEART_RATE = 130;
    public static final int ACTION_SENSOR_LIGHT = 131;
    public static final int ACTION_SENSOR_LINEAR_ACCELERATION = 132;
    public static final int ACTION_SENSOR_LOW_LATENCY_OFFBODY_DETECT = 133;
    public static final int ACTION_SENSOR_MAGNETIC_FIELD = 134;
    public static final int ACTION_SENSOR_MAGNETIC_FIELD_UNCALIBRATED = 135;
    public static final int ACTION_SENSOR_MOTION_DETECT = 136;
    public static final int ACTION_SENSOR_ORIENTATION = 137;
    public static final int ACTION_SENSOR_PRESSURE = 139;
    public static final int ACTION_SENSOR_PRIVATE_BASE = 123;
    public static final int ACTION_SENSOR_PROXIMITY = 140;
    public static final int ACTION_SENSOR_RELATIVE_HUMIDITY = 141;
    public static final int ACTION_SENSOR_ROTATION_VECTOR = 142;
    public static final int ACTION_SENSOR_SIGNIFICANT_MOTION = 143;
    public static final int ACTION_SENSOR_STATIONARY_DETECT = 144;
    public static final int ACTION_SENSOR_STEP_COUNTER = 145;
    public static final int ACTION_SENSOR_STEP_DETECTOR = 146;
    public static final int ACTION_SENSOR_TEMPERATURE = 147;
    public static final int ACTION_SENSOR_TYPE_ALL = 121;
    public static final int ACTION_SMSP_delete = 91;
    public static final int ACTION_SMSP_insert = 90;
    public static final int ACTION_SMSP_query = 89;
    public static final int ACTION_SMSP_update = 92;
    public static final String ACTION_TRIGGER_DETECT = "android.intent.action.ACTION_TRIGGER_DETECT";
    public static final int ACTION_UMS_getApplicationRestrictions = 73;
    public static final int ACTION_USC_sendDataForSubscriber = 88;
    public static final int ACTION_USC_sendMultipartTextForSubscriber = 87;
    public static final int ACTION_USC_sendTextForSubscriber = 86;
    public static final int ACTION_VS_vibrate = 54;
    public static final int ACTION_WMS_disableKeyguard = 71;
    public static final int ACTION_WMS_lockNow = 70;
    public static final int ACTION_WMS_reenableKeyguard = 72;
    public static final int ACTION_WfSI_disableNetwork = 64;
    public static final int ACTION_WfSI_disconnect = 66;
    public static final int ACTION_WfSI_enableNetwork = 61;
    public static final int ACTION_WfSI_getConfiguredNetworks = 60;
    public static final int ACTION_WfSI_getConnectionInfo = 58;
    public static final int ACTION_WfSI_getDhcpInfo = 59;
    public static final int ACTION_WfSI_isScanAlwaysAvailable = 57;
    public static final int ACTION_WfSI_reassociate = 65;
    public static final int ACTION_WfSI_reconnect = 67;
    public static final int ACTION_WfSI_removeNetwork = 63;
    public static final int ACTION_WfSI_saveConfiguration = 68;
    public static final int ACTION_WfSI_setWifiEnabled = 62;
    public static final int ACTION_WfSI_startScan = 69;
    public static final int ACTION_WndMS_addWindow = 82;
    public static final int ACTION_WndMS_addWindow_FIRST_SUB_WINDOW = 1206;
    public static final int ACTION_WndMS_addWindow_LAST_APPLICATION_WINDOW = 1205;
    public static final int ACTION_WndMS_addWindow_TYPE_ACCESSIBILITY_OVERLAY = 1228;
    public static final int ACTION_WndMS_addWindow_TYPE_APPLICATION = 1202;
    public static final int ACTION_WndMS_addWindow_TYPE_APPLICATION_ABOVE_SUB_PANEL = 1211;
    public static final int ACTION_WndMS_addWindow_TYPE_APPLICATION_ATTACHED_DIALOG = 1209;
    public static final int ACTION_WndMS_addWindow_TYPE_APPLICATION_MEDIA = 1207;
    public static final int ACTION_WndMS_addWindow_TYPE_APPLICATION_MEDIA_OVERLAY = 1210;
    public static final int ACTION_WndMS_addWindow_TYPE_APPLICATION_OVERLAY = 1229;
    public static final int ACTION_WndMS_addWindow_TYPE_APPLICATION_STARTING = 1203;
    public static final int ACTION_WndMS_addWindow_TYPE_APPLICATION_SUB_PANEL = 1208;
    public static final int ACTION_WndMS_addWindow_TYPE_BASE_APPLICATION = 1201;
    public static final int ACTION_WndMS_addWindow_TYPE_DRAWN_APPLICATION = 1204;
    public static final int ACTION_WndMS_addWindow_TYPE_INPUT_METHOD = 1223;
    public static final int ACTION_WndMS_addWindow_TYPE_INPUT_METHOD_DIALOG = 1224;
    public static final int ACTION_WndMS_addWindow_TYPE_KEYGUARD = 1216;
    public static final int ACTION_WndMS_addWindow_TYPE_KEYGUARD_DIALOG = 1221;
    public static final int ACTION_WndMS_addWindow_TYPE_PHONE = 1214;
    public static final int ACTION_WndMS_addWindow_TYPE_PRIORITY_PHONE = 1219;
    public static final int ACTION_WndMS_addWindow_TYPE_PRIVATE_PRESENTATION = 1227;
    public static final int ACTION_WndMS_addWindow_TYPE_SEARCH_BAR = 1213;
    public static final int ACTION_WndMS_addWindow_TYPE_STATUS_BAR = 1212;
    public static final int ACTION_WndMS_addWindow_TYPE_STATUS_BAR_PANEL = 1226;
    public static final int ACTION_WndMS_addWindow_TYPE_SYSTEM_ALERT = 1215;
    public static final int ACTION_WndMS_addWindow_TYPE_SYSTEM_DIALOG = 1220;
    public static final int ACTION_WndMS_addWindow_TYPE_SYSTEM_ERROR = 1222;
    public static final int ACTION_WndMS_addWindow_TYPE_SYSTEM_OVERLAY = 1218;
    public static final int ACTION_WndMS_addWindow_TYPE_TOAST = 1217;
    public static final int ACTION_WndMS_addWindow_TYPE_WALLPAPER = 1225;
    public static final int ACTION_WpMS_setWallpaper = 56;
    public static final String CERT_MD5 = "certMD5";
    private static final int COMPONENT_ENABLED_STATE_DEFAULT = 0;
    private static final int COMPONENT_ENABLED_STATE_DISABLED = 2;
    private static final int COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED = 4;
    private static final int COMPONENT_ENABLED_STATE_DISABLED_USER = 3;
    private static final int COMPONENT_ENABLED_STATE_ENABLED = 1;
    public static final String CONFIG = "config";
    public static final String CONFIG_ENGINE_RUN_TYPE = "engineRunType";
    public static final String CONFIG_MONITOR_ALL = "monitorAll";
    private static final boolean DEBUG;
    public static final String DETECT_ACTION_VECTOR = "actionVector";
    public static final String DETECT_ENGINE_RUN_TYPE = "engineRunType";
    public static final String FEATURE_INFO_BIN = "_bin";
    public static final String FEATURE_INFO_INT = "_int";
    public static final String FEATURE_INFO_STR = "_str";
    private static final int FIRST_SUB_WINDOW = 1000;
    private static final int FIRST_SYSTEM_WINDOW = 2000;
    private static final int LAST_APPLICATION_WINDOW = 99;
    public static final String PACKAGE_INDEX = "packageIndex";
    public static final String PACKAGE_NAME = "packageName";
    public static final String PACKAGE_NUMBER = "packageNumber";
    public static final int POINT_COUNT = 464;
    public static final String SERVICE_NAME = "trp";
    public static final String SESSION_ID = "sessionId";
    public static final int SET_ACTION_ERROR_FAKE_CALL = 102;
    public static final int SET_ACTION_ERROR_INVALID_ACTIONID = 3;
    public static final int SET_ACTION_ERROR_INVALID_UID = 2;
    public static final int SET_ACTION_ERROR_NOT_IN_FILTER = 5;
    public static final int SET_ACTION_ERROR_SYSTEM_APP = 6;
    public static final int SET_ACTION_ERROR_UNINITIALIZED = 1;
    public static final int SET_ACTION_SUCCESS = 0;
    private static final String TAG = "TRPEngManager";
    public static final int TRIGGER_BY_DURATION = 4;
    public static final int TRIGGER_BY_INCREMENT = 1;
    public static final int TRIGGER_BY_KEY_ID = 2;
    public static final int TRIGGER_BY_LIMIT = 3;
    public static final int TRIGGER_BY_SDK = 5;
    public static final String TRIGGER_MONITOR_MODE = "monitorMode";
    public static final String TRIGGER_MONITOR_SIZE = "monitorSize";
    public static final int TRIGGER_NOTHING = 0;
    public static final String TRIGGER_REASON = "triggerReason";
    public static final int TRP_ENGINE_CLOUD = 3;
    public static final int TRP_ENGINE_DISABLE = 1;
    public static final int TRP_ENGINE_LOCAL_CLOUD = 2;
    private static final int TYPE_ACCELEROMETER = 1;
    private static final int TYPE_ACCELEROMETER_UNCALIBRATED = 35;
    private static final int TYPE_ACCESSIBILITY_OVERLAY = 2032;
    private static final int TYPE_ALL = -1;
    private static final int TYPE_AMBIENT_TEMPERATURE = 13;
    private static final int TYPE_APPLICATION = 2;
    private static final int TYPE_APPLICATION_ABOVE_SUB_PANEL = 1005;
    private static final int TYPE_APPLICATION_ATTACHED_DIALOG = 1003;
    private static final int TYPE_APPLICATION_MEDIA = 1001;
    private static final int TYPE_APPLICATION_MEDIA_OVERLAY = 1004;
    private static final int TYPE_APPLICATION_OVERLAY = 2038;
    private static final int TYPE_APPLICATION_STARTING = 3;
    private static final int TYPE_APPLICATION_SUB_PANEL = 1002;
    private static final int TYPE_BASE_APPLICATION = 1;
    private static final int TYPE_DEVICE_ORIENTATION = 27;
    private static final int TYPE_DEVICE_PRIVATE_BASE = 65536;
    private static final int TYPE_DRAWN_APPLICATION = 4;
    private static final int TYPE_DYNAMIC_SENSOR_META = 32;
    private static final int TYPE_GAME_ROTATION_VECTOR = 15;
    private static final int TYPE_GEOMAGNETIC_ROTATION_VECTOR = 20;
    private static final int TYPE_GLANCE_GESTURE = 24;
    private static final int TYPE_GRAVITY = 9;
    private static final int TYPE_GYROSCOPE = 4;
    private static final int TYPE_GYROSCOPE_UNCALIBRATED = 16;
    private static final int TYPE_HEART_BEAT = 31;
    private static final int TYPE_HEART_RATE = 21;
    private static final int TYPE_INPUT_METHOD = 2011;
    private static final int TYPE_INPUT_METHOD_DIALOG = 2012;
    private static final int TYPE_KEYGUARD = 2004;
    private static final int TYPE_KEYGUARD_DIALOG = 2009;
    private static final int TYPE_LIGHT = 5;
    private static final int TYPE_LINEAR_ACCELERATION = 10;
    private static final int TYPE_LOW_LATENCY_OFFBODY_DETECT = 34;
    private static final int TYPE_MAGNETIC_FIELD = 2;
    private static final int TYPE_MAGNETIC_FIELD_UNCALIBRATED = 14;
    private static final int TYPE_MOTION_DETECT = 30;
    private static final int TYPE_ORIENTATION = 3;
    private static final int TYPE_PHONE = 2002;
    private static final int TYPE_PICK_UP_GESTURE = 25;
    private static final int TYPE_POSE_6DOF = 28;
    private static final int TYPE_PRESSURE = 6;
    private static final int TYPE_PRIORITY_PHONE = 2007;
    private static final int TYPE_PRIVATE_PRESENTATION = 2030;
    private static final int TYPE_PROXIMITY = 8;
    private static final int TYPE_RELATIVE_HUMIDITY = 12;
    private static final int TYPE_ROTATION_VECTOR = 11;
    private static final int TYPE_SEARCH_BAR = 2001;
    private static final int TYPE_SIGNIFICANT_MOTION = 17;
    private static final int TYPE_STATIONARY_DETECT = 29;
    private static final int TYPE_STATUS_BAR = 2000;
    private static final int TYPE_STATUS_BAR_PANEL = 2014;
    private static final int TYPE_STEP_COUNTER = 19;
    private static final int TYPE_STEP_DETECTOR = 18;
    private static final int TYPE_SYSTEM_ALERT = 2003;
    private static final int TYPE_SYSTEM_DIALOG = 2008;
    private static final int TYPE_SYSTEM_ERROR = 2010;
    private static final int TYPE_SYSTEM_OVERLAY = 2006;
    private static final int TYPE_TEMPERATURE = 7;
    private static final int TYPE_TILT_DETECTOR = 22;
    private static final int TYPE_TOAST = 2005;
    private static final int TYPE_WAKE_GESTURE = 23;
    private static final int TYPE_WALLPAPER = 2013;
    private static final int TYPE_WRIST_TILT_GESTURE = 26;
    private static final Singleton<ITRPEng> gTFRService = new Singleton() {
        /* class com.color.antivirus.tencent.TRPEngManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public ITRPEng create() {
            return ITRPEng.Stub.asInterface(ServiceManager.getService(TRPEngManager.SERVICE_NAME));
        }
    };
    private static TRPEngManager sInstance;

    static {
        boolean z = false;
        if (SystemProperties.getInt("persist.sys.ai_sec_log", 0) >= 1) {
            z = true;
        }
        DEBUG = z;
    }

    public static synchronized TRPEngManager getInstance() {
        TRPEngManager tRPEngManager;
        synchronized (TRPEngManager.class) {
            if (sInstance == null) {
                sInstance = new TRPEngManager();
            }
            tRPEngManager = sInstance;
        }
        return tRPEngManager;
    }

    private TRPEngManager() {
    }

    public static ITRPEng getTFRService() {
        return gTFRService.get();
    }

    public static void setAction(int actionId, int uid) {
        ITRPEng trpService;
        if (!isSystem(uid) && (trpService = getTFRService()) != null) {
            try {
                trpService.setAction(actionId, uid, Binder.getCallingPid());
            } catch (RemoteException e) {
                AntivirusLog.e(TAG, "setAction Remote E:" + e);
            }
        }
    }

    private static boolean isSystem(int uid) {
        return uid < 10000;
    }

    public static void setAction_AMS_getContentProvider(int uid, String name) {
        if (name != null) {
            int actionId = 0;
            if (name.equals(Contacts.AUTHORITY) || name.equals(ContactsContract.AUTHORITY)) {
                actionId = 109;
            } else if (name.equals(CallLog.AUTHORITY)) {
                actionId = 110;
            } else if (name.equals("sms")) {
                actionId = 111;
            } else if (name.equals(PhoneConstants.APN_TYPE_MMS)) {
                actionId = 112;
            } else if (name.equals("mms-sms")) {
                actionId = 113;
            }
            if (actionId > 0) {
                setAction(actionId, uid);
            }
        }
    }

    public static void setAction_SensorManager_registerListenerImp(int uid, int sensortype) {
        int actionId = 0;
        if (sensortype == -1) {
            actionId = 121;
        } else if (sensortype == 65536) {
            actionId = 123;
        } else if (sensortype == 34) {
            actionId = 133;
        } else if (sensortype != 35) {
            switch (sensortype) {
                case 1:
                    actionId = 119;
                    break;
                case 2:
                    actionId = 134;
                    break;
                case 3:
                    actionId = 137;
                    break;
                case 4:
                    actionId = 127;
                    break;
                case 5:
                    actionId = 131;
                    break;
                case 6:
                    actionId = 139;
                    break;
                case 7:
                    actionId = 147;
                    break;
                case 8:
                    actionId = 140;
                    break;
                case 9:
                    actionId = 126;
                    break;
                case 10:
                    actionId = 132;
                    break;
                case 11:
                    actionId = 142;
                    break;
                case 12:
                    actionId = 141;
                    break;
                case 13:
                    actionId = 122;
                    break;
                case 14:
                    actionId = 135;
                    break;
                case 15:
                    actionId = 124;
                    break;
                case 16:
                    actionId = 128;
                    break;
                case 17:
                    actionId = 143;
                    break;
                case 18:
                    actionId = 146;
                    break;
                case 19:
                    actionId = 145;
                    break;
                case 20:
                    actionId = 125;
                    break;
                case 21:
                    actionId = 130;
                    break;
                default:
                    switch (sensortype) {
                        case 28:
                            actionId = 138;
                            break;
                        case 29:
                            actionId = 144;
                            break;
                        case 30:
                            actionId = 136;
                            break;
                        case 31:
                            actionId = 129;
                            break;
                    }
            }
        } else {
            actionId = 120;
        }
        if (actionId > 0) {
            setAction(actionId, uid);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x0293, code lost:
        if (r23.equals("com.android.htmlviewer") != false) goto L_0x02a1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00fe A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0111  */
    public static void setAction_startActivityMayWait(int actionId, int uid, String action, ComponentName comp, String pkg, Uri data) {
        char c;
        boolean z;
        int actionId2 = actionId;
        if (80 == actionId2) {
            setAction(actionId, uid);
            char c2 = 0;
            if (pkg != null) {
                int hashCode = pkg.hashCode();
                if (hashCode != -973170826) {
                    if (hashCode != 361910168) {
                        if (hashCode == 2049668591 && pkg.equals("com.eg.android.AlipayGphone")) {
                            z = true;
                            if (!z) {
                                if (!z) {
                                    if (z) {
                                        if (comp != null && (comp.toShortString().contains("com.alipay.android.app.flybird.ui.window.FlyBirdWindowActivity") || comp.toShortString().contains("com.alipay.android.app.TransProcessPayActivity"))) {
                                            actionId2 = 1036;
                                        } else if (data != null && data.toString().contains(Context.ALIPAY_SERVICE) && data.toString().contains("platformapi/startapp") && (data.toString().contains("10000007") || data.toString().contains("20000221") || data.toString().contains("20000056") || data.toString().contains("20000116"))) {
                                            actionId2 = 1036;
                                        }
                                    }
                                } else if (comp != null && (comp.toShortString().contains("com.tencent.mm.plugin.wallet.pay.ui.WalletPayUI") || comp.toShortString().contains("com.tencent.mm.plugin.base.stub.WXPayEntryActivity"))) {
                                    actionId2 = 1034;
                                } else if (data != null && data.toString().contains("weixin://wap/pay")) {
                                    actionId2 = 1034;
                                } else if (data != null && data.toString().contains("weixin://dl/business/")) {
                                    actionId2 = 1035;
                                }
                            } else if (data != null && data.toString().contains("mqqwallet://open_pay/")) {
                                actionId2 = 1032;
                            }
                            if (80 != actionId2) {
                                setAction(actionId2, uid);
                                return;
                            }
                        }
                    } else if (pkg.equals(LMManager.QQ_PACKAGENAME)) {
                        z = false;
                        if (!z) {
                        }
                        if (80 != actionId2) {
                        }
                    }
                } else if (pkg.equals(LMManager.MM_PACKAGENAME)) {
                    z = true;
                    if (!z) {
                    }
                    if (80 != actionId2) {
                    }
                }
                z = true;
                if (!z) {
                }
                if (80 != actionId2) {
                }
            }
            if (action != null) {
                int actionId3 = 1037;
                switch (action.hashCode()) {
                    case -1405683728:
                        if (action.equals(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)) {
                            c = 3;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1173745501:
                        if (action.equals(Intent.ACTION_CALL)) {
                            c = 5;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1173708363:
                        if (action.equals(Intent.ACTION_DIAL)) {
                            c = 12;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1173683121:
                        if (action.equals(Intent.ACTION_EDIT)) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1173447682:
                        if (action.equals(Intent.ACTION_MAIN)) {
                            c = 13;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1173350810:
                        if (action.equals(Intent.ACTION_PICK)) {
                            c = 8;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1173264947:
                        if (action.equals(Intent.ACTION_SEND)) {
                            c = 0;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1173171990:
                        if (action.equals("android.intent.action.VIEW")) {
                            c = 15;
                            break;
                        }
                        c = 65535;
                        break;
                    case -570909077:
                        if (action.equals(Intent.ACTION_GET_CONTENT)) {
                            c = 11;
                            break;
                        }
                        c = 65535;
                        break;
                    case 239259848:
                        if (action.equals(Intent.ACTION_PICK_ACTIVITY)) {
                            c = 9;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1639291568:
                        if (action.equals(Intent.ACTION_DELETE)) {
                            c = 6;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1790957502:
                        if (action.equals("android.intent.action.INSERT")) {
                            c = 14;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1937529752:
                        if (action.equals(Intent.ACTION_WEB_SEARCH)) {
                            c = 1;
                            break;
                        }
                        c = 65535;
                        break;
                    case 2038242175:
                        if (action.equals(Intent.ACTION_ATTACH_DATA)) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    case 2068413101:
                        if (action.equals(Intent.ACTION_SEARCH)) {
                            c = 7;
                            break;
                        }
                        c = 65535;
                        break;
                    case 2068787464:
                        if (action.equals(Intent.ACTION_SENDTO)) {
                            c = 10;
                            break;
                        }
                        c = 65535;
                        break;
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                        actionId3 = 1001;
                        break;
                    case 1:
                        actionId3 = 1002;
                        break;
                    case 2:
                        actionId3 = 1003;
                        break;
                    case 3:
                        actionId3 = 1004;
                        break;
                    case 4:
                        actionId3 = 1005;
                        break;
                    case 5:
                        actionId3 = 1006;
                        break;
                    case 6:
                        actionId3 = 1007;
                        break;
                    case 7:
                        actionId3 = 1008;
                        break;
                    case 8:
                        actionId3 = 1009;
                        break;
                    case 9:
                        actionId3 = 1010;
                        break;
                    case 10:
                        actionId3 = 1011;
                        break;
                    case 11:
                        actionId3 = 1012;
                        break;
                    case 12:
                        actionId3 = 1013;
                        break;
                    case 13:
                        actionId3 = 1014;
                        break;
                    case 14:
                        actionId3 = 1015;
                        break;
                    case 15:
                        actionId3 = 1033;
                        if (pkg != null) {
                            switch (pkg.hashCode()) {
                                case -1590748058:
                                    if (pkg.equals("com.android.gallery")) {
                                        c2 = 9;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case -1558913047:
                                    break;
                                case -1253172024:
                                    if (pkg.equals("com.android.calendar2")) {
                                        c2 = 13;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case -1186720938:
                                    if (pkg.equals("com.heytap.market")) {
                                        c2 = 10;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case -1046965711:
                                    if (pkg.equals("com.android.vending")) {
                                        c2 = 5;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case -845193793:
                                    if (pkg.equals(ContactsContract.AUTHORITY)) {
                                        c2 = 8;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case -695601689:
                                    if (pkg.equals("com.android.mms")) {
                                        c2 = 1;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case 256457446:
                                    if (pkg.equals("com.android.chrome")) {
                                        c2 = 14;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case 285500553:
                                    if (pkg.equals("com.android.dialer")) {
                                        c2 = 3;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case 394871662:
                                    if (pkg.equals("com.android.packageinstaller")) {
                                        c2 = 12;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case 703863186:
                                    if (pkg.equals("com.coloros.calendar")) {
                                        c2 = 7;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case 1156888975:
                                    if (pkg.equals("com.android.settings")) {
                                        c2 = 15;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case 1178046286:
                                    if (pkg.equals("com.heytap.browser")) {
                                        c2 = 6;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case 1277566180:
                                    if (pkg.equals("com.oppo.music")) {
                                        c2 = 11;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case 1544296322:
                                    if (pkg.equals(TelephonyManager.PHONE_PROCESS_NAME)) {
                                        c2 = 2;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                case 1897569679:
                                    if (pkg.equals("com.coloros.gallery3d")) {
                                        c2 = 4;
                                        break;
                                    }
                                    c2 = 65535;
                                    break;
                                default:
                                    c2 = 65535;
                                    break;
                            }
                            switch (c2) {
                                case 0:
                                    actionId3 = 1016;
                                    break;
                                case 1:
                                    actionId3 = 1017;
                                    break;
                                case 2:
                                    actionId3 = 1018;
                                    break;
                                case 3:
                                    actionId3 = 1019;
                                    break;
                                case 4:
                                    actionId3 = 1020;
                                    break;
                                case 5:
                                    actionId3 = 1021;
                                    break;
                                case 6:
                                    actionId3 = 1022;
                                    break;
                                case 7:
                                    actionId3 = 1023;
                                    break;
                                case 8:
                                    actionId3 = 1024;
                                    break;
                                case 9:
                                    actionId3 = 1025;
                                    break;
                                case 10:
                                    actionId3 = 1026;
                                    break;
                                case 11:
                                    actionId3 = 1027;
                                    break;
                                case 12:
                                    actionId3 = 1028;
                                    break;
                                case 13:
                                    actionId3 = 1029;
                                    break;
                                case 14:
                                    actionId3 = 1030;
                                    break;
                                case 15:
                                    actionId3 = 1031;
                                    break;
                            }
                        }
                        break;
                }
                setAction(actionId3, uid);
            }
        }
    }

    public static void setAction_addWindow(int actionId, int uid, int type) {
        if (82 == actionId) {
            setAction(actionId, uid);
            if (type == 1) {
                actionId = 1201;
            } else if (type == 2) {
                actionId = 1202;
            } else if (type == 3) {
                actionId = 1203;
            } else if (type == 4) {
                actionId = 1204;
            } else if (type == 99) {
                actionId = 1205;
            } else if (type == 2030) {
                actionId = 1227;
            } else if (type == 2032) {
                actionId = 1228;
            } else if (type != 2038) {
                switch (type) {
                    case 1000:
                        actionId = 1206;
                        break;
                    case 1001:
                        actionId = 1207;
                        break;
                    case 1002:
                        actionId = 1208;
                        break;
                    case 1003:
                        actionId = 1209;
                        break;
                    case 1004:
                        actionId = 1210;
                        break;
                    case 1005:
                        actionId = 1211;
                        break;
                    default:
                        switch (type) {
                            case 2000:
                                actionId = 1212;
                                break;
                            case 2001:
                                actionId = 1213;
                                break;
                            case 2002:
                                actionId = 1214;
                                break;
                            case 2003:
                                actionId = 1215;
                                break;
                            case 2004:
                                actionId = 1216;
                                break;
                            case 2005:
                                actionId = 1217;
                                break;
                            case 2006:
                                actionId = 1218;
                                break;
                            case 2007:
                                actionId = 1219;
                                break;
                            case 2008:
                                actionId = 1220;
                                break;
                            case 2009:
                                actionId = 1221;
                                break;
                            case 2010:
                                actionId = 1222;
                                break;
                            case 2011:
                                actionId = 1223;
                                break;
                            case 2012:
                                actionId = 1224;
                                break;
                            case 2013:
                                actionId = 1225;
                                break;
                            case 2014:
                                actionId = 1226;
                                break;
                        }
                }
            } else {
                actionId = 1229;
            }
            if (82 != actionId) {
                setAction(actionId, uid);
            }
        }
    }

    public static void setAction_broadcastIntent(int actionId, int uid, String act) {
        int actionId2;
        if (81 == actionId) {
            setAction(actionId, uid);
            if (act == null) {
                setAction(1107, uid);
                return;
            }
            char c = 65535;
            switch (act.hashCode()) {
                case -1538406691:
                    if (act.equals(Intent.ACTION_BATTERY_CHANGED)) {
                        c = 4;
                        break;
                    }
                    break;
                case -1513032534:
                    if (act.equals(Intent.ACTION_TIME_TICK)) {
                        c = 2;
                        break;
                    }
                    break;
                case 172491798:
                    if (act.equals(Intent.ACTION_PACKAGE_CHANGED)) {
                        c = 0;
                        break;
                    }
                    break;
                case 505380757:
                    if (act.equals(Intent.ACTION_TIME_CHANGED)) {
                        c = 3;
                        break;
                    }
                    break;
                case 525384130:
                    if (act.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                        c = 1;
                        break;
                    }
                    break;
                case 555021408:
                    if (act.equals("com.android.launcher.action.INSTALL_SHORTCUT")) {
                        c = 6;
                        break;
                    }
                    break;
                case 1544582882:
                    if (act.equals(Intent.ACTION_PACKAGE_ADDED)) {
                        c = 5;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    actionId2 = 1100;
                    break;
                case 1:
                    actionId2 = 1101;
                    break;
                case 2:
                    actionId2 = 1102;
                    break;
                case 3:
                    actionId2 = 1103;
                    break;
                case 4:
                    actionId2 = 1104;
                    break;
                case 5:
                    actionId2 = 1105;
                    break;
                case 6:
                    actionId2 = 1106;
                    break;
                default:
                    actionId2 = 1107;
                    break;
            }
            if (81 != actionId2) {
                setAction(actionId2, uid);
            }
        }
    }

    public static void setAction_setComponentEnabledSetting(int actionId, int uid, int state) {
        if (38 == actionId) {
            setAction(actionId, uid);
            if (state == 0) {
                actionId = 1301;
            } else if (state == 1) {
                actionId = 1302;
            } else if (state == 2) {
                actionId = 1303;
            } else if (state == 3) {
                actionId = 1304;
            }
            if (38 != actionId) {
                setAction(actionId, uid);
            }
        }
    }

    public static void setForegroundApp(String packageName) {
        if (packageName != null) {
            ITRPEng trpService = getTFRService();
            if (trpService != null) {
                try {
                    trpService.setForegroundApp(packageName);
                } catch (RemoteException e) {
                    AntivirusLog.e(TAG, "setForegroundApp Remote E:" + e);
                }
            } else {
                AntivirusLog.d(TAG, "setForegroundApp E: service is null!");
            }
        }
    }

    public void setPackageToFilter(Bundle inBundle) {
        if (inBundle != null) {
            ITRPEng trpEngService = getTFRService();
            if (trpEngService == null) {
                AntivirusLog.d(TAG, "setPackageToFilter E: service is null!");
                return;
            }
            try {
                trpEngService.setPackageToFilter(inBundle);
            } catch (RemoteException e) {
                AntivirusLog.e(TAG, "setPackageToFilter RemoteException:" + e);
            }
        }
    }

    public void updateConfig(Bundle inBundle) {
        if (inBundle != null) {
            ITRPEng trpEngService = getTFRService();
            if (trpEngService == null) {
                AntivirusLog.d(TAG, "updateConfig E: service is null !");
                return;
            }
            try {
                trpEngService.updateConfig(inBundle);
            } catch (RemoteException e) {
                AntivirusLog.e(TAG, "updateConfig RemoteException:" + e);
            }
        }
    }

    public void setBroadcastTarget(String targetPackageName) {
        if (targetPackageName != null) {
            ITRPEng trpEngService = getTFRService();
            if (trpEngService == null) {
                AntivirusLog.d(TAG, "setBroadcastTarget E: service is null !");
                return;
            }
            try {
                trpEngService.setBroadcastTarget(targetPackageName);
            } catch (RemoteException e) {
                AntivirusLog.e(TAG, "setBroadcastTarget RemoteException:" + e.toString());
            }
        }
    }

    public int getVersion() {
        ITRPEng trpEngService = getTFRService();
        if (trpEngService == null) {
            AntivirusLog.d(TAG, "getVersion E: service is null !");
            return 0;
        }
        try {
            return trpEngService.getVersion();
        } catch (RemoteException e) {
            AntivirusLog.e(TAG, "getVersion RemoteException: " + e);
            return 0;
        }
    }

    public static void setAction_SetContentProviderAction(Uri uri, int action, int uid) {
        String authority;
        if (uri != null && (authority = uri.getAuthority()) != null) {
            if (Binder.getCallingUid() != uid) {
                AntivirusLog.e(TAG, "setAction failed: setAction_SetContentProviderAction_ACTION_ERROR_FAKE_CALL");
            } else if (authority.equals(CallLog.AUTHORITY)) {
                if (action == 1) {
                    setAction(101, uid);
                } else if (action == 10) {
                    setAction(104, uid);
                } else if (action == 3) {
                    setAction(102, uid);
                } else if (action == 4) {
                    setAction(103, uid);
                }
            } else if (authority.equals(Contacts.AUTHORITY) || authority.equals(ContactsContract.AUTHORITY)) {
                if (action == 1) {
                    setAction(105, uid);
                } else if (action == 10) {
                    setAction(108, uid);
                } else if (action == 3) {
                    setAction(106, uid);
                } else if (action == 4) {
                    setAction(107, uid);
                }
            } else if (authority.equals(PhoneConstants.APN_TYPE_MMS)) {
                if (action == 1) {
                    setAction(93, uid);
                } else if (action == 10) {
                    setAction(96, uid);
                } else if (action == 3) {
                    setAction(94, uid);
                } else if (action == 4) {
                    setAction(95, uid);
                }
            } else if (authority.equals("mms-sms")) {
                if (action == 1) {
                    setAction(97, uid);
                } else if (action == 10) {
                    setAction(100, uid);
                } else if (action == 3) {
                    setAction(98, uid);
                } else if (action == 4) {
                    setAction(99, uid);
                }
            } else if (!authority.equals("sms")) {
            } else {
                if (action == 1) {
                    setAction(89, uid);
                } else if (action == 10) {
                    setAction(92, uid);
                } else if (action == 3) {
                    setAction(90, uid);
                } else if (action == 4) {
                    setAction(91, uid);
                }
            }
        }
    }
}
