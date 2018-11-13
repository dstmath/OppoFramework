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
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
public class OppoAlarmManagerHelper {
    private static final String ACTION_ROM_UPDATE_CONFIG_SUCCES = "oppo.intent.action.ROM_UPDATE_CONFIG_SUCCESS";
    private static final long ALIGN_INTERVAL = 5;
    private static final List<String> ALIGN_WHITE_LIST = null;
    private static final String COLUMN_NAME_1 = "version";
    private static final String COLUMN_NAME_2 = "xml";
    private static final Uri CONTENT_URI_WHITE_LIST = null;
    private static boolean DEBUG = false;
    private static final String FILTER_NAME = "sys_alarm_filterpackages_list";
    private static final String INEXACT_ALARM_FEATURE_NAME = "oppo.inexact.alarm";
    private static final List<String> KEYWORD_WHITE_LIST = null;
    private static boolean OPPODEBUG = false;
    private static final String OPPO_CUSTOMIZE_WHITE_FILE_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final List<String> PKG_WHITE_LIST = null;
    private static final List<String> PKG_WHITE_LIST_NOT_COVERED = null;
    private static final List<String> REMOVE_FILTER_LIST_NOT_COVERED = null;
    private static final List<String> REMOVE_FILTER_PKG_LIST = null;
    private static final String TAG = "OppoAlarmManagerHelper";
    private static final List<String> UID_WHITE_LIST = null;
    private static AlarmManagerService mAlarm;
    private static ArrayList<String> mAlignEnforcedWhiteList;
    private static long mAlignFirstDelay;
    private static boolean mAlignFirstDelayFromLocal;
    private static boolean mAlignFirstDelayFromProvidor;
    private static long mAlignInterval;
    private static boolean mAlignIntervalFromLocal;
    private static boolean mAlignIntervalFromProvidor;
    private static ArrayList<String> mAlignWhiteList;
    private static Context mContext;
    private static boolean mHaveAlarmFeature;
    private static AtomicBoolean mIsWhiteListInited;
    private static ArrayList<String> mKeyList;
    private static final Object mLock = null;
    private static ArrayList<String> mPkgWhiteList;
    private static ArrayList<String> mRemoveFilterPackagesList;
    private static ArrayList<String> mUidWhiteList;

    private static class GetDataFromProviderRunnable implements Runnable {
        public void run() {
            if (OppoAlarmManagerHelper.DEBUG) {
                Log.d(OppoAlarmManagerHelper.TAG, "start run ");
            }
            while (!ActivityManagerNative.isSystemReady()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.w(OppoAlarmManagerHelper.TAG, "sleep 100 ms is Interrupted because of " + e);
                }
                if (OppoAlarmManagerHelper.DEBUG) {
                    Log.d(OppoAlarmManagerHelper.TAG, "sleep 100 ms ");
                }
            }
            synchronized (OppoAlarmManagerHelper.mLock) {
                OppoAlarmManagerHelper.getDataFromProvider();
                OppoAlarmManagerHelper.getDataFromLocal();
                OppoAlarmManagerHelper.getDataDefault();
                OppoAlarmManagerHelper.addNotCoveredWhitelist();
                OppoAlarmManagerHelper.addCustomizeWhiteList();
                OppoAlarmManagerHelper.mIsWhiteListInited.set(true);
            }
            if (OppoAlarmManagerHelper.DEBUG) {
                Log.d(OppoAlarmManagerHelper.TAG, "isSystemReady is true  !!!!! ");
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.OppoAlarmManagerHelper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.OppoAlarmManagerHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.OppoAlarmManagerHelper.<clinit>():void");
    }

    public static void init(Context context, AlarmManagerService alarm) {
        synchronized (mLock) {
            mContext = context;
            mAlarm = alarm;
            mHaveAlarmFeature = mContext.getPackageManager().hasSystemFeature(INEXACT_ALARM_FEATURE_NAME);
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
        synchronized (mLock) {
            if (mHaveAlarmFeature && mIsWhiteListInited.get() && windowLength == 0) {
                if (DEBUG) {
                    Log.d(TAG, "windowLength == AlarmManager.WINDOW_EXACT");
                }
                if (isNeedInexactAlarm()) {
                    if (DEBUG) {
                        Log.d(TAG, "Using  inexact alarm!!!!!!!!");
                    }
                    return -1;
                } else if (DEBUG) {
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
                    ArrayList<String> changeList = intent.getStringArrayListExtra(WifiRomUpdateHelper.ROM_UPDATE_CONFIG_LIST);
                    if (changeList != null && changeList.contains(OppoAlarmManagerHelper.FILTER_NAME)) {
                        new Thread(new GetDataFromProviderRunnable(), "AlarmRomUpdate").start();
                        Log.d(OppoAlarmManagerHelper.TAG, "ACTION_ROM_UPDATE_CONFIG_SUCCES");
                    }
                }
            }
        }, filter);
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x00e2 A:{SYNTHETIC, Splitter: B:48:0x00e2} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a3 A:{SYNTHETIC, Splitter: B:40:0x00a3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void addCustomizeWhiteList() {
        Exception e;
        Throwable th;
        if (mContext.getPackageManager() != null && mContext.getPackageManager().hasSystemFeature("oppo.business.custom")) {
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
                                    if (!(pkgName == null || mPkgWhiteList.contains(pkgName))) {
                                        mPkgWhiteList.add(pkgName);
                                        if (OPPODEBUG) {
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
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        xmlReader = xmlReader2;
                        if (xmlReader != null) {
                            try {
                                xmlReader.close();
                            } catch (IOException e222) {
                                Log.e(TAG, "Failed to close state FileInputStream " + e222);
                            }
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
            if (OPPODEBUG) {
                Log.d(TAG, "addCustomizeWhiteList failed: file doesn't exist!");
            }
        }
    }

    private static void addNotCoveredWhitelist() {
        int i;
        String pkg;
        for (i = 0; i < PKG_WHITE_LIST_NOT_COVERED.size(); i++) {
            pkg = (String) PKG_WHITE_LIST_NOT_COVERED.get(i);
            if (!mPkgWhiteList.contains(pkg)) {
                mPkgWhiteList.add(pkg);
            }
        }
        for (i = 0; i < REMOVE_FILTER_LIST_NOT_COVERED.size(); i++) {
            pkg = (String) REMOVE_FILTER_LIST_NOT_COVERED.get(i);
            if (!mRemoveFilterPackagesList.contains(pkg)) {
                mRemoveFilterPackagesList.add(pkg);
            }
        }
    }

    private static boolean isNeedInexactAlarm() {
        if (checkUid() || checkPackage(mContext)) {
            return false;
        }
        return true;
    }

    private static boolean checkUid() {
        if (DEBUG) {
            for (String uidWhite : mUidWhiteList) {
                Log.d(TAG, "uid in mUidWhiteList =  " + uidWhite);
            }
        }
        int uid = Binder.getCallingUid();
        if (inUidWhiteList(uid)) {
            if (DEBUG) {
                Log.d(TAG, "checkUid uid == " + uid + " is inUidWhiteList!  using exact alarm!!!");
            }
            return true;
        }
        if (DEBUG) {
            Log.d(TAG, "This uid use inexact alarm !!!  uid == " + uid);
        }
        return false;
    }

    private static boolean checkPackage(Context context) {
        if (DEBUG) {
            for (String pkgWhite : mPkgWhiteList) {
                Log.d(TAG, "Pkg in mPkgWhiteList =  " + pkgWhite);
            }
            for (String keyWhite : mKeyList) {
                Log.d(TAG, "key in mKeyList == " + keyWhite);
            }
        }
        int uid = Binder.getCallingUid();
        String[] packages = mContext.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            Log.w(TAG, "invalid UID " + uid);
            return true;
        }
        for (String pkg : packages) {
            if (inPackageNameWhiteList(pkg)) {
                if (DEBUG) {
                    Log.d(TAG, "Pkg is inPackageNameWhiteList! using exact alarm!!!   pkg == " + pkg);
                }
                return true;
            }
            for (String key : mKeyList) {
                if (pkg.contains(key)) {
                    if (DEBUG) {
                        Log.d(TAG, "Packagename match key! using exact alarm!!!  pkg == " + pkg);
                    }
                    return true;
                }
            }
        }
        if (DEBUG) {
            for (String pkg1 : packages) {
                Log.d(TAG, "This package use inexact alarm !!!  pkg1 == " + pkg1);
            }
        }
        return false;
    }

    public static boolean inPackageNameWhiteList(String pkgName) {
        synchronized (mLock) {
            if (mPkgWhiteList == null || pkgName == null) {
                return false;
            }
            boolean contains = mPkgWhiteList.contains(pkgName);
            return contains;
        }
    }

    private static boolean inUidWhiteList(int uid) {
        if (mUidWhiteList == null) {
            return false;
        }
        return mUidWhiteList.contains(Integer.toString(uid));
    }

    private static void resetList() {
        mUidWhiteList.clear();
        mPkgWhiteList.clear();
        mRemoveFilterPackagesList.clear();
        mKeyList.clear();
        mAlignWhiteList.clear();
        mAlignEnforcedWhiteList.clear();
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
            mAlignIntervalFromProvidor = false;
            mAlignFirstDelayFromProvidor = false;
        } else {
            boolean needParse = false;
            if (mUidWhiteList.isEmpty()) {
                needParse = true;
            } else {
                parseUidWhiteArray = false;
            }
            if (mPkgWhiteList.isEmpty()) {
                needParse = true;
            } else {
                parsePkgWhiteArray = false;
            }
            if (mRemoveFilterPackagesList.isEmpty()) {
                needParse = true;
            } else {
                parseFilterPackagesArray = false;
            }
            if (mKeyList.isEmpty()) {
                needParse = true;
            } else {
                parseKeyArray = false;
            }
            if (mAlignWhiteList.isEmpty()) {
                needParse = true;
            } else {
                parseAlignWhiteArray = false;
            }
            if (mAlignEnforcedWhiteList.isEmpty()) {
                needParse = true;
            } else {
                parseAlignEnforcedWhiteArray = false;
            }
            if (mAlignIntervalFromProvidor) {
                parseAlignInterval = false;
            } else {
                needParse = true;
            }
            if (mAlignFirstDelayFromProvidor) {
                parseAlignFirstDelay = false;
            } else {
                needParse = true;
            }
            if (needParse) {
                if (parseUidWhiteArray && OPPODEBUG) {
                    Log.d(TAG, "parseXml: parse uid From Local.");
                }
                if (parsePkgWhiteArray && OPPODEBUG) {
                    Log.d(TAG, "parseXml: parse pkg From Local.");
                }
                if (parseFilterPackagesArray && OPPODEBUG) {
                    Log.d(TAG, "parseXml: parse remove From Local.");
                }
                if (parseKeyArray && OPPODEBUG) {
                    Log.d(TAG, "parseXml: parse key From Local.");
                }
                if (parseAlignWhiteArray && OPPODEBUG) {
                    Log.d(TAG, "parseXml: parse a1 From Local.");
                }
                if (parseAlignEnforcedWhiteArray && OPPODEBUG) {
                    Log.d(TAG, "parseXml: parse a2 From Local.");
                }
                if (parseAlignInterval && OPPODEBUG) {
                    Log.d(TAG, "parseXml: parse aI From Local.");
                }
                if (parseAlignFirstDelay && OPPODEBUG) {
                    Log.d(TAG, "parseXml: parse aFD From Local.");
                }
                mAlignIntervalFromLocal = false;
                mAlignFirstDelayFromLocal = false;
            } else {
                if (OPPODEBUG) {
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
                                                    if ("AlignEnforcedWhiteArray".equals(strName) && parseAlignEnforcedWhiteArray && !mAlignEnforcedWhiteList.contains(strText)) {
                                                        mAlignEnforcedWhiteList.add(strText);
                                                        break;
                                                    }
                                                } else if (!mAlignWhiteList.contains(strText)) {
                                                    mAlignWhiteList.add(strText);
                                                    break;
                                                }
                                            }
                                            try {
                                                mAlignFirstDelay = Long.parseLong(strText);
                                                if (updateFromDb) {
                                                    mAlignFirstDelayFromProvidor = true;
                                                } else {
                                                    mAlignFirstDelayFromLocal = true;
                                                }
                                            } catch (NumberFormatException e) {
                                                mAlignFirstDelay = 0;
                                                Log.w(TAG, "AlignFirstDelay excption.", e);
                                            }
                                            Log.d(TAG, "first delay=" + mAlignFirstDelay);
                                            break;
                                        }
                                        try {
                                            mAlignInterval = Long.parseLong(strText);
                                            if (updateFromDb) {
                                                mAlignIntervalFromProvidor = true;
                                            } else {
                                                mAlignIntervalFromLocal = true;
                                            }
                                        } catch (NumberFormatException e2) {
                                            mAlignInterval = ALIGN_INTERVAL;
                                            Log.w(TAG, "AlignInterval excption.", e2);
                                        }
                                        Log.d(TAG, "int=" + mAlignInterval);
                                        break;
                                    } else if (!mKeyList.contains(strText)) {
                                        mKeyList.add(strText);
                                        break;
                                    }
                                } else if (!mRemoveFilterPackagesList.contains(strText)) {
                                    mRemoveFilterPackagesList.add(strText);
                                    break;
                                }
                            } else if (!mPkgWhiteList.contains(strText)) {
                                mPkgWhiteList.add(strText);
                                break;
                            }
                        } else if (!mUidWhiteList.contains(strText)) {
                            mUidWhiteList.add(strText);
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
            String[] projection = new String[2];
            projection[0] = "version";
            projection[1] = COLUMN_NAME_2;
            cursor = mContext.getContentResolver().query(CONTENT_URI_WHITE_LIST, projection, "filtername=\"sys_alarm_filterpackages_list\"", null, null);
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
        if (mUidWhiteList.isEmpty()) {
            mUidWhiteList = new ArrayList(UID_WHITE_LIST);
            Log.d(TAG, "uid use default.");
        }
        if (mPkgWhiteList.isEmpty()) {
            mPkgWhiteList = new ArrayList(PKG_WHITE_LIST);
            Log.d(TAG, "pkg use default.");
        }
        if (mRemoveFilterPackagesList.isEmpty()) {
            mRemoveFilterPackagesList = new ArrayList(REMOVE_FILTER_PKG_LIST);
            Log.d(TAG, "remove use default.");
        }
        if (mKeyList.isEmpty()) {
            mKeyList = new ArrayList(KEYWORD_WHITE_LIST);
            Log.d(TAG, "key use default.");
        }
        if (mAlignWhiteList.isEmpty()) {
            mAlignWhiteList = new ArrayList(ALIGN_WHITE_LIST);
            Log.d(TAG, "align use default.");
        }
        if (!(mAlignIntervalFromProvidor || mAlignIntervalFromLocal)) {
            mAlignInterval = ALIGN_INTERVAL;
            Log.d(TAG, "interval use default.");
        }
        if (!mAlignFirstDelayFromProvidor && !mAlignFirstDelayFromLocal) {
            mAlignFirstDelay = 0;
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
            FileReader xmlReader2 = new FileReader(new File(Environment.getRootDirectory(), "etc/alarm_filter_packages.xml"));
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
        synchronized (mLock) {
            if (DEBUG) {
                for (String removePackage : mRemoveFilterPackagesList) {
                    Log.d(TAG, "removePackage in mRemoveFilterPackagesList =  " + removePackage);
                }
            }
            contains = mRemoveFilterPackagesList.contains(pkg);
        }
        return contains;
    }

    public static void dump(PrintWriter pw) {
        synchronized (mLock) {
            int i;
            pw.println();
            pw.println("-----OppoAlarmManagerHelper-----");
            pw.println("mAlignInterval=" + mAlignInterval + "minutes");
            pw.println("mAlignFirstDelay=" + mAlignFirstDelay + "minutes");
            pw.println();
            pw.println("mUidWhiteList:");
            for (i = 0; i < mUidWhiteList.size(); i++) {
                pw.println((String) mUidWhiteList.get(i));
            }
            pw.println();
            pw.println("mKeyList:");
            for (i = 0; i < mKeyList.size(); i++) {
                pw.println((String) mKeyList.get(i));
            }
            pw.println();
            pw.println("mPkgWhiteList:");
            for (i = 0; i < mPkgWhiteList.size(); i++) {
                pw.println((String) mPkgWhiteList.get(i));
            }
            pw.println();
            pw.println("mRemoveFilterPackagesList:");
            for (i = 0; i < mRemoveFilterPackagesList.size(); i++) {
                pw.println((String) mRemoveFilterPackagesList.get(i));
            }
            pw.println();
            pw.println("mAlignEnforcedWhiteList:");
            for (i = 0; i < mAlignEnforcedWhiteList.size(); i++) {
                pw.println((String) mAlignEnforcedWhiteList.get(i));
            }
            pw.println();
            pw.println("mAlignWhiteList:");
            for (i = 0; i < mAlignWhiteList.size(); i++) {
                pw.println((String) mAlignWhiteList.get(i));
            }
            pw.println();
        }
    }

    public static boolean isInAlignWhiteList(String pkgName) {
        boolean contains;
        synchronized (mLock) {
            contains = mAlignWhiteList.contains(pkgName);
        }
        return contains;
    }

    public static boolean isInAlignEnforcedWhiteList(String pkgName) {
        boolean contains;
        synchronized (mLock) {
            contains = mAlignEnforcedWhiteList.contains(pkgName);
        }
        return contains;
    }

    public static boolean containKeyWord(String pkgName) {
        synchronized (mLock) {
            for (String key : mKeyList) {
                if (pkgName.toLowerCase().contains(key)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static long getAlignInterval() {
        return mAlignInterval;
    }

    public static long getAlignFirstDelay() {
        return mAlignFirstDelay;
    }
}
