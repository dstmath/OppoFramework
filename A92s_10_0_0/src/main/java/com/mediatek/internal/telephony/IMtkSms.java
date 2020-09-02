package com.mediatek.internal.telephony;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.telephony.SmsRawData;
import java.util.List;
import mediatek.telephony.MtkSimSmsInsertStatus;
import mediatek.telephony.MtkSmsParameters;

public interface IMtkSms extends IInterface {
    boolean activateCellBroadcastSmsForSubscriber(int i, boolean z) throws RemoteException;

    int copyTextMessageToIccCardForSubscriber(int i, String str, String str2, String str3, List<String> list, int i2, long j) throws RemoteException;

    List<SmsRawData> getAllMessagesFromIccEfByModeForSubscriber(int i, String str, int i2) throws RemoteException;

    String getCellBroadcastLangsForSubscriber(int i) throws RemoteException;

    String getCellBroadcastRangesForSubscriber(int i) throws RemoteException;

    SmsRawData getMessageFromIccEfForSubscriber(int i, String str, int i2) throws RemoteException;

    String getScAddressForSubscriber(int i) throws RemoteException;

    Bundle getScAddressWithErrorCodeForSubscriber(int i) throws RemoteException;

    MtkSmsParameters getSmsParametersForSubscriber(int i, String str) throws RemoteException;

    MtkIccSmsStorageStatus getSmsSimMemoryStatusForSubscriber(int i, String str) throws RemoteException;

    MtkSimSmsInsertStatus insertRawMessageToIccCardForSubscriber(int i, String str, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    MtkSimSmsInsertStatus insertTextMessageToIccCardForSubscriber(int i, String str, String str2, String str3, List<String> list, int i2, long j) throws RemoteException;

    boolean isSmsReadyForSubscriber(int i) throws RemoteException;

    boolean queryCellBroadcastSmsActivationForSubscriber(int i) throws RemoteException;

    boolean removeCellBroadcastMsgForSubscriber(int i, int i2, int i3) throws RemoteException;

    void sendDataWithOriginalPortForSubscriber(int i, String str, String str2, String str3, int i2, int i3, byte[] bArr, PendingIntent pendingIntent, PendingIntent pendingIntent2) throws RemoteException;

    void sendMultipartTextWithEncodingTypeForSubscriber(int i, String str, String str2, String str3, List<String> list, int i2, List<PendingIntent> list2, List<PendingIntent> list3, boolean z) throws RemoteException;

    void sendMultipartTextWithExtraParamsForSubscriber(int i, String str, String str2, String str3, List<String> list, Bundle bundle, List<PendingIntent> list2, List<PendingIntent> list3, boolean z) throws RemoteException;

    void sendTextWithEncodingTypeForSubscriber(int i, String str, String str2, String str3, String str4, int i2, PendingIntent pendingIntent, PendingIntent pendingIntent2, boolean z) throws RemoteException;

    void sendTextWithExtraParamsForSubscriber(int i, String str, String str2, String str3, String str4, Bundle bundle, PendingIntent pendingIntent, PendingIntent pendingIntent2, boolean z) throws RemoteException;

    boolean setCellBroadcastLangsForSubscriber(int i, String str) throws RemoteException;

    boolean setEtwsConfigForSubscriber(int i, int i2) throws RemoteException;

    boolean setScAddressForSubscriber(int i, String str) throws RemoteException;

    void setSmsMemoryStatusForSubscriber(int i, boolean z) throws RemoteException;

    boolean setSmsParametersForSubscriber(int i, String str, MtkSmsParameters mtkSmsParameters) throws RemoteException;

    public static class Default implements IMtkSms {
        @Override // com.mediatek.internal.telephony.IMtkSms
        public List<SmsRawData> getAllMessagesFromIccEfByModeForSubscriber(int subId, String callingPkg, int mode) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public int copyTextMessageToIccCardForSubscriber(int subId, String callingPkg, String scAddress, String address, List<String> list, int status, long timestamp) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public void sendDataWithOriginalPortForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public boolean isSmsReadyForSubscriber(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public void setSmsMemoryStatusForSubscriber(int subId, boolean status) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public MtkIccSmsStorageStatus getSmsSimMemoryStatusForSubscriber(int subId, String callingPkg) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public void sendTextWithEncodingTypeForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public void sendMultipartTextWithEncodingTypeForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, List<String> list, int encodingType, List<PendingIntent> list2, List<PendingIntent> list3, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public MtkSimSmsInsertStatus insertTextMessageToIccCardForSubscriber(int subId, String callingPkg, String scAddress, String address, List<String> list, int status, long timestamp) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public MtkSimSmsInsertStatus insertRawMessageToIccCardForSubscriber(int subId, String callingPkg, int status, byte[] pdu, byte[] smsc) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public void sendTextWithExtraParamsForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, String text, Bundle extraParams, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public void sendMultipartTextWithExtraParamsForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, List<String> list, Bundle extraParams, List<PendingIntent> list2, List<PendingIntent> list3, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public MtkSmsParameters getSmsParametersForSubscriber(int subId, String callingPkg) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public boolean setSmsParametersForSubscriber(int subId, String callingPkg, MtkSmsParameters params) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public SmsRawData getMessageFromIccEfForSubscriber(int subId, String callingPkg, int index) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public boolean queryCellBroadcastSmsActivationForSubscriber(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public boolean activateCellBroadcastSmsForSubscriber(int subId, boolean activate) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public boolean removeCellBroadcastMsgForSubscriber(int subId, int channelId, int serialId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public boolean setEtwsConfigForSubscriber(int subId, int mode) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public String getCellBroadcastRangesForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public boolean setCellBroadcastLangsForSubscriber(int subId, String lang) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public String getCellBroadcastLangsForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public String getScAddressForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public Bundle getScAddressWithErrorCodeForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSms
        public boolean setScAddressForSubscriber(int subId, String address) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkSms {
        private static final String DESCRIPTOR = "com.mediatek.internal.telephony.IMtkSms";
        static final int TRANSACTION_activateCellBroadcastSmsForSubscriber = 17;
        static final int TRANSACTION_copyTextMessageToIccCardForSubscriber = 2;
        static final int TRANSACTION_getAllMessagesFromIccEfByModeForSubscriber = 1;
        static final int TRANSACTION_getCellBroadcastLangsForSubscriber = 22;
        static final int TRANSACTION_getCellBroadcastRangesForSubscriber = 20;
        static final int TRANSACTION_getMessageFromIccEfForSubscriber = 15;
        static final int TRANSACTION_getScAddressForSubscriber = 23;
        static final int TRANSACTION_getScAddressWithErrorCodeForSubscriber = 24;
        static final int TRANSACTION_getSmsParametersForSubscriber = 13;
        static final int TRANSACTION_getSmsSimMemoryStatusForSubscriber = 6;
        static final int TRANSACTION_insertRawMessageToIccCardForSubscriber = 10;
        static final int TRANSACTION_insertTextMessageToIccCardForSubscriber = 9;
        static final int TRANSACTION_isSmsReadyForSubscriber = 4;
        static final int TRANSACTION_queryCellBroadcastSmsActivationForSubscriber = 16;
        static final int TRANSACTION_removeCellBroadcastMsgForSubscriber = 18;
        static final int TRANSACTION_sendDataWithOriginalPortForSubscriber = 3;
        static final int TRANSACTION_sendMultipartTextWithEncodingTypeForSubscriber = 8;
        static final int TRANSACTION_sendMultipartTextWithExtraParamsForSubscriber = 12;
        static final int TRANSACTION_sendTextWithEncodingTypeForSubscriber = 7;
        static final int TRANSACTION_sendTextWithExtraParamsForSubscriber = 11;
        static final int TRANSACTION_setCellBroadcastLangsForSubscriber = 21;
        static final int TRANSACTION_setEtwsConfigForSubscriber = 19;
        static final int TRANSACTION_setScAddressForSubscriber = 25;
        static final int TRANSACTION_setSmsMemoryStatusForSubscriber = 5;
        static final int TRANSACTION_setSmsParametersForSubscriber = 14;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkSms asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkSms)) {
                return new Proxy(obj);
            }
            return (IMtkSms) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PendingIntent _arg7;
            PendingIntent _arg8;
            PendingIntent _arg6;
            PendingIntent _arg72;
            Bundle _arg5;
            PendingIntent _arg62;
            PendingIntent _arg73;
            Bundle _arg52;
            MtkSmsParameters _arg2;
            if (code != 1598968902) {
                boolean _arg1 = false;
                boolean _arg12 = false;
                boolean _arg82 = false;
                boolean _arg83 = false;
                boolean _arg84 = false;
                boolean _arg85 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        List<SmsRawData> _result = getAllMessagesFromIccEfByModeForSubscriber(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = copyTextMessageToIccCardForSubscriber(data.readInt(), data.readString(), data.readString(), data.readString(), data.createStringArrayList(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg13 = data.readString();
                        String _arg22 = data.readString();
                        String _arg3 = data.readString();
                        int _arg4 = data.readInt();
                        int _arg53 = data.readInt();
                        byte[] _arg63 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg7 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg7 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg8 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg8 = null;
                        }
                        sendDataWithOriginalPortForSubscriber(_arg0, _arg13, _arg22, _arg3, _arg4, _arg53, _arg63, _arg7, _arg8);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSmsReadyForSubscriber = isSmsReadyForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isSmsReadyForSubscriber ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setSmsMemoryStatusForSubscriber(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        MtkIccSmsStorageStatus _result3 = getSmsSimMemoryStatusForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        String _arg14 = data.readString();
                        String _arg23 = data.readString();
                        String _arg32 = data.readString();
                        String _arg42 = data.readString();
                        int _arg54 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg6 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg72 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg72 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg85 = true;
                        }
                        sendTextWithEncodingTypeForSubscriber(_arg03, _arg14, _arg23, _arg32, _arg42, _arg54, _arg6, _arg72, _arg85);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        String _arg15 = data.readString();
                        String _arg24 = data.readString();
                        String _arg33 = data.readString();
                        List<String> _arg43 = data.createStringArrayList();
                        int _arg55 = data.readInt();
                        List<PendingIntent> _arg64 = data.createTypedArrayList(PendingIntent.CREATOR);
                        List<PendingIntent> _arg74 = data.createTypedArrayList(PendingIntent.CREATOR);
                        if (data.readInt() != 0) {
                            _arg84 = true;
                        }
                        sendMultipartTextWithEncodingTypeForSubscriber(_arg04, _arg15, _arg24, _arg33, _arg43, _arg55, _arg64, _arg74, _arg84);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        MtkSimSmsInsertStatus _result4 = insertTextMessageToIccCardForSubscriber(data.readInt(), data.readString(), data.readString(), data.readString(), data.createStringArrayList(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        MtkSimSmsInsertStatus _result5 = insertRawMessageToIccCardForSubscriber(data.readInt(), data.readString(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        String _arg16 = data.readString();
                        String _arg25 = data.readString();
                        String _arg34 = data.readString();
                        String _arg44 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg62 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg62 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg73 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg73 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg83 = true;
                        }
                        sendTextWithExtraParamsForSubscriber(_arg05, _arg16, _arg25, _arg34, _arg44, _arg5, _arg62, _arg73, _arg83);
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        String _arg17 = data.readString();
                        String _arg26 = data.readString();
                        String _arg35 = data.readString();
                        List<String> _arg45 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg52 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg52 = null;
                        }
                        List<PendingIntent> _arg65 = data.createTypedArrayList(PendingIntent.CREATOR);
                        List<PendingIntent> _arg75 = data.createTypedArrayList(PendingIntent.CREATOR);
                        if (data.readInt() != 0) {
                            _arg82 = true;
                        }
                        sendMultipartTextWithExtraParamsForSubscriber(_arg06, _arg17, _arg26, _arg35, _arg45, _arg52, _arg65, _arg75, _arg82);
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        MtkSmsParameters _result6 = getSmsParametersForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        String _arg18 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = MtkSmsParameters.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        boolean smsParametersForSubscriber = setSmsParametersForSubscriber(_arg07, _arg18, _arg2);
                        reply.writeNoException();
                        reply.writeInt(smsParametersForSubscriber ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        SmsRawData _result7 = getMessageFromIccEfForSubscriber(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean queryCellBroadcastSmsActivationForSubscriber = queryCellBroadcastSmsActivationForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(queryCellBroadcastSmsActivationForSubscriber ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        boolean activateCellBroadcastSmsForSubscriber = activateCellBroadcastSmsForSubscriber(_arg08, _arg12);
                        reply.writeNoException();
                        reply.writeInt(activateCellBroadcastSmsForSubscriber ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeCellBroadcastMsgForSubscriber = removeCellBroadcastMsgForSubscriber(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeCellBroadcastMsgForSubscriber ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean etwsConfigForSubscriber = setEtwsConfigForSubscriber(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(etwsConfigForSubscriber ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _result8 = getCellBroadcastRangesForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result8);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean cellBroadcastLangsForSubscriber = setCellBroadcastLangsForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(cellBroadcastLangsForSubscriber ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        String _result9 = getCellBroadcastLangsForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result9);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _result10 = getScAddressForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result10);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result11 = getScAddressWithErrorCodeForSubscriber(data.readInt());
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_setScAddressForSubscriber /*{ENCODED_INT: 25}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean scAddressForSubscriber = setScAddressForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(scAddressForSubscriber ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMtkSms {
            public static IMtkSms sDefaultImpl;
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

            @Override // com.mediatek.internal.telephony.IMtkSms
            public List<SmsRawData> getAllMessagesFromIccEfByModeForSubscriber(int subId, String callingPkg, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllMessagesFromIccEfByModeForSubscriber(subId, callingPkg, mode);
                    }
                    _reply.readException();
                    List<SmsRawData> _result = _reply.createTypedArrayList(SmsRawData.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public int copyTextMessageToIccCardForSubscriber(int subId, String callingPkg, String scAddress, String address, List<String> text, int status, long timestamp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(scAddress);
                        try {
                            _data.writeString(address);
                            _data.writeStringList(text);
                            _data.writeInt(status);
                            _data.writeLong(timestamp);
                            if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int copyTextMessageToIccCardForSubscriber = Stub.getDefaultImpl().copyTextMessageToIccCardForSubscriber(subId, callingPkg, scAddress, address, text, status, timestamp);
                            _reply.recycle();
                            _data.recycle();
                            return copyTextMessageToIccCardForSubscriber;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public void sendDataWithOriginalPortForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, int destPort, int originalPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                        _data.writeString(callingPkg);
                        _data.writeString(destAddr);
                        _data.writeString(scAddr);
                        _data.writeInt(destPort);
                        _data.writeInt(originalPort);
                        _data.writeByteArray(data);
                        if (sentIntent != null) {
                            _data.writeInt(1);
                            sentIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (deliveryIntent != null) {
                            _data.writeInt(1);
                            deliveryIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().sendDataWithOriginalPortForSubscriber(subId, callingPkg, destAddr, scAddr, destPort, originalPort, data, sentIntent, deliveryIntent);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public boolean isSmsReadyForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSmsReadyForSubscriber(subId);
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

            @Override // com.mediatek.internal.telephony.IMtkSms
            public void setSmsMemoryStatusForSubscriber(int subId, boolean status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(status ? 1 : 0);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSmsMemoryStatusForSubscriber(subId, status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public MtkIccSmsStorageStatus getSmsSimMemoryStatusForSubscriber(int subId, String callingPkg) throws RemoteException {
                MtkIccSmsStorageStatus _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSmsSimMemoryStatusForSubscriber(subId, callingPkg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MtkIccSmsStorageStatus.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public void sendTextWithEncodingTypeForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, String text, int encodingType, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                        _data.writeString(callingPkg);
                        _data.writeString(destAddr);
                        _data.writeString(scAddr);
                        _data.writeString(text);
                        _data.writeInt(encodingType);
                        int i = 1;
                        if (sentIntent != null) {
                            _data.writeInt(1);
                            sentIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (deliveryIntent != null) {
                            _data.writeInt(1);
                            deliveryIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (!persistMessageForNonDefaultSmsApp) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().sendTextWithEncodingTypeForSubscriber(subId, callingPkg, destAddr, scAddr, text, encodingType, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public void sendMultipartTextWithEncodingTypeForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, List<String> parts, int encodingType, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(destAddr);
                        _data.writeString(scAddr);
                        _data.writeStringList(parts);
                        _data.writeInt(encodingType);
                        _data.writeTypedList(sentIntents);
                        _data.writeTypedList(deliveryIntents);
                        _data.writeInt(persistMessageForNonDefaultSmsApp ? 1 : 0);
                        if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().sendMultipartTextWithEncodingTypeForSubscriber(subId, callingPkg, destAddr, scAddr, parts, encodingType, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public MtkSimSmsInsertStatus insertTextMessageToIccCardForSubscriber(int subId, String callingPkg, String scAddress, String address, List<String> text, int status, long timestamp) throws RemoteException {
                MtkSimSmsInsertStatus _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(scAddress);
                        try {
                            _data.writeString(address);
                            _data.writeStringList(text);
                            _data.writeInt(status);
                            _data.writeLong(timestamp);
                            if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = MtkSimSmsInsertStatus.CREATOR.createFromParcel(_reply);
                                } else {
                                    _result = null;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            MtkSimSmsInsertStatus insertTextMessageToIccCardForSubscriber = Stub.getDefaultImpl().insertTextMessageToIccCardForSubscriber(subId, callingPkg, scAddress, address, text, status, timestamp);
                            _reply.recycle();
                            _data.recycle();
                            return insertTextMessageToIccCardForSubscriber;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public MtkSimSmsInsertStatus insertRawMessageToIccCardForSubscriber(int subId, String callingPkg, int status, byte[] pdu, byte[] smsc) throws RemoteException {
                MtkSimSmsInsertStatus _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeInt(status);
                    _data.writeByteArray(pdu);
                    _data.writeByteArray(smsc);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().insertRawMessageToIccCardForSubscriber(subId, callingPkg, status, pdu, smsc);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MtkSimSmsInsertStatus.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public void sendTextWithExtraParamsForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, String text, Bundle extraParams, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeString(destAddr);
                    _data.writeString(scAddr);
                    _data.writeString(text);
                    int i = 1;
                    if (extraParams != null) {
                        _data.writeInt(1);
                        extraParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sentIntent != null) {
                        _data.writeInt(1);
                        sentIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (deliveryIntent != null) {
                        _data.writeInt(1);
                        deliveryIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!persistMessageForNonDefaultSmsApp) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendTextWithExtraParamsForSubscriber(subId, callingPkg, destAddr, scAddr, text, extraParams, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public void sendMultipartTextWithExtraParamsForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, List<String> parts, Bundle extraParams, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                        try {
                            _data.writeString(callingPkg);
                            _data.writeString(destAddr);
                            _data.writeString(scAddr);
                            _data.writeStringList(parts);
                            int i = 1;
                            if (extraParams != null) {
                                _data.writeInt(1);
                                extraParams.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            _data.writeTypedList(sentIntents);
                            _data.writeTypedList(deliveryIntents);
                            if (!persistMessageForNonDefaultSmsApp) {
                                i = 0;
                            }
                            _data.writeInt(i);
                            if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().sendMultipartTextWithExtraParamsForSubscriber(subId, callingPkg, destAddr, scAddr, parts, extraParams, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public MtkSmsParameters getSmsParametersForSubscriber(int subId, String callingPkg) throws RemoteException {
                MtkSmsParameters _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSmsParametersForSubscriber(subId, callingPkg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MtkSmsParameters.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public boolean setSmsParametersForSubscriber(int subId, String callingPkg, MtkSmsParameters params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    boolean _result = true;
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSmsParametersForSubscriber(subId, callingPkg, params);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public SmsRawData getMessageFromIccEfForSubscriber(int subId, String callingPkg, int index) throws RemoteException {
                SmsRawData _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeInt(index);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMessageFromIccEfForSubscriber(subId, callingPkg, index);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (SmsRawData) SmsRawData.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public boolean queryCellBroadcastSmsActivationForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCellBroadcastSmsActivationForSubscriber(subId);
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

            @Override // com.mediatek.internal.telephony.IMtkSms
            public boolean activateCellBroadcastSmsForSubscriber(int subId, boolean activate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = true;
                    _data.writeInt(activate ? 1 : 0);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().activateCellBroadcastSmsForSubscriber(subId, activate);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public boolean removeCellBroadcastMsgForSubscriber(int subId, int channelId, int serialId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(channelId);
                    _data.writeInt(serialId);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeCellBroadcastMsgForSubscriber(subId, channelId, serialId);
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

            @Override // com.mediatek.internal.telephony.IMtkSms
            public boolean setEtwsConfigForSubscriber(int subId, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(mode);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setEtwsConfigForSubscriber(subId, mode);
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

            @Override // com.mediatek.internal.telephony.IMtkSms
            public String getCellBroadcastRangesForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCellBroadcastRangesForSubscriber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public boolean setCellBroadcastLangsForSubscriber(int subId, String lang) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(lang);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setCellBroadcastLangsForSubscriber(subId, lang);
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

            @Override // com.mediatek.internal.telephony.IMtkSms
            public String getCellBroadcastLangsForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCellBroadcastLangsForSubscriber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public String getScAddressForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getScAddressForSubscriber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public Bundle getScAddressWithErrorCodeForSubscriber(int subId) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getScAddressWithErrorCodeForSubscriber(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSms
            public boolean setScAddressForSubscriber(int subId, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(address);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_setScAddressForSubscriber, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setScAddressForSubscriber(subId, address);
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
        }

        public static boolean setDefaultImpl(IMtkSms impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkSms getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
