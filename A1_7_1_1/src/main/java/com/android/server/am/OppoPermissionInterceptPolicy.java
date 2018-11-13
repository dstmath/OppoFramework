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
import com.android.server.oppo.IElsaManager;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.mediatek.appworkingset.AWSDBHelper.PackageProcessList;
import com.oppo.rutils.RUtils;
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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class OppoPermissionInterceptPolicy {
    private static final String ACTION_DIALOG_SERVICE = "coloros.safecenter.permission.PERMISSION_DIALOG_SERVICE";
    private static final String ALERT_PERMISSION_APPS = "alert_permission_apps.xml";
    private static final String ALERT_PERMISSION_DATA_DIR = "//data//oppo//permission//";
    private static final String COLOR_DEFAULT_MMS_REGIONS = "color_default_mms_regions";
    private static final boolean DEBUG = false;
    private static final int DO_CHECK_PERMISSION = 0;
    private static final String GAME_FILTER_FILE_NAME = "safe_marketfilter_list.xml";
    private static final String[] INITIAL_REGIONS = null;
    private static String KEY_CTSVERSION_PROPERTIES = null;
    private static final String KEY_PACKAGE_NAME = "pkgName";
    private static final String KEY_PERMISSION_NAME = "permissionName";
    private static String KEY_PERMISSION_PROPERTIES = null;
    private static final String KEY_PERMISSION_VALUE = "permissionValue";
    private static final int MSG_OPPODCIM_DELDCIMDIR_STATISTICS = 301;
    private static final int MSG_OPPODCIM_DELFAIL_DIALOG_SHOW = 104;
    private static final int MSG_PERMISSION_DIALOG_GET_RESULT = 211;
    private static final int MSG_PERMISSION_DIALOG_SEND_RESULT = 103;
    private static final int MSG_PERMISSION_DIALOG_SHOW = 101;
    private static final int PARAMETER_FOUR = 4;
    private static final int PARAMETER_ONE = 1;
    private static final int PARAMETER_THREE = 3;
    private static final int PARAMETER_TWO = 2;
    private static final String PERMISSION_ACCESS_MEDIA_PROVIDER = "android.permission.ACCESS_MEDIA_PROVIDER";
    private static final String PROPERTY_OPPOROM = "ro.build.version.opporom";
    private static final String SDCARDFS_DCIMPROTECT_ENABLED = "/sys/module/sdcardfs/parameters/skipd_enable";
    private static final String SDCARDFS_SKIP_UID = "/proc/fs/sdcardfs/skipd_delete";
    private static final String SWITCH_OFF = "off";
    private static final String SWITCH_ON = "on";
    private static final String SWITCH_SKIP_OPPO = "skip_oppo";
    private static final String TAG = "OppoPermissionInterceptPolicy";
    private static final String UEVENT_DCIM_DELETE_MSG = "DELETE_STAT=DCIM";
    private static final String UEVENT_DIR_DCIM_DELETE_MSG = "DELETE_STAT=DIR_DCIM";
    private static final int UPDATE_PERMISSION_CHOICE = 1;
    private static final String UPLOAD_LOGTAG = "20089";
    private static final String UPLOAD_LOG_EVENTID = "ScreenOffPermissionRequestEvent";
    private static final String VALUE_ACCEPT = "ACCEPT";
    private static final int VALUE_BG_CAMERA_SKIP_PKG = 1;
    private static final String VALUE_PROMPT = "PROMPT";
    private static final String VALUE_REJECT = "REJECT";
    private static final String VERSION_ALPHA = "alpha";
    private static final String VERSION_BETA = "beta";
    private static final String XML_TAG_ALERT = "alert";
    private static final String XML_TAG_ALLOW_BACKGROUND = "allowbackground";
    private static final String XML_TAG_BACKGROUND_CAMERA_SKIP = "background_camera_skip";
    private static final String XML_TAG_BACKGROUND_SKIP = "background_skip";
    private static final String XML_TAG_DCIMPROTECT_ENABLED = "dcimprotect_enabled";
    private static final String XML_TAG_GAME_NAME = "whitelist";
    private static final String XML_TAG_GAME_SUFFIX = "keyword";
    private static final String XML_TAG_OPPO_DCIM_RECYCLE_WHITE = "dcimprotect_recycle_white";
    private static final String XML_TAG_REJECT_DIALOG_PERMISSION = "rejectdialog_permission";
    private static final String XML_TAG_SMS_CHARGE_SWITCH = "smschargeswitch";
    private static final String XML_TAG_SMS_CONTENT = "sms";
    private static final String XML_TAG_SMS_CONTENT_SKIP = "sms_skip";
    private static final String XML_TAG_SMS_NUMBER = "number";
    private static final String XML_TAG_SMS_NUMBER_SKIP = "number_skip";
    private static final String XML_TAG_SMS_PROMPT_SWITCH = "smspromptswitch";
    private static List<String> alertAppList = null;
    private static AlertDataFileListener alertAppListener = null;
    private static Boolean allowBackgroundRequest = null;
    private static final String defaultSmsRegex = "[a-zA-Z0-9#$%&!'()*+,-./:;<=>?@\\[\\]^_`{|}~ ]{10,}";
    private static AlertDataFileListener gameAppListener;
    private static List<String> gameNameList;
    private static List<String> gameSuffixList;
    private static Boolean isCtaVersion;
    private static volatile boolean isPermissionInterceptEnabled;
    private static Map<String, Integer> mBackgroundCameraSkipPkgs;
    private static List<String> mBackgroundSkipList;
    private static OppoPermissionInterceptPolicy mPermissionInterceptPolicy;
    private static List<String> mRejectDialogPermissionList;
    private static String mSmsChargeSwitch;
    private static String mSmsPromptSwitch;
    private static List<String> regexSms;
    private static List<String> regexSmsNumber;
    private static List<String> regexSmsNumberSkip;
    private static List<String> regexSmsSkip;
    private static boolean rejectChargeSms;
    private static Boolean sDCIMProtectEnabled;
    private static ArrayList<String> sDefaultSkipPackages;
    private static ArrayList<Integer> sDefaultSkipUids;
    private static boolean sIsBusinessCustom;
    private static boolean sIsBussinessNoAccountDialog;
    private static boolean sIsExVersion;
    private static LinkedList<PermissionCheckingMsg> sPermissionCheckMsgList;
    private static List<String> sPermissionsPrompt;
    private static Bundle sRecordData;
    private static int sRecordMsgCode;
    private final String KEYGUARD_PACKAGE_NAME;
    private final String SMS_SEPARATOR;
    private final String STATISTIC_ACTION;
    private final String STATISTIC_CHECKED;
    private final String STATISTIC_DATA;
    private final String STATISTIC_REJECT;
    private final String STATISTIC_TYPE;
    private final int STATISTIC_TYPE_CHARGE_SMS;
    private final int STATISTIC_TYPE_NORMAL;
    private final int STATISTIC_TYPE_NORMAL_SMS;
    private String callNumber;
    private final Context mContext;
    private String mCurrentCountry;
    private String mCurrentLanguage;
    private UEventObserver mDCIMDeleteEventObserver;
    private UEventObserver mDCIMDirDeleteEventObserver;
    private Messenger mDialogService;
    private final H mH;
    private HandlerThread mHandlerThread;
    private KeyguardManager mKeyguardManager;
    private final Messenger mMessenger;
    private Handler mPendingMsgHandler;
    private HandlerThread mPendingMsgThread;
    private Handler mPermissionHandler;
    private final IPackageManager mPm;
    private PowerManager mPowerManager;
    private PermissionCheckingMsg mRecordPcm;
    private String mSafecenterPkg;
    private CheckBox mSaveCheckBox;
    private final ActivityManagerService mService;
    private Handler mTimerHandler;
    private HandlerThread mTimerThread;
    private ServiceConnection servConnection;
    private String smsContent;
    private String smsDestination;

    private class AlertDataFileListener extends FileObserver {
        String observerPath = null;

        public AlertDataFileListener(String path) {
            super(path, DhcpPacket.MIN_PACKET_LENGTH_L3);
            this.observerPath = path;
        }

        public void onEvent(int event, String path) {
            switch (event) {
                case 8:
                    if (this.observerPath != null && this.observerPath.contains(OppoPermissionInterceptPolicy.ALERT_PERMISSION_APPS)) {
                        OppoPermissionInterceptPolicy.initAlertAppList(OppoPermissionInterceptPolicy.ALERT_PERMISSION_APPS);
                        return;
                    } else if (this.observerPath != null && this.observerPath.contains(OppoPermissionInterceptPolicy.GAME_FILTER_FILE_NAME)) {
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
        OppoPermissionCallback callback;
        Object lock;
        String mContentUri;
        String mSelection;
        String mSelectionArgs;
        String permission;
        int pid;
        int res = 3;
        int token;
        int uid;

        CheckPermissionRunnable(Object lock, String permission, int pid, int uid, int token, OppoPermissionCallback callback) {
            this.lock = lock;
            this.permission = permission;
            this.pid = pid;
            this.uid = uid;
            this.token = token;
            this.callback = callback;
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

        /* JADX WARNING: Missing block: B:3:0x000b, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean isInputMethodApplication(String pkgName) {
            if (OppoPermissionInterceptPolicy.this.mService == null || pkgName == null || !OppoBroadcastManager.getInstance(OppoPermissionInterceptPolicy.this.mService).isEnableInputMethodApp(pkgName)) {
                return false;
            }
            return true;
        }

        public void run() {
            synchronized (this.lock) {
                ProcessRecord pr = OppoPermissionInterceptPolicy.this.getProcessForPid(this.pid);
                PackagePermission pkgPm = null;
                String takePicturePermission = this.permission;
                if (pr == null) {
                    this.res = 3;
                    this.lock.notifyAll();
                    return;
                }
                synchronized (OppoPermissionInterceptPolicy.this) {
                    PermissionCheckingMsg pcm;
                    if (this.permission.equals(OppoPermissionInterceptPolicy.PERMISSION_ACCESS_MEDIA_PROVIDER)) {
                        if (OppoPermissionInterceptPolicy.sDCIMProtectEnabled.booleanValue()) {
                            if (!OppoPermissionInterceptPolicy.sDefaultSkipUids.contains(Integer.valueOf(this.uid))) {
                                pcm = new PermissionCheckingMsg();
                                pcm.permission = this.permission;
                                pcm.pr = pr;
                                pcm.uid = this.uid;
                                pcm.callback = this.callback;
                                pcm.token = this.token;
                                pcm.mContentUri = this.mContentUri;
                                pcm.mSelection = this.mSelection;
                                pcm.mSelectionArgs = this.mSelectionArgs;
                                synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                    OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm);
                                    pr.isWaitingPermissionChoice = true;
                                    OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                    this.res = 2;
                                }
                                this.lock.notifyAll();
                                return;
                            }
                        }
                        this.res = 0;
                        this.lock.notifyAll();
                        return;
                    }
                    String tmpPermission = this.permission;
                    if (this.permission.equals("android.permission.CAMERA_TAKEPICTURE")) {
                        tmpPermission = this.permission;
                        this.permission = OppoPermissionConstants.PERMISSION_CAMERA;
                    }
                    if (pr.info != null) {
                        pkgPm = OppoPermissionInterceptPolicy.this.queryPackagePermissions(pr.info.packageName);
                    } else {
                        this.res = 3;
                    }
                    if (pkgPm != null) {
                        pr.mPackagePermission = pkgPm;
                        pr.mPersistPackagePermission = pkgPm.copy();
                    }
                    if (pkgPm != null) {
                        long mask = OppoPermissionInterceptPolicy.this.getPermissionMask(this.permission);
                        if (pkgPm.packageName.contains(".cts.")) {
                            this.res = 0;
                        } else {
                            ComponentName cn;
                            String foregroundPackageName;
                            if (this.permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && (pkgPm.reject & mask) == 0) {
                                if (OppoPermissionInterceptPolicy.sIsExVersion && (pkgPm.accept & mask) != 0 && OppoPermissionInterceptPolicy.isDefaultMmsRegion(OppoPermissionInterceptPolicy.this.mContext)) {
                                    String defaultSms = Sms.getDefaultSmsPackage(OppoPermissionInterceptPolicy.this.mContext);
                                    if (defaultSms != null && defaultSms.equals(pkgPm.packageName)) {
                                        this.res = 0;
                                        this.lock.notifyAll();
                                        return;
                                    }
                                }
                                if (OppoPermissionInterceptPolicy.rejectChargeSms) {
                                    this.res = 1;
                                } else {
                                    pcm = new PermissionCheckingMsg();
                                    pcm.permission = this.permission;
                                    pcm.pr = pr;
                                    pcm.uid = this.uid;
                                    pcm.callback = this.callback;
                                    pcm.permissionMask = mask;
                                    pcm.token = this.token;
                                    synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm);
                                        pr.isWaitingPermissionChoice = true;
                                        OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                        this.res = 2;
                                    }
                                }
                            } else if (pkgPm.trust != 0) {
                                this.res = 0;
                                OppoPermissionInterceptPolicy.this.sendNormalSmsStatistic(pkgPm.packageName, this.permission);
                            } else if ((pkgPm.accept & mask) != 0) {
                                if (!tmpPermission.equals("android.permission.CAMERA_TAKEPICTURE")) {
                                    this.res = 0;
                                    OppoPermissionInterceptPolicy.this.sendNormalSmsStatistic(pkgPm.packageName, this.permission);
                                } else if (OppoPermissionInterceptPolicy.this.isScreenOn()) {
                                    cn = null;
                                    try {
                                        cn = new OppoActivityManager().getTopActivityComponentName();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    foregroundPackageName = cn.getPackageName();
                                    if (!(pr.info.packageName.contains("com.cttl.") || pr.info.packageName.equals(foregroundPackageName))) {
                                        if (!OppoPermissionInterceptPolicy.mBackgroundCameraSkipPkgs.containsKey(pr.info.packageName)) {
                                            if (!isInputMethodApplication(pr.info.packageName)) {
                                                pcm = new PermissionCheckingMsg();
                                                pcm.permission = this.permission;
                                                pcm.pr = pr;
                                                pcm.uid = this.uid;
                                                pcm.callback = this.callback;
                                                pcm.permissionMask = mask;
                                                pcm.token = this.token;
                                                pcm.isBackground = true;
                                                synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                                    OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm);
                                                    pr.isWaitingPermissionChoice = true;
                                                    OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                                    this.res = 2;
                                                }
                                                this.lock.notifyAll();
                                                return;
                                            }
                                        }
                                    }
                                    this.res = 0;
                                } else {
                                    String pkgName = pkgPm.packageName;
                                    if (OppoPermissionInterceptPolicy.mBackgroundCameraSkipPkgs.containsKey(pkgName)) {
                                        this.res = 0;
                                    } else {
                                        this.res = 1;
                                        Log.d(OppoPermissionInterceptPolicy.TAG, "Reject background permission " + this.permission + "pkgName: " + pkgName);
                                    }
                                    OppoPermissionInterceptPolicy.this.statisticsScreenOffPermissionRequest(pkgName, takePicturePermission, OppoPermissionInterceptPolicy.VALUE_ACCEPT);
                                }
                            } else if ((pkgPm.reject & mask) != 0) {
                                if (OppoPermissionInterceptPolicy.mRejectDialogPermissionList != null) {
                                    if (OppoPermissionInterceptPolicy.mRejectDialogPermissionList.contains(this.permission)) {
                                        pcm = new PermissionCheckingMsg();
                                        pcm.permission = this.permission;
                                        pcm.pr = pr;
                                        pcm.uid = this.uid;
                                        pcm.callback = this.callback;
                                        pcm.permissionMask = mask;
                                        pcm.token = this.token;
                                        pcm.pkgPm = pkgPm;
                                        synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                            OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm);
                                            pr.isWaitingPermissionChoice = true;
                                            OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                            this.res = 2;
                                        }
                                        this.lock.notifyAll();
                                        return;
                                    }
                                }
                                this.res = 1;
                                ActivityManager manager = (ActivityManager) OppoPermissionInterceptPolicy.this.mContext.getSystemService("activity");
                                try {
                                    List<RunningTaskInfo> runningTasks = new ArrayList();
                                    if (manager.getRunningTasks(1) != null) {
                                        runningTasks.addAll(manager.getRunningTasks(1));
                                    }
                                    if (!(runningTasks == null || runningTasks.size() == 0 || runningTasks.get(0) == null)) {
                                        String packageName = ((RunningTaskInfo) runningTasks.get(0)).topActivity.getPackageName();
                                        if (this.uid == OppoPermissionInterceptPolicy.this.mPm.getPackageUid(packageName, 0, 0) || (packageName.equals("com.wandoujia.phoenix2") && pr.info.packageName.equals("com.wandoujia.phoenix2.usbproxy"))) {
                                            if (packageName.equals("com.wandoujia.phoenix2") && pr.info.packageName.equals("com.wandoujia.phoenix2.usbproxy")) {
                                                packageName = "com.wandoujia.phoenix2.usbproxy";
                                                Log.i("AAA", "----equals--");
                                            }
                                            Intent intent = new Intent();
                                            intent.setAction("com.oppo.permissionprotect.notify");
                                            intent.putExtra("PackageName", packageName);
                                            intent.putExtra("Permission", this.permission);
                                            OppoPermissionInterceptPolicy.this.mContext.sendBroadcast(intent);
                                            Log.i("AAA", "Notify!!!");
                                        }
                                    }
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                                OppoPermissionInterceptPolicy.this.sendNormalSmsStatistic(pkgPm.packageName, this.permission);
                            } else if ((pkgPm.prompt & mask) != 0) {
                                if (OppoPermissionInterceptPolicy.this.isScreenOn()) {
                                    if (!(OppoPermissionInterceptPolicy.isCtaVersion.booleanValue() || OppoPermissionInterceptPolicy.allowBackgroundRequest.booleanValue() || pr.info.packageName.contains("com.cttl."))) {
                                        cn = null;
                                        try {
                                            cn = new OppoActivityManager().getTopActivityComponentName();
                                        } catch (Exception e22) {
                                            e22.printStackTrace();
                                        }
                                        foregroundPackageName = cn.getPackageName();
                                        if (!(OppoPermissionInterceptPolicy.mBackgroundSkipList.contains(pr.info.packageName) || pr.info.packageName.equals(foregroundPackageName))) {
                                            if (!OppoPermissionInterceptPolicy.this.isEnabledInputMethod(OppoPermissionInterceptPolicy.this.mContext, pr.info.packageName)) {
                                                if (!OppoPermissionInterceptPolicy.this.getCurrentFocus().contains(pr.info.packageName)) {
                                                    this.res = 1;
                                                    Log.d(OppoPermissionInterceptPolicy.TAG, "not foreground app, reject it! app is " + pr.info.packageName);
                                                    this.lock.notifyAll();
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                    pcm = new PermissionCheckingMsg();
                                    pcm.permission = this.permission;
                                    pcm.pr = pr;
                                    pcm.uid = this.uid;
                                    pcm.callback = this.callback;
                                    pcm.permissionMask = mask;
                                    pcm.token = this.token;
                                    synchronized (OppoPermissionInterceptPolicy.sPermissionCheckMsgList) {
                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.addLast(pcm);
                                        pr.isWaitingPermissionChoice = true;
                                        OppoPermissionInterceptPolicy.this.mPendingMsgHandler.sendEmptyMessage(0);
                                        this.res = 2;
                                    }
                                } else {
                                    this.res = 1;
                                    OppoPermissionInterceptPolicy.this.statisticsScreenOffPermissionRequest(pr.info.packageName, takePicturePermission, OppoPermissionInterceptPolicy.VALUE_REJECT);
                                }
                            }
                        }
                    }
                    if (3 == this.res) {
                        if (OppoPermissionConstants.PERMISSION_ACCESS.equals(this.permission)) {
                            this.res = 0;
                        }
                    }
                    this.lock.notifyAll();
                }
            }
        }
    }

    private class ChoiceCountDownTimer extends CountDownTimer {
        PermissionCheckingMsg pcm;
        AlertDialog permissionChoiceDialog;
        Button rejectBtn;

        public ChoiceCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onFinish() {
            this.permissionChoiceDialog.dismiss();
            OppoPermissionInterceptPolicy.this.processPermission(this.pcm, 1, OppoPermissionInterceptPolicy.this.mSaveCheckBox.isChecked());
        }

        public void onTick(long millisUntilFinished) {
            String str = OppoPermissionInterceptPolicy.this.mContext.getString(201589895) + "(" + (millisUntilFinished / 1000) + "s)";
            if (OppoPermissionInterceptPolicy.this.isScreenOn()) {
                this.rejectBtn.setText(str);
            } else {
                onFinish();
            }
        }
    }

    final class H extends Handler {
        public static final int NOTIFY_DCIM_DELETE_EVENT = 2;
        public static final int NOTIFY_DCIM_DIR_DELETE_EVENT = 3;

        H() {
        }

        public void handleMessage(Message msg) {
            UEvent event;
            String uid;
            String pid;
            String path;
            Bundle bundle;
            switch (msg.what) {
                case 2:
                    event = msg.obj;
                    uid = event.get("UID");
                    pid = event.get("PID");
                    path = event.get("PATH");
                    try {
                        path = new String(path.getBytes("ISO-8859-1"), "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    bundle = new Bundle();
                    bundle.putInt(PackageProcessList.KEY_UID, Integer.valueOf(uid).intValue());
                    bundle.putInt("pid", Integer.valueOf(pid).intValue());
                    bundle.putString("path", path);
                    OppoPermissionInterceptPolicy.this.sendDialogMsg(104, bundle);
                    return;
                case 3:
                    event = (UEvent) msg.obj;
                    uid = event.get("UID");
                    pid = event.get("PID");
                    path = event.get("PATH");
                    bundle = new Bundle();
                    bundle.putInt(PackageProcessList.KEY_UID, Integer.valueOf(uid).intValue());
                    bundle.putInt("pid", Integer.valueOf(pid).intValue());
                    bundle.putString("path", path);
                    OppoPermissionInterceptPolicy.this.sendDialogMsg(301, bundle);
                    return;
                default:
                    return;
            }
        }
    }

    class PackagePermission {
        long accept;
        int id;
        String packageName;
        long prompt;
        long reject;
        int trust;

        PackagePermission() {
        }

        public PackagePermission copy() {
            PackagePermission copy = new PackagePermission();
            copy.id = this.id;
            copy.packageName = this.packageName;
            copy.accept = this.accept;
            copy.reject = this.reject;
            copy.prompt = this.prompt;
            copy.trust = this.trust;
            return copy;
        }

        public String toString() {
            return "[packageName=" + this.packageName + ", accept=" + this.accept + ", reject=" + this.reject + ", prompt=" + this.prompt + ", trust=" + this.trust + "]";
        }
    }

    class PermissionCheckingMsg {
        OppoPermissionCallback callback;
        boolean isBackground;
        String mContentUri = null;
        String mSelection = null;
        String mSelectionArgs = null;
        String permission;
        long permissionMask;
        PackagePermission pkgPm;
        ProcessRecord pr;
        int token;
        int uid;

        PermissionCheckingMsg() {
        }

        public boolean equals(PermissionCheckingMsg o) {
            if (this.permission == null || o == null || !this.permission.equals(o.permission) || this.uid != o.uid) {
                return false;
            }
            return true;
        }

        public String toString() {
            return "[permission=" + this.permission + ", pr=" + this.pr + ", uid=" + this.uid + ", permissionMask=" + this.permissionMask + ", token=" + this.token + ", callback=" + this.callback + "]";
        }
    }

    private class UpdateMsgDate {
        int choice;
        String packageName;
        String permission;

        /* synthetic */ UpdateMsgDate(OppoPermissionInterceptPolicy this$0, UpdateMsgDate updateMsgDate) {
            this();
        }

        private UpdateMsgDate() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoPermissionInterceptPolicy.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoPermissionInterceptPolicy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoPermissionInterceptPolicy.<clinit>():void");
    }

    public static OppoPermissionInterceptPolicy getInstance(ActivityManagerService service) {
        if (mPermissionInterceptPolicy == null) {
            mPermissionInterceptPolicy = new OppoPermissionInterceptPolicy(service);
        }
        return mPermissionInterceptPolicy;
    }

    private OppoPermissionInterceptPolicy(ActivityManagerService service) {
        String[] strArr;
        this.STATISTIC_ACTION = "coloros.safecenter.permission.STATISTIC_FRAMEWORK";
        this.STATISTIC_TYPE_NORMAL = 0;
        this.STATISTIC_TYPE_CHARGE_SMS = 1;
        this.STATISTIC_TYPE_NORMAL_SMS = 2;
        this.STATISTIC_TYPE = SoundModelContract.KEY_TYPE;
        this.STATISTIC_REJECT = "reject";
        this.STATISTIC_CHECKED = "checked";
        this.STATISTIC_DATA = "data";
        this.SMS_SEPARATOR = "#";
        this.smsContent = null;
        this.smsDestination = null;
        this.mRecordPcm = null;
        this.callNumber = null;
        this.KEYGUARD_PACKAGE_NAME = "com.android.keyguard";
        this.mSafecenterPkg = "com.coloros.securitypermission";
        this.mH = new H();
        this.mHandlerThread = new HandlerThread("PermissionThread", 10);
        this.mPendingMsgThread = new HandlerThread("PermissionMsgPendingThread", 10);
        this.mTimerThread = new HandlerThread("PermissionTimerThread", 10);
        this.servConnection = new ServiceConnection() {
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
        this.mMessenger = new Messenger(new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 211:
                        if (OppoPermissionInterceptPolicy.this.mRecordPcm != null) {
                            Bundle bundle = msg.getData();
                            OppoPermissionInterceptPolicy.this.processPermission(OppoPermissionInterceptPolicy.this.mRecordPcm, bundle.getInt("res", 0), bundle.getBoolean("save", false));
                            if (OppoPermissionInterceptPolicy.this.mRecordPcm.permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && OppoPermissionInterceptPolicy.this.isChargeSmsByPkg(OppoPermissionInterceptPolicy.this.mRecordPcm.pr.info.packageName, OppoPermissionInterceptPolicy.this.smsContent, OppoPermissionInterceptPolicy.this.smsDestination)) {
                                OppoPermissionInterceptPolicy.rejectChargeSms = true;
                                OppoPermissionInterceptPolicy.this.mPermissionHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        OppoPermissionInterceptPolicy.rejectChargeSms = false;
                                    }
                                }, 3000);
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
        this.mDCIMDeleteEventObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
                OppoPermissionInterceptPolicy.this.mH.sendMessage(OppoPermissionInterceptPolicy.this.mH.obtainMessage(2, event));
            }
        };
        this.mDCIMDirDeleteEventObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
                OppoPermissionInterceptPolicy.this.mH.sendMessage(OppoPermissionInterceptPolicy.this.mH.obtainMessage(3, event));
            }
        };
        this.mService = service;
        this.mContext = service.mContext;
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.system.cmcc.test")) {
            isPermissionInterceptEnabled = false;
        }
        sIsBusinessCustom = this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom");
        sIsBussinessNoAccountDialog = this.mContext.getPackageManager().hasSystemFeature("oppo.settings.account.dialog.disallow");
        sIsExVersion = this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
        SystemProperties.set(KEY_PERMISSION_PROPERTIES, String.valueOf(isPermissionInterceptEnabled));
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
                            long permissionMask = OppoPermissionInterceptPolicy.this.getPermissionMask(umd.permission);
                            ProcessRecord pr = OppoPermissionInterceptPolicy.this.getProcessForPackageName(umd.packageName);
                            if (!(pr == null || pr.mPackagePermission == null)) {
                                OppoPermissionInterceptPolicy.this.changePermissionChoice(pr.mPackagePermission, permissionMask, umd.choice);
                                OppoPermissionInterceptPolicy.this.changePermissionChoice(pr.mPersistPackagePermission, permissionMask, umd.choice);
                                pr.isSelected = (int) (((long) pr.isSelected) & (~permissionMask));
                                pkgPm = pr.mPersistPackagePermission;
                            }
                            if (pkgPm == null) {
                                pkgPm = OppoPermissionInterceptPolicy.this.queryPackagePermissions(umd.packageName);
                                if (pkgPm != null) {
                                    OppoPermissionInterceptPolicy.this.changePermissionChoice(pkgPm, permissionMask, umd.choice);
                                }
                            }
                            if (pkgPm != null) {
                                OppoPermissionInterceptPolicy.this.savePermissionChoice(pkgPm);
                            }
                        }
                        return;
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
                            if (OppoPermissionInterceptPolicy.sPermissionCheckMsgList.size() >= 1) {
                                PermissionCheckingMsg pcm = (PermissionCheckingMsg) OppoPermissionInterceptPolicy.sPermissionCheckMsgList.removeFirst();
                                if (pcm != null) {
                                    OppoPermissionInterceptPolicy.this.mRecordPcm = pcm;
                                    boolean isScreenOn = OppoPermissionInterceptPolicy.this.isScreenOn();
                                    if ((OppoPermissionInterceptPolicy.this.mKeyguardManager != null && OppoPermissionInterceptPolicy.this.mKeyguardManager.inKeyguardRestrictedInputMode()) || !isScreenOn) {
                                        try {
                                            String currentFocus = OppoPermissionInterceptPolicy.this.getCurrentFocus();
                                            if (currentFocus.equals("com.android.keyguard") || currentFocus.equals(OppoPermissionInterceptPolicy.this.mSafecenterPkg) || currentFocus.equals(pcm.pr.info.packageName) || !isScreenOn) {
                                                OppoPermissionInterceptPolicy.this.notifyWaitingApp(OppoPermissionInterceptPolicy.this.mRecordPcm, 1);
                                                Log.d(OppoPermissionInterceptPolicy.TAG, "behind keyguard, reject it! app is " + pcm.pr.info.packageName);
                                                return;
                                            }
                                        } catch (Exception e) {
                                            Log.e(OppoPermissionInterceptPolicy.TAG, "show permission dialog error.");
                                            e.printStackTrace();
                                        }
                                    }
                                    Bundle bundle = new Bundle();
                                    bundle.putString("packageName", pcm.pr.info.packageName);
                                    bundle.putString("permission", pcm.permission);
                                    bundle.putString("smsContent", OppoPermissionInterceptPolicy.this.smsContent);
                                    bundle.putString("smsDestination", OppoPermissionInterceptPolicy.this.smsDestination);
                                    bundle.putString("callNumber", OppoPermissionInterceptPolicy.this.callNumber);
                                    bundle.putBoolean("isBackground", pcm.isBackground);
                                    bundle.putInt(PackageProcessList.KEY_UID, pcm.uid);
                                    if (pcm.mContentUri != null) {
                                        bundle.putString("contentUri", pcm.mContentUri);
                                    }
                                    if (pcm.mSelection != null) {
                                        bundle.putString("selection", pcm.mSelection);
                                    }
                                    if (pcm.mSelectionArgs != null) {
                                        bundle.putString("selectionArgs", pcm.mSelectionArgs);
                                    }
                                    if (pcm.permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && OppoPermissionInterceptPolicy.this.isChargeSmsByPkg(pcm.pr.info.packageName, OppoPermissionInterceptPolicy.this.smsContent, OppoPermissionInterceptPolicy.this.smsDestination)) {
                                        bundle.putBoolean("isChargeSms", true);
                                    } else {
                                        bundle.putBoolean("isChargeSms", false);
                                    }
                                    if (OppoPermissionInterceptPolicy.mRejectDialogPermissionList != null && OppoPermissionInterceptPolicy.mRejectDialogPermissionList.contains(pcm.permission)) {
                                        if (pcm.pkgPm == null || (pcm.pkgPm.reject & pcm.permissionMask) == 0) {
                                            bundle.putBoolean("isRectDialog", false);
                                        } else {
                                            bundle.putBoolean("isRejectDialog", true);
                                        }
                                    }
                                    OppoPermissionInterceptPolicy.this.sendDialogMsg(101, bundle);
                                    OppoPermissionInterceptPolicy.this.callNumber = null;
                                    try {
                                        if (!pcm.permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                                            if (!pcm.permission.equals(OppoPermissionConstants.PERMISSION_CAMERA)) {
                                                OppoPermissionInterceptPolicy.sPermissionCheckMsgList.wait(20000);
                                                break;
                                            } else {
                                                OppoPermissionInterceptPolicy.sPermissionCheckMsgList.wait(50000);
                                                break;
                                            }
                                        }
                                        OppoPermissionInterceptPolicy.sPermissionCheckMsgList.wait(50000);
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
        isCtaVersion = Boolean.valueOf(this.mContext.getPackageManager().hasSystemFeature("oppo.cta.support"));
        initAlertAppList(ALERT_PERMISSION_APPS);
        initAlertAppList(GAME_FILTER_FILE_NAME);
        alertAppListener = new AlertDataFileListener("//data//oppo//permission//alert_permission_apps.xml");
        alertAppListener.startWatching();
        gameAppListener = new AlertDataFileListener("//data//oppo//permission//safe_marketfilter_list.xml");
        gameAppListener.startWatching();
        bindDialogService(this.mContext);
        if (mBackgroundSkipList != null && mBackgroundSkipList.isEmpty()) {
            strArr = new String[9];
            strArr[0] = "com.sogou.speech.offlineservice";
            strArr[1] = "com.google.android.gms";
            strArr[2] = "com.google.android.gsf";
            strArr[3] = "com.google.android.gsf.login";
            strArr[4] = "com.google.android.syncadapters.calendar";
            strArr[5] = "com.google.android.syncadapters.contacts";
            strArr[6] = "com.appstar.callrecorder";
            strArr[7] = "com.jiochat.jiochatapp";
            strArr[8] = "com.nll.acr";
            mBackgroundSkipList = new ArrayList(Arrays.asList(strArr));
        }
        if (mBackgroundCameraSkipPkgs != null && mBackgroundCameraSkipPkgs.isEmpty()) {
            mBackgroundCameraSkipPkgs.put("com.tencent.mm", Integer.valueOf(1));
            mBackgroundCameraSkipPkgs.put("com.tencent.mobileqq", Integer.valueOf(1));
            mBackgroundCameraSkipPkgs.put("jp.naver.line.android", Integer.valueOf(1));
            mBackgroundCameraSkipPkgs.put("com.viber.voip", Integer.valueOf(1));
            mBackgroundCameraSkipPkgs.put("com.whatsapp", Integer.valueOf(1));
        }
        if (mRejectDialogPermissionList != null && mRejectDialogPermissionList.isEmpty()) {
            strArr = new String[2];
            strArr[0] = OppoPermissionConstants.PERMISSION_CAMERA;
            strArr[1] = OppoPermissionConstants.PERMISSION_RECORD_AUDIO;
            mRejectDialogPermissionList = new ArrayList(Arrays.asList(strArr));
        }
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        startDCIMDeleteEventObserving();
        sDefaultSkipPackages.add("com.oppo.camera");
        sDefaultSkipPackages.add("com.android.providers.media");
        sDefaultSkipPackages.add("com.coloros.filemanager");
        sDefaultSkipPackages.add("com.coloros.cloud");
        sDefaultSkipPackages.add("com.coloros.gallery3d");
        sDefaultSkipPackages.add("com.coloros.video");
        sDefaultSkipPackages.add("com.google.android.apps.photos");
        echoUidsToSdcardfs();
        echoDCIMProtectEnabled();
    }

    private void echoUidsToSdcardfs() {
        for (String packageName : sDefaultSkipPackages) {
            try {
                ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfo(packageName, 0);
                if (appInfo != null) {
                    sDefaultSkipUids.add(Integer.valueOf(appInfo.uid));
                    RUtils.RUtilsCmd("echo " + appInfo.uid + " > " + SDCARDFS_SKIP_UID);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void echoUidsToSdcardfs(String packageName) {
        try {
            int uid = AppGlobals.getPackageManager().getPackageUid(packageName, 0, 0);
            sDefaultSkipUids.add(Integer.valueOf(uid));
            RUtils.RUtilsCmd("echo " + uid + " > " + SDCARDFS_SKIP_UID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void echoDCIMProtectEnabled() {
        try {
            RUtils.RUtilsCmd("echo " + (sDCIMProtectEnabled.booleanValue() ? 1 : 0) + " > " + SDCARDFS_DCIMPROTECT_ENABLED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int checkPermissionForProc(String permission, int pid, int uid, int token, OppoPermissionCallback callback) {
        String contentUri = null;
        String selection = null;
        String selectionArgs = null;
        if (permission.startsWith(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
            if (permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                this.smsContent = null;
                this.smsDestination = null;
            } else {
                this.smsContent = permission.substring(OppoPermissionConstants.PERMISSION_SEND_SMS.length(), permission.length());
                int separator = this.smsContent.lastIndexOf("#");
                if (separator != -1) {
                    this.smsDestination = this.smsContent.substring(separator + 1);
                    this.smsContent = this.smsContent.substring(0, separator);
                }
                permission = OppoPermissionConstants.PERMISSION_SEND_SMS;
            }
        } else {
            if (permission.startsWith(OppoPermissionConstants.PERMISSION_CALL_PHONE)) {
                if (!permission.equals(OppoPermissionConstants.PERMISSION_CALL_PHONE)) {
                    this.callNumber = permission.substring(OppoPermissionConstants.PERMISSION_CALL_PHONE.length(), permission.length());
                    this.callNumber = this.callNumber.substring(this.callNumber.indexOf(":") + 1).trim();
                    permission = OppoPermissionConstants.PERMISSION_CALL_PHONE;
                }
            }
            if (permission.startsWith(PERMISSION_ACCESS_MEDIA_PROVIDER)) {
                String[] extraStringArray = permission.split("#");
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
        if (uid < 10000 || !isPermissionInterceptEnabled) {
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
                    lock.wait(50000);
                } else {
                    if (permission.equals(OppoPermissionConstants.PERMISSION_CAMERA)) {
                        lock.wait(50000);
                    } else {
                        lock.wait(20000);
                    }
                }
                result = queryRunnable.res;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = 3;
        }
        return result;
    }

    public void updatePermissionChoice(String packageName, String permission, int choice) {
        if (packageName != null && permission != null) {
            UpdateMsgDate umd = new UpdateMsgDate(this, null);
            umd.packageName = packageName;
            umd.permission = permission;
            umd.choice = choice;
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
            String[] strArr = new String[1];
            strArr[0] = packageName;
            cursor = this.mContext.getContentResolver().query(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, null, "pkg_name=?", strArr, null);
            if (cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return 3;
            }
            cursor.moveToFirst();
            long valueAccept = cursor.getLong(cursor.getColumnIndex("accept"));
            long valueReject = cursor.getLong(cursor.getColumnIndex("reject"));
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
        if (isPermissionInterceptEnabled != enabled) {
            isPermissionInterceptEnabled = enabled;
            SystemProperties.set(KEY_PERMISSION_PROPERTIES, String.valueOf(enabled));
        }
    }

    public boolean isPermissionInterceptEnabled() {
        return isPermissionInterceptEnabled;
    }

    private void changePermissionChoice(PackagePermission packagePermission, long permissionMask, int choice) {
        if (packagePermission != null) {
            if (choice == 0) {
                packagePermission.accept |= permissionMask;
                packagePermission.reject &= ~permissionMask;
                packagePermission.prompt &= ~permissionMask;
            } else if (1 == choice) {
                packagePermission.accept &= ~permissionMask;
                packagePermission.reject |= permissionMask;
                packagePermission.prompt &= ~permissionMask;
            } else if (2 == choice) {
                packagePermission.accept &= ~permissionMask;
                packagePermission.reject &= ~permissionMask;
                packagePermission.prompt |= permissionMask;
            }
        }
    }

    private long getPermissionMask(String permission) {
        int index = OppoPermissionManager.sInterceptingPermissions.indexOf(permission);
        if (index > -1) {
            return 1 << index;
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private PackagePermission queryPackagePermissions(String packageName) {
        Exception ex;
        Throwable th;
        String selection = "pkg_name=?";
        String[] selectionArgs = new String[1];
        selectionArgs[0] = packageName;
        Cursor cursor = null;
        PackagePermission packagePermission = null;
        try {
            cursor = this.mContext.getContentResolver().query(OppoPermissionManager.PERMISSIONS_PROVIDER_URI, null, selection, selectionArgs, null);
            if (cursor != null && cursor.getCount() == 1 && cursor.moveToNext()) {
                PackagePermission pkgPermission = new PackagePermission();
                try {
                    pkgPermission.id = cursor.getInt(0);
                    pkgPermission.packageName = cursor.getString(1);
                    pkgPermission.accept = cursor.getLong(2);
                    pkgPermission.reject = cursor.getLong(3);
                    pkgPermission.prompt = cursor.getLong(4);
                    pkgPermission.trust = cursor.getInt(6);
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
        String[] selectionArgs = new String[1];
        selectionArgs[0] = packagePermission.packageName;
        ContentValues values = new ContentValues();
        values.put("accept", Long.valueOf(packagePermission.accept));
        values.put("reject", Long.valueOf(packagePermission.reject));
        values.put("prompt", Long.valueOf(packagePermission.prompt));
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
        if (this.mCurrentLanguage == null || this.mCurrentCountry == null || !this.mCurrentLanguage.equals(language) || !this.mCurrentCountry.equals(country)) {
            this.mCurrentLanguage = language;
            this.mCurrentCountry = country;
            sPermissionsPrompt = Arrays.asList(this.mContext.getResources().getStringArray(201786381));
        }
        int index = OppoPermissionManager.sInterceptingPermissions.indexOf(permission);
        String text = IElsaManager.EMPTY_PACKAGE;
        if (index <= -1 || index >= sPermissionsPrompt.size()) {
            return text;
        }
        return (String) sPermissionsPrompt.get(index);
    }

    private void notifyWaitingApp(PermissionCheckingMsg pcm, int res) {
        try {
            OppoPermissionCallback callBack = pcm.callback;
            if (callBack != null) {
                pcm.pr.isWaitingPermissionChoice = false;
                callBack.notifyApplication(pcm.permission, pcm.pr.pid, res, pcm.token);
            }
        } catch (Exception e) {
            Slog.w(TAG, e);
        }
    }

    private void processPermission(PermissionCheckingMsg pcm, int res, boolean save) {
        String[] data;
        synchronized (this) {
            if (save) {
                changePermissionChoice(pcm.pr.mPackagePermission, pcm.permissionMask, res);
                changePermissionChoice(pcm.pr.mPersistPackagePermission, pcm.permissionMask, res);
                savePermissionChoice(pcm.pr.mPersistPackagePermission);
                ProcessRecord processRecord = pcm.pr;
                processRecord.isSelected = (int) (((long) processRecord.isSelected) & (~pcm.permissionMask));
                try {
                    if (sPermissionCheckMsgList != null && sPermissionCheckMsgList.size() > 0) {
                        PermissionCheckingMsg nextPcm = (PermissionCheckingMsg) sPermissionCheckMsgList.getFirst();
                        if (nextPcm != null && nextPcm.pr.mPersistPackagePermission.packageName.equals(pcm.pr.mPersistPackagePermission.packageName)) {
                            changePermissionChoice(nextPcm.pr.mPackagePermission, pcm.permissionMask, res);
                            changePermissionChoice(nextPcm.pr.mPersistPackagePermission, pcm.permissionMask, res);
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
                    if (pcm.pr.pid != temp.pr.pid) {
                        Log.d(TAG, "pcm.pr.pid != temp.pr.pid");
                    }
                    iterator.remove();
                    notifyWaitingApp(temp, res);
                }
            }
            sPermissionCheckMsgList.notifyAll();
        }
        Intent intent = new Intent("coloros.safecenter.permission.STATISTIC_FRAMEWORK");
        Bundle bundle = new Bundle();
        if (pcm.permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && isChargeSmsByPkg(pcm.pr.info.packageName, this.smsContent, this.smsDestination)) {
            bundle.putInt(SoundModelContract.KEY_TYPE, 1);
            data = new String[3];
            data[0] = pcm.pr.info.packageName;
            data[1] = this.smsContent;
            data[2] = this.smsDestination;
        } else {
            bundle.putInt(SoundModelContract.KEY_TYPE, 0);
            data = new String[2];
            data[0] = pcm.pr.info.packageName;
            data[1] = pcm.permission;
            bundle.putBoolean("checked", save);
        }
        bundle.putInt("reject", res);
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
        View parentView = LayoutInflater.from(this.mContext).inflate(201917501, null);
        TextView permissiomPrompt = (TextView) parentView.findViewById(201458799);
        String packageLabel = getPackageLabel(pcm.pr);
        String securitystr = this.mContext.getString(201589892);
        permissiomPrompt.setText(packageLabel + securitystr + getPermissionPromptStr(pcm.permission));
        this.mSaveCheckBox = (CheckBox) parentView.findViewById(201458801);
        if (pcm.permission.equals(OppoPermissionConstants.PERMISSION_READ_SMS) || pcm.permission.equals(OppoPermissionConstants.PERMISSION_READ_MMS) || pcm.permission.equals(OppoPermissionConstants.PERMISSION_WRITE_MMS) || pcm.permission.equals(OppoPermissionConstants.PERMISSION_READ_CONTACTS)) {
            Log.d(TAG, "when permission is SMS,owWindow, set checkbox is true");
            this.mSaveCheckBox.setChecked(true);
        }
        String rejectStr = this.mContext.getString(201589895);
        String acceptStr = this.mContext.getString(201589896);
        final ChoiceCountDownTimer mCountDownTimer = new ChoiceCountDownTimer(20000, 1000);
        Builder builder = new Builder(this.mContext, 201523207);
        builder.setView(parentView);
        final PermissionCheckingMsg permissionCheckingMsg = pcm;
        builder.setPositiveButton(acceptStr, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                mCountDownTimer.cancel();
                Handler -get9 = OppoPermissionInterceptPolicy.this.mPermissionHandler;
                final PermissionCheckingMsg permissionCheckingMsg = permissionCheckingMsg;
                -get9.post(new Runnable() {
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
                Handler -get9 = OppoPermissionInterceptPolicy.this.mPermissionHandler;
                final PermissionCheckingMsg permissionCheckingMsg = permissionCheckingMsg;
                -get9.post(new Runnable() {
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
        mCountDownTimer.rejectBtn = permissionChoiceDialog.getButton(-2);
        mCountDownTimer.permissionChoiceDialog = permissionChoiceDialog;
        mCountDownTimer.pcm = pcm;
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
        String[] packageNames = new String[5];
        packageNames[0] = "com.qihoo360.mobilesafe";
        packageNames[1] = "com.anguanjia.safe";
        packageNames[2] = "com.blovestorm";
        packageNames[3] = "com.cootek.smartdialer";
        packageNames[4] = "com.sg.sledog";
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

    /* JADX WARNING: Removed duplicated region for block: B:37:0x008f A:{SYNTHETIC, Splitter: B:37:0x008f} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0083 A:{SYNTHETIC, Splitter: B:31:0x0083} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void initAlertAppList(String fileName) {
        Exception ex;
        Throwable th;
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
        if (fileName.equals(ALERT_PERMISSION_APPS) && !regexSms.contains(defaultSmsRegex)) {
            regexSms.add(defaultSmsRegex);
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
        boolean regexSmsUpdated = false;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            if (fileName.equals(ALERT_PERMISSION_APPS)) {
                alertAppList.clear();
                regexSms.clear();
                regexSmsNumber.clear();
                regexSmsSkip.clear();
                regexSmsNumberSkip.clear();
                mBackgroundSkipList.clear();
            } else if (fileName.equals(GAME_FILTER_FILE_NAME)) {
                gameSuffixList.clear();
                gameNameList.clear();
            }
            List<String> tmpRejectDialogPermissionList = new ArrayList();
            int type;
            do {
                type = parser.next();
                if (type == 2) {
                    String tag = parser.getName();
                    if (fileName.equals(ALERT_PERMISSION_APPS)) {
                        if (XML_TAG_ALERT.equals(tag)) {
                            alertAppList.add(parser.nextText());
                        } else if (XML_TAG_ALLOW_BACKGROUND.equals(tag)) {
                            allowBackgroundRequest = Boolean.valueOf(Boolean.parseBoolean(parser.nextText()));
                        } else if (XML_TAG_DCIMPROTECT_ENABLED.equals(tag)) {
                            sDCIMProtectEnabled = Boolean.valueOf(Boolean.parseBoolean(parser.nextText()));
                            echoDCIMProtectEnabled();
                        } else if (XML_TAG_SMS_CONTENT.equals(tag)) {
                            regexSms.add(parser.nextText());
                            regexSmsUpdated = true;
                        } else if (XML_TAG_SMS_NUMBER.equals(tag)) {
                            regexSmsNumber.add(parser.nextText());
                        } else if (XML_TAG_SMS_CONTENT_SKIP.equals(tag)) {
                            regexSmsSkip.add(parser.nextText());
                        } else if (XML_TAG_SMS_NUMBER_SKIP.equals(tag)) {
                            regexSmsNumberSkip.add(parser.nextText());
                        } else if (XML_TAG_BACKGROUND_SKIP.equals(tag)) {
                            mBackgroundSkipList.add(parser.nextText());
                        } else if (XML_TAG_REJECT_DIALOG_PERMISSION.equals(tag)) {
                            String permission = parser.nextText();
                            if (!(permission == null || permission.isEmpty())) {
                                tmpRejectDialogPermissionList.add(permission);
                            }
                        } else if (XML_TAG_BACKGROUND_CAMERA_SKIP.equals(tag)) {
                            String packageName = parser.nextText();
                            mBackgroundCameraSkipPkgs.put(packageName, Integer.valueOf(1));
                        } else if (XML_TAG_OPPO_DCIM_RECYCLE_WHITE.equals(tag)) {
                            String whitePkg = parser.nextText();
                            if (whitePkg != null) {
                                echoUidsToSdcardfs(whitePkg);
                            }
                        }
                    } else if (fileName.equals(GAME_FILTER_FILE_NAME)) {
                        if (XML_TAG_GAME_SUFFIX.equals(tag)) {
                            gameSuffixList.add(parser.nextText());
                        } else if (XML_TAG_GAME_NAME.equals(tag)) {
                            gameNameList.add(parser.nextText());
                        } else if (XML_TAG_SMS_PROMPT_SWITCH.equalsIgnoreCase(tag)) {
                            mSmsPromptSwitch = parser.nextText();
                            if (!(mSmsPromptSwitch.equalsIgnoreCase(SWITCH_ON) || mSmsPromptSwitch.equalsIgnoreCase(SWITCH_OFF) || mSmsPromptSwitch.equalsIgnoreCase(SWITCH_SKIP_OPPO))) {
                                mSmsPromptSwitch = SWITCH_ON;
                            }
                        } else if (XML_TAG_SMS_CHARGE_SWITCH.equalsIgnoreCase(tag)) {
                            mSmsChargeSwitch = parser.nextText();
                            if (!(mSmsChargeSwitch.equalsIgnoreCase(SWITCH_ON) || mSmsChargeSwitch.equalsIgnoreCase(SWITCH_OFF) || mSmsChargeSwitch.equalsIgnoreCase(SWITCH_SKIP_OPPO))) {
                                mSmsChargeSwitch = SWITCH_SKIP_OPPO;
                            }
                        }
                    }
                }
            } while (type != 1);
            if (!regexSmsUpdated) {
                regexSms.add(defaultSmsRegex);
            }
            if (tmpRejectDialogPermissionList != null && !tmpRejectDialogPermissionList.isEmpty()) {
                mRejectDialogPermissionList.clear();
                for (int i = 0; i < tmpRejectDialogPermissionList.size(); i++) {
                    mRejectDialogPermissionList.add((String) tmpRejectDialogPermissionList.get(i));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "readDataFromXML err !!");
        }
    }

    private static boolean isDeviceRooted() {
        String[] paths = new String[5];
        paths[0] = "/sbin/su";
        paths[1] = "/system/bin/su";
        paths[2] = "/system/xbin/su";
        paths[3] = "/system/sbin/su";
        paths[4] = "/vendor/bin/su";
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isChargeSms(String sms, String number) {
        boolean result = false;
        if (!(sms == null || regexSmsSkip == null || regexSmsSkip.size() <= 0)) {
            for (String regex : regexSmsSkip) {
                result = Pattern.compile(regex.trim()).matcher(sms).matches();
                if (result) {
                    return false;
                }
            }
        }
        if (!(number == null || regexSmsNumberSkip == null || regexSmsNumberSkip.size() <= 0)) {
            for (String regex2 : regexSmsNumberSkip) {
                result = Pattern.compile(regex2.trim()).matcher(number).matches();
                if (result) {
                    return false;
                }
            }
        }
        if (sms != null) {
            if (regexSms == null || regexSms.size() == 0) {
                Log.e(TAG, "charge regex is null !");
            }
            if (regexSms != null && regexSms.size() > 0) {
                for (String regex22 : regexSms) {
                    result = Pattern.compile(regex22.trim()).matcher(sms).matches();
                    if (result) {
                        return true;
                    }
                }
            }
        }
        if (!(number == null || regexSmsNumber == null || regexSmsNumber.size() <= 0)) {
            for (String regex222 : regexSmsNumber) {
                result = Pattern.compile(regex222.trim()).matcher(number).matches();
                if (result) {
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
        if (mSmsChargeSwitch.equalsIgnoreCase(SWITCH_SKIP_OPPO)) {
            result = (isFromGameCenter(packageName) || sms == null) ? false : isChargeSms(sms, number);
        } else if (mSmsChargeSwitch.equalsIgnoreCase(SWITCH_ON)) {
            result = sms != null ? isChargeSms(sms, number) : false;
        } else if (mSmsChargeSwitch.equalsIgnoreCase(SWITCH_OFF)) {
            result = false;
        }
        return result;
    }

    private boolean isFromGameCenter(String pkgName) {
        if (!(gameSuffixList == null || gameSuffixList.isEmpty())) {
            for (String suffix : gameSuffixList) {
                if (pkgName.contains(suffix)) {
                    return true;
                }
            }
        }
        return (gameNameList == null || gameNameList.isEmpty() || !gameNameList.contains(pkgName)) ? false : true;
    }

    private void sendNormalSmsStatistic(String pkgName, String permission) {
        if (permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS) && this.smsContent != null) {
            Intent intent = new Intent("coloros.safecenter.permission.STATISTIC_FRAMEWORK");
            Bundle bundle = new Bundle();
            bundle.putInt(SoundModelContract.KEY_TYPE, 2);
            String[] data = new String[3];
            data[0] = pkgName;
            data[1] = this.smsContent;
            data[2] = this.smsDestination;
            bundle.putStringArray("data", data);
            intent.putExtras(bundle);
            this.mContext.sendBroadcast(intent);
            this.smsContent = null;
            this.smsDestination = null;
        }
    }

    private boolean isEnabledInputMethod(Context context, String packageName) {
        List<InputMethodInfo> list = ((InputMethodManager) context.getSystemService("input_method")).getEnabledInputMethodList();
        if (!(list == null || list.isEmpty())) {
            for (InputMethodInfo imi : list) {
                if (packageName.equals(imi.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getCurrentFocus() {
        String result = IElsaManager.EMPTY_PACKAGE;
        if (!(this.mService == null || this.mService.mWindowManager == null)) {
            result = this.mService.mWindowManager.getFocusedWindowPkg();
        }
        return result == null ? IElsaManager.EMPTY_PACKAGE : result;
    }

    private void sendDialogMsg(int msgCode, Bundle data) {
        if (this.mDialogService == null) {
            sRecordMsgCode = msgCode;
            sRecordData = data;
            if (!bindDialogService(this.mContext)) {
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
            intent.setPackage(this.mSafecenterPkg);
            return context.bindService(intent, this.servConnection, 1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "bind service err !");
            return result;
        }
    }

    private void setDefaultDialogResult() {
        this.mPermissionHandler.post(new Runnable() {
            public void run() {
                if (OppoPermissionInterceptPolicy.this.mRecordPcm != null) {
                    OppoPermissionInterceptPolicy.this.processPermission(OppoPermissionInterceptPolicy.this.mRecordPcm, 0, false);
                }
            }
        });
    }

    private boolean checkPackage(String packageName) {
        if (!(packageName == null || packageName.isEmpty())) {
            try {
                this.mContext.getPackageManager().getApplicationInfo(packageName, DumpState.DUMP_PREFERRED_XML);
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
        this.mDCIMDirDeleteEventObserver.startObserving(UEVENT_DIR_DCIM_DELETE_MSG);
    }
}
