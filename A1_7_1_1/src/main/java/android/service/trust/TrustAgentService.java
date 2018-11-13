package android.service.trust;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.service.trust.ITrustAgentService.Stub;
import android.util.Log;
import android.util.Slog;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class TrustAgentService extends Service {
    private static final boolean DEBUG = false;
    public static final int FLAG_GRANT_TRUST_DISMISS_KEYGUARD = 2;
    public static final int FLAG_GRANT_TRUST_INITIATED_BY_USER = 1;
    private static final int MSG_CONFIGURE = 2;
    private static final int MSG_DEVICE_LOCKED = 4;
    private static final int MSG_DEVICE_UNLOCKED = 5;
    private static final int MSG_TRUST_TIMEOUT = 3;
    private static final int MSG_UNLOCK_ATTEMPT = 1;
    public static final String SERVICE_INTERFACE = "android.service.trust.TrustAgentService";
    public static final String TRUST_AGENT_META_DATA = "android.service.trust.trustagent";
    private final String TAG;
    private ITrustAgentServiceCallback mCallback;
    private Handler mHandler;
    private final Object mLock;
    private boolean mManagingTrust;
    private Runnable mPendingGrantTrustTask;

    /* renamed from: android.service.trust.TrustAgentService$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ TrustAgentService this$0;
        final /* synthetic */ long val$durationMs;
        final /* synthetic */ int val$flags;
        final /* synthetic */ CharSequence val$message;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.trust.TrustAgentService.2.<init>(android.service.trust.TrustAgentService, java.lang.CharSequence, long, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass2(android.service.trust.TrustAgentService r1, java.lang.CharSequence r2, long r3, int r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.trust.TrustAgentService.2.<init>(android.service.trust.TrustAgentService, java.lang.CharSequence, long, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.trust.TrustAgentService.2.<init>(android.service.trust.TrustAgentService, java.lang.CharSequence, long, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.trust.TrustAgentService.2.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.trust.TrustAgentService.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.trust.TrustAgentService.2.run():void");
        }
    }

    private static final class ConfigurationData {
        final List<PersistableBundle> options;
        final IBinder token;

        ConfigurationData(List<PersistableBundle> opts, IBinder t) {
            this.options = opts;
            this.token = t;
        }
    }

    private final class TrustAgentServiceWrapper extends Stub {
        final /* synthetic */ TrustAgentService this$0;

        /* synthetic */ TrustAgentServiceWrapper(TrustAgentService this$0, TrustAgentServiceWrapper trustAgentServiceWrapper) {
            this(this$0);
        }

        private TrustAgentServiceWrapper(TrustAgentService this$0) {
            this.this$0 = this$0;
        }

        public void onUnlockAttempt(boolean successful) {
            int i;
            Handler -get1 = this.this$0.mHandler;
            if (successful) {
                i = 1;
            } else {
                i = 0;
            }
            -get1.obtainMessage(1, i, 0).sendToTarget();
        }

        public void onTrustTimeout() {
            this.this$0.mHandler.sendEmptyMessage(3);
        }

        public void onConfigure(List<PersistableBundle> args, IBinder token) {
            this.this$0.mHandler.obtainMessage(2, new ConfigurationData(args, token)).sendToTarget();
        }

        public void onDeviceLocked() throws RemoteException {
            this.this$0.mHandler.obtainMessage(4).sendToTarget();
        }

        public void onDeviceUnlocked() throws RemoteException {
            this.this$0.mHandler.obtainMessage(5).sendToTarget();
        }

        public void setCallback(ITrustAgentServiceCallback callback) {
            synchronized (this.this$0.mLock) {
                this.this$0.mCallback = callback;
                if (this.this$0.mManagingTrust) {
                    try {
                        this.this$0.mCallback.setManagingTrust(this.this$0.mManagingTrust);
                    } catch (RemoteException e) {
                        this.this$0.onError("calling setManagingTrust()");
                    }
                }
                if (this.this$0.mPendingGrantTrustTask != null) {
                    this.this$0.mPendingGrantTrustTask.run();
                    this.this$0.mPendingGrantTrustTask = null;
                }
            }
            return;
        }
    }

    public TrustAgentService() {
        this.TAG = TrustAgentService.class.getSimpleName() + "[" + getClass().getSimpleName() + "]";
        this.mLock = new Object();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = false;
                switch (msg.what) {
                    case 1:
                        TrustAgentService trustAgentService = TrustAgentService.this;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        trustAgentService.onUnlockAttempt(z);
                        return;
                    case 2:
                        ConfigurationData data = msg.obj;
                        boolean result = TrustAgentService.this.onConfigure(data.options);
                        if (data.token != null) {
                            try {
                                synchronized (TrustAgentService.this.mLock) {
                                    TrustAgentService.this.mCallback.onConfigureCompleted(result, data.token);
                                }
                                return;
                            } catch (RemoteException e) {
                                TrustAgentService.this.onError("calling onSetTrustAgentFeaturesEnabledCompleted()");
                                return;
                            }
                        }
                        return;
                    case 3:
                        TrustAgentService.this.onTrustTimeout();
                        return;
                    case 4:
                        TrustAgentService.this.onDeviceLocked();
                        return;
                    case 5:
                        TrustAgentService.this.onDeviceUnlocked();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void onCreate() {
        super.onCreate();
        ComponentName component = new ComponentName(this, getClass());
        try {
            if (!"android.permission.BIND_TRUST_AGENT".equals(getPackageManager().getServiceInfo(component, 0).permission)) {
                throw new IllegalStateException(component.flattenToShortString() + " is not declared with the permission " + "\"" + "android.permission.BIND_TRUST_AGENT" + "\"");
            }
        } catch (NameNotFoundException e) {
            Log.e(this.TAG, "Can't get ServiceInfo for " + component.toShortString());
        }
    }

    public void onUnlockAttempt(boolean successful) {
    }

    public void onTrustTimeout() {
    }

    public void onDeviceLocked() {
    }

    public void onDeviceUnlocked() {
    }

    private void onError(String msg) {
        Slog.v(this.TAG, "Remote exception while " + msg);
    }

    public boolean onConfigure(List<PersistableBundle> list) {
        return false;
    }

    @Deprecated
    public final void grantTrust(CharSequence message, long durationMs, boolean initiatedByUser) {
        grantTrust(message, durationMs, initiatedByUser ? 1 : 0);
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public final void grantTrust(java.lang.CharSequence r9, long r10, int r12) {
        /*
        r8 = this;
        r7 = r8.mLock;
        monitor-enter(r7);
        r1 = r8.mManagingTrust;	 Catch:{ all -> 0x0010 }
        if (r1 != 0) goto L_0x0013;	 Catch:{ all -> 0x0010 }
    L_0x0007:
        r1 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x0010 }
        r2 = "Cannot grant trust if agent is not managing trust. Call setManagingTrust(true) first.";	 Catch:{ all -> 0x0010 }
        r1.<init>(r2);	 Catch:{ all -> 0x0010 }
        throw r1;	 Catch:{ all -> 0x0010 }
    L_0x0010:
        r1 = move-exception;
        monitor-exit(r7);
        throw r1;
    L_0x0013:
        r1 = r8.mCallback;	 Catch:{ all -> 0x0010 }
        if (r1 == 0) goto L_0x002a;
    L_0x0017:
        r1 = r8.mCallback;	 Catch:{ RemoteException -> 0x0022 }
        r2 = r9.toString();	 Catch:{ RemoteException -> 0x0022 }
        r1.grantTrust(r2, r10, r12);	 Catch:{ RemoteException -> 0x0022 }
    L_0x0020:
        monitor-exit(r7);
        return;
    L_0x0022:
        r0 = move-exception;
        r1 = "calling enableTrust()";	 Catch:{ all -> 0x0010 }
        r8.onError(r1);	 Catch:{ all -> 0x0010 }
        goto L_0x0020;	 Catch:{ all -> 0x0010 }
    L_0x002a:
        r1 = new android.service.trust.TrustAgentService$2;	 Catch:{ all -> 0x0010 }
        r2 = r8;	 Catch:{ all -> 0x0010 }
        r3 = r9;	 Catch:{ all -> 0x0010 }
        r4 = r10;	 Catch:{ all -> 0x0010 }
        r6 = r12;	 Catch:{ all -> 0x0010 }
        r1.<init>(r2, r3, r4, r6);	 Catch:{ all -> 0x0010 }
        r8.mPendingGrantTrustTask = r1;	 Catch:{ all -> 0x0010 }
        goto L_0x0020;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.trust.TrustAgentService.grantTrust(java.lang.CharSequence, long, int):void");
    }

    public final void revokeTrust() {
        synchronized (this.mLock) {
            if (this.mPendingGrantTrustTask != null) {
                this.mPendingGrantTrustTask = null;
            }
            if (this.mCallback != null) {
                try {
                    this.mCallback.revokeTrust();
                } catch (RemoteException e) {
                    onError("calling revokeTrust()");
                }
            }
        }
        return;
    }

    public final void setManagingTrust(boolean managingTrust) {
        synchronized (this.mLock) {
            if (this.mManagingTrust != managingTrust) {
                this.mManagingTrust = managingTrust;
                if (this.mCallback != null) {
                    try {
                        this.mCallback.setManagingTrust(managingTrust);
                    } catch (RemoteException e) {
                        onError("calling setManagingTrust()");
                    }
                }
            }
        }
        return;
    }

    public final IBinder onBind(Intent intent) {
        return new TrustAgentServiceWrapper(this, null);
    }
}
