package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.util.SparseArray;
import java.io.PrintWriter;
import java.util.ArrayList;

public interface IColorLockTaskController extends IOppoCommonFeature {
    public static final IColorLockTaskController DEFAULT = new IColorLockTaskController() {
        /* class com.android.server.wm.IColorLockTaskController.AnonymousClass1 */
    };
    public static final String NAME = "IColorLockTaskController";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorLockTaskController;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(Context context, ActivityStackSupervisor supervisor, LockTaskController taskController, ArrayList<TaskRecord> arrayList, SparseArray<String[]> sparseArray) {
    }

    default void startLockDeviceMode(int userId, String rootPkg, String[] packages) {
    }

    default void stopLockDeviceModeBySystem() {
    }

    default void stopLockDeviceMode() {
    }

    default void updateTaskAuthLocked() {
    }

    default boolean isLockDeviceMode() {
        return false;
    }

    default String getRootLockPkgName() {
        return new String();
    }

    default boolean canShowInLockDeviceMode(int type) {
        return true;
    }

    default void dump(PrintWriter pw, String prefix) {
    }
}
