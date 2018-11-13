package com.android.server.vr;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.service.vr.IVrListener;
import android.service.vr.IVrManager;
import android.service.vr.IVrManager.Stub;
import android.service.vr.IVrStateCallbacks;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.SystemService;
import com.android.server.oppo.IElsaManager;
import com.android.server.utils.ManagedApplicationService;
import com.android.server.utils.ManagedApplicationService.BinderChecker;
import com.android.server.utils.ManagedApplicationService.PendingEvent;
import com.android.server.vr.EnabledComponentsObserver.EnabledComponentChangeListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
public class VrManagerService extends SystemService implements EnabledComponentChangeListener {
    private static final int EVENT_LOG_SIZE = 32;
    private static final int INVALID_APPOPS_MODE = -1;
    private static final int MSG_PENDING_VR_STATE_CHANGE = 1;
    private static final int MSG_VR_STATE_CHANGE = 0;
    private static final int PENDING_STATE_DELAY_MS = 300;
    public static final String TAG = "VrManagerService";
    public static final String VR_MANAGER_BINDER_SERVICE = "vrmanager";
    private static final BinderChecker sBinderChecker = null;
    private EnabledComponentsObserver mComponentObserver;
    private Context mContext;
    private ComponentName mCurrentVrModeComponent;
    private int mCurrentVrModeUser;
    private ManagedApplicationService mCurrentVrService;
    private boolean mGuard;
    private final Handler mHandler;
    private final Object mLock;
    private final ArrayDeque<VrState> mLoggingDeque;
    private final NotificationAccessManager mNotifAccessManager;
    private final IBinder mOverlayToken;
    private VrState mPendingState;
    private int mPreviousCoarseLocationMode;
    private int mPreviousManageOverlayMode;
    private final RemoteCallbackList<IVrStateCallbacks> mRemoteCallbacks;
    private final IVrManager mVrManager;
    private boolean mVrModeEnabled;
    private boolean mWasDefaultGranted;

    private final class LocalService extends VrManagerInternal {
        /* synthetic */ LocalService(VrManagerService this$0, LocalService localService) {
            this();
        }

        private LocalService() {
        }

        public void setVrMode(boolean enabled, ComponentName packageName, int userId, ComponentName callingPackage) {
            VrManagerService.this.setVrMode(enabled, packageName, userId, callingPackage, false);
        }

        public void setVrModeImmediate(boolean enabled, ComponentName packageName, int userId, ComponentName callingPackage) {
            VrManagerService.this.setVrMode(enabled, packageName, userId, callingPackage, true);
        }

        public boolean isCurrentVrListener(String packageName, int userId) {
            return VrManagerService.this.isCurrentVrListener(packageName, userId);
        }

        public int hasVrPackage(ComponentName packageName, int userId) {
            return VrManagerService.this.hasVrPackage(packageName, userId);
        }
    }

    private final class NotificationAccessManager {
        private final SparseArray<ArraySet<String>> mAllowedPackages;
        private final ArrayMap<String, Integer> mNotificationAccessPackageToUserId;

        /* synthetic */ NotificationAccessManager(VrManagerService this$0, NotificationAccessManager notificationAccessManager) {
            this();
        }

        private NotificationAccessManager() {
            this.mAllowedPackages = new SparseArray();
            this.mNotificationAccessPackageToUserId = new ArrayMap();
        }

        public void update(Collection<String> packageNames) {
            int currentUserId = ActivityManager.getCurrentUser();
            ArraySet<String> allowed = (ArraySet) this.mAllowedPackages.get(currentUserId);
            if (allowed == null) {
                allowed = new ArraySet();
            }
            for (int i = this.mNotificationAccessPackageToUserId.size() - 1; i >= 0; i--) {
                int grantUserId = ((Integer) this.mNotificationAccessPackageToUserId.valueAt(i)).intValue();
                if (grantUserId != currentUserId) {
                    String packageName = (String) this.mNotificationAccessPackageToUserId.keyAt(i);
                    VrManagerService.this.revokeNotificationListenerAccess(packageName, grantUserId);
                    VrManagerService.this.revokeNotificationPolicyAccess(packageName);
                    VrManagerService.this.revokeCoarseLocationPermissionIfNeeded(packageName, grantUserId);
                    this.mNotificationAccessPackageToUserId.removeAt(i);
                }
            }
            for (String pkg : allowed) {
                if (!packageNames.contains(pkg)) {
                    VrManagerService.this.revokeNotificationListenerAccess(pkg, currentUserId);
                    VrManagerService.this.revokeNotificationPolicyAccess(pkg);
                    VrManagerService.this.revokeCoarseLocationPermissionIfNeeded(pkg, currentUserId);
                    this.mNotificationAccessPackageToUserId.remove(pkg);
                }
            }
            for (String pkg2 : packageNames) {
                if (!allowed.contains(pkg2)) {
                    VrManagerService.this.grantNotificationPolicyAccess(pkg2);
                    VrManagerService.this.grantNotificationListenerAccess(pkg2, currentUserId);
                    VrManagerService.this.grantCoarseLocationPermissionIfNeeded(pkg2, currentUserId);
                    this.mNotificationAccessPackageToUserId.put(pkg2, Integer.valueOf(currentUserId));
                }
            }
            allowed.clear();
            allowed.addAll(packageNames);
            this.mAllowedPackages.put(currentUserId, allowed);
        }
    }

    private static class VrState {
        final ComponentName callingPackage;
        final boolean defaultPermissionsGranted;
        final boolean enabled;
        final ComponentName targetPackageName;
        final long timestamp;
        final int userId;

        VrState(boolean enabled, ComponentName targetPackageName, int userId, ComponentName callingPackage) {
            this.enabled = enabled;
            this.userId = userId;
            this.targetPackageName = targetPackageName;
            this.callingPackage = callingPackage;
            this.defaultPermissionsGranted = false;
            this.timestamp = System.currentTimeMillis();
        }

        VrState(boolean enabled, ComponentName targetPackageName, int userId, ComponentName callingPackage, boolean defaultPermissionsGranted) {
            this.enabled = enabled;
            this.userId = userId;
            this.targetPackageName = targetPackageName;
            this.callingPackage = callingPackage;
            this.defaultPermissionsGranted = defaultPermissionsGranted;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.vr.VrManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.vr.VrManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.vr.VrManagerService.<clinit>():void");
    }

    private static native void initializeNative();

    private static native void setVrModeNative(boolean z);

    public void onEnabledComponentChanged() {
        synchronized (this.mLock) {
            ArraySet<ComponentName> enabledListeners = this.mComponentObserver.getEnabled(ActivityManager.getCurrentUser());
            ArraySet<String> enabledPackages = new ArraySet();
            for (ComponentName n : enabledListeners) {
                if (isDefaultAllowed(n.getPackageName())) {
                    enabledPackages.add(n.getPackageName());
                }
            }
            this.mNotifAccessManager.update(enabledPackages);
            if (this.mCurrentVrService == null) {
                return;
            }
            consumeAndApplyPendingStateLocked();
            if (this.mCurrentVrService == null) {
                return;
            }
            updateCurrentVrServiceLocked(this.mVrModeEnabled, this.mCurrentVrService.getComponent(), this.mCurrentVrService.getUserId(), null);
        }
    }

    private void enforceCallerPermission(String permission) {
        if (this.mContext.checkCallingOrSelfPermission(permission) != 0) {
            throw new SecurityException("Caller does not hold the permission " + permission);
        }
    }

    public VrManagerService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mOverlayToken = new Binder();
        this.mRemoteCallbacks = new RemoteCallbackList();
        this.mPreviousCoarseLocationMode = -1;
        this.mPreviousManageOverlayMode = -1;
        this.mLoggingDeque = new ArrayDeque(32);
        this.mNotifAccessManager = new NotificationAccessManager(this, null);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        boolean state = msg.arg1 == 1;
                        int i = VrManagerService.this.mRemoteCallbacks.beginBroadcast();
                        while (i > 0) {
                            i--;
                            try {
                                ((IVrStateCallbacks) VrManagerService.this.mRemoteCallbacks.getBroadcastItem(i)).onVrStateChanged(state);
                            } catch (RemoteException e) {
                            }
                        }
                        VrManagerService.this.mRemoteCallbacks.finishBroadcast();
                        return;
                    case 1:
                        synchronized (VrManagerService.this.mLock) {
                            VrManagerService.this.consumeAndApplyPendingStateLocked();
                        }
                        return;
                    default:
                        throw new IllegalStateException("Unknown message type: " + msg.what);
                }
            }
        };
        this.mVrManager = new Stub() {
            public void registerListener(IVrStateCallbacks cb) {
                VrManagerService.this.enforceCallerPermission("android.permission.ACCESS_VR_MANAGER");
                if (cb == null) {
                    throw new IllegalArgumentException("Callback binder object is null.");
                }
                VrManagerService.this.addStateCallback(cb);
            }

            public void unregisterListener(IVrStateCallbacks cb) {
                VrManagerService.this.enforceCallerPermission("android.permission.ACCESS_VR_MANAGER");
                if (cb == null) {
                    throw new IllegalArgumentException("Callback binder object is null.");
                }
                VrManagerService.this.removeStateCallback(cb);
            }

            public boolean getVrModeState() {
                return VrManagerService.this.getVrMode();
            }

            protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                if (VrManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                    pw.println("permission denied: can't dump VrManagerService from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                    return;
                }
                pw.println("********* Dump of VrManagerService *********");
                pw.println("Previous state transitions:\n");
                String tab = "  ";
                VrManagerService.this.dumpStateTransitions(pw);
                pw.println("\n\nRemote Callbacks:");
                int i = VrManagerService.this.mRemoteCallbacks.beginBroadcast();
                while (true) {
                    int i2 = i;
                    i = i2 - 1;
                    if (i2 <= 0) {
                        break;
                    }
                    pw.print(tab);
                    pw.print(VrManagerService.this.mRemoteCallbacks.getBroadcastItem(i));
                    if (i > 0) {
                        pw.println(",");
                    }
                }
                VrManagerService.this.mRemoteCallbacks.finishBroadcast();
                pw.println("\n");
                pw.println("Installed VrListenerService components:");
                int userId = VrManagerService.this.mCurrentVrModeUser;
                ArraySet<ComponentName> installed = VrManagerService.this.mComponentObserver.getInstalled(userId);
                if (installed == null || installed.size() == 0) {
                    pw.println("None");
                } else {
                    for (ComponentName n : installed) {
                        pw.print(tab);
                        pw.println(n.flattenToString());
                    }
                }
                pw.println("Enabled VrListenerService components:");
                ArraySet<ComponentName> enabled = VrManagerService.this.mComponentObserver.getEnabled(userId);
                if (enabled == null || enabled.size() == 0) {
                    pw.println("None");
                } else {
                    for (ComponentName n2 : enabled) {
                        pw.print(tab);
                        pw.println(n2.flattenToString());
                    }
                }
                pw.println("\n");
                pw.println("********* End of VrManagerService Dump *********");
            }
        };
    }

    public void onStart() {
        synchronized (this.mLock) {
            initializeNative();
            this.mContext = getContext();
        }
        publishLocalService(VrManagerInternal.class, new LocalService(this, null));
        publishBinderService(VR_MANAGER_BINDER_SERVICE, this.mVrManager.asBinder());
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            synchronized (this.mLock) {
                Looper looper = Looper.getMainLooper();
                Handler handler = new Handler(looper);
                ArrayList<EnabledComponentChangeListener> listeners = new ArrayList();
                listeners.add(this);
                this.mComponentObserver = EnabledComponentsObserver.build(this.mContext, handler, "enabled_vr_listeners", looper, "android.permission.BIND_VR_LISTENER_SERVICE", "android.service.vr.VrListenerService", this.mLock, listeners);
                this.mComponentObserver.rebuildAll();
            }
        }
    }

    public void onStartUser(int userHandle) {
        synchronized (this.mLock) {
            this.mComponentObserver.onUsersChanged();
        }
    }

    public void onSwitchUser(int userHandle) {
        synchronized (this.mLock) {
            this.mComponentObserver.onUsersChanged();
        }
    }

    public void onStopUser(int userHandle) {
        synchronized (this.mLock) {
            this.mComponentObserver.onUsersChanged();
        }
    }

    public void onCleanupUser(int userHandle) {
        synchronized (this.mLock) {
            this.mComponentObserver.onUsersChanged();
        }
    }

    private void updateOverlayStateLocked(String exemptedPackage, int newUserId, int oldUserId) {
        String[] exemptions;
        AppOpsManager appOpsManager = (AppOpsManager) getContext().getSystemService(AppOpsManager.class);
        if (oldUserId != newUserId) {
            appOpsManager.setUserRestrictionForUser(24, false, this.mOverlayToken, null, oldUserId);
        }
        if (exemptedPackage == null) {
            exemptions = new String[0];
        } else {
            exemptions = new String[1];
            exemptions[0] = exemptedPackage;
        }
        appOpsManager.setUserRestrictionForUser(24, this.mVrModeEnabled, this.mOverlayToken, exemptions, newUserId);
    }

    private void updateDependentAppOpsLocked(String newVrServicePackage, int newUserId, String oldVrServicePackage, int oldUserId) {
        if (!Objects.equals(newVrServicePackage, oldVrServicePackage)) {
            long identity = Binder.clearCallingIdentity();
            try {
                updateOverlayStateLocked(newVrServicePackage, newUserId, oldUserId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private boolean updateCurrentVrServiceLocked(boolean enabled, ComponentName component, int userId, ComponentName calling) {
        boolean sendUpdatedCaller = false;
        long identity = Binder.clearCallingIdentity();
        try {
            boolean validUserComponent = this.mComponentObserver.isValid(component, userId) == 0;
            if (this.mVrModeEnabled || enabled) {
                String oldVrServicePackage = this.mCurrentVrService != null ? this.mCurrentVrService.getComponent().getPackageName() : null;
                int oldUserId = this.mCurrentVrModeUser;
                changeVrModeLocked(enabled);
                if (enabled && validUserComponent) {
                    if (this.mCurrentVrService == null) {
                        createAndConnectService(component, userId);
                        sendUpdatedCaller = true;
                    } else if (this.mCurrentVrService.disconnectIfNotMatching(component, userId)) {
                        Slog.i(TAG, "Disconnecting " + this.mCurrentVrService.getComponent() + " for user " + this.mCurrentVrService.getUserId());
                        createAndConnectService(component, userId);
                        sendUpdatedCaller = true;
                    }
                } else if (this.mCurrentVrService != null) {
                    Slog.i(TAG, "Disconnecting " + this.mCurrentVrService.getComponent() + " for user " + this.mCurrentVrService.getUserId());
                    this.mCurrentVrService.disconnect();
                    this.mCurrentVrService = null;
                }
                if (calling != null) {
                    if (!Objects.equals(calling, this.mCurrentVrModeComponent)) {
                        this.mCurrentVrModeComponent = calling;
                        sendUpdatedCaller = true;
                    }
                }
                if (this.mCurrentVrModeUser != userId) {
                    this.mCurrentVrModeUser = userId;
                    sendUpdatedCaller = true;
                }
                updateDependentAppOpsLocked(this.mCurrentVrService != null ? this.mCurrentVrService.getComponent().getPackageName() : null, this.mCurrentVrModeUser, oldVrServicePackage, oldUserId);
                if (this.mCurrentVrService != null && sendUpdatedCaller) {
                    final ComponentName c = this.mCurrentVrModeComponent;
                    this.mCurrentVrService.sendEvent(new PendingEvent() {
                        public void runEvent(IInterface service) throws RemoteException {
                            ((IVrListener) service).focusedActivityChanged(c);
                        }
                    });
                }
                logStateLocked();
                return validUserComponent;
            }
            Binder.restoreCallingIdentity(identity);
            return validUserComponent;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean isDefaultAllowed(String packageName) {
        ApplicationInfo info = null;
        try {
            info = this.mContext.getPackageManager().getApplicationInfo(packageName, 128);
        } catch (NameNotFoundException e) {
        }
        if (info == null || (!info.isSystemApp() && !info.isUpdatedSystemApp())) {
            return false;
        }
        return true;
    }

    private void grantNotificationPolicyAccess(String pkg) {
        ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).setNotificationPolicyAccessGranted(pkg, true);
    }

    private void revokeNotificationPolicyAccess(String pkg) {
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        nm.removeAutomaticZenRules(pkg);
        nm.setNotificationPolicyAccessGranted(pkg, false);
    }

    private void grantNotificationListenerAccess(String pkg, int userId) {
        ArraySet<ComponentName> possibleServices = EnabledComponentsObserver.loadComponentNames(this.mContext.getPackageManager(), userId, "android.service.notification.NotificationListenerService", "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE");
        ContentResolver resolver = this.mContext.getContentResolver();
        ArraySet<String> current = getNotificationListeners(resolver, userId);
        for (ComponentName c : possibleServices) {
            String flatName = c.flattenToString();
            if (Objects.equals(c.getPackageName(), pkg) && !current.contains(flatName)) {
                current.add(flatName);
            }
        }
        if (current.size() > 0) {
            Secure.putStringForUser(resolver, "enabled_notification_listeners", formatSettings(current), userId);
        }
    }

    private void revokeNotificationListenerAccess(String pkg, int userId) {
        ContentResolver resolver = this.mContext.getContentResolver();
        ArraySet<String> current = getNotificationListeners(resolver, userId);
        ArrayList<String> toRemove = new ArrayList();
        for (String c : current) {
            ComponentName component = ComponentName.unflattenFromString(c);
            if (component != null && component.getPackageName().equals(pkg)) {
                toRemove.add(c);
            }
        }
        current.removeAll(toRemove);
        Secure.putStringForUser(resolver, "enabled_notification_listeners", formatSettings(current), userId);
    }

    private void grantCoarseLocationPermissionIfNeeded(String pkg, int userId) {
        if (!isPermissionUserUpdated("android.permission.ACCESS_COARSE_LOCATION", pkg, userId)) {
            this.mContext.getPackageManager().grantRuntimePermission(pkg, "android.permission.ACCESS_COARSE_LOCATION", new UserHandle(userId));
        }
    }

    private void revokeCoarseLocationPermissionIfNeeded(String pkg, int userId) {
        if (!isPermissionUserUpdated("android.permission.ACCESS_COARSE_LOCATION", pkg, userId)) {
            this.mContext.getPackageManager().revokeRuntimePermission(pkg, "android.permission.ACCESS_COARSE_LOCATION", new UserHandle(userId));
        }
    }

    private boolean isPermissionUserUpdated(String permission, String pkg, int userId) {
        if ((this.mContext.getPackageManager().getPermissionFlags(permission, pkg, new UserHandle(userId)) & 3) != 0) {
            return true;
        }
        return false;
    }

    private ArraySet<String> getNotificationListeners(ContentResolver resolver, int userId) {
        String flat = Secure.getStringForUser(resolver, "enabled_notification_listeners", userId);
        ArraySet<String> current = new ArraySet();
        if (flat != null) {
            for (String s : flat.split(":")) {
                if (!TextUtils.isEmpty(s)) {
                    current.add(s);
                }
            }
        }
        return current;
    }

    private static String formatSettings(Collection<String> c) {
        if (c == null || c.isEmpty()) {
            return IElsaManager.EMPTY_PACKAGE;
        }
        StringBuilder b = new StringBuilder();
        boolean start = true;
        for (String s : c) {
            if (!IElsaManager.EMPTY_PACKAGE.equals(s)) {
                if (!start) {
                    b.append(':');
                }
                b.append(s);
                start = false;
            }
        }
        return b.toString();
    }

    private void createAndConnectService(ComponentName component, int userId) {
        this.mCurrentVrService = create(this.mContext, component, userId);
        this.mCurrentVrService.connect();
        Slog.i(TAG, "Connecting " + component + " for user " + userId);
    }

    private void changeVrModeLocked(boolean enabled) {
        if (this.mVrModeEnabled != enabled) {
            this.mVrModeEnabled = enabled;
            Slog.i(TAG, "VR mode " + (this.mVrModeEnabled ? "enabled" : "disabled"));
            setVrModeNative(this.mVrModeEnabled);
            onVrModeChangedLocked();
        }
    }

    private void onVrModeChangedLocked() {
        int i;
        Handler handler = this.mHandler;
        Handler handler2 = this.mHandler;
        if (this.mVrModeEnabled) {
            i = 1;
        } else {
            i = 0;
        }
        handler.sendMessage(handler2.obtainMessage(0, i, 0));
    }

    private static ManagedApplicationService create(Context context, ComponentName component, int userId) {
        return ManagedApplicationService.build(context, component, userId, 17040512, "android.settings.VR_LISTENER_SETTINGS", sBinderChecker);
    }

    private void consumeAndApplyPendingStateLocked() {
        if (this.mPendingState != null) {
            updateCurrentVrServiceLocked(this.mPendingState.enabled, this.mPendingState.targetPackageName, this.mPendingState.userId, this.mPendingState.callingPackage);
            this.mPendingState = null;
        }
    }

    private void logStateLocked() {
        ComponentName currentBoundService;
        if (this.mCurrentVrService == null) {
            currentBoundService = null;
        } else {
            currentBoundService = this.mCurrentVrService.getComponent();
        }
        VrState current = new VrState(this.mVrModeEnabled, currentBoundService, this.mCurrentVrModeUser, this.mCurrentVrModeComponent, this.mWasDefaultGranted);
        if (this.mLoggingDeque.size() == 32) {
            this.mLoggingDeque.removeFirst();
        }
        this.mLoggingDeque.add(current);
    }

    private void dumpStateTransitions(PrintWriter pw) {
        SimpleDateFormat d = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        String tab = "  ";
        if (this.mLoggingDeque.size() == 0) {
            pw.print(tab);
            pw.println("None");
        }
        for (VrState state : this.mLoggingDeque) {
            pw.print(d.format(new Date(state.timestamp)));
            pw.print(tab);
            pw.print("State changed to:");
            pw.print(tab);
            pw.println(state.enabled ? "ENABLED" : "DISABLED");
            if (state.enabled) {
                String str;
                pw.print(tab);
                pw.print("User=");
                pw.println(state.userId);
                pw.print(tab);
                pw.print("Current VR Activity=");
                pw.println(state.callingPackage == null ? "None" : state.callingPackage.flattenToString());
                pw.print(tab);
                pw.print("Bound VrListenerService=");
                if (state.targetPackageName == null) {
                    str = "None";
                } else {
                    str = state.targetPackageName.flattenToString();
                }
                pw.println(str);
                if (state.defaultPermissionsGranted) {
                    pw.print(tab);
                    pw.println("Default permissions granted to the bound VrListenerService.");
                }
            }
        }
    }

    private void setVrMode(boolean enabled, ComponentName targetPackageName, int userId, ComponentName callingPackage, boolean immediate) {
        synchronized (this.mLock) {
            if (!enabled) {
                if (!(this.mCurrentVrService == null || immediate)) {
                    if (this.mPendingState == null) {
                        this.mHandler.sendEmptyMessageDelayed(1, 300);
                    }
                    this.mPendingState = new VrState(enabled, targetPackageName, userId, callingPackage);
                    return;
                }
            }
            this.mHandler.removeMessages(1);
            this.mPendingState = null;
            updateCurrentVrServiceLocked(enabled, targetPackageName, userId, callingPackage);
        }
    }

    private int hasVrPackage(ComponentName targetPackageName, int userId) {
        int isValid;
        synchronized (this.mLock) {
            isValid = this.mComponentObserver.isValid(targetPackageName, userId);
        }
        return isValid;
    }

    /* JADX WARNING: Missing block: B:14:0x0024, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isCurrentVrListener(String packageName, int userId) {
        boolean z = false;
        synchronized (this.mLock) {
            if (this.mCurrentVrService == null) {
                return false;
            } else if (this.mCurrentVrService.getComponent().getPackageName().equals(packageName) && userId == this.mCurrentVrService.getUserId()) {
                z = true;
            }
        }
    }

    private void addStateCallback(IVrStateCallbacks cb) {
        this.mRemoteCallbacks.register(cb);
    }

    private void removeStateCallback(IVrStateCallbacks cb) {
        this.mRemoteCallbacks.unregister(cb);
    }

    private boolean getVrMode() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mVrModeEnabled;
        }
        return z;
    }
}
