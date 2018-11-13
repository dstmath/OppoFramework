package com.mediatek.server.am.AutoBootControl;

import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.ResolveInfo;
import android.os.IUserManager;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import java.util.Iterator;
import java.util.List;

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
public class ReceiverController {
    static final boolean DEBUG = false;
    static final String TAG = "ReceiverController";
    private static Context mContext;
    private static boolean mMonitorEnabled;
    private static ReceiverController sInstance;
    private BootReceiverPolicy mBootReceiverPolicy;
    private final PackageMonitor mPackageMonitor;
    private ReceiverRecordHelper mRecordHelper;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.server.am.AutoBootControl.ReceiverController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.server.am.AutoBootControl.ReceiverController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.server.am.AutoBootControl.ReceiverController.<clinit>():void");
    }

    public static ReceiverController getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ReceiverController(context);
        }
        return sInstance;
    }

    private ReceiverController(Context context) {
        this.mRecordHelper = null;
        this.mBootReceiverPolicy = null;
        this.mPackageMonitor = new PackageMonitor() {
            public void onPackageAdded(String packageName, int uid) {
                Log.d(ReceiverController.TAG, "onPackageAdded()");
                if (ReceiverController.this.mRecordHelper != null) {
                    ReceiverController.this.mRecordHelper.updateReceiverCache();
                }
            }

            public void onPackageRemoved(String packageName, int uid) {
                Log.d(ReceiverController.TAG, "onPackageRemoved()");
                if (ReceiverController.this.mRecordHelper != null) {
                    ReceiverController.this.mRecordHelper.updateReceiverCache();
                }
            }

            public void onPackagesAvailable(String[] packages) {
                Log.d(ReceiverController.TAG, "onPackagesAvailable()");
                if (ReceiverController.this.mRecordHelper != null) {
                    ReceiverController.this.mRecordHelper.updateReceiverCache();
                }
            }

            public void onPackagesUnavailable(String[] packages) {
                Log.d(ReceiverController.TAG, "onPackagesUnavailable()");
                if (ReceiverController.this.mRecordHelper != null) {
                    ReceiverController.this.mRecordHelper.updateReceiverCache();
                }
            }
        };
        mContext = context;
        this.mBootReceiverPolicy = BootReceiverPolicy.getInstance(mContext);
        initRecordHelper();
        this.mPackageMonitor.register(context, mContext.getMainLooper(), UserHandle.ALL, true);
        startMonitor("Normal Bootup Start");
    }

    public void startMonitor(String cause) {
        Log.d(TAG, "startMonitor(" + cause + ")");
    }

    public void stopMonitor(String cause) {
        Log.d(TAG, "stopMonitor(" + cause + ")");
    }

    private void initRecordHelper() {
        this.mRecordHelper = new ReceiverRecordHelper(mContext, getUserManagerService(), getPackageManagerService());
        this.mRecordHelper.initReceiverList();
        Log.d(TAG, "init ReceiverRecordHelper done.");
    }

    public void filterReceiver(Intent intent, List<ResolveInfo> resolveList, int userId) {
        String action = intent.getAction();
        if (!mMonitorEnabled) {
            return;
        }
        if (action == null) {
            Log.e(TAG, "filterReceiver() ignored with null action");
        } else if (resolveList != null) {
            if (isValidUserId(userId)) {
                if (this.mBootReceiverPolicy.match(action)) {
                    this.mRecordHelper.updateReceiverCache();
                    Iterator<ResolveInfo> itor = resolveList.iterator();
                    while (itor.hasNext()) {
                        ResolveInfo info = (ResolveInfo) itor.next();
                        if (info.activityInfo != null) {
                            String packageName = info.activityInfo.packageName;
                            Log.d(TAG, "filterReceiver() - package = " + packageName + " has action = " + action);
                            if (!checkStrictPolicyAllowed(action, userId, packageName)) {
                                itor.remove();
                            }
                        }
                    }
                }
                return;
            }
            Log.e(TAG, "filterReceiver() ignored with invalid userId: " + userId);
        }
    }

    private boolean checkStrictPolicyAllowed(String action, int userId, String packageName) {
        boolean allowed = true;
        synchronized (this.mRecordHelper) {
            if (!this.mRecordHelper.getReceiverDataEnabled(userId, packageName)) {
                Log.d(TAG, "checkStrictPolicyAllowed() -  denied " + action + " to package: " + packageName + " at User(" + userId + ")");
                allowed = false;
            }
        }
        return allowed;
    }

    public static IPackageManager getPackageManagerService() {
        IPackageManager pm = Stub.asInterface(ServiceManager.getService("package"));
        if (pm != null) {
            return pm;
        }
        throw new RuntimeException("null package manager service");
    }

    public static IUserManager getUserManagerService() {
        IUserManager um = IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        if (um != null) {
            return um;
        }
        throw new RuntimeException("null user manager service");
    }

    public boolean isValidUserId(int userId) {
        if (userId >= 0 && userId < 100000) {
            return true;
        }
        Log.e(TAG, "Invalid userId: " + userId);
        return false;
    }
}
