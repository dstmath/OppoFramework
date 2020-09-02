package com.color.inner.content.pm;

import android.content.pm.UserInfo;
import android.os.UserHandle;

public class UserInfoWrapper {
    private static final String TAG = "UserInfoWrapper";
    private UserInfo user;

    private UserInfoWrapper() {
    }

    public UserInfoWrapper(UserInfo user2) {
        this.user = user2;
    }

    public int getId() {
        UserInfo userInfo = this.user;
        if (userInfo != null) {
            return userInfo.id;
        }
        return -1;
    }

    public UserHandle getUserHandle() {
        UserInfo userInfo = this.user;
        if (userInfo != null) {
            return userInfo.getUserHandle();
        }
        return null;
    }

    public boolean isEnabled() {
        UserInfo userInfo = this.user;
        if (userInfo != null) {
            return userInfo.isEnabled();
        }
        return true;
    }
}
