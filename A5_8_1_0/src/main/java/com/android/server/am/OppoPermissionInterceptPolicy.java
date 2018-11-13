package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.app.KeyguardManager;
import android.app.OppoActivityManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.OppoPermissionManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.dhcp.DhcpPacket;
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
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.provider.Settings.Global;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.server.coloros.OppoKillerManagerService.SystemServer;
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
import java.util.regex.Pattern;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;

public class OppoPermissionInterceptPolicy {
    private static final String ACTION_DIALOG_SERVICE = "coloros.safecenter.permission.PERMISSION_DIALOG_SERVICE";
    private static final int ADD_ATHENA_SERVICE = 4;
    private static final String ALERT_PERMISSION_APPS = "alert_permission_apps.xml";
    private static final String ALERT_PERMISSION_DATA_DIR = "//data//oppo//coloros//permission//";
    private static final String COLOR_DEFAULT_MMS_REGIONS = "color_default_mms_regions";
    private static final int COLUMN_ACCEPT = 2;
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_PKG_NAME = 1;
    private static final int COLUMN_PROMPT = 4;
    private static final int COLUMN_REJECT = 3;
    private static final int COLUMN_TRUST = 6;
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String DEFAULT_SMS_REGEX = "[a-zA-Z0-9#$%&!'()*+,-./:;<=>?@\\[\\]^_`{|}~ ]{10,}";
    private static final long DELAY_THREE = 3000;
    private static final int DO_CHECK_PERMISSION = 0;
    private static final int ECHO_TO_KERNEL_NODES = 3;
    private static final String GAME_FILTER_FILE_NAME = "safe_marketfilter_list.xml";
    private static final String[] INITIAL_REGIONS = new String[]{"AU", "NZ"};
    private static final String KEYGUARD_PACKAGE_NAME = "com.android.systemui";
    private static final String KEY_CTSVERSION_PROPERTIES = "persist.oppo.ctsversion";
    private static final String KEY_PACKAGE_NAME = "pkgName";
    private static final String KEY_PERMISSION_NAME = "permissionName";
    private static final String KEY_PERMISSION_PROPERTIES = "persist.sys.permission.enable";
    private static final String KEY_PERMISSION_VALUE = "permissionValue";
    private static final long MILLIS = 1000;
    private static final int MSG_OPPODCIM_DELFAIL_DIALOG_SHOW = 104;
    private static final int MSG_PERMISSION_DIALOG_GET_RESULT = 211;
    private static final int MSG_PERMISSION_DIALOG_SEND_RESULT = 103;
    private static final int MSG_PERMISSION_DIALOG_SHOW = 101;
    private static final int PARAMETER_FOUR = 4;
    private static final int PARAMETER_ONE = 1;
    private static final int PARAMETER_THREE = 3;
    private static final int PARAMETER_TWO = 2;
    private static final String PERMISSION_ACCESS_MEDIA_PROVIDER = "android.permission.ACCESS_MEDIA_PROVIDER";
    private static final String PERMISSION_PACKGE_NAME = "com.coloros.securitypermission";
    private static final String PROPERTY_OPPOROM = "ro.build.version.opporom";
    private static final String SMS_SEPARATOR = "#";
    private static final String STATISTIC_ACTION = "coloros.safecenter.permission.STATISTIC_FRAMEWORK";
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
    private static final String UEVENT_DCIM_DELETE_MSG = "DELETE_STAT=DCIM";
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
    private static final String XML_TAG_GAME_NAME = "whitelist";
    private static final String XML_TAG_GAME_SUFFIX = "keyword";
    private static final String XML_TAG_OPPO_DCIM_RECYCLE_WHITE = "dcimprotect_recycle_white";
    private static final String XML_TAG_REJECT_DIALOG_PERMISSION = "rejectdialog_permission";
    private static final String XML_TAG_SHELL_REVOKE_PERMISSION = "shell_revoke_permission";
    private static final String XML_TAG_SMS_CHARGE_SWITCH = "smschargeswitch";
    private static final String XML_TAG_SMS_CONTENT = "sms";
    private static final String XML_TAG_SMS_CONTENT_SKIP = "sms_skip";
    private static final String XML_TAG_SMS_NUMBER = "number";
    private static final String XML_TAG_SMS_NUMBER_SKIP = "number_skip";
    private static final String XML_TAG_SMS_PROMPT_SWITCH = "smspromptswitch";
    private static List<String> sAlertAppList = new ArrayList();
    private static AlertDataFileListener sAlertAppListener;
    private static Boolean sAllowBackgroundRequest = Boolean.valueOf(false);
    private static Map<String, Integer> sBackgroundCameraSkipPkgs = new HashMap();
    private static List<String> sBackgroundSkipList = new ArrayList();
    private static List<String> sBackgroundSkipPermissions = new ArrayList();
    private static Boolean sDCIMProtectEnabled = Boolean.valueOf(true);
    private static ArrayList<String> sDefaultSkipPackages = new ArrayList();
    private static ArrayList<Integer> sDefaultSkipUids = new ArrayList();
    private static AlertDataFileListener sGameAppListener;
    private static List<String> sGameNameList = new ArrayList();
    private static List<String> sGameSuffixList = new ArrayList();
    private static boolean sIsBusinessCustom = false;
    private static boolean sIsBussinessNoAccountDialog = false;
    private static Boolean sIsCtaVersion = Boolean.valueOf(false);
    private static boolean sIsExVersion = false;
    private static volatile boolean sIsPermissionInterceptEnabled = SystemProperties.getBoolean("persist.sys.permission.enable", SystemProperties.getBoolean(KEY_CTSVERSION_PROPERTIES, false) ^ 1);
    private static LinkedList<PermissionCheckingMsg> sPermissionCheckMsgList = new LinkedList();
    private static OppoPermissionInterceptPolicy sPermissionInterceptPolicy;
    private static List<String> sPermissionsPrompt;
    private static Bundle sRecordData = null;
    private static int sRecordMsgCode = 0;
    private static List<String> sRegexSms = new ArrayList();
    private static List<String> sRegexSmsNumber = new ArrayList();
    private static List<String> sRegexSmsNumberSkip = new ArrayList();
    private static List<String> sRegexSmsSkip = new ArrayList();
    private static boolean sRejectChargeSms = false;
    private static List<String> sRejectDialogPermissionList = new ArrayList();
    private static List<String> sShellRevokedPermissions = new ArrayList();
    private static String sSmsChargeSwitch = SWITCH_SKIP_OPPO;
    private static String sSmsPromptSwitch = SWITCH_ON;
    private String mCallNumber = null;
    private final Context mContext;
    private String mCurrentCountry;
    private String mCurrentLanguage;
    private UEventObserver mDCIMDeleteEventObserver = new UEventObserver() {
        public void onUEvent(UEvent event) {
            OppoPermissionInterceptPolicy.this.mH.sendMessage(OppoPermissionInterceptPolicy.this.mH.obtainMessage(2, event));
        }
    };
    private Messenger mDialogService;
    private final H mH = new H();
    private HandlerThread mHandlerThread = new HandlerThread("PermissionThread", 10);
    private KeyguardManager mKeyguardManager;
    private Messenger mMessenger = new Messenger(new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 211:
                    if (OppoPermissionInterceptPolicy.this.mRecordPcm != null) {
                        Bundle bundle = msg.getData();
                        int res = bundle.getInt("res", 0);
                        boolean save = bundle.getBoolean("save", false);
                        OppoPermissionInterceptPolicy.this.processPermission(OppoPermissionInterceptPolicy.this.mRecordPcm, res, save);
                        if (OppoPermissionInterceptPolicy.DEBUG) {
                            Log.v(OppoPermissionInterceptPolicy.TAG, "MSG_PERMISSION_DIALOG_GET_RESULT " + OppoPermissionInterceptPolicy.this.mRecordPcm.mPr.info.packageName + " , " + res + " , " + save);
                        }
                        if (OppoPermissionInterceptPolicy.this.mRecordPcm.mPermission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && OppoPermissionInterceptPolicy.this.isChargeSmsByPkg(OppoPermissionInterceptPolicy.this.mRecordPcm.mPr.info.packageName, OppoPermissionInterceptPolicy.this.mSmsContent, OppoPermissionInterceptPolicy.this.mSmsDestination)) {
                            OppoPermissionInterceptPolicy.sRejectChargeSms = true;
                            OppoPermissionInterceptPolicy.this.mPermissionHandler.postDelayed(new Runnable() {
                                public void run() {
                                    OppoPermissionInterceptPolicy.sRejectChargeSms = false;
                                }
                            }, OppoPermissionInterceptPolicy.DELAY_THREE);
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    });
    private Handler mPendingMsgHandler;
    private HandlerThread mPendingMsgThread = new HandlerThread("PermissionMsgPendingThread", 10);
    private Handler mPermissionHandler;
    private final IPackageManager mPm;
    private PowerManager mPowerManager;
    private PermissionCheckingMsg mRecordPcm = null;
    private CheckBox mSaveCheckBox;
    private ServiceConnection mServConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(OppoPermissionInterceptPolicy.TAG, "onServiceConnected");
            OppoPermissionInterceptPolicy.this.mDialogService = new Messenger(service);
            if (OppoPermissionInterceptPolicy.sRecordMsgCode != 0 && OppoPermissionInterceptPolicy.sRecordData != null) {
                OppoPermissionInterceptPolicy.this.sendDialogMsg(OppoPermissionInterceptPolicy.sRecordMsgCode, OppoPermissionInterceptPolicy.sRecordData);
                OppoPermissionInterceptPolicy.sRecordMsgCode = 0;
                OppoPermissionInterceptPolicy.sRecordData = null;
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(OppoPermissionInterceptPolicy.TAG, "onServiceDisconnected");
            OppoPermissionInterceptPolicy.this.mDialogService = null;
        }
    };
    private final ActivityManagerService mService;
    private String mSmsContent = null;
    private String mSmsDestination = null;
    private Handler mTimerHandler;
    private HandlerThread mTimerThread = new HandlerThread("PermissionTimerThread", 10);

    private class AlertDataFileListener extends FileObserver {
        String mObserverPath = null;

        public AlertDataFileListener(String path) {
            super(path, DhcpPacket.MIN_PACKET_LENGTH_L3);
            this.mObserverPath = path;
        }

        public void onEvent(int event, String path) {
            switch (event) {
                case 8:
                    if (this.mObserverPath != null && this.mObserverPath.contains(OppoPermissionInterceptPolicy.ALERT_PERMISSION_APPS)) {
                        OppoPermissionInterceptPolicy.initAlertAppList(OppoPermissionInterceptPolicy.ALERT_PERMISSION_APPS);
                        return;
                    } else if (this.mObserverPath != null && this.mObserverPath.contains(OppoPermissionInterceptPolicy.GAME_FILTER_FILE_NAME)) {
                        OppoPermissionInterceptPolicy.initAlertAppList(OppoPermissionInterceptPolicy.GAME_FILTER_FILE_NAME);
                        return;
                    } else {
                        return;
                    }
                default:
                    return;
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

        public void run() {
            synchronized (this.mLock) {
                if (OppoPermissionInterceptPolicy.DEBUG) {
                    Log.d(OppoPermissionInterceptPolicy.TAG, "checkPermissionForProc Runnable");
                }
                ProcessRecord pr = OppoPermissionInterceptPolicy.this.getProcessForPid(this.mPid);
                PackagePermission pkgPm = null;
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
                    PermissionCheckingMsg pcm;
                    if (this.mPermission.equals(OppoPermissionInterceptPolicy.PERMISSION_ACCESS_MEDIA_PROVIDER)) {
                        if (OppoPermissionInterceptPolicy.sDCIMProtectEnabled.booleanValue()) {
                            if (!OppoPermissionInterceptPolicy.sDefaultSkipUids.contains(Integer.valueOf(this.mUid))) {
                                pcm = new PermissionCheckingMsg();
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
                                    pr.isWaitingPermissionChoice = true;
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
                        pkgPm = OppoPermissionInterceptPolicy.this.queryPackagePermissions(pr.info.packageName);
                        if (OppoPermissionInterceptPolicy.DEBUG) {
                            Log.d(OppoPermissionInterceptPolicy.TAG, "checkPermissionForProc, pr.mPackagePermission = null, query pkgPm=" + pkgPm);
                        }
                    } else {
                        this.mRes = 3;
                        if (OppoPermissionInterceptPolicy.DEBUG) {
                            Log.d(OppoPermissionInterceptPolicy.TAG, "checkPermissionForProc, pr.info is null!!! return INVALID_RES!!!");
                        }
                    }
                    if (pkgPm != null) {
                        pr.mPackagePermission = pkgPm;
                        pr.mPersistPackagePermission = pkgPm.copy();
                    }
                    if (pkgPm != null) {
                        long mask = OppoPermissionInterceptPolicy.this.getPermissionMask(this.mPermission);
                        ComponentName cn;
                        String foregroundPackageName;
                        if (this.mPermission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && (pkgPm.mReject & mask) == 0) {
                            if (OppoPermissionInterceptPolicy.sIsExVersion && (pkgPm.mAccept & mask) != 0) {
                                String defaultSms = Sms.getDefaultSmsPackage(OppoPermissionInterceptPolicy.this.mContext);
                                if (defaultSms != null && defaultSms.equals(pkgPm.mPackageName)) {
                                    this.mRes = 0;
                                    this.mLock.notifyAll();
                                    return;
                                }
                            }
                            if (OppoPermissionInterceptPolicy.sRejectChargeSms) {
                                this.mRes = 1;
                            } else {
                                pcm = new PermissionCheckingMsg();
                                pcm.mPermission = this.mPermission;
                                pcm.mPr = pr;
                                pcm.mUid = this.mUid;
                                pcm.mCallback = this.mCallback;
                                pcm.mPermissionMask = mask;
                                pcm.mToken = this.mToken;
                                synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                    OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm);
                                    pr.isWaitingPermissionChoice = true;
                                    if (OppoPermissionInterceptPolicy.DEBUG) {
                                        Log.d(OppoPermissionInterceptPolicy.TAG, "add pcm, size=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size() + ", sPermissionCheckMsgList=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList);
                                    }
                                    OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                    this.mRes = 2;
                                }
                            }
                        } else if (pkgPm.mTrust != 0) {
                            this.mRes = 0;
                            OppoPermissionInterceptPolicy.this.sendNormalSmsStatistic(pkgPm.mPackageName, this.mPermission);
                        } else if ((pkgPm.mAccept & mask) != 0) {
                            if (!tmpPermission.equals("android.permission.CAMERA_TAKEPICTURE")) {
                                this.mRes = 0;
                                OppoPermissionInterceptPolicy.this.sendNormalSmsStatistic(pkgPm.mPackageName, this.mPermission);
                            } else if (OppoPermissionInterceptPolicy.this.isScreenOn()) {
                                cn = null;
                                try {
                                    cn = new OppoActivityManager().getTopActivityComponentName();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                foregroundPackageName = pr.info.packageName;
                                if (cn != null) {
                                    foregroundPackageName = cn.getPackageName();
                                }
                                if (!(pr.info.packageName.contains("com.cttl.") || (pr.info.packageName.equals(foregroundPackageName) ^ 1) == 0)) {
                                    if ((OppoPermissionInterceptPolicy.sBackgroundCameraSkipPkgs.containsKey(pr.info.packageName) ^ 1) != 0) {
                                        if ((pr.info.packageName.equals(OppoPermissionInterceptPolicy.this.getCurrentFocus()) ^ 1) != 0) {
                                            if (!OppoPermissionInterceptPolicy.this.isEnabledInputMethod(OppoPermissionInterceptPolicy.this.mContext, pr.info.packageName)) {
                                                pcm = new PermissionCheckingMsg();
                                                pcm.mPermission = this.mPermission;
                                                pcm.mPr = pr;
                                                pcm.mUid = this.mUid;
                                                pcm.mCallback = this.mCallback;
                                                pcm.mPermissionMask = mask;
                                                pcm.mToken = this.mToken;
                                                pcm.mIsBackground = true;
                                                synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                                    OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm);
                                                    pr.isWaitingPermissionChoice = true;
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
                                    }
                                }
                                this.mRes = 0;
                            } else {
                                String pkgName = pkgPm.mPackageName;
                                if (OppoPermissionInterceptPolicy.sBackgroundCameraSkipPkgs.containsKey(pkgName)) {
                                    this.mRes = 0;
                                } else {
                                    this.mRes = 1;
                                    Log.d(OppoPermissionInterceptPolicy.TAG, "Reject background permission " + this.mPermission + "pkgName: " + pkgName);
                                }
                                OppoPermissionInterceptPolicy.this.statisticsScreenOffPermissionRequest(pkgName, takePicturePermission, OppoPermissionInterceptPolicy.VALUE_ACCEPT);
                            }
                        } else if ((pkgPm.mReject & mask) != 0) {
                            if (OppoPermissionInterceptPolicy.sRejectDialogPermissionList != null) {
                                if (OppoPermissionInterceptPolicy.sRejectDialogPermissionList.contains(this.mPermission)) {
                                    pcm = new PermissionCheckingMsg();
                                    pcm.mPermission = this.mPermission;
                                    pcm.mPr = pr;
                                    pcm.mUid = this.mUid;
                                    pcm.mCallback = this.mCallback;
                                    pcm.mPermissionMask = mask;
                                    pcm.mToken = this.mToken;
                                    pcm.mPkgPm = pkgPm;
                                    synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm);
                                        pr.isWaitingPermissionChoice = true;
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
                            this.mRes = 1;
                            ActivityManager manager = (ActivityManager) OppoPermissionInterceptPolicy.this.mContext.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY);
                            try {
                                List<RunningTaskInfo> runningTasks = new ArrayList();
                                if (manager.getRunningTasks(1) != null) {
                                    runningTasks.addAll(manager.getRunningTasks(1));
                                }
                                if (!(runningTasks == null || runningTasks.size() == 0 || runningTasks.get(0) == null)) {
                                    String packageName = ((RunningTaskInfo) runningTasks.get(0)).topActivity.getPackageName();
                                    if (this.mUid == OppoPermissionInterceptPolicy.this.mPm.getPackageUid(packageName, 0, 0) || (packageName.equals("com.wandoujia.phoenix2") && pr.info.packageName.equals("com.wandoujia.phoenix2.usbproxy"))) {
                                        if (packageName.equals("com.wandoujia.phoenix2") && pr.info.packageName.equals("com.wandoujia.phoenix2.usbproxy")) {
                                            packageName = "com.wandoujia.phoenix2.usbproxy";
                                            Log.i("AAA", "----equals--");
                                        }
                                        Intent intent = new Intent();
                                        intent.setAction("com.oppo.permissionprotect.notify");
                                        intent.putExtra("PackageName", packageName);
                                        intent.putExtra("Permission", this.mPermission);
                                        OppoPermissionInterceptPolicy.this.mContext.sendBroadcast(intent);
                                        Log.i("AAA", "Notify!!!");
                                    }
                                }
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            OppoPermissionInterceptPolicy.this.sendNormalSmsStatistic(pkgPm.mPackageName, this.mPermission);
                        } else if ((pkgPm.mPrompt & mask) != 0) {
                            if (OppoPermissionInterceptPolicy.this.isScreenOn()) {
                                if (!(OppoPermissionInterceptPolicy.sIsCtaVersion.booleanValue() || (OppoPermissionInterceptPolicy.sAllowBackgroundRequest.booleanValue() ^ 1) == 0 || (pr.info.packageName.contains("com.cttl.") ^ 1) == 0)) {
                                    int contains;
                                    if (OppoPermissionInterceptPolicy.sIsExVersion) {
                                        contains = OppoPermissionInterceptPolicy.sBackgroundSkipPermissions.contains(this.mPermission);
                                    } else {
                                        contains = 0;
                                    }
                                    if ((contains ^ 1) != 0) {
                                        cn = null;
                                        try {
                                            cn = new OppoActivityManager().getTopActivityComponentName();
                                        } catch (Exception e22) {
                                            e22.printStackTrace();
                                        }
                                        foregroundPackageName = pr.info.packageName;
                                        if (cn != null) {
                                            foregroundPackageName = cn.getPackageName();
                                        }
                                        if (!(OppoPermissionInterceptPolicy.sBackgroundSkipList.contains(pr.info.packageName) || (pr.info.packageName.equals(foregroundPackageName) ^ 1) == 0)) {
                                            if ((OppoPermissionInterceptPolicy.this.isEnabledInputMethod(OppoPermissionInterceptPolicy.this.mContext, pr.info.packageName) ^ 1) != 0) {
                                                if ((OppoPermissionInterceptPolicy.this.getCurrentFocus().contains(pr.info.packageName) ^ 1) != 0) {
                                                    this.mRes = 1;
                                                    Log.d(OppoPermissionInterceptPolicy.TAG, "not foreground app, reject it! app is " + pr.info.packageName);
                                                    this.mLock.notifyAll();
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                                pcm = new PermissionCheckingMsg();
                                pcm.mPermission = this.mPermission;
                                pcm.mPr = pr;
                                pcm.mUid = this.mUid;
                                pcm.mCallback = this.mCallback;
                                pcm.mPermissionMask = mask;
                                pcm.mToken = this.mToken;
                                synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                    OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm);
                                    pr.isWaitingPermissionChoice = true;
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
                    }
                    if (3 == this.mRes) {
                        if (OppoPermissionConstants.PERMISSION_ACCESS.equals(this.mPermission)) {
                            this.mRes = 0;
                        }
                    }
                    this.mLock.notifyAll();
                }
            }
        }
    }

    private class ChoiceCountDownTimer extends CountDownTimer {
        PermissionCheckingMsg mPcm;
        AlertDialog mPermissionChoiceDialog;
        Button mRejectBtn;

        public ChoiceCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onFinish() {
            this.mPermissionChoiceDialog.dismiss();
            OppoPermissionInterceptPolicy.this.processPermission(this.mPcm, 1, OppoPermissionInterceptPolicy.this.mSaveCheckBox.isChecked());
        }

        public void onTick(long millisUntilFinished) {
            String str = OppoPermissionInterceptPolicy.this.mContext.getString(201589895) + "(" + (millisUntilFinished / 1000) + "s)";
            if (OppoPermissionInterceptPolicy.this.isScreenOn()) {
                this.mRejectBtn.setText(str);
            } else {
                onFinish();
            }
        }
    }

    final class H extends Handler {
        public static final int NOTIFY_DCIM_DELETE_EVENT = 2;

        H() {
        }

        public void handleMessage(Message msg) {
            if (OppoPermissionInterceptPolicy.DEBUG) {
                Slog.v(OppoPermissionInterceptPolicy.TAG, "DCIM Protection Event Handler Message: what=" + msg.what);
            }
            switch (msg.what) {
                case 2:
                    UEvent event = msg.obj;
                    String uid = event.get("UID");
                    String pid = event.get("PID");
                    String path = event.get("PATH");
                    Slog.d(OppoPermissionInterceptPolicy.TAG, "NOTIFY_DCIM_DELETE_EVENT uid " + uid);
                    Slog.d(OppoPermissionInterceptPolicy.TAG, "NOTIFY_DCIM_DELETE_EVENT pid " + pid);
                    Slog.d(OppoPermissionInterceptPolicy.TAG, "NOTIFY_DCIM_DELETE_EVENT path " + path);
                    try {
                        path = new String(path.getBytes("ISO-8859-1"), "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt("uid", Integer.valueOf(uid).intValue());
                    bundle.putInt("pid", Integer.valueOf(pid).intValue());
                    bundle.putString("path", path);
                    OppoPermissionInterceptPolicy.this.sendDialogMsg(104, bundle);
                    return;
                default:
                    return;
            }
        }
    }

    class PackagePermission {
        long mAccept;
        int mId;
        String mPackageName;
        long mPrompt;
        long mReject;
        int mTrust;

        PackagePermission() {
        }

        public PackagePermission copy() {
            PackagePermission copy = new PackagePermission();
            copy.mId = this.mId;
            copy.mPackageName = this.mPackageName;
            copy.mAccept = this.mAccept;
            copy.mReject = this.mReject;
            copy.mPrompt = this.mPrompt;
            copy.mTrust = this.mTrust;
            return copy;
        }

        public String toString() {
            return "[mPackageName=" + this.mPackageName + ", mAccept=" + this.mAccept + ", mReject=" + this.mReject + ", mPrompt=" + this.mPrompt + ", mTrust=" + this.mTrust + "]";
        }
    }

    class PermissionCheckingMsg {
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
            if (this.mPermission == null || o == null || !this.mPermission.equals(o.mPermission) || this.mUid != o.mUid) {
                return false;
            }
            return true;
        }

        public String toString() {
            return "[mPermission=" + this.mPermission + ", mPr=" + this.mPr + ", mUid=" + this.mUid + ", mPermissionMask=" + this.mPermissionMask + ", mToken=" + this.mToken + ", mCallback=" + this.mCallback + "]";
        }
    }

    private class UpdateMsgDate {
        int mChoice;
        String mPackageName;
        String mPermission;

        /* synthetic */ UpdateMsgDate(OppoPermissionInterceptPolicy this$0, UpdateMsgDate -this1) {
            this();
        }

        private UpdateMsgDate() {
        }
    }

    public static OppoPermissionInterceptPolicy getInstance(ActivityManagerService service) {
        if (sPermissionInterceptPolicy == null) {
            sPermissionInterceptPolicy = new OppoPermissionInterceptPolicy(service);
        }
        return sPermissionInterceptPolicy;
    }

    private OppoPermissionInterceptPolicy(ActivityManagerService service) {
        this.mService = service;
        this.mContext = service.mContext;
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.system.cmcc.test")) {
            sIsPermissionInterceptEnabled = false;
        }
        sIsBusinessCustom = this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom");
        sIsBussinessNoAccountDialog = this.mContext.getPackageManager().hasSystemFeature("oppo.settings.account.dialog.disallow");
        SystemProperties.set("persist.sys.permission.enable", String.valueOf(sIsPermissionInterceptEnabled));
        this.mPm = AppGlobals.getPackageManager();
        this.mCurrentLanguage = Locale.getDefault().getLanguage();
        this.mCurrentCountry = Locale.getDefault().getCountry();
        sPermissionsPrompt = Arrays.asList(this.mContext.getResources().getStringArray(201786381));
        this.mHandlerThread.start();
        this.mPermissionHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        synchronized (OppoPermissionInterceptPolicy.this) {
                            UpdateMsgDate umd = msg.obj;
                            PackagePermission pkgPm = null;
                            long permissionMask = OppoPermissionInterceptPolicy.this.getPermissionMask(umd.mPermission);
                            ProcessRecord pr = OppoPermissionInterceptPolicy.this.getProcessForPackageName(umd.mPackageName);
                            if (!(pr == null || pr.mPackagePermission == null)) {
                                OppoPermissionInterceptPolicy.this.changePermissionChoice(pr.mPackagePermission, permissionMask, umd.mChoice);
                                OppoPermissionInterceptPolicy.this.changePermissionChoice(pr.mPersistPackagePermission, permissionMask, umd.mChoice);
                                pr.isSelected = (int) (((long) pr.isSelected) & (~permissionMask));
                                pkgPm = pr.mPersistPackagePermission;
                            }
                            if (pkgPm == null) {
                                pkgPm = OppoPermissionInterceptPolicy.this.queryPackagePermissions(umd.mPackageName);
                                if (pkgPm != null) {
                                    OppoPermissionInterceptPolicy.this.changePermissionChoice(pkgPm, permissionMask, umd.mChoice);
                                }
                            }
                            if (OppoPermissionInterceptPolicy.DEBUG) {
                                Log.d(OppoPermissionInterceptPolicy.TAG, "UPDATE_PERMISSION_CHOICE, pkgPm=" + pkgPm);
                            }
                            if (pkgPm != null) {
                                OppoPermissionInterceptPolicy.this.savePermissionChoice(pkgPm);
                            }
                        }
                        return;
                    case 4:
                        try {
                            SystemServer.addService(OppoPermissionInterceptPolicy.this.mService);
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    default:
                        return;
                }
            }
        };
        this.mPendingMsgThread.start();
        this.mPendingMsgHandler = new Handler(this.mPendingMsgThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                            if (OppoPermissionInterceptPolicy.DEBUG) {
                                Log.d(OppoPermissionInterceptPolicy.TAG, "handleMessage DO_CHECK_PERMISSION,  size=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size() + ", sPermissionCheckMsgList=" + OppoPermissionInterceptPolicy.sPermissionCheckMsgList);
                            }
                            if (OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size() >= 1) {
                                PermissionCheckingMsg pcm = (PermissionCheckingMsg) OppoPermissionInterceptPolicy.sPermissionCheckMsgList.removeFirst();
                                if (pcm != null) {
                                    OppoPermissionInterceptPolicy.this.mRecordPcm = pcm;
                                    boolean isScreenOn = OppoPermissionInterceptPolicy.this.isScreenOn();
                                    if ((OppoPermissionInterceptPolicy.this.mKeyguardManager != null && OppoPermissionInterceptPolicy.this.mKeyguardManager.inKeyguardRestrictedInputMode()) || (isScreenOn ^ 1) != 0) {
                                        String currentFocus = OppoPermissionInterceptPolicy.this.getCurrentFocus();
                                        if (currentFocus.equals("com.android.systemui") || currentFocus.equals(OppoPermissionInterceptPolicy.PERMISSION_PACKGE_NAME) || (currentFocus.equals(pcm.mPr.info.packageName) ^ 1) != 0 || (isScreenOn ^ 1) != 0) {
                                            OppoPermissionInterceptPolicy.this.notifyWaitingApp(OppoPermissionInterceptPolicy.this.mRecordPcm, 1);
                                            Log.d(OppoPermissionInterceptPolicy.TAG, "behind keyguard, reject it! app is " + pcm.mPr.info.packageName);
                                            return;
                                        }
                                    }
                                    try {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("packageName", pcm.mPr.info.packageName);
                                        bundle.putString("permission", pcm.mPermission);
                                        bundle.putString("smsContent", OppoPermissionInterceptPolicy.this.mSmsContent);
                                        bundle.putString("smsDestination", OppoPermissionInterceptPolicy.this.mSmsDestination);
                                        bundle.putString("callNumber", OppoPermissionInterceptPolicy.this.mCallNumber);
                                        bundle.putBoolean("isBackground", pcm.mIsBackground);
                                        bundle.putInt("uid", pcm.mUid);
                                        if (pcm.mContentUri != null) {
                                            bundle.putString("contentUri", pcm.mContentUri);
                                        }
                                        if (pcm.mSelection != null) {
                                            bundle.putString("selection", pcm.mSelection);
                                        }
                                        if (pcm.mSelectionArgs != null) {
                                            bundle.putString("selectionArgs", pcm.mSelectionArgs);
                                        }
                                        if (pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && OppoPermissionInterceptPolicy.this.isChargeSmsByPkg(pcm.mPr.info.packageName, OppoPermissionInterceptPolicy.this.mSmsContent, OppoPermissionInterceptPolicy.this.mSmsDestination)) {
                                            bundle.putBoolean("isChargeSms", true);
                                        } else {
                                            bundle.putBoolean("isChargeSms", false);
                                        }
                                        if (OppoPermissionInterceptPolicy.sRejectDialogPermissionList != null && OppoPermissionInterceptPolicy.sRejectDialogPermissionList.contains(pcm.mPermission)) {
                                            if (pcm.mPkgPm == null || (pcm.mPkgPm.mReject & pcm.mPermissionMask) == 0) {
                                                bundle.putBoolean("isRectDialog", false);
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
                                        if (!pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                                            if (!pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_CAMERA)) {
                                                OppoPermissionInterceptPolicy.sPermissionCheckMsgList.wait(OppoPermissionInterceptPolicy.WATI_SHORT);
                                                break;
                                            } else {
                                                OppoPermissionInterceptPolicy.sPermissionCheckMsgList.wait(OppoPermissionInterceptPolicy.WAIT_LONG);
                                                break;
                                            }
                                        }
                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.wait(OppoPermissionInterceptPolicy.WAIT_LONG);
                                        break;
                                    } catch (InterruptedException e2) {
                                        break;
                                    }
                                }
                                return;
                            }
                            return;
                        }
                        break;
                }
                return;
                return;
            }
        };
        this.mTimerThread.start();
        this.mTimerHandler = new Handler(this.mTimerThread.getLooper());
        sIsCtaVersion = Boolean.valueOf(this.mContext.getPackageManager().hasSystemFeature("oppo.cta.support"));
        sIsExVersion = this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
        if (sBackgroundSkipList != null && sBackgroundSkipList.isEmpty()) {
            sBackgroundSkipList = new ArrayList(Arrays.asList(new String[]{"com.sogou.speech.offlineservice", "com.google.android.gms", "com.google.android.gsf", "com.google.android.gsf.login", "com.google.android.syncadapters.calendar", "com.google.android.syncadapters.contacts", "com.appstar.callrecorder", "com.jiochat.jiochatapp", "com.nll.acr", "com.tencent.mobileqq", "com.tencent.mm"}));
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
        if (sBackgroundCameraSkipPkgs != null && sBackgroundCameraSkipPkgs.isEmpty()) {
            sBackgroundCameraSkipPkgs.put("com.tencent.mm", Integer.valueOf(1));
            sBackgroundCameraSkipPkgs.put("com.tencent.mobileqq", Integer.valueOf(1));
            sBackgroundCameraSkipPkgs.put("jp.naver.line.android", Integer.valueOf(1));
            sBackgroundCameraSkipPkgs.put("com.viber.voip", Integer.valueOf(1));
            sBackgroundCameraSkipPkgs.put("com.whatsapp", Integer.valueOf(1));
        }
        if (sRejectDialogPermissionList != null && sRejectDialogPermissionList.isEmpty()) {
            sRejectDialogPermissionList = new ArrayList(Arrays.asList(new String[]{OppoPermissionConstants.PERMISSION_CAMERA, OppoPermissionConstants.PERMISSION_RECORD_AUDIO}));
        }
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        startDCIMDeleteEventObserving();
        initDCIMProtectWhiteList();
        addAthenaService();
    }

    private void initDCIMProtectWhiteList() {
        sDefaultSkipPackages.add("com.oppo.camera");
        sDefaultSkipPackages.add("com.android.providers.media");
        sDefaultSkipPackages.add("com.coloros.filemanager");
        sDefaultSkipPackages.add("com.coloros.cloud");
        sDefaultSkipPackages.add("com.coloros.gallery3d");
        sDefaultSkipPackages.add("com.coloros.video");
        sDefaultSkipPackages.add("com.google.android.apps.photos");
        for (String packageName : sDefaultSkipPackages) {
            try {
                ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfo(packageName, 0);
                if (appInfo != null) {
                    sDefaultSkipUids.add(Integer.valueOf(appInfo.uid));
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addToDCIMProtectWhiteList(String packageName) {
        try {
            ApplicationInfo appInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, 0);
            if (appInfo != null) {
                sDefaultSkipUids.add(Integer.valueOf(appInfo.uid));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAthenaService() {
        Message msg = this.mPermissionHandler.obtainMessage();
        msg.what = 4;
        this.mPermissionHandler.sendMessage(msg);
    }

    public int checkPermissionForProc(String permission, int pid, int uid, int token, OppoPermissionCallback callback) {
        String contentUri = null;
        String selection = null;
        String selectionArgs = null;
        if (permission.startsWith(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
            if (permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                this.mSmsContent = null;
                this.mSmsDestination = null;
            } else {
                this.mSmsContent = permission.substring(OppoPermissionConstants.PERMISSION_SEND_SMS.length(), permission.length());
                int separator = this.mSmsContent.lastIndexOf(SMS_SEPARATOR);
                if (separator != -1) {
                    this.mSmsDestination = this.mSmsContent.substring(separator + 1);
                    this.mSmsContent = this.mSmsContent.substring(0, separator);
                }
                permission = OppoPermissionConstants.PERMISSION_SEND_SMS;
            }
        } else {
            if (permission.startsWith(OppoPermissionConstants.PERMISSION_CALL_PHONE)) {
                if ((permission.equals(OppoPermissionConstants.PERMISSION_CALL_PHONE) ^ 1) != 0) {
                    this.mCallNumber = permission.substring(OppoPermissionConstants.PERMISSION_CALL_PHONE.length(), permission.length());
                    this.mCallNumber = this.mCallNumber.substring(this.mCallNumber.indexOf(":") + 1).trim();
                    permission = OppoPermissionConstants.PERMISSION_CALL_PHONE;
                }
            }
            if (permission.startsWith(PERMISSION_ACCESS_MEDIA_PROVIDER)) {
                if (DEBUG) {
                    Log.d(TAG, "PERMISSION_ACCESS_MEDIA_PROVIDER " + permission);
                }
                String[] extraStringArray = permission.split(SMS_SEPARATOR);
                if (extraStringArray.length > 1) {
                    contentUri = extraStringArray[1];
                    if (extraStringArray.length == 3) {
                        selection = extraStringArray[2];
                    } else if (extraStringArray.length == 4) {
                        selection = extraStringArray[2];
                        selectionArgs = extraStringArray[3];
                    }
                    permission = PERMISSION_ACCESS_MEDIA_PROVIDER;
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "checkPermissionForProc, permission=" + permission + ", uid=" + uid + ",  pid=" + pid + ", token=" + token + ", callback=" + callback);
        }
        if (uid < 10000 || (sIsPermissionInterceptEnabled ^ 1) != 0) {
            return 3;
        }
        int result;
        try {
            Object lock = new Object();
            synchronized (lock) {
                CheckPermissionRunnable queryRunnable = new CheckPermissionRunnable(lock, permission, pid, uid, token, callback);
                if (permission.equals(PERMISSION_ACCESS_MEDIA_PROVIDER)) {
                    queryRunnable.setContentUri(contentUri);
                    queryRunnable.setSelection(selection);
                    queryRunnable.setSelectionArgs(selectionArgs);
                }
                this.mPermissionHandler.post(queryRunnable);
                if (permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                    lock.wait(WAIT_LONG);
                } else {
                    if (permission.equals(OppoPermissionConstants.PERMISSION_CAMERA)) {
                        lock.wait(WAIT_LONG);
                    } else {
                        lock.wait(WATI_SHORT);
                    }
                }
                result = queryRunnable.mRes;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = 3;
        }
        if (DEBUG) {
            Log.d(TAG, "checkPermissionForProc, return " + result);
        }
        return result;
    }

    public void updatePermissionChoice(String packageName, String permission, int choice) {
        if (DEBUG) {
            Log.d(TAG, "updatePermissionChoice, packageName=" + packageName + ", permission=" + permission + ", choice=" + choice);
        }
        if (packageName != null && permission != null) {
            UpdateMsgDate umd = new UpdateMsgDate(this, null);
            umd.mPackageName = packageName;
            umd.mPermission = permission;
            umd.mChoice = choice;
            Message msg = this.mPermissionHandler.obtainMessage();
            msg.what = 1;
            msg.obj = umd;
            this.mPermissionHandler.sendMessage(msg);
        }
    }

    private int getPermissionState(String packageName, String permission) {
        int retValue = 3;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, null, "pkg_name=?", new String[]{packageName}, null);
            if (cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return 3;
            }
            cursor.moveToFirst();
            long valueAccept = cursor.getLong(cursor.getColumnIndex("accept"));
            long valueReject = cursor.getLong(cursor.getColumnIndex(STATISTIC_REJECT));
            long valuePrompt = cursor.getLong(cursor.getColumnIndex("prompt"));
            if (0 != (getPermissionMask(permission) & valueAccept)) {
                retValue = 1;
            } else if (0 != (getPermissionMask(permission) & valueReject)) {
                retValue = 2;
            } else if (0 != (getPermissionMask(permission) & valuePrompt)) {
                retValue = 0;
            }
            if (cursor != null) {
                cursor.close();
            }
            return retValue;
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void grantOppoPermissionsFromRuntime(String packageName, String permissionName) {
        PermissionInfo permissionInfo = null;
        try {
            permissionInfo = this.mContext.getPackageManager().getPermissionInfo(permissionName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (permissionInfo != null) {
            String permissionGroup = permissionInfo.group;
        }
        List<String> listOppoPermissions = (List) OppoPermissionConstants.MAP_DANGEROUS_PERMISSON_GROUP.get(permissionInfo.group);
        if (listOppoPermissions != null) {
            for (int index = 0; index < listOppoPermissions.size(); index++) {
                String oppoPermission = (String) listOppoPermissions.get(index);
                if (getPermissionState(packageName, oppoPermission) != 3) {
                    updatePermissionChoice(packageName, oppoPermission, 0);
                }
            }
        }
    }

    public void revokeOppoPermissionsFromRuntime(String packageName, String permissionName) {
        Object permissionGroup = null;
        PermissionInfo permissionInfo = null;
        try {
            permissionInfo = this.mContext.getPackageManager().getPermissionInfo(permissionName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (permissionInfo != null) {
            permissionGroup = permissionInfo.group;
        }
        List<String> listOppoPermissions = (List) OppoPermissionConstants.MAP_DANGEROUS_PERMISSON_GROUP.get(permissionGroup);
        if (listOppoPermissions != null) {
            for (int index = 0; index < listOppoPermissions.size(); index++) {
                String oppoPermission = (String) listOppoPermissions.get(index);
                if (getPermissionState(packageName, oppoPermission) != 3) {
                    updatePermissionChoice(packageName, oppoPermission, 2);
                }
            }
        }
    }

    public void setPermissionInterceptEnable(boolean enabled) {
        if (sIsPermissionInterceptEnabled != enabled) {
            sIsPermissionInterceptEnabled = enabled;
            SystemProperties.set("persist.sys.permission.enable", String.valueOf(enabled));
        }
    }

    public boolean isPermissionInterceptEnabled() {
        return sIsPermissionInterceptEnabled;
    }

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

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private PackagePermission queryPackagePermissions(String packageName) {
        Exception ex;
        Throwable th;
        Cursor cursor = null;
        PackagePermission packagePermission = null;
        try {
            cursor = this.mContext.getContentResolver().query(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, null, "pkg_name=?", new String[]{packageName}, null);
            if (cursor != null && cursor.getCount() == 1 && cursor.moveToNext()) {
                PackagePermission pkgPermission = new PackagePermission();
                try {
                    pkgPermission.mId = cursor.getInt(0);
                    pkgPermission.mPackageName = cursor.getString(1);
                    pkgPermission.mAccept = cursor.getLong(2);
                    pkgPermission.mReject = cursor.getLong(3);
                    pkgPermission.mPrompt = cursor.getLong(4);
                    pkgPermission.mTrust = cursor.getInt(6);
                    packagePermission = pkgPermission;
                } catch (Exception e) {
                    ex = e;
                    packagePermission = pkgPermission;
                    try {
                        ex.printStackTrace();
                        if (cursor != null) {
                        }
                        return packagePermission;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor != null) {
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            ex = e2;
            ex.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
            return packagePermission;
        }
        return packagePermission;
    }

    private void savePermissionChoice(PackagePermission packagePermission) {
        String selection = "pkg_name=?";
        String[] selectionArgs = new String[]{packagePermission.mPackageName};
        ContentValues values = new ContentValues();
        values.put("accept", Long.valueOf(packagePermission.mAccept));
        values.put(STATISTIC_REJECT, Long.valueOf(packagePermission.mReject));
        values.put("prompt", Long.valueOf(packagePermission.mPrompt));
        if (DEBUG) {
            Log.d(TAG, "savePermissionChoice, values=" + values);
        }
        try {
            this.mContext.getContentResolver().update(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, values, selection, selectionArgs);
        } catch (Exception ex) {
            Log.e(TAG, "savePermissionChoice error !");
            ex.printStackTrace();
        }
    }

    /* JADX WARNING: Missing block: B:21:0x003c, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ProcessRecord getProcessForPackageName(String packageName) {
        try {
            synchronized (this.mService) {
                ActivityManagerService.boostPriorityForLockedSection();
                int i = this.mService.mLruProcesses.size() - 1;
                while (i >= 0) {
                    ProcessRecord pr = (ProcessRecord) this.mService.mLruProcesses.get(i);
                    if (pr == null || pr.info == null || pr.info.packageName == null || !pr.info.packageName.equals(packageName)) {
                        i--;
                    } else {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return pr;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            ActivityManagerService.resetPriorityAfterLockedSection();
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:17:0x002e, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ProcessRecord getProcessForPid(int pid) {
        try {
            synchronized (this.mService) {
                ActivityManagerService.boostPriorityForLockedSection();
                int i = this.mService.mLruProcesses.size() - 1;
                while (i >= 0) {
                    ProcessRecord rec = (ProcessRecord) this.mService.mLruProcesses.get(i);
                    if (rec.thread == null || rec.pid != pid) {
                        i--;
                    } else {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return rec;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable th) {
            ActivityManagerService.resetPriorityAfterLockedSection();
        }
        return null;
    }

    private String getPackageLabel(ProcessRecord pr) {
        if (pr == null || pr.info == null) {
            return null;
        }
        try {
            ApplicationInfo ai = this.mContext.getPackageManager().getApplicationInfo(pr.info.packageName, 1024);
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
        if (this.mCurrentLanguage == null || this.mCurrentCountry == null || (this.mCurrentLanguage.equals(language) ^ 1) != 0 || (this.mCurrentCountry.equals(country) ^ 1) != 0) {
            this.mCurrentLanguage = language;
            this.mCurrentCountry = country;
            sPermissionsPrompt = Arrays.asList(this.mContext.getResources().getStringArray(201786381));
        }
        int index = OppoPermissionManager.sInterceptingPermissions.indexOf(permission);
        String text = "";
        if (index <= -1 || index >= sPermissionsPrompt.size()) {
            return text;
        }
        return (String) sPermissionsPrompt.get(index);
    }

    private void notifyWaitingApp(PermissionCheckingMsg pcm, int res) {
        try {
            OppoPermissionCallback callBack = pcm.mCallback;
            if (callBack != null) {
                pcm.mPr.isWaitingPermissionChoice = false;
                if (DEBUG) {
                    Slog.d(TAG, "notifyWaitingApp, pcm=" + pcm + ", res=" + res);
                }
                callBack.notifyApplication(pcm.mPermission, pcm.mPr.pid, res, pcm.mToken);
            }
        } catch (Exception e) {
            Slog.w(TAG, e);
        }
    }

    private void processPermission(PermissionCheckingMsg pcm, int res, boolean save) {
        String[] data;
        if (DEBUG) {
            Log.d(TAG, "processPermission, pcm=" + pcm + ", res=" + res + ", save=" + save);
        }
        synchronized (this) {
            if (save) {
                changePermissionChoice(pcm.mPr.mPackagePermission, pcm.mPermissionMask, res);
                changePermissionChoice(pcm.mPr.mPersistPackagePermission, pcm.mPermissionMask, res);
                savePermissionChoice(pcm.mPr.mPersistPackagePermission);
                ProcessRecord processRecord = pcm.mPr;
                processRecord.isSelected = (int) (((long) processRecord.isSelected) & (~pcm.mPermissionMask));
                try {
                    if (sPermissionCheckMsgList != null && sPermissionCheckMsgList.size() > 0) {
                        PermissionCheckingMsg nextPcm = (PermissionCheckingMsg) sPermissionCheckMsgList.getFirst();
                        if (nextPcm != null && nextPcm.mPr.mPersistPackagePermission.mPackageName.equals(pcm.mPr.mPersistPackagePermission.mPackageName)) {
                            changePermissionChoice(nextPcm.mPr.mPackagePermission, pcm.mPermissionMask, res);
                            changePermissionChoice(nextPcm.mPr.mPersistPackagePermission, pcm.mPermissionMask, res);
                        }
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
                PermissionCheckingMsg temp = (PermissionCheckingMsg) iterator.next();
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
        if (pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && isChargeSmsByPkg(pcm.mPr.info.packageName, this.mSmsContent, this.mSmsDestination)) {
            bundle.putInt("type", 1);
            data = new String[]{pcm.mPr.info.packageName, this.mSmsContent, this.mSmsDestination};
        } else {
            bundle.putInt("type", 0);
            data = new String[]{pcm.mPr.info.packageName, pcm.mPermission};
            bundle.putBoolean(STATISTIC_CHECKED, save);
        }
        bundle.putInt(STATISTIC_REJECT, res);
        bundle.putStringArray("data", data);
        intent.putExtras(bundle);
        this.mContext.sendBroadcast(intent);
        return;
    }

    private boolean isScreenOn() {
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        }
        if (this.mPowerManager != null) {
            return this.mPowerManager.isScreenOn();
        }
        return false;
    }

    private void showPermissionWindow(final PermissionCheckingMsg pcm) {
        this.mTimerHandler.post(new Runnable() {
            public void run() {
                OppoPermissionInterceptPolicy.this.showWindow(pcm);
            }
        });
    }

    private final void showWindow(PermissionCheckingMsg pcm) {
        if (DEBUG) {
            Log.d(TAG, "showWindow, pcm=" + pcm);
        }
        View parentView = LayoutInflater.from(this.mContext).inflate(201917501, null);
        TextView permissiomPrompt = (TextView) parentView.findViewById(201458799);
        String packageLabel = getPackageLabel(pcm.mPr);
        String securitystr = this.mContext.getString(201589892);
        permissiomPrompt.setText(packageLabel + securitystr + getPermissionPromptStr(pcm.mPermission));
        this.mSaveCheckBox = (CheckBox) parentView.findViewById(201458801);
        if (pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_READ_SMS) || pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_READ_MMS) || pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_WRITE_MMS) || pcm.mPermission.equals(OppoPermissionConstants.PERMISSION_READ_CONTACTS)) {
            Log.d(TAG, "when permission is SMS,owWindow, set checkbox is true");
            this.mSaveCheckBox.setChecked(true);
        }
        String rejectStr = this.mContext.getString(201589895);
        String acceptStr = this.mContext.getString(201589896);
        final ChoiceCountDownTimer mCountDownTimer = new ChoiceCountDownTimer(WATI_SHORT, 1000);
        Builder builder = new Builder(this.mContext, 201523207);
        builder.setView(parentView);
        final PermissionCheckingMsg permissionCheckingMsg = pcm;
        builder.setPositiveButton(acceptStr, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                mCountDownTimer.cancel();
                Handler -get6 = OppoPermissionInterceptPolicy.this.mPermissionHandler;
                final PermissionCheckingMsg permissionCheckingMsg = permissionCheckingMsg;
                -get6.post(new Runnable() {
                    public void run() {
                        OppoPermissionInterceptPolicy.this.processPermission(permissionCheckingMsg, 0, OppoPermissionInterceptPolicy.this.mSaveCheckBox.isChecked());
                    }
                });
            }
        });
        permissionCheckingMsg = pcm;
        builder.setNegativeButton(rejectStr, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                mCountDownTimer.cancel();
                Handler -get6 = OppoPermissionInterceptPolicy.this.mPermissionHandler;
                final PermissionCheckingMsg permissionCheckingMsg = permissionCheckingMsg;
                -get6.post(new Runnable() {
                    public void run() {
                        OppoPermissionInterceptPolicy.this.processPermission(permissionCheckingMsg, 1, OppoPermissionInterceptPolicy.this.mSaveCheckBox.isChecked());
                    }
                });
            }
        });
        builder.setTitle(this.mContext.getString(201589891));
        builder.setCancelable(false);
        AlertDialog permissionChoiceDialog = builder.create();
        permissionChoiceDialog.getWindow().getAttributes().setTitle("Permission Intercept");
        permissionChoiceDialog.getWindow().setType(2010);
        permissionChoiceDialog.show();
        mCountDownTimer.mRejectBtn = permissionChoiceDialog.getButton(-2);
        mCountDownTimer.mPermissionChoiceDialog = permissionChoiceDialog;
        mCountDownTimer.mPcm = pcm;
        mCountDownTimer.start();
    }

    public static void adjustThirdList(List thirdList, List res, String string) {
        Iterator iter = thirdList.iterator();
        while (iter.hasNext()) {
            ResolveInfo thirdApk = iter.next();
            if (thirdApk instanceof ResolveInfo) {
                ResolveInfo ri = thirdApk;
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
        int i = 0;
        if (receivers == null) {
            return null;
        }
        List<Object> systemList = new ArrayList();
        List<Object> thirdList = new ArrayList();
        List res = new ArrayList();
        String[] packageNames = new String[]{"com.qihoo360.mobilesafe", "com.anguanjia.safe", "com.blovestorm", "com.cootek.smartdialer", "com.sg.sledog"};
        for (ResolveInfo temp : receivers) {
            if (temp instanceof ResolveInfo) {
                ResolveInfo ri = temp;
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
        int length = packageNames.length;
        while (i < length) {
            adjustThirdList(thirdList, res, packageNames[i]);
            i++;
        }
        for (Object o : systemList) {
            res.add(o);
        }
        for (Object o2 : thirdList) {
            res.add(o2);
        }
        return res;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00bf A:{SYNTHETIC, Splitter: B:43:0x00bf} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00b3 A:{SYNTHETIC, Splitter: B:37:0x00b3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void initAlertAppList(String fileName) {
        Exception ex;
        Throwable th;
        if (DEBUG) {
            Log.d(TAG, "initAlertAppList");
        }
        File dataFile = new File(ALERT_PERMISSION_DATA_DIR + fileName);
        if (dataFile.exists()) {
            FileInputStream inputStream = null;
            try {
                FileInputStream inputStream2 = new FileInputStream(dataFile);
                try {
                    readDataFromXML(inputStream2, fileName);
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                        }
                    }
                    inputStream = inputStream2;
                } catch (Exception e) {
                    ex2 = e;
                    inputStream = inputStream2;
                    try {
                        ex2.printStackTrace();
                        Log.e(TAG, "initAlertAppList err !!");
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception ex22) {
                                ex22.printStackTrace();
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = inputStream2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception ex222) {
                            ex222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e2) {
                ex222 = e2;
                ex222.printStackTrace();
                Log.e(TAG, "initAlertAppList err !!");
                if (inputStream != null) {
                }
                return;
            }
            return;
        }
        if (DEBUG) {
            Log.d(TAG, dataFile.getName() + " does not exist!");
        }
        if (fileName.equals(ALERT_PERMISSION_APPS) && (sRegexSms.contains(DEFAULT_SMS_REGEX) ^ 1) != 0) {
            sRegexSms.add(DEFAULT_SMS_REGEX);
        }
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            dataFile.createNewFile();
        } catch (Exception ex2222) {
            Log.e(TAG, "init data error.");
            ex2222.printStackTrace();
        }
    }

    private static void readDataFromXML(FileInputStream stream, String fileName) {
        if (DEBUG) {
            Log.d(TAG, "readDataFromXML");
        }
        boolean regexSmsUpdated = false;
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
            } else {
                if (fileName.equals(GAME_FILTER_FILE_NAME)) {
                    sGameSuffixList.clear();
                    sGameNameList.clear();
                }
            }
            List<String> tmpRejectDialogPermissionList = new ArrayList();
            int type;
            do {
                type = parser.next();
                if (type == 2) {
                    String tag = parser.getName();
                    Boolean flag;
                    String packageName;
                    String regex;
                    String permission;
                    if (!fileName.equals(ALERT_PERMISSION_APPS)) {
                        if (fileName.equals(GAME_FILTER_FILE_NAME)) {
                            if (XML_TAG_GAME_SUFFIX.equals(tag)) {
                                String suffix = parser.nextText();
                                sGameSuffixList.add(suffix);
                                if (DEBUG) {
                                    Log.d(TAG, "add game suffix : " + suffix);
                                }
                            } else if (XML_TAG_GAME_NAME.equals(tag)) {
                                String name = parser.nextText();
                                sGameNameList.add(name);
                                if (DEBUG) {
                                    Log.d(TAG, "add game name : " + name);
                                }
                            } else if (XML_TAG_SMS_PROMPT_SWITCH.equalsIgnoreCase(tag)) {
                                sSmsPromptSwitch = parser.nextText();
                                if (!(sSmsPromptSwitch.equalsIgnoreCase(SWITCH_ON) || (sSmsPromptSwitch.equalsIgnoreCase(SWITCH_OFF) ^ 1) == 0 || (sSmsPromptSwitch.equalsIgnoreCase(SWITCH_SKIP_OPPO) ^ 1) == 0)) {
                                    sSmsPromptSwitch = SWITCH_ON;
                                }
                                if (DEBUG) {
                                    Log.d(TAG, "sSmsPromptSwitch is : " + sSmsPromptSwitch);
                                }
                            } else if (XML_TAG_SMS_CHARGE_SWITCH.equalsIgnoreCase(tag)) {
                                sSmsChargeSwitch = parser.nextText();
                                if (!(sSmsChargeSwitch.equalsIgnoreCase(SWITCH_ON) || (sSmsChargeSwitch.equalsIgnoreCase(SWITCH_OFF) ^ 1) == 0 || (sSmsChargeSwitch.equalsIgnoreCase(SWITCH_SKIP_OPPO) ^ 1) == 0)) {
                                    sSmsChargeSwitch = SWITCH_SKIP_OPPO;
                                }
                                if (DEBUG) {
                                    Log.d(TAG, "sSmsChargeSwitch is : " + sSmsChargeSwitch);
                                }
                            }
                        }
                    } else if (XML_TAG_ALERT.equals(tag)) {
                        String app = parser.nextText();
                        sAlertAppList.add(app);
                        if (DEBUG) {
                            Log.d(TAG, "add app : " + app);
                        }
                    } else if (XML_TAG_ALLOW_BACKGROUND.equals(tag)) {
                        flag = Boolean.valueOf(Boolean.parseBoolean(parser.nextText()));
                        sAllowBackgroundRequest = flag;
                        if (DEBUG) {
                            Log.d(TAG, "add flag : " + flag);
                        }
                    } else if (XML_TAG_OPPO_DCIM_RECYCLE_WHITE.equals(tag)) {
                        packageName = parser.nextText();
                        addToDCIMProtectWhiteList(packageName);
                        if (DEBUG) {
                            Log.d(TAG, "XML_TAG_OPPO_DCIM_RECYCLE_WHITE : " + packageName);
                        }
                    } else if (XML_TAG_DCIMPROTECT_ENABLED.equals(tag)) {
                        flag = Boolean.valueOf(Boolean.parseBoolean(parser.nextText()));
                        sDCIMProtectEnabled = flag;
                        if (DEBUG) {
                            Log.d(TAG, "XML_TAG_DCIMPROTECT_ENABLED flag : " + flag);
                        }
                    } else if (XML_TAG_SMS_CONTENT.equals(tag)) {
                        regex = parser.nextText();
                        sRegexSms.add(regex);
                        regexSmsUpdated = true;
                        if (DEBUG) {
                            Log.d(TAG, "add regex for sms content : " + regex);
                        }
                    } else if (XML_TAG_SMS_NUMBER.equals(tag)) {
                        regex = parser.nextText();
                        sRegexSmsNumber.add(regex);
                        if (DEBUG) {
                            Log.d(TAG, "add regex for sms number : " + regex);
                        }
                    } else if (XML_TAG_SMS_CONTENT_SKIP.equals(tag)) {
                        regex = parser.nextText();
                        sRegexSmsSkip.add(regex);
                        if (DEBUG) {
                            Log.d(TAG, "add regex for skip sms content : " + regex);
                        }
                    } else if (XML_TAG_SMS_NUMBER_SKIP.equals(tag)) {
                        regex = parser.nextText();
                        sRegexSmsNumberSkip.add(regex);
                        if (DEBUG) {
                            Log.d(TAG, "add regex for skip sms number : " + regex);
                        }
                    } else if (XML_TAG_BACKGROUND_SKIP.equals(tag)) {
                        packageName = parser.nextText();
                        if (!sBackgroundSkipList.contains(packageName)) {
                            sBackgroundSkipList.add(packageName);
                        }
                    } else if (XML_TAG_REJECT_DIALOG_PERMISSION.equals(tag)) {
                        permission = parser.nextText();
                        if (!(permission == null || (permission.isEmpty() ^ 1) == 0)) {
                            tmpRejectDialogPermissionList.add(permission);
                        }
                    } else if (XML_TAG_BACKGROUND_CAMERA_SKIP.equals(tag)) {
                        sBackgroundCameraSkipPkgs.put(parser.nextText(), Integer.valueOf(1));
                    } else if (XML_TAG_SHELL_REVOKE_PERMISSION.equals(tag)) {
                        permission = parser.nextText();
                        if (permission != null) {
                            sShellRevokedPermissions.add(permission);
                        }
                        if (DEBUG) {
                            Log.d(TAG, "add shell : " + permission);
                        }
                    }
                }
            } while (type != 1);
            if (!regexSmsUpdated) {
                sRegexSms.add(DEFAULT_SMS_REGEX);
            }
            if (!(tmpRejectDialogPermissionList == null || (tmpRejectDialogPermissionList.isEmpty() ^ 1) == 0)) {
                sRejectDialogPermissionList.clear();
                for (int i = 0; i < tmpRejectDialogPermissionList.size(); i++) {
                    sRejectDialogPermissionList.add((String) tmpRejectDialogPermissionList.get(i));
                }
            }
            if (sShellRevokedPermissions != null && (sShellRevokedPermissions.isEmpty() ^ 1) != 0) {
                OppoShellPermissionUtils.updateShellPermissions(sShellRevokedPermissions);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "readDataFromXML err !!");
        }
    }

    private static boolean isDeviceRooted() {
        for (String path : new String[]{"/sbin/su", "/system/bin/su", "/system/xbin/su", "/system/sbin/su", "/vendor/bin/su"}) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isChargeSms(String sms, String number) {
        boolean result = false;
        if (!(sms == null || sRegexSmsSkip == null || sRegexSmsSkip.size() <= 0)) {
            for (String regex : sRegexSmsSkip) {
                result = Pattern.compile(regex.trim()).matcher(sms).matches();
                if (result) {
                    if (DEBUG) {
                        Log.d(TAG, "charge regexSkip : " + regex + " , result " + result);
                    }
                    return false;
                }
            }
        }
        if (!(number == null || sRegexSmsNumberSkip == null || sRegexSmsNumberSkip.size() <= 0)) {
            for (String regex2 : sRegexSmsNumberSkip) {
                result = Pattern.compile(regex2.trim()).matcher(number).matches();
                if (result) {
                    if (DEBUG) {
                        Log.d(TAG, "charge sRegexSmsNumberSkip : " + regex2 + " , result " + result);
                    }
                    return false;
                }
            }
        }
        if (sms != null) {
            if (sRegexSms == null || sRegexSms.size() == 0) {
                Log.e(TAG, "charge regex is null !");
            }
            if (sRegexSms != null && sRegexSms.size() > 0) {
                for (String regex22 : sRegexSms) {
                    result = Pattern.compile(regex22.trim()).matcher(sms).matches();
                    if (DEBUG) {
                        Log.d(TAG, "charge regex : " + regex22 + " , result " + result);
                        continue;
                    }
                    if (result) {
                        if (DEBUG) {
                            Log.d(TAG, "charge sms : " + regex22 + " , " + sms);
                        }
                        return true;
                    }
                }
            }
        }
        if (!(number == null || sRegexSmsNumber == null || sRegexSmsNumber.size() <= 0)) {
            for (String regex222 : sRegexSmsNumber) {
                result = Pattern.compile(regex222.trim()).matcher(number).matches();
                if (result) {
                    if (DEBUG) {
                        Log.d(TAG, "charge sms number: " + regex222 + " , " + number);
                    }
                    return true;
                }
            }
        }
        return result;
    }

    private boolean isChargeSmsByPkg(String packageName, String sms, String number) {
        boolean result = false;
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        if (sIsBusinessCustom && sIsBussinessNoAccountDialog) {
            return false;
        }
        if (sSmsChargeSwitch.equalsIgnoreCase(SWITCH_SKIP_OPPO)) {
            result = (isFromGameCenter(packageName) || sms == null) ? false : isChargeSms(sms, number);
        } else if (sSmsChargeSwitch.equalsIgnoreCase(SWITCH_ON)) {
            result = sms != null ? isChargeSms(sms, number) : false;
        } else if (sSmsChargeSwitch.equalsIgnoreCase(SWITCH_OFF)) {
            result = false;
        }
        return result;
    }

    private boolean isFromGameCenter(String pkgName) {
        if (!(sGameSuffixList == null || (sGameSuffixList.isEmpty() ^ 1) == 0)) {
            for (String suffix : sGameSuffixList) {
                if (pkgName.contains(suffix)) {
                    return true;
                }
            }
        }
        return (sGameNameList == null || (sGameNameList.isEmpty() ^ 1) == 0 || !sGameNameList.contains(pkgName)) ? false : true;
    }

    private void sendNormalSmsStatistic(String pkgName, String permission) {
        if (permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && this.mSmsContent != null) {
            Intent intent = new Intent(STATISTIC_ACTION);
            Bundle bundle = new Bundle();
            bundle.putInt("type", 2);
            bundle.putStringArray("data", new String[]{pkgName, this.mSmsContent, this.mSmsDestination});
            intent.putExtras(bundle);
            this.mContext.sendBroadcast(intent);
            this.mSmsContent = null;
            this.mSmsDestination = null;
        }
    }

    private boolean isEnabledInputMethod(Context context, String packageName) {
        List<InputMethodInfo> list = ((InputMethodManager) context.getSystemService("input_method")).getEnabledInputMethodList();
        if (!(list == null || (list.isEmpty() ^ 1) == 0)) {
            for (InputMethodInfo imi : list) {
                if (packageName.equals(imi.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getCurrentFocus() {
        String result = "";
        if (!(this.mService == null || this.mService.mWindowManager == null)) {
            result = this.mService.mWindowManager.getFocusedWindowPkg();
            if (DEBUG) {
                Log.d(TAG, "getCurrentFocus " + result);
            }
        }
        return result == null ? "" : result;
    }

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
            Message msg = Message.obtain(null, msgCode);
            msg.replyTo = this.mMessenger;
            if (data != null) {
                msg.setData(data);
            }
            this.mDialogService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean bindDialogService(Context context) {
        boolean result = false;
        try {
            Intent intent = new Intent(ACTION_DIALOG_SERVICE);
            intent.setPackage(PERMISSION_PACKGE_NAME);
            return context.bindService(intent, this.mServConnection, 1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "bind service err !");
            return result;
        }
    }

    private void setDefaultDialogResult() {
        this.mPermissionHandler.post(new Runnable() {
            public void run() {
                if (OppoPermissionInterceptPolicy.DEBUG) {
                    Log.v(OppoPermissionInterceptPolicy.TAG, "set default dialog result");
                }
                if (OppoPermissionInterceptPolicy.this.mRecordPcm != null) {
                    OppoPermissionInterceptPolicy.this.processPermission(OppoPermissionInterceptPolicy.this.mRecordPcm, 0, false);
                } else if (OppoPermissionInterceptPolicy.DEBUG) {
                    Log.v(OppoPermissionInterceptPolicy.TAG, "mRecordPcm is null !");
                }
            }
        });
    }

    private boolean checkPackage(String packageName) {
        if (!(packageName == null || (packageName.isEmpty() ^ 1) == 0)) {
            try {
                this.mContext.getPackageManager().getApplicationInfo(packageName, 8192);
                return true;
            } catch (NameNotFoundException e) {
                Log.v(TAG, "not found package : " + packageName);
            }
        }
        return false;
    }

    private static boolean isMApp(Context context, String pkgName) {
        boolean z = true;
        try {
            if (context.getPackageManager().getApplicationInfo(pkgName, 0).targetSdkVersion < 23) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private boolean isForumVersion() {
        String ver = SystemProperties.get(PROPERTY_OPPOROM);
        if (ver == null) {
            return false;
        }
        ver = ver.toLowerCase();
        if (ver.endsWith(VERSION_ALPHA) || ver.endsWith(VERSION_BETA)) {
            return true;
        }
        return false;
    }

    private void statisticsScreenOffPermissionRequest(String packageName, String permissionName, String permissionValue) {
        if (isForumVersion()) {
            Map<String, String> statisticsMap = new HashMap();
            statisticsMap.put(KEY_PACKAGE_NAME, packageName);
            statisticsMap.put(KEY_PERMISSION_NAME, permissionName);
            statisticsMap.put(KEY_PERMISSION_VALUE, permissionValue);
            OppoStatistics.onCommon(this.mContext, UPLOAD_LOGTAG, UPLOAD_LOG_EVENTID, statisticsMap, false);
        }
    }

    private static boolean isDefaultMmsRegion(Context context) {
        String currentRegion = SystemProperties.get("persist.sys.oppo.region", "OC");
        String defaultRegions = Global.getString(context.getContentResolver(), COLOR_DEFAULT_MMS_REGIONS);
        Log.d(TAG, "currentRegion = " + currentRegion + ", defaultRegions = " + defaultRegions);
        if (TextUtils.isEmpty(defaultRegions) || "null".equals(defaultRegions)) {
            for (String region : INITIAL_REGIONS) {
                if (!TextUtils.isEmpty(region) && region.equals(currentRegion)) {
                    return true;
                }
            }
            return false;
        }
        String[] list = defaultRegions.split(";");
        if (list != null && list.length > 0) {
            for (String region2 : list) {
                if (!TextUtils.isEmpty(region2) && region2.equals(currentRegion)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void startDCIMDeleteEventObserving() {
        this.mDCIMDeleteEventObserver.startObserving(UEVENT_DCIM_DELETE_MSG);
    }
}
