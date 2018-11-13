package com.android.server.am;

import android.content.pm.UserInfo;
import android.os.IUserManager.Stub;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.pm.UserManagerService;
import java.util.Set;

public class OppoMultiAppManager {
    public static final int FLAG_FROM = 67108864;
    private static final String TAG = "OppoMultiAppManager";
    public static final int USER_ID = 999;
    private static final Object mAppLock = new Object();
    private static final Object mLock = new Object();
    private static OppoMultiAppManager sInstance = null;
    private ActivityManagerService mAms;
    private int mCurrentUserId = -1;
    private UserManagerService mUserManager;
    private UserManagerInternal mUserManagerInternal;
    private UserState mUss;

    public static OppoMultiAppManager getInstance() {
        OppoMultiAppManager oppoMultiAppManager;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new OppoMultiAppManager();
            }
            oppoMultiAppManager = sInstance;
        }
        return oppoMultiAppManager;
    }

    private OppoMultiAppManager() {
    }

    public void setAms(ActivityManagerService ams) {
        this.mAms = ams;
        this.mUserManager = getUserManagerLocked();
        if (this.mUserManager.exists(USER_ID)) {
            synchronized (this.mAms) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    Log.v(TAG, "multi app: setAms begin:");
                    this.mCurrentUserId = USER_ID;
                    setStartedUsersLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    UserManagerService getUserManagerLocked() {
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

    public boolean isCurrentProfile(int userId) {
        if (userId == this.mCurrentUserId) {
            return true;
        }
        return false;
    }

    public void createUser(UserInfo info) {
        if (this.mAms != null && info != null) {
            synchronized (this.mAms) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if ((info.flags & 67108864) != 0) {
                        Log.v(TAG, "multi app: createUser " + info);
                        this.mCurrentUserId = info.id;
                        setStartedUsersLocked();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    private void setStartedUsersLocked() {
        this.mUss = new UserState(new UserHandle(this.mCurrentUserId));
        this.mUss.state = 3;
        this.mAms.mUserController.getStartedUsers().put(this.mCurrentUserId, this.mUss);
        this.mAms.mUserController.updateStartedUserArrayLocked();
        getUserManagerInternal().setUserState(this.mCurrentUserId, this.mUss.state);
        this.mAms.mHandler.obtainMessage(59, USER_ID, 0, this.mUss).sendToTarget();
        this.mAms.startUserInBackground(USER_ID);
    }

    public void removeUser(UserInfo info) {
        if (this.mAms != null && info != null) {
            synchronized (this.mAms) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if ((info.flags & 67108864) != 0) {
                        Log.v(TAG, "multi app: removeUser " + info);
                        this.mCurrentUserId = -1;
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public void inRecentTask(Set<Integer> includedUsers) {
        if (this.mCurrentUserId > 0) {
            includedUsers.add(Integer.valueOf(this.mCurrentUserId));
        }
    }

    public boolean enforceCrossUserPermission(int callingUid, int userId) {
        if (userId == this.mCurrentUserId || this.mCurrentUserId == UserHandle.getUserId(callingUid)) {
            return true;
        }
        return false;
    }
}
