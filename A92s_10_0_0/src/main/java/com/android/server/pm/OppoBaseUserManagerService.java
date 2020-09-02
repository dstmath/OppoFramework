package com.android.server.pm;

import android.content.ContentResolver;
import android.os.IUserManager;
import com.android.server.notification.ZenModeHelper;

public abstract class OppoBaseUserManagerService extends IUserManager.Stub {
    /* access modifiers changed from: package-private */
    public void prePareMultiAppUser(UserDataPreparer mUserDataPreparer, PackageManagerService mPm) {
    }

    /* access modifiers changed from: package-private */
    public int getMultiAppUserId() {
        return ZenModeHelper.OPPO_MULTI_USER_ID;
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
}
