package com.android.server.face;

import android.content.Context;
import android.hidl.base.V1_0.DebugInfo;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.Surface;
import com.android.server.face.health.HealthMonitor;
import com.android.server.face.health.HealthState;
import com.android.server.face.utils.LogUtil;
import java.io.PrintWriter;
import java.util.ArrayList;
import vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFace;
import vendor.oppo.hardware.biometrics.face.V1_0.IBiometricsFaceClientCallback;
import vendor.oppo.hardware.biometrics.face.V1_0.RectHal;

public class FaceDaemonWrapper implements IBiometricsFace {
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

    public IHwBinder asBinder() {
        return this.mDaemon.asBinder();
    }

    public int authenticate(long sessionId, int groupId, int type) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.authenticate(sessionId, groupId, type);
            this.mHealthMonitor.start("authenticate", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int authenticate = this.mDaemon.authenticate(sessionId, groupId, type);
            return authenticate;
        } finally {
            this.mHealthMonitor.stop("authenticate", session);
        }
    }

    public int cancelAuthentication() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("cancelAuthentication", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int cancelAuthentication = this.mDaemon.cancelAuthentication();
            return cancelAuthentication;
        } finally {
            this.mHealthMonitor.stop("cancelAuthentication", session);
        }
    }

    public int enroll(byte[] token, int groupId, int timeout) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.enroll(groupId, timeout);
            this.mHealthMonitor.start("enroll", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int enroll = this.mDaemon.enroll(token, groupId, timeout);
            return enroll;
        } finally {
            this.mHealthMonitor.stop("enroll", session);
        }
    }

    public int cancelEnrollment() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("cancelEnrollment", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int cancelEnrollment = this.mDaemon.cancelEnrollment();
            return cancelEnrollment;
        } finally {
            this.mHealthMonitor.stop("cancelEnrollment", session);
        }
    }

    public long preEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        long preEnroll;
        try {
            this.mHealthMonitor.start("preEnroll", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            preEnroll = this.mDaemon.preEnroll();
            return preEnroll;
        } finally {
            preEnroll = this.mHealthMonitor;
            preEnroll.stop("preEnroll", session);
        }
    }

    public int remove(int faceId, int groupId) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("remove", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int remove = this.mDaemon.remove(faceId, groupId);
            return remove;
        } finally {
            this.mHealthMonitor.stop("remove", session);
        }
    }

    public long getAuthenticatorId() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        long authenticatorId;
        try {
            this.mHealthMonitor.start("getAuthenticatorId", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            authenticatorId = this.mDaemon.getAuthenticatorId();
            return authenticatorId;
        } finally {
            authenticatorId = this.mHealthMonitor;
            authenticatorId.stop("getAuthenticatorId", session);
        }
    }

    public int setActiveGroup(int groupId, String path) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("setActiveGroup", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int activeGroup = this.mDaemon.setActiveGroup(groupId, path);
            return activeGroup;
        } finally {
            this.mHealthMonitor.stop("setActiveGroup", session);
        }
    }

    public long setNotify(IBiometricsFaceClientCallback callback, boolean modelInit) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        long notify;
        try {
            this.mHealthMonitor.start("init", 10000, session);
            notify = this.mDaemon.setNotify(callback, modelInit);
            return notify;
        } finally {
            notify = this.mHealthMonitor;
            notify.stop("init", session);
        }
    }

    public int postEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("postEnroll", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int postEnroll = this.mDaemon.postEnroll();
            return postEnroll;
        } finally {
            this.mHealthMonitor.stop("postEnroll", session);
        }
    }

    public int executeCommand(int type) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.EXECUTE_COMMOND, TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int executeCommand = this.mDaemon.executeCommand(type);
            return executeCommand;
        } finally {
            this.mHealthMonitor.stop(HealthState.EXECUTE_COMMOND, session);
        }
    }

    public int cancelCommand(int type) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CANCEL_COMMOND, TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int cancelCommand = this.mDaemon.cancelCommand(type);
            return cancelCommand;
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCEL_COMMOND, session);
        }
    }

    public int get(int type) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.GET, TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int i = this.mDaemon.get(type);
            return i;
        } finally {
            this.mHealthMonitor.stop(HealthState.GET, session);
        }
    }

    public int verifyFace(byte[] nv21, long nv21ImageBufferSeq, int clientMode) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.VERIFYFACE, TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int verifyFace = this.mDaemon.verifyFace(nv21, nv21ImageBufferSeq, clientMode);
            return verifyFace;
        } finally {
            this.mHealthMonitor.stop(HealthState.VERIFYFACE, session);
        }
    }

    public int getEnrollmentTotalTimes() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("getEnrollmentTotalTimes", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int enrollmentTotalTimes = this.mDaemon.getEnrollmentTotalTimes();
            return enrollmentTotalTimes;
        } finally {
            this.mHealthMonitor.stop("getEnrollmentTotalTimes", session);
        }
    }

    public int setPreviewFrame(RectHal rectHal) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SET_PREVIEW_FRAME, TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int previewFrame = this.mDaemon.setPreviewFrame(rectHal);
            return previewFrame;
        } finally {
            this.mHealthMonitor.stop(HealthState.SET_PREVIEW_FRAME, session);
        }
    }

    public int setPreviewSurface() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SET_PREVIEW_SURFACE, 10000, session);
            int previewSurface = this.mDaemon.setPreviewSurface();
            return previewSurface;
        } finally {
            this.mHealthMonitor.stop(HealthState.SET_PREVIEW_SURFACE, session);
        }
    }

    public int getPreViewWidth() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            int res = this.mFaceRecoginition.getPreViewWidth();
            this.mHealthMonitor.start(HealthState.GET_PREIVIEW_WIDTH, TIMEOUT_FACED_BINDERCALL_CHECK, session);
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
            this.mHealthMonitor.start(HealthState.GET_PREVIEW_HEIGHT, TIMEOUT_FACED_BINDERCALL_CHECK, session);
            this.mDaemon.getPreViewHeight();
            return res;
        } finally {
            this.mHealthMonitor.stop(HealthState.GET_PREVIEW_HEIGHT, session);
        }
    }

    public int dynamicallyConfigLog(int type, int on) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("dynamicallyConfigLog", TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int dynamicallyConfigLog = this.mDaemon.dynamicallyConfigLog(type, on);
            return dynamicallyConfigLog;
        } finally {
            this.mHealthMonitor.stop("dynamicallyConfigLog", session);
        }
    }

    public int updateRusNativeData(float hacknessThreshold) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.UPDATE_RUS_NATIVE_DATA, TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int updateRusNativeData = this.mDaemon.updateRusNativeData(hacknessThreshold);
            return updateRusNativeData;
        } finally {
            this.mHealthMonitor.stop(HealthState.UPDATE_RUS_NATIVE_DATA, session);
        }
    }

    public int updateSettingSwitchStatus(int switchType, boolean on) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.UPDATE_SETTING_SWITCH_STATUS, TIMEOUT_FACED_BINDERCALL_CHECK, session);
            int updateSettingSwitchStatus = this.mDaemon.updateSettingSwitchStatus(switchType, on);
            return updateSettingSwitchStatus;
        } finally {
            this.mHealthMonitor.stop(HealthState.UPDATE_SETTING_SWITCH_STATUS, session);
        }
    }

    public int cancelRecognition() {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.cancelRecognition();
            this.mHealthMonitor.start(HealthState.CANCEL_RECOGNITION, TIMEOUT_FACED_BINDERCALL_CHECK, session);
            return 0;
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCEL_RECOGNITION, session);
        }
    }

    public void stopPreview() {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.stopPreview();
            this.mHealthMonitor.start(HealthState.STOP_RREVIEW, TIMEOUT_FACED_BINDERCALL_CHECK, session);
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
        this.mFaceRecoginition.dump(pw, args, "");
    }

    public void updateCameraParameters(PrintWriter pw, String parameterArgs) {
        this.mFaceRecoginition.updateCameraParameters(pw, parameterArgs);
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

    public long initDaemon(boolean modelInit) {
        long res = 0;
        try {
            return setNotify(this.mFaceRecoginition.getFaceDaemonCallback(), modelInit);
        } catch (RemoteException e) {
            LogUtil.e(TAG, "Failed to initDaemon", e);
            return res;
        }
    }

    public void setPreviewSurface(Surface surface) {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mFaceRecoginition.setPreviewSurface(surface);
            this.mHealthMonitor.start(HealthState.SET_PREVIEW_SURFACE, TIMEOUT_FACED_BINDERCALL_CHECK, session);
        } finally {
            this.mHealthMonitor.stop(HealthState.SET_PREVIEW_SURFACE, session);
        }
    }

    public ArrayList<String> interfaceChain() throws RemoteException {
        return this.mDaemon.interfaceChain();
    }

    public ArrayList<byte[]> getHashChain() throws RemoteException {
        return this.mDaemon.getHashChain();
    }

    public void notifySyspropsChanged() throws RemoteException {
        this.mDaemon.notifySyspropsChanged();
    }

    public void ping() throws RemoteException {
        this.mDaemon.ping();
    }

    public void setHALInstrumentation() throws RemoteException {
        this.mDaemon.setHALInstrumentation();
    }

    public DebugInfo getDebugInfo() throws RemoteException {
        return this.mDaemon.getDebugInfo();
    }

    public boolean unlinkToDeath(DeathRecipient recipient) throws RemoteException {
        return this.mDaemon.unlinkToDeath(recipient);
    }

    public boolean linkToDeath(DeathRecipient recipient, long arg) throws RemoteException {
        return this.mDaemon.linkToDeath(recipient, arg);
    }

    public String interfaceDescriptor() throws RemoteException {
        return this.mDaemon.interfaceDescriptor();
    }
}
