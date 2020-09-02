package com.android.server.biometrics.fingerprint;

import android.hidl.base.V1_0.DebugInfo;
import android.os.IHwBinder;
import android.os.NativeHandle;
import android.os.RemoteException;
import android.os.SystemClock;
import com.android.server.biometrics.fingerprint.tool.HealthMonitor;
import com.android.server.biometrics.fingerprint.tool.HealthState;
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

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public IHwBinder asBinder() {
        return this.mDaemon.asBinder();
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int authenticate(long sessionId, int groupId) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("authenticate", 10000, session);
            return this.mDaemon.authenticate(sessionId, groupId);
        } finally {
            this.mHealthMonitor.stop("authenticate", session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int authenticateAsType(long sessionId, int groupId, int fingerprintAuthType) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("authenticate", 10000, session);
            return this.mDaemon.authenticateAsType(sessionId, groupId, fingerprintAuthType);
        } finally {
            this.mHealthMonitor.stop("authenticate", session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int enroll(byte[] token, int groupId, int timeout) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("enroll", 10000, session);
            return this.mDaemon.enroll(token, groupId, timeout);
        } finally {
            this.mHealthMonitor.stop("enroll", session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public long preEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.PREENROLL, 10000, session);
            return this.mDaemon.preEnroll();
        } finally {
            this.mHealthMonitor.stop(HealthState.PREENROLL, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int remove(int fingerId, int groupId) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("remove", 10000, session);
            return this.mDaemon.remove(fingerId, groupId);
        } finally {
            this.mHealthMonitor.stop("remove", session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public long getAuthenticatorId() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("getAuthenticatorId", 10000, session);
            return this.mDaemon.getAuthenticatorId();
        } finally {
            this.mHealthMonitor.stop("getAuthenticatorId", session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int setActiveGroup(int groupId, String path) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SETACTIVEGROUP, 10000, session);
            return this.mDaemon.setActiveGroup(groupId, path);
        } finally {
            this.mHealthMonitor.stop(HealthState.SETACTIVEGROUP, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int postEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.POSTENROLL, 10000, session);
            return this.mDaemon.postEnroll();
        } finally {
            this.mHealthMonitor.stop(HealthState.POSTENROLL, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int getAlikeyStatus() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.GETALIKEYSTATUS, 10000, session);
            return this.mDaemon.getAlikeyStatus();
        } finally {
            this.mHealthMonitor.stop(HealthState.GETALIKEYSTATUS, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int getEngineeringInfo(int type) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.GETENGINEERINGINFO, 10000, session);
            return this.mDaemon.getEngineeringInfo(type);
        } finally {
            this.mHealthMonitor.stop(HealthState.GETENGINEERINGINFO, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int cleanUp() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CLEANUP, 10000, session);
            return this.mDaemon.cleanUp();
        } finally {
            this.mHealthMonitor.stop(HealthState.CLEANUP, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int pauseEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.PAUSEENROLL, 10000, session);
            return this.mDaemon.pauseEnroll();
        } finally {
            this.mHealthMonitor.stop(HealthState.PAUSEENROLL, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int continueEnroll() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CONTINUEENROLL, 10000, session);
            return this.mDaemon.continueEnroll();
        } finally {
            this.mHealthMonitor.stop(HealthState.CONTINUEENROLL, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int setTouchEventListener() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SETTOUCHEVENTLISTENER, 10000, session);
            return this.mDaemon.setTouchEventListener();
        } finally {
            this.mHealthMonitor.stop(HealthState.SETTOUCHEVENTLISTENER, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int setFingerKeymode(int enable) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SETTOUCHEVENTLISTENER, 10000, session);
            return this.mDaemon.setFingerKeymode(enable);
        } finally {
            this.mHealthMonitor.stop(HealthState.SETTOUCHEVENTLISTENER, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int dynamicallyConfigLog(int on) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("dynamicallyConfigLog", 10000, session);
            return this.mDaemon.dynamicallyConfigLog(on);
        } finally {
            this.mHealthMonitor.stop("dynamicallyConfigLog", session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int pauseIdentify() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.PAUSEIDENTIFY, 10000, session);
            return this.mDaemon.pauseIdentify();
        } finally {
            this.mHealthMonitor.stop(HealthState.PAUSEIDENTIFY, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int continueIdentify() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CONTINUEIDENTIFY, 10000, session);
            return this.mDaemon.continueIdentify();
        } finally {
            this.mHealthMonitor.stop(HealthState.CONTINUEIDENTIFY, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int getEnrollmentTotalTimes() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start("getEnrollmentTotalTimes", 10000, session);
            return this.mDaemon.getEnrollmentTotalTimes();
        } finally {
            this.mHealthMonitor.stop("getEnrollmentTotalTimes", session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int setScreenState(int state) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SETSCREENSTATE, 10000, session);
            return this.mDaemon.setScreenState(state);
        } finally {
            this.mHealthMonitor.stop(HealthState.SETSCREENSTATE, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int touchDown() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.TOUCHDOWN, 10000, session);
            return this.mDaemon.touchDown();
        } finally {
            this.mHealthMonitor.stop(HealthState.TOUCHDOWN, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int touchUp() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.TOUCHUP, 10000, session);
            return this.mDaemon.touchUp();
        } finally {
            this.mHealthMonitor.stop(HealthState.TOUCHUP, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int sendFingerprintCmd(int cmdId, ArrayList<Byte> inBuffer) throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.SENDFINGRTPRINTCMD, 10000, session);
            return this.mDaemon.sendFingerprintCmd(cmdId, inBuffer);
        } finally {
            this.mHealthMonitor.stop(HealthState.SENDFINGRTPRINTCMD, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public ArrayList<String> interfaceChain() throws RemoteException {
        return this.mDaemon.interfaceChain();
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public ArrayList<byte[]> getHashChain() throws RemoteException {
        return this.mDaemon.getHashChain();
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public void notifySyspropsChanged() throws RemoteException {
        this.mDaemon.notifySyspropsChanged();
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public void ping() throws RemoteException {
        this.mDaemon.ping();
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public void setHALInstrumentation() throws RemoteException {
        this.mDaemon.setHALInstrumentation();
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int enumerate() throws RemoteException {
        return this.mDaemon.enumerate();
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public DebugInfo getDebugInfo() throws RemoteException {
        return this.mDaemon.getDebugInfo();
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public long setNotify(IBiometricsFingerprintClientCallback clientCallback) throws RemoteException {
        return this.mDaemon.setNotify(clientCallback);
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
        return this.mDaemon.unlinkToDeath(recipient);
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long arg) throws RemoteException {
        return this.mDaemon.linkToDeath(recipient, arg);
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public String interfaceDescriptor() throws RemoteException {
        return this.mDaemon.interfaceDescriptor();
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint
    public int cancel() throws RemoteException {
        String session = String.valueOf(SystemClock.uptimeMillis());
        try {
            this.mHealthMonitor.start(HealthState.CANCEL, 10000, session);
            return this.mDaemon.cancel();
        } finally {
            this.mHealthMonitor.stop(HealthState.CANCEL, session);
        }
    }

    @Override // vendor.oppo.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint, android.hidl.base.V1_0.IBase
    public void debug(NativeHandle mNativeHandle, ArrayList<String> arrayList) throws RemoteException {
    }
}
