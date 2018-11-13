package com.android.server.am;

import android.app.ActivityManager.RecentTaskInfo;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.net.arp.OppoArpPeer;
import android.os.FileObserver;
import android.os.Handler;
import android.os.PowerManagerInternal;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.OppoSafeDbReader;
import android.util.Slog;
import android.util.Xml;
import android.view.inputmethod.InputMethodInfo;
import com.android.server.InputMethodManagerService;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.coloros.OppoListManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class OppoBroadcastManager {
    private static final String APP_BLACK_LIST = "bl";
    private static final String APP_WHITE_LIST = "wl";
    private static final String BROADCAST_NAME = "bn";
    private static final String CALL_WHITE_LIST = "cw";
    private static boolean DEBUG_ADJUST_OB_REC_QUE = false;
    private static boolean DEBUG_ADJUST_PB_REC_QUE = false;
    private static boolean DEBUG_BROADCAST_FIREWALL = false;
    private static boolean DEBUG_BROADCAST_FIREWALL_LIGHT = false;
    private static boolean DEBUG_JUMP_QUEUE = false;
    private static boolean DEBUG_JUMP_QUEUE_LIGHT = false;
    private static final String DEFAULT_HOME = "com.oppo.launcher";
    private static final String FEATURE_FILTER_POWER_BROADCAST = "oppo.ams.broadcast.filter.power";
    private static final String FEATURE_OPPO_ADJUST_OB_REC_QUE = "oppo.ams.broadcast.adjust.obrecque";
    private static final String FEATURE_OPPO_ADJUST_PB_REC_QUE = "oppo.ams.broadcast.adjust.pbrecque";
    private static final String FEATURE_OPPO_BROADCAST_JUMP_QUEUE = "oppo.ams.broadcast.jumpqueue";
    private static final String FILTER_PKG_NAME_LINE = "jp.naver.line";
    private static final String GOVERNMENT_WHITELIST_PATH = "/system/etc";
    private static final String GOVERNMENT_WHITELIST_XML = "oppo_customize_whitelist.xml";
    private static final int MAX_COUNT_FOR_INIT_IME_LIST = 3;
    private static final String OPPO_SKIP_BROADCAST_CONFIG = "/data/oppo/coloros/config/sys_ams_skipbroadcast.xml";
    private static final String OPPO_SKIP_BROADCAST_PATH = "/data/oppo/coloros/config";
    private static int REASONABLE_COUNT_MAX = 10;
    private static int REASONABLE_COUNT_PERCENT_MAX = 30;
    private static float REASONABLE_INDEX_DISTRIBUTION_REGION_BOUNDARY = 0.6f;
    private static final String SKIP_FEATURE = "feature";
    private static final String TAG = "OppoBroadcastManager";
    private static final int TOP_TASK_NUM = 5;
    private static OppoBroadcastManager mInstance = null;
    private static final Object mLock = new Object();
    final ActivityManagerService mAms;
    private List<String> mAppBlackList;
    private List<String> mAppWhiteList;
    private AppWidgetManager mAppWidgetMgr = null;
    private AudioManager mAudioManager = null;
    private List<String> mBroadcastNameList;
    private FileObserverPolicy mConfigFileObserver;
    private Context mContext;
    private String mCurrentDefaultImePkgName = null;
    private boolean mEnableAdjustOrderedBroadcastRecQue = false;
    private boolean mEnableAdjustParallelBroadcastRecQue = false;
    private boolean mEnableJumpQueue = false;
    private boolean mFilterPowerBroadcastForSomeApps = false;
    private boolean mFinishBootComplete = false;
    private List<String> mGovList;
    private Object mGovernmentLocker = new Object();
    private int mImeListInitCount = 0;
    private ImeSettingsObserver mImeSettingsObserver = null;
    private boolean mIsImeListInited = false;
    private List<String> mLocalBroadcastName = Arrays.asList(new String[]{"android.intent.action.SCREEN_ON", "android.intent.action.SCREEN_OFF", "android.intent.action.USER_PRESENT", "android.intent.action.CLOSE_SYSTEM_DIALOGS", "android.intent.action.TIME_TICK"});
    private List<InputMethodInfo> mLocalEnableImeInfoList = null;
    private Handler mLocalHandler = null;
    private Object mLocalImeListLocker = new Object();
    BroadcastQueue mOppoBgBroadcastQueue;
    OppoActivityControlerScheduler mOppoBroadcastBlockMonitor = null;
    ServiceThread mOppoBroadcastThread;
    BroadcastQueue mOppoFgBroadcastQueue;
    private PowerManagerInternal mPowerManagerInternal = null;
    private boolean mSkipFeature = true;
    private List<String> mSkipLocalList = Arrays.asList(new String[]{"com.coloros.screenrecorder", "com.toprand.voyager", "com.oppo.im", "com.oppo.toprand"});
    private final Runnable mUpdateEnableImeCallBack = new Runnable() {
        public void run() {
            List<InputMethodInfo> list = OppoBroadcastManager.this.getEnableInputMethodList();
            if (OppoBroadcastManager.DEBUG_BROADCAST_FIREWALL) {
                Slog.d(OppoBroadcastManager.TAG, "run mUpdateEnableImeCallBack");
                if (list != null) {
                    for (InputMethodInfo im : list) {
                        Slog.d(OppoBroadcastManager.TAG, "mUpdateEnableImeCallBack, enableIme:" + im.getPackageName());
                    }
                }
            }
            OppoBroadcastManager oppoBroadcastManager = OppoBroadcastManager.this;
            oppoBroadcastManager.mImeListInitCount = oppoBroadcastManager.mImeListInitCount + 1;
            if ((list == null || list.isEmpty()) && OppoBroadcastManager.this.mImeListInitCount <= 3) {
                OppoBroadcastManager.this.updateLocalEnableImeList(true, OppoArpPeer.ARP_DUP_RESPONSE_TIMEOUT);
                return;
            }
            synchronized (OppoBroadcastManager.this.mLocalImeListLocker) {
                OppoBroadcastManager.this.mLocalEnableImeInfoList = list;
            }
            OppoBroadcastManager.this.mIsImeListInited = true;
        }
    };

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(OppoBroadcastManager.OPPO_SKIP_BROADCAST_CONFIG)) {
                if (OppoBroadcastManager.DEBUG_BROADCAST_FIREWALL_LIGHT) {
                    Slog.i(OppoBroadcastManager.TAG, "onEvent: focusPath = OPPO_SKIP_BROADCAST_CONFIG");
                }
                OppoBroadcastManager.this.readConfigFile();
            }
        }
    }

    private class ImeSettingsObserver extends ContentObserver {
        private Handler mHandler;

        ImeSettingsObserver(Handler handler) {
            super(handler);
            this.mHandler = handler;
            ContentResolver resolver = OppoBroadcastManager.this.mContext.getContentResolver();
            resolver.registerContentObserver(Secure.getUriFor("default_input_method"), false, this);
            resolver.registerContentObserver(Secure.getUriFor("enabled_input_methods"), false, this);
            if (OppoBroadcastManager.DEBUG_BROADCAST_FIREWALL) {
                Slog.d(OppoBroadcastManager.TAG, "ImeSettingsObserver create end.");
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (OppoBroadcastManager.DEBUG_BROADCAST_FIREWALL) {
                Slog.d(OppoBroadcastManager.TAG, "ImeSettingsObserver.onChange");
            }
            this.mHandler.postDelayed(OppoBroadcastManager.this.mUpdateEnableImeCallBack, 1000);
        }
    }

    public static OppoBroadcastManager getInstance(ActivityManagerService service) {
        OppoBroadcastManager oppoBroadcastManager;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new OppoBroadcastManager(service);
            }
            oppoBroadcastManager = mInstance;
        }
        return oppoBroadcastManager;
    }

    private OppoBroadcastManager(ActivityManagerService service) {
        this.mAms = service;
        this.mContext = this.mAms.mContext;
        this.mOppoFgBroadcastQueue = new BroadcastQueue(this.mAms, this.mAms.mBroadcastThread, "oppoforeground", 10000, false);
        this.mOppoBgBroadcastQueue = new BroadcastQueue(this.mAms, this.mAms.mBroadcastThread, "oppobackground", 60000, false);
        this.mAms.mBroadcastQueues[2] = this.mOppoFgBroadcastQueue;
        this.mAms.mBroadcastQueues[3] = this.mOppoBgBroadcastQueue;
        initDir();
        initFileObserver();
        readConfigFile();
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

    protected void systemReady() {
        this.mEnableJumpQueue = this.mAms.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_BROADCAST_JUMP_QUEUE);
        this.mEnableAdjustParallelBroadcastRecQue = this.mAms.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_ADJUST_PB_REC_QUE);
        this.mEnableAdjustOrderedBroadcastRecQue = this.mAms.mContext.getPackageManager().hasSystemFeature(FEATURE_OPPO_ADJUST_OB_REC_QUE);
        this.mFilterPowerBroadcastForSomeApps = this.mAms.mContext.getPackageManager().hasSystemFeature(FEATURE_FILTER_POWER_BROADCAST);
        this.mAppWidgetMgr = AppWidgetManager.getInstance(this.mContext);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mLocalHandler = new Handler();
        startImeSettingsObserver();
        updateLocalEnableImeList(false, 0);
        if (this.mAms.mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            this.mGovList = readXMLFile(GOVERNMENT_WHITELIST_PATH, GOVERNMENT_WHITELIST_XML);
        }
    }

    /* JADX WARNING: Missing block: B:27:0x003b, code:
            if (isBroadcastInControledList(r11.getAction()) != false) goto L_0x0042;
     */
    /* JADX WARNING: Missing block: B:29:0x003e, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:33:0x0042, code:
            if (r9 != null) goto L_0x008b;
     */
    /* JADX WARNING: Missing block: B:34:0x0044, code:
            r6 = r8.mAms;
     */
    /* JADX WARNING: Missing block: B:35:0x0046, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
            r4 = (com.android.server.am.ProcessRecord) r8.mAms.mProcessNames.get(r12, r13);
     */
    /* JADX WARNING: Missing block: B:38:0x0054, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:43:0x0087, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:44:0x008b, code:
            r4 = r9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean skipSpecialBroadcast(ProcessRecord app, String pkgName, Intent intent, String processName, int uid, ApplicationInfo info) {
        if (filterSpecialBroadcastBeforeBoot(pkgName, intent)) {
            return true;
        }
        if (!this.mSkipFeature) {
            return false;
        }
        if (isInvalidIntent(intent)) {
            return false;
        }
        synchronized (this.mGovernmentLocker) {
            if (this.mGovList == null || this.mGovList.size() <= 0 || pkgName == null || !this.mGovList.contains(pkgName)) {
            } else {
                return false;
            }
        }
        if (proc == null) {
            Slog.v(TAG, "sikp broadcast for proc does not exist, pkg:" + pkgName + ", action:" + intent.getAction());
            return true;
        } else if (proc.uid < 10000) {
            return false;
        } else {
            if (proc.info != null && (proc.info.flags & 1) != 0) {
                return false;
            }
            if (!(this.mAppWhiteList == null || pkgName == null)) {
                for (String pkg : this.mAppWhiteList) {
                    if (pkgName.contains(pkg)) {
                        if (DEBUG_BROADCAST_FIREWALL) {
                            Slog.d(TAG, "pkg " + pkgName + " in whitelist" + "==" + intent.getAction());
                        }
                        return false;
                    }
                }
            }
            if (this.mAppBlackList == null || !this.mAppBlackList.contains(pkgName)) {
                boolean isOkToSkip = okToSkip(pkgName, proc);
                if (isOkToSkip) {
                    if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                        Slog.d(TAG, "skip broadcast for pkg:" + pkgName + ", action:" + intent.getAction() + " , app:" + app);
                    }
                } else if (DEBUG_BROADCAST_FIREWALL) {
                    Slog.d(TAG, "not broadcast for pkg:" + pkgName + ", action:" + intent.getAction() + " , app:" + app);
                }
                return isOkToSkip;
            }
            if (DEBUG_BROADCAST_FIREWALL) {
                Slog.d(TAG, "pkg " + pkgName + " in blacklist" + "==" + intent.getAction());
            }
            return true;
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
        if (this.mBroadcastNameList == null || this.mBroadcastNameList.isEmpty()) {
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

    public List<String> readXMLFile(String path, String name) {
        List<String> readFromFileLocked;
        File file = new File(path, name);
        synchronized (this.mGovernmentLocker) {
            readFromFileLocked = readFromFileLocked(file);
        }
        return readFromFileLocked;
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:0x00f8 A:{SYNTHETIC, Splitter: B:57:0x00f8} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00ed A:{SYNTHETIC, Splitter: B:52:0x00ed} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00c8 A:{SYNTHETIC, Splitter: B:45:0x00c8} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a5 A:{SYNTHETIC, Splitter: B:38:0x00a5} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0082 A:{SYNTHETIC, Splitter: B:31:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x005f A:{SYNTHETIC, Splitter: B:24:0x005f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<String> readFromFileLocked(File file) {
        NullPointerException e;
        NumberFormatException e2;
        XmlPullParserException e3;
        IOException e4;
        IndexOutOfBoundsException e5;
        Throwable th;
        FileInputStream stream = null;
        List<String> list = new ArrayList();
        try {
            FileInputStream stream2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                            String pkg = parser.getAttributeValue(null, "att");
                            if (pkg != null) {
                                list.add(pkg);
                            }
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e6) {
                    }
                }
                stream = stream2;
            } catch (NullPointerException e7) {
                e = e7;
                stream = stream2;
                Slog.i(TAG, "failed parsing " + e);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e8) {
                    }
                }
                return list;
            } catch (NumberFormatException e9) {
                e2 = e9;
                stream = stream2;
                Slog.i(TAG, "failed parsing " + e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e10) {
                    }
                }
                return list;
            } catch (XmlPullParserException e11) {
                e3 = e11;
                stream = stream2;
                Slog.i(TAG, "failed parsing " + e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e12) {
                    }
                }
                return list;
            } catch (IOException e13) {
                e4 = e13;
                stream = stream2;
                Slog.i(TAG, "failed IOException " + e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e14) {
                    }
                }
                return list;
            } catch (IndexOutOfBoundsException e15) {
                e5 = e15;
                stream = stream2;
                try {
                    Slog.i(TAG, "failed parsing " + e5);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e16) {
                        }
                    }
                    return list;
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e17) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                }
                throw th;
            }
        } catch (NullPointerException e18) {
            e = e18;
            Slog.i(TAG, "failed parsing " + e);
            if (stream != null) {
            }
            return list;
        } catch (NumberFormatException e19) {
            e2 = e19;
            Slog.i(TAG, "failed parsing " + e2);
            if (stream != null) {
            }
            return list;
        } catch (XmlPullParserException e20) {
            e3 = e20;
            Slog.i(TAG, "failed parsing " + e3);
            if (stream != null) {
            }
            return list;
        } catch (IOException e21) {
            e4 = e21;
            Slog.i(TAG, "failed IOException " + e4);
            if (stream != null) {
            }
            return list;
        } catch (IndexOutOfBoundsException e22) {
            e5 = e22;
            Slog.i(TAG, "failed parsing " + e5);
            if (stream != null) {
            }
            return list;
        }
        return list;
    }

    /* JADX WARNING: Removed duplicated region for block: B:73:0x0122 A:{Catch:{ IOException -> 0x01a5 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile() {
        Exception e;
        Throwable th;
        File xmlFile = new File(OPPO_SKIP_BROADCAST_CONFIG);
        if (xmlFile.exists()) {
            FileReader fileReader = null;
            try {
                this.mBroadcastNameList = new ArrayList();
                this.mAppWhiteList = new ArrayList();
                this.mAppBlackList = new ArrayList();
                XmlPullParser parser = Xml.newPullParser();
                try {
                    FileReader xmlReader = new FileReader(xmlFile);
                    try {
                        parser.setInput(xmlReader);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    if (!parser.getName().equals(BROADCAST_NAME)) {
                                        if (!parser.getName().equals(SKIP_FEATURE)) {
                                            if (!parser.getName().equals(APP_WHITE_LIST)) {
                                                if (parser.getName().equals(APP_BLACK_LIST)) {
                                                    eventType = parser.next();
                                                    updateBroadcastNameList(parser.getAttributeValue(null, "att"), this.mAppBlackList);
                                                    break;
                                                }
                                            }
                                            eventType = parser.next();
                                            updateBroadcastNameList(parser.getAttributeValue(null, "att"), this.mAppWhiteList);
                                            break;
                                        }
                                        eventType = parser.next();
                                        updateSkipFeature(parser.getAttributeValue(null, "att"));
                                        break;
                                    }
                                    eventType = parser.next();
                                    updateBroadcastNameList(parser.getAttributeValue(null, "att"), this.mBroadcastNameList);
                                    break;
                                    break;
                            }
                        }
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
                        } catch (IOException e2) {
                            Slog.w(TAG, "Got execption close permReader.", e2);
                        }
                    } catch (Exception e3) {
                        e = e3;
                        fileReader = xmlReader;
                        try {
                            Slog.w(TAG, "Got execption parsing permissions.", e);
                            try {
                                if (this.mBroadcastNameList != null && this.mBroadcastNameList.isEmpty()) {
                                    this.mBroadcastNameList = this.mLocalBroadcastName;
                                }
                                if (this.mAppWhiteList != null && this.mAppWhiteList.isEmpty()) {
                                    this.mAppWhiteList = this.mSkipLocalList;
                                }
                                if (fileReader != null) {
                                    fileReader.close();
                                }
                            } catch (IOException e22) {
                                Slog.w(TAG, "Got execption close permReader.", e22);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            try {
                                if (this.mBroadcastNameList != null && this.mBroadcastNameList.isEmpty()) {
                                    this.mBroadcastNameList = this.mLocalBroadcastName;
                                }
                                if (this.mAppWhiteList != null && this.mAppWhiteList.isEmpty()) {
                                    this.mAppWhiteList = this.mSkipLocalList;
                                }
                                if (fileReader != null) {
                                    fileReader.close();
                                }
                            } catch (IOException e222) {
                                Slog.w(TAG, "Got execption close permReader.", e222);
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileReader = xmlReader;
                        this.mBroadcastNameList = this.mLocalBroadcastName;
                        this.mAppWhiteList = this.mSkipLocalList;
                        if (fileReader != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    Slog.w(TAG, "Couldn't find or open skip_broadcast file " + xmlFile);
                    try {
                        if (this.mBroadcastNameList != null && this.mBroadcastNameList.isEmpty()) {
                            this.mBroadcastNameList = this.mLocalBroadcastName;
                        }
                        if (this.mAppWhiteList != null && this.mAppWhiteList.isEmpty()) {
                            this.mAppWhiteList = this.mSkipLocalList;
                        }
                    } catch (IOException e2222) {
                        Slog.w(TAG, "Got execption close permReader.", e2222);
                    }
                }
            } catch (Exception e5) {
                e = e5;
            }
        } else {
            this.mBroadcastNameList = this.mLocalBroadcastName;
            this.mAppWhiteList = this.mSkipLocalList;
        }
    }

    public void updateBroadcastNameList(String tagName, List<String> list) {
        if (tagName != null && tagName != "" && list != null) {
            list.add(tagName);
        }
    }

    public void updateSkipFeature(String feature) {
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
        } else if (OppoSafeDbReader.getInstance(this.mContext).isUserOpen(pkgName) || OppoListManager.getInstance().isInAutoBootWhiteList(pkgName)) {
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
                if (this.mIsImeListInited) {
                    if (isEnableInputMethodApp(pkgName)) {
                        if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                            Slog.v(TAG, "isEnableInputMethodApp, pkgName:" + pkgName);
                        }
                        return false;
                    }
                } else if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                    Slog.v(TAG, "mIsImeListInited false, let it go");
                }
                if (OppoListManager.getInstance().isInstalledAppWidget(pkgName)) {
                    if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                        Slog.v(TAG, "isInstalledAppWidget, pkgName:" + pkgName);
                    }
                    return false;
                } else if (!isHeldWakeLock(proc.pid)) {
                    return true;
                } else {
                    if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                        Slog.v(TAG, "isHeldWakeLock, pkgName:" + pkgName);
                    }
                    return false;
                }
            }
        }
    }

    private boolean inFrontRecentTaskList(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        ParceledListSlice<RecentTaskInfo> slice = this.mAms.getRecentTasks(5, 10, UserHandle.myUserId());
        if (slice != null) {
            List<RecentTaskInfo> recentTasks = slice.getList();
            if (recentTasks == null || recentTasks.size() <= 1) {
                return false;
            }
            for (RecentTaskInfo taskInfo : recentTasks) {
                if (pkgName.equals(taskInfo.baseIntent.getComponent().getPackageName())) {
                    if (DEBUG_BROADCAST_FIREWALL_LIGHT) {
                        Slog.v(TAG, "inFrontRecentTaskList packageName:" + pkgName);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:12:0x0028, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:13:0x002b, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isResumeAndVisible(String pkgName, ProcessRecord proc) {
        synchronized (this.mAms) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                ArrayList<ActivityRecord> activities = proc.activities;
                int activityNdx = activities.size();
                if (activityNdx == 0) {
                    ComponentName topApp = this.mAms.getTopAppName();
                    if (topApp == null || pkgName.equals(topApp.getPackageName())) {
                    }
                } else {
                    while (true) {
                        activityNdx--;
                        if (activityNdx >= 0) {
                            ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                            if (r.state == ActivityState.RESUMED) {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return true;
                            } else if (r.nowVisible) {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return true;
                            } else if (activityNdx == 0) {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return false;
                            }
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:37:0x008c, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:39:0x008e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isEnableInputMethodApp(String pkgName) {
        if (pkgName == null) {
            if (DEBUG_BROADCAST_FIREWALL) {
                Slog.d(TAG, "isEnableInputMethodApp pgnName empty.");
            }
            return false;
        } else if (this.mCurrentDefaultImePkgName != null && this.mCurrentDefaultImePkgName.equals(pkgName)) {
            if (DEBUG_BROADCAST_FIREWALL) {
                Slog.d(TAG, "match CurrentDefaultImePkgName:" + pkgName);
            }
            return true;
        } else if (this.mLocalEnableImeInfoList == null || this.mLocalEnableImeInfoList.isEmpty()) {
            if (DEBUG_BROADCAST_FIREWALL) {
                Slog.d(TAG, "isEnableInputMethodApp ime list is empty.");
            }
            updateLocalEnableImeList(true, 1000);
            return false;
        } else {
            synchronized (this.mLocalImeListLocker) {
                List<InputMethodInfo> imeInfoList = this.mLocalEnableImeInfoList;
                if (imeInfoList != null) {
                    for (InputMethodInfo im : imeInfoList) {
                        if (pkgName.equals(im.getPackageName())) {
                            if (DEBUG_BROADCAST_FIREWALL) {
                                Slog.d(TAG, "found in EnableInputMethodApp ime list.");
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateLocalEnableImeList(boolean delay, int delayTimeInMills) {
        String currentInputMethodId = Secure.getString(this.mContext.getContentResolver(), "default_input_method");
        if (DEBUG_BROADCAST_FIREWALL) {
            Slog.v(TAG, "updateLocalEnableImeList, currentInputMethodId:" + currentInputMethodId);
        }
        if (currentInputMethodId != null) {
            String[] str = currentInputMethodId.split("/");
            if (str != null && str.length > 0) {
                String currentImePkgName = str[0];
                if (currentImePkgName != null) {
                    this.mCurrentDefaultImePkgName = currentImePkgName;
                }
            }
        }
        if (this.mLocalHandler != null) {
            if (delay) {
                if (delayTimeInMills <= 0) {
                    delayTimeInMills = 1000;
                }
                this.mLocalHandler.postDelayed(this.mUpdateEnableImeCallBack, (long) delayTimeInMills);
            } else {
                this.mLocalHandler.post(this.mUpdateEnableImeCallBack);
            }
        }
    }

    private void startImeSettingsObserver() {
        if (this.mImeSettingsObserver == null) {
            this.mImeSettingsObserver = new ImeSettingsObserver(this.mLocalHandler);
        }
    }

    private List<InputMethodInfo> getEnableInputMethodList() {
        InputMethodManagerService imeManager = (InputMethodManagerService) ServiceManager.getService("input_method");
        if (imeManager != null) {
            return imeManager.getEnabledInputMethodList();
        }
        return null;
    }

    private boolean isActiveAudioPlayer(int pid, int uid) {
        if (pid <= 0) {
            return false;
        }
        if (this.mOppoBroadcastBlockMonitor == null) {
            this.mOppoBroadcastBlockMonitor = new OppoActivityControlerScheduler(null);
        }
        String pids = this.mOppoBroadcastBlockMonitor.scheduleGetParameters(this.mContext, "get_pid");
        if (pids != null && pids.equals("block")) {
            return true;
        }
        if (pids == null || pids.length() <= 0) {
            return false;
        }
        String[] pidsArray = pids.split(":");
        if (pidsArray == null || pidsArray.length <= 0) {
            return false;
        }
        for (ProcessRecord app : getProcessForUid(uid)) {
            String pidStr = String.valueOf(app.pid);
            for (String playerPidStr : pidsArray) {
                if (pidStr.equals(playerPidStr)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ArrayList<ProcessRecord> getProcessForUid(int uid) {
        ArrayList<ProcessRecord> res;
        synchronized (this.mAms) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                res = new ArrayList();
                for (int i = this.mAms.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord rec = (ProcessRecord) this.mAms.mLruProcesses.get(i);
                    if (rec.thread != null && rec.uid == uid) {
                        res.add(rec);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return res;
    }

    private boolean isHeldWakeLock(int pid) {
        if (pid <= 0) {
            return false;
        }
        int[] wakeLockPids = this.mPowerManagerInternal.getWakeLockedPids();
        if (wakeLockPids == null || wakeLockPids.length <= 0) {
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
        Intent homeIntent = new Intent("android.intent.action.MAIN", null);
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
                if ("com.oppo.launcher".equals(packageName)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public void informReadyToCheckJumpQueue() {
        this.mLocalHandler.postDelayed(new Runnable() {
            public void run() {
                OppoBroadcastManager.this.mFinishBootComplete = true;
            }
        }, 10000);
    }

    public void adjustQueueIfNecessary(ArrayList<BroadcastRecord> broadcastsQueue, BroadcastRecord r) {
        if (this.mEnableJumpQueue && (this.mFinishBootComplete ^ 1) == 0) {
            int size = broadcastsQueue.size();
            if (size <= 2) {
                if (DEBUG_JUMP_QUEUE) {
                    Slog.d(TAG, "adjustQueueIfNecessary return for size <= 2, broadcast:" + r);
                }
                return;
            }
            boolean isBroadcastQueuePrior = false;
            boolean enqueueAtHead = false;
            int callingPid = r.callingPid;
            Intent actionIntent = r.intent;
            if (actionIntent != null) {
                isBroadcastQueuePrior = (actionIntent.getFlags() & DumpState.DUMP_DEXOPT) != 0;
                enqueueAtHead = isBroadcastQueuePrior;
            }
            if (!enqueueAtHead) {
                ActivityRecord topActivity = this.mAms.resumedAppLockedForBroadcast();
                ProcessRecord topApp = topActivity != null ? topActivity.app : null;
                if (topApp != null && topApp.pid == callingPid) {
                    enqueueAtHead = true;
                }
            }
            if (enqueueAtHead) {
                BroadcastRecord tmpRecord;
                if (DEBUG_JUMP_QUEUE) {
                    Slog.d(TAG, "adjustQueueIfNecessary begin sort. size:" + size + ", queueSize:" + broadcastsQueue.size());
                }
                int countOfBroadcastR = 0;
                int sumOfSeq = 0;
                ArrayList<Integer> tmpBroadcastsIndexs = new ArrayList();
                for (int i = 1; i < size; i++) {
                    tmpRecord = (BroadcastRecord) broadcastsQueue.get(i);
                    if (tmpRecord != null && tmpRecord.callingPid == callingPid) {
                        countOfBroadcastR++;
                        sumOfSeq += i;
                        tmpBroadcastsIndexs.add(new Integer(i));
                    }
                }
                if (!isBroadcastQueuePrior ? estimateAdjust(countOfBroadcastR, sumOfSeq, size) : true) {
                    for (int k = 0; k < tmpBroadcastsIndexs.size(); k++) {
                        int adjustIndex = ((Integer) tmpBroadcastsIndexs.get(k)).intValue();
                        if (adjustIndex < 0 || adjustIndex >= size) {
                            Slog.w(TAG, "adjust error happend, adjustIndex:" + adjustIndex + ", size:" + size);
                        } else {
                            tmpRecord = (BroadcastRecord) broadcastsQueue.get(adjustIndex);
                            broadcastsQueue.remove(adjustIndex);
                            broadcastsQueue.add(k + 1, tmpRecord);
                        }
                    }
                    if (DEBUG_JUMP_QUEUE_LIGHT) {
                        Slog.d(TAG, "adjustQueueIfNecessary, do jump queue. size:" + size + ", broadcast:" + r);
                    }
                    return;
                }
                if (DEBUG_JUMP_QUEUE) {
                    Slog.d(TAG, "adjustQueueIfNecessary:estimate ignore jump queue:" + r);
                }
                return;
            }
            if (DEBUG_JUMP_QUEUE) {
                Slog.d(TAG, "adjustQueueIfNecessary:not from top app. Ignore jump queue:" + r);
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
        if (countOfBroadcastR <= REASONABLE_COUNT_MAX) {
            if (((int) (((float) (countOfBroadcastR * 100)) / (((float) totalSize) * 1.0f))) <= REASONABLE_COUNT_PERCENT_MAX) {
                if (sumOfSeq / countOfBroadcastR >= ((int) (((float) totalSize) * REASONABLE_INDEX_DISTRIBUTION_REGION_BOUNDARY))) {
                    return true;
                }
                if (DEBUG_JUMP_QUEUE) {
                    Slog.d(TAG, "estimateAdjust:conunt index distribution is unreasonable!");
                }
                return false;
            }
            if (DEBUG_JUMP_QUEUE) {
                Slog.d(TAG, "estimateAdjust:conunt percent is unreasonable!");
            }
            return false;
        }
        if (DEBUG_JUMP_QUEUE) {
            Slog.d(TAG, "estimateAdjust:conunt is unreasonable!");
        }
        return false;
    }

    public void adjustParallelBroadcastReceiversQueue(BroadcastRecord broadcastRecord) {
        if (this.mEnableAdjustParallelBroadcastRecQue) {
            ActivityRecord TOP_ACT = this.mAms.resumedAppLockedForBroadcast();
            ProcessRecord TOP_APP = TOP_ACT != null ? TOP_ACT.app : null;
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
        if (!this.mEnableAdjustOrderedBroadcastRecQue) {
            return;
        }
        if (broadcastRecord == null || broadcastRecord.intent == null || !"android.provider.Telephony.SMS_RECEIVED".equals(broadcastRecord.intent.getAction())) {
            ActivityRecord TOP_ACT = this.mAms.resumedAppLockedForBroadcast();
            ProcessRecord TOP_APP = TOP_ACT != null ? TOP_ACT.app : null;
            if (DEBUG_ADJUST_OB_REC_QUE) {
                Slog.v(TAG, "nextReceiver:" + broadcastRecord.nextReceiver + ", TOP_APP:" + TOP_APP);
            }
            for (int i = broadcastRecord.nextReceiver; i < numReceivers; i++) {
                ProcessRecord curApp;
                ResolveInfo target = broadcastRecord.receivers.get(i);
                if (target instanceof BroadcastFilter) {
                    curApp = ((BroadcastFilter) target).receiverList.app;
                } else {
                    ResolveInfo info = target;
                    curApp = this.mAms.getProcessRecordLocked(info.activityInfo.processName, info.activityInfo.applicationInfo.uid, false);
                }
                if (curApp != null && curApp == TOP_APP) {
                    broadcastRecord.receivers.remove(i);
                    broadcastRecord.receivers.add(broadcastRecord.nextReceiver, target);
                    if (DEBUG_ADJUST_OB_REC_QUE) {
                        Slog.v(TAG, "adjust pb receiver queue for app:" + curApp);
                    }
                }
            }
        }
    }

    boolean isPendingBroadcastProcessLocked(int pid) {
        if (this.mOppoFgBroadcastQueue.isPendingBroadcastProcessLocked(pid)) {
            return true;
        }
        return this.mOppoBgBroadcastQueue.isPendingBroadcastProcessLocked(pid);
    }

    BroadcastQueue broadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0;
        intent.addFlags(DumpState.DUMP_FROZEN);
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND) {
            Slog.i(TAG, "oppo Broadcast intent " + intent + " on " + (isFg ? "foreground" : "background") + " queue");
        }
        if (isFg) {
            return this.mOppoFgBroadcastQueue;
        }
        return this.mOppoBgBroadcastQueue;
    }

    BroadcastQueue GetQueueFromFlag(int flags) {
        boolean isFg = (268435456 & flags) != 0;
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND) {
            Slog.i(TAG, "this is isFg " + isFg);
        }
        return isFg ? this.mOppoFgBroadcastQueue : this.mOppoBgBroadcastQueue;
    }

    protected List adjustReceiverList(List receivers, Intent intent) {
        if (intent == null || receivers == null || !"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            return receivers;
        }
        List<Object> systemList = new ArrayList();
        List<Object> thirdList = new ArrayList();
        List res = new ArrayList();
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
        for (Object o : systemList) {
            res.add(o);
        }
        for (Object o2 : thirdList) {
            res.add(o2);
        }
        return res;
    }

    public void handleDynamicLog(boolean on) {
        DEBUG_BROADCAST_FIREWALL = on;
        DEBUG_JUMP_QUEUE = on;
        DEBUG_JUMP_QUEUE_LIGHT = on;
        DEBUG_ADJUST_PB_REC_QUE = on;
        DEBUG_ADJUST_OB_REC_QUE = on;
        DEBUG_BROADCAST_FIREWALL_LIGHT = on;
    }

    final boolean dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage, boolean needSep) {
        pw.println(" OppoBroadcastManager: ");
        pw.println(" mCurrentDefaultImePkgName:" + this.mCurrentDefaultImePkgName);
        List<InputMethodInfo> imeInfoList = this.mLocalEnableImeInfoList;
        if (imeInfoList == null) {
            pw.println(" mLocalEnableImeInfoList null.");
        } else if (imeInfoList.size() <= 0) {
            pw.println(" mLocalEnableImeInfoList empty.");
        } else {
            int index = 0;
            for (InputMethodInfo im : imeInfoList) {
                pw.println(" LocalEnableIme[" + index + "]:" + im.getPackageName());
                index++;
            }
        }
        return true;
    }
}
