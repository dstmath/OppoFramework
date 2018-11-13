package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.FileObserver;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.oppo.IElsaManager;
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class OppoInterceptWindow {
    private static boolean DEBUG_COLOROS = false;
    private static final String ENGINE_EXP_VERSION_FEATURE = "persist.sys.oppo.region";
    private static final String INTERCEPT_FEATURE = "open";
    private static final String OPPO_INTERCEPT_CONFIG = "/data/system/config/sys_wms_intercept_window.xml";
    private static final String OPPO_INTERCEPT_PATH = "/data/system/config";
    private static final String REGION = "CN";
    private static final String SKIP_NAME = "skip";
    private static final String TAG = "OppoInterceptWindow";
    private static OppoInterceptWindow mInstance;
    private static final Object mLock = null;
    private static final Object mSkipLock = null;
    private String mAudioPids;
    private FileObserverPolicy mConfigFileObserver;
    private boolean mExpVersion;
    private boolean mInterceptFeature;
    private boolean mOpenChage;
    private ArrayList<RunningAppProcessInfo> mRunProcList;
    protected List<String> mSkipNameList;
    private List<String> skipApp;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && this.focusPath.equals(OppoInterceptWindow.OPPO_INTERCEPT_CONFIG)) {
                Slog.i(OppoInterceptWindow.TAG, "onEvent: focusPath = OPPO_INTERCEPT_CONFIG");
                OppoInterceptWindow.this.readConfigFile();
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.OppoInterceptWindow.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.OppoInterceptWindow.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.OppoInterceptWindow.<clinit>():void");
    }

    public static OppoInterceptWindow getInstance() {
        OppoInterceptWindow oppoInterceptWindow;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new OppoInterceptWindow();
            }
            oppoInterceptWindow = mInstance;
        }
        return oppoInterceptWindow;
    }

    private OppoInterceptWindow() {
        boolean z;
        this.mConfigFileObserver = null;
        this.mInterceptFeature = true;
        this.mExpVersion = false;
        this.mOpenChage = false;
        this.mRunProcList = new ArrayList();
        this.mAudioPids = null;
        String[] strArr = new String[8];
        strArr[0] = "com.yuesuoping";
        strArr[1] = "com.tencent.mobileqq";
        strArr[2] = "com.tencent.mm";
        strArr[3] = "com.tencent.mobileqq/com.tencent.av.ui.VideoInviteLock";
        strArr[4] = "com.tencent.mobileqq/com.tencent.av.ui.AVActivity";
        strArr[5] = "com.android.calculator2";
        strArr[6] = "com.toprand.r11s";
        strArr[7] = "com.hunk.screen.ui";
        this.skipApp = Arrays.asList(strArr);
        if (SystemProperties.get(ENGINE_EXP_VERSION_FEATURE, REGION).equalsIgnoreCase(REGION)) {
            z = false;
        } else {
            z = true;
        }
        this.mExpVersion = z;
        if (this.mExpVersion) {
            this.mInterceptFeature = false;
        } else {
            this.mInterceptFeature = true;
        }
        initDir();
        readConfigFile();
        initFileObserver();
    }

    /* JADX WARNING: Missing block: B:2:0x0004, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void interceptWindow(Context context, Session session, LayoutParams attrs) {
        if (context != null && session != null && attrs != null && session.mUid >= 10000) {
            if (this.skipApp == null || !this.skipApp.contains(attrs.packageName)) {
                boolean showWhenLocked = (attrs.flags & DumpState.DUMP_FROZEN) != 0;
                boolean dismissKeyguard = (attrs.flags & 4194304) != 0;
                if (showWhenLocked || dismissKeyguard) {
                    if (showWhenLocked || dismissKeyguard) {
                        if (isSystemApp(context, attrs.packageName)) {
                            if (DEBUG_COLOROS) {
                                Slog.v(TAG, "isSystemApp pkg: " + attrs.packageName);
                            }
                        } else if (isAudioProcess(context, session, attrs.packageName)) {
                            if (DEBUG_COLOROS) {
                                Slog.v(TAG, "isAudioProcess pkg: " + attrs.packageName);
                            }
                        } else if (session == null || !OppoSysStateManager.getInstance().isSensorWorking(session.mUid)) {
                            if (showWhenLocked) {
                                if (DEBUG_COLOROS) {
                                    Slog.v(TAG, "interceptWindow showWhenLocked ... " + attrs.packageName);
                                }
                                attrs.flags &= -524289;
                            }
                            if (dismissKeyguard) {
                                if (DEBUG_COLOROS) {
                                    Slog.v(TAG, "interceptWindow dismissKeyguard ... " + attrs.packageName);
                                }
                                attrs.flags &= -4194305;
                            }
                        } else {
                            if (DEBUG_COLOROS) {
                                Slog.v(TAG, "isSensorWorking pkg: " + attrs.packageName);
                            }
                        }
                    }
                }
            }
        }
    }

    public void interceptWindow(Context context, WindowState win, WindowManagerPolicy policy) {
        if (this.mInterceptFeature && context != null && win != null && win.mAttrs != null) {
            int type = win.mAttrs.type;
            boolean startingWindow = win.mAttrs.type == 3;
            if (win.mSession != null && win.mSession.mUid < 10000 && !startingWindow) {
                return;
            }
            if (this.mSkipNameList != null && this.mSkipNameList.contains(win.mAttrs.packageName)) {
                return;
            }
            if (this.mSkipNameList == null || !this.mSkipNameList.contains(win.mLastTitle)) {
                boolean showWhenLocked = (win.mAttrs.flags & DumpState.DUMP_FROZEN) != 0;
                boolean dismissKeyguard = (win.mAttrs.flags & 4194304) != 0;
                if (showWhenLocked || dismissKeyguard) {
                    boolean showKeyguard = policy.isKeyguardShowingOrOccluded();
                    if (showKeyguard) {
                        if (showWhenLocked || dismissKeyguard) {
                            if (isSystemApp(context, win.getOwningPackage())) {
                                if (DEBUG_COLOROS) {
                                    Slog.v(TAG, "isSystemApp win: " + win);
                                }
                                return;
                            } else if (!startingWindow && isAudioProcess(context, win)) {
                                if (DEBUG_COLOROS) {
                                    Slog.v(TAG, "isAudioProcess win: " + win);
                                }
                                return;
                            } else if (startingWindow || win.mSession == null || !OppoSysStateManager.getInstance().isSensorWorking(win.mSession.mUid)) {
                                LayoutParams layoutParams;
                                if (showWhenLocked) {
                                    if (DEBUG_COLOROS) {
                                        Slog.v(TAG, "interceptWindow showWhenLocked ... " + win);
                                    }
                                    layoutParams = win.mAttrs;
                                    layoutParams.flags &= -524289;
                                }
                                if (dismissKeyguard) {
                                    if (DEBUG_COLOROS) {
                                        Slog.v(TAG, "interceptWindow dismissKeyguard ... " + win);
                                    }
                                    layoutParams = win.mAttrs;
                                    layoutParams.flags &= -4194305;
                                }
                            } else {
                                if (DEBUG_COLOROS) {
                                    Slog.v(TAG, "isSensorWorking win: " + win);
                                }
                                return;
                            }
                        }
                        return;
                    }
                    if (DEBUG_COLOROS) {
                        Slog.v(TAG, "interceptWindow showKeyguard ... " + showKeyguard);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0019, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:41:0x0077, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isAudioProcess(Context context, WindowState win) {
        if (win == null || win.mSession == null) {
            return false;
        }
        synchronized (mSkipLock) {
            if (this.mAudioPids == null || this.mAudioPids.length() == 0) {
            } else {
                String[] strPoint = this.mAudioPids.split(":");
                if (strPoint != null) {
                    for (int i = 0; i < strPoint.length; i++) {
                        if (!strPoint[i].isEmpty()) {
                            if (strPoint[i].equals(Integer.toString(win.mSession.mPid))) {
                                return true;
                            } else if (this.mRunProcList != null) {
                                for (int j = 0; j < this.mRunProcList.size(); j++) {
                                    RunningAppProcessInfo appInfo = (RunningAppProcessInfo) this.mRunProcList.get(j);
                                    if (strPoint[i].equals(Integer.toString(appInfo.pid)) && win.mSession.mUid == appInfo.uid) {
                                        return true;
                                    }
                                }
                                continue;
                            } else {
                                continue;
                            }
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x001a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isAudioProcess(Context context, Session session, String pkg) {
        String pids = ((AudioManager) context.getSystemService("audio")).getParameters("get_pid");
        if (pids == null || pids.length() == 0 || session == null) {
            return false;
        }
        String[] strPoint = pids.split(":");
        if (strPoint != null) {
            ArrayList<RunningAppProcessInfo> appList = getRunningAppList(context);
            for (int i = 0; i < strPoint.length; i++) {
                if (!strPoint[i].isEmpty()) {
                    if (strPoint[i].equals(Integer.toString(session.mPid))) {
                        return true;
                    }
                    if (appList != null) {
                        for (int j = 0; j < appList.size(); j++) {
                            RunningAppProcessInfo appInfo = (RunningAppProcessInfo) appList.get(j);
                            if (strPoint[i].equals(Integer.toString(appInfo.pid)) && session.mUid == appInfo.uid) {
                                return true;
                            }
                        }
                        continue;
                    } else {
                        continue;
                    }
                }
            }
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:7:0x0012, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSystemApp(Context context, String pkg) {
        if (pkg != null) {
            try {
                PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(pkg, 0);
                if (!(pkgInfo == null || pkgInfo.applicationInfo == null || (pkgInfo.applicationInfo.flags & 1) == 0)) {
                    return true;
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    protected ArrayList<RunningAppProcessInfo> getRunningAppList(Context context) {
        return (ArrayList) ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void getRunningAppList(Context context, Session session, LayoutParams attrs, WindowManagerPolicy policy) {
        if (this.mInterceptFeature && context != null && session != null && attrs != null && session.mUid >= 10000) {
            boolean showWhenLocked = (attrs.flags & DumpState.DUMP_FROZEN) != 0;
            boolean dismissKeyguard = (attrs.flags & 4194304) != 0;
            if (showWhenLocked || dismissKeyguard) {
                boolean showKeyguard = policy.isKeyguardShowingOrOccluded();
                if (showKeyguard) {
                    ArrayList<RunningAppProcessInfo> procList = (ArrayList) ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
                    synchronized (mSkipLock) {
                        this.mRunProcList.clear();
                        this.mRunProcList.addAll(procList);
                        this.mAudioPids = ((AudioManager) context.getSystemService("audio")).getParameters("get_pid");
                    }
                    return;
                }
                if (DEBUG_COLOROS) {
                    Slog.v(TAG, "getRunningAppList showKeyguard ... " + showKeyguard);
                }
            }
        }
    }

    private void initDir() {
        File interceptPath = new File(OPPO_INTERCEPT_PATH);
        File interceptConfig = new File(OPPO_INTERCEPT_CONFIG);
        try {
            if (!interceptPath.exists()) {
                interceptPath.mkdirs();
            }
            if (!interceptConfig.exists()) {
                interceptConfig.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "init interceptConfig Dir failed!!!");
        }
    }

    private void initFileObserver() {
        this.mConfigFileObserver = new FileObserverPolicy(OPPO_INTERCEPT_CONFIG);
        this.mConfigFileObserver.startWatching();
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x00ef A:{Catch:{ IOException -> 0x0152 }} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0108 A:{Catch:{ IOException -> 0x0152 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFile() {
        Exception e;
        Throwable th;
        File xmlFile = new File(OPPO_INTERCEPT_CONFIG);
        if (xmlFile.exists()) {
            FileReader fileReader = null;
            this.mOpenChage = false;
            try {
                this.mSkipNameList = new ArrayList();
                XmlPullParser parser = Xml.newPullParser();
                try {
                    FileReader xmlReader = new FileReader(xmlFile);
                    try {
                        parser.setInput(xmlReader);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    if (!parser.getName().equals(SKIP_NAME)) {
                                        if (parser.getName().equals(INTERCEPT_FEATURE)) {
                                            eventType = parser.next();
                                            updateFeature(parser.getAttributeValue(null, "att"));
                                            break;
                                        }
                                    }
                                    eventType = parser.next();
                                    updateListName(parser.getAttributeValue(null, "att"), this.mSkipNameList);
                                    break;
                                    break;
                            }
                        }
                        try {
                            if (!this.mOpenChage) {
                                if (this.mExpVersion) {
                                    this.mInterceptFeature = false;
                                } else {
                                    this.mInterceptFeature = true;
                                }
                            }
                            if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                                this.mSkipNameList = this.skipApp;
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
                                if (!this.mOpenChage) {
                                    if (this.mExpVersion) {
                                        this.mInterceptFeature = false;
                                    } else {
                                        this.mInterceptFeature = true;
                                    }
                                }
                                if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                                    this.mSkipNameList = this.skipApp;
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
                                if (!this.mOpenChage) {
                                }
                                this.mSkipNameList = this.skipApp;
                                if (fileReader != null) {
                                }
                            } catch (IOException e222) {
                                Slog.w(TAG, "Got execption close permReader.", e222);
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileReader = xmlReader;
                        if (this.mOpenChage) {
                            if (this.mExpVersion) {
                                this.mInterceptFeature = false;
                            } else {
                                this.mInterceptFeature = true;
                            }
                        }
                        if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                            this.mSkipNameList = this.skipApp;
                        }
                        if (fileReader != null) {
                            fileReader.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    Slog.w(TAG, "Couldn't find or open sys_wms_intercept_window file " + xmlFile);
                    try {
                        if (!this.mOpenChage) {
                            if (this.mExpVersion) {
                                this.mInterceptFeature = false;
                            } else {
                                this.mInterceptFeature = true;
                            }
                        }
                        if (this.mSkipNameList != null && this.mSkipNameList.isEmpty()) {
                            this.mSkipNameList = this.skipApp;
                        }
                    } catch (IOException e2222) {
                        Slog.w(TAG, "Got execption close permReader.", e2222);
                    }
                }
            } catch (Exception e5) {
                e = e5;
            }
        } else {
            this.mSkipNameList = this.skipApp;
        }
    }

    private void updateListName(String tagName, List<String> list) {
        if (tagName != null && tagName != IElsaManager.EMPTY_PACKAGE && list != null) {
            list.add(tagName);
        }
    }

    private void updateFeature(String feature) {
        if (feature != null) {
            try {
                this.mInterceptFeature = Boolean.parseBoolean(feature);
                this.mOpenChage = true;
            } catch (NumberFormatException e) {
                if (this.mExpVersion) {
                    this.mInterceptFeature = false;
                } else {
                    this.mInterceptFeature = true;
                }
                Slog.e(TAG, "updateFeature NumberFormatException: ", e);
            }
        }
    }
}
