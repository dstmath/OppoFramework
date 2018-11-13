package com.android.server.am;

import android.app.Dialog;
import android.app.IStopUserCallback;
import android.app.IUserSwitchObserver;
import android.content.IIntentReceiver;
import android.content.IIntentReceiver.Stub;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IProgressListener;
import android.os.IRemoteCallback;
import android.os.IUserManager;
import android.os.OppoManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.util.ArraySet;
import android.util.IntArray;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.AgingCriticalEvent;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.oppo.IElsaManager;
import com.android.server.pm.UserManagerService;
import com.oppo.rutils.RUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
final class UserController {
    private static final String ACTION_OPPO_BOOT_COMPLETED = "android.intent.action.OPPO_BOOT_COMPLETED";
    private static final boolean DEBUG_OPPO_BT_LIGHT = true;
    static final int MAX_RUNNING_USERS = 3;
    private static final String TAG = null;
    static final int USER_SWITCH_TIMEOUT = 2000;
    private volatile ArraySet<String> mCurWaitingUserSwitchCallbacks;
    private int[] mCurrentProfileIds;
    private int mCurrentUserId;
    private final Handler mHandler;
    private final LockPatternUtils mLockPatternUtils;
    private OppoBroadcastManager mOppoBroadcastManager;
    private final ActivityManagerService mService;
    private int[] mStartedUserArray;
    @GuardedBy("mService")
    private final SparseArray<UserState> mStartedUsers;
    private int mTargetUserId;
    private final ArrayList<Integer> mUserLru;
    private volatile UserManagerService mUserManager;
    private UserManagerInternal mUserManagerInternal;
    private final SparseIntArray mUserProfileGroupIdsSelfLocked;
    private final RemoteCallbackList<IUserSwitchObserver> mUserSwitchObservers;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.UserController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.UserController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.UserController.<clinit>():void");
    }

    UserController(ActivityManagerService service) {
        this.mCurrentUserId = 0;
        this.mTargetUserId = -10000;
        this.mStartedUsers = new SparseArray();
        this.mUserLru = new ArrayList();
        int[] iArr = new int[1];
        iArr[0] = 0;
        this.mStartedUserArray = iArr;
        this.mCurrentProfileIds = new int[0];
        this.mUserProfileGroupIdsSelfLocked = new SparseIntArray();
        this.mUserSwitchObservers = new RemoteCallbackList();
        this.mOppoBroadcastManager = null;
        this.mService = service;
        this.mHandler = this.mService.mHandler;
        this.mStartedUsers.put(0, new UserState(UserHandle.SYSTEM));
        this.mUserLru.add(Integer.valueOf(0));
        this.mLockPatternUtils = new LockPatternUtils(this.mService.mContext);
        updateStartedUserArrayLocked();
    }

    void finishUserSwitch(UserState uss) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                finishUserBoot(uss);
                startProfilesLocked();
                stopRunningUsersLocked(3);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void stopRunningUsersLocked(int maxRunningUsers) {
        int num = this.mUserLru.size();
        int i = 0;
        while (num > maxRunningUsers && i < this.mUserLru.size()) {
            Integer oldUserId = (Integer) this.mUserLru.get(i);
            UserState oldUss = (UserState) this.mStartedUsers.get(oldUserId.intValue());
            if (oldUss == null) {
                this.mUserLru.remove(i);
                num--;
            } else if (oldUss.state == 4 || oldUss.state == 5) {
                num--;
                i++;
            } else if (oldUserId.intValue() == 0 || oldUserId.intValue() == this.mCurrentUserId) {
                if (UserInfo.isSystemOnly(oldUserId.intValue())) {
                    num--;
                }
                i++;
            } else {
                if (stopUsersLocked(oldUserId.intValue(), false, null) != 0) {
                    num--;
                }
                num--;
                i++;
            }
        }
    }

    private void finishUserBoot(UserState uss) {
        finishUserBoot(uss, null);
    }

    /* JADX WARNING: Missing block: B:21:0x0129, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:22:0x012c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishUserBoot(UserState uss, IIntentReceiver resultTo) {
        int userId = uss.mHandle.getIdentifier();
        Slog.d(TAG, "Finishing user boot " + userId);
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mStartedUsers.get(userId) != uss) {
                } else {
                    if (uss.setState(0, 1)) {
                        getUserManagerInternal().setUserState(userId, uss.state);
                        MetricsLogger.histogram(this.mService.mContext, "framework_locked_boot_completed", (int) (SystemClock.elapsedRealtime() / 1000));
                        Intent intent = new Intent("android.intent.action.LOCKED_BOOT_COMPLETED", null);
                        intent.putExtra("android.intent.extra.user_handle", userId);
                        intent.addFlags(150994944);
                        ActivityManagerService activityManagerService = this.mService;
                        String[] strArr = new String[1];
                        strArr[0] = "android.permission.RECEIVE_BOOT_COMPLETED";
                        activityManagerService.broadcastIntentLocked(null, null, intent, null, resultTo, 0, null, null, strArr, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
                        AgingCriticalEvent instance = AgingCriticalEvent.getInstance();
                        String str = AgingCriticalEvent.EVENT_SYSTEM_BOOTCOMPLETE;
                        String[] strArr2 = new String[1];
                        strArr2[0] = "finishUserBoot called,userId:" + userId;
                        instance.writeEvent(str, strArr2);
                    }
                    if (getUserManager().isManagedProfile(userId)) {
                        UserInfo parent = getUserManager().getProfileParent(userId);
                        if (parent != null) {
                            if (isUserRunningLocked(parent.id, 4)) {
                                Slog.d(TAG, "User " + userId + " (parent " + parent.id + "): attempting unlock because parent is unlocked");
                                maybeUnlockUser(userId);
                            }
                        }
                        Slog.d(TAG, "User " + userId + " (parent " + (parent == null ? "<null>" : String.valueOf(parent.id)) + "): delaying unlock because parent is locked");
                    } else {
                        maybeUnlockUser(userId);
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:0x003e, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:21:0x0041, code:
            if (r0 == false) goto L_0x0071;
     */
    /* JADX WARNING: Missing block: B:22:0x0043, code:
            r6.mUnlockProgress.start();
            r6.mUnlockProgress.setProgress(5, r5.mService.mContext.getString(17040311));
            r5.mUserManager.onBeforeUnlockUser(r1);
            r6.mUnlockProgress.setProgress(20);
            r5.mHandler.obtainMessage(61, r1, 0, r6).sendToTarget();
     */
    /* JADX WARNING: Missing block: B:23:0x0071, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishUserUnlocking(UserState uss) {
        int userId = uss.mHandle.getIdentifier();
        boolean proceedWithUnlock = false;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mStartedUsers.get(uss.mHandle.getIdentifier()) != uss) {
                } else if (!StorageManager.isUserKeyUnlocked(userId)) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else if (uss.setState(1, 2)) {
                    getUserManagerInternal().setUserState(userId, uss.state);
                    proceedWithUnlock = true;
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: Missing block: B:34:0x0125, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:35:0x0128, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void finishUserUnlocked(UserState uss) {
        int userId = uss.mHandle.getIdentifier();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mStartedUsers.get(uss.mHandle.getIdentifier()) != uss) {
                } else if (!StorageManager.isUserKeyUnlocked(userId)) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } else if (uss.setState(2, 3)) {
                    getUserManagerInternal().setUserState(userId, uss.state);
                    uss.mUnlockProgress.finish();
                    Intent unlockedIntent = new Intent("android.intent.action.USER_UNLOCKED");
                    unlockedIntent.putExtra("android.intent.extra.user_handle", userId);
                    unlockedIntent.addFlags(1342177280);
                    this.mService.broadcastIntentLocked(null, null, unlockedIntent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, userId);
                    if (getUserInfo(userId).isManagedProfile()) {
                        UserInfo parent = getUserManager().getProfileParent(userId);
                        if (parent != null) {
                            Intent intent = new Intent("android.intent.action.MANAGED_PROFILE_UNLOCKED");
                            intent.putExtra("android.intent.extra.USER", UserHandle.of(userId));
                            intent.addFlags(1342177280);
                            this.mService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, parent.id);
                        }
                    }
                    UserInfo info = getUserInfo(userId);
                    if (Objects.equals(info.lastLoggedInFingerprint, Build.FINGERPRINT)) {
                        finishUserUnlockedCompleted(uss);
                    } else {
                        boolean quiet = info.isManagedProfile() ? uss.tokenProvided ? !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId) : true : false;
                        final UserState userState = uss;
                        new PreBootBroadcaster(this.mService, userId, null, quiet) {
                            public void onFinished() {
                                UserController.this.finishUserUnlockedCompleted(userState);
                            }
                        }.sendNext();
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void finishUserUnlockedCompleted(UserState uss) {
        int userId = uss.mHandle.getIdentifier();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mStartedUsers.get(uss.mHandle.getIdentifier()) != uss) {
                } else {
                    UserInfo userInfo = getUserInfo(userId);
                    if (userInfo == null) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    } else if (StorageManager.isUserKeyUnlocked(userId)) {
                        this.mUserManager.onUserLoggedIn(userId);
                        if (!(userInfo.isInitialized() || userId == 0)) {
                            Slog.d(TAG, "Initializing user #" + userId);
                            Intent intent = new Intent("android.intent.action.USER_INITIALIZE");
                            intent.addFlags(268435456);
                            final UserInfo userInfo2 = userInfo;
                            this.mService.broadcastIntentLocked(null, null, intent, null, new Stub() {
                                public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                                    UserController.this.getUserManager().makeInitialized(userInfo2.id);
                                }
                            }, 0, null, null, null, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
                        }
                        Slog.d(TAG, "Sending BOOT_COMPLETE user #" + userId);
                        MetricsLogger.histogram(this.mService.mContext, "framework_boot_completed", (int) (SystemClock.elapsedRealtime() / 1000));
                        Intent intent2 = new Intent("android.intent.action.BOOT_COMPLETED", null);
                        intent2.putExtra("android.intent.extra.user_handle", userId);
                        intent2.addFlags(150994944);
                        ActivityManagerService activityManagerService = this.mService;
                        String[] strArr = new String[1];
                        strArr[0] = "android.permission.RECEIVE_BOOT_COMPLETED";
                        activityManagerService.broadcastIntentLocked(null, null, intent2, null, null, 0, null, null, strArr, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
                        AgingCriticalEvent instance = AgingCriticalEvent.getInstance();
                        String str = AgingCriticalEvent.EVENT_SYSTEM_BOOTCOMPLETE;
                        String[] strArr2 = new String[1];
                        strArr2[0] = "finishUserUnlockedCompleted called,userId:" + userId;
                        instance.writeEvent(str, strArr2);
                        SystemProperties.set("sys.oppo.boot_completed", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    } else {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void sendOppoBootCompleteBroadcast() {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                for (int i = 0; i < this.mStartedUsers.size(); i++) {
                    int userId = this.mStartedUsers.keyAt(i);
                    if (userId == OppoMultiAppManager.USER_ID) {
                        Slog.d(TAG, "Sending oppo boot completed broadcast to 999, skip!");
                    } else {
                        Intent intent = new Intent(ACTION_OPPO_BOOT_COMPLETED, null);
                        intent.putExtra("android.intent.extra.user_handle", userId);
                        intent.addFlags(134217728);
                        Slog.d(TAG, "AMS: sendOppoBootCompleteBroadcast begin:user # " + userId);
                        this.mService.broadcastOppoBootComleteLocked(intent, new Stub() {
                            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                                Slog.d(UserController.TAG, "AMS: ACTION_OPPO_BOOT_COMPLETED process finish.");
                                if (UserController.this.mOppoBroadcastManager != null) {
                                    UserController.this.mOppoBroadcastManager.informReadyToCheckJumpQueue();
                                }
                            }
                        }, userId);
                        Slog.d(TAG, "AMS: sendOppoBootCompleteBroadcast end.");
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        recordRootState();
    }

    void setOppoBroadcastManager(OppoBroadcastManager oppoBcMgr) {
        this.mOppoBroadcastManager = oppoBcMgr;
    }

    int stopUser(int userId, boolean force, IStopUserCallback callback) {
        if (this.mService.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: switchUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (userId < 0 || userId == 0) {
            throw new IllegalArgumentException("Can't stop system user " + userId);
        } else {
            int stopUsersLocked;
            this.mService.enforceShellRestriction("no_debugging_features", userId);
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    stopUsersLocked = stopUsersLocked(userId, force, callback);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return stopUsersLocked;
        }
    }

    private int stopUsersLocked(int userId, boolean force, IStopUserCallback callback) {
        if (userId == 0) {
            return -3;
        }
        if (isCurrentUserLocked(userId)) {
            return -2;
        }
        int[] usersToStop = getUsersToStopLocked(userId);
        for (int relatedUserId : usersToStop) {
            if (relatedUserId == 0 || isCurrentUserLocked(relatedUserId)) {
                if (ActivityManagerDebugConfig.DEBUG_MU) {
                    Slog.i(TAG, "stopUsersLocked cannot stop related user " + relatedUserId);
                }
                if (!force) {
                    return -4;
                }
                Slog.i(TAG, "Force stop user " + userId + ". Related users will not be stopped");
                stopSingleUserLocked(userId, callback);
                return 0;
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i(TAG, "stopUsersLocked usersToStop=" + Arrays.toString(usersToStop));
        }
        for (int userIdToStop : usersToStop) {
            stopSingleUserLocked(userIdToStop, userIdToStop == userId ? callback : null);
        }
        return 0;
    }

    private void stopSingleUserLocked(int userId, IStopUserCallback callback) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i(TAG, "stopSingleUserLocked userId=" + userId);
        }
        UserState uss = (UserState) this.mStartedUsers.get(userId);
        if (uss == null) {
            if (callback != null) {
                final IStopUserCallback iStopUserCallback = callback;
                final int i = userId;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            iStopUserCallback.userStopped(i);
                        } catch (RemoteException e) {
                        }
                    }
                });
            }
            return;
        }
        if (callback != null) {
            uss.mStopCallbacks.add(callback);
        }
        if (!(uss.state == 4 || uss.state == 5)) {
            uss.setState(4);
            getUserManagerInternal().setUserState(userId, uss.state);
            updateStartedUserArrayLocked();
            long ident = Binder.clearCallingIdentity();
            try {
                Intent stoppingIntent = new Intent("android.intent.action.USER_STOPPING");
                stoppingIntent.addFlags(1073741824);
                stoppingIntent.putExtra("android.intent.extra.user_handle", userId);
                stoppingIntent.putExtra("android.intent.extra.SHUTDOWN_USERSPACE_ONLY", true);
                final int i2 = userId;
                final UserState userState = uss;
                IIntentReceiver stoppingReceiver = new Stub() {
                    public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                        Handler -get2 = UserController.this.mHandler;
                        final int i = i2;
                        final UserState userState = userState;
                        -get2.post(new Runnable() {
                            public void run() {
                                UserController.this.finishUserStopping(i, userState);
                            }
                        });
                    }
                };
                this.mService.clearBroadcastQueueForUserLocked(userId);
                ActivityManagerService activityManagerService = this.mService;
                String[] strArr = new String[1];
                strArr[0] = "android.permission.INTERACT_ACROSS_USERS";
                activityManagerService.broadcastIntentLocked(null, null, stoppingIntent, null, stoppingReceiver, 0, null, null, strArr, -1, null, true, false, ActivityManagerService.MY_PID, 1000, -1);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX WARNING: Missing block: B:12:0x002c, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
            getUserManagerInternal().setUserState(r21, r22.state);
            r20.mService.mBatteryStatsService.noteEvent(16391, java.lang.Integer.toString(r21), r21);
            r20.mService.mSystemServiceManager.stopUser(r21);
            r19 = r20.mService;
     */
    /* JADX WARNING: Missing block: B:13:0x005e, code:
            monitor-enter(r19);
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
            r20.mService.broadcastIntentLocked(null, null, r5, null, r7, 0, null, null, null, -1, null, true, false, com.android.server.am.ActivityManagerService.MY_PID, 1000, r21);
     */
    /* JADX WARNING: Missing block: B:16:0x007a, code:
            monitor-exit(r19);
     */
    /* JADX WARNING: Missing block: B:21:0x0087, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void finishUserStopping(int userId, UserState uss) {
        Intent shutdownIntent = new Intent("android.intent.action.ACTION_SHUTDOWN");
        final UserState userState = uss;
        IIntentReceiver shutdownReceiver = new Stub() {
            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                Handler -get2 = UserController.this.mHandler;
                final UserState userState = userState;
                -get2.post(new Runnable() {
                    public void run() {
                        UserController.this.finishUserStopped(userState);
                    }
                });
            }
        };
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (uss.state != 4) {
                } else {
                    uss.setState(5);
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void finishUserStopped(UserState uss) {
        ArrayList<IStopUserCallback> callbacks;
        boolean stopped;
        int userId = uss.mHandle.getIdentifier();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                callbacks = new ArrayList(uss.mStopCallbacks);
                if (this.mStartedUsers.get(userId) != uss) {
                    stopped = false;
                } else if (uss.state != 5) {
                    stopped = false;
                } else {
                    stopped = true;
                    this.mStartedUsers.remove(userId);
                    getUserManagerInternal().removeUserState(userId);
                    this.mUserLru.remove(Integer.valueOf(userId));
                    updateStartedUserArrayLocked();
                    this.mService.onUserStoppedLocked(userId);
                    forceStopUserLocked(userId, "finish user");
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        for (int i = 0; i < callbacks.size(); i++) {
            if (stopped) {
                try {
                    ((IStopUserCallback) callbacks.get(i)).userStopped(userId);
                } catch (RemoteException e) {
                }
            } else {
                ((IStopUserCallback) callbacks.get(i)).userStopAborted(userId);
            }
        }
        if (stopped) {
            this.mService.mSystemServiceManager.cleanupUser(userId);
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    this.mService.mStackSupervisor.removeUserLocked(userId);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (getUserInfo(userId).isEphemeral()) {
                this.mUserManager.removeUser(userId);
            }
        }
    }

    private int[] getUsersToStopLocked(int userId) {
        int startedUsersSize = this.mStartedUsers.size();
        IntArray userIds = new IntArray();
        userIds.add(userId);
        synchronized (this.mUserProfileGroupIdsSelfLocked) {
            int userGroupId = this.mUserProfileGroupIdsSelfLocked.get(userId, -10000);
            for (int i = 0; i < startedUsersSize; i++) {
                int startedUserId = ((UserState) this.mStartedUsers.valueAt(i)).mHandle.getIdentifier();
                boolean sameGroup = userGroupId != -10000 ? userGroupId == this.mUserProfileGroupIdsSelfLocked.get(startedUserId, -10000) : false;
                boolean sameUserId = startedUserId == userId;
                if (sameGroup && !sameUserId) {
                    userIds.add(startedUserId);
                }
            }
        }
        return userIds.toArray();
    }

    private void forceStopUserLocked(int userId, String reason) {
        this.mService.forceStopPackageLocked(null, -1, false, false, true, false, false, userId, reason);
        Intent intent = new Intent("android.intent.action.USER_STOPPED");
        intent.addFlags(1342177280);
        intent.putExtra("android.intent.extra.user_handle", userId);
        this.mService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, -1);
    }

    private void stopGuestOrEphemeralUserIfBackground() {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                int num = this.mUserLru.size();
                for (int i = 0; i < num; i++) {
                    Integer oldUserId = (Integer) this.mUserLru.get(i);
                    UserState oldUss = (UserState) this.mStartedUsers.get(oldUserId.intValue());
                    if (!(oldUserId.intValue() == 0 || oldUserId.intValue() == this.mCurrentUserId || oldUss.state == 4 || oldUss.state == 5)) {
                        UserInfo userInfo = getUserInfo(oldUserId.intValue());
                        if (userInfo.isEphemeral()) {
                            ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).onEphemeralUserStop(oldUserId.intValue());
                        }
                        if (userInfo.isGuest() || userInfo.isEphemeral()) {
                            stopUsersLocked(oldUserId.intValue(), true, null);
                            break;
                        }
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void startProfilesLocked() {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i(TAG, "startProfilesLocked");
        }
        List<UserInfo> profiles = getUserManager().getProfiles(this.mCurrentUserId, false);
        List<UserInfo> profilesToStart = new ArrayList(profiles.size());
        for (UserInfo user : profiles) {
            if (!((user.flags & 16) != 16 || user.id == this.mCurrentUserId || user.isQuietModeEnabled())) {
                profilesToStart.add(user);
            }
        }
        int profilesToStartSize = profilesToStart.size();
        int i = 0;
        while (i < profilesToStartSize && i < 2) {
            startUser(((UserInfo) profilesToStart.get(i)).id, false);
            i++;
        }
        if (i < profilesToStartSize) {
            Slog.w(TAG, "More profiles than MAX_RUNNING_USERS");
        }
    }

    private UserManagerService getUserManager() {
        UserManagerService userManager = this.mUserManager;
        if (userManager != null) {
            return userManager;
        }
        userManager = (UserManagerService) IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        this.mUserManager = userManager;
        return userManager;
    }

    private IMountService getMountService() {
        return IMountService.Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
    }

    boolean startUser(int userId, boolean foreground) {
        if (this.mService.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: switchUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        Slog.i(TAG, "Starting userid:" + userId + " fg:" + foreground);
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mService) {
                ActivityManagerService.boostPriorityForLockedSection();
                int oldUserId = this.mCurrentUserId;
                if (oldUserId == userId) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                    return true;
                }
                this.mService.mStackSupervisor.setLockTaskModeLocked(null, 0, "startUser", false);
                UserInfo userInfo = getUserInfo(userId);
                if (userInfo == null) {
                    Slog.w(TAG, "No user info for user #" + userId);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                    return false;
                }
                Intent intent;
                if (foreground) {
                    if (userInfo.isManagedProfile()) {
                        Slog.w(TAG, "Cannot switch to User #" + userId + ": not a full user");
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    }
                }
                if (foreground) {
                    this.mService.mWindowManager.startFreezingScreen(17432706, 17432705);
                }
                boolean needStart = false;
                if (this.mStartedUsers.get(userId) == null) {
                    UserState userState = new UserState(UserHandle.of(userId));
                    this.mStartedUsers.put(userId, userState);
                    getUserManagerInternal().setUserState(userId, userState.state);
                    updateStartedUserArrayLocked();
                    needStart = true;
                }
                UserState uss = (UserState) this.mStartedUsers.get(userId);
                Integer userIdInt = Integer.valueOf(userId);
                this.mUserLru.remove(userIdInt);
                this.mUserLru.add(userIdInt);
                if (foreground) {
                    this.mCurrentUserId = userId;
                    this.mService.updateUserConfigurationLocked();
                    this.mTargetUserId = -10000;
                    updateCurrentProfileIdsLocked();
                    this.mService.mWindowManager.setCurrentUser(userId, this.mCurrentProfileIds);
                    this.mService.mWindowManager.lockNow(null);
                } else {
                    Integer currentUserIdInt = Integer.valueOf(this.mCurrentUserId);
                    updateCurrentProfileIdsLocked();
                    this.mService.mWindowManager.setCurrentProfileIds(this.mCurrentProfileIds);
                    this.mUserLru.remove(currentUserIdInt);
                    this.mUserLru.add(currentUserIdInt);
                }
                if (uss.state == 4) {
                    uss.setState(uss.lastState);
                    getUserManagerInternal().setUserState(userId, uss.state);
                    updateStartedUserArrayLocked();
                    needStart = true;
                } else if (uss.state == 5) {
                    uss.setState(0);
                    getUserManagerInternal().setUserState(userId, uss.state);
                    updateStartedUserArrayLocked();
                    needStart = true;
                }
                if (uss.state == 0) {
                    getUserManager().onBeforeStartUser(userId);
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(42, userId, 0));
                }
                if (foreground) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(43, userId, oldUserId));
                    this.mHandler.removeMessages(34);
                    this.mHandler.removeMessages(36);
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(34, oldUserId, userId, uss));
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(36, oldUserId, userId, uss), 2000);
                }
                if (needStart) {
                    intent = new Intent("android.intent.action.USER_STARTED");
                    intent.addFlags(1342177280);
                    intent.putExtra("android.intent.extra.user_handle", userId);
                    this.mService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, userId);
                }
                if (foreground) {
                    moveUserToForegroundLocked(uss, oldUserId, userId);
                } else {
                    this.mService.mUserController.finishUserBoot(uss);
                }
                if (needStart) {
                    intent = new Intent("android.intent.action.USER_STARTING");
                    intent.addFlags(1073741824);
                    intent.putExtra("android.intent.extra.user_handle", userId);
                    ActivityManagerService activityManagerService = this.mService;
                    IIntentReceiver anonymousClass7 = new Stub() {
                        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
                        }
                    };
                    String[] strArr = new String[1];
                    strArr[0] = "android.permission.INTERACT_ACROSS_USERS";
                    activityManagerService.broadcastIntentLocked(null, null, intent, null, anonymousClass7, 0, null, null, strArr, -1, null, true, false, ActivityManagerService.MY_PID, 1000, -1);
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
                return true;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean startUserInForeground(int userId, Dialog dlg) {
        boolean result = startUser(userId, true);
        dlg.dismiss();
        return result;
    }

    boolean unlockUser(int userId, byte[] token, byte[] secret, IProgressListener listener) {
        if (this.mService.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: unlockUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        long binderToken = Binder.clearCallingIdentity();
        try {
            boolean unlockUserCleared = unlockUserCleared(userId, token, secret, listener);
            return unlockUserCleared;
        } finally {
            Binder.restoreCallingIdentity(binderToken);
        }
    }

    boolean maybeUnlockUser(int userId) {
        return unlockUserCleared(userId, null, null, null);
    }

    private static void notifyFinished(int userId, IProgressListener listener) {
        if (listener != null) {
            try {
                listener.onFinished(userId, null);
            } catch (RemoteException e) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x003c A:{Splitter: B:6:0x0016, ExcHandler: android.os.RemoteException (r4_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:16:0x003c, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            android.util.Slog.w(TAG, "Failed to unlock: " + r4.getMessage());
     */
    /* JADX WARNING: Missing block: B:27:0x006e, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
            finishUserUnlocking(r11);
            r3 = new android.util.ArraySet();
            r13 = r16.mService;
     */
    /* JADX WARNING: Missing block: B:28:0x007f, code:
            monitor-enter(r13);
     */
    /* JADX WARNING: Missing block: B:30:?, code:
            com.android.server.am.ActivityManagerService.boostPriorityForLockedSection();
            r5 = 0;
     */
    /* JADX WARNING: Missing block: B:32:0x008c, code:
            if (r5 >= r16.mStartedUsers.size()) goto L_0x00e3;
     */
    /* JADX WARNING: Missing block: B:33:0x008e, code:
            r9 = r16.mStartedUsers.keyAt(r5);
            r7 = getUserManager().getProfileParent(r9);
     */
    /* JADX WARNING: Missing block: B:34:0x009e, code:
            if (r7 == null) goto L_0x00de;
     */
    /* JADX WARNING: Missing block: B:36:0x00a4, code:
            if (r7.id != r17) goto L_0x00de;
     */
    /* JADX WARNING: Missing block: B:38:0x00a8, code:
            if (r9 == r17) goto L_0x00de;
     */
    /* JADX WARNING: Missing block: B:39:0x00aa, code:
            android.util.Slog.d(TAG, "User " + r9 + " (parent " + r7.id + "): attempting unlock because parent was just unlocked");
            r3.add(java.lang.Integer.valueOf(r9));
     */
    /* JADX WARNING: Missing block: B:40:0x00de, code:
            r5 = r5 + 1;
     */
    /* JADX WARNING: Missing block: B:42:0x00e3, code:
            monitor-exit(r13);
     */
    /* JADX WARNING: Missing block: B:46:0x0102, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean unlockUserCleared(int userId, byte[] token, byte[] secret, IProgressListener listener) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (!StorageManager.isUserKeyUnlocked(userId)) {
                    UserInfo userInfo = getUserInfo(userId);
                    getMountService().unlockUserKey(userId, userInfo.serialNumber, token, secret);
                }
            } catch (Exception e) {
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
            UserState uss = (UserState) this.mStartedUsers.get(userId);
            if (uss == null) {
                notifyFinished(userId, listener);
                ActivityManagerService.resetPriorityAfterLockedSection();
                return false;
            }
            boolean z;
            uss.mUnlockProgress.addListener(listener);
            if (token != null) {
                z = true;
            } else {
                z = false;
            }
            uss.tokenProvided = z;
        }
        int size = childProfilesToUnlock.size();
        for (int i = 0; i < size; i++) {
            maybeUnlockUser(((Integer) childProfilesToUnlock.valueAt(i)).intValue());
        }
        return true;
    }

    void showUserSwitchDialog(Pair<UserInfo, UserInfo> fromToUserPair) {
        new UserSwitchingDialog(this.mService, this.mService.mContext, (UserInfo) fromToUserPair.first, (UserInfo) fromToUserPair.second, true).show();
    }

    void dispatchForegroundProfileChanged(int userId) {
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        for (int i = 0; i < observerCount; i++) {
            try {
                ((IUserSwitchObserver) this.mUserSwitchObservers.getBroadcastItem(i)).onForegroundProfileSwitch(userId);
            } catch (RemoteException e) {
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    void dispatchUserSwitchComplete(int userId) {
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        for (int i = 0; i < observerCount; i++) {
            try {
                ((IUserSwitchObserver) this.mUserSwitchObservers.getBroadcastItem(i)).onUserSwitchComplete(userId);
            } catch (RemoteException e) {
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    private void stopBackgroundUsersIfEnforced(int oldUserId) {
        if (oldUserId != 0 && hasUserRestriction("no_run_in_background", oldUserId)) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (ActivityManagerDebugConfig.DEBUG_MU) {
                        Slog.i(TAG, "stopBackgroundUsersIfEnforced stopping " + oldUserId + " and related users");
                    }
                    stopUsersLocked(oldUserId, false, null);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    void timeoutUserSwitch(UserState uss, int oldUserId, int newUserId) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                Slog.w(TAG, "User switch timeout: from " + oldUserId + " to " + newUserId + ". Observers that didn't send results: " + this.mCurWaitingUserSwitchCallbacks);
                sendContinueUserSwitchLocked(uss, oldUserId, newUserId);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void dispatchUserSwitch(UserState uss, int oldUserId, int newUserId) {
        Slog.d(TAG, "Dispatch onUserSwitching oldUser #" + oldUserId + " newUser #" + newUserId);
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        if (observerCount > 0) {
            final ArraySet<String> curWaitingUserSwitchCallbacks = new ArraySet();
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    uss.switching = true;
                    this.mCurWaitingUserSwitchCallbacks = curWaitingUserSwitchCallbacks;
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            final AtomicInteger waitingCallbacksCount = new AtomicInteger(observerCount);
            for (int i = 0; i < observerCount; i++) {
                try {
                    final String name = "#" + i + " " + this.mUserSwitchObservers.getBroadcastCookie(i);
                    synchronized (this.mService) {
                        ActivityManagerService.boostPriorityForLockedSection();
                        curWaitingUserSwitchCallbacks.add(name);
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    final UserState userState = uss;
                    final int i2 = oldUserId;
                    final int i3 = newUserId;
                    ((IUserSwitchObserver) this.mUserSwitchObservers.getBroadcastItem(i)).onUserSwitching(newUserId, new IRemoteCallback.Stub() {
                        /* JADX WARNING: Missing block: B:12:0x0034, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
                        /* JADX WARNING: Missing block: B:13:0x0037, code:
            return;
     */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        public void sendResult(Bundle data) throws RemoteException {
                            synchronized (UserController.this.mService) {
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    if (curWaitingUserSwitchCallbacks != UserController.this.mCurWaitingUserSwitchCallbacks) {
                                    } else {
                                        curWaitingUserSwitchCallbacks.remove(name);
                                        if (waitingCallbacksCount.decrementAndGet() == 0) {
                                            UserController.this.sendContinueUserSwitchLocked(userState, i2, i3);
                                        }
                                    }
                                } finally {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                        }
                    });
                } catch (RemoteException e) {
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        } else {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    sendContinueUserSwitchLocked(uss, oldUserId, newUserId);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    void sendContinueUserSwitchLocked(UserState uss, int oldUserId, int newUserId) {
        this.mCurWaitingUserSwitchCallbacks = null;
        this.mHandler.removeMessages(36);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(35, oldUserId, newUserId, uss));
    }

    void continueUserSwitch(UserState uss, int oldUserId, int newUserId) {
        Slog.d(TAG, "Continue user switch oldUser #" + oldUserId + ", newUser #" + newUserId);
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mService.mWindowManager.stopFreezingScreen();
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        uss.switching = false;
        this.mHandler.removeMessages(56);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(56, newUserId, 0));
        stopGuestOrEphemeralUserIfBackground();
        stopBackgroundUsersIfEnforced(oldUserId);
    }

    void moveUserToForegroundLocked(UserState uss, int oldUserId, int newUserId) {
        if (this.mService.mStackSupervisor.switchUserLocked(newUserId, uss)) {
            this.mService.startHomeActivityLocked(newUserId, "moveUserToForeground");
        } else {
            this.mService.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        }
        EventLogTags.writeAmSwitchUser(newUserId);
        sendUserSwitchBroadcastsLocked(oldUserId, newUserId);
    }

    void sendUserSwitchBroadcastsLocked(int oldUserId, int newUserId) {
        List<UserInfo> profiles;
        int count;
        int i;
        int profileUserId;
        Intent intent;
        long ident = Binder.clearCallingIdentity();
        if (oldUserId >= 0) {
            try {
                profiles = getUserManager().getProfiles(oldUserId, false);
                count = profiles.size();
                for (i = 0; i < count; i++) {
                    profileUserId = ((UserInfo) profiles.get(i)).id;
                    intent = new Intent("android.intent.action.USER_BACKGROUND");
                    intent.addFlags(1342177280);
                    intent.putExtra("android.intent.extra.user_handle", profileUserId);
                    this.mService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, profileUserId);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        if (newUserId >= 0) {
            profiles = getUserManager().getProfiles(newUserId, false);
            count = profiles.size();
            for (i = 0; i < count; i++) {
                profileUserId = ((UserInfo) profiles.get(i)).id;
                intent = new Intent("android.intent.action.USER_FOREGROUND");
                intent.addFlags(1342177280);
                intent.putExtra("android.intent.extra.user_handle", profileUserId);
                this.mService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, profileUserId);
            }
            intent = new Intent("android.intent.action.USER_SWITCHED");
            intent.addFlags(1342177280);
            intent.putExtra("android.intent.extra.user_handle", newUserId);
            ActivityManagerService activityManagerService = this.mService;
            String[] strArr = new String[1];
            strArr[0] = "android.permission.MANAGE_USERS";
            activityManagerService.broadcastIntentLocked(null, null, intent, null, null, 0, null, null, strArr, -1, null, false, false, ActivityManagerService.MY_PID, 1000, -1);
        }
        Binder.restoreCallingIdentity(ident);
    }

    int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, int allowMode, String name, String callerPackage) {
        int callingUserId = UserHandle.getUserId(callingUid);
        if (callingUserId == userId) {
            return userId;
        }
        if (callingUserId == OppoMultiAppManager.USER_ID) {
            return callingUserId;
        }
        int targetUserId = unsafeConvertIncomingUserLocked(userId);
        if (!(callingUid == 0 || callingUid == 1000)) {
            boolean allow;
            if (this.mService.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS_FULL", callingPid, callingUid, -1, true) == 0) {
                allow = true;
            } else if (allowMode == 2) {
                allow = false;
            } else if (this.mService.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS", callingPid, callingUid, -1, true) != 0) {
                allow = false;
            } else if (allowMode == 0) {
                allow = true;
            } else if (allowMode == 1) {
                allow = isSameProfileGroup(callingUserId, targetUserId);
            } else {
                throw new IllegalArgumentException("Unknown mode: " + allowMode);
            }
            if (!allow) {
                if (userId == -3) {
                    targetUserId = callingUserId;
                } else {
                    StringBuilder builder = new StringBuilder(128);
                    builder.append("Permission Denial: ");
                    builder.append(name);
                    if (callerPackage != null) {
                        builder.append(" from ");
                        builder.append(callerPackage);
                    }
                    builder.append(" asks to run as user ");
                    builder.append(userId);
                    builder.append(" but is calling from user ");
                    builder.append(UserHandle.getUserId(callingUid));
                    builder.append("; this requires ");
                    builder.append("android.permission.INTERACT_ACROSS_USERS_FULL");
                    if (allowMode != 2) {
                        builder.append(" or ");
                        builder.append("android.permission.INTERACT_ACROSS_USERS");
                    }
                    String msg = builder.toString();
                    Slog.w(TAG, msg);
                    throw new SecurityException(msg);
                }
            }
        }
        if (!allowAll && targetUserId < 0) {
            throw new IllegalArgumentException("Call does not support special user #" + targetUserId);
        } else if (callingUid != USER_SWITCH_TIMEOUT || targetUserId < 0 || !hasUserRestriction("no_debugging_features", targetUserId)) {
            return targetUserId;
        } else {
            throw new SecurityException("Shell does not have permission to access user " + targetUserId + "\n " + Debug.getCallers(3));
        }
    }

    int unsafeConvertIncomingUserLocked(int userId) {
        if (userId == -2 || userId == -3) {
            return getCurrentUserIdLocked();
        }
        return userId;
    }

    void registerUserSwitchObserver(IUserSwitchObserver observer, String name) {
        Preconditions.checkNotNull(name, "Observer name cannot be null");
        if (this.mService.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: registerUserSwitchObserver() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        this.mUserSwitchObservers.register(observer, name);
    }

    void unregisterUserSwitchObserver(IUserSwitchObserver observer) {
        this.mUserSwitchObservers.unregister(observer);
    }

    UserState getStartedUserStateLocked(int userId) {
        return (UserState) this.mStartedUsers.get(userId);
    }

    boolean hasStartedUserState(int userId) {
        return this.mStartedUsers.get(userId) != null;
    }

    public void updateStartedUserArrayLocked() {
        int i;
        UserState uss;
        int num = 0;
        for (i = 0; i < this.mStartedUsers.size(); i++) {
            uss = (UserState) this.mStartedUsers.valueAt(i);
            if (!(uss.state == 4 || uss.state == 5)) {
                num++;
            }
        }
        this.mStartedUserArray = new int[num];
        num = 0;
        for (i = 0; i < this.mStartedUsers.size(); i++) {
            uss = (UserState) this.mStartedUsers.valueAt(i);
            if (!(uss.state == 4 || uss.state == 5)) {
                int num2 = num + 1;
                this.mStartedUserArray[num] = this.mStartedUsers.keyAt(i);
                num = num2;
            }
        }
    }

    void sendBootCompletedLocked(IIntentReceiver resultTo) {
        for (int i = 0; i < this.mStartedUsers.size(); i++) {
            finishUserBoot((UserState) this.mStartedUsers.valueAt(i), resultTo);
        }
    }

    void onSystemReady() {
        updateCurrentProfileIdsLocked();
    }

    private void updateCurrentProfileIdsLocked() {
        int i;
        List<UserInfo> profiles = getUserManager().getProfiles(this.mCurrentUserId, false);
        int[] currentProfileIds = new int[profiles.size()];
        for (i = 0; i < currentProfileIds.length; i++) {
            currentProfileIds[i] = ((UserInfo) profiles.get(i)).id;
        }
        this.mCurrentProfileIds = currentProfileIds;
        synchronized (this.mUserProfileGroupIdsSelfLocked) {
            this.mUserProfileGroupIdsSelfLocked.clear();
            List<UserInfo> users = getUserManager().getUsers(false);
            for (i = 0; i < users.size(); i++) {
                UserInfo user = (UserInfo) users.get(i);
                if (user.profileGroupId != -10000) {
                    this.mUserProfileGroupIdsSelfLocked.put(user.id, user.profileGroupId);
                }
            }
        }
    }

    int[] getStartedUserArrayLocked() {
        return this.mStartedUserArray;
    }

    boolean isUserStoppingOrShuttingDownLocked(int userId) {
        boolean z = true;
        UserState state = getStartedUserStateLocked(userId);
        if (state == null) {
            return false;
        }
        if (!(state.state == 4 || state.state == 5)) {
            z = false;
        }
        return z;
    }

    boolean isUserRunningLocked(int userId, int flags) {
        UserState state = getStartedUserStateLocked(userId);
        if (state == null) {
            return false;
        }
        if ((flags & 1) != 0) {
            return true;
        }
        if ((flags & 2) != 0) {
            switch (state.state) {
                case 0:
                case 1:
                    return true;
                default:
                    return false;
            }
        } else if ((flags & 8) != 0) {
            switch (state.state) {
                case 2:
                case 3:
                    return true;
                default:
                    return false;
            }
        } else if ((flags & 4) == 0) {
            return true;
        } else {
            switch (state.state) {
                case 3:
                    return true;
                default:
                    return false;
            }
        }
    }

    UserInfo getCurrentUser() {
        if (UserHandle.getUserId(Binder.getCallingUid()) == OppoMultiAppManager.USER_ID) {
            return getUserInfo(OppoMultiAppManager.USER_ID);
        }
        if (this.mService.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS") == 0 || this.mService.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            UserInfo currentUserLocked;
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    currentUserLocked = getCurrentUserLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return currentUserLocked;
        }
        String msg = "Permission Denial: getCurrentUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS";
        Slog.w(TAG, msg);
        throw new SecurityException(msg);
    }

    UserInfo getCurrentUserLocked() {
        return getUserInfo(this.mTargetUserId != -10000 ? this.mTargetUserId : this.mCurrentUserId);
    }

    int getCurrentOrTargetUserIdLocked() {
        return this.mTargetUserId != -10000 ? this.mTargetUserId : this.mCurrentUserId;
    }

    int getCurrentUserIdLocked() {
        return this.mCurrentUserId;
    }

    private boolean isCurrentUserLocked(int userId) {
        return userId == getCurrentOrTargetUserIdLocked();
    }

    int setTargetUserIdLocked(int targetUserId) {
        this.mTargetUserId = targetUserId;
        return targetUserId;
    }

    int[] getUsers() {
        UserManagerService ums = getUserManager();
        if (ums != null) {
            return ums.getUserIds();
        }
        int[] iArr = new int[1];
        iArr[0] = 0;
        return iArr;
    }

    UserInfo getUserInfo(int userId) {
        return getUserManager().getUserInfo(userId);
    }

    int[] getUserIds() {
        return getUserManager().getUserIds();
    }

    boolean exists(int userId) {
        return getUserManager().exists(userId);
    }

    boolean hasUserRestriction(String restriction, int userId) {
        return getUserManager().hasUserRestriction(restriction, userId);
    }

    Set<Integer> getProfileIds(int userId) {
        Set<Integer> userIds = new HashSet();
        for (UserInfo user : getUserManager().getProfiles(userId, false)) {
            userIds.add(Integer.valueOf(user.id));
        }
        return userIds;
    }

    boolean isSameProfileGroup(int callingUserId, int targetUserId) {
        boolean z = true;
        if (callingUserId == targetUserId) {
            return true;
        }
        synchronized (this.mUserProfileGroupIdsSelfLocked) {
            int callingProfile = this.mUserProfileGroupIdsSelfLocked.get(callingUserId, -10000);
            int targetProfile = this.mUserProfileGroupIdsSelfLocked.get(targetUserId, -10000);
            if (callingProfile == -10000) {
                z = false;
            } else if (callingProfile != targetProfile) {
                z = false;
            }
        }
        return z;
    }

    boolean isCurrentProfileLocked(int userId) {
        return ArrayUtils.contains(this.mCurrentProfileIds, userId);
    }

    int[] getCurrentProfileIdsLocked() {
        return this.mCurrentProfileIds;
    }

    SparseArray<UserState> getStartedUsers() {
        return this.mStartedUsers;
    }

    /* JADX WARNING: Missing block: B:8:0x0015, code:
            com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:9:0x001e, code:
            if (r4.mLockPatternUtils.isSeparateProfileChallengeEnabled(r5) != false) goto L_0x0027;
     */
    /* JADX WARNING: Missing block: B:10:0x0020, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:13:0x0027, code:
            r0 = (android.app.KeyguardManager) r4.mService.mContext.getSystemService("keyguard");
     */
    /* JADX WARNING: Missing block: B:14:0x0038, code:
            if (r0.isDeviceLocked(r5) == false) goto L_0x003e;
     */
    /* JADX WARNING: Missing block: B:15:0x003a, code:
            r1 = r0.isDeviceSecure(r5);
     */
    /* JADX WARNING: Missing block: B:16:0x003e, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean shouldConfirmCredentials(int userId) {
        boolean z = false;
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mStartedUsers.get(userId) == null) {
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return false;
    }

    boolean isLockScreenDisabled(int userId) {
        return this.mLockPatternUtils.isLockScreenDisabled(userId);
    }

    private UserManagerInternal getUserManagerInternal() {
        if (this.mUserManagerInternal == null) {
            this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        }
        return this.mUserManagerInternal;
    }

    void dump(PrintWriter pw, boolean dumpAll) {
        int i;
        pw.println("  mStartedUsers:");
        for (i = 0; i < this.mStartedUsers.size(); i++) {
            UserState uss = (UserState) this.mStartedUsers.valueAt(i);
            pw.print("    User #");
            pw.print(uss.mHandle.getIdentifier());
            pw.print(": ");
            uss.dump(IElsaManager.EMPTY_PACKAGE, pw);
        }
        pw.print("  mStartedUserArray: [");
        for (i = 0; i < this.mStartedUserArray.length; i++) {
            if (i > 0) {
                pw.print(", ");
            }
            pw.print(this.mStartedUserArray[i]);
        }
        pw.println("]");
        pw.print("  mUserLru: [");
        for (i = 0; i < this.mUserLru.size(); i++) {
            if (i > 0) {
                pw.print(", ");
            }
            pw.print(this.mUserLru.get(i));
        }
        pw.println("]");
        if (dumpAll) {
            pw.print("  mStartedUserArray: ");
            pw.println(Arrays.toString(this.mStartedUserArray));
        }
        synchronized (this.mUserProfileGroupIdsSelfLocked) {
            if (this.mUserProfileGroupIdsSelfLocked.size() > 0) {
                pw.println("  mUserProfileGroupIds:");
                for (i = 0; i < this.mUserProfileGroupIdsSelfLocked.size(); i++) {
                    pw.print("    User #");
                    pw.print(this.mUserProfileGroupIdsSelfLocked.keyAt(i));
                    pw.print(" -> profile #");
                    pw.println(this.mUserProfileGroupIdsSelfLocked.valueAt(i));
                }
            }
        }
    }

    void recordRootState() {
        new Thread() {
            public void run() {
                Slog.v(UserController.TAG, "in recordRootState");
                if (RUtils.OppoRUtilsCompareSystemMD5() == -1) {
                    OppoManager.writeCriticalData(OppoManager.TYPE_ROOT_FLAG, "android_root_yes");
                } else {
                    OppoManager.writeCriticalData(OppoManager.TYPE_ROOT_FLAG, "android_root_no");
                }
            }
        }.start();
    }
}
