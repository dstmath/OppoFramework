package com.android.server.am;

import java.util.List;

public class OppoBaseUserController {
    /* access modifiers changed from: protected */
    public boolean hasMultiAppUserLocked(List<Integer> users) {
        if (users == null) {
            return false;
        }
        for (Integer num : users) {
            if (num.intValue() == 999) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public int getMaxRunningUsersLocked(boolean excludeMultiApp, List<Integer> users, int maxRunningUsers) {
        if (!excludeMultiApp || !hasMultiAppUserLocked(users)) {
            return maxRunningUsers;
        }
        return maxRunningUsers + 1;
    }

    /* access modifiers changed from: package-private */
    public void sendOppoBootCompleteBroadcast() {
    }

    /* access modifiers changed from: package-private */
    public void sendOppoBootCompleteBroadcast(int userId) {
    }
}
