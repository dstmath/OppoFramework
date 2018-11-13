package android.security;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.HttpResponseCache;
import android.os.IPowerManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.security.net.config.ApplicationConfig;
import android.security.net.config.ManifestConfigSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;

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
public class NetworkSecurityPolicy {
    private static final NetworkSecurityPolicy INSTANCE = null;
    private static int mPerfHandle;
    private static int mPerfHandle_2;
    private static Object mPerfService;
    private static HttpResponseCache sCache;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.security.NetworkSecurityPolicy.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.security.NetworkSecurityPolicy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.NetworkSecurityPolicy.<clinit>():void");
    }

    private NetworkSecurityPolicy() {
    }

    public static NetworkSecurityPolicy getInstance() {
        return INSTANCE;
    }

    public boolean isCleartextTrafficPermitted() {
        return libcore.net.NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
    }

    public boolean isCleartextTrafficPermitted(String hostname) {
        return libcore.net.NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(hostname);
    }

    public void setCleartextTrafficPermitted(boolean permitted) {
        libcore.net.NetworkSecurityPolicy.setInstance(new FrameworkNetworkSecurityPolicy(permitted));
    }

    public void handleTrustStorageUpdate() {
        ApplicationConfig.getDefaultInstance().handleTrustStorageUpdate();
    }

    public static ApplicationConfig getApplicationConfigForPackage(Context context, String packageName) throws NameNotFoundException {
        return new ApplicationConfig(new ManifestConfigSource(context.createPackageContext(packageName, 0)));
    }

    public static void checkUrl(URL httpUrl) {
        if (httpUrl != null && INSTANCE.isSecurityUrl(httpUrl.toString())) {
            INSTANCE.doAction();
        }
    }

    private boolean isSecurityUrl(String httpUrl) {
        if (httpUrl.endsWith(".png") && httpUrl.contains("hongbao")) {
            return true;
        }
        return false;
    }

    private void doAction() {
        try {
            if (isInteractive()) {
                speedDownload();
            }
            if (sCache == null) {
                sCache = HttpResponseCache.install(new File(System.getProperty("java.io.tmpdir"), "HttpCache"), 2147483647L);
            }
        } catch (IOException ioe) {
            System.out.println("do1:" + ioe);
        }
    }

    private static boolean isInteractive() {
        boolean res = false;
        try {
            return Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE)).isInteractive();
        } catch (RemoteException e) {
            System.out.println("speedDownload isInteractive " + e.toString());
            return res;
        } catch (Throwable th) {
            return res;
        }
    }

    private static void speedDownload() {
        try {
            System.out.println("speedDownload start");
            synchronized (NetworkSecurityPolicy.class) {
                Class[] clsArr;
                Method method2;
                Method method3;
                Object obj;
                Object[] objArr;
                Class cls = Class.forName("com.mediatek.perfservice.PerfServiceWrapper");
                mPerfService = cls.newInstance();
                if (mPerfService != null && mPerfHandle == -1) {
                    mPerfHandle = ((Integer) cls.getMethod("userRegScn", new Class[0]).invoke(mPerfService, new Object[0])).intValue();
                    System.out.println("speedDownload init of cluster1: " + mPerfHandle);
                }
                if (!(mPerfService == null || mPerfHandle == -1)) {
                    clsArr = new Class[6];
                    clsArr[0] = Integer.TYPE;
                    clsArr[1] = Integer.TYPE;
                    clsArr[2] = Integer.TYPE;
                    clsArr[3] = Integer.TYPE;
                    clsArr[4] = Integer.TYPE;
                    clsArr[5] = Integer.TYPE;
                    method2 = cls.getMethod("userRegScnConfig", clsArr);
                    clsArr = new Class[2];
                    clsArr[0] = Integer.TYPE;
                    clsArr[1] = Integer.TYPE;
                    method3 = cls.getMethod("userEnableTimeoutMs", clsArr);
                    obj = mPerfService;
                    objArr = new Object[6];
                    objArr[0] = new Integer(mPerfHandle);
                    objArr[1] = Integer.valueOf(30);
                    objArr[2] = Integer.valueOf(1);
                    objArr[3] = Integer.valueOf(0);
                    objArr[4] = Integer.valueOf(0);
                    objArr[5] = Integer.valueOf(0);
                    method2.invoke(obj, objArr);
                    obj = mPerfService;
                    objArr = new Object[6];
                    objArr[0] = new Integer(mPerfHandle);
                    objArr[1] = Integer.valueOf(50);
                    objArr[2] = Integer.valueOf(99);
                    objArr[3] = Integer.valueOf(0);
                    objArr[4] = Integer.valueOf(0);
                    objArr[5] = Integer.valueOf(0);
                    method2.invoke(obj, objArr);
                    obj = mPerfService;
                    objArr = new Object[6];
                    objArr[0] = new Integer(mPerfHandle);
                    objArr[1] = Integer.valueOf(15);
                    objArr[2] = Integer.valueOf(1);
                    objArr[3] = Integer.valueOf(4);
                    objArr[4] = Integer.valueOf(0);
                    objArr[5] = Integer.valueOf(0);
                    method2.invoke(obj, objArr);
                    obj = mPerfService;
                    objArr = new Object[6];
                    objArr[0] = new Integer(mPerfHandle);
                    objArr[1] = Integer.valueOf(17);
                    objArr[2] = Integer.valueOf(1);
                    objArr[3] = Integer.valueOf(3000000);
                    objArr[4] = Integer.valueOf(0);
                    objArr[5] = Integer.valueOf(0);
                    method2.invoke(obj, objArr);
                    obj = mPerfService;
                    objArr = new Object[6];
                    objArr[0] = new Integer(mPerfHandle);
                    objArr[1] = Integer.valueOf(10);
                    objArr[2] = Integer.valueOf(3);
                    objArr[3] = Integer.valueOf(0);
                    objArr[4] = Integer.valueOf(0);
                    objArr[5] = Integer.valueOf(0);
                    method2.invoke(obj, objArr);
                    obj = mPerfService;
                    objArr = new Object[2];
                    objArr[0] = new Integer(mPerfHandle);
                    objArr[1] = Integer.valueOf(3000);
                    method3.invoke(obj, objArr);
                    System.out.println("speedDownload of cluster1: " + mPerfHandle + " perfenable done");
                }
                if (mPerfService != null && mPerfHandle_2 == -1) {
                    mPerfHandle_2 = ((Integer) cls.getMethod("userRegScn", new Class[0]).invoke(mPerfService, new Object[0])).intValue();
                    System.out.println("speedDownload init of cluster0: " + mPerfHandle_2);
                }
                if (!(mPerfService == null || mPerfHandle_2 == -1)) {
                    clsArr = new Class[6];
                    clsArr[0] = Integer.TYPE;
                    clsArr[1] = Integer.TYPE;
                    clsArr[2] = Integer.TYPE;
                    clsArr[3] = Integer.TYPE;
                    clsArr[4] = Integer.TYPE;
                    clsArr[5] = Integer.TYPE;
                    method2 = cls.getMethod("userRegScnConfig", clsArr);
                    clsArr = new Class[2];
                    clsArr[0] = Integer.TYPE;
                    clsArr[1] = Integer.TYPE;
                    method3 = cls.getMethod("userEnableTimeoutMs", clsArr);
                    obj = mPerfService;
                    objArr = new Object[6];
                    objArr[0] = new Integer(mPerfHandle_2);
                    objArr[1] = Integer.valueOf(30);
                    objArr[2] = Integer.valueOf(1);
                    objArr[3] = Integer.valueOf(0);
                    objArr[4] = Integer.valueOf(0);
                    objArr[5] = Integer.valueOf(0);
                    method2.invoke(obj, objArr);
                    obj = mPerfService;
                    objArr = new Object[6];
                    objArr[0] = new Integer(mPerfHandle_2);
                    objArr[1] = Integer.valueOf(50);
                    objArr[2] = Integer.valueOf(99);
                    objArr[3] = Integer.valueOf(0);
                    objArr[4] = Integer.valueOf(0);
                    objArr[5] = Integer.valueOf(0);
                    method2.invoke(obj, objArr);
                    obj = mPerfService;
                    objArr = new Object[6];
                    objArr[0] = new Integer(mPerfHandle_2);
                    objArr[1] = Integer.valueOf(15);
                    objArr[2] = Integer.valueOf(0);
                    objArr[3] = Integer.valueOf(4);
                    objArr[4] = Integer.valueOf(0);
                    objArr[5] = Integer.valueOf(0);
                    method2.invoke(obj, objArr);
                    obj = mPerfService;
                    objArr = new Object[6];
                    objArr[0] = new Integer(mPerfHandle_2);
                    objArr[1] = Integer.valueOf(17);
                    objArr[2] = Integer.valueOf(0);
                    objArr[3] = Integer.valueOf(3000000);
                    objArr[4] = Integer.valueOf(0);
                    objArr[5] = Integer.valueOf(0);
                    method2.invoke(obj, objArr);
                    obj = mPerfService;
                    objArr = new Object[2];
                    objArr[0] = new Integer(mPerfHandle_2);
                    objArr[1] = Integer.valueOf(15000);
                    method3.invoke(obj, objArr);
                    System.out.println("speedDownload of cluster0: " + mPerfHandle_2 + " perfenable done");
                }
            }
        } catch (Exception e) {
            System.out.println("err: " + e);
        }
    }

    private static void triggerWCP() {
        try {
            new Socket(null, 7879).close();
            System.out.println("Notify");
        } catch (Exception e) {
            System.out.println("err: " + e);
        }
    }
}
