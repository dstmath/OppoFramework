package com.android.server;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ServiceWatcher implements ServiceConnection {
    private static final boolean D = false;
    public static final String EXTRA_SERVICE_IS_MULTIUSER = "serviceIsMultiuser";
    public static final String EXTRA_SERVICE_VERSION = "serviceVersion";
    private final String mAction;
    @GuardedBy("mLock")
    private ComponentName mBoundComponent;
    @GuardedBy("mLock")
    private String mBoundPackageName;
    @GuardedBy("mLock")
    private IBinder mBoundService;
    @GuardedBy("mLock")
    private int mBoundUserId = -10000;
    @GuardedBy("mLock")
    private int mBoundVersion = Integer.MIN_VALUE;
    private final Context mContext;
    @GuardedBy("mLock")
    private int mCurrentUserId = 0;
    private final Handler mHandler;
    private final Object mLock = new Object();
    private final Runnable mNewServiceWork;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        public void onPackageUpdateFinished(String packageName, int uid) {
            synchronized (ServiceWatcher.this.mLock) {
                ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
            }
        }

        public void onPackageAdded(String packageName, int uid) {
            synchronized (ServiceWatcher.this.mLock) {
                ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
            }
        }

        public void onPackageRemoved(String packageName, int uid) {
            synchronized (ServiceWatcher.this.mLock) {
                ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
            }
        }

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            synchronized (ServiceWatcher.this.mLock) {
                ServiceWatcher.this.bindBestPackageLocked(null, Objects.equals(packageName, ServiceWatcher.this.mBoundPackageName));
            }
            return super.onPackageChanged(packageName, uid, components);
        }
    };
    private final PackageManager mPm;
    private final String mServicePackageName;
    private final List<HashSet<Signature>> mSignatureSets;
    private final String mTag;

    public static ArrayList<HashSet<Signature>> getSignatureSets(Context context, List<String> initialPackageNames) {
        PackageManager pm = context.getPackageManager();
        ArrayList<HashSet<Signature>> sigSets = new ArrayList();
        int size = initialPackageNames.size();
        for (int i = 0; i < size; i++) {
            String pkg = (String) initialPackageNames.get(i);
            try {
                HashSet<Signature> set = new HashSet();
                set.addAll(Arrays.asList(pm.getPackageInfo(pkg, 1048640).signatures));
                sigSets.add(set);
            } catch (NameNotFoundException e) {
                Log.w("ServiceWatcher", pkg + " not found");
            }
        }
        return sigSets;
    }

    public ServiceWatcher(Context context, String logTag, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Runnable newServiceWork, Handler handler) {
        this.mContext = context;
        this.mTag = logTag;
        this.mAction = action;
        this.mPm = this.mContext.getPackageManager();
        this.mNewServiceWork = newServiceWork;
        this.mHandler = handler;
        Resources resources = context.getResources();
        boolean enableOverlay = resources.getBoolean(overlaySwitchResId);
        ArrayList<String> initialPackageNames = new ArrayList();
        if (enableOverlay) {
            String[] pkgs = resources.getStringArray(initialPackageNamesResId);
            if (pkgs != null) {
                initialPackageNames.addAll(Arrays.asList(pkgs));
            }
            this.mServicePackageName = null;
        } else {
            String servicePackageName = resources.getString(defaultServicePackageNameResId);
            if (servicePackageName != null) {
                initialPackageNames.add(servicePackageName);
            }
            this.mServicePackageName = servicePackageName;
        }
        this.mSignatureSets = getSignatureSets(context, initialPackageNames);
    }

    public boolean start() {
        if (isServiceMissing()) {
            return false;
        }
        synchronized (this.mLock) {
            bindBestPackageLocked(this.mServicePackageName, false);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    ServiceWatcher.this.switchUser(userId);
                } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    ServiceWatcher.this.unlockUser(userId);
                }
            }
        }, UserHandle.ALL, intentFilter, null, this.mHandler);
        if (this.mServicePackageName == null) {
            this.mPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
        }
        return true;
    }

    private boolean isServiceMissing() {
        return this.mPm.queryIntentServicesAsUser(new Intent(this.mAction), 786432, this.mCurrentUserId).isEmpty();
    }

    private boolean bindBestPackageLocked(String justCheckThisPackage, boolean forceRebind) {
        Intent intent = new Intent(this.mAction);
        if (justCheckThisPackage != null) {
            intent.setPackage(justCheckThisPackage);
        }
        List<ResolveInfo> rInfos = this.mPm.queryIntentServicesAsUser(intent, 268435584, this.mCurrentUserId);
        int bestVersion = Integer.MIN_VALUE;
        ComponentName bestComponent = null;
        boolean z = false;
        if (rInfos != null) {
            for (ResolveInfo rInfo : rInfos) {
                ComponentName component = rInfo.serviceInfo.getComponentName();
                String packageName = component.getPackageName();
                try {
                    if (isSignatureMatch(this.mPm.getPackageInfo(packageName, 268435520).signatures)) {
                        int version = Integer.MIN_VALUE;
                        boolean isMultiuser = false;
                        if (rInfo.serviceInfo.metaData != null) {
                            version = rInfo.serviceInfo.metaData.getInt(EXTRA_SERVICE_VERSION, Integer.MIN_VALUE);
                            isMultiuser = rInfo.serviceInfo.metaData.getBoolean(EXTRA_SERVICE_IS_MULTIUSER);
                        }
                        if (version > bestVersion) {
                            bestVersion = version;
                            bestComponent = component;
                            z = isMultiuser;
                        }
                        if ((rInfo.serviceInfo.applicationInfo.flags & 1) == 1) {
                            Log.d(this.mTag, "BestPackage in /system: " + packageName);
                            break;
                        }
                    } else {
                        Log.w(this.mTag, packageName + " resolves service " + this.mAction + ", but has wrong signature, ignoring");
                    }
                } catch (NameNotFoundException e) {
                    Log.wtf(this.mTag, e);
                }
            }
        }
        if (bestComponent == null) {
            Slog.w(this.mTag, "Odd, no component found for service " + this.mAction);
            unbindLocked();
            return false;
        }
        int userId = z ? 0 : this.mCurrentUserId;
        boolean alreadyBound;
        if (Objects.equals(bestComponent, this.mBoundComponent) && bestVersion == this.mBoundVersion) {
            alreadyBound = userId == this.mBoundUserId;
        } else {
            alreadyBound = false;
        }
        if (forceRebind || (alreadyBound ^ 1) != 0) {
            unbindLocked();
            bindToPackageLocked(bestComponent, bestVersion, userId);
        }
        return true;
    }

    private void unbindLocked() {
        ComponentName component = this.mBoundComponent;
        this.mBoundComponent = null;
        this.mBoundPackageName = null;
        this.mBoundVersion = Integer.MIN_VALUE;
        this.mBoundUserId = -10000;
        if (component != null) {
            this.mContext.unbindService(this);
        }
    }

    private void bindToPackageLocked(ComponentName component, int version, int userId) {
        Intent intent = new Intent(this.mAction);
        intent.setComponent(component);
        this.mBoundComponent = component;
        this.mBoundPackageName = component.getPackageName();
        this.mBoundVersion = version;
        this.mBoundUserId = userId;
        this.mContext.bindServiceAsUser(intent, this, 1073741829, new UserHandle(userId));
    }

    public static boolean isSignatureMatch(Signature[] signatures, List<HashSet<Signature>> sigSets) {
        if (signatures == null) {
            return false;
        }
        HashSet<Signature> inputSet = new HashSet();
        for (Signature s : signatures) {
            inputSet.add(s);
        }
        for (HashSet<Signature> referenceSet : sigSets) {
            if (referenceSet.equals(inputSet)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSignatureMatch(Signature[] signatures) {
        return isSignatureMatch(signatures, this.mSignatureSets);
    }

    public void onServiceConnected(ComponentName component, IBinder binder) {
        synchronized (this.mLock) {
            if (component.equals(this.mBoundComponent)) {
                this.mBoundService = binder;
                if (!(this.mHandler == null || this.mNewServiceWork == null)) {
                    this.mHandler.post(this.mNewServiceWork);
                }
            } else {
                Log.w(this.mTag, "unexpected onServiceConnected: " + component);
            }
        }
    }

    public void onServiceDisconnected(ComponentName component) {
        synchronized (this.mLock) {
            if (component.equals(this.mBoundComponent)) {
                this.mBoundService = null;
            }
        }
    }

    public String getBestPackageName() {
        String str;
        synchronized (this.mLock) {
            str = this.mBoundPackageName;
        }
        return str;
    }

    public int getBestVersion() {
        int i;
        synchronized (this.mLock) {
            i = this.mBoundVersion;
        }
        return i;
    }

    public IBinder getBinder() {
        IBinder iBinder;
        synchronized (this.mLock) {
            iBinder = this.mBoundService;
        }
        return iBinder;
    }

    public void switchUser(int userId) {
        synchronized (this.mLock) {
            this.mCurrentUserId = userId;
            bindBestPackageLocked(this.mServicePackageName, false);
        }
    }

    public void unlockUser(int userId) {
        synchronized (this.mLock) {
            if (userId == this.mCurrentUserId) {
                bindBestPackageLocked(this.mServicePackageName, false);
            }
        }
    }
}
