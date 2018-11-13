package com.android.server.am;

import android.content.Context;
import android.content.Intent;
import android.os.FileObserver;
import android.os.FileUtils;
import android.util.Slog;
import android.util.Xml;
import com.android.server.coloros.OppoListManager;
import com.android.server.oppo.IElsaManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
public class OppoProtectEyeManagerService {
    private static boolean DEBUG = false;
    private static OppoProtectEyeManagerService OppoProtectEyeManagerService = null;
    private static final String PROTECT_EYE_DIR = "/data/system/protecteye";
    private static final String PROTECT_EYE_FEATURE_NAME = "oppo.eye.protection.support";
    private static final String PROTECT_EYE_STATUS_PATH = "/data/system/protecteye/status.xml";
    private static final String PROTECT_EYE_WHITE_PATH = "/data/system/protecteye/whitelist.xml";
    private static final String TAG = "OppoProtectEyeManagerService";
    private static List<ActivityChangedListener> mActivityChangedListenerList;
    private static boolean mHaveProtectEyeFeature;
    private boolean mProtectEyeStatus;
    private FileObserverPolicy mProtectEyeStatusFileObserver;

    public interface ActivityChangedListener {
        void onActivityChanged(String str, String str2);
    }

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 2);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 2 && this.focusPath.equals(OppoProtectEyeManagerService.PROTECT_EYE_STATUS_PATH)) {
                OppoProtectEyeManagerService.this.loadStatus();
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoProtectEyeManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoProtectEyeManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoProtectEyeManagerService.<clinit>():void");
    }

    public void init(Context context) {
        mHaveProtectEyeFeature = context.getPackageManager().hasSystemFeature(PROTECT_EYE_FEATURE_NAME);
        Slog.d(TAG, "mHaveProtectEyeFeature  == " + mHaveProtectEyeFeature);
        if (mHaveProtectEyeFeature) {
            initDir();
            loadStatus();
            this.mProtectEyeStatusFileObserver = new FileObserverPolicy(PROTECT_EYE_STATUS_PATH);
            this.mProtectEyeStatusFileObserver.startWatching();
        }
    }

    protected OppoProtectEyeManagerService() {
        this.mProtectEyeStatus = false;
    }

    public static final OppoProtectEyeManagerService getInstance() {
        if (OppoProtectEyeManagerService == null) {
            OppoProtectEyeManagerService = new OppoProtectEyeManagerService();
        }
        return OppoProtectEyeManagerService;
    }

    public boolean getProtectEyeEnable() {
        return this.mProtectEyeStatus;
    }

    public void handleProtectEyeMode(Context context, ActivityRecord prev, ActivityRecord next) {
        if (next == null) {
            if (DEBUG) {
                Slog.d(TAG, "handleProtectEyeMode next == null");
            }
            return;
        }
        boolean isPreMultiApp = false;
        boolean isNextMultiApp = false;
        if (next.userId == OppoMultiAppManager.USER_ID) {
            isNextMultiApp = true;
        }
        String prevActivity = IElsaManager.EMPTY_PACKAGE;
        String nextActivity = IElsaManager.EMPTY_PACKAGE;
        if (!(next.info == null || next.info.name == null)) {
            nextActivity = next.info.name;
        }
        if (prev == null) {
            if (DEBUG) {
                Slog.d(TAG, "handleProtectEyeMode: prev == null");
            }
            handleActivityChanged(context, next.packageName, IElsaManager.EMPTY_PACKAGE, prevActivity, nextActivity, false, isNextMultiApp);
        } else if (next.packageName == null) {
            if (DEBUG) {
                Slog.d(TAG, "handleProtectEyeMode: next.packageName == null");
            }
        } else {
            if (!(prev.info == null || prev.info.name == null)) {
                prevActivity = prev.info.name;
            }
            if (prev.userId == OppoMultiAppManager.USER_ID) {
                isPreMultiApp = true;
            }
            if (prev.packageName == null) {
                if (DEBUG) {
                    Slog.d(TAG, "handleProtectEyeMode: prev.packageName == null");
                }
                handleActivityChanged(context, next.packageName, IElsaManager.EMPTY_PACKAGE, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp);
            } else if (!next.packageName.equals(prev.packageName) || OppoListManager.getInstance().isRedundentActivity(prevActivity) || OppoListManager.getInstance().isRedundentActivity(nextActivity)) {
                handleActivityChanged(context, next.packageName, prev.packageName, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp);
            }
        }
    }

    private void handleActivityChanged(Context context, String nextPkgName, String prePkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp) {
        sendProtectEyeBroadcast(context, nextPkgName, prePkgName, prevActivity, nextActivity);
        onActivityChanged(prePkgName, nextPkgName);
        OppoProcessManager.getInstance().handleApplicationSwitch(prePkgName, nextPkgName);
        OppoGameSpaceManager.getInstance().handleApplicationSwitch(prePkgName, nextPkgName, prevActivity, nextActivity, isPreMultiApp, isNextMultiApp);
    }

    public void sendProtectEyeBroadcast(Context context, String nextPkgName, String prePkgName, String prevActivity, String nextActivity) {
        if (nextPkgName == null) {
            Slog.d(TAG, "handleProtectEyeMode: nextPkgName == null");
            nextPkgName = IElsaManager.EMPTY_PACKAGE;
        }
        if (DEBUG) {
            Slog.d(TAG, "sendProtectEyeBroadcast: prePkgName = " + prePkgName + "    nextPkgName = " + nextPkgName);
        }
        Intent intent = new Intent("android.intent.action.OPPO_ROM_APP_CHANGE");
        intent.putExtra("next_app_pkgname", nextPkgName);
        intent.putExtra("pre_app_pkgname", prePkgName);
        if (OppoListManager.getInstance().isRedundentActivity(prevActivity) || OppoListManager.getInstance().isRedundentActivity(nextActivity)) {
            intent.putExtra("pre_activity", prevActivity);
            intent.putExtra("next_activity", nextActivity);
        }
        context.sendBroadcast(intent);
    }

    private void onActivityChanged(String prePkg, String nextPkg) {
        if (mActivityChangedListenerList != null) {
            for (ActivityChangedListener listener : mActivityChangedListenerList) {
                if (listener != null) {
                    listener.onActivityChanged(prePkg, nextPkg);
                }
            }
        }
    }

    public static void setActivityChangedListener(ActivityChangedListener activityChangedListener) {
        Slog.d(TAG, "setActivityChangedListener");
        mActivityChangedListenerList.add(activityChangedListener);
    }

    public static void removeActivityChangedListener(ActivityChangedListener activityChangedListener) {
        mActivityChangedListenerList.remove(activityChangedListener);
    }

    private void changeMod() {
        try {
            Runtime.getRuntime().exec("chmod 744 /data/system/protecteye");
            Runtime.getRuntime().exec("chmod 777 /data/system/protecteye/status.xml");
            Runtime.getRuntime().exec("chmod 777 /data/system/protecteye/whitelist.xml");
        } catch (IOException e) {
            Slog.w(TAG, " " + e);
        }
    }

    private void initDir() {
        try {
            File file = new File(PROTECT_EYE_DIR);
            if (!file.exists()) {
                file.mkdirs();
            }
            copyFile("/system/protecteye/status.xml", PROTECT_EYE_STATUS_PATH);
            copyFile("/system/protecteye/whitelist.xml", PROTECT_EYE_WHITE_PATH);
            changeMod();
        } catch (Exception e) {
            Slog.w(TAG, "mkdir failed " + e);
        }
    }

    private void copyFile(String fromFile, String toFile) throws IOException {
        File targetFile = new File(toFile);
        if (!targetFile.exists()) {
            FileUtils.copyFile(new File(fromFile), targetFile);
        }
    }

    private void loadStatus() {
        try {
            FileInputStream stream = new FileInputStream(new File(PROTECT_EYE_STATUS_PATH));
            readStatusFromFile(stream);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to close state FileInputStream " + e);
                }
            }
        } catch (FileNotFoundException e2) {
            Slog.w(TAG, "Failed to read state: " + e2);
        }
    }

    private void readStatusFromFile(FileInputStream stream) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            int type;
            do {
                type = parser.next();
                if (type == 2) {
                    if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                        String sts = parser.getAttributeValue(null, "att");
                        if (sts == null) {
                            continue;
                        } else {
                            if (sts.equals("true")) {
                                this.mProtectEyeStatus = true;
                            } else {
                                this.mProtectEyeStatus = false;
                            }
                            Slog.d(TAG, "mProtectEyeStatus == " + this.mProtectEyeStatus);
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            } while (type != 1);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (NullPointerException e2) {
            Slog.w(TAG, "failed parsing ", e2);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (NumberFormatException e4) {
            Slog.w(TAG, "failed parsing ", e4);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
        } catch (XmlPullParserException e5) {
            Slog.w(TAG, "failed parsing ", e5);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
        } catch (IOException e3222) {
            Slog.w(TAG, "failed parsing ", e3222);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e32222) {
                    e32222.printStackTrace();
                }
            }
        } catch (IndexOutOfBoundsException e6) {
            Slog.w(TAG, "failed parsing ", e6);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e322222) {
                    e322222.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3222222) {
                    e3222222.printStackTrace();
                }
            }
        }
    }
}
