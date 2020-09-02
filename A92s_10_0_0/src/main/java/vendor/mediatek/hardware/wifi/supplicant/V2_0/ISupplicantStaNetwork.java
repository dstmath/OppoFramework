package vendor.mediatek.hardware.wifi.supplicant.V2_0;

import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
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
import vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork;

public interface ISupplicantStaNetwork extends ISupplicantNetwork {
    public static final String kInterfaceName = "vendor.mediatek.hardware.wifi.supplicant@2.0::ISupplicantStaNetwork";

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    SupplicantStatus registerCallback(ISupplicantStaNetworkCallback iSupplicantStaNetworkCallback) throws RemoteException;

    SupplicantStatus setEapOcsp(int i) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    SupplicantStatus setWapiCertAlias(String str) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    static default ISupplicantStaNetwork asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof ISupplicantStaNetwork)) {
            return (ISupplicantStaNetwork) iface;
        }
        ISupplicantStaNetwork proxy = new Proxy(binder);
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

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    static default ISupplicantStaNetwork castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    static default ISupplicantStaNetwork getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork.getService(java.lang.String, boolean):vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork
     arg types: [java.lang.String, boolean]
     candidates:
      vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork.getService(java.lang.String, boolean):vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork
      android.hidl.base.V1_0.IBase.getService(java.lang.String, boolean):android.hidl.base.V1_0.IBase
      vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork.getService(java.lang.String, boolean):vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork */
    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    static default ISupplicantStaNetwork getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    static default ISupplicantStaNetwork getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, android.hidl.base.V1_0.IBase
    static default ISupplicantStaNetwork getService() throws RemoteException {
        return getService("default");
    }

    public static final class KeyMgmtMask {
        public static final int WAPI_CERT = 8192;
        public static final int WAPI_PSK = 4096;
        public static final int WPA_EAP_SHA256 = 128;

        public static final String toString(int o) {
            if (o == 128) {
                return "WPA_EAP_SHA256";
            }
            if (o == 4096) {
                return "WAPI_PSK";
            }
            if (o == 8192) {
                return "WAPI_CERT";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 128) == 128) {
                list.add("WPA_EAP_SHA256");
                flipped = 0 | 128;
            }
            if ((o & 4096) == 4096) {
                list.add("WAPI_PSK");
                flipped |= 4096;
            }
            if ((o & 8192) == 8192) {
                list.add("WAPI_CERT");
                flipped |= 8192;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class ProtoMask {
        public static final int WAPI = 4;

        public static final String toString(int o) {
            if (o == 4) {
                return "WAPI";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 4) == 4) {
                list.add("WAPI");
                flipped = 0 | 4;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class GroupCipherMask {
        public static final int SM4 = 128;

        public static final String toString(int o) {
            if (o == 128) {
                return "SM4";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 128) == 128) {
                list.add("SM4");
                flipped = 0 | 128;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class PairwiseCipherMask {
        public static final int SM4 = 128;

        public static final String toString(int o) {
            if (o == 128) {
                return "SM4";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 128) == 128) {
                list.add("SM4");
                flipped = 0 | 128;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class Ocsp {
        public static final int MANDATORY_OCSP = 2;
        public static final int NO_OCSP = 0;
        public static final int OPTIONAL_OCSP = 1;

        public static final String toString(int o) {
            if (o == 0) {
                return "NO_OCSP";
            }
            if (o == 1) {
                return "OPTIONAL_OCSP";
            }
            if (o == 2) {
                return "MANDATORY_OCSP";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            list.add("NO_OCSP");
            if ((o & 1) == 1) {
                list.add("OPTIONAL_OCSP");
                flipped = 0 | 1;
            }
            if ((o & 2) == 2) {
                list.add("MANDATORY_OCSP");
                flipped |= 2;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class Proxy implements ISupplicantStaNetwork {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.mediatek.hardware.wifi.supplicant@2.0::ISupplicantStaNetwork]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork
        public void getId(ISupplicantNetwork.getIdCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork
        public void getInterfaceName(ISupplicantNetwork.getInterfaceNameCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork
        public void getType(ISupplicantNetwork.getTypeCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantNetwork.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork
        public SupplicantStatus registerCallback(ISupplicantStaNetworkCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeStrongBinder(callback == null ? null : callback.asBinder());
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork
        public SupplicantStatus setWapiCertAlias(String alias) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeString(alias);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork
        public SupplicantStatus setEapOcsp(int ocsp) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaNetwork.kInterfaceName);
            _hidl_request.writeInt32(ocsp);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> options) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements ISupplicantStaNetwork {
        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(ISupplicantStaNetwork.kInterfaceName, ISupplicantNetwork.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return ISupplicantStaNetwork.kInterfaceName;
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{11, -35, -120, -95, -70, 57, -108, -126, -57, 71, 68, -78, -110, -2, -62, -35, -114, 96, -55, -69, -65, -15, 101, Byte.MAX_VALUE, -53, 65, -22, Byte.MIN_VALUE, -87, 32, 63, 109}, new byte[]{-62, 67, 11, -90, -19, -102, 81, 62, -120, 64, 40, -49, -24, -54, 59, 75, 93, 9, -45, 101, 113, 23, -121, 44, 1, -54, 48, -76, -90, 120, 9, 75}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (ISupplicantStaNetwork.kInterfaceName.equals(descriptor)) {
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

        public void onTransact(int _hidl_code, HwParcel _hidl_request, final HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            boolean _hidl_is_oneway = false;
            boolean _hidl_is_oneway2 = true;
            switch (_hidl_code) {
                case 1:
                    if (_hidl_flags != false && true) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantNetwork.kInterfaceName);
                    getId(new ISupplicantNetwork.getIdCallback() {
                        /* class vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork.Stub.AnonymousClass1 */

                        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork.getIdCallback
                        public void onValues(SupplicantStatus status, int id) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(id);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 2:
                    if (_hidl_flags != false && true) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantNetwork.kInterfaceName);
                    getInterfaceName(new ISupplicantNetwork.getInterfaceNameCallback() {
                        /* class vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork.Stub.AnonymousClass2 */

                        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork.getInterfaceNameCallback
                        public void onValues(SupplicantStatus status, String name) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeString(name);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 3:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantNetwork.kInterfaceName);
                    getType(new ISupplicantNetwork.getTypeCallback() {
                        /* class vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaNetwork.Stub.AnonymousClass3 */

                        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantNetwork.getTypeCallback
                        public void onValues(SupplicantStatus status, int type) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(type);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 4:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status = registerCallback(ISupplicantStaNetworkCallback.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 5:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status2 = setWapiCertAlias(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status2.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 6:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaNetwork.kInterfaceName);
                    SupplicantStatus _hidl_out_status3 = setEapOcsp(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status3.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                default:
                    switch (_hidl_code) {
                        case 256067662:
                            if (_hidl_flags == false || !true) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ArrayList<String> _hidl_out_descriptors = interfaceChain();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeStringVector(_hidl_out_descriptors);
                            _hidl_reply.send();
                            return;
                        case 256131655:
                            if (_hidl_flags == false || !true) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            debug(_hidl_request.readNativeHandle(), _hidl_request.readStringVector());
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 256136003:
                            if (_hidl_flags == false || !true) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            String _hidl_out_descriptor = interfaceDescriptor();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeString(_hidl_out_descriptor);
                            _hidl_reply.send();
                            return;
                        case 256398152:
                            if (_hidl_flags == false || !true) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
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
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
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
                            if (_hidl_flags == false || !true) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
                            ping();
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.send();
                            return;
                        case 257049926:
                            if (_hidl_flags == false || !true) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
                                _hidl_reply.writeStatus(Integer.MIN_VALUE);
                                _hidl_reply.send();
                                return;
                            }
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
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
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
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
