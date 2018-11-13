package com.android.server.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.FileObserver;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.coloros.OppoListManager;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class OppoAutostartManager {
    private static final String ACTION_WHITE_FILE_PATH = "/data/oppo/boot/broadcast_action_white.txt";
    public static boolean DEBUG_DETAIL = false;
    private static final String DIR = "//data//oppo//boot//";
    private static final String FILE_BOOT_OPTION = "//data//oppo//boot//bootoption.txt";
    private static final String KEY_PERMISSION_PROPERTIES = "persist.sys.permission.enable";
    private static final String PACKAGE_REMOVED_ADDED_FILTER = "/data/oppo/boot/broadcastlist.xml";
    private static final String PATH = "/data/oppo/boot/bootoption.txt";
    private static final String SAVE_FILE_NAME = "bootoption.txt";
    public static final String SKIP_FEATURE = "nodialog_switch";
    public static final String SKIP_NAME = "nodialog";
    private static final String TAG = "OppoAutostartManager";
    private static List<String> compareList;
    private static File file;
    private static BootFileListener listener;
    private static final Object mActionWhiteLock = null;
    private static BootFileListener mBroadActionFileObserver;
    private static BroadcastFileListener mBroadcastFileListener;
    private static File mBroadcatFilterFile;
    private static OppoAutostartManager mOppoAutostartManager;
    private static List<String> mWidgetActionList;
    private List<String> mBroadcastFilterList;
    private final Object mBroadcastLock;
    private List<String> mLocalFilterList;
    private boolean mSkipFeature;

    public class BootFileListener extends FileObserver {
        private String focusPath;

        public BootFileListener(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event != 8) {
                return;
            }
            if (this.focusPath.equals(OppoAutostartManager.PATH)) {
                OppoAutostartManager.this.getBootList(OppoAutostartManager.file);
            } else if (this.focusPath.equals(OppoAutostartManager.ACTION_WHITE_FILE_PATH)) {
                OppoAutostartManager.this.updateActionWhiteList();
            }
        }
    }

    public class BroadcastFileListener extends FileObserver {
        public BroadcastFileListener(String path) {
            super(path, 8);
        }

        public void onEvent(int event, String path) {
            switch (event) {
                case 8:
                    OppoAutostartManager.this.readBroadcastFilter();
                    return;
                default:
                    return;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAutostartManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAutostartManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoAutostartManager.<clinit>():void");
    }

    public OppoAutostartManager() {
        this.mSkipFeature = true;
        this.mBroadcastLock = new Object();
        String[] strArr = new String[5];
        strArr[0] = "com.lbe.security";
        strArr[1] = "com.tencent.qqpimsecure";
        strArr[2] = "com.qihoo360.mobilesafe";
        strArr[3] = "com.cleanmaster.mguard_cn";
        strArr[4] = "com.cleanmaster.security_cn";
        this.mLocalFilterList = Arrays.asList(strArr);
        file = new File(FILE_BOOT_OPTION);
        mBroadcatFilterFile = new File(PACKAGE_REMOVED_ADDED_FILTER);
        if (!file.exists()) {
            Slog.v(TAG, "file.exists() is not exit!");
            new File(DIR).mkdirs();
            file = new File(FILE_BOOT_OPTION);
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                Slog.e(TAG, "File creation failed " + ioe.getMessage());
            }
        } else if (mBroadcatFilterFile.exists()) {
            Slog.v(TAG, "file.exists() is  exit!");
        } else if (file.exists()) {
            try {
                mBroadcatFilterFile.createNewFile();
            } catch (IOException ioe2) {
                Slog.e(TAG, "FilterFile creation failed " + ioe2.getMessage());
            }
        }
        listener = new BootFileListener(PATH);
        listener.startWatching();
        mBroadcastFileListener = new BroadcastFileListener(PACKAGE_REMOVED_ADDED_FILTER);
        mBroadcastFileListener.startWatching();
        File actionWhiteFile = new File(ACTION_WHITE_FILE_PATH);
        try {
            if (!actionWhiteFile.exists()) {
                actionWhiteFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBroadActionFileObserver = new BootFileListener(ACTION_WHITE_FILE_PATH);
        mBroadActionFileObserver.startWatching();
    }

    public static final OppoAutostartManager getInstance() {
        if (mOppoAutostartManager == null) {
            mOppoAutostartManager = new OppoAutostartManager();
        }
        return mOppoAutostartManager;
    }

    public void initBootList(boolean clear) {
        if (!(!clear || compareList == null || compareList.size() == 0)) {
            compareList.clear();
        }
        getBootList(file);
        readBroadcastFilter();
        updateActionWhiteList();
    }

    public static final void changeAutobootist(String packageName) {
        if (compareList.contains(packageName)) {
            compareList.remove(packageName);
        }
    }

    public static final boolean checkAutoBootForbiddenStart(BroadcastQueue queue, ResolveInfo info, BroadcastRecord r) {
        if (!SystemProperties.getBoolean(KEY_PERMISSION_PROPERTIES, true) || processCanStart(info.activityInfo)) {
            return false;
        }
        if (r.callingUid >= 10000) {
            if (r.callerApp == null) {
                Slog.w(TAG, "r.callerApp == null!!!!");
                return false;
            } else if (r.callerApp.info != null && (r.callerApp.info.flags & 1) == 0) {
                return false;
            }
        }
        if (OppoListManager.getInstance().isInstalledAppWidget(info.activityInfo.applicationInfo.packageName)) {
            return false;
        }
        if (r.intent != null) {
            String action = r.intent.getAction();
            synchronized (mActionWhiteLock) {
                if (action != null) {
                    if (mWidgetActionList.contains(action)) {
                        Slog.w(TAG, action + " in WidgetActionList");
                        return false;
                    }
                }
            }
        }
        if (DEBUG_DETAIL) {
            Slog.w(TAG, "*Do not want to launch app " + info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.applicationInfo.uid + " for broadcast " + r.intent + " callUid:" + r.callingUid + " callPid:" + r.callingPid);
        }
        queue.finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, true);
        queue.scheduleBroadcastsLocked();
        return true;
    }

    private static boolean processCanStart(ActivityInfo info) {
        boolean res = true;
        if (info == null) {
            return false;
        }
        if (compareList.contains(info.packageName)) {
            res = false;
        }
        return res;
    }

    private void getBootList(File file) {
        if (file.exists()) {
            if (!(compareList == null || compareList.size() == 0)) {
                compareList.clear();
            }
            try {
                FileReader fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr);
                while (true) {
                    String strT = reader.readLine();
                    if (strT != null) {
                        compareList.add(strT.trim());
                    } else {
                        reader.close();
                        fr.close();
                        return;
                    }
                }
            } catch (Exception e) {
                Slog.v(TAG, "error:" + e);
                return;
            }
        }
        Slog.v(TAG, "bootoption  file not exists!");
    }

    private void updateActionWhiteList() {
        File file = new File(ACTION_WHITE_FILE_PATH);
        if (file.exists()) {
            List<String> actionList = new ArrayList();
            try {
                FileReader fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr);
                while (true) {
                    String strT = reader.readLine();
                    if (strT == null) {
                        break;
                    }
                    actionList.add(strT.trim());
                }
                reader.close();
                fr.close();
            } catch (Exception e) {
                Log.e(TAG, "associateStartFile read execption: " + e);
            }
            if (!actionList.isEmpty()) {
                synchronized (mActionWhiteLock) {
                    mWidgetActionList.clear();
                    mWidgetActionList.addAll(actionList);
                    Slog.v(TAG, "update broadcast action " + mWidgetActionList);
                }
            }
            return;
        }
        Slog.e(TAG, "updateActionWhiteList failed: file doesn't exist!");
    }

    public boolean skipSpecialBroadcast(String packageName, Intent intent) {
        boolean result = false;
        if (!this.mSkipFeature || intent == null || intent.getAction() == null || packageName == null) {
            return false;
        }
        if ((intent.getAction().equals("android.intent.action.PACKAGE_REMOVED") || intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) && isInFilterList(packageName)) {
            Slog.v(TAG, "skip broadcast package: " + packageName);
            result = true;
        }
        return result;
    }

    private boolean isInFilterList(String pkgName) {
        if (this.mBroadcastFilterList == null) {
            return false;
        }
        synchronized (this.mBroadcastLock) {
            Slog.v(TAG, "isInFilterList pkgName==" + pkgName);
            for (int i = 0; i < this.mBroadcastFilterList.size(); i++) {
                Slog.v(TAG, "isInFilterList mBroadcastFilterList==" + ((String) this.mBroadcastFilterList.get(i)));
            }
            if (this.mBroadcastFilterList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:63:0x00d2 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x00e1 A:{Catch:{ IOException -> 0x011b }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readBroadcastFilter() {
        Exception e;
        Throwable th;
        File xmlFile = new File(PACKAGE_REMOVED_ADDED_FILTER);
        if (xmlFile.exists()) {
            FileReader fileReader = null;
            try {
                this.mBroadcastFilterList = new ArrayList();
                XmlPullParser parser = Xml.newPullParser();
                try {
                    FileReader xmlReader = new FileReader(xmlFile);
                    try {
                        parser.setInput(xmlReader);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    if (!parser.getName().equals(SKIP_NAME)) {
                                        if (parser.getName().equals(SKIP_FEATURE)) {
                                            eventType = parser.next();
                                            updateSkipFeature(parser.getAttributeValue(null, "att"));
                                            break;
                                        }
                                    }
                                    eventType = parser.next();
                                    updateBroadcastFilterList(parser.getAttributeValue(null, "att"));
                                    break;
                                    break;
                            }
                        }
                        try {
                            synchronized (this.mBroadcastLock) {
                                if (this.mBroadcastFilterList.isEmpty()) {
                                    this.mBroadcastFilterList = this.mLocalFilterList;
                                }
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
                                synchronized (this.mBroadcastLock) {
                                    if (this.mBroadcastFilterList.isEmpty()) {
                                        this.mBroadcastFilterList = this.mLocalFilterList;
                                    }
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
                                synchronized (this.mBroadcastLock) {
                                    if (this.mBroadcastFilterList.isEmpty()) {
                                        this.mBroadcastFilterList = this.mLocalFilterList;
                                    }
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
                        synchronized (this.mBroadcastLock) {
                        }
                        if (fileReader != null) {
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    Log.w(TAG, "Couldn't find or open alarm_filter_packages file " + xmlFile);
                    try {
                        synchronized (this.mBroadcastLock) {
                            if (this.mBroadcastFilterList.isEmpty()) {
                                this.mBroadcastFilterList = this.mLocalFilterList;
                            }
                        }
                    } catch (IOException e2222) {
                        Log.w(TAG, "Got execption close permReader.", e2222);
                    }
                }
            } catch (Exception e5) {
                e = e5;
            }
        } else {
            this.mBroadcastFilterList = this.mLocalFilterList;
        }
    }

    public void updateBroadcastFilterList(String tagName) {
        synchronized (this.mBroadcastLock) {
            if (tagName != null) {
                if (tagName != IElsaManager.EMPTY_PACKAGE) {
                    this.mBroadcastFilterList.add(tagName);
                }
            }
        }
    }

    public void updateSkipFeature(String feature) {
        if (feature != null) {
            try {
                this.mSkipFeature = Boolean.parseBoolean(feature);
            } catch (NumberFormatException e) {
                this.mSkipFeature = true;
                Log.e(TAG, "updateSkipFeature NumberFormatException: ", e);
            }
        }
    }
}
