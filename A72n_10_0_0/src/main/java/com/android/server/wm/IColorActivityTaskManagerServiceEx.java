package com.android.server.wm;

import android.common.OppoFeatureList;
import android.content.Context;

public interface IColorActivityTaskManagerServiceEx extends IOppoActivityTaskManagerServiceEx {
    public static final IColorActivityTaskManagerServiceEx DEFAULT = new IColorActivityTaskManagerServiceEx() {
        /* class com.android.server.wm.IColorActivityTaskManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IColorActivityTaskManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorActivityTaskManagerServiceEx;
    }

    default IColorActivityTaskManagerServiceEx getDefault() {
        return DEFAULT;
    }

    default IColorActivityRecordEx getColorActivityRecordEx(ActivityRecord ar) {
        return new ColorDummyActivityRecordEx(ar);
    }

    default IColorActivityStackEx getColorActivityStackEx(ActivityStack stack) {
        return new ColorDummyActivityStackEx(stack);
    }

    default IColorActivityStarterEx getColorActivityStarterEx(ActivityStarter starter) {
        return new ColorDummyActivityStarterEx(starter);
    }

    default IColorActivityStackSupervisorEx getColorActivityStackSupervisorEx(ActivityStackSupervisor supervisor) {
        return new ColorDummyActivityStackSupervisorEx(supervisor);
    }

    default IColorActivityStackSupervisorInner getColorActivityStackSupervisorInner(ActivityStackSupervisor supervisor) {
        return new IColorActivityStackSupervisorInner() {
            /* class com.android.server.wm.IColorActivityTaskManagerServiceEx.AnonymousClass2 */
        };
    }

    default IColorActivityStackInner getColorActivityStackInner(ActivityStack stack) {
        return new IColorActivityStackInner() {
            /* class com.android.server.wm.IColorActivityTaskManagerServiceEx.AnonymousClass3 */
        };
    }

    default boolean execInterceptWindow(Context context, ActivityRecord r, boolean keyguardLocked, boolean showWhenLocked, boolean dismissKeyguard, boolean showDialog) {
        return false;
    }

    default boolean execInterceptWindow(Context context, String packageName, boolean showDialog) {
        return false;
    }

    default boolean execInterceptFloatWindow(WindowManagerService ws, Context context, WindowState win, boolean keyguardLocked, boolean showDialog) {
        return false;
    }

    default boolean execInterceptDisableKeyguard(Context context, int uid) {
        return false;
    }

    default void execResolveScreenOnFlag(ActivityRecord record, boolean turnScreenOn) {
    }

    default void execHandleKeyguardGoingAway(boolean keyguardGoingAway) {
    }
}
