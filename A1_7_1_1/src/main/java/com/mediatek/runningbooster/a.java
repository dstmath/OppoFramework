package com.mediatek.runningbooster;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.os.Binder;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;

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
class a {
    private static a A;
    private static IPackageManager B;
    private static IActivityManager C;
    private static Context mContext;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.runningbooster.a.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.runningbooster.a.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.runningbooster.a.<clinit>():void");
    }

    private a(Context context) {
        C = b();
        B = a();
        mContext = context;
        b.init(mContext);
    }

    public static final a a(Context context) {
        if (A == null) {
            A = new a(context);
        }
        return A;
    }

    private static IPackageManager a() {
        IPackageManager asInterface = Stub.asInterface(ServiceManager.getService("package"));
        if (asInterface != null) {
            return asInterface;
        }
        throw new RuntimeException("null package manager service");
    }

    private static IActivityManager b() {
        IActivityManager iActivityManager = ActivityManagerNative.getDefault();
        if (iActivityManager != null) {
            return iActivityManager;
        }
        throw new RuntimeException("null activity manager service");
    }

    private boolean a(PackageInfo packageInfo) {
        if (packageInfo == null || packageInfo.signatures == null) {
            Log.e("LicenseController", "Package without C! ");
            return false;
        }
        for (Signature a : packageInfo.signatures) {
            if (b.a(a)) {
                Log.d("LicenseController", "Package check C pass");
                return true;
            }
        }
        Log.e("LicenseController", "Invalid C! ");
        return false;
    }

    private boolean a(int i) {
        try {
            int userId = UserHandle.getUserId(i);
            String[] packagesForUid = B.getPackagesForUid(i);
            if (packagesForUid != null) {
                for (String packageInfo : packagesForUid) {
                    if (!a(B.getPackageInfo(packageInfo, 4160, userId))) {
                        return false;
                    }
                }
                Log.d("LicenseController", "checkProtocol(" + i + ") passed!");
                return true;
            }
            Log.e("LicenseController", "getPackagesForUid() with null packages! ");
            return false;
        } catch (Throwable e) {
            Log.e("LicenseController", "get PackagesInfo failed! ", e);
            return false;
        }
    }

    private boolean c() {
        return a(Binder.getCallingUid());
    }

    public void f(String str) {
        if (!c()) {
            throw new SecurityException("Use API without valid license: " + str + " uid: " + Binder.getCallingUid() + " pid: " + Binder.getCallingPid());
        }
    }
}
