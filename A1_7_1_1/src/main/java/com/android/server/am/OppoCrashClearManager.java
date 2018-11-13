package com.android.server.am;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.Xml;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.oppo.IElsaManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
public class OppoCrashClearManager {
    private static final long CLEAR_MAP_TIME = 600000;
    public static final String CLEAR_TIME = "c";
    public static final String CRASH_CLEAR_NAME = "p";
    private static final long CRASH_CLEAR_TIMEOUT = 1800000;
    public static final int CRASH_CONUNT = 3;
    public static final String CRASH_COUNT = "n";
    public static final String CRASH_TIMEOUT = "o";
    private static final int DATA_CLEAR_MESSAGE = 1;
    public static final String FEATURE = "f";
    private static final String OPPO_CRASH_CLEAR_CONFIG_PATH = "/data/system/config/crashclear_white_list.xml";
    private static final String OPPO_CRASH_CLEAR_PATH = "/data/system/config";
    private static final String TAG = "OppoCrashClearManager";
    private static OppoCrashClearManager mOppoCrashCleanManager;
    private boolean isReady;
    private FileObserverPolicy mClearConfigFileObserver;
    private long mClearTime;
    private Context mContext;
    private long mCrashClearTimeout;
    public int mCrashCount;
    private boolean mFeature;
    private ClearHandler mHandler;
    private HandlerThread mHandlerThread;
    private long mLastClearMapTime;
    private final Object mObjectLock;
    public final HashMap<String, OppoAppStartInfo> mProcessCrashCount;
    private List<String> mSkipClearList;
    private List<String> skipCrashApp;

    private class ClearHandler extends Handler {
        public ClearHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            ProcessRecord processRecord = null;
            if (msg.obj != null) {
                processRecord = msg.obj;
            }
            OppoCrashClearManager.this.dataClearAlert(processRecord);
        }
    }

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(OppoCrashClearManager.OPPO_CRASH_CLEAR_CONFIG_PATH)) {
                Log.i(OppoCrashClearManager.TAG, "onEvent: focusPath = OPPO_CRASH_CLEAR_CONFIG_PATH");
                OppoCrashClearManager.this.readConfigFile();
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoCrashClearManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoCrashClearManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoCrashClearManager.<clinit>():void");
    }

    public OppoCrashClearManager() {
        this.mProcessCrashCount = new HashMap();
        this.mClearConfigFileObserver = null;
        this.mCrashCount = 3;
        this.mCrashClearTimeout = 1800000;
        this.mClearTime = 600000;
        this.mLastClearMapTime = -1;
        this.mFeature = true;
        this.isReady = false;
        this.mObjectLock = new Object();
        String[] strArr = new String[6];
        strArr[0] = "com.android.dialer";
        strArr[1] = "com.android.mms";
        strArr[2] = "com.android.mms.service";
        strArr[3] = "com.android.contacts";
        strArr[4] = "com.android.providers.telephony";
        strArr[5] = "com.android.providers.contacts";
        this.skipCrashApp = Arrays.asList(strArr);
        initDir();
        initFileObserver();
        readConfigFile();
    }

    private void initDir() {
        File crashClearFilePath = new File(OPPO_CRASH_CLEAR_PATH);
        File crashClearConfigPath = new File(OPPO_CRASH_CLEAR_CONFIG_PATH);
        try {
            if (!crashClearFilePath.exists()) {
                crashClearFilePath.mkdirs();
            }
            if (!crashClearConfigPath.exists()) {
                crashClearConfigPath.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "init crashClearConfigPath Dir failed!!!");
        }
    }

    protected void init(Context context) {
        this.mContext = context;
        this.isReady = true;
        this.mHandlerThread = new HandlerThread("oppocrashclear");
        this.mHandlerThread.start();
        this.mHandler = new ClearHandler(this.mHandlerThread.getLooper());
    }

    public static final OppoCrashClearManager getInstance() {
        if (mOppoCrashCleanManager == null) {
            mOppoCrashCleanManager = new OppoCrashClearManager();
        }
        return mOppoCrashCleanManager;
    }

    private void initFileObserver() {
        this.mClearConfigFileObserver = new FileObserverPolicy(OPPO_CRASH_CLEAR_CONFIG_PATH);
        this.mClearConfigFileObserver.startWatching();
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00cc A:{Catch:{ IOException -> 0x015b }} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00d2 A:{Catch:{ IOException -> 0x015b }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile() {
        Exception e;
        Throwable th;
        File xmlFile = new File(OPPO_CRASH_CLEAR_CONFIG_PATH);
        if (xmlFile.exists()) {
            FileReader fileReader = null;
            try {
                this.mSkipClearList = new ArrayList();
                XmlPullParser parser = Xml.newPullParser();
                try {
                    FileReader xmlReader = new FileReader(xmlFile);
                    try {
                        parser.setInput(xmlReader);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    if (!parser.getName().equals(CRASH_CLEAR_NAME)) {
                                        if (!parser.getName().equals(CRASH_COUNT)) {
                                            if (!parser.getName().equals(CRASH_TIMEOUT)) {
                                                if (!parser.getName().equals(FEATURE)) {
                                                    if (parser.getName().equals(CLEAR_TIME)) {
                                                        eventType = parser.next();
                                                        updateClearTime(parser.getAttributeValue(null, "att"));
                                                        break;
                                                    }
                                                }
                                                eventType = parser.next();
                                                updateFeature(parser.getAttributeValue(null, "att"));
                                                break;
                                            }
                                            eventType = parser.next();
                                            updateCrashTimeout(parser.getAttributeValue(null, "att"));
                                            break;
                                        }
                                        eventType = parser.next();
                                        updateCrashCount(parser.getAttributeValue(null, "att"));
                                        break;
                                    }
                                    eventType = parser.next();
                                    updateCrashClearName(parser.getAttributeValue(null, "att"));
                                    break;
                                    break;
                            }
                        }
                        try {
                            if (this.mSkipClearList.isEmpty()) {
                                this.mSkipClearList = this.skipCrashApp;
                            }
                            if (xmlReader != null) {
                                xmlReader.close();
                            }
                        } catch (IOException e2) {
                            Log.w(TAG, "Got execption close permReader.", e2);
                        }
                    } catch (Exception e3) {
                        e = e3;
                        fileReader = xmlReader;
                        try {
                            Log.w(TAG, "Got execption parsing permissions.", e);
                            try {
                                if (this.mSkipClearList.isEmpty()) {
                                    this.mSkipClearList = this.skipCrashApp;
                                }
                                if (fileReader != null) {
                                    fileReader.close();
                                }
                            } catch (IOException e22) {
                                Log.w(TAG, "Got execption close permReader.", e22);
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            try {
                                if (this.mSkipClearList.isEmpty()) {
                                    this.mSkipClearList = this.skipCrashApp;
                                }
                                if (fileReader != null) {
                                    fileReader.close();
                                }
                            } catch (IOException e222) {
                                Log.w(TAG, "Got execption close permReader.", e222);
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileReader = xmlReader;
                        if (this.mSkipClearList.isEmpty()) {
                        }
                        if (fileReader != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    Log.w(TAG, "Couldn't find or open alarm_filter_packages file " + xmlFile);
                    try {
                        if (this.mSkipClearList.isEmpty()) {
                            this.mSkipClearList = this.skipCrashApp;
                        }
                    } catch (IOException e2222) {
                        Log.w(TAG, "Got execption close permReader.", e2222);
                    }
                }
            } catch (Exception e5) {
                e = e5;
            }
        } else {
            this.mSkipClearList = this.skipCrashApp;
        }
    }

    public void updateCrashClearName(String tagName) {
        if (tagName != null && tagName != IElsaManager.EMPTY_PACKAGE) {
            this.mSkipClearList.add(tagName);
        }
    }

    public void updateCrashCount(String crashCount) {
        if (crashCount != null) {
            try {
                this.mCrashCount = Integer.parseInt(crashCount);
            } catch (NumberFormatException e) {
                this.mCrashCount = 3;
                Log.e(TAG, "updateCrashCount NumberFormatException: ", e);
            }
        }
    }

    public void updateCrashTimeout(String crashTimeout) {
        if (crashTimeout != null) {
            try {
                this.mCrashClearTimeout = Long.parseLong(crashTimeout);
            } catch (NumberFormatException e) {
                this.mCrashClearTimeout = 1800000;
                Log.e(TAG, "updateCrashTimeout NumberFormatException: ", e);
            }
        }
    }

    public void updateFeature(String feature) {
        if (feature != null) {
            try {
                this.mFeature = Boolean.parseBoolean(feature);
            } catch (Exception e) {
                this.mFeature = true;
                Log.e(TAG, "updateFeature NumberFormatException: ", e);
            }
        }
    }

    public void updateClearTime(String clearTime) {
        if (clearTime != null) {
            try {
                this.mClearTime = Long.parseLong(clearTime);
            } catch (Exception e) {
                this.mClearTime = 600000;
                Log.e(TAG, "updateClearTime NumberFormatException: ", e);
            }
        }
    }

    protected void collectCrashInfo(ProcessRecord app, String hostingType, ActivityRecord r) {
        if (!this.mFeature || r == null || !this.isReady || OppoSysStateManager.getInstance().isScreenOff()) {
            return;
        }
        if (app == null || app.info == null || !(this.mSkipClearList.contains(app.info.packageName) || OppoAbnormalAppManager.getInstance().inRestrictAppList(app.info.packageName))) {
            boolean isSystemApp = (app.info.flags & 1) != 0;
            long startTime = SystemClock.elapsedRealtime();
            if (isSystemApp || "activity".equals(hostingType)) {
                if (this.mLastClearMapTime < 0) {
                    this.mLastClearMapTime = startTime;
                }
                synchronized (this.mObjectLock) {
                    if (startTime > this.mLastClearMapTime + this.mCrashClearTimeout) {
                        try {
                            Iterator<String> iterator = this.mProcessCrashCount.keySet().iterator();
                            while (iterator.hasNext()) {
                                if (startTime > this.mClearTime + ((OppoAppStartInfo) this.mProcessCrashCount.get((String) iterator.next())).getFirstStartTime()) {
                                    iterator.remove();
                                }
                            }
                            this.mLastClearMapTime = -1;
                        } catch (Exception e) {
                            Log.w(TAG, "collectCrashInfo Exception: " + e);
                            this.mLastClearMapTime = -1;
                        } catch (Throwable th) {
                            this.mLastClearMapTime = -1;
                        }
                    }
                    OppoAppStartInfo crashInfo = (OppoAppStartInfo) this.mProcessCrashCount.get(app.info.processName);
                    if (crashInfo == null) {
                        crashInfo = new OppoAppStartInfo();
                        crashInfo.setFirstStartTime(startTime);
                        crashInfo.setProcessName(app.info.processName);
                        crashInfo.mLaunchedFromPackage = r.launchedFromPackage;
                        this.mProcessCrashCount.put(app.info.processName, crashInfo);
                    } else {
                        if (startTime > this.mClearTime + crashInfo.getFirstStartTime()) {
                            crashInfo.setCrashCount(0);
                        }
                        crashInfo.mLaunchedFromPackage = r.launchedFromPackage;
                        crashInfo.setFirstStartTime(startTime);
                    }
                }
            }
        }
    }

    protected void clearAppUserData(ProcessRecord app) {
        if (this.mFeature && !OppoSysStateManager.getInstance().isScreenOff() && app != null) {
            if (!app.isANR) {
                long now = SystemClock.elapsedRealtime();
                OppoAppStartInfo crashCountInfo = (OppoAppStartInfo) this.mProcessCrashCount.get(app.info.processName);
                if (crashCountInfo != null && isHomeProcess(crashCountInfo.mLaunchedFromPackage)) {
                    crashCountInfo.setCrashCount(crashCountInfo.getCrashCount() + 1);
                    if (crashCountInfo.getCrashCount() >= this.mCrashCount) {
                        boolean visb = false;
                        if (crashCountInfo.getFirstStartTime() < 0) {
                            visb = true;
                        } else if (now > crashCountInfo.getFirstStartTime() + 60000) {
                            visb = true;
                        }
                        if (!visb) {
                            sendDataClear(app);
                        }
                    }
                    crashCountInfo.setFirstStartTime(now);
                }
            }
            app.isANR = false;
        }
    }

    void sendDataClear(ProcessRecord app) {
        if (this.mHandler == null || this.mContext == null) {
            Log.i(TAG, "ClearHandler: mHandler is null!!!");
        } else if (app != null) {
            this.mHandler.removeMessages(1);
            Message mg = this.mHandler.obtainMessage(1);
            mg.obj = app;
            this.mHandler.sendMessage(mg);
        }
    }

    private void dataClearAlert(final ProcessRecord app) {
        if (this.mContext != null && app != null) {
            CharSequence name;
            Resources resources;
            Object[] objArr;
            AlertDialog dataClearAlert;
            ProcessRecord tmpApp = app;
            Builder builder = new Builder(this.mContext);
            if (app.pkgList.size() == 1) {
                name = this.mContext.getPackageManager().getApplicationLabel(app.info);
                if (name != null) {
                    resources = this.mContext.getResources();
                    objArr = new Object[2];
                    objArr[0] = name.toString();
                    objArr[1] = app.info.processName;
                    builder.setTitle(resources.getString(201590110, objArr));
                    builder.setPositiveButton(this.mContext.getResources().getString(201590111), new OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            OppoCrashClearManager.this.clearData(app);
                        }
                    });
                    builder.setNegativeButton(201590112, null);
                    dataClearAlert = builder.create();
                    dataClearAlert.getWindow().setType(2003);
                    dataClearAlert.setCancelable(false);
                    dataClearAlert.getWindow().getAttributes().ignoreHomeMenuKey = 1;
                    dataClearAlert.show();
                }
            }
            name = app.processName;
            resources = this.mContext.getResources();
            objArr = new Object[1];
            objArr[0] = name.toString();
            builder.setTitle(resources.getString(201590110, objArr));
            builder.setPositiveButton(this.mContext.getResources().getString(201590111), /* anonymous class already generated */);
            builder.setNegativeButton(201590112, null);
            dataClearAlert = builder.create();
            dataClearAlert.getWindow().setType(2003);
            dataClearAlert.setCancelable(false);
            dataClearAlert.getWindow().getAttributes().ignoreHomeMenuKey = 1;
            dataClearAlert.show();
        }
    }

    private void clearData(ProcessRecord app) {
        if (app != null && app.info != null && app.info.processName != null) {
            try {
                synchronized (this.mObjectLock) {
                    this.mProcessCrashCount.remove(app.info.processName);
                }
                IPackageManager pm = AppGlobals.getPackageManager();
                Log.i(TAG, "clearApplicationUserData more than 3 times app:" + app);
                pm.clearApplicationUserData(app.info.packageName, null, UserHandle.myUserId());
            } catch (Exception e) {
                Log.w(TAG, "Exception has crashed too many " + app, e);
            }
        }
    }

    private List<ResolveInfo> queryHomeResolveInfo() {
        Intent mHomeIntent = new Intent("android.intent.action.MAIN", null);
        mHomeIntent.addCategory("android.intent.category.HOME");
        return this.mContext.getPackageManager().queryIntentActivities(mHomeIntent, 270532608);
    }

    private boolean isHomeProcess(String packageName) {
        if (packageName == null || this.mContext == null) {
            return false;
        }
        List<ResolveInfo> mHomeList = queryHomeResolveInfo();
        if (mHomeList != null) {
            for (ResolveInfo ri : mHomeList) {
                if (packageName.equals(ri.activityInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
