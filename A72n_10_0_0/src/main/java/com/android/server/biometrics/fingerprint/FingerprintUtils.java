package com.android.server.biometrics.fingerprint;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.fingerprint.Fingerprint;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.server.biometrics.BiometricUtils;
import com.android.server.wm.OppoMultiAppManagerUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class FingerprintUtils implements BiometricUtils {
    private static final long[] FP_ERROR_VIBRATE_PATTERN = {0, 30, 100, 30};
    private static final long[] FP_KEY_VIBRATE_PATTERN = {0, 10, 20, 30};
    private static final long[] FP_SUCCESS_VIBRATE_PATTERN = {0, 30};
    private static FingerprintUtils sInstance;
    private static final Object sInstanceLock = new Object();
    @GuardedBy({"this"})
    private final SparseArray<FingerprintUserState> mUsers = new SparseArray<>();

    public static FingerprintUtils getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FingerprintUtils();
            }
        }
        return sInstance;
    }

    private FingerprintUtils() {
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public List<Fingerprint> getBiometricsForUser(Context ctx, int userId) {
        return getStateForUser(ctx, userId).getBiometrics();
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public void addBiometricForUser(Context context, int userId, BiometricAuthenticator.Identifier identifier) {
        getStateForUser(context, userId).addBiometric(identifier);
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public void removeBiometricForUser(Context context, int userId, int fingerId) {
        getStateForUser(context, userId).removeBiometric(fingerId);
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public void renameBiometricForUser(Context context, int userId, int fingerId, CharSequence name) {
        if (!TextUtils.isEmpty(name)) {
            getStateForUser(context, userId).renameBiometric(fingerId, name);
        }
    }

    @Override // com.android.server.biometrics.BiometricUtils
    public CharSequence getUniqueName(Context context, int userId) {
        return getStateForUser(context, userId).getUniqueName();
    }

    private FingerprintUserState getStateForUser(Context ctx, int userId) {
        FingerprintUserState state;
        synchronized (this) {
            state = this.mUsers.get(userId);
            if (state == null) {
                state = new FingerprintUserState(ctx, userId);
                this.mUsers.put(userId, state);
            }
        }
        return state;
    }

    public static void vibrateFingerprintKey(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FP_KEY_VIBRATE_PATTERN, -1);
        }
    }

    public static void vibrateFingerprintSuccess(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FP_SUCCESS_VIBRATE_PATTERN, -1);
        }
    }

    public static void vibrateFingerprintError(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FP_ERROR_VIBRATE_PATTERN, -1);
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args, String prefix, Context ctx, int userId) {
        pw.print("  " + prefix);
        pw.println("userId = " + userId);
        getStateForUser(ctx, userId).dump(fd, pw, args, "  ");
    }

    public void syncFingerprintIdForUser(Context ctx, int[] fingerIds, int userId) {
        getStateForUser(ctx, userId).syncFingerprints(fingerIds, userId);
    }

    public static boolean isMultiApp(int userId, String opPackageName) {
        return OppoMultiAppManagerUtil.getInstance().isMultiApp(userId, opPackageName);
    }
}
