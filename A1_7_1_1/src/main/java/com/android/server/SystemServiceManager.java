package com.android.server;

import android.content.Context;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Slog;
import com.android.server.oppo.IElsaManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

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
public class SystemServiceManager {
    private static boolean DEBUG_INIT_PROGRESS = false;
    private static final boolean IS_USER_BUILD = false;
    private static final String TAG = "SystemServiceManager";
    private final Context mContext;
    private int mCurrentPhase;
    private boolean mSafeMode;
    private final ArrayList<SystemService> mServices;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.SystemServiceManager.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.SystemServiceManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.SystemServiceManager.<clinit>():void");
    }

    public SystemServiceManager(Context context) {
        this.mServices = new ArrayList();
        this.mCurrentPhase = -1;
        this.mContext = context;
    }

    public SystemService startService(String className) {
        try {
            return startService(Class.forName(className));
        } catch (ClassNotFoundException ex) {
            Slog.i(TAG, "Starting " + className);
            throw new RuntimeException("Failed to create service " + className + ": service class not found, usually indicates that the caller should " + "have called PackageManager.hasSystemFeature() to check whether the " + "feature is available on this device before trying to start the " + "services that implement it", ex);
        }
    }

    public <T extends SystemService> T startService(Class<T> serviceClass) {
        String name;
        try {
            name = serviceClass.getName();
            Slog.i(TAG, "Starting " + name);
            Trace.traceBegin(2097152, "StartService " + name);
            if (SystemService.class.isAssignableFrom(serviceClass)) {
                Class[] clsArr = new Class[1];
                clsArr[0] = Context.class;
                Constructor<T> constructor = serviceClass.getConstructor(clsArr);
                Object[] objArr = new Object[1];
                objArr[0] = this.mContext;
                SystemService service = (SystemService) constructor.newInstance(objArr);
                this.mServices.add(service);
                service.onStart();
                Trace.traceEnd(2097152);
                return service;
            }
            throw new RuntimeException("Failed to create " + name + ": service must extend " + SystemService.class.getName());
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to start service " + name + ": onStart threw an exception", ex);
        } catch (InstantiationException ex2) {
            throw new RuntimeException("Failed to create service " + name + ": service could not be instantiated", ex2);
        } catch (IllegalAccessException ex3) {
            throw new RuntimeException("Failed to create service " + name + ": service must have a public constructor with a Context argument", ex3);
        } catch (NoSuchMethodException ex4) {
            throw new RuntimeException("Failed to create service " + name + ": service must have a public constructor with a Context argument", ex4);
        } catch (InvocationTargetException ex5) {
            throw new RuntimeException("Failed to create service " + name + ": service constructor threw an exception", ex5);
        } catch (Throwable th) {
            Trace.traceEnd(2097152);
        }
    }

    public void startBootPhase(int phase) {
        if (phase <= this.mCurrentPhase) {
            throw new IllegalArgumentException("Next phase must be larger than previous");
        }
        this.mCurrentPhase = phase;
        Slog.i(TAG, "Starting phase " + this.mCurrentPhase);
        SystemService service;
        try {
            Trace.traceBegin(2097152, "OnBootPhase " + phase);
            int serviceLen = this.mServices.size();
            for (int i = 0; i < serviceLen; i++) {
                service = (SystemService) this.mServices.get(i);
                long startTime = 0;
                if (!IS_USER_BUILD) {
                    startTime = SystemClock.elapsedRealtime();
                }
                service.onBootPhase(this.mCurrentPhase);
                if (!IS_USER_BUILD) {
                    checkTime(startTime, "Phase " + this.mCurrentPhase, service.getClass().getName());
                }
            }
            Trace.traceEnd(2097152);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to boot service " + service.getClass().getName() + ": onBootPhase threw an exception during phase " + this.mCurrentPhase, ex);
        } catch (Throwable th) {
            Trace.traceEnd(2097152);
        }
    }

    public void startUser(int userHandle) {
        int serviceLen = this.mServices.size();
        Slog.i(TAG, "Starting phase " + this.mCurrentPhase + " for " + serviceLen + " services.");
        String serviceName = IElsaManager.EMPTY_PACKAGE;
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = (SystemService) this.mServices.get(i);
            Trace.traceBegin(2097152, "onStartUser " + service.getClass().getName());
            if (DEBUG_INIT_PROGRESS) {
                serviceName = service.getClass().getName();
                Slog.i(TAG, "onBootPhase " + this.mCurrentPhase + " begin for server[" + i + "]:" + serviceName);
            }
            try {
                service.onStartUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting start of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            Trace.traceEnd(2097152);
            if (DEBUG_INIT_PROGRESS) {
                Slog.i(TAG, "onBootPhase " + this.mCurrentPhase + "   end for server[" + i + "]:" + serviceName);
            }
        }
    }

    public void unlockUser(int userHandle) {
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = (SystemService) this.mServices.get(i);
            Trace.traceBegin(2097152, "onUnlockUser " + service.getClass().getName());
            try {
                service.onUnlockUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting unlock of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            Trace.traceEnd(2097152);
        }
    }

    public void switchUser(int userHandle) {
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = (SystemService) this.mServices.get(i);
            Trace.traceBegin(2097152, "onSwitchUser " + service.getClass().getName());
            try {
                service.onSwitchUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting switch of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            Trace.traceEnd(2097152);
        }
    }

    public void stopUser(int userHandle) {
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = (SystemService) this.mServices.get(i);
            Trace.traceBegin(2097152, "onStopUser " + service.getClass().getName());
            try {
                service.onStopUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting stop of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            Trace.traceEnd(2097152);
        }
    }

    public void cleanupUser(int userHandle) {
        int serviceLen = this.mServices.size();
        for (int i = 0; i < serviceLen; i++) {
            SystemService service = (SystemService) this.mServices.get(i);
            Trace.traceBegin(2097152, "onCleanupUser " + service.getClass().getName());
            try {
                service.onCleanupUser(userHandle);
            } catch (Exception ex) {
                Slog.wtf(TAG, "Failure reporting cleanup of user " + userHandle + " to service " + service.getClass().getName(), ex);
            }
            Trace.traceEnd(2097152);
        }
    }

    public void setSafeMode(boolean safeMode) {
        this.mSafeMode = safeMode;
    }

    public boolean isSafeMode() {
        return this.mSafeMode;
    }

    public void dump() {
        StringBuilder builder = new StringBuilder();
        builder.append("Current phase: ").append(this.mCurrentPhase).append("\n");
        builder.append("Services:\n");
        int startedLen = this.mServices.size();
        for (int i = 0; i < startedLen; i++) {
            builder.append("\t").append(((SystemService) this.mServices.get(i)).getClass().getSimpleName()).append("\n");
        }
        Slog.e(TAG, builder.toString());
    }

    private void checkTime(long startTime, String op, String service) {
        long now = SystemClock.elapsedRealtime();
        if (now - startTime > 500) {
            Slog.w(TAG, "[" + op + "]" + service + " took " + (now - startTime) + "ms");
        }
    }
}
