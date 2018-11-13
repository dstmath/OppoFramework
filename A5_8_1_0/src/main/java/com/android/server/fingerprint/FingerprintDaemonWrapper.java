package com.android.server.fingerprint;

import android.hidl.base.V1_0.DebugInfo;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.SystemClock;
import java.util.ArrayList;
import vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprintClientCallback;

public class FingerprintDaemonWrapper implements IBiometricsFingerprint {
    public static final long TIMEOUT_FINGERPRINTD_BINDERCALL_CHECK = 10000;
    public static final long TIMEOUT_FINGERPRINTD_OPENHAL_CHECK = 10000;
    private IBiometricsFingerprint mDaemon;
    private HealthMonitor mHealthMonitor;

    public FingerprintDaemonWrapper(IBiometricsFingerprint daemon, HealthMonitor monitor) throws RemoteException {
        this.mDaemon = daemon;
        this.mHealthMonitor = monitor;
    }

    public IHwBinder asBinder() {
        return this.mDaemon.asBinder();
    }

    public int authenticate(long sessionId, int groupId) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("authenticate", 10000, session);
            int authenticate = this.mDaemon.authenticate(sessionId, groupId);
            return authenticate;
        } finally {
            this.mHealthMonitor.stop("authenticate", session);
        }
    }

    public int enroll(byte[] token, int groupId, int timeout) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("enroll", 10000, session);
            int enroll = this.mDaemon.enroll(token, groupId, timeout);
            return enroll;
        } finally {
            this.mHealthMonitor.stop("enroll", session);
        }
    }

    public long preEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        long preEnroll;
        try {
            this.mHealthMonitor.start("preEnroll", 10000, session);
            preEnroll = this.mDaemon.preEnroll();
            return preEnroll;
        } finally {
            preEnroll = this.mHealthMonitor;
            preEnroll.stop("preEnroll", session);
        }
    }

    public int remove(int fingerId, int groupId) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("remove", 10000, session);
            int remove = this.mDaemon.remove(fingerId, groupId);
            return remove;
        } finally {
            this.mHealthMonitor.stop("remove", session);
        }
    }

    public long getAuthenticatorId() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        long authenticatorId;
        try {
            this.mHealthMonitor.start("getAuthenticatorId", 10000, session);
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
            this.mHealthMonitor.start("setActiveGroup", 10000, session);
            int activeGroup = this.mDaemon.setActiveGroup(groupId, path);
            return activeGroup;
        } finally {
            this.mHealthMonitor.stop("setActiveGroup", session);
        }
    }

    public int postEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("postEnroll", 10000, session);
            int postEnroll = this.mDaemon.postEnroll();
            return postEnroll;
        } finally {
            this.mHealthMonitor.stop("postEnroll", session);
        }
    }

    public int getAlikeyStatus() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.GETALIKEYSTATUS, 10000, session);
            int alikeyStatus = this.mDaemon.getAlikeyStatus();
            return alikeyStatus;
        } finally {
            this.mHealthMonitor.stop(HealthState.GETALIKEYSTATUS, session);
        }
    }

    public int getEngineeringInfo(int type) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.GETENGINEERINGINFO, 10000, session);
            int engineeringInfo = this.mDaemon.getEngineeringInfo(type);
            return engineeringInfo;
        } finally {
            this.mHealthMonitor.stop(HealthState.GETENGINEERINGINFO, session);
        }
    }

    public int cleanUp() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CLEANUP, 10000, session);
            int cleanUp = this.mDaemon.cleanUp();
            return cleanUp;
        } finally {
            this.mHealthMonitor.stop(HealthState.CLEANUP, session);
        }
    }

    public int pauseEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.PAUSEENROLL, 10000, session);
            int pauseEnroll = this.mDaemon.pauseEnroll();
            return pauseEnroll;
        } finally {
            this.mHealthMonitor.stop(HealthState.PAUSEENROLL, session);
        }
    }

    public int continueEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CONTINUEENROLL, 10000, session);
            int continueEnroll = this.mDaemon.continueEnroll();
            return continueEnroll;
        } finally {
            this.mHealthMonitor.stop(HealthState.CONTINUEENROLL, session);
        }
    }

    public int setTouchEventListener() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SETTOUCHEVENTLISTENER, 10000, session);
            int touchEventListener = this.mDaemon.setTouchEventListener();
            return touchEventListener;
        } finally {
            this.mHealthMonitor.stop(HealthState.SETTOUCHEVENTLISTENER, session);
        }
    }

    public int dynamicallyConfigLog(int on) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("dynamicallyConfigLog", 10000, session);
            int dynamicallyConfigLog = this.mDaemon.dynamicallyConfigLog(on);
            return dynamicallyConfigLog;
        } finally {
            this.mHealthMonitor.stop("dynamicallyConfigLog", session);
        }
    }

    public int pauseIdentify() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.PAUSEIDENTIFY, 10000, session);
            int pauseIdentify = this.mDaemon.pauseIdentify();
            return pauseIdentify;
        } finally {
            this.mHealthMonitor.stop(HealthState.PAUSEIDENTIFY, session);
        }
    }

    public int continueIdentify() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CONTINUEIDENTIFY, 10000, session);
            int continueIdentify = this.mDaemon.continueIdentify();
            return continueIdentify;
        } finally {
            this.mHealthMonitor.stop(HealthState.CONTINUEIDENTIFY, session);
        }
    }

    public int getEnrollmentTotalTimes() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("getEnrollmentTotalTimes", 10000, session);
            int enrollmentTotalTimes = this.mDaemon.getEnrollmentTotalTimes();
            return enrollmentTotalTimes;
        } finally {
            this.mHealthMonitor.stop("getEnrollmentTotalTimes", session);
        }
    }

    public int setScreenState(int state) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SETSCREENSTATE, 10000, session);
            int screenState = this.mDaemon.setScreenState(state);
            return screenState;
        } finally {
            this.mHealthMonitor.stop(HealthState.SETSCREENSTATE, session);
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

    public int enumerate() throws RemoteException {
        return this.mDaemon.enumerate();
    }

    public DebugInfo getDebugInfo() throws RemoteException {
        return this.mDaemon.getDebugInfo();
    }

    public long setNotify(IBiometricsFingerprintClientCallback clientCallback) throws RemoteException {
        return this.mDaemon.setNotify(clientCallback);
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

    public int cancel() throws RemoteException {
        return this.mDaemon.cancel();
    }
}
