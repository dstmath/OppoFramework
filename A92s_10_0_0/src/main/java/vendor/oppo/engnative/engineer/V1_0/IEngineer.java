package vendor.oppo.engnative.engineer.V1_0;

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
import com.android.server.BatteryService;
import com.android.server.OppoServiceBootPhase;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public interface IEngineer extends IBase {
    public static final String kInterfaceName = "vendor.oppo.engnative.engineer@1.0::IEngineer";

    @FunctionalInterface
    public interface exportAttkKeyPairCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface getDeviceIdCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface getImpedanceCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface getVibratorCalibrateResultCallback {
        void onValues(int i, String str);
    }

    @FunctionalInterface
    public interface readDataCallback {
        void onValues(int i, ArrayList<Byte> arrayList);
    }

    @Override // android.hidl.base.V1_0.IBase
    IHwBinder asBinder();

    @Override // android.hidl.base.V1_0.IBase
    void debug(NativeHandle nativeHandle, ArrayList<String> arrayList) throws RemoteException;

    void exportAttkKeyPair(exportAttkKeyPairCallback exportattkkeypaircallback) throws RemoteException;

    int generateAttkKeyPair(int i) throws RemoteException;

    ArrayList<Byte> getBadBatteryConfig(int i, int i2) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    DebugInfo getDebugInfo() throws RemoteException;

    void getDeviceId(getDeviceIdCallback getdeviceidcallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getImpedance(getImpedanceCallback getimpedancecallback) throws RemoteException;

    boolean getPartionWriteProtectState() throws RemoteException;

    ArrayList<Byte> getProductLineTestResult() throws RemoteException;

    void getVibratorCalibrateResult(String str, getVibratorCalibrateResultCallback getvibratorcalibrateresultcallback) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    ArrayList<String> interfaceChain() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    String interfaceDescriptor() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j) throws RemoteException;

    ArrayList<Byte> loadSecrecyConfig() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void notifySyspropsChanged() throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void ping() throws RemoteException;

    void readData(String str, int i, int i2, readDataCallback readdatacallback) throws RemoteException;

    ArrayList<Byte> readEngineerData(int i) throws RemoteException;

    ArrayList<Byte> readOppoUsageRecords(String str) throws RemoteException;

    boolean resetProductLineTestResult() throws RemoteException;

    int saveCameraPersistParams(String str, ArrayList<Byte> arrayList, int i, int i2, boolean z) throws RemoteException;

    boolean saveDisplayCaliData(String str, ArrayList<Byte> arrayList) throws RemoteException;

    boolean saveEngineerData(int i, ArrayList<Byte> arrayList, int i2) throws RemoteException;

    boolean saveOppoUsageRecords(String str, String str2, boolean z) throws RemoteException;

    boolean saveSecrecyConfig(String str) throws RemoteException;

    int setBatteryBatteryConfig(int i, int i2, ArrayList<Byte> arrayList) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    void setHALInstrumentation() throws RemoteException;

    boolean setPartionWriteProtectState(boolean z) throws RemoteException;

    boolean setProductLineTestResult(int i, int i2) throws RemoteException;

    boolean setProperties(String str, String str2) throws RemoteException;

    int setVibratorCalibrateData(String str, int i) throws RemoteException;

    int startAgeVibrate(int i, int i2) throws RemoteException;

    boolean startAmbient(String str) throws RemoteException;

    int startVibrate(int i, int i2, int i3) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient) throws RemoteException;

    int verifyAttkKeyPair() throws RemoteException;

    int writeData(String str, int i, boolean z, int i2, ArrayList<Byte> arrayList) throws RemoteException;

    @Override // android.hidl.base.V1_0.IBase
    static default IEngineer asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IEngineer)) {
            return (IEngineer) iface;
        }
        IEngineer proxy = new Proxy(binder);
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
    static default IEngineer castFrom(IHwInterface iface) {
        if (iface == null) {
            return null;
        }
        return asInterface(iface.asBinder());
    }

    @Override // android.hidl.base.V1_0.IBase
    static default IEngineer getService(String serviceName, boolean retry) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName, retry));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: vendor.oppo.engnative.engineer.V1_0.IEngineer.getService(java.lang.String, boolean):vendor.oppo.engnative.engineer.V1_0.IEngineer
     arg types: [java.lang.String, boolean]
     candidates:
      android.hidl.base.V1_0.IBase.getService(java.lang.String, boolean):android.hidl.base.V1_0.IBase
      vendor.oppo.engnative.engineer.V1_0.IEngineer.getService(java.lang.String, boolean):vendor.oppo.engnative.engineer.V1_0.IEngineer */
    @Override // android.hidl.base.V1_0.IBase
    static default IEngineer getService(boolean retry) throws RemoteException {
        return getService(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR, retry);
    }

    @Override // android.hidl.base.V1_0.IBase
    static default IEngineer getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    @Override // android.hidl.base.V1_0.IBase
    static default IEngineer getService() throws RemoteException {
        return getService(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR);
    }

    public static final class Proxy implements IEngineer {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.oppo.engnative.engineer@1.0::IEngineer]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public boolean saveEngineerData(int type, ArrayList<Byte> engineerData, int length) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeInt32(type);
            _hidl_request.writeInt8Vector(engineerData);
            _hidl_request.writeInt32(length);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public ArrayList<Byte> readEngineerData(int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt8Vector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public boolean getPartionWriteProtectState() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public boolean setPartionWriteProtectState(boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public ArrayList<Byte> getBadBatteryConfig(int offset, int size) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeInt32(offset);
            _hidl_request.writeInt32(size);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt8Vector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public int setBatteryBatteryConfig(int offset, int size, ArrayList<Byte> config) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeInt32(offset);
            _hidl_request.writeInt32(size);
            _hidl_request.writeInt8Vector(config);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public ArrayList<Byte> getProductLineTestResult() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt8Vector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public boolean setProductLineTestResult(int position, int result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeInt32(position);
            _hidl_request.writeInt32(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public boolean resetProductLineTestResult() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public int saveCameraPersistParams(String path, ArrayList<Byte> params, int offset, int size, boolean override) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(path);
            _hidl_request.writeInt8Vector(params);
            _hidl_request.writeInt32(offset);
            _hidl_request.writeInt32(size);
            _hidl_request.writeBool(override);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public ArrayList<Byte> loadSecrecyConfig() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt8Vector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public boolean saveSecrecyConfig(String config) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(config);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public boolean saveOppoUsageRecords(String path, String usage, boolean isSingleRecord) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(path);
            _hidl_request.writeString(usage);
            _hidl_request.writeBool(isSingleRecord);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public ArrayList<Byte> readOppoUsageRecords(String path) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(path);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt8Vector();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public void readData(String path, int offset, int length, readDataCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(path);
            _hidl_request.writeInt32(offset);
            _hidl_request.writeInt32(length);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public int writeData(String path, int offset, boolean fromEnd, int length, ArrayList<Byte> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(path);
            _hidl_request.writeInt32(offset);
            _hidl_request.writeBool(fromEnd);
            _hidl_request.writeInt32(length);
            _hidl_request.writeInt8Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public int generateAttkKeyPair(int copy_num) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeInt32(copy_num);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public int verifyAttkKeyPair() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public void exportAttkKeyPair(exportAttkKeyPairCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public void getDeviceId(getDeviceIdCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public int setVibratorCalibrateData(String type, int value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(type);
            _hidl_request.writeInt32(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public void getVibratorCalibrateResult(String type, getVibratorCalibrateResultCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public void getImpedance(getImpedanceCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt32(), _hidl_reply.readString());
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public int startVibrate(int duration, int index, int amplitude) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeInt32(duration);
            _hidl_request.writeInt32(index);
            _hidl_request.writeInt32(amplitude);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public int startAgeVibrate(int duration, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeInt32(duration);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readInt32();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public boolean startAmbient(String flashvalues) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(flashvalues);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public boolean setProperties(String key, String value) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(key);
            _hidl_request.writeString(value);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer
        public boolean saveDisplayCaliData(String path, ArrayList<Byte> data) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IEngineer.kInterfaceName);
            _hidl_request.writeString(path);
            _hidl_request.writeInt8Vector(data);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                return _hidl_reply.readBool();
            } finally {
                _hidl_reply.release();
            }
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
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

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
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

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
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

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
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

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
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

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
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

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
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

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
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

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    public static abstract class Stub extends HwBinder implements IEngineer {
        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public IHwBinder asBinder() {
            return this;
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public final ArrayList<String> interfaceChain() {
            return new ArrayList<>(Arrays.asList(IEngineer.kInterfaceName, IBase.kInterfaceName));
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public void debug(NativeHandle fd, ArrayList<String> arrayList) {
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public final String interfaceDescriptor() {
            return IEngineer.kInterfaceName;
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList<>(Arrays.asList(new byte[]{-54, 84, 65, 116, -19, 8, -87, 110, -87, 121, 74, 121, 21, -98, 75, 122, 125, 103, 58, -39, -65, 61, -36, 49, -82, 86, -87, -69, 104, -4, -74, 83}, new byte[]{-20, Byte.MAX_VALUE, -41, -98, -48, 45, -6, -123, -68, 73, -108, 38, -83, -82, 62, -66, UsbDescriptor.DESCRIPTORTYPE_PHYSICAL, -17, 5, UsbDescriptor.DESCRIPTORTYPE_AUDIO_INTERFACE, -13, -51, 105, 87, 19, -109, UsbDescriptor.DESCRIPTORTYPE_AUDIO_INTERFACE, -72, 59, 24, -54, 76}));
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public final void setHALInstrumentation() {
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public final boolean linkToDeath(IHwBinder.DeathRecipient recipient, long cookie) {
            return true;
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public final void ping() {
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = HidlSupport.getPidIfSharable();
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public final void notifySyspropsChanged() {
            HwBinder.enableInstrumentation();
        }

        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer, android.hidl.base.V1_0.IBase
        public final boolean unlinkToDeath(IHwBinder.DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IEngineer.kInterfaceName.equals(descriptor)) {
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
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    boolean _hidl_out_data = saveEngineerData(_hidl_request.readInt32(), _hidl_request.readInt8Vector(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_data);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    ArrayList<Byte> _hidl_out_data2 = readEngineerData(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8Vector(_hidl_out_data2);
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
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    boolean _hidl_out_data3 = getPartionWriteProtectState();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_data3);
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
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    boolean _hidl_out_data4 = setPartionWriteProtectState(_hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_data4);
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
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    ArrayList<Byte> _hidl_out_data5 = getBadBatteryConfig(_hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8Vector(_hidl_out_data5);
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
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    int _hidl_out_data6 = setBatteryBatteryConfig(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_data6);
                    _hidl_reply.send();
                    return;
                case 7:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    ArrayList<Byte> _hidl_out_data7 = getProductLineTestResult();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8Vector(_hidl_out_data7);
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
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    boolean _hidl_out_data8 = setProductLineTestResult(_hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_data8);
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
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    boolean _hidl_out_data9 = resetProductLineTestResult();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_data9);
                    _hidl_reply.send();
                    return;
                case 10:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    int _hidl_out_data10 = saveCameraPersistParams(_hidl_request.readString(), _hidl_request.readInt8Vector(), _hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_data10);
                    _hidl_reply.send();
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
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    ArrayList<Byte> _hidl_out_data11 = loadSecrecyConfig();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8Vector(_hidl_out_data11);
                    _hidl_reply.send();
                    return;
                case 12:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    boolean _hidl_out_data12 = saveSecrecyConfig(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_data12);
                    _hidl_reply.send();
                    return;
                case 13:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    boolean _hidl_out_data13 = saveOppoUsageRecords(_hidl_request.readString(), _hidl_request.readString(), _hidl_request.readBool());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_data13);
                    _hidl_reply.send();
                    return;
                case 14:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    ArrayList<Byte> _hidl_out_data14 = readOppoUsageRecords(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt8Vector(_hidl_out_data14);
                    _hidl_reply.send();
                    return;
                case 15:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    readData(_hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readInt32(), new readDataCallback() {
                        /* class vendor.oppo.engnative.engineer.V1_0.IEngineer.Stub.AnonymousClass1 */

                        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer.readDataCallback
                        public void onValues(int ret, ArrayList<Byte> data) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            _hidl_reply.writeInt8Vector(data);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 16:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    int _hidl_out_ret = writeData(_hidl_request.readString(), _hidl_request.readInt32(), _hidl_request.readBool(), _hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_ret);
                    _hidl_reply.send();
                    return;
                case 17:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    int _hidl_out_ret2 = generateAttkKeyPair(_hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_ret2);
                    _hidl_reply.send();
                    return;
                case 18:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    int _hidl_out_ret3 = verifyAttkKeyPair();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_ret3);
                    _hidl_reply.send();
                    return;
                case 19:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    exportAttkKeyPair(new exportAttkKeyPairCallback() {
                        /* class vendor.oppo.engnative.engineer.V1_0.IEngineer.Stub.AnonymousClass2 */

                        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer.exportAttkKeyPairCallback
                        public void onValues(int ret, String key_blob) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            _hidl_reply.writeString(key_blob);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 20:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    getDeviceId(new getDeviceIdCallback() {
                        /* class vendor.oppo.engnative.engineer.V1_0.IEngineer.Stub.AnonymousClass3 */

                        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer.getDeviceIdCallback
                        public void onValues(int ret, String device_id) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            _hidl_reply.writeString(device_id);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 21:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    int _hidl_out_ret4 = setVibratorCalibrateData(_hidl_request.readString(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_ret4);
                    _hidl_reply.send();
                    return;
                case 22:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    getVibratorCalibrateResult(_hidl_request.readString(), new getVibratorCalibrateResultCallback() {
                        /* class vendor.oppo.engnative.engineer.V1_0.IEngineer.Stub.AnonymousClass4 */

                        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer.getVibratorCalibrateResultCallback
                        public void onValues(int ret, String result) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            _hidl_reply.writeString(result);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 23:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    getImpedance(new getImpedanceCallback() {
                        /* class vendor.oppo.engnative.engineer.V1_0.IEngineer.Stub.AnonymousClass5 */

                        @Override // vendor.oppo.engnative.engineer.V1_0.IEngineer.getImpedanceCallback
                        public void onValues(int ret, String result) {
                            _hidl_reply.writeStatus(0);
                            _hidl_reply.writeInt32(ret);
                            _hidl_reply.writeString(result);
                            _hidl_reply.send();
                        }
                    });
                    return;
                case 24:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    int _hidl_out_ret5 = startVibrate(_hidl_request.readInt32(), _hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_ret5);
                    _hidl_reply.send();
                    return;
                case 25:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    int _hidl_out_ret6 = startAgeVibrate(_hidl_request.readInt32(), _hidl_request.readInt32());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeInt32(_hidl_out_ret6);
                    _hidl_reply.send();
                    return;
                case OppoServiceBootPhase.PHASE_ALARM_READY:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    boolean _hidl_out_data15 = startAmbient(_hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_data15);
                    _hidl_reply.send();
                    return;
                case 27:
                    if (_hidl_flags == false || !true) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    boolean _hidl_out_result = setProperties(_hidl_request.readString(), _hidl_request.readString());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_result);
                    _hidl_reply.send();
                    return;
                case 28:
                    if ((_hidl_flags & 1) == 0) {
                        _hidl_is_oneway2 = false;
                    }
                    if (_hidl_is_oneway2) {
                        _hidl_reply.writeStatus(Integer.MIN_VALUE);
                        _hidl_reply.send();
                        return;
                    }
                    _hidl_request.enforceInterface(IEngineer.kInterfaceName);
                    boolean _hidl_out_result2 = saveDisplayCaliData(_hidl_request.readString(), _hidl_request.readInt8Vector());
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeBool(_hidl_out_result2);
                    _hidl_reply.send();
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
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
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
                            if ((_hidl_flags & 1) == 0) {
                                _hidl_is_oneway2 = false;
                            }
                            if (_hidl_is_oneway2) {
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
