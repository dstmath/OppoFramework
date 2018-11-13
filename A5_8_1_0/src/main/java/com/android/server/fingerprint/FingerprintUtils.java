package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.server.am.OppoMultiAppManagerUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class FingerprintUtils {
    private static final long[] FP_ERROR_VIBRATE_PATTERN = new long[]{0, 30, 100, 30};
    private static final long[] FP_KEY_VIBRATE_PATTERN = new long[]{0, 10, 20, 30};
    private static final long[] FP_SUCCESS_VIBRATE_PATTERN = new long[]{0, 30};
    private static FingerprintUtils sInstance;
    private static final Object sInstanceLock = new Object();
    @GuardedBy("this")
    private final SparseArray<FingerprintsUserState> mUsers = new SparseArray();

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

    public List<Fingerprint> getFingerprintsForUser(Context ctx, int userId) {
        return getStateForUser(ctx, userId).getFingerprints();
    }

    public void addFingerprintForUser(Context ctx, int fingerId, int userId) {
        getStateForUser(ctx, userId).addFingerprint(fingerId, userId);
    }

    public void removeFingerprintIdForUser(Context ctx, int fingerId, int userId) {
        getStateForUser(ctx, userId).removeFingerprint(fingerId);
    }

    public void syncFingerprintIdForUser(Context ctx, int[] fingerIds, int userId) {
        getStateForUser(ctx, userId).syncFingerprints(fingerIds, userId);
    }

    public void renameFingerprintForUser(Context ctx, int fingerId, int userId, CharSequence name) {
        if (!TextUtils.isEmpty(name)) {
            getStateForUser(ctx, userId).renameFingerprint(fingerId, name);
        }
    }

    public static void vibrateFingerprintError(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FP_ERROR_VIBRATE_PATTERN, -1);
        }
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

    private FingerprintsUserState getStateForUser(Context ctx, int userId) {
        FingerprintsUserState state;
        synchronized (this) {
            state = (FingerprintsUserState) this.mUsers.get(userId);
            if (state == null) {
                state = new FingerprintsUserState(ctx, userId);
                this.mUsers.put(userId, state);
            }
        }
        return state;
    }

    public static boolean isMultiApp(int userId, String opPackageName) {
        return OppoMultiAppManagerUtil.getInstance().isMultiApp(userId, opPackageName);
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args, String prefix, Context ctx, int userId) {
        pw.print("  " + prefix);
        pw.println("userId = " + userId);
        getStateForUser(ctx, userId).dump(fd, pw, args, "  ");
    }
}
