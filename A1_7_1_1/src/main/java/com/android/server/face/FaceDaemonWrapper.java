package com.android.server.face;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.face.FaceInfoWrapper;
import android.hardware.face.FaceRusNativeData;
import android.hardware.face.IFaceDaemon;
import android.hardware.face.IFaceDaemonCallback;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.Surface;
import com.android.server.face.health.HealthMonitor;
import com.android.server.face.health.HealthState;
import com.android.server.face.utils.LogUtil;
import com.android.server.oppo.IElsaManager;
import java.io.PrintWriter;

public class FaceDaemonWrapper implements IFaceDaemon {
    public static final String TAG = "FaceService.FaceDaemonWrapper";
    public static final long TIMEOUT_FACED_BINDERCALL_CHECK = 5000;
    public static final long TIMEOUT_FACED_OPENHAL_CHECK = 10000;
    private final IFaceDaemon mDaemon;
    private final FaceRecognition mFaceRecoginition;
    private final HealthMonitor mHealthMonitor;

    public FaceDaemonWrapper(IFaceDaemon daemon, HealthMonitor monitor, Context context, IRecognitionCallback recognitionCallback) throws RemoteException {
        this.mDaemon = daemon;
        this.mHealthMonitor = monitor;
        this.mFaceRecoginition = FaceRecognition.getInstance(context, recognitionCallback);
    }

    public IBinder asBinder() {
        return this.mDaemon.asBinder();
    }

    public int authenticate(long sessionId, int groupId, int type) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.authenticate(sessionId, groupId, type);
            this.mHealthMonitor.start(HealthState.AUTHENTICATE, 5000, session);
            int authenticate = this.mDaemon.authenticate(sessionId, groupId, type);
            return authenticate;
        } finally {
            this.mHealthMonitor.stop(HealthState.AUTHENTICATE, session);
        }
    }

    public int cancelAuthentication() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CANCELAUTHENTICATE, 5000, session);
            int cancelAuthentication = this.mDaemon.cancelAuthentication();
            return cancelAuthentication;
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCELAUTHENTICATE, session);
        }
    }

    public int enroll(byte[] token, int groupId, int timeout) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.enroll(groupId, timeout);
            this.mHealthMonitor.start(HealthState.ENROLL, 5000, session);
            int enroll = this.mDaemon.enroll(token, groupId, timeout);
            return enroll;
        } finally {
            this.mHealthMonitor.stop(HealthState.ENROLL, session);
        }
    }

    public int cancelEnrollment() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CANCELENROLLMENT, 5000, session);
            int cancelEnrollment = this.mDaemon.cancelEnrollment();
            return cancelEnrollment;
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCELENROLLMENT, session);
        }
    }

    public long preEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        long preEnroll;
        try {
            this.mHealthMonitor.start(HealthState.PREENROLL, 5000, session);
            preEnroll = this.mDaemon.preEnroll();
            return preEnroll;
        } finally {
            preEnroll = this.mHealthMonitor;
            preEnroll.stop(HealthState.PREENROLL, session);
        }
    }

    public int remove(int faceId, int groupId) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.REMOVE, 5000, session);
            int remove = this.mDaemon.remove(faceId, groupId);
            return remove;
        } finally {
            this.mHealthMonitor.stop(HealthState.REMOVE, session);
        }
    }

    public long getAuthenticatorId() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        long authenticatorId;
        try {
            this.mHealthMonitor.start(HealthState.GETAUTHENTICATORID, 5000, session);
            authenticatorId = this.mDaemon.getAuthenticatorId();
            return authenticatorId;
        } finally {
            authenticatorId = this.mHealthMonitor;
            authenticatorId.stop(HealthState.GETAUTHENTICATORID, session);
        }
    }

    public int setActiveGroup(int groupId, byte[] path, boolean hasFace) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SETACTIVEGROUP, 10000, session);
            int activeGroup = this.mDaemon.setActiveGroup(groupId, path, hasFace);
            return activeGroup;
        } finally {
            this.mHealthMonitor.stop(HealthState.SETACTIVEGROUP, session);
        }
    }

    public long openHal() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        long openHal;
        try {
            this.mHealthMonitor.start(HealthState.OPENHAL, 10000, session);
            openHal = this.mDaemon.openHal();
            return openHal;
        } finally {
            openHal = this.mHealthMonitor;
            openHal.stop(HealthState.OPENHAL, session);
        }
    }

    public int closeHal() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CLOSEHAL, 5000, session);
            int closeHal = this.mDaemon.closeHal();
            return closeHal;
        } finally {
            this.mHealthMonitor.stop(HealthState.CLOSEHAL, session);
        }
    }

    public void init(IFaceDaemonCallback callback, boolean modelInit) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.init();
            this.mHealthMonitor.start(HealthState.INIT, 10000, session);
            this.mDaemon.init(callback, modelInit);
        } finally {
            this.mHealthMonitor.stop(HealthState.INIT, session);
        }
    }

    public int postEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.POSTENROLL, 5000, session);
            int postEnroll = this.mDaemon.postEnroll();
            return postEnroll;
        } finally {
            this.mHealthMonitor.stop(HealthState.POSTENROLL, session);
        }
    }

    public int executeCommand(int type) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.EXECUTE_COMMOND, 5000, session);
            int executeCommand = this.mDaemon.executeCommand(type);
            return executeCommand;
        } finally {
            this.mHealthMonitor.stop(HealthState.EXECUTE_COMMOND, session);
        }
    }

    public int cancelCommand(int type) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CANCEL_COMMOND, 5000, session);
            int cancelCommand = this.mDaemon.cancelCommand(type);
            return cancelCommand;
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCEL_COMMOND, session);
        }
    }

    public int get(int type) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.GET, 5000, session);
            int i = this.mDaemon.get(type);
            return i;
        } finally {
            this.mHealthMonitor.stop(HealthState.GET, session);
        }
    }

    public int verifyFace(byte[] nv21, int width, int height, FaceInfoWrapper faceInfo, int faceOrientation, int clientMode) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.VERIFYFACE, 5000, session);
            int verifyFace = this.mDaemon.verifyFace(nv21, width, height, faceInfo, faceOrientation, clientMode);
            return verifyFace;
        } finally {
            this.mHealthMonitor.stop(HealthState.VERIFYFACE, session);
        }
    }

    public int getEnrollmentTotalTimes() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.GET_ENROLLMENT_TOTAL_TIMES, 5000, session);
            int enrollmentTotalTimes = this.mDaemon.getEnrollmentTotalTimes();
            return enrollmentTotalTimes;
        } finally {
            this.mHealthMonitor.stop(HealthState.GET_ENROLLMENT_TOTAL_TIMES, session);
        }
    }

    public int setPreviewFrame(Rect rect) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.setPreviewFrame(rect);
            this.mHealthMonitor.start(HealthState.SET_PREVIEW_FRAME, 5000, session);
            int previewFrame = this.mDaemon.setPreviewFrame(rect);
            return previewFrame;
        } finally {
            this.mHealthMonitor.stop(HealthState.SET_PREVIEW_FRAME, session);
        }
    }

    public int setPreviewSurface(Surface surface) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.setPreviewSurface(surface);
            this.mHealthMonitor.start(HealthState.SET_PREVIEW_SURFACE, 10000, session);
            int previewSurface = this.mDaemon.setPreviewSurface(surface);
            return previewSurface;
        } finally {
            this.mHealthMonitor.stop(HealthState.SET_PREVIEW_SURFACE, session);
        }
    }

    public int getPreViewWidth() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            int res = this.mFaceRecoginition.getPreViewWidth();
            this.mHealthMonitor.start(HealthState.GET_PREIVIEW_WIDTH, 5000, session);
            this.mDaemon.getPreViewWidth();
            return res;
        } finally {
            this.mHealthMonitor.stop(HealthState.GET_PREIVIEW_WIDTH, session);
        }
    }

    public int getPreViewHeight() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            int res = this.mFaceRecoginition.getPreViewHeight();
            this.mHealthMonitor.start(HealthState.GET_PREVIEW_HEIGHT, 5000, session);
            this.mDaemon.getPreViewHeight();
            return res;
        } finally {
            this.mHealthMonitor.stop(HealthState.GET_PREVIEW_HEIGHT, session);
        }
    }

    public int preRecognition(boolean needStartPreview) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            int res = this.mFaceRecoginition.preRecognition();
            this.mHealthMonitor.start(HealthState.PRE_RECOGNITION, 5000, session);
            this.mDaemon.preRecognition(needStartPreview);
            return res;
        } finally {
            this.mHealthMonitor.stop(HealthState.PRE_RECOGNITION, session);
        }
    }

    public void dynamicallyConfigLog(int type, int on) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.DYNAMICALLY_CONFIG_LOG, 5000, session);
            this.mDaemon.dynamicallyConfigLog(type, on);
        } finally {
            this.mHealthMonitor.stop(HealthState.DYNAMICALLY_CONFIG_LOG, session);
        }
    }

    public void updateRusNativeData(FaceRusNativeData rusNativeData) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.UPDATE_RUS_NATIVE_DATA, 5000, session);
            this.mDaemon.updateRusNativeData(rusNativeData);
        } finally {
            this.mHealthMonitor.stop(HealthState.UPDATE_RUS_NATIVE_DATA, session);
        }
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

    public void stopPreview() {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.stopPreview();
            this.mHealthMonitor.start(HealthState.STOP_RREVIEW, 5000, session);
        } finally {
            this.mHealthMonitor.stop(HealthState.STOP_RREVIEW, session);
        }
    }

    public void binderDied() {
        this.mFaceRecoginition.cancelRecognition();
    }

    public void dump(PrintWriter pw, String[] args, String prefix) {
        String subPrefix = "  " + prefix;
        pw.print(prefix);
        pw.println("FaceDaemonWrapper dump");
        this.mFaceRecoginition.dump(pw, args, IElsaManager.EMPTY_PACKAGE);
    }

    public void updateCameraParameters(PrintWriter pw, String parameter_args) {
        this.mFaceRecoginition.updateCameraParameters(pw, parameter_args);
    }

    public void updateEnrollTimeout(PrintWriter pw, String enrollTimeout) {
        this.mFaceRecoginition.updateEnrollTimeout(pw, enrollTimeout);
    }

    public void autoVerify(PrintWriter pw, String on) {
        this.mFaceRecoginition.autoVerify(pw, on);
    }

    public void adjustDropFrames(PrintWriter pw, String frames) {
        this.mFaceRecoginition.adjustDropFrames(pw, frames);
    }

    public void initDaemon(boolean modelInit) {
        try {
            init(this.mFaceRecoginition.getFaceDaemonCallback(), modelInit);
        } catch (RemoteException e) {
            LogUtil.e(TAG, "Failed to initDaemon", e);
        }
    }
}
