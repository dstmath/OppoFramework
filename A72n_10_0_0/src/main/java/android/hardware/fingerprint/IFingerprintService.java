package android.hardware.fingerprint;

import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.fingerprint.IFingerprintClientActiveCallback;
import android.hardware.fingerprint.IFingerprintCommandCallback;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.hardware.fingerprint.IOpticalFingerprintListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IFingerprintService extends IInterface {
    void addClientActiveCallback(IFingerprintClientActiveCallback iFingerprintClientActiveCallback) throws RemoteException;

    void addLockoutResetCallback(IBiometricServiceLockoutResetCallback iBiometricServiceLockoutResetCallback) throws RemoteException;

    byte[] alipayInvokeCommand(byte[] bArr) throws RemoteException;

    void authenticate(IBinder iBinder, long j, int i, IFingerprintServiceReceiver iFingerprintServiceReceiver, int i2, String str) throws RemoteException;

    void cancelAuthentication(IBinder iBinder, String str) throws RemoteException;

    void cancelAuthenticationFromService(IBinder iBinder, String str, int i, int i2, int i3, boolean z) throws RemoteException;

    void cancelEnrollment(IBinder iBinder, String str) throws RemoteException;

    void cancelGetEngineeringInfo(IBinder iBinder, String str, int i) throws RemoteException;

    void cancelTouchEventListener(IBinder iBinder, String str) throws RemoteException;

    int continueEnroll() throws RemoteException;

    int continueIdentify(IBinder iBinder) throws RemoteException;

    void enroll(IBinder iBinder, byte[] bArr, int i, IFingerprintServiceReceiver iFingerprintServiceReceiver, int i2, String str) throws RemoteException;

    void enumerate(IBinder iBinder, int i, IFingerprintServiceReceiver iFingerprintServiceReceiver, String str) throws RemoteException;

    void finishUnLockedScreen(boolean z, String str) throws RemoteException;

    int getAlikeyStatus() throws RemoteException;

    long getAuthenticatorId(String str) throws RemoteException;

    int getCurrentIconStatus() throws RemoteException;

    int getEngineeringInfo(IBinder iBinder, String str, int i, IFingerprintServiceReceiver iFingerprintServiceReceiver, int i2) throws RemoteException;

    List<Fingerprint> getEnrolledFingerprints(int i, String str) throws RemoteException;

    int getEnrollmentTotalTimes(IBinder iBinder) throws RemoteException;

    int getFailedAttempts(String str) throws RemoteException;

    byte[] getFingerprintAuthToken(String str) throws RemoteException;

    long getLockoutAttemptDeadline(String str) throws RemoteException;

    boolean hasEnrolledFingerprints(int i, String str) throws RemoteException;

    void hideFingerprintIcon(int i, String str) throws RemoteException;

    boolean isClientActive() throws RemoteException;

    boolean isHardwareDetected(long j, String str) throws RemoteException;

    int pauseEnroll() throws RemoteException;

    int pauseIdentify(IBinder iBinder) throws RemoteException;

    int postEnroll(IBinder iBinder) throws RemoteException;

    long preEnroll(IBinder iBinder) throws RemoteException;

    void prepareForAuthentication(IBinder iBinder, long j, int i, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, String str, int i2, int i3, int i4, int i5) throws RemoteException;

    int regsiterFingerprintCmdCallback(IFingerprintCommandCallback iFingerprintCommandCallback) throws RemoteException;

    int regsiterOpticalFingerprintListener(IOpticalFingerprintListener iOpticalFingerprintListener) throws RemoteException;

    void remove(IBinder iBinder, int i, int i2, int i3, IFingerprintServiceReceiver iFingerprintServiceReceiver, String str) throws RemoteException;

    void removeClientActiveCallback(IFingerprintClientActiveCallback iFingerprintClientActiveCallback) throws RemoteException;

    void rename(int i, int i2, String str) throws RemoteException;

    void resetTimeout(byte[] bArr) throws RemoteException;

    int sendFingerprintCmd(int i, byte[] bArr) throws RemoteException;

    void setActiveUser(int i) throws RemoteException;

    void setFingerKeymode(IBinder iBinder, int i, String str, int i2) throws RemoteException;

    void setFingerprintEnabled(boolean z) throws RemoteException;

    void setTouchEventListener(IBinder iBinder, IFingerprintServiceReceiver iFingerprintServiceReceiver, int i, String str) throws RemoteException;

    void showFingerprintIcon(String str) throws RemoteException;

    void startPreparedClient(int i) throws RemoteException;

    int touchDown() throws RemoteException;

    int touchUp() throws RemoteException;

    int unregsiterFingerprintCmdCallback(IFingerprintCommandCallback iFingerprintCommandCallback) throws RemoteException;

    int unregsiterOpticalFingerprintListener() throws RemoteException;

    public static class Default implements IFingerprintService {
        @Override // android.hardware.fingerprint.IFingerprintService
        public void authenticate(IBinder token, long sessionId, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void prepareForAuthentication(IBinder token, long sessionId, int userId, IBiometricServiceReceiverInternal wrapperReceiver, String opPackageName, int cookie, int callingUid, int callingPid, int callingUserId) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void startPreparedClient(int cookie) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void cancelAuthentication(IBinder token, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void cancelAuthenticationFromService(IBinder token, String opPackageName, int callingUid, int callingPid, int callingUserId, boolean fromClient) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void enroll(IBinder token, byte[] cryptoToken, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void cancelEnrollment(IBinder token, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void rename(int fingerId, int groupId, String name) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public List<Fingerprint> getEnrolledFingerprints(int groupId, String opPackageName) throws RemoteException {
            return null;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public boolean isHardwareDetected(long deviceId, String opPackageName) throws RemoteException {
            return false;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public long preEnroll(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int postEnroll(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public boolean hasEnrolledFingerprints(int groupId, String opPackageName) throws RemoteException {
            return false;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public long getAuthenticatorId(String opPackageName) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void resetTimeout(byte[] cryptoToken) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void addLockoutResetCallback(IBiometricServiceLockoutResetCallback callback) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void setActiveUser(int uid) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int getEngineeringInfo(IBinder token, String opPackageName, int userId, IFingerprintServiceReceiver receiver, int type) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void cancelGetEngineeringInfo(IBinder token, String opPackageName, int type) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int getEnrollmentTotalTimes(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int pauseEnroll() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int continueEnroll() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void setTouchEventListener(IBinder token, IFingerprintServiceReceiver receiver, int groupId, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void setFingerKeymode(IBinder token, int groupId, String opPackageName, int enable) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void cancelTouchEventListener(IBinder token, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void finishUnLockedScreen(boolean isfinished, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int getAlikeyStatus() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void setFingerprintEnabled(boolean enabled) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int pauseIdentify(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int continueIdentify(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public long getLockoutAttemptDeadline(String opPackageName) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int getFailedAttempts(String opPackageName) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void enumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public boolean isClientActive() throws RemoteException {
            return false;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void addClientActiveCallback(IFingerprintClientActiveCallback callback) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void removeClientActiveCallback(IFingerprintClientActiveCallback callback) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public byte[] alipayInvokeCommand(byte[] inbuf) throws RemoteException {
            return null;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int touchDown() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int touchUp() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int sendFingerprintCmd(int cmdId, byte[] inbuf) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public byte[] getFingerprintAuthToken(String opPackageName) throws RemoteException {
            return null;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int getCurrentIconStatus() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int regsiterFingerprintCmdCallback(IFingerprintCommandCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int unregsiterFingerprintCmdCallback(IFingerprintCommandCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int regsiterOpticalFingerprintListener(IOpticalFingerprintListener listener) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int unregsiterOpticalFingerprintListener() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void showFingerprintIcon(String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void hideFingerprintIcon(int status, String opPackageName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFingerprintService {
        private static final String DESCRIPTOR = "android.hardware.fingerprint.IFingerprintService";
        static final int TRANSACTION_addClientActiveCallback = 36;
        static final int TRANSACTION_addLockoutResetCallback = 17;
        static final int TRANSACTION_alipayInvokeCommand = 38;
        static final int TRANSACTION_authenticate = 1;
        static final int TRANSACTION_cancelAuthentication = 4;
        static final int TRANSACTION_cancelAuthenticationFromService = 5;
        static final int TRANSACTION_cancelEnrollment = 7;
        static final int TRANSACTION_cancelGetEngineeringInfo = 20;
        static final int TRANSACTION_cancelTouchEventListener = 26;
        static final int TRANSACTION_continueEnroll = 23;
        static final int TRANSACTION_continueIdentify = 31;
        static final int TRANSACTION_enroll = 6;
        static final int TRANSACTION_enumerate = 34;
        static final int TRANSACTION_finishUnLockedScreen = 27;
        static final int TRANSACTION_getAlikeyStatus = 28;
        static final int TRANSACTION_getAuthenticatorId = 15;
        static final int TRANSACTION_getCurrentIconStatus = 43;
        static final int TRANSACTION_getEngineeringInfo = 19;
        static final int TRANSACTION_getEnrolledFingerprints = 10;
        static final int TRANSACTION_getEnrollmentTotalTimes = 21;
        static final int TRANSACTION_getFailedAttempts = 33;
        static final int TRANSACTION_getFingerprintAuthToken = 42;
        static final int TRANSACTION_getLockoutAttemptDeadline = 32;
        static final int TRANSACTION_hasEnrolledFingerprints = 14;
        static final int TRANSACTION_hideFingerprintIcon = 49;
        static final int TRANSACTION_isClientActive = 35;
        static final int TRANSACTION_isHardwareDetected = 11;
        static final int TRANSACTION_pauseEnroll = 22;
        static final int TRANSACTION_pauseIdentify = 30;
        static final int TRANSACTION_postEnroll = 13;
        static final int TRANSACTION_preEnroll = 12;
        static final int TRANSACTION_prepareForAuthentication = 2;
        static final int TRANSACTION_regsiterFingerprintCmdCallback = 44;
        static final int TRANSACTION_regsiterOpticalFingerprintListener = 46;
        static final int TRANSACTION_remove = 8;
        static final int TRANSACTION_removeClientActiveCallback = 37;
        static final int TRANSACTION_rename = 9;
        static final int TRANSACTION_resetTimeout = 16;
        static final int TRANSACTION_sendFingerprintCmd = 41;
        static final int TRANSACTION_setActiveUser = 18;
        static final int TRANSACTION_setFingerKeymode = 25;
        static final int TRANSACTION_setFingerprintEnabled = 29;
        static final int TRANSACTION_setTouchEventListener = 24;
        static final int TRANSACTION_showFingerprintIcon = 48;
        static final int TRANSACTION_startPreparedClient = 3;
        static final int TRANSACTION_touchDown = 39;
        static final int TRANSACTION_touchUp = 40;
        static final int TRANSACTION_unregsiterFingerprintCmdCallback = 45;
        static final int TRANSACTION_unregsiterOpticalFingerprintListener = 47;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFingerprintService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFingerprintService)) {
                return new Proxy(obj);
            }
            return (IFingerprintService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "authenticate";
                case 2:
                    return "prepareForAuthentication";
                case 3:
                    return "startPreparedClient";
                case 4:
                    return "cancelAuthentication";
                case 5:
                    return "cancelAuthenticationFromService";
                case 6:
                    return "enroll";
                case 7:
                    return "cancelEnrollment";
                case 8:
                    return "remove";
                case 9:
                    return "rename";
                case 10:
                    return "getEnrolledFingerprints";
                case 11:
                    return "isHardwareDetected";
                case 12:
                    return "preEnroll";
                case 13:
                    return "postEnroll";
                case 14:
                    return "hasEnrolledFingerprints";
                case 15:
                    return "getAuthenticatorId";
                case 16:
                    return "resetTimeout";
                case 17:
                    return "addLockoutResetCallback";
                case 18:
                    return "setActiveUser";
                case 19:
                    return "getEngineeringInfo";
                case 20:
                    return "cancelGetEngineeringInfo";
                case 21:
                    return "getEnrollmentTotalTimes";
                case 22:
                    return "pauseEnroll";
                case 23:
                    return "continueEnroll";
                case 24:
                    return "setTouchEventListener";
                case 25:
                    return "setFingerKeymode";
                case 26:
                    return "cancelTouchEventListener";
                case 27:
                    return "finishUnLockedScreen";
                case 28:
                    return "getAlikeyStatus";
                case 29:
                    return "setFingerprintEnabled";
                case 30:
                    return "pauseIdentify";
                case 31:
                    return "continueIdentify";
                case 32:
                    return "getLockoutAttemptDeadline";
                case 33:
                    return "getFailedAttempts";
                case 34:
                    return "enumerate";
                case 35:
                    return "isClientActive";
                case 36:
                    return "addClientActiveCallback";
                case 37:
                    return "removeClientActiveCallback";
                case 38:
                    return "alipayInvokeCommand";
                case 39:
                    return "touchDown";
                case 40:
                    return "touchUp";
                case 41:
                    return "sendFingerprintCmd";
                case 42:
                    return "getFingerprintAuthToken";
                case 43:
                    return "getCurrentIconStatus";
                case 44:
                    return "regsiterFingerprintCmdCallback";
                case 45:
                    return "unregsiterFingerprintCmdCallback";
                case 46:
                    return "regsiterOpticalFingerprintListener";
                case 47:
                    return "unregsiterOpticalFingerprintListener";
                case 48:
                    return "showFingerprintIcon";
                case 49:
                    return "hideFingerprintIcon";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        authenticate(data.readStrongBinder(), data.readLong(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        prepareForAuthentication(data.readStrongBinder(), data.readLong(), data.readInt(), IBiometricServiceReceiverInternal.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        startPreparedClient(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAuthentication(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAuthenticationFromService(data.readStrongBinder(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        enroll(data.readStrongBinder(), data.createByteArray(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        cancelEnrollment(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        remove(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        rename(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        List<Fingerprint> _result = getEnrolledFingerprints(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHardwareDetected = isHardwareDetected(data.readLong(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isHardwareDetected ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        long _result2 = preEnroll(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeLong(_result2);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = postEnroll(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasEnrolledFingerprints = hasEnrolledFingerprints(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(hasEnrolledFingerprints ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        long _result4 = getAuthenticatorId(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        resetTimeout(data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        addLockoutResetCallback(IBiometricServiceLockoutResetCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        setActiveUser(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getEngineeringInfo(data.readStrongBinder(), data.readString(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        cancelGetEngineeringInfo(data.readStrongBinder(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getEnrollmentTotalTimes(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = pauseEnroll();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = continueEnroll();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        setTouchEventListener(data.readStrongBinder(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        setFingerKeymode(data.readStrongBinder(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        cancelTouchEventListener(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        finishUnLockedScreen(_arg0, data.readString());
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getAlikeyStatus();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setFingerprintEnabled(_arg0);
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = pauseIdentify(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = continueIdentify(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        long _result12 = getLockoutAttemptDeadline(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result12);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getFailedAttempts(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        enumerate(data.readStrongBinder(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isClientActive = isClientActive();
                        reply.writeNoException();
                        reply.writeInt(isClientActive ? 1 : 0);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        addClientActiveCallback(IFingerprintClientActiveCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        removeClientActiveCallback(IFingerprintClientActiveCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result14 = alipayInvokeCommand(data.createByteArray());
                        reply.writeNoException();
                        reply.writeByteArray(_result14);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = touchDown();
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = touchUp();
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = sendFingerprintCmd(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result18 = getFingerprintAuthToken(data.readString());
                        reply.writeNoException();
                        reply.writeByteArray(_result18);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = getCurrentIconStatus();
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = regsiterFingerprintCmdCallback(IFingerprintCommandCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = unregsiterFingerprintCmdCallback(IFingerprintCommandCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = regsiterOpticalFingerprintListener(IOpticalFingerprintListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = unregsiterOpticalFingerprintListener();
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        showFingerprintIcon(data.readString());
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        hideFingerprintIcon(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IFingerprintService {
            public static IFingerprintService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void authenticate(IBinder token, long sessionId, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(sessionId);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flags);
                        _data.writeString(opPackageName);
                        if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().authenticate(token, sessionId, userId, receiver, flags, opPackageName);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void prepareForAuthentication(IBinder token, long sessionId, int userId, IBiometricServiceReceiverInternal wrapperReceiver, String opPackageName, int cookie, int callingUid, int callingPid, int callingUserId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                        _data.writeLong(sessionId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        _data.writeStrongBinder(wrapperReceiver != null ? wrapperReceiver.asBinder() : null);
                        _data.writeString(opPackageName);
                        _data.writeInt(cookie);
                        _data.writeInt(callingUid);
                        _data.writeInt(callingPid);
                        _data.writeInt(callingUserId);
                        if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().prepareForAuthentication(token, sessionId, userId, wrapperReceiver, opPackageName, cookie, callingUid, callingPid, callingUserId);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void startPreparedClient(int cookie) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cookie);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startPreparedClient(cookie);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void cancelAuthentication(IBinder token, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelAuthentication(token, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void cancelAuthenticationFromService(IBinder token, String opPackageName, int callingUid, int callingPid, int callingUserId, boolean fromClient) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(opPackageName);
                        try {
                            _data.writeInt(callingUid);
                            try {
                                _data.writeInt(callingPid);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(callingUserId);
                        _data.writeInt(fromClient ? 1 : 0);
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().cancelAuthenticationFromService(token, opPackageName, callingUid, callingPid, callingUserId, fromClient);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void enroll(IBinder token, byte[] cryptoToken, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByteArray(cryptoToken);
                        try {
                            _data.writeInt(groupId);
                            _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                            try {
                                _data.writeInt(flags);
                                try {
                                    _data.writeString(opPackageName);
                                } catch (Throwable th3) {
                                    th = th3;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().enroll(token, cryptoToken, groupId, receiver, flags, opPackageName);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void cancelEnrollment(IBinder token, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelEnrollment(token, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver, String opPackageName) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(fingerId);
                        try {
                            _data.writeInt(groupId);
                            try {
                                _data.writeInt(userId);
                                _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                            try {
                                _data.writeString(opPackageName);
                            } catch (Throwable th4) {
                                th = th4;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().remove(token, fingerId, groupId, userId, receiver, opPackageName);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void rename(int fingerId, int groupId, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeString(name);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().rename(fingerId, groupId, name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public List<Fingerprint> getEnrolledFingerprints(int groupId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(groupId);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEnrolledFingerprints(groupId, opPackageName);
                    }
                    _reply.readException();
                    List<Fingerprint> _result = _reply.createTypedArrayList(Fingerprint.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public boolean isHardwareDetected(long deviceId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeString(opPackageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHardwareDetected(deviceId, opPackageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public long preEnroll(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().preEnroll(token);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int postEnroll(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().postEnroll(token);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public boolean hasEnrolledFingerprints(int groupId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(groupId);
                    _data.writeString(opPackageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasEnrolledFingerprints(groupId, opPackageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public long getAuthenticatorId(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAuthenticatorId(opPackageName);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void resetTimeout(byte[] cryptoToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(cryptoToken);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetTimeout(cryptoToken);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void addLockoutResetCallback(IBiometricServiceLockoutResetCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addLockoutResetCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void setActiveUser(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setActiveUser(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int getEngineeringInfo(IBinder token, String opPackageName, int userId, IFingerprintServiceReceiver receiver, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(opPackageName);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEngineeringInfo(token, opPackageName, userId, receiver, type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void cancelGetEngineeringInfo(IBinder token, String opPackageName, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(opPackageName);
                    _data.writeInt(type);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelGetEngineeringInfo(token, opPackageName, type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int getEnrollmentTotalTimes(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEnrollmentTotalTimes(token);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int pauseEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().pauseEnroll();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int continueEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().continueEnroll();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void setTouchEventListener(IBinder token, IFingerprintServiceReceiver receiver, int groupId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    _data.writeInt(groupId);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTouchEventListener(token, receiver, groupId, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void setFingerKeymode(IBinder token, int groupId, String opPackageName, int enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(groupId);
                    _data.writeString(opPackageName);
                    _data.writeInt(enable);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFingerKeymode(token, groupId, opPackageName, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void cancelTouchEventListener(IBinder token, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelTouchEventListener(token, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void finishUnLockedScreen(boolean isfinished, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isfinished ? 1 : 0);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(27, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().finishUnLockedScreen(isfinished, opPackageName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int getAlikeyStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAlikeyStatus();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void setFingerprintEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFingerprintEnabled(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int pauseIdentify(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().pauseIdentify(token);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int continueIdentify(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().continueIdentify(token);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public long getLockoutAttemptDeadline(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLockoutAttemptDeadline(opPackageName);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int getFailedAttempts(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFailedAttempts(opPackageName);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void enumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enumerate(token, userId, receiver, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public boolean isClientActive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isClientActive();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void addClientActiveCallback(IFingerprintClientActiveCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addClientActiveCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void removeClientActiveCallback(IFingerprintClientActiveCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeClientActiveCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public byte[] alipayInvokeCommand(byte[] inbuf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(inbuf);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().alipayInvokeCommand(inbuf);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int touchDown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().touchDown();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int touchUp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().touchUp();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int sendFingerprintCmd(int cmdId, byte[] inbuf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmdId);
                    _data.writeByteArray(inbuf);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendFingerprintCmd(cmdId, inbuf);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public byte[] getFingerprintAuthToken(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFingerprintAuthToken(opPackageName);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int getCurrentIconStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentIconStatus();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int regsiterFingerprintCmdCallback(IFingerprintCommandCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().regsiterFingerprintCmdCallback(callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int unregsiterFingerprintCmdCallback(IFingerprintCommandCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregsiterFingerprintCmdCallback(callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int regsiterOpticalFingerprintListener(IOpticalFingerprintListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().regsiterOpticalFingerprintListener(listener);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int unregsiterOpticalFingerprintListener() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregsiterOpticalFingerprintListener();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void showFingerprintIcon(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().showFingerprintIcon(opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void hideFingerprintIcon(int status, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().hideFingerprintIcon(status, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IFingerprintService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFingerprintService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
