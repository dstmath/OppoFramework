package com.mediatek.internal.telephony.phb;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IMtkIccPhoneBook extends IInterface {
    boolean addContactToGroup(int i, int i2, int i3) throws RemoteException;

    int colorAddAdnRecordsInEfBySearchExUsingSubId(int i, int i2, String str, String str2, String str3, String str4, String str5, String str6, String str7) throws RemoteException;

    int colorGetAdnEmailLenUsingSubId(int i) throws RemoteException;

    int colorGetSimNameLenUsingSubId(int i) throws RemoteException;

    int colorGetSimPhonebookAllSpace(int i) throws RemoteException;

    int colorGetSimPhonebookUsedSpace(int i) throws RemoteException;

    boolean colorISPhoneBookReady(int i) throws RemoteException;

    boolean colorIsPhoneBookPbrExist(int i) throws RemoteException;

    boolean colorUpdateAdnRecordsInEfByIndexExUsingSubId(int i, int i2, String str, String str2, String str3, int i3, String str4, String str5) throws RemoteException;

    int[] getAdnRecordsCapacity() throws RemoteException;

    int[] getAdnRecordsCapacityForSubscriber(int i) throws RemoteException;

    List<MtkAdnRecord> getAdnRecordsInEf(int i) throws RemoteException;

    List<MtkAdnRecord> getAdnRecordsInEfForSubscriber(int i, int i2) throws RemoteException;

    int getAnrCount(int i) throws RemoteException;

    int getEmailCount(int i) throws RemoteException;

    UsimPBMemInfo[] getPhonebookMemStorageExt(int i) throws RemoteException;

    int getSneRecordLen(int i) throws RemoteException;

    int getUpbDone(int i) throws RemoteException;

    String getUsimAasById(int i, int i2) throws RemoteException;

    List<AlphaTag> getUsimAasList(int i) throws RemoteException;

    int getUsimAasMaxCount(int i) throws RemoteException;

    int getUsimAasMaxNameLen(int i) throws RemoteException;

    String getUsimGroupById(int i, int i2) throws RemoteException;

    List<UsimGroup> getUsimGroups(int i) throws RemoteException;

    int getUsimGrpMaxCount(int i) throws RemoteException;

    int getUsimGrpMaxNameLen(int i) throws RemoteException;

    int hasExistGroup(int i, String str) throws RemoteException;

    boolean hasSne(int i) throws RemoteException;

    int insertUsimAas(int i, String str) throws RemoteException;

    int insertUsimGroup(int i, String str) throws RemoteException;

    boolean isAdnAccessible(int i) throws RemoteException;

    boolean isPhbReady(int i) throws RemoteException;

    boolean moveContactFromGroupsToGroups(int i, int i2, int[] iArr, int[] iArr2) throws RemoteException;

    boolean removeContactFromGroup(int i, int i2, int i3) throws RemoteException;

    boolean removeUsimAasById(int i, int i2, int i3) throws RemoteException;

    boolean removeUsimGroupById(int i, int i2) throws RemoteException;

    int updateAdnRecordsInEfByIndexWithError(int i, int i2, String str, String str2, int i3, String str3) throws RemoteException;

    int updateAdnRecordsInEfBySearchWithError(int i, int i2, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    boolean updateContactToGroups(int i, int i2, int[] iArr) throws RemoteException;

    boolean updateUsimAas(int i, int i2, int i3, String str) throws RemoteException;

    int updateUsimGroup(int i, int i2, String str) throws RemoteException;

    int updateUsimPBRecordsByIndexWithError(int i, int i2, MtkAdnRecord mtkAdnRecord, int i3) throws RemoteException;

    int updateUsimPBRecordsBySearchWithError(int i, int i2, MtkAdnRecord mtkAdnRecord, MtkAdnRecord mtkAdnRecord2) throws RemoteException;

    int updateUsimPBRecordsInEfByIndexWithError(int i, int i2, String str, String str2, String str3, String str4, String[] strArr, int i3) throws RemoteException;

    int updateUsimPBRecordsInEfBySearchWithError(int i, int i2, String str, String str2, String str3, String str4, String[] strArr, String str5, String str6, String str7, String str8, String[] strArr2) throws RemoteException;

    public static class Default implements IMtkIccPhoneBook {
        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public List<MtkAdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public List<MtkAdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int updateAdnRecordsInEfBySearchWithError(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int updateUsimPBRecordsInEfBySearchWithError(int subId, int efid, String oldTag, String oldPhoneNumber, String oldAnr, String oldGrpIds, String[] oldEmails, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int updateAdnRecordsInEfByIndexWithError(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int updateUsimPBRecordsInEfByIndexWithError(int subId, int efid, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails, int index) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int updateUsimPBRecordsByIndexWithError(int subId, int efid, MtkAdnRecord record, int index) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int updateUsimPBRecordsBySearchWithError(int subId, int efid, MtkAdnRecord oldAdn, MtkAdnRecord newAdn) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean isPhbReady(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public List<UsimGroup> getUsimGroups(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public String getUsimGroupById(int subId, int nGasId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean removeUsimGroupById(int subId, int nGasId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int insertUsimGroup(int subId, String grpName) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int updateUsimGroup(int subId, int nGasId, String grpName) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean addContactToGroup(int subId, int adnIndex, int grpIndex) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean removeContactFromGroup(int subId, int adnIndex, int grpIndex) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean updateContactToGroups(int subId, int adnIndex, int[] grpIdList) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean moveContactFromGroupsToGroups(int subId, int adnIndex, int[] fromGrpIdList, int[] toGrpIdList) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int hasExistGroup(int subId, String grpName) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int getUsimGrpMaxNameLen(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int getUsimGrpMaxCount(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public List<AlphaTag> getUsimAasList(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public String getUsimAasById(int subId, int index) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int insertUsimAas(int subId, String aasName) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int getAnrCount(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int getEmailCount(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int getUsimAasMaxCount(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int getUsimAasMaxNameLen(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean updateUsimAas(int subId, int index, int pbrIndex, String aasName) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean removeUsimAasById(int subId, int index, int pbrIndex) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean hasSne(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int getSneRecordLen(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean isAdnAccessible(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public UsimPBMemInfo[] getPhonebookMemStorageExt(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int getUpbDone(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int[] getAdnRecordsCapacity() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int[] getAdnRecordsCapacityForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int colorGetSimNameLenUsingSubId(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int colorAddAdnRecordsInEfBySearchExUsingSubId(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber1, String newPhoneNumber2, String pin2, String email) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean colorUpdateAdnRecordsInEfByIndexExUsingSubId(int subId, int efid, String newTag, String newPhoneNumber1, String newPhoneNumber2, int index, String pin2, String email) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean colorISPhoneBookReady(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public boolean colorIsPhoneBookPbrExist(int subId) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int colorGetAdnEmailLenUsingSubId(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int colorGetSimPhonebookAllSpace(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
        public int colorGetSimPhonebookUsedSpace(int subId) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkIccPhoneBook {
        private static final String DESCRIPTOR = "com.mediatek.internal.telephony.phb.IMtkIccPhoneBook";
        static final int TRANSACTION_addContactToGroup = 15;
        static final int TRANSACTION_colorAddAdnRecordsInEfBySearchExUsingSubId = 39;
        static final int TRANSACTION_colorGetAdnEmailLenUsingSubId = 43;
        static final int TRANSACTION_colorGetSimNameLenUsingSubId = 38;
        static final int TRANSACTION_colorGetSimPhonebookAllSpace = 44;
        static final int TRANSACTION_colorGetSimPhonebookUsedSpace = 45;
        static final int TRANSACTION_colorISPhoneBookReady = 41;
        static final int TRANSACTION_colorIsPhoneBookPbrExist = 42;
        static final int TRANSACTION_colorUpdateAdnRecordsInEfByIndexExUsingSubId = 40;
        static final int TRANSACTION_getAdnRecordsCapacity = 36;
        static final int TRANSACTION_getAdnRecordsCapacityForSubscriber = 37;
        static final int TRANSACTION_getAdnRecordsInEf = 1;
        static final int TRANSACTION_getAdnRecordsInEfForSubscriber = 2;
        static final int TRANSACTION_getAnrCount = 25;
        static final int TRANSACTION_getEmailCount = 26;
        static final int TRANSACTION_getPhonebookMemStorageExt = 34;
        static final int TRANSACTION_getSneRecordLen = 32;
        static final int TRANSACTION_getUpbDone = 35;
        static final int TRANSACTION_getUsimAasById = 23;
        static final int TRANSACTION_getUsimAasList = 22;
        static final int TRANSACTION_getUsimAasMaxCount = 27;
        static final int TRANSACTION_getUsimAasMaxNameLen = 28;
        static final int TRANSACTION_getUsimGroupById = 11;
        static final int TRANSACTION_getUsimGroups = 10;
        static final int TRANSACTION_getUsimGrpMaxCount = 21;
        static final int TRANSACTION_getUsimGrpMaxNameLen = 20;
        static final int TRANSACTION_hasExistGroup = 19;
        static final int TRANSACTION_hasSne = 31;
        static final int TRANSACTION_insertUsimAas = 24;
        static final int TRANSACTION_insertUsimGroup = 13;
        static final int TRANSACTION_isAdnAccessible = 33;
        static final int TRANSACTION_isPhbReady = 9;
        static final int TRANSACTION_moveContactFromGroupsToGroups = 18;
        static final int TRANSACTION_removeContactFromGroup = 16;
        static final int TRANSACTION_removeUsimAasById = 30;
        static final int TRANSACTION_removeUsimGroupById = 12;
        static final int TRANSACTION_updateAdnRecordsInEfByIndexWithError = 5;
        static final int TRANSACTION_updateAdnRecordsInEfBySearchWithError = 3;
        static final int TRANSACTION_updateContactToGroups = 17;
        static final int TRANSACTION_updateUsimAas = 29;
        static final int TRANSACTION_updateUsimGroup = 14;
        static final int TRANSACTION_updateUsimPBRecordsByIndexWithError = 7;
        static final int TRANSACTION_updateUsimPBRecordsBySearchWithError = 8;
        static final int TRANSACTION_updateUsimPBRecordsInEfByIndexWithError = 6;
        static final int TRANSACTION_updateUsimPBRecordsInEfBySearchWithError = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkIccPhoneBook asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkIccPhoneBook)) {
                return new Proxy(obj);
            }
            return (IMtkIccPhoneBook) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            MtkAdnRecord _arg2;
            MtkAdnRecord _arg22;
            MtkAdnRecord _arg3;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        List<MtkAdnRecord> _result = getAdnRecordsInEf(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List<MtkAdnRecord> _result2 = getAdnRecordsInEfForSubscriber(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = updateAdnRecordsInEfBySearchWithError(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = updateUsimPBRecordsInEfBySearchWithError(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.createStringArray(), data.readString(), data.readString(), data.readString(), data.readString(), data.createStringArray());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = updateAdnRecordsInEfByIndexWithError(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = updateUsimPBRecordsInEfByIndexWithError(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.createStringArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = MtkAdnRecord.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result7 = updateUsimPBRecordsByIndexWithError(_arg0, _arg1, _arg2, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = MtkAdnRecord.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = MtkAdnRecord.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        int _result8 = updateUsimPBRecordsBySearchWithError(_arg02, _arg12, _arg22, _arg3);
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPhbReady = isPhbReady(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isPhbReady ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        List<UsimGroup> _result9 = getUsimGroups(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result9);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _result10 = getUsimGroupById(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result10);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeUsimGroupById = removeUsimGroupById(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeUsimGroupById ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = insertUsimGroup(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = updateUsimGroup(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addContactToGroup = addContactToGroup(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(addContactToGroup ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeContactFromGroup = removeContactFromGroup(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeContactFromGroup ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateContactToGroups = updateContactToGroups(data.readInt(), data.readInt(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(updateContactToGroups ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean moveContactFromGroupsToGroups = moveContactFromGroupsToGroups(data.readInt(), data.readInt(), data.createIntArray(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(moveContactFromGroupsToGroups ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = hasExistGroup(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getUsimGrpMaxNameLen(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = getUsimGrpMaxCount(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case TRANSACTION_getUsimAasList /*{ENCODED_INT: 22}*/:
                        data.enforceInterface(DESCRIPTOR);
                        List<AlphaTag> _result16 = getUsimAasList(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result16);
                        return true;
                    case TRANSACTION_getUsimAasById /*{ENCODED_INT: 23}*/:
                        data.enforceInterface(DESCRIPTOR);
                        String _result17 = getUsimAasById(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result17);
                        return true;
                    case TRANSACTION_insertUsimAas /*{ENCODED_INT: 24}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = insertUsimAas(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case TRANSACTION_getAnrCount /*{ENCODED_INT: 25}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = getAnrCount(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case TRANSACTION_getEmailCount /*{ENCODED_INT: 26}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = getEmailCount(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case TRANSACTION_getUsimAasMaxCount /*{ENCODED_INT: 27}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result21 = getUsimAasMaxCount(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case TRANSACTION_getUsimAasMaxNameLen /*{ENCODED_INT: 28}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result22 = getUsimAasMaxNameLen(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case TRANSACTION_updateUsimAas /*{ENCODED_INT: 29}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateUsimAas = updateUsimAas(data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateUsimAas ? 1 : 0);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeUsimAasById = removeUsimAasById(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeUsimAasById ? 1 : 0);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasSne = hasSne(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasSne ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = getSneRecordLen(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAdnAccessible = isAdnAccessible(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isAdnAccessible ? 1 : 0);
                        return true;
                    case TRANSACTION_getPhonebookMemStorageExt /*{ENCODED_INT: 34}*/:
                        data.enforceInterface(DESCRIPTOR);
                        UsimPBMemInfo[] _result24 = getPhonebookMemStorageExt(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedArray(_result24, 1);
                        return true;
                    case TRANSACTION_getUpbDone /*{ENCODED_INT: 35}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result25 = getUpbDone(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case TRANSACTION_getAdnRecordsCapacity /*{ENCODED_INT: 36}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result26 = getAdnRecordsCapacity();
                        reply.writeNoException();
                        reply.writeIntArray(_result26);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result27 = getAdnRecordsCapacityForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result27);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int _result28 = colorGetSimNameLenUsingSubId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        int _result29 = colorAddAdnRecordsInEfBySearchExUsingSubId(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        boolean colorUpdateAdnRecordsInEfByIndexExUsingSubId = colorUpdateAdnRecordsInEfByIndexExUsingSubId(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(colorUpdateAdnRecordsInEfByIndexExUsingSubId ? 1 : 0);
                        return true;
                    case TRANSACTION_colorISPhoneBookReady /*{ENCODED_INT: 41}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean colorISPhoneBookReady = colorISPhoneBookReady(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(colorISPhoneBookReady ? 1 : 0);
                        return true;
                    case TRANSACTION_colorIsPhoneBookPbrExist /*{ENCODED_INT: 42}*/:
                        data.enforceInterface(DESCRIPTOR);
                        boolean colorIsPhoneBookPbrExist = colorIsPhoneBookPbrExist(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(colorIsPhoneBookPbrExist ? 1 : 0);
                        return true;
                    case TRANSACTION_colorGetAdnEmailLenUsingSubId /*{ENCODED_INT: 43}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result30 = colorGetAdnEmailLenUsingSubId(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case TRANSACTION_colorGetSimPhonebookAllSpace /*{ENCODED_INT: 44}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result31 = colorGetSimPhonebookAllSpace(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case TRANSACTION_colorGetSimPhonebookUsedSpace /*{ENCODED_INT: 45}*/:
                        data.enforceInterface(DESCRIPTOR);
                        int _result32 = colorGetSimPhonebookUsedSpace(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result32);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMtkIccPhoneBook {
            public static IMtkIccPhoneBook sDefaultImpl;
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public List<MtkAdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsInEf(efid);
                    }
                    _reply.readException();
                    List<MtkAdnRecord> _result = _reply.createTypedArrayList(MtkAdnRecord.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public List<MtkAdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsInEfForSubscriber(subId, efid);
                    }
                    _reply.readException();
                    List<MtkAdnRecord> _result = _reply.createTypedArrayList(MtkAdnRecord.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int updateAdnRecordsInEfBySearchWithError(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
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
                        _data.writeInt(efid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(oldTag);
                        try {
                            _data.writeString(oldPhoneNumber);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(newTag);
                            _data.writeString(newPhoneNumber);
                            _data.writeString(pin2);
                            if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int updateAdnRecordsInEfBySearchWithError = Stub.getDefaultImpl().updateAdnRecordsInEfBySearchWithError(subId, efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateAdnRecordsInEfBySearchWithError;
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
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int updateUsimPBRecordsInEfBySearchWithError(int subId, int efid, String oldTag, String oldPhoneNumber, String oldAnr, String oldGrpIds, String[] oldEmails, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    _data.writeString(oldTag);
                    _data.writeString(oldPhoneNumber);
                    _data.writeString(oldAnr);
                    _data.writeString(oldGrpIds);
                    _data.writeStringArray(oldEmails);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeString(newAnr);
                    _data.writeString(newGrpIds);
                    _data.writeStringArray(newEmails);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateUsimPBRecordsInEfBySearchWithError(subId, efid, oldTag, oldPhoneNumber, oldAnr, oldGrpIds, oldEmails, newTag, newPhoneNumber, newAnr, newGrpIds, newEmails);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int updateAdnRecordsInEfByIndexWithError(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
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
                        _data.writeInt(efid);
                        try {
                            _data.writeString(newTag);
                            try {
                                _data.writeString(newPhoneNumber);
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
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(index);
                        try {
                            _data.writeString(pin2);
                            if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int updateAdnRecordsInEfByIndexWithError = Stub.getDefaultImpl().updateAdnRecordsInEfByIndexWithError(subId, efid, newTag, newPhoneNumber, index, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateAdnRecordsInEfByIndexWithError;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int updateUsimPBRecordsInEfByIndexWithError(int subId, int efid, String newTag, String newPhoneNumber, String newAnr, String newGrpIds, String[] newEmails, int index) throws RemoteException {
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
                        _data.writeInt(efid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(newTag);
                        try {
                            _data.writeString(newPhoneNumber);
                            _data.writeString(newAnr);
                            _data.writeString(newGrpIds);
                            _data.writeStringArray(newEmails);
                            _data.writeInt(index);
                            if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int updateUsimPBRecordsInEfByIndexWithError = Stub.getDefaultImpl().updateUsimPBRecordsInEfByIndexWithError(subId, efid, newTag, newPhoneNumber, newAnr, newGrpIds, newEmails, index);
                            _reply.recycle();
                            _data.recycle();
                            return updateUsimPBRecordsInEfByIndexWithError;
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int updateUsimPBRecordsByIndexWithError(int subId, int efid, MtkAdnRecord record, int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    if (record != null) {
                        _data.writeInt(1);
                        record.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(index);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateUsimPBRecordsByIndexWithError(subId, efid, record, index);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int updateUsimPBRecordsBySearchWithError(int subId, int efid, MtkAdnRecord oldAdn, MtkAdnRecord newAdn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    if (oldAdn != null) {
                        _data.writeInt(1);
                        oldAdn.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (newAdn != null) {
                        _data.writeInt(1);
                        newAdn.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateUsimPBRecordsBySearchWithError(subId, efid, oldAdn, newAdn);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean isPhbReady(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPhbReady(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public List<UsimGroup> getUsimGroups(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimGroups(subId);
                    }
                    _reply.readException();
                    List<UsimGroup> _result = _reply.createTypedArrayList(UsimGroup.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public String getUsimGroupById(int subId, int nGasId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(nGasId);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimGroupById(subId, nGasId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean removeUsimGroupById(int subId, int nGasId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(nGasId);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeUsimGroupById(subId, nGasId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int insertUsimGroup(int subId, String grpName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(grpName);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().insertUsimGroup(subId, grpName);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int updateUsimGroup(int subId, int nGasId, String grpName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(nGasId);
                    _data.writeString(grpName);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateUsimGroup(subId, nGasId, grpName);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean addContactToGroup(int subId, int adnIndex, int grpIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(adnIndex);
                    _data.writeInt(grpIndex);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addContactToGroup(subId, adnIndex, grpIndex);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean removeContactFromGroup(int subId, int adnIndex, int grpIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(adnIndex);
                    _data.writeInt(grpIndex);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeContactFromGroup(subId, adnIndex, grpIndex);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean updateContactToGroups(int subId, int adnIndex, int[] grpIdList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(adnIndex);
                    _data.writeIntArray(grpIdList);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateContactToGroups(subId, adnIndex, grpIdList);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean moveContactFromGroupsToGroups(int subId, int adnIndex, int[] fromGrpIdList, int[] toGrpIdList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(adnIndex);
                    _data.writeIntArray(fromGrpIdList);
                    _data.writeIntArray(toGrpIdList);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().moveContactFromGroupsToGroups(subId, adnIndex, fromGrpIdList, toGrpIdList);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int hasExistGroup(int subId, String grpName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(grpName);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasExistGroup(subId, grpName);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int getUsimGrpMaxNameLen(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimGrpMaxNameLen(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int getUsimGrpMaxCount(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimGrpMaxCount(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public List<AlphaTag> getUsimAasList(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getUsimAasList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimAasList(subId);
                    }
                    _reply.readException();
                    List<AlphaTag> _result = _reply.createTypedArrayList(AlphaTag.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public String getUsimAasById(int subId, int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(index);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getUsimAasById, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimAasById(subId, index);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int insertUsimAas(int subId, String aasName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(aasName);
                    if (!this.mRemote.transact(Stub.TRANSACTION_insertUsimAas, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().insertUsimAas(subId, aasName);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int getAnrCount(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getAnrCount, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAnrCount(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int getEmailCount(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getEmailCount, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEmailCount(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int getUsimAasMaxCount(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getUsimAasMaxCount, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimAasMaxCount(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int getUsimAasMaxNameLen(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getUsimAasMaxNameLen, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimAasMaxNameLen(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean updateUsimAas(int subId, int index, int pbrIndex, String aasName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(index);
                    _data.writeInt(pbrIndex);
                    _data.writeString(aasName);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_updateUsimAas, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateUsimAas(subId, index, pbrIndex, aasName);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean removeUsimAasById(int subId, int index, int pbrIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(index);
                    _data.writeInt(pbrIndex);
                    boolean _result = false;
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeUsimAasById(subId, index, pbrIndex);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean hasSne(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasSne(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int getSneRecordLen(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSneRecordLen(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean isAdnAccessible(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAdnAccessible(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public UsimPBMemInfo[] getPhonebookMemStorageExt(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getPhonebookMemStorageExt, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPhonebookMemStorageExt(subId);
                    }
                    _reply.readException();
                    UsimPBMemInfo[] _result = (UsimPBMemInfo[]) _reply.createTypedArray(UsimPBMemInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int getUpbDone(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getUpbDone, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUpbDone(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int[] getAdnRecordsCapacity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getAdnRecordsCapacity, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsCapacity();
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int[] getAdnRecordsCapacityForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsCapacityForSubscriber(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int colorGetSimNameLenUsingSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().colorGetSimNameLenUsingSubId(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int colorAddAdnRecordsInEfBySearchExUsingSubId(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber1, String newPhoneNumber2, String pin2, String email) throws RemoteException {
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
                        _data.writeInt(efid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(oldTag);
                        _data.writeString(oldPhoneNumber);
                        _data.writeString(newTag);
                        _data.writeString(newPhoneNumber1);
                        _data.writeString(newPhoneNumber2);
                        _data.writeString(pin2);
                        _data.writeString(email);
                        if (this.mRemote.transact(39, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int colorAddAdnRecordsInEfBySearchExUsingSubId = Stub.getDefaultImpl().colorAddAdnRecordsInEfBySearchExUsingSubId(subId, efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber1, newPhoneNumber2, pin2, email);
                        _reply.recycle();
                        _data.recycle();
                        return colorAddAdnRecordsInEfBySearchExUsingSubId;
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean colorUpdateAdnRecordsInEfByIndexExUsingSubId(int subId, int efid, String newTag, String newPhoneNumber1, String newPhoneNumber2, int index, String pin2, String email) throws RemoteException {
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
                        _data.writeInt(efid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(newTag);
                        try {
                            _data.writeString(newPhoneNumber1);
                            _data.writeString(newPhoneNumber2);
                            _data.writeInt(index);
                            _data.writeString(pin2);
                            _data.writeString(email);
                            boolean _result = false;
                            if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean colorUpdateAdnRecordsInEfByIndexExUsingSubId = Stub.getDefaultImpl().colorUpdateAdnRecordsInEfByIndexExUsingSubId(subId, efid, newTag, newPhoneNumber1, newPhoneNumber2, index, pin2, email);
                            _reply.recycle();
                            _data.recycle();
                            return colorUpdateAdnRecordsInEfByIndexExUsingSubId;
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean colorISPhoneBookReady(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_colorISPhoneBookReady, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().colorISPhoneBookReady(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public boolean colorIsPhoneBookPbrExist(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_colorIsPhoneBookPbrExist, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().colorIsPhoneBookPbrExist(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int colorGetAdnEmailLenUsingSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_colorGetAdnEmailLenUsingSubId, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().colorGetAdnEmailLenUsingSubId(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int colorGetSimPhonebookAllSpace(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_colorGetSimPhonebookAllSpace, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().colorGetSimPhonebookAllSpace(subId);
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

            @Override // com.mediatek.internal.telephony.phb.IMtkIccPhoneBook
            public int colorGetSimPhonebookUsedSpace(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_colorGetSimPhonebookUsedSpace, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().colorGetSimPhonebookUsedSpace(subId);
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

        public static boolean setDefaultImpl(IMtkIccPhoneBook impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkIccPhoneBook getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
