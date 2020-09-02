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
import vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface;

public interface ISupplicantStaIface extends ISupplicantIface {
    public static final String kInterfaceName = "vendor.mediatek.hardware.wifi.supplicant@2.0::ISupplicantStaIface";

    @FunctionalInterface
    public interface getFeatureMaskCallback {
        void onValues(SupplicantStatus supplicantStatus, int i);
    }

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    void getFeatureMask(getFeatureMaskCallback getfeaturemaskcallback) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    SupplicantStatus registerCallback(ISupplicantStaIfaceCallback iSupplicantStaIfaceCallback) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    SupplicantStatus setWapiCertAliasList(String str) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    static default ISupplicantStaIface asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof ISupplicantStaIface)) {
            return (ISupplicantStaIface) iface;
        }
        ISupplicantStaIface proxy = new Proxy(binder);
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

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    static default ISupplicantStaIface castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    static default ISupplicantStaIface getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface.getService(java.lang.String, boolean):vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface
     arg types: [java.lang.String, boolean]
     candidates:
      vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface.getService(java.lang.String, boolean):vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface
      android.hidl.base.V1_0.IBase.getService(java.lang.String, boolean):android.hidl.base.V1_0.IBase
      vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface.getService(java.lang.String, boolean):vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface */
    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    static default ISupplicantStaIface getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    static default ISupplicantStaIface getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
    static default ISupplicantStaIface getService() throws RemoteException {
        return getService("default");
    }

    public static final class FeatureMask {
        public static final int WAPI = 1;

        public static final String toString(int o) {
            if (o == 1) {
                return "WAPI";
            }
            return "0x" + Integer.toHexString(o);
        }

        public static final String dumpBitfield(int o) {
            ArrayList<String> list = new ArrayList<>();
            int flipped = 0;
            if ((o & 1) == 1) {
                list.add("WAPI");
                flipped = 0 | 1;
            }
            if (o != flipped) {
                list.add("0x" + Integer.toHexString((~flipped) & o));
            }
            return String.join(" | ", list);
        }
    }

    public static final class Proxy implements ISupplicantStaIface {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.mediatek.hardware.wifi.supplicant@2.0::ISupplicantStaIface]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface
        public void getNetwork(int id, ISupplicantIface.getNetworkCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantIface.kInterfaceName);
            _hidl_request.writeInt32(id);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, ISupplicantNetwork.asInterface(_hidl_reply.readStrongBinder()));
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface
        public void getName(ISupplicantIface.getNameCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantIface.kInterfaceName);
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface
        public void getType(ISupplicantIface.getTypeCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantIface.kInterfaceName);
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface
        public SupplicantStatus registerCallback(ISupplicantStaIfaceCallback callback) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIface.kInterfaceName);
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface
        public SupplicantStatus setWapiCertAliasList(String aliasList) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIface.kInterfaceName);
            _hidl_request.writeString(aliasList);
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface
        public void getFeatureMask(getFeatureMaskCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISupplicantStaIface.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                SupplicantStatus _hidl_out_status = new SupplicantStatus();
                _hidl_out_status.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_status, _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
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

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements ISupplicantStaIface {
        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(ISupplicantStaIface.kInterfaceName, ISupplicantIface.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return ISupplicantStaIface.kInterfaceName;
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-76, 36, 12, -28, -90, 1, 77, -109, -55, 87, 23, -19, 20, -93, -43, 122, -122, -91, 28, -46, -111, 7, 92, -106, 25, -74, 41, 60, -3, 42, -25, 61}, new byte[]{32, -6, 35, -28, 83, -121, -70, -39, 95, 71, 12, 69, 111, 104, -23, 98, 65, 116, 4, 126, 12, 18, 113, -47, -38, -112, 55, -91, -110, 87, -8, -44}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface, vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (ISupplicantStaIface.kInterfaceName.equals(descriptor)) {
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
                    _hidl_request.enforceInterface(ISupplicantIface.kInterfaceName);
                    getNetwork(_hidl_request.readInt32(), new ISupplicantIface.getNetworkCallback() {
                        /* class vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface.Stub.AnonymousClass1 */

                        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface.getNetworkCallback
                        public void onValues(SupplicantStatus status, ISupplicantNetwork network) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeStrongBinder(network == null ? null : network.asBinder());
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
                    _hidl_request.enforceInterface(ISupplicantIface.kInterfaceName);
                    getName(new ISupplicantIface.getNameCallback() {
                        /* class vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface.Stub.AnonymousClass2 */

                        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface.getNameCallback
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
                    _hidl_request.enforceInterface(ISupplicantIface.kInterfaceName);
                    getType(new ISupplicantIface.getTypeCallback() {
                        /* class vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface.Stub.AnonymousClass3 */

                        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantIface.getTypeCallback
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
                    _hidl_request.enforceInterface(ISupplicantStaIface.kInterfaceName);
                    SupplicantStatus _hidl_out_status = registerCallback(ISupplicantStaIfaceCallback.asInterface(_hidl_request.readStrongBinder()));
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
                    _hidl_request.enforceInterface(ISupplicantStaIface.kInterfaceName);
                    SupplicantStatus _hidl_out_status2 = setWapiCertAliasList(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_out_status2.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 6:
                    if (_hidl_flags != false && true) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISupplicantStaIface.kInterfaceName);
                    getFeatureMask(new getFeatureMaskCallback() {
                        /* class vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface.Stub.AnonymousClass4 */

                        @Override // vendor.mediatek.hardware.wifi.supplicant.V2_0.ISupplicantStaIface.getFeatureMaskCallback
                        public void onValues(SupplicantStatus status, int feature_mask) {
                            _hidl_reply.writeStatus(0);
                            status.writeToParcel(_hidl_reply);
                            _hidl_reply.writeInt32(feature_mask);
                            _hidl_reply.send();
                        }
                    });
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
