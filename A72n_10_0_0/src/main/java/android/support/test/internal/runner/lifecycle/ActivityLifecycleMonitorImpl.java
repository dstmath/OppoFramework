package android.support.test.internal.runner.lifecycle;

import android.app.Activity;
import android.os.Looper;
import android.support.test.internal.util.Checks;
import android.support.test.runner.lifecycle.ActivityLifecycleCallback;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class ActivityLifecycleMonitorImpl implements ActivityLifecycleMonitor {
    private List<ActivityStatus> mActivityStatuses;
    private final List<WeakReference<ActivityLifecycleCallback>> mCallbacks;
    private final boolean mDeclawThreadCheck;

    public ActivityLifecycleMonitorImpl() {
        this(false);
    }

    public ActivityLifecycleMonitorImpl(boolean declawThreadCheck) {
        this.mCallbacks = new ArrayList();
        this.mActivityStatuses = new ArrayList();
        this.mDeclawThreadCheck = declawThreadCheck;
    }

    public Collection<Activity> getActivitiesInStage(Stage stage) {
        checkMainThread();
        Checks.checkNotNull(stage);
        List<Activity> activities = new ArrayList<>();
        Iterator<ActivityStatus> statusIterator = this.mActivityStatuses.iterator();
        while (statusIterator.hasNext()) {
            ActivityStatus status = statusIterator.next();
            Activity statusActivity = (Activity) status.mActivityRef.get();
            if (statusActivity == null) {
                statusIterator.remove();
            } else if (stage == status.mLifecycleStage) {
                activities.add(statusActivity);
            }
        }
        return activities;
    }

    public void signalLifecycleChange(Stage stage, Activity activity) {
        String valueOf = String.valueOf(activity);
        String valueOf2 = String.valueOf(stage);
        StringBuilder sb = new StringBuilder(30 + String.valueOf(valueOf).length() + String.valueOf(valueOf2).length());
        sb.append("Lifecycle status change: ");
        sb.append(valueOf);
        sb.append(" in: ");
        sb.append(valueOf2);
        Log.d("LifecycleMonitor", sb.toString());
        boolean needsAdd = true;
        Iterator<ActivityStatus> statusIterator = this.mActivityStatuses.iterator();
        while (statusIterator.hasNext()) {
            ActivityStatus status = statusIterator.next();
            Activity statusActivity = (Activity) status.mActivityRef.get();
            if (statusActivity == null) {
                statusIterator.remove();
            } else if (activity == statusActivity) {
                needsAdd = false;
                status.mLifecycleStage = stage;
            }
        }
        if (needsAdd) {
            this.mActivityStatuses.add(new ActivityStatus(activity, stage));
        }
        synchronized (this.mCallbacks) {
            Iterator<WeakReference<ActivityLifecycleCallback>> refIter = this.mCallbacks.iterator();
            while (refIter.hasNext()) {
                ActivityLifecycleCallback callback = refIter.next().get();
                if (callback == null) {
                    refIter.remove();
                } else {
                    try {
                        String valueOf3 = String.valueOf(callback);
                        StringBuilder sb2 = new StringBuilder(18 + String.valueOf(valueOf3).length());
                        sb2.append("running callback: ");
                        sb2.append(valueOf3);
                        Log.d("LifecycleMonitor", sb2.toString());
                        callback.onActivityLifecycleChanged(activity, stage);
                        String valueOf4 = String.valueOf(callback);
                        StringBuilder sb3 = new StringBuilder(20 + String.valueOf(valueOf4).length());
                        sb3.append("callback completes: ");
                        sb3.append(valueOf4);
                        Log.d("LifecycleMonitor", sb3.toString());
                    } catch (RuntimeException re) {
                        Log.e("LifecycleMonitor", String.format("Callback threw exception! (callback: %s activity: %s stage: %s)", callback, activity, stage), re);
                    }
                }
            }
        }
    }

    private void checkMainThread() {
        if (!this.mDeclawThreadCheck && !Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
            throw new IllegalStateException("Querying activity state off main thread is not allowed.");
        }
    }

    /* access modifiers changed from: private */
    public static class ActivityStatus {
        private final WeakReference<Activity> mActivityRef;
        private Stage mLifecycleStage;

        ActivityStatus(Activity activity, Stage stage) {
            this.mActivityRef = new WeakReference<>((Activity) Checks.checkNotNull(activity));
            this.mLifecycleStage = (Stage) Checks.checkNotNull(stage);
        }
    }
}
