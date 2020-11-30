package com.android.server.am;

import android.app.BroadcastOptions;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.ColorDeviceIdleHelper;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.wm.ColorAppSwitchManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ColorBootCompleteBroadcastManager {
    public static final String KEY_BOOT_BROADCAST_OPTIMIZE_ENABLE = "switch";
    public static final String KEY_DISPATCH_TIME_PERIOD = "dispatchTimePeriod";
    public static final String KEY_NO_DELAY_APP = "noDelayApp";
    public static final String KEY_QUEUE_MAX_NUM = "queueMaxNum";
    public static final String KEY_SHORT_DELAY_APP = "shortDelayApp";
    public static final String KEY_SHORT_DELAY_APP_DISPATCH_TIME = "shortDelayAppDispatchTime";
    public static final String KEY_SYS_APP_DELAY_ENABLE = "sysAppDelay";
    public static final String KEY_SYS_APP_DISPATCH_TIME = "sysAppDispatchTime";
    public static final String KEY_SYS_LONG_DELAY_APP = "sysLongDelayApp";
    public static final String KEY_SYS_SHORT_DELAY_APP = "sysShortDelayApp";
    public static final String KEY_THIRD_APP_DELAY_ENABLE = "thirdAppDelay";
    public static final String KEY_THIRD_APP_DISPATCH_TIME = "thirdAppDispatchTime";
    private static ColorBootCompleteBroadcastManager mInstance = null;
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic.bootcompleted.optimal.log", false);
    private final String TAG = "ColorBootCompleteBroadcastManager";
    private boolean isEnable = true;
    private boolean isSysAppDelayEnable = true;
    private boolean isThirdAppDelayEnable = false;
    private ActivityManagerService mAms;
    private BrdRecord mBrdRecord = null;
    private int mCheckCount = 0;
    private final Runnable mCheckRunnable = new Runnable() {
        /* class com.android.server.am.ColorBootCompleteBroadcastManager.AnonymousClass1 */

        public void run() {
            if (ColorBootCompleteBroadcastManager.this.mBrdRecord == null) {
                Slog.e("ColorBootCompleteBroadcastManager", "mCheckRunnable brdRecord is null");
                return;
            }
            ColorBootCompleteBroadcastManager.access$108(ColorBootCompleteBroadcastManager.this);
            if (ColorBootCompleteBroadcastManager.this.mCheckCount == 1) {
                if (!ColorBootCompleteBroadcastManager.this.mBrdRecord.queueShortDelayApp.isEmpty()) {
                    ColorBootCompleteBroadcastManager colorBootCompleteBroadcastManager = ColorBootCompleteBroadcastManager.this;
                    colorBootCompleteBroadcastManager.enqueueOrderedBroadcast(colorBootCompleteBroadcastManager.mBrdRecord.queueShortDelayApp);
                }
                ColorBootCompleteBroadcastManager.this.mBrdRecord.nextDeliver = ColorBootCompleteBroadcastManager.this.mSysAppDispatchTime - ColorBootCompleteBroadcastManager.this.mShortDelayAppDispatchTime;
            } else if (ColorBootCompleteBroadcastManager.this.mCheckCount == 2) {
                if (ColorBootCompleteBroadcastManager.this.mBrdRecord.queueSysApp.size() <= ColorBootCompleteBroadcastManager.this.mQueueMaxNum) {
                    if (ColorBootCompleteBroadcastManager.this.mBrdRecord.queueSysApp.size() != 0) {
                        ColorBootCompleteBroadcastManager colorBootCompleteBroadcastManager2 = ColorBootCompleteBroadcastManager.this;
                        colorBootCompleteBroadcastManager2.enqueueOrderedBroadcast(colorBootCompleteBroadcastManager2.mBrdRecord.queueSysApp);
                    }
                    ColorBootCompleteBroadcastManager.this.mBrdRecord.nextDeliver = (ColorBootCompleteBroadcastManager.this.mThirdAppDispatchTime - ColorBootCompleteBroadcastManager.this.mSysAppDispatchTime) - (((long) ColorBootCompleteBroadcastManager.this.mSysAppLoopCount) * ColorBootCompleteBroadcastManager.this.mDispatchTimePeriod);
                    if (ColorBootCompleteBroadcastManager.this.mBrdRecord.nextDeliver <= 0) {
                        ColorBootCompleteBroadcastManager.this.mBrdRecord.nextDeliver = ColorBootCompleteBroadcastManager.this.mDispatchTimePeriod;
                    }
                } else {
                    List tmpList = new ArrayList();
                    for (int i = 0; i < ColorBootCompleteBroadcastManager.this.mQueueMaxNum; i++) {
                        tmpList.add(ColorBootCompleteBroadcastManager.this.mBrdRecord.queueSysApp.get(i));
                    }
                    ColorBootCompleteBroadcastManager.this.enqueueOrderedBroadcast(tmpList);
                    ColorBootCompleteBroadcastManager.this.mBrdRecord.queueSysApp.removeAll(tmpList);
                    ColorBootCompleteBroadcastManager.access$110(ColorBootCompleteBroadcastManager.this);
                    ColorBootCompleteBroadcastManager.access$708(ColorBootCompleteBroadcastManager.this);
                    ColorBootCompleteBroadcastManager.this.mAms.mHandler.postDelayed(ColorBootCompleteBroadcastManager.this.mCheckRunnable, ColorBootCompleteBroadcastManager.this.mDispatchTimePeriod);
                    return;
                }
            } else if (ColorBootCompleteBroadcastManager.this.mCheckCount != 3) {
                if (ColorBootCompleteBroadcastManager.this.DEBUG) {
                    Slog.i("ColorBootCompleteBroadcastManager", "clear mBrdRecord");
                }
                if (ColorBootCompleteBroadcastManager.this.mBrdRecord != null) {
                    ColorBootCompleteBroadcastManager.this.mBrdRecord = null;
                    if (!ColorBootCompleteBroadcastManager.this.mNoDelayList.isEmpty()) {
                        ColorBootCompleteBroadcastManager.this.mNoDelayList.clear();
                    }
                    if (!ColorBootCompleteBroadcastManager.this.mShortDelayList.isEmpty()) {
                        ColorBootCompleteBroadcastManager.this.mNoDelayList.clear();
                    }
                    if (!ColorBootCompleteBroadcastManager.this.mSysShortDelayList.isEmpty()) {
                        ColorBootCompleteBroadcastManager.this.mSysShortDelayList.clear();
                    }
                    if (!ColorBootCompleteBroadcastManager.this.mSysLongDelayList.isEmpty()) {
                        ColorBootCompleteBroadcastManager.this.mSysLongDelayList.clear();
                        return;
                    }
                    return;
                }
                return;
            } else if (ColorBootCompleteBroadcastManager.this.mBrdRecord.queueThirdApp.isEmpty()) {
                ColorBootCompleteBroadcastManager.this.mAms.mHandler.postDelayed(ColorBootCompleteBroadcastManager.this.mCheckRunnable, ColorBootCompleteBroadcastManager.this.mClearBrdRecordTime);
                return;
            } else if (ColorBootCompleteBroadcastManager.this.mBrdRecord.queueThirdApp.size() <= ColorBootCompleteBroadcastManager.this.mQueueMaxNum) {
                ColorBootCompleteBroadcastManager colorBootCompleteBroadcastManager3 = ColorBootCompleteBroadcastManager.this;
                colorBootCompleteBroadcastManager3.enqueueOrderedBroadcast(colorBootCompleteBroadcastManager3.mBrdRecord.queueThirdApp);
                ColorBootCompleteBroadcastManager.this.mAms.mHandler.postDelayed(ColorBootCompleteBroadcastManager.this.mCheckRunnable, ColorBootCompleteBroadcastManager.this.mClearBrdRecordTime);
                return;
            } else {
                List tmpList2 = new ArrayList();
                for (int i2 = 0; i2 < ColorBootCompleteBroadcastManager.this.mQueueMaxNum; i2++) {
                    tmpList2.add(ColorBootCompleteBroadcastManager.this.mBrdRecord.queueThirdApp.get(i2));
                }
                ColorBootCompleteBroadcastManager.this.enqueueOrderedBroadcast(tmpList2);
                ColorBootCompleteBroadcastManager.this.mBrdRecord.queueThirdApp.removeAll(tmpList2);
                ColorBootCompleteBroadcastManager.access$110(ColorBootCompleteBroadcastManager.this);
                ColorBootCompleteBroadcastManager.this.mAms.mHandler.postDelayed(ColorBootCompleteBroadcastManager.this.mCheckRunnable, ColorBootCompleteBroadcastManager.this.mDispatchTimePeriod);
                return;
            }
            if (ColorBootCompleteBroadcastManager.this.DEBUG) {
                Slog.i("ColorBootCompleteBroadcastManager", "mCheckRunnable nextDeliver " + ColorBootCompleteBroadcastManager.this.mBrdRecord.nextDeliver + " checkCount " + ColorBootCompleteBroadcastManager.this.mCheckCount);
            }
            ColorBootCompleteBroadcastManager.this.mAms.mHandler.postDelayed(ColorBootCompleteBroadcastManager.this.mCheckRunnable, ColorBootCompleteBroadcastManager.this.mBrdRecord.nextDeliver);
        }
    };
    private long mClearBrdRecordTime = 1200000;
    private List<String> mDefaultNoDelayList = Arrays.asList("com.tsinghua.autostartactivity", "com.redteamobile.roaming", "com.android.vending", "com.facebook.katana", "com.imo.android.imoim");
    private List<String> mDefaultShortDelayList = Arrays.asList("com.android.mms", "com.coloros.regservice", "com.oppo.ctautoregist", "com.oppo.tzupdate", "com.nearme.gamecenter", "com.coloros.note", "com.coloros.providers.fileinfo", "com.ted.number");
    private List<String> mDefaultSysLongDelayList = Arrays.asList("");
    private List<String> mDefaultSysShortDelayList = Arrays.asList("com.heytap.cloud", "com.heytap.browser", "com.nearme.instant.platform", "com.heytap.themestore", "com.heytap.market", "com.heytap.usercenter", "com.coloros.backuprestore");
    private long mDispatchTimePeriod = ColorAppSwitchManager.INTERVAL;
    private ArrayList<String> mGlobalWhiteList = null;
    private List<String> mGmsList = Arrays.asList("com.google.android.gms", "com.google.android.gsf", "com.google.android.backuptransport", "com.google.android.partnersetup", "com.google.android.printservice.recommendation", "com.google.android.ext.services", "com.google.android.onetimeinitializer", "com.google.android.ext.shared", "com.google.android.configupdater", "com.google.android.marvin.talkback", "com.google.android.syncadapters.contacts", "com.google.android.webview", "com.eg.android.AlipayGphone", "com.xunmeng.pinduoduo");
    private ArrayList<String> mNoDelayList = new ArrayList<>();
    private int mQueueMaxNum = 20;
    private long mShortDelayAppDispatchTime = ColorAppSwitchManager.INTERVAL;
    private ArrayList<String> mShortDelayList = new ArrayList<>();
    private long mSysAppDispatchTime = 60000;
    private int mSysAppLoopCount = 0;
    private ArrayList<String> mSysLongDelayList = new ArrayList<>();
    private ArrayList<String> mSysShortDelayList = new ArrayList<>();
    private long mThirdAppDispatchTime = ColorDeviceIdleHelper.ALARM_WINDOW_LENGTH;

    static /* synthetic */ int access$108(ColorBootCompleteBroadcastManager x0) {
        int i = x0.mCheckCount;
        x0.mCheckCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$110(ColorBootCompleteBroadcastManager x0) {
        int i = x0.mCheckCount;
        x0.mCheckCount = i - 1;
        return i;
    }

    static /* synthetic */ int access$708(ColorBootCompleteBroadcastManager x0) {
        int i = x0.mSysAppLoopCount;
        x0.mSysAppLoopCount = i + 1;
        return i;
    }

    private ColorBootCompleteBroadcastManager() {
    }

    public static final ColorBootCompleteBroadcastManager getInstance() {
        if (mInstance == null) {
            synchronized (ColorBootCompleteBroadcastManager.class) {
                if (mInstance == null) {
                    mInstance = new ColorBootCompleteBroadcastManager();
                }
            }
        }
        return mInstance;
    }

    public void init(ActivityManagerService ams) {
        this.mAms = ams;
        initData();
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null && activityManagerService.mContext != null) {
            this.mGlobalWhiteList = OppoListManager.getInstance().getGlobalWhiteList(this.mAms.mContext, 2);
        }
    }

    private void initData() {
        Bundle bundle = ColorAppStartupManagerUtils.getInstance().getBootBroadcastOptimizeConfig();
        if (bundle != null) {
            try {
                this.isEnable = bundle.getBoolean("switch");
                this.isSysAppDelayEnable = bundle.getBoolean("sysAppDelay");
                this.isThirdAppDelayEnable = bundle.getBoolean("thirdAppDelay");
                this.mQueueMaxNum = bundle.getInt("queueMaxNum");
                this.mShortDelayAppDispatchTime = bundle.getLong("shortDelayAppDispatchTime");
                this.mSysAppDispatchTime = bundle.getLong("sysAppDispatchTime");
                this.mThirdAppDispatchTime = bundle.getLong("thirdAppDispatchTime");
                this.mDispatchTimePeriod = bundle.getLong("dispatchTimePeriod");
                this.mNoDelayList.addAll(bundle.getStringArrayList("noDelayApp"));
                this.mShortDelayList.addAll(bundle.getStringArrayList("shortDelayApp"));
                this.mSysShortDelayList.addAll(bundle.getStringArrayList("sysShortDelayApp"));
                this.mSysLongDelayList.addAll(bundle.getStringArrayList("sysLongDelayApp"));
            } catch (Exception e) {
                Slog.e("ColorBootCompleteBroadcastManager", "ColorBootCompleteBroadcastManager initData error!!!");
            }
        }
        if (this.mNoDelayList.isEmpty()) {
            this.mNoDelayList.addAll(this.mDefaultNoDelayList);
        }
        if (this.mShortDelayList.isEmpty()) {
            this.mShortDelayList.addAll(this.mDefaultShortDelayList);
        }
        if (this.mSysShortDelayList.isEmpty()) {
            this.mSysShortDelayList.addAll(this.mDefaultSysShortDelayList);
        }
        if (this.mSysLongDelayList.isEmpty()) {
            this.mSysLongDelayList.addAll(this.mDefaultSysLongDelayList);
        }
        if (!isExpVersion()) {
            this.mSysLongDelayList.addAll(this.mGmsList);
        }
        if (this.DEBUG) {
            Slog.i("ColorBootCompleteBroadcastManager", "initData: isEnable = " + this.isEnable + ", isSysAppDelayEnable = " + this.isSysAppDelayEnable + ", isThirdAppDelayEnable = " + this.isThirdAppDelayEnable + ", mQueueMaxNum = " + this.mQueueMaxNum + ", mShortDelayAppDispatchTime = " + this.mShortDelayAppDispatchTime + ", mSysAppDispatchTime = " + this.mSysAppDispatchTime + ", mThirdAppDispatchTime = " + this.mThirdAppDispatchTime + ", mDispatchTimePeriod = " + this.mDispatchTimePeriod + ", mNoDelayList = " + this.mNoDelayList + ", mShortDelayList = " + this.mShortDelayList + ", mSysShortDelayList = " + this.mSysShortDelayList + ", mSysLongDelayList = " + this.mSysLongDelayList);
        }
    }

    public List adjustQueueOrderedBroadcastLocked(BroadcastQueue _queue, Intent _intent, ProcessRecord _callerApp, String _callerPackage, int _callingPid, int _callingUid, boolean _callerInstantApp, String _resolvedType, String[] _requiredPermissions, int _appOp, BroadcastOptions _options, List _receivers, IIntentReceiver _resultTo, int _resultCode, String _resultData, Bundle _resultExtras, boolean _serialized, boolean _sticky, boolean _initialSticky, int _userId, boolean _allowBackgroundActivityStarts, boolean _timeoutExempt) {
        BroadcastFilter bf;
        if (!this.isEnable) {
            return _receivers;
        }
        if (_intent == null || !BrightnessConstants.ACTION_BOOT_COMPLETED.equals(_intent.getAction()) || _receivers == null) {
            return _receivers;
        }
        if (_userId != 0) {
            return _receivers;
        }
        this.mBrdRecord = new BrdRecord(_queue, _intent, _callerApp, _callerPackage, _callingPid, _callingUid, _callerInstantApp, _resolvedType, _requiredPermissions, _appOp, _options, _receivers, _resultTo, _resultCode, _resultData, _resultExtras, _serialized, _sticky, false, _userId, _allowBackgroundActivityStarts, _timeoutExempt);
        this.mBrdRecord.nextDeliver = this.mShortDelayAppDispatchTime;
        List receivers = new ArrayList();
        for (Object temp : _receivers) {
            if (temp instanceof ResolveInfo) {
                ResolveInfo ri = (ResolveInfo) temp;
                if (!(ri == null || ri.activityInfo == null || ri.activityInfo.applicationInfo == null)) {
                    String receiverPkg = ri.activityInfo.packageName;
                    if (!TextUtils.isEmpty(receiverPkg) && !this.mNoDelayList.contains(receiverPkg) && !isCtaPackage(receiverPkg) && !isGlobalWhitePkg(receiverPkg)) {
                        if (this.mShortDelayList.contains(receiverPkg)) {
                            this.mBrdRecord.queueShortDelayApp.add(ri);
                        } else if ((ri.activityInfo.applicationInfo.flags & 1) != 0) {
                            if (this.isSysAppDelayEnable) {
                                if (this.mSysShortDelayList.contains(receiverPkg)) {
                                    this.mBrdRecord.queueSysApp.add(ri);
                                } else if (this.mSysLongDelayList.contains(receiverPkg)) {
                                    this.mBrdRecord.queueThirdApp.add(ri);
                                }
                            }
                        } else if (this.isThirdAppDelayEnable) {
                            this.mBrdRecord.queueThirdApp.add(ri);
                        }
                    }
                }
                receivers.add(ri);
            } else if (!(!(temp instanceof BroadcastFilter) || (bf = (BroadcastFilter) temp) == null || bf.receiverList == null || bf.receiverList.app == null || bf.receiverList.app.info == null)) {
                String receiverPkg2 = bf.receiverList.app.info.packageName;
                if (!TextUtils.isEmpty(receiverPkg2) && !this.mNoDelayList.contains(receiverPkg2) && !isCtaPackage(receiverPkg2) && !isGlobalWhitePkg(receiverPkg2)) {
                    if (this.mShortDelayList.contains(receiverPkg2)) {
                        this.mBrdRecord.queueShortDelayApp.add(bf);
                    } else if ((bf.receiverList.app.info.flags & 1) != 0) {
                        if (this.isSysAppDelayEnable) {
                            if (this.mSysShortDelayList.contains(receiverPkg2)) {
                                this.mBrdRecord.queueSysApp.add(bf);
                            } else if (this.mSysLongDelayList.contains(receiverPkg2)) {
                                this.mBrdRecord.queueThirdApp.add(bf);
                            }
                        }
                    } else if (this.isThirdAppDelayEnable) {
                        this.mBrdRecord.queueThirdApp.add(bf);
                    }
                }
                receivers.add(bf);
            }
        }
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService == null) {
            return _receivers;
        }
        if (activityManagerService.mHandler == null) {
            return _receivers;
        }
        if (this.DEBUG) {
            Slog.i("ColorBootCompleteBroadcastManager", "receivers size=" + receivers.size() + " { " + receivers + " }");
            this.mBrdRecord.dumpQueue();
            Slog.i("ColorBootCompleteBroadcastManager", "_receivers size=" + _receivers.size() + " " + _receivers);
        }
        return receivers;
    }

    public void scheduleNextDispatch(Intent intent) {
        ActivityManagerService activityManagerService;
        if (this.isEnable && (activityManagerService = this.mAms) != null && activityManagerService.mHandler != null && intent != null && this.mBrdRecord != null && "oppo.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            this.mAms.mHandler.postDelayed(this.mCheckRunnable, this.mBrdRecord.nextDeliver);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enqueueOrderedBroadcast(List receivers) {
        Throwable th;
        synchronized (this.mAms) {
            try {
                if (this.DEBUG) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append("enqueueOrderedBroadcast ");
                        sb.append(receivers.size());
                        sb.append(" ");
                        sb.append(receivers);
                        Slog.i("ColorBootCompleteBroadcastManager", sb.toString());
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                this.mBrdRecord.queue.enqueueOrderedBroadcastLocked(new BroadcastRecord(this.mBrdRecord.queue, this.mBrdRecord.intent, this.mBrdRecord.callerApp, this.mBrdRecord.callerPackage, this.mBrdRecord.callingPid, this.mBrdRecord.callingUid, this.mBrdRecord.callerInstantApp, this.mBrdRecord.resolvedType, this.mBrdRecord.requiredPermissions, this.mBrdRecord.appOp, this.mBrdRecord.options, receivers, this.mBrdRecord.resultTo, this.mBrdRecord.resultCode, this.mBrdRecord.resultData, this.mBrdRecord.resultExtras, this.mBrdRecord.ordered, this.mBrdRecord.sticky, this.mBrdRecord.initialSticky, this.mBrdRecord.userId, this.mBrdRecord.allowBackgroundActivityStarts, this.mBrdRecord.timeoutExempt));
                this.mBrdRecord.queue.scheduleBroadcastsLocked();
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class BrdRecord {
        boolean allowBackgroundActivityStarts;
        int appOp;
        ProcessRecord callerApp;
        boolean callerInstantApp;
        String callerPackage;
        int callingPid;
        int callingUid;
        boolean initialSticky;
        Intent intent;
        long nextDeliver;
        BroadcastOptions options;
        boolean ordered;
        BroadcastQueue queue;
        List queueShortDelayApp = new ArrayList();
        List queueSysApp = new ArrayList();
        List queueThirdApp = new ArrayList();
        List receivers;
        String[] requiredPermissions;
        String resolvedType;
        int resultCode;
        String resultData;
        Bundle resultExtras;
        IIntentReceiver resultTo;
        boolean sticky;
        boolean timeoutExempt;
        int userId;

        BrdRecord(BroadcastQueue _queue, Intent _intent, ProcessRecord _callerApp, String _callerPackage, int _callingPid, int _callingUid, boolean _callerInstantApp, String _resolvedType, String[] _requiredPermissions, int _appOp, BroadcastOptions _options, List _receivers, IIntentReceiver _resultTo, int _resultCode, String _resultData, Bundle _resultExtras, boolean _serialized, boolean _sticky, boolean _initialSticky, int _userId, boolean _allowBackgroundActivityStarts, boolean _timeoutExempt) {
            this.queue = _queue;
            this.intent = _intent;
            this.callerApp = _callerApp;
            this.callerPackage = _callerPackage;
            this.callingPid = _callingPid;
            this.callingUid = _callingUid;
            this.callerInstantApp = _callerInstantApp;
            this.resolvedType = _resolvedType;
            this.requiredPermissions = _requiredPermissions;
            this.appOp = _appOp;
            this.options = _options;
            this.receivers = _receivers;
            this.resultTo = _resultTo;
            this.resultCode = _resultCode;
            this.resultData = _resultData;
            this.resultExtras = _resultExtras;
            this.ordered = _serialized;
            this.sticky = _sticky;
            this.initialSticky = _initialSticky;
            this.userId = _userId;
            this.allowBackgroundActivityStarts = _allowBackgroundActivityStarts;
            this.timeoutExempt = _timeoutExempt;
        }

        /* access modifiers changed from: package-private */
        public void dumpQueue() {
            Slog.i("ColorBootCompleteBroadcastManager", "1st=" + this.queueShortDelayApp.size() + " " + this.queueShortDelayApp);
            Slog.i("ColorBootCompleteBroadcastManager", "2nd=" + this.queueSysApp.size() + " " + this.queueSysApp);
            Slog.i("ColorBootCompleteBroadcastManager", "3rd=" + this.queueThirdApp.size() + " " + this.queueThirdApp);
        }
    }

    private boolean isExpVersion() {
        boolean result = false;
        try {
            result = this.mAms.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
            Slog.i("ColorBootCompleteBroadcastManager", "isExpVersion = " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }

    private boolean isCtaPackage(String pkgName) {
        if (TextUtils.isEmpty(pkgName) || !pkgName.contains("com.cttl.")) {
            return false;
        }
        return true;
    }

    private boolean isGlobalWhitePkg(String pkgName) {
        ArrayList<String> arrayList = this.mGlobalWhiteList;
        if (arrayList == null || arrayList.isEmpty() || TextUtils.isEmpty(pkgName)) {
            return false;
        }
        return this.mGlobalWhiteList.contains(pkgName);
    }
}
