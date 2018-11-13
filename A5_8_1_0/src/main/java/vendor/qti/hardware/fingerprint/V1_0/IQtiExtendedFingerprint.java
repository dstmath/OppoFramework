package vendor.qti.hardware.fingerprint.V1_0;

import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public interface IQtiExtendedFingerprint extends IBase {
    public static final String kInterfaceName = "vendor.qti.hardware.fingerprint@1.0::IQtiExtendedFingerprint";

    public static final class Proxy implements IQtiExtendedFingerprint {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.qti.hardware.fingerprint@1.0::IQtiExtendedFingerprint]@Proxy";
            }
        }

        public int testInit() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int testDeinit() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public String testParamGet(int name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt32(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                String _hidl_out_value = _hidl_reply.readString();
                return _hidl_out_value;
            } finally {
                _hidl_reply.release();
            }
        }

        public void testParamSet(int name, String value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt32(name);
            _hidl_request.writeString(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public ImageQualityTestResult testImageQuality() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ImageQualityTestResult _hidl_out_result = new ImageQualityTestResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public PsfCalibrationResult psfCalibraton() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                PsfCalibrationResult _hidl_out_result = new PsfCalibrationResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public PsfSettingFormat calibratePsfSettings() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                PsfSettingFormat _hidl_out_result = new PsfSettingFormat();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public PsfVerificationResult runPsfVerification() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                PsfVerificationResult _hidl_out_result = new PsfVerificationResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public DigitalTestResult testDigital() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DigitalTestResult _hidl_out_result = new DigitalTestResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public int bgeCalibration() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public TemperatureTestResult testTemperature() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                TemperatureTestResult _hidl_out_result = new TemperatureTestResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public ContactResistanceTestResult testContactResistance() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ContactResistanceTestResult _hidl_out_result = new ContactResistanceTestResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public CaptouchCalibrationResult captouchCalibration() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                CaptouchCalibrationResult _hidl_out_result = new CaptouchCalibrationResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public int runBoostRegulation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public BiasTestResult testTft() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                BiasTestResult _hidl_out_result = new BiasTestResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public BiasTestResult testRx() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                BiasTestResult _hidl_out_result = new BiasTestResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public TxTestResult testTx() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                TxTestResult _hidl_out_result = new TxTestResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public int bgeBinsExpiration(int bgeNumRangesToExpire) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt32(bgeNumRangesToExpire);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public DbiasCalibrationResult dbiasCalibration() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DbiasCalibrationResult _hidl_out_result = new DbiasCalibrationResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public NoiseTestResult testNoise() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                NoiseTestResult _hidl_out_result = new NoiseTestResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public ReadInfoResult readInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ReadInfoResult _hidl_out_result = new ReadInfoResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public int getPingResult() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int sendPingCmd() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int writeCalibration() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public String calibrationGet(String name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                String _hidl_out_value = _hidl_reply.readString();
                return _hidl_out_value;
            } finally {
                _hidl_reply.release();
            }
        }

        public void calibrationSet(String name, String value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeString(name);
            _hidl_request.writeString(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public int toggleDumpStream(int onOrOff) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt32(onOrOff);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int powerTest() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public OtpTestResult getOtpValue() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                OtpTestResult _hidl_out_result = new OtpTestResult();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public int isFingerprintEnabled() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int notifyPowerState(int powerState) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt32(powerState);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int postEvent(ArrayList<Byte> payload) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt8Vector(payload);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int notifyAlarm() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int registerAndroidServices(IQfpAndroidServices androidServices) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            if (androidServices != null) {
                iHwBinder = androidServices.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int match(IQtiExtendedFingerprintCallback cb, int timeoutMs, String user, ArrayList<Byte> nonce, String secAppName, int option) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            if (cb != null) {
                iHwBinder = cb.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            _hidl_request.writeInt32(timeoutMs);
            _hidl_request.writeString(user);
            _hidl_request.writeInt8Vector(nonce);
            _hidl_request.writeString(secAppName);
            _hidl_request.writeInt32(option);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int cancel() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(36, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public String retrieveUser(long userId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt64(userId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(37, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                String _hidl_out_enrolleeId = _hidl_reply.readString();
                return _hidl_out_enrolleeId;
            } finally {
                _hidl_reply.release();
            }
        }

        public int enableFingerEvent(int eventKeyCode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt32(eventKeyCode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(38, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int disableFingerEvent() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(39, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int setLivenessEnabled(ArrayList<Byte> hat, byte enabled) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt8Vector(hat);
            _hidl_request.writeInt8(enabled);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(40, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public byte getLivenessEnabled(ArrayList<Byte> hat) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt8Vector(hat);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(41, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                byte _hidl_out_isEnabled = _hidl_reply.readInt8();
                return _hidl_out_isEnabled;
            } finally {
                _hidl_reply.release();
            }
        }

        public int startCapture(IQtiExtendedFingerprintCallback cb, int mode) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            if (cb != null) {
                iHwBinder = cb.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(42, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public FrameworkInfo getFrameworkInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(43, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                FrameworkInfo _hidl_out_info = new FrameworkInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<String> enumEnrollments() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(44, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<String> _hidl_out_enrollees = _hidl_reply.readStringVector();
                return _hidl_out_enrollees;
            } finally {
                _hidl_reply.release();
            }
        }

        public EnrollRecord getEnrollRecord(String enrolleeId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeString(enrolleeId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(45, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                EnrollRecord _hidl_out_record = new EnrollRecord();
                _hidl_out_record.readFromParcel(_hidl_reply);
                return _hidl_out_record;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<Byte> getDebugData(String property) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeString(property);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(46, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<Byte> _hidl_out_data = _hidl_reply.readInt8Vector();
                return _hidl_out_data;
            } finally {
                _hidl_reply.release();
            }
        }

        public byte isIvvEnabled() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(47, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                byte _hidl_out_enabled = _hidl_reply.readInt8();
                return _hidl_out_enabled;
            } finally {
                _hidl_reply.release();
            }
        }

        public int enableIvv() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(48, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int disableIvv() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(49, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<Byte> processRequest(ArrayList<Byte> request) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt8Vector(request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(50, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<Byte> _hidl_out_response = _hidl_reply.readInt8Vector();
                return _hidl_out_response;
            } finally {
                _hidl_reply.release();
            }
        }

        public FieldMfgFormat getMfgValue(short id) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeInt16(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(51, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                FieldMfgFormat _hidl_out_result = new FieldMfgFormat();
                _hidl_out_result.readFromParcel(_hidl_reply);
                return _hidl_out_result;
            } finally {
                _hidl_reply.release();
            }
        }

        public int retrieveDebugData(IQtiExtendedFingerprintCallback cb) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            if (cb != null) {
                iHwBinder = cb.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(52, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int pauseEnrollment() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(53, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int resumeEnrollment() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(54, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public void setConfigValue(String name, String value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeString(name);
            _hidl_request.writeString(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(55, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public String getConfigValue(String name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(56, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                String _hidl_out_value = _hidl_reply.readString();
                return _hidl_out_value;
            } finally {
                _hidl_reply.release();
            }
        }

        public int enableFingerDetect() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(57, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int disableFingerDetect() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(58, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int skipFingerDetect() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiExtendedFingerprint.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(59, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<String> _hidl_out_descriptors = _hidl_reply.readStringVector();
                return _hidl_out_descriptors;
            } finally {
                _hidl_reply.release();
            }
        }

        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                String _hidl_out_descriptor = _hidl_reply.readString();
                return _hidl_out_descriptor;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    Object _hidl_vec_element = new byte[32];
                    long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                    for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                        _hidl_vec_element[_hidl_index_1_0] = childBlob.getInt8(_hidl_array_offset_1);
                        _hidl_array_offset_1++;
                    }
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean linkToDeath(DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257049926, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DebugInfo _hidl_out_info = new DebugInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean unlinkToDeath(DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IQtiExtendedFingerprint {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList(Arrays.asList(new String[]{IQtiExtendedFingerprint.kInterfaceName, "android.hidl.base@1.0::IBase"}));
        }

        public final String interfaceDescriptor() {
            return IQtiExtendedFingerprint.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) 122, (byte) 96, (byte) 51, (byte) -107, (byte) -52, (byte) 95, (byte) 71, (byte) 84, (byte) -40, (byte) 42, (byte) -8, (byte) -55, (byte) -71, (byte) -53, (byte) 82, (byte) 2, (byte) -74, (byte) -20, (byte) -27, (byte) -107, (byte) -76, (byte) 111, (byte) -36, (byte) 103, (byte) 21, (byte) -88, (byte) 53, (byte) 40, (byte) 124, (byte) 93, (byte) 49, (byte) 58}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
        }

        public final void setHALInstrumentation() {
        }

        public final boolean linkToDeath(DeathRecipient recipient, long cookie) {
            return true;
        }

        public final void ping() {
        }

        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = -1;
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        public final void notifySyspropsChanged() {
            SystemProperties.reportSyspropChanged();
        }

        public final boolean unlinkToDeath(DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IQtiExtendedFingerprint.kInterfaceName.equals(descriptor)) {
                return this;
            }
            return null;
        }

        public void registerAsService(String serviceName) throws RemoteException {
            registerService(serviceName);
        }

        public String toString() {
            return interfaceDescriptor() + "@Stub";
        }

        public void onTransact(int _hidl_code, HwParcel _hidl_request, HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            int _hidl_out_status;
            String _hidl_out_value;
            BiasTestResult _hidl_out_result;
            switch (_hidl_code) {
                case Status.FAILURE /*1*/:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = testInit();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 2:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = testDeinit();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 3:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_value = testParamGet(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_value);
                    _hidl_reply.send();
                    return;
                case 4:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    testParamSet(_hidl_request.readInt32(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 5:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    ImageQualityTestResult _hidl_out_result2 = testImageQuality();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result2.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 6:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    PsfCalibrationResult _hidl_out_result3 = psfCalibraton();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result3.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 7:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    PsfSettingFormat _hidl_out_result4 = calibratePsfSettings();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result4.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 8:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    PsfVerificationResult _hidl_out_result5 = runPsfVerification();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result5.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 9:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    DigitalTestResult _hidl_out_result6 = testDigital();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result6.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 10:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = bgeCalibration();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 11:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    TemperatureTestResult _hidl_out_result7 = testTemperature();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result7.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 12:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    ContactResistanceTestResult _hidl_out_result8 = testContactResistance();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result8.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 13:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    CaptouchCalibrationResult _hidl_out_result9 = captouchCalibration();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result9.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 14:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = runBoostRegulation();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 15:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_result = testTft();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 16:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_result = testRx();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 17:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    TxTestResult _hidl_out_result10 = testTx();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result10.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 18:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = bgeBinsExpiration(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 19:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    DbiasCalibrationResult _hidl_out_result11 = dbiasCalibration();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result11.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 20:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    NoiseTestResult _hidl_out_result12 = testNoise();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result12.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 21:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    ReadInfoResult _hidl_out_result13 = readInfo();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result13.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 22:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = getPingResult();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 23:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = sendPingCmd();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 24:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = writeCalibration();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 25:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_value = calibrationGet(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_value);
                    _hidl_reply.send();
                    return;
                case 26:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    calibrationSet(_hidl_request.readString(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 27:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = toggleDumpStream(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 28:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = powerTest();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 29:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    OtpTestResult _hidl_out_result14 = getOtpValue();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result14.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 30:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = isFingerprintEnabled();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 31:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = notifyPowerState(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 32:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = postEvent(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 33:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = notifyAlarm();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 34:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = registerAndroidServices(IQfpAndroidServices.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 35:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = match(IQtiExtendedFingerprintCallback.asInterface(_hidl_request.readStrongBinder()), _hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt8Vector(), _hidl_request.readString(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 36:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = cancel();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 37:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    String _hidl_out_enrolleeId = retrieveUser(_hidl_request.readInt64());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_enrolleeId);
                    _hidl_reply.send();
                    return;
                case 38:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = enableFingerEvent(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 39:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = disableFingerEvent();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 40:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = setLivenessEnabled(_hidl_request.readInt8Vector(), _hidl_request.readInt8());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 41:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    byte _hidl_out_isEnabled = getLivenessEnabled(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8(_hidl_out_isEnabled);
                    _hidl_reply.send();
                    return;
                case 42:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = startCapture(IQtiExtendedFingerprintCallback.asInterface(_hidl_request.readStrongBinder()), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 43:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    FrameworkInfo _hidl_out_info = getFrameworkInfo();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_info.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 44:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    ArrayList<String> _hidl_out_enrollees = enumEnrollments();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_enrollees);
                    _hidl_reply.send();
                    return;
                case 45:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    EnrollRecord _hidl_out_record = getEnrollRecord(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_record.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 46:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    ArrayList<Byte> _hidl_out_data = getDebugData(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8Vector(_hidl_out_data);
                    _hidl_reply.send();
                    return;
                case 47:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    byte _hidl_out_enabled = isIvvEnabled();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8(_hidl_out_enabled);
                    _hidl_reply.send();
                    return;
                case 48:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = enableIvv();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 49:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = disableIvv();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 50:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    ArrayList<Byte> _hidl_out_response = processRequest(_hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8Vector(_hidl_out_response);
                    _hidl_reply.send();
                    return;
                case 51:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    FieldMfgFormat _hidl_out_result15 = getMfgValue(_hidl_request.readInt16());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_result15.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 52:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = retrieveDebugData(IQtiExtendedFingerprintCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 53:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = pauseEnrollment();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 54:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = resumeEnrollment();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 55:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    setConfigValue(_hidl_request.readString(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 56:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_value = getConfigValue(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_value);
                    _hidl_reply.send();
                    return;
                case 57:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = enableFingerDetect();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 58:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = disableFingerDetect();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 59:
                    _hidl_request.enforceInterface(IQtiExtendedFingerprint.kInterfaceName);
                    _hidl_out_status = skipFingerDetect();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 256067662:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    ArrayList<String> _hidl_out_descriptors = interfaceChain();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_descriptors);
                    _hidl_reply.send();
                    return;
                case 256131655:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 256136003:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    String _hidl_out_descriptor = interfaceDescriptor();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_descriptor);
                    _hidl_reply.send();
                    return;
                case 256398152:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                    _hidl_reply.writeStatus(0);
                    HwBlob _hidl_blob = new HwBlob(16);
                    int _hidl_vec_size = _hidl_out_hashchain.size();
                    _hidl_blob.putInt32(8, _hidl_vec_size);
                    _hidl_blob.putBool(12, false);
                    HwBlob hwBlob = new HwBlob(_hidl_vec_size * 32);
                    for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                        long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                        for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                            hwBlob.putInt8(_hidl_array_offset_1, ((byte[]) _hidl_out_hashchain.get(_hidl_index_0))[_hidl_index_1_0]);
                            _hidl_array_offset_1++;
                        }
                    }
                    _hidl_blob.putBlob(0, hwBlob);
                    _hidl_reply.writeBuffer(_hidl_blob);
                    _hidl_reply.send();
                    return;
                case 256462420:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    setHALInstrumentation();
                    return;
                case 257049926:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    DebugInfo _hidl_out_info2 = getDebugInfo();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_info2.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 257120595:
                    _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                    notifySyspropsChanged();
                    return;
                default:
                    return;
            }
        }
    }

    IHwBinder asBinder();

    int bgeBinsExpiration(int i) throws RemoteException;

    int bgeCalibration() throws RemoteException;

    PsfSettingFormat calibratePsfSettings() throws RemoteException;

    String calibrationGet(String str) throws RemoteException;

    void calibrationSet(String str, String str2) throws RemoteException;

    int cancel() throws RemoteException;

    CaptouchCalibrationResult captouchCalibration() throws RemoteException;

    DbiasCalibrationResult dbiasCalibration() throws RemoteException;

    int disableFingerDetect() throws RemoteException;

    int disableFingerEvent() throws RemoteException;

    int disableIvv() throws RemoteException;

    int enableFingerDetect() throws RemoteException;

    int enableFingerEvent(int i) throws RemoteException;

    int enableIvv() throws RemoteException;

    ArrayList<String> enumEnrollments() throws RemoteException;

    String getConfigValue(String str) throws RemoteException;

    ArrayList<Byte> getDebugData(String str) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    EnrollRecord getEnrollRecord(String str) throws RemoteException;

    FrameworkInfo getFrameworkInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    byte getLivenessEnabled(ArrayList<Byte> arrayList) throws RemoteException;

    FieldMfgFormat getMfgValue(short s) throws RemoteException;

    OtpTestResult getOtpValue() throws RemoteException;

    int getPingResult() throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    int isFingerprintEnabled() throws RemoteException;

    byte isIvvEnabled() throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    int match(IQtiExtendedFingerprintCallback iQtiExtendedFingerprintCallback, int i, String str, ArrayList<Byte> arrayList, String str2, int i2) throws RemoteException;

    int notifyAlarm() throws RemoteException;

    int notifyPowerState(int i) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    int pauseEnrollment() throws RemoteException;

    void ping() throws RemoteException;

    int postEvent(ArrayList<Byte> arrayList) throws RemoteException;

    int powerTest() throws RemoteException;

    ArrayList<Byte> processRequest(ArrayList<Byte> arrayList) throws RemoteException;

    PsfCalibrationResult psfCalibraton() throws RemoteException;

    ReadInfoResult readInfo() throws RemoteException;

    int registerAndroidServices(IQfpAndroidServices iQfpAndroidServices) throws RemoteException;

    int resumeEnrollment() throws RemoteException;

    int retrieveDebugData(IQtiExtendedFingerprintCallback iQtiExtendedFingerprintCallback) throws RemoteException;

    String retrieveUser(long j) throws RemoteException;

    int runBoostRegulation() throws RemoteException;

    PsfVerificationResult runPsfVerification() throws RemoteException;

    int sendPingCmd() throws RemoteException;

    void setConfigValue(String str, String str2) throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    int setLivenessEnabled(ArrayList<Byte> arrayList, byte b) throws RemoteException;

    int skipFingerDetect() throws RemoteException;

    int startCapture(IQtiExtendedFingerprintCallback iQtiExtendedFingerprintCallback, int i) throws RemoteException;

    ContactResistanceTestResult testContactResistance() throws RemoteException;

    int testDeinit() throws RemoteException;

    DigitalTestResult testDigital() throws RemoteException;

    ImageQualityTestResult testImageQuality() throws RemoteException;

    int testInit() throws RemoteException;

    NoiseTestResult testNoise() throws RemoteException;

    String testParamGet(int i) throws RemoteException;

    void testParamSet(int i, String str) throws RemoteException;

    BiasTestResult testRx() throws RemoteException;

    TemperatureTestResult testTemperature() throws RemoteException;

    BiasTestResult testTft() throws RemoteException;

    TxTestResult testTx() throws RemoteException;

    int toggleDumpStream(int i) throws RemoteException;

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

    int writeCalibration() throws RemoteException;

    static IQtiExtendedFingerprint asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IQtiExtendedFingerprint)) {
            return (IQtiExtendedFingerprint) iface;
        }
        IQtiExtendedFingerprint proxy = new Proxy(binder);
        try {
            for (String descriptor : proxy.interfaceChain()) {
                if (descriptor.equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static IQtiExtendedFingerprint castFrom(IHwInterface iface) {
        return iface == null ? null : asInterface(iface.asBinder());
    }

    static IQtiExtendedFingerprint getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IQtiExtendedFingerprint getService() throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, "default"));
    }
}
