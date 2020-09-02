package com.android.server.am;

import android.app.ActivityManager;
import android.app.IActivityController;
import android.app.IWallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.OppoSafeDbReader;
import android.util.Slog;
import android.util.Xml;
import android.view.inputmethod.InputMethodInfo;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.coloros.OppoListManager;
import com.android.server.power.OppoPowerManagerInternal;
import com.android.server.wm.ColorAppStartupManagerHelper;
import com.android.server.wm.ColorFreeformManagerService;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorBroadcastManager implements IColorBroadcastManager {
    private static final String APP_BLACK_LIST = "bl";
    private static final String APP_WHITE_LIST = "wl";
    private static final String BROADCAST_NAME = "bn";
    private static final String CALL_WHITE_LIST = "cw";
    private static boolean DEBUG_ADJUST_OB_REC_QUE = false;
    private static boolean DEBUG_ADJUST_PB_REC_QUE = false;
    /* access modifiers changed from: private */
    public static boolean DEBUG_BROADCAST_FIREWALL = false;
    /* access modifiers changed from: private */
    public static boolean DEBUG_BROADCAST_FIREWALL_LIGHT = false;
    private static boolean DEBUG_JUMP_QUEUE = false;
    private static boolean DEBUG_JUMP_QUEUE_LIGHT = true;
    private static final String DEFAULT_HOME = "com.oppo.launcher";
    private static final String FEATURE_FILTER_POWER_BROADCAST = "oppo.ams.broadcast.filter.power";
    private static final String FEATURE_OPPO_ADJUST_OB_REC_QUE = "oppo.ams.broadcast.adjust.obrecque";
    private static final String FEATURE_OPPO_ADJUST_PB_REC_QUE = "oppo.ams.broadcast.adjust.pbrecque";
    private static final String FEATURE_OPPO_BROADCAST_JUMP_QUEUE = "oppo.ams.broadcast.jumpqueue";
    private static final String FILTER_PKG_NAME_LINE = "jp.naver.line";
    private static final String GOVERNMENT_WHITELIST_PATH = "/system/etc";
    private static final String GOVERNMENT_WHITELIST_XML = "oppo_customize_whitelist.xml";
    private static final int MAX_COUNT_FOR_INIT_IME_LIST = 3;
    static final int MY_PID = Process.myPid();
    private static final String OPPO_SKIP_BROADCAST_CONFIG = "/data/oppo/coloros/config/sys_ams_skipbroadcast.xml";
    private static final String OPPO_SKIP_BROADCAST_PATH = "/data/oppo/coloros/config";
    private static int REASONABLE_COUNT_MAX = 10;
    private static int REASONABLE_COUNT_PERCENT_MAX = 30;
    private static float REASONABLE_INDEX_DISTRIBUTION_REGION_BOUNDARY = 0.6f;
    private static final String SKIP_FEATURE = "feature";
    private static final String TAG = "ColorBroadcastManager";
    private static final int TOP_TASK_NUM = 5;
    private static ColorBroadcastManager mInstance = null;
    private static final Object mLock = new Object();
    private ActivityManagerService mAms;
    private List<String> mAppBlackList;
    private List<String> mAppWhiteList;
    private AppWidgetManager mAppWidgetMgr = null;
    private AudioManager mAudioManager = null;
    private List<String> mBroadcastNameList;
    private ServiceThread mBroadcastThread = null;
    private IColorActivityManagerServiceInner mColorAmsInner = null;
    private FileObserverPolicy mConfigFileObserver;
    /* access modifiers changed from: private */
    public Context mContext;
    private String mCurrentDefaultImePkgName = null;
    private String mDefaultInputMethod = null;
    private boolean mEnableAdjustOrderedBroadcastRecQue = false;
    private boolean mEnableAdjustParallelBroadcastRecQue = false;
    private boolean mEnableJumpQueue = false;
    private boolean mFilterPowerBroadcastForSomeApps = false;
    /* access modifiers changed from: private */
    public boolean mFinishBootComplete = false;
    private int mFinishBroadcastCount = 0;
    private List<String> mGovList;
    private Object mGovernmentLocker = new Object();
    private ServiceThread mHandlerThread = null;
    private int mImeListInitCount = 0;
    private ImeSettingsObserver mImeSettingsObserver = null;
    private boolean mIsImeListInited = false;
    private List<String> mLocalBroadcastName = Arrays.asList("android.intent.action.SCREEN_ON", "android.intent.action.SCREEN_OFF", "android.intent.action.USER_PRESENT", "android.intent.action.CLOSE_SYSTEM_DIALOGS", "android.intent.action.TIME_TICK", "android.provider.Telephony.SMS_RECEIVED");
    private List<InputMethodInfo> mLocalEnableImeInfoList = null;
    private Handler mLocalHandler = null;
    private Object mLocalImeListLocker = new Object();
    BroadcastQueue mOppoBgBroadcastQueue;
    private OppoActivityControlerScheduler mOppoBroadcastBlockMonitor = null;
    ServiceThread mOppoBroadcastThread;
    BroadcastQueue mOppoFgBroadcastQueue;
    private OppoPowerManagerInternal mOppoPowerManagerInternal = null;
    private PowerManagerInternal mPowerManagerInternal = null;
    private boolean mSkipFeature = true;
    private List<String> mSkipLocalList = Arrays.asList("com.coloros.screenrecorder", "com.toprand.voyager", "com.oppo.im", "com.ademo.one.oppo", "com.ademo.two.oppo", "com.rdemo.one.oppo", "com.rdemo.two.oppo", "com.coloros.videoeditor", "com.oppo.toprand");
    final long mTimeoutPeriodForApp = 300000;
    private IWallpaperManager mWallpaperManagerService = null;

    static {
        boolean z = DEBUG_JUMP_QUEUE;
    }

    public static ColorBroadcastManager getInstance() {
        ColorBroadcastManager colorBroadcastManager;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new ColorBroadcastManager();
            }
            colorBroadcastManager = mInstance;
        }
        return colorBroadcastManager;
    }

    private ColorBroadcastManager() {
        Slog.d(TAG, "ColorBroadcastManager create");
    }

    public void init(ActivityManagerService ams) {
        Slog.d(TAG, "init beging...");
        this.mAms = ams;
        this.mContext = this.mAms.mContext;
        initDir();
        initFileObserver();
        readConfigFile();
        Slog.d(TAG, "init end.");
    }

    public void addBroadcastThread() {
        if (this.mBroadcastThread == null) {
            this.mBroadcastThread = new ServiceThread("BroadcastQueue", -2, false);
            this.mBroadcastThread.start();
        }
    }

    public void instanceBroadcast(BroadcastConstants foreConstants, BroadcastConstants backConstants) {
        this.mOppoFgBroadcastQueue = new BroadcastQueue(this.mAms, (Handler) null, "oppoforeground", foreConstants, false);
        this.mOppoBgBroadcastQueue = new BroadcastQueue(this.mAms, (Handler) null, "oppobackground", backConstants, false);
        this.mAms.mBroadcastQueues[3] = this.mOppoFgBroadcastQueue;
        this.mAms.mBroadcastQueues[4] = this.mOppoBgBroadcastQueue;
    }

    public ServiceThread getBroadcastThread() {
        return this.mBroadcastThread;
    }

    public void instanceBroadcastThread(OppoBaseBroadcastQueue queue) {
        if (this.mHandlerThread == null) {
            this.mHandlerThread = new ServiceThread(queue.getBroadcastQueueName(), -2, false);
            this.mHandlerThread.start();
        }
    }

    public void oppoScheduleReceiver(OppoBaseBroadcastQueue queue, Intent intent, BroadcastRecord r, ActivityManagerService service, ProcessRecord app) {
        try {
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("impl oppoScheduleReceiver, begin:");
                    sb.append(r);
                    sb.append(", ");
                    sb.append(intent);
                    Slog.d(TAG, sb.toString());
                } catch (RemoteException e) {
                    e = e;
                    Slog.w(TAG, "impl oppoScheduleReceiver Failure sending broadcast " + r.intent + " to " + app, e);
                }
            }
            OppoReceiverRecord receiverRecord = new OppoReceiverRecord(service, queue, r, r.curApp, app.thread.asBinder(), intent, this.mHandlerThread.getLooper());
            OppoBaseProcessRecord baseProc = typeCastToParent(app);
            try {
                baseProc.requestScheduleReceiver(intent, r.curReceiver, service.compatibilityInfoForPackage(r.curReceiver.applicationInfo), r.resultCode, r.resultData, r.resultExtras, r.ordered, r.userId, app.getReportedProcState(), receiverRecord.hashCode());
                if (!r.ordered) {
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG, "impl oppoScheduleReceiver Schedule receiverRecord.hashCode() = " + receiverRecord.hashCode() + " receiverRecord = " + receiverRecord);
                    }
                    queue.addOppoReceiverRecord(receiverRecord);
                    queue.setOppoReceiverRecord(r, receiverRecord);
                    baseProc.addOppoReceiverRecord(receiverRecord);
                    receiverRecord.setBroadcastTimeoutLocked(300000);
                } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.d(TAG, "impl oppoScheduleReceiver, r is not ordered:" + r);
                }
            } catch (RemoteException e2) {
                e = e2;
                Slog.w(TAG, "impl oppoScheduleReceiver Failure sending broadcast " + r.intent + " to " + app, e);
            }
        } catch (RemoteException e3) {
            e = e3;
            Slog.w(TAG, "impl oppoScheduleReceiver Failure sending broadcast " + r.intent + " to " + app, e);
        }
    }

    public void skipCurrentReceiverLocked(OppoBaseBroadcastQueue queue, ProcessRecord app) {
        typeCastToParent(app).skipCurrentReceiver(queue);
    }

    private void initDir() {
        File broadcastFilePath = new File(OPPO_SKIP_BROADCAST_PATH);
        File broadcastConfigPath = new File(OPPO_SKIP_BROADCAST_CONFIG);
        try {
            if (!broadcastFilePath.exists()) {
                broadcastFilePath.mkdirs();
            }
            if (!broadcastConfigPath.exists()) {
                broadcastConfigPath.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "init skip_broadcast Dir failed!!!");
        }
    }

    public void systemReady() {
        this.mEnableJumpQueue = this.mAms.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_BROADCAST_JUMP_QUEUE);
        this.mEnableAdjustParallelBroadcastRecQue = this.mAms.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_ADJUST_PB_REC_QUE);
        this.mEnableAdjustOrderedBroadcastRecQue = this.mAms.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_ADJUST_OB_REC_QUE);
        this.mFilterPowerBroadcastForSomeApps = this.mAms.mContext.getPackageManager().hasSystemFeature(FEATURE_FILTER_POWER_BROADCAST);
        this.mAppWidgetMgr = AppWidgetManager.getInstance(this.mContext);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mOppoPowerManagerInternal = (OppoPowerManagerInternal) LocalServices.getService(OppoPowerManagerInternal.class);
        this.mLocalHandler = new Handler();
        startImeSettingsObserver();
        updateDefaultInputMethod();
        if (this.mAms.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            this.mGovList = readXMLFile(GOVERNMENT_WHITELIST_PATH, GOVERNMENT_WHITELIST_XML);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0229, code lost:
        android.util.Slog.d(com.android.server.am.ColorBroadcastManager.TAG, "SMS_RECEIVED pass , " + r18);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0241, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0249, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:?, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0066, code lost:
        if (isBroadcastInControledList(r19.getAction()) != false) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0068, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0069, code lost:
        if (r17 != null) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006b, code lost:
        r7 = r16.mAms;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006d, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        r8 = (com.android.server.am.ProcessRecord) r16.mAms.mProcessList.mProcessNames.get(r20, r21);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x007e, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0080, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0085, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0086, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0087, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0089, code lost:
        r8 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x008f, code lost:
        if (r8 != null) goto L_0x00b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0091, code lost:
        android.util.Slog.v(com.android.server.am.ColorBroadcastManager.TAG, "sikp broadcast for proc does not exist, pkg:" + r18 + ", action:" + r19.getAction());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00b3, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00b8, code lost:
        if (r8.uid >= 10000) goto L_0x00bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00ba, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00bd, code lost:
        if (r8.info == null) goto L_0x00c7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00c4, code lost:
        if ((r8.info.flags & 1) == 0) goto L_0x00c7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00c6, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00c7, code lost:
        r7 = r16.mAppWhiteList;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00c9, code lost:
        if (r7 == null) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00cb, code lost:
        if (r18 == null) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00cd, code lost:
        r7 = r7.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00d5, code lost:
        if (r7.hasNext() == false) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00e1, code lost:
        if (r18.contains(r7.next()) == false) goto L_0x00d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00e5, code lost:
        if (com.android.server.am.ColorBroadcastManager.DEBUG_BROADCAST_FIREWALL == false) goto L_0x0109;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00e7, code lost:
        android.util.Slog.d(com.android.server.am.ColorBroadcastManager.TAG, "pkg " + r18 + " in whitelist==" + r19.getAction());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0109, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x010b, code lost:
        r5 = r16.mAppBlackList;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x010d, code lost:
        if (r5 == null) goto L_0x013c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0113, code lost:
        if (r5.contains(r18) == false) goto L_0x013c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0117, code lost:
        if (com.android.server.am.ColorBroadcastManager.DEBUG_BROADCAST_FIREWALL == false) goto L_0x013b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0119, code lost:
        android.util.Slog.d(com.android.server.am.ColorBroadcastManager.TAG, "pkg " + r18 + " in blacklist==" + r19.getAction());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x013b, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x013c, code lost:
        r5 = okToSkip(r18, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0140, code lost:
        if (r5 == false) goto L_0x0171;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0144, code lost:
        if (com.android.server.am.ColorBroadcastManager.DEBUG_BROADCAST_FIREWALL_LIGHT == false) goto L_0x019f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0146, code lost:
        android.util.Slog.d(com.android.server.am.ColorBroadcastManager.TAG, "skip broadcast for pkg:" + r18 + ", action:" + r19.getAction() + " , app:" + r17);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0173, code lost:
        if (com.android.server.am.ColorBroadcastManager.DEBUG_BROADCAST_FIREWALL == false) goto L_0x019f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0175, code lost:
        android.util.Slog.d(com.android.server.am.ColorBroadcastManager.TAG, "not broadcast for pkg:" + r18 + ", action:" + r19.getAction() + " , app:" + r17);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01a9, code lost:
        if ("android.provider.Telephony.SMS_RECEIVED".equals(r19.getAction()) == false) goto L_0x0241;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01b9, code lost:
        if (android.os.SystemProperties.getBoolean("persist.sys.permission.enable", true) == false) goto L_0x0229;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01bd, code lost:
        if (r8.info == null) goto L_0x0229;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01c3, code lost:
        if (r19.getBooleanExtra("skip", true) == false) goto L_0x0229;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x01c5, code lost:
        android.util.Slog.d(com.android.server.am.ColorBroadcastManager.TAG, "SMS_RECEIVED, " + r18 + " , " + r8.pid + " , " + r8.uid);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:?, code lost:
        r0 = new android.content.Intent();
        r0.setAction("oppo.intent.action.PERMISSION_SMS_RECEIVED");
        r0.putExtra("sms_intent", r19);
        r0.putExtra("sms_package", r18);
        r0.setPackage("com.coloros.securitypermission");
        r16.mContext.sendBroadcast(r0, "oppo.permission.OPPO_COMPONENT_SAFE");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0209, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x020b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x020c, code lost:
        android.util.Slog.d(com.android.server.am.ColorBroadcastManager.TAG, "SMS_RECEIVED error." + r0.toString());
     */
    public boolean skipSpecialBroadcast(ProcessRecord app, String pkgName, Intent intent, String processName, int uid, ApplicationInfo info) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.d(TAG, "skipSpecialBroadcast imple:" + intent);
        }
        if (OppoListManager.getInstance().isExpVersion()) {
            return false;
        }
        if (filterSpecialBroadcastBeforeBoot(pkgName, intent)) {
            return true;
        }
        if (!this.mSkipFeature || isInvalidIntent(intent)) {
            return false;
        }
        synchronized (this.mGovernmentLocker) {
            if (this.mGovList != null && this.mGovList.size() > 0 && pkgName != null && this.mGovList.contains(pkgName)) {
                return false;
            }
        }
        while (true) {
        }
    }

    private boolean filterSpecialBroadcastBeforeBoot(String receiverPkgName, Intent broadcastIntent) {
        if (!this.mFilterPowerBroadcastForSomeApps || this.mFinishBootComplete) {
            return false;
        }
        String actionStr = broadcastIntent.getAction();
        if ((!"android.intent.action.ACTION_POWER_DISCONNECTED".equals(actionStr) && !"android.intent.action.ACTION_POWER_CONNECTED".equals(actionStr) && !"android.intent.action.USER_PRESENT".equals(actionStr) && !"android.intent.action.LOCALE_CHANGED".equals(actionStr)) || receiverPkgName == null || !receiverPkgName.startsWith(FILTER_PKG_NAME_LINE)) {
            return false;
        }
        Slog.d(TAG, "filter " + actionStr + " for:" + receiverPkgName);
        return true;
    }

    private boolean isInvalidIntent(Intent intent) {
        return intent == null || intent.getAction() == null;
    }

    private boolean isBroadcastInControledList(String broadcastAction) {
        List<String> list = this.mBroadcastNameList;
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (String name : this.mBroadcastNameList) {
            if (name != null && name.equals(broadcastAction)) {
                return true;
            }
        }
        return false;
    }

    private void initFileObserver() {
        this.mConfigFileObserver = new FileObserverPolicy(OPPO_SKIP_BROADCAST_CONFIG);
        this.mConfigFileObserver.startWatching();
    }

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(ColorBroadcastManager.OPPO_SKIP_BROADCAST_CONFIG)) {
                if (ColorBroadcastManager.DEBUG_BROADCAST_FIREWALL_LIGHT) {
                    Slog.i(ColorBroadcastManager.TAG, "onEvent: focusPath = OPPO_SKIP_BROADCAST_CONFIG");
                }
                ColorBroadcastManager.this.readConfigFile();
            }
        }
    }

    private List<String> readXMLFile(String path, String name) {
        List<String> readFromFileLocked;
        File file = new File(path, name);
        synchronized (this.mGovernmentLocker) {
            readFromFileLocked = readFromFileLocked(file);
        }
        return readFromFileLocked;
    }

    private List<String> readFromFileLocked(File file) {
        int type;
        String pkg;
        FileInputStream stream = null;
        List<String> list = new ArrayList<>();
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName()) && (pkg = parser.getAttributeValue(null, "att")) != null) {
                    list.add(pkg);
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
            }
        } catch (NullPointerException e2) {
            Slog.i(TAG, "failed parsing " + e2);
            if (stream != null) {
                stream.close();
            }
        } catch (NumberFormatException e3) {
            Slog.i(TAG, "failed parsing " + e3);
            if (stream != null) {
                stream.close();
            }
        } catch (XmlPullParserException e4) {
            Slog.i(TAG, "failed parsing " + e4);
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e5) {
            Slog.i(TAG, "failed IOException " + e5);
            if (stream != null) {
                stream.close();
            }
        } catch (IndexOutOfBoundsException e6) {
            Slog.i(TAG, "failed parsing " + e6);
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e7) {
                }
            }
            throw th;
        }
        return list;
    }

    /* access modifiers changed from: private */
    public void readConfigFile() {
        File xmlFile = new File(OPPO_SKIP_BROADCAST_CONFIG);
        if (!xmlFile.exists()) {
            this.mBroadcastNameList = this.mLocalBroadcastName;
            this.mAppWhiteList = this.mSkipLocalList;
            return;
        }
        FileReader xmlReader = null;
        StringReader strReader = null;
        try {
            this.mBroadcastNameList = new ArrayList();
            this.mAppWhiteList = new ArrayList();
            this.mAppBlackList = new ArrayList();
            XmlPullParser parser = Xml.newPullParser();
            try {
                FileReader xmlReader2 = new FileReader(xmlFile);
                parser.setInput(xmlReader2);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType != 0) {
                        if (eventType != 2) {
                            if (eventType != 3) {
                            }
                        } else if (parser.getName().equals(BROADCAST_NAME)) {
                            parser.next();
                            updateBroadcastNameList(parser.getAttributeValue(null, "att"), this.mBroadcastNameList);
                        } else if (parser.getName().equals(SKIP_FEATURE)) {
                            parser.next();
                            updateSkipFeature(parser.getAttributeValue(null, "att"));
                        } else if (parser.getName().equals(APP_WHITE_LIST)) {
                            parser.next();
                            updateBroadcastNameList(parser.getAttributeValue(null, "att"), this.mAppWhiteList);
                        } else if (parser.getName().equals(APP_BLACK_LIST)) {
                            parser.next();
                            updateBroadcastNameList(parser.getAttributeValue(null, "att"), this.mAppBlackList);
                        }
                    }
                }
                try {
                    if (this.mBroadcastNameList != null && this.mBroadcastNameList.isEmpty()) {
                        this.mBroadcastNameList = this.mLocalBroadcastName;
                    }
                    if (this.mAppWhiteList != null && this.mAppWhiteList.isEmpty()) {
                        this.mAppWhiteList = this.mSkipLocalList;
                    }
                    xmlReader2.close();
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (IOException e) {
                    Slog.w(TAG, "Got execption close permReader.", e);
                }
            } catch (FileNotFoundException e2) {
                Slog.w(TAG, "Couldn't find or open skip_broadcast file " + xmlFile);
                try {
                    if (this.mBroadcastNameList != null && this.mBroadcastNameList.isEmpty()) {
                        this.mBroadcastNameList = this.mLocalBroadcastName;
                    }
                    if (this.mAppWhiteList != null && this.mAppWhiteList.isEmpty()) {
                        this.mAppWhiteList = this.mSkipLocalList;
                    }
                    if (xmlReader != null) {
                        xmlReader.close();
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (IOException e3) {
                    Slog.w(TAG, "Got execption close permReader.", e3);
                }
            }
        } catch (Exception e4) {
            Slog.w(TAG, "Got execption parsing permissions.", e4);
            if (this.mBroadcastNameList != null && this.mBroadcastNameList.isEmpty()) {
                this.mBroadcastNameList = this.mLocalBroadcastName;
            }
            if (this.mAppWhiteList != null && this.mAppWhiteList.isEmpty()) {
                this.mAppWhiteList = this.mSkipLocalList;
            }
            if (xmlReader != null) {
                xmlReader.close();
            }
            if (strReader != null) {
                strReader.close();
            }
        } catch (Throwable th) {
            try {
                if (this.mBroadcastNameList != null && this.mBroadcastNameList.isEmpty()) {
                    this.mBroadcastNameList = this.mLocalBroadcastName;
                }
                if (this.mAppWhiteList != null && this.mAppWhiteList.isEmpty()) {
                    this.mAppWhiteList = this.mSkipLocalList;
                }
                if (xmlReader != null) {
                    xmlReader.close();
                }
                if (strReader != null) {
                    strReader.close();
                }
            } catch (IOException e5) {
                Slog.w(TAG, "Got execption close permReader.", e5);
            }
            throw th;
        }
    }

    private void updateBroadcastNameList(String tagName, List<String> list) {
        if (tagName != null && tagName != "" && list != null) {
            list.add(tagName);
        }
    }

    private void updateSkipFeature(String feature) {
        if (feature != null) {
            try {
                this.mSkipFeature = Boolean.parseBoolean(feature);
            } catch (NumberFormatException e) {
                this.mSkipFeature = true;
                Slog.e(TAG, "updateSkipFeature NumberFormatException: ", e);
            }
        }
    }

    private boolean okToSkip(String pkgName, ProcessRecord proc) {
        if (pkgName == null || pkgName.length() <= 0) {
            Slog.w(TAG, "okToSkip, pkgName is empty!");
            return false;
        } else if (OppoSafeDbReader.getInstance(this.mContext).isUserOpen(pkgName) || OppoListManager.getInstance().isInAutoBootWhiteList(pkgName, proc.userId)) {
            return false;
        } else {
            if (isThirdHome(pkgName)) {
                if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                    Slog.v(TAG, "isThirdHome, pkgName:" + pkgName);
                }
                return true;
            } else if (isActiveAudioPlayer(proc.pid, proc.uid)) {
                if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                    Slog.v(TAG, "isActiveAudioPlayer, pkgName:" + pkgName);
                }
                return false;
            } else if (isResumeAndVisible(pkgName, proc)) {
                if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                    Slog.v(TAG, "isResumeAndVisible, pkgName:" + pkgName);
                }
                return false;
            } else {
                String str = this.mDefaultInputMethod;
                if (str == null) {
                    if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                        Slog.v(TAG, "mDefaultInputMethod null, let it go");
                    }
                } else if (pkgName.equals(str)) {
                    if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                        Slog.v(TAG, "isDefaultInputMethodApp, pkgName:" + pkgName);
                    }
                    return false;
                }
                if (OppoListManager.getInstance().isInstalledAppWidget(pkgName, proc.uid)) {
                    if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                        Slog.v(TAG, "isInstalledAppWidget, pkgName:" + pkgName);
                    }
                    return false;
                } else if (isHeldWakeLock(proc.pid)) {
                    if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                        Slog.v(TAG, "isHeldWakeLock, pkgName:" + pkgName);
                    }
                    return false;
                } else if (!isLiveWallpaper(pkgName, proc.userId)) {
                    return true;
                } else {
                    if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                        Slog.v(TAG, "isLiveWallpaper, pkgName:" + pkgName + ", userId=" + proc.userId);
                    }
                    return false;
                }
            }
        }
    }

    private boolean inFrontRecentTaskList(String pkgName) {
        ParceledListSlice<ActivityManager.RecentTaskInfo> slice;
        List<ActivityManager.RecentTaskInfo> recentTasks;
        if (pkgName == null || (slice = this.mAms.getRecentTasks(5, 2, UserHandle.myUserId())) == null || (recentTasks = slice.getList()) == null || recentTasks.size() <= 1) {
            return false;
        }
        for (ActivityManager.RecentTaskInfo taskInfo : recentTasks) {
            if (pkgName.equals(taskInfo.baseIntent.getComponent().getPackageName())) {
                if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                    Slog.v(TAG, "inFrontRecentTaskList packageName:" + pkgName);
                }
                return true;
            }
        }
        return false;
    }

    private boolean isLiveWallpaper(String pkgName, int userId) {
        if (pkgName == null) {
            return false;
        }
        return OppoListManager.getInstance().isLiveWallpaper(pkgName, userId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0028, code lost:
        return true;
     */
    private boolean isResumeAndVisible(String pkgName, ProcessRecord proc) {
        synchronized (this.mAms) {
            if (proc.getWindowProcessController().hasActivities()) {
                return proc.getWindowProcessController().hasVisibleActivities();
            }
            ComponentName topApp = ColorAppStartupManagerHelper.getInstance().getTopActivityComponent(this.mAms);
            if (topApp != null && !pkgName.equals(topApp.getPackageName())) {
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateDefaultInputMethod() {
        Slog.i(TAG, "updateDefaultInputMethod ");
        this.mDefaultInputMethod = getDefaultInputMethod();
    }

    private void startImeSettingsObserver() {
        if (this.mImeSettingsObserver == null) {
            this.mImeSettingsObserver = new ImeSettingsObserver(this.mLocalHandler);
        }
    }

    private String getDefaultInputMethod() {
        String defaultInput = null;
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            try {
                String inputMethod = Settings.Secure.getStringForUser(activityManagerService.mContext.getContentResolver(), "default_input_method", this.mAms.getCurrentUser().id);
                if (inputMethod != null) {
                    defaultInput = inputMethod.substring(0, inputMethod.indexOf("/"));
                }
            } catch (Exception e) {
                Slog.e(TAG, "Failed to get default input method");
            }
        }
        if (DEBUG_BROADCAST_FIREWALL) {
            Slog.i(TAG, "defaultInputMethod " + defaultInput);
        }
        return defaultInput;
    }

    private class ImeSettingsObserver extends ContentObserver {
        ImeSettingsObserver(Handler handler) {
            super(handler);
            ColorBroadcastManager.this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("default_input_method"), false, this, -1);
            if (ColorBroadcastManager.DEBUG_BROADCAST_FIREWALL) {
                Slog.d(ColorBroadcastManager.TAG, "ImeSettingsObserver create end.");
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            ColorBroadcastManager.this.updateDefaultInputMethod();
        }
    }

    private boolean isActiveAudioPlayer(int pid, int uid) {
        String[] pidsArray;
        if (pid <= 0) {
            return false;
        }
        if (this.mOppoBroadcastBlockMonitor == null) {
            this.mOppoBroadcastBlockMonitor = new OppoActivityControlerScheduler((IActivityController) null);
        }
        String pids = this.mOppoBroadcastBlockMonitor.scheduleGetParameters(this.mContext, "get_pid");
        if (pids != null && pids.equals("block")) {
            return true;
        }
        if (pids == null || pids.length() <= 0 || (pidsArray = pids.split(":")) == null || pidsArray.length <= 0) {
            return false;
        }
        for (ProcessRecord app : getProcessForUid(uid)) {
            String pidStr = String.valueOf(app.pid);
            int length = pidsArray.length;
            int i = 0;
            while (true) {
                if (i < length) {
                    if (pidStr.equals(pidsArray[i])) {
                        return true;
                    }
                    i++;
                }
            }
        }
        return false;
    }

    private ArrayList<ProcessRecord> getProcessForUid(int uid) {
        ArrayList<ProcessRecord> res;
        synchronized (this.mAms) {
            res = new ArrayList<>();
            for (int i = this.mAms.mProcessList.mLruProcesses.size() - 1; i >= 0; i--) {
                ProcessRecord rec = (ProcessRecord) this.mAms.mProcessList.mLruProcesses.get(i);
                if (rec.thread != null && rec.uid == uid) {
                    res.add(rec);
                }
            }
        }
        return res;
    }

    private boolean isHeldWakeLock(int pid) {
        int[] wakeLockPids;
        if (pid <= 0 || (wakeLockPids = this.mOppoPowerManagerInternal.getWakeLockedPids()) == null || wakeLockPids.length <= 0) {
            return false;
        }
        for (int i : wakeLockPids) {
            if (pid == i) {
                return true;
            }
        }
        return false;
    }

    private List<ResolveInfo> queryHomeResolveInfo() {
        Intent homeIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        homeIntent.addCategory("android.intent.category.HOME");
        return this.mAms.mContext.getPackageManager().queryIntentActivities(homeIntent, 270532608);
    }

    private boolean isHomeProcess(String packageName) {
        for (ResolveInfo ri : queryHomeResolveInfo()) {
            if (packageName.equals(ri.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isThirdHome(String packageName) {
        for (ResolveInfo ri : queryHomeResolveInfo()) {
            if (packageName.equals(ri.activityInfo.packageName)) {
                if (!"com.oppo.launcher".equals(packageName)) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public void informReadyToBootComplete() {
        this.mLocalHandler.postDelayed(new Runnable() {
            /* class com.android.server.am.ColorBroadcastManager.AnonymousClass1 */

            public void run() {
                boolean unused = ColorBroadcastManager.this.mFinishBootComplete = true;
            }
        }, 10000);
    }

    public void adjustQueueIfNecessary(ArrayList<BroadcastRecord> broadcastsQueue, BroadcastRecord r) {
        ProcessRecord topApp;
        if (this.mEnableJumpQueue && this.mFinishBootComplete) {
            int size = broadcastsQueue.size();
            if (size > 2) {
                boolean isBroadcastQueuePrior = false;
                boolean enqueueAtHead = false;
                int callingPid = r.callingPid;
                Intent actionIntent = r.intent;
                if (actionIntent != null) {
                    isBroadcastQueuePrior = (actionIntent.getFlags() & 1048576) != 0;
                    enqueueAtHead = isBroadcastQueuePrior;
                }
                if (!enqueueAtHead && (topApp = getTopAppLockedForBroadcast()) != null && topApp.pid == callingPid) {
                    enqueueAtHead = true;
                }
                if (enqueueAtHead) {
                    if (DEBUG_JUMP_QUEUE) {
                        Slog.d(TAG, "adjustQueueIfNecessary begin sort. size:" + size + ", queueSize:" + broadcastsQueue.size());
                    }
                    int countOfBroadcastR = 0;
                    int sumOfSeq = 0;
                    ArrayList<Integer> tmpBroadcastsIndexs = new ArrayList<>();
                    for (int i = 1; i < size; i++) {
                        BroadcastRecord tmpRecord = broadcastsQueue.get(i);
                        if (tmpRecord != null && tmpRecord.callingPid == callingPid) {
                            countOfBroadcastR++;
                            sumOfSeq += i;
                            tmpBroadcastsIndexs.add(new Integer(i));
                        }
                    }
                    if (isBroadcastQueuePrior || estimateAdjust(countOfBroadcastR, sumOfSeq, size)) {
                        for (int k = 0; k < tmpBroadcastsIndexs.size(); k++) {
                            int adjustIndex = tmpBroadcastsIndexs.get(k).intValue();
                            if (adjustIndex < 0 || adjustIndex >= size) {
                                Slog.w(TAG, "adjust error happend, adjustIndex:" + adjustIndex + ", size:" + size);
                            } else {
                                broadcastsQueue.remove(adjustIndex);
                                broadcastsQueue.add(k + 1, broadcastsQueue.get(adjustIndex));
                            }
                        }
                        if (DEBUG_JUMP_QUEUE_LIGHT) {
                            Slog.d(TAG, "adjustQueueIfNecessary, do jump queue. size:" + size + ", broadcast:" + r);
                        }
                    } else if (DEBUG_JUMP_QUEUE) {
                        Slog.d(TAG, "adjustQueueIfNecessary:estimate ignore jump queue:" + r);
                    }
                } else if (DEBUG_JUMP_QUEUE) {
                    Slog.d(TAG, "adjustQueueIfNecessary:not from top app. Ignore jump queue:" + r);
                }
            } else if (DEBUG_JUMP_QUEUE) {
                Slog.d(TAG, "adjustQueueIfNecessary return for size <= 2, broadcast:" + r);
            }
        }
    }

    private boolean estimateAdjust(int countOfBroadcastR, int sumOfSeq, int totalSize) {
        if (totalSize <= 0 || countOfBroadcastR <= 0 || sumOfSeq <= 0 || countOfBroadcastR > totalSize) {
            return false;
        }
        if (DEBUG_JUMP_QUEUE) {
            Slog.d(TAG, "estimateAdjust, countOfBroadcastR:" + countOfBroadcastR + ", sumOfSeq:" + sumOfSeq + ", totalSize:" + totalSize);
        }
        if (!(countOfBroadcastR <= REASONABLE_COUNT_MAX)) {
            if (DEBUG_JUMP_QUEUE) {
                Slog.d(TAG, "estimateAdjust:conunt is unreasonable!");
            }
            return false;
        }
        if (!(((int) (((float) (countOfBroadcastR * 100)) / (((float) totalSize) * 1.0f))) <= REASONABLE_COUNT_PERCENT_MAX)) {
            if (DEBUG_JUMP_QUEUE) {
                Slog.d(TAG, "estimateAdjust:conunt percent is unreasonable!");
            }
            return false;
        }
        if (sumOfSeq / countOfBroadcastR >= ((int) (((float) totalSize) * REASONABLE_INDEX_DISTRIBUTION_REGION_BOUNDARY))) {
            return true;
        }
        if (DEBUG_JUMP_QUEUE) {
            Slog.d(TAG, "estimateAdjust:conunt index distribution is unreasonable!");
        }
        return false;
    }

    public void adjustParallelBroadcastReceiversQueue(BroadcastRecord broadcastRecord) {
        if (this.mEnableAdjustParallelBroadcastRecQue) {
            ProcessRecord TOP_APP = getTopAppLockedForBroadcast();
            int N = broadcastRecord.receivers.size();
            for (int i = 0; i < N; i++) {
                Object target = broadcastRecord.receivers.get(i);
                ProcessRecord curApp = ((BroadcastFilter) target).receiverList.app;
                if (curApp == TOP_APP) {
                    broadcastRecord.receivers.remove(i);
                    broadcastRecord.receivers.add(0, target);
                    if (DEBUG_ADJUST_PB_REC_QUE) {
                        Slog.v(TAG, "adjust pb receiver queue for app:" + curApp);
                    }
                }
            }
        }
    }

    public void adjustOrderedBroadcastReceiversQueue(BroadcastRecord broadcastRecord, int numReceivers) {
        ProcessRecord curApp;
        if (!this.mEnableAdjustOrderedBroadcastRecQue || broadcastRecord == null) {
            return;
        }
        if (broadcastRecord == null || broadcastRecord.intent == null || !"android.provider.Telephony.SMS_RECEIVED".equals(broadcastRecord.intent.getAction())) {
            ProcessRecord TOP_APP = getTopAppLockedForBroadcast();
            if (DEBUG_ADJUST_OB_REC_QUE) {
                Slog.v(TAG, "nextReceiver:" + broadcastRecord.nextReceiver + ", TOP_APP:" + TOP_APP);
            }
            for (int i = broadcastRecord.nextReceiver; i < numReceivers; i++) {
                Object target = broadcastRecord.receivers.get(i);
                if (target instanceof BroadcastFilter) {
                    curApp = ((BroadcastFilter) target).receiverList.app;
                } else {
                    ResolveInfo info = (ResolveInfo) target;
                    curApp = this.mAms.getProcessRecordLocked(info.activityInfo.processName, info.activityInfo.applicationInfo.uid, false);
                }
                if (curApp != null && curApp == TOP_APP && curApp.info != null && !"com.gotokeep.keep".equals(curApp.info.processName) && !"me.ele.napos".equals(curApp.info.processName)) {
                    broadcastRecord.receivers.remove(i);
                    broadcastRecord.receivers.add(broadcastRecord.nextReceiver, target);
                    if (DEBUG_ADJUST_OB_REC_QUE) {
                        Slog.v(TAG, "adjust pb receiver queue for app:" + curApp);
                    }
                }
            }
        }
    }

    public boolean isPendingBroadcastProcessLocked(int pid) {
        return this.mOppoFgBroadcastQueue.isPendingBroadcastProcessLocked(pid) || this.mOppoBgBroadcastQueue.isPendingBroadcastProcessLocked(pid);
    }

    public BroadcastQueue broadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0;
        intent.addFlags(524288);
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND) {
            StringBuilder sb = new StringBuilder();
            sb.append("oppo Broadcast intent ");
            sb.append(intent);
            sb.append(" on ");
            sb.append(isFg ? "foreground" : "background");
            sb.append(" queue");
            Slog.i(TAG, sb.toString());
        }
        return isFg ? this.mOppoFgBroadcastQueue : this.mOppoBgBroadcastQueue;
    }

    public BroadcastQueue getQueueFromFlag(int flags) {
        boolean isFg = (268435456 & flags) != 0;
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND) {
            Slog.i(TAG, "this is isFg " + isFg);
        }
        return isFg ? this.mOppoFgBroadcastQueue : this.mOppoBgBroadcastQueue;
    }

    public List adjustReceiverList(List receivers, Intent intent) {
        BroadcastFilter bf;
        if (intent == null || receivers == null || !"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            if (!(intent == null || receivers == null || !"android.intent.action.SCREEN_ON".equals(intent.getAction()))) {
                ResolveInfo riToEnd = null;
                BroadcastFilter bfToEnd = null;
                Iterator it = receivers.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Object temp = it.next();
                    if (!(temp instanceof ResolveInfo)) {
                        if (!(!(temp instanceof BroadcastFilter) || (bf = (BroadcastFilter) temp) == null || bf.receiverList == null || bf.receiverList.app == null || bf.receiverList.app.info == null || !"com.gotokeep.keep".equals(bf.receiverList.app.info.processName))) {
                            bfToEnd = bf;
                            break;
                        }
                    } else {
                        ResolveInfo ri = (ResolveInfo) temp;
                        if (!(ri == null || ri.activityInfo == null || ri.activityInfo.applicationInfo == null || !"com.gotokeep.keep".equals(ri.activityInfo.applicationInfo.processName))) {
                            riToEnd = ri;
                            break;
                        }
                    }
                }
                if (riToEnd != null) {
                    receivers.remove(riToEnd);
                    receivers.add(riToEnd);
                } else if (bfToEnd != null) {
                    receivers.remove(bfToEnd);
                    receivers.add(bfToEnd);
                }
            }
            return receivers;
        }
        List<Object> systemList = new ArrayList();
        List<Object> thirdList = new ArrayList();
        List res = new ArrayList();
        for (Object temp2 : receivers) {
            if (temp2 instanceof ResolveInfo) {
                ResolveInfo ri2 = (ResolveInfo) temp2;
                if (ri2 == null || ri2.activityInfo == null || ri2.activityInfo.applicationInfo == null || (ri2.activityInfo.applicationInfo.flags & 1) == 0) {
                    thirdList.add(ri2);
                } else {
                    systemList.add(ri2);
                }
            } else if (temp2 instanceof BroadcastFilter) {
                BroadcastFilter bf2 = (BroadcastFilter) temp2;
                if (bf2 == null || bf2.receiverList == null || bf2.receiverList.app == null || bf2.receiverList.app.info == null || (bf2.receiverList.app.info.flags & 1) == 0) {
                    thirdList.add(bf2);
                } else {
                    systemList.add(bf2);
                }
            }
        }
        for (Object o : systemList) {
            res.add(o);
        }
        for (Object o2 : thirdList) {
            res.add(o2);
        }
        return res;
    }

    public void finishNotOrderReceiver(IBinder who, int hasCode, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG, "Finish not order hasCode: " + hasCode);
        }
        long origId = Binder.clearCallingIdentity();
        boolean doNext = false;
        try {
            synchronized (this.mAms) {
                OppoReceiverRecord receiverRecord = broadcastRecordForNotOrderReceiverLocked(who, hasCode);
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG, "receiverRecord: " + receiverRecord);
                }
                if (receiverRecord != null) {
                    if (!receiverRecord.mHasFinish) {
                        OppoBaseBroadcastQueue baseQueue = receiverRecord.getBroadcastQueue();
                        BroadcastRecord r = receiverRecord.getBroadcastRecord();
                        if (baseQueue != null) {
                            if (r != null && baseQueue.getMessageDelayFlagOfBroadcastRecord(r)) {
                                baseQueue.removeNextMessages(r);
                                doNext = r.queue.finishReceiverLocked(r, resultCode, resultData, resultExtras, resultAbort, true);
                            }
                            if (doNext) {
                                baseQueue.requestProcessNextBroadcastLocked(false, true);
                            }
                        }
                    }
                    receiverRecord.cancelBroadcastTimeoutLocked();
                }
            }
            this.mFinishBroadcastCount++;
            if (this.mFinishBroadcastCount == 5) {
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG, "trimApplications() ");
                }
                this.mAms.trimApplications("updateOomAdj_finishReceiver");
                this.mFinishBroadcastCount = 0;
            }
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public OppoReceiverRecord broadcastRecordForNotOrderReceiverLocked(IBinder receiver, int hasCode) {
        for (BroadcastQueue queue : this.mAms.mBroadcastQueues) {
            OppoBaseBroadcastQueue baseQueue = typeCastToParent(queue);
            OppoReceiverRecord receiverRecord = baseQueue.getOppoReceiverRecord(hasCode);
            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Slog.v(TAG, "getMatchingNotOrderedReceiver receiverRecord " + receiverRecord);
            }
            if (receiverRecord != null) {
                ProcessRecord app = receiverRecord.getApp();
                if (app != null) {
                    OppoBaseProcessRecord baseProc = typeCastToParent(app);
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG, "app.receiverRecords size = " + baseProc.getOppoReceiverRecordListSize() + " app: " + app);
                    }
                    baseProc.removeOppoReceiverRecord(receiverRecord);
                } else if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.d(TAG, "broadcastRecordForNotOrderReceiverLocked, can't found process for:" + receiverRecord);
                }
                baseQueue.removeOppoReceiverRecord(receiverRecord);
                return receiverRecord;
            }
        }
        return null;
    }

    public boolean isReceivingBroadcastLocked(ProcessRecord app) {
        if (app != null) {
            return typeCastToParent(app).isReceivingBroadcast();
        }
        return false;
    }

    public void broadcastOppoBootComleteLocked(Intent intent, IIntentReceiver resultTo, int userId) {
        this.mAms.broadcastIntentLocked((ProcessRecord) null, (String) null, intent, (String) null, resultTo, 0, (String) null, (Bundle) null, new String[]{"android.permission.RECEIVE_BOOT_COMPLETED"}, -1, (Bundle) null, true, false, MY_PID, (int) ColorFreeformManagerService.FREEFORM_CALLER_UID, Binder.getCallingUid(), Binder.getCallingPid(), userId);
    }

    public void handleDynamicLog(boolean on) {
        DEBUG_BROADCAST_FIREWALL = on;
        DEBUG_JUMP_QUEUE = on;
        DEBUG_JUMP_QUEUE_LIGHT = on;
        DEBUG_ADJUST_PB_REC_QUE = on;
        DEBUG_ADJUST_OB_REC_QUE = on;
        DEBUG_BROADCAST_FIREWALL_LIGHT = on;
    }

    public final boolean dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage, boolean needSep) {
        pw.println(" ColorBroadcastManager: ");
        pw.println(" mCurrentDefaultImePkgName:" + this.mCurrentDefaultImePkgName);
        List<InputMethodInfo> imeInfoList = this.mLocalEnableImeInfoList;
        if (imeInfoList == null) {
            pw.println(" mLocalEnableImeInfoList null.");
            return true;
        } else if (imeInfoList.size() <= 0) {
            pw.println(" mLocalEnableImeInfoList empty.");
            return true;
        } else {
            int index = 0;
            for (InputMethodInfo im : imeInfoList) {
                String imePkgName = im.getPackageName();
                pw.println(" LocalEnableIme[" + index + "]:" + imePkgName);
                index++;
            }
            return true;
        }
    }

    public BroadcastQueue getOppoFgBroadcastQueue() {
        return this.mOppoFgBroadcastQueue;
    }

    public boolean hasOppoBroadcastManager() {
        if (!ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            return true;
        }
        Slog.d(TAG, "impl hasOppoBroadcastManager true");
        return true;
    }

    private OppoBaseBroadcastQueue typeCastToParent(BroadcastQueue queue) {
        return (OppoBaseBroadcastQueue) ColorTypeCastingHelper.typeCasting(OppoBaseBroadcastQueue.class, queue);
    }

    private OppoBaseProcessRecord typeCastToParent(ProcessRecord processRecord) {
        return (OppoBaseProcessRecord) ColorTypeCastingHelper.typeCasting(OppoBaseProcessRecord.class, processRecord);
    }

    private OppoBaseActivityManagerService typeCastToParent(ActivityManagerService ams) {
        return (OppoBaseActivityManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityManagerService.class, ams);
    }

    private ProcessRecord getTopAppLockedForBroadcast() {
        if (this.mColorAmsInner == null) {
            this.mColorAmsInner = typeCastToParent(this.mAms).mColorAmsInner;
        }
        return this.mColorAmsInner.getTopAppLockedForBroadcast();
    }
}
