package com.android.server.engineer;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.engineer.OppoEngineerInternal;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.notification.ZenModeHelper;
import com.android.server.pm.UserManagerService;
import java.io.File;

public class OppoEngineerFunctionManager {
    private static final String IMAGES_DIRNAME = "recent_images";
    private static final String TAG = OppoEngineerFunctionManager.class.getSimpleName();
    private static final String TASKS_DIRNAME = "recent_tasks";
    private static boolean sNeedCleanUpRecentTasks = SystemProperties.getBoolean("persist.sys.remove.task", false);
    private static OppoEngineerInternal sOppoEngineerInternal;

    public static boolean shouldPreventStartActivity(ActivityInfo aInfo, String callingPackage, int callingPid, int callingUid) {
        if (sOppoEngineerInternal == null) {
            sOppoEngineerInternal = (OppoEngineerInternal) LocalServices.getService(OppoEngineerInternal.class);
        }
        OppoEngineerInternal oppoEngineerInternal = sOppoEngineerInternal;
        if (oppoEngineerInternal == null || aInfo == null || !oppoEngineerInternal.handleStartActivity(aInfo, callingPackage, callingUid, callingPid)) {
            return false;
        }
        String str = TAG;
        Slog.d(str, aInfo.toString() + " is isInEngineerBlackList ");
        return true;
    }

    public static boolean shouldPreventStartService(Intent service) {
        if (sOppoEngineerInternal == null) {
            sOppoEngineerInternal = (OppoEngineerInternal) LocalServices.getService(OppoEngineerInternal.class);
        }
        OppoEngineerInternal oppoEngineerInternal = sOppoEngineerInternal;
        if (oppoEngineerInternal == null || service == null || !oppoEngineerInternal.handleStartServiceOrBindService(service)) {
            return false;
        }
        String str = TAG;
        Slog.d(str, service.toString() + " is isInEngineerBlackList ");
        return true;
    }

    public static void tryRemoveAllUserRecentTasksLocked() {
        if (sNeedCleanUpRecentTasks) {
            for (int userId : UserManagerService.getInstance().getUserIds()) {
                removeUserRecentTasksLocked(userId);
            }
            removeUserRecentTasksLocked(ZenModeHelper.OPPO_MULTI_USER_ID);
            SystemProperties.set("persist.sys.remove.task", "");
            sNeedCleanUpRecentTasks = false;
        }
    }

    private static File getUserTasksDir(int userId) {
        return new File(Environment.getDataSystemCeDirectory(userId), TASKS_DIRNAME);
    }

    private static File getUserImagesDir(int userId) {
        return new File(Environment.getDataSystemCeDirectory(userId), IMAGES_DIRNAME);
    }

    private static void removeUserRecentTasksLocked(int userId) {
        Slog.i(TAG, "removeUserRecentTasksLocked userId = " + userId);
        File[] recentFiles = getUserTasksDir(userId).listFiles();
        if (recentFiles == null || recentFiles.length == 0) {
            Slog.d(TAG, "No recent task record to remove.");
        } else {
            for (File recentFile : recentFiles) {
                try {
                    recentFile.delete();
                } catch (Exception e) {
                    Slog.e(TAG, "Fail to get or delete recent task xml file.");
                }
            }
        }
        File[] imageFiles = getUserImagesDir(userId).listFiles();
        if (imageFiles == null || imageFiles.length == 0) {
            Slog.d(TAG, "No recent task image to remove.");
            return;
        }
        for (File imageFile : imageFiles) {
            try {
                imageFile.delete();
            } catch (Exception e2) {
                Slog.e(TAG, "Fail to get or delete recent task image file.");
            }
        }
    }
}
