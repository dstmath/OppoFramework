package vendor.qti.hardware.radio.qtiradio.V1_0;

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
import com.qualcomm.qcnvitems.IQcNvItems;
import com.qualcomm.qcnvitems.QcNvItemIds;
import com.qualcomm.qcrilhook.EmbmsOemHook;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public interface IQtiRadioResponse extends IBase {
    public static final String kInterfaceName = "vendor.qti.hardware.radio.qtiradio@1.0::IQtiRadioResponse";

    public static abstract class Stub extends HwBinder implements IQtiRadioResponse {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList(Arrays.asList(new String[]{IQtiRadioResponse.kInterfaceName, "android.hidl.base@1.0::IBase"}));
        }

        public final String interfaceDescriptor() {
            return IQtiRadioResponse.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) -77, (byte) 22, (byte) -47, (byte) 29, (byte) -121, (byte) -4, (byte) 2, (byte) 33, (byte) 63, (byte) -45, (byte) -74, (byte) 0, (byte) -2, (byte) 105, (byte) 32, (byte) -27, (byte) 44, (byte) 70, (byte) 110, (byte) -106, (byte) 37, (byte) -20, (byte) 126, (byte) -96, (byte) -109, (byte) -71, (byte) 117, (byte) -38, (byte) -6, (byte) 17, (byte) -110, (byte) 79}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
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
            if (IQtiRadioResponse.kInterfaceName.equals(descriptor)) {
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
            QtiRadioResponseInfo info;
            switch (_hidl_code) {
                case 1:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getAtrResponse(info, _hidl_request.readString());
                    return;
                case 2:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    reserveNullResponse(info);
                    return;
                case 3:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    processFactoryModeNVResponse(info);
                    return;
                case 4:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setFactoryModeGPIOResponse(info, _hidl_request.readInt32());
                    return;
                case 5:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getBandModeResponse(info, _hidl_request.readInt32());
                    return;
                case 6:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    reportNvRestoreResponse(info);
                    return;
                case 7:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getRffeDevInfoResponse(info, _hidl_request.readInt32());
                    return;
                case 8:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setModemErrorFatalResponse(info);
                    return;
                case 9:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getMdmBaseBandResponse(info, _hidl_request.readString());
                    return;
                case 10:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setTddLTEResponse(info);
                    return;
                case 11:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    OPPO_RIL_Radio_info respInfo = new OPPO_RIL_Radio_info();
                    respInfo.readFromParcel(_hidl_request);
                    getRadioInfoResponse(info, respInfo);
                    return;
                case EmbmsOemHook.UNSOL_TYPE_EMBMS_STATUS /*12*/:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setFilterArfcnResponse(info, _hidl_request.readInt32());
                    return;
                case EmbmsOemHook.UNSOL_TYPE_GET_INTERESTED_TMGI_LIST /*13*/:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setPplmnListResponse(info);
                    return;
                case 14:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    OPPO_RIL_Tx_Rx_info result = new OPPO_RIL_Tx_Rx_info();
                    result.readFromParcel(_hidl_request);
                    getTxRxInfoResponse(info, result);
                    return;
                case IQcNvItems.NV_ENCRYPT_IMEI_NUMBER_SIZE /*15*/:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getRegionChangedForEccListResponse(info);
                    return;
                case 16:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setFakesBsWeightResponse(info, _hidl_request.readInt32());
                    return;
                case 17:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setVolteFr2Response(info, _hidl_request.readInt32());
                    return;
                case QcNvItemIds.NV_ANALOG_HOME_SID_I /*18*/:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setVolteFr1Response(info, _hidl_request.readInt32());
                    return;
                case 19:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    lockGsmArfcnResponse(info, _hidl_request.readInt32());
                    return;
                case 20:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getRffeCmdResponse(info, _hidl_request.readInt32());
                    return;
                case QcNvItemIds.NV_SCDMACH_I /*21*/:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    lockLteCellResponse(info, _hidl_request.readInt32());
                    return;
                case 22:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    controlModemFeatureResponse(info, _hidl_request.readInt32());
                    return;
                case 23:
                    _hidl_request.enforceInterface(IQtiRadioResponse.kInterfaceName);
                    info = new QtiRadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getASDIVStateResponse(info, _hidl_request.readString());
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

    public static final class Proxy implements IQtiRadioResponse {
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
                return "[class or subclass of vendor.qti.hardware.radio.qtiradio@1.0::IQtiRadioResponse]@Proxy";
            }
        }

        public void getAtrResponse(QtiRadioResponseInfo info, String atr) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(atr);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void reserveNullResponse(QtiRadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void processFactoryModeNVResponse(QtiRadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setFactoryModeGPIOResponse(QtiRadioResponseInfo info, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getBandModeResponse(QtiRadioResponseInfo info, int band) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(band);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void reportNvRestoreResponse(QtiRadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getRffeDevInfoResponse(QtiRadioResponseInfo info, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setModemErrorFatalResponse(QtiRadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getMdmBaseBandResponse(QtiRadioResponseInfo info, String baseband) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(baseband);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setTddLTEResponse(QtiRadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getRadioInfoResponse(QtiRadioResponseInfo info, OPPO_RIL_Radio_info respInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            respInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setFilterArfcnResponse(QtiRadioResponseInfo info, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setPplmnListResponse(QtiRadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getTxRxInfoResponse(QtiRadioResponseInfo info, OPPO_RIL_Tx_Rx_info result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            result.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getRegionChangedForEccListResponse(QtiRadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setFakesBsWeightResponse(QtiRadioResponseInfo info, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setVolteFr2Response(QtiRadioResponseInfo info, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setVolteFr1Response(QtiRadioResponseInfo info, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void lockGsmArfcnResponse(QtiRadioResponseInfo info, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getRffeCmdResponse(QtiRadioResponseInfo info, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void lockLteCellResponse(QtiRadioResponseInfo info, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void controlModemFeatureResponse(QtiRadioResponseInfo info, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getASDIVStateResponse(QtiRadioResponseInfo info, String result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IQtiRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
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

    IHwBinder asBinder();

    void controlModemFeatureResponse(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    void getASDIVStateResponse(QtiRadioResponseInfo qtiRadioResponseInfo, String str) throws RemoteException;

    void getAtrResponse(QtiRadioResponseInfo qtiRadioResponseInfo, String str) throws RemoteException;

    void getBandModeResponse(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getMdmBaseBandResponse(QtiRadioResponseInfo qtiRadioResponseInfo, String str) throws RemoteException;

    void getRadioInfoResponse(QtiRadioResponseInfo qtiRadioResponseInfo, OPPO_RIL_Radio_info oPPO_RIL_Radio_info) throws RemoteException;

    void getRegionChangedForEccListResponse(QtiRadioResponseInfo qtiRadioResponseInfo) throws RemoteException;

    void getRffeCmdResponse(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    void getRffeDevInfoResponse(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    void getTxRxInfoResponse(QtiRadioResponseInfo qtiRadioResponseInfo, OPPO_RIL_Tx_Rx_info oPPO_RIL_Tx_Rx_info) throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    void lockGsmArfcnResponse(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    void lockLteCellResponse(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    void processFactoryModeNVResponse(QtiRadioResponseInfo qtiRadioResponseInfo) throws RemoteException;

    void reportNvRestoreResponse(QtiRadioResponseInfo qtiRadioResponseInfo) throws RemoteException;

    void reserveNullResponse(QtiRadioResponseInfo qtiRadioResponseInfo) throws RemoteException;

    void setFactoryModeGPIOResponse(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    void setFakesBsWeightResponse(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    void setFilterArfcnResponse(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    void setModemErrorFatalResponse(QtiRadioResponseInfo qtiRadioResponseInfo) throws RemoteException;

    void setPplmnListResponse(QtiRadioResponseInfo qtiRadioResponseInfo) throws RemoteException;

    void setTddLTEResponse(QtiRadioResponseInfo qtiRadioResponseInfo) throws RemoteException;

    void setVolteFr1Response(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    void setVolteFr2Response(QtiRadioResponseInfo qtiRadioResponseInfo, int i) throws RemoteException;

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

    static IQtiRadioResponse asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IQtiRadioResponse)) {
            return (IQtiRadioResponse) iface;
        }
        IQtiRadioResponse proxy = new Proxy(binder);
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

    static IQtiRadioResponse castFrom(IHwInterface iface) {
        return iface == null ? null : asInterface(iface.asBinder());
    }

    static IQtiRadioResponse getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IQtiRadioResponse getService() throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, "default"));
    }
}
