package com.android.server.wm;

import android.app.ActivityManager;
import android.util.ArrayMap;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class TaskSnapshotCache {
    private final ArrayMap<AppWindowToken, Integer> mAppTaskMap = new ArrayMap<>();
    private final TaskSnapshotLoader mLoader;
    private final ArrayMap<Integer, CacheEntry> mRunningCache = new ArrayMap<>();
    private final WindowManagerService mService;

    TaskSnapshotCache(WindowManagerService service, TaskSnapshotLoader loader) {
        this.mService = service;
        this.mLoader = loader;
    }

    /* access modifiers changed from: package-private */
    public void putSnapshot(Task task, ActivityManager.TaskSnapshot snapshot) {
        CacheEntry entry = this.mRunningCache.get(Integer.valueOf(task.mTaskId));
        if (entry != null) {
            this.mAppTaskMap.remove(entry.topApp);
        }
        this.mAppTaskMap.put((AppWindowToken) task.getTopChild(), Integer.valueOf(task.mTaskId));
        this.mRunningCache.put(Integer.valueOf(task.mTaskId), new CacheEntry(snapshot, (AppWindowToken) task.getTopChild()));
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
        if (r6 != false) goto L_0x0025;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0023, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
        return tryRestoreFromDisk(r4, r5, r7);
     */
    public ActivityManager.TaskSnapshot getSnapshot(int taskId, int userId, boolean restoreFromDisk, boolean reducedResolution) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                CacheEntry entry = this.mRunningCache.get(Integer.valueOf(taskId));
                if (entry != null) {
                    return entry.snapshot;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private ActivityManager.TaskSnapshot tryRestoreFromDisk(int taskId, int userId, boolean reducedResolution) {
        ActivityManager.TaskSnapshot snapshot = this.mLoader.loadTask(taskId, userId, reducedResolution);
        if (snapshot == null) {
            return null;
        }
        return snapshot;
    }

    /* access modifiers changed from: package-private */
    public void onAppRemoved(AppWindowToken wtoken) {
        Integer taskId = this.mAppTaskMap.get(wtoken);
        if (taskId != null) {
            removeRunningEntry(taskId.intValue());
        }
    }

    /* access modifiers changed from: package-private */
    public void onAppDied(AppWindowToken wtoken) {
        Integer taskId = this.mAppTaskMap.get(wtoken);
        if (taskId != null) {
            removeRunningEntry(taskId.intValue());
        }
    }

    /* access modifiers changed from: package-private */
    public void onTaskRemoved(int taskId) {
        removeRunningEntry(taskId);
    }

    private void removeRunningEntry(int taskId) {
        CacheEntry entry = this.mRunningCache.get(Integer.valueOf(taskId));
        if (entry != null) {
            this.mAppTaskMap.remove(entry.topApp);
            this.mRunningCache.remove(Integer.valueOf(taskId));
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        String doublePrefix = prefix + "  ";
        String triplePrefix = doublePrefix + "  ";
        pw.println(prefix + "SnapshotCache");
        for (int i = this.mRunningCache.size() + -1; i >= 0; i += -1) {
            CacheEntry entry = this.mRunningCache.valueAt(i);
            pw.println(doublePrefix + "Entry taskId=" + this.mRunningCache.keyAt(i));
            pw.println(triplePrefix + "topApp=" + entry.topApp);
            pw.println(triplePrefix + "snapshot=" + entry.snapshot);
        }
    }

    /* access modifiers changed from: private */
    public static final class CacheEntry {
        final ActivityManager.TaskSnapshot snapshot;
        final AppWindowToken topApp;

        CacheEntry(ActivityManager.TaskSnapshot snapshot2, AppWindowToken topApp2) {
            this.snapshot = snapshot2;
            this.topApp = topApp2;
        }
    }
}
