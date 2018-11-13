package com.android.server.tv;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import java.util.ArrayList;
import java.util.Collections;

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
final class TvRemoteProviderWatcher {
    private static final boolean DEBUG = false;
    private static final String TAG = "TvRemoteProvWatcher";
    private final Context mContext;
    private final Handler mHandler;
    private final PackageManager mPackageManager;
    private final ProviderMethods mProvider;
    private final ArrayList<TvRemoteProviderProxy> mProviderProxies;
    private boolean mRunning;
    private final BroadcastReceiver mScanPackagesReceiver;
    private final Runnable mScanPackagesRunnable;
    private final String mUnbundledServicePackage;
    private final int mUserId;

    public interface ProviderMethods {
        void addProvider(TvRemoteProviderProxy tvRemoteProviderProxy);

        void removeProvider(TvRemoteProviderProxy tvRemoteProviderProxy);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.tv.TvRemoteProviderWatcher.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.tv.TvRemoteProviderWatcher.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.tv.TvRemoteProviderWatcher.<clinit>():void");
    }

    public TvRemoteProviderWatcher(Context context, ProviderMethods provider, Handler handler) {
        this.mProviderProxies = new ArrayList();
        this.mScanPackagesReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (TvRemoteProviderWatcher.DEBUG) {
                    Slog.d(TvRemoteProviderWatcher.TAG, "Received package manager broadcast: " + intent);
                }
                TvRemoteProviderWatcher.this.mHandler.post(TvRemoteProviderWatcher.this.mScanPackagesRunnable);
            }
        };
        this.mScanPackagesRunnable = new Runnable() {
            public void run() {
                TvRemoteProviderWatcher.this.scanPackages();
            }
        };
        this.mContext = context;
        this.mProvider = provider;
        this.mHandler = handler;
        this.mUserId = UserHandle.myUserId();
        this.mPackageManager = context.getPackageManager();
        this.mUnbundledServicePackage = context.getString(17039475);
    }

    public void start() {
        if (DEBUG) {
            Slog.d(TAG, "start()");
        }
        if (!this.mRunning) {
            this.mRunning = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_REPLACED");
            filter.addAction("android.intent.action.PACKAGE_RESTARTED");
            filter.addDataScheme("package");
            this.mContext.registerReceiverAsUser(this.mScanPackagesReceiver, new UserHandle(this.mUserId), filter, null, this.mHandler);
            this.mHandler.post(this.mScanPackagesRunnable);
        }
    }

    public void stop() {
        if (this.mRunning) {
            this.mRunning = false;
            this.mContext.unregisterReceiver(this.mScanPackagesReceiver);
            this.mHandler.removeCallbacks(this.mScanPackagesRunnable);
            for (int i = this.mProviderProxies.size() - 1; i >= 0; i--) {
                ((TvRemoteProviderProxy) this.mProviderProxies.get(i)).stop();
            }
        }
    }

    private void scanPackages() {
        if (this.mRunning) {
            TvRemoteProviderProxy providerProxy;
            if (DEBUG) {
                Log.d(TAG, "scanPackages()");
            }
            int targetIndex = 0;
            for (ResolveInfo resolveInfo : this.mPackageManager.queryIntentServicesAsUser(new Intent("com.android.media.tv.remoteprovider.TvRemoteProvider"), 0, this.mUserId)) {
                ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                if (serviceInfo != null && verifyServiceTrusted(serviceInfo)) {
                    int sourceIndex = findProvider(serviceInfo.packageName, serviceInfo.name);
                    int targetIndex2;
                    if (sourceIndex < 0) {
                        providerProxy = new TvRemoteProviderProxy(this.mContext, new ComponentName(serviceInfo.packageName, serviceInfo.name), this.mUserId, serviceInfo.applicationInfo.uid);
                        providerProxy.start();
                        targetIndex2 = targetIndex + 1;
                        this.mProviderProxies.add(targetIndex, providerProxy);
                        this.mProvider.addProvider(providerProxy);
                        targetIndex = targetIndex2;
                    } else if (sourceIndex >= targetIndex) {
                        TvRemoteProviderProxy provider = (TvRemoteProviderProxy) this.mProviderProxies.get(sourceIndex);
                        provider.start();
                        provider.rebindIfDisconnected();
                        targetIndex2 = targetIndex + 1;
                        Collections.swap(this.mProviderProxies, sourceIndex, targetIndex);
                        targetIndex = targetIndex2;
                    }
                }
            }
            if (DEBUG) {
                Log.d(TAG, "scanPackages() targetIndex " + targetIndex);
            }
            if (targetIndex < this.mProviderProxies.size()) {
                for (int i = this.mProviderProxies.size() - 1; i >= targetIndex; i--) {
                    providerProxy = (TvRemoteProviderProxy) this.mProviderProxies.get(i);
                    this.mProvider.removeProvider(providerProxy);
                    this.mProviderProxies.remove(providerProxy);
                    providerProxy.stop();
                }
            }
        }
    }

    private boolean verifyServiceTrusted(ServiceInfo serviceInfo) {
        if (serviceInfo.permission == null || !serviceInfo.permission.equals("android.permission.BIND_TV_REMOTE_SERVICE")) {
            Slog.w(TAG, "Ignoring atv remote provider service because it did not require the BIND_TV_REMOTE_SERVICE permission in its manifest: " + serviceInfo.packageName + "/" + serviceInfo.name);
            return false;
        } else if (!serviceInfo.packageName.equals(this.mUnbundledServicePackage)) {
            Slog.w(TAG, "Ignoring atv remote provider service because the package has not been set and/or whitelisted: " + serviceInfo.packageName + "/" + serviceInfo.name);
            return false;
        } else if (hasNecessaryPermissions(serviceInfo.packageName)) {
            return true;
        } else {
            Slog.w(TAG, "Ignoring atv remote provider service because its package does not have TV_VIRTUAL_REMOTE_CONTROLLER permission: " + serviceInfo.packageName);
            return false;
        }
    }

    private boolean hasNecessaryPermissions(String packageName) {
        if (this.mPackageManager.checkPermission("android.permission.TV_VIRTUAL_REMOTE_CONTROLLER", packageName) == 0) {
            return true;
        }
        return false;
    }

    private int findProvider(String packageName, String className) {
        int count = this.mProviderProxies.size();
        for (int i = 0; i < count; i++) {
            if (((TvRemoteProviderProxy) this.mProviderProxies.get(i)).hasComponentName(packageName, className)) {
                return i;
            }
        }
        return -1;
    }
}
