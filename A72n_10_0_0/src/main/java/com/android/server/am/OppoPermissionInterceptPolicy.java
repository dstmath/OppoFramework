package com.android.server.am;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.KeyguardManager;
import android.app.OppoActivityManager;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.OppoPermissionManager;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.server.devicepolicy.OppoDevicePolicyUtils;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.policy.OppoPhoneWindowManager;
import com.android.server.wm.OppoBaseWindowManagerService;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import com.color.util.ColorTypeCastingHelper;
import com.oppo.app.IOppoPermissionRecordController;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;

public class OppoPermissionInterceptPolicy {
    private static final String ACTION_DIALOG_SERVICE = "coloros.safecenter.permission.PERMISSION_DIALOG_SERVICE";
    private static final String ACTION_RECORD_SERVICE = "oppo.intent.action.PERMISSSION_RECORD_SERVICE";
    private static final String ALERT_PERMISSION_APPS = "alert_permission_apps.xml";
    private static final String ALERT_PERMISSION_DATA_DIR = "//data//oppo//coloros//permission//";
    private static final String COLOR_DEFAULT_MMS_REGIONS = "color_default_mms_regions";
    private static final int COLUMN_ACCEPT = 2;
    public static final String COLUMN_ACCEPT_STR = "accept";
    private static final int COLUMN_ID = 0;
    public static final String COLUMN_ID_STR = "_id";
    private static final int COLUMN_PKG_NAME = 1;
    public static final String COLUMN_PKG_NAME_STR = "pkg_name";
    private static final int COLUMN_PROMPT = 4;
    public static final String COLUMN_PROMPT_STR = "prompt";
    private static final int COLUMN_REJECT = 3;
    public static final String COLUMN_REJECT_STR = "reject";
    public static final String COLUMN_STATE_STR = "state";
    private static final int COLUMN_TRUST = 6;
    public static final String COLUMN_TRUST_STR = "trust";
    private static final int DATA_NUM_THRESHOLD = 50;
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final ArrayList<String> DEFAULT_CHARGE_SKIP_PACKAGE = new ArrayList<>(Arrays.asList("com.tsinghua.tairapitest", "com.tsinghua.tairapitestm"));
    private static final String DEFAULT_SMS_REGEX = "[a-zA-Z0-9#$%&!'()*+,-./:;<=>?@\\[\\]^_`{|}~ ]{10,}";
    private static final long DELAY_THREE = 3000;
    private static final int DO_CHECK_PERMISSION = 0;
    private static final int ECHO_TO_KERNEL_NODES = 3;
    private static final String GAME_FILTER_FILE_NAME = "safe_marketfilter_list.xml";
    private static final String[] INITIAL_REGIONS = {"AU", "NZ"};
    private static final String KEYGUARD_PACKAGE_NAME = "com.android.systemui";
    private static final String KEY_CTSVERSION_PROPERTIES = "persist.oppo.ctsversion";
    private static final String KEY_PACKAGE_NAME = "pkgName";
    private static final String KEY_PERMISSION_NAME = "permissionName";
    private static final String KEY_PERMISSION_PROPERTIES = "persist.sys.permission.enable";
    private static final String KEY_PERMISSION_VALUE = "permissionValue";
    private static final long MILLIS = 1000;
    private static final int MSG_PERMISSION_DIALOG_GET_RESULT = 211;
    private static final int MSG_PERMISSION_DIALOG_SEND_RESULT = 103;
    private static final int MSG_PERMISSION_DIALOG_SHOW = 101;
    private static final int PARAMETER_FOUR = 4;
    private static final int PARAMETER_ONE = 1;
    private static final int PARAMETER_THREE = 3;
    private static final int PARAMETER_TWO = 2;
    private static final String PERMISSION_ACCESS_MEDIA_PROVIDER = "android.permission.ACCESS_MEDIA_PROVIDER";
    private static final String PERMISSION_COMPONENT_SAFE = "oppo.permission.OPPO_COMPONENT_SAFE";
    private static final String PERMISSION_PACKGE_NAME = "com.coloros.securitypermission";
    private static final String PROPERTY_OPPOROM = "ro.build.version.opporom";
    private static final String SMS_SEPARATOR = "#";
    private static final String STATISTIC_ACTION = "coloros.safecenter.permission.STATISTIC_FRAMEWORK";
    private static final String STATISTIC_ACTION_PRIVACY_PROTECT = "oppo.intent.action.PERMISSION_ACTION_DETAIL";
    private static final String STATISTIC_CHECKED = "checked";
    private static final String STATISTIC_DATA = "data";
    private static final String STATISTIC_REJECT = "reject";
    private static final String STATISTIC_TYPE = "type";
    private static final int STATISTIC_TYPE_CHARGE_SMS = 1;
    private static final int STATISTIC_TYPE_NORMAL = 0;
    private static final int STATISTIC_TYPE_NORMAL_SMS = 2;
    private static final String SWITCH_OFF = "off";
    private static final String SWITCH_ON = "on";
    private static final String SWITCH_SKIP_OPPO = "skip_oppo";
    private static final String TAG = "OppoPermissionInterceptPolicy";
    private static final int UPDATE_PERMISSION_CHOICE = 1;
    private static final String UPLOAD_LOGTAG = "20089";
    private static final String UPLOAD_LOG_EVENTID = "ScreenOffPermissionRequestEvent";
    private static final String VALUE_ACCEPT = "ACCEPT";
    private static final int VALUE_BG_CAMERA_SKIP_PKG = 1;
    private static final String VALUE_PROMPT = "PROMPT";
    private static final String VALUE_REJECT = "REJECT";
    private static final String VERSION_ALPHA = "alpha";
    private static final String VERSION_BETA = "beta";
    private static final long WAIT_LONG = 50000;
    private static final long WATI_SHORT = 20000;
    private static final String XML_TAG_ALERT = "alert";
    private static final String XML_TAG_ALLOW_BACKGROUND = "allowbackground";
    private static final String XML_TAG_BACKGROUND_CAMERA_SKIP = "background_camera_skip";
    private static final String XML_TAG_BACKGROUND_SKIP = "background_skip";
    private static final String XML_TAG_DCIMPROTECT_ENABLED = "dcimprotect_enabled";
    private static final String XML_TAG_DEVICE_OWNER_SIGNATURE_LIST = "device_owner_signature";
    private static final String XML_TAG_DEVICE_OWNER_SWITCH = "device_owner_switch";
    private static final String XML_TAG_GAME_NAME = "whitelist";
    private static final String XML_TAG_GAME_SUFFIX = "keyword";
    private static final String XML_TAG_OPPO_DCIM_RECYCLE_WHITE = "dcimprotect_recycle_white";
    private static final String XML_TAG_PERM_RECORD_SWITCH = "perm_record_switch";
    private static final String XML_TAG_REJECT_DIALOG_PERMISSION = "rejectdialog_permission";
    private static final String XML_TAG_SHELL_REVOKE_PERMISSION = "shell_revoke_permission";
    private static final String XML_TAG_SMS_CHARGE_SWITCH = "smschargeswitch";
    private static final String XML_TAG_SMS_CONFIRM_SKIP = "sms_confirm_skip";
    private static final String XML_TAG_SMS_CONTENT = "sms";
    private static final String XML_TAG_SMS_CONTENT_SKIP = "sms_skip";
    private static final String XML_TAG_SMS_NUMBER = "number";
    private static final String XML_TAG_SMS_NUMBER_SKIP = "number_skip";
    private static final String XML_TAG_SMS_PROMPT_SWITCH = "smspromptswitch";
    private static List<String> sAlertAppList = new ArrayList();
    private static AlertDataFileListener sAlertAppListener;
    private static Boolean sAllowBackgroundRequest;
    public static boolean sAllowPermissionRecord;
    private static Map<String, Integer> sBackgroundCameraSkipPkgs = new HashMap();
    private static List<String> sBackgroundSkipList = new ArrayList();
    private static List<String> sBackgroundSkipPermissions = new ArrayList();
    private static List<String> sDeviceOwnerMd5List = new ArrayList();
    private static AlertDataFileListener sGameAppListener;
    private static List<String> sGameNameList = new ArrayList();
    private static List<String> sGameSuffixList = new ArrayList();
    private static boolean sIsBusinessCustom = DEBUG;
    private static boolean sIsBussinessNoAccountDialog = DEBUG;
    protected static Boolean sIsCtaVersion;
    private static boolean sIsExVersion = DEBUG;
    private static volatile boolean sIsPermissionInterceptEnabled = SystemProperties.getBoolean(KEY_PERMISSION_PROPERTIES, !SystemProperties.getBoolean(KEY_CTSVERSION_PROPERTIES, (boolean) DEBUG));
    private static boolean sIsSpecialCarrier = DEBUG;
    private static LinkedList<PermissionCheckingMsg> sPermissionCheckMsgList = new LinkedList<>();
    private static OppoPermissionInterceptPolicy sPermissionInterceptPolicy;
    private static List<String> sPermissionsPrompt;
    private static Bundle sRecordData = null;
    private static int sRecordMsgCode = 0;
    private static List<String> sRegexSms = new ArrayList();
    private static List<String> sRegexSmsNumber = new ArrayList();
    private static List<String> sRegexSmsNumberSkip = new ArrayList();
    private static List<String> sRegexSmsSkip = new ArrayList();
    private static boolean sRejectChargeSms = DEBUG;
    private static List<String> sRejectDialogPermissionList = new ArrayList();
    private static List<String> sSMSConfirmSkipList = new ArrayList();
    private static List<String> sShellRevokedPermissions = new ArrayList();
    private static String sSmsChargeSwitch = SWITCH_SKIP_OPPO;
    private static String sSmsPromptSwitch = SWITCH_ON;
    private String mCallNumber = null;
    private ConcurrentHashMap<Integer, ContentProviderClient> mContentProviderMap = new ConcurrentHashMap<>();
    private final Context mContext;
    private IOppoPermissionRecordController mController = null;
    private String mCurrentCountry;
    private String mCurrentLanguage;
    private Messenger mDialogService;
    private HandlerThread mHandlerThread = new HandlerThread("PermissionThread", 10);
    private KeyguardManager mKeyguardManager;
    private Messenger mMessenger;
    private String[] mPackageNameList = new String[DATA_NUM_THRESHOLD];
    private Handler mPendingMsgHandler;
    private HandlerThread mPendingMsgThread = new HandlerThread("PermissionMsgPendingThread", 10);
    private int mPermissionCount = 0;
    private Handler mPermissionHandler;
    private final Object mPermissionLock = new Object();
    private String[] mPermissionNameList = new String[DATA_NUM_THRESHOLD];
    private Handler mPermissionRecordHandler;
    private HandlerThread mPermissionRecordThread;
    private final IPackageManager mPm;
    private PowerManager mPowerManager;
    private PermissionCheckingMsg mRecordPcm = null;
    private int[] mResultList = new int[DATA_NUM_THRESHOLD];
    private CheckBox mSaveCheckBox;
    private ServiceConnection mServConnection = new ServiceConnection() {
        /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass7 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(OppoPermissionInterceptPolicy.TAG, "onServiceConnected");
            OppoPermissionInterceptPolicy.this.mDialogService = new Messenger(service);
            if (OppoPermissionInterceptPolicy.sRecordMsgCode != 0 && OppoPermissionInterceptPolicy.sRecordData != null) {
                OppoPermissionInterceptPolicy.this.sendDialogMsg(OppoPermissionInterceptPolicy.sRecordMsgCode, OppoPermissionInterceptPolicy.sRecordData);
                int unused = OppoPermissionInterceptPolicy.sRecordMsgCode = 0;
                Bundle unused2 = OppoPermissionInterceptPolicy.sRecordData = null;
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(OppoPermissionInterceptPolicy.TAG, "onServiceDisconnected");
            OppoPermissionInterceptPolicy.this.mDialogService = null;
            OppoPermissionInterceptPolicy oppoPermissionInterceptPolicy = OppoPermissionInterceptPolicy.this;
            oppoPermissionInterceptPolicy.bindDialogService(oppoPermissionInterceptPolicy.mContext);
            if (OppoPermissionInterceptPolicy.this.mContentProviderMap != null) {
                try {
                    for (ContentProviderClient providerClient : OppoPermissionInterceptPolicy.this.mContentProviderMap.values()) {
                        providerClient.close();
                        Log.d(OppoPermissionInterceptPolicy.TAG, "close client:" + providerClient);
                    }
                    OppoPermissionInterceptPolicy.this.mContentProviderMap.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private final ActivityManagerService mService;
    private String mSmsContent = null;
    private String mSmsDestination = null;
    private long[] mTimeList = new long[DATA_NUM_THRESHOLD];
    private Handler mTimerHandler;
    private HandlerThread mTimerThread = new HandlerThread("PermissionTimerThread", 10);

    static {
        Boolean valueOf = Boolean.valueOf((boolean) DEBUG);
        sIsCtaVersion = valueOf;
        sAllowBackgroundRequest = valueOf;
    }

    /* access modifiers changed from: package-private */
    public class PermissionCheckingMsg {
        OppoPermissionCallback mCallback;
        String mContentUri = null;
        boolean mIsBackground;
        String mPermission;
        long mPermissionMask;
        PackagePermission mPkgPm;
        ProcessRecord mPr;
        String mSelection = null;
        String mSelectionArgs = null;
        int mToken;
        int mUid;

        PermissionCheckingMsg() {
        }

        public boolean equals(PermissionCheckingMsg o) {
            String str = this.mPermission;
            if (str == null || o == null || !str.equals(o.mPermission) || this.mUid != o.mUid) {
                return OppoPermissionInterceptPolicy.DEBUG;
            }
            return true;
        }

        public String toString() {
            return "[mPermission=" + this.mPermission + ", mPr=" + this.mPr + ", mUid=" + this.mUid + ", mPermissionMask=" + this.mPermissionMask + ", mToken=" + this.mToken + ", mCallback=" + this.mCallback + "]";
        }
    }

    public static OppoPermissionInterceptPolicy getInstance(ActivityManagerService service) {
        if (sPermissionInterceptPolicy == null) {
            synchronized (OppoPermissionInterceptPolicy.class) {
                if (sPermissionInterceptPolicy == null) {
                    sPermissionInterceptPolicy = new OppoPermissionInterceptPolicy(service);
                }
            }
        }
        return sPermissionInterceptPolicy;
    }

    private OppoPermissionInterceptPolicy(ActivityManagerService service) {
        this.mService = service;
        this.mContext = service.mContext;
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.system.cmcc.test")) {
            sIsPermissionInterceptEnabled = DEBUG;
        }
        sIsBusinessCustom = this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom");
        sIsBussinessNoAccountDialog = this.mContext.getPackageManager().hasSystemFeature("oppo.settings.account.dialog.disallow");
        sIsSpecialCarrier = isSpecialCarrier(this.mContext);
        SystemProperties.set(KEY_PERMISSION_PROPERTIES, String.valueOf(sIsPermissionInterceptEnabled));
        this.mPm = AppGlobals.getPackageManager();
        this.mCurrentLanguage = Locale.getDefault().getLanguage();
        this.mCurrentCountry = Locale.getDefault().getCountry();
        sPermissionsPrompt = Arrays.asList(this.mContext.getResources().getStringArray(201786381));
        this.mHandlerThread.start();
        this.mPermissionHandler = new Handler(this.mHandlerThread.getLooper()) {
            /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass1 */

            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    synchronized (OppoPermissionInterceptPolicy.this) {
                        UpdateMsgDate umd = (UpdateMsgDate) msg.obj;
                        PackagePermission pkgPm = OppoPermissionInterceptPolicy.this.queryPackagePermissionsAsUser(umd.mPackageName, umd.mUserId);
                        long permissionMask = OppoPermissionInterceptPolicy.this.getPermissionMask(umd.mPermission);
                        ProcessRecord pr = OppoPermissionInterceptPolicy.this.getProcessForPackageName(umd.mPackageName);
                        OppoBaseProcessRecord basePr = OppoPermissionInterceptPolicy.typeCasting(pr);
                        if (pkgPm != null) {
                            OppoPermissionInterceptPolicy.this.changePermissionChoice(pkgPm, permissionMask, umd.mChoice);
                            OppoPermissionInterceptPolicy.this.savePermissionChoiceAsUser(pkgPm, umd.mUserId);
                        }
                        if (pr != null) {
                            if (basePr != null) {
                                basePr.isSelected = (int) (((long) basePr.isSelected) & (~permissionMask));
                            }
                            if (!(pkgPm == null || basePr == null)) {
                                basePr.mPackagePermission = pkgPm;
                                basePr.mPersistPackagePermission = pkgPm;
                            }
                        }
                        if (OppoPermissionInterceptPolicy.DEBUG) {
                            Log.d(OppoPermissionInterceptPolicy.TAG, "UPDATE_PERMISSION_CHOICE, pkgPm=" + pkgPm + ",perm:" + umd.mPermission);
                        }
                    }
                }
            }
        };
        this.mPendingMsgThread.start();
        this.mPendingMsgHandler = new Handler(this.mPendingMsgThread.getLooper()) {
            /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass2 */

            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                        if (OppoPermissionInterceptPolicy.DEBUG) {
                            Log.d(OppoPermissionInterceptPolicy.TAG, "handleMessage DO_CHECK_PERMISSION,  size=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size() + ", sPermissionCheckMsgList=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList);
                        }
                        if (OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size() >= 1) {
                            PermissionCheckingMsg pcm = (PermissionCheckingMsg) OppoPermissionInterceptPolicy.sPermissionCheckMsgList.removeFirst();
                            if (pcm != null) {
                                try {
                                    OppoPermissionInterceptPolicy.this.mRecordPcm = pcm;
                                    boolean isScreenOn = OppoPermissionInterceptPolicy.this.isScreenOn();
                                    if (pcm.mPermission != null && !pcm.mPermission.startsWith(OppoPermissionInterceptPolicy.PERMISSION_ACCESS_MEDIA_PROVIDER) && ((OppoPermissionInterceptPolicy.this.mKeyguardManager != null && OppoPermissionInterceptPolicy.this.mKeyguardManager.inKeyguardRestrictedInputMode()) || !isScreenOn)) {
                                        String currentFocus = OppoPermissionInterceptPolicy.this.getCurrentFocus();
                                        if (currentFocus.equals("com.android.systemui") || currentFocus.equals(OppoPermissionInterceptPolicy.PERMISSION_PACKGE_NAME) || !currentFocus.equals(pcm.mPr.info.packageName) || !isScreenOn) {
                                            OppoPermissionInterceptPolicy.this.notifyWaitingApp(OppoPermissionInterceptPolicy.this.mRecordPcm, 1);
                                            Log.d(OppoPermissionInterceptPolicy.TAG, "behind keyguard, reject it! app is " + pcm.mPr.info.packageName);
                                            return;
                                        }
                                    }
                                    Bundle bundle = new Bundle();
                                    bundle.putString(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NAME, pcm.mPr.info.packageName);
                                    bundle.putString("permission", pcm.mPermission);
                                    bundle.putString("smsContent", OppoPermissionInterceptPolicy.this.mSmsContent);
                                    bundle.putString("smsDestination", OppoPermissionInterceptPolicy.this.mSmsDestination);
                                    bundle.putString("callNumber", OppoPermissionInterceptPolicy.this.mCallNumber);
                                    bundle.putBoolean("isBackground", pcm.mIsBackground);
                                    bundle.putInt(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, pcm.mUid);
                                    if (pcm.mContentUri != null) {
                                        bundle.putString("contentUri", pcm.mContentUri);
                                    }
                                    if (pcm.mSelection != null) {
                                        bundle.putString("selection", pcm.mSelection);
                                    }
                                    if (pcm.mSelectionArgs != null) {
                                        bundle.putString("selectionArgs", pcm.mSelectionArgs);
                                    }
                                    if (!pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) || !OppoPermissionInterceptPolicy.this.isChargeSmsByPkg(pcm.mPr.info.packageName, OppoPermissionInterceptPolicy.this.mSmsContent, OppoPermissionInterceptPolicy.this.mSmsDestination)) {
                                        bundle.putBoolean("isChargeSms", OppoPermissionInterceptPolicy.DEBUG);
                                    } else {
                                        bundle.putBoolean("isChargeSms", true);
                                    }
                                    if (OppoPermissionInterceptPolicy.sRejectDialogPermissionList != null && OppoPermissionInterceptPolicy.sRejectDialogPermissionList.contains(pcm.mPermission)) {
                                        if (pcm.mPkgPm == null || (pcm.mPkgPm.mReject & pcm.mPermissionMask) == 0) {
                                            bundle.putBoolean("isRectDialog", OppoPermissionInterceptPolicy.DEBUG);
                                        } else {
                                            bundle.putBoolean("isRejectDialog", true);
                                        }
                                    }
                                    if (OppoPermissionInterceptPolicy.DEBUG) {
                                        Log.v(OppoPermissionInterceptPolicy.TAG, "let's show permission dialog");
                                    }
                                    OppoPermissionInterceptPolicy.this.sendDialogMsg(101, bundle);
                                    OppoPermissionInterceptPolicy.this.mCallNumber = null;
                                } catch (Exception e) {
                                    Log.e(OppoPermissionInterceptPolicy.TAG, "show permission dialog error.");
                                    e.printStackTrace();
                                }
                                try {
                                    if (pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.wait(OppoPermissionInterceptPolicy.WAIT_LONG);
                                    } else if (pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_CAMERA)) {
                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.wait(OppoPermissionInterceptPolicy.WAIT_LONG);
                                    } else {
                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.wait(OppoPermissionInterceptPolicy.WATI_SHORT);
                                    }
                                } catch (InterruptedException e2) {
                                }
                            }
                        }
                    }
                }
            }
        };
        this.mTimerThread.start();
        this.mTimerHandler = new Handler(this.mTimerThread.getLooper());
        this.mMessenger = new Messenger(new Handler(this.mHandlerThread.getLooper()) {
            /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass3 */

            public void handleMessage(Message msg) {
                if (msg.what == OppoPermissionInterceptPolicy.MSG_PERMISSION_DIALOG_GET_RESULT && OppoPermissionInterceptPolicy.this.mRecordPcm != null) {
                    Bundle bundle = msg.getData();
                    int res = bundle.getInt("res", 0);
                    boolean save = bundle.getBoolean("save", OppoPermissionInterceptPolicy.DEBUG);
                    OppoPermissionInterceptPolicy oppoPermissionInterceptPolicy = OppoPermissionInterceptPolicy.this;
                    oppoPermissionInterceptPolicy.processPermission(oppoPermissionInterceptPolicy.mRecordPcm, res, save);
                    if (OppoPermissionInterceptPolicy.DEBUG) {
                        Log.v(OppoPermissionInterceptPolicy.TAG, "MSG_PERMISSION_DIALOG_GET_RESULT " + OppoPermissionInterceptPolicy.this.mRecordPcm.mPr.info.packageName + " , " + res + " , " + save);
                    }
                    if (OppoPermissionInterceptPolicy.this.mRecordPcm.mPermission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                        OppoPermissionInterceptPolicy oppoPermissionInterceptPolicy2 = OppoPermissionInterceptPolicy.this;
                        if (oppoPermissionInterceptPolicy2.isChargeSmsByPkg(oppoPermissionInterceptPolicy2.mRecordPcm.mPr.info.packageName, OppoPermissionInterceptPolicy.this.mSmsContent, OppoPermissionInterceptPolicy.this.mSmsDestination)) {
                            boolean unused = OppoPermissionInterceptPolicy.sRejectChargeSms = true;
                            OppoPermissionInterceptPolicy.this.mPermissionHandler.postDelayed(new Runnable() {
                                /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass3.AnonymousClass1 */

                                public void run() {
                                    boolean unused = OppoPermissionInterceptPolicy.sRejectChargeSms = OppoPermissionInterceptPolicy.DEBUG;
                                }
                            }, OppoPermissionInterceptPolicy.DELAY_THREE);
                        }
                    }
                }
            }
        });
        sIsCtaVersion = Boolean.valueOf(this.mContext.getPackageManager().hasSystemFeature("oppo.cta.support"));
        sIsExVersion = this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
        sAllowPermissionRecord = !sIsExVersion;
        List<String> list = sBackgroundSkipList;
        if (list != null && list.isEmpty()) {
            sBackgroundSkipList = new ArrayList(Arrays.asList("com.sogou.speech.offlineservice", "com.google.android.gms", "com.google.android.gsf", "com.google.android.gsf.login", "com.google.android.syncadapters.calendar", "com.google.android.syncadapters.contacts", "com.appstar.callrecorder", "com.jiochat.jiochatapp", "com.nll.acr", "com.tencent.mobileqq", ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.coloros.screenrecorder", "com.coloros.personalassistant"));
        }
        sBackgroundSkipPermissions.add(OppoPermissionConstants.PERMISSION_CALL_PHONE);
        sBackgroundSkipPermissions.add(OppoPermissionConstants.PERMISSION_RECORD_AUDIO);
        initAlertAppList(ALERT_PERMISSION_APPS);
        initAlertAppList(GAME_FILTER_FILE_NAME);
        sAlertAppListener = new AlertDataFileListener("//data//oppo//coloros//permission//alert_permission_apps.xml");
        sAlertAppListener.startWatching();
        sGameAppListener = new AlertDataFileListener("//data//oppo//coloros//permission//safe_marketfilter_list.xml");
        sGameAppListener.startWatching();
        bindDialogService(this.mContext);
        Map<String, Integer> map = sBackgroundCameraSkipPkgs;
        if (map != null && map.isEmpty()) {
            sBackgroundCameraSkipPkgs.put(ColorStartingWindowContants.WECHAT_PACKAGE_NAME, 1);
            sBackgroundCameraSkipPkgs.put("com.tencent.mobileqq", 1);
            sBackgroundCameraSkipPkgs.put("jp.naver.line.android", 1);
            sBackgroundCameraSkipPkgs.put("com.viber.voip", 1);
            sBackgroundCameraSkipPkgs.put("com.whatsapp", 1);
        }
        List<String> list2 = sRejectDialogPermissionList;
        if (list2 != null && list2.isEmpty()) {
            sRejectDialogPermissionList = new ArrayList(Arrays.asList(OppoPermissionConstants.PERMISSION_CAMERA, OppoPermissionConstants.PERMISSION_RECORD_AUDIO));
        }
        List<String> list3 = sSMSConfirmSkipList;
        if (list3 != null) {
            list3.add("com.yc.sqt");
        }
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        OppoDCIMProtectManager.getInstance().startDCIMProtectEventObserving(this.mContext);
    }

    public void preparePermRecordThreadIfNeed() {
        if (this.mPermissionRecordThread == null) {
            this.mPermissionRecordThread = new HandlerThread("PermissionRecordThread", 10);
            this.mPermissionRecordThread.start();
        }
        this.mPermissionRecordHandler = new Handler(this.mPermissionRecordThread.getLooper());
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x010b  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x014a  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x015a  */
    public int checkPermissionForProc(String permission, int pid, int uid, int token, OppoPermissionCallback callback) {
        String selectionArgs;
        String selection;
        String contentUri;
        String permission2;
        int result;
        Object lock;
        Throwable th;
        String selection2 = null;
        String selectionArgs2 = null;
        if (permission.startsWith(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
            if (!permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                this.mSmsContent = permission.substring(OppoPermissionConstants.PERMISSION_SEND_SMS.length(), permission.length());
                int separator = this.mSmsContent.lastIndexOf(SMS_SEPARATOR);
                if (separator != -1) {
                    this.mSmsDestination = this.mSmsContent.substring(separator + 1);
                    this.mSmsContent = this.mSmsContent.substring(0, separator);
                }
                permission2 = OppoPermissionConstants.PERMISSION_SEND_SMS;
                contentUri = null;
                selection = null;
                selectionArgs = null;
                if (!DEBUG) {
                    Log.d(TAG, "checkPermissionForProc, permission=" + permission2 + ", uid=" + uid + ",  pid=" + pid + ", token=" + token + ", callback=" + callback);
                }
                if (!UserHandle.isCore(uid) || !sIsPermissionInterceptEnabled) {
                    return 3;
                }
                try {
                    Object lock2 = new Object();
                    synchronized (lock2) {
                        try {
                            lock = lock2;
                            try {
                                CheckPermissionRunnable queryRunnable = new CheckPermissionRunnable(lock2, permission2, pid, uid, token, callback);
                                if (permission2.equals(PERMISSION_ACCESS_MEDIA_PROVIDER)) {
                                    queryRunnable.setContentUri(contentUri);
                                    queryRunnable.setSelection(selection);
                                    queryRunnable.setSelectionArgs(selectionArgs);
                                }
                                this.mPermissionHandler.post(queryRunnable);
                                if (permission2.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                                    lock.wait(WAIT_LONG);
                                } else if (permission2.equals(OppoPermissionConstants.PERMISSION_CAMERA)) {
                                    lock.wait(WAIT_LONG);
                                } else {
                                    lock.wait(WATI_SHORT);
                                }
                                result = queryRunnable.mRes;
                                try {
                                    if (DEBUG) {
                                        Log.d(TAG, "checkPermissionForProc, return " + result);
                                    }
                                    return result;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            lock = lock2;
                            throw th;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result = 3;
                }
            } else {
                this.mSmsContent = null;
                this.mSmsDestination = null;
            }
        } else if (permission.startsWith(OppoPermissionConstants.PERMISSION_CALL_PHONE) && !permission.equals(OppoPermissionConstants.PERMISSION_CALL_PHONE)) {
            this.mCallNumber = permission.substring(OppoPermissionConstants.PERMISSION_CALL_PHONE.length(), permission.length());
            if (!OppoMmiCode.isServiceCodeCallForwarding(this.mContext, this.mCallNumber)) {
                this.mCallNumber = Uri.parse(this.mCallNumber).getSchemeSpecificPart();
            }
            if (OppoMmiCode.isServiceCodeCallForwarding(this.mContext, this.mCallNumber)) {
                if (DEBUG) {
                    Log.d(TAG, "isServiceCodeCallForwarding " + this.mCallNumber);
                }
                permission2 = OppoPermissionConstants.PERMISSION_CALL_FORWARDING;
                contentUri = null;
                selection = null;
                selectionArgs = null;
            } else {
                permission2 = OppoPermissionConstants.PERMISSION_CALL_PHONE;
                contentUri = null;
                selection = null;
                selectionArgs = null;
            }
            if (!DEBUG) {
            }
            if (!UserHandle.isCore(uid)) {
            }
            return 3;
        } else if (permission.startsWith(PERMISSION_ACCESS_MEDIA_PROVIDER)) {
            if (DEBUG) {
                Log.d(TAG, "PERMISSION_ACCESS_MEDIA_PROVIDER " + permission);
            }
            String[] extraStringArray = permission.split(SMS_SEPARATOR);
            if (extraStringArray.length > 1) {
                String contentUri2 = extraStringArray[1];
                if (extraStringArray.length == 3) {
                    selection2 = extraStringArray[2];
                } else if (extraStringArray.length >= 4) {
                    selection2 = extraStringArray[2];
                    selectionArgs2 = extraStringArray[3];
                }
                permission2 = PERMISSION_ACCESS_MEDIA_PROVIDER;
                contentUri = contentUri2;
                selection = selection2;
                selectionArgs = selectionArgs2;
                if (!DEBUG) {
                }
                if (!UserHandle.isCore(uid)) {
                }
                return 3;
            }
        }
        permission2 = permission;
        contentUri = null;
        selection = null;
        selectionArgs = null;
        if (!DEBUG) {
        }
        if (!UserHandle.isCore(uid)) {
        }
        return 3;
    }

    public void updatePermissionChoice(String packageName, String permission, int choice) {
        if (DEBUG) {
            Log.d(TAG, "updatePermissionChoice, packageName=" + packageName + ", permission=" + permission + ", choice=" + choice);
        }
        if (packageName != null && permission != null) {
            UpdateMsgDate umd = new UpdateMsgDate();
            umd.mPackageName = packageName;
            umd.mPermission = permission;
            umd.mChoice = choice;
            umd.mUserId = UserHandle.getUserId(Binder.getCallingUid());
            if (packageName.contains(SMS_SEPARATOR)) {
                String[] packageWithUserId = packageName.split(SMS_SEPARATOR);
                if (packageWithUserId.length == 2) {
                    umd.mPackageName = packageWithUserId[0];
                    umd.mUserId = Integer.parseInt(packageWithUserId[1]);
                }
            }
            Message msg = this.mPermissionHandler.obtainMessage();
            msg.what = 1;
            msg.obj = umd;
            this.mPermissionHandler.sendMessage(msg);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x010a, code lost:
        if (0 != 0) goto L_0x00f5;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00a8 A[Catch:{ Exception -> 0x0100 }] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00fc  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0111  */
    private int getPermissionState(String packageName, String permission) {
        String packageName2;
        Uri uri;
        Cursor cursor;
        Bundle resultBundle;
        Throwable th;
        Exception e;
        int retValue = 3;
        int userId = UserHandle.getUserId(Binder.getCallingUid());
        if (!TextUtils.isEmpty(packageName) && packageName.contains(SMS_SEPARATOR)) {
            String[] packageWithUserId = packageName.split(SMS_SEPARATOR);
            if (packageWithUserId.length == 2) {
                String packageName3 = packageWithUserId[0];
                userId = Integer.parseInt(packageWithUserId[1]);
                packageName2 = packageName3;
                uri = ContentProvider.maybeAddUserId(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, userId);
                cursor = null;
                resultBundle = queryOppoPermissionAsUser(packageName2, userId);
                if (resultBundle != null || resultBundle.size() <= 0 || TextUtils.isEmpty(resultBundle.getString(COLUMN_PKG_NAME_STR))) {
                    cursor = this.mContext.getContentResolver().query(uri, null, "pkg_name=?", new String[]{packageName2}, null);
                    if (cursor != null) {
                        if (cursor.getCount() != 0) {
                            cursor.moveToFirst();
                            long valueAccept = cursor.getLong(cursor.getColumnIndex(COLUMN_ACCEPT_STR));
                            long valueReject = cursor.getLong(cursor.getColumnIndex("reject"));
                            long valuePrompt = cursor.getLong(cursor.getColumnIndex(COLUMN_PROMPT_STR));
                            if (0 != (valueAccept & getPermissionMask(permission))) {
                                retValue = 1;
                            } else if (0 != (valueReject & getPermissionMask(permission))) {
                                retValue = 2;
                            } else if (0 != (valuePrompt & getPermissionMask(permission))) {
                                retValue = 0;
                            }
                            cursor.close();
                            return retValue;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    return 3;
                }
                long valueAccept2 = resultBundle.getLong(COLUMN_ACCEPT_STR);
                long valueReject2 = resultBundle.getLong("reject");
                long valuePrompt2 = resultBundle.getLong(COLUMN_PROMPT_STR);
                if (0 != (valueAccept2 & getPermissionMask(permission))) {
                    return 1;
                }
                if (0 != (valueReject2 & getPermissionMask(permission))) {
                    return 2;
                }
                if (0 != (valuePrompt2 & getPermissionMask(permission))) {
                    return 0;
                }
                return 3;
            }
        }
        packageName2 = packageName;
        uri = ContentProvider.maybeAddUserId(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, userId);
        cursor = null;
        resultBundle = queryOppoPermissionAsUser(packageName2, userId);
        if (resultBundle != null) {
        }
        try {
            try {
                cursor = this.mContext.getContentResolver().query(uri, null, "pkg_name=?", new String[]{packageName2}, null);
                if (cursor != null) {
                }
                if (cursor != null) {
                }
                return 3;
            } catch (Exception e2) {
                e = e2;
                try {
                    e.printStackTrace();
                } catch (Throwable th2) {
                    th = th2;
                    if (0 != 0) {
                    }
                    throw th;
                }
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
        } catch (Throwable th3) {
            th = th3;
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    public void grantOppoPermissionsFromRuntime(String packageName, String permissionName) {
        String permissionGroup = null;
        PermissionInfo permissionInfo = null;
        try {
            permissionInfo = this.mContext.getPackageManager().getPermissionInfo(permissionName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (permissionInfo != null) {
            permissionGroup = OppoPermissionConstants.PLATFORM_PERMISSIONS.get(permissionName);
        }
        if (permissionGroup == null) {
            Log.e(TAG, "permissionGroup is null, permissionName:" + permissionName);
            return;
        }
        List<String> listOppoPermissions = OppoPermissionConstants.MAP_DANGEROUS_PERMISSON_GROUP.get(permissionGroup);
        if (listOppoPermissions != null) {
            for (int index = 0; index < listOppoPermissions.size(); index++) {
                String oppoPermission = listOppoPermissions.get(index);
                if (getPermissionState(packageName, oppoPermission) != 3) {
                    updatePermissionChoice(packageName, oppoPermission, 0);
                }
            }
        }
    }

    public void revokeOppoPermissionsFromRuntime(String packageName, String permissionName) {
        String permissionGroup = null;
        PermissionInfo permissionInfo = null;
        try {
            permissionInfo = this.mContext.getPackageManager().getPermissionInfo(permissionName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (permissionInfo != null) {
            permissionGroup = OppoPermissionConstants.PLATFORM_PERMISSIONS.get(permissionName);
        }
        if (permissionGroup == null) {
            Log.e(TAG, "permissionGroup is null, permissionName:" + permissionName);
            return;
        }
        List<String> listOppoPermissions = OppoPermissionConstants.MAP_DANGEROUS_PERMISSON_GROUP.get(permissionGroup);
        if (listOppoPermissions != null) {
            for (int index = 0; index < listOppoPermissions.size(); index++) {
                String oppoPermission = listOppoPermissions.get(index);
                if (getPermissionState(packageName, oppoPermission) != 3) {
                    updatePermissionChoice(packageName, oppoPermission, 2);
                }
            }
        }
    }

    public void setPermissionInterceptEnable(boolean enabled) {
        if (sIsPermissionInterceptEnabled != enabled) {
            sIsPermissionInterceptEnabled = enabled;
            SystemProperties.set(KEY_PERMISSION_PROPERTIES, String.valueOf(enabled));
        }
    }

    public boolean isPermissionInterceptEnabled() {
        return sIsPermissionInterceptEnabled;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changePermissionChoice(PackagePermission packagePermission, long permissionMask, int choice) {
        if (packagePermission != null) {
            if (choice == 0) {
                packagePermission.mAccept |= permissionMask;
                packagePermission.mReject &= ~permissionMask;
                packagePermission.mPrompt &= ~permissionMask;
            } else if (1 == choice) {
                packagePermission.mAccept &= ~permissionMask;
                packagePermission.mReject |= permissionMask;
                packagePermission.mPrompt &= ~permissionMask;
            } else if (2 == choice) {
                packagePermission.mAccept &= ~permissionMask;
                packagePermission.mReject &= ~permissionMask;
                packagePermission.mPrompt |= permissionMask;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getPermissionMask(String permission) {
        long mask = 0;
        int index = OppoPermissionManager.sInterceptingPermissions.indexOf(permission);
        if (index > -1) {
            mask = 1 << index;
        }
        if (DEBUG) {
            Log.d(TAG, "getPermissionMask, permission=" + permission + ", mask=" + mask + ", index=" + index);
        }
        return mask;
    }

    private PackagePermission queryPackagePermissions(String packageName) {
        return queryPackagePermissionsAsUser(packageName, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00e0, code lost:
        if (r9 != null) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00e2, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00ec, code lost:
        if (0 == 0) goto L_0x00ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00ef, code lost:
        return r10;
     */
    private PackagePermission queryPackagePermissionsAsUser(String packageName, int userId) {
        String[] selectionArgs = {packageName};
        Cursor cr = null;
        PackagePermission pkgPermission = null;
        Bundle resultBundle = queryOppoPermissionAsUser(packageName, userId);
        if (resultBundle == null || resultBundle.size() <= 0 || TextUtils.isEmpty(resultBundle.getString(COLUMN_PKG_NAME_STR))) {
            try {
                cr = this.mContext.getContentResolver().query(ContentProvider.maybeAddUserId(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, userId), null, "pkg_name=?", selectionArgs, null);
                if (cr != null && cr.getCount() == 1 && cr.moveToNext()) {
                    pkgPermission = new PackagePermission();
                    pkgPermission.mId = cr.getInt(0);
                    pkgPermission.mPackageName = cr.getString(1);
                    pkgPermission.mAccept = cr.getLong(2);
                    pkgPermission.mReject = cr.getLong(3);
                    pkgPermission.mPrompt = cr.getLong(4);
                    pkgPermission.mTrust = cr.getInt(6);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } catch (Throwable th) {
                if (0 != 0) {
                    cr.close();
                }
                throw th;
            }
        } else {
            PackagePermission pkgPermission2 = new PackagePermission();
            pkgPermission2.mId = resultBundle.getInt(COLUMN_ID_STR);
            pkgPermission2.mPackageName = resultBundle.getString(COLUMN_PKG_NAME_STR);
            pkgPermission2.mAccept = resultBundle.getLong(COLUMN_ACCEPT_STR);
            pkgPermission2.mReject = resultBundle.getLong("reject");
            pkgPermission2.mPrompt = resultBundle.getLong(COLUMN_PROMPT_STR);
            pkgPermission2.mTrust = resultBundle.getInt(COLUMN_TRUST_STR);
            if (DEBUG) {
                Log.d(TAG, "queryPackagePermissionsAsUser pkgName:" + pkgPermission2.mPackageName + ",accept:" + pkgPermission2.mAccept + ",reject:" + pkgPermission2.mReject + ",prompt:" + pkgPermission2.mPrompt);
            }
            return pkgPermission2;
        }
    }

    private void savePermissionChoice(PackagePermission packagePermission) {
        savePermissionChoiceAsUser(packagePermission, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void savePermissionChoiceAsUser(PackagePermission packagePermission, int userId) {
        String[] selectionArgs = {packagePermission.mPackageName};
        ContentValues values = new ContentValues();
        values.put(COLUMN_ACCEPT_STR, Long.valueOf(packagePermission.mAccept));
        values.put("reject", Long.valueOf(packagePermission.mReject));
        values.put(COLUMN_PROMPT_STR, Long.valueOf(packagePermission.mPrompt));
        if (DEBUG) {
            Log.d(TAG, "savePermissionChoice, values=" + values);
        }
        try {
            this.mContext.getContentResolver().update(ContentProvider.maybeAddUserId(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, userId), values, "pkg_name=?", selectionArgs);
        } catch (Exception ex) {
            Log.e(TAG, "savePermissionChoice error !");
            ex.printStackTrace();
        }
    }

    public ProcessRecord getProcessForPackageName(String packageName) {
        try {
            synchronized (this.mService) {
                for (int i = this.mService.mProcessList.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord pr = (ProcessRecord) this.mService.mProcessList.mLruProcesses.get(i);
                    if (!(pr == null || pr.info == null || pr.info.packageName == null || !pr.info.packageName.equals(packageName))) {
                        return pr;
                    }
                }
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ProcessRecord getProcessForPid(int pid) {
        synchronized (this.mService.mPidsSelfLocked) {
            ProcessRecord curProc = this.mService.mPidsSelfLocked.get(pid);
            if (curProc != null) {
                return curProc;
            }
            return null;
        }
    }

    private String getPackageLabel(ProcessRecord pr) {
        if (pr == null || pr.info == null) {
            return null;
        }
        try {
            ApplicationInfo ai = this.mContext.getPackageManager().getApplicationInfo(pr.info.packageName, OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE);
            if (ai != null) {
                return this.mContext.getPackageManager().getApplicationLabel(ai).toString();
            }
        } catch (Exception e) {
            Slog.w(TAG, e);
        }
        return null;
    }

    private String getPermissionPromptStr(String permission) {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        String str = this.mCurrentLanguage;
        if (str == null || this.mCurrentCountry == null || !str.equals(language) || !this.mCurrentCountry.equals(country)) {
            this.mCurrentLanguage = language;
            this.mCurrentCountry = country;
            sPermissionsPrompt = Arrays.asList(this.mContext.getResources().getStringArray(201786381));
        }
        int index = OppoPermissionManager.sInterceptingPermissions.indexOf(permission);
        if (index <= -1 || index >= sPermissionsPrompt.size()) {
            return "";
        }
        return sPermissionsPrompt.get(index);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyWaitingApp(PermissionCheckingMsg pcm, int res) {
        try {
            OppoPermissionCallback callBack = pcm.mCallback;
            if (callBack != null) {
                OppoBaseProcessRecord basePr = typeCasting(pcm.mPr);
                if (basePr != null) {
                    basePr.isWaitingPermissionChoice = DEBUG;
                }
                if (DEBUG) {
                    Slog.d(TAG, "notifyWaitingApp, pcm=" + pcm + ", res=" + res);
                }
                callBack.notifyApplication(pcm.mPermission, pcm.mPr.pid, res, pcm.mToken);
            }
        } catch (Exception e) {
            Slog.w(TAG, e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processPermission(PermissionCheckingMsg pcm, int res, boolean save) {
        String[] data;
        PermissionCheckingMsg nextPcm;
        OppoBaseProcessRecord nextBasePr;
        if (DEBUG) {
            Log.d(TAG, "processPermission, pcm=" + pcm + ", res=" + res + ", save=" + save);
        }
        synchronized (this) {
            if (save) {
                OppoBaseProcessRecord basePr = typeCasting(pcm.mPr);
                if (basePr != null) {
                    changePermissionChoice(basePr.mPackagePermission, pcm.mPermissionMask, res);
                    changePermissionChoice(basePr.mPersistPackagePermission, pcm.mPermissionMask, res);
                }
                int userId = UserHandle.getUserId(pcm.mUid);
                if (basePr != null) {
                    savePermissionChoiceAsUser(basePr.mPersistPackagePermission, userId);
                    basePr.isSelected = (int) (((long) basePr.isSelected) & (~pcm.mPermissionMask));
                }
                try {
                    if (!(sPermissionCheckMsgList == null || sPermissionCheckMsgList.size() <= 0 || (nextPcm = sPermissionCheckMsgList.getFirst()) == null || (nextBasePr = typeCasting(nextPcm.mPr)) == null || !nextBasePr.mPackagePermission.mPackageName.equals(basePr.mPersistPackagePermission.mPackageName))) {
                        changePermissionChoice(nextBasePr.mPackagePermission, pcm.mPermissionMask, res);
                        changePermissionChoice(nextBasePr.mPersistPackagePermission, pcm.mPermissionMask, res);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "sync data err.");
                    ex.printStackTrace();
                }
            }
        }
        notifyWaitingApp(pcm, res);
        synchronized (sPermissionCheckMsgList) {
            Iterator<PermissionCheckingMsg> iterator = sPermissionCheckMsgList.iterator();
            while (iterator.hasNext()) {
                PermissionCheckingMsg temp = iterator.next();
                if (pcm.equals(temp)) {
                    if (pcm.mPr.pid != temp.mPr.pid) {
                        Log.d(TAG, "pcm.mPr.pid != temp.mPr.pid");
                    }
                    iterator.remove();
                    notifyWaitingApp(temp, res);
                }
            }
            sPermissionCheckMsgList.notifyAll();
        }
        Intent intent = new Intent(STATISTIC_ACTION);
        Bundle bundle = new Bundle();
        if (!pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) || !isChargeSmsByPkg(pcm.mPr.info.packageName, this.mSmsContent, this.mSmsDestination)) {
            bundle.putInt(STATISTIC_TYPE, 0);
            data = new String[]{pcm.mPr.info.packageName, pcm.mPermission};
            bundle.putBoolean(STATISTIC_CHECKED, save);
        } else {
            bundle.putInt(STATISTIC_TYPE, 1);
            data = new String[]{pcm.mPr.info.packageName, this.mSmsContent, this.mSmsDestination};
        }
        bundle.putInt("reject", res);
        bundle.putStringArray(STATISTIC_DATA, data);
        intent.putExtras(bundle);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.getUserHandleForUid(pcm.mUid), "oppo.permission.OPPO_COMPONENT_SAFE");
        if (res == 0) {
            sendActionStatisticAsUser(pcm.mPr.info.packageName, pcm.mPermission, true, UserHandle.getUserId(pcm.mUid));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isScreenOn() {
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        }
        PowerManager powerManager = this.mPowerManager;
        if (powerManager != null) {
            return powerManager.isScreenOn();
        }
        return DEBUG;
    }

    private void showPermissionWindow(final PermissionCheckingMsg pcm) {
        this.mTimerHandler.post(new Runnable() {
            /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass4 */

            public void run() {
                OppoPermissionInterceptPolicy.this.showWindow(pcm);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void showWindow(final PermissionCheckingMsg pcm) {
        if (DEBUG) {
            Log.d(TAG, "showWindow, pcm=" + pcm);
        }
        View parentView = LayoutInflater.from(this.mContext).inflate(201917501, (ViewGroup) null);
        String packageLabel = getPackageLabel(pcm.mPr);
        String securitystr = this.mContext.getString(201589892);
        String perimissionLabel = getPermissionPromptStr(pcm.mPermission);
        ((TextView) parentView.findViewById(201458799)).setText(packageLabel + securitystr + perimissionLabel);
        this.mSaveCheckBox = (CheckBox) parentView.findViewById(201458801);
        if (pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_READ_SMS) || pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_READ_MMS) || pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_WRITE_MMS) || pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_READ_CONTACTS)) {
            Log.d(TAG, "when permission is SMS,owWindow, set checkbox is true");
            this.mSaveCheckBox.setChecked(true);
        }
        String rejectStr = this.mContext.getString(201589895);
        String acceptStr = this.mContext.getString(201589896);
        final ChoiceCountDownTimer mCountDownTimer = new ChoiceCountDownTimer(WATI_SHORT, MILLIS);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 201523207);
        builder.setView(parentView);
        builder.setPositiveButton(acceptStr, new DialogInterface.OnClickListener() {
            /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass5 */

            public void onClick(DialogInterface arg0, int arg1) {
                mCountDownTimer.cancel();
                OppoPermissionInterceptPolicy.this.mPermissionHandler.post(new Runnable() {
                    /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass5.AnonymousClass1 */

                    public void run() {
                        OppoPermissionInterceptPolicy.this.processPermission(pcm, 0, OppoPermissionInterceptPolicy.this.mSaveCheckBox.isChecked());
                    }
                });
            }
        });
        builder.setNegativeButton(rejectStr, new DialogInterface.OnClickListener() {
            /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass6 */

            public void onClick(DialogInterface arg0, int arg1) {
                mCountDownTimer.cancel();
                OppoPermissionInterceptPolicy.this.mPermissionHandler.post(new Runnable() {
                    /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass6.AnonymousClass1 */

                    public void run() {
                        OppoPermissionInterceptPolicy.this.processPermission(pcm, 1, OppoPermissionInterceptPolicy.this.mSaveCheckBox.isChecked());
                    }
                });
            }
        });
        builder.setTitle(this.mContext.getString(201589891));
        builder.setCancelable(DEBUG);
        AlertDialog permissionChoiceDialog = builder.create();
        permissionChoiceDialog.getWindow().getAttributes().setTitle("Permission Intercept");
        permissionChoiceDialog.getWindow().setType(2010);
        permissionChoiceDialog.show();
        mCountDownTimer.mRejectBtn = permissionChoiceDialog.getButton(-2);
        mCountDownTimer.mPermissionChoiceDialog = permissionChoiceDialog;
        mCountDownTimer.mPcm = pcm;
        mCountDownTimer.start();
    }

    /* access modifiers changed from: private */
    public class ChoiceCountDownTimer extends CountDownTimer {
        PermissionCheckingMsg mPcm;
        AlertDialog mPermissionChoiceDialog;
        Button mRejectBtn;

        public ChoiceCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onFinish() {
            this.mPermissionChoiceDialog.dismiss();
            OppoPermissionInterceptPolicy oppoPermissionInterceptPolicy = OppoPermissionInterceptPolicy.this;
            oppoPermissionInterceptPolicy.processPermission(this.mPcm, 1, oppoPermissionInterceptPolicy.mSaveCheckBox.isChecked());
        }

        public void onTick(long millisUntilFinished) {
            String str = OppoPermissionInterceptPolicy.this.mContext.getString(201589895) + "(" + (millisUntilFinished / OppoPermissionInterceptPolicy.MILLIS) + "s)";
            if (OppoPermissionInterceptPolicy.this.isScreenOn()) {
                this.mRejectBtn.setText(str);
            } else {
                onFinish();
            }
        }
    }

    private class CheckPermissionRunnable implements Runnable {
        OppoPermissionCallback mCallback;
        String mContentUri;
        Object mLock;
        String mPermission;
        int mPid;
        int mRes = 3;
        String mSelection;
        String mSelectionArgs;
        int mToken;
        int mUid;

        CheckPermissionRunnable(Object lock, String permission, int pid, int uid, int token, OppoPermissionCallback callback) {
            this.mLock = lock;
            this.mPermission = permission;
            this.mPid = pid;
            this.mUid = uid;
            this.mToken = token;
            this.mCallback = callback;
        }

        public void setContentUri(String contentUri) {
            this.mContentUri = contentUri;
        }

        public void setSelection(String selection) {
            this.mSelection = selection;
        }

        public void setSelectionArgs(String selectionArgs) {
            this.mSelectionArgs = selectionArgs;
        }

        /* JADX INFO: Multiple debug info for r9v59 java.lang.String: [D('tmpPermission' java.lang.String), D('pkgName' java.lang.String)] */
        public void run() {
            PackagePermission pkgPm;
            Exception e;
            Throwable th;
            synchronized (this.mLock) {
                if (OppoPermissionInterceptPolicy.DEBUG) {
                    Log.d(OppoPermissionInterceptPolicy.TAG, "checkPermissionForProc Runnable");
                }
                ProcessRecord pr = OppoPermissionInterceptPolicy.this.getProcessForPid(this.mPid);
                OppoBaseProcessRecord basePr = OppoPermissionInterceptPolicy.typeCasting(pr);
                PackagePermission pkgPm2 = null;
                String takePicturePermission = this.mPermission;
                if (pr == null) {
                    if (OppoPermissionInterceptPolicy.DEBUG) {
                        Log.d(OppoPermissionInterceptPolicy.TAG, "checkPermissionForProc, pr==null");
                    }
                    this.mRes = 3;
                    this.mLock.notifyAll();
                    return;
                }
                synchronized (OppoPermissionInterceptPolicy.this) {
                    try {
                        if (this.mPermission.equals(OppoPermissionInterceptPolicy.PERMISSION_ACCESS_MEDIA_PROVIDER)) {
                            if (OppoDCIMProtectManager.getInstance().isDCIMProtectEnabled()) {
                                if (!OppoDCIMProtectManager.getInstance().isSkippedUid(this.mUid)) {
                                    PermissionCheckingMsg pcm = new PermissionCheckingMsg();
                                    pcm.mPermission = this.mPermission;
                                    pcm.mPr = pr;
                                    pcm.mUid = this.mUid;
                                    pcm.mCallback = this.mCallback;
                                    pcm.mToken = this.mToken;
                                    pcm.mContentUri = this.mContentUri;
                                    pcm.mSelection = this.mSelection;
                                    pcm.mSelectionArgs = this.mSelectionArgs;
                                    synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm);
                                        if (basePr != null) {
                                            basePr.isWaitingPermissionChoice = true;
                                        }
                                        if (OppoPermissionInterceptPolicy.DEBUG) {
                                            Log.d(OppoPermissionInterceptPolicy.TAG, "add pcm, size=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size() + ", sPermissionCheckMsgList=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList);
                                        }
                                        OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                        this.mRes = 2;
                                    }
                                    this.mLock.notifyAll();
                                    return;
                                }
                            }
                            this.mRes = 0;
                            this.mLock.notifyAll();
                            return;
                        }
                        String tmpPermission = this.mPermission;
                        if (this.mPermission.equals("android.permission.CAMERA_TAKEPICTURE")) {
                            tmpPermission = this.mPermission;
                            this.mPermission = OppoPermissionConstants.PERMISSION_CAMERA;
                        }
                        if (pr.info != null) {
                            pkgPm2 = OppoPermissionInterceptPolicy.this.queryPackagePermissionsAsUser(pr.info.packageName, UserHandle.getUserId(this.mUid));
                            if (OppoPermissionInterceptPolicy.DEBUG) {
                                Log.d(OppoPermissionInterceptPolicy.TAG, "checkPermissionForProc, pr.mPackagePermission = null, query pkgPm=" + pkgPm2);
                            }
                        } else {
                            this.mRes = 3;
                            if (OppoPermissionInterceptPolicy.DEBUG) {
                                Log.d(OppoPermissionInterceptPolicy.TAG, "checkPermissionForProc, pr.info is null!!! return INVALID_RES!!!");
                            }
                        }
                        if (!(pkgPm2 == null || basePr == null)) {
                            basePr.mPackagePermission = pkgPm2;
                            basePr.mPersistPackagePermission = pkgPm2.copy();
                        }
                        if (pkgPm2 != null) {
                            try {
                                long mask = OppoPermissionInterceptPolicy.this.getPermissionMask(this.mPermission);
                                if (!this.mPermission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) || (pkgPm2.mReject & mask) != 0) {
                                    if (pkgPm2.mTrust != 0) {
                                        this.mRes = 0;
                                        OppoPermissionInterceptPolicy.this.sendNormalSmsStatisticAsUser(pkgPm2.mPackageName, this.mPermission, pr.userId);
                                    } else if ((pkgPm2.mAccept & mask) != 0) {
                                        if (!tmpPermission.equals("android.permission.CAMERA_TAKEPICTURE")) {
                                            this.mRes = 0;
                                            OppoPermissionInterceptPolicy.this.sendNormalSmsStatisticAsUser(pkgPm2.mPackageName, this.mPermission, pr.userId);
                                            OppoPermissionInterceptPolicy.this.sendActionStatisticAsUser(pkgPm2.mPackageName, this.mPermission, true, pr.userId);
                                        } else if (OppoPermissionInterceptPolicy.this.isScreenOn()) {
                                            ComponentName cn = null;
                                            try {
                                                cn = new OppoActivityManager().getTopActivityComponentName();
                                            } catch (Exception e2) {
                                                e2.printStackTrace();
                                            }
                                            String foregroundPackageName = pr.info.packageName;
                                            if (cn != null) {
                                                foregroundPackageName = cn.getPackageName();
                                            }
                                            if (!pr.info.packageName.contains("com.cttl.")) {
                                                if (!pr.info.packageName.equals(foregroundPackageName)) {
                                                    if (!OppoPermissionInterceptPolicy.sBackgroundCameraSkipPkgs.containsKey(pr.info.packageName)) {
                                                        if (!pr.info.packageName.equals(OppoPermissionInterceptPolicy.this.getCurrentFocus())) {
                                                            if (!OppoPermissionInterceptPolicy.this.isEnabledInputMethod(OppoPermissionInterceptPolicy.this.mContext, pr.info.packageName)) {
                                                                PermissionCheckingMsg pcm2 = new PermissionCheckingMsg();
                                                                pcm2.mPermission = this.mPermission;
                                                                pcm2.mPr = pr;
                                                                pcm2.mUid = this.mUid;
                                                                pcm2.mCallback = this.mCallback;
                                                                pcm2.mPermissionMask = mask;
                                                                pcm2.mToken = this.mToken;
                                                                pcm2.mIsBackground = true;
                                                                synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                                                    try {
                                                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm2);
                                                                        if (basePr != null) {
                                                                            try {
                                                                                basePr.isWaitingPermissionChoice = true;
                                                                            } catch (Throwable th2) {
                                                                                th = th2;
                                                                            }
                                                                        }
                                                                        if (OppoPermissionInterceptPolicy.DEBUG) {
                                                                            try {
                                                                                StringBuilder sb = new StringBuilder();
                                                                                sb.append("add pcm, size=");
                                                                                sb.append(OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size());
                                                                                sb.append(", sPermissionCheckMsgList=");
                                                                                sb.append(OppoPermissionInterceptPolicy.sPermissionCheckMsgList);
                                                                                Log.d(OppoPermissionInterceptPolicy.TAG, sb.toString());
                                                                            } catch (Throwable th3) {
                                                                                th = th3;
                                                                                throw th;
                                                                            }
                                                                        }
                                                                        OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                                                        this.mRes = 2;
                                                                        this.mLock.notifyAll();
                                                                        return;
                                                                    } catch (Throwable th4) {
                                                                        th = th4;
                                                                        throw th;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            this.mRes = 0;
                                        } else {
                                            String pkgName = pkgPm2.mPackageName;
                                            if (OppoPermissionInterceptPolicy.sBackgroundCameraSkipPkgs.containsKey(pkgName)) {
                                                this.mRes = 0;
                                            } else {
                                                this.mRes = 1;
                                                Log.d(OppoPermissionInterceptPolicy.TAG, "Reject background permission " + this.mPermission + "pkgName: " + pkgName);
                                            }
                                            OppoPermissionInterceptPolicy.this.statisticsScreenOffPermissionRequest(pkgName, takePicturePermission, OppoPermissionInterceptPolicy.VALUE_ACCEPT);
                                        }
                                    } else if ((pkgPm2.mReject & mask) != 0) {
                                        if (OppoPermissionInterceptPolicy.sRejectDialogPermissionList == null || !OppoPermissionInterceptPolicy.sRejectDialogPermissionList.contains(this.mPermission)) {
                                            this.mRes = 1;
                                            ActivityManager manager = (ActivityManager) OppoPermissionInterceptPolicy.this.mContext.getSystemService("activity");
                                            try {
                                                List<ActivityManager.RunningTaskInfo> runningTasks = new ArrayList<>();
                                                if (manager.getRunningTasks(1) != null) {
                                                    try {
                                                        runningTasks.addAll(manager.getRunningTasks(1));
                                                    } catch (Exception e3) {
                                                        e = e3;
                                                    }
                                                }
                                                if (runningTasks.size() != 0) {
                                                    if (runningTasks.get(0) != null) {
                                                        String packageName = runningTasks.get(0).topActivity.getPackageName();
                                                        try {
                                                            if (this.mUid == OppoPermissionInterceptPolicy.this.mPm.getPackageUid(packageName, 0, pr.userId) || (packageName.equals("com.wandoujia.phoenix2") && pr.info.packageName.equals("com.wandoujia.phoenix2.usbproxy"))) {
                                                                if (packageName.equals("com.wandoujia.phoenix2") && pr.info.packageName.equals("com.wandoujia.phoenix2.usbproxy")) {
                                                                    packageName = "com.wandoujia.phoenix2.usbproxy";
                                                                    Log.i("AAA", "----equals--");
                                                                }
                                                                Intent intent = new Intent();
                                                                intent.setAction("com.oppo.permissionprotect.notify");
                                                                intent.putExtra("PackageName", packageName);
                                                                intent.putExtra("Permission", this.mPermission);
                                                                OppoPermissionInterceptPolicy.this.mContext.sendBroadcastAsUser(intent, UserHandle.of(pr.userId), "oppo.permission.OPPO_COMPONENT_SAFE");
                                                                Log.i("AAA", "Notify!!!");
                                                            }
                                                        } catch (Exception e4) {
                                                            e = e4;
                                                            e.printStackTrace();
                                                            OppoPermissionInterceptPolicy.this.sendNormalSmsStatisticAsUser(pkgPm2.mPackageName, this.mPermission, pr.userId);
                                                            this.mLock.notifyAll();
                                                        }
                                                    }
                                                }
                                            } catch (Exception e5) {
                                                e = e5;
                                                e.printStackTrace();
                                                OppoPermissionInterceptPolicy.this.sendNormalSmsStatisticAsUser(pkgPm2.mPackageName, this.mPermission, pr.userId);
                                                this.mLock.notifyAll();
                                            }
                                            OppoPermissionInterceptPolicy.this.sendNormalSmsStatisticAsUser(pkgPm2.mPackageName, this.mPermission, pr.userId);
                                        } else {
                                            PermissionCheckingMsg pcm3 = new PermissionCheckingMsg();
                                            pcm3.mPermission = this.mPermission;
                                            pcm3.mPr = pr;
                                            pcm3.mUid = this.mUid;
                                            pcm3.mCallback = this.mCallback;
                                            pcm3.mPermissionMask = mask;
                                            pcm3.mToken = this.mToken;
                                            pcm3.mPkgPm = pkgPm2;
                                            synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                                OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm3);
                                                if (basePr != null) {
                                                    basePr.isWaitingPermissionChoice = true;
                                                }
                                                if (OppoPermissionInterceptPolicy.DEBUG) {
                                                    Log.d(OppoPermissionInterceptPolicy.TAG, "add pcm, size=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size() + ", sPermissionCheckMsgList=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList);
                                                }
                                                OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                                this.mRes = 2;
                                            }
                                            this.mLock.notifyAll();
                                            return;
                                        }
                                    } else if ((pkgPm2.mPrompt & mask) != 0) {
                                        if (OppoPermissionInterceptPolicy.this.isScreenOn()) {
                                            if (!OppoPermissionInterceptPolicy.sIsCtaVersion.booleanValue() || OppoPermissionConstants.PERMISSION_RECORD_AUDIO.equals(this.mPermission)) {
                                                if (!OppoPermissionInterceptPolicy.sAllowBackgroundRequest.booleanValue()) {
                                                    if (!pr.info.packageName.contains("com.cttl.")) {
                                                        if (!OppoPermissionInterceptPolicy.sIsExVersion || !OppoPermissionInterceptPolicy.sBackgroundSkipPermissions.contains(this.mPermission)) {
                                                            if (!OppoPermissionInterceptPolicy.sIsExVersion || !OppoPermissionInterceptPolicy.this.isDefaultSmsApp(OppoPermissionInterceptPolicy.this.mContext, pkgPm2.mPackageName)) {
                                                                if (!OppoPermissionInterceptPolicy.this.isDefaultDialerApp(OppoPermissionInterceptPolicy.this.mContext, pkgPm2.mPackageName)) {
                                                                    ComponentName cn2 = null;
                                                                    try {
                                                                        cn2 = new OppoActivityManager().getTopActivityComponentName();
                                                                    } catch (Exception e6) {
                                                                        e6.printStackTrace();
                                                                    }
                                                                    String foregroundPackageName2 = pr.info.packageName;
                                                                    boolean isGrantRuntimePermission = OppoPermissionInterceptPolicy.DEBUG;
                                                                    if (cn2 != null) {
                                                                        foregroundPackageName2 = cn2.getPackageName();
                                                                        isGrantRuntimePermission = "com.android.packageinstaller.permission.ui.GrantPermissionsActivity".equals(cn2.getClassName());
                                                                    }
                                                                    if (!OppoPermissionInterceptPolicy.sBackgroundSkipList.contains(pr.info.packageName)) {
                                                                        if (!pr.info.packageName.equals(foregroundPackageName2)) {
                                                                            try {
                                                                                if (!OppoPermissionInterceptPolicy.this.isEnabledInputMethod(OppoPermissionInterceptPolicy.this.mContext, ((ApplicationInfo) pr.info).packageName) && !OppoPermissionInterceptPolicy.this.getCurrentFocus().contains(pr.info.packageName) && !isGrantRuntimePermission) {
                                                                                    this.mRes = 1;
                                                                                    Log.d(OppoPermissionInterceptPolicy.TAG, "not foreground app, reject it! app is " + pr.info.packageName + ",foregroundPackageName" + foregroundPackageName2);
                                                                                    this.mLock.notifyAll();
                                                                                    return;
                                                                                }
                                                                            } catch (Throwable th5) {
                                                                                pkgPm = th5;
                                                                                throw pkgPm;
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            PermissionCheckingMsg pcm4 = new PermissionCheckingMsg();
                                            pcm4.mPermission = this.mPermission;
                                            pcm4.mPr = pr;
                                            pcm4.mUid = this.mUid;
                                            pcm4.mCallback = this.mCallback;
                                            pcm4.mPermissionMask = mask;
                                            pcm4.mToken = this.mToken;
                                            synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                                OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm4);
                                                if (basePr != null) {
                                                    basePr.isWaitingPermissionChoice = true;
                                                }
                                                if (OppoPermissionInterceptPolicy.DEBUG) {
                                                    Log.d(OppoPermissionInterceptPolicy.TAG, "add pcm, size=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size() + ", sPermissionCheckMsgList=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList);
                                                }
                                                OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                                this.mRes = 2;
                                            }
                                        } else {
                                            this.mRes = 1;
                                            OppoPermissionInterceptPolicy.this.statisticsScreenOffPermissionRequest(pr.info.packageName, takePicturePermission, OppoPermissionInterceptPolicy.VALUE_REJECT);
                                        }
                                    }
                                } else if (OppoPermissionInterceptPolicy.sIsExVersion && (pkgPm2.mAccept & mask) != 0) {
                                    this.mRes = 0;
                                    this.mLock.notifyAll();
                                    return;
                                } else if ((pkgPm2.mAccept & mask) != 0 && (OppoPermissionInterceptPolicy.sIsSpecialCarrier || OppoPermissionInterceptPolicy.sSMSConfirmSkipList.contains(pkgPm2.mPackageName))) {
                                    this.mRes = 0;
                                    this.mLock.notifyAll();
                                    return;
                                } else if (OppoPermissionInterceptPolicy.sRejectChargeSms) {
                                    this.mRes = 1;
                                } else {
                                    PermissionCheckingMsg pcm5 = new PermissionCheckingMsg();
                                    pcm5.mPermission = this.mPermission;
                                    pcm5.mPr = pr;
                                    pcm5.mUid = this.mUid;
                                    pcm5.mCallback = this.mCallback;
                                    pcm5.mPermissionMask = mask;
                                    pcm5.mToken = this.mToken;
                                    synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm5);
                                        if (basePr != null) {
                                            basePr.isWaitingPermissionChoice = true;
                                        }
                                        if (OppoPermissionInterceptPolicy.DEBUG) {
                                            Log.d(OppoPermissionInterceptPolicy.TAG, "add pcm, size=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size() + ", sPermissionCheckMsgList=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList);
                                        }
                                        OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                        this.mRes = 2;
                                    }
                                }
                            } catch (Throwable th6) {
                                pkgPm = th6;
                                throw pkgPm;
                            }
                        }
                        this.mLock.notifyAll();
                    } catch (Throwable th7) {
                        pkgPm = th7;
                        throw pkgPm;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class UpdateMsgDate {
        int mChoice;
        String mPackageName;
        String mPermission;
        int mUserId;

        private UpdateMsgDate() {
        }
    }

    public static void adjustThirdList(List thirdList, List res, String string) {
        Iterator iter = thirdList.iterator();
        while (iter.hasNext()) {
            Object thirdApk = iter.next();
            if (thirdApk instanceof ResolveInfo) {
                ResolveInfo ri = (ResolveInfo) thirdApk;
                if (ri.activityInfo.applicationInfo.packageName.equals(string)) {
                    res.add(ri);
                    iter.remove();
                }
            } else if (thirdApk instanceof BroadcastFilter) {
                BroadcastFilter bf = (BroadcastFilter) thirdApk;
                if (bf.packageName.equals(string)) {
                    res.add(bf);
                    iter.remove();
                }
            }
        }
    }

    public static List reorderReceiverList(List receivers) {
        if (receivers == null) {
            return null;
        }
        List<Object> systemList = new ArrayList();
        List<Object> thirdList = new ArrayList();
        List res = new ArrayList();
        String[] packageNames = {"com.qihoo360.mobilesafe", "com.anguanjia.safe", "com.blovestorm", "com.cootek.smartdialer", "com.sg.sledog"};
        for (Object temp : receivers) {
            if (temp instanceof ResolveInfo) {
                ResolveInfo ri = (ResolveInfo) temp;
                if (ri == null || ri.activityInfo == null || ri.activityInfo.applicationInfo == null || (ri.activityInfo.applicationInfo.flags & 1) == 0) {
                    thirdList.add(ri);
                } else {
                    systemList.add(ri);
                }
            } else if (temp instanceof BroadcastFilter) {
                BroadcastFilter bf = (BroadcastFilter) temp;
                if (bf == null || bf.receiverList == null || bf.receiverList.app == null || bf.receiverList.app.info == null || (bf.receiverList.app.info.flags & 1) == 0) {
                    thirdList.add(bf);
                } else {
                    systemList.add(bf);
                }
            }
        }
        for (String packageName : packageNames) {
            adjustThirdList(thirdList, res, packageName);
        }
        for (Object o : systemList) {
            res.add(o);
        }
        for (Object o2 : thirdList) {
            res.add(o2);
        }
        return res;
    }

    /* access modifiers changed from: private */
    public static void initAlertAppList(String fileName) {
        if (DEBUG) {
            Log.d(TAG, "initAlertAppList");
        }
        File dataFile = new File(ALERT_PERMISSION_DATA_DIR + fileName);
        if (!dataFile.exists()) {
            if (DEBUG) {
                Log.d(TAG, dataFile.getName() + " does not exist!");
            }
            if (fileName.equals(ALERT_PERMISSION_APPS) && !sRegexSms.contains(DEFAULT_SMS_REGEX)) {
                sRegexSms.add(DEFAULT_SMS_REGEX);
            }
            try {
                if (!dataFile.getParentFile().exists()) {
                    dataFile.getParentFile().mkdirs();
                }
                dataFile.createNewFile();
            } catch (Exception ex) {
                Log.e(TAG, "init data error.");
                ex.printStackTrace();
            }
        } else {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(dataFile);
                readDataFromXML(inputStream, fileName);
                try {
                    inputStream.close();
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            } catch (Exception ex3) {
                ex3.printStackTrace();
                Log.e(TAG, "initAlertAppList err !!");
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception ex4) {
                        ex4.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    private static void readDataFromXML(FileInputStream stream, String fileName) {
        int type;
        if (DEBUG) {
            Log.d(TAG, "readDataFromXML");
        }
        boolean regexSmsUpdated = DEBUG;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            if (fileName.equals(ALERT_PERMISSION_APPS)) {
                sAlertAppList.clear();
                sRegexSms.clear();
                sRegexSmsNumber.clear();
                sRegexSmsSkip.clear();
                sRegexSmsNumberSkip.clear();
                sShellRevokedPermissions.clear();
                sDeviceOwnerMd5List.clear();
            } else if (fileName.equals(GAME_FILTER_FILE_NAME)) {
                sGameSuffixList.clear();
                sGameNameList.clear();
            }
            List<String> tmpRejectDialogPermissionList = new ArrayList<>();
            do {
                type = parser.next();
                if (type == 2) {
                    String tag = parser.getName();
                    if (fileName.equals(ALERT_PERMISSION_APPS)) {
                        if (XML_TAG_ALERT.equals(tag)) {
                            String app = parser.nextText();
                            sAlertAppList.add(app);
                            if (DEBUG) {
                                Log.d(TAG, "add app : " + app);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_ALLOW_BACKGROUND.equals(tag)) {
                            Boolean flag = Boolean.valueOf(Boolean.parseBoolean(parser.nextText()));
                            sAllowBackgroundRequest = flag;
                            if (DEBUG) {
                                Log.d(TAG, "add flag : " + flag);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_OPPO_DCIM_RECYCLE_WHITE.equals(tag)) {
                            String packageName = parser.nextText();
                            OppoDCIMProtectManager.getInstance().addToDCIMProtectWhiteList(packageName);
                            if (DEBUG) {
                                Log.d(TAG, "XML_TAG_OPPO_DCIM_RECYCLE_WHITE : " + packageName);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_DCIMPROTECT_ENABLED.equals(tag)) {
                            Boolean enabled = Boolean.valueOf(Boolean.parseBoolean(parser.nextText()));
                            OppoDCIMProtectManager.getInstance().setDCIMProtectEnabled(enabled.booleanValue());
                            if (DEBUG) {
                                Log.d(TAG, "XML_TAG_DCIMPROTECT_ENABLED flag : " + enabled);
                                continue;
                            } else {
                                continue;
                            }
                        } else if ("sms".equals(tag)) {
                            String regex = parser.nextText();
                            sRegexSms.add(regex);
                            regexSmsUpdated = true;
                            if (DEBUG) {
                                Log.d(TAG, "add regex for sms content : " + regex);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_SMS_NUMBER.equals(tag)) {
                            String regex2 = parser.nextText();
                            sRegexSmsNumber.add(regex2);
                            if (DEBUG) {
                                Log.d(TAG, "add regex for sms number : " + regex2);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_SMS_CONTENT_SKIP.equals(tag)) {
                            String regex3 = parser.nextText();
                            sRegexSmsSkip.add(regex3);
                            if (DEBUG) {
                                Log.d(TAG, "add regex for skip sms content : " + regex3);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_SMS_NUMBER_SKIP.equals(tag)) {
                            String regex4 = parser.nextText();
                            sRegexSmsNumberSkip.add(regex4);
                            if (DEBUG) {
                                Log.d(TAG, "add regex for skip sms number : " + regex4);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_BACKGROUND_SKIP.equals(tag)) {
                            String packageName2 = parser.nextText();
                            if (!sBackgroundSkipList.contains(packageName2)) {
                                sBackgroundSkipList.add(packageName2);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_REJECT_DIALOG_PERMISSION.equals(tag)) {
                            String permission = parser.nextText();
                            if (permission != null && !permission.isEmpty()) {
                                tmpRejectDialogPermissionList.add(permission);
                                continue;
                            }
                        } else if (XML_TAG_BACKGROUND_CAMERA_SKIP.equals(tag)) {
                            sBackgroundCameraSkipPkgs.put(parser.nextText(), 1);
                            continue;
                        } else if (XML_TAG_SHELL_REVOKE_PERMISSION.equals(tag)) {
                            String permission2 = parser.nextText();
                            if (permission2 != null) {
                                sShellRevokedPermissions.add(permission2);
                            }
                            if (DEBUG) {
                                Log.d(TAG, "add shell : " + permission2);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_DEVICE_OWNER_SWITCH.equals(tag)) {
                            OppoDevicePolicyUtils.updateSwitchState(Boolean.parseBoolean(parser.nextText()));
                            continue;
                        } else if (XML_TAG_DEVICE_OWNER_SIGNATURE_LIST.equals(tag)) {
                            String md5 = parser.nextText();
                            if (md5 != null) {
                                sDeviceOwnerMd5List.add(md5);
                            }
                            if (DEBUG) {
                                Log.d(TAG, "add device owner : " + md5);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_SMS_CONFIRM_SKIP.equals(tag)) {
                            String packageName3 = parser.nextText();
                            if (packageName3 != null) {
                                sSMSConfirmSkipList.add(packageName3);
                            }
                            if (DEBUG) {
                                Log.d(TAG, "add sms skip : " + packageName3);
                                continue;
                            } else {
                                continue;
                            }
                        } else if (XML_TAG_PERM_RECORD_SWITCH.equals(tag)) {
                            sAllowPermissionRecord = !sIsExVersion && Boolean.parseBoolean(parser.nextText());
                            if (DEBUG) {
                                Log.d(TAG, "allow permission record: " + sAllowPermissionRecord);
                                continue;
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    } else if (!fileName.equals(GAME_FILTER_FILE_NAME)) {
                        continue;
                    } else if (XML_TAG_GAME_SUFFIX.equals(tag)) {
                        String suffix = parser.nextText();
                        sGameSuffixList.add(suffix);
                        if (DEBUG) {
                            Log.d(TAG, "add game suffix : " + suffix);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (XML_TAG_GAME_NAME.equals(tag)) {
                        String name = parser.nextText();
                        sGameNameList.add(name);
                        if (DEBUG) {
                            Log.d(TAG, "add game name : " + name);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (XML_TAG_SMS_PROMPT_SWITCH.equalsIgnoreCase(tag)) {
                        sSmsPromptSwitch = parser.nextText();
                        if (!sSmsPromptSwitch.equalsIgnoreCase(SWITCH_ON) && !sSmsPromptSwitch.equalsIgnoreCase(SWITCH_OFF) && !sSmsPromptSwitch.equalsIgnoreCase(SWITCH_SKIP_OPPO)) {
                            sSmsPromptSwitch = SWITCH_ON;
                        }
                        if (DEBUG) {
                            Log.d(TAG, "sSmsPromptSwitch is : " + sSmsPromptSwitch);
                            continue;
                        } else {
                            continue;
                        }
                    } else if (XML_TAG_SMS_CHARGE_SWITCH.equalsIgnoreCase(tag)) {
                        sSmsChargeSwitch = parser.nextText();
                        if (!sSmsChargeSwitch.equalsIgnoreCase(SWITCH_ON) && !sSmsChargeSwitch.equalsIgnoreCase(SWITCH_OFF) && !sSmsChargeSwitch.equalsIgnoreCase(SWITCH_SKIP_OPPO)) {
                            sSmsChargeSwitch = SWITCH_SKIP_OPPO;
                        }
                        if (DEBUG) {
                            Log.d(TAG, "sSmsChargeSwitch is : " + sSmsChargeSwitch);
                            continue;
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            } while (type != 1);
            if (!regexSmsUpdated) {
                sRegexSms.add(DEFAULT_SMS_REGEX);
            }
            if (!tmpRejectDialogPermissionList.isEmpty()) {
                sRejectDialogPermissionList.clear();
                for (int i = 0; i < tmpRejectDialogPermissionList.size(); i++) {
                    sRejectDialogPermissionList.add(tmpRejectDialogPermissionList.get(i));
                }
            }
            if (sShellRevokedPermissions != null && !sShellRevokedPermissions.isEmpty()) {
                OppoShellPermissionUtils.updateShellPermissions(sShellRevokedPermissions);
            }
            if (!(sDeviceOwnerMd5List == null || sDeviceOwnerMd5List.isEmpty())) {
                OppoDevicePolicyUtils.updateWhiteList(sDeviceOwnerMd5List);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "readDataFromXML err !!");
        }
    }

    private class AlertDataFileListener extends FileObserver {
        String mObserverPath = null;

        public AlertDataFileListener(String path) {
            super(path, 264);
            this.mObserverPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8) {
                String str = this.mObserverPath;
                if (str == null || !str.contains(OppoPermissionInterceptPolicy.ALERT_PERMISSION_APPS)) {
                    String str2 = this.mObserverPath;
                    if (str2 != null && str2.contains(OppoPermissionInterceptPolicy.GAME_FILTER_FILE_NAME)) {
                        OppoPermissionInterceptPolicy.initAlertAppList(OppoPermissionInterceptPolicy.GAME_FILTER_FILE_NAME);
                        return;
                    }
                    return;
                }
                OppoPermissionInterceptPolicy.initAlertAppList(OppoPermissionInterceptPolicy.ALERT_PERMISSION_APPS);
            }
        }
    }

    private static boolean isDeviceRooted() {
        for (String path : new String[]{"/sbin/su", "/system/bin/su", "/system/xbin/su", "/system/sbin/su", "/vendor/bin/su"}) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return DEBUG;
    }

    private static boolean isChargeSms(String sms, String number) {
        List<String> list;
        List<String> list2;
        List<String> list3;
        boolean result = DEBUG;
        if (!(sms == null || (list3 = sRegexSmsSkip) == null || list3.size() <= 0)) {
            for (String regex : sRegexSmsSkip) {
                result = Pattern.compile(regex.trim()).matcher(sms).matches();
                if (result) {
                    if (DEBUG) {
                        Log.d(TAG, "charge regexSkip : " + regex + " , result " + result);
                    }
                    return DEBUG;
                }
            }
        }
        if (!(number == null || (list2 = sRegexSmsNumberSkip) == null || list2.size() <= 0)) {
            for (String regex2 : sRegexSmsNumberSkip) {
                result = Pattern.compile(regex2.trim()).matcher(number).matches();
                if (result) {
                    if (DEBUG) {
                        Log.d(TAG, "charge sRegexSmsNumberSkip : " + regex2 + " , result " + result);
                    }
                    return DEBUG;
                }
            }
        }
        if (sms != null) {
            List<String> list4 = sRegexSms;
            if (list4 == null || list4.size() == 0) {
                Log.e(TAG, "charge regex is null !");
            }
            List<String> list5 = sRegexSms;
            if (list5 != null && list5.size() > 0) {
                for (String regex3 : sRegexSms) {
                    result = Pattern.compile(regex3.trim()).matcher(sms).matches();
                    if (DEBUG) {
                        Log.d(TAG, "charge regex : " + regex3 + " , result " + result);
                        continue;
                    }
                    if (result) {
                        if (DEBUG) {
                            Log.d(TAG, "charge sms : " + regex3 + " , " + sms);
                        }
                        return true;
                    }
                }
            }
        }
        if (!(number == null || (list = sRegexSmsNumber) == null || list.size() <= 0)) {
            for (String regex4 : sRegexSmsNumber) {
                result = Pattern.compile(regex4.trim()).matcher(number).matches();
                if (result) {
                    if (DEBUG) {
                        Log.d(TAG, "charge sms number: " + regex4 + " , " + number);
                    }
                    return true;
                }
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isChargeSmsByPkg(String packageName, String sms, String number) {
        boolean result = DEBUG;
        if (packageName == null || packageName.isEmpty() || sIsCtaVersion.booleanValue()) {
            return DEBUG;
        }
        if ((sIsBusinessCustom && sIsBussinessNoAccountDialog) || DEFAULT_CHARGE_SKIP_PACKAGE.contains(packageName)) {
            return DEBUG;
        }
        if (sSmsChargeSwitch.equalsIgnoreCase(SWITCH_SKIP_OPPO)) {
            if (!isFromGameCenter(packageName) && sms != null && isChargeSms(sms, number)) {
                result = true;
            }
            return result;
        } else if (sSmsChargeSwitch.equalsIgnoreCase(SWITCH_ON)) {
            if (sms != null && isChargeSms(sms, number)) {
                result = true;
            }
            return result;
        } else if (sSmsChargeSwitch.equalsIgnoreCase(SWITCH_OFF)) {
            return DEBUG;
        } else {
            return DEBUG;
        }
    }

    private boolean isFromGameCenter(String pkgName) {
        List<String> list = sGameSuffixList;
        if (list != null && !list.isEmpty()) {
            for (String suffix : sGameSuffixList) {
                if (pkgName.contains(suffix)) {
                    return true;
                }
            }
        }
        List<String> list2 = sGameNameList;
        if (list2 == null || list2.isEmpty() || !sGameNameList.contains(pkgName)) {
            return DEBUG;
        }
        return true;
    }

    private void sendNormalSmsStatistic(String pkgName, String permission) {
        sendNormalSmsStatisticAsUser(pkgName, permission, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNormalSmsStatisticAsUser(String pkgName, String permission, int userId) {
        if (permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && this.mSmsContent != null) {
            Intent intent = new Intent(STATISTIC_ACTION);
            Bundle bundle = new Bundle();
            bundle.putInt(STATISTIC_TYPE, 2);
            bundle.putStringArray(STATISTIC_DATA, new String[]{pkgName, this.mSmsContent, this.mSmsDestination});
            intent.putExtras(bundle);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userId), "oppo.permission.OPPO_COMPONENT_SAFE");
            this.mSmsContent = null;
            this.mSmsDestination = null;
        }
    }

    private void sendActionStatistic(String pkgName, String permission, boolean allowed) {
        sendActionStatisticAsUser(pkgName, permission, allowed, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendActionStatisticAsUser(String pkgName, String permission, boolean allowed, int userId) {
        if (OppoPermissionManager.OPPO_PRIVACY_PROTECT_PERMISSIONS.contains(permission)) {
            Intent intent = new Intent(STATISTIC_ACTION_PRIVACY_PROTECT);
            Bundle bundle = new Bundle();
            bundle.putString(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NAME, pkgName);
            bundle.putString("permission", permission);
            bundle.putBoolean("allowed", allowed);
            intent.putExtras(bundle);
            intent.setPackage(PERMISSION_PACKGE_NAME);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userId), "oppo.permission.OPPO_COMPONENT_SAFE");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isEnabledInputMethod(Context context, String packageName) {
        List<InputMethodInfo> list = ((InputMethodManager) context.getSystemService("input_method")).getEnabledInputMethodList();
        if (list == null || list.isEmpty()) {
            return DEBUG;
        }
        for (InputMethodInfo imi : list) {
            if (packageName.equals(imi.getPackageName())) {
                return true;
            }
        }
        return DEBUG;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCurrentFocus() {
        String result = "";
        ActivityManagerService activityManagerService = this.mService;
        if (!(activityManagerService == null || activityManagerService.mWindowManager == null)) {
            OppoBaseWindowManagerService baseWMS = typeCasting(this.mService.mWindowManager);
            if (!(baseWMS == null || baseWMS.mColorWmsInner == null || baseWMS.mColorWmsInner.getFocusedWindow() == null)) {
                result = baseWMS.mColorWmsInner.getFocusWindowPkgName();
            }
            if (DEBUG) {
                Log.d(TAG, "getCurrentFocus " + result);
            }
        }
        return result == null ? "" : result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendDialogMsg(int msgCode, Bundle data) {
        if (this.mDialogService == null) {
            if (DEBUG) {
                Log.e(TAG, "dialog Service is null");
            }
            sRecordMsgCode = msgCode;
            sRecordData = data;
            if (!bindDialogService(this.mContext)) {
                if (DEBUG) {
                    Log.e(TAG, "bind false");
                }
                sRecordMsgCode = 0;
                sRecordData = null;
                setDefaultDialogResult();
                return;
            }
            return;
        }
        try {
            Message msg = Message.obtain((Handler) null, msgCode);
            msg.replyTo = this.mMessenger;
            if (data != null) {
                msg.setData(data);
            }
            this.mDialogService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean bindDialogService(Context context) {
        try {
            Intent intent = new Intent(ACTION_DIALOG_SERVICE);
            intent.setPackage(PERMISSION_PACKGE_NAME);
            return context.bindService(intent, this.mServConnection, 1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "bind service err !");
            return DEBUG;
        }
    }

    private void setDefaultDialogResult() {
        this.mPermissionHandler.post(new Runnable() {
            /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass8 */

            public void run() {
                if (OppoPermissionInterceptPolicy.DEBUG) {
                    Log.v(OppoPermissionInterceptPolicy.TAG, "set default dialog result");
                }
                if (OppoPermissionInterceptPolicy.this.mRecordPcm != null) {
                    OppoPermissionInterceptPolicy oppoPermissionInterceptPolicy = OppoPermissionInterceptPolicy.this;
                    oppoPermissionInterceptPolicy.processPermission(oppoPermissionInterceptPolicy.mRecordPcm, 0, OppoPermissionInterceptPolicy.DEBUG);
                } else if (OppoPermissionInterceptPolicy.DEBUG) {
                    Log.v(OppoPermissionInterceptPolicy.TAG, "mRecordPcm is null !");
                }
            }
        });
    }

    private boolean checkPackage(String packageName) {
        if (packageName != null && !packageName.isEmpty()) {
            try {
                this.mContext.getPackageManager().getApplicationInfo(packageName, 8192);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                Log.v(TAG, "not found package : " + packageName);
            }
        }
        return DEBUG;
    }

    private static boolean isMApp(Context context, String pkgName) {
        try {
            if (context.getPackageManager().getApplicationInfo(pkgName, 0).targetSdkVersion >= 23) {
                return true;
            }
            return DEBUG;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private boolean isForumVersion() {
        String ver = SystemProperties.get(PROPERTY_OPPOROM);
        if (ver == null) {
            return DEBUG;
        }
        String ver2 = ver.toLowerCase();
        if (ver2.endsWith(VERSION_ALPHA) || ver2.endsWith(VERSION_BETA)) {
            return true;
        }
        return DEBUG;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void statisticsScreenOffPermissionRequest(String packageName, String permissionName, String permissionValue) {
        if (isForumVersion()) {
            Map<String, String> statisticsMap = new HashMap<>();
            statisticsMap.put(KEY_PACKAGE_NAME, packageName);
            statisticsMap.put(KEY_PERMISSION_NAME, permissionName);
            statisticsMap.put(KEY_PERMISSION_VALUE, permissionValue);
            OppoStatistics.onCommon(this.mContext, UPLOAD_LOGTAG, UPLOAD_LOG_EVENTID, statisticsMap, (boolean) DEBUG);
        }
    }

    private static boolean isDefaultMmsRegion(Context context) {
        String currentRegion = SystemProperties.get("persist.sys.oppo.region", "OC");
        String defaultRegions = Settings.Global.getString(context.getContentResolver(), COLOR_DEFAULT_MMS_REGIONS);
        Log.d(TAG, "currentRegion = " + currentRegion + ", defaultRegions = " + defaultRegions);
        if (TextUtils.isEmpty(defaultRegions) || "null".equals(defaultRegions)) {
            String[] list = INITIAL_REGIONS;
            for (String region : list) {
                if (!TextUtils.isEmpty(region) && region.equals(currentRegion)) {
                    return true;
                }
            }
            return DEBUG;
        }
        String[] list2 = defaultRegions.split(";");
        if (list2 != null && list2.length > 0) {
            for (String region2 : list2) {
                if (!TextUtils.isEmpty(region2) && region2.equals(currentRegion)) {
                    return true;
                }
            }
        }
        return DEBUG;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDefaultSmsApp(Context context, String packageName) {
        boolean result = DEBUG;
        String defaultSms = Telephony.Sms.getDefaultSmsPackage(this.mContext);
        if (defaultSms != null && defaultSms.equals(packageName)) {
            result = true;
            if (DEBUG) {
                Log.d(TAG, "default sms " + packageName);
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDefaultDialerApp(Context context, String packageName) {
        boolean result = DEBUG;
        String defaultDialer = ((TelecomManager) this.mContext.getSystemService("telecom")).getDefaultDialerPackage();
        if (defaultDialer != null && defaultDialer.equals(packageName)) {
            result = true;
            if (DEBUG) {
                Log.d(TAG, "default dialer " + packageName);
            }
        }
        return result;
    }

    private boolean isSpecialCarrier(Context context) {
        if (context.getPackageManager().hasSystemFeature("oppo.permission.orange")) {
            return true;
        }
        return DEBUG;
    }

    public void setPermissionRecordController(IOppoPermissionRecordController controller) {
        this.mController = controller;
    }

    public void syncPermissionRecord() {
        synchronized (this.mPermissionLock) {
            if (sAllowPermissionRecord) {
                sendPermissionRecordIfNecessary(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyPermissionRecord(String packageName, String permissionName, long time, int result) {
        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(permissionName) && !OppoPermissionConstants.SKIP_RECORD_PERMISSIONS.contains(permissionName)) {
            synchronized (this.mPermissionLock) {
                startAppServiceIfNecessary();
                if (this.mPermissionCount >= DATA_NUM_THRESHOLD) {
                    sendPermissionRecordIfNecessary(DEBUG);
                }
                this.mPackageNameList[this.mPermissionCount] = packageName;
                this.mPermissionNameList[this.mPermissionCount] = permissionName;
                this.mTimeList[this.mPermissionCount] = time;
                this.mResultList[this.mPermissionCount] = result;
                this.mPermissionCount++;
                if (this.mPermissionCount >= DATA_NUM_THRESHOLD) {
                    sendPermissionRecordIfNecessary(DEBUG);
                }
            }
        }
    }

    private void sendPermissionRecordIfNecessary(boolean force) {
        if (DEBUG) {
            Log.d(TAG, "sendPermissionRecordIfNecessary with force: " + force);
        }
        if (this.mPermissionCount >= DATA_NUM_THRESHOLD || force) {
            int i = this.mPermissionCount;
            String[] packageNameList = new String[i];
            String[] permissionNameList = new String[i];
            long[] timeList = new long[i];
            int[] resultList = new int[i];
            System.arraycopy(this.mPackageNameList, 0, packageNameList, 0, i);
            System.arraycopy(this.mPermissionNameList, 0, permissionNameList, 0, this.mPermissionCount);
            System.arraycopy(this.mTimeList, 0, timeList, 0, this.mPermissionCount);
            System.arraycopy(this.mResultList, 0, resultList, 0, this.mPermissionCount);
            if (this.mPermissionRecordHandler == null) {
                preparePermRecordThreadIfNeed();
            }
            this.mPermissionRecordHandler.post(new UpdatePermissionRecord(packageNameList, permissionNameList, timeList, resultList));
            this.mPermissionCount = 0;
        }
    }

    /* access modifiers changed from: private */
    public class UpdatePermissionRecord implements Runnable {
        private String[] packageNameList;
        private String[] permissionNameList;
        private int[] resultList;
        private long[] timeList;

        public UpdatePermissionRecord(String[] packageNameList2, String[] permissionNameList2, long[] timeList2, int[] resultList2) {
            this.packageNameList = packageNameList2;
            this.permissionNameList = permissionNameList2;
            this.timeList = timeList2;
            this.resultList = resultList2;
        }

        public void run() {
            String[] strArr;
            long[] jArr;
            int[] iArr;
            String[] strArr2 = this.packageNameList;
            if (strArr2 != null && (strArr = this.permissionNameList) != null && (jArr = this.timeList) != null && (iArr = this.resultList) != null) {
                OppoPermissionInterceptPolicy.this.notifyPermissionRecordInfo(strArr2, strArr, jArr, iArr);
            }
        }
    }

    private void startAppServiceIfNecessary() {
        if (this.mController == null) {
            Log.d(TAG, "mController is null, resstart service");
            new Thread(new Runnable() {
                /* class com.android.server.am.OppoPermissionInterceptPolicy.AnonymousClass9 */

                public void run() {
                    try {
                        Intent intent = new Intent(OppoPermissionInterceptPolicy.ACTION_RECORD_SERVICE);
                        intent.setPackage(OppoPermissionInterceptPolicy.PERMISSION_PACKGE_NAME);
                        OppoPermissionInterceptPolicy.this.mContext.startService(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(OppoPermissionInterceptPolicy.TAG, "start record service err !");
                    }
                }
            }).start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPermissionRecordInfo(String[] packageNameList, String[] permissionNameList, long[] timeList, int[] resultList) {
        startAppServiceIfNecessary();
        if (this.mController != null) {
            int size = packageNameList.length;
            if (permissionNameList.length == size && timeList.length == size && resultList.length == size) {
                if (DEBUG) {
                    for (int index = 0; index < size; index++) {
                        String packageName = packageNameList[index];
                        String permissionName = permissionNameList[index];
                        long time = timeList[index];
                        int result = resultList[index];
                        Log.d(TAG, "notifyPermissionRecordInfo " + packageName + ",permissionName:" + permissionName + ",time:" + time + ",result:" + result);
                    }
                }
                try {
                    this.mController.notifyPermissionRecordInfo(packageNameList, permissionNameList, timeList, resultList);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    this.mController = null;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    this.mController = null;
                }
            } else {
                Log.d(TAG, "notifyPermissionRecordInfo with inconsistent data");
            }
        }
    }

    /* access modifiers changed from: private */
    public static OppoBaseProcessRecord typeCasting(ProcessRecord pr) {
        if (pr != null) {
            return (OppoBaseProcessRecord) ColorTypeCastingHelper.typeCasting(OppoBaseProcessRecord.class, pr);
        }
        return null;
    }

    private static OppoBaseWindowManagerService typeCasting(WindowManagerService wms) {
        if (wms != null) {
            return (OppoBaseWindowManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseWindowManagerService.class, wms);
        }
        return null;
    }

    private Bundle queryOppoPermissionAsUser(String packageName, int userId) {
        Bundle resultBundle = new Bundle();
        ContentProviderClient contentProvider = null;
        try {
            ContentResolver resolver = this.mContext.getContentResolver();
            if (resolver == null) {
                return resultBundle;
            }
            ContentProviderClient contentProvider2 = this.mContentProviderMap.get(Integer.valueOf(userId));
            if (contentProvider2 == null) {
                contentProvider2 = resolver.acquireUnstableContentProviderClient(ContentProvider.maybeAddUserId(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, userId));
                if (contentProvider2 != null) {
                    this.mContentProviderMap.put(Integer.valueOf(userId), contentProvider2);
                }
                if (DEBUG) {
                    Log.d(TAG, "queryOppoPermissionAsUser contentProvider is null to create,userId:" + userId + ",contentProvider:" + contentProvider2);
                }
            }
            if (contentProvider2 == null) {
                return resultBundle;
            }
            Bundle bundle = new Bundle();
            bundle.putString(ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NAME, packageName);
            resultBundle = contentProvider2.call("getOppoPermission", TAG, bundle);
            if (resultBundle != null && (resultBundle.size() == 0 || TextUtils.isEmpty(resultBundle.getString(COLUMN_PKG_NAME_STR)))) {
                contentProvider2.close();
                this.mContentProviderMap.clear();
                ContentProviderClient contentProvider3 = resolver.acquireUnstableContentProviderClient(ContentProvider.maybeAddUserId(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, userId));
                if (contentProvider3 != null) {
                    this.mContentProviderMap.put(Integer.valueOf(userId), contentProvider3);
                    resultBundle = contentProvider3.call("getOppoPermission", TAG, bundle);
                }
                Log.i(TAG, "queryOppoPermissionAsUser cache is old recache again,userId:" + userId + ",provider:" + contentProvider3);
            }
            return resultBundle;
        } catch (Exception e) {
            if (0 != 0) {
                try {
                    contentProvider.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            this.mContentProviderMap.clear();
        }
    }
}
