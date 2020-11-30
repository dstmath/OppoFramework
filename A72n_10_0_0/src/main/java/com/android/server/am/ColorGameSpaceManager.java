package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OppoBaseIntent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.database.ContentObserver;
import android.graphics.RectF;
import android.net.INetworkPolicyManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.server.OppoBaseNetworkPolicyManagerService;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.net.IColorNetworkPolicyManagerServiceEx;
import com.android.server.net.NetworkPolicyManagerService;
import com.color.util.ColorTypeCastingHelper;
import com.oppo.app.IOppoGameSpaceController;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ColorGameSpaceManager implements IColorGameSpaceManager {
    private static final String ACTION_AUDIO_RECORD_START = "android.media.ACTION_AUDIO_RECORD_START";
    private static final String ACTION_AUDIO_RECORD_STOP = "android.media.ACTION_AUDIO_RECORD_STOP";
    private static final String ACTION_GAME_DOCK_SERVICE_ACTION = "oppo.intent.action.GameDockService";
    private static final String ACTION_GAME_ENTER = "oppo.intent.action.GAMESPACE_ENTER";
    private static final String ACTION_GAME_INSIDE_BUTTON_SELECT_CLICK = "action_game_inside_button_select_click";
    private static final String ACTION_GAME_INSIDE_BUTTON_START_CLICK = "action_game_inside_button_start_click";
    private static final String ACTION_GAME_INSIDE_BUTTON_START_LONG_CLICK = "action_game_inside_button_start_long_click";
    private static final String ACTION_GAME_OUTER_BUTTON_SELECT_CLICK = "action_game_outer_button_select_click";
    private static final String ACTION_GAME_STOP = "oppo.intent.action.GAMESPACE_STOP";
    private static final String EXTRA_RECORD_ACTION_PID = "android.media.EXTRA_RECORD_ACTION_PID";
    private static final String FEATURE_SUPPORT_JOYSTICK = "oppo.feature.gamejoystick.support";
    private static final String GAME_DOCK_PACKAGE = "com.coloros.gamespaceui";
    private static final String GAME_DOCK_STATE_KEY = "game_dock_panel_state";
    private static final String GAME_DOCK_SWITCH_KEY = "show_gamespace_edge_panel";
    public static final String JOYSTICK_CONFIG_PROPERTY = "oppo.joystick.config";
    public static final int JOYSTICK_DISABLE = 0;
    public static final int JOYSTICK_ENABLE_ROTATION_270 = 2;
    public static final int JOYSTICK_ENABLE_ROTATION_90 = 1;
    public static final int JOYSTICK_INVALID = -1;
    private static final String KEY_ACTION = "key_action";
    private static final long MSG_DELAY_TIME = 200;
    public static final int MSG_DEVICE_UPDATE = 130;
    public static final int MSG_READY_ENTER_GAMEMODE = 100;
    public static final int MSG_SCREEN_OFF = 121;
    public static final int MSG_SCREEN_ON = 120;
    public static final int MSG_SEND_GAME_ENTER = 140;
    public static final int MSG_SEND_GAME_STOP = 141;
    public static final int MSG_STOP_GAMEMODE = 101;
    private static final String PKG_GAME_SPACE_UI = "com.coloros.gamespaceui";
    private static final String SETTING_VALUE_INTERCEPT_KEY = "sys_gamespace_joystick_intercept_key";
    public static final int STATUS_CHANGE_TO_GAME = 1;
    public static final int STATUS_CHANGE_TO_NORMAL = 2;
    public static final int STATUS_NORMAL = 0;
    public static final int WINDOW_DISPLAY_ROTATION_270 = 3;
    private static ColorGameSpaceManager sColorGameSpaceManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private ActivityManagerService mAms = null;
    private OppoBaseNetworkPolicyManagerService mBaseNpms = null;
    private ContentObserver mDufaultInputMethodObserver = new ContentObserver(this.mGsHandler) {
        /* class com.android.server.am.ColorGameSpaceManager.AnonymousClass5 */

        public void onChange(boolean selfChange) {
            ColorGameSpaceManager.this.handleDefatultInputMethodAppId();
        }
    };
    boolean mDynamicDebug = false;
    private RectF mEndRect;
    private boolean mGameDockEnable = true;
    private boolean mGameDockHide;
    private boolean mGameMode = false;
    private long mGameModeEnterTime = 0;
    private IOppoGameSpaceController mGameSpaceController = null;
    private GameSpaceHandler mGsHandler = null;
    private boolean mInterceptGamepadKey;
    private final BroadcastReceiver mJoystickReceiver = new BroadcastReceiver() {
        /* class com.android.server.am.ColorGameSpaceManager.AnonymousClass8 */

        public void onReceive(Context context, Intent intent) {
            int curStatusValue = ColorGameSpaceManager.this.getJoyStickAbleStatus();
            if (-1 != curStatusValue && curStatusValue != 0) {
                ColorGameSpaceManager.this.setJoyStickAbleStatus(((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRotation() == 3 ? 2 : 1);
            }
        }
    };
    private boolean mLongPress = false;
    private Runnable mLongPressRunnable = new Runnable() {
        /* class com.android.server.am.ColorGameSpaceManager.AnonymousClass9 */

        public void run() {
            ColorGameSpaceManager.this.mLongPress = true;
            ColorGameSpaceManager.this.startGameDockAction(ColorGameSpaceManager.ACTION_GAME_INSIDE_BUTTON_START_LONG_CLICK);
        }
    };
    private int mModeStatus = 0;
    private boolean mNeedInterceptSystemGesture;
    private INetworkPolicyManager mNetworkPolicyManager = null;
    private NetworkPolicyManagerService mNpms = null;
    private final BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        /* class com.android.server.am.ColorGameSpaceManager.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int uid = intent.getIntExtra("android.intent.extra.UID", -1);
            if (uid != -1) {
                if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    if (ColorGameSpaceManager.this.DEBUG_SWITCH) {
                        Slog.v("ColorGameSpaceManager", "package change for uid = " + uid);
                    }
                    ColorGameSpaceManagerUtils.getInstance().updateNetWhiteAppIdList();
                    ColorGameSpaceManager.this.checkGameDockEnable(context, intent, action);
                }
            }
        }
    };
    private final BroadcastReceiver mRadioRecordReceiver = new BroadcastReceiver() {
        /* class com.android.server.am.ColorGameSpaceManager.AnonymousClass7 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                try {
                    if (ColorGameSpaceManager.ACTION_AUDIO_RECORD_START.equals(action)) {
                        int pid = intent.getIntExtra(ColorGameSpaceManager.EXTRA_RECORD_ACTION_PID, 0);
                        if (ColorGameSpaceManager.this.DEBUG_SWITCH) {
                            Slog.v("ColorGameSpaceManager", "receive ACTION_AUDIO_RECORD_START pid = " + pid);
                        }
                        if (pid != 0) {
                            ColorGameSpaceManagerUtils.getInstance().addRadioRecordingList(pid);
                            return;
                        }
                        return;
                    }
                } catch (Exception e) {
                    Slog.e("ColorGameSpaceManager", "ColorGameSpaceManager BroadcastReceiver catch exception: " + e.toString());
                    return;
                }
            }
            if (action != null && ColorGameSpaceManager.ACTION_AUDIO_RECORD_STOP.equals(action)) {
                int pid2 = intent.getIntExtra(ColorGameSpaceManager.EXTRA_RECORD_ACTION_PID, 0);
                if (ColorGameSpaceManager.this.DEBUG_SWITCH) {
                    Slog.v("ColorGameSpaceManager", "receive ACTION_AUDIO_RECORD_STOP pid = " + pid2);
                }
                if (pid2 != 0) {
                    ColorGameSpaceManagerUtils.getInstance().removeRadioRecordingList(pid2);
                }
            }
        }
    };
    private int mScreenWidth;
    private long mSendBroadcastDelayTime = 5000;
    private SettingsContentObserver mSettingsContentObserver;
    private RectF mStartRect;
    private boolean mSupportJoystickFeature;
    private BroadcastReceiver mUserSwitchReceiver = new BroadcastReceiver() {
        /* class com.android.server.am.ColorGameSpaceManager.AnonymousClass6 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (ColorGameSpaceManager.this.mDynamicDebug) {
                    Slog.i("ColorGameSpaceManager", "User switched!!--switch to:" + intent.getIntExtra("android.intent.extra.user_handle", 0));
                }
                ColorGameSpaceManagerUtils.getInstance().readGameSpacePkgFile();
                ColorGameSpaceManagerUtils.getInstance().readGsUtilFile();
            }
        }
    };

    public static ColorGameSpaceManager getInstance() {
        if (sColorGameSpaceManager == null) {
            sColorGameSpaceManager = new ColorGameSpaceManager();
        }
        return sColorGameSpaceManager;
    }

    private ColorGameSpaceManager() {
    }

    public boolean getDynamicDebug() {
        return this.mDynamicDebug;
    }

    public void init(ActivityManagerService ams, IColorNetworkPolicyManagerServiceEx npmsEx) {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "init ams = " + ams);
        }
        if (npmsEx != null) {
            this.mNpms = npmsEx.getNetworkPolicyManagerService();
            this.mBaseNpms = (OppoBaseNetworkPolicyManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseNetworkPolicyManagerService.class, this.mNpms);
        }
        this.mAms = ams;
        ColorGameSpaceManagerUtils.getInstance().init(this.mAms);
        HandlerThread thread = new HandlerThread("gameSpaceThread");
        thread.start();
        this.mGsHandler = new GameSpaceHandler(thread.getLooper());
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme(BrightnessConstants.AppSplineXml.TAG_PACKAGE);
        this.mAms.mContext.registerReceiver(this.mPackageReceiver, packageFilter, null, this.mGsHandler);
        registerLogModule();
        handleDefatultInputMethodAppId();
        this.mAms.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("default_input_method"), true, this.mDufaultInputMethodObserver);
        IntentFilter radioRecordFilter = new IntentFilter();
        radioRecordFilter.addAction(ACTION_AUDIO_RECORD_START);
        radioRecordFilter.addAction(ACTION_AUDIO_RECORD_STOP);
        this.mAms.mContext.registerReceiver(this.mRadioRecordReceiver, radioRecordFilter, null, this.mGsHandler);
        this.mAms.mContext.registerReceiver(this.mUserSwitchReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
        initGameDockState(this.mAms.mContext);
        this.mSettingsContentObserver = new SettingsContentObserver(this.mGsHandler);
        this.mSettingsContentObserver.register(this.mAms.mContext.getContentResolver());
        this.mSupportJoystickFeature = this.mAms.mContext.getPackageManager().hasSystemFeature(FEATURE_SUPPORT_JOYSTICK);
        Slog.i("ColorGameSpaceManager", "mSupportJoystickFeature= " + this.mSupportJoystickFeature);
    }

    public void setGameSpaceController(IOppoGameSpaceController controller) {
        this.mGameSpaceController = controller;
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "setGameSpaceController controller " + controller);
        }
    }

    public void handleApplicationSwitch(final String prePkgName, final String nextPkgName, final String prevActivity, final String nextActivity, final boolean isPreMultiApp, final boolean isNextMultiApp, final boolean isPreForFreeForm, final boolean isNextForFreeForm) {
        GameSpaceHandler gameSpaceHandler = this.mGsHandler;
        if (gameSpaceHandler != null) {
            gameSpaceHandler.post(new Runnable() {
                /* class com.android.server.am.ColorGameSpaceManager.AnonymousClass1 */

                public void run() {
                    ColorGameSpaceManager.this.handleApplicationSwitchInner(prePkgName, nextPkgName, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp, isPreForFreeForm, isNextForFreeForm);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleApplicationSwitchInner(String prePkgName, String nextPkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp, boolean isPreForFreeForm, boolean isNextForFreeForm) {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "handleApplicationSwitch   prePkgName " + prePkgName + "  nextPkgName " + nextPkgName);
        }
        if (!ColorGameSpaceManagerUtils.getInstance().isGameSpaceSwtichEnable()) {
            if (isGameSpaceMode()) {
                restoreNormalModeDefault();
                notityApkRestoreNormalMode("swithc disable");
            }
            if (this.mDynamicDebug) {
                Slog.i("ColorGameSpaceManager", "handleApplicationSwitch return");
            }
        } else if ((!inGameSpacePkgList(prePkgName) || !isSpecialActivity(nextActivity)) && (!inGameSpacePkgList(nextPkgName) || !isSpecialActivity(prevActivity))) {
            if ((!inGameSpacePkgList(prePkgName) || !isSecureActivity(nextActivity) || !isNextForFreeForm) && (!inGameSpacePkgList(nextPkgName) || !isSecureActivity(prevActivity) || !isPreForFreeForm)) {
                if (inGameSpacePkgList(nextPkgName) && !isNextMultiApp) {
                    this.mModeStatus = 1;
                } else if (((isPreMultiApp || !inGameSpacePkgList(prePkgName)) && !prePkgName.isEmpty()) || (inGameSpacePkgList(nextPkgName) && !isNextMultiApp)) {
                    this.mModeStatus = 0;
                } else if (this.mModeStatus == 1) {
                    this.mModeStatus = 2;
                } else {
                    this.mModeStatus = 0;
                }
                if (this.mDynamicDebug) {
                    Slog.i("ColorGameSpaceManager", "handleApplicationSwitch mModeStatus = " + this.mModeStatus);
                }
                int i = this.mModeStatus;
                if (!(i == 0 || this.mGameSpaceController == null)) {
                    if (i == 1) {
                        try {
                            boolean isAppExist = checkAppProcessExist(nextPkgName);
                            if (this.DEBUG_SWITCH) {
                                Slog.i("ColorGameSpaceManager", "gameStarting begin isAppExist = " + isAppExist);
                            }
                            setGameMode(true);
                            ColorGameSpaceManagerUtils.getInstance().handleDozeRuleWhite(true, nextPkgName);
                            sendReadyGameModeMsg(nextPkgName);
                            this.mGameSpaceController.gameStarting((Intent) null, nextPkgName, isAppExist);
                            return;
                        } catch (RemoteException e) {
                            restoreNormalModeDefault();
                            this.mGameSpaceController = null;
                            Slog.w("ColorGameSpaceManager", e);
                        }
                    } else if (i == 2) {
                        if (this.DEBUG_SWITCH) {
                            Slog.i("ColorGameSpaceManager", "gameExiting begin ");
                        }
                        setGameMode(false);
                        sendStopGameModeMsg();
                        this.mGameSpaceController.gameExiting(nextPkgName);
                        return;
                    }
                }
                if (isGameSpaceMode()) {
                    restoreNormalModeDefault();
                    notityApkRestoreNormalMode("ensure other path");
                }
            } else if (this.mDynamicDebug) {
                Slog.i("ColorGameSpaceManager", "filter the appunlock activity change");
            }
        } else if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "filter the permission or vpn activity change");
        }
    }

    public boolean isGameSpaceMode() {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "isGameSpaceMode mMode = " + this.mGameMode);
        }
        return this.mGameMode;
    }

    private void setGameMode(boolean mode) {
        this.mGameMode = mode;
    }

    private void sendReadyGameModeMsg(String gamePkg) {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "sendReadyGameModeMsg!");
        }
        if (this.mGsHandler.hasMessages(100)) {
            this.mGsHandler.removeMessages(100);
        }
        sendGameSpaceMessage(100, this.mGameModeEnterTime, gamePkg);
    }

    private void sendStopGameModeMsg() {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "sendStopGameModeMsg!");
        }
        if (this.mGsHandler.hasMessages(101)) {
            this.mGsHandler.removeMessages(101);
        }
        sendGameSpaceEmptyMessage(101, 0);
    }

    public void sendGameSpaceEmptyMessage(int what, long delay) {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "sendGameSpaceEmptyMessage what = " + what + " delay = " + delay);
        }
        this.mGsHandler.sendEmptyMessageDelayed(what, delay);
    }

    public void sendGameSpaceMessage(int what, long delay, String gamePkg) {
        Message msg = this.mGsHandler.obtainMessage(what);
        msg.obj = gamePkg;
        this.mGsHandler.sendMessageDelayed(msg, delay);
    }

    public void handleReadyGameModeMsg(String gamePkg) {
        if (this.DEBUG_SWITCH) {
            Slog.i("ColorGameSpaceManager", "handleReadyGameModeMsg!");
        }
        if (isGameSpaceMode()) {
            ColorGameSpaceManagerUtils.getInstance().handleDozeRuleWhite(true);
            if (ColorGameSpaceManagerUtils.getInstance().isNetProtectEnable(gamePkg)) {
                setGameModeNetworkPoicy(true);
            }
            if (ColorGameSpaceManagerUtils.getInstance().isPerformanceEnable()) {
                setElsaManagerPolicy(true);
            }
            ColorGameSpaceManagerUtils.getInstance().setGameSpaceModeProp(true);
        }
        if (this.mGsHandler.hasMessages(MSG_SEND_GAME_ENTER)) {
            this.mGsHandler.removeMessages(MSG_SEND_GAME_ENTER);
        }
        sendGameSpaceEmptyMessage(MSG_SEND_GAME_ENTER, this.mSendBroadcastDelayTime);
    }

    public void handleStopGameModeMsg() {
        if (this.DEBUG_SWITCH) {
            Slog.i("ColorGameSpaceManager", "handleStopGameModeMsg!");
        }
        ColorGameSpaceManagerUtils.getInstance().handleDozeRuleWhite(false);
        if (ColorGameSpaceManagerUtils.getInstance().isNetProtectEnable("")) {
            setGameModeNetworkPoicy(false);
        }
        if (ColorGameSpaceManagerUtils.getInstance().isPerformanceEnable()) {
            setElsaManagerPolicy(false);
        }
        ColorGameSpaceManagerUtils.getInstance().setGameSpaceModeProp(false);
        if (this.mGsHandler.hasMessages(MSG_SEND_GAME_STOP)) {
            this.mGsHandler.removeMessages(MSG_SEND_GAME_STOP);
        }
        sendGameSpaceEmptyMessage(MSG_SEND_GAME_STOP, this.mSendBroadcastDelayTime);
    }

    public void handleScreenOffMsg() {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "handleScreenOffMsg!");
        }
        if (isGameSpaceMode()) {
            restoreNormalModeDefault();
            notityApkRestoreNormalMode("ScreenOff");
            return;
        }
        this.mGsHandler.removeMessages(100);
    }

    public void handleDeviceUpdateMsg() {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "handleDeviceUpdateMsg!");
        }
        ColorGameSpaceManagerUtils.getInstance().updateNetWhiteAppIdList();
    }

    private void restoreNormalModeDefault() {
        setGameMode(false);
        sendStopGameModeMsg();
    }

    private void notityApkRestoreNormalMode(String reason) {
        if (this.mGameSpaceController != null) {
            try {
                if (this.DEBUG_SWITCH) {
                    Slog.d("ColorGameSpaceManager", "gameExiting for " + reason);
                }
                this.mGameSpaceController.gameExiting("");
            } catch (RemoteException e) {
                this.mGameSpaceController = null;
                Slog.w("ColorGameSpaceManager", e);
            }
        }
    }

    private void setGameModeNetworkPoicy(boolean isGameMode) {
        if (this.mNetworkPolicyManager == null) {
            this.mNetworkPolicyManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy"));
        }
        OppoBaseNetworkPolicyManagerService oppoBaseNetworkPolicyManagerService = this.mBaseNpms;
        if (oppoBaseNetworkPolicyManagerService != null && oppoBaseNetworkPolicyManagerService.getGameSpaceMode() != isGameMode) {
            if (this.DEBUG_SWITCH) {
                Slog.i("ColorGameSpaceManager", "setGameModeNetworkPoicy " + isGameMode);
            }
            this.mBaseNpms.setGameSpaceMode(isGameMode);
        }
    }

    private void setElsaManagerPolicy(boolean isGameMode) {
        if (!ColorGameSpaceManagerUtils.getInstance().isElsaSwitchEnable()) {
            Slog.i("ColorGameSpaceManager", "setElsaManagerPolicy return!");
        } else if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "setElsaManagerPolicy " + isGameMode);
        }
    }

    private boolean checkAppProcessExist(String pkg) {
        boolean result = false;
        if (this.mAms == null || pkg == null || pkg.isEmpty()) {
            return false;
        }
        synchronized (this.mAms) {
            int i = this.mAms.mProcessList.mLruProcesses.size() - 1;
            while (true) {
                if (i >= 0) {
                    ProcessRecord proc = (ProcessRecord) this.mAms.mProcessList.mLruProcesses.get(i);
                    if (proc != null && proc.thread != null && pkg.equals(proc.processName)) {
                        result = true;
                        break;
                    }
                    i--;
                } else {
                    break;
                }
            }
        }
        return result;
    }

    private boolean isVideoActivity(String cpn) {
        return ColorGameSpaceManagerUtils.getInstance().isVideoCpn(cpn);
    }

    private boolean isSecureActivity(String cpn) {
        return ColorGameSpaceManagerUtils.getInstance().isSecureCpn(cpn);
    }

    private boolean isSpecialActivity(String cpn) {
        return ColorGameSpaceManagerUtils.getInstance().isSpecialCpn(cpn);
    }

    public boolean inGameSpacePkgList(String pkg) {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "inGameSpacePkgList pkg = " + pkg);
        }
        return ColorGameSpaceManagerUtils.getInstance().inGameSpacePkgList(pkg);
    }

    public boolean inNetWhitePkgList(String pkg) {
        return ColorGameSpaceManagerUtils.getInstance().inNetWhitePkgList(pkg);
    }

    public boolean inNetWhiteAppIdList(int appId) {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "inNetWhiteAppIdList  appId = " + appId);
        }
        return ColorGameSpaceManagerUtils.getInstance().inNetWhiteAppIdList(appId);
    }

    public List<Integer> getNetWhiteAppIdlist() {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "getNetWhiteAppIdlist  ");
        }
        return ColorGameSpaceManagerUtils.getInstance().getNetWhiteAppIdlist();
    }

    public boolean isBpmEnable() {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "isBpmEnable  ");
        }
        return ColorGameSpaceManagerUtils.getInstance().isBpmEnable();
    }

    public boolean isVideoInterceptEnable() {
        return ColorGameSpaceManagerUtils.getInstance().isVideoInterceptEnable();
    }

    public int getDefaultInputMethodAppId() {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "getDefaultInputMethodAppId  ");
        }
        return ColorGameSpaceManagerUtils.getInstance().getDefatultInputMethodAppId();
    }

    public boolean isDefaultInputMethodAppId(int appId) {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "isDefaultInputMethodAppId  appId = " + appId);
        }
        return ColorGameSpaceManagerUtils.getInstance().getDefatultInputMethodAppId() == appId;
    }

    public void sendDeviceUpdateMessage() {
        if (this.mGsHandler.hasMessages(MSG_DEVICE_UPDATE)) {
            this.mGsHandler.removeMessages(MSG_DEVICE_UPDATE);
        }
        sendGameSpaceEmptyMessage(MSG_DEVICE_UPDATE, MSG_DELAY_TIME);
    }

    public void handleGameSpaceEnterBroadcast() {
        if (this.mDynamicDebug) {
            Slog.v("ColorGameSpaceManager", "sendGameSpaceEnterBroadcast!");
        }
        Intent intent = new Intent(ACTION_GAME_ENTER);
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            activityManagerService.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public void handleGameSpaceStopBroadcast() {
        if (this.mDynamicDebug) {
            Slog.v("ColorGameSpaceManager", "sendGameSpaceStopBroadcast!");
        }
        Intent intent = new Intent(ACTION_GAME_STOP);
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            activityManagerService.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public boolean handleVideoComingNotification(Intent intent, ActivityInfo aInfo) {
        OppoBaseIntent baseIntent = typeCasting(intent);
        boolean result = false;
        if (!isVideoInterceptEnable()) {
            if (this.mDynamicDebug) {
                Slog.v("ColorGameSpaceManager", "handleVideoComingNotification return for switch.");
            }
            return false;
        }
        if (this.mDynamicDebug) {
            Slog.v("ColorGameSpaceManager", "handleVideoComingNotification intent = " + intent);
            Slog.v("ColorGameSpaceManager", "handleVideoComingNotification aInfo = " + aInfo);
        }
        if (!(aInfo == null || aInfo.name == null || !isVideoActivity(aInfo.name))) {
            if (intent != null && baseIntent != null && baseIntent.getIsFromGameSpace() == 1) {
                Slog.v("ColorGameSpaceManager", "IsFromGameSpace retrun!");
                return false;
            } else if (this.mGameSpaceController != null && isGameSpaceMode()) {
                if (isVideoShowInFreeform(aInfo)) {
                    Slog.v("ColorGameSpaceManager", "isVideoShowInFreeform retrun!");
                    return false;
                }
                try {
                    Slog.v("ColorGameSpaceManager", "videoStarting");
                    this.mGameSpaceController.videoStarting(intent, aInfo.packageName);
                    result = true;
                } catch (RemoteException e) {
                    Slog.w("ColorGameSpaceManager", e);
                }
            }
        }
        if (this.mDynamicDebug) {
            Slog.v("ColorGameSpaceManager", "handleVideoComingNotification result = " + result);
        }
        return result;
    }

    private boolean isVideoShowInFreeform(ActivityInfo aInfo) {
        OppoBaseActivityManagerService baseAms;
        ApplicationInfo appInfo;
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService == null || (baseAms = (OppoBaseActivityManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityManagerService.class, activityManagerService)) == null || (appInfo = baseAms.getFreeFormAppInfo()) == null || aInfo.applicationInfo == null || appInfo.uid != aInfo.applicationInfo.uid) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkGameDockEnable(Context context, Intent intent, String action) {
        Uri uri = intent.getData();
        if (uri != null && "com.coloros.gamespaceui".equals(uri.getSchemeSpecificPart())) {
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                this.mGameDockEnable = true;
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                this.mGameDockEnable = false;
            }
            Settings.Global.putInt(context.getContentResolver(), GAME_DOCK_SWITCH_KEY, this.mGameDockEnable ? 1 : 0);
        }
    }

    private void initGameDockState(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        try {
            this.mGameDockEnable = Settings.Global.getInt(contentResolver, GAME_DOCK_SWITCH_KEY) == 1;
        } catch (Settings.SettingNotFoundException e) {
            Settings.Global.putInt(contentResolver, GAME_DOCK_SWITCH_KEY, 1);
            Slog.e("ColorGameSpaceManager", "Settings.SettingNotFoundException for mGameDockEnable.");
        }
        try {
            this.mGameDockHide = Settings.Global.getInt(contentResolver, GAME_DOCK_STATE_KEY) == 0;
        } catch (Settings.SettingNotFoundException e2) {
            Settings.Global.putInt(contentResolver, GAME_DOCK_STATE_KEY, 0);
            Slog.e("ColorGameSpaceManager", "Settings.SettingNotFoundException for mGameDockEnable.");
        }
        Slog.i("ColorGameSpaceManager", "mGameDockEnable = " + this.mGameDockEnable);
        contentResolver.registerContentObserver(Settings.Global.getUriFor(GAME_DOCK_SWITCH_KEY), true, new ContentObserver(this.mGsHandler) {
            /* class com.android.server.am.ColorGameSpaceManager.AnonymousClass2 */

            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                try {
                    ColorGameSpaceManager colorGameSpaceManager = ColorGameSpaceManager.this;
                    boolean z = true;
                    if (Settings.Global.getInt(contentResolver, ColorGameSpaceManager.GAME_DOCK_SWITCH_KEY) != 1) {
                        z = false;
                    }
                    colorGameSpaceManager.mGameDockEnable = z;
                    Slog.i("ColorGameSpaceManager", "GameDockEnable has changed. mGameDockEnable = " + ColorGameSpaceManager.this.mGameDockEnable);
                } catch (Settings.SettingNotFoundException e) {
                    Slog.e("ColorGameSpaceManager", "Settings.SettingNotFoundException for mGameDockEnable.");
                }
            }
        });
        contentResolver.registerContentObserver(Settings.Global.getUriFor(GAME_DOCK_STATE_KEY), true, new ContentObserver(this.mGsHandler) {
            /* class com.android.server.am.ColorGameSpaceManager.AnonymousClass3 */

            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                try {
                    ColorGameSpaceManager.this.mGameDockHide = Settings.Global.getInt(contentResolver, ColorGameSpaceManager.GAME_DOCK_STATE_KEY) == 0;
                    Slog.i("ColorGameSpaceManager", "GameDockEnable has changed. mGameDockEnable = " + ColorGameSpaceManager.this.mGameDockEnable);
                } catch (Settings.SettingNotFoundException e) {
                    Slog.e("ColorGameSpaceManager", "Settings.SettingNotFoundException for mGameDockEnable.");
                }
            }
        });
    }

    private boolean isNeedInterceptSystemGesture() {
        if (!this.mGameMode || !this.mGameDockHide) {
            return false;
        }
        return this.mNeedInterceptSystemGesture;
    }

    private boolean isHideSystemUINavBar() {
        if (!this.mGameMode || !this.mGameDockEnable || !this.mNeedInterceptSystemGesture || this.mAms.mContext.getResources().getConfiguration().orientation == 1) {
            return false;
        }
        return true;
    }

    public boolean requestGameDockIfNecessary() {
        int dockSide = -1;
        try {
            dockSide = WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
        }
        if (dockSide == -1 && this.mGameMode) {
            return isNeedInterceptSystemGesture();
        }
        return false;
    }

    public int getSystemUIFlagAfterGesture(int lastSystemUIFlag) {
        if (isHideSystemUINavBar()) {
            return lastSystemUIFlag | 4358;
        }
        return lastSystemUIFlag;
    }

    public void setSystemGesturePointerPosition(int screenWidth, int statusBarHeight, float x, float y) {
        if (!this.mGameMode || !this.mGameDockEnable) {
            this.mNeedInterceptSystemGesture = false;
            if (this.mStartRect != null) {
                this.mStartRect = null;
            }
            if (this.mEndRect != null) {
                this.mEndRect = null;
                return;
            }
            return;
        }
        RectF rectF = this.mStartRect;
        if (rectF == null) {
            this.mStartRect = new RectF();
            this.mScreenWidth = screenWidth;
            this.mStartRect.set(0.0f, 0.0f, (float) statusBarHeight, (float) statusBarHeight);
        } else if (this.mScreenWidth != screenWidth) {
            rectF.right = (float) statusBarHeight;
        }
        RectF rectF2 = this.mEndRect;
        if (rectF2 == null) {
            this.mEndRect = new RectF();
            this.mScreenWidth = screenWidth;
            this.mEndRect.set((float) (this.mScreenWidth - statusBarHeight), 0.0f, (float) screenWidth, (float) statusBarHeight);
        } else {
            int i = this.mScreenWidth;
            if (i != screenWidth) {
                rectF2.left = (float) (i - statusBarHeight);
            }
        }
        if (this.mStartRect.contains(x, y) || this.mEndRect.contains(x, y)) {
            this.mNeedInterceptSystemGesture = true;
        } else {
            this.mNeedInterceptSystemGesture = false;
        }
        if (this.mDynamicDebug) {
            Slog.v("ColorGameSpaceManager", "setSystemGesturePointerPosition mNeedInterceptSystemGesture = " + this.mNeedInterceptSystemGesture);
        }
    }

    /* access modifiers changed from: private */
    public class GameSpaceHandler extends Handler {
        public GameSpaceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 100) {
                if (i == 101) {
                    ColorGameSpaceManager.this.disableJoystick();
                    ColorGameSpaceManager.this.handleStopGameModeMsg();
                } else if (i == 121) {
                    ColorGameSpaceManager.this.handleScreenOffMsg();
                } else if (i == 130) {
                    ColorGameSpaceManager.this.handleDeviceUpdateMsg();
                } else if (i == 140) {
                    ColorGameSpaceManager.this.handleGameSpaceEnterBroadcast();
                } else if (i == 141) {
                    ColorGameSpaceManager.this.handleGameSpaceStopBroadcast();
                }
            } else if (msg.obj != null) {
                ColorGameSpaceManager.this.handleReadyGameModeMsg((String) msg.obj);
                ColorGameSpaceManager.this.enableJoystick();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDefatultInputMethodAppId() {
        String defaultInput = null;
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            try {
                String inputMethod = Settings.Secure.getString(activityManagerService.mContext.getContentResolver(), "default_input_method");
                if (inputMethod != null) {
                    defaultInput = inputMethod.substring(0, inputMethod.indexOf("/"));
                }
            } catch (Exception e) {
                Slog.e("ColorGameSpaceManager", "Failed to get default input method");
            }
        }
        if (this.DEBUG_SWITCH) {
            Slog.v("ColorGameSpaceManager", "defaultInputMethod " + defaultInput);
        }
        if (defaultInput != null) {
            ColorGameSpaceManagerUtils.getInstance().handleDefatultInputMethodAppId(defaultInput);
        }
    }

    public List<Integer> getDozeRuleWhiteAppIdlist() {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "getDozeRuleWhiteAppIdlist ");
        }
        return ColorGameSpaceManagerUtils.getInstance().getDozeRuleWhiteAppIdlist();
    }

    public boolean inDozeRuleAppIdList(int appId) {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "inDozeRuleAppIdList  ");
        }
        return ColorGameSpaceManagerUtils.getInstance().inDozeRuleAppIdList(appId);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
        ColorGameSpaceManagerUtils.getInstance().setDynamicDebugSwitch();
    }

    public void openLog(boolean on) {
        Slog.i("ColorGameSpaceManager", "#####openlog####");
        Slog.i("ColorGameSpaceManager", "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i("ColorGameSpaceManager", "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i("ColorGameSpaceManager", "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i("ColorGameSpaceManager", "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i("ColorGameSpaceManager", "invoke " + m);
            m.invoke(cls.newInstance(), ColorGameSpaceManager.class.getName());
            Slog.i("ColorGameSpaceManager", "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    public void addPkgToDisplayDeviceList(String pkgName) {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "addPkgToDisplayDeviceList pkgName = " + pkgName);
        }
        ColorGameSpaceManagerUtils.getInstance().addPkgToDisplayDeviceList(pkgName);
    }

    public void removePkgFromDisplayDeviceList(String pkgName) {
        if (this.mDynamicDebug) {
            Slog.i("ColorGameSpaceManager", "removePkgFromDisplayDeviceList pkgName = " + pkgName);
        }
        ColorGameSpaceManagerUtils.getInstance().removePkgFromDisplayDeviceList(pkgName);
    }

    private static OppoBaseIntent typeCasting(Intent intent) {
        if (intent != null) {
            return (OppoBaseIntent) ColorTypeCastingHelper.typeCasting(OppoBaseIntent.class, intent);
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getJoyStickAbleStatus() {
        String curStatus = SystemProperties.get(JOYSTICK_CONFIG_PROPERTY, "0");
        if (curStatus != null) {
            return Integer.parseInt(curStatus) & 15;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setJoyStickAbleStatus(int value) {
        String curStatus = SystemProperties.get(JOYSTICK_CONFIG_PROPERTY, "0");
        if (curStatus != null) {
            SystemProperties.set(JOYSTICK_CONFIG_PROPERTY, String.valueOf((Integer.parseInt(curStatus) & -16) | value));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableJoystick() {
        if (this.DEBUG_SWITCH) {
            Slog.i("ColorGameSpaceManager", "game mode ready,enableJoystick");
        }
        int curStatusValue = getJoyStickAbleStatus();
        if (-1 != curStatusValue && curStatusValue == 0) {
            IntentFilter joystickFilter = new IntentFilter();
            joystickFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            this.mAms.mContext.registerReceiver(this.mJoystickReceiver, joystickFilter);
            setJoyStickAbleStatus(((WindowManager) this.mAms.mContext.getSystemService("window")).getDefaultDisplay().getRotation() == 3 ? 2 : 1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableJoystick() {
        if (this.DEBUG_SWITCH) {
            Slog.i("ColorGameSpaceManager", "stop game mode,disableJoystick");
        }
        int curStatusValue = getJoyStickAbleStatus();
        if (-1 != curStatusValue && curStatusValue != 0) {
            try {
                this.mAms.mContext.unregisterReceiver(this.mJoystickReceiver);
                setJoyStickAbleStatus(0);
            } catch (IllegalArgumentException e) {
                if (this.DEBUG_SWITCH) {
                    Slog.w("ColorGameSpaceManager", "joystick has already disabled");
                }
            }
        }
    }

    private Intent getGameDockServiceIntent() {
        Intent targetIntent = new Intent(ACTION_GAME_DOCK_SERVICE_ACTION);
        targetIntent.setPackage("com.coloros.gamespaceui");
        return targetIntent;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startGameDockAction(String action) {
        try {
            Intent intent = getGameDockServiceIntent();
            intent.putExtra(KEY_ACTION, action);
            this.mAms.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isInterceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        if (this.mAms == null || !this.mInterceptGamepadKey || !this.mSupportJoystickFeature) {
            return false;
        }
        boolean isInterceptKey = false;
        if (isGameSpaceMode()) {
            if (109 == event.getKeyCode()) {
                isInterceptKey = true;
                if (event.getRepeatCount() == 0 && event.getAction() == 0) {
                    Slog.i("ColorGameSpaceManager", "InterceptKey " + event);
                    startGameDockAction(ACTION_GAME_INSIDE_BUTTON_SELECT_CLICK);
                }
            } else if (108 == event.getKeyCode()) {
                Slog.i("ColorGameSpaceManager", "InterceptKey " + event);
                isInterceptKey = true;
                if (event.getAction() == 0) {
                    Slog.i("ColorGameSpaceManager", "InterceptKey event.getRepeatCount " + event.getRepeatCount());
                    if (event.getRepeatCount() == 0) {
                        this.mLongPress = false;
                        this.mGsHandler.postDelayed(this.mLongPressRunnable, (long) ViewConfiguration.getLongPressTimeout());
                    } else {
                        event.getRepeatCount();
                    }
                } else if (event.getAction() == 1) {
                    this.mGsHandler.removeCallbacks(this.mLongPressRunnable);
                    if (!this.mLongPress) {
                        startGameDockAction(ACTION_GAME_INSIDE_BUTTON_START_CLICK);
                    }
                    this.mLongPress = false;
                }
            }
        } else if (109 == event.getKeyCode()) {
            Slog.i("ColorGameSpaceManager", "InterceptKey " + event);
            isInterceptKey = true;
            if (event.getRepeatCount() == 0 && event.getAction() == 0) {
                startGameDockAction(ACTION_GAME_OUTER_BUTTON_SELECT_CLICK);
            }
        }
        if (isInterceptKey) {
            Slog.i("ColorGameSpaceManager", " InterceptKey " + event);
        }
        return isInterceptKey;
    }

    private class SettingsContentObserver extends ContentObserver {
        public SettingsContentObserver(Handler handler) {
            super(handler);
        }

        public void register(ContentResolver contentResolver) {
            contentResolver.registerContentObserver(Settings.System.getUriFor(ColorGameSpaceManager.SETTING_VALUE_INTERCEPT_KEY), false, this);
        }

        public void unregister(ContentResolver contentResolver) {
            contentResolver.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                try {
                    Slog.i("ColorGameSpaceManager", "SettingsContentObserver onChange  uri = " + uri);
                    if (uri.equals(Settings.System.getUriFor(ColorGameSpaceManager.SETTING_VALUE_INTERCEPT_KEY))) {
                        int enable = Settings.System.getInt(ColorGameSpaceManager.this.mAms.mContext.getContentResolver(), ColorGameSpaceManager.SETTING_VALUE_INTERCEPT_KEY);
                        ColorGameSpaceManager colorGameSpaceManager = ColorGameSpaceManager.this;
                        boolean z = true;
                        if (enable != 1) {
                            z = false;
                        }
                        colorGameSpaceManager.mInterceptGamepadKey = z;
                        Slog.i("ColorGameSpaceManager", "mInterceptGamepadKey = " + ColorGameSpaceManager.this.mInterceptGamepadKey);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
