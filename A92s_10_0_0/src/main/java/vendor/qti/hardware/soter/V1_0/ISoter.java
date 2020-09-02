package vendor.qti.hardware.soter.V1_0;

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

public interface ISoter extends IBase {
    public static final String kInterfaceName = "vendor.qti.hardware.soter@1.0::ISoter";

    @FunctionalInterface
    public interface exportAskPublicKeyCallback {
        void onValues(int i, ArrayList<Byte> arrayList, int i2);
    }

    @FunctionalInterface
    public interface exportAttkPublicKeyCallback {
        void onValues(int i, ArrayList<Byte> arrayList, int i2);
    }

    @FunctionalInterface
    public interface exportAuthKeyPublicKeyCallback {
        void onValues(int i, ArrayList<Byte> arrayList, int i2);
    }

    @FunctionalInterface
    public interface finishSignCallback {
        void onValues(int i, ArrayList<Byte> arrayList, int i2);
    }

    @FunctionalInterface
    public interface getDeviceIdCallback {
        void onValues(int i, ArrayList<Byte> arrayList, int i2);
    }

    @FunctionalInterface
    public interface initSignCallback {
        void onValues(int i, long j);
    }

    @Override // android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    void exportAskPublicKey(int i, exportAskPublicKeyCallback exportaskpublickeycallback) throws RemoteException;

    void exportAttkPublicKey(exportAttkPublicKeyCallback exportattkpublickeycallback) throws RemoteException;

    void exportAuthKeyPublicKey(int i, String str, exportAuthKeyPublicKeyCallback exportauthkeypublickeycallback) throws RemoteException;

    void finishSign(long j, finishSignCallback finishsigncallback) throws RemoteException;

    int generateAskKeyPair(int i) throws RemoteException;

    int generateAttkKeyPair(byte b) throws RemoteException;

    int generateAuthKeyPair(int i, String str) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    void getDeviceId(getDeviceIdCallback getdeviceidcallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    int hasAskAlready(int i) throws RemoteException;

    int hasAuthKey(int i, String str) throws RemoteException;

    void initSign(int i, String str, String str2, initSignCallback initsigncallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    int removeAllUidKey(int i) throws RemoteException;

    int removeAuthKey(int i, String str) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    int verifyAttkKeyPair() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    static default ISoter asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof ISoter)) {
            return (ISoter) iface;
        }
        ISoter proxy = new Proxy(binder);
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

    @Override // android.hidl.base.V1_0.IBase
    static default ISoter castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    @Override // android.hidl.base.V1_0.IBase
    static default ISoter getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: vendor.qti.hardware.soter.V1_0.ISoter.getService(java.lang.String, boolean):vendor.qti.hardware.soter.V1_0.ISoter
     arg types: [java.lang.String, boolean]
     candidates:
      android.hidl.base.V1_0.IBase.getService(java.lang.String, boolean):android.hidl.base.V1_0.IBase
      vendor.qti.hardware.soter.V1_0.ISoter.getService(java.lang.String, boolean):vendor.qti.hardware.soter.V1_0.ISoter */
    @Override // android.hidl.base.V1_0.IBase
    static default ISoter getService(boolean retry) throws RemoteException {
        return getService("default", retry);
    }

    @Override // android.hidl.base.V1_0.IBase
    static default ISoter getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    @Override // android.hidl.base.V1_0.IBase
    static default ISoter getService() throws RemoteException {
        return getService("default");
    }

    public static final class Proxy implements ISoter {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.qti.hardware.soter@1.0::ISoter]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public int generateAttkKeyPair(byte copyNum) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt8(copyNum);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public int verifyAttkKeyPair() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public void exportAttkPublicKey(exportAttkPublicKeyCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readInt8Vector(), _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public void getDeviceId(getDeviceIdCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readInt8Vector(), _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public int generateAskKeyPair(int uid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt32(uid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public void exportAskPublicKey(int uid, exportAskPublicKeyCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt32(uid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readInt8Vector(), _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public int removeAllUidKey(int uid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt32(uid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public int hasAskAlready(int uid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt32(uid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public int generateAuthKeyPair(int uid, String name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt32(uid);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public void exportAuthKeyPublicKey(int uid, String name, exportAuthKeyPublicKeyCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt32(uid);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readInt8Vector(), _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public int removeAuthKey(int uid, String name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt32(uid);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public int hasAuthKey(int uid, String name) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt32(uid);
            _hidl_request.writeString(name);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public void initSign(int uid, String name, String challenge, initSignCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt32(uid);
            _hidl_request.writeString(name);
            _hidl_request.writeString(challenge);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readInt64());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter
        public void finishSign(long session, finishSignCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(ISoter.kInterfaceName);
            _hidl_request.writeInt64(session);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readInt8Vector(), _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
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

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
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

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
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

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
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

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
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

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
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

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
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

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
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

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements ISoter {
        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(ISoter.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return ISoter.kInterfaceName;
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-59, -20, -95, 10, 71, -81, -94, 126, -104, -12, 52, -101, 8, 55, -54, 117, -70, 32, 41, 92, 4, 102, -48, -69, -98, -12, -11, -50, -72, -17, -43, 100}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, 35, -17, 5, 36, -13, -51, 105, 87, 19, -109, 36, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.qti.hardware.soter.V1_0.ISoter, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (ISoter.kInterfaceName.equals(descriptor)) {
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
                case DebugInfo.Architecture.IS_64BIT:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    int _hidl_out_error = generateAttkKeyPair(_hidl_request.readInt8());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_error);
                    _hidl_reply.send();
                    return;
                case DebugInfo.Architecture.IS_32BIT:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    int _hidl_out_error2 = verifyAttkKeyPair();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_error2);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    exportAttkPublicKey(new exportAttkPublicKeyCallback() {
                        /* class vendor.qti.hardware.soter.V1_0.ISoter.Stub.AnonymousClass1 */

                        @Override // vendor.qti.hardware.soter.V1_0.ISoter.exportAttkPublicKeyCallback
                        public void onValues(int error, ArrayList<Byte> pubKeyData, int pubKeyDataLength) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(error);
                            _hidl_reply.writeInt8Vector(pubKeyData);
                            _hidl_reply.writeInt32(pubKeyDataLength);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 4:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    getDeviceId(new getDeviceIdCallback() {
                        /* class vendor.qti.hardware.soter.V1_0.ISoter.Stub.AnonymousClass2 */

                        @Override // vendor.qti.hardware.soter.V1_0.ISoter.getDeviceIdCallback
                        public void onValues(int error, ArrayList<Byte> deviceId, int deviceIdLength) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(error);
                            _hidl_reply.writeInt8Vector(deviceId);
                            _hidl_reply.writeInt32(deviceIdLength);
                            _hidl_reply.send();
                        }
                    });
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
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    int _hidl_out_error3 = generateAskKeyPair(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_error3);
                    _hidl_reply.send();
                    return;
                case 6:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    exportAskPublicKey(_hidl_request.readInt32(), new exportAskPublicKeyCallback() {
                        /* class vendor.qti.hardware.soter.V1_0.ISoter.Stub.AnonymousClass3 */

                        @Override // vendor.qti.hardware.soter.V1_0.ISoter.exportAskPublicKeyCallback
                        public void onValues(int error, ArrayList<Byte> data, int dataLength) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(error);
                            _hidl_reply.writeInt8Vector(data);
                            _hidl_reply.writeInt32(dataLength);
                            _hidl_reply.send();
                        }
                    });
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
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    int _hidl_out_error4 = removeAllUidKey(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_error4);
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
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    int _hidl_out_error5 = hasAskAlready(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_error5);
                    _hidl_reply.send();
                    return;
                case 9:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    int _hidl_out_error6 = generateAuthKeyPair(_hidl_request.readInt32(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_error6);
                    _hidl_reply.send();
                    return;
                case 10:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    exportAuthKeyPublicKey(_hidl_request.readInt32(), _hidl_request.readString(), new exportAuthKeyPublicKeyCallback() {
                        /* class vendor.qti.hardware.soter.V1_0.ISoter.Stub.AnonymousClass4 */

                        @Override // vendor.qti.hardware.soter.V1_0.ISoter.exportAuthKeyPublicKeyCallback
                        public void onValues(int error, ArrayList<Byte> data, int dataLength) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(error);
                            _hidl_reply.writeInt8Vector(data);
                            _hidl_reply.writeInt32(dataLength);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 11:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    int _hidl_out_error7 = removeAuthKey(_hidl_request.readInt32(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_error7);
                    _hidl_reply.send();
                    return;
                case 12:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    int _hidl_out_error8 = hasAuthKey(_hidl_request.readInt32(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_error8);
                    _hidl_reply.send();
                    return;
                case 13:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    initSign(_hidl_request.readInt32(), _hidl_request.readString(), _hidl_request.readString(), new initSignCallback() {
                        /* class vendor.qti.hardware.soter.V1_0.ISoter.Stub.AnonymousClass5 */

                        @Override // vendor.qti.hardware.soter.V1_0.ISoter.initSignCallback
                        public void onValues(int error, long session) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(error);
                            _hidl_reply.writeInt64(session);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 14:
                    if ((_hidl_flags & 1) != 0) {
                        _hidl_is_oneway = true;
                    }
                    if (_hidl_is_oneway) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(ISoter.kInterfaceName);
                    finishSign(_hidl_request.readInt64(), new finishSignCallback() {
                        /* class vendor.qti.hardware.soter.V1_0.ISoter.Stub.AnonymousClass6 */

                        @Override // vendor.qti.hardware.soter.V1_0.ISoter.finishSignCallback
                        public void onValues(int error, ArrayList<Byte> data, int dataLength) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(error);
                            _hidl_reply.writeInt8Vector(data);
                            _hidl_reply.writeInt32(dataLength);
                            _hidl_reply.send();
                        }
                    });
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
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
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
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
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
                            _hidl_request.enforceInterface(IBase.kInterfaceName);
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
                            if ((_hidl_flags & 1) == 0) {
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
                            if ((_hidl_flags & 1) == 0) {
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
