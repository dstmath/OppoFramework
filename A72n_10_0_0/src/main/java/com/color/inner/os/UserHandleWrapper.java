package com.color.inner.os;

import android.os.UserHandle;
import android.util.Log;

public class UserHandleWrapper {
    public static final UserHandle CURRENT = UserHandle.CURRENT;
    public static final UserHandle OWNER = UserHandle.OWNER;
    public static final String TAG = "UserHandleWrapper";
    public static final int USER_ALL = -1;
    public static final int USER_CURRENT = -2;
    public static final int USER_SYSTEM = 0;

    public static int myUserId() {
        try {
            return UserHandle.myUserId();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int getIdentifier(UserHandle userHandle) {
        try {
            return userHandle.getIdentifier();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int getUserId(int uid) {
        try {
            return UserHandle.getUserId(uid);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static UserHandle createUserHandle(int h) {
        return new UserHandle(h);
    }
}
