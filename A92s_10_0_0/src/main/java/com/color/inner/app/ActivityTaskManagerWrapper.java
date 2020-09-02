package com.color.inner.app;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.util.Log;

public class ActivityTaskManagerWrapper {
    private static final String TAG = "ActivityTaskManagerWrapper";

    public static class StackInfoWrapper {
        public String[] taskNames;
        public int[] taskUserIds;
        public int userId;

        public StackInfoWrapper(ActivityManager.StackInfo stackInfo) {
            this.taskNames = stackInfo.taskNames;
            this.userId = stackInfo.userId;
            this.taskUserIds = stackInfo.taskUserIds;
        }
    }

    public static StackInfoWrapper getStackInfo(int windowingMode, int activityType) {
        try {
            return new StackInfoWrapper(ActivityTaskManager.getService().getStackInfo(windowingMode, activityType));
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
