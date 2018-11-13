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
import com.android.server.oppo.IElsaManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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
public class ServiceWatcher implements ServiceConnection {
    private static final boolean D = false;
    public static final String EXTRA_SERVICE_IS_MULTIUSER = "serviceIsMultiuser";
    public static final String EXTRA_SERVICE_VERSION = "serviceVersion";
    private static final String GMS_PACKAGE_NAME = "com.google.android.gms";
    private final String mAction;
    @GuardedBy("mLock")
    private ComponentName mBoundComponent;
    @GuardedBy("mLock")
    private String mBoundPackageName;
    @GuardedBy("mLock")
    private IBinder mBoundService;
    @GuardedBy("mLock")
    private int mBoundUserId;
    @GuardedBy("mLock")
    private int mBoundVersion;
    private final Context mContext;
    @GuardedBy("mLock")
    private int mCurrentUserId;
    private final Handler mHandler;
    private boolean mIsStop;
    private final Object mLock;
    private final Runnable mNewServiceWork;
    private final PackageMonitor mPackageMonitor;
    private final PackageManager mPm;
    private int mPreferPackageNameResId;
    String[] mPreferPkgs;
    private final String mServicePackageName;
    private final List<HashSet<Signature>> mSignatureSets;
    private final String mTag;
    private BroadcastReceiver mUserSwitched;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.ServiceWatcher.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.ServiceWatcher.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.ServiceWatcher.<clinit>():void");
    }

    public static ArrayList<HashSet<Signature>> getSignatureSets(Context context, List<String> initialPackageNames) {
        PackageManager pm = context.getPackageManager();
        ArrayList<HashSet<Signature>> sigSets = new ArrayList();
        int size = initialPackageNames.size();
        for (int i = 0; i < size; i++) {
            String pkg = (String) initialPackageNames.get(i);
            try {
                HashSet<Signature> set = new HashSet();
                set.addAll(Arrays.asList(pm.getPackageInfo(pkg, 64).signatures));
                sigSets.add(set);
            } catch (NameNotFoundException e) {
                Log.w("ServiceWatcher", pkg + " not found");
            }
        }
        return sigSets;
    }

    public ServiceWatcher(Context context, String logTag, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Runnable newServiceWork, Handler handler) {
        this.mPreferPackageNameResId = 0;
        this.mPreferPkgs = null;
        this.mLock = new Object();
        this.mCurrentUserId = 0;
        this.mBoundVersion = Integer.MIN_VALUE;
        this.mBoundUserId = -10000;
        this.mUserSwitched = null;
        this.mIsStop = true;
        this.mPackageMonitor = new PackageMonitor() {
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
            Log.d(this.mTag, "Overlay enabled, packages=" + Arrays.toString(pkgs));
        } else {
            String servicePackageName = resources.getString(defaultServicePackageNameResId);
            if (servicePackageName != null) {
                initialPackageNames.add(servicePackageName);
            }
            this.mServicePackageName = servicePackageName;
            Log.d(this.mTag, "Overlay disabled, default package=" + servicePackageName);
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
        if (this.mUserSwitched == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("android.intent.action.USER_UNLOCKED");
            this.mUserSwitched = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                    if ("android.intent.action.USER_SWITCHED".equals(action)) {
                        ServiceWatcher.this.switchUser(userId);
                    } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                        ServiceWatcher.this.unlockUser(userId);
                    }
                }
            };
            this.mContext.registerReceiverAsUser(this.mUserSwitched, UserHandle.ALL, intentFilter, null, this.mHandler);
        }
        if (this.mServicePackageName == null) {
            this.mPackageMonitor.register(this.mContext, null, UserHandle.ALL, true);
        }
        this.mIsStop = false;
        return true;
    }

    public ServiceWatcher(Context context, String logTag, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, int vendorPackageNamesResId, int preferPackageNamesResId, Runnable newServiceWork, Handler handler) {
        this.mPreferPackageNameResId = 0;
        this.mPreferPkgs = null;
        this.mLock = new Object();
        this.mCurrentUserId = 0;
        this.mBoundVersion = Integer.MIN_VALUE;
        this.mBoundUserId = -10000;
        this.mUserSwitched = null;
        this.mIsStop = true;
        this.mPackageMonitor = /* anonymous class already generated */;
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
            String[] vendorPkgs = resources.getStringArray(vendorPackageNamesResId);
            if (vendorPkgs != null) {
                initialPackageNames.addAll(Arrays.asList(vendorPkgs));
            }
            this.mServicePackageName = null;
            Log.d(this.mTag, "Overlay enabled, packages=" + Arrays.toString(pkgs));
        } else {
            String servicePackageName = resources.getString(defaultServicePackageNameResId);
            if (servicePackageName != null) {
                initialPackageNames.add(servicePackageName);
            }
            this.mServicePackageName = servicePackageName;
            Log.d(this.mTag, "Overlay disabled, default package=" + servicePackageName);
        }
        this.mSignatureSets = getSignatureSets(context, initialPackageNames);
        this.mPreferPackageNameResId = preferPackageNamesResId;
        if (this.mPreferPackageNameResId > 0) {
            this.mPreferPkgs = resources.getStringArray(this.mPreferPackageNameResId);
        } else {
            this.mPreferPkgs = new String[1];
            this.mPreferPkgs[0] = GMS_PACKAGE_NAME;
        }
        if (D) {
            Log.d(this.mTag, "Constructed, mPreferPkgs[0] = " + this.mPreferPkgs[0]);
        }
    }

    public void stop() {
        if (D) {
            Log.d(this.mTag, "Stop");
        }
        synchronized (this.mLock) {
            unbindLocked();
            try {
                if (D) {
                    Log.d(this.mTag, "mPackageMonitor.unregister()");
                }
                this.mPackageMonitor.unregister();
            } catch (Exception e) {
            }
        }
        this.mIsStop = true;
    }

    private boolean isPreferredPackage(String packageName) {
        if (this.mPreferPkgs != null) {
            for (String s : this.mPreferPkgs) {
                if (packageName != null && s.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isServiceMissing() {
        boolean isEmpty;
        synchronized (this.mLock) {
            isEmpty = this.mPm.queryIntentServicesAsUser(new Intent(this.mAction), 786432, this.mCurrentUserId).isEmpty();
        }
        return isEmpty;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0128  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean bindBestPackageLocked(String justCheckThisPackage, boolean forceRebind) {
        Intent intent = new Intent(this.mAction);
        if (justCheckThisPackage != null) {
            intent.setPackage(justCheckThisPackage);
        }
        List<ResolveInfo> rInfos = this.mPm.queryIntentServicesAsUser(intent, 268435584, this.mCurrentUserId);
        int bestVersion = Integer.MIN_VALUE;
        ComponentName bestComponent = null;
        boolean bestIsMultiuser = false;
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
                            bestIsMultiuser = isMultiuser;
                        }
                        if (1 == (rInfo.serviceInfo.applicationInfo.flags & 1)) {
                            if (D) {
                                Log.d(this.mTag, "BestPackage in /system: " + packageName);
                            }
                            if (D) {
                                String str;
                                String str2 = this.mTag;
                                String str3 = "bindBestPackage for %s : %s found %d, %s";
                                Object[] objArr = new Object[4];
                                objArr[0] = this.mAction;
                                if (justCheckThisPackage == null) {
                                    str = IElsaManager.EMPTY_PACKAGE;
                                } else {
                                    str = "(" + justCheckThisPackage + ") ";
                                }
                                objArr[1] = str;
                                objArr[2] = Integer.valueOf(rInfos.size());
                                if (bestComponent == null) {
                                    str = "no new best component";
                                } else {
                                    str = "new best component: " + bestComponent;
                                }
                                objArr[3] = str;
                                Log.d(str2, String.format(str3, objArr));
                            }
                        }
                    } else {
                        Log.w(this.mTag, packageName + " resolves service " + this.mAction + ", but has wrong signature, ignoring");
                    }
                } catch (NameNotFoundException e) {
                    Log.wtf(this.mTag, e);
                }
            }
            if (D) {
            }
        } else if (D) {
            Log.d(this.mTag, "Unable to query intent services for action: " + this.mAction);
        }
        if (bestComponent == null) {
            Slog.w(this.mTag, "Odd, no component found for service " + this.mAction);
            unbindLocked();
            return false;
        }
        int userId = bestIsMultiuser ? 0 : this.mCurrentUserId;
        boolean alreadyBound;
        if (Objects.equals(bestComponent, this.mBoundComponent) && bestVersion == this.mBoundVersion) {
            alreadyBound = userId == this.mBoundUserId;
        } else {
            alreadyBound = false;
        }
        if (forceRebind || !alreadyBound) {
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
            if (D) {
                Log.d(this.mTag, "unbinding " + component);
            }
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
        Log.d(this.mTag, "binding " + component + " (v" + version + ") (u" + userId + ")");
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
                Log.d(this.mTag, component + " connected");
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
            Log.d(this.mTag, component + " disconnected");
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

    /* JADX WARNING: Missing block: B:9:0x0014, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void switchUser(int userId) {
        synchronized (this.mLock) {
            if (!this.mIsStop) {
                this.mCurrentUserId = userId;
                bindBestPackageLocked(this.mServicePackageName, false);
            } else if (D) {
                Log.d(this.mTag, "has stop,no need to switch again!");
            }
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
