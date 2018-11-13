package com.android.location.provider;

import android.hardware.location.IActivityRecognitionHardware;
import android.hardware.location.IActivityRecognitionHardwareWatcher.Stub;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

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
@Deprecated
public class ActivityRecognitionProviderWatcher {
    private static final String TAG = "ActivityRecognitionProviderWatcher";
    private static ActivityRecognitionProviderWatcher sWatcher;
    private static final Object sWatcherLock = null;
    private ActivityRecognitionProvider mActivityRecognitionProvider;
    private Stub mWatcherStub;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.location.provider.ActivityRecognitionProviderWatcher.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.location.provider.ActivityRecognitionProviderWatcher.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.location.provider.ActivityRecognitionProviderWatcher.<clinit>():void");
    }

    private ActivityRecognitionProviderWatcher() {
        this.mWatcherStub = new Stub() {
            public void onInstanceChanged(IActivityRecognitionHardware instance) {
                int callingUid = Binder.getCallingUid();
                if (callingUid != 1000) {
                    Log.d(ActivityRecognitionProviderWatcher.TAG, "Ignoring calls from non-system server. Uid: " + callingUid);
                    return;
                }
                try {
                    ActivityRecognitionProviderWatcher.this.mActivityRecognitionProvider = new ActivityRecognitionProvider(instance);
                } catch (RemoteException e) {
                    Log.e(ActivityRecognitionProviderWatcher.TAG, "Error creating Hardware Activity-Recognition", e);
                }
            }
        };
    }

    public static ActivityRecognitionProviderWatcher getInstance() {
        ActivityRecognitionProviderWatcher activityRecognitionProviderWatcher;
        synchronized (sWatcherLock) {
            if (sWatcher == null) {
                sWatcher = new ActivityRecognitionProviderWatcher();
            }
            activityRecognitionProviderWatcher = sWatcher;
        }
        return activityRecognitionProviderWatcher;
    }

    public IBinder getBinder() {
        return this.mWatcherStub;
    }

    public ActivityRecognitionProvider getActivityRecognitionProvider() {
        return this.mActivityRecognitionProvider;
    }
}
