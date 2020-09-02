package com.android.server.wm;

import android.app.ActivityOptions;
import java.util.ArrayList;

public interface IColorActivityStackInner {
    default boolean canEnterPipOnTaskSwitch(ActivityRecord pipCandidate, TaskRecord toFrontTask, ActivityRecord toFrontActivity, ActivityOptions opts) {
        return false;
    }

    default void removeActivityFromHistoryLocked(ActivityRecord r, String reason) {
    }

    default ArrayList<ActivityRecord> getLRUActivities() {
        return null;
    }
}
