package com.android.server.pm;

import android.app.IInstantAppResolver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.InstantAppResolveInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IRemoteCallback;
import android.os.IRemoteCallback.Stub;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import android.util.TimedRemoteCaller;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

final class EphemeralResolverConnection implements DeathRecipient {
    private static final long BIND_SERVICE_TIMEOUT_MS = ((long) (Build.IS_ENG ? 500 : 300));
    private static final long CALL_SERVICE_TIMEOUT_MS = ((long) (Build.IS_ENG ? 200 : 100));
    private static final boolean DEBUG_EPHEMERAL = Build.IS_DEBUGGABLE;
    private static final int STATE_BINDING = 1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PENDING = 2;
    private static final String TAG = "PackageManager";
    @GuardedBy("mLock")
    private int mBindState = 0;
    private final Context mContext;
    private final GetEphemeralResolveInfoCaller mGetEphemeralResolveInfoCaller = new GetEphemeralResolveInfoCaller();
    private final Intent mIntent;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private IInstantAppResolver mRemoteInstance;
    private final ServiceConnection mServiceConnection = new MyServiceConnection(this, null);

    public static class ConnectionException extends Exception {
        public static final int FAILURE_BIND = 1;
        public static final int FAILURE_CALL = 2;
        public static final int FAILURE_INTERRUPTED = 3;
        public final int failure;

        public ConnectionException(int _failure) {
            this.failure = _failure;
        }
    }

    private static final class GetEphemeralResolveInfoCaller extends TimedRemoteCaller<List<InstantAppResolveInfo>> {
        private final IRemoteCallback mCallback = new Stub() {
            public void sendResult(Bundle data) throws RemoteException {
                GetEphemeralResolveInfoCaller.this.onRemoteMethodResult(data.getParcelableArrayList("android.app.extra.RESOLVE_INFO"), data.getInt("android.app.extra.SEQUENCE", -1));
            }
        };

        public GetEphemeralResolveInfoCaller() {
            super(EphemeralResolverConnection.CALL_SERVICE_TIMEOUT_MS);
        }

        public List<InstantAppResolveInfo> getEphemeralResolveInfoList(IInstantAppResolver target, int[] hashPrefix, String token) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.getInstantAppResolveInfoList(hashPrefix, token, sequence, this.mCallback);
            return (List) getResultTimed(sequence);
        }
    }

    private final class MyServiceConnection implements ServiceConnection {
        /* synthetic */ MyServiceConnection(EphemeralResolverConnection this$0, MyServiceConnection -this1) {
            this();
        }

        private MyServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (EphemeralResolverConnection.DEBUG_EPHEMERAL) {
                Slog.d(EphemeralResolverConnection.TAG, "Connected to instant app resolver");
            }
            synchronized (EphemeralResolverConnection.this.mLock) {
                EphemeralResolverConnection.this.mRemoteInstance = IInstantAppResolver.Stub.asInterface(service);
                if (EphemeralResolverConnection.this.mBindState == 2) {
                    EphemeralResolverConnection.this.mBindState = 0;
                }
                try {
                    service.linkToDeath(EphemeralResolverConnection.this, 0);
                } catch (RemoteException e) {
                    EphemeralResolverConnection.this.handleBinderDiedLocked();
                }
                EphemeralResolverConnection.this.mLock.notifyAll();
            }
            return;
        }

        public void onServiceDisconnected(ComponentName name) {
            if (EphemeralResolverConnection.DEBUG_EPHEMERAL) {
                Slog.d(EphemeralResolverConnection.TAG, "Disconnected from instant app resolver");
            }
            synchronized (EphemeralResolverConnection.this.mLock) {
                EphemeralResolverConnection.this.handleBinderDiedLocked();
            }
        }
    }

    public static abstract class PhaseTwoCallback {
        abstract void onPhaseTwoResolved(List<InstantAppResolveInfo> list, long j);
    }

    public EphemeralResolverConnection(Context context, ComponentName componentName, String action) {
        this.mContext = context;
        this.mIntent = new Intent(action).setComponent(componentName);
    }

    public final List<InstantAppResolveInfo> getInstantAppResolveInfoList(int[] hashPrefix, String token) throws ConnectionException {
        throwIfCalledOnMainThread();
        try {
            List<InstantAppResolveInfo> ephemeralResolveInfoList = this.mGetEphemeralResolveInfoCaller.getEphemeralResolveInfoList(getRemoteInstanceLazy(token), hashPrefix, token);
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
            return ephemeralResolveInfoList;
        } catch (TimeoutException e) {
            throw new ConnectionException(2);
        } catch (RemoteException e2) {
            synchronized (this.mLock) {
                this.mLock.notifyAll();
                return null;
            }
        } catch (TimeoutException e3) {
            throw new ConnectionException(1);
        } catch (InterruptedException e4) {
            throw new ConnectionException(3);
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
        }
    }

    public final void getInstantAppIntentFilterList(int[] hashPrefix, String token, String hostName, PhaseTwoCallback callback, Handler callbackHandler, long startTime) throws ConnectionException {
        final Handler handler = callbackHandler;
        final PhaseTwoCallback phaseTwoCallback = callback;
        final long j = startTime;
        try {
            getRemoteInstanceLazy(token).getInstantAppIntentFilterList(hashPrefix, token, hostName, new Stub() {
                public void sendResult(Bundle data) throws RemoteException {
                    final ArrayList<InstantAppResolveInfo> resolveList = data.getParcelableArrayList("android.app.extra.RESOLVE_INFO");
                    Handler handler = handler;
                    final PhaseTwoCallback phaseTwoCallback = phaseTwoCallback;
                    final long j = j;
                    handler.post(new Runnable() {
                        public void run() {
                            phaseTwoCallback.onPhaseTwoResolved(resolveList, j);
                        }
                    });
                }
            });
        } catch (TimeoutException e) {
            throw new ConnectionException(1);
        } catch (InterruptedException e2) {
            throw new ConnectionException(3);
        } catch (RemoteException e3) {
        }
    }

    private IInstantAppResolver getRemoteInstanceLazy(String token) throws ConnectionException, TimeoutException, InterruptedException {
        long binderToken = Binder.clearCallingIdentity();
        try {
            IInstantAppResolver bind = bind(token);
            return bind;
        } finally {
            Binder.restoreCallingIdentity(binderToken);
        }
    }

    private void waitForBindLocked(String token) throws TimeoutException, InterruptedException {
        long startMillis = SystemClock.uptimeMillis();
        while (this.mBindState != 0 && this.mRemoteInstance == null) {
            long remainingMillis = BIND_SERVICE_TIMEOUT_MS - (SystemClock.uptimeMillis() - startMillis);
            if (remainingMillis <= 0) {
                throw new TimeoutException("[" + token + "] Didn't bind to resolver in time!");
            }
            this.mLock.wait(remainingMillis);
        }
    }

    /* JADX WARNING: Missing block: B:43:0x0086, code:
            r4 = false;
            r3 = null;
     */
    /* JADX WARNING: Missing block: B:44:0x0088, code:
            if (r0 == false) goto L_0x00b6;
     */
    /* JADX WARNING: Missing block: B:47:0x008c, code:
            if (DEBUG_EPHEMERAL == false) goto L_0x00af;
     */
    /* JADX WARNING: Missing block: B:48:0x008e, code:
            android.util.Slog.i(TAG, "[" + r11 + "] Previous connection never established; rebinding");
     */
    /* JADX WARNING: Missing block: B:49:0x00af, code:
            r10.mContext.unbindService(r10.mServiceConnection);
     */
    /* JADX WARNING: Missing block: B:51:0x00b8, code:
            if (DEBUG_EPHEMERAL == false) goto L_0x00db;
     */
    /* JADX WARNING: Missing block: B:52:0x00ba, code:
            android.util.Slog.v(TAG, "[" + r11 + "] Binding to instant app resolver");
     */
    /* JADX WARNING: Missing block: B:53:0x00db, code:
            r4 = r10.mContext.bindServiceAsUser(r10.mIntent, r10.mServiceConnection, 67108865, android.os.UserHandle.SYSTEM);
     */
    /* JADX WARNING: Missing block: B:54:0x00ed, code:
            if (r4 == false) goto L_0x0125;
     */
    /* JADX WARNING: Missing block: B:55:0x00ef, code:
            r6 = r10.mLock;
     */
    /* JADX WARNING: Missing block: B:56:0x00f1, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:58:?, code:
            waitForBindLocked(r11);
            r3 = r10.mRemoteInstance;
     */
    /* JADX WARNING: Missing block: B:60:?, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:61:0x00f8, code:
            r6 = r10.mLock;
     */
    /* JADX WARNING: Missing block: B:62:0x00fa, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:63:0x00fb, code:
            if (r4 == false) goto L_0x0109;
     */
    /* JADX WARNING: Missing block: B:64:0x00fd, code:
            if (r3 != null) goto L_0x0109;
     */
    /* JADX WARNING: Missing block: B:67:?, code:
            r10.mBindState = 2;
     */
    /* JADX WARNING: Missing block: B:68:0x0102, code:
            r10.mLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:69:0x0107, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:70:0x0108, code:
            return r3;
     */
    /* JADX WARNING: Missing block: B:73:?, code:
            r10.mBindState = 0;
     */
    /* JADX WARNING: Missing block: B:83:0x0116, code:
            monitor-enter(r10.mLock);
     */
    /* JADX WARNING: Missing block: B:84:0x0117, code:
            if (r4 == false) goto L_0x0153;
     */
    /* JADX WARNING: Missing block: B:88:?, code:
            r10.mBindState = 2;
     */
    /* JADX WARNING: Missing block: B:89:0x011e, code:
            r10.mLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:93:?, code:
            android.util.Slog.w(TAG, "[" + r11 + "] Failed to bind to: " + r10.mIntent);
     */
    /* JADX WARNING: Missing block: B:94:0x0152, code:
            throw new com.android.server.pm.EphemeralResolverConnection.ConnectionException(1);
     */
    /* JADX WARNING: Missing block: B:97:?, code:
            r10.mBindState = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private IInstantAppResolver bind(String token) throws ConnectionException, TimeoutException, InterruptedException {
        boolean doUnbind = false;
        synchronized (this.mLock) {
            IInstantAppResolver iInstantAppResolver;
            if (this.mRemoteInstance != null) {
                iInstantAppResolver = this.mRemoteInstance;
                return iInstantAppResolver;
            }
            if (this.mBindState == 2) {
                if (DEBUG_EPHEMERAL) {
                    Slog.i(TAG, "[" + token + "] Previous bind timed out; waiting for connection");
                }
                try {
                    waitForBindLocked(token);
                    if (this.mRemoteInstance != null) {
                        iInstantAppResolver = this.mRemoteInstance;
                        return iInstantAppResolver;
                    }
                } catch (TimeoutException e) {
                    doUnbind = true;
                }
            }
            if (this.mBindState == 1) {
                if (DEBUG_EPHEMERAL) {
                    Slog.i(TAG, "[" + token + "] Another thread is binding; waiting for connection");
                }
                waitForBindLocked(token);
                if (this.mRemoteInstance != null) {
                    iInstantAppResolver = this.mRemoteInstance;
                    return iInstantAppResolver;
                }
                throw new ConnectionException(1);
            }
            this.mBindState = 1;
        }
    }

    private void throwIfCalledOnMainThread() {
        if (Thread.currentThread() == this.mContext.getMainLooper().getThread()) {
            throw new RuntimeException("Cannot invoke on the main thread");
        }
    }

    public void binderDied() {
        if (DEBUG_EPHEMERAL) {
            Slog.d(TAG, "Binder to instant app resolver died");
        }
        synchronized (this.mLock) {
            handleBinderDiedLocked();
        }
    }

    private void handleBinderDiedLocked() {
        if (this.mRemoteInstance != null) {
            try {
                this.mRemoteInstance.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
            }
        }
        this.mRemoteInstance = null;
    }
}
