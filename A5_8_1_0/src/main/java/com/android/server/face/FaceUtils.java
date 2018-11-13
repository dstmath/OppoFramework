package com.android.server.face;

import android.content.Context;
import android.hardware.face.FaceFeature;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import java.util.List;

public class FaceUtils {
    private static final long[] FACE_ERROR_VIBRATE_PATTERN = new long[]{0, 30, 100, 30};
    private static final long[] FACE_SUCCESS_VIBRATE_PATTERN = new long[]{0, 30};
    private static FaceUtils sInstance;
    private static final Object sInstanceLock = new Object();
    @GuardedBy("this")
    private final SparseArray<FacesUserState> mUsers = new SparseArray();

    public static FaceUtils getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new FaceUtils();
            }
        }
        return sInstance;
    }

    private FaceUtils() {
    }

    public List<FaceFeature> getFacesForUser(Context ctx, int userId) {
        return getStateForUser(ctx, userId).getFaces();
    }

    public void addFaceForUser(Context ctx, int faceId, int userId) {
        getStateForUser(ctx, userId).addFace(faceId, userId);
    }

    public void removeFaceIdForUser(Context ctx, int faceId, int userId) {
        getStateForUser(ctx, userId).removeFace(faceId);
    }

    public void syncFaceIdForUser(Context ctx, int[] faceIds, int userId) {
        getStateForUser(ctx, userId).syncFaces(faceIds, userId);
    }

    public void renameFaceForUser(Context ctx, int faceId, int userId, CharSequence name) {
        if (!TextUtils.isEmpty(name)) {
            getStateForUser(ctx, userId).renameFace(faceId, name);
        }
    }

    public static void vibrateFaceError(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FACE_ERROR_VIBRATE_PATTERN, -1);
        }
    }

    public static void vibrateFaceSuccess(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Vibrator.class);
        if (vibrator != null) {
            vibrator.vibrate(FACE_SUCCESS_VIBRATE_PATTERN, -1);
        }
    }

    private FacesUserState getStateForUser(Context ctx, int userId) {
        FacesUserState state;
        synchronized (this) {
            state = (FacesUserState) this.mUsers.get(userId);
            if (state == null) {
                state = new FacesUserState(ctx, userId);
                this.mUsers.put(userId, state);
            }
        }
        return state;
    }
}
