package androidx.arch.core.executor;

import java.util.concurrent.Executor;

public class ArchTaskExecutor extends TaskExecutor {
    private static final Executor sIOThreadExecutor = new Executor() {
        /* class androidx.arch.core.executor.ArchTaskExecutor.AnonymousClass2 */

        public void execute(Runnable command) {
            ArchTaskExecutor.getInstance().executeOnDiskIO(command);
        }
    };
    private static volatile ArchTaskExecutor sInstance;
    private static final Executor sMainThreadExecutor = new Executor() {
        /* class androidx.arch.core.executor.ArchTaskExecutor.AnonymousClass1 */

        public void execute(Runnable command) {
            ArchTaskExecutor.getInstance().postToMainThread(command);
        }
    };
    private TaskExecutor mDefaultTaskExecutor = new DefaultTaskExecutor();
    private TaskExecutor mDelegate = this.mDefaultTaskExecutor;

    private ArchTaskExecutor() {
    }

    public static ArchTaskExecutor getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (ArchTaskExecutor.class) {
            if (sInstance == null) {
                sInstance = new ArchTaskExecutor();
            }
        }
        return sInstance;
    }

    @Override // androidx.arch.core.executor.TaskExecutor
    public void executeOnDiskIO(Runnable runnable) {
        this.mDelegate.executeOnDiskIO(runnable);
    }

    @Override // androidx.arch.core.executor.TaskExecutor
    public void postToMainThread(Runnable runnable) {
        this.mDelegate.postToMainThread(runnable);
    }

    @Override // androidx.arch.core.executor.TaskExecutor
    public boolean isMainThread() {
        return this.mDelegate.isMainThread();
    }
}
