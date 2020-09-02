package vendor.mediatek.hardware.radio_op.V1_2;

import android.internal.hidl.base.V1_0.DebugInfo;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import vendor.mediatek.hardware.radio_op.V1_1.DialFrom;
import vendor.mediatek.hardware.radio_op.V1_1.IDigitsRadioIndication;
import vendor.mediatek.hardware.radio_op.V1_1.IDigitsRadioResponse;
import vendor.mediatek.hardware.radio_op.V1_1.IImsRadioIndicationOp;
import vendor.mediatek.hardware.radio_op.V1_1.IImsRadioResponseOp;
import vendor.mediatek.hardware.radio_op.V1_1.IRadioIndicationOp;
import vendor.mediatek.hardware.radio_op.V1_1.IRadioResponseOp;
import vendor.mediatek.hardware.radio_op.V1_1.IRcsRadioIndication;
import vendor.mediatek.hardware.radio_op.V1_1.IRcsRadioResponse;
import vendor.mediatek.hardware.radio_op.V2_0.RsuRequest;

public interface IRadioOp extends vendor.mediatek.hardware.radio_op.V1_1.IRadioOp {
    public static final String kInterfaceName = "vendor.mediatek.hardware.radio_op@1.2::IRadioOp";

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    IHwBinder asBinder();

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    void exitSCBM(int i) throws RemoteException;

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    void notifySyspropsChanged() throws RemoteException;

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    void ping() throws RemoteException;

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    void setHALInstrumentation() throws RemoteException;

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    static default IRadioOp asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IRadioOp)) {
            return (IRadioOp) iface;
        }
        IRadioOp proxy = new Proxy(binder);
        try {
            Iterator<String> it = proxy.interfaceChain().iterator();
            while (it.hasNext()) {
                if (it.next().equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    static default IRadioOp castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    static default IRadioOp getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: vendor.mediatek.hardware.radio_op.V1_2.IRadioOp.getService(java.lang.String, boolean):vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
     arg types: [java.lang.String, boolean]
     candidates:
      vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.getService(java.lang.String, boolean):vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
      vendor.mediatek.hardware.radio_op.V1_2.IRadioOp.getService(java.lang.String, boolean):vendor.mediatek.hardware.radio_op.V1_2.IRadioOp */
    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    static default IRadioOp getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    static default IRadioOp getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
    static default IRadioOp getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements IRadioOp {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.mediatek.hardware.radio_op@1.2::IRadioOp]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void responseAcknowledgement() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setResponseFunctions(IRadioResponseOp radioResponse, IRadioIndicationOp radioIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setResponseFunctionsIms(IImsRadioResponseOp radioResponse, IImsRadioIndicationOp radioIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setResponseFunctionsDigits(IDigitsRadioResponse radioResponse, IDigitsRadioIndication radioIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setRttMode(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void sendRttModifyRequest(int serial, int callId, int newMode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callId);
            _hidl_request.writeInt32(newMode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void sendRttText(int serial, int callId, int lenOfString, String text) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callId);
            _hidl_request.writeInt32(lenOfString);
            _hidl_request.writeString(text);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void rttModifyRequestResponse(int serial, int callId, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callId);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setDigitsLine(int serial, int accountId, int digitsSerial, boolean isLogout, boolean hasNext, boolean isNative, String msisdn, String sit) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(accountId);
            _hidl_request.writeInt32(digitsSerial);
            _hidl_request.writeBool(isLogout);
            _hidl_request.writeBool(hasNext);
            _hidl_request.writeBool(isNative);
            _hidl_request.writeString(msisdn);
            _hidl_request.writeString(sit);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setTrn(int serial, String fromMsisdn, String toMsisdn, String trn) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(fromMsisdn);
            _hidl_request.writeString(toMsisdn);
            _hidl_request.writeString(trn);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setIncomingVirtualLine(int serial, String fromMsisdn, String toMsisdn) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(fromMsisdn);
            _hidl_request.writeString(toMsisdn);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setRxTestConfig(int serial, int antType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(antType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void getRxTestResult(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void dialFrom(int serial, DialFrom dialInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            dialInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void sendUssiFrom(int serial, String from, int action, String ussi) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(from);
            _hidl_request.writeInt32(action);
            _hidl_request.writeString(ussi);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void cancelUssiFrom(int serial, String from) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(from);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setEmergencyCallConfig(int serial, int category, boolean isForceEcc) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(category);
            _hidl_request.writeBool(isForceEcc);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setDisable2G(int serial, boolean mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void getDisable2G(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void deviceSwitch(int serial, String number, String deviceId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(number);
            _hidl_request.writeString(deviceId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void cancelDeviceSwitch(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setResponseFunctionsRcs(IRcsRadioResponse radioResponse, IRcsRadioIndication radioIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp
        public void setDigitsRegStatus(int serial, String digitsinfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(digitsinfo);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public void exitSCBM(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioOp.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readStringVector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public void debug(NativeHandle fd, ArrayList<String> options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            _hidl_request.writeNativeHandle(fd);
            _hidl_request.writeStringVector(options);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256131655, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readString();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList<>();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    byte[] _hidl_vec_element = new byte[32];
                    childBlob.copyToInt8Array((long) (_hidl_index_0 * 32), _hidl_vec_element, 32);
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
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

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
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

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
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

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
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

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IRadioOp {
        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IRadioOp.kInterfaceName, vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName, "android.hidl.base@1.0::IBase"));
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public final String interfaceDescriptor() {
            return IRadioOp.kInterfaceName;
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{0, -53, -29, -18, 1, -47, 46, -126, -69, -111, -14, -107, -22, 44, 73, -88, -121, 73, 69, 75, 40, -32, -5, 58, -95, 27, -20, 53, -109, -103, -75, -15}, new byte[]{-14, 35, 84, 39, 12, 100, -101, 90, -16, -48, -13, 38, 40, 22, 77, -110, -23, -87, 124, -90, 51, 15, 112, 57, -83, -58, 62, 67, 100, -91, 50, 85}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public final void setHALInstrumentation() {
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public final void ping() {
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.mediatek.hardware.radio_op.V1_1.IRadioOp, vendor.mediatek.hardware.radio_op.V1_2.IRadioOp
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IRadioOp.kInterfaceName.equals(descriptor)) {
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
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case RsuRequest.RSU_REQUEST_GET_SHARED_KEY /*{ENCODED_INT: 1}*/:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    responseAcknowledgement();
                    return;
                case RsuRequest.RSU_REQUEST_UPDATE_LOCK_DATA /*{ENCODED_INT: 2}*/:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setResponseFunctions(IRadioResponseOp.asInterface(_hidl_request.readStrongBinder()), IRadioIndicationOp.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case RsuRequest.RSU_REQUEST_GET_LOCK_VERSION /*{ENCODED_INT: 3}*/:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setResponseFunctionsIms(IImsRadioResponseOp.asInterface(_hidl_request.readStrongBinder()), IImsRadioIndicationOp.asInterface(_hidl_request.readStrongBinder()));
                    return;
                case RsuRequest.RSU_REQUEST_RESET_LOCK_DATA /*{ENCODED_INT: 4}*/:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setResponseFunctionsDigits(IDigitsRadioResponse.asInterface(_hidl_request.readStrongBinder()), IDigitsRadioIndication.asInterface(_hidl_request.readStrongBinder()));
                    return;
                case RsuRequest.RSU_REQUEST_GET_LOCK_STATUS /*{ENCODED_INT: 5}*/:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setRttMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    sendRttModifyRequest(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 7:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    sendRttText(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 8:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    rttModifyRequestResponse(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 9:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setDigitsLine(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readBool(), _hidl_request.readBool(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 10:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setTrn(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 11:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setIncomingVirtualLine(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 12:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setRxTestConfig(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 13:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    getRxTestResult(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 14:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    int serial = _hidl_request.readInt32();
                    DialFrom dialInfo = new DialFrom();
                    dialInfo.readFromParcel(_hidl_request);
                    dialFrom(serial, dialInfo);
                    return;
                case 15:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    sendUssiFrom(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 16:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    cancelUssiFrom(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 17:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setEmergencyCallConfig(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 18:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setDisable2G(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 19:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    getDisable2G(_hidl_request.readInt32());
                    return;
                case 20:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    deviceSwitch(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 21:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    cancelDeviceSwitch(_hidl_request.readInt32());
                    return;
                case 22:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setResponseFunctionsRcs(IRcsRadioResponse.asInterface(_hidl_request.readStrongBinder()), IRcsRadioIndication.asInterface(_hidl_request.readStrongBinder()));
                    return;
                case 23:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.radio_op.V1_1.IRadioOp.kInterfaceName);
                    setDigitsRegStatus(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 24:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IRadioOp.kInterfaceName);
                    exitSCBM(_hidl_request.readInt32());
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                            ArrayList<String> _hidl_out_descriptors = interfaceChain();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeStringVector(_hidl_out_descriptors);
                            _hidl_reply.send();
                            return;
                        case 256131655:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                            debug(_hidl_request.readNativeHandle(), _hidl_request.readStringVector());
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 256136003:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                            String _hidl_out_descriptor = interfaceDescriptor();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeString(_hidl_out_descriptor);
                            _hidl_reply.send();
                            return;
                        case 256398152:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                            ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                            _hidl_reply.writeStatus(0);
                            HwBlob _hidl_blob = new HwBlob(16);
                            int _hidl_vec_size = _hidl_out_hashchain.size();
                            _hidl_blob.putInt32(8, _hidl_vec_size);
                            _hidl_blob.putBool(12, false);
                            HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
                            for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                                long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                                byte[] _hidl_array_item_1 = _hidl_out_hashchain.get(_hidl_index_0);
                                if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 32) {
                                    throw new IllegalArgumentException("Array element is not of the expected length");
                                }
                                childBlob.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
                            }
                            _hidl_blob.putBlob(0, childBlob);
                            _hidl_reply.writeBuffer(_hidl_blob);
                            _hidl_reply.send();
                            return;
                        case 256462420:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                            setHALInstrumentation();
                            return;
                        case 256660548:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        case 256921159:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                            ping();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 257049926:
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                            DebugInfo _hidl_out_info = getDebugInfo();
                            _hidl_reply.writeStatus(0);
                            _hidl_out_info.writeToParcel(_hidl_reply);
                            _hidl_reply.send();
                            return;
                        case 257120595:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (!_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface("android.hidl.base@1.0::IBase");
                            notifySyspropsChanged();
                            return;
                        case 257250372:
                            if ((_hidl_flags & 1) != 0) {
                                _hidl_is_oneway = true;
                            }
                            if (_hidl_is_oneway) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            return;
                        default:
                            return;
                    }
            }
        }
    }
}
