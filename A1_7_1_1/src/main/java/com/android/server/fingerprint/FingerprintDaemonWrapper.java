package com.android.server.fingerprint;

import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintDaemonCallback;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;

public class FingerprintDaemonWrapper implements IFingerprintDaemon {
    public static final long TIMEOUT_FINGERPRINTD_BINDERCALL_CHECK = 10000;
    public static final long TIMEOUT_FINGERPRINTD_OPENHAL_CHECK = 10000;
    private IFingerprintDaemon mDaemon;
    private HealthMonitor mHealthMonitor;

    public FingerprintDaemonWrapper(IFingerprintDaemon daemon, HealthMonitor monitor) throws RemoteException {
        this.mDaemon = daemon;
        this.mHealthMonitor = monitor;
    }

    public IBinder asBinder() {
        return this.mDaemon.asBinder();
    }

    public int authenticate(long sessionId, int groupId) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.AUTHENTICATE, 10000, session);
            int authenticate = this.mDaemon.authenticate(sessionId, groupId);
            return authenticate;
        } finally {
            this.mHealthMonitor.stop(HealthState.AUTHENTICATE, session);
        }
    }

    public int cancelAuthentication() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CANCELAUTHENTICATE, 10000, session);
            int cancelAuthentication = this.mDaemon.cancelAuthentication();
            return cancelAuthentication;
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCELAUTHENTICATE, session);
        }
    }

    public int enroll(byte[] token, int groupId, int timeout) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.ENROLL, 10000, session);
            int enroll = this.mDaemon.enroll(token, groupId, timeout);
            return enroll;
        } finally {
            this.mHealthMonitor.stop(HealthState.ENROLL, session);
        }
    }

    public int cancelEnrollment() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CANCELENROLLMENT, 10000, session);
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
            this.mHealthMonitor.start(HealthState.PREENROLL, 10000, session);
            preEnroll = this.mDaemon.preEnroll();
            return preEnroll;
        } finally {
            preEnroll = this.mHealthMonitor;
            preEnroll.stop(HealthState.PREENROLL, session);
        }
    }

    public int remove(int fingerId, int groupId) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.REMOVE, 10000, session);
            int remove = this.mDaemon.remove(fingerId, groupId);
            return remove;
        } finally {
            this.mHealthMonitor.stop(HealthState.REMOVE, session);
        }
    }

    public long getAuthenticatorId() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        long authenticatorId;
        try {
            this.mHealthMonitor.start(HealthState.GETAUTHENTICATORID, 10000, session);
            authenticatorId = this.mDaemon.getAuthenticatorId();
            return authenticatorId;
        } finally {
            authenticatorId = this.mHealthMonitor;
            authenticatorId.stop(HealthState.GETAUTHENTICATORID, session);
        }
    }

    public int setActiveGroup(int groupId, byte[] path) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SETACTIVEGROUP, 10000, session);
            int activeGroup = this.mDaemon.setActiveGroup(groupId, path);
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
            this.mHealthMonitor.start(HealthState.CLOSEHAL, 10000, session);
            int closeHal = this.mDaemon.closeHal();
            return closeHal;
        } finally {
            this.mHealthMonitor.stop(HealthState.CLOSEHAL, session);
        }
    }

    public void init(IFingerprintDaemonCallback callback) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.INIT, 10000, session);
            this.mDaemon.init(callback);
        } finally {
            this.mHealthMonitor.stop(HealthState.INIT, session);
        }
    }

    public int postEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.POSTENROLL, 10000, session);
            int postEnroll = this.mDaemon.postEnroll();
            return postEnroll;
        } finally {
            this.mHealthMonitor.stop(HealthState.POSTENROLL, session);
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

    public int cancelGetEngineeringInfo() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CANCELGETENGINEERINGINFO, 10000, session);
            int cancelGetEngineeringInfo = this.mDaemon.cancelGetEngineeringInfo();
            return cancelGetEngineeringInfo;
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCELGETENGINEERINGINFO, session);
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

    public int cancelTouchEventListener() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CANCELTOUCHEVENTLISTENER, 10000, session);
            int cancelTouchEventListener = this.mDaemon.cancelTouchEventListener();
            return cancelTouchEventListener;
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCELTOUCHEVENTLISTENER, session);
        }
    }

    public void dynamicallyConfigLog(int on) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.DYNAMICALLYCONFIGLOG, 10000, session);
            this.mDaemon.dynamicallyConfigLog(on);
        } finally {
            this.mHealthMonitor.stop(HealthState.DYNAMICALLYCONFIGLOG, session);
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
            this.mHealthMonitor.start(HealthState.GETENROLLMENTTOTALTIMES, 10000, session);
            int enrollmentTotalTimes = this.mDaemon.getEnrollmentTotalTimes();
            return enrollmentTotalTimes;
        } finally {
            this.mHealthMonitor.stop(HealthState.GETENROLLMENTTOTALTIMES, session);
        }
    }

    public void setScreenState(int state) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SETSCREENSTATE, 10000, session);
            this.mDaemon.setScreenState(state);
        } finally {
            this.mHealthMonitor.stop(HealthState.SETSCREENSTATE, session);
        }
    }
}
