package android.content.pm.dex;

import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.dex.ISnapshotRuntimeProfileCallback;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import libcore.io.Streams;

public class ColorArtManager {
    private static final String TAG = "ColorArtManager";

    /* JADX WARN: Type inference failed for: r0v3, types: [int, boolean] */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d7, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00d8, code lost:
        $closeResource(r7, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00db, code lost:
        throw r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00de, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00df, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00e2, code lost:
        throw r7;
     */
    /* JADX WARNING: Unknown variable types count: 1 */
    public static boolean runSnapshotApplicationProfile(String packageName, String callingPackage, String outputProfilePath) {
        Log.d(TAG, "runSnapdshotApplicationProfile, callingPackage = " + callingPackage + "packageName = " + packageName);
        ?? equals = "android".equals(packageName);
        String baseCodePath = null;
        IPackageManager iPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (equals == 0) {
            try {
                PackageInfo packageInfo = iPackageManager.getPackageInfo(packageName, 0, 0);
                if (packageInfo == null) {
                    Log.d(TAG, "Snapshot Profile: Package not found " + packageName);
                    return false;
                }
                baseCodePath = packageInfo.applicationInfo.getBaseCodePath();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException : " + e.getMessage());
            }
        }
        SnapshotRuntimeProfileCallback callback = new SnapshotRuntimeProfileCallback();
        try {
            if (!iPackageManager.getArtManager().isRuntimeProfilingEnabled(equals == true ? 1 : 0, callingPackage)) {
                Log.e(TAG, "Error: Runtime profiling is not enabled");
                return false;
            }
            iPackageManager.getArtManager().snapshotRuntimeProfile(equals, packageName, baseCodePath, callback, callingPackage);
            if (!callback.waitTillDone()) {
                Log.e(TAG, "Error: Snapshot profile callback not called");
                return false;
            }
            try {
                InputStream inStream = new ParcelFileDescriptor.AutoCloseInputStream(callback.mProfileReadFd);
                OutputStream outStream = new FileOutputStream(outputProfilePath);
                Streams.copy(inStream, outStream);
                $closeResource(null, outStream);
                $closeResource(null, inStream);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "Error when reading the profile fd: " + e2.getMessage());
                return false;
            }
        } catch (Exception e3) {
            Log.e(TAG, "Snapshot Profile Exception : " + e3.getMessage());
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private static class SnapshotRuntimeProfileCallback extends ISnapshotRuntimeProfileCallback.Stub {
        private CountDownLatch mDoneSignal;
        private int mErrCode;
        private ParcelFileDescriptor mProfileReadFd;
        private boolean mSuccess;

        private SnapshotRuntimeProfileCallback() {
            this.mSuccess = false;
            this.mErrCode = -1;
            this.mProfileReadFd = null;
            this.mDoneSignal = new CountDownLatch(1);
        }

        @Override // android.content.pm.dex.ISnapshotRuntimeProfileCallback
        public void onSuccess(ParcelFileDescriptor profileReadFd) {
            Log.i(ColorArtManager.TAG, "SnapshotRuntimeProfileCallback onSuccess");
            this.mSuccess = true;
            try {
                this.mProfileReadFd = profileReadFd.dup();
            } catch (IOException e) {
                Log.e(ColorArtManager.TAG, "SnapshotRuntimeProfileCallback onSuccess IOException : " + e.getMessage());
            }
            this.mDoneSignal.countDown();
        }

        @Override // android.content.pm.dex.ISnapshotRuntimeProfileCallback
        public void onError(int errCode) {
            Log.i(ColorArtManager.TAG, "SnapshotRuntimeProfileCallback onError, errorCode = " + errCode);
            this.mSuccess = false;
            this.mErrCode = errCode;
            this.mDoneSignal.countDown();
        }

        /* access modifiers changed from: package-private */
        public boolean waitTillDone() {
            boolean done = false;
            try {
                done = this.mDoneSignal.await(10000000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                Log.e(ColorArtManager.TAG, "waitTillDone Exception = " + e.getMessage());
            }
            return done && this.mSuccess;
        }
    }
}
