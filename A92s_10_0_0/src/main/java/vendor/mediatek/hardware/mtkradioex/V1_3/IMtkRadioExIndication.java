package vendor.mediatek.hardware.mtkradioex.V1_3;

import android.hardware.radio.V1_0.SuppSvcNotification;
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
import com.mediatek.internal.telephony.MtkEtwsUtils;
import com.mediatek.internal.telephony.MtkWspTypeDecoder;
import com.mediatek.internal.telephony.cat.MtkCatService;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import com.mediatek.internal.telephony.worldphone.WorldMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import vendor.mediatek.hardware.mtkradioex.V1_0.CfuStatusNotification;
import vendor.mediatek.hardware.mtkradioex.V1_0.CipherNotification;
import vendor.mediatek.hardware.mtkradioex.V1_0.CrssNotification;
import vendor.mediatek.hardware.mtkradioex.V1_0.DedicateDataCall;
import vendor.mediatek.hardware.mtkradioex.V1_0.EtwsNotification;
import vendor.mediatek.hardware.mtkradioex.V1_0.IncomingCallNotification;
import vendor.mediatek.hardware.mtkradioex.V1_0.PcoDataAttachedInfo;
import vendor.mediatek.hardware.mtkradioex.V1_0.SignalStrengthWithWcdmaEcio;
import vendor.mediatek.hardware.mtkradioex.V1_0.VsimOperationEvent;

public interface IMtkRadioExIndication extends vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication {
    public static final String kInterfaceName = "vendor.mediatek.hardware.mtkradioex@1.3::IMtkRadioExIndication";

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    IHwBinder asBinder();

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    void notifySyspropsChanged() throws RemoteException;

    void onNwLimitInd(int i, ArrayList<Integer> arrayList) throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    void ping() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    void setHALInstrumentation() throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    static default IMtkRadioExIndication asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IMtkRadioExIndication)) {
            return (IMtkRadioExIndication) iface;
        }
        IMtkRadioExIndication proxy = new Proxy(binder);
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

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    static default IMtkRadioExIndication castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    static default IMtkRadioExIndication getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication.getService(java.lang.String, boolean):vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
     arg types: [java.lang.String, boolean]
     candidates:
      vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.getService(java.lang.String, boolean):vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
      vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication.getService(java.lang.String, boolean):vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication */
    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    static default IMtkRadioExIndication getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    static default IMtkRadioExIndication getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
    static default IMtkRadioExIndication getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements IMtkRadioExIndication {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.mediatek.hardware.mtkradioex@1.3::IMtkRadioExIndication]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void eMBMSAtInfoIndication(int type, String info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(info);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void eMBMSSessionStatusIndication(int type, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void phbReadyNotification(int type, int isPhbReady) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(isPhbReady);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void cfuStatusNotify(int type, CfuStatusNotification cfuStatus) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            cfuStatus.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void incomingCallIndication(int type, IncomingCallNotification inCallNotify) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            inCallNotify.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void callAdditionalInfoInd(int type, int ciType, ArrayList<String> info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(ciType);
            _hidl_request.writeStringVector(info);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void cipherIndication(int type, CipherNotification cipherNotify) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            cipherNotify.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void suppSvcNotifyEx(int type, SuppSvcNotification suppSvc) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            suppSvc.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void crssIndication(int type, CrssNotification crssNotify) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            crssNotify.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void cdmaCallAccepted(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void eccNumIndication(int type, String ecc_list_with_card, String ecc_list_no_card) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(ecc_list_with_card);
            _hidl_request.writeString(ecc_list_no_card);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onVirtualSimStatusChanged(int type, int simInserted) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(simInserted);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onImeiLock(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onImsiRefreshDone(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onSimHotSwapInd(int type, int event, String info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(event);
            _hidl_request.writeString(info);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onSimMeLockEvent(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onRsuSimLockEvent(int type, int eventId) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(eventId);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onCardDetectedInd(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onVsimEventIndication(int type, VsimOperationEvent event) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            event.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void newEtwsInd(int type, EtwsNotification etws) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            etws.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void meSmsStorageFullInd(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void smsReadyInd(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void oemHookRaw(int type, ArrayList<Byte> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt8Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void plmnChangedIndication(int type, ArrayList<String> plmns) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeStringVector(plmns);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void registrationSuspendedIndication(int type, ArrayList<Integer> sessionIds) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(sessionIds);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void gmssRatChangedIndication(int type, ArrayList<Integer> gmsss) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(gmsss);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void worldModeChangedIndication(int type, ArrayList<Integer> modes) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(modes);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void esnMeidChangeInd(int type, String esnMeid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(esnMeid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void responseCsNetworkStateChangeInd(int type, ArrayList<String> state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeStringVector(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void responsePsNetworkStateChangeInd(int type, ArrayList<Integer> state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void responseInvalidSimInd(int type, ArrayList<String> state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeStringVector(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void responseNetworkEventInd(int type, ArrayList<Integer> event) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(event);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void responseModulationInfoInd(int type, ArrayList<Integer> modulation) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(modulation);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void dataAllowedNotification(int type, int allowed) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(allowed);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void responseFemtocellInfo(int type, ArrayList<String> info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeStringVector(info);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void networkInfoInd(int type, ArrayList<String> networkinfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeStringVector(networkinfo);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(36, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void currentSignalStrengthWithWcdmaEcioInd(int type, SignalStrengthWithWcdmaEcio signalStrength) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            signalStrength.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(37, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void responseLteNetworkInfo(int type, int lteBand) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(lteBand);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(38, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void dedicatedBearerActivationInd(int type, DedicateDataCall ddcData) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            ddcData.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(39, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void dedicatedBearerModificationInd(int type, DedicateDataCall ddcData) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            ddcData.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(40, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void dedicatedBearerDeactivationInd(int type, int cid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(cid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(41, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void bipProactiveCommand(int type, String cmd) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(cmd);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(42, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void triggerOtaSP(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(43, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onStkMenuReset(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(44, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void resetAttachApnInd(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(45, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void mdChangedApnInd(int type, int apnClassType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(apnClassType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(46, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void pcoDataAfterAttached(int type, PcoDataAttachedInfo pcoData) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            pcoData.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(47, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void confSRVCC(int type, ArrayList<Integer> callIds) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(callIds);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(48, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onMdDataRetryCountReset(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(49, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onRemoveRestrictEutran(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(50, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onLteAccessStratumStateChanged(int type, ArrayList<Integer> state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(51, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onTxPowerIndication(int type, ArrayList<Integer> indPower) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(indPower);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(52, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onPseudoCellInfoInd(int type, ArrayList<Integer> cellInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(cellInfo);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(53, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onMccMncChanged(int type, String mccmnc) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(mccmnc);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(54, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onTxPowerStatusIndication(int type, ArrayList<Integer> indPower) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(indPower);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(55, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void networkRejectCauseInd(int type, ArrayList<Integer> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(56, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void dsbpStateChanged(int indicationType, int dsbpState) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(indicationType);
            _hidl_request.writeInt32(dsbpState);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(57, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void smlSlotLockInfoChangedInd(int type, ArrayList<Integer> info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(info);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(58, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onSimPowerChangedInd(int type, ArrayList<Integer> info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(info);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(59, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void networkBandInfoInd(int type, ArrayList<Integer> state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(60, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void smsInfoExtInd(int type, String info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeString(info);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(61, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onDsdaChangedInd(int type, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(62, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void qualifiedNetworkTypesChangedInd(int type, ArrayList<Integer> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(63, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void onCellularQualityChangedInd(int type, ArrayList<Integer> indStgs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(indStgs);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(64, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void mobileDataUsageInd(int type, ArrayList<Integer> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(65, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void smlDeviceLockInfoChangedInd(int type, ArrayList<Integer> info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(info);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(66, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication
        public void smlEncryptedSerialIdInd(int type, ArrayList<String> indStgs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeStringVector(indStgs);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(67, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public void onNwLimitInd(int type, ArrayList<Integer> state) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IMtkRadioExIndication.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt32Vector(state);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(68, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
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

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IMtkRadioExIndication {
        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IMtkRadioExIndication.kInterfaceName, vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName, "android.hidl.base@1.0::IBase"));
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public final String interfaceDescriptor() {
            return IMtkRadioExIndication.kInterfaceName;
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{60, -124, 6, 35, -32, -75, -116, -3, -125, -23, 110, -62, 68, 18, 63, 46, -27, 25, -32, -118, -82, -97, 108, -39, -73, 110, 93, -3, -80, 126, -28, -116}, new byte[]{-80, -26, -22, -12, -41, 48, -121, -37, -45, -44, 36, 100, -100, -57, -37, -35, -127, -3, 93, 73, 34, 52, -127, -65, 51, 9, -111, 40, -124, 6, 120, 126}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public final void setHALInstrumentation() {
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public final void ping() {
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication, vendor.mediatek.hardware.mtkradioex.V1_3.IMtkRadioExIndication
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IMtkRadioExIndication.kInterfaceName.equals(descriptor)) {
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    eMBMSAtInfoIndication(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case 2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    eMBMSSessionStatusIndication(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    phbReadyNotification(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type = _hidl_request.readInt32();
                    CfuStatusNotification cfuStatus = new CfuStatusNotification();
                    cfuStatus.readFromParcel(_hidl_request);
                    cfuStatusNotify(type, cfuStatus);
                    return;
                case 5:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type2 = _hidl_request.readInt32();
                    IncomingCallNotification inCallNotify = new IncomingCallNotification();
                    inCallNotify.readFromParcel(_hidl_request);
                    incomingCallIndication(type2, inCallNotify);
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    callAdditionalInfoInd(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readStringVector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type3 = _hidl_request.readInt32();
                    CipherNotification cipherNotify = new CipherNotification();
                    cipherNotify.readFromParcel(_hidl_request);
                    cipherIndication(type3, cipherNotify);
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type4 = _hidl_request.readInt32();
                    SuppSvcNotification suppSvc = new SuppSvcNotification();
                    suppSvc.readFromParcel(_hidl_request);
                    suppSvcNotifyEx(type4, suppSvc);
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type5 = _hidl_request.readInt32();
                    CrssNotification crssNotify = new CrssNotification();
                    crssNotify.readFromParcel(_hidl_request);
                    crssIndication(type5, crssNotify);
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    cdmaCallAccepted(_hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    eccNumIndication(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onVirtualSimStatusChanged(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onImeiLock(_hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onImsiRefreshDone(_hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onSimHotSwapInd(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onSimMeLockEvent(_hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onRsuSimLockEvent(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onCardDetectedInd(_hidl_request.readInt32());
                    return;
                case WorldMode.MD_WORLD_MODE_LTWCG:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type6 = _hidl_request.readInt32();
                    VsimOperationEvent event = new VsimOperationEvent();
                    event.readFromParcel(_hidl_request);
                    onVsimEventIndication(type6, event);
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type7 = _hidl_request.readInt32();
                    EtwsNotification etws = new EtwsNotification();
                    etws.readFromParcel(_hidl_request);
                    newEtwsInd(type7, etws);
                    return;
                case WorldMode.MD_WORLD_MODE_LFCTG:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    meSmsStorageFullInd(_hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    smsReadyInd(_hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    oemHookRaw(_hidl_request.readInt32(), _hidl_request.readInt8Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    plmnChangedIndication(_hidl_request.readInt32(), _hidl_request.readStringVector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    registrationSuspendedIndication(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    gmssRatChangedIndication(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    worldModeChangedIndication(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    esnMeidChangeInd(_hidl_request.readInt32(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    responseCsNetworkStateChangeInd(_hidl_request.readInt32(), _hidl_request.readStringVector());
                    return;
                case IWorldPhone.EVENT_REG_SUSPENDED_1:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    responsePsNetworkStateChangeInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    return;
                case IWorldPhone.EVENT_REG_SUSPENDED_2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    responseInvalidSimInd(_hidl_request.readInt32(), _hidl_request.readStringVector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    responseNetworkEventInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    responseModulationInfoInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    dataAllowedNotification(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    responseFemtocellInfo(_hidl_request.readInt32(), _hidl_request.readStringVector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    networkInfoInd(_hidl_request.readInt32(), _hidl_request.readStringVector());
                    return;
                case MtkCharacterSets.ISO_2022_KR:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type8 = _hidl_request.readInt32();
                    SignalStrengthWithWcdmaEcio signalStrength = new SignalStrengthWithWcdmaEcio();
                    signalStrength.readFromParcel(_hidl_request);
                    currentSignalStrengthWithWcdmaEcioInd(type8, signalStrength);
                    return;
                case MtkCharacterSets.EUC_KR:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    responseLteNetworkInfo(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case MtkCharacterSets.ISO_2022_JP:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type9 = _hidl_request.readInt32();
                    DedicateDataCall ddcData = new DedicateDataCall();
                    ddcData.readFromParcel(_hidl_request);
                    dedicatedBearerActivationInd(type9, ddcData);
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type10 = _hidl_request.readInt32();
                    DedicateDataCall ddcData2 = new DedicateDataCall();
                    ddcData2.readFromParcel(_hidl_request);
                    dedicatedBearerModificationInd(type10, ddcData2);
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    dedicatedBearerDeactivationInd(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    bipProactiveCommand(_hidl_request.readInt32(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    triggerOtaSP(_hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onStkMenuReset(_hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    resetAttachApnInd(_hidl_request.readInt32());
                    return;
                case MtkCatService.MSG_ID_CACHED_DISPLAY_TEXT_TIMEOUT:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    mdChangedApnInd(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case MtkCatService.MSG_ID_CONN_RETRY_TIMEOUT:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    int type11 = _hidl_request.readInt32();
                    PcoDataAttachedInfo pcoData = new PcoDataAttachedInfo();
                    pcoData.readFromParcel(_hidl_request);
                    pcoDataAfterAttached(type11, pcoData);
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    confSRVCC(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    return;
                case PplMessageManager.PendingMessage.PENDING_MESSAGE_LENGTH:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onMdDataRetryCountReset(_hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_QUERY_MODEM_TYPE:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onRemoveRestrictEutran(_hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onLteAccessStratumStateChanged(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onTxPowerIndication(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    return;
                case MtkWspTypeDecoder.CONTENT_TYPE_B_CONNECTIVITY:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onPseudoCellInfoInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onMccMncChanged(_hidl_request.readInt32(), _hidl_request.readString());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onTxPowerStatusIndication(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    return;
                case MtkEtwsUtils.ETWS_PDU_LENGTH:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    networkRejectCauseInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    dsbpStateChanged(_hidl_request.readInt32(), _hidl_request.readInt32());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    smlSlotLockInfoChangedInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onSimPowerChangedInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    networkBandInfoInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
                    return;
                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_2:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    smsInfoExtInd(_hidl_request.readInt32(), _hidl_request.readString());
                    return;
                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onDsdaChangedInd(_hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case IWorldPhone.EVENT_INVALID_SIM_NOTIFY_4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (!_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    qualifiedNetworkTypesChangedInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    onCellularQualityChangedInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    mobileDataUsageInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    smlDeviceLockInfoChangedInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
                    _hidl_request.enforceInterface(vendor.mediatek.hardware.mtkradioex.V1_0.IMtkRadioExIndication.kInterfaceName);
                    smlEncryptedSerialIdInd(_hidl_request.readInt32(), _hidl_request.readStringVector());
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
                    _hidl_request.enforceInterface(IMtkRadioExIndication.kInterfaceName);
                    onNwLimitInd(_hidl_request.readInt32(), _hidl_request.readInt32Vector());
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
