package com.android.server;

import android.app.ActivityManagerNative;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.ColorAccessController;
import com.android.server.wm.ColorFreeformManagerService;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmlpull.v1.XmlPullParser;

public class ColorAlarmManagerHelper implements IColorAlarmManagerHelper {
    static final int DEFAULT_RESTORE_COUNT = 3;
    private static ColorAlarmManagerHelper mInstance = null;
    private static ArrayList<String> sDeepSleepRulesList = new ArrayList<>();
    private final long ACME_ALIGN_INTERVAL = 15;
    private final List<String> ACME_BLACK_WORD = Arrays.asList("com.heytap");
    private final long ACME_SCREENOFF_TIME = 30;
    private final String ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private final long ALIGN_INTERVAL = 5;
    private final List<String> ALIGN_WHITE_LIST = Arrays.asList("com.google.android.gms", "com.taobao.qianniu", "com.google.android.gsf", "com.kuaidi.daijia.driver", "com.sdu.didi.gsui", "com.sdu.didi.gui", "com.duoduo.vip.taxi", "com.ubercab.driver", "net.zdsoft.taxiclient2", "cn.edaijia.android.driverclient", "com.yongche", "com.funcity.taxi.driver", "com.hn.client.driver", "com.joyskim.taxis_driver", "com.anyimob.taxi", "com.eastedge.taxidriverforpad", "com.oppo.im", "com.sankuai.meituan.dispatch.homebrew", "com.baidu.lbs.waimai.baidurider");
    private final String COLUMN_NAME_1 = "version";
    private final String COLUMN_NAME_2 = "xml";
    private final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private final String DEEP_SLEEP_WHITELIST_TAG_IN_MOBILE = "DEEP_SLEEP_WHITE_IN_MOBILE";
    private final String DEEP_SLEEP_WHITELIST_TAG_IN_WIFI = "DEEP_SLEEP_WHITE_IN_WIFI";
    private final String FILTER_NAME = "sys_alarm_filterpackages_list";
    private final String INEXACT_ALARM_FEATURE_NAME = "oppo.inexact.alarm";
    private final int INTEGER_NUMBER_THREE = 3;
    private final int INTEGER_NUMBER_TWO = 2;
    private final List<String> KEYWORD_WHITE_LIST = Arrays.asList("clock", "alarm", "calendar");
    private final int MSG_PROCESS_DIED = 201;
    private final String OPPO_CUSTOMIZE_WHITE_FILE_PATH = SystemProperties.get("sys.custom.whitelist", "/system/etc/oppo_customize_whitelist.xml");
    private final List<String> PENDING_JOB_BLACK = Arrays.asList("com.sina.weibo", "com.tencent.qqlive", "com.qiyi.video", "com.youku.phone", "com.smile.gifmaker", "com.ss.android.ugc.aweme", "com.tencent.tmgp.sgame", "com.tencent.tmgp.pubgmhd", "com.taobao.taobao", "com.baidu.searchbox", "com.UCMobile", "com.tencent.mtt", "com.ss.android.article.news", "com.kugou.android", "com.autonavi.minimap", "com.kmxs.reader", "com.xunmeng.pinduoduo", "com.jifen.qukan", "com.ss.android.article.video", "com.ss.android.article.lite");
    private final List<String> PKG_WHITE_LIST = Arrays.asList("com.android.mms", "com.coloros.soundrecorder", "com.android.providers.calendar", "com.oppo.music", "com.coloros.backuprestore", "com.coloros.weather", "com.mobiletools.systemhelper", "com.oppo.community", "com.nearme.themespace", "com.nearme.themestore", "com.heytap.themestore", ColorAccessController.PROTECT_FILTER_USERCENTER_EXTRA_VALUE, "com.nearme.romupdate");
    private final List<String> PKG_WHITE_LIST_NOT_COVERED = Arrays.asList("com.oppo.ctautoregist", "com.coloros.sauhelper", ColorFreeformManagerService.FREEFORM_CALLER_PKG);
    private final List<String> REMOVE_FILTER_LIST_NOT_COVERED = Arrays.asList("com.coloros.weather.service", "com.android.providers.media", "com.coloros.childrenspace", "com.coloros.sauhelper", "com.nearme.instant.platform", "com.heytap.instant.platform", "com.coloros.athena", "com.coloros.oppoguardelf", "com.coloros.safecenter", "com.coloros.apprecover");
    private final List<String> REMOVE_FILTER_PKG_LIST = Arrays.asList("com.coloros.alarmclock", "com.android.calendar", "com.android.mms", "com.android.providers.calendar", "com.oppo.market", "com.heytap.market", "com.nearme.note", "com.coloros.note", "com.oppo.ota", "com.nearme.statistics.rom", "android", "com.coloros.feedback", "com.nearme.gamecenter", "com.heytap.gamecenter", "com.coloros.scratch", "com.coloros.soundrecorder", "com.oppo.reader", "com.nearme.themespace", "com.heytap.themestore", "com.android.browser", "com.coloros.browser", "com.heytap.browser", "com.coloros.oppomorningsystem", "com.coloros.backuprestore", "com.coloros.filemanager", "com.android.engineeringmode", "com.oppo.engineermode", "com.oppo.usercenter", "com.zdworks.android.zdclock", "com.coloros.leather", "com.coloros.gallery3d", "com.nearme.romupdate", "com.android.providers.calendar", "com.coloros.sau", "com.coloros.safe.service.framework", "com.android.contacts", "com.coloros.cloud", "com.heytap.cloud", "com.oppo.community", "com.coloros.video", "com.ted.number");
    private final int SLEEP_TIME = 100;
    private final String TAG = "ColorAlarmManagerHelper";
    private final List<String> UID_WHITE_LIST = Arrays.asList("1000", "1001");
    private ArrayMap<String, Integer> mAcmeBlackConfig = new ArrayMap<>();
    private ArrayList<String> mAcmeBlackWord = new ArrayList<>();
    private long sAcmeAlignInterval = 15;
    private boolean sAcmeAlignIntervalFromLocal = false;
    private boolean sAcmeAlignIntervalFromProvidor = false;
    private boolean sAcmeBlackWordFromLocal = false;
    private boolean sAcmeBlackWordFromProvidor = false;
    private long sAcmeScreenOffTime = 30;
    private boolean sAcmeScreenOffTimeFromLocal = false;
    private boolean sAcmeScreenOffTimeFromProvidor = false;
    private AlarmManagerService sAlarm;
    private ArrayList<String> sAlignEnforcedWhiteList = new ArrayList<>();
    private long sAlignFirstDelay = 0;
    private boolean sAlignFirstDelayFromLocal = false;
    private boolean sAlignFirstDelayFromProvidor = false;
    private long sAlignInterval = 5;
    private boolean sAlignIntervalFromLocal = false;
    private boolean sAlignIntervalFromProvidor = false;
    private ArrayList<String> sAlignWhiteList = new ArrayList<>();
    private Context sContext;
    private ArrayList<String> sCustomPkgWhiteList = new ArrayList<>();
    private boolean sDebug = false;
    private ArrayList<String> sDeepSleepPkgList = new ArrayList<>();
    private boolean sHaveAlarmFeature = false;
    private AtomicBoolean sIsWhiteListInited = new AtomicBoolean();
    private ArrayList<String> sKeyList = new ArrayList<>();
    private final Object sLock = new Object();
    private boolean sOppoDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private ArrayList<String> sPendingJobBlack = new ArrayList<>();
    private int sPendingJobCount = 3;
    private boolean sPendingJobFromLocal = false;
    private boolean sPendingJobFromProvidor = false;
    private ArrayList<String> sPkgWhiteList = new ArrayList<>();
    private ArrayList<String> sRemoveFilterPackagesList = new ArrayList<>();
    private ArrayList<String> sUidWhiteList = new ArrayList<>();
    private WorkerHandler sWorkerHandler;

    public static ColorAlarmManagerHelper getInstance() {
        if (mInstance == null) {
            synchronized (ColorAlarmManagerHelper.class) {
                if (mInstance == null) {
                    mInstance = new ColorAlarmManagerHelper();
                }
            }
        }
        return mInstance;
    }

    private ColorAlarmManagerHelper() {
    }

    public void init(Context context, AlarmManagerService alarm) {
        synchronized (this.sLock) {
            this.sContext = context;
            this.sAlarm = alarm;
            this.sHaveAlarmFeature = this.sContext.getPackageManager().hasSystemFeature("oppo.inexact.alarm");
            new Thread(new GetDataFromProviderRunnable(), "get_data_from_provider").start();
            initRomUpdateBroadcast(context);
        }
        initSmartDozeAlarmExemptionBroadcast(context);
    }

    public void init(Context context, AlarmManagerService alarm, Looper looper) {
        synchronized (this.sLock) {
            this.sContext = context;
            this.sAlarm = alarm;
            this.sHaveAlarmFeature = this.sContext.getPackageManager().hasSystemFeature("oppo.inexact.alarm");
            new Thread(new GetDataFromProviderRunnable(), "get_data_from_provider").start();
            initRomUpdateBroadcast(context);
            this.sWorkerHandler = new WorkerHandler(looper);
        }
        initSmartDozeAlarmExemptionBroadcast(context);
    }

    public long setInexactAlarm(long windowLength) {
        synchronized (this.sLock) {
            if (!this.sHaveAlarmFeature || !this.sIsWhiteListInited.get() || windowLength != 0) {
                return windowLength;
            }
            if (this.sDebug) {
                Log.d("ColorAlarmManagerHelper", "windowLength == AlarmManager.WINDOW_EXACT");
            }
            if (isNeedInexactAlarm()) {
                if (this.sDebug) {
                    Log.d("ColorAlarmManagerHelper", "Using  inexact alarm!!!!!!!!");
                }
                return -1;
            }
            if (this.sDebug) {
                Log.d("ColorAlarmManagerHelper", "Using exact alarm!!!!!!!!");
            }
            return 0;
        }
    }

    private void initSmartDozeAlarmExemptionBroadcast(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ColorSmartDozeHelper.ACTION_OPPO_SMARTDOZE_ALARM_EXEMPTION_END);
        context.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.ColorAlarmManagerHelper.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (ColorSmartDozeHelper.ACTION_OPPO_SMARTDOZE_ALARM_EXEMPTION_END.equals(intent.getAction())) {
                    OppoFeatureCache.get(IColorSmartDozeHelper.DEFAULT).exitAlarmExemption();
                    ColorAlarmManagerHelper.this.sAlarm.rebatchAllAlarms();
                }
            }
        }, filter, null, this.sWorkerHandler);
    }

    private void initRomUpdateBroadcast(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        context.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.ColorAlarmManagerHelper.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                ArrayList<String> changeList;
                if ("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS".equals(intent.getAction()) && (changeList = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST")) != null && changeList.contains("sys_alarm_filterpackages_list")) {
                    new Thread(new GetDataFromProviderRunnable(), "AlarmRomUpdate").start();
                    Log.d("ColorAlarmManagerHelper", "ACTION_ROM_UPDATE_CONFIG_SUCCES");
                }
            }
        }, filter, "oppo.permission.OPPO_COMPONENT_SAFE", this.sWorkerHandler);
    }

    private class GetDataFromProviderRunnable implements Runnable {
        public GetDataFromProviderRunnable() {
        }

        public void run() {
            if (ColorAlarmManagerHelper.this.sDebug) {
                Log.d("ColorAlarmManagerHelper", "start run ");
            }
            while (!ActivityManagerNative.isSystemReady()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.w("ColorAlarmManagerHelper", "sleep 100 ms is Interrupted because of " + e);
                }
                if (ColorAlarmManagerHelper.this.sDebug) {
                    Log.d("ColorAlarmManagerHelper", "sleep 100 ms ");
                }
            }
            synchronized (ColorAlarmManagerHelper.this.sLock) {
                ColorAlarmManagerHelper.this.getDataFromProvider();
                ColorAlarmManagerHelper.this.getDataFromLocal();
                ColorAlarmManagerHelper.this.getDataDefault();
                ColorAlarmManagerHelper.this.addNotCoveredWhitelist();
                ColorAlarmManagerHelper.this.addCustomizeWhiteList();
                ColorAlarmManagerHelper.this.addSparkCellBroadcast();
                ColorAlarmManagerHelper.this.sIsWhiteListInited.set(true);
            }
            if (ColorAlarmManagerHelper.this.sDebug) {
                Log.d("ColorAlarmManagerHelper", "isSystemReady is true  !!!!! ");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addSparkCellBroadcast() {
        if (this.sContext.getPackageManager() != null && this.sContext.getPackageManager().hasSystemFeature("oppo.cellbroadcast.emergency.alert.grey")) {
            this.sPkgWhiteList.add("com.android.cellbroadcastreceiver");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addCustomizeWhiteList() {
        StringBuilder sb;
        int type;
        String pkgName;
        if (this.sContext.getPackageManager() != null && this.sContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            File file = new File(this.OPPO_CUSTOMIZE_WHITE_FILE_PATH);
            if (file.exists()) {
                FileReader xmlReader = null;
                try {
                    FileReader xmlReader2 = new FileReader(file);
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(xmlReader2);
                    do {
                        type = parser.next();
                        if (type == 2 && "noalign".equals(parser.getName()) && (pkgName = parser.getAttributeValue(null, "att")) != null && !this.sPkgWhiteList.contains(pkgName)) {
                            this.sPkgWhiteList.add(pkgName);
                            if (this.sOppoDebug) {
                                Log.d("ColorAlarmManagerHelper", "addCustomizeWhiteList pkgName: " + pkgName);
                            }
                        }
                    } while (type != 1);
                    try {
                        xmlReader2.close();
                        return;
                    } catch (IOException e) {
                        e = e;
                        sb = new StringBuilder();
                    }
                } catch (Exception e2) {
                    Log.e("ColorAlarmManagerHelper", "failed parsing ", e2);
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                            return;
                        } catch (IOException e3) {
                            e = e3;
                            sb = new StringBuilder();
                        }
                    } else {
                        return;
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e4) {
                            Log.e("ColorAlarmManagerHelper", "Failed to close state FileInputStream " + e4);
                        }
                    }
                    throw th;
                }
            } else if (this.sOppoDebug) {
                Log.d("ColorAlarmManagerHelper", "addCustomizeWhiteList failed: file doesn't exist!");
                return;
            } else {
                return;
            }
        } else {
            return;
        }
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Log.e("ColorAlarmManagerHelper", sb.toString());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addNotCoveredWhitelist() {
        for (int i = 0; i < this.PKG_WHITE_LIST_NOT_COVERED.size(); i++) {
            String pkg = this.PKG_WHITE_LIST_NOT_COVERED.get(i);
            if (!this.sPkgWhiteList.contains(pkg)) {
                this.sPkgWhiteList.add(pkg);
            }
        }
        for (int i2 = 0; i2 < this.REMOVE_FILTER_LIST_NOT_COVERED.size(); i2++) {
            String pkg2 = this.REMOVE_FILTER_LIST_NOT_COVERED.get(i2);
            if (!this.sRemoveFilterPackagesList.contains(pkg2)) {
                this.sRemoveFilterPackagesList.add(pkg2);
            }
        }
    }

    public boolean isNeedInexactAlarm() {
        if (!checkUid() && !checkPackage(this.sContext)) {
            return true;
        }
        return false;
    }

    private boolean checkUid() {
        if (this.sDebug) {
            Iterator<String> it = this.sUidWhiteList.iterator();
            while (it.hasNext()) {
                Log.d("ColorAlarmManagerHelper", "uid in sUidWhiteList =  " + it.next());
            }
        }
        int uid = Binder.getCallingUid();
        if (inUidWhiteList(uid)) {
            if (!this.sDebug) {
                return true;
            }
            Log.d("ColorAlarmManagerHelper", "checkUid uid == " + uid + " is inUidWhiteList!  using exact alarm!!!");
            return true;
        } else if (!this.sDebug) {
            return false;
        } else {
            Log.d("ColorAlarmManagerHelper", "This uid use inexact alarm !!!  uid == " + uid);
            return false;
        }
    }

    private boolean checkPackage(Context context) {
        if (this.sDebug) {
            Iterator<String> it = this.sPkgWhiteList.iterator();
            while (it.hasNext()) {
                Log.d("ColorAlarmManagerHelper", "Pkg in sPkgWhiteList =  " + it.next());
            }
            Iterator<String> it2 = this.sKeyList.iterator();
            while (it2.hasNext()) {
                Log.d("ColorAlarmManagerHelper", "key in sKeyList == " + it2.next());
            }
        }
        int uid = Binder.getCallingUid();
        String[] packages = this.sContext.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            Log.w("ColorAlarmManagerHelper", "invalid UID " + uid);
            return true;
        }
        for (String pkg : packages) {
            if (ColorStartingWindowContants.WECHAT_PACKAGE_NAME.equals(pkg) && OppoSysStateManager.getInstance().isSuperPowersaveOn()) {
                return false;
            }
            if (inPackageNameWhiteList(pkg)) {
                if (this.sDebug) {
                    Log.d("ColorAlarmManagerHelper", "Pkg is inPackageNameWhiteList! using exact alarm!!!   pkg == " + pkg);
                }
                return true;
            }
            Iterator<String> it3 = this.sKeyList.iterator();
            while (it3.hasNext()) {
                if (pkg.contains(it3.next())) {
                    if (this.sDebug) {
                        Log.d("ColorAlarmManagerHelper", "Packagename match key! using exact alarm!!!  pkg == " + pkg);
                    }
                    return true;
                }
            }
        }
        if (this.sDebug) {
            for (String pkg1 : packages) {
                Log.d("ColorAlarmManagerHelper", "This package use inexact alarm !!!  pkg1 == " + pkg1);
            }
        }
        return false;
    }

    public boolean inPackageNameWhiteList(String pkgName) {
        synchronized (this.sLock) {
            if (this.sPkgWhiteList != null) {
                if (pkgName != null) {
                    return this.sPkgWhiteList.contains(pkgName);
                }
            }
            return false;
        }
    }

    public Integer getAcmeBlackConfig(String pkgName, String tag) {
        synchronized (this.sLock) {
            if (this.mAcmeBlackConfig != null) {
                if (pkgName != null) {
                    ArrayMap<String, Integer> arrayMap = this.mAcmeBlackConfig;
                    return arrayMap.get(pkgName + "_" + tag);
                }
            }
            return 0;
        }
    }

    public boolean isAcmeBlackWord(String pkgName) {
        synchronized (this.sLock) {
            if (this.mAcmeBlackWord != null) {
                if (pkgName != null) {
                    Iterator<String> it = this.mAcmeBlackWord.iterator();
                    while (it.hasNext()) {
                        if (pkgName.contains(it.next())) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            return false;
        }
    }

    public long getAcmeAlignInterval() {
        return this.sAcmeAlignInterval;
    }

    public long getAcmeScreenOffTime() {
        return this.sAcmeScreenOffTime;
    }

    public boolean inUidWhiteList(int uid) {
        ArrayList<String> arrayList = this.sUidWhiteList;
        if (arrayList == null) {
            return false;
        }
        return arrayList.contains(Integer.toString(uid));
    }

    private void resetList() {
        this.sUidWhiteList.clear();
        this.sPkgWhiteList.clear();
        this.sRemoveFilterPackagesList.clear();
        this.sKeyList.clear();
        this.sAlignWhiteList.clear();
        this.sAlignEnforcedWhiteList.clear();
        sDeepSleepRulesList.clear();
        this.sDeepSleepPkgList.clear();
        this.mAcmeBlackConfig.clear();
        this.mAcmeBlackWord.clear();
        this.sPendingJobBlack.clear();
    }

    /* JADX INFO: Multiple debug info for r12v5 java.lang.String: [D('parseAcmeAlignInterval' boolean), D('strText' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r12v6 'strText'  java.lang.String: [D('parseAcmeAlignInterval' boolean), D('strText' java.lang.String)] */
    private void parseXml(XmlPullParser parser, boolean updateFromDb) {
        boolean parseUidWhiteArray;
        Exception e;
        boolean parseFilterPackagesArray;
        boolean parsePkgWhiteArray;
        boolean parseAcmeAlignInterval;
        String strText;
        boolean needParse;
        boolean needParse2;
        boolean needParse3;
        boolean needParse4;
        boolean needParse5;
        boolean needParse6;
        boolean needParse7;
        boolean needParse8;
        boolean needParse9;
        boolean needParse10;
        boolean needParse11;
        boolean needParse12;
        boolean needParse13;
        boolean parsePkgWhiteArray2 = true;
        boolean parseFilterPackagesArray2 = true;
        boolean parseKeyArray = true;
        boolean parseAlignInterval = true;
        boolean parseAlignFirstDelay = true;
        boolean parseAlignWhiteArray = true;
        boolean parseAlignEnforcedWhiteArray = true;
        boolean parseDeepSleepWhiteArray = true;
        boolean parseAcmeBlackList = true;
        boolean parseAcmeBlackWords = true;
        boolean parseAcmeAlignInterval2 = true;
        boolean parseAcmeScreenOffTime = true;
        boolean parsePendingJob = true;
        if (updateFromDb) {
            resetList();
            parseUidWhiteArray = true;
            this.sAlignIntervalFromProvidor = false;
            this.sAlignFirstDelayFromProvidor = false;
            this.sAcmeBlackWordFromProvidor = false;
            this.sAcmeAlignIntervalFromProvidor = false;
            this.sAcmeScreenOffTimeFromProvidor = false;
            this.sPendingJobFromProvidor = false;
        } else {
            parseUidWhiteArray = true;
            if (!this.sUidWhiteList.isEmpty()) {
                parseUidWhiteArray = false;
                needParse = false;
            } else {
                needParse = true;
            }
            if (!this.sPkgWhiteList.isEmpty()) {
                parsePkgWhiteArray2 = false;
                needParse2 = needParse;
            } else {
                needParse2 = true;
            }
            if (!this.sRemoveFilterPackagesList.isEmpty()) {
                parseFilterPackagesArray2 = false;
                needParse3 = needParse2;
            } else {
                needParse3 = true;
            }
            if (!this.sKeyList.isEmpty()) {
                parseKeyArray = false;
                needParse4 = needParse3;
            } else {
                needParse4 = true;
            }
            if (!this.sAlignWhiteList.isEmpty()) {
                parseAlignWhiteArray = false;
                needParse5 = needParse4;
            } else {
                needParse5 = true;
            }
            if (!this.sAlignEnforcedWhiteList.isEmpty()) {
                parseAlignEnforcedWhiteArray = false;
                needParse6 = needParse5;
            } else {
                needParse6 = true;
            }
            if (this.sAlignIntervalFromProvidor) {
                parseAlignInterval = false;
                needParse7 = needParse6;
            } else {
                needParse7 = true;
            }
            if (this.sAlignFirstDelayFromProvidor) {
                parseAlignFirstDelay = false;
                needParse8 = needParse7;
            } else {
                needParse8 = true;
            }
            if (!sDeepSleepRulesList.isEmpty()) {
                parseDeepSleepWhiteArray = false;
            } else {
                needParse8 = true;
            }
            if (!this.mAcmeBlackConfig.isEmpty()) {
                parseAcmeBlackList = false;
                needParse9 = needParse8;
            } else {
                needParse9 = true;
            }
            if (this.sAcmeBlackWordFromProvidor) {
                parseAcmeBlackWords = false;
                needParse10 = needParse9;
            } else {
                needParse10 = true;
            }
            if (this.sAcmeAlignIntervalFromProvidor) {
                parseAcmeAlignInterval2 = false;
                needParse11 = needParse10;
            } else {
                needParse11 = true;
            }
            if (this.sAcmeScreenOffTimeFromProvidor) {
                parseAcmeScreenOffTime = false;
                needParse12 = needParse11;
            } else {
                needParse12 = true;
            }
            if (this.sPendingJobFromProvidor) {
                parsePendingJob = false;
                needParse13 = needParse12;
            } else {
                needParse13 = true;
            }
            if (needParse13) {
                if (parseUidWhiteArray && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse uid From Local.");
                }
                if (parsePkgWhiteArray2 && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse pkg From Local.");
                }
                if (parseFilterPackagesArray2 && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse remove From Local.");
                }
                if (parseKeyArray && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse key From Local.");
                }
                if (parseAlignWhiteArray && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse a1 From Local.");
                }
                if (parseAlignEnforcedWhiteArray && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse a2 From Local.");
                }
                if (parseAlignInterval && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse aI From Local.");
                }
                if (parseAlignFirstDelay && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse aFD From Local.");
                }
                if (parseDeepSleepWhiteArray && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse deep sleep rules From Local.");
                }
                if (parseAcmeBlackList && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse acme black list From Local.");
                }
                if (parseAcmeBlackWords && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse acme black word From Local.");
                }
                if (parseAcmeAlignInterval2 && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse acme align interval From Local.");
                }
                if (parseAcmeScreenOffTime && this.sOppoDebug) {
                    Log.d("ColorAlarmManagerHelper", "parseXml: parse acme screen off time From Local.");
                }
                this.sAlignIntervalFromLocal = false;
                this.sAlignFirstDelayFromLocal = false;
                this.sAcmeBlackWordFromLocal = false;
                this.sAcmeAlignIntervalFromLocal = false;
                this.sAcmeScreenOffTimeFromLocal = false;
                this.sPendingJobFromLocal = false;
            } else if (this.sOppoDebug) {
                Log.d("ColorAlarmManagerHelper", "parseXml: no need to update From Local.");
                return;
            } else {
                return;
            }
        }
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType == 0) {
                    parsePkgWhiteArray = parsePkgWhiteArray2;
                    parseFilterPackagesArray = parseFilterPackagesArray2;
                    parseAcmeAlignInterval = parseAcmeAlignInterval2;
                } else if (eventType != 2) {
                    parsePkgWhiteArray = parsePkgWhiteArray2;
                    parseFilterPackagesArray = parseFilterPackagesArray2;
                    parseAcmeAlignInterval = parseAcmeAlignInterval2;
                } else {
                    try {
                        String strName = parser.getName();
                        parser.next();
                        String strText2 = parser.getText();
                        if (!"UidWhiteArray".equals(strName) || !parseUidWhiteArray) {
                            parseAcmeAlignInterval = parseAcmeAlignInterval2;
                            strText = strText2;
                            try {
                                if (!"PkgWhiteArray".equals(strName) || !parsePkgWhiteArray2) {
                                    if (!"FilterPackagesArray".equals(strName) || !parseFilterPackagesArray2) {
                                        if (!"KeyArray".equals(strName) || !parseKeyArray) {
                                            if (!"AlignInterval".equals(strName) || !parseAlignInterval) {
                                                parsePkgWhiteArray = parsePkgWhiteArray2;
                                                parseFilterPackagesArray = parseFilterPackagesArray2;
                                                if ("AlignFirstDelay".equals(strName) && parseAlignFirstDelay) {
                                                    try {
                                                        this.sAlignFirstDelay = Long.parseLong(strText);
                                                        if (updateFromDb) {
                                                            this.sAlignFirstDelayFromProvidor = true;
                                                        } else {
                                                            this.sAlignFirstDelayFromLocal = true;
                                                        }
                                                    } catch (NumberFormatException e2) {
                                                        this.sAlignFirstDelay = 0;
                                                        Log.w("ColorAlarmManagerHelper", "AlignFirstDelay excption.", e2);
                                                    }
                                                    Log.d("ColorAlarmManagerHelper", "first delay=" + this.sAlignFirstDelay);
                                                } else if (!"AlignWhiteArray".equals(strName) || !parseAlignWhiteArray) {
                                                    if (!"AlignEnforcedWhiteArray".equals(strName) || !parseAlignEnforcedWhiteArray) {
                                                        if ("DeepSleepArray".equals(strName) && parseDeepSleepWhiteArray) {
                                                            if (!sDeepSleepRulesList.contains(strText)) {
                                                                sDeepSleepRulesList.add(strText);
                                                            }
                                                            parseDeepSleepPkgName(strText);
                                                        } else if ("PendingJobCount".equals(strName) && parsePendingJob) {
                                                            try {
                                                                this.sPendingJobCount = Integer.parseInt(strText);
                                                                if (updateFromDb) {
                                                                    this.sPendingJobFromProvidor = true;
                                                                } else {
                                                                    this.sPendingJobFromLocal = true;
                                                                }
                                                            } catch (NumberFormatException e3) {
                                                                this.sPendingJobCount = 3;
                                                                Log.w("ColorAlarmManagerHelper", "PendingJobCount excption.", e3);
                                                            }
                                                            Log.d("ColorAlarmManagerHelper", "sPendingJobCount=" + this.sPendingJobCount);
                                                        } else if ("PendingJobBlack".equals(strName) && parsePendingJob) {
                                                            try {
                                                                if (!this.sPendingJobBlack.contains(strText)) {
                                                                    this.sPendingJobBlack.add(strText);
                                                                }
                                                                if (updateFromDb) {
                                                                    this.sPendingJobFromProvidor = true;
                                                                } else {
                                                                    this.sPendingJobFromLocal = true;
                                                                }
                                                            } catch (NumberFormatException e4) {
                                                                Log.w("ColorAlarmManagerHelper", "PendingJobCount excption.", e4);
                                                            }
                                                        } else if ("AcmeBlackArray".equals(strName) && parseAcmeBlackList) {
                                                            parseAcmeBlackConfig(strText);
                                                        } else if ("AcmeBlackWord".equals(strName) && parseAcmeBlackWords) {
                                                            parseAcmeBlackWord(strText);
                                                            if (updateFromDb) {
                                                                this.sAcmeBlackWordFromProvidor = true;
                                                            } else {
                                                                this.sAcmeBlackWordFromLocal = true;
                                                            }
                                                        } else if ("AcmeAlignInterval".equals(strName) && parseAcmeAlignInterval) {
                                                            try {
                                                                Long interval = Long.valueOf(Long.parseLong(strText));
                                                                if (interval != null) {
                                                                    this.sAcmeAlignInterval = interval.longValue();
                                                                }
                                                                if (updateFromDb) {
                                                                    this.sAcmeAlignIntervalFromProvidor = true;
                                                                } else {
                                                                    this.sAcmeAlignIntervalFromLocal = true;
                                                                }
                                                            } catch (Exception e5) {
                                                            }
                                                        } else if ("AcmeScreenOffTime".equals(strName) && parseAcmeAlignInterval) {
                                                            try {
                                                                Long interval2 = Long.valueOf(Long.parseLong(strText));
                                                                if (interval2 != null) {
                                                                    this.sAcmeScreenOffTime = interval2.longValue();
                                                                }
                                                                if (updateFromDb) {
                                                                    this.sAcmeScreenOffTimeFromProvidor = true;
                                                                } else {
                                                                    this.sAcmeScreenOffTimeFromLocal = true;
                                                                }
                                                            } catch (Exception e6) {
                                                            }
                                                        }
                                                    } else if (!this.sAlignEnforcedWhiteList.contains(strText)) {
                                                        this.sAlignEnforcedWhiteList.add(strText);
                                                    }
                                                } else if (!this.sAlignWhiteList.contains(strText)) {
                                                    this.sAlignWhiteList.add(strText);
                                                }
                                            } else {
                                                parsePkgWhiteArray = parsePkgWhiteArray2;
                                                parseFilterPackagesArray = parseFilterPackagesArray2;
                                                try {
                                                    this.sAlignInterval = Long.parseLong(strText);
                                                    if (updateFromDb) {
                                                        this.sAlignIntervalFromProvidor = true;
                                                    } else {
                                                        this.sAlignIntervalFromLocal = true;
                                                    }
                                                } catch (NumberFormatException e7) {
                                                    try {
                                                        this.sAlignInterval = 5;
                                                        Log.w("ColorAlarmManagerHelper", "AlignInterval excption.", e7);
                                                    } catch (Exception e8) {
                                                        e = e8;
                                                        Log.w("ColorAlarmManagerHelper", "parseXml: Got execption. ", e);
                                                    }
                                                }
                                                Log.d("ColorAlarmManagerHelper", "int=" + this.sAlignInterval);
                                            }
                                        } else if (!this.sKeyList.contains(strText)) {
                                            this.sKeyList.add(strText);
                                            parsePkgWhiteArray = parsePkgWhiteArray2;
                                            parseFilterPackagesArray = parseFilterPackagesArray2;
                                        } else {
                                            parsePkgWhiteArray = parsePkgWhiteArray2;
                                            parseFilterPackagesArray = parseFilterPackagesArray2;
                                        }
                                    } else if (!this.sRemoveFilterPackagesList.contains(strText)) {
                                        this.sRemoveFilterPackagesList.add(strText);
                                        parsePkgWhiteArray = parsePkgWhiteArray2;
                                        parseFilterPackagesArray = parseFilterPackagesArray2;
                                    } else {
                                        parsePkgWhiteArray = parsePkgWhiteArray2;
                                        parseFilterPackagesArray = parseFilterPackagesArray2;
                                    }
                                } else if (!this.sPkgWhiteList.contains(strText)) {
                                    this.sPkgWhiteList.add(strText);
                                    parsePkgWhiteArray = parsePkgWhiteArray2;
                                    parseFilterPackagesArray = parseFilterPackagesArray2;
                                } else {
                                    parsePkgWhiteArray = parsePkgWhiteArray2;
                                    parseFilterPackagesArray = parseFilterPackagesArray2;
                                }
                            } catch (Exception e9) {
                                e = e9;
                                Log.w("ColorAlarmManagerHelper", "parseXml: Got execption. ", e);
                            }
                        } else {
                            try {
                                parseAcmeAlignInterval = parseAcmeAlignInterval2;
                                strText = strText2;
                                try {
                                    if (!this.sUidWhiteList.contains(strText)) {
                                        this.sUidWhiteList.add(strText);
                                        parsePkgWhiteArray = parsePkgWhiteArray2;
                                        parseFilterPackagesArray = parseFilterPackagesArray2;
                                    } else {
                                        parsePkgWhiteArray = parsePkgWhiteArray2;
                                        parseFilterPackagesArray = parseFilterPackagesArray2;
                                    }
                                } catch (Exception e10) {
                                    e = e10;
                                    Log.w("ColorAlarmManagerHelper", "parseXml: Got execption. ", e);
                                }
                            } catch (Exception e11) {
                                e = e11;
                                Log.w("ColorAlarmManagerHelper", "parseXml: Got execption. ", e);
                            }
                        }
                    } catch (Exception e12) {
                        e = e12;
                        Log.w("ColorAlarmManagerHelper", "parseXml: Got execption. ", e);
                    }
                }
                eventType = parser.next();
                parseAcmeScreenOffTime = parseAcmeScreenOffTime;
                parseAcmeAlignInterval2 = parseAcmeAlignInterval;
                parsePkgWhiteArray2 = parsePkgWhiteArray;
                parseFilterPackagesArray2 = parseFilterPackagesArray;
            }
        } catch (Exception e13) {
            e = e13;
            Log.w("ColorAlarmManagerHelper", "parseXml: Got execption. ", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0051, code lost:
        if (0 == 0) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0054, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0055, code lost:
        if (r5 != null) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0057, code lost:
        android.util.Log.w("ColorAlarmManagerHelper", "getDataFromProvider: failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005c, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r2 = android.util.Xml.newPullParser();
        r1 = new java.io.StringReader(r5);
        r2.setInput(r1);
        parseXml(r2, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x006f, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0073, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0075, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        android.util.Log.w("ColorAlarmManagerHelper", "getDataFromProvider: Got execption. ", r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007a, code lost:
        if (r1 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007e, code lost:
        if (r1 != null) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0080, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0083, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0037, code lost:
        if (r4 != null) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0039, code lost:
        r4.close();
     */
    private void getDataFromProvider() {
        Cursor cursor = null;
        String strAlarmWhiteList = null;
        try {
            cursor = this.sContext.getContentResolver().query(this.CONTENT_URI_WHITE_LIST, new String[]{"version", "xml"}, "filtername=\"sys_alarm_filterpackages_list\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex("xml");
                cursor.moveToNext();
                strAlarmWhiteList = cursor.getString(xmlcolumnIndex);
            }
        } catch (Exception e) {
            Log.w("ColorAlarmManagerHelper", "getDataFromProvider: Got execption. " + e);
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getDataDefault() {
        if (this.sUidWhiteList.isEmpty()) {
            this.sUidWhiteList = new ArrayList<>(this.UID_WHITE_LIST);
            Log.d("ColorAlarmManagerHelper", "uid use default.");
        }
        if (this.sPkgWhiteList.isEmpty()) {
            this.sPkgWhiteList = new ArrayList<>(this.PKG_WHITE_LIST);
            Log.d("ColorAlarmManagerHelper", "pkg use default.");
        }
        if (this.sRemoveFilterPackagesList.isEmpty()) {
            this.sRemoveFilterPackagesList = new ArrayList<>(this.REMOVE_FILTER_PKG_LIST);
            Log.d("ColorAlarmManagerHelper", "remove use default.");
        }
        if (this.sKeyList.isEmpty()) {
            this.sKeyList = new ArrayList<>(this.KEYWORD_WHITE_LIST);
            Log.d("ColorAlarmManagerHelper", "key use default.");
        }
        if (this.sAlignWhiteList.isEmpty()) {
            this.sAlignWhiteList = new ArrayList<>(this.ALIGN_WHITE_LIST);
            Log.d("ColorAlarmManagerHelper", "align use default.");
        }
        if (!this.sAlignIntervalFromProvidor && !this.sAlignIntervalFromLocal) {
            this.sAlignInterval = 5;
            Log.d("ColorAlarmManagerHelper", "interval use default.");
        }
        if (!this.sAlignFirstDelayFromProvidor && !this.sAlignFirstDelayFromLocal) {
            this.sAlignFirstDelay = 0;
            Log.d("ColorAlarmManagerHelper", "first delay use default.");
        }
        if (!this.sAcmeBlackWordFromProvidor && !this.sAcmeBlackWordFromLocal) {
            this.mAcmeBlackWord = new ArrayList<>(this.ACME_BLACK_WORD);
            Log.d("ColorAlarmManagerHelper", "acme black work use default.");
        }
        if (!this.sAcmeAlignIntervalFromProvidor && !this.sAcmeAlignIntervalFromLocal) {
            this.sAcmeAlignInterval = 15;
            Log.d("ColorAlarmManagerHelper", "interval use default.");
        }
        if (!this.sAcmeScreenOffTimeFromProvidor && !this.sAcmeScreenOffTimeFromLocal) {
            this.sAcmeScreenOffTime = 30;
            Log.d("ColorAlarmManagerHelper", "screen off time use default.");
        }
        if (!this.sPendingJobFromProvidor && !this.sPendingJobFromLocal) {
            this.sPendingJobCount = 3;
            this.sPendingJobBlack.clear();
            this.sPendingJobBlack.addAll(this.PENDING_JOB_BLACK);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getDataFromLocal() {
        FileReader xmlReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            xmlReader = new FileReader(new File(Environment.getRootDirectory(), "oppo/alarm_filter_packages.xml"));
            parser.setInput(xmlReader);
            parseXml(parser, false);
            try {
                xmlReader.close();
            } catch (IOException e) {
                Log.w("ColorAlarmManagerHelper", "getDataFromLocal: Got execption close xmlReader. ", e);
            }
        } catch (Exception e2) {
            Log.w("ColorAlarmManagerHelper", "getDataFromLocal: Got execption. ", e2);
            if (xmlReader != null) {
                xmlReader.close();
            }
        } catch (Throwable th) {
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (IOException e3) {
                    Log.w("ColorAlarmManagerHelper", "getDataFromLocal: Got execption close xmlReader. ", e3);
                }
            }
            throw th;
        }
    }

    public boolean isFilterRemovePackage(String pkg) {
        boolean contains;
        synchronized (this.sLock) {
            if (this.sDebug) {
                Iterator<String> it = this.sRemoveFilterPackagesList.iterator();
                while (it.hasNext()) {
                    Log.d("ColorAlarmManagerHelper", "removePackage in sRemoveFilterPackagesList =  " + it.next());
                }
            }
            contains = this.sRemoveFilterPackagesList.contains(pkg);
        }
        return contains;
    }

    public void dump(PrintWriter pw) {
        synchronized (this.sLock) {
            pw.println();
            pw.println("-----ColorAlarmManagerHelper-----");
            pw.println("sAlignInterval=" + this.sAlignInterval + "minutes");
            pw.println("sAlignFirstDelay=" + this.sAlignFirstDelay + "minutes");
            pw.println();
            pw.println("sUidWhiteList:");
            for (int i = 0; i < this.sUidWhiteList.size(); i++) {
                pw.println(this.sUidWhiteList.get(i));
            }
            pw.println();
            pw.println("sKeyList:");
            for (int i2 = 0; i2 < this.sKeyList.size(); i2++) {
                pw.println(this.sKeyList.get(i2));
            }
            pw.println();
            pw.println("sPkgWhiteList:");
            for (int i3 = 0; i3 < this.sPkgWhiteList.size(); i3++) {
                pw.println(this.sPkgWhiteList.get(i3));
            }
            pw.println();
            pw.println("sRemoveFilterPackagesList:");
            for (int i4 = 0; i4 < this.sRemoveFilterPackagesList.size(); i4++) {
                pw.println(this.sRemoveFilterPackagesList.get(i4));
            }
            pw.println();
            pw.println("sAlignEnforcedWhiteList:");
            for (int i5 = 0; i5 < this.sAlignEnforcedWhiteList.size(); i5++) {
                pw.println(this.sAlignEnforcedWhiteList.get(i5));
            }
            pw.println();
            pw.println("sAlignWhiteList:");
            for (int i6 = 0; i6 < this.sAlignWhiteList.size(); i6++) {
                pw.println(this.sAlignWhiteList.get(i6));
            }
            pw.println();
            pw.println("sDeepSleepRulesList:");
            for (int i7 = 0; i7 < sDeepSleepRulesList.size(); i7++) {
                pw.println(sDeepSleepRulesList.get(i7));
            }
            pw.println();
            pw.println("sDeepSleepPkgList:");
            for (int i8 = 0; i8 < this.sDeepSleepPkgList.size(); i8++) {
                pw.println("deepsleeppkg:" + this.sDeepSleepPkgList.get(i8));
            }
            pw.println("mAcmeBlackConfig:");
            for (int i9 = 0; i9 < this.mAcmeBlackConfig.size(); i9++) {
                pw.println("k = " + this.mAcmeBlackConfig.keyAt(i9) + ", v = " + this.mAcmeBlackConfig.valueAt(i9));
            }
            pw.println("mAcmeBlackWord:");
            for (int i10 = 0; i10 < this.mAcmeBlackWord.size(); i10++) {
                pw.println("k = " + this.mAcmeBlackWord.get(i10));
            }
            pw.println("sAcmeAlignInterval=" + this.sAcmeAlignInterval + "minutes");
            pw.println("sAcmeScreenOffTime=" + this.sAcmeScreenOffTime + "minutes");
            pw.println();
            pw.println("sPendingJobCount:" + this.sPendingJobCount);
            pw.println();
            for (int i11 = 0; i11 < this.sPendingJobBlack.size(); i11++) {
                pw.println("sPendingJobBlack:" + this.sPendingJobBlack.get(i11));
            }
            pw.println();
        }
    }

    public boolean isInAlignWhiteList(String pkgName) {
        boolean contains;
        synchronized (this.sLock) {
            contains = this.sAlignWhiteList.contains(pkgName);
        }
        return contains;
    }

    public boolean isInAlignEnforcedWhiteList(String pkgName) {
        boolean contains;
        synchronized (this.sLock) {
            contains = this.sAlignEnforcedWhiteList.contains(pkgName);
        }
        return contains;
    }

    public boolean containKeyWord(String pkgName) {
        synchronized (this.sLock) {
            Iterator<String> it = this.sKeyList.iterator();
            while (it.hasNext()) {
                if (pkgName.toLowerCase().contains(it.next())) {
                    return true;
                }
            }
            return false;
        }
    }

    public long getAlignInterval() {
        return this.sAlignInterval;
    }

    public long getAlignFirstDelay() {
        return this.sAlignFirstDelay;
    }

    public boolean isMatchDeepSleepRule(String pkg, String tag, int netStatus) {
        String rule;
        if (tag.contains("DEEP_SLEEP_WHITE_IN_MOBILE")) {
            rule = pkg;
        } else if (tag.contains("DEEP_SLEEP_WHITE_IN_WIFI")) {
            StringBuilder sb = new StringBuilder();
            sb.append(pkg);
            sb.append("=");
            sb.append(netStatus == 1 ? "" : tag);
            rule = sb.toString();
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(pkg);
            sb2.append("=");
            sb2.append(tag);
            sb2.append("=");
            sb2.append(netStatus == 1 ? "" : Integer.valueOf(netStatus));
            rule = sb2.toString();
        }
        synchronized (this.sLock) {
            Iterator<String> it = sDeepSleepRulesList.iterator();
            while (it.hasNext()) {
                if (it.next().startsWith(rule)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isMatchDeepSleepRule(ComponentName component) {
        boolean contains;
        synchronized (this.sLock) {
            contains = sDeepSleepRulesList.contains(component.flattenToShortString());
        }
        return contains;
    }

    public int getPendingJobCount() {
        return this.sPendingJobCount;
    }

    public boolean isBlackJobList(String pkgName, String flattenToShortString) {
        boolean result = false;
        Slog.d("ColorAlarmManagerHelper", "isBlackJobList pkg = " + pkgName + ", flatten = " + flattenToShortString);
        synchronized (this.sLock) {
            Iterator<String> it = this.sPendingJobBlack.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String black = it.next();
                boolean isComponent = black.contains("/");
                if (isComponent) {
                    result = black.equals(flattenToShortString);
                } else {
                    result = black.equals(pkgName);
                }
                Slog.d("ColorAlarmManagerHelper", "isBlackJobList black = " + black + ", isComponent = " + isComponent + ", result = " + result);
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    public void processDied(String processName) {
        WorkerHandler workerHandler = this.sWorkerHandler;
        if (workerHandler != null) {
            Message msg = workerHandler.obtainMessage();
            msg.what = 201;
            Bundle data = new Bundle();
            data.putString("pkg", processName);
            msg.setData(data);
            this.sWorkerHandler.sendMessage(msg);
        }
    }

    public void removeAlarmLocked(int uid) {
        if (UserHandle.getAppId(uid) == 1000) {
            Slog.w("ColorAlarmManagerHelper", "removeLocked: Shouldn't for UID=" + uid);
            return;
        }
        String[] packageNames = this.sContext.getPackageManager().getPackagesForUid(uid);
        if (packageNames == null || packageNames.length <= 1) {
            if (packageNames != null && packageNames.length == 1) {
                String packageName = packageNames[0];
                if (isFilterRemovePackage(packageName)) {
                    Slog.d("ColorAlarmManagerHelper", "Not remove alarm for white list package name: " + packageName);
                    return;
                }
            }
            this.sAlarm.removeLocked(uid);
            return;
        }
        for (String packageName2 : packageNames) {
            if (isFilterRemovePackage(packageName2)) {
                Slog.d("ColorAlarmManagerHelper", "Not remove alarm for white list package name: " + packageName2);
            } else {
                this.sAlarm.removeLocked(packageName2);
                Slog.d("ColorAlarmManagerHelper", "remove alarm for package name: " + packageName2);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDeepSleepTrafficPkg(String pkg) {
        boolean match = false;
        if (pkg == null) {
            return false;
        }
        synchronized (this.sLock) {
            if (this.sDeepSleepPkgList.contains(pkg)) {
                match = true;
            }
        }
        return match;
    }

    private void parseDeepSleepPkgName(String strText) {
        String[] splitStr = strText.split("=");
        if (splitStr.length != 3) {
            String[] splitStr2 = strText.split("/");
            if (splitStr2.length == 2 && !this.sDeepSleepPkgList.contains(splitStr2[0])) {
                this.sDeepSleepPkgList.add(splitStr2[0]);
            }
        } else if (!this.sDeepSleepPkgList.contains(splitStr[0])) {
            this.sDeepSleepPkgList.add(splitStr[0]);
        }
    }

    private void parseAcmeBlackConfig(String strText) {
        String[] splitStr = strText.split("=");
        if (splitStr.length == 2 && !this.mAcmeBlackConfig.containsKey(splitStr[0])) {
            try {
                Integer value = Integer.valueOf(Integer.parseInt(splitStr[1]));
                if (value != null) {
                    this.mAcmeBlackConfig.put(splitStr[0], value);
                }
            } catch (Exception e) {
            }
        }
    }

    private void parseAcmeBlackWord(String strText) {
        if (!this.mAcmeBlackWord.contains(strText)) {
            this.mAcmeBlackWord.add(strText);
        }
    }

    /* access modifiers changed from: private */
    public class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (201 == msg.what) {
                Bundle data = msg.getData();
                if (data != null) {
                    String pkg = data.getString("pkg");
                    if (ColorAlarmManagerHelper.this.isDeepSleepTrafficPkg(pkg)) {
                        Intent i = new Intent("oppo.intent.action.DEEP_SLEEP_ESPECIAL_TRAFFIC_REQ");
                        i.setPackage("com.coloros.oppoguardelf");
                        i.putExtra("req", "stop");
                        i.putExtra("pkg", pkg);
                        ColorAlarmManagerHelper.this.sContext.sendBroadcast(i, "oppo.permission.OPPO_COMPONENT_SAFE");
                        if (ColorAlarmManagerHelper.this.sOppoDebug) {
                            Log.d("ColorAlarmManagerHelper", "PROCESS DIED send broad. pkg=" + pkg);
                        }
                    }
                }
            } else if (ColorAlarmManagerHelper.this.sOppoDebug) {
                Log.d("ColorAlarmManagerHelper", "unkown msg. msg=" + msg.what);
            }
        }
    }

    public void addAppAlarmWhiteList(List<String> packageNames) {
        if (this.sContext.getPackageManager() == null || !this.sContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            Log.d("ColorAlarmManagerHelper", "addAppAlarmWhiteList failed ");
            return;
        }
        synchronized (this.sLock) {
            for (String pkgName : packageNames) {
                if (pkgName != null && !this.sPkgWhiteList.contains(pkgName)) {
                    this.sPkgWhiteList.add(pkgName);
                    this.sCustomPkgWhiteList.add(pkgName);
                    if (this.sOppoDebug) {
                        Log.d("ColorAlarmManagerHelper", "addAppAlarmWhiteList pkgName: " + pkgName);
                    }
                }
            }
        }
    }

    public List<String> getAppAlarmWhiteList() {
        ArrayList<String> sCPWhiteList;
        if (this.sContext.getPackageManager() == null || !this.sContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            Log.d("ColorAlarmManagerHelper", "getAppAlarmWhiteList failed ");
            return new ArrayList();
        }
        synchronized (this.sLock) {
            sCPWhiteList = new ArrayList<>(this.sCustomPkgWhiteList);
        }
        return sCPWhiteList;
    }

    public boolean removeAppAlarmWhiteList(List<String> packageNames) {
        if (this.sContext.getPackageManager() == null || !this.sContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            Log.d("ColorAlarmManagerHelper", "removeAppAlarmWhiteList failed ");
            return false;
        }
        synchronized (this.sLock) {
            for (String pkgName : packageNames) {
                if (pkgName != null && this.sPkgWhiteList.contains(pkgName) && this.sCustomPkgWhiteList.contains(pkgName)) {
                    this.sPkgWhiteList.remove(pkgName);
                    this.sCustomPkgWhiteList.remove(pkgName);
                    if (this.sOppoDebug) {
                        Log.d("ColorAlarmManagerHelper", "removeAppAlarmWhiteList pkgName: " + pkgName);
                    }
                }
            }
        }
        return true;
    }

    public boolean removeAllAppAlarmWhiteList() {
        if (this.sContext.getPackageManager() == null || !this.sContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
            Log.d("ColorAlarmManagerHelper", "removeAllAppAlarmWhiteList failed ");
            return false;
        }
        synchronized (this.sLock) {
            Iterator<String> it = this.sCustomPkgWhiteList.iterator();
            while (it.hasNext()) {
                String pkgName = it.next();
                if (pkgName != null && this.sPkgWhiteList.contains(pkgName)) {
                    this.sPkgWhiteList.remove(pkgName);
                    if (this.sOppoDebug) {
                        Log.d("ColorAlarmManagerHelper", "removeAppAlarmWhiteList pkgName: " + pkgName);
                    }
                }
            }
            this.sCustomPkgWhiteList.clear();
        }
        return true;
    }
}
