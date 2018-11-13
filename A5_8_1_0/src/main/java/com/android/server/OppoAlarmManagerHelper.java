package com.android.server;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmlpull.v1.XmlPullParser;

public class OppoAlarmManagerHelper {
    private static final String ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final long ALIGN_INTERVAL = 5;
    private static final List<String> ALIGN_WHITE_LIST = Arrays.asList(new String[]{"com.google.android.gms", "com.taobao.qianniu", "com.google.android.gsf", "com.kuaidi.daijia.driver", "com.sdu.didi.gsui", "com.sdu.didi.gui", "com.duoduo.vip.taxi", "com.ubercab.driver", "net.zdsoft.taxiclient2", "cn.edaijia.android.driverclient", "com.yongche", "com.funcity.taxi.driver", "com.hn.client.driver", "com.joyskim.taxis_driver", "com.anyimob.taxi", "com.eastedge.taxidriverforpad", "com.oppo.im", "com.sankuai.meituan.dispatch.homebrew", "com.baidu.lbs.waimai.baidurider"});
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = Uri.parse("content://com.nearme.romupdate.provider.db/update_list");
    private static final String FILTER_NAME = "sys_alarm_filterpackages_list";
    private static final String INEXACT_ALARM_FEATURE_NAME = "oppo.inexact.alarm";
    private static final List<String> KEYWORD_WHITE_LIST = Arrays.asList(new String[]{"clock", "alarm", "calendar"});
    private static final String OPPO_CUSTOMIZE_WHITE_FILE_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final List<String> PKG_WHITE_LIST = Arrays.asList(new String[]{"com.android.mms", "com.coloros.soundrecorder", "com.android.providers.calendar", "com.oppo.music", "com.coloros.backuprestore", "com.coloros.weather", "com.mobiletools.systemhelper", "com.oppo.community", "com.nearme.themespace", "com.android.keyguard", "com.nearme.romupdate"});
    private static final List<String> PKG_WHITE_LIST_NOT_COVERED = Arrays.asList(new String[]{"com.oppo.ctautoregist", "com.coloros.sauhelper"});
    private static final List<String> REMOVE_FILTER_LIST_NOT_COVERED = Arrays.asList(new String[]{"com.coloros.weather.service", "com.android.providers.media", "com.coloros.childrenspace", "com.coloros.sauhelper", "com.nearme.instant.platform", "com.coloros.athena", "com.coloros.oppoguardelf", "com.coloros.alarmclock"});
    private static final List<String> REMOVE_FILTER_PKG_LIST = Arrays.asList(new String[]{"com.coloros.alarmclock", "com.android.calendar", "com.android.mms", "com.android.providers.calendar", "com.oppo.market", "com.nearme.note", "com.oppo.ota", "com.nearme.statistics.rom", "android", "com.coloros.feedback", "com.nearme.gamecenter", "com.coloros.scratch", "com.coloros.soundrecorder", "com.oppo.reader", "com.nearme.themespace", "com.android.browser", "com.coloros.oppomorningsystem", "com.coloros.backuprestore", "com.coloros.filemanager", "com.android.engineeringmode", "com.oppo.engineermode", "com.oppo.usercenter", "com.zdworks.android.zdclock", "com.coloros.leather", "com.coloros.gallery3d", "com.nearme.romupdate", "com.android.providers.calendar", "com.coloros.sau", "com.coloros.safe.service.framework", "com.android.contacts", "com.coloros.cloud", "com.oppo.community", "com.coloros.video", "com.ted.number"});
    private static final int SLEEP_TIME = 100;
    private static final String TAG = "OppoAlarmManagerHelper";
    private static final List<String> UID_WHITE_LIST = Arrays.asList(new String[]{"1000", "1001"});
    private static AlarmManagerService sAlarm;
    private static ArrayList<String> sAlignEnforcedWhiteList = new ArrayList();
    private static long sAlignFirstDelay = 0;
    private static boolean sAlignFirstDelayFromLocal = false;
    private static boolean sAlignFirstDelayFromProvidor = false;
    private static long sAlignInterval = ALIGN_INTERVAL;
    private static boolean sAlignIntervalFromLocal = false;
    private static boolean sAlignIntervalFromProvidor = false;
    private static ArrayList<String> sAlignWhiteList = new ArrayList();
    private static Context sContext;
    private static boolean sDebug = false;
    private static boolean sHaveAlarmFeature = false;
    private static AtomicBoolean sIsWhiteListInited = new AtomicBoolean();
    private static ArrayList<String> sKeyList = new ArrayList();
    private static final Object sLock = new Object();
    private static boolean sOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static ArrayList<String> sPkgWhiteList = new ArrayList();
    private static ArrayList<String> sRemoveFilterPackagesList = new ArrayList();
    private static ArrayList<String> sUidWhiteList = new ArrayList();

    private static class GetDataFromProviderRunnable implements Runnable {
        public void run() {
            if (OppoAlarmManagerHelper.sDebug) {
                Log.d(OppoAlarmManagerHelper.TAG, "start run ");
            }
            while (!ActivityManagerNative.isSystemReady()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.w(OppoAlarmManagerHelper.TAG, "sleep 100 ms is Interrupted because of " + e);
                }
                if (OppoAlarmManagerHelper.sDebug) {
                    Log.d(OppoAlarmManagerHelper.TAG, "sleep 100 ms ");
                }
            }
            synchronized (OppoAlarmManagerHelper.sLock) {
                OppoAlarmManagerHelper.getDataFromProvider();
                OppoAlarmManagerHelper.getDataFromLocal();
                OppoAlarmManagerHelper.getDataDefault();
                OppoAlarmManagerHelper.addNotCoveredWhitelist();
                OppoAlarmManagerHelper.addCustomizeWhiteList();
                OppoAlarmManagerHelper.sIsWhiteListInited.set(true);
            }
            if (OppoAlarmManagerHelper.sDebug) {
                Log.d(OppoAlarmManagerHelper.TAG, "isSystemReady is true  !!!!! ");
            }
        }
    }

    public static void init(Context context, AlarmManagerService alarm) {
        synchronized (sLock) {
            sContext = context;
            sAlarm = alarm;
            sHaveAlarmFeature = sContext.getPackageManager().hasSystemFeature(INEXACT_ALARM_FEATURE_NAME);
            new Thread(new GetDataFromProviderRunnable(), "get_data_from_provider").start();
            initRomUpdateBroadcast(context);
        }
    }

    /* JADX WARNING: Missing block: B:25:0x0047, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:27:0x0049, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long setInexactAlarm(long windowLength) {
        synchronized (sLock) {
            if (sHaveAlarmFeature && sIsWhiteListInited.get() && windowLength == 0) {
                if (sDebug) {
                    Log.d(TAG, "windowLength == AlarmManager.WINDOW_EXACT");
                }
                if (isNeedInexactAlarm()) {
                    if (sDebug) {
                        Log.d(TAG, "Using  inexact alarm!!!!!!!!");
                    }
                    return -1;
                } else if (sDebug) {
                    Log.d(TAG, "Using exact alarm!!!!!!!!");
                }
            }
        }
    }

    private static void initRomUpdateBroadcast(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS".equals(intent.getAction())) {
                    ArrayList<String> changeList = intent.getStringArrayListExtra("ROM_UPDATE_CONFIG_LIST");
                    if (changeList != null && changeList.contains(OppoAlarmManagerHelper.FILTER_NAME)) {
                        new Thread(new GetDataFromProviderRunnable(), "AlarmRomUpdate").start();
                        Log.d(OppoAlarmManagerHelper.TAG, "ACTION_ROM_UPDATE_CONFIG_SUCCES");
                    }
                }
            }
        }, filter);
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x00e4 A:{SYNTHETIC, Splitter: B:46:0x00e4} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c1 A:{SYNTHETIC, Splitter: B:40:0x00c1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void addCustomizeWhiteList() {
        Exception e;
        Throwable th;
        if (sContext.getPackageManager() != null && (sContext.getPackageManager().hasSystemFeature("oppo.business.custom") ^ 1) == 0) {
            File file = new File(OPPO_CUSTOMIZE_WHITE_FILE_PATH);
            if (file.exists()) {
                FileReader xmlReader = null;
                try {
                    FileReader xmlReader2 = new FileReader(file);
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(xmlReader2);
                        int type;
                        do {
                            type = parser.next();
                            if (type == 2) {
                                if ("noalign".equals(parser.getName())) {
                                    String pkgName = parser.getAttributeValue(null, "att");
                                    if (!(pkgName == null || (sPkgWhiteList.contains(pkgName) ^ 1) == 0)) {
                                        sPkgWhiteList.add(pkgName);
                                        if (sOppoDebug) {
                                            Log.d(TAG, "addCustomizeWhiteList pkgName: " + pkgName);
                                        }
                                    }
                                }
                            }
                        } while (type != 1);
                        if (xmlReader2 != null) {
                            try {
                                xmlReader2.close();
                            } catch (IOException e2) {
                                Log.e(TAG, "Failed to close state FileInputStream " + e2);
                            }
                        }
                        xmlReader = xmlReader2;
                    } catch (Exception e3) {
                        e = e3;
                        xmlReader = xmlReader2;
                        try {
                            Log.e(TAG, "failed parsing ", e);
                            if (xmlReader != null) {
                                try {
                                    xmlReader.close();
                                } catch (IOException e22) {
                                    Log.e(TAG, "Failed to close state FileInputStream " + e22);
                                }
                            }
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            if (xmlReader != null) {
                                try {
                                    xmlReader.close();
                                } catch (IOException e222) {
                                    Log.e(TAG, "Failed to close state FileInputStream " + e222);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        xmlReader = xmlReader2;
                        if (xmlReader != null) {
                        }
                        throw th;
                    }
                } catch (Exception e4) {
                    e = e4;
                    Log.e(TAG, "failed parsing ", e);
                    if (xmlReader != null) {
                    }
                    return;
                }
                return;
            }
            if (sOppoDebug) {
                Log.d(TAG, "addCustomizeWhiteList failed: file doesn't exist!");
            }
        }
    }

    private static void addNotCoveredWhitelist() {
        int i;
        String pkg;
        for (i = 0; i < PKG_WHITE_LIST_NOT_COVERED.size(); i++) {
            pkg = (String) PKG_WHITE_LIST_NOT_COVERED.get(i);
            if (!sPkgWhiteList.contains(pkg)) {
                sPkgWhiteList.add(pkg);
            }
        }
        for (i = 0; i < REMOVE_FILTER_LIST_NOT_COVERED.size(); i++) {
            pkg = (String) REMOVE_FILTER_LIST_NOT_COVERED.get(i);
            if (!sRemoveFilterPackagesList.contains(pkg)) {
                sRemoveFilterPackagesList.add(pkg);
            }
        }
    }

    private static boolean isNeedInexactAlarm() {
        if (checkUid() || checkPackage(sContext)) {
            return false;
        }
        return true;
    }

    private static boolean checkUid() {
        if (sDebug) {
            for (String uidWhite : sUidWhiteList) {
                Log.d(TAG, "uid in sUidWhiteList =  " + uidWhite);
            }
        }
        int uid = Binder.getCallingUid();
        if (inUidWhiteList(uid)) {
            if (sDebug) {
                Log.d(TAG, "checkUid uid == " + uid + " is inUidWhiteList!  using exact alarm!!!");
            }
            return true;
        }
        if (sDebug) {
            Log.d(TAG, "This uid use inexact alarm !!!  uid == " + uid);
        }
        return false;
    }

    private static boolean checkPackage(Context context) {
        if (sDebug) {
            for (String pkgWhite : sPkgWhiteList) {
                Log.d(TAG, "Pkg in sPkgWhiteList =  " + pkgWhite);
            }
            for (String keyWhite : sKeyList) {
                Log.d(TAG, "key in sKeyList == " + keyWhite);
            }
        }
        int uid = Binder.getCallingUid();
        String[] packages = sContext.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            Log.w(TAG, "invalid UID " + uid);
            return true;
        }
        for (String pkg : packages) {
            if (inPackageNameWhiteList(pkg)) {
                if (sDebug) {
                    Log.d(TAG, "Pkg is inPackageNameWhiteList! using exact alarm!!!   pkg == " + pkg);
                }
                return true;
            }
            for (String key : sKeyList) {
                if (pkg.contains(key)) {
                    if (sDebug) {
                        Log.d(TAG, "Packagename match key! using exact alarm!!!  pkg == " + pkg);
                    }
                    return true;
                }
            }
        }
        if (sDebug) {
            for (String pkg1 : packages) {
                Log.d(TAG, "This package use inexact alarm !!!  pkg1 == " + pkg1);
            }
        }
        return false;
    }

    public static boolean inPackageNameWhiteList(String pkgName) {
        synchronized (sLock) {
            if (sPkgWhiteList == null || pkgName == null) {
                return false;
            }
            boolean contains = sPkgWhiteList.contains(pkgName);
            return contains;
        }
    }

    private static boolean inUidWhiteList(int uid) {
        if (sUidWhiteList == null) {
            return false;
        }
        return sUidWhiteList.contains(Integer.toString(uid));
    }

    private static void resetList() {
        sUidWhiteList.clear();
        sPkgWhiteList.clear();
        sRemoveFilterPackagesList.clear();
        sKeyList.clear();
        sAlignWhiteList.clear();
        sAlignEnforcedWhiteList.clear();
    }

    private static void parseXml(XmlPullParser parser, boolean updateFromDb) {
        boolean parseUidWhiteArray = true;
        boolean parsePkgWhiteArray = true;
        boolean parseFilterPackagesArray = true;
        boolean parseKeyArray = true;
        boolean parseAlignInterval = true;
        boolean parseAlignFirstDelay = true;
        boolean parseAlignWhiteArray = true;
        boolean parseAlignEnforcedWhiteArray = true;
        if (updateFromDb) {
            resetList();
            sAlignIntervalFromProvidor = false;
            sAlignFirstDelayFromProvidor = false;
        } else {
            boolean needParse = false;
            if (sUidWhiteList.isEmpty()) {
                needParse = true;
            } else {
                parseUidWhiteArray = false;
            }
            if (sPkgWhiteList.isEmpty()) {
                needParse = true;
            } else {
                parsePkgWhiteArray = false;
            }
            if (sRemoveFilterPackagesList.isEmpty()) {
                needParse = true;
            } else {
                parseFilterPackagesArray = false;
            }
            if (sKeyList.isEmpty()) {
                needParse = true;
            } else {
                parseKeyArray = false;
            }
            if (sAlignWhiteList.isEmpty()) {
                needParse = true;
            } else {
                parseAlignWhiteArray = false;
            }
            if (sAlignEnforcedWhiteList.isEmpty()) {
                needParse = true;
            } else {
                parseAlignEnforcedWhiteArray = false;
            }
            if (sAlignIntervalFromProvidor) {
                parseAlignInterval = false;
            } else {
                needParse = true;
            }
            if (sAlignFirstDelayFromProvidor) {
                parseAlignFirstDelay = false;
            } else {
                needParse = true;
            }
            if (needParse) {
                if (parseUidWhiteArray && sOppoDebug) {
                    Log.d(TAG, "parseXml: parse uid From Local.");
                }
                if (parsePkgWhiteArray && sOppoDebug) {
                    Log.d(TAG, "parseXml: parse pkg From Local.");
                }
                if (parseFilterPackagesArray && sOppoDebug) {
                    Log.d(TAG, "parseXml: parse remove From Local.");
                }
                if (parseKeyArray && sOppoDebug) {
                    Log.d(TAG, "parseXml: parse key From Local.");
                }
                if (parseAlignWhiteArray && sOppoDebug) {
                    Log.d(TAG, "parseXml: parse a1 From Local.");
                }
                if (parseAlignEnforcedWhiteArray && sOppoDebug) {
                    Log.d(TAG, "parseXml: parse a2 From Local.");
                }
                if (parseAlignInterval && sOppoDebug) {
                    Log.d(TAG, "parseXml: parse aI From Local.");
                }
                if (parseAlignFirstDelay && sOppoDebug) {
                    Log.d(TAG, "parseXml: parse aFD From Local.");
                }
                sAlignIntervalFromLocal = false;
                sAlignFirstDelayFromLocal = false;
            } else {
                if (sOppoDebug) {
                    Log.d(TAG, "parseXml: no need to update From Local.");
                }
                return;
            }
        }
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                switch (eventType) {
                    case 2:
                        String strName = parser.getName();
                        eventType = parser.next();
                        String strText = parser.getText();
                        if (!"UidWhiteArray".equals(strName) || !parseUidWhiteArray) {
                            if (!"PkgWhiteArray".equals(strName) || !parsePkgWhiteArray) {
                                if (!"FilterPackagesArray".equals(strName) || !parseFilterPackagesArray) {
                                    if (!"KeyArray".equals(strName) || !parseKeyArray) {
                                        if (!"AlignInterval".equals(strName) || !parseAlignInterval) {
                                            if (!"AlignFirstDelay".equals(strName) || !parseAlignFirstDelay) {
                                                if (!"AlignWhiteArray".equals(strName) || !parseAlignWhiteArray) {
                                                    if ("AlignEnforcedWhiteArray".equals(strName) && parseAlignEnforcedWhiteArray && !sAlignEnforcedWhiteList.contains(strText)) {
                                                        sAlignEnforcedWhiteList.add(strText);
                                                        break;
                                                    }
                                                } else if (!sAlignWhiteList.contains(strText)) {
                                                    sAlignWhiteList.add(strText);
                                                    break;
                                                }
                                            }
                                            try {
                                                sAlignFirstDelay = Long.parseLong(strText);
                                                if (updateFromDb) {
                                                    sAlignFirstDelayFromProvidor = true;
                                                } else {
                                                    sAlignFirstDelayFromLocal = true;
                                                }
                                            } catch (NumberFormatException e) {
                                                sAlignFirstDelay = 0;
                                                Log.w(TAG, "AlignFirstDelay excption.", e);
                                            }
                                            Log.d(TAG, "first delay=" + sAlignFirstDelay);
                                            break;
                                        }
                                        try {
                                            sAlignInterval = Long.parseLong(strText);
                                            if (updateFromDb) {
                                                sAlignIntervalFromProvidor = true;
                                            } else {
                                                sAlignIntervalFromLocal = true;
                                            }
                                        } catch (NumberFormatException e2) {
                                            sAlignInterval = ALIGN_INTERVAL;
                                            Log.w(TAG, "AlignInterval excption.", e2);
                                        }
                                        Log.d(TAG, "int=" + sAlignInterval);
                                        break;
                                    } else if (!sKeyList.contains(strText)) {
                                        sKeyList.add(strText);
                                        break;
                                    }
                                } else if (!sRemoveFilterPackagesList.contains(strText)) {
                                    sRemoveFilterPackagesList.add(strText);
                                    break;
                                }
                            } else if (!sPkgWhiteList.contains(strText)) {
                                sPkgWhiteList.add(strText);
                                break;
                            }
                        } else if (!sUidWhiteList.contains(strText)) {
                            sUidWhiteList.add(strText);
                            break;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e3) {
            Log.w(TAG, "parseXml: Got execption. ", e3);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x009c  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00a3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void getDataFromProvider() {
        Exception e;
        Throwable th;
        Cursor cursor = null;
        String strAlarmWhiteList = null;
        try {
            cursor = sContext.getContentResolver().query(CONTENT_URI_WHITE_LIST, new String[]{"version", COLUMN_NAME_2}, "filtername=\"sys_alarm_filterpackages_list\"", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int versioncolumnIndex = cursor.getColumnIndex("version");
                int xmlcolumnIndex = cursor.getColumnIndex(COLUMN_NAME_2);
                cursor.moveToNext();
                strAlarmWhiteList = cursor.getString(xmlcolumnIndex);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e2) {
            Log.w(TAG, "getDataFromProvider: Got execption. " + e2);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.close();
            }
        }
        StringReader strReader = null;
        if (strAlarmWhiteList == null) {
            Log.w(TAG, "getDataFromProvider: failed");
            return;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            StringReader strReader2 = new StringReader(strAlarmWhiteList);
            try {
                parser.setInput(strReader2);
                parseXml(parser, true);
                if (strReader2 != null) {
                    strReader2.close();
                }
                strReader = strReader2;
            } catch (Exception e3) {
                e2 = e3;
                strReader = strReader2;
                try {
                    Log.w(TAG, "getDataFromProvider: Got execption. ", e2);
                    if (strReader != null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (strReader != null) {
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                strReader = strReader2;
                if (strReader != null) {
                    strReader.close();
                }
                throw th;
            }
        } catch (Exception e4) {
            e2 = e4;
            Log.w(TAG, "getDataFromProvider: Got execption. ", e2);
            if (strReader != null) {
                strReader.close();
            }
        }
    }

    private static void getDataDefault() {
        if (sUidWhiteList.isEmpty()) {
            sUidWhiteList = new ArrayList(UID_WHITE_LIST);
            Log.d(TAG, "uid use default.");
        }
        if (sPkgWhiteList.isEmpty()) {
            sPkgWhiteList = new ArrayList(PKG_WHITE_LIST);
            Log.d(TAG, "pkg use default.");
        }
        if (sRemoveFilterPackagesList.isEmpty()) {
            sRemoveFilterPackagesList = new ArrayList(REMOVE_FILTER_PKG_LIST);
            Log.d(TAG, "remove use default.");
        }
        if (sKeyList.isEmpty()) {
            sKeyList = new ArrayList(KEYWORD_WHITE_LIST);
            Log.d(TAG, "key use default.");
        }
        if (sAlignWhiteList.isEmpty()) {
            sAlignWhiteList = new ArrayList(ALIGN_WHITE_LIST);
            Log.d(TAG, "align use default.");
        }
        if (!(sAlignIntervalFromProvidor || (sAlignIntervalFromLocal ^ 1) == 0)) {
            sAlignInterval = ALIGN_INTERVAL;
            Log.d(TAG, "interval use default.");
        }
        if (!sAlignFirstDelayFromProvidor && (sAlignFirstDelayFromLocal ^ 1) != 0) {
            sAlignFirstDelay = 0;
            Log.d(TAG, "first delay use default.");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x003d A:{SYNTHETIC, Splitter: B:16:0x003d} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004f A:{SYNTHETIC, Splitter: B:22:0x004f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void getDataFromLocal() {
        Exception e;
        Throwable th;
        FileReader xmlReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            FileReader xmlReader2 = new FileReader(new File(Environment.getRootDirectory(), "oppo/alarm_filter_packages.xml"));
            if (xmlReader2 != null) {
                try {
                    parser.setInput(xmlReader2);
                    parseXml(parser, false);
                } catch (Exception e2) {
                    e = e2;
                    xmlReader = xmlReader2;
                    try {
                        Log.w(TAG, "getDataFromLocal: Got execption. ", e);
                        if (xmlReader == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (xmlReader != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    xmlReader = xmlReader2;
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e3) {
                            Log.w(TAG, "getDataFromLocal: Got execption close xmlReader. ", e3);
                        }
                    }
                    throw th;
                }
            }
            if (xmlReader2 != null) {
                try {
                    xmlReader2.close();
                } catch (IOException e32) {
                    Log.w(TAG, "getDataFromLocal: Got execption close xmlReader. ", e32);
                }
            }
            xmlReader = xmlReader2;
        } catch (Exception e4) {
            e = e4;
            Log.w(TAG, "getDataFromLocal: Got execption. ", e);
            if (xmlReader == null) {
                try {
                    xmlReader.close();
                } catch (IOException e322) {
                    Log.w(TAG, "getDataFromLocal: Got execption close xmlReader. ", e322);
                }
            }
        }
    }

    public static boolean isFilterRemovePackage(String pkg) {
        boolean contains;
        synchronized (sLock) {
            if (sDebug) {
                for (String removePackage : sRemoveFilterPackagesList) {
                    Log.d(TAG, "removePackage in sRemoveFilterPackagesList =  " + removePackage);
                }
            }
            contains = sRemoveFilterPackagesList.contains(pkg);
        }
        return contains;
    }

    public static void dump(PrintWriter pw) {
        synchronized (sLock) {
            int i;
            pw.println();
            pw.println("-----OppoAlarmManagerHelper-----");
            pw.println("sAlignInterval=" + sAlignInterval + "minutes");
            pw.println("sAlignFirstDelay=" + sAlignFirstDelay + "minutes");
            pw.println();
            pw.println("sUidWhiteList:");
            for (i = 0; i < sUidWhiteList.size(); i++) {
                pw.println((String) sUidWhiteList.get(i));
            }
            pw.println();
            pw.println("sKeyList:");
            for (i = 0; i < sKeyList.size(); i++) {
                pw.println((String) sKeyList.get(i));
            }
            pw.println();
            pw.println("sPkgWhiteList:");
            for (i = 0; i < sPkgWhiteList.size(); i++) {
                pw.println((String) sPkgWhiteList.get(i));
            }
            pw.println();
            pw.println("sRemoveFilterPackagesList:");
            for (i = 0; i < sRemoveFilterPackagesList.size(); i++) {
                pw.println((String) sRemoveFilterPackagesList.get(i));
            }
            pw.println();
            pw.println("sAlignEnforcedWhiteList:");
            for (i = 0; i < sAlignEnforcedWhiteList.size(); i++) {
                pw.println((String) sAlignEnforcedWhiteList.get(i));
            }
            pw.println();
            pw.println("sAlignWhiteList:");
            for (i = 0; i < sAlignWhiteList.size(); i++) {
                pw.println((String) sAlignWhiteList.get(i));
            }
            pw.println();
        }
    }

    public static boolean isInAlignWhiteList(String pkgName) {
        boolean contains;
        synchronized (sLock) {
            contains = sAlignWhiteList.contains(pkgName);
        }
        return contains;
    }

    public static boolean isInAlignEnforcedWhiteList(String pkgName) {
        boolean contains;
        synchronized (sLock) {
            contains = sAlignEnforcedWhiteList.contains(pkgName);
        }
        return contains;
    }

    public static boolean containKeyWord(String pkgName) {
        synchronized (sLock) {
            for (String key : sKeyList) {
                if (pkgName.toLowerCase().contains(key)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static long getAlignInterval() {
        return sAlignInterval;
    }

    public static long getAlignFirstDelay() {
        return sAlignFirstDelay;
    }
}
