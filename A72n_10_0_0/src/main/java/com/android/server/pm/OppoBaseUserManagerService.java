package com.android.server.pm;

import android.content.ContentResolver;
import android.os.IUserManager;
import com.android.server.notification.ZenModeHelper;

public abstract class OppoBaseUserManagerService extends IUserManager.Stub {
    public static final int STATE_ONE_CUSTOMER = 1;
    public static final int STATE_TWO_CUSTOMER = 2;

    /* access modifiers changed from: package-private */
    public void prePareMultiAppUser(UserDataPreparer mUserDataPreparer, PackageManagerService mPm) {
    }

    /* access modifiers changed from: package-private */
    public int getMultiAppUserId() {
        return ZenModeHelper.OPPO_MULTI_USER_ID;
    }

    /* access modifiers changed from: package-private */
    public boolean hasCustomerModeFlag(int flag) {
        return hasMultiAppFlag(flag) || (134217728 & flag) > 0;
    }

    /* access modifiers changed from: package-private */
    public int hasCustomerModeUser(int[] userIds, int count) {
        int hasCustomerModeUser = hasCustomerModeUser(userIds);
        if (hasCustomerModeUser == 1) {
            return count - 1;
        }
        if (hasCustomerModeUser != 2) {
            return count;
        }
        return count - 2;
    }

    /* access modifiers changed from: package-private */
    public int hasCustomerModeUser(int[] userIds) {
        int count = 0;
        if (userIds != null) {
            for (int id : userIds) {
                if (id == 888 || id == 999) {
                    count++;
                }
            }
        }
        return count;
    }

    /* access modifiers changed from: package-private */
    public boolean hasMultiAppFlag(int flag) {
        return (67108864 & flag) > 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasMultiAppUser(int[] userIds) {
        if (userIds != null) {
            for (int id : userIds) {
                if (id == 999) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeExtraConfigurationForUser(ContentResolver resolver, int userId) {
    }

    public void onUserCreated(int userHandle) {
    }

    public void onUserRemoved(int userHandle) {
    }
}
