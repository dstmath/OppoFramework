package com.android.server;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.Preconditions;
import com.android.server.ServiceWatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ServiceWatcher implements ServiceConnection {
    private static final boolean D = true;
    public static final String EXTRA_SERVICE_IS_MULTIUSER = "serviceIsMultiuser";
    public static final String EXTRA_SERVICE_VERSION = "serviceVersion";
    private static final String GEO_ACTION = "com.android.location.service.GeocodeProvider";
    private static final int MSG_REBIND_GEOCODER_SERVICE = 2;
    private static final int MSG_REBIND_NLP_SERVICE = 1;
    private static final String NLP_ACTION = "com.android.location.service.v3.NetworkLocationProvider";
    private static final String TAG = "ServiceWatcher";
    /* access modifiers changed from: private */
    public final String mAction;
    private volatile ComponentName mBestComponent;
    private IBinder mBestService;
    private volatile int mBestUserId;
    private volatile int mBestVersion;
    private final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentUserId;
    private final Handler mHandler;
    private ServiceWatcherHandler mRebindHandler = null;
    private boolean mServiceBindSucess = true;
    private final String mServicePackageName;
    private final List<HashSet<Signature>> mSignatureSets;
    private final String mTag;

    public interface BinderRunner {
        void run(IBinder iBinder) throws RemoteException;
    }

    public interface BlockingBinderRunner<T> {
        T run(IBinder iBinder) throws RemoteException;
    }

    public static ArrayList<HashSet<Signature>> getSignatureSets(Context context, String... packageNames) {
        PackageManager pm = context.getPackageManager();
        ArrayList<HashSet<Signature>> signatureSets = new ArrayList<>(packageNames.length);
        for (String packageName : packageNames) {
            try {
                Signature[] signatures = pm.getPackageInfo(packageName, 1048640).signatures;
                HashSet<Signature> set = new HashSet<>();
                Collections.addAll(set, signatures);
                signatureSets.add(set);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, packageName + " not found");
            }
        }
        return signatureSets;
    }

    public static boolean isSignatureMatch(Signature[] signatures, List<HashSet<Signature>> sigSets) {
        if (signatures == null) {
            return false;
        }
        HashSet<Signature> inputSet = new HashSet<>();
        Collections.addAll(inputSet, signatures);
        for (HashSet<Signature> referenceSet : sigSets) {
            if (referenceSet.equals(inputSet)) {
                return true;
            }
        }
        return false;
    }

    public ServiceWatcher(Context context, String logTag, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        Resources resources = context.getResources();
        this.mContext = context;
        this.mTag = logTag;
        this.mAction = action;
        if (resources.getBoolean(overlaySwitchResId)) {
            String[] pkgs = resources.getStringArray(initialPackageNamesResId);
            this.mServicePackageName = null;
            this.mSignatureSets = getSignatureSets(context, pkgs);
            String str = this.mTag;
            Log.d(str, "Overlay enabled, packages=" + Arrays.toString(pkgs));
        } else {
            this.mServicePackageName = resources.getString(defaultServicePackageNameResId);
            this.mSignatureSets = getSignatureSets(context, this.mServicePackageName);
            String str2 = this.mTag;
            Log.d(str2, "Overlay disabled, default package=" + this.mServicePackageName);
        }
        this.mHandler = handler;
        this.mRebindHandler = new ServiceWatcherHandler(this.mHandler.getLooper());
        this.mBestComponent = null;
        this.mBestVersion = Integer.MIN_VALUE;
        this.mBestUserId = -10000;
        this.mBestService = null;
    }

    /* access modifiers changed from: protected */
    public void onBind() {
    }

    /* access modifiers changed from: protected */
    public void onUnbind() {
    }

    public final boolean start() {
        if (isServiceMissing()) {
            return false;
        }
        if (this.mServicePackageName == null) {
            new PackageMonitor() {
                /* class com.android.server.ServiceWatcher.AnonymousClass1 */

                public void onPackageUpdateFinished(String packageName, int uid) {
                    ServiceWatcher serviceWatcher = ServiceWatcher.this;
                    serviceWatcher.bindBestPackage(Objects.equals(packageName, serviceWatcher.getCurrentPackageName()));
                }

                public void onPackageAdded(String packageName, int uid) {
                    ServiceWatcher serviceWatcher = ServiceWatcher.this;
                    serviceWatcher.bindBestPackage(Objects.equals(packageName, serviceWatcher.getCurrentPackageName()));
                }

                public void onPackageRemoved(String packageName, int uid) {
                    ServiceWatcher serviceWatcher = ServiceWatcher.this;
                    serviceWatcher.bindBestPackage(Objects.equals(packageName, serviceWatcher.getCurrentPackageName()));
                }

                public boolean onPackageChanged(String packageName, int uid, String[] components) {
                    ServiceWatcher serviceWatcher = ServiceWatcher.this;
                    serviceWatcher.bindBestPackage(Objects.equals(packageName, serviceWatcher.getCurrentPackageName()));
                    return ServiceWatcher.super.onPackageChanged(packageName, uid, components);
                }
            }.register(this.mContext, UserHandle.ALL, true, this.mHandler);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.ServiceWatcher.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    int unused = ServiceWatcher.this.mCurrentUserId = userId;
                    ServiceWatcher.this.bindBestPackage(false);
                } else if ("android.intent.action.USER_UNLOCKED".equals(action) && userId == ServiceWatcher.this.mCurrentUserId) {
                    ServiceWatcher.this.bindBestPackage(false);
                }
            }
        }, UserHandle.ALL, intentFilter, null, this.mHandler);
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        this.mHandler.post(new Runnable() {
            /* class com.android.server.$$Lambda$ServiceWatcher$IP3HV4ye72eH3YxzGb9dMfcGWPE */

            public final void run() {
                ServiceWatcher.this.lambda$start$0$ServiceWatcher();
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$start$0$ServiceWatcher() {
        bindBestPackage(false);
    }

    public String getCurrentPackageName() {
        ComponentName bestComponent = this.mBestComponent;
        if (bestComponent == null) {
            return null;
        }
        return bestComponent.getPackageName();
    }

    private boolean isServiceMissing() {
        return this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent(this.mAction), 786432, 0).isEmpty();
    }

    /* access modifiers changed from: private */
    public void bindBestPackage(boolean forceRebind) {
        List<ResolveInfo> rInfos;
        String str;
        String str2;
        Preconditions.checkState(Looper.myLooper() == this.mHandler.getLooper());
        Intent intent = new Intent(this.mAction);
        String str3 = this.mServicePackageName;
        if (str3 != null) {
            intent.setPackage(str3);
        }
        List<ResolveInfo> rInfos2 = this.mContext.getPackageManager().queryIntentServicesAsUser(intent, 268435584, this.mCurrentUserId);
        if (rInfos2 == null) {
            rInfos = Collections.emptyList();
        } else {
            rInfos = rInfos2;
        }
        int bestVersion = Integer.MIN_VALUE;
        Iterator<ResolveInfo> it = rInfos.iterator();
        boolean bestIsMultiuser = false;
        ComponentName bestComponent = null;
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ResolveInfo rInfo = it.next();
            ComponentName component = rInfo.serviceInfo.getComponentName();
            String packageName = component.getPackageName();
            try {
                if (!isSignatureMatch(this.mContext.getPackageManager().getPackageInfo(packageName, 268435520).signatures, this.mSignatureSets)) {
                    Log.w(this.mTag, packageName + " resolves service " + this.mAction + ", but has wrong signature, ignoring");
                } else {
                    Bundle metadata = rInfo.serviceInfo.metaData;
                    int version = Integer.MIN_VALUE;
                    boolean isMultiuser = false;
                    if (metadata != null) {
                        version = metadata.getInt(EXTRA_SERVICE_VERSION, Integer.MIN_VALUE);
                        isMultiuser = metadata.getBoolean(EXTRA_SERVICE_IS_MULTIUSER, false);
                    }
                    if (version > bestVersion) {
                        bestVersion = version;
                        bestIsMultiuser = isMultiuser;
                        bestComponent = component;
                    }
                    if ((rInfo.serviceInfo.applicationInfo.flags & 1) == 1) {
                        Log.d(this.mTag, "BestPackage in /system: " + packageName);
                        break;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.wtf(this.mTag, e);
            }
        }
        String str4 = this.mTag;
        Object[] objArr = new Object[4];
        objArr[0] = this.mAction;
        if (this.mServicePackageName == null) {
            str = "";
        } else {
            str = "(" + this.mServicePackageName + ") ";
        }
        boolean alreadyBound = true;
        objArr[1] = str;
        objArr[2] = Integer.valueOf(rInfos.size());
        if (bestComponent == null) {
            str2 = "no new best component";
        } else {
            str2 = "new best component: " + bestComponent;
        }
        objArr[3] = str2;
        Log.d(str4, String.format("bindBestPackage for %s : %s found %d, %s", objArr));
        if (bestComponent == null) {
            Slog.w(this.mTag, "Odd, no component found for service " + this.mAction);
            unbind();
            return;
        }
        int userId = bestIsMultiuser ? 0 : this.mCurrentUserId;
        if (!(Objects.equals(bestComponent, this.mBestComponent) && bestVersion == this.mBestVersion && userId == this.mBestUserId)) {
            alreadyBound = false;
        }
        if (forceRebind || !alreadyBound) {
            unbind();
            bind(bestComponent, bestVersion, userId);
        }
    }

    private void bind(ComponentName component, int version, int userId) {
        Preconditions.checkState(Looper.myLooper() == this.mHandler.getLooper());
        Intent intent = new Intent(this.mAction);
        intent.setComponent(component);
        this.mBestComponent = component;
        this.mBestVersion = version;
        this.mBestUserId = userId;
        String str = this.mTag;
        Log.d(str, "binding " + component + " (v" + version + ") (u" + userId + ")");
        this.mContext.bindServiceAsUser(intent, this, 1073741829, UserHandle.of(userId));
    }

    private void unbind() {
        Preconditions.checkState(Looper.myLooper() == this.mHandler.getLooper());
        if (this.mBestComponent != null) {
            String str = this.mTag;
            Log.d(str, "unbinding " + this.mBestComponent);
            this.mContext.unbindService(this);
        }
        this.mBestComponent = null;
        this.mBestVersion = Integer.MIN_VALUE;
        this.mBestUserId = -10000;
    }

    public final void runOnBinder(BinderRunner runner) {
        sendRebindMessage(1);
        runOnHandler(new Runnable(runner) {
            /* class com.android.server.$$Lambda$ServiceWatcher$gVk2fFkq2aamIua2kIpukAFtf8 */
            private final /* synthetic */ ServiceWatcher.BinderRunner f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ServiceWatcher.this.lambda$runOnBinder$1$ServiceWatcher(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$runOnBinder$1$ServiceWatcher(BinderRunner runner) {
        IBinder iBinder = this.mBestService;
        if (iBinder != null) {
            try {
                runner.run(iBinder);
            } catch (RuntimeException e) {
                Log.e(TAG, "exception while while running " + runner + " on " + this.mBestService + " from " + this, e);
            } catch (RemoteException e2) {
            }
        }
    }

    @Deprecated
    public final <T> T runOnBinderBlocking(BlockingBinderRunner<T> runner, T defaultValue) {
        sendRebindMessage(2);
        try {
            return runOnHandlerBlocking(new Callable(defaultValue, runner) {
                /* class com.android.server.$$Lambda$ServiceWatcher$b1z9OeL1VpQ_8p47qz7nMNUpsE */
                private final /* synthetic */ Object f$1;
                private final /* synthetic */ ServiceWatcher.BlockingBinderRunner f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.concurrent.Callable
                public final Object call() {
                    return ServiceWatcher.this.lambda$runOnBinderBlocking$2$ServiceWatcher(this.f$1, this.f$2);
                }
            });
        } catch (InterruptedException e) {
            return defaultValue;
        }
    }

    public /* synthetic */ Object lambda$runOnBinderBlocking$2$ServiceWatcher(Object defaultValue, BlockingBinderRunner runner) throws Exception {
        IBinder iBinder = this.mBestService;
        if (iBinder == null) {
            return defaultValue;
        }
        try {
            return runner.run(iBinder);
        } catch (RemoteException e) {
            return defaultValue;
        }
    }

    public final void onServiceConnected(ComponentName component, IBinder binder) {
        this.mServiceBindSucess = true;
        runOnHandler(new Runnable(component, binder) {
            /* class com.android.server.$$Lambda$ServiceWatcher$uru7j1zDGiN8rndFZ3KWaTrxYo */
            private final /* synthetic */ ComponentName f$1;
            private final /* synthetic */ IBinder f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ServiceWatcher.this.lambda$onServiceConnected$3$ServiceWatcher(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$onServiceConnected$3$ServiceWatcher(ComponentName component, IBinder binder) {
        String str = this.mTag;
        Log.d(str, component + " connected");
        this.mBestService = binder;
        onBind();
    }

    public final void onServiceDisconnected(ComponentName component) {
        this.mServiceBindSucess = false;
        runOnHandler(new Runnable(component) {
            /* class com.android.server.$$Lambda$ServiceWatcher$uCZpuTwrOzCS9PQS2NY1ZXaU8U */
            private final /* synthetic */ ComponentName f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ServiceWatcher.this.lambda$onServiceDisconnected$4$ServiceWatcher(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onServiceDisconnected$4$ServiceWatcher(ComponentName component) {
        if (this.mAction.equals(NLP_ACTION) || this.mAction.equals(GEO_ACTION)) {
            bindBestPackage(true);
            return;
        }
        String str = this.mTag;
        Log.d(str, component + " disconnected");
        this.mBestService = null;
        onUnbind();
    }

    public String toString() {
        ComponentName bestComponent = this.mBestComponent;
        if (bestComponent == null) {
            return "null";
        }
        return bestComponent.toShortString() + "@" + this.mBestVersion;
    }

    private void runOnHandler(Runnable r) {
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            r.run();
        } else {
            this.mHandler.post(r);
        }
    }

    private <T> T runOnHandlerBlocking(Callable<T> c) throws InterruptedException {
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            try {
                return c.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            FutureTask<T> task = new FutureTask<>(c);
            this.mHandler.post(task);
            try {
                return task.get();
            } catch (ExecutionException e2) {
                throw new IllegalStateException(e2);
            }
        }
    }

    private class ServiceWatcherHandler extends Handler {
        public ServiceWatcherHandler(Looper looper) {
            super(looper, null);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                ServiceWatcher.this.bindBestPackage(true);
                Log.v(ServiceWatcher.TAG, ServiceWatcher.this.mAction + ":pre bind fail,try rebind");
            } else if (i != 2) {
                Log.v(ServiceWatcher.TAG, "receive unkonw message:" + msg.what);
            } else {
                ServiceWatcher.this.bindBestPackage(true);
                Log.v(ServiceWatcher.TAG, ServiceWatcher.this.mAction + ":pre bind fail,try rebind");
            }
        }
    }

    private void sendRebindMessage(int messageType) {
        ServiceWatcherHandler serviceWatcherHandler;
        if ((this.mAction.equals(NLP_ACTION) || this.mAction.equals(GEO_ACTION)) && !this.mServiceBindSucess && this.mBestService != null && (serviceWatcherHandler = this.mRebindHandler) != null) {
            if (serviceWatcherHandler.hasMessages(messageType)) {
                this.mRebindHandler.removeMessages(messageType);
            }
            this.mRebindHandler.sendMessage(Message.obtain(this.mRebindHandler, messageType));
        }
    }
}
