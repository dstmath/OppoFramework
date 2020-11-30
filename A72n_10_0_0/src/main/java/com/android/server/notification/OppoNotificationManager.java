package com.android.server.notification;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.WindowManagerGlobal;
import com.android.server.am.ColorMultiAppManagerService;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.am.OppoPermissionInterceptPolicy;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.notification.OppoNotificationManager;
import com.color.app.ColorAccessControlManager;
import com.oppo.os.LinearmotorVibrator;
import com.oppo.os.WaveformEffect;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

public class OppoNotificationManager {
    private static final Uri APP_ENCRYPT_SWITCH_CHANGED = Settings.Secure.getUriFor("access_control_lock_enabled");
    private static final String COLUMN_SMART_DRIVE_SWITCH = "smart_drive_switch";
    private static final String COLUMN_TURN_ON_FROM = "turn_on_from";
    private static final String DISTURB_FOR_GAME_SPACE_MODE = "disturb_for_game_space_mode_flag";
    private static final Uri DISTURB_FOR_GAME_SPACE_MODE_URI = Settings.Global.getUriFor(DISTURB_FOR_GAME_SPACE_MODE);
    private static final String EDGE_PANEL_TOGGLE = "edge_panel_toggle";
    private static final Uri EDGE_PANEL_TOGGLE_URI = Settings.Secure.getUriFor(EDGE_PANEL_TOGGLE);
    private static final String EVENTID_FOR_BLOCKED_BY_BLACKLIST = "notification_blacklist";
    private static final String EVENTID_FOR_BLOCKED_BY_DYNAMIC = "notification_dynamic";
    private static final String EVENTID_FOR_BLOCKED_BY_NORMAL = "notification_normal";
    private static final String EVENTID_FOR_BLOCKED_BY_UNNORMAL = "notification_unnormal";
    private static final String EVENT_PACKAGE = "package_name";
    private static final int FLAG_NO_DISTURBING_MESSAGE = 1;
    private static final String FORCE_GROUP_SUB_TITLE_NAME = "force_group_pkg";
    private static final String FORCE_GROUP_TITLE_NAME = "force_group";
    private static final String[] GAME_SPACE_WHITELIST = {"com.android.calendar", "com.android.incallui", "com.coloros.alarmclock", "com.google.android.calendar", "com.coloros.gamespace"};
    private static final String IS_EDGE_PANEL_DISABLE = "oppo.systemui.disable.edgepanel";
    private static final String IS_SYSTEM_SUPER_POWER_DISABLED_STRING = "oppo.superpowersave.disable";
    private static final String[] KEEP_ALIVE_APP_WHITELIST = {"com.autonavi.minimap", "com.baidu.BaiduMap", "com.coloros.notificationdemo"};
    private static final String[] KEEP_NOTIFICATION_APP_LIST = {"com.nearme.gamecenter", "com.nearme.instant.platform"};
    private static final long LINEARMOTOR_TIME = 400;
    private static final String LOGTAG = "NotificationService";
    private static final int MAX_CHANNELS_PER_APP = 1000;
    private static final String MAX_CHANNELS_TITLE_NAME = "max-channels";
    private static final int MAX_NOTIFICATION_IS_BLOCKED_BY_BLACKLIST = 20;
    private static final int MAX_NOTIFICATION_IS_BLOCKED_BY_DYNAMIC = 0;
    private static final int MAX_NOTIFICATION_IS_BLOCKED_BY_NORMAL = 20;
    private static final int MAX_NOTIFICATION_IS_BLOCKED_BY_UNNORMAL = 20;
    public static final int MESSAGE_SAVE_CONFIG_FILE = 1;
    private static final int MIN_CHANNELS_PER_APP = 100;
    private static final Integer[] NAVIGAION_NOTIFACTION_IDS = {Integer.valueOf((int) NAVIGAION_NOTIFICATION_CLOSE), Integer.valueOf((int) NAVIGAION_NOTIFICATION_COMMON), Integer.valueOf((int) NAVIGAION_NOTIFICATION_CAR), Integer.valueOf((int) NAVIGAION_NOTIFICATION_WALK), Integer.valueOf((int) NAVIGAION_NOTIFICATION_CYCLING), Integer.valueOf((int) NAVIGAION_NOTIFICATION_TRANSIT), Integer.valueOf((int) NAVIGAION_NOTIFICATION_OTHRE)};
    public static final int NAVIGAION_NOTIFICATION_CAR = 99910001;
    public static final int NAVIGAION_NOTIFICATION_CLOSE = 99910000;
    public static final int NAVIGAION_NOTIFICATION_COMMON = 10000;
    public static final int NAVIGAION_NOTIFICATION_CYCLING = 99910003;
    public static final int NAVIGAION_NOTIFICATION_OTHRE = 99910005;
    public static final int NAVIGAION_NOTIFICATION_TRANSIT = 99910004;
    public static final int NAVIGAION_NOTIFICATION_WALK = 99910002;
    private static final String NAVIGATION_SUB_TITLE_NAME = "package";
    private static final String NAVIGATION_TITLE_NAME = "navigation";
    private static final int NOTIFICATION_FILTER_SIZE = 3;
    private static final String NOTIFICATION_ITEM_URI = "deep_protect_notification";
    private static final int NOTIFICATION_PACKAGE_NAME = 0;
    private static final int NOTIFICATION_SUMMARY_NAME = 2;
    private static final int NOTIFICATION_TITLE_NAME = 1;
    private static final String NOTIFICATON_TITLE_NAME = "notification";
    private static final String NO_DISTURB_FOR_SCREEN_ASSISTANT = "no_disturb_for_screen_assistant";
    private static final Uri NO_DISTURB_FOR_SCREEN_ASSISTANT_URI = Settings.Secure.getUriFor(NO_DISTURB_FOR_SCREEN_ASSISTANT);
    private static final String OPENID_APID_NAME = "openid_apid";
    private static final String OPENID_APID_PKG = "apid_pkg";
    private static final String OPENID_DUID_NAME = "openid_duid";
    private static final String OPENID_DUID_PKG = "duid_pkg";
    private static final String OPENID_GUID_NAME = "openid_guid";
    private static final String OPENID_GUID_PKG = "guid_pkg";
    private static final String OPENID_TOGGLE = "openid_toggle";
    private static final Uri OPENID_TOGGLE_URI = Settings.Secure.getUriFor(OPENID_TOGGLE);
    private static final String OPPO_ACTION_SMART_DRIVE_MODE = "oppo.intent.action.SMART_DRIVE_MODE";
    private static final String OPPO_NOTIFICATION_BLACKLIST_DIRECTORY = "/data/oppo/coloros/notification";
    private static final String OPPO_NOTIFICATION_BLACKLIST_FILE_PATH = "/data/oppo/coloros/notification/sys_nms_intercept_blacklist.xml";
    private static final String OPPO_VERSION_EXP = "oppo.version.exp";
    private static final String PRIVACY_PROTECT_URI = "content://com.color.provider.SafeProvider/pp_privacy_protect";
    private static final String[] PROJECTION = {OppoPermissionInterceptPolicy.COLUMN_PKG_NAME_STR};
    private static final String QS_NO_DISTURB = "qs_no_disturb";
    private static final Uri QS_NO_DISTURB_URI = Settings.Secure.getUriFor(QS_NO_DISTURB);
    public static final int SDK_INT_26 = 26;
    private static final String[] SELECTION_ARGS = {"1", "0"};
    private static final String SELECTION_CLAUSE = "protect_type = ? AND show_notification = ?";
    private static final String SELECTION_CLAUSE_MULTI = "multi_protect_type = ? AND multi_show_notification = ?";
    private static final String SETTING_KEY_BLOCK_BANNER = "edge_panel_block_banner";
    private static final Uri SETTING_KEY_BLOCK_BANNER_URI = Settings.Secure.getUriFor(SETTING_KEY_BLOCK_BANNER);
    private static final String SETTING_KEY_CHILDREN_MODE = "children_mode_on";
    private static final Uri SETTING_KEY_CHILDREN_MODE_URI = Settings.Global.getUriFor(SETTING_KEY_CHILDREN_MODE);
    private static final String SETTING_KEY_SA_TOGGLE = "screen_assistant_toggle";
    private static final Uri SETTING_KEY_SA_TOGGLE_URI = Settings.Secure.getUriFor(SETTING_KEY_SA_TOGGLE);
    private static final String SMART_DRIVE_COMPONENT = "com.coloros.smartdrive.Receiver";
    private static final String SMART_DRIVE_PACKAGE_NAME = "com.coloros.smartdrive";
    private static final int STEP = 1;
    private static final int STRONG_AMPLITUDE = 250;
    private static final String SUPER_POWER_GROUP_SUB_TITLE_NAME = "super_power_group_pkg";
    private static final String SUPER_POWER_GROUP_TITLE_NAME = "super_power_group";
    private static final Uri SUPER_POWER_MODE_URI = Settings.System.getUriFor(SUPER_POWER_SAVE_DESKTOP_APP_LIST);
    private static final String SUPER_POWER_SAVE_DESKTOP_APP_LIST = "super_power_save_desktop_app_list";
    private static final String SUPER_POWER_SAVE_FUNC_STATE = "super_powersave_mode_state";
    private static final String SUPER_POWER_SAVE_PKG = "packageName";
    private static final String SUPER_POWER_SAVE_USER = "user";
    private static final String[] SUPER_POWER_SPACE_WHITELIST = {"com.android.calendar", "com.coloros.calendar", "com.coloros.note", "com.coloros.alarmclock", "com.coloros.favorite"};
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    private static final String TAG = "OppoNotificationManager";
    private static final long TIME_UPLOAD_THRESHOLD = 10800000;
    private static final int TURN_ON_FROM_MAP = 4;
    private static final Uri URI_DEEP_PROTECT_NOTIFICATION_CHANGED = Uri.withAppendedPath(Uri.parse(PRIVACY_PROTECT_URI), NOTIFICATION_ITEM_URI);
    private static final Uri VIBRATE_WHEN_RINGING_URI = Settings.System.getUriFor("vibrate_when_ringing");
    private static boolean sIsSuperPowerModeSupport = true;
    private static final OppoNotificationManager sOppoNotificationManager = new OppoNotificationManager();
    private boolean DEBUG = false;
    private boolean DEBUG_INTERNAL = false;
    private ArrayList<String> mBlacklistNotificationStatisticList = new ArrayList<>();
    private boolean mBlockForEdgePanel;
    private boolean mBlockedForQsNoDisturb;
    private Context mContext;
    private long[] mDefaultVibrate = {0, LINEARMOTOR_TIME};
    private List<String[]> mDynamicFilterNotificationList = new ArrayList();
    private ArrayList<String> mDynamicNotificationStatisticList = new ArrayList<>();
    private EnvelopeDetectorController mEnvelopeDetectorController;
    private Map<String, String> mEventMap = new HashMap();
    private List<String> mForceGroupList = new ArrayList();
    private boolean mGameSpaceMode;
    private HandlerThread mHandlerThread;
    private List<String> mHidePkgList;
    private final Object mHidePkgListLock = new Object();
    private List<String> mHidePkgListMulti;
    private boolean mIsAppEncryptSwitchOn = false;
    private boolean mIsChildrenModeEnable;
    private boolean mIsCtaVersion = false;
    private boolean mIsEdgePanelToggle = false;
    private boolean mIsNoDisturbForScreenAssistant;
    private boolean mIsReleaseVersion = false;
    private boolean mIsScreenAssistantEnable;
    private boolean mIsShutDown = false;
    private boolean mIsSupportEdgePanel = true;
    private boolean mIsSupportLinermotor = false;
    private List<String> mKeepAliveAppWhiteList = new ArrayList();
    private Map<String, Integer> mKeepAliveByNotificationMap = new HashMap();
    private List<String> mKeepNotificationList = new ArrayList();
    private long mLastUploadStaticsDataTime = 0;
    private int mLimitMaxChannels = 0;
    private Object mLock = new Object();
    private Handler mMainHandler;
    private List<Integer> mNavigationNotificationIDList = Arrays.asList(NAVIGAION_NOTIFACTION_IDS);
    private ArrayList<String> mNormalNotificationStatisticList = new ArrayList<>();
    private FileObserverPolicy mNotificationFileObserver = null;
    private NotificationManagerService mNotificationManagerService;
    private ArrayList<String> mNotificationNoClear = new ArrayList<>();
    private OpenID mOpenID;
    private OppoSettingsObserver mOppoSettingsObserver;
    private List<String> mSuperPowerList = new ArrayList();
    private String mSuperPowerListString;
    private boolean mSuppressedByDriveMode;
    private Handler mThreadHandler;
    private ArrayList<String> mUnNormalNotificationStatisticList = new ArrayList<>();
    private boolean mVibrateWhenRingingEnabled;
    private Vibrator mVibrator = null;

    public static OppoNotificationManager getInstance() {
        return sOppoNotificationManager;
    }

    private OppoNotificationManager() {
    }

    public void init(Context context, Handler handler, NotificationManagerService service) {
        Log.d(TAG, "init start...");
        this.mContext = context;
        this.mMainHandler = handler;
        this.mNotificationManagerService = service;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mThreadHandler = new Handler(this.mHandlerThread.getLooper()) {
            /* class com.android.server.notification.OppoNotificationManager.AnonymousClass1 */

            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    OppoNotificationManager.this.mOpenID.handleSaveConfigFile();
                }
            }
        };
        this.mOpenID = new OpenID(this.mContext, this.mThreadHandler);
        this.mEnvelopeDetectorController = new EnvelopeDetectorController(this.mContext, this.mThreadHandler);
        initProperty();
        initConfig();
        registerForUserSwitch(this.mContext);
    }

    public EnvelopeDetectorController getEnvelopeDetectorController() {
        return this.mEnvelopeDetectorController;
    }

    private void initProperty() {
        this.DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
        this.mIsCtaVersion = this.mContext.getPackageManager().hasSystemFeature("oppo.cta.support");
        this.mIsReleaseVersion = SystemProperties.getBoolean("ro.build.release_type", false);
        this.mIsSupportEdgePanel = !this.mContext.getPackageManager().hasSystemFeature(IS_EDGE_PANEL_DISABLE);
        this.mKeepNotificationList.clear();
        this.mKeepNotificationList.addAll(Arrays.asList(KEEP_NOTIFICATION_APP_LIST));
        this.mOppoSettingsObserver = new OppoSettingsObserver(this.mMainHandler);
        this.mIsSupportLinermotor = this.mContext.getPackageManager().hasSystemFeature("oppo.hardware.linermotor.support");
        this.mSuperPowerList.clear();
        this.mSuperPowerList.addAll(Arrays.asList(SUPER_POWER_SPACE_WHITELIST));
    }

    private void initConfig() {
        File notificationBlacklistDirectory = new File(OPPO_NOTIFICATION_BLACKLIST_DIRECTORY);
        File notificationBlacklistFilePath = new File(OPPO_NOTIFICATION_BLACKLIST_FILE_PATH);
        try {
            if (!notificationBlacklistDirectory.exists()) {
                notificationBlacklistDirectory.mkdirs();
            }
            if (!notificationBlacklistFilePath.exists()) {
                notificationBlacklistFilePath.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "init notificationBlacklistFilePath Dir failed!!!");
        }
        this.mNotificationFileObserver = new FileObserverPolicy(OPPO_NOTIFICATION_BLACKLIST_FILE_PATH);
        this.mNotificationFileObserver.startWatching();
        parseConfig();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:135:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x02a2, code lost:
        if (0 == 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x02a4, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x02a8, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x02a9, code lost:
        android.util.Log.w(com.android.server.notification.OppoNotificationManager.TAG, "Notification--Got execption close permReader.", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:?, code lost:
        return;
     */
    private void parseConfig() {
        Throwable th;
        Iterator<String> it;
        List<String> keepAliveList;
        OppoNotificationManager oppoNotificationManager = this;
        if (oppoNotificationManager.DEBUG) {
            Log.d(TAG, "frameworks updateNMSRUSList! ");
        }
        synchronized (oppoNotificationManager.mLock) {
            oppoNotificationManager.mDynamicFilterNotificationList.clear();
            oppoNotificationManager.mKeepAliveAppWhiteList.clear();
            oppoNotificationManager.mKeepAliveAppWhiteList.addAll(Arrays.asList(KEEP_ALIVE_APP_WHITELIST));
        }
        File xmlFile = new File(OPPO_NOTIFICATION_BLACKLIST_FILE_PATH);
        if (xmlFile.exists()) {
            FileReader xmlReader = null;
            StringReader strReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                FileReader xmlReader2 = new FileReader(xmlFile);
                parser.setInput(xmlReader2);
                int eventType = parser.getEventType();
                List<String> keepAliveList2 = new ArrayList<>();
                List<String> guidList = new ArrayList<>();
                List<String> duidList = new ArrayList<>();
                List<String> apidList = new ArrayList<>();
                List<String> forceGroupList = new ArrayList<>();
                List<String> superPowerGroupList = new ArrayList<>();
                boolean hasSuperPowerGroupList = false;
                boolean hasForceGroupList = false;
                boolean hasApidPkgList = false;
                boolean hasDuidPkgList = false;
                boolean hasGuidPkgList = false;
                boolean hasKeepAliveList = false;
                for (int eventType2 = eventType; eventType2 != 1; eventType2 = parser.next()) {
                    if (eventType2 != 0) {
                        if (eventType2 == 2) {
                            if (NOTIFICATON_TITLE_NAME.equals(parser.getName())) {
                                oppoNotificationManager.updateDynamicInterceptList(parser.getAttributeValue(null, "value"));
                            } else if (NAVIGATION_TITLE_NAME.equals(parser.getName())) {
                                hasKeepAliveList = true;
                            } else if ("package".equals(parser.getName())) {
                                String value = parser.nextText();
                                if (!TextUtils.isEmpty(value)) {
                                    keepAliveList2.add(value);
                                }
                            } else if (MAX_CHANNELS_TITLE_NAME.equals(parser.getName())) {
                                oppoNotificationManager.setLimitMaxChannels(parser.nextText());
                            } else if (OPENID_GUID_NAME.equals(parser.getName())) {
                                hasGuidPkgList = true;
                            } else if (OPENID_GUID_PKG.equals(parser.getName())) {
                                String value2 = parser.nextText();
                                if (!TextUtils.isEmpty(value2)) {
                                    guidList.add(value2);
                                }
                            } else if (OPENID_DUID_NAME.equals(parser.getName())) {
                                hasDuidPkgList = true;
                            } else if (OPENID_DUID_PKG.equals(parser.getName())) {
                                String value3 = parser.nextText();
                                if (!TextUtils.isEmpty(value3)) {
                                    duidList.add(value3);
                                }
                            } else if (OPENID_APID_NAME.equals(parser.getName())) {
                                hasApidPkgList = true;
                            } else if (OPENID_APID_PKG.equals(parser.getName())) {
                                String value4 = parser.nextText();
                                if (!TextUtils.isEmpty(value4)) {
                                    apidList.add(value4);
                                }
                            } else if (FORCE_GROUP_TITLE_NAME.equals(parser.getName())) {
                                hasForceGroupList = true;
                            } else if (FORCE_GROUP_SUB_TITLE_NAME.equals(parser.getName())) {
                                String value5 = parser.nextText();
                                if (!TextUtils.isEmpty(value5)) {
                                    forceGroupList.add(value5);
                                }
                            } else if (SUPER_POWER_GROUP_TITLE_NAME.equals(parser.getName())) {
                                hasSuperPowerGroupList = true;
                            } else if (SUPER_POWER_GROUP_SUB_TITLE_NAME.equals(parser.getName())) {
                                String value6 = parser.nextText();
                                if (!TextUtils.isEmpty(value6)) {
                                    superPowerGroupList.add(value6);
                                }
                            }
                        }
                    }
                }
                synchronized (oppoNotificationManager.mLock) {
                    if (hasKeepAliveList) {
                        try {
                            oppoNotificationManager.mKeepAliveAppWhiteList.clear();
                            oppoNotificationManager.mKeepAliveAppWhiteList.addAll(keepAliveList2);
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                    if (hasGuidPkgList && oppoNotificationManager.mOpenID != null) {
                        oppoNotificationManager.mOpenID.setGuidList(guidList);
                    }
                    if (hasDuidPkgList && oppoNotificationManager.mOpenID != null) {
                        oppoNotificationManager.mOpenID.setColorDuidList(duidList);
                    }
                    if (hasApidPkgList && oppoNotificationManager.mOpenID != null) {
                        oppoNotificationManager.mOpenID.setApidList(apidList);
                    }
                    try {
                        Iterator<Map.Entry<String, Integer>> it2 = oppoNotificationManager.mKeepAliveByNotificationMap.entrySet().iterator();
                        while (it2.hasNext()) {
                            String key = it2.next().getKey();
                            try {
                                if (!oppoNotificationManager.mKeepAliveAppWhiteList.contains(key)) {
                                    keepAliveList = keepAliveList2;
                                    oppoNotificationManager.mKeepAliveByNotificationMap.put(key, Integer.valueOf((int) NAVIGAION_NOTIFICATION_CLOSE));
                                } else {
                                    keepAliveList = keepAliveList2;
                                }
                                it2 = it2;
                                parser = parser;
                                keepAliveList2 = keepAliveList;
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        }
                        if (hasForceGroupList) {
                            oppoNotificationManager.mForceGroupList.clear();
                            oppoNotificationManager.mForceGroupList.addAll(forceGroupList);
                        }
                        if (hasSuperPowerGroupList) {
                            oppoNotificationManager.mSuperPowerList.clear();
                            oppoNotificationManager.mSuperPowerList.addAll(superPowerGroupList);
                            if (oppoNotificationManager.DEBUG) {
                                Log.d(TAG, "frameworks updateSuperPowerWhiteList! ");
                            }
                        }
                        Iterator<String> it3 = oppoNotificationManager.mSuperPowerList.iterator();
                        while (it3.hasNext()) {
                            String s = it3.next();
                            if (oppoNotificationManager.DEBUG) {
                                it = it3;
                                Log.d(TAG, "frameworks updateSuperPowerWhiteList: " + s);
                            } else {
                                it = it3;
                            }
                            oppoNotificationManager = this;
                            it3 = it;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        throw th;
                    }
                }
            } catch (FileNotFoundException e) {
                Log.w(TAG, "Notification--Couldn't find or open alarm_filter_packages file " + xmlFile);
                if (0 != 0) {
                    xmlReader.close();
                }
                if (0 != 0) {
                    strReader.close();
                }
            } catch (Exception e2) {
                Log.w(TAG, "Notification--Got execption parsing permissions.", e2);
                if (0 != 0) {
                    xmlReader.close();
                }
                if (0 != 0) {
                    strReader.close();
                }
            } catch (Throwable th5) {
                if (0 != 0) {
                    try {
                        xmlReader.close();
                    } catch (IOException e3) {
                        Log.w(TAG, "Notification--Got execption close permReader.", e3);
                        throw th5;
                    }
                }
                if (0 != 0) {
                    strReader.close();
                }
                throw th5;
            }
        }
    }

    private void updateDynamicInterceptList(String tagName) {
        String[] stringArray;
        if (this.DEBUG) {
            Log.i(TAG, "Notification--updateNotifcationTitleName_tagName = " + tagName);
        }
        if (!TextUtils.isEmpty(tagName) && (stringArray = tagName.split("/")) != null && stringArray.length > 0) {
            synchronized (this.mLock) {
                if (this.DEBUG) {
                    Log.d(TAG, "stringArray = " + Arrays.asList(stringArray));
                }
                this.mDynamicFilterNotificationList.add(stringArray);
            }
        }
    }

    public void onPhaseThrirdPartyAppsCanStart() {
        this.mOppoSettingsObserver.observe();
        this.mOpenID.init(OpenID.MD5);
    }

    public boolean isSupportLinermotor() {
        return this.mIsSupportLinermotor;
    }

    public long[] getDefaultVibrate() {
        return this.mDefaultVibrate;
    }

    public VibrationEffect getVibrateEffect(long[] times) {
        return getVibrateEffect(times, -1);
    }

    public VibrationEffect getVibrateEffect(long[] times, int repeat) {
        if (times == null) {
            return null;
        }
        try {
            int[] amplitudes = new int[times.length];
            for (int i = 0; i < times.length / 2; i++) {
                amplitudes[(i * 2) + 1] = STRONG_AMPLITUDE;
            }
            if (this.DEBUG) {
                Log.d(TAG, "Notification--getVibrateEffect--times:" + Arrays.toString(times) + ",repeat:" + repeat + ",amplitudes:" + Arrays.toString(amplitudes));
            }
            return VibrationEffect.createWaveform(times, amplitudes, repeat);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean vibrateLinearmotorIfNeed(long[] vibration, boolean hasValidSound, Uri soundUri) {
        if (this.DEBUG) {
            Log.d(TAG, "vibrateLinearmotorIfNeed() called with: vibration = [" + Arrays.toString(vibration) + "], hasValidSound = [" + hasValidSound + "], soundUri = [" + soundUri + "]");
        }
        long identity = Binder.clearCallingIdentity();
        if (!hasValidSound) {
            Binder.restoreCallingIdentity(identity);
            return false;
        }
        try {
            if (((AudioManager) this.mContext.getSystemService("audio")).getRingerModeInternal() == 1) {
                return false;
            }
            LinearmotorVibrator linearmotorVibrator = (LinearmotorVibrator) this.mContext.getSystemService("linearmotor");
            if (linearmotorVibrator == null) {
                Binder.restoreCallingIdentity(identity);
                return false;
            }
            String path = getPath(soundUri);
            if (isSystemResources(path)) {
                linearmotorVibrator.vibrate(new WaveformEffect.Builder().setRingtoneVibrateType(64).setIsRingtoneCustomized(false).setRingtoneFilePath(path).setStrengthSettingEnabled(true).build());
                Binder.restoreCallingIdentity(identity);
                return true;
            }
            Binder.restoreCallingIdentity(identity);
            return false;
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "vibrateLinearmotorIfNeed: no LinearmotorVibrator class");
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean isSystemResources(String path) {
        return path != null && path.startsWith("/system/media/audio/notifications");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006f, code lost:
        if (r10 != null) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0071, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007e, code lost:
        if (0 == 0) goto L_0x0087;
     */
    public String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String stringUri = null;
        if (uri.toString().startsWith("content://media/internal/audio/media")) {
            stringUri = uri.toString();
        } else if (TextUtils.equals(uri.toString(), "content://settings/system/notification_sound")) {
            try {
                stringUri = Settings.System.getString(this.mContext.getContentResolver(), "notification_sound");
            } catch (Exception e) {
                Log.d(TAG, "isSystemResources: exception");
            }
        }
        if (stringUri != null) {
            Uri systemUri = Uri.parse(stringUri);
            Cursor cursor = null;
            systemUri.getScheme();
            try {
                cursor = this.mContext.getContentResolver().query(systemUri, new String[]{"_data"}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                    cursor.close();
                    return string;
                }
            } catch (Exception e2) {
                Log.d(TAG, "getPath: exception");
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
        return null;
    }

    public boolean getDebug() {
        return this.DEBUG;
    }

    public boolean isShutdown() {
        return this.mIsShutDown;
    }

    public void setShutdown(boolean shutdown) {
        this.mIsShutDown = shutdown;
    }

    public boolean shouldShowNotificationToast() {
        if (this.mIsCtaVersion || this.mIsReleaseVersion) {
            return false;
        }
        return true;
    }

    public boolean isMultiAppUserIdMatch(NotificationRecord r, int userId) {
        if (!OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isCurrentProfile(userId) || r == null || r.sbn == null || userId != r.sbn.getUserId()) {
            return false;
        }
        return true;
    }

    public void updateNoClearNotification(Notification notification, String pkg) {
        synchronized (this.mNotificationNoClear) {
            if ((notification.flags & 98) != 0 && !this.mNotificationNoClear.contains(pkg)) {
                this.mNotificationNoClear.add(pkg);
                if (this.DEBUG) {
                    Log.d(TAG, "Notification--enqueueNotificationInternal: add no clear notification : " + pkg);
                }
            }
        }
    }

    public int getAppTargetSdkVersion(Context context, String pkgName) {
        int targetSdkVersion = 0;
        try {
            Context appContext = context.createPackageContext(pkgName, 2);
            if (appContext != null) {
                targetSdkVersion = appContext.getApplicationInfo().targetSdkVersion;
            }
        } catch (Exception e) {
        }
        if (this.DEBUG) {
            Log.d(TAG, "Notification--getAppTargetSdkVersion:" + targetSdkVersion);
        }
        return targetSdkVersion;
    }

    public boolean shouldInterceptToast(String pkg) {
        return false;
    }

    public boolean shouldInterceptSound(PreferencesHelper preferencesHelper, ZenModeHelper mZenModeHelper, String pkg, int uid) {
        if (this.DEBUG) {
            Log.d(TAG, "Notification--shouldInterceptSound:" + pkg);
        }
        if (getAppTargetSdkVersion(this.mContext, pkg) >= 26) {
            return mZenModeHelper.getZenMode() != 0 || shouldSuppressEffect(pkg);
        }
        if (isHidePkg(pkg, UserHandle.getUserId(uid))) {
            if (this.DEBUG) {
                Log.e(TAG, "Notification--shouldInterceptSound-isHidePkg intercept");
            }
            return true;
        } else if (isFold(preferencesHelper.getNotificationChannel(pkg, uid, (String) null, false))) {
            if (this.DEBUG) {
                Log.e(TAG, "Notification--shouldInterceptSound-isFold intercept");
            }
            return true;
        } else {
            int importance = preferencesHelper.getImportance(pkg, uid);
            NotificationChannel notificationChannel = preferencesHelper.getNotificationChannel(pkg, uid, (String) null, false);
            if (this.DEBUG) {
                Log.e(TAG, "Notification--shouldInterceptSound-importance:" + importance + ", notificationChannel : " + notificationChannel);
            }
            if ((importance < 3 && importance != -1000) || (notificationChannel != null && notificationChannel.getSound() == null)) {
                return true;
            }
            boolean bIsInImportantInterruptions = 1 == mZenModeHelper.getZenMode();
            boolean bAllowReminders = mZenModeHelper.getConfig().allowReminders;
            boolean bIsPriorityInterruption = preferencesHelper.getPackagePriority(pkg, uid) != 0;
            if (this.DEBUG) {
                Log.e(TAG, "Notification--bIsInImportantInterruptions:" + bIsInImportantInterruptions + ", bAllowReminders : " + bAllowReminders + ", bIsPriorityInterruption: " + bIsPriorityInterruption);
            }
            return (bIsInImportantInterruptions && !bAllowReminders && !bIsPriorityInterruption) || shouldSuppressEffect(pkg);
        }
    }

    public boolean isFold(NotificationChannel channel) {
        if (channel == null) {
            return false;
        }
        try {
            return ((Boolean) channel.getClass().getMethod("isFold", new Class[0]).invoke(channel, new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean shouldSuppressEffect(NotificationRecord record) {
        Notification n;
        record.getChannel();
        boolean isEnvelope = false;
        boolean isStowed = false;
        if (!(record.sbn == null || (n = record.sbn.getNotification()) == null)) {
            isStowed = n.extras.getInt("key_option_stow", 0) == -1;
            EnvelopeDetectorController mEnvelopeDetectorController2 = getInstance().getEnvelopeDetectorController();
            if (mEnvelopeDetectorController2 != null && mEnvelopeDetectorController2.interceptBuzzBeepBlink(n)) {
                Log.w(TAG, "don't vibrate and play sound because envelope assistant");
                isEnvelope = true;
            } else if (!(n.extras.getString(EnvelopeDetectorController.NOTIFICATION_SOURCE) == null || this.mNotificationManagerService.mZenModeHelper.getZenMode() == 0)) {
                Log.w(TAG, "don't vibrate and play sound because a envelope in zen mode");
                isEnvelope = true;
            }
        }
        return isEnvelope || isStowed || shouldSuppressEffect(record.sbn.getPackageName());
    }

    public boolean isStowOptionKey(String key) {
        return "key_option_stow".equals(key);
    }

    public boolean shouldSuppressEffect(String pkg) {
        boolean gameMode = SystemProperties.getBoolean("debug.gamemode.value", false);
        if (this.DEBUG) {
            Log.d(TAG, "Notification--shouldSuppressEffect-pkg:" + pkg + ",gameMode = " + gameMode + ",gameSpace:" + suppressedByGameSpace(pkg) + ",noDisturb:" + suppressedByNoDisturb() + ",driveMode:" + suppressedByDriveMode(pkg));
        }
        if (gameMode) {
            return suppressedByGameSpace(pkg);
        }
        if (suppressedByDriveMode(pkg) || suppressedByQsNoDisturb()) {
            return true;
        }
        return false;
    }

    public boolean suppressedByQsNoDisturb() {
        if (this.DEBUG_INTERNAL) {
            Log.d(TAG, "suppressedByQsNoDisturb: mBlockedForQsNoDisturb = " + this.mBlockedForQsNoDisturb);
        }
        return this.mBlockedForQsNoDisturb;
    }

    public boolean suppressedByNoDisturb() {
        if (this.DEBUG_INTERNAL) {
            Log.d(TAG, "Notification--suppressedByNoDisturb--mIsScreenAssistantEnable:" + this.mIsScreenAssistantEnable + ",mIsChildrenModeEnable = " + this.mIsChildrenModeEnable + ",mIsNoDisturbForScreenAssistant:" + this.mIsNoDisturbForScreenAssistant + ",orientation:" + this.mContext.getResources().getConfiguration().orientation + ",isInMultiWindowMode:" + isInMultiWindowMode() + ",mIsSupportEdgePanel:" + this.mIsSupportEdgePanel + ",mBlockForEdgePanel:" + this.mBlockForEdgePanel);
        }
        return this.mIsSupportEdgePanel ? this.mIsEdgePanelToggle && !this.mIsChildrenModeEnable && this.mBlockForEdgePanel && !isInMultiWindowMode() : this.mIsScreenAssistantEnable && !this.mIsChildrenModeEnable && this.mIsNoDisturbForScreenAssistant && this.mContext.getResources().getConfiguration().orientation == 2 && !isInMultiWindowMode();
    }

    public static boolean isInMultiWindowMode() {
        int dockSide;
        try {
            dockSide = WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
            dockSide = -1;
            Log.w(TAG, "Failed to get dock side: " + e);
        }
        if (dockSide == -1) {
            return false;
        }
        return true;
    }

    public boolean suppressedByDriveMode(String pkg) {
        if (SMART_DRIVE_PACKAGE_NAME.equals(pkg)) {
            return false;
        }
        return this.mSuppressedByDriveMode;
    }

    public boolean isSuppressedByDriveMode(int userId) {
        if (this.DEBUG_INTERNAL) {
            Log.d(TAG, "Notification--isSuppressedByDriveMode--userId:" + userId + ",mode:" + this.mSuppressedByDriveMode);
        }
        return this.mSuppressedByDriveMode;
    }

    public void setSuppressedByDriveMode(boolean mode, int userId) {
        if (this.DEBUG_INTERNAL) {
            Log.d(TAG, "Notification--setSuppressedByDriveMode:" + mode + ",userId:" + userId);
        }
        this.mSuppressedByDriveMode = mode;
    }

    public void setNavigationStatus(String pkg, String channelId, int callingUid, int callingPid, int reason) {
        if (this.DEBUG) {
            Log.d(TAG, "Notification--cancelAllNotificationsInt=" + callingUid + " callingPid=" + callingPid + ",pkg:" + pkg + ",channelId:" + channelId + ",reason:" + reason);
        }
        if (reason != 17 && reason != 7 && reason != 9) {
            setNavigationModeIfNeed(pkg, NAVIGAION_NOTIFICATION_CLOSE, false);
        }
    }

    public boolean shouldKeepNotifcationWhenForceStop(String pkg, NotificationRecord r, int reason) {
        if (reason != 10021 && reason != 10020) {
            return false;
        }
        if (r.sbn.isClearable() && (isSystemApp(pkg) || this.mKeepNotificationList.contains(pkg))) {
            return true;
        }
        Notification notification = r.getNotification();
        if (notification == null) {
            return false;
        }
        String appPackage = notification.extras.getString("appPackage");
        if (pkg == null || !pkg.equals(appPackage)) {
            return false;
        }
        return true;
    }

    public boolean canListenNotificationChannelChange(String pkg) {
        return "com.coloros.notificationmanager".equals(pkg);
    }

    public void sendDataToDcs(String logTag, String eventId, ArrayList<String> dataStatisticList) {
        if (eventId.equals(EVENTID_FOR_BLOCKED_BY_BLACKLIST)) {
            for (int i = 0; i < dataStatisticList.size(); i++) {
                sendDataToDcsAfterLocalHandle(logTag, eventId, dataStatisticList.get(i));
            }
            return;
        }
        Map<String, String> eventMap = new HashMap<>();
        for (int j = 0; j < dataStatisticList.size(); j++) {
            String strPkg = dataStatisticList.get(j);
            if (eventMap.containsKey(strPkg)) {
                eventMap.put(strPkg, String.valueOf(Integer.parseInt(eventMap.get(strPkg)) + 1));
            } else {
                eventMap.put(strPkg, "1");
            }
        }
        if (eventId.equals(EVENTID_FOR_BLOCKED_BY_DYNAMIC)) {
            this.mDynamicNotificationStatisticList.clear();
        } else if (eventId.equals(EVENTID_FOR_BLOCKED_BY_NORMAL)) {
            this.mNormalNotificationStatisticList.clear();
        } else if (eventId.equals(EVENTID_FOR_BLOCKED_BY_UNNORMAL)) {
            this.mUnNormalNotificationStatisticList.clear();
        }
        if (this.DEBUG) {
            Log.d(TAG, "Notification--sendDataToDcs_eventId = " + eventId);
        }
        OppoStatistics.onCommon(this.mContext, logTag, eventId, eventMap, false);
    }

    public boolean shouldInterceptNotification(String pkg, Notification notification, int incomingUserId) {
        List<String[]> list;
        boolean isNeedBlock = false;
        if (notification == null) {
            return false;
        }
        String title = notification.extras.getString("android.title");
        String text = notification.extras.getString("android.text");
        if (!(title == null || (list = this.mDynamicFilterNotificationList) == null || list.size() <= 0)) {
            int index = 0;
            while (true) {
                if (index >= this.mDynamicFilterNotificationList.size()) {
                    break;
                }
                String[] tempArray = this.mDynamicFilterNotificationList.get(index);
                if (tempArray == null || tempArray.length != 3 || ((!"null".equals(tempArray[0]) && !pkg.equals(tempArray[0])) || ((!"null".equals(tempArray[1]) && !title.equals(tempArray[1])) || (!"null".equals(tempArray[2]) && !text.contains(tempArray[2]))))) {
                    index++;
                }
            }
            isNeedBlock = true;
            if (this.DEBUG) {
                Log.d(TAG, "notification.sendTitleData_hasMatched!");
            }
            if (isNeedBlock) {
                String resultString = pkg + "_" + title + "_" + text;
                if (!resultString.isEmpty()) {
                    this.mDynamicNotificationStatisticList.add(resultString);
                }
                if (this.mDynamicNotificationStatisticList.size() > 0) {
                    if (this.DEBUG) {
                        Log.d(TAG, "Notification--notification.sendDynamicInterceptData");
                    }
                    sendDataToDcs(LOGTAG, EVENTID_FOR_BLOCKED_BY_DYNAMIC, this.mDynamicNotificationStatisticList);
                }
                if (this.DEBUG) {
                    Log.d(TAG, "Notification--we discard it, because " + pkg + " is in list of Dynamic intercept notification!");
                }
            }
        }
        if (!isNeedBlock && checkGroup(pkg, notification)) {
            return true;
        }
        if (isNeedBlock || !isNeedToBlockInSuperPowerMode(pkg, incomingUserId)) {
            return isNeedBlock;
        }
        if (!this.DEBUG) {
            return true;
        }
        Log.d(TAG, "Super Power Mode intercept this notification" + pkg);
        return true;
    }

    private boolean checkGroup(String pkg, Notification notification) {
        boolean forceGroup = shouldForceGroup(pkg);
        if (this.DEBUG) {
            Log.d(TAG, "Notification--checkGroup: pkg=" + pkg + ",notification=" + notification + ",forceGroup:" + forceGroup);
        }
        if (!forceGroup) {
            return false;
        }
        if (notification.isGroupSummary()) {
            return true;
        }
        Notification.Builder builder = Notification.Builder.recoverBuilder(this.mContext, notification);
        builder.setGroup(null);
        builder.setSortKey(null);
        builder.build();
        return false;
    }

    private boolean shouldForceGroup(String pkg) {
        return this.mForceGroupList.contains(pkg);
    }

    private int getMutilAppUid(String pkg, int uid) {
        if (UserHandle.getUserId(uid) != 999 || pkg == null || !OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiApp(pkg)) {
            return uid;
        }
        return UserHandle.getUid(0, UserHandle.getAppId(uid));
    }

    private void sendDataToDcsAfterLocalHandle(String logTag, String eventID, String eventTag) {
        if (this.DEBUG) {
            Log.d(TAG, "Notification--sendEventDataAfterLocalHandle_eventID = " + eventID + ", eventTag = " + eventTag);
        }
        if (this.mEventMap.containsKey(eventTag)) {
            this.mEventMap.put(eventTag, String.valueOf(Integer.valueOf(this.mEventMap.get(eventTag)).intValue() + 1));
            if (this.DEBUG) {
                Log.d(TAG, "containsKey = " + eventTag + ", and new value = " + this.mEventMap.get(eventTag));
            }
        } else {
            this.mEventMap.put(eventTag, String.valueOf(1));
            if (this.DEBUG) {
                Log.d(TAG, "not contains " + eventTag);
            }
        }
        if (this.DEBUG) {
            Log.d(TAG, "value =" + this.mEventMap.get(eventTag) + ",eventMap = " + this.mEventMap);
        }
        long currentTime = System.currentTimeMillis();
        if (this.mLastUploadStaticsDataTime <= 0) {
            this.mLastUploadStaticsDataTime = currentTime;
        }
        if (this.DEBUG) {
            Log.d(TAG, " onKVEvent, durring = " + (currentTime - this.mLastUploadStaticsDataTime));
        }
        if (currentTime - this.mLastUploadStaticsDataTime > TIME_UPLOAD_THRESHOLD && this.mEventMap.size() > 0) {
            OppoStatistics.onCommon(this.mContext, logTag, eventID, this.mEventMap, false);
            this.mLastUploadStaticsDataTime = currentTime;
            this.mEventMap.clear();
        }
    }

    private boolean dumpNoClearNotification(PrintWriter pw, String[] args) {
        if (args.length != 1 || !"noClear".equals(args[0])) {
            return false;
        }
        pw.println("\n  mNotificationNoClear:");
        synchronized (this.mNotificationNoClear) {
            Iterator<String> it = this.mNotificationNoClear.iterator();
            while (it.hasNext()) {
                pw.println("    NoClearNotification:" + it.next());
            }
        }
        return true;
    }

    public boolean isForegroundPackage(String pkg) {
        try {
            if (((ActivityManager) this.mContext.getSystemService("activity")).getPackageImportance(pkg) == 100) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.w(TAG, "Notification--getTopActivityComponentName exception");
            return false;
        }
    }

    public boolean isSystemApp(String pkg) {
        try {
            if ((this.mContext.getPackageManager().getApplicationInfo(pkg, 0).flags & 1) != 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean suppressedByGameSpace(String pkgName) {
        if (!this.mGameSpaceMode) {
            return false;
        }
        for (String pkg : GAME_SPACE_WHITELIST) {
            if (pkg.equals(pkgName)) {
                return false;
            }
        }
        return true;
    }

    public void setKeepAliveAppIfNeed(String pkgName, int id, boolean isKeepAlive) {
        setNavigationModeIfNeed(pkgName, id, isKeepAlive);
    }

    public void setNavigationModeIfNeed(String pkgName, int id, boolean isKeepAlive) {
        boolean shouldSetDriveMode = false;
        synchronized (this.mLock) {
            if (!TextUtils.isEmpty(pkgName) && this.mKeepAliveAppWhiteList.contains(pkgName) && this.mNavigationNotificationIDList.contains(Integer.valueOf(id))) {
                this.mKeepAliveByNotificationMap.put(pkgName, Integer.valueOf(isKeepAlive ? id : NAVIGAION_NOTIFICATION_CLOSE));
                shouldSetDriveMode = true;
            }
            if (this.DEBUG_INTERNAL) {
                Log.d(TAG, "Notification--setNavigationModeIfNeed--pkgName:" + pkgName + ",id:" + id + ",isKeepAlive:" + isKeepAlive + ",list:" + this.mNavigationNotificationIDList + ",mKeepAliveByNotificationMap:" + this.mKeepAliveByNotificationMap);
            }
        }
        if (shouldSetDriveMode) {
            sendBroadcastToSmartDrive(isKeepAlive);
        }
    }

    private boolean dumpNavigationStatus(PrintWriter pw, String[] args) {
        if (args.length != 1 || !NAVIGATION_TITLE_NAME.equals(args[0])) {
            return false;
        }
        synchronized (this.mLock) {
            try {
                pw.println(" list:" + this.mKeepAliveAppWhiteList);
                Iterator<Map.Entry<String, Integer>> it = this.mKeepAliveByNotificationMap.entrySet().iterator();
                while (it.hasNext()) {
                    String pkg = it.next().getKey();
                    pw.println(" pkg:" + pkg + ", mode:" + getNavigationMode(pkg, -1) + ",keepAlive:" + shouldKeepAlive(pkg, -1) + ",isDriveNavigationMode:" + isDriveNavigationMode(pkg, -1));
                }
                pw.println("\n isDriveNavigationMode-null:" + isDriveNavigationMode(null, -1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean shouldKeepAlive(String pkg, int userId) {
        if (getNavigationMode(pkg, userId) != 99910000) {
            return true;
        }
        return false;
    }

    public boolean isNavigationMode(int userId) {
        synchronized (this.mLock) {
            Iterator<Map.Entry<String, Integer>> it = this.mKeepAliveByNotificationMap.entrySet().iterator();
            while (it.hasNext()) {
                if (getNavigationMode(it.next().getKey(), userId) != 99910000) {
                    return true;
                }
            }
            return false;
        }
    }

    public String[] getEnableNavigationApps(int userId) {
        String[] strArr;
        synchronized (this.mLock) {
            strArr = (String[]) this.mKeepAliveAppWhiteList.toArray(new String[0]);
        }
        return strArr;
    }

    public boolean isDriveNavigationMode(String pkg, int userId) {
        if (TextUtils.isEmpty(pkg)) {
            synchronized (this.mLock) {
                Iterator<Map.Entry<String, Integer>> it = this.mKeepAliveByNotificationMap.entrySet().iterator();
                while (it.hasNext()) {
                    if (getNavigationMode(it.next().getKey(), userId) == 99910001) {
                        return true;
                    }
                }
                return false;
            }
        } else if (getNavigationMode(pkg, userId) == 99910001) {
            return true;
        } else {
            return false;
        }
    }

    public int getNavigationMode(String pkg, int userId) {
        if (TextUtils.isEmpty(pkg)) {
            return NAVIGAION_NOTIFICATION_CLOSE;
        }
        synchronized (this.mLock) {
            if (!this.mKeepAliveByNotificationMap.containsKey(pkg)) {
                return NAVIGAION_NOTIFICATION_CLOSE;
            }
            return this.mKeepAliveByNotificationMap.get(pkg).intValue();
        }
    }

    public boolean checkGetGUID(String pkg, int uid) {
        OpenID openID = this.mOpenID;
        if (openID != null) {
            return openID.checkGetGUID(pkg, uid);
        }
        return false;
    }

    public boolean checkGetAPID(String pkg, int uid) {
        OpenID openID = this.mOpenID;
        if (openID != null) {
            return openID.checkGetAPID(pkg, uid);
        }
        return false;
    }

    public String getOpenid(String pkg, int uid, String type) {
        OpenID openID = this.mOpenID;
        if (openID == null) {
            return "";
        }
        return openID.getOpenid(pkg, uid, type);
    }

    public void clearOpenid(String pkg, int uid, String type) {
        OpenID openID = this.mOpenID;
        if (openID != null) {
            openID.clearOpenid(pkg, uid, type);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initHidePkgList() {
        if (this.mIsAppEncryptSwitchOn) {
            List<String> hiddenPkgList = new ArrayList<>();
            List<String> multiHiddenPkgList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : ColorAccessControlManager.getInstance().getPrivacyAppInfo(ColorAccessControlManager.USER_CURRENT).entrySet()) {
                if ((entry.getValue().intValue() & 4) != 0) {
                    hiddenPkgList.add(entry.getKey());
                }
            }
            for (Map.Entry<String, Integer> entry2 : ColorAccessControlManager.getInstance().getPrivacyAppInfo((int) ColorMultiAppManagerService.USER_ID).entrySet()) {
                if ((entry2.getValue().intValue() & 4) != 0) {
                    multiHiddenPkgList.add(entry2.getKey());
                }
            }
            synchronized (this.mHidePkgListLock) {
                this.mHidePkgList = hiddenPkgList;
                this.mHidePkgListMulti = multiHiddenPkgList;
            }
            if (this.DEBUG) {
                Log.d(TAG, "Notification--initHidePkgList: mHidePkgList = " + this.mHidePkgList + " mHidePkgListMulti = " + this.mHidePkgListMulti);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetHidePkgList() {
        if (!this.mIsAppEncryptSwitchOn) {
            synchronized (this.mHidePkgListLock) {
                this.mHidePkgList = null;
                this.mHidePkgListMulti = null;
            }
        }
    }

    private void setLimitMaxChannels(String count) {
        try {
            int max = Integer.parseInt(count);
            if (max < 100) {
                Log.d(TAG, "Notification--setLimitMaxChannels MIN_CHANNELS_PER_APP > max:" + max + ",count:" + count);
                return;
            }
            this.mLimitMaxChannels = max;
        } catch (Exception e) {
            Log.d(TAG, "Notification--setMaxChannels e:" + e.toString());
        }
    }

    private int getLimitMaxChannels() {
        int i = this.mLimitMaxChannels;
        if (i != 0) {
            return i;
        }
        return 1000;
    }

    public boolean shouldLimitChannels(PreferencesHelper preferencesHelper, String pkg, int uid, int channelSize) {
        try {
            int curChannelCount = preferencesHelper.getNotificationChannels(pkg, uid, false).getList().size();
            if (this.DEBUG_INTERNAL) {
                Log.d(TAG, "Notification--needLimitChannels--pkg:" + pkg + ",channelSize:" + channelSize + ",curChannelCount:" + curChannelCount + ",max:" + getLimitMaxChannels());
            }
            if (curChannelCount + channelSize > getLimitMaxChannels()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.d(TAG, "Notification--shouldLimitChannels e:" + e.toString());
        }
    }

    public boolean isHidePkg(String pkg, int userId) {
        synchronized (this.mHidePkgListLock) {
            if (userId == 0) {
                try {
                    if (this.mHidePkgList != null) {
                        if (!this.mHidePkgList.isEmpty()) {
                            return this.mHidePkgList.contains(pkg);
                        }
                    }
                    return false;
                } catch (Throwable th) {
                    throw th;
                }
            } else if (userId != 999) {
                return false;
            } else {
                if (this.mHidePkgListMulti != null) {
                    if (!this.mHidePkgListMulti.isEmpty()) {
                        return this.mHidePkgListMulti.contains(pkg);
                    }
                }
                return false;
            }
        }
    }

    public void sendBroadcastToSmartDrive(boolean isKeepAlive) {
        try {
            boolean switchOn = isDriveNavigationMode(null, -1);
            if (isKeepAlive || !switchOn) {
                Intent intent = new Intent(OPPO_ACTION_SMART_DRIVE_MODE);
                intent.setComponent(new ComponentName(SMART_DRIVE_PACKAGE_NAME, SMART_DRIVE_COMPONENT));
                intent.addFlags(32);
                intent.putExtra(COLUMN_SMART_DRIVE_SWITCH, switchOn ? 1 : 0);
                intent.putExtra(COLUMN_TURN_ON_FROM, 4);
                intent.setPackage(SMART_DRIVE_PACKAGE_NAME);
                Log.e(TAG, "Notification--sendBroadcastToSmartDrive");
                this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
            }
        } catch (Exception e) {
            Log.e(TAG, "Notification--sendBroadcastToSmartDrive--" + e.toString());
        }
    }

    public boolean dumpOppoNotificationInfo(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (dumpNoClearNotification(pw, args) || dumpNavigationStatus(pw, args)) {
            return true;
        }
        OpenID openID = this.mOpenID;
        if (openID == null || !openID.dumpOpenidInfo(pw, args)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
            if (OppoNotificationManager.this.DEBUG) {
                Log.d(OppoNotificationManager.TAG, "Notification--FileObserverPolicy_path = " + path);
            }
        }

        public void onEvent(int event, String path) {
            if (OppoNotificationManager.this.DEBUG) {
                Log.d(OppoNotificationManager.TAG, "Notification--onEvent: event = " + event + ",focusPath = " + this.mFocusPath);
            }
            if (event == 8 && this.mFocusPath.equals(OppoNotificationManager.OPPO_NOTIFICATION_BLACKLIST_FILE_PATH)) {
                if (OppoNotificationManager.this.DEBUG) {
                    Log.d(OppoNotificationManager.TAG, "Notification--onEvent: focusPath = OPPO_NOTIFICATION_BLACKLIST_FILE_PATH");
                }
                OppoNotificationManager.this.parseConfig();
            }
        }
    }

    /* access modifiers changed from: private */
    public class OppoSettingsObserver extends ContentObserver {
        OppoSettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver resolver = OppoNotificationManager.this.mContext.getContentResolver();
            try {
                resolver.registerContentObserver(OppoNotificationManager.VIBRATE_WHEN_RINGING_URI, false, this, -1);
                resolver.registerContentObserver(OppoNotificationManager.DISTURB_FOR_GAME_SPACE_MODE_URI, false, this, -1);
                resolver.registerContentObserver(OppoNotificationManager.NO_DISTURB_FOR_SCREEN_ASSISTANT_URI, false, this, -1);
                resolver.registerContentObserver(OppoNotificationManager.SETTING_KEY_CHILDREN_MODE_URI, false, this, -1);
                resolver.registerContentObserver(OppoNotificationManager.SETTING_KEY_SA_TOGGLE_URI, false, this, -1);
                resolver.registerContentObserver(OppoNotificationManager.EDGE_PANEL_TOGGLE_URI, false, this, -1);
                resolver.registerContentObserver(OppoNotificationManager.SETTING_KEY_BLOCK_BANNER_URI, false, this, -1);
                resolver.registerContentObserver(OppoNotificationManager.OPENID_TOGGLE_URI, false, this, -1);
                resolver.registerContentObserver(OppoNotificationManager.QS_NO_DISTURB_URI, false, this, -1);
                resolver.registerContentObserver(OppoNotificationManager.APP_ENCRYPT_SWITCH_CHANGED, false, this, -1);
                resolver.registerContentObserver(OppoNotificationManager.SUPER_POWER_MODE_URI, false, this, -1);
            } catch (Exception e) {
                Log.e(OppoNotificationManager.TAG, "Notification--Exception trying to registerContentObserver on setting", e);
            }
            try {
                resolver.registerContentObserver(OppoNotificationManager.URI_DEEP_PROTECT_NOTIFICATION_CHANGED, false, this, -1);
            } catch (Exception e2) {
                Log.e(OppoNotificationManager.TAG, "Notification--Exception trying to registerContentObserver on URI_DEEP_PROTECT_NOTIFICATION_CHANGED", e2);
            }
            try {
                update(null, -1);
            } catch (Exception e3) {
                Log.e(OppoNotificationManager.TAG, "Notification--update", e3);
            }
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            update(uri, userId);
        }

        public void update(Uri uri, int userId) {
            Log.d(OppoNotificationManager.TAG, "Notification--OppoSettingsObserver onChange " + uri);
            ContentResolver resolver = OppoNotificationManager.this.mContext.getContentResolver();
            boolean z = false;
            if (uri == null || OppoNotificationManager.SUPER_POWER_MODE_URI.equals(uri)) {
                OppoNotificationManager oppoNotificationManager = OppoNotificationManager.this;
                oppoNotificationManager.mSuperPowerListString = Settings.System.getStringForUser(oppoNotificationManager.mContext.getContentResolver(), OppoNotificationManager.SUPER_POWER_SAVE_DESKTOP_APP_LIST, 0);
            }
            if (uri == null || OppoNotificationManager.VIBRATE_WHEN_RINGING_URI.equals(uri)) {
                OppoNotificationManager.this.mVibrateWhenRingingEnabled = Settings.System.getIntForUser(resolver, "vibrate_when_ringing", 0, -2) != 0;
            }
            if (uri == null || OppoNotificationManager.DISTURB_FOR_GAME_SPACE_MODE_URI.equals(uri)) {
                OppoNotificationManager.this.mGameSpaceMode = (Settings.Global.getInt(resolver, OppoNotificationManager.DISTURB_FOR_GAME_SPACE_MODE, 0) & 1) != 0;
            }
            if (uri == null || OppoNotificationManager.NO_DISTURB_FOR_SCREEN_ASSISTANT_URI.equals(uri)) {
                OppoNotificationManager.this.mIsNoDisturbForScreenAssistant = Settings.Secure.getIntForUser(resolver, OppoNotificationManager.NO_DISTURB_FOR_SCREEN_ASSISTANT, 0, -2) == 1;
            }
            if (uri == null || OppoNotificationManager.APP_ENCRYPT_SWITCH_CHANGED.equals(uri)) {
                OppoNotificationManager.this.mIsAppEncryptSwitchOn = Settings.Secure.getIntForUser(resolver, "access_control_lock_enabled", 0, -2) == 1;
                OppoNotificationManager.this.mThreadHandler.post(new Runnable() {
                    /* class com.android.server.notification.$$Lambda$OppoNotificationManager$OppoSettingsObserver$8iM6EyDELQvr6XwrhV6OlUseY18 */

                    public final void run() {
                        OppoNotificationManager.OppoSettingsObserver.this.lambda$update$0$OppoNotificationManager$OppoSettingsObserver();
                    }
                });
            }
            if (uri == null || OppoNotificationManager.URI_DEEP_PROTECT_NOTIFICATION_CHANGED.equals(uri)) {
                OppoNotificationManager.this.mThreadHandler.post(new Runnable() {
                    /* class com.android.server.notification.$$Lambda$OppoNotificationManager$OppoSettingsObserver$RFw1z1FNcvUHEZUYd2Ko_MRYB7Y */

                    public final void run() {
                        OppoNotificationManager.OppoSettingsObserver.this.lambda$update$1$OppoNotificationManager$OppoSettingsObserver();
                    }
                });
            }
            if (uri == null || OppoNotificationManager.SETTING_KEY_CHILDREN_MODE_URI.equals(uri)) {
                OppoNotificationManager.this.mIsChildrenModeEnable = Settings.Global.getInt(resolver, OppoNotificationManager.SETTING_KEY_CHILDREN_MODE, 0) == 1;
            }
            if (uri == null || OppoNotificationManager.SETTING_KEY_SA_TOGGLE_URI.equals(uri)) {
                OppoNotificationManager oppoNotificationManager2 = OppoNotificationManager.this;
                oppoNotificationManager2.mIsScreenAssistantEnable = Settings.Secure.getIntForUser(oppoNotificationManager2.mContext.getContentResolver(), OppoNotificationManager.SETTING_KEY_SA_TOGGLE, 0, -2) == 1;
            }
            if (uri == null || OppoNotificationManager.EDGE_PANEL_TOGGLE_URI.equals(uri)) {
                OppoNotificationManager oppoNotificationManager3 = OppoNotificationManager.this;
                oppoNotificationManager3.mIsEdgePanelToggle = Settings.Secure.getIntForUser(oppoNotificationManager3.mContext.getContentResolver(), OppoNotificationManager.EDGE_PANEL_TOGGLE, 0, -2) == 1;
            }
            if (uri == null || OppoNotificationManager.SETTING_KEY_BLOCK_BANNER_URI.equals(uri)) {
                OppoNotificationManager.this.mBlockForEdgePanel = Settings.Secure.getIntForUser(resolver, OppoNotificationManager.SETTING_KEY_BLOCK_BANNER, 0, -2) == 1;
            }
            if (uri == null || OppoNotificationManager.QS_NO_DISTURB_URI.equals(uri)) {
                OppoNotificationManager oppoNotificationManager4 = OppoNotificationManager.this;
                if (Settings.Secure.getIntForUser(resolver, OppoNotificationManager.QS_NO_DISTURB, 0, -2) == 1) {
                    z = true;
                }
                oppoNotificationManager4.mBlockedForQsNoDisturb = z;
            }
            if ((uri == null || OppoNotificationManager.OPENID_TOGGLE_URI.equals(uri)) && OppoNotificationManager.this.mOpenID != null) {
                OppoNotificationManager.this.updateAllUserOuidToggle(userId);
            }
        }

        public /* synthetic */ void lambda$update$0$OppoNotificationManager$OppoSettingsObserver() {
            OppoNotificationManager.this.resetHidePkgList();
        }

        public /* synthetic */ void lambda$update$1$OppoNotificationManager$OppoSettingsObserver() {
            OppoNotificationManager.this.initHidePkgList();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAllUserOuidToggle(int userId) {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean z = false;
        if (userId != -1) {
            OpenID openID = this.mOpenID;
            if (Settings.Secure.getIntForUser(resolver, OPENID_TOGGLE, 1, userId) == 1) {
                z = true;
            }
            openID.setOuidToggle(z, userId);
            return;
        }
        for (UserInfo ui : UserManager.get(this.mContext).getUsers()) {
            this.mOpenID.setOuidToggle(Settings.Secure.getIntForUser(resolver, OPENID_TOGGLE, 1, ui.id) == 1, ui.id);
        }
    }

    public boolean isInvalidOpushNotification(String pkg, int uid, PreferencesHelper preferencesHelper, Notification notification) {
        boolean oPushChannel;
        if (preferencesHelper == null || notification == null) {
            return false;
        }
        try {
            NotificationChannel channel = preferencesHelper.getNotificationChannel(pkg, uid, notification.getChannelId(), false);
            oPushChannel = ((Boolean) channel.getClass().getMethod("isOpush", new Class[0]).invoke(channel, new Object[0])).booleanValue();
        } catch (Exception e) {
            oPushChannel = false;
            Log.e(TAG, "Notification--call isOpush error: " + e.getMessage() + "--" + notification);
        }
        boolean sentByOPush = "com.coloros.mcs".equals(pkg);
        if (!oPushChannel || sentByOPush) {
            return false;
        }
        return true;
    }

    private void registerForUserSwitch(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        context.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.notification.OppoNotificationManager.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                if (OppoNotificationManager.this.mOppoSettingsObserver != null) {
                    OppoNotificationManager.this.mOppoSettingsObserver.update(null, -2);
                }
                if (OppoNotificationManager.this.mEnvelopeDetectorController != null) {
                    OppoNotificationManager.this.mEnvelopeDetectorController.initStatus();
                }
            }
        }, filter);
    }

    private boolean getSuperPowerModeState() {
        boolean inSuperPowerMode = false;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), SUPER_POWER_SAVE_FUNC_STATE, 0, 0) == 1) {
            inSuperPowerMode = true;
        }
        if (this.DEBUG) {
            Log.d(TAG, "Get SuperPower mode state: inSuperPowerMode: " + inSuperPowerMode);
        }
        return inSuperPowerMode;
    }

    private static boolean isSuperPowerModeAllowed(Context mContext2) {
        sIsSuperPowerModeSupport = !mContext2.getPackageManager().hasSystemFeature(IS_SYSTEM_SUPER_POWER_DISABLED_STRING);
        return sIsSuperPowerModeSupport;
    }

    private boolean isNeedToBlockInSuperPowerMode(String pkg, int incomingUserId) {
        List<String> whiteList;
        if (!isSuperPowerModeAllowed(this.mContext) || !getSuperPowerModeState()) {
            return false;
        }
        boolean isNeedCanced = true;
        String superPowerList = this.mSuperPowerListString;
        synchronized (this.mLock) {
            whiteList = new ArrayList<>(this.mSuperPowerList);
        }
        try {
            JSONArray jsonArray = new JSONArray(superPowerList);
            int i = 0;
            while (true) {
                if (i >= jsonArray.length()) {
                    break;
                }
                JSONObject ruleObject = jsonArray.getJSONObject(i);
                String superPowrPkg = ruleObject.optString("packageName");
                int superPowerPkgUser = ruleObject.optInt(SUPER_POWER_SAVE_USER);
                if (!pkg.equals(superPowrPkg) || superPowerPkgUser != incomingUserId) {
                    i++;
                } else {
                    if (this.DEBUG) {
                        Log.d(TAG, "Super Power Mode do not intercept this notification" + pkg);
                    }
                    isNeedCanced = false;
                }
            }
            if (whiteList.contains(pkg)) {
                return false;
            }
            return isNeedCanced;
        } catch (Exception e) {
            return true;
        }
    }
}
