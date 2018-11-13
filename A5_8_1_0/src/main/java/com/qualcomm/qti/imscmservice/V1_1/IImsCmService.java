package com.qualcomm.qti.imscmservice.V1_1;

import android.hidl.base.V1_0.DebugInfo;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.os.SystemProperties;
import com.qualcomm.qti.imscmservice.V1_0.IImsCMConnection;
import com.qualcomm.qti.imscmservice.V1_0.IImsCMConnectionListener;
import com.qualcomm.qti.imscmservice.V1_0.IImsCmServiceListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public interface IImsCmService extends com.qualcomm.qti.imscmservice.V1_0.IImsCmService {
    public static final String kInterfaceName = "com.qualcomm.qti.imscmservice@1.1::IImsCmService";

    public static final class Proxy implements IImsCmService {
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
                return "[class or subclass of com.qualcomm.qti.imscmservice@1.1::IImsCmService]@Proxy";
            }
        }

        public int InitializeService(String iccId, IImsCmServiceListener cmListener, int userData) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
            _hidl_request.writeString(iccId);
            if (cmListener != null) {
                iHwBinder = cmListener.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            _hidl_request.writeInt32(userData);
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

        public int addListener(long connectionManager, IImsCmServiceListener cmListener) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
            _hidl_request.writeInt64(connectionManager);
            if (cmListener != null) {
                iHwBinder = cmListener.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
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

        public int removeListener(long connectionManager, IImsCmServiceListener cmListener) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
            _hidl_request.writeInt64(connectionManager);
            if (cmListener != null) {
                iHwBinder = cmListener.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public IImsCMConnection createConnection(long connectionManager, IImsCMConnectionListener cmConnListener, String uriStr) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
            _hidl_request.writeInt64(connectionManager);
            if (cmConnListener != null) {
                iHwBinder = cmConnListener.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            _hidl_request.writeString(uriStr);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                IImsCMConnection _hidl_out_connection = IImsCMConnection.asInterface(_hidl_reply.readStrongBinder());
                return _hidl_out_connection;
            } finally {
                _hidl_reply.release();
            }
        }

        public int closeConnection(long connectionManager, IImsCMConnection connection) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
            _hidl_request.writeInt64(connectionManager);
            if (connection != null) {
                iHwBinder = connection.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int getConfiguration(long connectionManager, int configType, int userdata) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
            _hidl_request.writeInt64(connectionManager);
            _hidl_request.writeInt32(configType);
            _hidl_request.writeInt32(userdata);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int triggerRegistration(long connectionManager, int userdata) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
            _hidl_request.writeInt64(connectionManager);
            _hidl_request.writeInt32(userdata);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int triggerDeRegistration(long connectionManager, int userdata) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
            _hidl_request.writeInt64(connectionManager);
            _hidl_request.writeInt32(userdata);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int closeService(long connectionManager) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
            _hidl_request.writeInt64(connectionManager);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int methodResponse(long connectionManager, String method, short responseCode, int userdata) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
            _hidl_request.writeInt64(connectionManager);
            _hidl_request.writeString(method);
            _hidl_request.writeInt16(responseCode);
            _hidl_request.writeInt32(userdata);
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

        public int InitializeService_1_1(String iccId, IImsCmServiceListener cmListener, int userData) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IImsCmService.kInterfaceName);
            _hidl_request.writeString(iccId);
            if (cmListener != null) {
                iHwBinder = cmListener.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            _hidl_request.writeInt32(userData);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int addListener_1_1(long connectionManager, IImsCmServiceListener cmListener) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IImsCmService.kInterfaceName);
            _hidl_request.writeInt64(connectionManager);
            if (cmListener != null) {
                iHwBinder = cmListener.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int removeListener_1_1(long connectionManager, IImsCmServiceListener cmListener) throws RemoteException {
            IHwBinder iHwBinder = null;
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IImsCmService.kInterfaceName);
            _hidl_request.writeInt64(connectionManager);
            if (cmListener != null) {
                iHwBinder = cmListener.asBinder();
            }
            _hidl_request.writeStrongBinder(iHwBinder);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                int _hidl_out_status = _hidl_reply.readInt32();
                return _hidl_out_status;
            } finally {
                _hidl_reply.release();
            }
        }

        public int triggerACSRequest(long connectionManager, int autoConfigReasonType, int userdata) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IImsCmService.kInterfaceName);
            _hidl_request.writeInt64(connectionManager);
            _hidl_request.writeInt32(autoConfigReasonType);
            _hidl_request.writeInt32(userdata);
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

    public static abstract class Stub extends HwBinder implements IImsCmService {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList(Arrays.asList(new String[]{IImsCmService.kInterfaceName, "com.qualcomm.qti.imscmservice@1.0::IImsCmService", "android.hidl.base@1.0::IBase"}));
        }

        public final String interfaceDescriptor() {
            return IImsCmService.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) 15, (byte) 7, (byte) 38, (byte) -78, (byte) -119, (byte) 94, (byte) -56, (byte) 26, (byte) -50, (byte) -33, (byte) -79, (byte) -85, (byte) -124, (byte) 116, (byte) -111, (byte) -79, (byte) -40, (byte) 63, (byte) -68, (byte) 5, (byte) -28, (byte) -14, (byte) -114, (byte) -127, (byte) 29, (byte) 27, (byte) 45, (byte) 107, (byte) -14, (byte) -98, (byte) 59, (byte) 65}, new byte[]{(byte) 120, (byte) -43, (byte) 100, (byte) -76, (byte) 28, (byte) -38, (byte) 42, (byte) -81, (byte) 27, (byte) 117, (byte) 73, (byte) 13, (byte) -41, (byte) 31, (byte) -15, (byte) -55, (byte) 81, (byte) -102, (byte) -59, (byte) -59, (byte) 53, (byte) -49, (byte) -62, (byte) 41, (byte) 74, (byte) -59, (byte) 39, (byte) -15, (byte) 66, (byte) 122, (byte) 44, (byte) -93}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
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
            if (IImsCmService.kInterfaceName.equals(descriptor)) {
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
            switch (_hidl_code) {
                case 1:
                    _hidl_request.enforceInterface("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
                    _hidl_out_status = InitializeService(_hidl_request.readString(), IImsCmServiceListener.asInterface(_hidl_request.readStrongBinder()), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case IMSCM_AUTOCONFIG_TRIGGER_REASON.IMSCM_AUTOCONFIG_INVALID_TOKEN /*2*/:
                    _hidl_request.enforceInterface("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
                    _hidl_out_status = addListener(_hidl_request.readInt64(), IImsCmServiceListener.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case IMSCM_AUTOCONFIG_TRIGGER_REASON.IMSCM_AUTOCONFIG_INVALID_CREDENTIAL /*3*/:
                    _hidl_request.enforceInterface("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
                    _hidl_out_status = removeListener(_hidl_request.readInt64(), IImsCmServiceListener.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case IMSCM_AUTOCONFIG_TRIGGER_REASON.IMSCM_AUTOCONFIG_CLIENT_CHANGE /*4*/:
                    _hidl_request.enforceInterface("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
                    IImsCMConnection _hidl_out_connection = createConnection(_hidl_request.readInt64(), IImsCMConnectionListener.asInterface(_hidl_request.readStrongBinder()), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStrongBinder(_hidl_out_connection == null ? null : _hidl_out_connection.asBinder());
                    _hidl_reply.send();
                    return;
                case IMSCM_AUTOCONFIG_TRIGGER_REASON.IMSCM_AUTOCONFIG_DEVICE_UPGRADE /*5*/:
                    _hidl_request.enforceInterface("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
                    _hidl_out_status = closeConnection(_hidl_request.readInt64(), IImsCMConnection.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case IMSCM_AUTOCONFIG_TRIGGER_REASON.IMSCM_AUTOCONFIG_FACTORY_RESET /*6*/:
                    _hidl_request.enforceInterface("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
                    _hidl_out_status = getConfiguration(_hidl_request.readInt64(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 7:
                    _hidl_request.enforceInterface("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
                    _hidl_out_status = triggerRegistration(_hidl_request.readInt64(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 8:
                    _hidl_request.enforceInterface("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
                    _hidl_out_status = triggerDeRegistration(_hidl_request.readInt64(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 9:
                    _hidl_request.enforceInterface("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
                    _hidl_out_status = closeService(_hidl_request.readInt64());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 10:
                    _hidl_request.enforceInterface("com.qualcomm.qti.imscmservice@1.0::IImsCmService");
                    _hidl_out_status = methodResponse(_hidl_request.readInt64(), _hidl_request.readString(), _hidl_request.readInt16(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 11:
                    _hidl_request.enforceInterface(IImsCmService.kInterfaceName);
                    _hidl_out_status = InitializeService_1_1(_hidl_request.readString(), IImsCmServiceListener.asInterface(_hidl_request.readStrongBinder()), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 12:
                    _hidl_request.enforceInterface(IImsCmService.kInterfaceName);
                    _hidl_out_status = addListener_1_1(_hidl_request.readInt64(), IImsCmServiceListener.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 13:
                    _hidl_request.enforceInterface(IImsCmService.kInterfaceName);
                    _hidl_out_status = removeListener_1_1(_hidl_request.readInt64(), IImsCmServiceListener.asInterface(_hidl_request.readStrongBinder()));
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_status);
                    _hidl_reply.send();
                    return;
                case 14:
                    _hidl_request.enforceInterface(IImsCmService.kInterfaceName);
                    _hidl_out_status = triggerACSRequest(_hidl_request.readInt64(), _hidl_request.readInt32(), _hidl_request.readInt32());
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
                    DebugInfo _hidl_out_info = getDebugInfo();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_info.writeToParcel(_hidl_reply);
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

    int InitializeService_1_1(String str, IImsCmServiceListener iImsCmServiceListener, int i) throws RemoteException;

    int addListener_1_1(long j, IImsCmServiceListener iImsCmServiceListener) throws RemoteException;

    IHwBinder asBinder();

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    int removeListener_1_1(long j, IImsCmServiceListener iImsCmServiceListener) throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    int triggerACSRequest(long j, int i, int i2) throws RemoteException;

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

    static IImsCmService asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IImsCmService)) {
            return (IImsCmService) iface;
        }
        IImsCmService proxy = new Proxy(binder);
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

    static IImsCmService castFrom(IHwInterface iface) {
        return iface == null ? null : asInterface(iface.asBinder());
    }

    static IImsCmService getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IImsCmService getService() throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, "default"));
    }
}
