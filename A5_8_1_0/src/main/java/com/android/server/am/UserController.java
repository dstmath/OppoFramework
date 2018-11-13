package com.android.server.am;

import android.app.AppGlobals;
import android.app.IStopUserCallback;
import android.app.IUserSwitchObserver;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.net.arp.OppoArpPeer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IProgressListener;
import android.os.IRemoteCallback;
import android.os.IUserManager.Stub;
import android.os.OppoManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.os.storage.IStorageManager;
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
import com.android.server.pm.UserManagerService;
import com.android.server.wm.WindowManagerService;
import com.oppo.rutils.RUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

final class UserController {
    private static final String ACTION_OPPO_BOOT_COMPLETED = "oppo.intent.action.BOOT_COMPLETED";
    private static final boolean DEBUG_OPPO_BT_LIGHT = true;
    static final int MAX_RUNNING_USERS = 3;
    private static final String TAG = "ActivityManager";
    static final int USER_SWITCH_TIMEOUT = 3000;
    @GuardedBy("mLock")
    private volatile ArraySet<String> mCurWaitingUserSwitchCallbacks;
    @GuardedBy("mLock")
    private int[] mCurrentProfileIds;
    @GuardedBy("mLock")
    private volatile int mCurrentUserId;
    private final Handler mHandler;
    private final Injector mInjector;
    private final Object mLock;
    private final LockPatternUtils mLockPatternUtils;
    private OppoBroadcastManager mOppoBroadcastManager;
    @GuardedBy("mLock")
    private int[] mStartedUserArray;
    @GuardedBy("mLock")
    private final SparseArray<UserState> mStartedUsers;
    @GuardedBy("mLock")
    private volatile int mTargetUserId;
    @GuardedBy("mLock")
    private final ArrayList<Integer> mUserLru;
    private volatile UserManagerService mUserManager;
    private final SparseIntArray mUserProfileGroupIdsSelfLocked;
    private final RemoteCallbackList<IUserSwitchObserver> mUserSwitchObservers;
    boolean mUserSwitchUiEnabled;

    static class Injector {
        private final ActivityManagerService mService;
        private UserManagerService mUserManager;
        private UserManagerInternal mUserManagerInternal;

        Injector(ActivityManagerService service) {
            this.mService = service;
        }

        protected Object getLock() {
            return this.mService;
        }

        protected Handler getHandler() {
            return this.mService.mHandler;
        }

        protected Context getContext() {
            return this.mService.mContext;
        }

        protected LockPatternUtils getLockPatternUtils() {
            return new LockPatternUtils(getContext());
        }

        protected int broadcastIntentLocked(Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions, boolean ordered, boolean sticky, int callingPid, int callingUid, int userId) {
            return this.mService.broadcastIntentLocked(null, null, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermissions, appOp, bOptions, ordered, sticky, callingPid, callingUid, userId);
        }

        int checkCallingPermission(String permission) {
            return this.mService.checkCallingPermission(permission);
        }

        WindowManagerService getWindowManager() {
            return this.mService.mWindowManager;
        }

        void activityManagerOnUserStopped(int userId) {
            this.mService.onUserStoppedLocked(userId);
        }

        void systemServiceManagerCleanupUser(int userId) {
            this.mService.mSystemServiceManager.cleanupUser(userId);
        }

        protected UserManagerService getUserManager() {
            if (this.mUserManager == null) {
                this.mUserManager = (UserManagerService) Stub.asInterface(ServiceManager.getService("user"));
            }
            return this.mUserManager;
        }

        UserManagerInternal getUserManagerInternal() {
            if (this.mUserManagerInternal == null) {
                this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
            }
            return this.mUserManagerInternal;
        }

        KeyguardManager getKeyguardManager() {
            return (KeyguardManager) this.mService.mContext.getSystemService(KeyguardManager.class);
        }

        void batteryStatsServiceNoteEvent(int code, String name, int uid) {
            this.mService.mBatteryStatsService.noteEvent(code, name, uid);
        }

        void systemServiceManagerStopUser(int userId) {
            this.mService.mSystemServiceManager.stopUser(userId);
        }

        boolean isRuntimeRestarted() {
            return this.mService.mSystemServiceManager.isRuntimeRestarted();
        }

        boolean isFirstBootOrUpgrade() {
            IPackageManager pm = AppGlobals.getPackageManager();
            try {
                return !pm.isFirstBoot() ? pm.isUpgrade() : true;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        void sendPreBootBroadcast(int userId, boolean quiet, Runnable onFinish) {
            final Runnable runnable = onFinish;
            new PreBootBroadcaster(this.mService, userId, null, quiet) {
                public void onFinished() {
                    runnable.run();
                }
            }.sendNext();
        }

        void activityManagerForceStopPackageLocked(int userId, String reason) {
            this.mService.forceStopPackageLocked(null, -1, false, false, true, false, false, userId, reason);
        }

        int checkComponentPermission(String permission, int pid, int uid, int owningUid, boolean exported) {
            return this.mService.checkComponentPermission(permission, pid, uid, owningUid, exported);
        }

        void startHomeActivityLocked(int userId, String reason) {
            this.mService.startHomeActivityLocked(userId, reason);
        }

        void updateUserConfigurationLocked() {
            this.mService.updateUserConfigurationLocked();
        }

        void clearBroadcastQueueForUserLocked(int userId) {
            this.mService.clearBroadcastQueueForUserLocked(userId);
        }

        void enforceShellRestriction(String restriction, int userId) {
            this.mService.enforceShellRestriction(restriction, userId);
        }

        void showUserSwitchingDialog(UserInfo fromUser, UserInfo toUser) {
            new UserSwitchingDialog(this.mService, this.mService.mContext, fromUser, toUser, true).show();
        }

        ActivityStackSupervisor getActivityStackSupervisor() {
            return this.mService.mStackSupervisor;
        }

        int broadcastOppoBootComleteLocked(Intent intent, IIntentReceiver resultTo, int userId) {
            return this.mService.broadcastOppoBootComleteLocked(intent, resultTo, userId);
        }
    }

    UserController(ActivityManagerService service) {
        this(new Injector(service));
    }

    UserController(Injector injector) {
        this.mCurrentUserId = 0;
        this.mTargetUserId = -10000;
        this.mStartedUsers = new SparseArray();
        this.mUserLru = new ArrayList();
        this.mStartedUserArray = new int[]{0};
        this.mCurrentProfileIds = new int[0];
        this.mUserProfileGroupIdsSelfLocked = new SparseIntArray();
        this.mUserSwitchObservers = new RemoteCallbackList();
        this.mUserSwitchUiEnabled = true;
        this.mOppoBroadcastManager = null;
        this.mInjector = injector;
        this.mLock = injector.getLock();
        this.mHandler = injector.getHandler();
        this.mStartedUsers.put(0, new UserState(UserHandle.SYSTEM));
        this.mUserLru.add(Integer.valueOf(0));
        this.mLockPatternUtils = this.mInjector.getLockPatternUtils();
        updateStartedUserArrayLocked();
    }

    void finishUserSwitch(UserState uss) {
        synchronized (this.mLock) {
            finishUserBoot(uss);
            startProfilesLocked();
            stopRunningUsersLocked(3);
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

    /* JADX WARNING: Missing block: B:30:0x0181, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishUserBoot(UserState uss, IIntentReceiver resultTo) {
        int userId = uss.mHandle.getIdentifier();
        Slog.d(TAG, "Finishing user boot " + userId);
        synchronized (this.mLock) {
            if (this.mStartedUsers.get(userId) != uss) {
                return;
            }
            if (uss.setState(0, 1)) {
                this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                if (!(userId != 0 || (this.mInjector.isRuntimeRestarted() ^ 1) == 0 || (this.mInjector.isFirstBootOrUpgrade() ^ 1) == 0)) {
                    int uptimeSeconds = (int) (SystemClock.elapsedRealtime() / 1000);
                    MetricsLogger.histogram(this.mInjector.getContext(), "framework_locked_boot_completed", uptimeSeconds);
                    if (uptimeSeconds > 120) {
                        Slog.wtf("SystemServerTiming", "finishUserBoot took too long. uptimeSeconds=" + uptimeSeconds);
                    }
                }
                this.mHandler.sendMessage(this.mHandler.obtainMessage(64, userId, 0));
                Intent intent = new Intent("android.intent.action.LOCKED_BOOT_COMPLETED", null);
                intent.putExtra("android.intent.extra.user_handle", userId);
                intent.addFlags(150994944);
                this.mInjector.broadcastIntentLocked(intent, null, resultTo, 0, null, null, new String[]{"android.permission.RECEIVE_BOOT_COMPLETED"}, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
                AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTCOMPLETE, "finishUserBoot called,userId:" + userId);
            }
            if (this.mInjector.getUserManager().isManagedProfile(userId)) {
                UserInfo parent = this.mInjector.getUserManager().getProfileParent(userId);
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
    }

    /* JADX WARNING: Missing block: B:19:0x0037, code:
            if (r0 == false) goto L_0x006d;
     */
    /* JADX WARNING: Missing block: B:20:0x0039, code:
            r6.mUnlockProgress.start();
            r6.mUnlockProgress.setProgress(5, r5.mInjector.getContext().getString(17039482));
            r5.mInjector.getUserManager().onBeforeUnlockUser(r1);
            r6.mUnlockProgress.setProgress(20);
            r5.mHandler.obtainMessage(59, r1, 0, r6).sendToTarget();
     */
    /* JADX WARNING: Missing block: B:21:0x006d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishUserUnlocking(UserState uss) {
        int userId = uss.mHandle.getIdentifier();
        boolean proceedWithUnlock = false;
        synchronized (this.mLock) {
            if (this.mStartedUsers.get(uss.mHandle.getIdentifier()) != uss) {
            } else if (!StorageManager.isUserKeyUnlocked(userId)) {
            } else if (uss.setState(1, 2)) {
                this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                proceedWithUnlock = true;
            }
        }
    }

    /* JADX WARNING: Missing block: B:31:0x011c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void finishUserUnlocked(UserState uss) {
        int userId = uss.mHandle.getIdentifier();
        synchronized (this.mLock) {
            if (this.mStartedUsers.get(uss.mHandle.getIdentifier()) != uss) {
            } else if (!StorageManager.isUserKeyUnlocked(userId)) {
            } else if (uss.setState(2, 3)) {
                this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                uss.mUnlockProgress.finish();
                Intent unlockedIntent = new Intent("android.intent.action.USER_UNLOCKED");
                unlockedIntent.putExtra("android.intent.extra.user_handle", userId);
                unlockedIntent.addFlags(1342177280);
                this.mInjector.broadcastIntentLocked(unlockedIntent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, userId);
                if (getUserInfo(userId).isManagedProfile()) {
                    UserInfo parent = this.mInjector.getUserManager().getProfileParent(userId);
                    if (parent != null) {
                        Intent intent = new Intent("android.intent.action.MANAGED_PROFILE_UNLOCKED");
                        intent.putExtra("android.intent.extra.USER", UserHandle.of(userId));
                        intent.addFlags(1342177280);
                        this.mInjector.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, parent.id);
                    }
                }
                UserInfo info = getUserInfo(userId);
                if (Objects.equals(info.lastLoggedInFingerprint, Build.FINGERPRINT)) {
                    finishUserUnlockedCompleted(uss);
                } else {
                    boolean quiet;
                    if (!info.isManagedProfile()) {
                        quiet = false;
                    } else if (uss.tokenProvided) {
                        quiet = this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId) ^ 1;
                    } else {
                        quiet = true;
                    }
                    this.mInjector.sendPreBootBroadcast(userId, quiet, new -$Lambda$-wbdEBNBIl8hthLGGkbuzj1haLA(this, uss));
                }
            }
        }
    }

    private void finishUserUnlockedCompleted(UserState uss) {
        int userId = uss.mHandle.getIdentifier();
        synchronized (this.mLock) {
            if (this.mStartedUsers.get(uss.mHandle.getIdentifier()) != uss) {
                return;
            }
            UserInfo userInfo = getUserInfo(userId);
            if (userInfo == null) {
            } else if (StorageManager.isUserKeyUnlocked(userId)) {
                this.mInjector.getUserManager().onUserLoggedIn(userId);
                if (!(userInfo.isInitialized() || userId == 0)) {
                    Slog.d(TAG, "Initializing user #" + userId);
                    Intent intent = new Intent("android.intent.action.USER_INITIALIZE");
                    intent.addFlags(285212672);
                    final UserInfo userInfo2 = userInfo;
                    this.mInjector.broadcastIntentLocked(intent, null, new IIntentReceiver.Stub() {
                        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                            UserController.this.mInjector.getUserManager().makeInitialized(userInfo2.id);
                        }
                    }, 0, null, null, null, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
                }
                Slog.i(TAG, "Sending BOOT_COMPLETE user #" + userId);
                if (!(userId != 0 || (this.mInjector.isRuntimeRestarted() ^ 1) == 0 || (this.mInjector.isFirstBootOrUpgrade() ^ 1) == 0)) {
                    MetricsLogger.histogram(this.mInjector.getContext(), "framework_boot_completed", (int) (SystemClock.elapsedRealtime() / 1000));
                }
                Intent intent2 = new Intent("android.intent.action.BOOT_COMPLETED", null);
                intent2.putExtra("android.intent.extra.user_handle", userId);
                intent2.addFlags(150994944);
                final int i = userId;
                this.mInjector.broadcastIntentLocked(intent2, null, new IIntentReceiver.Stub() {
                    public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
                        Slog.i(UserController.TAG, "Finished processing BOOT_COMPLETED for u" + i);
                    }
                }, 0, null, null, new String[]{"android.permission.RECEIVE_BOOT_COMPLETED"}, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
                SystemProperties.set("sys.oppo.boot_completed", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
                AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTCOMPLETE, "finishUserUnlockedCompleted called,userId:" + userId + "systemserver pid:" + Process.myPid());
                recordRootState();
            }
        }
    }

    void sendOppoBootCompleteBroadcast() {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mStartedUsers.size(); i++) {
                final int userId = this.mStartedUsers.keyAt(i);
                Intent intent = new Intent(ACTION_OPPO_BOOT_COMPLETED, null);
                intent.putExtra("android.intent.extra.user_handle", userId);
                intent.addFlags(134217728);
                Slog.d(TAG, "AMS: sendOppoBootCompleteBroadcast begin:user # " + userId);
                this.mInjector.broadcastOppoBootComleteLocked(intent, new IIntentReceiver.Stub() {
                    public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                        Slog.d(UserController.TAG, "AMS: ACTION_OPPO_BOOT_COMPLETED process finish, sendingUser:" + sendingUser + ", userId:" + userId);
                        if (UserController.this.mOppoBroadcastManager != null) {
                            UserController.this.mOppoBroadcastManager.informReadyToCheckJumpQueue();
                        }
                    }
                }, userId);
                Slog.d(TAG, "AMS: sendOppoBootCompleteBroadcast end, user # " + userId);
            }
        }
        recordRootState();
    }

    void setOppoBroadcastManager(OppoBroadcastManager oppoBcMgr) {
        this.mOppoBroadcastManager = oppoBcMgr;
    }

    int restartUser(int userId, final boolean foreground) {
        return stopUser(userId, true, new IStopUserCallback.Stub() {
            /* renamed from: lambda$-com_android_server_am_UserController$4_25326 */
            /* synthetic */ void m34lambda$-com_android_server_am_UserController$4_25326(int userId, boolean foreground) {
                UserController.this.startUser(userId, foreground);
            }

            public void userStopped(int userId) {
                UserController.this.mHandler.post(new -$Lambda$5yQSwWrsRDcxoFuTXgyaBIqPvDw((byte) 1, foreground, userId, this));
            }

            public void userStopAborted(int userId) {
            }
        });
    }

    int stopUser(int userId, boolean force, IStopUserCallback callback) {
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: switchUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (userId < 0 || userId == 0) {
            throw new IllegalArgumentException("Can't stop system user " + userId);
        } else {
            int stopUsersLocked;
            this.mInjector.enforceShellRestriction("no_debugging_features", userId);
            synchronized (this.mLock) {
                stopUsersLocked = stopUsersLocked(userId, force, callback);
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
            this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
            updateStartedUserArrayLocked();
            long ident = Binder.clearCallingIdentity();
            try {
                Intent stoppingIntent = new Intent("android.intent.action.USER_STOPPING");
                stoppingIntent.addFlags(1073741824);
                stoppingIntent.putExtra("android.intent.extra.user_handle", userId);
                stoppingIntent.putExtra("android.intent.extra.SHUTDOWN_USERSPACE_ONLY", true);
                final int i2 = userId;
                final UserState userState = uss;
                IIntentReceiver stoppingReceiver = new IIntentReceiver.Stub() {
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
                this.mInjector.clearBroadcastQueueForUserLocked(userId);
                this.mInjector.broadcastIntentLocked(stoppingIntent, null, stoppingReceiver, 0, null, null, new String[]{"android.permission.INTERACT_ACROSS_USERS"}, -1, null, true, false, ActivityManagerService.MY_PID, 1000, -1);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    void finishUserStopping(int userId, UserState uss) {
        Intent shutdownIntent = new Intent("android.intent.action.ACTION_SHUTDOWN");
        shutdownIntent.addFlags(16777216);
        final UserState userState = uss;
        IIntentReceiver shutdownReceiver = new IIntentReceiver.Stub() {
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
        synchronized (this.mLock) {
            if (uss.state != 4) {
                return;
            }
            uss.setState(5);
            this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
            this.mInjector.batteryStatsServiceNoteEvent(16391, Integer.toString(userId), userId);
            this.mInjector.systemServiceManagerStopUser(userId);
            synchronized (this.mLock) {
                this.mInjector.broadcastIntentLocked(shutdownIntent, null, shutdownReceiver, 0, null, null, null, -1, null, true, false, ActivityManagerService.MY_PID, 1000, userId);
            }
        }
    }

    void finishUserStopped(UserState uss) {
        ArrayList<IStopUserCallback> callbacks;
        boolean stopped;
        int userId = uss.mHandle.getIdentifier();
        synchronized (this.mLock) {
            callbacks = new ArrayList(uss.mStopCallbacks);
            if (this.mStartedUsers.get(userId) != uss) {
                stopped = false;
            } else if (uss.state != 5) {
                stopped = false;
            } else {
                stopped = true;
                this.mStartedUsers.remove(userId);
                this.mInjector.getUserManagerInternal().removeUserState(userId);
                this.mUserLru.remove(Integer.valueOf(userId));
                updateStartedUserArrayLocked();
                this.mInjector.activityManagerOnUserStopped(userId);
                forceStopUserLocked(userId, "finish user");
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
            this.mInjector.systemServiceManagerCleanupUser(userId);
            synchronized (this.mLock) {
                this.mInjector.getActivityStackSupervisor().removeUserLocked(userId);
            }
            if (getUserInfo(userId).isEphemeral()) {
                this.mInjector.getUserManager().removeUser(userId);
            }
            try {
                getStorageManager().lockUserKey(userId);
            } catch (RemoteException re) {
                throw re.rethrowAsRuntimeException();
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
        this.mInjector.activityManagerForceStopPackageLocked(userId, reason);
        Intent intent = new Intent("android.intent.action.USER_STOPPED");
        intent.addFlags(1342177280);
        intent.putExtra("android.intent.extra.user_handle", userId);
        this.mInjector.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, -1);
    }

    private void stopGuestOrEphemeralUserIfBackground() {
        synchronized (this.mLock) {
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
        }
    }

    void startProfilesLocked() {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.i(TAG, "startProfilesLocked");
        }
        List<UserInfo> profiles = this.mInjector.getUserManager().getProfiles(this.mCurrentUserId, false);
        List<UserInfo> profilesToStart = new ArrayList(profiles.size());
        for (UserInfo user : profiles) {
            if (!((user.flags & 16) != 16 || user.id == this.mCurrentUserId || (user.isQuietModeEnabled() ^ 1) == 0)) {
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

    private IStorageManager getStorageManager() {
        return IStorageManager.Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
    }

    boolean startUser(int userId, boolean foreground) {
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: switchUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS_FULL";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        Slog.i(TAG, "Starting userid:" + userId + " fg:" + foreground);
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                int oldUserId = this.mCurrentUserId;
                if (oldUserId != userId) {
                    if (foreground) {
                        this.mInjector.getActivityStackSupervisor().setLockTaskModeLocked(null, 0, "startUser", false);
                    }
                    UserInfo userInfo = getUserInfo(userId);
                    if (userInfo == null) {
                        Slog.w(TAG, "No user info for user #" + userId);
                        Binder.restoreCallingIdentity(ident);
                        return false;
                    }
                    Intent intent;
                    if (foreground) {
                        if (userInfo.isManagedProfile()) {
                            Slog.w(TAG, "Cannot switch to User #" + userId + ": not a full user");
                            Binder.restoreCallingIdentity(ident);
                            return false;
                        }
                    }
                    if (foreground) {
                        if (this.mUserSwitchUiEnabled) {
                            this.mInjector.getWindowManager().startFreezingScreen(17432706, 17432705);
                        }
                    }
                    boolean needStart = false;
                    if (this.mStartedUsers.get(userId) == null) {
                        UserState userState = new UserState(UserHandle.of(userId));
                        this.mStartedUsers.put(userId, userState);
                        this.mInjector.getUserManagerInternal().setUserState(userId, userState.state);
                        updateStartedUserArrayLocked();
                        needStart = true;
                    }
                    UserState uss = (UserState) this.mStartedUsers.get(userId);
                    Integer userIdInt = Integer.valueOf(userId);
                    this.mUserLru.remove(userIdInt);
                    this.mUserLru.add(userIdInt);
                    if (foreground) {
                        this.mCurrentUserId = userId;
                        this.mInjector.updateUserConfigurationLocked();
                        this.mTargetUserId = -10000;
                        updateCurrentProfileIdsLocked();
                        this.mInjector.getWindowManager().setCurrentUser(userId, this.mCurrentProfileIds);
                        if (this.mUserSwitchUiEnabled) {
                            this.mInjector.getWindowManager().setSwitchingUser(true);
                            this.mInjector.getWindowManager().lockNow(null);
                        }
                    } else {
                        Integer currentUserIdInt = Integer.valueOf(this.mCurrentUserId);
                        updateCurrentProfileIdsLocked();
                        this.mInjector.getWindowManager().setCurrentProfileIds(this.mCurrentProfileIds);
                        this.mUserLru.remove(currentUserIdInt);
                        this.mUserLru.add(currentUserIdInt);
                    }
                    if (uss.state == 4) {
                        uss.setState(uss.lastState);
                        this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                        updateStartedUserArrayLocked();
                        needStart = true;
                    } else if (uss.state == 5) {
                        uss.setState(0);
                        this.mInjector.getUserManagerInternal().setUserState(userId, uss.state);
                        updateStartedUserArrayLocked();
                        needStart = true;
                    }
                    if (uss.state == 0) {
                        this.mInjector.getUserManager().onBeforeStartUser(userId);
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(42, userId, 0));
                    }
                    if (foreground) {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(43, userId, oldUserId));
                        this.mHandler.removeMessages(34);
                        this.mHandler.removeMessages(36);
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(34, oldUserId, userId, uss));
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(36, oldUserId, userId, uss), 3000);
                    }
                    if (needStart) {
                        intent = new Intent("android.intent.action.USER_STARTED");
                        intent.addFlags(1342177280);
                        intent.putExtra("android.intent.extra.user_handle", userId);
                        this.mInjector.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, userId);
                    }
                    if (foreground) {
                        moveUserToForegroundLocked(uss, oldUserId, userId);
                    } else {
                        finishUserBoot(uss);
                    }
                    if (needStart) {
                        intent = new Intent("android.intent.action.USER_STARTING");
                        intent.addFlags(1073741824);
                        intent.putExtra("android.intent.extra.user_handle", userId);
                        this.mInjector.broadcastIntentLocked(intent, null, new IIntentReceiver.Stub() {
                            public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
                            }
                        }, 0, null, null, new String[]{"android.permission.INTERACT_ACROSS_USERS"}, -1, null, true, false, ActivityManagerService.MY_PID, 1000, -1);
                    }
                    Binder.restoreCallingIdentity(ident);
                    return true;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        return true;
    }

    void startUserInForeground(int targetUserId) {
        if (!startUser(targetUserId, true)) {
            this.mInjector.getWindowManager().setSwitchingUser(false);
        }
    }

    boolean unlockUser(int userId, byte[] token, byte[] secret, IProgressListener listener) {
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
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

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0036 A:{Splitter: B:6:0x0013, ExcHandler: android.os.RemoteException (r4_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:15:0x0036, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:17:?, code:
            android.util.Slog.w(TAG, "Failed to unlock: " + r4.getMessage());
     */
    /* JADX WARNING: Missing block: B:27:0x0065, code:
            finishUserUnlocking(r11);
            r3 = new android.util.ArraySet();
            r13 = r16.mLock;
     */
    /* JADX WARNING: Missing block: B:28:0x0073, code:
            monitor-enter(r13);
     */
    /* JADX WARNING: Missing block: B:29:0x0074, code:
            r5 = 0;
     */
    /* JADX WARNING: Missing block: B:32:0x007d, code:
            if (r5 >= r16.mStartedUsers.size()) goto L_0x00d8;
     */
    /* JADX WARNING: Missing block: B:33:0x007f, code:
            r9 = r16.mStartedUsers.keyAt(r5);
            r6 = r16.mInjector.getUserManager().getProfileParent(r9);
     */
    /* JADX WARNING: Missing block: B:34:0x0093, code:
            if (r6 == null) goto L_0x00d3;
     */
    /* JADX WARNING: Missing block: B:36:0x0099, code:
            if (r6.id != r17) goto L_0x00d3;
     */
    /* JADX WARNING: Missing block: B:38:0x009d, code:
            if (r9 == r17) goto L_0x00d3;
     */
    /* JADX WARNING: Missing block: B:39:0x009f, code:
            android.util.Slog.d(TAG, "User " + r9 + " (parent " + r6.id + "): attempting unlock because parent was just unlocked");
            r3.add(java.lang.Integer.valueOf(r9));
     */
    /* JADX WARNING: Missing block: B:40:0x00d3, code:
            r5 = r5 + 1;
     */
    /* JADX WARNING: Missing block: B:42:0x00d8, code:
            monitor-exit(r13);
     */
    /* JADX WARNING: Missing block: B:43:0x00d9, code:
            r7 = r3.size();
            r5 = 0;
     */
    /* JADX WARNING: Missing block: B:44:0x00de, code:
            if (r5 >= r7) goto L_0x00f5;
     */
    /* JADX WARNING: Missing block: B:45:0x00e0, code:
            maybeUnlockUser(((java.lang.Integer) r3.valueAt(r5)).intValue());
            r5 = r5 + 1;
     */
    /* JADX WARNING: Missing block: B:50:0x00f6, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean unlockUserCleared(int userId, byte[] token, byte[] secret, IProgressListener listener) {
        synchronized (this.mLock) {
            if (!StorageManager.isUserKeyUnlocked(userId)) {
                try {
                    getStorageManager().unlockUserKey(userId, getUserInfo(userId).serialNumber, token, secret);
                } catch (Exception e) {
                }
            }
            UserState uss = (UserState) this.mStartedUsers.get(userId);
            if (uss == null) {
                notifyFinished(userId, listener);
                return false;
            }
            uss.mUnlockProgress.addListener(listener);
            uss.tokenProvided = token != null;
        }
    }

    void showUserSwitchDialog(Pair<UserInfo, UserInfo> fromToUserPair) {
        this.mInjector.showUserSwitchingDialog((UserInfo) fromToUserPair.first, (UserInfo) fromToUserPair.second);
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
        this.mInjector.getWindowManager().setSwitchingUser(false);
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        for (int i = 0; i < observerCount; i++) {
            try {
                ((IUserSwitchObserver) this.mUserSwitchObservers.getBroadcastItem(i)).onUserSwitchComplete(userId);
            } catch (RemoteException e) {
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    void dispatchLockedBootComplete(int userId) {
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        for (int i = 0; i < observerCount; i++) {
            try {
                ((IUserSwitchObserver) this.mUserSwitchObservers.getBroadcastItem(i)).onLockedBootComplete(userId);
            } catch (RemoteException e) {
            }
        }
        this.mUserSwitchObservers.finishBroadcast();
    }

    private void stopBackgroundUsersIfEnforced(int oldUserId) {
        if (oldUserId != 0 && hasUserRestriction("no_run_in_background", oldUserId)) {
            synchronized (this.mLock) {
                if (ActivityManagerDebugConfig.DEBUG_MU) {
                    Slog.i(TAG, "stopBackgroundUsersIfEnforced stopping " + oldUserId + " and related users");
                }
                stopUsersLocked(oldUserId, false, null);
            }
        }
    }

    void timeoutUserSwitch(UserState uss, int oldUserId, int newUserId) {
        synchronized (this.mLock) {
            Slog.wtf(TAG, "User switch timeout: from " + oldUserId + " to " + newUserId);
            sendContinueUserSwitchLocked(uss, oldUserId, newUserId);
        }
    }

    void dispatchUserSwitch(UserState uss, int oldUserId, int newUserId) {
        Slog.d(TAG, "Dispatch onUserSwitching oldUser #" + oldUserId + " newUser #" + newUserId);
        int observerCount = this.mUserSwitchObservers.beginBroadcast();
        if (observerCount > 0) {
            final ArraySet<String> curWaitingUserSwitchCallbacks = new ArraySet();
            synchronized (this.mLock) {
                uss.switching = true;
                this.mCurWaitingUserSwitchCallbacks = curWaitingUserSwitchCallbacks;
            }
            final AtomicInteger waitingCallbacksCount = new AtomicInteger(observerCount);
            final long dispatchStartedTime = SystemClock.elapsedRealtime();
            for (int i = 0; i < observerCount; i++) {
                try {
                    final String name = "#" + i + " " + this.mUserSwitchObservers.getBroadcastCookie(i);
                    synchronized (this.mLock) {
                        curWaitingUserSwitchCallbacks.add(name);
                    }
                    final UserState userState = uss;
                    final int i2 = oldUserId;
                    final int i3 = newUserId;
                    ((IUserSwitchObserver) this.mUserSwitchObservers.getBroadcastItem(i)).onUserSwitching(newUserId, new IRemoteCallback.Stub() {
                        /* JADX WARNING: Missing block: B:15:0x006b, code:
            return;
     */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        public void sendResult(Bundle data) throws RemoteException {
                            synchronized (UserController.this.mLock) {
                                long delay = SystemClock.elapsedRealtime() - dispatchStartedTime;
                                if (delay > 3000) {
                                    Slog.wtf(UserController.TAG, "User switch timeout: observer " + name + " sent result after " + delay + " ms");
                                }
                                if (curWaitingUserSwitchCallbacks != UserController.this.mCurWaitingUserSwitchCallbacks) {
                                    return;
                                }
                                curWaitingUserSwitchCallbacks.remove(name);
                                if (waitingCallbacksCount.decrementAndGet() == 0) {
                                    UserController.this.sendContinueUserSwitchLocked(userState, i2, i3);
                                }
                            }
                        }
                    });
                } catch (RemoteException e) {
                }
            }
        } else {
            synchronized (this.mLock) {
                sendContinueUserSwitchLocked(uss, oldUserId, newUserId);
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
        if (this.mUserSwitchUiEnabled) {
            synchronized (this.mLock) {
                this.mInjector.getWindowManager().stopFreezingScreen();
            }
        }
        uss.switching = false;
        this.mHandler.removeMessages(55);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(55, newUserId, 0));
        stopGuestOrEphemeralUserIfBackground();
        stopBackgroundUsersIfEnforced(oldUserId);
    }

    void moveUserToForegroundLocked(UserState uss, int oldUserId, int newUserId) {
        if (this.mInjector.getActivityStackSupervisor().switchUserLocked(newUserId, uss)) {
            this.mInjector.startHomeActivityLocked(newUserId, "moveUserToForeground");
        } else {
            this.mInjector.getActivityStackSupervisor().resumeFocusedStackTopActivityLocked();
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
                profiles = this.mInjector.getUserManager().getProfiles(oldUserId, false);
                count = profiles.size();
                for (i = 0; i < count; i++) {
                    profileUserId = ((UserInfo) profiles.get(i)).id;
                    intent = new Intent("android.intent.action.USER_BACKGROUND");
                    intent.addFlags(1342177280);
                    intent.putExtra("android.intent.extra.user_handle", profileUserId);
                    this.mInjector.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, profileUserId);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        if (newUserId >= 0) {
            profiles = this.mInjector.getUserManager().getProfiles(newUserId, false);
            count = profiles.size();
            for (i = 0; i < count; i++) {
                profileUserId = ((UserInfo) profiles.get(i)).id;
                intent = new Intent("android.intent.action.USER_FOREGROUND");
                intent.addFlags(1342177280);
                intent.putExtra("android.intent.extra.user_handle", profileUserId);
                this.mInjector.broadcastIntentLocked(intent, null, null, 0, null, null, null, -1, null, false, false, ActivityManagerService.MY_PID, 1000, profileUserId);
            }
            intent = new Intent("android.intent.action.USER_SWITCHED");
            intent.addFlags(1342177280);
            intent.putExtra("android.intent.extra.user_handle", newUserId);
            Intent intent2 = intent;
            this.mInjector.broadcastIntentLocked(intent2, null, null, 0, null, null, new String[]{"android.permission.MANAGE_USERS"}, -1, null, false, false, ActivityManagerService.MY_PID, 1000, -1);
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
            if (this.mInjector.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS_FULL", callingPid, callingUid, -1, true) == 0) {
                allow = true;
            } else if (allowMode == 2) {
                allow = false;
            } else if (this.mInjector.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS", callingPid, callingUid, -1, true) != 0) {
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
        } else if (callingUid != OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT || targetUserId < 0 || !hasUserRestriction("no_debugging_features", targetUserId)) {
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
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
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
        List<UserInfo> profiles = this.mInjector.getUserManager().getProfiles(this.mCurrentUserId, false);
        int[] currentProfileIds = new int[profiles.size()];
        for (i = 0; i < currentProfileIds.length; i++) {
            currentProfileIds[i] = ((UserInfo) profiles.get(i)).id;
        }
        this.mCurrentProfileIds = currentProfileIds;
        synchronized (this.mUserProfileGroupIdsSelfLocked) {
            this.mUserProfileGroupIdsSelfLocked.clear();
            List<UserInfo> users = this.mInjector.getUserManager().getUsers(false);
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

    boolean isUserRunningLocked(int userId, int flags) {
        boolean z = true;
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
                case 4:
                case 5:
                    return StorageManager.isUserKeyUnlocked(userId);
                default:
                    return false;
            }
        } else if ((flags & 4) != 0) {
            switch (state.state) {
                case 3:
                    return true;
                case 4:
                case 5:
                    return StorageManager.isUserKeyUnlocked(userId);
                default:
                    return false;
            }
        } else {
            if (state.state == 4 || state.state == 5) {
                z = false;
            }
            return z;
        }
    }

    UserInfo getCurrentUser() {
        if (this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS") != 0 && this.mInjector.checkCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
            String msg = "Permission Denial: getCurrentUser() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (this.mTargetUserId == -10000) {
            return getUserInfo(this.mCurrentUserId);
        } else {
            UserInfo currentUserLocked;
            synchronized (this.mLock) {
                currentUserLocked = getCurrentUserLocked();
            }
            return currentUserLocked;
        }
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
        UserManagerService ums = this.mInjector.getUserManager();
        if (ums != null) {
            return ums.getUserIds();
        }
        return new int[]{0};
    }

    UserInfo getUserInfo(int userId) {
        return this.mInjector.getUserManager().getUserInfo(userId);
    }

    int[] getUserIds() {
        return this.mInjector.getUserManager().getUserIds();
    }

    boolean exists(int userId) {
        return this.mInjector.getUserManager().exists(userId);
    }

    boolean hasUserRestriction(String restriction, int userId) {
        return this.mInjector.getUserManager().hasUserRestriction(restriction, userId);
    }

    Set<Integer> getProfileIds(int userId) {
        Set<Integer> userIds = new HashSet();
        for (UserInfo user : this.mInjector.getUserManager().getProfiles(userId, false)) {
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

    /* JADX WARNING: Missing block: B:9:0x0015, code:
            if (r4.mLockPatternUtils.isSeparateProfileChallengeEnabled(r5) != false) goto L_0x001b;
     */
    /* JADX WARNING: Missing block: B:10:0x0017, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:14:0x001b, code:
            r0 = r4.mInjector.getKeyguardManager();
     */
    /* JADX WARNING: Missing block: B:15:0x0025, code:
            if (r0.isDeviceLocked(r5) == false) goto L_0x002b;
     */
    /* JADX WARNING: Missing block: B:16:0x0027, code:
            r1 = r0.isDeviceSecure(r5);
     */
    /* JADX WARNING: Missing block: B:17:0x002b, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean shouldConfirmCredentials(int userId) {
        boolean z = false;
        synchronized (this.mLock) {
            if (this.mStartedUsers.get(userId) == null) {
                return false;
            }
        }
    }

    boolean isLockScreenDisabled(int userId) {
        return this.mLockPatternUtils.isLockScreenDisabled(userId);
    }

    void dump(PrintWriter pw, boolean dumpAll) {
        int i;
        pw.println("  mStartedUsers:");
        for (i = 0; i < this.mStartedUsers.size(); i++) {
            UserState uss = (UserState) this.mStartedUsers.valueAt(i);
            pw.print("    User #");
            pw.print(uss.mHandle.getIdentifier());
            pw.print(": ");
            uss.dump("", pw);
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
