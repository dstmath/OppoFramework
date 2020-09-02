package com.android.server.devicepolicy;

import android.app.admin.StartInstallingUpdateCallback;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.RecoverySystem;
import android.util.Log;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.io.IOException;
import java.security.GeneralSecurityException;

class NonAbUpdateInstaller extends UpdateInstaller implements RecoverySystem.ProgressListener {
    NonAbUpdateInstaller(Context context, ParcelFileDescriptor updateFileDescriptor, StartInstallingUpdateCallback callback, DevicePolicyManagerService.Injector injector, DevicePolicyConstants constants) {
        super(context, updateFileDescriptor, callback, injector, constants);
    }

    @Override // com.android.server.devicepolicy.UpdateInstaller
    public void installUpdateInThread() {
        try {
            RecoverySystem.verifyPackage(this.mCopiedUpdateFile, this, null);
            RecoverySystem.installPackage(this.mContext, this.mCopiedUpdateFile);
            notifyCallbackOnSuccess();
        } catch (IOException e) {
            Log.w("UpdateInstaller", "IO error while trying to install non AB update.", e);
            notifyCallbackOnError(1, Log.getStackTraceString(e));
        } catch (GeneralSecurityException e2) {
            Log.w("UpdateInstaller", "GeneralSecurity error while trying to install non AB update.", e2);
            notifyCallbackOnError(1, Log.getStackTraceString(e2));
        }
    }

    public void onProgress(int progress) {
    }
}
