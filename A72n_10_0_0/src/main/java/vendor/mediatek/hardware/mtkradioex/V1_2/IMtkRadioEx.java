package vendor.mediatek.hardware.mtkradioex.V1_2;

import android.hardware.radio.V1_0.CdmaSmsAck;
import android.hardware.radio.V1_0.Dial;
import android.hardware.radio.V1_0.ImsSmsMessage;
import android.internal.hidl.base.V1_0.DebugInfo;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.NativeHandle;
import android.os.RemoteException;
import com.mediatek.android.mms.pdu.MtkCharacterSets;
import com.mediatek.android.mms.pdu.MtkPduHeaders;
import com.mediatek.android.mms.pdu.MtkPduPart;
import com.mediatek.internal.telephony.MtkEtwsUtils;
import com.mediatek.internal.telephony.MtkWspTypeDecoder;
import com.mediatek.internal.telephony.cat.BipUtils;
import com.mediatek.internal.telephony.cat.MtkCatService;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import com.mediatek.internal.telephony.worldphone.WorldMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import vendor.mediatek.hardware.mtkradioex.V1_0.CallForwardInfoEx;
import vendor.mediatek.hardware.mtkradioex.V1_0.ConferenceDial;
import vendor.mediatek.hardware.mtkradioex.V1_0.IAssistRadioResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.IAtciIndication;
import vendor.mediatek.hardware.mtkradioex.V1_0.IAtciResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.ICapRadioResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.IEmRadioIndication;
import vendor.mediatek.hardware.mtkradioex.V1_0.IEmRadioResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.IImsRadioIndication;
import vendor.mediatek.hardware.mtkradioex.V1_0.IImsRadioResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication;
import vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.IMwiRadioIndication;
import vendor.mediatek.hardware.mtkradioex.V1_0.IMwiRadioResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.IRcsRadioIndication;
import vendor.mediatek.hardware.mtkradioex.V1_0.IRcsRadioResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.ISERadioIndication;
import vendor.mediatek.hardware.mtkradioex.V1_0.ISERadioResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.ISubsidyLockIndication;
import vendor.mediatek.hardware.mtkradioex.V1_0.ISubsidyLockResponse;
import vendor.mediatek.hardware.mtkradioex.V1_0.PhbEntryExt;
import vendor.mediatek.hardware.mtkradioex.V1_0.PhbEntryStructure;
import vendor.mediatek.hardware.mtkradioex.V1_0.SimAuthStructure;
import vendor.mediatek.hardware.mtkradioex.V1_0.SmsParams;

public interface IMtkRadioEx extends vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx {
    public static final String kInterfaceName = "vendor.mediatek.hardware.mtkradioex@1.2::IMtkRadioEx";

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    IHwBinder asBinder();

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    void notifySyspropsChanged() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    void ping() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    void setHALInstrumentation() throws RemoteException;

    void setMaxUlSpeed(int i, int i2) throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    static IMtkRadioEx asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IMtkRadioEx)) {
            return (IMtkRadioEx) iface;
        }
        IMtkRadioEx proxy = new Proxy(binder);
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

    static IMtkRadioEx castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    static IMtkRadioEx getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    static IMtkRadioEx getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    static IMtkRadioEx getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IMtkRadioEx getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements IMtkRadioEx {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.mediatek.hardware.mtkradioex@1.2::IMtkRadioEx]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void responseAcknowledgementMtk() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResponseFunctionsMtk(IMtkRadioExResponse radioResponse, IMtkRadioExIndication radioIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResponseFunctionsIms(IImsRadioResponse radioResponse, IImsRadioIndication radioIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResponseFunctionsMwi(IMwiRadioResponse radioResponse, IMwiRadioIndication radioIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResponseFunctionsSE(ISERadioResponse radioResponse, ISERadioIndication radioIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResponseFunctionsEm(IEmRadioResponse radioResponse, IEmRadioIndication radioIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResponseFunctionsAssist(IAssistRadioResponse radioResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResponseFunctionsCap(ICapRadioResponse radioResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void videoCallAccept(int serial, int videoMode, int callId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(videoMode);
            _hidl_request.writeInt32(callId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void imsEctCommand(int serial, String number, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(number);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void controlCall(int serial, int controlType, int callId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(controlType);
            _hidl_request.writeInt32(callId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void imsDeregNotification(int serial, int cause) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(cause);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setImsEnable(int serial, boolean isOn) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(isOn);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setImsVideoEnable(int serial, boolean isOn) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(isOn);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setImscfg(int serial, boolean volteEnable, boolean vilteEnable, boolean vowifiEnable, boolean viwifiEnable, boolean smsEnable, boolean eimsEnable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(volteEnable);
            _hidl_request.writeBool(vilteEnable);
            _hidl_request.writeBool(vowifiEnable);
            _hidl_request.writeBool(viwifiEnable);
            _hidl_request.writeBool(smsEnable);
            _hidl_request.writeBool(eimsEnable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getProvisionValue(int serial, String provisionstring) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(provisionstring);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setProvisionValue(int serial, String provisionstring, String provisionValue) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(provisionstring);
            _hidl_request.writeString(provisionValue);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void controlImsConferenceCallMember(int serial, int controlType, int confCallId, String address, int callId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(controlType);
            _hidl_request.writeInt32(confCallId);
            _hidl_request.writeString(address);
            _hidl_request.writeInt32(callId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setWfcProfile(int serial, int wfcPreference) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(wfcPreference);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void conferenceDial(int serial, ConferenceDial dailInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            dailInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setModemImsCfg(int serial, String keys, String values, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(keys);
            _hidl_request.writeString(values);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void dialWithSipUri(int serial, String address) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(address);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void vtDialWithSipUri(int serial, String address) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(address);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void vtDial(int serial, Dial dialInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            dialInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void forceReleaseCall(int serial, int callId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void imsBearerStateConfirm(int serial, int aid, int action, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(aid);
            _hidl_request.writeInt32(action);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setImsRtpReport(int serial, int pdnId, int networkId, int timer) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(pdnId);
            _hidl_request.writeInt32(networkId);
            _hidl_request.writeInt32(timer);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void pullCall(int serial, String target, boolean isVideoCall) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(target);
            _hidl_request.writeBool(isVideoCall);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setImsRegistrationReport(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendEmbmsAtCommand(int serial, String data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setRoamingEnable(int serial, ArrayList<Integer> config) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32Vector(config);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getRoamingEnable(int serial, int phoneId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(phoneId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setBarringPasswordCheckedByNW(int serial, String facility, String oldPassword, String newPassword, String cfmPassword) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(facility);
            _hidl_request.writeString(oldPassword);
            _hidl_request.writeString(newPassword);
            _hidl_request.writeString(cfmPassword);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setClip(int serial, int clipEnable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(clipEnable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getColp(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getColr(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(36, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendCnap(int serial, String cnapssMessage) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(cnapssMessage);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(37, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setColp(int serial, int colpEnable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(colpEnable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(38, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setColr(int serial, int colrEnable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(colrEnable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(39, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void queryCallForwardInTimeSlotStatus(int serial, CallForwardInfoEx callInfoEx) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            callInfoEx.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(40, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setCallForwardInTimeSlot(int serial, CallForwardInfoEx callInfoEx) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            callInfoEx.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(41, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void runGbaAuthentication(int serial, String nafFqdn, String nafSecureProtocolId, boolean forceRun, int netId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(nafFqdn);
            _hidl_request.writeString(nafSecureProtocolId);
            _hidl_request.writeBool(forceRun);
            _hidl_request.writeInt32(netId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(42, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendUssi(int serial, String ussiString) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(ussiString);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(43, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void cancelUssi(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(44, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getXcapStatus(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(45, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void resetSuppServ(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(46, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setupXcapUserAgentString(int serial, String userAgent) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(userAgent);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(47, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void hangupAll(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(48, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setCallIndication(int serial, int mode, int callId, int seqNumber, int cause) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            _hidl_request.writeInt32(callId);
            _hidl_request.writeInt32(seqNumber);
            _hidl_request.writeInt32(cause);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(49, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setEccMode(int serial, String number, int enable, int airplaneMode, int imsReg) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(number);
            _hidl_request.writeInt32(enable);
            _hidl_request.writeInt32(airplaneMode);
            _hidl_request.writeInt32(imsReg);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(50, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void eccPreferredRat(int serial, int phoneType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(phoneType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(51, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setVoicePreferStatus(int serial, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(52, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setEccNum(int serial, String ecc_list_with_card, String ecc_list_no_card) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(ecc_list_with_card);
            _hidl_request.writeString(ecc_list_no_card);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(53, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getEccNum(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(54, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void queryPhbStorageInfo(int serial, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(55, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void writePhbEntry(int serial, PhbEntryStructure phbEntry) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            phbEntry.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(56, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void readPhbEntry(int serial, int type, int bIndex, int eIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(bIndex);
            _hidl_request.writeInt32(eIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(57, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void queryUPBCapability(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(58, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void editUPBEntry(int serial, ArrayList<String> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeStringVector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(59, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void deleteUPBEntry(int serial, int entryType, int adnIndex, int entryIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(entryType);
            _hidl_request.writeInt32(adnIndex);
            _hidl_request.writeInt32(entryIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(60, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void readUPBGasList(int serial, int startIndex, int endIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(startIndex);
            _hidl_request.writeInt32(endIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(61, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void readUPBGrpEntry(int serial, int adnIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(adnIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(62, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void writeUPBGrpEntry(int serial, int adnIndex, ArrayList<Integer> grpIds) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(adnIndex);
            _hidl_request.writeInt32Vector(grpIds);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(63, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getPhoneBookStringsLength(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(64, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getPhoneBookMemStorage(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(65, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setPhoneBookMemStorage(int serial, String storage, String password) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(storage);
            _hidl_request.writeString(password);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(66, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void readPhoneBookEntryExt(int serial, int index1, int index2) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(index1);
            _hidl_request.writeInt32(index2);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(67, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void writePhoneBookEntryExt(int serial, PhbEntryExt phbEntryExt) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            phbEntryExt.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(68, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void queryUPBAvailable(int serial, int eftype, int fileIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(eftype);
            _hidl_request.writeInt32(fileIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(69, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void readUPBEmailEntry(int serial, int adnIndex, int fileIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(adnIndex);
            _hidl_request.writeInt32(fileIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(70, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void readUPBSneEntry(int serial, int adnIndex, int fileIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(adnIndex);
            _hidl_request.writeInt32(fileIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(71, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void readUPBAnrEntry(int serial, int adnIndex, int fileIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(adnIndex);
            _hidl_request.writeInt32(fileIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(72, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void readUPBAasList(int serial, int startIndex, int endIndex) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(startIndex);
            _hidl_request.writeInt32(endIndex);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(73, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setPhonebookReady(int serial, int ready) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(ready);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(74, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setModemPower(int serial, boolean isOn) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(isOn);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(75, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void triggerModeSwitchByEcc(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(76, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getATR(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(77, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getIccid(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(78, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setSimPower(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(79, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void activateUiccCard(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(80, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void deactivateUiccCard(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(81, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getCurrentUiccCardProvisioningStatus(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(82, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void doGeneralSimAuthentication(int serial, SimAuthStructure simAuth) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            simAuth.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(83, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void queryNetworkLock(int serial, int category) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(category);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(84, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setNetworkLock(int serial, int category, int lockop, String password, String data_imsi, String gid1, String gid2) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(category);
            _hidl_request.writeInt32(lockop);
            _hidl_request.writeString(password);
            _hidl_request.writeString(data_imsi);
            _hidl_request.writeString(gid1);
            _hidl_request.writeString(gid2);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(85, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void supplyDepersonalization(int serial, String netPin, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(netPin);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(86, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendVsimNotification(int serial, int transactionId, int eventId, int simType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(transactionId);
            _hidl_request.writeInt32(eventId);
            _hidl_request.writeInt32(simType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(87, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendVsimOperation(int serial, int transactionId, int eventId, int result, int dataLength, ArrayList<Byte> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(transactionId);
            _hidl_request.writeInt32(eventId);
            _hidl_request.writeInt32(result);
            _hidl_request.writeInt32(dataLength);
            _hidl_request.writeInt8Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(88, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getSmsParameters(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(89, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setSmsParameters(int serial, SmsParams message) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            message.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(90, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getSmsMemStatus(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(91, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setEtws(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(92, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void removeCbMsg(int serial, int channelId, int serialId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(channelId);
            _hidl_request.writeInt32(serialId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(93, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setGsmBroadcastLangs(int serial, String langs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(langs);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(94, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getGsmBroadcastLangs(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(95, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getGsmBroadcastActivation(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(96, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendImsSmsEx(int serial, ImsSmsMessage message) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            message.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(97, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void acknowledgeLastIncomingGsmSmsEx(int serial, boolean success, int cause) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(success);
            _hidl_request.writeInt32(cause);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(98, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void acknowledgeLastIncomingCdmaSmsEx(int serial, CdmaSmsAck smsAck) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            smsAck.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(99, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendRequestRaw(int serial, ArrayList<Byte> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt8Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(100, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendRequestStrings(int serial, ArrayList<String> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeStringVector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(101, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResumeRegistration(int serial, int sessionId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(sessionId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(102, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void modifyModemType(int serial, int applyType, int modemType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(applyType);
            _hidl_request.writeInt32(modemType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(103, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getSmsRuimMemoryStatus(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact((int) MtkCharacterSets.ISO_2022_CN, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setNetworkSelectionModeManualWithAct(int serial, String operatorNumeric, String act, String mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(operatorNumeric);
            _hidl_request.writeString(act);
            _hidl_request.writeString(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact((int) MtkCharacterSets.ISO_2022_CN_EXT, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getAvailableNetworksWithAct(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(106, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getSignalStrengthWithWcdmaEcio(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(107, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void cancelAvailableNetworks(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(108, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getFemtocellList(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(109, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void abortFemtocellList(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(110, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void selectFemtocell(int serial, String operatorNumeric, String act, String csgId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(operatorNumeric);
            _hidl_request.writeString(act);
            _hidl_request.writeString(csgId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(111, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void queryFemtoCellSystemSelectionMode(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact((int) MtkCharacterSets.ISO_8859_16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setFemtoCellSystemSelectionMode(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact((int) MtkCharacterSets.GBK, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setServiceStateToModem(int serial, int voiceRegState, int dataRegState, int voiceRoamingType, int dataRoamingType, int rilVoiceRegState, int rilDataRegState) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(voiceRegState);
            _hidl_request.writeInt32(dataRegState);
            _hidl_request.writeInt32(voiceRoamingType);
            _hidl_request.writeInt32(dataRoamingType);
            _hidl_request.writeInt32(rilVoiceRegState);
            _hidl_request.writeInt32(rilDataRegState);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact((int) MtkCharacterSets.GB18030, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void cfgA2offset(int serial, int offset, int threshBound) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(offset);
            _hidl_request.writeInt32(threshBound);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(115, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void cfgB1offset(int serial, int offset, int threshBound) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(offset);
            _hidl_request.writeInt32(threshBound);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(116, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void enableSCGfailure(int serial, boolean enable, int T1, int P1, int T2) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(enable);
            _hidl_request.writeInt32(T1);
            _hidl_request.writeInt32(P1);
            _hidl_request.writeInt32(T2);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(117, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void disableNR(int serial, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(118, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setTxPower(int serial, int limitpower) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(limitpower);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(119, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setSearchStoredFreqInfo(int serial, int operation, int plmn_id, int rat, ArrayList<Integer> freq) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(operation);
            _hidl_request.writeInt32(plmn_id);
            _hidl_request.writeInt32(rat);
            _hidl_request.writeInt32Vector(freq);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(120, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setSearchRat(int serial, ArrayList<Integer> rat) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32Vector(rat);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(121, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setBgsrchDeltaSleepTimer(int serial, int sleepDuration) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(sleepDuration);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(122, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setRxTestConfig(int serial, int antType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(antType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(123, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getRxTestResult(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(124, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getPOLCapability(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(125, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getCurrentPOLList(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(126, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setPOLEntry(int serial, int index, String numeric, int nAct) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(index);
            _hidl_request.writeString(numeric);
            _hidl_request.writeInt32(nAct);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(127, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setFdMode(int serial, int mode, int param1, int param2) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            _hidl_request.writeInt32(param1);
            _hidl_request.writeInt32(param2);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(128, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setTrm(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(129, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void handleStkCallSetupRequestFromSimWithResCode(int serial, int resultCode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(resultCode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(130, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResponseFunctionsForAtci(IAtciResponse atciResponse, IAtciIndication atciIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(atciResponse == null ? null : atciResponse.asBinder());
            if (atciIndication != null) {
                iHwBinder = atciIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(131, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendAtciRequest(int serial, ArrayList<Byte> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt8Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(132, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void restartRILD(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(133, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void syncDataSettingsToMd(int serial, ArrayList<Integer> settings) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32Vector(settings);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(134, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void resetMdDataRetryCount(int serial, String apn) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(apn);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(135, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setRemoveRestrictEutranMode(int serial, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(136, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setLteAccessStratumReport(int serial, int enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact((int) MtkPduHeaders.STATE_SKIP_RETRYING, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setLteUplinkDataTransfer(int serial, int state, int interfaceId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(state);
            _hidl_request.writeInt32(interfaceId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(138, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setVoiceDomainPreference(int serial, int vdp) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(vdp);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(139, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setWifiEnabled(int serial, String ifName, int isWifiEnabled, int isFlightModeOn) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(ifName);
            _hidl_request.writeInt32(isWifiEnabled);
            _hidl_request.writeInt32(isFlightModeOn);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(140, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setWifiAssociated(int serial, String ifName, int associated, String ssid, String apMac, int mtuSize, String ueMac) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(ifName);
            _hidl_request.writeInt32(associated);
            _hidl_request.writeString(ssid);
            _hidl_request.writeString(apMac);
            _hidl_request.writeInt32(mtuSize);
            _hidl_request.writeString(ueMac);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(141, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setWifiSignalLevel(int serial, int rssi, int snr) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(rssi);
            _hidl_request.writeInt32(snr);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(142, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setWifiIpAddress(int serial, String ifName, String ipv4Addr, String ipv6Addr, int ipv4PrefixLen, int ipv6PrefixLen, String ipv4Gateway, String ipv6Gateway, int dnsCount, String dnsServers) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(ifName);
            _hidl_request.writeString(ipv4Addr);
            _hidl_request.writeString(ipv6Addr);
            _hidl_request.writeInt32(ipv4PrefixLen);
            _hidl_request.writeInt32(ipv6PrefixLen);
            _hidl_request.writeString(ipv4Gateway);
            _hidl_request.writeString(ipv6Gateway);
            _hidl_request.writeInt32(dnsCount);
            _hidl_request.writeString(dnsServers);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(143, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setWfcConfig(int serial, int setting, String ifName, String value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(setting);
            _hidl_request.writeString(ifName);
            _hidl_request.writeString(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(144, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void querySsacStatus(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(145, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setLocationInfo(int serial, String accountId, String broadcastFlag, String latitude, String longitude, String accuracy, String method, String city, String state, String zip, String countryCode, String ueWlanMac) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(accountId);
            _hidl_request.writeString(broadcastFlag);
            _hidl_request.writeString(latitude);
            _hidl_request.writeString(longitude);
            _hidl_request.writeString(accuracy);
            _hidl_request.writeString(method);
            _hidl_request.writeString(city);
            _hidl_request.writeString(state);
            _hidl_request.writeString(zip);
            _hidl_request.writeString(countryCode);
            _hidl_request.writeString(ueWlanMac);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact((int) MtkPduPart.P_DATE, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setEmergencyAddressId(int serial, String aid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(aid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(147, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setNattKeepAliveStatus(int serial, String ifName, boolean enable, String srcIp, int srcPort, String dstIp, int dstPort) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(ifName);
            _hidl_request.writeBool(enable);
            _hidl_request.writeString(srcIp);
            _hidl_request.writeInt32(srcPort);
            _hidl_request.writeString(dstIp);
            _hidl_request.writeInt32(dstPort);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(148, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setWifiPingResult(int serial, int rat, int latency, int pktloss) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(rat);
            _hidl_request.writeInt32(latency);
            _hidl_request.writeInt32(pktloss);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(149, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setApcMode(int serial, int mode, int reportMode, int interval) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            _hidl_request.writeInt32(reportMode);
            _hidl_request.writeInt32(interval);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(150, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getApcInfo(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(151, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setImsBearerNotification(int serial, int enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(152, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setImsCfgFeatureValue(int serial, int featureId, int network, int value, int isLast) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(featureId);
            _hidl_request.writeInt32(network);
            _hidl_request.writeInt32(value);
            _hidl_request.writeInt32(isLast);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(153, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getImsCfgFeatureValue(int serial, int featureId, int network) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(featureId);
            _hidl_request.writeInt32(network);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(154, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setImsCfgProvisionValue(int serial, int configId, String value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(configId);
            _hidl_request.writeString(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(155, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getImsCfgProvisionValue(int serial, int configId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(configId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(156, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getImsCfgResourceCapValue(int serial, int featureId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(featureId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(157, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void dataConnectionAttach(int serial, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(158, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void dataConnectionDetach(int serial, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(159, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void resetAllConnections(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(160, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setLteReleaseVersion(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(161, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getLteReleaseVersion(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(162, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setTxPowerStatus(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(163, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setSuppServProperty(int serial, String name, String value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(name);
            _hidl_request.writeString(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(164, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void supplyDeviceNetworkDepersonalization(int serial, String pwd) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(pwd);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(165, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void notifyEPDGScreenState(int serial, int state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(166, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void hangupWithReason(int serial, int callId, int reason) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callId);
            _hidl_request.writeInt32(reason);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact((int) MtkPduPart.P_TRANSFER_ENCODING, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResponseFunctionsSubsidyLock(ISubsidyLockResponse sublockResp, ISubsidyLockIndication sublockInd) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(sublockResp == null ? null : sublockResp.asBinder());
            if (sublockInd != null) {
                iHwBinder = sublockInd.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(168, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setResponseFunctionsRcs(IRcsRadioResponse radioResponse, IRcsRadioIndication radioIndication) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            IHwBinder iHwBinder = null;
            _hidl_request.writeStrongBinder(radioResponse == null ? null : radioResponse.asBinder());
            if (radioIndication != null) {
                iHwBinder = radioIndication.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(169, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendSubsidyLockRequest(int serial, int reqType, ArrayList<Byte> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(reqType);
            _hidl_request.writeInt8Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(170, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setVendorSetting(int serial, int setting, String value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(setting);
            _hidl_request.writeString(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(171, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setRttMode(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(172, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendRttModifyRequest(int serial, int callId, int newMode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callId);
            _hidl_request.writeInt32(newMode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(173, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void sendRttText(int serial, int callId, int lenOfString, String text) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callId);
            _hidl_request.writeInt32(lenOfString);
            _hidl_request.writeString(text);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(174, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void rttModifyRequestResponse(int serial, int callId, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callId);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(175, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void toggleRttAudioIndication(int serial, int callId, int audio) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(callId);
            _hidl_request.writeInt32(audio);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(176, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void queryVopsStatus(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(177, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void notifyImsServiceReady() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(178, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getPlmnNameFromSE13Table(int serial, int mcc, int mnc) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mcc);
            _hidl_request.writeInt32(mnc);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(179, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void enableCAPlusBandWidthFilter(int serial, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(180, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getVoiceDomainPreference(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(181, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setSipHeader(int serial, ArrayList<String> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeStringVector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(182, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setSipHeaderReport(int serial, ArrayList<String> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeStringVector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(183, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setImsCallMode(int serial, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(184, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setGwsdMode(int serial, ArrayList<String> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeStringVector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(185, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setCallValidTimer(int serial, int timer) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(timer);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(186, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setIgnoreSameNumberInterval(int serial, int interval) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(interval);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(187, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setKeepAliveByPDCPCtrlPDU(int serial, String config) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(config);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(188, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void setKeepAliveByIpData(int serial, String config) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(config);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(189, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void enableDsdaIndication(int serial, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(190, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getDsdaStatus(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(191, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void registerCellQltyReport(int serial, String registerQuality, String type, String thresholdValues, String triggerTime) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(registerQuality);
            _hidl_request.writeString(type);
            _hidl_request.writeString(thresholdValues);
            _hidl_request.writeString(triggerTime);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(192, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void getSuggestedPlmnList(int serial, int rat, int num, int timer) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(rat);
            _hidl_request.writeInt32(num);
            _hidl_request.writeInt32(timer);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(193, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void routeCertificate(int serial, int uid, ArrayList<Byte> cert, ArrayList<Byte> msg) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(uid);
            _hidl_request.writeInt8Vector(cert);
            _hidl_request.writeInt8Vector(msg);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(194, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void routeAuthMessage(int serial, int uid, ArrayList<Byte> msg) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(uid);
            _hidl_request.writeInt8Vector(msg);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(195, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void enableCapabaility(int serial, String id, int uid, int toActive) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeString(id);
            _hidl_request.writeInt32(uid);
            _hidl_request.writeInt32(toActive);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(196, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void abortCertificate(int serial, int uid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(uid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(197, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void eccRedialApprove(int serial, int approve, int callId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(approve);
            _hidl_request.writeInt32(callId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(198, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx
        public void deactivateNrScgCommunication(int serial, boolean deactivate, boolean allowSCGAdd) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeBool(deactivate);
            _hidl_request.writeBool(allowSCGAdd);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(199, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx
        public void getDeactivateNrScgCommunication(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(200, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx
        public void setMaxUlSpeed(int serial, int ulSpeed) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IMtkRadioEx.kInterfaceName);
            _hidl_request.writeInt32(serial);
            _hidl_request.writeInt32(ulSpeed);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(201, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IMtkRadioEx {
        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IMtkRadioEx.kInterfaceName, vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx.kInterfaceName, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName, "android.hidl.base@1.0::IBase"));
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public final String interfaceDescriptor() {
            return IMtkRadioEx.kInterfaceName;
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{105, -3, -17, 22, -64, -89, 34, -55, 13, -96, -52, 23, -121, -38, 23, 26, -87, 1, -91, 50, -90, 65, -48, -29, -74, 17, -6, 41, -37, -45, -32, 123}, new byte[]{-98, 68, 106, 111, -1, -58, -70, -2, -103, 29, 38, -27, 126, 85, 119, -114, 89, -3, 36, 89, -115, -82, -95, 38, 100, -74, 40, 100, -6, -120, 60, -81}, new byte[]{84, -101, 80, -62, -7, -41, 77, -21, 46, 80, -102, -88, -106, -90, 34, 84, 61, -88, 19, 92, 101, 85, -88, -70, -56, 37, -94, -118, -63, 46, -58, 49}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public final void setHALInstrumentation() {
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public final void ping() {
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_2.IMtkRadioEx, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IMtkRadioEx.kInterfaceName.equals(descriptor)) {
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
                case 1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    responseAcknowledgementMtk();
                    return;
                case 2:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResponseFunctionsMtk(IMtkRadioExResponse.asInterface(_hidl_request.readStrongBinder()), IMtkRadioExIndication.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 3:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResponseFunctionsIms(IImsRadioResponse.asInterface(_hidl_request.readStrongBinder()), IImsRadioIndication.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 4:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResponseFunctionsMwi(IMwiRadioResponse.asInterface(_hidl_request.readStrongBinder()), IMwiRadioIndication.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 5:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResponseFunctionsSE(ISERadioResponse.asInterface(_hidl_request.readStrongBinder()), ISERadioIndication.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 6:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResponseFunctionsEm(IEmRadioResponse.asInterface(_hidl_request.readStrongBinder()), IEmRadioIndication.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 7:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResponseFunctionsAssist(IAssistRadioResponse.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 8:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResponseFunctionsCap(ICapRadioResponse.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    videoCallAccept(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    imsEctCommand(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    controlCall(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    imsDeregNotification(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setImsEnable(_hidl_request.readInt32(), _hidl_request.readBool());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setImsVideoEnable(_hidl_request.readInt32(), _hidl_request.readBool());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setImscfg(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readBool(), _hidl_request.readBool(), _hidl_request.readBool(), _hidl_request.readBool(), _hidl_request.readBool());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getProvisionValue(_hidl_request.readInt32(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setProvisionValue(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    controlImsConferenceCallMember(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32());
                    return;
                case WorldMode.MD_WORLD_MODE_LTWCG /* 19 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setWfcProfile(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    int serial = _hidl_request.readInt32();
                    ConferenceDial dailInfo = new ConferenceDial();
                    dailInfo.readFromParcel(_hidl_request);
                    conferenceDial(serial, dailInfo);
                    return;
                case WorldMode.MD_WORLD_MODE_LFCTG /* 21 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setModemImsCfg(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    dialWithSipUri(_hidl_request.readInt32(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    vtDialWithSipUri(_hidl_request.readInt32(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    int serial2 = _hidl_request.readInt32();
                    Dial dialInfo = new Dial();
                    dialInfo.readFromParcel(_hidl_request);
                    vtDial(serial2, dialInfo);
                    return;
                case 25:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    forceReleaseCall(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 26:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    imsBearerStateConfirm(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 27:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setImsRtpReport(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 28:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    pullCall(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readBool());
                    return;
                case 29:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setImsRegistrationReport(_hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_REG_SUSPENDED_1 /* 30 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendEmbmsAtCommand(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case IWorldPhone.EVENT_REG_SUSPENDED_2 /* 31 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setRoamingEnable(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    return;
                case 32:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getRoamingEnable(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 33:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setBarringPasswordCheckedByNW(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 34:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setClip(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 35:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getColp(_hidl_request.readInt32());
                    return;
                case 36:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getColr(_hidl_request.readInt32());
                    return;
                case MtkCharacterSets.ISO_2022_KR /* 37 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendCnap(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case MtkCharacterSets.EUC_KR /* 38 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setColp(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case MtkCharacterSets.ISO_2022_JP /* 39 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setColr(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 40:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    int serial3 = _hidl_request.readInt32();
                    CallForwardInfoEx callInfoEx = new CallForwardInfoEx();
                    callInfoEx.readFromParcel(_hidl_request);
                    queryCallForwardInTimeSlotStatus(serial3, callInfoEx);
                    return;
                case 41:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    int serial4 = _hidl_request.readInt32();
                    CallForwardInfoEx callInfoEx2 = new CallForwardInfoEx();
                    callInfoEx2.readFromParcel(_hidl_request);
                    setCallForwardInTimeSlot(serial4, callInfoEx2);
                    return;
                case 42:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    runGbaAuthentication(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 43:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendUssi(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 44:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    cancelUssi(_hidl_request.readInt32());
                    return;
                case 45:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getXcapStatus(_hidl_request.readInt32());
                    return;
                case MtkCatService.MSG_ID_CACHED_DISPLAY_TEXT_TIMEOUT /* 46 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    resetSuppServ(_hidl_request.readInt32());
                    return;
                case MtkCatService.MSG_ID_CONN_RETRY_TIMEOUT /* 47 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setupXcapUserAgentString(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 48:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    hangupAll(_hidl_request.readInt32());
                    return;
                case PplMessageManager.PendingMessage.PENDING_MESSAGE_LENGTH /* 49 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setCallIndication(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_QUERY_MODEM_TYPE /* 50 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setEccMode(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 51:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    eccPreferredRat(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 52:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setVoicePreferStatus(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case MtkWspTypeDecoder.CONTENT_TYPE_B_CONNECTIVITY /* 53 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setEccNum(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 54:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getEccNum(_hidl_request.readInt32());
                    return;
                case 55:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    queryPhbStorageInfo(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case MtkEtwsUtils.ETWS_PDU_LENGTH /* 56 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    int serial5 = _hidl_request.readInt32();
                    PhbEntryStructure phbEntry = new PhbEntryStructure();
                    phbEntry.readFromParcel(_hidl_request);
                    writePhbEntry(serial5, phbEntry);
                    return;
                case 57:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    readPhbEntry(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 58:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    queryUPBCapability(_hidl_request.readInt32());
                    return;
                case 59:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    editUPBEntry(_hidl_request.readInt32(), _hidl_request.readStringVector());
                    return;
                case 60:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    deleteUPBEntry(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_2 /* 61 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    readUPBGasList(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_3 /* 62 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    readUPBGrpEntry(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_4 /* 63 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    writeUPBGrpEntry(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    return;
                case 64:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getPhoneBookStringsLength(_hidl_request.readInt32());
                    return;
                case 65:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getPhoneBookMemStorage(_hidl_request.readInt32());
                    return;
                case 66:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setPhoneBookMemStorage(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 67:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    readPhoneBookEntryExt(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 68:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    int serial6 = _hidl_request.readInt32();
                    PhbEntryExt phbEntryExt = new PhbEntryExt();
                    phbEntryExt.readFromParcel(_hidl_request);
                    writePhoneBookEntryExt(serial6, phbEntryExt);
                    return;
                case 69:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    queryUPBAvailable(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_RESUME_CAMPING_1 /* 70 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    readUPBEmailEntry(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_RESUME_CAMPING_2 /* 71 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    readUPBSneEntry(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_RESUME_CAMPING_3 /* 72 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    readUPBAnrEntry(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_RESUME_CAMPING_4 /* 73 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    readUPBAasList(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 74:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setPhonebookReady(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 75:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setModemPower(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 76:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    triggerModeSwitchByEcc(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 77:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getATR(_hidl_request.readInt32());
                    return;
                case 78:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getIccid(_hidl_request.readInt32());
                    return;
                case 79:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setSimPower(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_1 /* 80 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    activateUiccCard(_hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_2 /* 81 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    deactivateUiccCard(_hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_3 /* 82 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getCurrentUiccCardProvisioningStatus(_hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_SERVICE_STATE_CHANGED_4 /* 83 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    int serial7 = _hidl_request.readInt32();
                    SimAuthStructure simAuth = new SimAuthStructure();
                    simAuth.readFromParcel(_hidl_request);
                    doGeneralSimAuthentication(serial7, simAuth);
                    return;
                case 84:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    queryNetworkLock(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 85:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setNetworkLock(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 86:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    supplyDepersonalization(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32());
                    return;
                case BipUtils.ADDRESS_TYPE_IPV6 /* 87 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendVsimNotification(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 88:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendVsimOperation(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 89:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getSmsParameters(_hidl_request.readInt32());
                    return;
                case 90:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    int serial8 = _hidl_request.readInt32();
                    SmsParams message = new SmsParams();
                    message.readFromParcel(_hidl_request);
                    setSmsParameters(serial8, message);
                    return;
                case 91:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getSmsMemStatus(_hidl_request.readInt32());
                    return;
                case 92:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setEtws(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 93:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    removeCbMsg(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 94:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setGsmBroadcastLangs(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 95:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getGsmBroadcastLangs(_hidl_request.readInt32());
                    return;
                case 96:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getGsmBroadcastActivation(_hidl_request.readInt32());
                    return;
                case 97:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    int serial9 = _hidl_request.readInt32();
                    ImsSmsMessage message2 = new ImsSmsMessage();
                    message2.readFromParcel(_hidl_request);
                    sendImsSmsEx(serial9, message2);
                    return;
                case 98:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    acknowledgeLastIncomingGsmSmsEx(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 99:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    int serial10 = _hidl_request.readInt32();
                    CdmaSmsAck smsAck = new CdmaSmsAck();
                    smsAck.readFromParcel(_hidl_request);
                    acknowledgeLastIncomingCdmaSmsEx(serial10, smsAck);
                    return;
                case 100:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendRequestRaw(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 101:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendRequestStrings(_hidl_request.readInt32(), _hidl_request.readStringVector());
                    return;
                case 102:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResumeRegistration(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 103:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    modifyModemType(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case MtkCharacterSets.ISO_2022_CN /* 104 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getSmsRuimMemoryStatus(_hidl_request.readInt32());
                    return;
                case MtkCharacterSets.ISO_2022_CN_EXT /* 105 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setNetworkSelectionModeManualWithAct(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 106:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getAvailableNetworksWithAct(_hidl_request.readInt32());
                    return;
                case 107:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getSignalStrengthWithWcdmaEcio(_hidl_request.readInt32());
                    return;
                case 108:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    cancelAvailableNetworks(_hidl_request.readInt32());
                    return;
                case 109:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getFemtocellList(_hidl_request.readInt32());
                    return;
                case 110:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    abortFemtocellList(_hidl_request.readInt32());
                    return;
                case 111:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    selectFemtocell(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case MtkCharacterSets.ISO_8859_16 /* 112 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    queryFemtoCellSystemSelectionMode(_hidl_request.readInt32());
                    return;
                case MtkCharacterSets.GBK /* 113 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setFemtoCellSystemSelectionMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case MtkCharacterSets.GB18030 /* 114 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setServiceStateToModem(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 115:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    cfgA2offset(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 116:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    cfgB1offset(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 117:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    enableSCGfailure(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 118:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    disableNR(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 119:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setTxPower(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 120:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setSearchStoredFreqInfo(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    return;
                case 121:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setSearchRat(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    return;
                case 122:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setBgsrchDeltaSleepTimer(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 123:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setRxTestConfig(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 124:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getRxTestResult(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 125:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getPOLCapability(_hidl_request.readInt32());
                    return;
                case 126:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getCurrentPOLList(_hidl_request.readInt32());
                    return;
                case 127:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setPOLEntry(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32());
                    return;
                case 128:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setFdMode(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 129:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setTrm(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 130:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    handleStkCallSetupRequestFromSimWithResCode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 131:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResponseFunctionsForAtci(IAtciResponse.asInterface(_hidl_request.readStrongBinder()), IAtciIndication.asInterface(_hidl_request.readStrongBinder()));
                    return;
                case 132:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendAtciRequest(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 133:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    restartRILD(_hidl_request.readInt32());
                    return;
                case 134:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    syncDataSettingsToMd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    return;
                case 135:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    resetMdDataRetryCount(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 136:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setRemoveRestrictEutranMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case MtkPduHeaders.STATE_SKIP_RETRYING /* 137 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setLteAccessStratumReport(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 138:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setLteUplinkDataTransfer(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 139:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setVoiceDomainPreference(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 140:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setWifiEnabled(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 141:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setWifiAssociated(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 142:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setWifiSignalLevel(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 143:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setWifiIpAddress(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 144:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setWfcConfig(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 145:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    querySsacStatus(_hidl_request.readInt32());
                    return;
                case MtkPduPart.P_DATE /* 146 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setLocationInfo(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 147:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setEmergencyAddressId(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 148:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setNattKeepAliveStatus(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readBool(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32());
                    return;
                case 149:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setWifiPingResult(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 150:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setApcMode(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 151:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getApcInfo(_hidl_request.readInt32());
                    return;
                case 152:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setImsBearerNotification(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 153:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setImsCfgFeatureValue(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 154:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getImsCfgFeatureValue(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 155:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setImsCfgProvisionValue(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 156:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getImsCfgProvisionValue(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 157:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getImsCfgResourceCapValue(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 158:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    dataConnectionAttach(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 159:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    dataConnectionDetach(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 160:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    resetAllConnections(_hidl_request.readInt32());
                    return;
                case 161:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setLteReleaseVersion(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 162:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getLteReleaseVersion(_hidl_request.readInt32());
                    return;
                case 163:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setTxPowerStatus(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 164:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setSuppServProperty(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 165:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    supplyDeviceNetworkDepersonalization(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 166:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    notifyEPDGScreenState(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case MtkPduPart.P_TRANSFER_ENCODING /* 167 */:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    hangupWithReason(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 168:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResponseFunctionsSubsidyLock(ISubsidyLockResponse.asInterface(_hidl_request.readStrongBinder()), ISubsidyLockIndication.asInterface(_hidl_request.readStrongBinder()));
                    return;
                case 169:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setResponseFunctionsRcs(IRcsRadioResponse.asInterface(_hidl_request.readStrongBinder()), IRcsRadioIndication.asInterface(_hidl_request.readStrongBinder()));
                    return;
                case 170:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendSubsidyLockRequest(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 171:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setVendorSetting(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 172:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setRttMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 173:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendRttModifyRequest(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 174:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    sendRttText(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 175:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    rttModifyRequestResponse(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 176:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    toggleRttAudioIndication(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 177:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    queryVopsStatus(_hidl_request.readInt32());
                    return;
                case 178:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    notifyImsServiceReady();
                    return;
                case 179:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getPlmnNameFromSE13Table(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 180:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    enableCAPlusBandWidthFilter(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 181:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getVoiceDomainPreference(_hidl_request.readInt32());
                    return;
                case 182:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setSipHeader(_hidl_request.readInt32(), _hidl_request.readStringVector());
                    return;
                case 183:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setSipHeaderReport(_hidl_request.readInt32(), _hidl_request.readStringVector());
                    return;
                case 184:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setImsCallMode(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 185:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setGwsdMode(_hidl_request.readInt32(), _hidl_request.readStringVector());
                    return;
                case 186:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setCallValidTimer(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 187:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setIgnoreSameNumberInterval(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 188:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setKeepAliveByPDCPCtrlPDU(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 189:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    setKeepAliveByIpData(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 190:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    enableDsdaIndication(_hidl_request.readInt32(), _hidl_request.readBool());
                    return;
                case 191:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getDsdaStatus(_hidl_request.readInt32());
                    return;
                case 192:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    registerCellQltyReport(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 193:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    getSuggestedPlmnList(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 194:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    routeCertificate(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt8Vector(), _hidl_request.readInt8Vector());
                    return;
                case 195:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    routeAuthMessage(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 196:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    enableCapabaility(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 197:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    abortCertificate(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 198:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioEx.kInterfaceName);
                    eccRedialApprove(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 199:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx.kInterfaceName);
                    deactivateNrScgCommunication(_hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readBool());
                    return;
                case 200:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_1.IMtkRadioEx.kInterfaceName);
                    getDeactivateNrScgCommunication(_hidl_request.readInt32());
                    return;
                case 201:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IMtkRadioEx.kInterfaceName);
                    setMaxUlSpeed(_hidl_request.readInt32(), _hidl_request.readInt32());
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
