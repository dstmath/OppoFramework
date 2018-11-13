package com.android.server.pm;

import android.app.IEphemeralResolver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.EphemeralResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.IRemoteCallback.Stub;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.TimedRemoteCaller;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
final class EphemeralResolverConnection {
    private static final long BIND_SERVICE_TIMEOUT_MS = 0;
    private volatile boolean mBindRequested;
    private final Context mContext;
    private final GetEphemeralResolveInfoCaller mGetEphemeralResolveInfoCaller;
    private final Intent mIntent;
    private final Object mLock;
    private IEphemeralResolver mRemoteInstance;
    private final ServiceConnection mServiceConnection;

    private static final class GetEphemeralResolveInfoCaller extends TimedRemoteCaller<List<EphemeralResolveInfo>> {
        private final IRemoteCallback mCallback = new Stub() {
            public void sendResult(Bundle data) throws RemoteException {
                GetEphemeralResolveInfoCaller.this.onRemoteMethodResult(data.getParcelableArrayList("android.app.extra.RESOLVE_INFO"), data.getInt("android.app.extra.SEQUENCE", -1));
            }
        };

        public GetEphemeralResolveInfoCaller() {
            super(5000);
        }

        public List<EphemeralResolveInfo> getEphemeralResolveInfoList(IEphemeralResolver target, int[] hashPrefix, int prefixMask) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.getEphemeralResolveInfoList(this.mCallback, hashPrefix, prefixMask, sequence);
            return (List) getResultTimed(sequence);
        }
    }

    private final class MyServiceConnection implements ServiceConnection {
        /* synthetic */ MyServiceConnection(EphemeralResolverConnection this$0, MyServiceConnection myServiceConnection) {
            this();
        }

        private MyServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (EphemeralResolverConnection.this.mLock) {
                EphemeralResolverConnection.this.mRemoteInstance = IEphemeralResolver.Stub.asInterface(service);
                EphemeralResolverConnection.this.mLock.notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (EphemeralResolverConnection.this.mLock) {
                EphemeralResolverConnection.this.mRemoteInstance = null;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.EphemeralResolverConnection.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.EphemeralResolverConnection.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.EphemeralResolverConnection.<clinit>():void");
    }

    public EphemeralResolverConnection(Context context, ComponentName componentName) {
        this.mLock = new Object();
        this.mGetEphemeralResolveInfoCaller = new GetEphemeralResolveInfoCaller();
        this.mServiceConnection = new MyServiceConnection(this, null);
        this.mContext = context;
        this.mIntent = new Intent("android.intent.action.RESOLVE_EPHEMERAL_PACKAGE").setComponent(componentName);
    }

    public final List<EphemeralResolveInfo> getEphemeralResolveInfoList(int[] hashPrefix, int prefixMask) {
        Object obj;
        throwIfCalledOnMainThread();
        try {
            List<EphemeralResolveInfo> ephemeralResolveInfoList = this.mGetEphemeralResolveInfoCaller.getEphemeralResolveInfoList(getRemoteInstanceLazy(), hashPrefix, prefixMask);
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
            return ephemeralResolveInfoList;
        } catch (RemoteException e) {
            obj = this.mLock;
            synchronized (obj) {
                this.mLock.notifyAll();
                return null;
            }
        } catch (TimeoutException e2) {
            obj = this.mLock;
            synchronized (obj) {
                this.mLock.notifyAll();
                return null;
            }
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        synchronized (this.mLock) {
            pw.append(prefix).append("bound=").append(this.mRemoteInstance != null ? "true" : "false").println();
            pw.flush();
            try {
                IBinder asBinder = getRemoteInstanceLazy().asBinder();
                String[] strArr = new String[1];
                strArr[0] = prefix;
                asBinder.dump(fd, strArr);
            } catch (TimeoutException e) {
            } catch (RemoteException e2) {
            }
        }
    }

    private IEphemeralResolver getRemoteInstanceLazy() throws TimeoutException {
        synchronized (this.mLock) {
            IEphemeralResolver iEphemeralResolver;
            if (this.mRemoteInstance != null) {
                iEphemeralResolver = this.mRemoteInstance;
                return iEphemeralResolver;
            }
            bindLocked();
            iEphemeralResolver = this.mRemoteInstance;
            return iEphemeralResolver;
        }
    }

    private void bindLocked() throws TimeoutException {
        if (this.mRemoteInstance == null) {
            if (!this.mBindRequested) {
                this.mBindRequested = true;
                this.mContext.bindServiceAsUser(this.mIntent, this.mServiceConnection, 67108865, UserHandle.SYSTEM);
            }
            long startMillis = SystemClock.uptimeMillis();
            while (this.mRemoteInstance == null) {
                long remainingMillis = BIND_SERVICE_TIMEOUT_MS - (SystemClock.uptimeMillis() - startMillis);
                if (remainingMillis <= 0) {
                    throw new TimeoutException("Didn't bind to resolver in time.");
                }
                try {
                    this.mLock.wait(remainingMillis);
                } catch (InterruptedException e) {
                }
            }
            this.mLock.notifyAll();
        }
    }

    private void throwIfCalledOnMainThread() {
        if (Thread.currentThread() == this.mContext.getMainLooper().getThread()) {
            throw new RuntimeException("Cannot invoke on the main thread");
        }
    }
}
