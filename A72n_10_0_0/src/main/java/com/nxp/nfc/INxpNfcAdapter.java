package com.nxp.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.nxp.nfc.INxpNfcAdapterExtras;
import java.util.List;
import java.util.Map;

public interface INxpNfcAdapter extends IInterface {
    void DefaultRouteSet(int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    void MifareCLTRouteSet(int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    void MifareDesfireRouteSet(int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    int activateSeInterface() throws RemoteException;

    void changeDiscoveryTech(IBinder iBinder, int i, int i2) throws RemoteException;

    int changeRfParams(byte[] bArr, boolean z) throws RemoteException;

    int changeRfParamsByConfig(byte[] bArr) throws RemoteException;

    int deactivateSeInterface() throws RemoteException;

    byte[] doReadT4tData(byte[] bArr) throws RemoteException;

    int doWriteT4tData(byte[] bArr, byte[] bArr2, int i) throws RemoteException;

    int[] getActiveSecureElementList(String str) throws RemoteException;

    int getCommittedAidRoutingTableSize() throws RemoteException;

    byte[] getFWVersion() throws RemoteException;

    int getMaxAidRoutingTableSize() throws RemoteException;

    INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface() throws RemoteException;

    int getSelectedUicc() throws RemoteException;

    List<NfcAidServiceInfo> getServicesAidInfo(int i, String str) throws RemoteException;

    boolean isFieldDetectEnabled() throws RemoteException;

    boolean mPOSGetReaderMode(String str) throws RemoteException;

    int mPOSSetReaderMode(String str, boolean z) throws RemoteException;

    int nfcSelfTest(int i) throws RemoteException;

    byte[] readerPassThruMode(byte b, byte b2) throws RemoteException;

    int selectUicc(int i) throws RemoteException;

    int setConfig(String str, String str2) throws RemoteException;

    int setFieldDetectMode(boolean z) throws RemoteException;

    void startPoll(String str) throws RemoteException;

    void stopPoll(String str, int i) throws RemoteException;

    byte[] transceiveAppData(byte[] bArr) throws RemoteException;

    int updateServiceState(int i, Map map) throws RemoteException;

    public static class Default implements INxpNfcAdapter {
        @Override // com.nxp.nfc.INxpNfcAdapter
        public void DefaultRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void MifareDesfireRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void MifareCLTRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public List<NfcAidServiceInfo> getServicesAidInfo(int userId, String category) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int[] getActiveSecureElementList(String pkg) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface() throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int mPOSSetReaderMode(String pkg, boolean on) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public boolean mPOSGetReaderMode(String pkg) throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void stopPoll(String pkg, int mode) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void changeDiscoveryTech(IBinder binder, int pollTech, int listenTech) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public void startPoll(String pkg) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public byte[] getFWVersion() throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public byte[] readerPassThruMode(byte status, byte modulationTyp) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public byte[] transceiveAppData(byte[] data) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int setConfig(String configs, String pkg) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int selectUicc(int uiccSlot) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getMaxAidRoutingTableSize() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getCommittedAidRoutingTableSize() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int getSelectedUicc() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int updateServiceState(int userId, Map serviceState) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int activateSeInterface() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int deactivateSeInterface() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int setFieldDetectMode(boolean mode) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public boolean isFieldDetectEnabled() throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int doWriteT4tData(byte[] fileId, byte[] data, int length) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public byte[] doReadT4tData(byte[] fileId) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int nfcSelfTest(int type) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int changeRfParams(byte[] data, boolean lastCMD) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapter
        public int changeRfParamsByConfig(byte[] data) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INxpNfcAdapter {
        private static final String DESCRIPTOR = "com.nxp.nfc.INxpNfcAdapter";
        static final int TRANSACTION_DefaultRouteSet = 1;
        static final int TRANSACTION_MifareCLTRouteSet = 3;
        static final int TRANSACTION_MifareDesfireRouteSet = 2;
        static final int TRANSACTION_activateSeInterface = 21;
        static final int TRANSACTION_changeDiscoveryTech = 10;
        static final int TRANSACTION_changeRfParams = 28;
        static final int TRANSACTION_changeRfParamsByConfig = 29;
        static final int TRANSACTION_deactivateSeInterface = 22;
        static final int TRANSACTION_doReadT4tData = 26;
        static final int TRANSACTION_doWriteT4tData = 25;
        static final int TRANSACTION_getActiveSecureElementList = 5;
        static final int TRANSACTION_getCommittedAidRoutingTableSize = 18;
        static final int TRANSACTION_getFWVersion = 12;
        static final int TRANSACTION_getMaxAidRoutingTableSize = 17;
        static final int TRANSACTION_getNxpNfcAdapterExtrasInterface = 6;
        static final int TRANSACTION_getSelectedUicc = 19;
        static final int TRANSACTION_getServicesAidInfo = 4;
        static final int TRANSACTION_isFieldDetectEnabled = 24;
        static final int TRANSACTION_mPOSGetReaderMode = 8;
        static final int TRANSACTION_mPOSSetReaderMode = 7;
        static final int TRANSACTION_nfcSelfTest = 27;
        static final int TRANSACTION_readerPassThruMode = 13;
        static final int TRANSACTION_selectUicc = 16;
        static final int TRANSACTION_setConfig = 15;
        static final int TRANSACTION_setFieldDetectMode = 23;
        static final int TRANSACTION_startPoll = 11;
        static final int TRANSACTION_stopPoll = 9;
        static final int TRANSACTION_transceiveAppData = 14;
        static final int TRANSACTION_updateServiceState = 20;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INxpNfcAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INxpNfcAdapter)) {
                return new Proxy(obj);
            }
            return (INxpNfcAdapter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        boolean _arg12 = data.readInt() != 0;
                        boolean _arg2 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        DefaultRouteSet(_arg0, _arg12, _arg2, _arg1);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        boolean _arg13 = data.readInt() != 0;
                        boolean _arg22 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        MifareDesfireRouteSet(_arg02, _arg13, _arg22, _arg1);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        boolean _arg14 = data.readInt() != 0;
                        boolean _arg23 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        MifareCLTRouteSet(_arg03, _arg14, _arg23, _arg1);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        List<NfcAidServiceInfo> _result = getServicesAidInfo(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case TRANSACTION_getActiveSecureElementList /* 5 */:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result2 = getActiveSecureElementList(data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result2);
                        return true;
                    case TRANSACTION_getNxpNfcAdapterExtrasInterface /* 6 */:
                        data.enforceInterface(DESCRIPTOR);
                        INxpNfcAdapterExtras _result3 = getNxpNfcAdapterExtrasInterface();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result3 != null ? _result3.asBinder() : null);
                        return true;
                    case TRANSACTION_mPOSSetReaderMode /* 7 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        int _result4 = mPOSSetReaderMode(_arg04, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case TRANSACTION_mPOSGetReaderMode /* 8 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean mPOSGetReaderMode = mPOSGetReaderMode(data.readString());
                        reply.writeNoException();
                        reply.writeInt(mPOSGetReaderMode ? 1 : 0);
                        return true;
                    case TRANSACTION_stopPoll /* 9 */:
                        data.enforceInterface(DESCRIPTOR);
                        stopPoll(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_changeDiscoveryTech /* 10 */:
                        data.enforceInterface(DESCRIPTOR);
                        changeDiscoveryTech(data.readStrongBinder(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_startPoll /* 11 */:
                        data.enforceInterface(DESCRIPTOR);
                        startPoll(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getFWVersion /* 12 */:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result5 = getFWVersion();
                        reply.writeNoException();
                        reply.writeByteArray(_result5);
                        return true;
                    case TRANSACTION_readerPassThruMode /* 13 */:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result6 = readerPassThruMode(data.readByte(), data.readByte());
                        reply.writeNoException();
                        reply.writeByteArray(_result6);
                        return true;
                    case TRANSACTION_transceiveAppData /* 14 */:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result7 = transceiveAppData(data.createByteArray());
                        reply.writeNoException();
                        reply.writeByteArray(_result7);
                        return true;
                    case TRANSACTION_setConfig /* 15 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = setConfig(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case TRANSACTION_selectUicc /* 16 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = selectUicc(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case TRANSACTION_getMaxAidRoutingTableSize /* 17 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getMaxAidRoutingTableSize();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case TRANSACTION_getCommittedAidRoutingTableSize /* 18 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getCommittedAidRoutingTableSize();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case TRANSACTION_getSelectedUicc /* 19 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getSelectedUicc();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case TRANSACTION_updateServiceState /* 20 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = updateServiceState(data.readInt(), data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case TRANSACTION_activateSeInterface /* 21 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = activateSeInterface();
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case TRANSACTION_deactivateSeInterface /* 22 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = deactivateSeInterface();
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case TRANSACTION_setFieldDetectMode /* 23 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        int _result16 = setFieldDetectMode(_arg1);
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case TRANSACTION_isFieldDetectEnabled /* 24 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFieldDetectEnabled = isFieldDetectEnabled();
                        reply.writeNoException();
                        reply.writeInt(isFieldDetectEnabled ? 1 : 0);
                        return true;
                    case TRANSACTION_doWriteT4tData /* 25 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = doWriteT4tData(data.createByteArray(), data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case TRANSACTION_doReadT4tData /* 26 */:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result18 = doReadT4tData(data.createByteArray());
                        reply.writeNoException();
                        reply.writeByteArray(_result18);
                        return true;
                    case TRANSACTION_nfcSelfTest /* 27 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = nfcSelfTest(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case TRANSACTION_changeRfParams /* 28 */:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _arg05 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        int _result20 = changeRfParams(_arg05, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case TRANSACTION_changeRfParamsByConfig /* 29 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = changeRfParamsByConfig(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INxpNfcAdapter {
            public static INxpNfcAdapter sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void DefaultRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    _data.writeInt(fullPower ? 1 : 0);
                    _data.writeInt(lowPower ? 1 : 0);
                    _data.writeInt(noPower ? 1 : 0);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().DefaultRouteSet(routeLoc, fullPower, lowPower, noPower);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void MifareDesfireRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    int i = 1;
                    _data.writeInt(fullPower ? 1 : 0);
                    _data.writeInt(lowPower ? 1 : 0);
                    if (!noPower) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().MifareDesfireRouteSet(routeLoc, fullPower, lowPower, noPower);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void MifareCLTRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(routeLoc);
                    int i = 1;
                    _data.writeInt(fullPower ? 1 : 0);
                    _data.writeInt(lowPower ? 1 : 0);
                    if (!noPower) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().MifareCLTRouteSet(routeLoc, fullPower, lowPower, noPower);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public List<NfcAidServiceInfo> getServicesAidInfo(int userId, String category) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(category);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServicesAidInfo(userId, category);
                    }
                    _reply.readException();
                    List<NfcAidServiceInfo> _result = _reply.createTypedArrayList(NfcAidServiceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int[] getActiveSecureElementList(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getActiveSecureElementList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveSecureElementList(pkg);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getNxpNfcAdapterExtrasInterface, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNxpNfcAdapterExtrasInterface();
                    }
                    _reply.readException();
                    INxpNfcAdapterExtras _result = INxpNfcAdapterExtras.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int mPOSSetReaderMode(String pkg, boolean on) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(on ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_mPOSSetReaderMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().mPOSSetReaderMode(pkg, on);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public boolean mPOSGetReaderMode(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_mPOSGetReaderMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().mPOSGetReaderMode(pkg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void stopPoll(String pkg, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(Stub.TRANSACTION_stopPoll, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopPoll(pkg, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void changeDiscoveryTech(IBinder binder, int pollTech, int listenTech) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    _data.writeInt(pollTech);
                    _data.writeInt(listenTech);
                    if (this.mRemote.transact(Stub.TRANSACTION_changeDiscoveryTech, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().changeDiscoveryTech(binder, pollTech, listenTech);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public void startPoll(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (this.mRemote.transact(Stub.TRANSACTION_startPoll, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startPoll(pkg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public byte[] getFWVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getFWVersion, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFWVersion();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public byte[] readerPassThruMode(byte status, byte modulationTyp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByte(status);
                    _data.writeByte(modulationTyp);
                    if (!this.mRemote.transact(Stub.TRANSACTION_readerPassThruMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readerPassThruMode(status, modulationTyp);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public byte[] transceiveAppData(byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    if (!this.mRemote.transact(Stub.TRANSACTION_transceiveAppData, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().transceiveAppData(data);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int setConfig(String configs, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(configs);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setConfig, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setConfig(configs, pkg);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int selectUicc(int uiccSlot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uiccSlot);
                    if (!this.mRemote.transact(Stub.TRANSACTION_selectUicc, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().selectUicc(uiccSlot);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getMaxAidRoutingTableSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getMaxAidRoutingTableSize, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxAidRoutingTableSize();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getCommittedAidRoutingTableSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCommittedAidRoutingTableSize, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCommittedAidRoutingTableSize();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int getSelectedUicc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSelectedUicc, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSelectedUicc();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int updateServiceState(int userId, Map serviceState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeMap(serviceState);
                    if (!this.mRemote.transact(Stub.TRANSACTION_updateServiceState, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateServiceState(userId, serviceState);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int activateSeInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_activateSeInterface, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().activateSeInterface();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int deactivateSeInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_deactivateSeInterface, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deactivateSeInterface();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int setFieldDetectMode(boolean mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setFieldDetectMode, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setFieldDetectMode(mode);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public boolean isFieldDetectEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isFieldDetectEnabled, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFieldDetectEnabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int doWriteT4tData(byte[] fileId, byte[] data, int length) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(fileId);
                    _data.writeByteArray(data);
                    _data.writeInt(length);
                    if (!this.mRemote.transact(Stub.TRANSACTION_doWriteT4tData, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().doWriteT4tData(fileId, data, length);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public byte[] doReadT4tData(byte[] fileId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(fileId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_doReadT4tData, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().doReadT4tData(fileId);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int nfcSelfTest(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(Stub.TRANSACTION_nfcSelfTest, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().nfcSelfTest(type);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int changeRfParams(byte[] data, boolean lastCMD) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    _data.writeInt(lastCMD ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_changeRfParams, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().changeRfParams(data, lastCMD);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapter
            public int changeRfParamsByConfig(byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    if (!this.mRemote.transact(Stub.TRANSACTION_changeRfParamsByConfig, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().changeRfParamsByConfig(data);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INxpNfcAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INxpNfcAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
