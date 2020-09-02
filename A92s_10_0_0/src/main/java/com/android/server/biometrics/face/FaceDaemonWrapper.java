package com.android.server.biometrics.face;

import android.content.Context;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.Surface;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.face.health.HealthMonitor;
import com.android.server.biometrics.face.health.HealthState;
import com.android.server.biometrics.face.utils.LogUtil;
import java.util.ArrayList;
import vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFace;
import vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback;
import vendor.oppo.hardware.biometrics.face.V1_0.RectHal;

public class FaceDaemonWrapper implements BiometricServiceBase.DaemonWrapper {
    public static final String TAG = "FaceService.FaceDaemonWrapper";
    public static final long TIMEOUT_FACED_BINDERCALL_CHECK = 5000;
    public static final long TIMEOUT_FACED_OPENHAL_CHECK = 10000;
    private final IBiometricsFace mDaemon;
    private final FaceRecognition mFaceRecoginition;
    private final HealthMonitor mHealthMonitor;

    public FaceDaemonWrapper(IBiometricsFace daemon, HealthMonitor monitor, Context context, IRecognitionCallback recognitionCallback) throws RemoteException {
        this.mDaemon = daemon;
        this.mHealthMonitor = monitor;
        this.mFaceRecoginition = FaceRecognition.getInstance(context, recognitionCallback);
    }

    /* access modifiers changed from: package-private */
    public IBiometricsFaceClientCallback getFaceDaemonCallback() {
        return this.mFaceRecoginition.getFaceDaemonCallback();
    }

    @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
    public int authenticate(long operationId, int groupId) throws RemoteException {
        IBiometricsFace daemon = this.mDaemon;
        if (daemon == null) {
            LogUtil.w(TAG, "authenticate(): no face HAL!");
            return 3;
        }
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.authenticate(operationId, groupId);
            this.mHealthMonitor.start("authenticate", 5000, session);
            return daemon.authenticate(operationId);
        } finally {
            this.mHealthMonitor.stop("authenticate", session);
        }
    }

    @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
    public int cancel() throws RemoteException {
        IBiometricsFace daemon = this.mDaemon;
        if (daemon == null) {
            LogUtil.w(TAG, "cancel(): no face HAL!");
            return 3;
        }
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.cancelRecognition();
            this.mHealthMonitor.start(HealthState.CANCEL_FACE, 5000, session);
            return daemon.cancel();
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCEL_FACE, session);
        }
    }

    @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
    public int enroll(byte[] cryptoToken, int groupId, int timeout, ArrayList<Integer> disabledFeatures) throws RemoteException {
        IBiometricsFace daemon = this.mDaemon;
        if (daemon == null) {
            LogUtil.w(TAG, "enroll(): no face HAL!");
            return 3;
        }
        ArrayList<Byte> token = new ArrayList<>();
        for (byte b : cryptoToken) {
            token.add(Byte.valueOf(b));
        }
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.enroll(groupId, timeout);
            this.mHealthMonitor.start("enroll", 5000, session);
            return daemon.enroll(token, timeout, disabledFeatures);
        } finally {
            this.mHealthMonitor.stop("enroll", session);
        }
    }

    @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
    public int remove(int groupId, int biometricId) throws RemoteException {
        IBiometricsFace daemon = this.mDaemon;
        if (daemon == null) {
            LogUtil.w(TAG, "remove(): no face HAL!");
            return 3;
        }
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("remove", 5000, session);
            return daemon.remove(biometricId);
        } finally {
            this.mHealthMonitor.stop("remove", session);
        }
    }

    @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
    public int enumerate() throws RemoteException {
        IBiometricsFace daemon = this.mDaemon;
        if (daemon == null) {
            LogUtil.w(TAG, "enumerate(): no face HAL!");
            return 3;
        }
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.ENUMERATE, 5000, session);
            return daemon.enumerate();
        } finally {
            this.mHealthMonitor.stop(HealthState.ENUMERATE, session);
        }
    }

    @Override // com.android.server.biometrics.BiometricServiceBase.DaemonWrapper
    public void resetLockout(byte[] cryptoToken) throws RemoteException {
        IBiometricsFace daemon = this.mDaemon;
        if (daemon == null) {
            LogUtil.w(TAG, "resetLockout(): no face HAL!");
            return;
        }
        ArrayList<Byte> token = new ArrayList<>();
        for (byte b : cryptoToken) {
            token.add(Byte.valueOf(b));
        }
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.RESET_LOCKOUT, 5000, session);
            daemon.resetLockout(token);
        } finally {
            this.mHealthMonitor.stop(HealthState.RESET_LOCKOUT, session);
        }
    }

    public int setPreviewFrame(RectHal rectHal) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SET_PREVIEW_FRAME, 5000, session);
            return this.mDaemon.setPreviewFrame(rectHal);
        } finally {
            this.mHealthMonitor.stop(HealthState.SET_PREVIEW_FRAME, session);
        }
    }

    public int getPreViewWidth() throws RemoteException {
        return this.mFaceRecoginition.getPreViewWidth();
    }

    public int getPreViewHeight() throws RemoteException {
        return this.mFaceRecoginition.getPreViewHeight();
    }

    public int cancelRecognition() {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.cancelRecognition();
            this.mHealthMonitor.start(HealthState.CANCEL_RECOGNITION, 5000, session);
            return 0;
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCEL_RECOGNITION, session);
        }
    }

    public void serviceDied() {
        this.mFaceRecoginition.cancelRecognition();
    }

    public void setPreviewSurface(Surface surface) {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.setPreviewSurface(surface);
            this.mHealthMonitor.start(HealthState.SET_PREVIEW_SURFACE, 5000, session);
        } finally {
            this.mHealthMonitor.stop(HealthState.SET_PREVIEW_SURFACE, session);
        }
    }

    public void removeCameraTimeoutMessage() {
        this.mFaceRecoginition.removeCameraTimeoutMessage();
    }
}
