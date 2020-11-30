package com.color.inner.os;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.color.inner.content.pm.UserInfoWrapper;
import java.util.ArrayList;
import java.util.List;

public class UserManagerWrapper {
    private static final String TAG = "UserManagerWrapper";

    private UserManagerWrapper() {
    }

    public static List<UserInfoWrapper> getUsers(Context context) {
        UserManager userMgr = (UserManager) context.getSystemService("user");
        try {
            List<UserInfoWrapper> list = new ArrayList<>();
            for (UserInfo user : userMgr.getUsers()) {
                list.add(new UserInfoWrapper(user));
            }
            return list;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static boolean isUserIDExist(Context context, int userID) {
        UserInfo mUserInfo = ((UserManager) context.getSystemService("user")).getUserInfo(userID);
        if (mUserInfo == null || mUserInfo.getUserHandle().getIdentifier() != userID) {
            return false;
        }
        return true;
    }

    public static UserInfoWrapper createUser(UserManager userManager, String name, int flag) {
        UserInfo userInfo = userManager.createUser(name, flag);
        if (userInfo != null) {
            return new UserInfoWrapper(userInfo);
        }
        return null;
    }

    public static UserInfoWrapper getUserInfo(UserManager userManager, int userHandle) {
        try {
            UserInfo userInfo = userManager.getUserInfo(userHandle);
            if (userInfo != null) {
                return new UserInfoWrapper(userInfo);
            }
            return null;
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static boolean isUserUnlockingOrUnlocked(Context context, UserHandle user) {
        return ((UserManager) context.getSystemService("user")).isUserUnlockingOrUnlocked(user);
    }

    public static boolean canShowMultiUserEntry(Context context) throws IllegalArgumentException {
        if (context != null) {
            return canShowMultiUserEntry(context, context.getUserId());
        }
        throw new IllegalArgumentException("Params error");
    }

    public static boolean canShowMultiUserEntry(Context context, int userId) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("Params error");
        } else if (userId == 888) {
            return false;
        } else {
            if (SystemProperties.getBoolean("persist.sys.assert.panic.multi.user.entrance", false)) {
                return true;
            }
            return !context.getPackageManager().hasSystemFeature("oppo.multiuser.entry.unsupport");
        }
    }

    public static boolean isGuestUser(Context context) {
        return ((UserManager) context.getSystemService("user")).isGuestUser();
    }
}
