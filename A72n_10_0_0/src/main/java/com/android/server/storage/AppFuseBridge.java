package com.android.server.storage;

import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.FuseUnavailableMountException;
import com.android.internal.util.Preconditions;
import com.android.server.NativeDaemonConnectorException;
import java.util.concurrent.CountDownLatch;
import libcore.io.IoUtils;

public class AppFuseBridge implements Runnable {
    private static final String APPFUSE_MOUNT_NAME_TEMPLATE = "/mnt/appfuse/%d_%d";
    public static final String TAG = "AppFuseBridge";
    @GuardedBy({"this"})
    private long mNativeLoop = native_new();
    @GuardedBy({"this"})
    private final SparseArray<MountScope> mScopes = new SparseArray<>();

    private native int native_add_bridge(long j, int i, int i2);

    private native void native_delete(long j);

    private native long native_new();

    private native void native_start_loop(long j);

    public ParcelFileDescriptor addBridge(MountScope mountScope) throws FuseUnavailableMountException, NativeDaemonConnectorException {
        int i = 0;
        while (i < 10) {
            try {
                synchronized (this) {
                    Preconditions.checkArgument(this.mScopes.indexOfKey(mountScope.mountId) < 0);
                    if (this.mNativeLoop != 0) {
                        int fd = native_add_bridge(this.mNativeLoop, mountScope.mountId, mountScope.open().detachFd());
                        if (fd == -1) {
                            throw new FuseUnavailableMountException(mountScope.mountId);
                        } else if (fd == -2) {
                            Slog.i(TAG, "try to addBridge, get mutex fail");
                            if (i != 9) {
                                i++;
                            } else {
                                throw new FuseUnavailableMountException(mountScope.mountId);
                            }
                        } else {
                            ParcelFileDescriptor result = ParcelFileDescriptor.adoptFd(fd);
                            this.mScopes.put(mountScope.mountId, mountScope);
                            mountScope = null;
                            return result;
                        }
                    } else {
                        throw new FuseUnavailableMountException(mountScope.mountId);
                    }
                }
            } finally {
                IoUtils.closeQuietly(mountScope);
            }
        }
        IoUtils.closeQuietly(mountScope);
        throw new FuseUnavailableMountException(mountScope.mountId);
    }

    public void run() {
        native_start_loop(this.mNativeLoop);
        synchronized (this) {
            native_delete(this.mNativeLoop);
            this.mNativeLoop = 0;
        }
    }

    public ParcelFileDescriptor openFile(int mountId, int fileId, int mode) throws FuseUnavailableMountException, InterruptedException {
        MountScope scope;
        synchronized (this) {
            scope = this.mScopes.get(mountId);
            if (scope == null) {
                throw new FuseUnavailableMountException(mountId);
            }
        }
        if (scope.waitForMount()) {
            try {
                return scope.openFile(mountId, fileId, FileUtils.translateModePfdToPosix(mode));
            } catch (NativeDaemonConnectorException e) {
                throw new FuseUnavailableMountException(mountId);
            }
        } else {
            throw new FuseUnavailableMountException(mountId);
        }
    }

    private synchronized void onMount(int mountId) {
        MountScope scope = this.mScopes.get(mountId);
        if (scope != null) {
            scope.setMountResultLocked(true);
        }
    }

    private synchronized void onClosed(int mountId) {
        MountScope scope = this.mScopes.get(mountId);
        if (scope != null) {
            scope.setMountResultLocked(false);
            IoUtils.closeQuietly(scope);
            this.mScopes.remove(mountId);
        }
    }

    public static abstract class MountScope implements AutoCloseable {
        private boolean mMountResult = false;
        private final CountDownLatch mMounted = new CountDownLatch(1);
        public final int mountId;
        public final int uid;

        public abstract ParcelFileDescriptor open() throws NativeDaemonConnectorException;

        public abstract ParcelFileDescriptor openFile(int i, int i2, int i3) throws NativeDaemonConnectorException;

        public MountScope(int uid2, int mountId2) {
            this.uid = uid2;
            this.mountId = mountId2;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"AppFuseBridge.this"})
        public void setMountResultLocked(boolean result) {
            if (this.mMounted.getCount() != 0) {
                this.mMountResult = result;
                this.mMounted.countDown();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean waitForMount() throws InterruptedException {
            this.mMounted.await();
            return this.mMountResult;
        }
    }
}
