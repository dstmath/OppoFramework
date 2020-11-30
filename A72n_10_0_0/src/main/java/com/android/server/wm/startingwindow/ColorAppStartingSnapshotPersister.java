package com.android.server.wm.startingwindow;

import android.graphics.Bitmap;
import android.os.Process;
import android.os.SystemClock;
import com.android.internal.annotations.GuardedBy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;

public class ColorAppStartingSnapshotPersister {
    private static final String APP_SNAPSHOTS_DIRNAME = "appsnapshots";
    private static final String BITMAP_EXTENSION = ".jpg";
    private static final long DELAY_MS = 100;
    private static final int MAX_STORE_QUEUE_DEPTH = 2;
    private static final int QUALITY = 95;
    private final DirectoryResolver mDirectoryResolver;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private boolean mPaused;
    private Thread mPersister = new Thread("AppSnapshotPersister") {
        /* class com.android.server.wm.startingwindow.ColorAppStartingSnapshotPersister.AnonymousClass1 */

        public void run() {
            WriteQueueItem next;
            Process.setThreadPriority(10);
            while (true) {
                synchronized (ColorAppStartingSnapshotPersister.this.mLock) {
                    if (ColorAppStartingSnapshotPersister.this.mPaused) {
                        next = null;
                    } else {
                        next = (WriteQueueItem) ColorAppStartingSnapshotPersister.this.mWriteQueue.poll();
                        if (next != null) {
                            next.onDequeuedLocked();
                        }
                    }
                }
                if (next != null) {
                    next.write();
                    SystemClock.sleep(ColorAppStartingSnapshotPersister.DELAY_MS);
                }
                synchronized (ColorAppStartingSnapshotPersister.this.mLock) {
                    boolean writeQueueEmpty = ColorAppStartingSnapshotPersister.this.mWriteQueue.isEmpty();
                    if (writeQueueEmpty || ColorAppStartingSnapshotPersister.this.mPaused) {
                        try {
                            ColorAppStartingSnapshotPersister.this.mQueueIdling = writeQueueEmpty;
                            ColorAppStartingSnapshotPersister.this.mLock.wait();
                            ColorAppStartingSnapshotPersister.this.mQueueIdling = false;
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    };
    @GuardedBy({"mLock"})
    private boolean mQueueIdling;
    private boolean mStarted;
    @GuardedBy({"mLock"})
    private final ArrayDeque<StoreWriteQueueItem> mStoreQueueItems = new ArrayDeque<>();
    @GuardedBy({"mLock"})
    private final ArrayDeque<WriteQueueItem> mWriteQueue = new ArrayDeque<>();

    public interface DirectoryResolver {
        File getSystemDirectoryForUser(int i);
    }

    public ColorAppStartingSnapshotPersister(DirectoryResolver resolver) {
        this.mDirectoryResolver = resolver;
    }

    public void start() {
        if (!this.mStarted) {
            this.mStarted = true;
            this.mPersister.start();
        }
    }

    public void persistSnapshot(String packageName, int userId, Bitmap snapshot) {
        synchronized (this.mLock) {
            sendToQueueLocked(new StoreWriteQueueItem(packageName, userId, snapshot));
        }
    }

    public void onAppUnstalled(int userId, String packageName) {
        synchronized (this.mLock) {
            sendToQueueLocked(new DeleteWriteQueueItem(userId, packageName));
        }
    }

    public void clearCache(int userId) {
        synchronized (this.mLock) {
            sendToQueueLocked(new DeleteAllCacheForUserQueueItem(userId));
        }
    }

    /* access modifiers changed from: package-private */
    public void setPaused(boolean paused) {
        synchronized (this.mLock) {
            this.mPaused = paused;
            if (!paused) {
                this.mLock.notifyAll();
            }
        }
    }

    @GuardedBy({"mLock"})
    private void sendToQueueLocked(WriteQueueItem item) {
        this.mWriteQueue.offer(item);
        item.onQueuedLocked();
        ensureStoreQueueDepthLocked();
        if (!this.mPaused) {
            this.mLock.notifyAll();
        }
    }

    @GuardedBy({"mLock"})
    private void ensureStoreQueueDepthLocked() {
        while (this.mStoreQueueItems.size() > 2) {
            StoreWriteQueueItem item = this.mStoreQueueItems.poll();
            this.mWriteQueue.remove(item);
            ColorStartingWindowUtils.logD("Queue is too deep! Purged item with package =" + item.mPackageName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private File getDirectory(int userId) {
        return new File(this.mDirectoryResolver.getSystemDirectoryForUser(userId), APP_SNAPSHOTS_DIRNAME);
    }

    /* access modifiers changed from: package-private */
    public File getBitmapFile(int userId, String packageName) {
        File directory = getDirectory(userId);
        return new File(directory, packageName + BITMAP_EXTENSION);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean createDirectory(int userId) {
        File dir = getDirectory(userId);
        return dir.exists() || dir.mkdirs();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deleteSnapshot(int userId, String packageName) {
        try {
            if (getBitmapFile(userId, packageName).delete()) {
                ColorStartingWindowUtils.logD("deleteSnapshot surcess packageName =: " + packageName + ",userId =: " + userId);
            }
        } catch (Exception e) {
            ColorStartingWindowUtils.logE("deleteSnapshot fail packageName =: " + packageName + ",userId =: " + userId + ",error =: " + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deleteAllSnapshotsForUser(int userId) {
        try {
            File snapshotsDir = getDirectory(userId);
            long start = System.currentTimeMillis();
            String[] files = snapshotsDir.list();
            if (files == null) {
                return;
            }
            if (files.length != 0) {
                for (String file : files) {
                    new File(snapshotsDir, file).delete();
                }
                ColorStartingWindowUtils.logD("deleteAllSnapshotsForUser userId =: " + userId + ",fileNumber = : " + files.length + ",spend time = : " + (System.currentTimeMillis() - start));
            }
        } catch (Exception e) {
            ColorStartingWindowUtils.logE("deleteAllSnapshotsForUser fail userId =: " + userId + ",error =: " + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public abstract class WriteQueueItem {
        /* access modifiers changed from: package-private */
        public abstract void write();

        private WriteQueueItem() {
        }

        /* access modifiers changed from: package-private */
        public void onQueuedLocked() {
        }

        /* access modifiers changed from: package-private */
        public void onDequeuedLocked() {
        }
    }

    /* access modifiers changed from: private */
    public class StoreWriteQueueItem extends WriteQueueItem {
        private final String mPackageName;
        private final Bitmap mSnapshot;
        private final int mUserId;

        StoreWriteQueueItem(String packageName, int userId, Bitmap snapshot) {
            super();
            this.mPackageName = packageName;
            this.mUserId = userId;
            this.mSnapshot = snapshot;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.startingwindow.ColorAppStartingSnapshotPersister.WriteQueueItem
        @GuardedBy({"mLock"})
        public void onQueuedLocked() {
            ColorAppStartingSnapshotPersister.this.mStoreQueueItems.offer(this);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.startingwindow.ColorAppStartingSnapshotPersister.WriteQueueItem
        @GuardedBy({"mLock"})
        public void onDequeuedLocked() {
            ColorAppStartingSnapshotPersister.this.mStoreQueueItems.remove(this);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.startingwindow.ColorAppStartingSnapshotPersister.WriteQueueItem
        public void write() {
            if (!ColorAppStartingSnapshotPersister.this.createDirectory(this.mUserId)) {
                ColorStartingWindowUtils.logD("Unable to create snapshot directory for user dir=" + ColorAppStartingSnapshotPersister.this.getDirectory(this.mUserId));
            }
            boolean failed = false;
            if (!writeBuffer()) {
                failed = true;
            }
            if (failed) {
                ColorAppStartingSnapshotPersister.this.deleteSnapshot(this.mUserId, this.mPackageName);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean writeBuffer() {
            if (this.mSnapshot == null) {
                ColorStartingWindowUtils.logE("Invalid snapshot bitmap");
                return false;
            }
            File bitmapFile = ColorAppStartingSnapshotPersister.this.getBitmapFile(this.mUserId, this.mPackageName);
            ColorStartingWindowUtils.logD("writeBuffer bitmapFile =: " + bitmapFile);
            try {
                FileOutputStream bmpFos = new FileOutputStream(bitmapFile);
                this.mSnapshot.compress(Bitmap.CompressFormat.JPEG, ColorAppStartingSnapshotPersister.QUALITY, bmpFos);
                bmpFos.close();
                return true;
            } catch (IOException e) {
                ColorStartingWindowUtils.logE("Unable to open " + bitmapFile + " for persisting.\ne =:" + e.getMessage());
                return false;
            }
        }
    }

    private class DeleteWriteQueueItem extends WriteQueueItem {
        private final String mPackageName;
        private final int mUserId;

        DeleteWriteQueueItem(int userId, String packageName) {
            super();
            this.mUserId = userId;
            this.mPackageName = packageName;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.startingwindow.ColorAppStartingSnapshotPersister.WriteQueueItem
        public void write() {
            ColorAppStartingSnapshotPersister.this.deleteSnapshot(this.mUserId, this.mPackageName);
        }
    }

    /* access modifiers changed from: private */
    public class DeleteAllCacheForUserQueueItem extends WriteQueueItem {
        private final int mUserId;

        DeleteAllCacheForUserQueueItem(int userId) {
            super();
            this.mUserId = userId;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.startingwindow.ColorAppStartingSnapshotPersister.WriteQueueItem
        public void write() {
            ColorAppStartingSnapshotPersister.this.deleteAllSnapshotsForUser(this.mUserId);
        }
    }
}
