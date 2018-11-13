package com.android.server.trust;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.admin.DevicePolicyManager;
import android.app.trust.ITrustListener;
import android.app.trust.ITrustManager.Stub;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.Settings.Secure;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.Xml;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocationManagerService;
import com.android.server.SystemService;
import com.android.server.job.controllers.JobStatus;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
public class TrustManagerService extends SystemService {
    private static final boolean DEBUG = false;
    private static final int MSG_CLEANUP_USER = 8;
    private static final int MSG_DISPATCH_UNLOCK_ATTEMPT = 3;
    private static final int MSG_ENABLED_AGENTS_CHANGED = 4;
    private static final int MSG_FLUSH_TRUST_USUALLY_MANAGED = 10;
    private static final int MSG_KEYGUARD_SHOWING_CHANGED = 6;
    private static final int MSG_REGISTER_LISTENER = 1;
    private static final int MSG_START_USER = 7;
    private static final int MSG_SWITCH_USER = 9;
    private static final int MSG_UNLOCK_USER = 11;
    private static final int MSG_UNREGISTER_LISTENER = 2;
    private static final String PERMISSION_PROVIDE_AGENT = "android.permission.PROVIDE_TRUST_AGENT";
    private static final String TAG = "TrustManagerService";
    private static final Intent TRUST_AGENT_INTENT = null;
    private static final int TRUST_USUALLY_MANAGED_FLUSH_DELAY = 120000;
    private final ArraySet<AgentInfo> mActiveAgents;
    private final ActivityManager mActivityManager;
    final TrustArchive mArchive;
    private final Context mContext;
    private int mCurrentUser;
    @GuardedBy("mDeviceLockedForUser")
    private final SparseBooleanArray mDeviceLockedForUser;
    private final Handler mHandler;
    private final LockPatternUtils mLockPatternUtils;
    private final PackageMonitor mPackageMonitor;
    private final Receiver mReceiver;
    private final IBinder mService;
    private final StrongAuthTracker mStrongAuthTracker;
    private boolean mTrustAgentsCanRun;
    private final ArrayList<ITrustListener> mTrustListeners;
    @GuardedBy("mDeviceLockedForUser")
    private final SparseBooleanArray mTrustUsuallyManagedForUser;
    @GuardedBy("mUserIsTrusted")
    private final SparseBooleanArray mUserIsTrusted;
    private final UserManager mUserManager;

    private static final class AgentInfo {
        TrustAgentWrapper agent;
        ComponentName component;
        Drawable icon;
        CharSequence label;
        ComponentName settings;
        int userId;

        /* synthetic */ AgentInfo(AgentInfo agentInfo) {
            this();
        }

        private AgentInfo() {
        }

        public boolean equals(Object other) {
            boolean z = false;
            if (!(other instanceof AgentInfo)) {
                return false;
            }
            AgentInfo o = (AgentInfo) other;
            if (this.component.equals(o.component) && this.userId == o.userId) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.component.hashCode() * 31) + this.userId;
        }
    }

    private class Receiver extends BroadcastReceiver {
        /* synthetic */ Receiver(TrustManagerService this$0, Receiver receiver) {
            this();
        }

        private Receiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int userId;
            if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                TrustManagerService.this.refreshAgentList(getSendingUserId());
                TrustManagerService.this.updateDevicePolicyFeatures();
            } else if ("android.intent.action.USER_ADDED".equals(action)) {
                userId = getUserId(intent);
                if (userId > 0) {
                    TrustManagerService.this.maybeEnableFactoryTrustAgents(TrustManagerService.this.mLockPatternUtils, userId);
                }
            } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                userId = getUserId(intent);
                if (userId > 0) {
                    synchronized (TrustManagerService.this.mUserIsTrusted) {
                        TrustManagerService.this.mUserIsTrusted.delete(userId);
                    }
                    synchronized (TrustManagerService.this.mDeviceLockedForUser) {
                        TrustManagerService.this.mDeviceLockedForUser.delete(userId);
                    }
                    TrustManagerService.this.refreshAgentList(userId);
                    TrustManagerService.this.refreshDeviceLockedForUser(userId);
                }
            }
        }

        private int getUserId(Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -100);
            if (userId > 0) {
                return userId;
            }
            Slog.wtf(TrustManagerService.TAG, "EXTRA_USER_HANDLE missing or invalid, value=" + userId);
            return -100;
        }

        public void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
            filter.addAction("android.intent.action.USER_ADDED");
            filter.addAction("android.intent.action.USER_REMOVED");
            context.registerReceiverAsUser(this, UserHandle.ALL, filter, null, null);
        }
    }

    private class StrongAuthTracker extends com.android.internal.widget.LockPatternUtils.StrongAuthTracker {
        SparseBooleanArray mStartFromSuccessfulUnlock = new SparseBooleanArray();

        public StrongAuthTracker(Context context) {
            super(context);
        }

        public void onStrongAuthRequiredChanged(int userId) {
            this.mStartFromSuccessfulUnlock.delete(userId);
            TrustManagerService.this.refreshAgentList(userId);
            TrustManagerService.this.updateTrust(userId, 0);
        }

        boolean canAgentsRunForUser(int userId) {
            if (this.mStartFromSuccessfulUnlock.get(userId)) {
                return true;
            }
            return super.isTrustAllowedForUser(userId);
        }

        void allowTrustFromUnlock(int userId) {
            if (userId < 0) {
                throw new IllegalArgumentException("userId must be a valid user: " + userId);
            }
            boolean previous = canAgentsRunForUser(userId);
            this.mStartFromSuccessfulUnlock.put(userId, true);
            if (canAgentsRunForUser(userId) != previous) {
                TrustManagerService.this.refreshAgentList(userId);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.trust.TrustManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.trust.TrustManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.trust.TrustManagerService.<clinit>():void");
    }

    public TrustManagerService(Context context) {
        super(context);
        this.mActiveAgents = new ArraySet();
        this.mTrustListeners = new ArrayList();
        this.mReceiver = new Receiver(this, null);
        this.mArchive = new TrustArchive();
        this.mUserIsTrusted = new SparseBooleanArray();
        this.mDeviceLockedForUser = new SparseBooleanArray();
        this.mTrustUsuallyManagedForUser = new SparseBooleanArray();
        this.mTrustAgentsCanRun = false;
        this.mCurrentUser = 0;
        this.mService = new Stub() {
            public void reportUnlockAttempt(boolean authenticated, int userId) throws RemoteException {
                enforceReportPermission();
                TrustManagerService.this.mHandler.obtainMessage(3, authenticated ? 1 : 0, userId).sendToTarget();
            }

            public void reportEnabledTrustAgentsChanged(int userId) throws RemoteException {
                enforceReportPermission();
                TrustManagerService.this.mHandler.removeMessages(4);
                TrustManagerService.this.mHandler.sendEmptyMessage(4);
            }

            public void reportKeyguardShowingChanged() throws RemoteException {
                enforceReportPermission();
                TrustManagerService.this.mHandler.removeMessages(6);
                TrustManagerService.this.mHandler.sendEmptyMessage(6);
            }

            public void registerTrustListener(ITrustListener trustListener) throws RemoteException {
                enforceListenerPermission();
                TrustManagerService.this.mHandler.obtainMessage(1, trustListener).sendToTarget();
            }

            public void unregisterTrustListener(ITrustListener trustListener) throws RemoteException {
                enforceListenerPermission();
                TrustManagerService.this.mHandler.obtainMessage(2, trustListener).sendToTarget();
            }

            public boolean isDeviceLocked(int userId) throws RemoteException {
                userId = ActivityManager.handleIncomingUser(AnonymousClass1.getCallingPid(), AnonymousClass1.getCallingUid(), userId, false, true, "isDeviceLocked", null);
                long token = Binder.clearCallingIdentity();
                try {
                    if (!TrustManagerService.this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId)) {
                        userId = TrustManagerService.this.resolveProfileParent(userId);
                    }
                    boolean isDeviceLockedInner = TrustManagerService.this.isDeviceLockedInner(userId);
                    return isDeviceLockedInner;
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }

            public boolean isDeviceSecure(int userId) throws RemoteException {
                userId = ActivityManager.handleIncomingUser(AnonymousClass1.getCallingPid(), AnonymousClass1.getCallingUid(), userId, false, true, "isDeviceSecure", null);
                long token = Binder.clearCallingIdentity();
                try {
                    if (!TrustManagerService.this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId)) {
                        userId = TrustManagerService.this.resolveProfileParent(userId);
                    }
                    boolean isSecure = TrustManagerService.this.mLockPatternUtils.isSecure(userId);
                    return isSecure;
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }

            private void enforceReportPermission() {
                TrustManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_KEYGUARD_SECURE_STORAGE", "reporting trust events");
            }

            private void enforceListenerPermission() {
                TrustManagerService.this.mContext.enforceCallingPermission("android.permission.TRUST_LISTENER", "register trust listener");
            }

            protected void dump(FileDescriptor fd, final PrintWriter fout, String[] args) {
                TrustManagerService.this.mContext.enforceCallingPermission("android.permission.DUMP", "dumping TrustManagerService");
                if (TrustManagerService.this.isSafeMode()) {
                    fout.println("disabled because the system is in safe mode.");
                } else if (TrustManagerService.this.mTrustAgentsCanRun) {
                    final List<UserInfo> userInfos = TrustManagerService.this.mUserManager.getUsers(true);
                    TrustManagerService.this.mHandler.runWithScissors(new Runnable() {
                        public void run() {
                            fout.println("Trust manager state:");
                            for (UserInfo user : userInfos) {
                                AnonymousClass1.this.dumpUser(fout, user, user.id == TrustManagerService.this.mCurrentUser);
                            }
                        }
                    }, 1500);
                } else {
                    fout.println("disabled because the third-party apps can't run yet.");
                }
            }

            private void dumpUser(PrintWriter fout, UserInfo user, boolean isCurrent) {
                Object[] objArr = new Object[3];
                objArr[0] = user.name;
                objArr[1] = Integer.valueOf(user.id);
                objArr[2] = Integer.valueOf(user.flags);
                fout.printf(" User \"%s\" (id=%d, flags=%#x)", objArr);
                if (user.supportsSwitchToByUser()) {
                    if (isCurrent) {
                        fout.print(" (current)");
                    }
                    fout.print(": trusted=" + dumpBool(TrustManagerService.this.aggregateIsTrusted(user.id)));
                    fout.print(", trustManaged=" + dumpBool(TrustManagerService.this.aggregateIsTrustManaged(user.id)));
                    fout.print(", deviceLocked=" + dumpBool(TrustManagerService.this.isDeviceLockedInner(user.id)));
                    fout.print(", strongAuthRequired=" + dumpHex(TrustManagerService.this.mStrongAuthTracker.getStrongAuthForUser(user.id)));
                    fout.println();
                    fout.println("   Enabled agents:");
                    boolean duplicateSimpleNames = false;
                    ArraySet<String> simpleNames = new ArraySet();
                    for (AgentInfo info : TrustManagerService.this.mActiveAgents) {
                        if (info.userId == user.id) {
                            boolean trusted = info.agent.isTrusted();
                            fout.print("    ");
                            fout.println(info.component.flattenToShortString());
                            fout.print("     bound=" + dumpBool(info.agent.isBound()));
                            fout.print(", connected=" + dumpBool(info.agent.isConnected()));
                            fout.print(", managingTrust=" + dumpBool(info.agent.isManagingTrust()));
                            fout.print(", trusted=" + dumpBool(trusted));
                            fout.println();
                            if (trusted) {
                                fout.println("      message=\"" + info.agent.getMessage() + "\"");
                            }
                            if (!info.agent.isConnected()) {
                                fout.println("      restartScheduledAt=" + TrustArchive.formatDuration(info.agent.getScheduledRestartUptimeMillis() - SystemClock.uptimeMillis()));
                            }
                            if (!simpleNames.add(TrustArchive.getSimpleName(info.component))) {
                                duplicateSimpleNames = true;
                            }
                        }
                    }
                    fout.println("   Events:");
                    TrustManagerService.this.mArchive.dump(fout, 50, user.id, "    ", duplicateSimpleNames);
                    fout.println();
                    return;
                }
                fout.println("(managed profile)");
                fout.println("   disabled because switching to this user is not possible.");
            }

            private String dumpBool(boolean b) {
                return b ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0";
            }

            private String dumpHex(int i) {
                return "0x" + Integer.toHexString(i);
            }

            public void setDeviceLockedForUser(int userId, boolean locked) {
                enforceReportPermission();
                long identity = Binder.clearCallingIdentity();
                try {
                    if (TrustManagerService.this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId)) {
                        synchronized (TrustManagerService.this.mDeviceLockedForUser) {
                            TrustManagerService.this.mDeviceLockedForUser.put(userId, locked);
                        }
                        if (locked) {
                            try {
                                ActivityManagerNative.getDefault().notifyLockedProfile(userId);
                            } catch (RemoteException e) {
                            }
                        }
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }

            public boolean isTrustUsuallyManaged(int userId) {
                TrustManagerService.this.mContext.enforceCallingPermission("android.permission.TRUST_LISTENER", "query trust state");
                return TrustManagerService.this.isTrustUsuallyManagedInternal(userId);
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                boolean z = false;
                switch (msg.what) {
                    case 1:
                        TrustManagerService.this.addListener((ITrustListener) msg.obj);
                        return;
                    case 2:
                        TrustManagerService.this.removeListener((ITrustListener) msg.obj);
                        return;
                    case 3:
                        TrustManagerService trustManagerService = TrustManagerService.this;
                        if (msg.arg1 != 0) {
                            z = true;
                        }
                        trustManagerService.dispatchUnlockAttempt(z, msg.arg2);
                        return;
                    case 4:
                        TrustManagerService.this.refreshAgentList(-1);
                        TrustManagerService.this.refreshDeviceLockedForUser(-1);
                        return;
                    case 6:
                        TrustManagerService.this.refreshDeviceLockedForUser(TrustManagerService.this.mCurrentUser);
                        return;
                    case 7:
                    case 8:
                    case 11:
                        TrustManagerService.this.refreshAgentList(msg.arg1);
                        return;
                    case 9:
                        TrustManagerService.this.mCurrentUser = msg.arg1;
                        TrustManagerService.this.refreshDeviceLockedForUser(-1);
                        return;
                    case 10:
                        SparseBooleanArray usuallyManaged;
                        synchronized (TrustManagerService.this.mTrustUsuallyManagedForUser) {
                            usuallyManaged = TrustManagerService.this.mTrustUsuallyManagedForUser.clone();
                        }
                        for (int i = 0; i < usuallyManaged.size(); i++) {
                            int userId = usuallyManaged.keyAt(i);
                            boolean value = usuallyManaged.valueAt(i);
                            if (value != TrustManagerService.this.mLockPatternUtils.isTrustUsuallyManaged(userId)) {
                                TrustManagerService.this.mLockPatternUtils.setTrustUsuallyManaged(value, userId);
                            }
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mPackageMonitor = new PackageMonitor() {
            public void onSomePackagesChanged() {
                TrustManagerService.this.refreshAgentList(-1);
            }

            public boolean onPackageChanged(String packageName, int uid, String[] components) {
                return true;
            }

            public void onPackageDisappeared(String packageName, int reason) {
                TrustManagerService.this.removeAgentsOfPackage(packageName);
            }
        };
        this.mContext = context;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mStrongAuthTracker = new StrongAuthTracker(context);
    }

    public void onStart() {
        publishBinderService("trust", this.mService);
    }

    public void onBootPhase(int phase) {
        if (!isSafeMode()) {
            if (phase == 500) {
                this.mPackageMonitor.register(this.mContext, this.mHandler.getLooper(), UserHandle.ALL, true);
                this.mReceiver.register(this.mContext);
                this.mLockPatternUtils.registerStrongAuthTracker(this.mStrongAuthTracker);
            } else if (phase == 600) {
                this.mTrustAgentsCanRun = true;
                refreshAgentList(-1);
            } else if (phase == 1000) {
                maybeEnableFactoryTrustAgents(this.mLockPatternUtils, 0);
            }
        }
    }

    private void updateTrustAll() {
        for (UserInfo userInfo : this.mUserManager.getUsers(true)) {
            updateTrust(userInfo.id, 0);
        }
    }

    public void updateTrust(int userId, int flags) {
        boolean changed;
        boolean managed = aggregateIsTrustManaged(userId);
        dispatchOnTrustManagedChanged(managed, userId);
        if (this.mStrongAuthTracker.isTrustAllowedForUser(userId) && isTrustUsuallyManagedInternal(userId) != managed) {
            updateTrustUsuallyManaged(userId, managed);
        }
        boolean trusted = aggregateIsTrusted(userId);
        synchronized (this.mUserIsTrusted) {
            changed = this.mUserIsTrusted.get(userId) != trusted;
            this.mUserIsTrusted.put(userId, trusted);
        }
        dispatchOnTrustChanged(trusted, userId, flags);
        if (changed) {
            refreshDeviceLockedForUser(userId);
        }
    }

    private void updateTrustUsuallyManaged(int userId, boolean managed) {
        synchronized (this.mTrustUsuallyManagedForUser) {
            this.mTrustUsuallyManagedForUser.put(userId, managed);
        }
        this.mHandler.removeMessages(10);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(10), JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
    }

    void refreshAgentList(int userIdOrAll) {
        if (this.mTrustAgentsCanRun) {
            if (userIdOrAll != -1 && userIdOrAll < 0) {
                Log.e(TAG, "refreshAgentList(userId=" + userIdOrAll + "): Invalid user handle," + " must be USER_ALL or a specific user.", new Throwable("here"));
                userIdOrAll = -1;
            }
            PackageManager pm = this.mContext.getPackageManager();
            List<UserInfo> userInfos;
            if (userIdOrAll == -1) {
                userInfos = this.mUserManager.getUsers(true);
            } else {
                userInfos = new ArrayList();
                userInfos.add(this.mUserManager.getUserInfo(userIdOrAll));
            }
            LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
            ArraySet<AgentInfo> obsoleteAgents = new ArraySet();
            obsoleteAgents.addAll(this.mActiveAgents);
            for (UserInfo userInfo : userInfos) {
                if (userInfo != null && !userInfo.partial && userInfo.isEnabled() && !userInfo.guestToRemove && userInfo.supportsSwitchToByUser() && StorageManager.isUserKeyUnlocked(userInfo.id) && this.mActivityManager.isUserRunning(userInfo.id) && lockPatternUtils.isSecure(userInfo.id) && this.mStrongAuthTracker.canAgentsRunForUser(userInfo.id)) {
                    DevicePolicyManager dpm = lockPatternUtils.getDevicePolicyManager();
                    boolean disableTrustAgents = (dpm.getKeyguardDisabledFeatures(null, userInfo.id) & 16) != 0;
                    List<ComponentName> enabledAgents = lockPatternUtils.getEnabledTrustAgents(userInfo.id);
                    if (enabledAgents != null) {
                        for (ResolveInfo resolveInfo : resolveAllowedTrustAgents(pm, userInfo.id)) {
                            ComponentName name = getComponentName(resolveInfo);
                            if (enabledAgents.contains(name)) {
                                if (disableTrustAgents) {
                                    List<PersistableBundle> config = dpm.getTrustAgentConfiguration(null, name, userInfo.id);
                                    if (config != null) {
                                        if (config.isEmpty()) {
                                        }
                                    }
                                }
                                AgentInfo agentInfo = new AgentInfo(null);
                                agentInfo.component = name;
                                agentInfo.userId = userInfo.id;
                                if (this.mActiveAgents.contains(agentInfo)) {
                                    obsoleteAgents.remove(agentInfo);
                                } else {
                                    agentInfo.label = resolveInfo.loadLabel(pm);
                                    agentInfo.icon = resolveInfo.loadIcon(pm);
                                    agentInfo.settings = getSettingsComponentName(pm, resolveInfo);
                                    agentInfo.agent = new TrustAgentWrapper(this.mContext, this, new Intent().setComponent(name), userInfo.getUserHandle());
                                    this.mActiveAgents.add(agentInfo);
                                }
                            }
                        }
                    }
                }
            }
            boolean trustMayHaveChanged = false;
            for (int i = 0; i < obsoleteAgents.size(); i++) {
                AgentInfo info = (AgentInfo) obsoleteAgents.valueAt(i);
                if (userIdOrAll == -1 || userIdOrAll == info.userId) {
                    if (info.agent.isManagingTrust()) {
                        trustMayHaveChanged = true;
                    }
                    info.agent.destroy();
                    this.mActiveAgents.remove(info);
                }
            }
            if (trustMayHaveChanged) {
                if (userIdOrAll == -1) {
                    updateTrustAll();
                } else {
                    updateTrust(userIdOrAll, 0);
                }
            }
        }
    }

    boolean isDeviceLockedInner(int userId) {
        boolean z;
        synchronized (this.mDeviceLockedForUser) {
            z = this.mDeviceLockedForUser.get(userId, true);
        }
        return z;
    }

    private void refreshDeviceLockedForUser(int userId) {
        List<UserInfo> userInfos;
        if (userId != -1 && userId < 0) {
            Log.e(TAG, "refreshDeviceLockedForUser(userId=" + userId + "): Invalid user handle," + " must be USER_ALL or a specific user.", new Throwable("here"));
            userId = -1;
        }
        if (userId == -1) {
            userInfos = this.mUserManager.getUsers(true);
        } else {
            userInfos = new ArrayList();
            userInfos.add(this.mUserManager.getUserInfo(userId));
        }
        IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
        for (int i = 0; i < userInfos.size(); i++) {
            UserInfo info = (UserInfo) userInfos.get(i);
            if (!(info == null || info.partial || !info.isEnabled() || info.guestToRemove || !info.supportsSwitchToByUser())) {
                boolean changed;
                int id = info.id;
                boolean secure = this.mLockPatternUtils.isSecure(id);
                boolean trusted = aggregateIsTrusted(id);
                boolean showingKeyguard = true;
                if (this.mCurrentUser == id) {
                    try {
                        showingKeyguard = wm.isKeyguardLocked();
                    } catch (RemoteException e) {
                    }
                }
                boolean deviceLocked = secure && showingKeyguard && !trusted;
                synchronized (this.mDeviceLockedForUser) {
                    changed = isDeviceLockedInner(id) != deviceLocked;
                    this.mDeviceLockedForUser.put(id, deviceLocked);
                }
                if (changed) {
                    dispatchDeviceLocked(id, deviceLocked);
                }
            }
        }
    }

    private void dispatchDeviceLocked(int userId, boolean isLocked) {
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo agent = (AgentInfo) this.mActiveAgents.valueAt(i);
            if (agent.userId == userId) {
                if (isLocked) {
                    agent.agent.onDeviceLocked();
                } else {
                    agent.agent.onDeviceUnlocked();
                }
            }
        }
    }

    void updateDevicePolicyFeatures() {
        boolean changed = false;
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = (AgentInfo) this.mActiveAgents.valueAt(i);
            if (info.agent.isConnected()) {
                info.agent.updateDevicePolicyFeatures();
                changed = true;
            }
        }
        if (changed) {
            this.mArchive.logDevicePolicyChanged();
        }
    }

    private void removeAgentsOfPackage(String packageName) {
        boolean trustMayHaveChanged = false;
        for (int i = this.mActiveAgents.size() - 1; i >= 0; i--) {
            AgentInfo info = (AgentInfo) this.mActiveAgents.valueAt(i);
            if (packageName.equals(info.component.getPackageName())) {
                Log.i(TAG, "Resetting agent " + info.component.flattenToShortString());
                if (info.agent.isManagingTrust()) {
                    trustMayHaveChanged = true;
                }
                info.agent.destroy();
                this.mActiveAgents.removeAt(i);
            }
        }
        if (trustMayHaveChanged) {
            updateTrustAll();
        }
    }

    public void resetAgent(ComponentName name, int userId) {
        boolean trustMayHaveChanged = false;
        for (int i = this.mActiveAgents.size() - 1; i >= 0; i--) {
            AgentInfo info = (AgentInfo) this.mActiveAgents.valueAt(i);
            if (name.equals(info.component) && userId == info.userId) {
                Log.i(TAG, "Resetting agent " + info.component.flattenToShortString());
                if (info.agent.isManagingTrust()) {
                    trustMayHaveChanged = true;
                }
                info.agent.destroy();
                this.mActiveAgents.removeAt(i);
            }
        }
        if (trustMayHaveChanged) {
            updateTrust(userId, 0);
        }
        refreshAgentList(userId);
    }

    private ComponentName getSettingsComponentName(PackageManager pm, ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.metaData == null) {
            return null;
        }
        String str = null;
        XmlResourceParser xmlResourceParser = null;
        Throwable caughtException = null;
        try {
            xmlResourceParser = resolveInfo.serviceInfo.loadXmlMetaData(pm, "android.service.trust.trustagent");
            if (xmlResourceParser == null) {
                Slog.w(TAG, "Can't find android.service.trust.trustagent meta-data");
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                return null;
            }
            Resources res = pm.getResourcesForApplication(resolveInfo.serviceInfo.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(xmlResourceParser);
            int type;
            do {
                type = xmlResourceParser.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            if ("trust-agent".equals(xmlResourceParser.getName())) {
                TypedArray sa = res.obtainAttributes(attrs, R.styleable.TrustAgent);
                str = sa.getString(2);
                sa.recycle();
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                if (caughtException != null) {
                    Slog.w(TAG, "Error parsing : " + resolveInfo.serviceInfo.packageName, caughtException);
                    return null;
                } else if (str == null) {
                    return null;
                } else {
                    if (str.indexOf(47) < 0) {
                        str = resolveInfo.serviceInfo.packageName + "/" + str;
                    }
                    return ComponentName.unflattenFromString(str);
                }
            }
            Slog.w(TAG, "Meta-data does not start with trust-agent tag");
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            return null;
        } catch (Throwable e) {
            caughtException = e;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable e2) {
            caughtException = e2;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable e3) {
            caughtException = e3;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    private ComponentName getComponentName(ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            return null;
        }
        return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
    }

    private void maybeEnableFactoryTrustAgents(LockPatternUtils utils, int userId) {
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "trust_agents_initialized", 0, userId) == 0) {
            List<ResolveInfo> resolveInfos = resolveAllowedTrustAgents(this.mContext.getPackageManager(), userId);
            ArraySet<ComponentName> discoveredAgents = new ArraySet();
            for (ResolveInfo resolveInfo : resolveInfos) {
                ComponentName componentName = getComponentName(resolveInfo);
                if ((resolveInfo.serviceInfo.applicationInfo.flags & 1) == 0) {
                    Log.i(TAG, "Leaving agent " + componentName + " disabled because package " + "is not a system package.");
                } else {
                    discoveredAgents.add(componentName);
                }
            }
            List<ComponentName> previouslyEnabledAgents = utils.getEnabledTrustAgents(userId);
            if (previouslyEnabledAgents != null) {
                discoveredAgents.addAll(previouslyEnabledAgents);
            }
            utils.setEnabledTrustAgents(discoveredAgents, userId);
            Secure.putIntForUser(this.mContext.getContentResolver(), "trust_agents_initialized", 1, userId);
        }
    }

    private List<ResolveInfo> resolveAllowedTrustAgents(PackageManager pm, int userId) {
        List<ResolveInfo> resolveInfos = pm.queryIntentServicesAsUser(TRUST_AGENT_INTENT, 786432, userId);
        ArrayList<ResolveInfo> allowedAgents = new ArrayList(resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (!(resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.applicationInfo == null)) {
                if (pm.checkPermission(PERMISSION_PROVIDE_AGENT, resolveInfo.serviceInfo.packageName) != 0) {
                    Log.w(TAG, "Skipping agent " + getComponentName(resolveInfo) + " because package does not have" + " permission " + PERMISSION_PROVIDE_AGENT + ".");
                } else {
                    allowedAgents.add(resolveInfo);
                }
            }
        }
        return allowedAgents;
    }

    private boolean aggregateIsTrusted(int userId) {
        if (!this.mStrongAuthTracker.isTrustAllowedForUser(userId)) {
            return false;
        }
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = (AgentInfo) this.mActiveAgents.valueAt(i);
            if (info.userId == userId && info.agent.isTrusted()) {
                return true;
            }
        }
        return false;
    }

    private boolean aggregateIsTrustManaged(int userId) {
        if (!this.mStrongAuthTracker.isTrustAllowedForUser(userId)) {
            return false;
        }
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = (AgentInfo) this.mActiveAgents.valueAt(i);
            if (info.userId == userId && info.agent.isManagingTrust()) {
                return true;
            }
        }
        return false;
    }

    private void dispatchUnlockAttempt(boolean successful, int userId) {
        if (successful) {
            this.mStrongAuthTracker.allowTrustFromUnlock(userId);
        }
        for (int i = 0; i < this.mActiveAgents.size(); i++) {
            AgentInfo info = (AgentInfo) this.mActiveAgents.valueAt(i);
            if (info.userId == userId) {
                info.agent.onUnlockAttempt(successful);
            }
        }
    }

    private void addListener(ITrustListener listener) {
        int i = 0;
        while (i < this.mTrustListeners.size()) {
            if (((ITrustListener) this.mTrustListeners.get(i)).asBinder() != listener.asBinder()) {
                i++;
            } else {
                return;
            }
        }
        this.mTrustListeners.add(listener);
        updateTrustAll();
    }

    private void removeListener(ITrustListener listener) {
        for (int i = 0; i < this.mTrustListeners.size(); i++) {
            if (((ITrustListener) this.mTrustListeners.get(i)).asBinder() == listener.asBinder()) {
                this.mTrustListeners.remove(i);
                return;
            }
        }
    }

    private void dispatchOnTrustChanged(boolean enabled, int userId, int flags) {
        if (!enabled) {
            flags = 0;
        }
        int i = 0;
        while (i < this.mTrustListeners.size()) {
            try {
                ((ITrustListener) this.mTrustListeners.get(i)).onTrustChanged(enabled, userId, flags);
            } catch (DeadObjectException e) {
                Slog.d(TAG, "Removing dead TrustListener.");
                this.mTrustListeners.remove(i);
                i--;
            } catch (RemoteException e2) {
                Slog.e(TAG, "Exception while notifying TrustListener.", e2);
            }
            i++;
        }
    }

    private void dispatchOnTrustManagedChanged(boolean managed, int userId) {
        int i = 0;
        while (i < this.mTrustListeners.size()) {
            try {
                ((ITrustListener) this.mTrustListeners.get(i)).onTrustManagedChanged(managed, userId);
            } catch (DeadObjectException e) {
                Slog.d(TAG, "Removing dead TrustListener.");
                this.mTrustListeners.remove(i);
                i--;
            } catch (RemoteException e2) {
                Slog.e(TAG, "Exception while notifying TrustListener.", e2);
            }
            i++;
        }
    }

    public void onStartUser(int userId) {
        this.mHandler.obtainMessage(7, userId, 0, null).sendToTarget();
    }

    public void onCleanupUser(int userId) {
        this.mHandler.obtainMessage(8, userId, 0, null).sendToTarget();
    }

    public void onSwitchUser(int userId) {
        this.mHandler.obtainMessage(9, userId, 0, null).sendToTarget();
    }

    public void onUnlockUser(int userId) {
        this.mHandler.obtainMessage(11, userId, 0, null).sendToTarget();
    }

    /* JADX WARNING: Missing block: B:9:0x0014, code:
            r1 = r4.mLockPatternUtils.isTrustUsuallyManaged(r5);
            r3 = r4.mTrustUsuallyManagedForUser;
     */
    /* JADX WARNING: Missing block: B:10:0x001c, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:12:?, code:
            r0 = r4.mTrustUsuallyManagedForUser.indexOfKey(r5);
     */
    /* JADX WARNING: Missing block: B:13:0x0023, code:
            if (r0 < 0) goto L_0x0030;
     */
    /* JADX WARNING: Missing block: B:14:0x0025, code:
            r2 = r4.mTrustUsuallyManagedForUser.valueAt(r0);
     */
    /* JADX WARNING: Missing block: B:15:0x002b, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:16:0x002c, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            r4.mTrustUsuallyManagedForUser.put(r5, r1);
     */
    /* JADX WARNING: Missing block: B:22:0x0035, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:23:0x0036, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isTrustUsuallyManagedInternal(int userId) {
        synchronized (this.mTrustUsuallyManagedForUser) {
            int i = this.mTrustUsuallyManagedForUser.indexOfKey(userId);
            if (i >= 0) {
                boolean valueAt = this.mTrustUsuallyManagedForUser.valueAt(i);
                return valueAt;
            }
        }
    }

    private int resolveProfileParent(int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            UserInfo parent = this.mUserManager.getProfileParent(userId);
            if (parent != null) {
                int identifier = parent.getUserHandle().getIdentifier();
                return identifier;
            }
            Binder.restoreCallingIdentity(identity);
            return userId;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }
}
