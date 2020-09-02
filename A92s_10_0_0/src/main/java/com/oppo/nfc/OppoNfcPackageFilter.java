package com.oppo.nfc;

import android.os.RemoteException;
import android.util.Slog;
import com.android.server.slice.SliceClientPermissions;
import java.util.NoSuchElementException;
import vendor.oppo.hardware.nfc.V1_0.IOppoNfc;

public class OppoNfcPackageFilter {
    public static final String TAG = "OppoNfcPackageFilter";

    private boolean isNfcPackageSupported(String path) {
        String[] paths = path.split(SliceClientPermissions.SliceAuthority.DELIMITER);
        if (paths.length == 0) {
            Slog.e(TAG, "Input path not right");
            return false;
        }
        String currentPackageName = paths[paths.length - 1];
        try {
            IOppoNfc mOppoNfcService = IOppoNfc.getService();
            if (mOppoNfcService == null) {
                Slog.e(TAG, "Oppo nfc interface not avaliable");
            }
            boolean isSupported = mOppoNfcService.isNfcPackageSupported(currentPackageName);
            Slog.e(TAG, "is nfc package[ " + currentPackageName + " ] supported = " + isSupported);
            return isSupported;
        } catch (NoSuchElementException e1) {
            Slog.e(TAG, "IOppoNfc is not supported" + e1);
            return false;
        } catch (RemoteException e2) {
            Slog.e(TAG, "Error connecting to IOppoNfc " + e2);
            return false;
        }
    }

    public boolean filterPackage(String packageName, String path) {
        if (!packageName.equals("com.android.nfc")) {
            return false;
        }
        return !isNfcPackageSupported(path);
    }
}
