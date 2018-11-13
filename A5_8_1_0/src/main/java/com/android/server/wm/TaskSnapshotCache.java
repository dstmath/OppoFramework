package com.android.server.wm;

import android.app.ActivityManager.TaskSnapshot;
import android.util.ArrayMap;
import java.io.PrintWriter;

class TaskSnapshotCache {
    private final ArrayMap<AppWindowToken, Integer> mAppTaskMap = new ArrayMap();
    private final TaskSnapshotLoader mLoader;
    private final ArrayMap<Integer, CacheEntry> mRunningCache = new ArrayMap();
    private final WindowManagerService mService;

    private static final class CacheEntry {
        final TaskSnapshot snapshot;
        final AppWindowToken topApp;

        CacheEntry(TaskSnapshot snapshot, AppWindowToken topApp) {
            this.snapshot = snapshot;
            this.topApp = topApp;
        }
    }

    TaskSnapshotCache(WindowManagerService service, TaskSnapshotLoader loader) {
        this.mService = service;
        this.mLoader = loader;
    }

    void putSnapshot(Task task, TaskSnapshot snapshot) {
        CacheEntry entry = (CacheEntry) this.mRunningCache.get(Integer.valueOf(task.mTaskId));
        if (entry != null) {
            this.mAppTaskMap.remove(entry.topApp);
        }
        this.mAppTaskMap.put((AppWindowToken) task.getTopChild(), Integer.valueOf(task.mTaskId));
        this.mRunningCache.put(Integer.valueOf(task.mTaskId), new CacheEntry(snapshot, (AppWindowToken) task.getTopChild()));
    }

    /* JADX WARNING: Missing block: B:9:0x001f, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Missing block: B:10:0x0022, code:
            if (r8 != false) goto L_0x002b;
     */
    /* JADX WARNING: Missing block: B:11:0x0024, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:15:0x002f, code:
            return tryRestoreFromDisk(r6, r7, r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    TaskSnapshot getSnapshot(int taskId, int userId, boolean restoreFromDisk, boolean reducedResolution) {
        TaskSnapshot taskSnapshot;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                CacheEntry entry = (CacheEntry) this.mRunningCache.get(Integer.valueOf(taskId));
                if (entry != null) {
                    taskSnapshot = entry.snapshot;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return taskSnapshot;
    }

    private TaskSnapshot tryRestoreFromDisk(int taskId, int userId, boolean reducedResolution) {
        TaskSnapshot snapshot = this.mLoader.loadTask(taskId, userId, reducedResolution);
        if (snapshot == null) {
            return null;
        }
        return snapshot;
    }

    void onAppRemoved(AppWindowToken wtoken) {
        Integer taskId = (Integer) this.mAppTaskMap.get(wtoken);
        if (taskId != null) {
            removeRunningEntry(taskId.intValue());
        }
    }

    void onAppDied(AppWindowToken wtoken) {
        Integer taskId = (Integer) this.mAppTaskMap.get(wtoken);
        if (taskId != null) {
            removeRunningEntry(taskId.intValue());
        }
    }

    void onTaskRemoved(int taskId) {
        removeRunningEntry(taskId);
    }

    private void removeRunningEntry(int taskId) {
        CacheEntry entry = (CacheEntry) this.mRunningCache.get(Integer.valueOf(taskId));
        if (entry != null) {
            this.mAppTaskMap.remove(entry.topApp);
            this.mRunningCache.remove(Integer.valueOf(taskId));
        }
    }

    void dump(PrintWriter pw, String prefix) {
        String doublePrefix = prefix + "  ";
        String triplePrefix = doublePrefix + "  ";
        pw.println(prefix + "SnapshotCache");
        for (int i = this.mRunningCache.size() - 1; i >= 0; i--) {
            CacheEntry entry = (CacheEntry) this.mRunningCache.valueAt(i);
            pw.println(doublePrefix + "Entry taskId=" + this.mRunningCache.keyAt(i));
            pw.println(triplePrefix + "topApp=" + entry.topApp);
            pw.println(triplePrefix + "snapshot=" + entry.snapshot);
        }
    }
}
