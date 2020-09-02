package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import com.android.server.am.IColorActivityManagerServiceEx;
import java.util.ArrayList;
import java.util.List;

public interface IColorAthenaManager extends IOppoCommonFeature {
    public static final IColorAthenaManager DEFAULT = new IColorAthenaManager() {
        /* class com.android.server.wm.IColorAthenaManager.AnonymousClass1 */
    };
    public static final String NAME = "ColorAthena";
    public static final int TYPE_KILL = 3;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_NOT_KILL_PKG = 1;
    public static final int TYPE_NOT_KILL_PROC = 2;

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAthenaManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default int getRemoveTaskFilterType(WindowProcessController proc) {
        return 0;
    }

    default List<String> getProtectList() {
        return null;
    }

    default List<String> getAppAssociatedActivity(String packageName) {
        return null;
    }

    default boolean killPackageProcessesFilter(WindowProcessController app, String packageName, boolean isDep, int appId) {
        return false;
    }

    default boolean killBackgroundProcessFilter(String packageName, int callingUid) {
        return false;
    }

    default boolean startActivityFilter(Intent intent, String callingPackage, int callingUid, int callingPid) {
        return false;
    }

    default boolean skipAmsEmptyKill(WindowProcessController app) {
        return false;
    }

    default boolean skipAmsEmptyKillBootUp(WindowProcessController app) {
        return false;
    }

    default boolean isRecentLockTask(TaskRecord task) {
        return false;
    }

    default List<String> getPkgListInTask(int taskId) {
        return null;
    }

    default List<String> getTaskPkgList(int taskId) {
        return new ArrayList();
    }
}
